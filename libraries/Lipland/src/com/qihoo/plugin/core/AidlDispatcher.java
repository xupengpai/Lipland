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

package com.qihoo.plugin.core;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;

import com.qihoo.plugin.util.RWLock;
import com.qihoo.plugin.util.RefUtil;

/**
 * 处理aidl的分发
 * @author xupengpai
 * @date 2015年12月17日 下午7:13:44
 */
public class AidlDispatcher extends IAidlDispatcher.Stub{
	
	private final static String TAG = AidlDispatcher.class.getSimpleName();

	
	private Map<String,Binder> binders = new HashMap<String,Binder>();
	private WrapService service;
	
	public void addBinder(String name,Binder binder){
		binders.put(name, binder);
	}
	
	@Override
	public void test(String msg) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	public AidlDispatcher(WrapService service){
		this.service = service;
	}
	
	@Override
	public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		// TODO Auto-generated method stub

		//读取验证头
		int  strictPolicy = data.readInt();
		
		//读取描述符
		String descriptor = data.readString();
		
		//重置数据
		data.setDataPosition(0);
		
		Log.i(TAG, "strictPolicy="+strictPolicy);
		Log.i(TAG, "descriptor="+descriptor);
		Log.i(TAG, "this.getInterfaceDescriptor()="+this.getInterfaceDescriptor());

		Binder binder = binders.get(descriptor);
		
		if(binder != null){
			
			//调用描述符对应的IBinder对象
			Boolean ret = false;
			try {
				ret = (Boolean)RefUtil.callDeclaredMethod(binder, "onTransact", new Class<?>[]{
						int.class,Parcel.class,Parcel.class,int.class
				}, new Object[]{
						code,data,reply,flags
				});
				if(ret)
					return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e);
				if(e.getCause() instanceof RemoteException)
					throw (RemoteException)e;
					
			}
		}

		return super.onTransact(code, data, reply, flags);
	}

	private boolean isMainThread(){
		return Thread.currentThread().getId() == 1;
	}
	
	@Override
	public void bindService(final String tag, final String className,final Intent intent)
			throws RemoteException {
		// TODO Auto-generated method stub

		Log.i(TAG, "bindService(),tag"+tag+",className="+className+",intent="+intent);
		Log.i(TAG, "bindService(),isMainThread()="+isMainThread());
//		if(!isMainThread()){
//			final Object lock = new Object();
//			new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					try{
//						service.bindPluginService(tag, className, intent);
//					}finally{
//						synchronized (lock) {
//							lock.notifyAll();
//						}
//					}
//				}
//			});
//			try {
//				synchronized (lock) {
//					lock.wait();
//				}
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else
			{
			service.bindPluginService(tag, className, intent);
		}
	}

	@Override
	public void unbindService(final String tag, final String className, final Intent intent)
			throws RemoteException {
		// TODO Auto-generated method stub

		if(!isMainThread()){
			final Object lock = new Object();
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						service.unbindPluginService(tag, className, intent);
					}finally{
						synchronized (lock) {
							lock.notifyAll();
						}
					}
				}
			});
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			service.unbindPluginService(tag, className, intent);
		}
	}

}
