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

package com.qihoo.plugin.core.hook;

import com.qihoo.plugin.bean.PluginContextInfo;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.util.RefUtil;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler.Callback;

/**
 * 
 * @author xupengpai
 * @date 2015年11月26日 下午3:11:56
 */
public class SystemCallbackHacker implements Callback {

	private final static String TAG = SystemCallbackHacker.class
			.getSimpleName();

    public static final int LAUNCH_ACTIVITY         = 100;
    public static final int PAUSE_ACTIVITY          = 101;
    public static final int PAUSE_ACTIVITY_FINISHING= 102;
    public static final int STOP_ACTIVITY_SHOW      = 103;
    public static final int STOP_ACTIVITY_HIDE      = 104;
    public static final int SHOW_WINDOW             = 105;
    public static final int HIDE_WINDOW             = 106;
    public static final int RESUME_ACTIVITY         = 107;
    public static final int SEND_RESULT             = 108;
    public static final int DESTROY_ACTIVITY        = 109;
    public static final int BIND_APPLICATION        = 110;
    public static final int EXIT_APPLICATION        = 111;
    public static final int NEW_INTENT              = 112;
    public static final int RECEIVER                = 113;
    public static final int CREATE_SERVICE          = 114;
    public static final int SERVICE_ARGS            = 115;
    public static final int STOP_SERVICE            = 116;

    public static final int CONFIGURATION_CHANGED   = 118;
    public static final int CLEAN_UP_CONTEXT        = 119;
    public static final int GC_WHEN_IDLE            = 120;
    public static final int BIND_SERVICE            = 121;
    public static final int UNBIND_SERVICE          = 122;
    public static final int DUMP_SERVICE            = 123;
    public static final int LOW_MEMORY              = 124;
    public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
    public static final int RELAUNCH_ACTIVITY       = 126;
    public static final int PROFILER_CONTROL        = 127;
    public static final int CREATE_BACKUP_AGENT     = 128;
    public static final int DESTROY_BACKUP_AGENT    = 129;
    public static final int SUICIDE                 = 130;
    public static final int REMOVE_PROVIDER         = 131;
    public static final int ENABLE_JIT              = 132;
    public static final int DISPATCH_PACKAGE_BROADCAST = 133;
    public static final int SCHEDULE_CRASH          = 134;
    public static final int DUMP_HEAP               = 135;
    public static final int DUMP_ACTIVITY           = 136;
    public static final int SLEEPING                = 137;
    public static final int SET_CORE_SETTINGS       = 138;
    public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
    public static final int TRIM_MEMORY             = 140;
    public static final int DUMP_PROVIDER           = 141;
    public static final int UNSTABLE_PROVIDER_DIED  = 142;
    public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
    public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
    public static final int INSTALL_PROVIDER        = 145;
    public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
    public static final int CANCEL_VISIBLE_BEHIND = 147;
    public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED = 148;
    public static final int ENTER_ANIMATION_COMPLETE = 149;
    
    private static final boolean DEBUG_MESSAGES = true;

	private Callback origin;
    
    private SystemCallbackHacker(Callback origin){
    	this.origin = origin;
    }
    
    public static SystemCallbackHacker hook(ActivityThread activityThread){
    	Handler mH = (Handler) RefUtil.getFieldValue(activityThread, ActivityThread.class, "mH");
    	if(mH != null){
    		Callback origin =  (Callback) RefUtil.getFieldValue(mH, Handler.class, "mCallback");
    		SystemCallbackHacker hacker = new SystemCallbackHacker(origin);
    		RefUtil.setDeclaredFieldValue(mH, Handler.class, "mCallback", hacker);
    		return hacker;
    	}
    	return null;
    }
    
