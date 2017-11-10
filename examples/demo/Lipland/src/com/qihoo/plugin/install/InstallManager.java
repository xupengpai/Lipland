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

package com.qihoo.plugin.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IActivityManager.ContentProviderHolder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.Service;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import android.text.TextUtils;

import com.qihoo.common.ormapping.DDLUtils;
import com.qihoo.plugin.Config;
import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.base.PluginHelper;
import com.qihoo.plugin.base.PluginProcessListener;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.core.ContentProviderFaker;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.db.PluginsDBHelper;
import com.qihoo.plugin.update.UpdateManager;
import com.qihoo.plugin.util.ApkUtil;
import com.qihoo.plugin.util.IO;
import com.qihoo.plugin.util.MD5Util;
import com.qihoo.plugin.util.RefUtil;

/**
 * 管理插件的安装、卸载、查询、数据解析等
 * @author xupengpai
 * @date 2015年11月18日 上午11:56:47
 */
public class InstallManager {
	
	private final static String TAG = InstallManager.class.getSimpleName();
	private final static String DB_TABLE_NAME = "installed";
	
	private Application application;
	private ActivityThread activityThread;
	private UpdateManager updateManager;

	public final static int DEFAULT_THREAD_MAX_COUNT = 30;
	
	private Map<String,PluginPackage> installedPlugins;
	private PluginManager pluginManager;

	private ExecutorService threadPool;
	
	public UpdateManager getUpdateManager() {
		return updateManager;
	}

	
	public Map<String, PluginPackage> getInstalledPlugins() {
		return installedPlugins;
	}
	
	public class PluginInstallReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(Actions.ACTION_PLUGIN_INSTALLED.equals(intent.getAction())){
				//接收到插件安装成功消息
				Log.i(TAG, "PluginInstallReceiver::onReceive(),ACTION_PLUGIN_INSTALLED");
				PluginInfo pi = (PluginInfo)intent.getSerializableExtra(Actions.DATA_PLUGIN_INFO);
				
