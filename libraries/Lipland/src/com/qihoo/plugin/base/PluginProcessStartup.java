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


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.qihoo.plugin.BuildConfig;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.util.CodeTraceTS;


/**
 * 用于启动插件进程
 * @author xupengpai
 * @date 2015年12月9日 下午3:07:21
 */
public class PluginProcessStartup extends Service {

	private final static String TAG = "PluginProcessStartup";

//	public static boolean isReady;
	
	IPluginProcess binder = new IPluginProcess.Stub() {

		@Override
		public void test(String msg) throws RemoteException {

		}

		public boolean isReady()  throws RemoteException {
			return PluginManager.getInstance().isReady();
		}


		public boolean isLoaded(String tag)  throws RemoteException {
			return PluginManager.getInstance().isLoaded(tag);
		}

	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return binder.asBinder();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		new Thread(new Runnable() {
			@Override
			public void run() {

				try {

					Log.d(TAG,"onCreate(),Thread,begin...");
					PluginManager pluginManager = PluginManager.getInstance();
					if(Log.isDebug()) {
						CodeTraceTS.begin("installDefaultPlugins");
					}
					Log.d(TAG,"onCreate(),Thread,installDefaultPlugins...");
					pluginManager.installDefaultPlugins(false);

					if(Log.isDebug()) {
						Log.d(TAG, "installDefaultPlugins " + CodeTraceTS.end("installDefaultPlugins").time() + "ms");
					}

					if(Log.isDebug()) {
						CodeTraceTS.begin("loadPluginOnAppStarted");
					}
					Log.d(TAG,"onCreate(),Thread,handlePendingInstallPlugin...");

					pluginManager.handlePendingInstallPlugin();
					Log.d(TAG,"onCreate(),Thread,preproccess...");
					pluginManager.preproccess();

//					Thread.sleep(10000);

					Log.d(TAG,"onCreate(),Thread,loadPluginOnAppStarted...");
					pluginManager.loadPluginOnAppStarted();

					if(Log.isDebug()) {
						Log.d(TAG, "loadPluginOnAppStarted " + CodeTraceTS.end("loadPluginOnAppStarted").time() + "ms");
					}

				}catch (Throwable thr) {
					//以上两步操作即使出现问题，也不应该对其他插件有影响，所以这里全部捕获
					Log.e(TAG, thr );
				}finally {
					Log.d(TAG, "onCreate(),Thread,send Actions.ACTION_PLUGIN_PROCESS_READY");
					sendBroadcast(new Intent(Actions.ACTION_PLUGIN_PROCESS_READY));
				}
//						}
//					},100);

			}
		}).start();
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(TAG, "onStartCommand()...");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy()...");
		super.onDestroy();
	}

}
