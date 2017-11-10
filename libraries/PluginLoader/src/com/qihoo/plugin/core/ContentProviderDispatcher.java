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


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.install.InstallManager;
import com.qihoo.plugin.util.RefUtil;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.pm.PackageParser.Provider;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.text.TextUtils;

/**
 * 
 * 一个真实的ContentProvider，用于处理插件ContentProvider的调用协议解包和事件分发
 * @author xupengpai
 * @date 2015年11月10日 下午3:45:17
 */
public class ContentProviderDispatcher extends ContentProvider {
	
	private final static String TAG = ContentProviderDispatcher.class.getSimpleName();
	
	private Map<String,ContentProvider> contentProviderInstances;

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		contentProviderInstances = new HashMap<String, ContentProvider>();
		return true;
	}

	//如果pluginTag为null，则查询所有，否则使用指定tag的插件加载
	private ContentProvider getProviderInstance(String pluginTag,String realUri){
		
		PluginManager pluginManager = PluginManager.getInstance();
		InstallManager installManager = pluginManager.getInstallManager();
		Plugin plugin = null;
		Provider provider = null;
		
		if(!TextUtils.isEmpty(pluginTag)){

			plugin = getPlugin(pluginTag);

			if(plugin != null){
				//根据tag和uri查询provider的class
				provider = installManager.queryProvider(pluginTag, realUri);
			}else{
				Log.e(TAG, "getProviderInstance::getPlugin(),error");
			}

		}else{
			//查询所有插件，从其中找一个符合条件的Provider，按照android规范来说，必然只能有一个符合条件
			provider = installManager.queryFirstProvider(realUri);
			PluginPackage pluginPackage = installManager.queryPluginInfoByProvider(provider);
			if(pluginPackage != null){
				plugin = getPlugin(pluginPackage.tag);
			}
			
		}
		
		if(plugin != null && provider != null){
			Class<?> cls = null;
			String className = provider.className; 
			
			try{
				cls = plugin.loadClass(className);
			}catch(Exception e){
				Log.d(TAG, "unwrap:: "+className+" not found");
				Log.e(TAG,e);
			}
			if(cls != null){
				try {
					return (ContentProvider) cls.newInstance();
				} catch (Exception e) {
					Log.e(TAG,e);
					return null;
				}
			}
				
		}else{
			Log.d(TAG, "unwrap:: plugin "+pluginTag+" not found");
		}
		
		return null;
	}
	
	private Plugin getPlugin(String tag){
		PluginManager pluginManager = PluginManager.getInstance();
		if(!pluginManager.isLoaded(tag)){
			return pluginManager.load(tag);
		}
		return pluginManager.getPlugin(tag);
	}
	
	private ContentProvider unwrap(Uri uri){
		
		if("/forward".equals(uri.getEncodedPath())){
			ContentProvider provider = null;
			String pluginTag = uri.getQueryParameter("pluginTag");
			String realUri = uri.getQueryParameter("realUri");
			String key = null;
			
			if(TextUtils.isEmpty(realUri)){
				return null;
			}
			
			if(TextUtils.isEmpty(pluginTag)){
				key = realUri.toString();
			}else{
				key = pluginTag+"|"+realUri;
			}
			
			if(contentProviderInstances.containsKey(key)){
				provider = contentProviderInstances.get(key);
			}else{
				provider = getProviderInstance(TextUtils.isEmpty(pluginTag)?null:pluginTag,realUri);
				if(provider != null){
					//将当前类的Provider环境全部拷贝到插件Provider中，忽略拷贝失败的属性
//					RefUtil.cloneObject(this, provider);
					cloneObject(this,provider,this.getClass());
					if(provider.onCreate())
						contentProviderInstances.put(key, provider);
				}
			}
			
			return provider;
			
		}else{
			Log.d(TAG, "unwrap:: unknow path");
		}
		return null;
	}



	public static void cloneObject(Object src,Object target,Class clz){
		do{
			Field[] fields = clz.getDeclaredFields();
			for(Field field : fields){
				field.setAccessible(true);
				Object value = null;
				try {
					value = field.get(src);
				} catch (IllegalAccessException e) {
					Log.e(TAG,e);
				}
				try {
					field.set(target, value);
				} catch (Exception e) {
//						e.printStackTrace();
				}
			}
			clz = clz.getSuperclass();
		}while(!clz.equals(Object.class));
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Log.i(TAG, "query:: uri="+uri);
		
		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "query:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "query:: contentProvider="+contentProvider);
		if(contentProvider != null)
			return contentProvider.query(Uri.parse(realUri), projection, selection, selectionArgs, sortOrder);
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder,
			CancellationSignal cancellationSignal) {
		// TODO Auto-generated method stub

		Log.i(TAG, "query:: uri="+uri);
		Log.i(TAG, "query:: cancellationSignal="+cancellationSignal);
		
		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "query:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "query:: contentProvider="+contentProvider);
		if(contentProvider != null)
			return contentProvider.query(Uri.parse(realUri), projection, selection, selectionArgs, sortOrder,
						cancellationSignal);
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		Log.i(TAG, "getType:: uri="+uri);

		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "getType:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "getType:: contentProvider="+contentProvider);
		if(contentProvider != null)
			return contentProvider.getType(Uri.parse(realUri));
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Log.i(TAG, "insert:: uri="+uri);

		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "insert:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "insert:: contentProvider="+contentProvider);
		if(contentProvider != null)
			return contentProvider.insert(Uri.parse(realUri), values);
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		Log.i(TAG, "delete:: uri="+uri);

		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "delete:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "delete:: contentProvider="+contentProvider);
		if(contentProvider != null)
			return contentProvider.delete(Uri.parse(realUri), selection, selectionArgs);
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		Log.i(TAG, "update:: uri="+uri);

		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "update:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "update:: contentProvider="+contentProvider);
		if(contentProvider != null)
			return contentProvider.update(Uri.parse(realUri), values, selection, selectionArgs);
		return 0;
	}
	
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub

		Log.i(TAG, "bulkInsert:: uri="+uri);

		String realUri = uri.getQueryParameter("realUri");
		Log.i(TAG, "bulkInsert:: realUri="+realUri);
		
		ContentProvider contentProvider = unwrap(uri);
		Log.i(TAG, "bulkInsert:: contentProvider="+contentProvider);
		
		if(contentProvider != null)
			return contentProvider.bulkInsert(Uri.parse(realUri), values);
		return super.bulkInsert(uri, values);
	}

}
