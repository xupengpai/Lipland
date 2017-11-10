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

package com.qihoo.plugin.bean;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

public class PluginContextInfo {
	public PluginContextInfo(Context context, Plugin plugin,
			String proxyActivityClass, ActivityInfo ai, Intent proxyIntent) {
		this.context = context;
		this.plugin = plugin;
		this.proxyActivityClass = proxyActivityClass;
		this.proxyIntent = proxyIntent;
		this.ai = ai;
	}

	public ActivityInfo ai;
	public Context context;
	public Plugin plugin;
	public String proxyActivityClass;
	public Intent proxyIntent;
}