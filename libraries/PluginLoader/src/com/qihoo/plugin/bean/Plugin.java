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

package com.qihoo.plugin.bean;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.LoadedApk;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Build;

import com.qihoo.plugin.IPlugin;
import com.qihoo.plugin.core.DexClassLoaderEx;
import com.qihoo.plugin.core.PluginManager;

/**
 * 插件数据类
 * @author xupengpai
 * @date 2015年11月30日 上午11:31:49
 */
public class Plugin {

	private String tag;
	private String path;
	private String srcPath;

	private Resources res;
	private DexClassLoaderEx cl;
	private ActivityInfo[] activityInfo;
	private IPlugin callback;
	private Application pluginApplication;
	private PackageInfo packageInfo;
	private PluginPackage pluginPackage;
	private LoadedApk loadedApk;
	private List<WeakReference<Activity>> activities;
	
	
	public Plugin(){
		this.activities = new ArrayList<WeakReference<Activity>>();
	}
	
	public void putActivity(Activity activity){
		synchronized (activities) {
			activities.add(new WeakReference<Activity>(activity));
		}
	}
	
	public List<WeakReference<Activity>> getActivities(){
		synchronized (activities) {
			//先清理
			for(int i=activities.size();i>=0;i--){
				WeakReference<Activity> ref = activities.get(i);
				if(ref.get() == null)
					activities.remove(ref);
			}
			return activities;
		}
	}
	
	public PluginPackage getPluginPackage() {
		return pluginPackage;
	}
	
	public void setPluginPackage(PluginPackage pluginPackage) {
		this.pluginPackage = pluginPackage;
	}
	
	public LoadedApk getLoadedApk() {
		return loadedApk;
	}
	
	public void setLoadedApk(LoadedApk loadedApk) {
		this.loadedApk = loadedApk;
	}

	public PackageInfo getPackageInfo() {
		return packageInfo;
	}
	
	public void setPackageInfo(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPath() {
		return path;
	}

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ActivityInfo[] getActivityInfo() {
		return activityInfo;
	}

	public void setActivityInfo(ActivityInfo[] activityInfo) {
		this.activityInfo = activityInfo;
	}


	public Resources getRes() {
		return res;
	}

	public void setRes(Resources res) {
		this.res = res;
	}

	public DexClassLoaderEx getCl() {
		return cl;
	}

	public void setCl(DexClassLoaderEx cl) {
		this.cl = cl;
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return cl.loadClass(className);
	}

	public void setCallback(IPlugin callback) {
		this.callback = callback;
	}

	public IPlugin getCallback() {
		return callback;
	}

	public ActivityInfo findActivity(String className) {
		if(activityInfo != null){
			for (ActivityInfo ai : activityInfo) {
				if (ai.name.equals(className)) {
					return ai;
				}
			}
		}
		return null;
	}

	// 启动插件，具体动作由插件自行在onStart()中实现
	public void start() {
		if (callback != null)
			callback.onStart(null);
	}

	public void start(Intent intent) {
		if (callback != null)
			callback.onStart(intent);
	}


	public void setApplication(Application application){
		this.pluginApplication = application;
	}

	public Application getApplication() {
		return this.pluginApplication;
	}
	
	/**
	 * 获取一个插件专用的context
	 * 
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public Application getContext() {
//		try {
//
//			if (pluginApplication == null) {
//
//				Application application = pluginManager.getApplicationContext();
////				RefUtil.cloneObject(src, target);
//				Context context = application.createPackageContext(
//						application.getPackageName(),
//						Context.CONTEXT_IGNORE_SECURITY);
//				
//				
//				pluginManager.beginUsePluginContext(this, context);
//
////				pluginApplication = new Application();
//
//				try {
//					pluginApplication = (Application)Instrumentation.newApplication(getCl().loadClass("android.app.Application"), context);
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//
//				// 将baseContext注入到插件Application中
//				Method method = null;
//				try {
//					method = Application.class.getDeclaredMethod("attach",
//							new Class[] { Context.class });
//					method.setAccessible(true);
//					method.invoke(pluginApplication, context);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				// 设置application中对应的application引用，解决在某些组件中需要调用getApplication()，而getApplication()为null的问题
//				Object packageInfo = RefUtil.getFieldValue(context,
//						"mPackageInfo");
//				if (packageInfo != null)
//					RefUtil.setFieldValue(packageInfo, "mApplication",
//							pluginApplication);
//
//			}
//
//			return pluginApplication;
//
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
		return pluginApplication;
	}


	public Intent startCommand(Intent intent) {
		if (callback != null)
			try {
				return this.callback.startCommand(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}

	// public Context

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "{tag="+tag;

		if(activityInfo != null){
			str+=",ai=[";
			for (ActivityInfo ai : activityInfo) {
				str += "{"+ai.processName+","+ai.name+"},";
			}
			if(activityInfo.length>0)
				str = str.substring(0, str.length()-1);
			str += "]";
		}
		
		str += "}";
		
		return str;
		
	}
}
