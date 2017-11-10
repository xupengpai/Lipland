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

import java.lang.reflect.Method;

import android.os.Build;
import android.os.storage.IMountService;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;


/**
 * 
 * @author xupengpai
 * @date 2016年8月29日 下午6:18:06
 */
public class IMountServiceHacker extends ServiceInterfaceProxy {

	private static final String TAG = "IMountServiceHacker";

	private static final String SERVICE_NAME = "mount";
	private static IMountServiceHacker instance;
	
	protected IMountServiceHacker() {
		super(SERVICE_NAME, IMountService.class);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void invokeHandle() {
		// TODO Auto-generated method stub
		getDProxy().setHookHandlerByName("mkdirs", new HookHandler() {
			@Override
			public boolean onBefore(Object origin, Method method, Object[] args) {
				// TODO Auto-generated method stub
				//修改调用mkdirs()时的包名参数
				if(args != null && args.length != 0){
					args[0] = HostGlobal.getPackageName();
				}
				return super.onBefore(origin, method, args);
			}
		});
	}
	
	public static void hook() {
		//android 8.0 = 26{
		if (Build.VERSION.SDK_INT < 26){
			try {
				if (instance == null) {
					instance = new IMountServiceHacker();
				}
				instance.doHook();
			} catch (Throwable thr) {
				Log.e(TAG, thr);
			}
		}
	}

}
