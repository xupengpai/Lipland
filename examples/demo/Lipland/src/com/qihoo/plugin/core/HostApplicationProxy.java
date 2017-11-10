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

package com.qihoo.plugin.core;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.Application;
import android.app.IServiceConnection;
import android.app.Instrumentation;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.Service;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.view.accessibility.IAccessibilityManager;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.bean.PluginContextInfo;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.core.PluginManager.ServiceConnectionWrapper;
import com.qihoo.plugin.core.hook.ActivityManagerHacker;
import com.qihoo.plugin.core.hook.IAccessibilityManagerHacker;
import com.qihoo.plugin.core.hook.IClipboardHacker;
import com.qihoo.plugin.core.hook.IMountServiceHacker;
import com.qihoo.plugin.core.hook.INotificationManagerHacker;
import com.qihoo.plugin.core.hook.IPackageManagerHacker;
import com.qihoo.plugin.core.hook.ITelephonyHacker;
import com.qihoo.plugin.core.hook.InstrumentationHacker;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.install.InstallManager;
import com.qihoo.plugin.util.NetworkManager;
import com.qihoo.plugin.util.PluginUtil;
import com.qihoo.plugin.util.RefUtil;


/**
 * 应用全局处理
 * 包括全局hook、全局事件回调
 * @author xupengpai
 * @date 2015年12月30日 下午3:27:50
 */
public class HostApplicationProxy  {
	
	
	private final static String TAG = HostApplicationProxy.class.getSimpleName();
	
	public static ActivityThread activityThread;

	/*
	 * 接管Instrumentation
	 * Instrumentation可以拦截整个应用的Activity调用，包括Activity实例化
	 */
	public static void injectInstrumentation(final ActivityThread activityThread){
		
		final String fieldName = "mInstrumentation";
		
		Instrumentation globalInstrumentation = (Instrumentation) RefUtil.getFieldValue(activityThread,ActivityThread.class, fieldName);
		InstrumentationHacker instrumentationHacker = new InstrumentationHacker(globalInstrumentation,activityThread);
		
		RefUtil.cloneObject(globalInstrumentation, instrumentationHacker, Instrumentation.class);
		RefUtil.setDeclaredFieldValue(activityThread,ActivityThread.class, fieldName, instrumentationHacker);
		
		PluginManager.setInstrumentation(instrumentationHacker);

		if(HostGlobal.getProcessName().equals("com.qihoo.haosou:plugin")){
//			final Thread thread = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					while(this != null) {
//						try {
//							Thread.sleep(4000);
//							Log.d(TAG, Process.myPid() + ",processName=" + HostGlobal.getProcessName() + ", mInstrumentation=" + (Instrumentation) RefUtil.getFieldValue(activityThread, ActivityThread.class, fieldName));
//
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//
//				}
//			});
//			thread.start();
		}
		Log.d(TAG, Process.myPid() + ",processName="+HostGlobal.getProcessName() + ", mInstrumentation=" + (Instrumentation) RefUtil.getFieldValue(activityThread,ActivityThread.class, fieldName));

		
	}
	
	private static ServiceConnection getServiceConnection(IServiceConnection conn){

//		1068            final WeakReference<LoadedApk.ServiceDispatcher> mDispatcher;
		WeakReference<?> ref = (WeakReference<?>) RefUtil.getFieldValue(conn, "mDispatcher");
		Object obj = ref.get();
		return (ServiceConnection) RefUtil.getFieldValue(obj, "mConnection");
		
	}
	
	/**
	 * 挂钩权限检测模块
	 */
	private static void hookAppOpsManager(){
//		AppOpsManagerHacker hacker = AppOpsManagerHacker.hook();


//		if(hacker != null){
//			hacker.addHookHandler("checkPackage", new HookHandler() {
//				@Override
//				public boolean onBefore(Object origin, Method method, Object[] args) {
//					// TODO Auto-generated method stub
//					if(args != null && args.length > 1){
//						String pkgName = args[1].toString();
//						for(Plugin p : PluginManager.getInstance().getPlugins().values()){
//							
//							//只要在插件列表中存在该包，则不再检测
//							if(p.getPackageInfo().packageName.equals(pkgName)){
//								return false;
//							}
//						}
//						
//					}
//					return super.onBefore(origin, method, args);
//				}
//			});
//		}
	}
	

