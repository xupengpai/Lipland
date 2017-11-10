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

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;


public abstract class ResponseLisenter{
	
	//调用时使用的编码
	protected String defualtEncoding;
	
	//请求时传入的URL
	protected String url;
	
	//实际请求的URL(可能拼接过参数)
	protected String requestUrl;
	
	//参数列表
	protected Map<String,String> params;
	
	//http客户端会话对象
	protected HttpClientSession session;
	
	protected abstract void onResponse(HttpResponse response);
	protected abstract void onTimeout(ConnectTimeoutException e);
	protected abstract void onThrowException(Exception e);
}