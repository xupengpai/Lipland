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
import android.app.ActivityThread;
import android.app.Instrumentation;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.qihoo.plugin.core.HostApplicationProxy;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.core.ProxyActivity;
import com.qihoo.plugin.core.hook.InstrumentationHacker;
import com.qihoo.plugin.util.RefUtil;

import java.io.File;

/**
 * 公用插件相关方法
 * 
 * @author xupengpai
 * @date 2015年12月9日 下午7:34:05
 */
public class PluginHelper {

	private final static String TAG = "PluginHelper";

	private static boolean isConnected = false;
	private static IPluginProcess pluginProcess = null;
	private static boolean registered = false;

	private static class PluginProcessConnection implements ServiceConnection{

		private PluginProcessListener listener;

		public void setPluginProcessListener(PluginProcessListener listener){
			this.listener = listener;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected()...name=" + name);

			if(listener != null){
				listener.onDisconnected();
			}

			isConnected = false;
//			pluginProcessReady = false;
			listener = null;
			pluginProcess = null;

			try {
				HostGlobal.getBaseApplication().unbindService(this);
			} catch (Exception e) {
				Log.e(TAG, e);
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onServiceConnected()...name=" + name + ", service=" + service);
			isConnected = true;
			pluginProcess = IPluginProcess.Stub.asInterface(service);
			if(listener != null) {
				listener.onConnected();
				//只调用一次
//				listener = null;

			}
		}

	}

	private static void registerMonitor(Context context){
		if(!registered) {
			Log.d("PluginInitHandler", "registerMonitor()...context=" + context);
			IntentFilter filter = new IntentFilter();
			filter.addAction(Actions.ACTION_PLUGIN_PROCESS_READY);
			context.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					Log.d("PluginInitHandler", "onReceive()...action=" + action);
					if(action.equals(Actions.ACTION_PLUGIN_PROCESS_READY)){
//						pluginProcessReady = true;
						Log.d("PluginInitHandler", "onReceive()...conn=" + conn);
						Log.d("PluginInitHandler", "onReceive()...conn.listener=" + (conn != null ? conn.listener : "null"));
						if(conn != null && conn.listener != null){
							conn.listener.onReady();
						}
					}
				}
			}, filter);
			registered = true;
		}
	}

	private static PluginProcessConnection conn;

	public static void hookActivityInstrumentation(Activity activity){

		//某些情况下，不知道什么原因，导致Instrumentation被还原导致启动Activity无效，这里在每次启动插件进程前，都重新hook一次，解决这个问题
		String fieldName = "mInstrumentation";
		Instrumentation instrumentation = (Instrumentation) RefUtil.getFieldValue(activity,Activity.class, fieldName);

		Log.d(TAG, "hookActivityInstrumentation()...instrumentation=" + instrumentation);
		if(!(instrumentation instanceof InstrumentationHacker)){
			Log.d(TAG, "hookActivityInstrumentation()...,activity=" + activity);
			Log.d(TAG, "hookActivityInstrumentation()...,PluginManager.getInstrumentation()=" + PluginManager.getInstrumentation());
			try {
				PluginManager.getInstance().postExceptionToHost("pluginloader_reset_activity_instrumentation", "instrumentation=" + instrumentation + ",activity=" + activity + ",process=" + HostGlobal.getProcessName(), null);
			}finally {
				RefUtil.setDeclaredFieldValue(activity,Activity.class, fieldName, PluginManager.getInstrumentation());
			}
		}
	}

	public static void startPluginProcess() {
		startPluginProcess(null);
	}

	/**
	 * 使用PluginProcessStartup服务启动插件进程，便于插件启动回调
	 * 
	 * @param context
	 * @param listener
	 */
	public static void startPluginProcess(PluginProcessListener listener) {

		Log.d(TAG, "startPluginProcess()...listener=" + listener);
		registerMonitor(HostGlobal.getBaseApplication());

		Intent service = new Intent();
		service.setClass(HostGlobal.getBaseApplication(), PluginProcessStartup.class);


		//某些情况下，不知道什么原因，导致Instrumentation被还原导致启动Activity无效，这里在每次启动插件进程前，都重新hook一次，解决这个问题
		String fieldName = "mInstrumentation";
		ActivityThread activityThread = PluginManager.getInstance().getActivityThread();
		Instrumentation instrumentation = (Instrumentation) RefUtil.getFieldValue(activityThread,ActivityThread.class, fieldName);

		Log.d(TAG, "startPluginProcess()...instrumentation=" + instrumentation);
		if(!(instrumentation instanceof InstrumentationHacker)){
			Log.d(TAG, "startPluginProcess()...injectInstrumentation(),activityThread=" + activityThread);
			try {
				PluginManager.getInstance().postExceptionToHost("pluginloader_reset_instrumentation", "instrumentation=" + instrumentation + ",activityThread=" + activityThread + ",process=" + HostGlobal.getProcessName(), null);
			}finally {
				HostApplicationProxy.injectInstrumentation(activityThread);
			}
		}

		if(!isConnected) {
			conn = new PluginProcessConnection();
			conn.setPluginProcessListener(listener);
			HostGlobal.getBaseApplication().bindService(service, conn, Service.BIND_AUTO_CREATE);
			HostGlobal.getBaseApplication().startService(service);
		}else{
			if(listener != null) {
				conn.listener = listener;
				listener.onConnected();
				boolean isReady = false;
				try {
					isReady = pluginProcess.isReady();
				} catch (Exception e) {
					Log.e(TAG, e);
					listener.onException(e);
					isConnected = false;
					killPluginProcess();
					startPluginProcess(listener);
					return;
				}

				if(isReady){
					listener.onReady();
				}

			}
		}

	}


	public static boolean isPluginProcessReady(){
		if(pluginProcess != null) {
			try {
				return pluginProcess.isReady();
			}catch (Exception e){
				isConnected = false;
				Log.e(TAG,e);
				killPluginProcess();
			}
		}
		return false;
	}

	public static boolean isPluginLoaded(String tag){
		if(pluginProcess != null) {
			try {
				return pluginProcess.isLoaded(tag);
			}catch (Exception e){
				isConnected = false;
				Log.e(TAG,e);
				killPluginProcess();
			}
		}
		return false;
	}

	/**
	 * 强制结束插件进程
	 *
	 * @param app
	 */
	public static void killPluginProcess() {

		try {
			Context context = HostGlobal.getBaseApplication();
			Intent service = new Intent();
			service.setClass(context,
					PluginProcessStartup.class);

			if (isConnected) {
				isConnected = false;
				context.unbindService(conn);
			}
			context.stopService(service);

			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : am
					.getRunningAppProcesses()) {
				if (appProcess.processName.equals(PluginManager.getInstance().getPluginProcessName())) {
					Process.killProcess(appProcess.pid);
				}
			}
		}catch(Throwable thr){
			Log.e(TAG, thr);
		}
	}