	//接管PackageManager
	private static void hookPackageManager(Application app){

//		//hook getPackageInfo()
		IPackageManagerHacker.hook(app).setHookHandlerByName("getPackageInfo", new HookHandler() {
			
			private Map<String,PackageInfo> packageInfoCache;
			
			@Override
			public boolean onBefore(Object origin, Method method, Object[] args) {
				// TODO Auto-generated method stub
				PluginManager pluginManager = PluginManager.getInstance();
				String packageName = (String)args[0];
				Integer flags = (Integer)args[1];

				Log.d(TAG, "getPackageInfo(),onBefore(),packageName="+packageName+",flags="+flags);

				//如果是宿主包名，则忽略，可以节省查询时间
				if(packageName.equals(HostGlobal.getPackageName())){
					return super.onBefore(origin, method, args);
				}


				//查询包名如果是一个插件，则解析包信息直接返回
				PluginPackage pluginPackage =  pluginManager.getInstallManager().queryPluginByPackageName(packageName);
				Log.d(TAG, "getPackageInfo(),onBefore(),pluginPackage=" + pluginPackage);

				if(pluginPackage != null){
					Log.d(TAG, "getPackageInfo(),onBefore(),pluginPackage.pi.path=" + pluginPackage.pi.path);
					PackageInfo pi = HostGlobal.getBaseApplication()
							.getPackageManager()
							.getPackageArchiveInfo(pluginPackage.pi.path,flags);
					setResult(pi);
					Log.d(TAG, "getPackageInfo(),onBefore(),pi=" + pi);
					if(pi != null)
						Log.d(TAG, "getPackageInfo(),onBefore(),pi.signatures=" + pi.signatures);
					return false;
				}
				
				return super.onBefore(origin, method, args);
			}
		});
		//hook getActivityInfo()
		IPackageManagerHacker.hook(app).setHookHandlerByName("getActivityInfo", new HookHandler() {
			
			
			@Override
			public boolean onBefore(Object origin, Method method, Object[] args) {
				// TODO Auto-generated method stub
				ComponentName className = (ComponentName)args[0];
				PluginManager pluginManager = PluginManager.getInstance();

				Activity activity = pluginManager.getInstallManager().queryActivity(className);
				if(activity != null){
					activity.info.metaData = activity.metaData;
					setResult(activity.info);
					return false;
				}
				return super.onBefore(origin, method, args);
			}
		});

		//hook getActivityInfo()
		IPackageManagerHacker.hook(app).setHookHandlerByName("getApplicationInfo", new HookHandler() {

			@Override
			public boolean onBefore(Object origin, Method method, Object[] args) {
				// TODO Auto-generated method stub
				String packageName = (String)args[0];
				Integer flags = (Integer)args[1];

				//如果是宿主包名，则忽略，可以节省查询时间
				if(packageName.equals(HostGlobal.getPackageName())){
					return super.onBefore(origin, method, args);
				}

				PluginManager pluginManager = PluginManager.getInstance();

				//查询包名如果是一个插件，则解析包信息直接返回
				PluginPackage pluginPackage =  pluginManager.getInstallManager().queryPluginByPackageName(packageName);
				if(pluginPackage != null){
					setResult(pluginPackage.pkg.applicationInfo);
					return false;
				}
				return super.onBefore(origin, method, args);
			}
		});

//		Context context = null;
//
//		Uri uri = Uri.parse("clean://fragment1?p1=a&p2=b");
//		Intent intent = new Intent("action.android.cleaner", uri);
//		if(queryIntentActivities(intent) != null) {
//			context.startActivity(intent);
//		}

		//queryIntentActivities
		//queryIntentActivitiesAsUser
		IPackageManagerHacker.hook(app).setHookHandlerByName("queryIntentActivities", new HookHandler() {


					private ResolveInfo buildActivityResolveInfo(InstallManager.ComponentIntentResult result) {
						ResolveInfo res = new ResolveInfo();
						res.activityInfo = ((Activity) result.component).info;
						res.priority = result.intentInfo.getPriority();
						res.isDefault = result.intentInfo.hasDefault;
						res.labelRes = result.intentInfo.labelRes;
						res.icon = result.intentInfo.icon;
						return res;
					}

					@Override
					public boolean onBefore(Object origin, Method method, Object[] args) {
						return super.onBefore(origin, method, args);
					}

					@Override
					public Object onAfter(Object origin, Method method, Object[] args, Object result, Throwable thr) {

						try {
							Intent intent = (Intent) args[0];

							if (intent != null) {
								List list = null;
								if(result != null){
									if(List.class.isAssignableFrom(result.getClass())){
										list = (List) result;
									}else if(ParceledListSlice.class.isAssignableFrom(result.getClass())){
										list = (List)RefUtil.callDeclaredMethod(result,ParceledListSlice.class,"getList",null,null);
									}
								}

								if (list == null || list.size() == 0) {

									//如果在系统里面没有匹配的Intent，则尝试从插件里面寻找
									PluginManager pluginManager = PluginManager.getInstance();
									List<InstallManager.ComponentIntentResult> resultList = pluginManager.getInstallManager().queryActivities(null, intent);
									List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
									if (resultList != null && resultList.size() > 0) {
										for (InstallManager.ComponentIntentResult componentIntentResult : resultList) {
											ResolveInfo info = buildActivityResolveInfo(componentIntentResult);
											resolveInfos.add(info);
										}
									}
									if (resolveInfos.size() > 0) {

										if(VERSION.SDK_INT >= 24){
											Constructor constructor = ParceledListSlice.class.getDeclaredConstructor(new Class[]{List.class});
											if(constructor != null){
												setResult(constructor.newInstance(resolveInfos));
												return getResult();
											}
										}
										return resolveInfos;
									}
								}
							}
						}catch(Throwable t){
							Log.e(TAG,t);
						}
						return super.onAfter(origin, method, args, result, thr);
					}
				}
		);



//		IMountServiceHacker.hook().setHookHandlerByName("mkdirs", new HookHandler() {
//			@Override
//			public boolean onBefore(Object origin, Method method,
//					Object[] args) {
//				// TODO Auto-generated method stub
//				if(args != null && args.length >= 1){
//					//把包名改为宿主的包名，避免权限问题
//					args[0] = HostGlobal.getPackageName();
//				}
//				return super.onBefore(origin, method, args);
//			}
//		});

//		ILocationManagerHacker.hook().setHookHandlerByName("removeUpdates", new HookHandler() {
//			@Override
//			public boolean onBefore(Object origin, Method method, Object[] args) {
//				Log.i("MY", "removeUpdates....");
//				// TODO Auto-generated method stub
//				if(args != null && args.length >=3){
//					Log.i("MY", "1-args[2]...."+args[2]);
//					args[2] = HostGlobal.getPackageName();
//				}
//				Log.i("MY", "2-args[2]...."+args[2]);
//				return super.onBefore(origin, method, args);
//			}
//		});
	}

	
//	checkPackage(int uid, String packageName) {
	private static void hookActivityManager(){
		
//		ActivityManagerHacker.hook().setHookHandlerByName("startActivity", new HookHandler() {
//			@Override
//			public boolean onBefore(Object origin, Method method, Object[] args) {
//				// TODO Auto-generated method stub
//				return super.onBefore(origin, method, args);
//			}
//		});

		
		//拦截startService
		ActivityManagerHacker.hook().setHookHandlerByName("startService", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
					Object[] args) {
				// TODO Auto-generated method stub
//				  public ComponentName startService(IApplicationThread caller, Intent service,
//						            String resolvedType, String callingPackage, int userId) throws RemoteException

				PluginManager pluginManager = PluginManager.getInstance();
				try{
					Intent intent = (Intent)args[1];
					Log.i(TAG, "startService..intent="+intent);
					if(PluginUtil.needFindPlugin(intent)){
						Service service = pluginManager.queryService(null, intent);
						if(service != null){
							pluginManager.startService(service, intent);
							setResult(null);
							return false;
						}
					}
				}catch(Exception e){
					Log.e(TAG, e);
					pluginManager.postCrash(e);
				}
				return super.onBefore(origin, method, args);
			}
		});
		
		//拦截stopService
		ActivityManagerHacker.hook().setHookHandlerByName("stopService", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
					Object[] args) {
				// TODO Auto-generated method stub
				try{
					Intent intent = (Intent)args[1];
					Log.i(TAG, "stopService..intent="+intent);
					if(PluginUtil.needFindPlugin(intent)){
						PluginManager pluginManager = PluginManager.getInstance();
						Service service = pluginManager.queryService(null, intent);
						if(service != null){
							pluginManager.stopService(pluginManager.getApplicationContext(), service, intent,-1);
							setResult(0);
							return false;
						}
					}
				}catch(Exception e){
					Log.e(TAG, e);
				}
				return super.onBefore(origin, method, args);
			}
		});
		
		//拦截stopServiceToken
		ActivityManagerHacker.hook().setHookHandlerByName("stopServiceToken", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
					Object[] args) {
				// TODO Auto-generated method stub
				try{
					ComponentName className = (ComponentName)args[0];
					int startId = -1;
					try{
						startId = Integer.valueOf(args[2].toString());
					}catch(Throwable thr){
						thr.printStackTrace();
					}
					Log.i(TAG, "stopServiceToken..className="+className);
					
					Intent intent = new Intent();
					intent.setComponent(className);
					
					PluginManager pluginManager = PluginManager.getInstance();

					String hostPackageName = HostGlobal.getPackageName();

					//目标为宿主，则走原始逻辑，与插件无关
					if(!className.getPackageName().equals(hostPackageName)) {
						Service service = pluginManager.queryService(null, intent);
						if (service != null) {
							pluginManager.stopService(pluginManager.getApplicationContext(), service, intent, startId);
							setResult(true);
							return false;
						}
					}
				}catch(Exception e){
					Log.e(TAG, e);
				}
				return super.onBefore(origin, method, args);
			}
		});
		
		ActivityManagerHacker.hook().setHookHandlerByName("bindService", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
					Object[] args) {
				// TODO Auto-generated method stub
				IBinder activityToken = (IBinder)args[1];
				PluginManager pluginManager = PluginManager.getInstance();
				try{
					Intent intent = (Intent)args[2];
					Log.i(TAG, "bindService..intent="+intent);
					if(PluginUtil.needFindPlugin(intent)){
						IServiceConnection sd = (IServiceConnection)args[4];
						Log.i(TAG, "bindService..sd="+sd);
						int flags = (Integer)args[5];
						Service service = pluginManager.queryService(null, intent);
						Log.i(TAG, "bindService..service="+service);
						if(service != null){
							ServiceConnection sc = getServiceConnection(sd);
							Log.i(TAG, "bindService..sc="+sc);
							if(!(sc instanceof ServiceConnectionWrapper)){
								//Activity在onCreate()执行完了之后才放到mActivities里面，所以在onCreate()里面会找不到
	//							Activity activity = activityThread.getActivity(activityToken);
								PluginContextInfo pci = null;
								if(activityToken != null)
									pci = pluginManager.getInstrumentation().getPluginContextInfoByActivityToken(activityToken);
								String tag = null;
								Context context = null;
								if(pci != null){
									tag = pci.plugin.getTag();
									context = pci.context;
								}else{
									context = pluginManager.getInstrumentation().findActivityByToken(activityToken);
								}
								
								if(context == null)
									context = pluginManager.getApplicationContext();

								Log.i(TAG, "bindService..context="+context);
								Log.i(TAG, "bindService..service="+service);
								int result = pluginManager.bindService(context, service,intent, sc, flags) ? 1 : 0;
								Log.i(TAG, "bindService..result="+result);
								setResult(result);
								return false;
							}
						}
					}
				}catch(Exception e){
					Log.e(TAG, e);
					pluginManager.postCrash(e);
				}
				
				return super.onBefore(origin, method, args);
			}
		});

		ActivityManagerHacker.hook().setHookHandlerByName("unbindService", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
									Object[] args) {
				// TODO Auto-generated method stub

				PluginManager pluginManager = PluginManager.getInstance();
				try{
					IServiceConnection sd = (IServiceConnection)args[0];
					if(sd != null){
						ServiceConnection sc = getServiceConnection(sd);
						if(sc != null && sc instanceof ServiceConnectionWrapper){
							ServiceConnectionWrapper wrapper = (ServiceConnectionWrapper)sc;
							//						if(pluginManager.tryUnbindService(wrapper)){
							//							return true;
							//						}
							//						return false;
							pluginManager.tryUnbindService(wrapper);
						}
					}
				}catch(Exception e){
					Log.e(TAG, e);
					pluginManager.postCrash(e);
				}
				return true;
			}
		});

		ActivityManagerHacker.hook().setHookHandlerByName("getIntentSender", new HookHandler() {

			@Override
			public boolean onBefore(Object origin, Method method, Object[] args) {
				// TODO Auto-generated method stub
				if(args != null && args.length > 2){
					args[1] = HostGlobal.getPackageName();
				}
				return super.onBefore(origin, method, args);
			}
		});


		//将插件包名加入到进程信息中，这里主要为了解决小米手机Toast弹不出来的问题，小米的ToastInjector中的逻辑导致
		ActivityManagerHacker.hook().setHookHandlerByName("getRunningAppProcesses", new HookHandler() {

			@Override
			public Object onAfter(Object origin, Method method, Object[] args, Object result, Throwable thr) {

				if(result != null){
					List<ActivityManager.RunningAppProcessInfo> list = (List<ActivityManager.RunningAppProcessInfo>)result;
					for(ActivityManager.RunningAppProcessInfo info : list){

						if(info.pkgList != null){

							List<String> pkgList = new ArrayList<String>();

							boolean isSelfProc = false;
							for(String pkg : info.pkgList){
								if(pkg.equals(HostGlobal.getPackageName())) {
									isSelfProc = true;
									break;
								}
							}

							if(isSelfProc){

								//加入原有包名
								for(String pkg : info.pkgList){
									pkgList.add(pkg);
								}

								//将所有插件的包名加入进去
								Map<String, PluginPackage>  installPlugins = PluginManager.getInstance().getInstallManager().getInstalledPlugins();//.queryInstalledPluginInfo();
								if(installPlugins != null){
									String[] keys = installPlugins.keySet().toArray(new String[]{});
									for(String key : keys){
										PluginPackage pp = installPlugins.get(key);
										pkgList.add(pp.pi.packageName);
									}
								}

								info.pkgList = pkgList.toArray(new String[]{});

							}

						}
					}
				}
				return super.onAfter(origin, method, args, result, thr);
			}
		});

