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

import android.app.Service;
import android.content.ContentProvider;
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
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;


/**
 * 
 * @author xupengpai
 * @date 2015年11月10日 下午3:36:25
 */
public class ContentProviderProxy implements IContentProvider {

	private String auths;
	private ContentProvider contentProvider;
	Service service;
	public ContentProviderProxy(ContentProvider contentProvider,
			String auths) {
		this.auths = auths;
		this.contentProvider = contentProvider;
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
		return contentProvider.applyBatch(operations);
	}

	public ContentProviderResult[] applyBatch(String packageName,
			ArrayList<ContentProviderOperation> operations)
			throws RemoteException, OperationApplicationException {
		// TODO Auto-generated method stub
		return contentProvider.applyBatch(operations);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.bulkInsert(uri, values);
	}

	public int bulkInsert(String packageName,Uri uri, ContentValues[] values)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.bulkInsert(uri, values);
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.call(method, arg, extras);
	}

	public Bundle call(String packageName,String method, String arg, Bundle extras)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.call(method, arg, extras);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.delete(uri, selection, selectionArgs);
	}

	public int delete(String packageName,Uri uri, String selection, String[] selectionArgs)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.delete(uri, selection, selectionArgs);
	}

	@Override
	public String[] getStreamTypes(Uri uri, String mimeTypeFilter)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.getStreamTypes(uri, mimeTypeFilter);
	}


	public String[] getStreamTypes(String packageName,Uri uri, String mimeTypeFilter)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.getStreamTypes(uri, mimeTypeFilter);
	}

	@Override
	public String getType(Uri uri) throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.getType(uri);
	}

	public String getType(String packageName,Uri uri) throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.getType(uri);
	}

	public Uri insert(String packageName,Uri uri, ContentValues values) throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.insert(uri, values);
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.insert(uri, values);
	}

	public AssetFileDescriptor openAssetFile(String packageName,Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return contentProvider.openAssetFile(uri, mode);
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return contentProvider.openAssetFile(uri, mode);
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return contentProvider.openFile(uri, mode);
	}

	public ParcelFileDescriptor openFile(String packageName,Uri uri, String mode)
			throws RemoteException, FileNotFoundException {
		// TODO Auto-generated method stub
		return contentProvider.openFile(uri, mode);
	}

	@Override
	public AssetFileDescriptor openTypedAssetFile(Uri uri,
			String mimeTypeFilter, Bundle opts) throws RemoteException,
			FileNotFoundException {
		// TODO Auto-generated method stub
		return contentProvider
				.openTypedAssetFile(uri, mimeTypeFilter, opts);
	}

	public AssetFileDescriptor openTypedAssetFile(String packageName,Uri uri,
			String mimeTypeFilter, Bundle opts) throws RemoteException,
			FileNotFoundException {
		// TODO Auto-generated method stub
		return contentProvider
				.openTypedAssetFile(uri, mimeTypeFilter, opts);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.query(uri, projection, selection,
				selectionArgs, sortOrder);
	}

	public Cursor query(String packageName,Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
			throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider.query(uri, projection, selection,
				selectionArgs, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider
				.update(uri, values, selection, selectionArgs);
	}

	public int update(String packageName,Uri uri, ContentValues values, String selection,
			String[] selectionArgs) throws RemoteException {
		// TODO Auto-generated method stub
		return contentProvider
				.update(uri, values, selection, selectionArgs);
	}


}
