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

import com.qihoo.plugin.ILog;
import com.qihoo.plugin.base.DefaultLogHandler;

/**
 * 插件模块日志接口，可外设日志处理器
 * 通过ILog接口可接入其他日志处理工具
 * 
 * @author xupengpai 
 * @date 2014年12月18日 下午3:26:11
 *
 */
public class Log {
    
    private static ILog g_handler = new DefaultLogHandler();
    
    private static boolean isDebug = true;
    
    public static void setDebug(boolean enable){
    	isDebug = enable;
    }
    
    public static void setLogHandler(ILog handler){
    	if(isDebug)
    		g_handler = handler;
    }

    public static boolean isDebug(){
        return isDebug;
    }

    public static int d(String tag, String msg){
    	if(isDebug)
    		return g_handler.d(tag, msg);
    	return 0;
    }

    public static int d(String tag, String msg, Throwable tr){
    	if(isDebug)
            return g_handler.d(tag, msg, tr);
    	return 0;
    }

    public static int i(String tag, String msg){
    	if(isDebug)
            return g_handler.i(tag, msg);
    	return 0;
    }

    public static int i(String tag, String msg, Throwable tr){
    	if(isDebug)
            return g_handler.i(tag, msg, tr);
    	return 0;
    }

    public static int w(String tag, String msg){
    	if(isDebug)
    	       return g_handler.w(tag, msg);
    	return 0;
    }

    public static int w(String tag, String msg, Throwable tr){
    	if(isDebug)
            return g_handler.w(tag, msg,tr);
    	return 0;
    }

    public static int w(String tag, Throwable tr){
    	if(isDebug)
            return g_handler.w(tag, tr);
    	return 0;
    }

    public static int e(String tag, String msg){
//    	if(isDebug)
            return g_handler.e(tag, msg);
//    	return 0;
    }

    public static int e(String tag, String msg, Throwable tr){
//    	if(isDebug)
            return g_handler.e(tag, msg, tr);
//    	return 0;
    }

    public static int e(String tag, Throwable tr){
//    	if(isDebug)
            return g_handler.e(tag, tr.getMessage(), tr);
//    	return 0;
    }
    
    public static int e(Throwable e){
//    	if(isDebug)
            return g_handler.e(e);
//    	return 0;
    }
}
