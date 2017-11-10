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

import android.app.ActivityThread;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;

import com.qihoo.plugin.core.ContentProviderProxy;
import com.qihoo.plugin.core.ContentResolverWrapper;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.util.RefUtil;

/**
 * 
 * @author xupengpai
 * @date 2015年11月6日 下午4:36:40
 */
public class ContentResolverHacker extends ContentResolver {

	private ContentResolverWrapper contentResolverWrapper;
	private ContentProviderProxy proxy;
	private ContentResolver contentResolver;

	private final static String TAG = ContentResolverHacker.class.getSimpleName();

	public ContentResolverHacker(Context context,String pluginTag,ContentResolver contentResolver){
		super(context);
		// TODO Auto-generated constructor stub
		this.contentResolver = contentResolver;
		this.contentResolverWrapper = new ContentResolverWrapper(pluginTag, context.getPackageName(), contentResolver);
		this.proxy = new ContentProviderProxy(new ContentProvider() {
			
			@Override
			public int update(Uri uri, ContentValues values, String selection,
					String[] selectionArgs) {
				// TODO Auto-generated method stub
				return contentResolverWrapper.update(uri, values, selection, selectionArgs);
			}
			
			@Override
			public Cursor query(Uri uri, String[] projection, String selection,
					String[] selectionArgs, String sortOrder) {
				// TODO Auto-generated method stub
				return contentResolverWrapper.query(uri, projection, selection, selectionArgs, sortOrder);
			}
			
			@Override
			public boolean onCreate() {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public Uri insert(Uri uri, ContentValues values) {
				// TODO Auto-generated method stub
				return contentResolverWrapper.insert(uri, values);
			}
			
			@Override
			public String getType(Uri uri) {
				// TODO Auto-generated method stub
				return contentResolverWrapper.getType(uri);
			}
			
			@Override
			public int delete(Uri uri, String selection, String[] selectionArgs) {
				// TODO Auto-generated method stub
				return contentResolverWrapper.delete(uri, selection, selectionArgs);
			}
		}, null);
	}
	



	protected IContentProvider acquireProvider(Context c, String name) {
		IContentProvider provider = null;
		Log.i(TAG, "acquireProvider::proxy="+proxy+",c="+c+",name="+name);
		try {
			provider = (IContentProvider)RefUtil.callDeclaredMethod(
					contentResolver, "acquireProvider", new Class[] {
							Context.class, String.class }, new Object[] {
							c, name });
		}catch(Exception e){
			Log.d(TAG, "acquireProvider::", e);
		}
		
		if(provider == null)
			provider = proxy;

		return provider;
	}

	protected IContentProvider acquireExistingProvider(Context c, String name) {
		IContentProvider provider = null;
		Log.i(TAG, "acquireExistingProvider::proxy="+proxy+",c="+c+",name="+name);
		try {
			provider = (IContentProvider) RefUtil.callDeclaredMethod(
					contentResolver, "acquireExistingProvider",
					new Class[] { Context.class, String.class }, new Object[] {
							c, name });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "acquireExistingProvider::", e);
		}
		
		if(provider == null)
			provider = proxy;
		
		return provider;
	}

	public boolean releaseProvider(IContentProvider icp) {

		Log.i(TAG, "acquireProvider::proxy="+proxy+",icp="+icp);
		
		//如果是自定义代理类，直接返回true
		if(icp != null && icp instanceof ContentProviderProxy)
			return true;
		
		try {
			return (Boolean) RefUtil.callDeclaredMethod(contentResolver,
					"releaseProvider", new Class[] { IContentProvider.class },
					new Object[] { icp });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "releaseProvider::", e);
		}
		return false;
	}

	protected IContentProvider acquireUnstableProvider(Context c, String name) {
		
		IContentProvider provider = null;
		
		Log.i(TAG, "acquireUnstableProvider::proxy="+proxy+",c="+c+",name="+name);
		try {
			provider = (IContentProvider) RefUtil.callDeclaredMethod(
					contentResolver, "acquireUnstableProvider",
					new Class[] { Context.class, String.class }, new Object[] {
							c, name });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "acquireUnstableProvider::", e);
		}
		
		if(provider == null)
			provider = proxy;
		
		return provider;
	}

	public boolean releaseUnstableProvider(IContentProvider icp) {

		Log.i(TAG, "releaseUnstableProvider::proxy="+proxy+",icp="+icp);
		
		//如果是自定义代理类，直接返回true
		if(icp != null && icp instanceof ContentProviderProxy)
			return true;
		
		try {
			return (Boolean) RefUtil.callDeclaredMethod(contentResolver,
					"releaseUnstableProvider",
					new Class[] { IContentProvider.class },
					new Object[] { icp });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "releaseUnstableProvider::", e);
		}

		return false;
	}

	public void unstableProviderDied(IContentProvider icp) {

		Log.i(TAG, "unstableProviderDied::proxy="+proxy+",icp="+icp);

		//如果是自定义代理类，直接返回
		if(icp != null && icp instanceof ContentProviderProxy)
			return;
		
		try {
			RefUtil.callDeclaredMethod(contentResolver,
					"unstableProviderDied",
					new Class[] { IContentProvider.class },
					new Object[] { icp });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "unstableProviderDied::", e);
		}
	}

	public void appNotRespondingViaProvider(IContentProvider icp) {

		//如果是自定义代理类，直接返回
		if(icp != null && icp instanceof ContentProviderProxy)
			return;
		
		try {
			RefUtil.callDeclaredMethod(contentResolver,
					"appNotRespondingViaProvider",
					new Class[] { IContentProvider.class },
					new Object[] { icp });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "appNotRespondingViaProvider::", e);
		}
	}


}
