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

import java.util.Map;

import com.qihoo.plugin.core.Log;

import android.app.Activity;
import android.app.ActivityThread;
import android.os.IBinder;

/**
 * 
 * @author xupengpai
 * @date 2015年12月1日 下午2:30:59
 */
public class InternalUtil {

	private final static String TAG = InternalUtil.class
			.getSimpleName();
	private static Class<?> ActivityClientRecord_class = null;
	
	public static Class<?> getActivityClientRecordClass(){
		if(ActivityClientRecord_class == null)
			try {
				ActivityClientRecord_class = Class.forName(ActivityThread.class.getName()+"$ActivityClientRecord");
			} catch (ClassNotFoundException e) {
				Log.e(TAG, e);
			}
		return ActivityClientRecord_class;
	}

	public static Map<IBinder,Object> getActivityClientRecords(ActivityThread activityThread){
		Class<?> ActivityClientRecord_class = null;
		try {
			
			Map<IBinder,Object> mActivities = (Map<IBinder, Object>) RefUtil.getFieldValue(activityThread, ActivityThread.class, "mActivities");
			return mActivities;
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		return null;
	}

	public static Object getActivityClientRecord(ActivityThread activityThread,IBinder key){
		try {
			Map<IBinder,Object> mActivities = getActivityClientRecords(activityThread);
			if(mActivities != null)
				return mActivities.get(key);
		} catch (Exception e) {
			Log.e(TAG, e);
		}
		return null;
	}
	
	public static IBinder getActivityToken(Activity activity) {
		return (IBinder) RefUtil.getFieldValue(activity, "mToken");
	}
	
}
