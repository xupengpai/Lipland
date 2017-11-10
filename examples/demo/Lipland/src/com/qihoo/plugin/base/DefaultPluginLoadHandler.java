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

import android.content.Intent;

import com.qihoo.plugin.IPluginLoadListener;
import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.core.Log;

/**
 * 默认插件加载处理器，发出广播，不处理任何事件
 * @author xupengpai 
 * @date 2014年12月18日 下午2:22:55
 *
 */
public class DefaultPluginLoadHandler implements IPluginLoadListener {
	
	private final static String TAG = DefaultPluginLoadHandler.class.getSimpleName();

    @Override
    public void onStart(String tag) {
        // TODO Auto-generated method stub
    	Log.i(TAG, "onStart,tag="+tag);
    	Intent intent = new Intent(Actions.ACTION_LOAD_EVENT_START);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, tag);
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
    }

    @Override
    public void onComplete(String tag, Plugin plugin) {
        // TODO Auto-generated method stub
    	Log.i(TAG, "onLoading,tag="+tag+",plugin="+plugin);
    	Intent intent = new Intent(Actions.ACTION_LOAD_EVENT_COMPLETE);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, tag);
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
    }

    @Override
    public void onLoading(String tag, int pos) {
        // TODO Auto-generated method stub
    	Log.i(TAG, "onLoading,tag="+tag+",pos="+pos);
    	Intent intent = new Intent(Actions.ACTION_LOAD_EVENT_LOADING);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, tag);
    	intent.putExtra(Actions.DATA_POS, pos);
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
    }

    @Override
    public void onError(String tag, int code) {
        // TODO Auto-generated method stub
    	Log.e(TAG, "onError,tag="+tag+",code="+code);
    	Intent intent = new Intent(Actions.ACTION_LOAD_EVENT_ERROR);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, tag);
    	intent.putExtra(Actions.DATA_ERROR_CODE, code);
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
    }

    @Override
    public void onThrowException(String tag, Throwable thr) {
        // TODO Auto-generated method stub
    	Log.e(TAG, "onThrowException,tag="+tag+",thr="+thr);
    	Intent intent = new Intent(Actions.ACTION_LOAD_EVENT_EXCEPTION);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, tag);
    	intent.putExtra(Actions.DATA_EXCEPTION, thr);
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
    }

}
