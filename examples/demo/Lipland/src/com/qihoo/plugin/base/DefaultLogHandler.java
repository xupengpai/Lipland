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

package com.qihoo.plugin.base;

import com.qihoo.plugin.ILog;

import android.util.Log;

/**
 * 默认日志处理器，统一使用android自带的Log处理
 * @author xupengpai 
 * @date 2014年12月18日 下午3:29:44
 *
 */
public class DefaultLogHandler implements ILog {

    @Override
    public int v(String tag, String msg) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
    	
        return Log.v(tag, msg);
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.v(tag, msg, tr);
    }

    @Override
    public int d(String tag, String msg) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.d(tag, msg);
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.d(tag, msg, tr);
    }

    @Override
    public int i(String tag, String msg) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.i(tag, msg);
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.i(tag, msg, tr);
    }

    @Override
    public int w(String tag, String msg) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.w(tag, msg);
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.w(tag, msg, tr);
    }

    @Override
    public int w(String tag, Throwable tr) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
        return Log.w(tag, tr);
    }

    @Override
    public int e(String tag, String msg) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.e(tag, msg);
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        // TODO Auto-generated method stub
    	if(tag==null)
    		tag = "null";
    	if(msg==null)
    		msg = "null";
        return Log.e(tag, msg, tr);
    }

    @Override
    public int wtf(String tag, String msg) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int wtfStack(String tag, String msg) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int wtf(String tag, Throwable tr) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int wtf(String tag, String msg, Throwable tr) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int wtf(int logId, String tag, String msg, Throwable tr, boolean localStack) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int e(Throwable e) {
        return 0;
    }

	@Override
	public int d(String msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int i(String msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int e(String msg, Throwable tr) {
		// TODO Auto-generated method stub
		return 0;
	}


}
