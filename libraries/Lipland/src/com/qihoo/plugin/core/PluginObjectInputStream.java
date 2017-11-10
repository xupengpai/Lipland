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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import com.qihoo.plugin.bean.Plugin;

/**
 * 插件与宿主间对象序列化接口
 * @author xupengpai 
 * @date 2014年12月5日 下午5:02:50
 *
 */
public class PluginObjectInputStream extends ObjectInputStream {

	private Plugin plugin;
	
	public PluginObjectInputStream(InputStream input,Plugin plugin)
			throws StreamCorruptedException, IOException {
		super(input);
		this.plugin = plugin;
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass osClass)
			throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		try{
			return plugin.loadClass(osClass.getName());
		}catch(ClassNotFoundException e){
			return super.resolveClass(osClass);
		}
	}

}
