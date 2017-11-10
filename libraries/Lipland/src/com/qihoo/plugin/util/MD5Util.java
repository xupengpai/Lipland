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

package com.qihoo.plugin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    
    public static String md5str(String data){
		return md5str(data.getBytes());
    }
    
    public static String md5str(byte[] data){

    	// compute md5  
    	MessageDigest m = null;   
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();   
		}    
		m.update(data,0,data.length);   
		
		// get md5 bytes   
		byte p_md5Data[] = m.digest();   
		
		// create a hex string   
		String md5 = new String();   
		for (int i=0;i<p_md5Data.length;i++) {   
		     int b =  (0xFF & p_md5Data[i]);    
		// if it is a single digit, make sure it have 0 in front (proper padding)    
		    if (b <= 0xF) 
		    	md5+="0";    
		// add number to string    
		    md5+=Integer.toHexString(b); 
		 }
		// hex string to uppercase   
		return md5.toUpperCase();
    }
}
