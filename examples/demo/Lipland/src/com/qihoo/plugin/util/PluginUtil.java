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

package com.qihoo.plugin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;

/**
 * 插件接口工具
 * 
 * @author xupengpai
 * 
 */
public class PluginUtil {
	

	public static final String TAG = PluginUtil.class.getSimpleName();

	public static int verCompare(String a, String b) {

		String[] aVerBits = a.split("\\.");
		String[] bVerBits = b.split("\\.");

		int aVerBit1 = Integer.parseInt(aVerBits[0]);
		int aVerBit2 = Integer.parseInt(aVerBits[1]);
		int aVerBit3 = Integer.parseInt(aVerBits[2]);

		int bVerBit1 = Integer.parseInt(bVerBits[0]);
		int bVerBit2 = Integer.parseInt(bVerBits[1]);
		int bVerBit3 = Integer.parseInt(bVerBits[2]);

		long an = Long.parseLong(String.format("%03d%03d%03d", aVerBit1,
				aVerBit2, aVerBit3));
		long bn = Long.parseLong(String.format("%03d%03d%03d", bVerBit1,
				bVerBit2, bVerBit3));

		int r = 0;
		if (an > bn) {
			r = 1;
		} else if (an < bn) {
			r = -1;
		}
		return r;
	}

	private static void handleExternalDirs(Context hostContextImpl,Context context,String fieldName) {


		File[] dirs = (File[]) RefUtil.getFieldValue(
				hostContextImpl, fieldName);
		
		if (dirs != null) {
			List<File> list = new ArrayList<File>();
			for(File dir : dirs)
				if(dir.exists())
					list.add(dir);
			
			// 处理API 19以上的情况
			RefUtil.setFieldValue(context, fieldName,
					list.toArray(new File[]{}));
		}

	}


	public static boolean needFindPlugin(Intent intent){
		String hostPackageName = HostGlobal.getPackageName();
		ComponentName name = intent.getComponent();
		String pkgName = name != null ? name.getPackageName() : null;
		return !intent.getBooleanExtra(PluginManager.KEY_IS_PLUGIN_INTENT, false) &&
				!hostPackageName.equals(intent.getPackage()) &&   //来自宿主的请求或者限定宿主的请求均直接放行
				!hostPackageName.equals(pkgName);
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void handleExternalDirs(Context context) {

		Context hostContextImpl = HostGlobal.getBaseApplication()
				.getBaseContext();
		
		try{
			// 确认宿主外部目录全部创建好，避免插件再去创建导致因为包名出现权限问题
			hostContextImpl.getExternalCacheDir();
			hostContextImpl.getObbDir();
			
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_ALARMS);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_DCIM);

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
				hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
			}
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
				hostContextImpl.getExternalMediaDirs();
			}
			
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
			hostContextImpl.getExternalFilesDir(Environment.DIRECTORY_RINGTONES);
			
			handleExternalDirs(hostContextImpl,context,"mExternalCacheDirs");
			handleExternalDirs(hostContextImpl,context,"mExternalFilesDirs");
			handleExternalDirs(hostContextImpl,context,"mExternalMediaDirs");
			handleExternalDirs(hostContextImpl,context,"mExternalObbDirs");
			
		}catch(Exception e){
			Log.e(TAG, e);
		}
		
	}

}
