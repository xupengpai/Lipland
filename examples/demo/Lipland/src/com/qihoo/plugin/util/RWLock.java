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

package com.qihoo.plugin.util;


import com.qihoo.plugin.core.Log;

/**
 * 同步锁
 * @author xupengpai
 * @date 2015年12月4日 下午6:05:18
 */
public class RWLock {
	

	private final static String TAG = RWLock.class
			.getSimpleName();
	
	private Object lockObj = new Object();
	private Thread owner;
	
	public void lock(Thread owner) {
		synchronized (lockObj) {
			this.owner = owner;
		}
	}
	
	public void lock(){
		synchronized (lockObj) {
			if(owner != null){
				if(Thread.currentThread() != owner){
					try {
						lockObj.wait();
						
					} catch (InterruptedException e) {
						Log.e(TAG, e);
					}
				}
			}
			this.owner = Thread.currentThread();
		}
	}

	public boolean peekLock(){
		synchronized (lockObj) {
			return owner == null || Thread.currentThread() == owner;
		}
	}

	//如果已经被其他线程锁定，则返回，否则锁定
	public boolean tryLock(){
		synchronized (lockObj) {
			if( owner == null || Thread.currentThread() == owner){
				lock();
				return true;
			}else{
				return false;
			}
		}
	}
	
	public void unlock(){
		synchronized (lockObj) {
			lockObj.notifyAll();
			owner = null;
		}
	}
	
}
