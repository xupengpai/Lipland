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

public class Actions {
    
    //测试Action
    public final static String TOAST_SAY_HELLO = "com.qihoo.plugin.reader.action.toast.sayhello";
    public final static String START_READER_HOME_ACTIVITY = "com.qihoo.plugin.reader.action.home";
    
    
    //加载apk发出的事件
    public final static String ACTION_LOAD_EVENT_START = HostGlobal.getPackageName()+".action.plugin.load.event.start";
    public final static String ACTION_LOAD_EVENT_COMPLETE = HostGlobal.getPackageName()+".action.plugin.load.event.complete";
    public final static String ACTION_LOAD_EVENT_LOADING = HostGlobal.getPackageName()+".action.plugin.load.event.loading";
    public final static String ACTION_LOAD_EVENT_ERROR = HostGlobal.getPackageName()+".action.plugin.load.event.error";
    public final static String ACTION_LOAD_EVENT_EXCEPTION = HostGlobal.getPackageName()+".action.plugin.load.event.exception";

    public final static String ACTION_START_ACTIVITY_ERROR = HostGlobal.getPackageName()+".action.plugin.activity.event.start.error";

    public final static String ACTION_UPDATE_CHECK = HostGlobal.getPackageName()+".action.plugin.update.check";    
    public final static String ACTION_UPDATE_START = HostGlobal.getPackageName()+".action.plugin.update.start";    
    public final static String ACTION_UPDATE_DOWNLOADING = HostGlobal.getPackageName()+".action.plugin.update.downloading";    
    public final static String ACTION_UPDATE_DOWNLOAD_ERROR = HostGlobal.getPackageName()+".action.plugin.update.download_error";
    public final static String ACTION_UPDATE_DOWNLOAD_EXCEPTION = HostGlobal.getPackageName()+".action.plugin.update.download_exception";
    public final static String ACTION_UPDATE_DOWNLOAD_TIMEOUT = HostGlobal.getPackageName()+".action.plugin.update.download_timeout";
    public final static String ACTION_UPDATE_DOWNLOAD_COMPLETE = HostGlobal.getPackageName()+".action.plugin.update.download_complete";
    public final static String ACTION_UPDATE_GLOBAL_DOWNLOAD_ALL_COMPLETE = HostGlobal.getPackageName()+".action.plugin.update.global.download_all_complete";

    public final static String ACTION_UPDATE_GLOBAL_UPDATED = HostGlobal.getPackageName()+".action.plugin.update.global.has_updated";
    

	public final static String ACTION_PLUGIN_INSTALLED = HostGlobal.getPackageName()+".action.plugin.installed";
	

	//插件Activity事件
    public final static String ACTION_ACTIVITY_STUB_RESET = HostGlobal.getPackageName()+"action.plugin.activity.stub.reset";
    public final static String ACTION_ACTIVITY_STUB_STATUS = HostGlobal.getPackageName()+"action.plugin.activity.stub.status";
    public final static String ACTION_ACTIVITY_ON_CREATE = HostGlobal.getPackageName()+".action.plugin.activity.onCreate";
    public final static String ACTION_ACTIVITY_ON_STOP = HostGlobal.getPackageName()+".action.plugin.activity.onStop";
	public final static String ACTION_ACTIVITY_ON_START = HostGlobal.getPackageName()+".action.plugin.activity.onStart";
	public final static String ACTION_ACTIVITY_ON_RESUME = HostGlobal.getPackageName()+".action.plugin.activity.onResume";
	public final static String ACTION_ACTIVITY_ON_PAUSE = HostGlobal.getPackageName()+".action.plugin.activity.onPause";
	public final static String ACTION_ACTIVITY_ON_DESTROY = HostGlobal.getPackageName()+".action.plugin.activity.onDestroy";
	public final static String ACTION_ACTIVITY_ON_NEW_INTENT = HostGlobal.getPackageName()+".action.plugin.activity.onNewIntent";
	
	public final static String DATA_PLUGIN_INFO = "DATA_PLUGIN_INFO";
    //与发出的action有关的插件tag,String
    public final static String DATA_PLUGIN_TAG = "_tag";
    public final static String DATA_UPDATE_INFO = "_update_info";
    public final static String DATA_FILE_SIZE = "_file_size";
    public final static String DATA_CUR_SIZE = "_cur_size";

    //加载插件过程中的位置信息，int
    public final static String DATA_POS = "_pos";
    public final static String DATA_ERROR_CODE = "_error_code";
    public final static String DATA_EXCEPTION = "_exception";

    //类传递
    public final static String DATA_CLASS_NAME = "_class";
    public final static String DATA_ONLY_WIFI = "_only_wifi";
    public final static String DATA_FILE_PATH = "_file_path";
    public final static String DATA_RELOAD = "_reload";


    public final static String DATA_INTENT = "_intent";
    public final static String DATA_ICICLE = "_icicle";
    public final static String DATA_IDLE = "_idle";


    public final static String ACTION_PLUGIN_PROCESS_READY = HostGlobal.getPackageName()+"action.plugin.process.ready";
    
    
}
