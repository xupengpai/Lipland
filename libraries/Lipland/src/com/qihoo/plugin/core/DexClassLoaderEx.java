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

import java.io.File;
import java.util.Map;

import com.qihoo.plugin.bean.LibInfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import dalvik.system.BaseDexClassLoader;

/**
 * 重写ClassLoader，使其选择合适的类加载器来加载插件类
 * @author xupengpai
 * @date 2015年2月5日 下午5:01:46
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DexClassLoaderEx extends BaseDexClassLoader {
	
	private final static String TAG = DexClassLoaderEx.class.getSimpleName();

	private ClassLoader hostClassLoader;
	private Context context;
	private String tag;
	
	private Map<String,Map<String,LibInfo>> libs;

	public DexClassLoaderEx(String tag,Map<String,Map<String,LibInfo>> libs,Context context,String dexPath, String optimizedDirectory,
			String libraryPath, ClassLoader parent, ClassLoader hostLoader) {
		super(dexPath, new File(optimizedDirectory), libraryPath, parent);
		
		this.hostClassLoader = hostLoader;
		this.context = context;
		this.tag = tag;
		this.libs = libs;
	}

	public Class<?> loadClassOrig(String className) throws ClassNotFoundException {
		return super.loadClass(className);
	}

	@Override
	public String findLibrary(String name) {
		// TODO Auto-generated method stub
		if(libs != null){
			if(libs.containsKey(tag)){
				Map<String,LibInfo> pLibs = libs.get(tag);
				if(pLibs.containsKey(name))
					name = pLibs.get(name).mappingName;
			}
		}
		String filename = super.findLibrary(name);
		if(filename == null)
			return context.getApplicationInfo().nativeLibraryDir+"/lib"+name+".so";
		else
			return filename;
	}
	
//	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//	@Override
//	public Class<?> loadClass(String className) throws ClassNotFoundException {
//		// TODO Auto-generated method stub
//
//		Log.d(TAG, "loadClass [" + className + "]");
//		
//		Class<?> clazz = super.loadClass(className);
//		try {
//			clazz = super.loadClass(className);
//		} catch (ClassNotFoundException e) {
//			Log.d(TAG, "clazz="+clazz);
//			throw e;
//		}
//		Log.d(TAG, "clazz="+clazz);
//		return clazz;
//	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		// TODO Auto-generated method stub

		Log.d(TAG, "loadClass [" + className + "]");
		
		Class<?> clazz = null;
		try {
			try {
//				if (className.startsWith("com.qihoo.plugin")) {
//					return hostClassLoader.loadClass(className);
//				}
				clazz = super.loadClass(className);
			} catch (ClassNotFoundException e) {
				Log.d(TAG, "Class not found [" + className
						+ "],try to find from the host class loader");
				clazz = hostClassLoader.loadClass(className);
			}
		} catch (java.lang.IllegalAccessError err) {
			Log.e(TAG, "className= [" + className + "],cl=" + this + ",parent="
					+ this.getParent());
			Log.e(TAG, "className= [" + className + "],hostClassLoader="
					+ hostClassLoader + ",parent" + hostClassLoader.getParent());
			throw err;
		}

		return clazz;
	}

}
