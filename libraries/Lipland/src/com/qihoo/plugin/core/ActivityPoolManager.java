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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.content.pm.ActivityInfo;

/**
 * 代理Activity池管理，用于模拟activity的各种属性实现
 * 
 * @author xupengpai
 * @date 2014年12月18日 下午3:36:10
 */
public class ActivityPoolManager {

	public final static int LAUNCH_MODE_STANDARD = ActivityInfo.LAUNCH_MULTIPLE;
	public final static int LAUNCH_MODE_SINGLE_TOP = ActivityInfo.LAUNCH_SINGLE_TOP;
	public final static int LAUNCH_MODE_SINGLE_TASK = ActivityInfo.LAUNCH_SINGLE_TASK;
	public final static int LAUNCH_MODE_SINGLE_INSTANCE = ActivityInfo.LAUNCH_SINGLE_INSTANCE;

	private static ActivityPoolManager instance;

	public static class ProxyInfo {
		public String tag;
		public String className;
		public Activity activity;
		public int taskId;
	}

	private Map<String, List<ProxyInfo>> activityPool;

	private ActivityPoolManager() {
		activityPool = new HashMap<String, List<ProxyInfo>>();
	}

	public static ActivityPoolManager getInstance() {
		if (instance == null)
			instance = new ActivityPoolManager();
		return instance;
	}

	public void push(String className, ProxyInfo info) {
		List<ProxyInfo> list = activityPool.get(className);
		if (list == null) {
			list = new ArrayList<ProxyInfo>();
			activityPool.put(className, list);
		}
		list.add(info);
	}

	public void remove(Activity activity) {
		if (activityPool != null) {
			List<ProxyInfo> list = activityPool.get(activity.getClass()
					.getName());
			if (list != null) {
				ProxyInfo inPi = null;
				for (ProxyInfo pi : list) {
					if (pi.activity == activity) {
						inPi = pi;
						break;
					}
				}
				if (inPi != null)
					list.remove(inPi);
			}
		}
	}

	// 模拟lunch_mode
	public void doStartActivity(int taskId, String className, int launchMode) {
		List<ProxyInfo> list = activityPool.get(className);
		switch (launchMode) {
		case LAUNCH_MODE_SINGLE_TASK:
		case LAUNCH_MODE_SINGLE_INSTANCE:
			// 如果存在历史activity，则finish掉
			if (list != null) {
				for (int i = list.size() - 1; i >= 0; i--) {
					ProxyInfo pi = list.get(i);
					if ((launchMode == LAUNCH_MODE_SINGLE_TASK || launchMode==LAUNCH_MODE_SINGLE_INSTANCE)
							&& pi.taskId != taskId) {
						continue;
					}
					pi.activity.finish();
					list.remove(pi);
				}

				// 清理activity池，内存占用不多，不清除的话性高更高，可权衡
				// if(list.size() == 0){
				// activityPool.remove(className);
				// }
			}
			break;
		case LAUNCH_MODE_SINGLE_TOP:
			break;
		}
	}

}
