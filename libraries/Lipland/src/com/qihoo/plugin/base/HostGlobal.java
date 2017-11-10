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

package com.qihoo.plugin.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 应用全局信息统一在该类中获取,该类尽量不写与应用业务有关的代码
 * 
 * @author xupengpai
 * @date 2015年10月23日 下午3:57:03
 */
public class HostGlobal {

	private final static String TAG = HostGlobal.class.getSimpleName();

	private static Activity mainActivity;
	private static String packageName;
	private static String processName;
	private static String versionName;
	private static int versionCode;
	
	// 全局application
	private static Application application;

	public static Application getBaseApplication() {
		checkError();
		return application;
	}

	public static void init(Application app) {
		HostGlobal.application = app;
	}

	public static Activity getMainActivity() {
		return mainActivity;
	}

	public static void setMainActivity(Activity activity) {
		HostGlobal.mainActivity = activity;
	}

	private static void checkError() {
		if (application == null)
			throw new RuntimeException(
					"HostGlobal did not call through to HostGlobal.init()");
	}

	public static String getPackageName() {
		checkError();
		if(packageName == null)
			packageName = application.getPackageName();
		return packageName;
	}

	/**
	 * 获取当前进程名称
	 * @return
	 */
	public static String getProcessName() {
		checkError();

		if(processName == null){
			int pid = android.os.Process.myPid();
			ActivityManager mActivityManager = (ActivityManager) application
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
					.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {
	
					processName = appProcess.processName;
					break;
				}
			}
		}
		return processName;
	}

	public static boolean isMainProcess() {
		return getPackageName().equals(getProcessName());
	}

//	public static boolean isPluginProcess() {
//		return (getPackageName()+":plugin").equals(getProcessName());
//	}
	
	private static void getVersionInfo(){
		checkError();
		if(versionName == null){
			PackageManager pm = application.getPackageManager();
			if (pm != null) {
				PackageInfo pkgInfo;
				try {
					pkgInfo = pm.getPackageInfo(getPackageName(), 0);
					versionName = pkgInfo.versionName;
					versionCode = pkgInfo.versionCode;
				} catch (Exception e) {
					Log.e(TAG, e.getMessage() , e);
				}
			}
		}
	}

	public static int getVersionCode() {
		getVersionInfo();
		return versionCode;
	}

	public static String getVersionName() {
		getVersionInfo();
		return versionName;
	}

	/**
	 * 判断栈顶Activity
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isTopActivity(String className) {
		ComponentName cn = getTopActivity();
		return (cn.getPackageName().equals(getPackageName()) && cn
				.getClassName().equals(className));
	}

	/**
	 * 获取栈顶Activity
	 * 
	 * @return
	 */
	public static ComponentName getTopActivity() {

		checkError();

		ActivityManager am = (ActivityManager) application
				.getSystemService(Context.ACTIVITY_SERVICE);
		return am.getRunningTasks(1).get(0).topActivity;

	}

}
