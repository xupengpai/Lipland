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

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.content.Context;
import android.location.ILocationManager;
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
public class ILocationManagerHacker {
	
	private final static String TAG = ILocationManagerHacker.class.getSimpleName();
	private static ILocationManagerHacker instance;

	private ILocationManager origin;
	private ILocationManager proxy;
	private ILocationManagerProxy locationManagerProxy;
	private NameFiltrationHookHandler nameFiltrationHookHandler;

	private ILocationManagerHacker(ClassLoader classLoader,ILocationManager origin){
		this.origin = origin;
		locationManagerProxy =  new ILocationManagerProxy(origin);
		ILocationManager proxy = (ILocationManager)Proxy.newProxyInstance(classLoader,  
				new Class<?>[]{ILocationManager.class}, locationManagerProxy);
		this.proxy = proxy;
	}
	
	
	public class ILocationManagerProxy extends ProxyHandler{
		
		public ILocationManagerProxy(ILocationManager origin){
			super(ILocationManagerProxy.class.getSimpleName(), origin);
		}
		
	} 
	
	
	public ILocationManager getOrigin() {
		return origin;
	}
	
	public ILocationManager getProxy() {
		return proxy;
	}


	public void setHookHandlerByName(String methodName,HookHandler handler){
		if(handler != null){
			if(nameFiltrationHookHandler == null){
				nameFiltrationHookHandler = new NameFiltrationHookHandler();
				this.locationManagerProxy.setHookHandler(nameFiltrationHookHandler);
			}
			nameFiltrationHookHandler.setHookHandler(methodName, handler);
		}
	}
	
	
	public static ILocationManagerHacker hook(){
		
		if(instance != null)
			return instance;
		LocationManager locationManager = (LocationManager) HostGlobal.getBaseApplication().getSystemService(Context.LOCATION_SERVICE);
		
		try{
			ILocationManager origin = (ILocationManager) RefUtil.getFieldValue(locationManager, "mService");
			instance = new ILocationManagerHacker(ILocationManagerHacker.class.getClassLoader(), origin);
			RefUtil.setFieldValue(ActivityThread.class, "mService", instance.getProxy());
			Object o = HostGlobal.getBaseApplication().getSystemService(Context.LOCATION_SERVICE);
			System.out.println(o);
		}catch(Exception e){
        	Log.e(TAG,e);
		}
		
		return instance;
	}
	
	private static boolean isInterfaceImpl(Class<?> cls,Class<?> inter){
		Class<?> inters[] = cls.getInterfaces();
		if(inters != null){
			for(Class<?> i : inters){
				if(i.equals(inter))
					return true;
			}
		}
		return false;
	}

	public static void replace(Context baseContext,Object service){
		
		if(baseContext == null || service == null)
			return;
		
		baseContext.getSystemService(Context.LOCATION_SERVICE);
		Object objCache = RefUtil.getFieldValue(baseContext, "mServiceCache");
		if(objCache != null){
			if(objCache instanceof List){
				List<Object> list = (List<Object>)objCache;
				for(int i=0;i<list.size();i++){
					Object s = list.get(i);
					if(s!=null&&s.getClass().equals(service.getClass())){
						list.set(i, service);
					}
				}
			}else{
				Object[] list = (Object[])objCache;
				for(int i=0;i<list.length;i++){
					Object s = list[i];
					if(s!=null&&s.getClass().equals(service.getClass())){
						list[i] = service;
					}
				}
			}
		}
	}
}
