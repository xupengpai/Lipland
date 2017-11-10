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

import java.lang.reflect.Proxy;

import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.core.hook.ProxyHandler.NameFiltrationHookHandler;
import com.qihoo.plugin.util.RefUtil;

import android.os.IServiceManager;
import android.os.ServiceManager;

/**
 * 
 * @author xupengpai
 * @date 2015年12月28日 下午3:32:04
 */
public class ServiceManagerHacker {
	
	private final static String TAG = "ServiceManagerHacker";
	
	private static ServiceManagerHacker instance;

	private ProxyHandler proxyHandler;
	private NameFiltrationHookHandler gHookHandler;

	private IServiceManager proxy;
	private IServiceManager origin;
	
	
	public IServiceManager getOrigin() {
		return origin;
	}
	
	public void addHookHandler(String methodName,HookHandler handler){
		if(gHookHandler == null){
			gHookHandler = new NameFiltrationHookHandler();
			proxyHandler.setHookHandler(gHookHandler);
		}
		gHookHandler.setHookHandler(methodName, handler);
	}

	
	public static ServiceManagerHacker hook(){
//		context.getSystemService(Context.APP_OPS_SERVICE);

//2453        final AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(
//2454                Context.APP_OPS_SERVICE);
//2453        final AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(
//2454                Context.APP_OPS_SERVICE);
//2455        appOps.checkPackage(Binder.getCallingUid(), callingPkg);

		if(instance == null){
			IServiceManager ism  = null;
			try {
				ism = (IServiceManager) RefUtil.callDeclaredMethod(null, ServiceManager.class, "getIServiceManager", null, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e);
			}
			
			if(ism == null){
				//error
				Log.e(TAG, "IServiceManager hook error");
				return null;
			}
			instance = new ServiceManagerHacker();
			instance.origin = ism;
			instance.proxyHandler = new ProxyHandler(ism);
			instance.proxy = (IServiceManager) Proxy.newProxyInstance(
					Proxy.class.getClassLoader(),
					new Class<?>[] { IServiceManager.class }, instance.proxyHandler);
			RefUtil.setFieldValue(ServiceManager.class, "sServiceManager", instance.proxy);
			System.out.println(RefUtil.getFieldValue(ServiceManager.class, "sServiceManager"));
		}
		
		return instance;
		
	}
	
}
