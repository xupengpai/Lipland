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

package com.qihoo.plugin.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

public class CookieUtil {
	public static List<Cookie> parseCookie(String domain,String path,String cookies){
//		cookies = cookies.replaceAll("\\\"", "##@#@#@@#");
		String[] cs = cookies.split(";");
		List<Cookie> list = new ArrayList<Cookie>();
		for(String c : cs){
			String[] d = c.split("=");
			if(d.length>1){
				String value = d[1].trim();
				for(int i=2;i<d.length;i++)
					value += ("="+d[i]);
//				System.out.println(d[1].trim().replaceAll("##@#@#@@#", "\\\""));
				System.out.println(value);
//				BasicClientCookie bccookie = new BasicClientCookie(d[0].trim(), d[1].trim().replaceAll("##@#@#@@#", "\\\""));
				BasicClientCookie bccookie = new BasicClientCookie(d[0].trim(), value);
				bccookie.setDomain(domain);
				bccookie.setPath(path);
				list.add(bccookie);
			}
		}
		return list;
	}
}
