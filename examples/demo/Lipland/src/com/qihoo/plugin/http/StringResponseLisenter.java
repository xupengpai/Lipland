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

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;

import com.qihoo.plugin.core.Log;


public abstract class StringResponseLisenter extends ResponseLisenter{
	
	public static String TAG = "StringResponseLisenter";
	
	protected HttpResponse httpResponse;
	
	@Override
	protected void onTimeout(ConnectTimeoutException e) {
		// TODO Auto-generated method stub
		Log.e(TAG, e.getMessage());
	}

	@Override
	protected void onThrowException(Exception e) {
		// TODO Auto-generated method stub
		Log.e(TAG, e.getMessage());
	}

	@Override
	final protected void onResponse(HttpResponse response) {
		// TODO Auto-generated method stub
		this.httpResponse = response;
		try {
			onResponse(EntityUtils.toString(response.getEntity(), defualtEncoding));
		} catch (Exception e) {
			onThrowException(e);
			return;
		}
	}

	public abstract void onResponse(String response);
	
	
}