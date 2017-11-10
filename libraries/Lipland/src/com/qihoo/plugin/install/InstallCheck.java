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

package com.qihoo.plugin.install;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 静态插件安装检测
 * @author xupengpai
 * @date 2015年3月5日 上午11:39:20
 */
public class InstallCheck {
	

    public static final String SUFFIX_HOST_APP_VERSION_NAME = "_host_app_version_name";
    public static final String PREFS_NAME = "plugins";
	
    
    public static String getVersion(Context context)//获取版本号
	{
		try {
			PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
    
	public static int getVersionCode(Context context)//获取版本号(内部识别号)
	{
		try {
			PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
    
    /**
     * 已有插件是否为最新,该判断只要是为了覆盖安装时,强制将捆包内的插件覆盖掉本地的插件
     * 
     * @param context
     * @param tag
     * @return
     */
    public static final boolean isFirstInstall(Context context, String tag) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(
        		PREFS_NAME, Context.MODE_PRIVATE);
        int verCode = preferences.getInt(tag + SUFFIX_HOST_APP_VERSION_NAME, 0);
        return verCode != getVersionCode(context);
    }
    
    public static final void resetVersion(Context context, String tag) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putInt(tag + SUFFIX_HOST_APP_VERSION_NAME,
            		0);
        editor.commit();
    }

    /**
     * 保存使用的插件的拷贝时,使用的版本号. 该方法主要是针对覆盖安装 参考
     * {@link PluginConstans#isNewestVersion(Context, String)}
     * 
     * @param context
     * @param tag
     */
    public static void storeVersion(Context context, String tag) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);

        int verCode = preferences.getInt(tag + SUFFIX_HOST_APP_VERSION_NAME, 0);
        int curVerCode = getVersionCode(context);
        if(verCode != curVerCode){
            Editor editor = preferences.edit();
            editor.putInt(tag + SUFFIX_HOST_APP_VERSION_NAME,
            		curVerCode);
            editor.commit();
        }
    }
    

    public static final boolean isFirstInstall(Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(
        		PREFS_NAME, Context.MODE_PRIVATE);
        int verCode = preferences.getInt(SUFFIX_HOST_APP_VERSION_NAME, 0);
        return verCode != getVersionCode(context);
    }
    
    public static void storeVersion(Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);

        int verCode = preferences.getInt(SUFFIX_HOST_APP_VERSION_NAME, 0);
        int curVerCode = getVersionCode(context);
        if(verCode != curVerCode){
            Editor editor = preferences.edit();
            editor.putInt(SUFFIX_HOST_APP_VERSION_NAME,
            		curVerCode);
            editor.commit();
        }
    }
}
