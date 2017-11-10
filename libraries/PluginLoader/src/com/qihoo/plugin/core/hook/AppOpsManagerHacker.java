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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.core.hook.ProxyHandler.NameFiltrationHookHandler;
import com.qihoo.plugin.util.RefUtil;

import android.content.Context;
import android.os.IBinder;
import android.os.IServiceManager;
import android.os.RemoteException;
import android.os.ServiceManager;

/**
 * 
 * @author xupengpai
 * @date 2015年12月28日 下午3:32:04
 */
public class AppOpsManagerHacker {
	
	private final static String TAG = "AppOpsManagerHacker";
	
	private static AppOpsManagerHacker instance;

	private ProxyHandler proxyHandler;
	private NameFiltrationHookHandler gHookHandler;

	private IBinder proxy;
	private IBinder origin;
	
	
	public void addHookHandler(String methodName,HookHandler handler){
		if(gHookHandler == null){
			gHookHandler = new NameFiltrationHookHandler();
			proxyHandler.setHookHandler(gHookHandler);
		}
		gHookHandler.setHookHandler(methodName, handler);
	}

	
	public static AppOpsManagerHacker hook(){
//		context.getSystemService(Context.APP_OPS_SERVICE);

//2453        final AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(
//2454                Context.APP_OPS_SERVICE);
//2453        final AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(
//2454                Context.APP_OPS_SERVICE);
//2455        appOps.checkPackage(Binder.getCallingUid(), callingPkg);

		if(instance == null){
			ServiceManagerHacker serviceManagerHacker = ServiceManagerHacker.hook();
			IBinder serviceManagerBinder = null;
			Class<?> IAppOpsService_clz = null;
			
			if(serviceManagerHacker != null){
				try {
					serviceManagerBinder = serviceManagerHacker.getOrigin().getService(Context.APP_OPS_SERVICE);
					IAppOpsService_clz = Class.forName("com.android.internal.app.IAppOpsService");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e(TAG, e);
				}
			}
			
			if(serviceManagerBinder == null || IAppOpsService_clz == null){
				return null;
			}

			instance = new AppOpsManagerHacker();
			instance.origin = serviceManagerBinder;
			instance.proxyHandler = new ProxyHandler(serviceManagerBinder);
			instance.proxy = (IBinder)Proxy.newProxyInstance(
					Proxy.class.getClassLoader(),
					new Class<?>[] { IAppOpsService_clz ,IBinder.class}, instance.proxyHandler);
			
			ServiceManagerHacker.hook().addHookHandler("getService", new HookHandler() {
				@Override
				public boolean onBefore(Object origin, Method method,
						Object[] args) {
					// TODO Auto-generated method stub
					if(args != null && args.length > 0){
						if(args[0].equals(Context.APP_OPS_SERVICE)){
							setResult(instance.proxy);
							return false;
						}
					}
					return super.onBefore(origin, method, args);
				}
			});
		}
		
		return instance;
		
	}
	
}
