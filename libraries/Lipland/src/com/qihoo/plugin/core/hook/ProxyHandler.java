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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.qihoo.plugin.core.Log;

/**
 * 
 * @author xupengpai
 * @date 2016年4月20日 上午11:19:54
 */
public class ProxyHandler implements InvocationHandler{

	private String TAG = ProxyHandler.class.getSimpleName();
	private Object origin;
	private HookHandler hookHandler;
	private Object result;
	
	public Object getResult() {
		return result;
	}
	
	public void setResult(Object result) {
		this.result = result;
	}
	
	public ProxyHandler(Object origin){
		this.origin = origin;
	}
	
	public ProxyHandler(String tag,Object origin){
		TAG = tag;
		this.origin = origin;
	}
	
	public void setOrigin(Object origin) {
		this.origin = origin;
	}
	
	public static abstract class HookHandler{
		
		Object result;
		
		public void setResult(Object result) {
			this.result = result;
		}
		
		public Object getResult() {
			return result;
		}
		
		/**
		 * 调用原方法之前，可做一些过滤处理，如果返回false，则不调用原方法
		 * @param origin
		 * @param method
		 * @param args
		 * @return
		 */
		public boolean onBefore(Object origin, Method method, Object[] args){
			return true;
		}
		
		/**
		 * 调用原方法之后，可做些后续处理，也可以改变返回值
		 * @param origin
		 * @param method
		 * @param args
		 * @param result
		 * @return
		 */
		public Object onAfter(Object origin, Method method, Object[] args,Object result,Throwable thr){
			return result;
		}
		
		public Object invoke(Object origin, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
			return method.invoke(origin, args);
		}
		
	}
	
	@Override
	public Object invoke(Object obj, Method method, Object[] args)
			throws Throwable {
		
//		// TODO Auto-generated method stub
//		Log.i(TAG, "--------------------------------------");
//		Log.i(TAG, "obj="+(obj==null?"null":obj.getClass().getName()));
//		Log.i(TAG, "method="+method.getName());
//		if(args != null){
//			for(Object arg : args) 
//				Log.i(TAG, "arg="+arg);
//				
//		}
//		if(hookHandler != null){
//			if(!hookHandler.onBefore(origin, method, args))
//					return hookHandler.result;
//		}
//		
//		Object result = null;
//		Throwable thr = null;
//		try{
//			if(hookHandler != null){
//				result = hookHandler.invoke(origin,method, args);
//			}else{
//				result = method.invoke(origin, args);
//			}
//		}catch(Throwable th){
//			thr = th;
//		}
//
//		try{
//			if(hookHandler != null){
//				hookHandler.onAfter(origin, method, args, result,thr);
//			}
//		}catch(Exception e){
//			Log.e(TAG, e);
//		}
//		
//		if(thr != null)
//			throw thr;
//		
//		return result;


		
		// TODO Auto-generated method stub
//		if(Config.DEBUG){
//			Log.i(TAG, "--------------------------------------");
//			Log.i(TAG, "obj="+(obj==null?"null":obj.getClass().getName()));
//			Log.i(TAG, "method="+method.getName());
//			if(args != null){
//				for(Object arg : args) 
//					Log.i(TAG, "arg="+arg);
//					
//			}
//		}
		
		
		if(hookHandler != null){
			if(!hookHandler.onBefore(origin, method, args))
					return hookHandler.result;
		}
		
		Object result = null;
		Throwable thr = null;
		try{
			if(hookHandler != null){
				result = hookHandler.invoke(origin,method, args);
			}else{
				result = method.invoke(origin, args);
			}
		}catch(Throwable th){
			if(th instanceof InvocationTargetException){  
                thr = ((InvocationTargetException) th).getTargetException();  
            }else{  
                //doXXX()  
    			thr = th;
            }  
		}

		try{
			if(hookHandler != null){
				result = hookHandler.onAfter(origin, method, args, result,thr);
			}
		}catch(Exception e){
			Log.e(TAG, e);
		}
		
		if(thr != null)
			throw thr;
		
		return result;
		
		
		
	}
	
	/**
	 * 根据方法名称过滤的HookHandler
	 */
	public static class NameFiltrationHookHandler extends HookHandler{

		private Map<String,HookHandler> hookHandlers;
		private final String HIT_TAG = "NameFiltrationHookHandler_hit";

		public void setHookHandler(String methodName,HookHandler handler){
			if(hookHandlers == null)
				hookHandlers = new HashMap<String, ProxyHandler.HookHandler>();
			hookHandlers.put(methodName, handler);
		}

		public HookHandler removeHookHandler(String methodName){
			if(hookHandlers != null && !TextUtils.isEmpty(methodName))
				return hookHandlers.remove(methodName);
			return null;
		}
		
		public NameFiltrationHookHandler(){
		}
		
		@Override
		public boolean onBefore(Object origin, Method method,
				Object[] args) {
			// TODO Auto-generated method stub
			String methodName = method.getName();
			if(hookHandlers != null && hookHandlers.containsKey(methodName)){

//				Log.i(HIT_TAG, "onBefore()----------------"+methodName+"------------------");
//				Log.i(HIT_TAG, "origin="+(origin==null?"null":origin.getClass().getName()));
//				Log.i(HIT_TAG, "method="+method.getName());
//				if(args != null){
//					for(Object arg : args) 
//						Log.i(HIT_TAG, "arg="+arg);
//				}
				
//				long s = System.currentTimeMillis();
				
				HookHandler handler = hookHandlers.get(methodName);
				boolean ret = handler.onBefore(origin, method, args);
				setResult(handler.getResult());
				
//				long e = System.currentTimeMillis();
//				Log.i(HIT_TAG, "time:"+(e-s));
//				Log.i(HIT_TAG, "");
				
				
				return ret;
			}else{
				return super.onBefore(origin, method, args);
			}
		}
		
		@Override
		public Object onAfter(Object origin, Method method,
				Object[] args, Object result, Throwable thr) {
			// TODO Auto-generated method stub
			String methodName = method.getName();
			if(hookHandlers != null && hookHandlers.containsKey(methodName)){
				HookHandler handler = hookHandlers.get(methodName);
				return handler.onAfter(origin, method, args, result, thr);
			}else{
				return super.onAfter(origin, method, args, result, thr);
			}


		}
		
		@Override
		public Object invoke(Object origin, Method method, Object[] args)
				throws IllegalAccessException, IllegalArgumentException,
				InvocationTargetException {
			// TODO Auto-generated method stub
			String methodName = method.getName();
			if(hookHandlers != null && hookHandlers.containsKey(methodName)){
				HookHandler handler = hookHandlers.get(methodName);
				return handler.invoke(origin, method, args);
			}else{
				return super.invoke(origin, method, args);
			}
		}

	}
	
	public void setHookHandler(HookHandler handler){
		this.hookHandler = handler;
	}
	
} 