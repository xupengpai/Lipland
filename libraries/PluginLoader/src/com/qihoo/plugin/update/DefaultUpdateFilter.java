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

package com.qihoo.plugin.update;

import java.io.File;
import java.io.NotSerializableException;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Intent;

import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.UpdateInfo;
import com.qihoo.plugin.core.Log;


/*
 * 默认下载处理接口，发送广播
 * @author xupengpai
 * @date 2015年11月23日 下午2:56:16
 */
public class DefaultUpdateFilter extends UpdateFilter {
	
	private static final String TAG = DefaultUpdateFilter.class.getSimpleName();

	@Override
	public boolean onCheckUpdate() {
		return true;
	}
	
	@Override
	public void onComplete(UpdateInfo updateInfo, File file, long fileSize) {
		// TODO Auto-generated method stub
		super.onComplete(updateInfo, file, fileSize);
    	Intent intent = new Intent(Actions.ACTION_UPDATE_DOWNLOAD_COMPLETE);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, updateInfo.getTag());
    	intent.putExtra(Actions.DATA_UPDATE_INFO, updateInfo);
    	intent.putExtra(Actions.DATA_FILE_SIZE, fileSize);
    	intent.putExtra(Actions.DATA_FILE_PATH, file.getAbsolutePath());
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
	}
	
	@Override
	public void onStart(UpdateInfo updateInfo, File file, long fileSize) {
		// TODO Auto-generated method stub
		super.onStart(updateInfo, file, fileSize);

    	Intent intent = new Intent(Actions.ACTION_UPDATE_START);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, updateInfo.getTag());
    	intent.putExtra(Actions.DATA_UPDATE_INFO, updateInfo);
    	intent.putExtra(Actions.DATA_FILE_SIZE, fileSize);
    	intent.putExtra(Actions.DATA_FILE_PATH, file.getAbsolutePath());
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
	}
	
	@Override
	public void onDownloading(UpdateInfo updateInfo, File workFile, long pos,
			int size, long fileTotalSize) {
		// TODO Auto-generated method stub
		super.onDownloading(updateInfo, workFile, pos, size, fileTotalSize);
    	Intent intent = new Intent(Actions.ACTION_UPDATE_DOWNLOADING);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, updateInfo.getTag());
    	intent.putExtra(Actions.DATA_UPDATE_INFO, updateInfo);
    	intent.putExtra(Actions.DATA_FILE_SIZE, fileTotalSize);
    	intent.putExtra(Actions.DATA_CUR_SIZE, size);
    	intent.putExtra(Actions.DATA_POS, pos);
    	intent.putExtra(Actions.DATA_FILE_PATH, workFile.getAbsolutePath());
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
	}
	
	@Override
	public void onThrowException(UpdateInfo updateInfo, Exception e) {
		// TODO Auto-generated method stub
		super.onThrowException(updateInfo, e);
		Log.e(TAG, e);
    	Intent intent = new Intent(Actions.ACTION_UPDATE_DOWNLOAD_EXCEPTION);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, updateInfo.getTag());
    	intent.putExtra(Actions.DATA_UPDATE_INFO, updateInfo);
		try{
	    	intent.putExtra(Actions.DATA_EXCEPTION, e);
	    	HostGlobal.getBaseApplication().sendBroadcast(intent);
		}catch(Exception exception){
//		}catch(NotSerializableException exception){
			//有些异常对象内部没有实现序列化，导致传递可能失败
			//如果失败，则包装异常信息为一个Runtime异常，用来传递
			Exception wrapException = new RuntimeException(e.toString());
	    	intent.putExtra(Actions.DATA_EXCEPTION, wrapException);
	    	HostGlobal.getBaseApplication().sendBroadcast(intent);
		}
	}
	
	@Override
	public void onTimeout(UpdateInfo updateInfo, ConnectTimeoutException e) {
		// TODO Auto-generated method stub
		super.onTimeout(updateInfo, e);
    	Intent intent = new Intent(Actions.ACTION_UPDATE_DOWNLOAD_TIMEOUT);
    	intent.putExtra(Actions.DATA_PLUGIN_TAG, updateInfo.getTag());
    	intent.putExtra(Actions.DATA_UPDATE_INFO, updateInfo);
    	intent.putExtra(Actions.DATA_EXCEPTION, e);
    	HostGlobal.getBaseApplication().sendBroadcast(intent);
	}
	

}
