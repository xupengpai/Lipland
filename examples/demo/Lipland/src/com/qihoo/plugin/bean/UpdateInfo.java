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
import java.util.List;

/**
 * 一个插件版本的更新实体
 * 
 * @author xupengpai
 * @date 2014年12月15日 上午11:25:24
 * 
 */
public class UpdateInfo implements Serializable{

	private String tag;
    private PluginInfo pluginInfo;
    private String version;
    private String desc;
    private String md5;
    private boolean forceUpdate;
    private boolean installIfNot;
    private boolean loadOnAppStarted;
    private String icon;
    private String url;
    private List<UpdateRule> rules;
    private String tmpPath;
    

    public String getTag() {
		return tag;
	}

    public void setInstallIfNot(boolean installIfNot) {
        this.installIfNot = installIfNot;
    }

    public boolean isInstallIfNot(){
        return installIfNot;
    }

    public void setTag(String tag) {
		this.tag = tag;
	}

	public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<UpdateRule> getRules() {
        return rules;
    }

    public void setRules(List<UpdateRule> rules) {
        this.rules = rules;
    }

    public boolean isLoadOnAppStarted() {
        return loadOnAppStarted;
    }

    public void setLoadOnAppStarted(boolean loadOnAppStarted) {
        this.loadOnAppStarted = loadOnAppStarted;
    }

    public void setTmpPath(String tmpPath) {
        this.tmpPath = tmpPath;
    }

    public String getTmpPath() {
        return tmpPath;
    }
}