//	/**
//	 * 强制结束插件进程
//	 *
//	 * @param app
//	 */
//	public static void killPluginProcess() {
//		Intent service = new Intent();
//		service.setClass(HostGlobal.getBaseApplication(),
//				PluginProcessStartup.class);
//		HostGlobal.getBaseApplication().stopService(service);
//		ActivityManager mActivityManager = (ActivityManager) HostGlobal.getBaseApplication()
//				.getSystemService(Context.ACTIVITY_SERVICE);
//		for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
//				.getRunningAppProcesses()) {
//			if (appProcess.processName.equals(HostGlobal.getPackageName()+":plugin")) {
//				Process.killProcess(appProcess.pid);
//			}
//		}
//	}

	/**
	 * 重启插件进程
	 * 
	 * @param app
	 */
	public static void restartPluginProcess() {
		killPluginProcess();
		startPluginProcess(new PluginProcessListener() {
			@Override
			public void onConnected() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onReady() {

			}

			@Override
			public void onDisconnected() {

			}

			@Override
			public void onException(Exception e) {

			}
		});
	}

	/**
	 * 删除update.xml 一般在更新出错或者更新失败时调用，好让程序在下次启动时会再次尝试更新
	 * 
	 * @param context
	 */
	public static void deleteUpdateConfig(Context context) {
		String updateInfoConfig = context.getFilesDir().getAbsolutePath()
				+ "/plugin/config/update.xml";
		new File(updateInfoConfig).delete();
	}

	public static boolean isPluginActivityShowing(Context context) {

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

		if (cn.getPackageName().equals(context.getPackageName())) {
			Class<? extends ProxyActivity> cls;
			try {
				cls = (Class<? extends ProxyActivity>) Class.forName(cn
						.getClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			Class<?> spcls = cls;
			while (spcls != null && !spcls.equals(Object.class)
					&& !spcls.equals(ProxyActivity.class)) {
				spcls = spcls.getSuperclass();
			}
			return spcls.equals(ProxyActivity.class);
		}

		return false;
	}

}
