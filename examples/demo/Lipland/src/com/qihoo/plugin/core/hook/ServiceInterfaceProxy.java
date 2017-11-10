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
import java.util.HashMap;

import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;
import com.qihoo.plugin.util.RefUtil;

import android.os.IBinder;
import android.os.ServiceManager;



/**
 * 代理service的IBinder接口
 * 该类使每个IBinder单例化，从而可以实现统一hook，但没有处理单例可能引起的其他问题(如果有的话)
 * @author xupengpai
 * @date 2016年5月23日 下午5:09:52
 */
public abstract class ServiceInterfaceProxy {

	private final static String TAG = ServiceInterfaceProxy.class.getSimpleName();

	protected String serviceName;
	private Class<?> serviceInterface;
	private DProxy dproxy;
	

	public DProxy getDProxy() {
		return dproxy;
	}

	protected ServiceInterfaceProxy(String serviceName,Class<?> serviceInterface) {
		this.serviceName = serviceName;
		this.serviceInterface = serviceInterface;
	}

	protected abstract void invokeHandle();

	// 使用双重动态代理实现兼容性高的代理
	public boolean doHook() {
		// TODO Auto-generated method stub
		
		if(serviceName == null || serviceInterface == null)
			return false;
		
		final ClassLoader cl = serviceInterface.getClassLoader();
		Log.i(TAG, serviceInterface.getName() + ":: hook()...cl=" + cl);
		
		// 获取旧的对象
		HashMap<String, IBinder> sCache = (HashMap<String, IBinder>) RefUtil
				.getFieldValue(ServiceManager.class, "sCache");
		Log.i(TAG, serviceInterface.getName() + ":: hook()...sCache=" + sCache);
		final IBinder binder = ServiceManager.getService(serviceName);
		Log.i(TAG, serviceInterface.getName() + ":: binder()...sCache=" + binder);
		if (binder != null) {
			DProxy binderProxy = DProxy.hook(cl, IBinder.class, binder);
			Log.i(TAG, serviceInterface.getName() + ":: binderProxy()...sCache="
					+ binderProxy);

			Method[] methods = serviceInterface.getDeclaredMethods();

			// 调试时，记得dump，有的手机接口不一样
			// ***************
			// ****************
			// ********重要*****
			Log.i(TAG, "***************" + serviceInterface.getSimpleName()
					+ "****************");

			if(Log.isDebug()) {
				try {
					for (Method m : methods) {
						String mstr = m.getReturnType().getName() + " "
								+ m.getName() + "(";
						Class<?>[] params = m.getParameterTypes();
						if (params != null && params.length > 0) {
							for (Class<?> p : params) {
								mstr += p.getName() + ",";
							}
							mstr = mstr.subSequence(0, mstr.length() - 1)
									.toString();
						}
						mstr += ")";
						Log.i(TAG, mstr);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			binderProxy.setHookHandlerByName("queryLocalInterface",
					new HookHandler() {
						@Override
						public boolean onBefore(Object origin, Method method,
								Object[] args) {
							// TODO Auto-generated method stub
							Log.d(TAG, serviceInterface.getName() + ":: binderProxy()...dproxy="
									+ dproxy);
							if (dproxy == null) {
								try {
									Class<?> cStub = Class
											.forName(serviceInterface.getName()
													+ "$Stub");
									Object service = (Object)RefUtil.callDeclaredMethod(cStub, cStub,
											"asInterface",
											new Class[] { IBinder.class },
											new Object[] { binder });

									Log.d(TAG, serviceInterface.getName() + ":: binderProxy()...serviceInterface=" + serviceInterface);
									dproxy = DProxy.hook(cl, serviceInterface, service);
								} catch (Throwable e) {
									// TODO Auto-generated catch block
									Log.e(TAG, e);
								}
								invokeHandle();
							}

							setResult(dproxy.getProxy());
							return false;
						}
					});

			// 挂钩
			if (sCache == null) {
				Log.e(TAG, "ServiceManager.sCache == null");
			} else {
				sCache.put(serviceName, (IBinder) binderProxy.getProxy());
			}
			return true;
		} else {
			// cache里面不一定放有指定的serivce，或者系统不一定有指定的serivce，可能会出现错误，需要注意以后的优化
			return false;
		}

	}

}
