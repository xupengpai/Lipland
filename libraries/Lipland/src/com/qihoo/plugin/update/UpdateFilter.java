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
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import com.qihoo.plugin.IPluginUpdateListener;
import com.qihoo.plugin.bean.UpdateInfo;

/**
 * 更新过滤器
 * @author xupengpai
 * @date 2015年3月6日 上午10:28:05
 */
public abstract class UpdateFilter implements IPluginUpdateListener{

	public abstract boolean onCheckUpdate();
	
	@Override
	public boolean onUpdateList(List<UpdateInfo> list) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean onUpdate(boolean exists, UpdateInfo updateInfo) {
		// TODO Auto-generated method stub
		return true;
	}

    @Override
    public void onStart(UpdateInfo updateInfo, File file, long fileSize) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDownloading(UpdateInfo updateInfo, File workFile, long pos, int size,
            long fileTotalSize) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onComplete(UpdateInfo updateInfo, File file, long fileSize) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onThrowException(UpdateInfo updateInfo, Exception e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTimeout(UpdateInfo updateInfo, ConnectTimeoutException e) {
        // TODO Auto-generated method stub
        
    }

}