				//如果已经加载旧版本，则继续使用旧版本，避免数据不对称导致插件异常
				if(pi != null && !pluginManager.isLoaded(pi.tag)){
					parseAndPut(pi,true);
				}
			}
		}
	}

	private void installProviderFor8_0(ArrayList<Provider> providers){

		Log.w(TAG, "installProvider()::installProviderFor8_0");
		Method method = RefUtil.getDeclaredMethod(ActivityThread.class, "installProvider", new Class<?>[]{
				Context.class,IContentProvider.class,ProviderInfo.class,boolean.class,boolean.class,boolean.class
		});
		Log.w(TAG, "installProvider()::installProviderFor8_0,method="+method);
		Log.w(TAG, "installProvider()::installProviderFor8_0,providers="+providers);

		if(method != null){

			method.setAccessible(true);

			for(Provider prov : providers){
				try {
					method.invoke(activityThread, application,new ContentProviderFaker(application),prov.info,false,true,true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, e);
				}
			}
		}
	}
	
	private void installProviders(ArrayList<Provider> providers){
		//4.1.1或以上
//		private IActivityManager.ContentProviderHolder installProvider(Context context,
//				4515            IActivityManager.ContentProviderHolder holder, ProviderInfo info,
//				4516            boolean noisy, boolean noReleaseNeeded, boolean stable) {
//				4517        ContentProvider localProvider = null;
//				4518        IContentProvider provider;



//		if(Build.VERSION.SDK_INT >= 26){
//
//			installProviderFor8_0(providers);
//
//		//else后面是老代码，还是按照8.0以前的处理
//		}else
		{


			Method method = RefUtil.getDeclaredMethod(ActivityThread.class, "installProvider", new Class<?>[]{
					Context.class,IActivityManager.ContentProviderHolder.class,ProviderInfo.class,boolean.class,boolean.class,boolean.class
			});


			if(method != null){
				method.setAccessible(true);
				for(Provider prov : providers){
					ContentProviderHolder holder = new ContentProviderHolder(prov.info);
					holder.noReleaseNeeded = true;
					holder.provider = new ContentProviderFaker(application);
					try {
						method.invoke(activityThread, application,holder,prov.info,false,true,true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, e);
					}
				}
			}else{
				Log.w(TAG, "installProvider()::v4.1.1 No such method");
				method = RefUtil.getDeclaredMethod(ActivityThread.class, "installProvider", new Class<?>[]{
						Context.class,IContentProvider.class,ProviderInfo.class,boolean.class,boolean.class
				});
				if(method != null){

					method.setAccessible(true);

					for(Provider prov : providers){
						try {
							method.invoke(activityThread, application,new ContentProviderFaker(application),prov.info,false,true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.e(TAG, e);
						}
					}
				}
//			private IContentProvider installProvider(Context context,
//					4193            IContentProvider provider, ProviderInfo info,
//					4194            boolean noisy, boolean noReleaseNeeded) {
			}
		}


		
	}
	
	/**
	 * 异步解析已经安装的插件
	 * 该方法在解析前会生成一个空对象到已安装插件集合中，并将其中的锁对象锁定，在解析完成后释放锁对象
	 * 如果解析失败,error字段置为true
	 * 因此从插件集合中获取插件对象时，应该先获取锁对象，然后检测error字段
	 * 所有插件在每个进程中只解析一次
	 * 
	 * 当调用getInstalledPlugin()时，锁对象可以帮助它等待插件解析结束
	 * 
	 * @param pi
	 */
	private void parseAndPut(final PluginInfo pi,final boolean install){
		
		final PluginPackage pluginPackage = new PluginPackage();
		
		pluginPackage.pi = pi;
		pluginPackage.tag = pi.tag;

		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				synchronized () {

				Log.i(TAG, "parse " + pi.path);
				android.content.pm.PackageParser.Package pkg = null;
				try {
					if (new File(pi.path).isFile()) {
						pkg = ApkUtil.parseApkInfo(pi.path);
					} else {
						pluginPackage.error = true;
						pluginPackage.parseException = new FileNotFoundException(pi.path);
					}

					if (pkg != null) {

						pluginPackage.pkg = pkg;
						pi.packageName = pkg.packageName;
						pkg.applicationInfo.uid = Process.myUid();
						pkg.applicationInfo.nativeLibraryDir = Config.getLibPath(pi.tag);
						pkg.applicationInfo.sourceDir = Config.getCurrentProcessPluginWorkDir(pi.tag);
						pkg.applicationInfo.publicSourceDir = pkg.applicationInfo.sourceDir;
//						pkg.applicationInfo.publicSourceDir = Config.getCurrentProcessPluginWorkDir(pi.tag);

						pkg.applicationInfo.dataDir = Config.getPluginDataDir(pi);

//						String deviceProtectedDataDir = (String) RefUtil.getFieldValue(pkg.applicationInfo,"deviceProtectedDataDir");
//						String credentialProtectedDataDir = (String) RefUtil.getFieldValue(pkg.applicationInfo,"credentialProtectedDataDir");
//						System.out.println("deviceProtectedDataDir="+deviceProtectedDataDir);
//						System.out.println("credentialProtectedDataDir="+credentialProtectedDataDir);
						RefUtil.setFieldValue(pkg.applicationInfo, "deviceProtectedDataDir", pkg.applicationInfo.dataDir);
						RefUtil.setFieldValue(pkg.applicationInfo, "credentialProtectedDataDir", pkg.applicationInfo.dataDir);
						pkg.applicationInfo.metaData = pkg.mAppMetaData;

						if (pkg.providers != null && pkg.providers.size() > 0) {
							try {
								installProviders(pkg.providers);
							}catch (Throwable thr){
								Log.e(TAG, thr);
							}
						}

					} else {
						pluginPackage.error = true;
					}

				} catch (Throwable e) {
					Log.e(TAG, e);
					pluginPackage.error = true;
					pluginPackage.parseException = e;
				} finally {
					//无论如何要解锁，避免ANR

					if (pluginPackage.error) {
						installedPlugins.remove(pi.tag);
						deletePlugin(pi.tag);
					}

					pluginPackage.syncLock.unlock();
				}


//				if(install) {
//
//					//本来在安装时做这个工作，但由于在某些情况下，classloader加载还是会耗时比较大，尝试放在这里解决。
//					threadPool.execute(new Runnable() {
//						@Override
//						public void run() {
//
//							//这里提前做一些针对加载插件时需要做的事情，加快加载速度和启动速度。
//							//如拷贝到工作目录，加载一次(第一次加载需要编译所以耗时)，解压so(解压一次)
//							pluginManager.preproccessPlugin(pi);
//						}
//					});
//				}


			}
		};
		
		Thread thread = new Thread(run);

		//先锁定对象再进行解析
		pluginPackage.syncLock.lock(thread);
		installedPlugins.put(pi.tag, pluginPackage);
		thread.start();
		
//		threadPool.execute();
		
		
	}
	


	public InstallManager(PluginManager pluginManager,ActivityThread activityThread,Application application,boolean useUpdateManager){
		this.pluginManager = pluginManager;
		this.application = application;
		this.activityThread = activityThread;
		
		if(useUpdateManager){
			this.updateManager = UpdateManager.getInstance();
			try {
				this.updateManager.init(this);
			} catch (Exception e) {
				Log.e(TAG, e);
			}
		}
		
		//注册安装监听，以便接收安装成功事件，不论是从哪个进程发来的
		application.registerReceiver(new PluginInstallReceiver(), new IntentFilter(Actions.ACTION_PLUGIN_INSTALLED));
		installedPlugins = new HashMap<String, PluginPackage>();
		threadPool = Executors
				.newFixedThreadPool(DEFAULT_THREAD_MAX_COUNT);

		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				initData();
			}
		};
		
		/**
		 * 如果在主进程，则在线程中初始化已安装插件数据，优点是不影响启动速度，缺点是：不能立马在主进程中运行插件
		 * 如果是在其他进程，则直接初始化已安装插件数据。
		 */
		if(HostGlobal.isMainProcess()){
			threadPool.execute(run);
		}else{
			run.run();
		}
		
	}

	/**
	 * 查询已经安装的插件，这里查询到的插件不一定已经解析到内存，只能获取插件基本信息
	 * @return
	 */
	public List<PluginInfo> queryInstalledPluginInfo(){
		return queryAll();
	}

	private void initData(){
		List<PluginInfo> pluginInfoList = queryAll();
		if(pluginInfoList != null){
			for(PluginInfo pi : pluginInfoList){
//				parseAndPut(pi);
				/**
				 * 这样写，避免覆盖安装后再次解析插件 
				 * 在重启插件进程之前，插件永远只解析一次
				 */
				getInstalledPlugin(pi.tag);
			}
		}
	}

	/**
	 * 版本字符串转换成版本整形，如：1.0.1，对应1000001，
	 * 字符串每小节对应3位数字，所以整形版本最大为999999999
	 * @param versionName
	 * @return
	 */
	private static int versionNameToVersionCode(String versionName){
		
        String[] aVerBits = versionName.split("\\.");

        int aVerBit1 = Integer.parseInt(aVerBits[0]);
        int aVerBit2 = Integer.parseInt(aVerBits[1]);
        int aVerBit3 = Integer.parseInt(aVerBits[2]);

        return Integer.parseInt(String.format("%03d%03d%03d", aVerBit1, aVerBit2, aVerBit3));

	}
	
	/**
	 * 整形版本转换成字符串版本，如：1000001，对应1.0.1
	 * 整形每3位对应字符串一个小节，整形版本最大为999999999
	 * @param versionCode
	 * @return
	 */
	private static String versionCodeToVersionName(int versionCode){
		
		String str = versionCode + "";
		String v1 = "0";
		String v2 = "0";
		String v3 = "0";
		
		if(str.length() > 6){
			int len = str.length() - 6;
			v1 = str.substring(0,len);
			v2 = str.substring(len,len+3);
			v3 = str.substring(len+3,len+6);
		}else if(str.length() > 3){
			int len = str.length() - 3;
			v2 = str.substring(0,len);
			v3 = str.substring(len,len+3);
		}else{
			v3 = str;
		}
        return String.format("%s.%s.%s", Integer.parseInt(v1), Integer.parseInt(v2), Integer.parseInt(v3));

	}
	
	
	/**
	 * 安装一个插件
	 * @param tag 插件唯一标识
	 * @param versionCode 插件版本号，最大为999999999
	 * @param apkPath 插件路径
	 */
	public boolean install(String tag,int versionCode,String apkPath){

		if(TextUtils.isEmpty(tag) || TextUtils.isEmpty(apkPath)){
			Log.e(TAG, "install::argments error,tag="+tag+",versionCode="+versionCode+",apkPath="+apkPath);
			return false;
		}
		
		File apkFile = new File(apkPath);
		if(!apkFile.isFile()){
			Log.e(TAG, "install::error,File does not exist or is not a file!,apkPath="+apkPath);
			return false;
		}

		Log.i(TAG, "install::tag="+tag+",versionCode="+versionCode+",apkPath="+apkPath);
		PluginInfo info = new PluginInfo();
		info.tag = tag;
		info.versionCode = versionCode;
		info.path = apkPath;

		Log.i(TAG, "install::tag="+tag+",versionCode="+versionCode+",apkPath="+apkPath);
		byte[] bytes = null;
		try {
			bytes = IO.readBytes(apkPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e);
		}

		if(bytes != null){
			String md5 = MD5Util.md5str(bytes);
			info.md5 = md5;
			return install(info,true);
		}else{
			Log.e(TAG, "install::error,file bytes=null");
		}

		return false;

	}

	/**
	 * 安装一个插件
	 * @param tag 插件唯一标识
	 * @param versionName 插件版本号，格式为：xxx.xxx.xxx
	 * @param apkPath 插件路径
	 */
	public boolean install(String tag,String versionName,String apkPath){
		if(TextUtils.isEmpty(versionName)){
			Log.e(TAG, "install::argments error,versionName="+versionName);
			return false;
		}
		return install(tag,versionNameToVersionCode(versionName),apkPath);
	}
	
	/**
	 * 查询插件是否已经安装
	 * @param tag
	 * @return
	 */
	public boolean isInstalled(String tag){
//		return queryInstalled(tag,false) != null;
		return installedPlugins.containsKey(tag) || queryInstalled(tag, false)!=null;
	}

	/**
	 * 查询插件信息
	 * @param tag
	 * @return
	 */
	public PluginPackage getInstalledPlugin(String tag){
		PluginPackage pluginPackage = installedPlugins.get(tag);
		if(pluginPackage != null){

			//先获取锁，避免正在解析的情况
			pluginPackage.syncLock.lock();

			try{
				//如果之前已经解析并且出错，则直接返回，避免死循环
				if(pluginPackage.error)
					return null;
			
				return pluginPackage;
				
			}finally{
				pluginPackage.syncLock.unlock();
			}
			
		}else{
			PluginInfo pi = queryInstalled(tag, true);
			if(pi != null){
				parseAndPut(pi,false);
				return getInstalledPlugin(tag);
			}
		}
		return null;
	}
	
	public PluginInfo queryInstalled(String tag,boolean needResult){
		PluginsDBHelper dbhelper = null;
		SQLiteDatabase db = null;
    	Cursor cursor = null;
        try {
        	dbhelper = new PluginsDBHelper(application);
        	db = dbhelper.getReadableDatabase(); 
        	cursor = db.query(DB_TABLE_NAME, null, "tag = ?", new String[]{tag}, null,null,null);
        	if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()){
        		if(needResult)
        			return DDLUtils.toObject(cursor, PluginInfo.class);
        		else
        			return new PluginInfo();
        	}
        } catch (Exception e) {
            Log.e(TAG, e);
            return null;
        } finally {
        	
        	if(cursor != null)
        		cursor.close();
        	
            if (dbhelper != null) {
            	dbhelper.close();
            }
        }
        return null;
	}
	
	private List<PluginInfo> queryAll(){
		PluginsDBHelper dbhelper = null;
		SQLiteDatabase db = null;
    	Cursor cursor = null;
		List<PluginInfo> list = new ArrayList<PluginInfo>();
       
		try {
			dbhelper = new PluginsDBHelper(application);
	        db = dbhelper.getReadableDatabase(); 
	        cursor = null;
        	cursor = db.query(DB_TABLE_NAME, null, null , null, null,null,null);
        	if(cursor != null && cursor.getCount() > 0){
	        	while(cursor.moveToNext()){
	        		list.add(DDLUtils.toObject(cursor, PluginInfo.class));
	        	}
        	}
        	return list;
        } catch (Exception e) {
            Log.e(TAG, e);
            return null;
        } finally {
        	
        	if(cursor != null)
        		cursor.close();
        	
            if (dbhelper != null) {
            	dbhelper.close();
            }
        }
	}

	public void uninstall(String tag){
		deletePlugin(tag);
		installedPlugins.remove(tag);
	}
	
