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
import java.util.List;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.location.LocationManager;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.core.hook.ProxyHandler.NameFiltrationHookHandler;
import com.qihoo.plugin.util.RefUtil;


/**
 * 
 * @author xupengpai
 * @date 2016年4月19日 下午2:48:48
 */
public class IPackageManagerHacker {
	
	private final static String TAG = IPackageManagerHacker.class.getSimpleName();
	private static IPackageManagerHacker instance;

	private IPackageManager origin;
	private IPackageManager proxy;
	private PackageManagerProxy packageManagerProxy;
	private NameFiltrationHookHandler nameFiltrationHookHandler;

	private IPackageManagerHacker(ClassLoader classLoader,IPackageManager origin){
		this.origin = origin;
		packageManagerProxy =  new PackageManagerProxy(origin);
		IPackageManager proxy = (IPackageManager)Proxy.newProxyInstance(classLoader,  
				new Class<?>[]{IPackageManager.class}, packageManagerProxy);
		this.proxy = proxy;
	}
	
	public PackageManagerProxy getPackageManagerProxy() {
		return packageManagerProxy;
	}
	
	
	public class PackageManagerProxy extends ProxyHandler{
		
		public PackageManagerProxy(IPackageManager origin){
			super(PackageManagerProxy.class.getSimpleName(), origin);
		}
		
	} 
	
	
	public IPackageManager getOrigin() {
		return origin;
	}
	
	public IPackageManager getProxy() {
		return proxy;
	}


	public void setHookHandlerByName(String methodName,HookHandler handler){
		if(handler != null){
			if(nameFiltrationHookHandler == null){
				nameFiltrationHookHandler = new NameFiltrationHookHandler();
				this.packageManagerProxy.setHookHandler(nameFiltrationHookHandler);
			}
			nameFiltrationHookHandler.setHookHandler(methodName, handler);
		}
	}
	
	
	public static IPackageManagerHacker hook(Application app){
		
		if(instance != null)
			return instance;
		
		IPackageManager origin = ActivityThread.getPackageManager();
		
		try{
			instance = new IPackageManagerHacker(IPackageManagerHacker.class.getClassLoader(), origin);
			RefUtil.setFieldValue(ActivityThread.class, "sPackageManager", instance.getProxy());
			RefUtil.setFieldValue(app.getPackageManager(), "mPM", instance.getProxy());

		}catch(Exception e){
        	Log.e(TAG,e);
		}


		
		
		return instance;
	}
	


}
