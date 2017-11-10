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

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.content.OperationApplicationException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;


/**
 * 
 * @author xupengpai
 * @date 2015年12月16日 下午8:17:41
 */
public class ContentProviderFaker implements IContentProvider {

	private ContentResolverWrapper wrapper;
	
	public ContentProviderFaker(Application app) {
		this.wrapper = new ContentResolverWrapper(null, app.getPackageName(), app.getContentResolver());
	}
	

	@Override
	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return new IBinder() {
			@Override
			public boolean isBinderAlive() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public String getInterfaceDescriptor() throws RemoteException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean pingBinder() {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public IInterface queryLocalInterface(String descriptor) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void dump(FileDescriptor fd, String[] args)
					throws RemoteException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void dumpAsync(FileDescriptor fd, String[] args)
					throws RemoteException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean transact(int code, Parcel data, Parcel reply,
					int flags) throws RemoteException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void linkToDeath(DeathRecipient recipient, int flags)
					throws RemoteException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws RemoteException, OperationApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContentProviderResult[] applyBatch(String packageName,
			ArrayList<ContentProviderOperation> operations)
			throws RemoteException, OperationApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.bulkInsert(uri, values);
	}

	public int bulkInsert(String packageName,Uri uri, ContentValues[] values)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.bulkInsert(uri, values);
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Bundle call(String packageName,String method, String arg, Bundle extras)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.delete(uri, selection, selectionArgs);
	}

	public int delete(String packageName,Uri uri, String selection, String[] selectionArgs)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.delete(uri, selection, selectionArgs);
	}

	@Override
	public String[] getStreamTypes(Uri uri, String mimeTypeFilter)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.getStreamTypes(uri, mimeTypeFilter);
	}


	public String[] getStreamTypes(String packageName,Uri uri, String mimeTypeFilter)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.getStreamTypes(uri, mimeTypeFilter);
	}

	@Override
	public String getType(Uri uri) throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.getType(uri);
	}

	public String getType(String packageName,Uri uri) throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.getType(uri);
	}

	public Uri insert(String packageName,Uri uri, ContentValues values) throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.insert(uri, values);
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.insert(uri, values);
	}

	public AssetFileDescriptor openAssetFile(String packageName,Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public ParcelFileDescriptor openFile(String packageName,Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AssetFileDescriptor openTypedAssetFile(Uri uri,
			String mimeTypeFilter, Bundle opts) throws RemoteException,
			FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public AssetFileDescriptor openTypedAssetFile(String packageName,Uri uri,
			String mimeTypeFilter, Bundle opts) throws RemoteException,
			FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.query(uri, projection, selection,
				selectionArgs, sortOrder);
	}

	public Cursor query(String packageName,Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
			throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper.query(uri, projection, selection,
				selectionArgs, sortOrder);
	}

	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder, ICancellationSignal cancellationSignal)
							throws RemoteException{
		return wrapper.query(uri, projection, selection,
				selectionArgs, sortOrder,cancellationSignal);
	}

	public Cursor query(String packageName,Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder, ICancellationSignal cancellationSignal)
							throws RemoteException{
		return wrapper.query(uri, projection, selection,
				selectionArgs, sortOrder,cancellationSignal);
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper
				.update(uri, values, selection, selectionArgs);
	}

	public int update(String packageName,Uri uri, ContentValues values, String selection,
			String[] selectionArgs) throws RemoteException {
		// TODO Auto-generated method stub
		return wrapper
				.update(uri, values, selection, selectionArgs);
	}


}
