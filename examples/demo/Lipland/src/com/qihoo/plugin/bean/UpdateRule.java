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

import java.io.Serializable;

/**
 * 插件更新规则定义
 * 
 * @author xupengpai
 * @date 2014年12月15日 上午10:41:34
 * 
 */
public class UpdateRule implements Serializable{

    public final static String TYPE_ANDROID = "android";
    public final static String TYPE_HOST = "host";
    public final static String TYPE_APP = "app";

    //规则针对的版本实体，有"android系统"、"插件管理器"、"应用"三种
    private String type;
    private String minVer;
    private String maxVer;
    private String vers;
    private String ignoreVers;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMinVer() {
        return minVer;
    }

    public void setMinVer(String minVer) {
        this.minVer = minVer;
    }

    public String getMaxVer() {
        return maxVer;
    }

    public void setMaxVer(String maxVer) {
        this.maxVer = maxVer;
    }

    public String getVers() {
        return vers;
    }

    public void setVers(String vers) {
        this.vers = vers;
    }

    public String getIgnoreVers() {
        return ignoreVers;
    }

    public void setIgnoreVers(String ignoreVers) {
        this.ignoreVers = ignoreVers;
    }

}
