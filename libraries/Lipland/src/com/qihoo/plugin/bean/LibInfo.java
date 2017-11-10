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

import com.qihoo.plugin.util.RefUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * so库信息
 * @author xupengpai
 * @date 2015年12月15日 上午10:46:22
 */
public class LibInfo {
	public String tag;
	public String path;
	public String fileName;
	
	//库名
	public String name;
	
	//映射库名，一般情况下与name一致，但在有命名冲突的情况下会不一样
	public String mappingName;


	@Override
	public String toString() {
		return "tag="+tag+"," +
				"path="+path+"," +
				"fileName="+fileName+"," +
				"name="+name+"," +
				"mappingName="+mappingName;
	}

	public static LibInfo fromString(String str){
		if(str == null || str.trim().equals("")){
			return null;
		}
		String data[] = str.split(",");
		if(data != null){
			LibInfo info = new LibInfo();
			for(String values : data){
				String field[] = values.split("=");
				RefUtil.setDeclaredFieldValue(info,field[0],field[1]);
			}
			return info;
		}
		return null;
	}

	public static Map<String, LibInfo> toMap(String str){
		Map<String, LibInfo> infos = new HashMap<>();
		if(str != null && !str.trim().equals("")) {
			String data[] = str.split("\\$");
			if (data != null) {
				for (String values : data) {
					LibInfo info = LibInfo.fromString(values);
					if (info != null) {
						infos.put(info.name, info);
					}
				}
			}
		}
		return infos;
	}

	public static String toString(Map<String, LibInfo> map){
		if(map != null){
			Iterator<String> keyIter = map.keySet().iterator();
			String str = "";
			while(keyIter.hasNext()){
				if(!str.equals("")){
					str += "$";
				}
				String key = keyIter.next();
				LibInfo value = map.get(key);
				str += value.toString();
			}
			return str;
		}
		return "";
	}

}
