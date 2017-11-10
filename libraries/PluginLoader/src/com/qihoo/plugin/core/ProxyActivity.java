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

import com.qihoo.plugin.base.BaseProxyActivity;
import com.qihoo.plugin.base.ActivityStub;

import android.content.pm.ActivityInfo;
import android.os.Bundle;



/**
 * 空壳
 * @author xupengpai
 * @date 2015年11月27日 下午4:26:40
 */
public class ProxyActivity extends BaseProxyActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
	}


	@ActivityStub(launchMode = ActivityInfo.LAUNCH_SINGLE_TOP)
	public class SingleTopActivity extends ProxyActivity{}

	@ActivityStub(launchMode = ActivityInfo.LAUNCH_SINGLE_INSTANCE)
	public class SingleInstanceActivity extends ProxyActivity{}

	@ActivityStub(launchMode = ActivityInfo.LAUNCH_SINGLE_TASK)
	public class SingleTaskActivity extends ProxyActivity{}


	//[特性]Activity_[index]
	public class SingleTopActivity_1 extends SingleTopActivity{}
	public class SingleTopActivity_2 extends SingleTopActivity{}
	public class SingleTopActivity_3 extends SingleTopActivity{}
	public class SingleTopActivity_4 extends SingleTopActivity{}
	public class SingleTopActivity_5 extends SingleTopActivity{}
	public class SingleTopActivity_6 extends SingleTopActivity{}
	public class SingleTopActivity_7 extends SingleTopActivity{}
	public class SingleTopActivity_8 extends SingleTopActivity{}
	public class SingleTopActivity_9 extends SingleTopActivity{}
	public class SingleTopActivity_10 extends SingleTopActivity{}

	public class SingleInstanceActivity_1 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_2 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_3 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_4 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_5 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_6 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_7 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_8 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_9 extends SingleInstanceActivity{}
	public class SingleInstanceActivity_10 extends SingleInstanceActivity{}


	public class SingleTaskActivity_1 extends SingleTaskActivity{}
	public class SingleTaskActivity_2 extends SingleTaskActivity{}
	public class SingleTaskActivity_3 extends SingleTaskActivity{}
	public class SingleTaskActivity_4 extends SingleTaskActivity{}
	public class SingleTaskActivity_5 extends SingleTaskActivity{}
	public class SingleTaskActivity_6 extends SingleTaskActivity{}
	public class SingleTaskActivity_7 extends SingleTaskActivity{}
	public class SingleTaskActivity_8 extends SingleTaskActivity{}
	public class SingleTaskActivity_9 extends SingleTaskActivity{}
	public class SingleTaskActivity_10 extends SingleTaskActivity{

		public SingleTaskActivity_10(){

		}

	}

}
