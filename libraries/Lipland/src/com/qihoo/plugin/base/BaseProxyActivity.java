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

package com.qihoo.plugin.base;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.util.RefUtil;

/**
 * 
 * @author xupengpai
 * @date 2015年11月24日 下午7:12:50
 */
public class BaseProxyActivity extends FragmentActivity {

	private final static String TAG = BaseProxyActivity.class.getSimpleName();

	protected Activity targetActivity;
	protected PluginManager pluginManager;
	protected Plugin plugin;

	protected int taskId;

	public int getActivityTaskId() {
		// TODO Auto-generated method stub
		return taskId;
	}

	public Activity getTargetActivity() {
		return targetActivity;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public IBinder getToken() {
		return (IBinder) RefUtil.getFieldValue(this, "mToken");
	}

	/**
	 * 5.0以下的版本 将真实acitivy代理类附加到插件activity上 抛出所有异常，只要出现异常则可以当做调用失败，以便进行其他兼容处理
	 * 
	 * @throws Exception
	 */
	private void attach1() throws Exception {
		ActivityThread activityThread = ActivityThread.currentActivityThread();
		Class<?> NonConfigurationInstances_cls = Class
				.forName("android.app.Activity$NonConfigurationInstances");
		RefUtil.callDeclaredMethod(
				this,
				Activity.class,
				"attach",
				new Class[] { Context.class, ActivityThread.class,
						Instrumentation.class, IBinder.class, int.class,
						Application.class, Intent.class, ActivityInfo.class,
						CharSequence.class, Activity.class, String.class,
						NonConfigurationInstances_cls, Configuration.class },
				new Object[] {
						this,
						activityThread,
						activityThread.getInstrumentation(),
						this.getToken(),
						RefUtil.getFieldValue(this, "mIdent"),
						plugin.getApplication(),
						getIntent(),
						RefUtil.getFieldValue(this, "mActivityInfo"),
						RefUtil.getFieldValue(this, "mTitle"),
						RefUtil.getFieldValue(this, "mParent"),
						RefUtil.getFieldValue(this, "mEmbeddedID"),
						RefUtil.getFieldValue(this,
								"mNonConfigurationInstances"),
						RefUtil.getFieldValue(this, "mCurrentConfig") });
	}

	// 5.0.2左右的版本
	private void attach2() throws Exception {
		ActivityThread activityThread = ActivityThread.currentActivityThread();
		Class<?> NonConfigurationInstances_cls = Class
				.forName("android.app.Activity$NonConfigurationInstances");
		Class<?> IVoiceInteractor_cls = Class
				.forName("com.android.internal.app.IVoiceInteractor");

		RefUtil.callDeclaredMethod(
				this,
				Activity.class,
				"attach",
				new Class[] { Context.class, ActivityThread.class,
						Instrumentation.class, IBinder.class, int.class,
						Application.class, Intent.class, ActivityInfo.class,
						CharSequence.class, Activity.class, String.class,
						NonConfigurationInstances_cls, Configuration.class,
						IVoiceInteractor_cls },
				new Object[] {
						this,
						activityThread,
						activityThread.getInstrumentation(),
						this.getToken(),
						RefUtil.getFieldValue(this, "mIdent"),
						plugin.getApplication(),
						getIntent(),
						RefUtil.getFieldValue(this, "mActivityInfo"),
						RefUtil.getFieldValue(this, "mTitle"),
						RefUtil.getFieldValue(this, "mParent"),
						RefUtil.getFieldValue(this, "mEmbeddedID"),
						RefUtil.getFieldValue(this,
								"mNonConfigurationInstances"),
						RefUtil.getFieldValue(this, "mCurrentConfig"),
						RefUtil.getFieldValue(this, "mVoiceInteractor"),

				});
	}

	// 5.1.0以上的版本
	private void attach3() throws Exception {

		ActivityThread activityThread = ActivityThread.currentActivityThread();
		Class<?> NonConfigurationInstances_cls = Class
				.forName("android.app.Activity$NonConfigurationInstances");
		Class<?> IVoiceInteractor_cls = Class
				.forName("com.android.internal.app.IVoiceInteractor");

		RefUtil.callDeclaredMethod(
				this,
				Activity.class,
				"attach",
				new Class[] { Context.class, ActivityThread.class,
						Instrumentation.class, IBinder.class, int.class,
						Application.class, Intent.class, ActivityInfo.class,
						CharSequence.class, Activity.class, String.class,
						NonConfigurationInstances_cls, Configuration.class,
						String.class, IVoiceInteractor_cls },
				new Object[] {
						this,
						activityThread,
						activityThread.getInstrumentation(),
						this.getToken(),
						RefUtil.getFieldValue(this, "mIdent"),
						plugin.getApplication(),
						getIntent(),
						RefUtil.getFieldValue(this, "mActivityInfo"),
						RefUtil.getFieldValue(this, "mTitle"),
						RefUtil.getFieldValue(this, "mParent"),
						RefUtil.getFieldValue(this, "mEmbeddedID"),
						RefUtil.getFieldValue(this,
								"mNonConfigurationInstances"),
						RefUtil.getFieldValue(this, "mCurrentConfig"),
						RefUtil.getFieldValue(this, "mReferrer"),
						RefUtil.getFieldValue(this, "mVoiceInteractor"),

				});
	}

	private void printDeclaredMethods(Class<?> clz) {
		Method[] methods = clz.getDeclaredMethods();
		for (Method m : methods) {
			if (!m.getName().equals("attach"))
				continue;
			Type returnType = m.getGenericReturnType();
			String str = "";
			if (returnType == null)
				str = " void ";
			else
				str = returnType.toString();
			str += m.getName() + "(";
			Type[] types = m.getGenericParameterTypes();
			if (types != null) {
				for (Type type : types) {
					str += type + ",";
				}
				str = str.substring(0, str.length() - 1);
			}
			str += ");";
			Log.i(TAG, str);
		}
	}

	private void printMethods(Class<?> clz) {
		Method[] methods = clz.getMethods();
		for (Method m : methods) {
			if (!m.getName().equals("attach"))
				continue;
			Type returnType = m.getGenericReturnType();
			String str = "";
			if (returnType == null)
				str = "void ";
			else
				str = "[" + returnType + "] ";
			str += m.getName() + "(";
			Type[] types = m.getGenericParameterTypes();
			if (types != null) {
				for (Type type : types) {
					str += "[" + type + "],";
				}
				str = str.substring(0, str.length() - 1);
			}
			str += ");";
			Log.i(TAG, str);
		}
	}

	protected void cloneThis() {
		
//		Log.i(TAG, "------------DeclaredMethods-------------");
//		printDeclaredMethods(Activity.class);
//		Log.i(TAG, "------------Methods-------------");
//		printMethods(Activity.class);
		
		boolean successed = false;
		try {
			attach1();
			successed = true;
		} catch (Exception e) {
			Log.d(TAG, "attach1() fail");
		}
		if (!successed) {
			try {
				attach2();
				successed = true;
			} catch (Exception e) {
				Log.d(TAG, "attach3() fail");
			}
		}
		if (!successed) {
			try {
				attach3();
				successed = true;
			} catch (Exception e) {
				Log.d(TAG, "attach3() fail");
			}
		}
		
//		if(!successed)
			RefUtil.cloneObject(this, targetActivity, Activity.class);

	}

}
