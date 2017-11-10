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

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.qihoo.plugin.bean.Plugin;

/**
 * 
 * @author xupengpai
 * @date 2015年12月1日 下午7:05:25
 */
public abstract class ConfigFilter {
	
	public void startActivity(Plugin plugin,Intent intent){
		
	}
	
	/**
	 * 启动一个插件activity时，获取真实定义在宿主中的activity，重载该方法可以给插件activity做映射，自定义配置
	 * @param plugin
	 * @param className
	 * @param origin
	 * @return
	 */
	public abstract Class<?> getProxyActivity(String tag,ActivityInfo ai,Intent intent,String className,Class<?> origin);

}