//	private List<PluginInfo> queryAll(){
//		ContentResolver resolver = this.application.getContentResolver();
//		List<PluginInfo> list = new ArrayList<PluginInfo>();
//		PluginsDBHelper dbhelper = new PluginsDBHelper(application);
//        SQLiteDatabase db = dbhelper.getReadableDatabase(); 
//        Cursor cursor = null;
//        try {
//        	cursor = db.query(DB_TABLE_NAME, null, null , null, null,null,null);
//        	if(cursor != null && cursor.getCount() > 0){
//	        	while(cursor.moveToNext()){
//	        		list.add(DDLUtils.toObject(cursor, PluginInfo.class));
//	        	}
//        	}
//        	return list;
//        } catch (Exception e) {
//            Log.e(TAG, e);
//            return null;
//        } finally {
//        	
//        	if(cursor != null)
//        		cursor.close();
//        	
//            if (dbhelper != null) {
//            	dbhelper.close();
//            }
//        }
//	}
	
	private boolean addPluginInfo(PluginInfo info){
		PluginsDBHelper dbhelper = null;
		SQLiteDatabase db = null;
        try {
        	dbhelper = new PluginsDBHelper(application);
            db = dbhelper.getWritableDatabase(); 
        	ContentValues values = DDLUtils.getContentValues(info);
        	db.insert(DB_TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e(TAG, e);
            return false;
        } finally {
            if (dbhelper != null) {
            	dbhelper.close();
            }
        }
        return true;
	}
	
	private void deletePlugin(String tag){
		PluginsDBHelper dbhelper = null;
		SQLiteDatabase db = null;
        try {
        	dbhelper = new PluginsDBHelper(application);
            db = dbhelper.getWritableDatabase(); 
        	db.delete(DB_TABLE_NAME, "tag = ?", new String[]{tag});
        } catch (Exception e) {
            Log.e(TAG, e);
        } finally {
            if (dbhelper != null) {
            	dbhelper.close();
            }
        }
	}
	
	
//	private boolean doInstall(PluginInfo info){
//		//安装前先卸载
//		uninstallPlugin(info.tag);
//		if(addPluginInfo(info)){
//			Intent intent = new Intent(ACTION_PLUGIN_INSTALLED);
//			intent.putExtra(DATA_PLUGIN_INFO, info);
//
//			PluginPackage pluginPackage = new PluginPackage();
//			pluginPackage.syncLock.lock();
//			installedPlugins.put(info.tag, pluginPackage);
//			this.application.sendBroadcast(intent);
//			return true;
//		}
//		return false;
//	}

	//版本号转换
	private void revertVersion(PluginInfo info){
		if(TextUtils.isEmpty(info.versionName)){
			info.versionName = versionCodeToVersionName(info.versionCode);
		}else{
			info.versionCode = versionNameToVersionCode(info.versionName);
		}
	}
	
//	private void createPluginPackage(PluginInfo info){
//		
//		Intent intent = new Intent(ACTION_PLUGIN_INSTALLED);
//		intent.putExtra(DATA_PLUGIN_INFO, info);
//
//		PluginPackage pluginPackage = new PluginPackage();
//		pluginPackage.syncLock.lock();
//		installedPlugins.put(info.tag, pluginPackage);
//		this.application.sendBroadcast(intent);
//	}
	
	public boolean install(final PluginInfo info,boolean syncPreproccess){

		File apkFile = new File(info.path);

		//如果插件不在安装目录下，则先拷贝到安装目录
		if(!apkFile.getParentFile().equals(new File(Config.getPluginDir()))){
			String installPath = Config.getPluginDir() + "/" + apkFile.getName();
			IO.copy(info.path , installPath);
			info.path = installPath;
		}

		revertVersion(info);
		
		boolean isInstalled = (getInstalledPlugin(info.tag) != null);

		if(isInstalled){
			//安装前先卸载
			deletePlugin(info.tag);
		}

		String packageName = ApkUtil.getApkPackageName(application, info.path);
        if(packageName != null){
        	info.packageName = packageName;
        }
        
		if(addPluginInfo(info)){


			if(pluginManager != null){
				pluginManager.cleanPlugin(info.tag);

				Runnable preproccess = new Runnable() {
					@Override
					public void run() {

						if(pluginManager.isPluginProcess()){
								//这里提前做一些针对加载插件时需要做的事情，加快加载速度和启动速度。
							//如拷贝到工作目录，加载一次(第一次加载需要编译所以耗时)，解压so(解压一次)
							if(!pluginManager.isLoaded(info.tag))
							{
								Log.d(TAG, "install(),preproccessPlugin()..info.tag="+info.tag+",info.path="+info.path);
								pluginManager.preproccessPlugin(info.tag, info.path, false);
							}else{
								Log.d(TAG, "install(),addUnprocessdPlugin()..info.tag="+info.tag+",info.path="+info.path);
								pluginManager.addUnprocessdPlugin(info.tag, info.path);
							}
						}else {

							PluginHelper.startPluginProcess(new PluginProcessListener() {
								@Override
								public void onConnected() {
									//这里提前做一些针对加载插件时需要做的事情，加快加载速度和启动速度。
									//如拷贝到工作目录，加载一次(第一次加载需要编译所以耗时)，解压so(解压一次)
									if (!PluginHelper.isPluginLoaded(info.tag)) {
										Log.d(TAG, "install(),onConnected(),preproccessPlugin()..info.tag=" + info.tag + ",info.path=" + info.path);
										pluginManager.preproccessPlugin(info.tag, info.path, false);
									} else {
										Log.d(TAG, "install(),onConnected(),addUnprocessdPlugin()..info.tag=" + info.tag + ",info.path=" + info.path);
										pluginManager.addUnprocessdPlugin(info.tag, info.path);
									}
								}

								@Override
								public void onReady() {

								}

								@Override
								public void onDisconnected() {

								}

								@Override
								public void onException(Exception e) {
									Log.e(TAG, "install(),onException(),addUnprocessdPlugin()..info.tag=" + info.tag + ",info.path=" + info.path);
									pluginManager.addUnprocessdPlugin(info.tag, info.path);
								}
							});
						}
//						pluginManager.preproccessPluginLibrary(info);
					}
				};

				//由于插件更新进程更新完后会杀掉自身，如果这里异步工作的话，可能会导致预处理工作被中断。
				//所以这里通过一个变量来决定是否异步预处理。
				if(syncPreproccess){
					threadPool.execute(preproccess);
				}else{
					preproccess.run();
				}
			}

			try {
				IO.writeString(info.path + ".md5",info.md5);
			} catch (IOException e) {
				Log.e(TAG, e);
			}

			//只有新增的插件(非覆盖安装)，才广播安装消息，目的是避免插件在某个进程已经加载了该插件的情况下导致各进程间插件信息不统一的问题。
			//最佳方案是，检测插件是否被加载，没被加载则发出广播，但比较复杂。
//			if(!isInstalled){
			Intent intent = new Intent(Actions.ACTION_PLUGIN_INSTALLED);
			intent.putExtra(Actions.DATA_PLUGIN_INFO, info);
			this.application.sendBroadcast(intent);
//			}


			return true;
		}
		return false;
		
	}

//  /**
//   * 根据action和插件tag查找插件的Activity信息
//   * 如果tag为null，则返回所有匹配action规则的Activity信息
//   * @param tag
//   * @param className
//   * @return
//   */
//  public List<ActivityInfo> queryActivityInfoByAction(String tag,Intent intent){
//  	PluginManager pluginManager = PluginManager.getInstance();
//  	List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
//  	
//  	//如果插件已经加载，则直接从内存里面寻找Activity信息
//  	if(tag != null && pluginManager.isLoaded(tag)){
//  		Plugin plugin = pluginManager.getPlugin(tag);
//  		ActivityInfo ai = plugin.findActivity(className);
//  		if(ai != null){
//  			activities.add(ai);
//  		}else{
//  			Log.e(TAG, "queryActivityInfoByClassName:: ActivityInfo is not found in memory,tag="+tag);
//  		}
//  		return activities;
//  	}
//  	
//  	//从数据库中查找信息
//  	
//  	return activities;
//  	
//  }
	

	public final static int COMPONENT_TYPE_ACTIVITY = 0;
	public final static int COMPONENT_TYPE_SERVICE = 1;
	public final static int COMPONENT_TYPE_RECEIVER = 2;

	public static class ComponentIntentResult{
		public PackageParser.Component component;
		public IntentInfo intentInfo;
	}
	
	private List<ComponentIntentResult> findComponentByAction(List<PackageParser.Component> components,Intent intent,int count){

		Log.d(TAG,"findComponentByAction(), intent = " + intent);
		List<ComponentIntentResult> list = new ArrayList<>();
		for(Component component : components){
			for(IntentInfo intentInfo : ((Component<IntentInfo>)component).intents){

				Log.d(TAG,"findComponentByAction(), intentInfo = " + intentInfo);
				Log.d(TAG,"findComponentByAction(), match = " + intentInfo.match(null, intent, false, TAG));

//				int c = intentInfo.countDataSchemes();
//				for(int i=0;i<c;i++){
//					Log.d(TAG,"findComponentByAction(), scheme = " + intentInfo.getDataScheme(i));
//				}
//
//				for(int i=0;i<c;i++){
//					Log.d(TAG,"findComponentByAction(), path = " + intentInfo.getDataPath(i));
//				}
//
//				c = intentInfo.countDataPaths();
//				for(int i=0;i<c;i++){
//					Log.d(TAG,"findComponentByAction(), path = " + intentInfo.getDataPath(i));
//				}


				if(intentInfo.match(null, intent, false, TAG) >= 0){
					ComponentIntentResult result = new ComponentIntentResult();
					result.component = component;
					result.intentInfo = intentInfo;
					list.add(result);
					if(list.size() == count)
						break;
				}
			}
		}
		return list;
	}


	private List<ComponentIntentResult> findComponentByAction(PluginPackage pluginPackage,Intent intent,int type,int count){

		if(pluginPackage != null && count != 0 && pluginPackage.pkg != null){
//			pluginPackage.syncLock.lock();
			try{
				switch(type){
				case COMPONENT_TYPE_ACTIVITY:
					return findComponentByAction((List<PackageParser.Component>)(Object)pluginPackage.pkg.activities, intent, count);
				case COMPONENT_TYPE_SERVICE:
					return findComponentByAction((List<PackageParser.Component>)(Object)pluginPackage.pkg.services, intent, count);
				case COMPONENT_TYPE_RECEIVER:
					return findComponentByAction((List<PackageParser.Component>)(Object)pluginPackage.pkg.receivers, intent, count);
				}
			}finally{
//				pluginPackage.syncLock.unlock();
			}
		}
		
		return null;
	}
	
	
	/**
	 * action匹配查找组件，count为-1时返回所有匹配项，否则返回<=count个
	 * @param type
	 * @param tag
	 * @param intent
	 * @param count
	 * @return
	 */
	private List<ComponentIntentResult> findComponentByAction(int type,String tag,Intent intent,int count){
//		if(tag != null){
//			if(isInstalled(tag)){
//				return findComponentByAction(getInstalledPlugin(tag), intent,type,-1);
//			}else{
//				Log.e(TAG,
//						"startActivity:: Plugin is not installed,please call to install () for installation. tag="
//								+ tag);
//			}
//		}else{
			synchronized (installedPlugins) {

				List<ComponentIntentResult> all = new ArrayList<>();

				String pkgName = intent.getPackage();
				Iterator<PluginPackage> iter = installedPlugins.values().iterator();
				boolean isPackageNameEmpty = TextUtils.isEmpty(pkgName);
				while (iter.hasNext()) {

					PluginPackage pp = iter.next();

					if((!isPackageNameEmpty && pkgName.equals(pp.pi.packageName)) || isPackageNameEmpty){
						List<ComponentIntentResult> list = findComponentByAction(pp, intent, type, -1);
						if (list != null)
							all.addAll(list);
					}
				}
				return all;
			}
//		}
		
//		return null;
	}

	private Component findComponent(List<Component> components,String className){
		if(components != null){
			for(Component<IntentInfo> comp : components){
				if(comp.className.equals(className))
					return comp;
			}
		}
		return null;
	}
	
	private Component findComponent(int type,PluginPackage pluginPackage,String className){
		if(pluginPackage != null && pluginPackage.pkg != null){
			//等待解析完毕，可能导致耗时比较多
			//			pluginPackage.syncLock.lock();
			try{
				switch(type){
				case COMPONENT_TYPE_ACTIVITY:
					return findComponent((List<PackageParser.Component>)(Object)pluginPackage.pkg.activities, className);
				case COMPONENT_TYPE_SERVICE:
					return findComponent((List<PackageParser.Component>)(Object)pluginPackage.pkg.services, className);
				case COMPONENT_TYPE_RECEIVER:
					return findComponent((List<PackageParser.Component>)(Object)pluginPackage.pkg.receivers, className);
				}			
			}finally{
		//		pluginPackage.syncLock.unlock();
			}				
			
			
		}
		return null;
	}

	private List<PackageParser.Component> findComponentByClassName(int type,String tag,String className,int count){
		List<PackageParser.Component> all = new ArrayList<PackageParser.Component>();
		if(tag != null){
			if(isInstalled(tag)){
				Component component = findComponent(type,getInstalledPlugin(tag), className);
				if(component != null){
					all.add(component);
				}
			}else{
				Log.e(TAG,
						"findComponentByClassName:: Plugin is not installed,please call to install () for installation. tag="
								+ tag);
			}
		}else{
			synchronized (installedPlugins) {
				Iterator<PluginPackage> iter = installedPlugins.values().iterator();
				while(iter.hasNext()){
					Component component = findComponent(type,iter.next(), className);
					if(component != null){
						all.add(component);
					}
					if(all.size() == count)
						break;
				}
			}
		}
		return all;
	}

	private PackageParser.Component findComponentByClassName(int type,ComponentName cn){
		PluginPackage pluginPackage = queryPluginByPackageName(cn.getPackageName());
		if(pluginPackage != null && pluginPackage.pkg != null){
			return findComponent(type,pluginPackage, cn.getClassName());
		}
		return null;
		
	}

	private List<PackageParser.Activity> findReceiverByAction(PluginPackage pluginPackage,Intent intent){
		List<PackageParser.Activity> list = new ArrayList<PackageParser.Activity>();
		if(pluginPackage != null && pluginPackage.pkg != null){
//			pluginPackage.syncLock.lock();
			try{
				for(Activity activity : pluginPackage.pkg.receivers){
					for(ActivityIntentInfo intentInfo : activity.intents){
						if(intentInfo.match(null, intent, false, TAG) >= 0){
							list.add(activity);
						}
					}
				}
			}finally{
//				pluginPackage.syncLock.unlock();
			}
		}
		return list;
	}

	

	/**
	 * 根据activity对象查询插件信息
	 * @param activity
	 * @return
	 */
	public PluginPackage queryPluginInfoByActivity(Component activity){
		return queryPluginInfo(COMPONENT_TYPE_ACTIVITY,activity);
	}
	
	/**
	 * 根据activity对象查询插件信息
	 * @param activity
	 * @return
	 */
	private boolean queryPluginInfo(List<Component> components,Component component){
		for(Component c : components){
			if(c.equals(component))
				return true;
		}			
		return false;
	}
	
	private boolean queryPluginInfo(int type,PluginPackage pluginPackage,Component component){
		if(pluginPackage != null){
			pluginPackage.syncLock.lock();
			try{
				switch(type){
				case COMPONENT_TYPE_ACTIVITY:
					return queryPluginInfo((List<PackageParser.Component>)(Object)pluginPackage.pkg.activities, component);
				case COMPONENT_TYPE_SERVICE:
					return queryPluginInfo((List<PackageParser.Component>)(Object)pluginPackage.pkg.services, component);
				case COMPONENT_TYPE_RECEIVER:
					return queryPluginInfo((List<PackageParser.Component>)(Object)pluginPackage.pkg.receivers, component);
				}
			}finally{
				pluginPackage.syncLock.unlock();
			}
		}
		return false;
	}
	
	public PluginPackage queryPluginInfo(int type,Component component){
		synchronized (installedPlugins) {
			Iterator<PluginPackage> iter = installedPlugins.values().iterator();
			while(iter.hasNext()){
				
				PluginPackage pluginPackage = iter.next();
	
				if(pluginPackage != null){
					if(queryPluginInfo(type,pluginPackage,component)){
						return pluginPackage;
					}
				}
			}
			return null;
		
		}
	}
	
	public PluginPackage queryPluginInfoByService(Service service){
		return queryPluginInfo(COMPONENT_TYPE_SERVICE,service);
	}
	
	/**
	 * 根据Provider对象查询插件信息
	 * @param provider
	 * @return
	 */
	public PluginPackage queryPluginInfoByProvider(Provider provider){
		synchronized (installedPlugins) {
			Iterator<PluginPackage> iter = installedPlugins.values().iterator();
			while(iter.hasNext()){
				
				PluginPackage pluginPackage = iter.next();
	
				if(pluginPackage != null){
					pluginPackage.syncLock.lock();
					try{
						for(Provider prov : pluginPackage.pkg.providers){
							if(prov.equals(provider))
								return pluginPackage;
						}
					}finally{
						pluginPackage.syncLock.unlock();
					}
				}
			}
			return null;
		}
	}
	
	public List<PackageParser.Activity> queryReceiverByAction(Intent intent){
		synchronized (installedPlugins) {
			Iterator<PluginPackage> iter = installedPlugins.values().iterator();
			List<PackageParser.Activity> all = new ArrayList<PackageParser.Activity>();
			while(iter.hasNext()){
				List<PackageParser.Activity> list = findReceiverByAction(iter.next(),intent);
				if(list != null)
					all.addAll(list);
			}	
			return all;
		}
	}

	private Provider findProvider(PluginPackage pluginPackage,String uri){
		if(pluginPackage != null && pluginPackage.pkg != null){
//			pluginPackage.syncLock.lock();
			try{
				for(Provider provider : pluginPackage.pkg.providers){
					if(uri.equals("content://"+provider.info.authority)||
							uri.startsWith("content://"+provider.info.authority+"/")
							){
						return provider;
					}
				}
			}finally{
//				pluginPackage.syncLock.unlock();
			}
		}
		return null;
	}
	
	/**
	 * 查询一个Provider信息
	 * @param tag
	 * @param uri
	 * @return
	 */
	public Provider queryProvider(String tag,String uri){
		
		if(tag != null){
			PluginPackage pluginPackage = getInstalledPlugin(tag);
			if(pluginPackage != null)
				return findProvider(pluginPackage,uri);
		}
		return null;
		
	}

	public List<PackageParser.Provider> queryProvider(String uri,int count){

		synchronized (installedPlugins) {
			List<PackageParser.Provider> all = new ArrayList<PackageParser.Provider>();
			
			Iterator<PluginPackage> iter = installedPlugins.values().iterator();
			while(count != 0 && iter.hasNext()){
				
				Provider provider = findProvider(iter.next(),uri);
				if(provider != null)
					all.add(provider);
				if(all.size() == count)
					break;
				
			}	
			return all;
		}
	}

	/**
	 * 查找一个符合条件的Provider，按照android规范来说，必然只能有一个符合条件
	 * @param uri
	 * @return
	 */
	public Provider queryFirstProvider(String uri){
		List<PackageParser.Provider> all = queryProvider(uri,1);
		if(all.size() > 0)
			return all.get(0);
		return null;
	}

	/**
	 * ---------------------------------Service--------------------------------------
	 */
	
	/**
	 * 
	 * @param tag
	 * @param className
	 * @param count
	 * @return
	 */
	public List<PackageParser.Service> queryServicesByClassName(String tag,String className,int count){


		return (List<PackageParser.Service>)(Object)findComponentByClassName(COMPONENT_TYPE_SERVICE, tag, className,count);
	}

	public List<PackageParser.Service> queryServicesByAction(String tag,Intent intent,int count){
		return (List<PackageParser.Service>)(Object)findComponentByAction(COMPONENT_TYPE_SERVICE, tag, intent,count);
	}

//	public List<PackageParser.Service> queryServices(String tag,Intent intent){
//		
//		ComponentName name = intent.getComponent();
//		String className = null;
//		if(name != null)
//			className = name.getClassName();
//		
//		if(className != null){
//			return (List<PackageParser.Service>)(Object)findComponentByClassName(COMPONENT_TYPE_SERVICE, tag, className,-1);
//		}else{
//			return (List<PackageParser.Service>)(Object)findComponentByAction(COMPONENT_TYPE_SERVICE, tag, intent,-1);
//		}
//	}
	
	/**
	 * 查询services
	 * tag和包名只需要指定一个，两个都指定以包名为准
	 * 包名为null或者为宿主包名，则直接查询tag指定的插件，如果tag也为null，则查询所有的插件
	 * 
	 * @param intent
	 * @return
	 */
	public List<PackageParser.Service> queryServices(String tag,Intent intent){
		
		ComponentName name = intent.getComponent();
		String className = null;
		String pkgName = HostGlobal.getPackageName();
		
		if(name != null)
			className = name.getClassName();
		if(name != null)
			pkgName = name.getPackageName();
		
		
		if(className != null){
			//如果包名为宿主包名，则忽略包名，查询所有插件
			if(pkgName.equals(HostGlobal.getPackageName())){
				return (List<PackageParser.Service>)(Object)findComponentByClassName(COMPONENT_TYPE_SERVICE,tag,className,-1);
			}else{
				//非宿主包名，则直接用包名+类名查询
				List<PackageParser.Service> list = new ArrayList<PackageParser.Service>();
				PackageParser.Service component = (PackageParser.Service)findComponentByClassName(COMPONENT_TYPE_SERVICE, name);
				if(component != null){
					list.add(component);
					return list;
				}
				return null;
			}
		}else{
			List<ComponentIntentResult> resultList = findComponentByAction(COMPONENT_TYPE_SERVICE, tag, intent,-1);
			List<PackageParser.Service> list = new ArrayList<PackageParser.Service>();
			if(resultList != null && resultList.size() > 0){
				for(ComponentIntentResult result : resultList){
					list.add((Service) result.component);
				}
				return list;
			}
			return list;
		}
		
	}

	/**
	 * ---------------------------------Activity--------------------------------------
	 */
	
	/**
	 * 
	 * @param tag
	 * @param className
	 * @param count
	 * @return
	 */
	public List<PackageParser.Activity> queryActivitiesByClassName(String tag,String className,int count){
		return (List<PackageParser.Activity>)(Object)findComponentByClassName(COMPONENT_TYPE_ACTIVITY, tag, className,count);
	}

	public List<PackageParser.Activity> queryActivitiesByAction(String tag,Intent intent,int count){
		return (List<PackageParser.Activity>)(Object)findComponentByAction(COMPONENT_TYPE_ACTIVITY, tag, intent,count);
	}
	
	public List<ComponentIntentResult> queryActivities(String tag,Intent intent){
		return queryActivities(tag,intent,-1);
		
	}

	//组件是否在宿主中有定义
	public boolean isHostIntent(int type,Intent intent){
		List<ResolveInfo> resolveInfos = null;
		PackageManager pm =  HostGlobal.getBaseApplication().getPackageManager();
		switch (type){
			case COMPONENT_TYPE_ACTIVITY:
				resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
				break;
			case COMPONENT_TYPE_SERVICE:
				resolveInfos = pm.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
			case COMPONENT_TYPE_RECEIVER:
				resolveInfos = pm.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);
				break;
		}

		if (resolveInfos != null && resolveInfos.size() > 0) {

			for(ResolveInfo info : resolveInfos) {
				if (HostGlobal.getPackageName().equals(info.resolvePackageName)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<ComponentIntentResult> queryActivities(String tag,Intent intent,int count){

		List<ComponentIntentResult> list = new ArrayList<>();

		ComponentName name = intent.getComponent();
		String className = null;
		String pkgName = HostGlobal.getPackageName();
		
		if(name != null)
			className = name.getClassName();
		
		if(name != null)
			pkgName = name.getPackageName();
		
//		if(className != null){
//			return (List<PackageParser.Activity>)(Object)findComponentByClassName(COMPONENT_TYPE_ACTIVITY, tag, className,count);
//		}else{
//			return (List<PackageParser.Activity>)(Object)findComponentByAction(COMPONENT_TYPE_ACTIVITY, tag, intent,count);
//		}
//		
		if(className != null){
			//如果包名为宿主包名，则忽略包名，查询所有插件
			if(pkgName.equals(HostGlobal.getPackageName())){
//				return (List<PackageParser.Activity>)(Object)findComponentByClassName(COMPONENT_TYPE_ACTIVITY,tag,className,count);

				List<PackageParser.Component> components = findComponentByClassName(COMPONENT_TYPE_ACTIVITY,tag,className,count);
				if(components != null){
					for(Component component : components) {
						ComponentIntentResult result = new ComponentIntentResult();
						result.component = component;
						result.intentInfo = null;
						list.add(result);
					}
					return list;
				}
			}else{
				//非宿主包名，则直接用包名+类名查询
				PackageParser.Activity component = (PackageParser.Activity)findComponentByClassName(COMPONENT_TYPE_ACTIVITY, name);
				if(component != null){
					ComponentIntentResult result = new ComponentIntentResult();
					result.component = component;
					result.intentInfo = null;
					list.add(result);
					return list;
				}
			}
		}else{
			//action情况
			List<ComponentIntentResult> results = findComponentByAction(COMPONENT_TYPE_ACTIVITY, tag, intent,count);
			if(results != null && results.size() > 0){

				list.addAll(results);
			}
		}
		return list;
		
	}
	
	public PluginPackage queryPluginByPackageName(String packageName){

		Log.d(TAG, "queryPluginByPackageName(),packageName=" + packageName);
		synchronized (installedPlugins) {

			PluginPackage[] pps = installedPlugins.values().toArray(new PluginPackage[]{});
			Log.d(TAG, "queryPluginByPackageName(),pps=" + pps);
			Log.d(TAG, "queryPluginByPackageName(),pps.length=" + pps.length);
			for(int i=0;i<pps.length;i++){
				PluginPackage p = pps[i];
//			for(PluginPackage p : installedPlugins.values()){
				//可能还未解析完毕，使用getInstalledPlugin()等待解析完毕。问题：影响程序速度
				//p = installManager.getInstalledPlugin(p.tag);

				//未解析完毕时，为避免影响查询速度，这里直接放弃该插件的查询
				//可能导致的问题是，当程序刚启动时，插件系统未初始化完毕，所以不能立马查询到插件信息
				//一般情况下，不会由此产生问题，因为这类需求的调用都在插件中，而在插件代码运行的时候，插件系统必然已经初始化完毕。
				Log.d(TAG, "queryPluginByPackageName(),p.pkg=" + p.pkg);
				if(p.pkg != null){
					if(p.pkg.packageName.equals(packageName)){
						return p;
					}
				}

				//以上问题有一种折中的解决方案是，在安装插件的时候把包名全部写入数据库，查询时用包名查询，查到了才解析它(需要保证包名正确，所以需要解析，或者保证人工配置的正确性)
				//该方案修改比较多，先不管了(⊙﹏⊙)b

				}
		
		}
		return null;
	}
	
	
	//根据ComponentName从所有插件中查询activity信息，没解析完毕的忽略
	public PackageParser.Activity queryActivity(ComponentName name){
//		synchronized (installedPlugins) {
//			Iterator<PluginPackage> iter = installedPlugins.values().iterator();
//			while(iter.hasNext()){
//				PluginPackage pluginPackage = iter.next();
//				if(pluginPackage.pkg.packageName.equals(name.getPackageName())){
//					return (Activity) findComponent(COMPONENT_TYPE_ACTIVITY,pluginPackage, name.getClassName());
//				}
//			}
//		}
		
		if(name == null)
			return null;

		PluginPackage[] pps = installedPlugins.values().toArray(new PluginPackage[]{});
		for(int i=0;i<pps.length;i++){
			PluginPackage p = pps[i];
			if(p.pkg != null){
				if(p.pkg.packageName.equals(name.getPackageName())){
					return (Activity) findComponent(COMPONENT_TYPE_ACTIVITY,p, name.getClassName());
				}
			}
		}
		return null;
	}
	
	
}
