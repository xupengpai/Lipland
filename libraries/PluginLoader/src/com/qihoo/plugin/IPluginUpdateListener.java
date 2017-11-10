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

package com.qihoo.plugin;

import java.io.File;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.bean.UpdateInfo;

public interface IPluginUpdateListener {
    
    /**
     * 插件有更新的情况下回调，如果返回false，则不会更新该插件 
     * 
     * @param exists 插件是否已经安装，已经安装就表示当前是更新插件，否则表示是新下载并安装插件
     * @param updateInfo 插件更新包
     * @return
     */
    public boolean onUpdate(boolean exists,UpdateInfo updateInfo);
    

    //获取到可更新的插件列表后调用，如果返回false，则不更新插件，onUpdate()将不会触发
    public boolean onUpdateList(List<UpdateInfo> list);
    
    //开始更新时触发
    public void onStart(UpdateInfo updateInfo,File file, long fileSize);
    
    //下载插件过程中调用，PS：这里的workFile是工作目录的file，路径不在插件目录中
    public void onDownloading(UpdateInfo updateInfo,File workFile, long pos, int size, long fileTotalSize);

    //插件下载完成时调用
    public void onComplete(UpdateInfo updateInfo,File file, long fileSize);

    //下载过程中抛出异常时调用
    public void onThrowException(UpdateInfo updateInfo,Exception e);

    //下载超时时触发
    public void onTimeout(UpdateInfo updateInfo,ConnectTimeoutException e);
    
}
