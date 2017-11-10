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

package com.qihoo.plugin;



import com.qihoo.plugin.bean.Plugin;
import android.content.Context;
import android.content.Intent;

/**
 * 插件接口，用于主程序对插件进行事件通知
 * @author xupengpai
 * @date 2015年4月3日 上午11:21:31
 */
public abstract class IPlugin {
	
	/**
	 * 加载插件后调用，可提供给插件初始化用，可以代替插件Application的onCreate()
	 * @param plugin 插件对象
	 * @param applicationContext 
	 * @param isMainProcess 是否为主进程，可以根据该参数 来注册广播接收器
	 */
	public abstract void onLoad(Plugin plugin,Context applicationContext,boolean isMainProcess);
	
	//主程序的Application的onTerminate()被调用时，该方法会被调用
	public abstract void onTerminate(Context applicationContext);

	//调用插件start()方法时自动调用
	public void onStart(Intent intent){
		

	}
	/**
	 * 当宿主或者插件发出命令时调用
	 * @param intent
	 */
	public void onCommand(Intent intent){
	}
	
	/*
	 * 宿主发起的内部调用，用于与插件进行交互
	 */
	public Intent startCommand(Intent intent){
		return null;
	}
	
}
