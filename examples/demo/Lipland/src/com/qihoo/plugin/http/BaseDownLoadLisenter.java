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

package com.qihoo.plugin.http;

import java.io.File;

import org.apache.http.conn.ConnectTimeoutException;

public class BaseDownLoadLisenter extends DownloadLisenter {
	
	public String getSavePath(){
		return savePath;
	}

	@Override
	public void onStart(File file, long fileSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDownloading(File file, long pos, int size, long fileTotalSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onComplete(File file, long fileSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onThrowException(Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTimeout(ConnectTimeoutException e) {
		// TODO Auto-generated method stub

	}

}
