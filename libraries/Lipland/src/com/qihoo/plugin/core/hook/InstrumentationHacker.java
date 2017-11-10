/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.qihoo.plugin.core.hook;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.text.TextUtils;

import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.base.BaseProxyActivity;
import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.bean.PluginContextInfo;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.bean.StartTimeInfo;
import com.qihoo.plugin.core.ActivityPoolManager;
import com.qihoo.plugin.core.ActivityPoolManager.ProxyInfo;
import com.qihoo.plugin.core.HostApplicationProxy;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.core.ProxyActivityPool;
import com.qihoo.plugin.core.ThreadContextData;
import com.qihoo.plugin.core.TimeStatistics;
import com.qihoo.plugin.util.AndroidUtil;
import com.qihoo.plugin.util.CodeTraceTS;
import com.qihoo.plugin.util.PluginUtil;
import com.qihoo.plugin.util.RefUtil;


/**
 * 
 * 主要用于Activity修改
 * 
 * @author xupengpai
 * @date 2015年11月26日 下午4:12:57
 */
public class InstrumentationHacker extends Instrumentation {

	private final static String TAG = InstrumentationHacker.class
			.getSimpleName();

	// 存放原来的Instrumentation
	private Instrumentation origin;
	private ActivityThread activityThread;

	// 存放正在运行的插件Activity
	private Map<Context, PluginContextInfo> pluginContexts;

	// 存放正在运行的Activity
	private Map<IBinder, WeakReference<Activity>> allActivities;
	
	private Map<Activity, ComponentCallbacks> componentCallbacksCache;

	public ActivityThread getActivityThread() {
		return activityThread;
	}

	public PluginContextInfo getPluginContextInfo(Context context) {
		return pluginContexts.get(context);
	}

	public PluginContextInfo getPluginContextInfoByToken(IBinder token) {
		for (PluginContextInfo pci : pluginContexts.values()) {
			if (pci.context != null
					&& RefUtil.instanceOf(pci.context, Activity.class)) {
				if (token == getToken((Activity) pci.context)) {
					return pci;
				}
			}
		}
		return null;
	}


	public PluginContextInfo getPluginContextInfo(String tag,String className) {
		for (PluginContextInfo pci : pluginContexts.values()) {
			if (pci.context != null && pci.plugin.getTag().equals(tag) && pci.context.getClass().getName().equals(className)){
				return pci;
			}
		}
		return null;
	}


	public PluginContextInfo getPluginContextInfoByClassName(String className) {
		for (PluginContextInfo pci : pluginContexts.values()) {
			if (pci.context.getClass().getName().equals(className)){
				return pci;
			}
		}
		return null;
	}

	public Map<Context, PluginContextInfo> getAllPluginContextInfo() {
		return pluginContexts;
	}

	public Activity findActivityByToken(IBinder token) {
		WeakReference<Activity> ref = this.allActivities.get(token);
		if(ref == null){
			return null;
		}else{
			Activity act = ref.get();
			//清理内存
			if(act == null)
				this.allActivities.remove(token);
			return act;
		}
	}

	public PluginContextInfo getPluginContextInfoByActivityToken(IBinder token) {
		for (PluginContextInfo pci : pluginContexts.values()) {
			if (pci.context != null) {
				
				if (pci.context instanceof Activity && token == getToken((Activity)pci.context)) {
					return pci;
				}else if(token == getActivityToken(pci.context)){
					return pci;
				}
			}
		}
		return null;
	}

	public void addContextInfo(Context context, PluginContextInfo info) {
		this.pluginContexts.put(context, info);
	}

	public InstrumentationHacker(Instrumentation origin,
			ActivityThread activityThread) {
		this.origin = origin;
		this.activityThread = activityThread;
		this.pluginContexts = new HashMap<Context, PluginContextInfo>();
		this.allActivities = new HashMap<IBinder, WeakReference<Activity>>();
		this.componentCallbacksCache = new HashMap<Activity, ComponentCallbacks>();
	}

	@Override
	public Activity newActivity(Class<?> clazz, Context context, IBinder token,
			Application application, Intent intent, ActivityInfo info,
			CharSequence title, Activity parent, String id,
			Object lastNonConfigurationInstance) throws InstantiationException,
			IllegalAccessException {
		// TODO Auto-generated method stub
		Log.i(TAG, "newActivity()::clazz=" + clazz);
		resetInstrumentation(null);
		return super.newActivity(clazz, context, token, application, intent,
				info, title, parent, id, lastNonConfigurationInstance);
	}
	
	private Map<Activity,Activity> singleActivityCache = new HashMap<Activity,Activity>();

	@Override
	public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String tag = (String)ThreadContextData.getData(0);
		LoadedApk loadedApk = (LoadedApk)ThreadContextData.getData(1);

		//清除引用
		ThreadContextData.clean();

