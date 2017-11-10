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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.util.TypedValue;

import com.qihoo.plugin.core.Log;

/**
 * android平台通用工具类
 * 
 * @author xupengpai
 * @date 2016年5月4日 下午3:09:28
 */
public class AndroidUtil {

	public final static String TAG = "AndroidUtil";

	/**
	 * Convert a translucent themed Activity
	 * {@link android.R.attr#windowIsTranslucent} back from opaque to
	 * translucent following a call to {@link #convertActivityFromTranslucent()}
	 * .
	 * <p>
	 * Calling this allows the Activity behind this one to be seen again. Once
	 * all such Activities have been redrawn
	 * <p>
	 * This call has no effect on non-translucent activities or on activities
	 * with the {@link android.R.attr#windowIsFloating} attribute.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void convertActivityToTranslucent(Activity activity) {
		try {
			Class<?>[] classes = Activity.class.getDeclaredClasses();
			Class<?> translucentConversionListenerClazz = null;
			for (Class<?> clazz : classes) {
				if (clazz.getSimpleName().contains(
						"TranslucentConversionListener")) {
					translucentConversionListenerClazz = clazz;
				}
			}
			Method[] methods = Activity.class.getDeclaredMethods();
			if (Build.VERSION.SDK_INT < 21) { //Build.VERSION_CODES.L
				Method method = Activity.class.getDeclaredMethod(
						"convertToTranslucent",
						translucentConversionListenerClazz);
				method.setAccessible(true);
				method.invoke(activity, new Object[] { null });
			} else {
				Method method = Activity.class.getDeclaredMethod(
						"convertToTranslucent",
						translucentConversionListenerClazz,
						ActivityOptions.class);
				method.setAccessible(true);
				method.invoke(activity, new Object[] { null, null });
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * 将一个activity变为转换为不透明
	 * @param activity
	 */
	public static void convertActivityFromTranslucent(Activity activity) {
		try {
			Method method = Activity.class.getDeclaredMethod("convertFromTranslucent");
			if(method != null){
				method.setAccessible(true);
				method.invoke(activity);
			}
		} catch (Throwable t) {
			Log.e(TAG,t);
//			t.printStackTrace();
		}
	}

	/**
	 * 判断一个theme是否设置了透明属性
	 * @param theme
	 */
	public static boolean isThemeTranslucent(Theme theme){
		final TypedValue outValue = new TypedValue();
		theme.resolveAttribute(android.R.attr.windowIsTranslucent, outValue, true);
		return (outValue.type==TypedValue.TYPE_INT_BOOLEAN&&outValue.data != 0);
	}

	public static String getExceptionStackTrace(Throwable thr){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(out);
		thr.printStackTrace(pw);
		return new String(out.toByteArray());
	}

}
