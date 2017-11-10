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

import com.qihoo.plugin.util.RWLock;

import android.content.pm.PackageParser.Package;

/**
 * 插件信息
 * @author xupengpai
 * @date 2015年12月3日 下午5:58:40
 */
public class PluginPackage {
	
	public String tag;
	public PluginInfo pi;
	public Package pkg;
	public Throwable parseException;
	public boolean error = false;
	
	/**
	 * 同步锁，非常重要
	 * 同于同步解析插件、获取插件信息两个操作
	 */
	public RWLock syncLock = new RWLock();
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+"{tag="+tag+",pi="+pi+",pkg="+pkg+"}";
	}

}
