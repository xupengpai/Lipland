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
import java.io.IOException;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.util.IO;

/**
 * 保存插件管理器相关的配置
 * @author xupengpai
 * @date 2015年11月18日 下午7:24:10
 */
public class Config {
	
	public final static String PLUGIN_ASSETS_DEFAULT_INSTALL_CONFIG_FILE = "plugin/default_install.xml";
	public final static String PLUGIN_DIR = "plugin";
	public final static String PLUGIN_PENDING_INSTALL_DIR = "pending_install";
	public final static String ASSETS_PLUGIN_DIR = "plugin";
	public final static String PLUGIN_WORK_DIR = "work";
	public final static String PLUGIN_CONFIG_DIR = "data";

	public static String getPluginDir(){
		String dir = HostGlobal.getBaseApplication().getFilesDir() + "/" + PLUGIN_DIR;
		new File(dir).mkdirs();
		return dir;
	}


	public static String getPluginPendingInstallDir(){
		String path = getPluginDir() + "/"
				+ Config.PLUGIN_PENDING_INSTALL_DIR;
		new File(path).mkdirs();
		return path;
	}

	public static String getLibraryInfo(String tag){
		File file = new File(getLibraryInfoConfigFile(tag));
		if(file.exists()){
			try {
				return IO.readString(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	public static String getLibraryInfoConfigFile(String tag){
		String file = HostGlobal.getBaseApplication().getFilesDir() + "/" + PLUGIN_CONFIG_DIR + "/" + tag + "/library_info";
		new File(file).getParentFile().mkdirs();
		return file;
	}

	public static void setLibraryInfo(String tag,String info){
		File file = new File(getLibraryInfoConfigFile(tag));
		try {
			IO.writeString(file,info);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getPluginDir(String tag){
		String dir = HostGlobal.getBaseApplication().getFilesDir() + "/" + PLUGIN_DIR + "/" + tag;
		new File(dir).mkdirs();
		return dir;
	}
	
	public static String getPluginDataDir(String tag){
		return getPluginDir(tag);
	}
	
	public static String getPluginDataDir(PluginInfo pi){
		return getPluginDir(pi.tag);
	}

	public static String getLibPath(String tag) {
		return getCurrentProcessPluginWorkDir(tag) + "/lib/armeabi";
	}


	public static String getCurrentProcessPluginWorkDir(String tag) {

		//为每个不同进程建立不同的工作目录，避免文件变动导致不同步产生不可预知的问题，但是相对比较占空间。
//		String workPath = getPluginWorkDir(tag) + "/" + HostGlobal.getProcessName();
//		workPath = workPath.replaceAll(":", "_");


		//实践中，冲突的情况比较少，这里为每个插件只设定一个工作目录
		String workPath = getPluginWorkDir(tag) + "/";
		new File(workPath).mkdirs();
		return workPath;
	}

//	public static String getCurrentProcessPluginWorkDir(String tag) {
//		String workPath = getCurrentProcessPluginWorkDir() + "/" + tag;
//		new File(workPath).mkdirs();
//		return workPath;
//	}


//	public static String getPluginWorkDir() {
//		String workPath = getPluginDir() + "/"
//				+ Config.PLUGIN_WORK_DIR;
//		new File(workPath).mkdirs();
//		return workPath;
//	}

	public static String getPluginWorkDir(String tag) {
		String workPath = getPluginDir(tag) + "/"
				+ Config.PLUGIN_WORK_DIR;
		new File(workPath).mkdirs();
		return workPath;
	}
	
	public static String getDexWorkPath(String tag){
		String workPath = getCurrentProcessPluginWorkDir(tag) + "/dex";
		new File(workPath).mkdirs();
		return workPath;
	}
	
}
