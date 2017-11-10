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

/**
 * 线程数据
 * @author xupengpai
 *
 */
public class ThreadContextData {
	
	private static Map<Long,Map<Integer,Object>> g_data = new HashMap<Long,Map<Integer,Object>>();
	
	public static void putData(int index,Object data){
		
		Long tid = Long.valueOf(Thread.currentThread().getId());
		Map<Integer, Object> map = null;
		
		synchronized (g_data) {
			if(!g_data.containsKey(tid)){
				map = new HashMap<Integer, Object>();
				g_data.put(tid, map);
			}else{
				map = g_data.get(tid);
			}
			map.put(index, data);
		}
	}

	public static Object remove(int index){
		Long tid = Long.valueOf(Thread.currentThread().getId());

		synchronized (g_data) {
			if(g_data.containsKey(tid)){
				return g_data.get(tid).remove(index);
			}
		}
		return null;
	}

	public static Map<Integer,Object> clean(){
		Long tid = Long.valueOf(Thread.currentThread().getId());

		synchronized (g_data) {
			if(g_data.containsKey(tid)){
				return g_data.remove(tid);
			}
		}
		return null;
	}
	
	public static Object getData(int tag){
		
		Long tid = Long.valueOf(Thread.currentThread().getId());
		synchronized (g_data) {
			if(g_data.containsKey(tid)){
				return g_data.get(tid).get(tag);
			}
		}
		return null;
		
	}
	
}
