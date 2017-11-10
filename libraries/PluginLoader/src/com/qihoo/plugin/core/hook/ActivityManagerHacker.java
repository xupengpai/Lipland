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


import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Singleton;

import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.core.hook.ProxyHandler.NameFiltrationHookHandler;
import com.qihoo.plugin.util.RefUtil;

/**
 * 
 * @author xupengpai
 * @date 2015年11月24日 下午4:09:08
 */
public class ActivityManagerHacker {
	
	private final static String TAG = ActivityManagerHacker.class.getSimpleName();
	private static ActivityManagerHacker instance;

	private Singleton<IActivityManager> gDefault;
	private IActivityManager origin;
	private IActivityManager proxy;
	private ActivityManagerProxy activityManagerProxy;
	private NameFiltrationHookHandler nameFiltrationHookHandler;

	private ActivityManagerHacker(ClassLoader classLoader,Singleton<IActivityManager> gDefault,IActivityManager origin){
		this.gDefault = gDefault;
		this.origin = origin;
		activityManagerProxy =  new ActivityManagerProxy(origin);
		IActivityManager proxy = (IActivityManager)Proxy.newProxyInstance(classLoader,  
				new Class<?>[]{IActivityManager.class}, activityManagerProxy);
		this.proxy = proxy;
	}
	
	public ActivityManagerProxy getActivityManagerProxy() {
		return activityManagerProxy;
	}

	
	public class ActivityManagerProxy extends ProxyHandler{

		public ActivityManagerProxy(Object origin) {
			super(TAG,origin);
			// TODO Auto-generated constructor stub
		}
	}
	
	public Singleton<IActivityManager> getgDefault() {
		return gDefault;
	}
	
	public IActivityManager getOrigin() {
		return origin;
	}
	
	public IActivityManager getProxy() {
		return proxy;
	}

//	public void addHookHandler(HookHandler handler){
//		if(handler != null){
//			if(hookHandlers == null)
//				hookHandlers = new ArrayList<HookHandler>();
//			hookHandlers.add(handler);




//		}
//	}

	public void setHookHandlerByName(String methodName,HookHandler handler){
		if(handler != null){
			if(nameFiltrationHookHandler == null){
				nameFiltrationHookHandler = new NameFiltrationHookHandler();
				this.activityManagerProxy.setHookHandler(nameFiltrationHookHandler);
			}
			nameFiltrationHookHandler.setHookHandler(methodName, handler);
		}
	}
	
//	
//	public void removeHookHandler(HookHandler handler){
//		if(hookHandlers != null && handler != null)
//			hookHandlers.remove(handler);
//	}



//	android 8.0
//	/**
//	 * @hide
//	 */
//	public static IActivityManager getService() {
//		return IActivityManagerSingleton.get();
//	}
//
//	private static final Singleton<IActivityManager> IActivityManagerSingleton =
//			new Singleton<IActivityManager>() {
//				@Override
//				protected IActivityManager create() {
//					final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
//					final IActivityManager am = IActivityManager.Stub.asInterface(b);
//					return am;
//				}
//			};

	private static ActivityManagerHacker hookFor8_0(){

		ActivityManagerHacker hacker = null;
		Singleton<IActivityManager> IActivityManagerSingleton = null;
		IActivityManager origin = null;
		Field field = RefUtil.getField(ActivityManager.class, "IActivityManagerSingleton");
		if (field == null) {
			Log.e(TAG, "hookFor8_0::error,gDefault field not found");
			return null;
		}
		field.setAccessible(true);
		try {
			IActivityManagerSingleton = (Singleton<IActivityManager>) field.get(ActivityManager.class);
			origin = IActivityManagerSingleton.get();
			Log.i(TAG, "hookFor8_0::origin IActivityManager = " + origin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e);
			return null;
		}

		try {
			hacker = new ActivityManagerHacker(ActivityManager.class.getClassLoader(), IActivityManagerSingleton, origin);

			RefUtil.setFieldValue(IActivityManagerSingleton, "mInstance", hacker.getProxy());
		} catch (Exception e) {
			Log.e(TAG, e);
		}

		return hacker;
	}

	private static ActivityManagerHacker hookFor4_0(){

		ActivityManagerHacker hacker = null;
		Singleton<IActivityManager> gDefault = null;
		IActivityManager origin = null;
		Field field = RefUtil.getField(ActivityManagerNative.class, "gDefault");
		if (field == null) {
			Log.e(TAG, "hookFor4_0::error,gDefault field not found");
			return null;
		}
		field.setAccessible(true);
		try {
			gDefault = (Singleton<IActivityManager>) field.get(ActivityManagerNative.class);
			origin = gDefault.get();
			Log.i(TAG, "hookFor4_0::origin IActivityManager = " + origin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e);
			return null;
		}

		try {
			hacker = new ActivityManagerHacker(ActivityManagerHacker.class.getClassLoader(), gDefault, origin);

			RefUtil.setFieldValue(gDefault, "mInstance", hacker.getProxy());
		} catch (Exception e) {
			Log.e(TAG, e);
		}

		return hacker;
	}

	public static ActivityManagerHacker hook(){
		
		if(instance != null)
			return instance;

		//26为android 8.0
		if(Build.VERSION.SDK_INT < 26) {
			instance = hookFor4_0();

		}else{
			instance = hookFor8_0();
		}
		
		return instance;
	}

}
