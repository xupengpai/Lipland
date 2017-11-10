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

package com.qihoo.plugin.bean;

/**
 * Created by xupengpai on 2017/5/16.
 */

public class InstallTimeInfo {

    //安装/加载耗时
    public long copyToWork;
    public long parseApk;
    public long createApplication;
    public long createClassLoader;
    public long loadResources;
    public long getInstalledPlugin;
    public long verifySign;
    public long registerReceivers;
    public long hookContext;
    public long unzipLibs;

    //启动耗时
    public long onCreate;


}
