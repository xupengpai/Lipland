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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.qihoo.plugin.util.RefUtil;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ICancellationSignal;
import android.os.RemoteException;

/**
 * 包装插件contentResolver
 * @author xupengpai
 * @date 2015年11月10日 下午4:01:14
 */
public class ContentResolverWrapper {

	private ContentResolver contentResolver;
	private String pluginTag;
	private String hostPackageName;
	private String URI_PROVIDER_DISPATCHER_FORWARD = "content://%s.plugin.provider.dispatcher/forward";
	private final static String TAG = "ContentResolverWrapper";
	
	public ContentResolverWrapper(String pluginTag,String hostPackageName,ContentResolver contentResolver) {
		this.pluginTag = pluginTag;
		this.hostPackageName = hostPackageName;
		this.contentResolver = contentResolver;
	}
	
	private Uri wrapUri(Uri uri){
		
		String realUri = "";
		try {
			realUri = URLEncoder.encode(uri.toString(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e(e);
		}
		
		String url = String.format(URI_PROVIDER_DISPATCHER_FORWARD, hostPackageName)+"?realUri="+realUri;
		if(pluginTag != null)
			url += "&pluginTag="+pluginTag;
		
		Uri newUri = Uri.parse(url);
		
		return newUri;
	}

	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Uri url = wrapUri(uri);
		return contentResolver.query(url,projection,selection,selectionArgs,sortOrder);
	}

	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder,ICancellationSignal signal) {
		// TODO Auto-generated method stub
//		Uri url = wrapUri(uri);
//		try{
//			return (Cursor)RefUtil.callDeclaredMethod(contentResolver, "query", new Class[]{
//					Uri.class,String[].class,String.class,String[].class,String.class,ICancellationSignal.class
//			}, url,projection,selection,selectionArgs,sortOrder,signal);
////			return contentResolver.query(url,projection,selection,selectionArgs,sortOrder,signal);
//		}catch(Exception e){
//			Log.e(TAG, e);
//			e.printStackTrace();
//		}
		return this.query(uri, projection, selection, selectionArgs, sortOrder);
	}

	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		Uri url = wrapUri(uri);
		return contentResolver.getType(url);
	}

	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Uri url = wrapUri(uri);
		return contentResolver.insert(url, values);
	}

	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		Uri url = wrapUri(uri);
		return contentResolver.delete(url,selection,selectionArgs);
	}

	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		Uri url = wrapUri(uri);
		return contentResolver.update(url,values,selection,selectionArgs);
	}
	

	public int bulkInsert(Uri uri, ContentValues[] values)
			throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.bulkInsert(uri, values);
	}

	public int bulkInsert(String packageName,Uri uri, ContentValues[] values)
			throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.bulkInsert(uri, values);
	}


	public int delete(String packageName,Uri uri, String selection, String[] selectionArgs)
			throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.delete(uri, selection, selectionArgs);
	}

	public String[] getStreamTypes(Uri uri, String mimeTypeFilter)
			throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.getStreamTypes(uri, mimeTypeFilter);
	}


	public String[] getStreamTypes(String packageName,Uri uri, String mimeTypeFilter)
			throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.getStreamTypes(uri, mimeTypeFilter);
	}


	public String getType(String packageName,Uri uri) throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.getType(uri);
	}

	public Uri insert(String packageName,Uri uri, ContentValues values) throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.insert(uri, values);
	}


	public Cursor query(String packageName,Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
			throws RemoteException {
		// TODO Auto-generated method stub
		uri = wrapUri(uri);
		return contentResolver.query(uri, projection, selection,
				selectionArgs, sortOrder);
	}

	public int update(String packageName,Uri uri, ContentValues values, String selection,
			String[] selectionArgs) throws RemoteException {
		// TODO Auto-generated method stub
		return contentResolver
				.update(uri, values, selection, selectionArgs);
	}

}
