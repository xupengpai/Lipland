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

import com.qihoo.common.ormapping.BaseDDL;
import com.qihoo.common.ormapping.DDLColumn;
import com.qihoo.common.ormapping.DDLTable;

/**
 * 插件信息
 * @author xupengpai
 * @date 2015年11月18日 下午2:33:04
 */

@DDLTable(name="installed")
public class PluginInfo extends BaseDDL{
	

	@DDLColumn(isPrimary=true)
	public String tag;
	
	@DDLColumn()
	public String path;

	@DDLColumn()
	public int versionCode;

	@DDLColumn()
	public String versionName;
	
	@DDLColumn()
	public String name;

	@DDLColumn()
	public String packageName;
	@DDLColumn()
	public String desc;
	@DDLColumn()
	public String md5;
	@DDLColumn()
	public String updateDesc;
	@DDLColumn()
	public String icon;
	@DDLColumn()
	public String url;
	@DDLColumn()
	public String fileName;

	@DDLColumn()
	public int loadOnAppStarted;

	@DDLColumn()
	public String reserved1;
	@DDLColumn()
	public String reserved2;
	@DDLColumn()
	public String reserved3;
	@DDLColumn()
	public String reserved4;
	@DDLColumn()
	public String reserved5;
	
	@Override
	public void process(Object superObject) {
		// TODO Auto-generated method stub
		
	}
	
}
