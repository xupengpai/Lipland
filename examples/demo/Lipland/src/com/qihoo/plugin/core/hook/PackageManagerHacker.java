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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.core.hook.ProxyHandler.NameFiltrationHookHandler;
import com.qihoo.plugin.util.RefUtil;

/**
 * 
 * @author xupengpai
 * @date 2015年11月24日 下午4:09:08
 */
public class PackageManagerHacker {

	private final static String TAG = PackageManagerHacker.class
			.getSimpleName();
	private static PackageManagerHacker instance;
	private ProxyHandler proxyHandler;
	private NameFiltrationHookHandler nameFiltrationHookHandler;

	public IPackageManager createIPackageManagerHook(
			IPackageManager origin) {

		proxyHandler = new ProxyHandler(origin);

		IPackageManager proxy = (IPackageManager) Proxy.newProxyInstance(
				Proxy.class.getClassLoader(),
				new Class<?>[] { IPackageManager.class }, proxyHandler);

		return proxy;

	}

	public void setHookHandler(String methodName, HookHandler handler) {
		nameFiltrationHookHandler.setHookHandler(methodName, handler);
	}

	private PackageManager createApplicationPackageManager(Context baseContext,
			IPackageManager origin) {
		try {
			Class<?> pm_clz = Class
					.forName("android.app.ApplicationPackageManager");
			Constructor<?> constructor = pm_clz
					.getDeclaredConstructor(new Class<?>[] {
							Class.forName("android.app.ContextImpl"),
							IPackageManager.class });
			constructor.setAccessible(true);
			IPackageManager ipm = createIPackageManagerHook(origin);
			return (PackageManager) constructor.newInstance(baseContext,ipm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static PackageManager getPluginPackageManager(final String tag,
			PackageManager origin) {

		PackageManagerHacker hacker = new PackageManagerHacker();
		Context baseContext = (Context)RefUtil.getFieldValue(origin, "mContext");
		PackageManager pm = hacker.createApplicationPackageManager(baseContext,(IPackageManager) RefUtil.getFieldValue(origin, "mPM"));
		ProxyHandler proxyHandler = hacker.proxyHandler;
		NameFiltrationHookHandler nameFiltrationHookHandler = new NameFiltrationHookHandler();
		hacker.nameFiltrationHookHandler = nameFiltrationHookHandler;
		proxyHandler.setHookHandler(hacker.nameFiltrationHookHandler);
//		nameFiltrationHookHandler.setHookHandler("getActivityInfo",
//				new HookHandler() {
//
//					@Override
//					public Object invoke(Object origin, Method method,
//							Object[] args) throws IllegalAccessException,
//							IllegalArgumentException, InvocationTargetException {
//						if (args != null) {
//
//							ComponentName componentName = null;
//
//							for (Object arg : args) {
//								Log.i(TAG, "arg=" + arg);
//								if (arg != null) {
//									// 找到intent参数
//									if (arg instanceof ComponentName) {
//										componentName = (ComponentName) arg;
//										break;
//									}
//								}
//							}
//							for (Object arg : args) {
//								Log.i(TAG, "--arg=" + arg);
//							}
//							if (componentName != null) {
//								RefUtil.setFieldValue(componentName, "mClass",
//										proxyActivityClass);
//							}
//						}
//						return super.invoke(origin, method, args);
//					}
//
//					@Override
//					public boolean onBefore(Object origin, Method method,
//							Object[] args) {
//						// TODO Auto-generated method stub
//						// if (args != null) {
//						//
//						// ComponentName componentName = null;
//						//
//						// for (Object arg : args) {
//						// Log.i(TAG, "arg=" + arg);
//						// if (arg != null) {
//						// // 找到intent参数
//						// if (arg instanceof ComponentName) {
//						// componentName = (ComponentName) arg;
//						// break;
//						// }
//						// }
//						// }
//						// if(componentName != null){
//						// RefUtil.setFieldValue(componentName, "mClass",
//						// proxyActivityClass);
//						// }
//						// }
//						return true;
//					}
//				});
		
		
		nameFiltrationHookHandler.setHookHandler("getPackageInfo",
		new HookHandler() {

			@Override
			public Object invoke(Object origin, Method method,
					Object[] args) throws IllegalAccessException,
					IllegalArgumentException, InvocationTargetException {

				if (args != null && args.length > 0 && tag != null) {
					PluginInfo pluginInfo = PluginManager.getInstance().getInstalledPluginInfo(tag);
					if(pluginInfo != null && args[0].equals(pluginInfo.packageName)){
						return HostGlobal.getBaseApplication().getPackageManager()
								.getPackageArchiveInfo(
										pluginInfo.path,(Integer)args[1]);
					}
				}
				return super.invoke(origin, method, args);
			}

			@Override
			public boolean onBefore(Object origin, Method method,
					Object[] args) {
				return true;
			}
		});
		return pm;
	}

}
