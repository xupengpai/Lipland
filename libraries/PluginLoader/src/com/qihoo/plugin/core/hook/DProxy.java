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

import com.qihoo.plugin.core.hook.ProxyHandler.HookHandler;



/**
 * 
 * @author xupengpai
 * @date 2016年5月18日 下午1:02:07
 *
 */
public class DProxy {
	
	private final static String TAG = DProxy.class.getSimpleName();

	private Object origin;
	private Object proxy;
	private ProxyHandler proxyHandler;
	private ProxyHandler.NameFiltrationHookHandler nameFiltrationHookHandler;

	private DProxy(ClassLoader classLoader,Class<?>[] interfaces,Object origin){
		this.origin = origin;
		proxyHandler =  new ProxyHandler(origin);
		this.proxy = Proxy.newProxyInstance(classLoader,interfaces, proxyHandler);
	}
	
	public ProxyHandler getProxyHandler() {
		return proxyHandler;
	}
	
	
	public Object getOrigin() {
		return origin;
	}
	
	public Object getProxy() {
		return proxy;
	}


	public void setHookHandlerByName(String methodName,HookHandler handler){
		if(handler != null){
			if(nameFiltrationHookHandler == null){
				nameFiltrationHookHandler = new ProxyHandler.NameFiltrationHookHandler();
				this.proxyHandler.setHookHandler(nameFiltrationHookHandler);
			}
			nameFiltrationHookHandler.setHookHandler(methodName, handler);
		}
	}
	
	public static DProxy hook(ClassLoader cl,Class<?>[] interfaces,Object origin){
		return new DProxy(cl,interfaces,origin);
	}
	
	public static DProxy hook(ClassLoader cl,Class<?>interf,Object origin){
		return new DProxy(cl,new Class<?>[]{interf},origin);
	}

}
