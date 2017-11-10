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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.UpdateInfo;

/**
 * 更新管理的全局监听
 * 主要处理，当一次更新完成后(不论是否有更新失败的)
 * @author xupengpai
 * @date 2015年11月23日 下午4:05:58
 */
public class DefaultGlobalUpdateHandler implements GlobalUpdateListener{

	private List<UpdateInfo> updateList;
	private boolean hasUpdated = false;
	
	//取消"更新完成退出进程"
	private boolean cancel;
	
	private void allComplete(){

		if(hasUpdated){
			//只要有一个插件更新成功，在完成一次完全的更新流程后，发出一个通知。方便主程序处理
			HostGlobal.getBaseApplication().sendBroadcast(new Intent(Actions.ACTION_UPDATE_GLOBAL_UPDATED));
		}
		
		//插件全部更新完全
		Intent intent = new Intent(Actions.ACTION_UPDATE_GLOBAL_DOWNLOAD_ALL_COMPLETE);
		HostGlobal.getBaseApplication().sendBroadcast(intent);
		
	}
	
	@Override
	public void onBeginUpdate(List<UpdateInfo> list) {
		// TODO Auto-generated method stub
		if(list == null || list.size() == 0){
			allComplete();
		}
		hasUpdated = false;
		this.updateList = new ArrayList<UpdateInfo>();
		this.updateList.addAll(list);
	}

	@Override
	public void onUpdateFinish(int status, UpdateInfo ui) {
		// TODO Auto-generated method stub
		
		if(!hasUpdated&&status == UpdateStatus.UPDATE_STATUS_SUCCESSED && ui != null){
			hasUpdated = true;
		}
		
		synchronized (this) {
			if(ui != null && updateList != null)
				updateList.remove(ui);
		}
		
		if(updateList == null || updateList.size() == 0){
			allComplete();
		}

	}

	@Override
	public void onBeginUpdate(UpdateInfo ui) {
		// TODO Auto-generated method stub
		updateList.add(ui);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		cancel = true;
	}
	
}
