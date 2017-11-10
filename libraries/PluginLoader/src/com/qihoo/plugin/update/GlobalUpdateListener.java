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

package com.qihoo.plugin.update;

import java.util.List;

import com.qihoo.plugin.bean.UpdateInfo;

/**
 * 插件更新的全局监听
 * @author xupengpai
 * @date 2015年4月22日 下午2:41:12
 *
 */
public interface GlobalUpdateListener {

	public void onBeginUpdate(List<UpdateInfo> list);
	public void onBeginUpdate(UpdateInfo ui);
	public void onUpdateFinish(int status,UpdateInfo ui);
	public void cancel();
	    
}