//		@Override
//		public int checkCallingPermission(String permission) {
//			if (permission == null) {
//				throw new IllegalArgumentException("permission is null");
//			}
//
//			int pid = Binder.getCallingPid();
//			if (pid != Process.myPid()) {
//				return checkPermission(permission, pid, Binder.getCallingUid());
//			}
//			return PackageManager.PERMISSION_DENIED;
//		}
//
//		@Override
//		public int checkCallingOrSelfPermission(String permission) {
//			if (permission == null) {
//				throw new IllegalArgumentException("permission is null");
//			}
//
//			return checkPermission(permission, Binder.getCallingPid(),
//					Binder.getCallingUid());
//		}
//
//		@Override
//		public int checkSelfPermission(String permission) {
//			if (permission == null) {
//				throw new IllegalArgumentException("permission is null");
//			}
//
//			return checkPermission(permission, Process.myPid(), Process.myUid());
//		}

		ActivityManagerHacker.hook().setHookHandlerByName("checkPermission", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
									Object[] args) {
				// TODO Auto-generated method stub

				PluginManager pluginManager = PluginManager.getInstance();
				try{
					String permission = (String)args[0];
					int pid = (Integer) args[1];
					int uid = (Integer) args[2];
					Log.d(TAG,"checkPermission(),permission="+permission+",pid="+pid+",uid="+uid);
//					setResult(HostGlobal.getBaseApplication().checkCallingOrSelfPermission(permission));

				}catch(Exception e){
					Log.e(TAG, e);
					pluginManager.postCrash(e);
				}
				return true;
			}
		});

		ActivityManagerHacker.hook().setHookHandlerByName("checkPermissionWithToken", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method,
									Object[] args) {
				// TODO Auto-generated method stub

				PluginManager pluginManager = PluginManager.getInstance();

				try{
					String permission = (String)args[0];
					int pid = (Integer) args[1];
					int uid = (Integer) args[2];
					int callerToken = (Integer) args[3];
//					setResult(HostGlobal.getBaseApplication().checkSelfPermission(permission));
					Log.d(TAG,"checkPermissionWithToken(),permission="+permission+",pid="+pid+",uid="+uid+",callerToken="+callerToken);

				}catch(Exception e){
					Log.e(TAG, e);
					pluginManager.postCrash(e);
				}
				return true;
			}
		});


	}
	
	private static boolean isMainThread(){
		return Thread.currentThread().getId() == 1;
	}


	public static void fixServiceCache(Context baseContext,Context pluginBaseContext){
		try {
			Log.d(TAG, "fixServiceCache::baseContext=" + baseContext);
			Log.d(TAG, "fixServiceCache::pluginBaseContext=" + pluginBaseContext);
			Object mServiceCache = RefUtil.getFieldValue(baseContext, "mServiceCache");
			Log.d(TAG, "fixServiceCache::mServiceCache=" + mServiceCache);
			RefUtil.setFieldValue(pluginBaseContext, "mServiceCache", mServiceCache);
		}catch (Exception e){
			Log.e(TAG, e);
		}
	}

	public static void initService(Application app){
		try {
			//让service用宿主的Application实例化，绕开插件的权限问题
			AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
			Log.d(TAG,"initService::AlarmManager am = " + am);
		}catch (Exception e){
			Log.e(TAG, e);
		}
	}
	
	static void hook(final Application app){

		activityThread = ActivityThread.currentActivityThread();

		injectInstrumentation(activityThread);

		/**
		 * 挂钩系统回调
		 */
//		SystemCallbackHacker.hook(activityThread);
		
		/**
		 * 挂钩ActivityManager
		 */
//		hookActivityManager();
		
		//策略一
		//默认主进程不挂钩PackageManager
		//也可以在启动完毕后，延迟挂钩，这样可以忽略性能影响
//		if(!HostGlobal.isMainProcess()){
//			hookPackageManager();
//		}
		
		//策略二
		//主进程延迟500毫秒挂钩，等程序启动起来了再说
		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				hookActivityManager();
				IClipboardHacker.hook();
				ITelephonyHacker.hook();
				IMountServiceHacker.hook();
				INotificationManagerHacker.hook();
//				IAccessibilityManagerHacker.hook();
			}
		};

		initService(app);

		hookPackageManager(app);

		if(!HostGlobal.isMainProcess()){
			run.run();
		}else{
			new Handler().postDelayed(run, 500);
		}
		