		PluginInfo pluginInfo = null;
		if(tag != null) {
			pluginInfo = PluginManager.getInstance().getInstalledPluginInfo(tag);
			//需要设置插件的context中的基础包名为宿主的，否则跟系统交互时，可能出现权限问题。
			RefUtil.setFieldValue(context, "mOpPackageName", HostGlobal.getPackageName());
			RefUtil.setFieldValue(context, "mBasePackageName", HostGlobal.getPackageName());
			RefUtil.setFieldValue(loadedApk, "mPackageName", pluginInfo.packageName);
		}

		Application app = super.newApplication(cl, className, context);
//		if(pluginInfo != null){
//			if(pluginInfo != null)
//				PluginManager.hookContext(app, plugin);
//		}
		return app;
	}

	private Activity createPluginActivity(Intent intent, String proxyClassName) {

		CodeTraceTS.begin("createPluginActivity");

		Activity targetActivity = null;
		
		PluginManager pluginManager = PluginManager.getInstance();

		String pTag = null;
		String targetClassName = null;
		try {
			targetClassName = 
			intent.getStringExtra(PluginManager.KEY_TARGET_CLASS_NAME);
			pTag = intent.getStringExtra(PluginManager.KEY_PLUGIN_TAG);

		} catch (Exception e) {
			Log.e(TAG, e);
			PluginManager.getInstance().postCrash(e);
		}


		if (pTag == null) {
			Log.e(TAG, "createPluginActivity()::error,pTag=" + pTag);
		}
		Log.i(TAG, "createPluginActivity()::pTag=" + pTag);

		com.qihoo.plugin.bean.Plugin plugin = pluginManager.getPlugin(pTag);
		Log.i(TAG, "createPluginActivity()::plugin=" + plugin);

		if (plugin == null) {
			Log.i(TAG, "createPluginActivity()::Plugin is not loaded in the "
					+ (TextUtils.isEmpty(pluginManager.getName()) ? "main"
							: "[" + pluginManager.getName() + "]")
					+ " process,tag=" + pTag);
			plugin = pluginManager.load(pTag);
			if (plugin == null)
				return null;
		}

		Log.i(TAG, "createPluginActivity()::targetClassName=" + targetClassName);
		
		ActivityInfo ai = plugin.findActivity(targetClassName);
//		if(ai.launchMode==ActivityInfo.LAUNCH_SINGLE_TASK
//				|| ai.launchMode==ActivityInfo.LAUNCH_SINGLE_INSTANCE){
//			PluginContextInfo pci = getPluginContextInfo(pTag, targetClassName);
//			if (pci != null) {
//				Activity act = (Activity)pci.context;
//				RefUtil.setFieldValue(act, "mBase", null);
//				RefUtil.setFieldValue(act.getFragmentManager(), "mActivity", null);
//				singleActivityCache.put(act, act);
//				act.setIntent(intent);
//				return (Activity)pci.context;
//			}
//		}

		if (targetClassName != null) {

			Class<?> clz = null;

			try {
				clz = plugin.getCl().loadClass(targetClassName);
			} catch (Exception e) {
				Log.e(TAG, e);
				Log.e(TAG, "createPluginActivity()::Didn't find class "
						+ targetClassName);
				PluginManager.getInstance().postCrash(e);
				return null;
			}

			try {
				targetActivity = (Activity) clz.newInstance();
				Log.i(TAG, "createPluginActivity()::targetActivity="
						+ targetActivity);
			} catch (Error e) {
				Log.e(TAG, "createPluginActivity()::clz.newInstance(),error,e="
						+ e);
			} catch (Exception e) {
				Log.e(TAG,
						"createPluginActivity()::clz.newInstance(),Exception,e="
								+ e);
			}
			// 缓存在当前正在运行的activity中
			pluginContexts.put(targetActivity, new PluginContextInfo(
					targetActivity, plugin, proxyClassName, ai, intent));


			TimeStatistics.getOrNewStartTimeInfo(pTag).activity_newActivity = CodeTraceTS.end("createPluginActivity").time();

			return targetActivity;

		}
		return null;

	}

	@Override
	public void callActivityOnNewIntent(Activity activity, Intent intent) {
		// TODO Auto-generated method stub
		Intent oriIntent = null;
		try{
			oriIntent = (Intent) (intent.hasExtra(PluginManager.KEY_IS_PLUGIN_INTENT) ? intent.getParcelableExtra(PluginManager.KEY_ORIGIN_INTENT) : intent);
		}catch(Exception e){
			Log.e(TAG, e);
		}
		if(oriIntent == null){
			oriIntent = intent;
		}
		super.callActivityOnNewIntent(activity, oriIntent);
		
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public Activity newActivity(ClassLoader cl, String className, Intent intent)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		Log.i(TAG, "newActivity()::cl=" + cl);
		Log.i(TAG, "newActivity()::className=" + className);

		try {

			intent.setExtrasClassLoader(cl);
			
			if (intent.hasExtra(PluginManager.KEY_IS_PLUGIN_INTENT)) {
				Activity act = createPluginActivity(intent, className);
				if(act != null)
					return act; //这里这么处理是因为有些系统返回null会导致android sdk内部异常，不会正确的抛出异常到app里面
			}
			resetInstrumentation(null);
		}catch(Exception e){
			Log.e(TAG, e);
		}
		
		return super.newActivity(cl, className, intent);
	}

	public IBinder getToken(Activity activity) {
		return (IBinder) RefUtil.getFieldValue(activity, "mToken");
	}

	public IBinder getActivityToken(Context context) {
		return (IBinder) RefUtil.getFieldValue(context, "mActivityToken");
	}

	@Override
	public void callActivityOnDestroy(Activity activity) {
		// TODO Auto-generated method stub

		try {
			if (pluginContexts.containsKey(activity)) {

				// 清理缓存
				ActivityPoolManager.getInstance().remove(activity);
				PluginContextInfo info = pluginContexts.remove(activity);

				// 注销监听
				activity.unregisterComponentCallbacks(this.componentCallbacksCache
						.get(activity));
				this.componentCallbacksCache.remove(activity);

				ProxyActivityPool.notifyIdle(activity, info.proxyActivityClass, true);

				//发送onDestory通知
				Intent onDestoryIntent = new Intent();
				onDestoryIntent.setPackage(HostGlobal.getPackageName());
				onDestoryIntent.setAction(Actions.ACTION_ACTIVITY_ON_DESTROY);
				onDestoryIntent.putExtra(Actions.DATA_INTENT, activity.getIntent());
				onDestoryIntent.putExtra(Actions.DATA_PLUGIN_TAG, info.plugin.getTag());
				activity.sendBroadcast(onDestoryIntent);

			}

			this.allActivities.remove(getToken(activity));
			this.singleActivityCache.remove(activity);
		}finally {

			try {
				super.callActivityOnDestroy(activity);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private void fixActivity(Plugin p, Activity context, ActivityInfo ai) {

		Log.d(TAG, "fixActivity()::context=" + context);
		Log.d(TAG, "fixActivity()::tag=" + p.getTag());
		Log.d(TAG, "fixActivity()::ai=" + ai);

		Class<?> contextImplClass = null;
		try {
			contextImplClass = Class.forName("android.app.ContextImpl");
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e);
			PluginManager.getInstance().postCrash(e);
		}

		Log.d(TAG, "fixActivity()::contextImplClass=" + contextImplClass);
		if (contextImplClass == null)
			return;

		// //注入LoadedApk
		// Object mBase = RefUtil.getFieldValue(context, Activity.class,
		// "mBase");
		// Object mymbase = RefUtil.getFieldValue(p.getApplication(),
		// Application.class, "mBase");
		// Object myLoadedApk = RefUtil.getFieldValue(mymbase, contextImplClass
		// , "mPackageInfo");
		//
		// RefUtil.setDeclaredFieldValue(mBase, contextImplClass,
		// "mPackageInfo", myLoadedApk);
		// RefUtil.setDeclaredFieldValue(context, Activity.class,
		// "mApplication", p.getApplication());

		PluginManager.getInstance().hookContext(context, p);

		// 注入ActivityInfo
		RefUtil.setFieldValue(context, "mActivityInfo", ai);
		

		// 重建inflater
		// RefUtil.setFieldValue(context, "mInflater", null);

	}

	private void handleLunchMode(Activity activity, String className,
			ActivityInfo ai, Plugin plugin) {

		ActivityPoolManager pool = ActivityPoolManager.getInstance();
		if (ai != null) {
			pool.doStartActivity(activity.getTaskId(), className, ai.launchMode);
		} else {
			Log.e(TAG, "Didn't find class " + className);
			return;
		}
		ProxyInfo info = new ProxyInfo();
		info.tag = plugin.getTag();
		info.className = className;
		info.activity = activity;
		info.taskId = activity.getTaskId();

		// 将activity放入插件activity池，用来模拟处理activity栈
		pool.push(className, info);

	}

	private void handleConfigChanges(final Activity activity,
			final ActivityInfo ai) {
		// 处理configChanges属性

		ComponentCallbacks componentCallbacks = new ComponentCallbacks() {

			@Override
			public void onLowMemory() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onConfigurationChanged(Configuration newConfig) {
				// TODO Auto-generated method stub
				// Configuration oldConfig =
				// (Configuration)RefUtil.getFieldValue(activity,
				// Activity.class, "mCurrentConfig");
				// Log.i(TAG, "=======================================");
				// Log.i(TAG, "handleConfigChanges::newConfig="+newConfig);
				// Log.i(TAG, "handleConfigChanges::oldConfig="+oldConfig);
				// int configChanges = newConfig.diff(oldConfig);
				//
				// Log.i(TAG,
				// "handleConfigChanges::configChanges="+configChanges);
				// Log.i(TAG,
				// "handleConfigChanges::ai.configChanges="+ai.configChanges);
				// Log.i(TAG,
				// "handleConfigChanges::needNewResources()="+Configuration.needNewResources(configChanges,ai.configChanges));
				// if(Configuration.needNewResources(configChanges,
				// ai.configChanges))
				// restartActivity(activity, newConfig);
			}
		};

		this.componentCallbacksCache.put(activity, componentCallbacks);
		activity.registerComponentCallbacks(componentCallbacks);
	}

	// 专门处理Activity的各种属性设定
	private void handleActivitySettings(Plugin plugin, Activity activity,
			ActivityInfo ai) {

		String className = activity.getClass().getName();

		Intent intent = activity.getIntent();
		
		Theme originTheme = activity.getTheme();

		// 注入插件资源到Activity中
		fixActivity(plugin, activity, ai);
		

		// 使用插件Resources重建Theme
		Theme mTheme = (Theme) RefUtil.getFieldValue(activity, "mTheme");
		if (mTheme != null) {
			RefUtil.setFieldValue(activity, "mTheme", null);
			activity.getTheme();
		}

		/**
		 * 没有自定义代理Activity才做属性的默认处理 如果是自定义的，系统怎么处理就是怎么处理
		 */
		if (!intent.getBooleanExtra(PluginManager.KEY_IS_CUSTOM_PROXY_ACTIVITY,
				false)) {

			// 处理Lunch mode
//			handleLunchMode(activity, className, ai, plugin);

			// 处理configChanges属性
			handleConfigChanges(activity, ai);
			
			// 设置activity属性
			activity.setRequestedOrientation(ai.screenOrientation);
			
			int resid = ai.getThemeResource();
			activity.setTheme(resid);

			Theme theme = plugin.getRes().newTheme();
			theme.applyStyle(resid, true);
			theme.setTo(plugin.getApplication().getTheme());
			RefUtil.setFieldValue(activity, "mTheme", theme);
//			RefUtil.setFieldValue(plugin.getApplication().getBaseContext(), "mTheme", theme);
			
			//处理透明Theme
			Theme newTheme = activity.getTheme();

			boolean isProxyActivityTranslucent = AndroidUtil.isThemeTranslucent(originTheme);
			boolean isPluginActivityTranslucent = AndroidUtil.isThemeTranslucent(newTheme);
			RefUtil.setFieldValue(activity, "mTheme", null);
			
			if(!isProxyActivityTranslucent && isPluginActivityTranslucent){
				//如果代理Activity非透明插件activity透明，则转换插件activity为透明
				AndroidUtil.convertActivityToTranslucent(activity);
			}else if(isProxyActivityTranslucent && !isPluginActivityTranslucent){
				//如果代理Activity透明插件activity非透明，则转换插件activity为透明
				AndroidUtil.convertActivityFromTranslucent(activity);
			}
			
			Log.i(TAG, "handleActivitySettings::activity=" + activity);
			Log.i(TAG, "handleActivitySettings::isProxyActivityTranslucent=" + isProxyActivityTranslucent);
			Log.i(TAG, "handleActivitySettings::isPluginActivityTranslucent=" + isPluginActivityTranslucent);
			
		}


		// ai.configChanges

	}

	@SuppressLint("NewApi")
	private boolean pluginActivityOnPreCreate(Activity activity,Bundle icicle) {

		PluginContextInfo pci = pluginContexts.get(activity);
		Plugin plugin = pci.plugin;
		activity.getIntent().setExtrasClassLoader(plugin.getCl());

		if(icicle != null)
			icicle.setClassLoader(plugin.getCl());

		Intent oriIntent = activity.getIntent().getParcelableExtra(
				PluginManager.KEY_ORIGIN_INTENT);
		if(oriIntent != null){
			oriIntent.setExtrasClassLoader(plugin.getCl());
		}
//		// 解包Intent，主要是将Intent中的序列化对象还原
		PluginManager.unwrapIntent(plugin, oriIntent);

		Activity cache = singleActivityCache.get(activity);
		if(cache != null){
			this.callActivityOnNewIntent(activity, oriIntent);
			RefUtil.setFieldValue(activity, "mCalled", true);
			return true;
		}

		PluginUtil.handleExternalDirs(activity.getBaseContext());

		Log.i(TAG, "handleActivitySettings()...1");
		handleActivitySettings(plugin, activity, pci.ai);

		LoadedApk pi = (LoadedApk)RefUtil.getFieldValue(activity, "mPackageInfo");
		Log.d(TAG, "pluginActivityOnPreCreate(), plugin.getCl()="+plugin.getCl());
		Log.d(TAG, "pluginActivityOnPreCreate(), activity.getClassLoader())="+activity.getClassLoader());
		Log.d(TAG, "pluginActivityOnPreCreate(), mPackageInfo="+pi);
		if(pi != null){
			Log.d(TAG, "pluginActivityOnPreCreate(), .getClassLoader()="+pi.getClassLoader());
		}
		Log.i(TAG, "pluginActivityOnPreCreate::mToken=" + getToken(activity));

		// PackageManager pm =
		// PackageManagerHacker.getPluginPackageManager(plugin.getTag(),
		// activity.getPackageManager());
		//
		// RefUtil.setFieldValue(activity.getBaseContext(), "mPackageManager",
		// pm);

		// 处理完后还原intent
		activity.setIntent(oriIntent);
		RefUtil.setFieldValue(activity, "mComponent", new ComponentName(plugin.getPackageInfo().packageName, activity.getClass().getName()));

		ILocationManagerHacker.replace(activity.getBaseContext(), HostGlobal.getBaseApplication().getSystemService(Context.LOCATION_SERVICE));
		HostApplicationProxy.fixServiceCache( HostGlobal.getBaseApplication().getBaseContext(), activity.getBaseContext());


		return false;

	}

	   public static final int CONTEXT_DEVICE_PROTECTED_STORAGE = 0x00000008;
	     public static final int CONTEXT_CREDENTIAL_PROTECTED_STORAGE = 0x00000010;
	@Override
	public void callActivityOnCreate(Activity activity, Bundle icicle) {
		// TODO Auto-generated method stub
		Log.i(TAG, "callActivityOnCreate()::activity=" + activity + ",icicle="
				+ icicle);

		StartTimeInfo st = new StartTimeInfo();

		CodeTraceTS.begin();
		boolean isPluginActivity = false;
		boolean hasExecption = false;
		try{
			this.allActivities.put(getToken(activity), new WeakReference<Activity>(activity));
			
			if (pluginContexts.containsKey(activity)) {
				isPluginActivity = true;
				// 如果是插件activity，则预处理下
				if(pluginActivityOnPreCreate(activity,icicle)){
					return;
				}
			}
		}catch(Exception e){
			Log.e(TAG,e);
			hasExecption = true;
		}finally{
			//修复手机卫士bug
			asyncResetInstrumentation(activity);
		}
		
//		System.out.println(activity.getSharedPreferences("aaa", Context.MODE_PRIVATE));
//		LoadedApk mPackageInfo = (LoadedApk) RefUtil.getFieldValue(activity.getBaseContext(), "mPackageInfo");
//
//		System.out.println(((Integer)RefUtil.getFieldValue(activity.getBaseContext(), "mFlags")& CONTEXT_DEVICE_PROTECTED_STORAGE)!=0);
//		System.out.println(((Integer)RefUtil.getFieldValue(activity.getBaseContext(), "mFlags")& CONTEXT_CREDENTIAL_PROTECTED_STORAGE)!=0);
//		
//		File file = mPackageInfo.getDataDirFile();

		st.activity_onCreate_host = CodeTraceTS.end().time();

		CodeTraceTS.begin("Activity.onCreate()" + activity);
		try {
			origin.callActivityOnCreate(activity, icicle);
		}catch(Throwable thr){
			hasExecption = true;
			Log.e(TAG,thr);
			if(isPluginActivity) {
				try {
					activity.finish();
				} catch (Throwable finishThr) {
					Log.e(TAG, finishThr);
				}
				PluginManager.getInstance().postExceptionToHost("plugin_callActivityOnCreate","activity="+activity+",icicle="+icicle, thr );
			}

		}
		st.activity_onCreate = CodeTraceTS.end("Activity.onCreate()" + activity).time();

		if(isPluginActivity && ! hasExecption){

			PluginContextInfo pci = pluginContexts.get(activity);

			
			if(pci != null){
				st.activity_onCreate_total = st.activity_onCreate + st.activity_onCreate_host;
				st.activity_name = activity.getClass().getName();
				TimeStatistics.updateStartTime(pci.plugin.getTag(),st);

				//发送onCreate通知
				Intent onCreateIntent = new Intent();
				onCreateIntent.setPackage(HostGlobal.getPackageName());
				onCreateIntent.setAction(Actions.ACTION_ACTIVITY_ON_CREATE);
				onCreateIntent.putExtra(Actions.DATA_INTENT, activity.getIntent());
				onCreateIntent.putExtra(Actions.DATA_PLUGIN_TAG, pci.plugin.getTag());
				
				if(icicle != null)
					onCreateIntent.putExtra(Actions.DATA_ICICLE, icicle);
				
				activity.sendBroadcast(onCreateIntent);

				ProxyActivityPool.notifyIdle(activity,pci.proxyActivityClass,false);


			}
			
		}
		
	}

	// @Override
	// 低版本，参考4.0.3
	public ActivityResult execStartActivity(Context who, IBinder contextThread,
			IBinder token, Activity target, Intent intent, int requestCode) {
		return _execStartActivity(who, contextThread, token, target, intent,
				requestCode, null, false);
	}

	private Map<String, Activity> stacksForActivitySingleInstance = new HashMap<String, Activity>();

	private Intent handleLaunchMode(String pluginTag, Intent intent,
			ActivityInfo ai) {

		switch (ai.launchMode) {

		case ActivityInfo.LAUNCH_SINGLE_TASK:
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


		case ActivityInfo.LAUNCH_SINGLE_TOP:
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			break;

		case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
			// 如果是LAUNCH_SINGLE_INSTANCE，则模拟实现
			ComponentName name = intent.getComponent();
			if (name != null) {
				String key = pluginTag + ":" + name.getClassName();
				Activity activity = stacksForActivitySingleInstance.get(key);
				// 如果没有找到实例，则创建一个实例，并创建一个新的task
				if (activity == null) {
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				}
			}
			break;
		default:
			break;
		}

		return intent;

	}
	
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Fragment target,
            Intent intent, int requestCode, Bundle options) {
		return _execStartActivity(who, contextThread, token, target, intent,
				requestCode, options, true);
    }

	/**
	 * 中转函数 根据不同版本调用不同父类方法
	 */
	private ActivityResult _execStartActivity(Context who,
			IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options, boolean hasOptions) {

		CodeTraceTS.begin("_execStartActivity");
		Log.i(TAG, "execStartActivity::who=" + who);
		Log.i(TAG, "execStartActivity::target=" + target);
		Log.i(TAG, "execStartActivity::intent=" + intent);
		PluginManager pluginManager = PluginManager.getInstance();

		try{
			Intent newIntent = null;
			
	//		if (!containsKey(intent,PluginManager.KEY_IS_PLUGIN_INTENT)) {
			if(PluginUtil.needFindPlugin(intent)){
				if (pluginContexts.containsKey(who)) {
					
					// 插件内部调用的
					PluginContextInfo info = pluginContexts.get(who);
					Plugin plugin = info.plugin;
					try {
						Class<?> proxyActivity = (Class<? extends BaseProxyActivity>) Class
								.forName(info.proxyActivityClass);
						newIntent = pluginManager.makeActivityIntent(who,
								plugin.getTag(), intent,
								(Class<? extends BaseProxyActivity>) proxyActivity);
	
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, e);
						pluginManager.postCrash(e);
					}
				} else {
					newIntent = pluginManager.makeActivityIntent(who, null, intent);
				}
				if (newIntent != null) {
					intent = newIntent;
				}
			}
		}catch(Exception e){
			Log.e(TAG, e);
			pluginManager.postCrash(e);
		}

		try {
			// 因为调不到父类的该方法，所以使用原实例调用
			if (hasOptions) {
				try {
					return (ActivityResult) RefUtil.callDeclaredMethod(origin,
							Instrumentation.class, "execStartActivity",
							new Class<?>[]{Context.class, IBinder.class,
									IBinder.class, Activity.class, Intent.class,
									int.class, Bundle.class}, new Object[]{who,
									contextThread, token, target, intent,
									requestCode, options});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e(TAG, e.getCause());
					pluginManager.postCrash(e.getCause());
				}
			} else {

				try {
					return (ActivityResult) RefUtil.callDeclaredMethod(origin,
							Instrumentation.class, "execStartActivity",
							new Class<?>[]{Context.class, IBinder.class,
									IBinder.class, Activity.class, Intent.class,
									int.class}, new Object[]{who, contextThread,
									token, target, intent, requestCode});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e(TAG, e);
					pluginManager.postCrash(e);
				}
			}
		}finally {

			Log.d("_execStartActivity" , "_execStartActivity() " + CodeTraceTS.end("_execStartActivity").time() + "ms");
		}
		return null;
	}
	
	private ActivityResult _execStartActivity(Context who,
			IBinder contextThread, IBinder token, Fragment target,
			Intent intent, int requestCode, Bundle options, boolean hasOptions) {

		Log.i(TAG, "execStartActivity::who=" + who);
		Log.i(TAG, "execStartActivity::target=" + target);
		PluginManager pluginManager = PluginManager.getInstance();

		try{
			Intent newIntent = null;
			
	//		if (!containsKey(intent,PluginManager.KEY_IS_PLUGIN_INTENT)) {
			if (!intent.hasExtra(PluginManager.KEY_IS_PLUGIN_INTENT)) {
				if (pluginContexts.containsKey(who)) {
					// 插件内部调用的
					PluginContextInfo info = pluginContexts.get(who);
					Plugin plugin = info.plugin;
					try {
						Class<?> proxyActivity = (Class<? extends BaseProxyActivity>) Class
								.forName(info.proxyActivityClass);
						newIntent = pluginManager.makeActivityIntent(who,
								plugin.getTag(), intent,
								(Class<? extends BaseProxyActivity>) proxyActivity);
	
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, e);
					}
				} else {
					newIntent = pluginManager.makeActivityIntent(who, null, intent);
				}
				if (newIntent != null) {
					intent = newIntent;
				}
			}
		}catch(Exception e){
			Log.e(TAG, e);
			pluginManager.postCrash(e);
		}


		// 因为调不到父类的该方法，所以使用原实例调用
		if (hasOptions) {
			try {
				return (ActivityResult) RefUtil.callDeclaredMethod(origin,
						Instrumentation.class, "execStartActivity",
						new Class<?>[] { Context.class, IBinder.class,
								IBinder.class, Fragment.class, Intent.class,
								int.class, Bundle.class }, new Object[] { who,
								contextThread, token, target, intent,
								requestCode, options });
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getCause());
				pluginManager.postCrash(e.getCause());
			}
		} else {

			try {
				return (ActivityResult) RefUtil.callDeclaredMethod(origin,
						Instrumentation.class, "execStartActivity",
						new Class<?>[] { Context.class, IBinder.class,
								IBinder.class, Fragment.class, Intent.class,
								int.class }, new Object[] { who, contextThread,
								token, target, intent, requestCode });
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e);
				pluginManager.postCrash(e);
			}
		}
		return null;
	}

	// @Override
	public ActivityResult execStartActivity(Context who, IBinder contextThread,
			IBinder token, Activity target, Intent intent, int requestCode,
			Bundle options) {
		return _execStartActivity(who, contextThread, token, target, intent,
				requestCode, options, true);
	}

	private void printDeclaredMethods(Class<?> clz, String methodName) {
		Method[] methods = clz.getDeclaredMethods();
		for (Method m : methods) {
			if (!m.getName().equals(methodName))
				continue;
			Type returnType = m.getGenericReturnType();
			String str = "";
			if (returnType == null)
				str = " void ";
			else
				str = returnType.toString() + " ";
			str += m.getName() + "(";
			Type[] types = m.getGenericParameterTypes();
			if (types != null) {
				for (Type type : types) {
					str += type + ",";
				}
				str = str.substring(0, str.length() - 1);
			}
			str += ");";
			Log.e(TAG, str);
		}
	}

	private List<Method> getDeclaredMethods(Class<?> clz, String methodName) {
		Method[] methods = clz.getDeclaredMethods();
		List<Method> list = new ArrayList<Method>();
		for (Method m : methods) {
			if (m.getName().equals(methodName))
				list.add(m);
		}
		return list;
	}
	
	

	public void requestRelaunchActivity(IBinder token, Configuration config) {
		// printDeclaredMethods(ActivityThread.class,"requestRelaunchActivity");
		Object[] args = null;

		// 低版本，参考4.0.4
		Method method = RefUtil.getDeclaredMethod(ActivityThread.class,
				"requestRelaunchActivity", new Class[] { IBinder.class,
						List.class, List.class, int.class, boolean.class,
						Configuration.class, boolean.class });
		if (method != null) {
			args = new Object[] { token, null, null, 0, true, config, false };
		}

		if (method == null) {
			// 高版本，参考6.0
			method = RefUtil.getDeclaredMethod(ActivityThread.class,
					"requestRelaunchActivity", new Class[] { IBinder.class,
							List.class, List.class, int.class, boolean.class,
							Configuration.class, Configuration.class,
							boolean.class });
			if (method != null) {
				args = new Object[] { token, null, null, 0, true, config,
						config, false };
			}
		}

		// 三星多窗口
		if (method == null) {

			try {
				method = RefUtil
						.getDeclaredMethod(
								ActivityThread.class,
								"requestRelaunchActivity",
								new Class[] {
										IBinder.class,
										List.class,
										List.class,
										int.class,
										boolean.class,
										Configuration.class,
										boolean.class,
										Class.forName("com.samsung.android.multiwindow.MultiWindowStyle") });
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (method != null) {
				args = new Object[] { token, null, null, 0, true, config,
						false, null };
			}

			if (method == null) {
				try {
					method = RefUtil
							.getDeclaredMethod(
									ActivityThread.class,
									"requestRelaunchActivity",
									new Class[] {
											IBinder.class,
											List.class,
											List.class,
											int.class,
											boolean.class,
											Configuration.class,
											Configuration.class,
											boolean.class,
											Class.forName("com.samsung.android.multiwindow.MultiWindowStyle") });
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (method != null) {
					args = new Object[] { token, null, null, 0, true, config,
							config, false, null };
				}
			}
		}

		// 在都没有找到的情况下，做模糊查找匹配操作，尽量兼容
		if (method == null) {
			List<Method> methods = getDeclaredMethods(ActivityThread.class,
					"requestRelaunchActivity");
			if (methods.size() > 0) {
				method = methods.get(0);
				Class<?>[] params = method.getParameterTypes();

				List<Object> argList = new ArrayList<Object>();
				for (Class<?> c : params) {
					if (c.equals(IBinder.class))
						argList.add(token);
					else if (c.equals(Configuration.class))
						argList.add(config);
					else if (c.isPrimitive()) {
						if (c.equals(boolean.class))
							argList.add(false);
						else if (c.equals(int.class))
							argList.add(0);
						else if (c.equals(long.class))
							argList.add(0);
					} else {
						argList.add(null);
					}
				}
				args = argList.toArray();
			}
		}

		if (method == null) {
			Log.e(TAG,
					"requestRelaunchActivity:: Method not found. activityThread.requestRelaunchActivity()");
			printDeclaredMethods(ActivityThread.class,
					"requestRelaunchActivity");
			return;
		}

		try {
			method.invoke(activityThread, args);
		} catch (Exception e) {
			Log.e(TAG, e.getCause());
			e.printStackTrace();
		}

		// 3907 public final void requestRelaunchActivity(IBinder token,
		// 3908 List<ResultInfo> pendingResults, List<ReferrerIntent>
		// pendingNewIntents,
		// 3909 int configChanges, boolean notResumed, Configuration config,
		// 3910 Configuration overrideConfig, boolean fromServer) {

	}

	@Override
	public void callActivityOnResume(Activity activity) {
		CodeTraceTS.begin("callActivityOnResume");
		super.callActivityOnResume(activity);
		Log.d(TAG,
				"callActivityOnResume()," + CodeTraceTS.end("callActivityOnResume").time() + "ms");
	}

	@Override
	public void callActivityOnPause(Activity activity) {

		CodeTraceTS.begin("callActivityOnPause");
		super.callActivityOnPause(activity);
		Log.d(TAG,
				"callActivityOnPause()," + CodeTraceTS.end("callActivityOnPause").time() + "ms");
	}

	public void restartActivity(Activity activity, Configuration config) {

		IBinder token = getToken(activity);
		requestRelaunchActivity(token, config);
		// Class<?> ActivityClientRecord_class = null;
		// try {
		// ActivityClientRecord_class =
		// Class.forName(ActivityThread.class.getName()+"$ActivityClientRecord");
		// Map<Object,Object> mActivities = (Map<Object, Object>)
		// RefUtil.getFieldValue(activityThread, ActivityThread.class,
		// "mActivities");
		// Object record = mActivities.get(token);
		// RefUtil.callDeclaredMethod(activityThread, ActivityThread.class,
		// "handleRelaunchActivity", new Class<?>[]{ActivityClientRecord_class},
		// new Object[]{record});
		//
		// } catch (Exception e) {
		// Log.e(TAG, e);
		// }
	}

	@Override
	public void callActivityOnStart(Activity activity) {
		// TODO Auto-generated method stub
		Log.i(TAG, "callActivityOnStart::activity=" + activity);
		Log.i(TAG,
				"callActivityOnStart::activity.getClassLoader()="
						+ activity.getClassLoader());
		CodeTraceTS.begin("callActivityOnStart");
		super.callActivityOnStart(activity);
		Log.d(TAG,
				"callActivityOnStart()," + CodeTraceTS.end("callActivityOnStart").time() + "ms")
		;
	}

	@Override
	public void onCreate(Bundle arguments) {
		// TODO Auto-generated method stub
		super.onCreate(arguments);
	}

	@Override
	public boolean onException(Object obj, Throwable e) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onException()::obj=" + obj + ",e=" + e);
		Log.e(TAG, e);
		PluginManager.getInstance().postCrash(e);
		return super.onException(obj, e);
	}

	/**
	 * 重设instrumentation
	 * 修复手机卫士将instrumentation替换掉的问题
	 * @param instrumentation
	 * @param activity
	 */
	private void asyncResetInstrumentation(final Activity activity) {
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				resetInstrumentation(activity);
			}
		});
	}
	
	/**
	 * 重设instrumentation
	 * 修复手机卫士将instrumentation替换掉的问题
	 * @param instrumentation
	 * @param activity
	 */
	private void resetInstrumentation(final Activity activity) {
		RefUtil.setFieldValue(ActivityThread.currentActivityThread(),
				"mInstrumentation", PluginManager.getInstrumentation());
		if (activity != null) {
			RefUtil.setFieldValue(activity, "mInstrumentation",
					PluginManager.getInstrumentation());
		}
	}
	
	@Override
	public void callActivityOnStop(Activity activity) {
		// TODO Auto-generated method stub
		if(origin != null)
			origin.callActivityOnStop(activity);
	}
}
