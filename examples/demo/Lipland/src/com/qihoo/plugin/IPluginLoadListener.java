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

import com.qihoo.plugin.bean.Plugin;

public interface IPluginLoadListener {

    //插件开始加载
    public void onStart(String tag);
    
    //插件加载完毕
    public void onComplete(String tag,Plugin plugin);
    
    //插件加载进度
    public void onLoading(String tag,int pos);
    
    //加载插件时，主动触发的错误
    public void onError(String tag,int code);
    
    //加载插件时，出现的不可预知异常
    public void onThrowException(String tag,Throwable thr);
    
}
