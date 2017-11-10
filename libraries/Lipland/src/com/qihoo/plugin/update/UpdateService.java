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

package com.qihoo.plugin.update;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;

import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.core.Log;

/**
 * 插件更新服务，最好配置在独立进程
 * @author xupengpai
 * @date 2015年11月23日 上午11:13:10
 */
public class UpdateService extends Service {
	
	private final static String TAG = "Plugin"+UpdateService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i(TAG, "onStartCommand::intent="+intent);
		if(intent != null){
			String action = intent.getAction();
			Log.i(TAG, "onStartCommand::action="+action);
			if(!TextUtils.isEmpty(action)){
				if(action.equals(Actions.ACTION_UPDATE_CHECK)){

					Log.i(TAG, "onStartCommand::begin check update");
					this.registerReceiver(new BroadcastReceiver() {
						
						@Override
						public void onReceive(Context context, Intent intent) {
							// TODO Auto-generated method stub
							
							Log.i(TAG, "BroadcastReceiver::intent="+intent);
							if(intent != null){
								Log.i(TAG, "BroadcastReceiver::action="+intent.getAction());
								
								//更新完成后退出进程
								if(Actions.ACTION_UPDATE_GLOBAL_DOWNLOAD_ALL_COMPLETE.equals(intent.getAction())){
									Log.i(TAG, "BroadcastReceiver::UpdateService exit.");
									stopSelf();
									System.exit(0);
								}
								
							}
							
						}
					}, new IntentFilter(Actions.ACTION_UPDATE_GLOBAL_DOWNLOAD_ALL_COMPLETE));
					
					String className = intent.getStringExtra(Actions.DATA_CLASS_NAME);
					String filePath = intent.getStringExtra(Actions.DATA_FILE_PATH);
					boolean onlyWifi = intent.getBooleanExtra(Actions.DATA_ONLY_WIFI, true);
					boolean reload = intent.getBooleanExtra(Actions.DATA_RELOAD, true);
					
					UpdateManager updateManager = UpdateManager.getInstance();

					Log.i(TAG, "onStartCommand::className="+className);
					Log.i(TAG, "onStartCommand::filePath="+filePath);
					Log.i(TAG, "onStartCommand::onlyWifi="+onlyWifi);
					Log.i(TAG, "onStartCommand::reload="+reload);
					
					if(reload){
						try {
							if(!TextUtils.isEmpty(filePath))
								updateManager.reload(filePath);
							else
								updateManager.reload();
						} catch (Exception e1) {
							Log.e(TAG, e1);
						}
					}
					
					updateManager.setOnlyWifi(onlyWifi);
					UpdateFilter filter = updateManager.getDefaultUpdateFilter();
					if(!TextUtils.isEmpty(className)){
						//如果自定义了UpdateFilter,onlyWifi设定忽略
						try{
							Class<?> filterClass = Class.forName(className);
							if(filterClass != null){
								filter = (UpdateFilter)filterClass.newInstance();
							}
						}catch(Exception e){
							Log.e(TAG, e);
						}
					}
					Log.i(TAG, "onStartCommand::filter="+filter);
					UpdateManager.getInstance().doUpdate(filter);
				}
			}
		}
		return Service.START_NOT_STICKY;
	}

}
