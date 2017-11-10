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

package com.qihoo.plugin.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;

import com.qihoo.plugin.base.PluginCarshHandler;
import com.qihoo.plugin.bean.Plugin;

import android.os.Process;
import android.util.Log;

/**
 * 插件异常处理
 * @author xupengpai
 * @date 2015年12月24日 下午6:20:07
 */
public class CrashHandlerDispatcher implements UncaughtExceptionHandler {
	
	private final static String TAG = "PluginCrashHandler";
	private UncaughtExceptionHandler originHandler;
	private Map<String,UncaughtExceptionHandler> crashHandlers;
	private static CrashHandlerDispatcher instance;
	private PluginCarshHandler pluginCarshHandler;
	
	private CrashHandlerDispatcher(){
		
	}
	
	public void setCrashHandlers(
			Map<String, UncaughtExceptionHandler> crashHandlers) {
		this.crashHandlers = crashHandlers;
	}
	
	public PluginCarshHandler getPluginCarshHandler() {
		return pluginCarshHandler;
	}
	
	public static CrashHandlerDispatcher getInstance(){
		if(instance == null)
			instance = new CrashHandlerDispatcher();
		return instance;
	}

	public void setToApp(){
		originHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	private boolean isMainThread(){
		return Thread.currentThread().getId() == 1;
	}
	
	private String buildExceptionDetail(Throwable ex){

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return ex.toString()+"\r\n"+sw.getBuffer().toString();
	}
	
	public void setPluginCarshHandler(PluginCarshHandler pluginCarshHandler) {
		this.pluginCarshHandler = pluginCarshHandler;
	}
	
	private void handleException(Thread thread, Throwable ex){
		Plugin p = PluginManager.getInstance().analysisException(ex);
		if(pluginCarshHandler != null){
			pluginCarshHandler.uncaughtException(p, thread, ex,false,"");
		}
		
		if(crashHandlers != null){
			if(p == null){
				for(UncaughtExceptionHandler handler : crashHandlers.values()){
					handler.uncaughtException(thread, ex);
				}
			}else{
				if(crashHandlers.containsKey(p.getTag())){
					crashHandlers.get(p.getTag()).uncaughtException(thread, ex);
				}
			}
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if(ex == null){
			return;
		}
		
		handleException(thread,ex);
		
		/**
		 * 如果是主线程，则退出程序
		 */
		if(isMainThread()){
			Process.killProcess(Process.myPid());
		}
		
	}
	
}