    private String codeToString(int code) {
        if (DEBUG_MESSAGES) {
            switch (code) {
                case LAUNCH_ACTIVITY: return "LAUNCH_ACTIVITY";
                case PAUSE_ACTIVITY: return "PAUSE_ACTIVITY";
                case PAUSE_ACTIVITY_FINISHING: return "PAUSE_ACTIVITY_FINISHING";
                case STOP_ACTIVITY_SHOW: return "STOP_ACTIVITY_SHOW";
                case STOP_ACTIVITY_HIDE: return "STOP_ACTIVITY_HIDE";
                case SHOW_WINDOW: return "SHOW_WINDOW";
                case HIDE_WINDOW: return "HIDE_WINDOW";
                case RESUME_ACTIVITY: return "RESUME_ACTIVITY";
                case SEND_RESULT: return "SEND_RESULT";
                case DESTROY_ACTIVITY: return "DESTROY_ACTIVITY";
                case BIND_APPLICATION: return "BIND_APPLICATION";
                case EXIT_APPLICATION: return "EXIT_APPLICATION";
                case NEW_INTENT: return "NEW_INTENT";
                case RECEIVER: return "RECEIVER";
                case CREATE_SERVICE: return "CREATE_SERVICE";
                case SERVICE_ARGS: return "SERVICE_ARGS";
                case STOP_SERVICE: return "STOP_SERVICE";
                case CONFIGURATION_CHANGED: return "CONFIGURATION_CHANGED";
                case CLEAN_UP_CONTEXT: return "CLEAN_UP_CONTEXT";
                case GC_WHEN_IDLE: return "GC_WHEN_IDLE";
                case BIND_SERVICE: return "BIND_SERVICE";
                case UNBIND_SERVICE: return "UNBIND_SERVICE";
                case DUMP_SERVICE: return "DUMP_SERVICE";
                case LOW_MEMORY: return "LOW_MEMORY";
                case ACTIVITY_CONFIGURATION_CHANGED: return "ACTIVITY_CONFIGURATION_CHANGED";
                case RELAUNCH_ACTIVITY: return "RELAUNCH_ACTIVITY";
                case PROFILER_CONTROL: return "PROFILER_CONTROL";
                case CREATE_BACKUP_AGENT: return "CREATE_BACKUP_AGENT";
                case DESTROY_BACKUP_AGENT: return "DESTROY_BACKUP_AGENT";
                case SUICIDE: return "SUICIDE";
                case REMOVE_PROVIDER: return "REMOVE_PROVIDER";
                case ENABLE_JIT: return "ENABLE_JIT";
                case DISPATCH_PACKAGE_BROADCAST: return "DISPATCH_PACKAGE_BROADCAST";
                case SCHEDULE_CRASH: return "SCHEDULE_CRASH";
                case DUMP_HEAP: return "DUMP_HEAP";
                case DUMP_ACTIVITY: return "DUMP_ACTIVITY";
                case SLEEPING: return "SLEEPING";
                case SET_CORE_SETTINGS: return "SET_CORE_SETTINGS";
                case UPDATE_PACKAGE_COMPATIBILITY_INFO: return "UPDATE_PACKAGE_COMPATIBILITY_INFO";
                case TRIM_MEMORY: return "TRIM_MEMORY";
                case DUMP_PROVIDER: return "DUMP_PROVIDER";
                case UNSTABLE_PROVIDER_DIED: return "UNSTABLE_PROVIDER_DIED";
                case REQUEST_ASSIST_CONTEXT_EXTRAS: return "REQUEST_ASSIST_CONTEXT_EXTRAS";
                case TRANSLUCENT_CONVERSION_COMPLETE: return "TRANSLUCENT_CONVERSION_COMPLETE";
                case INSTALL_PROVIDER: return "INSTALL_PROVIDER";
                case ON_NEW_ACTIVITY_OPTIONS: return "ON_NEW_ACTIVITY_OPTIONS";
                case CANCEL_VISIBLE_BEHIND: return "CANCEL_VISIBLE_BEHIND";
                case BACKGROUND_VISIBLE_BEHIND_CHANGED: return "BACKGROUND_VISIBLE_BEHIND_CHANGED";
                case ENTER_ANIMATION_COMPLETE: return "ENTER_ANIMATION_COMPLETE";
            }
        }
        return Integer.toString(code);
    }

	@Override
	public boolean handleMessage(Message msg) {
		
		Log.i(TAG, "receive msg " + codeToString(msg.what));
		
		switch (msg.what) {
		case ACTIVITY_CONFIGURATION_CHANGED:
			IBinder token = (IBinder)msg.obj;
			if(token != null){
				try{
					InstrumentationHacker instrumentationHacker = PluginManager.getInstance().getInstrumentation();
					ActivityThread activityThread = instrumentationHacker.getActivityThread();
					Activity activity = activityThread.getActivity(token);
					
					//如果是一个正在运行的插件Activity，则拦截下来
					if(activity != null){
						Log.i(TAG, "is a activity-"+activity);
						PluginContextInfo pci = instrumentationHacker.getPluginContextInfo(activity);
						if(pci != null)
						{
							Log.i(TAG, "is a plugin activity");
							Configuration newConfig = (Configuration) RefUtil.getFieldValue(activityThread, ActivityThread.class, "mCompatConfiguration");
							Configuration oldConfig = (Configuration)RefUtil.getFieldValue(activity, Activity.class, "mCurrentConfig");
							Log.i(TAG, "newConfig="+newConfig);
							Log.i(TAG, "oldConfig="+oldConfig);
							int configChanges = newConfig.diff(oldConfig);
	
							Log.i(TAG, "configChanges="+configChanges);
							Log.i(TAG, "pci.ai.configChanges="+pci.ai.configChanges);
							Log.i(TAG, "needNewResources()="+Configuration.needNewResources(configChanges, pci.ai.configChanges));
//							Object record = InternalUtil.getActivityClientRecord(activityThread, token);
//							RefUtil.setDeclaredFieldValue(record, InternalUtil.getActivityClientRecordClass(), "paused", true);
							if(Configuration.needNewResources(configChanges, pci.ai.configChanges)){
								activity.onConfigurationChanged(newConfig);
								RefUtil.setDeclaredFieldValue(activity, Activity.class, "mCurrentConfig", new Configuration(newConfig));
								RefUtil.setDeclaredFieldValue(activity, Activity.class, "mConfigChangeFlags", 0);
								Log.i(TAG, "restartActivity()...");
								instrumentationHacker.restartActivity(activity, newConfig);
							}
							return true;
						}
					}
				}catch(Exception e){
					Log.e(TAG, e);
				}
				
			}

		default:
			if(origin != null)
				return origin.handleMessage(msg);
			return false;
		}
	}

}