//		run.run();
		
//		hookAppOpsManager();
	}

    public static void setup(final Application app) {

//        // 4.0以下版本不支持
//        if (VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH) {
//            return;
//        }
//
//        HostGlobal.init(app);
//        
//        hook();
//        
//        NetworkManager.getInstance(app);
//        
//        app.registerComponentCallbacks(new ComponentCallbacks() {
//			
//			@Override
//			public void onLowMemory() {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onConfigurationChanged(Configuration newConfig) {
//		        // 同步插件中资源的onConfigurationChanged事件
//		        Map<String, Plugin> plugins = PluginManager.getInstance().getPlugins();
//		        for (Plugin p : plugins.values()) {
//		            p.getRes().updateConfiguration(newConfig, app.getResources().getDisplayMetrics());
//		        }
//			}
//		});
        
//        //拦截startActivity调用
//        MyActivityManager.hook().addHookHandler("startActivity", new HookHandler() {
//        	@Override
//        	public boolean onBefore(IActivityManager origin, Method method,
//        			Object[] args) {
//        		// TODO Auto-generated method stub
//        		if(args != null){
//        			
//        			Intent intent = null;
//        			IBinder token = null;
//        			
//        			for(Object arg : args){
//						Log.i(TAG, "arg="+arg);
//        				if(arg != null){
//        					//找到intent参数
//        					if(arg instanceof Intent){
//        						intent = (Intent)arg;
//        					}else if(arg instanceof IBinder){
//        						token = (IBinder)arg;
//        					}
//        				}
//        			}
//
//					Log.i(TAG, "token="+token);
//					Log.i(TAG, "intent="+intent);
//        			if(token != null){
//        				ActivityPoolManager pool = ActivityPoolManager.getInstance();
//        				ProxyInfo pi = pool.getActivityByToken(token);
//        				//从插件内部调用的
//        				if(pi != null){
//        					Log.i(TAG, "Find the proxy class,tag="+pi.tag+",activity="+pi.activity+",className="+pi.className);
//        					//生成调用插件的intent
//        					if(intent != null)
//        						PluginManager.getInstance().makeIntent(pi.activity,pi.tag, intent,pi.activity.getClass());
//        				}
//        			}
//        			
//        		}
//        		return true;
//        	}
//        	@Override
//        	public Object onAfter(IActivityManager origin, Method method,
//        			Object[] args, Object result, Throwable thr) {
//        		// TODO Auto-generated method stub
//        		return super.onAfter(origin, method, args, result, thr);
//        	}
//		});

    }


	
}
