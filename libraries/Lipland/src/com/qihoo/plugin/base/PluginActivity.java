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

import android.app.Activity;
import android.support.v4.app.FragmentActivity;



/**
 * 已经废弃，保持旧版插件兼容，旧版本插件Activity继承自该类
 * @author xupengpai
 * @date 2015年11月27日 下午4:34:13
 */
@Deprecated
public class PluginActivity extends FragmentActivity {
	public Activity getProxyActivity(){
		return this;
	}
}
