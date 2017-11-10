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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginObjectInputStream;

/**
 * 简化IO操作
 * @author xupengpai 
 * @date 2014年12月9日 上午11:50:28
 *
 */
public class IO {
    
    //以页为单位作为缓冲区，提高数据交换效率
    public static int DEFAULT_BUFFERED_SIZE = 1024 * 4; 

	public static void delete(File file){
		file.delete();
	}
	
	public static void delete(String file){
		new File(file).delete();
	}
	
	public static void mv(String source,String target){
		try {
			Runtime.getRuntime().exec(String.format("mv %s %s",source,target));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String readString(String file) throws IOException{
		return readString(new File(file));
	}

	public static String readString(File file) throws IOException{
		InputStream in = new FileInputStream(file);
		String str = readString(in);
		in.close();
		return str;
	}
	
	public static byte[] readBytes(InputStream in) throws IOException{
		byte[] buf = new byte[DEFAULT_BUFFERED_SIZE];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int c = 0;
		while((c = in.read(buf)) > 0){
			out.write(buf,0,c);
		}
		byte[] bytes = out.toByteArray();
		out.close();
		return bytes;
		
	}

	
	public static byte[] readBytes(String path) throws IOException{
		FileInputStream in = new FileInputStream(path);
		byte[] bytes = IO.readBytes(in);
		in.close();
		return bytes;
	}


	public static byte[] readBytes(File file) throws IOException{
		FileInputStream in = new FileInputStream(file);
		byte[] bytes = IO.readBytes(in);
		in.close();
		return bytes;
	}

	public static void writeBytes(File file,byte[] bytes) throws IOException{
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(bytes);
		}finally {
			if(out != null) {
				out.close();
			}
		}
	}

	public static void writeBytes(String path,byte[] bytes) throws IOException{
		writeBytes(new File(path),bytes);
	}
	
	public static String readString(InputStream in) throws IOException{
		byte[] bytes = readBytes(in);
		return new String(bytes,"UTF-8");
	}
	
	public static void writeString(OutputStream out,String str) throws IOException{
		out.write(str.getBytes());
	}

	
	public static void appendString(OutputStream out,String str) throws IOException{
		out.write(str.getBytes());
	}

	public static void appendString(File file,String str) throws IOException{
		FileOutputStream out = new FileOutputStream(file,true);
		out.write(str.getBytes());
		out.close();
	}
	
	public static void appendString(String file,String str) throws IOException{
		appendString(new File(file),str);
	}
	
	public static void writeString(File file,String str) throws IOException{
		FileOutputStream out = new FileOutputStream(file);
		out.write(str.getBytes());
		out.close();
	}
	
	public static void writeUTF8String(File file,String str) throws IOException{
		file.getParentFile().mkdirs();
		OutputStreamWriter outw = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
		outw.write(str);
		outw.close();
	}
	
	public static void writeUTF8String(String file,String str) throws IOException{
		writeUTF8String(new File(file),str);
	}
	
	public static void writeString(String file,String str) throws IOException{
		writeString(new File(file),str);
	}

	public static boolean copy(InputStream in,String target) throws IOException{
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(new File(target));
			byte[] buf = new byte[DEFAULT_BUFFERED_SIZE];
			int c = 0;
			while((c = in.read(buf)) > 0){
				out.write(buf,0,c);
			}
		}finally{
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return true;
	}

	public static boolean copy(InputStream in,File target) throws IOException{
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(target);
			byte[] buf = new byte[DEFAULT_BUFFERED_SIZE];
			int c = 0;
			while((c = in.read(buf)) > 0){
				out.write(buf,0,c);
			}
		}finally{
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return true;
	}
	
	public static boolean copy(String source,String target){
		FileInputStream in = null;;
		FileOutputStream out = null;;
		try {
			in = new FileInputStream(new File(source));
			out = new FileOutputStream(new File(target));
			byte[] buf = new byte[DEFAULT_BUFFERED_SIZE];
			int c = 0;
			while((c = in.read(buf)) > 0){
				out.write(buf,0,c);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return true;
	}
	
	public static void serialize(Serializable obj,String file) throws FileNotFoundException, IOException{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		try {
			oos.writeObject(obj);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}finally{
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public static byte[] serialize(Serializable obj){

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(out);
			oos.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(oos != null)
					oos.close();
				if(out != null)
					out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return out.toByteArray();
			
	}
	
	public static Object unserialize(String file) throws Exception{
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(file));
			return ois.readObject();
		} catch (Exception e) {
			return null;
		}finally{
			try {
				if(ois != null)
					ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Object unserialize(byte[] bytes) throws Exception{
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(in);
			return ois.readObject();
		} catch (Exception e) {
			return null;
		}finally{
			try {
				if(ois != null)
					ois.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static Object unserialize(Plugin plugin,byte[] bytes){
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		PluginObjectInputStream ois = null;
		try {
			ois = new PluginObjectInputStream(in,plugin);
			return ois.readObject();
		} catch (Exception e) {
			return null;
		}finally{
			try {
				if(ois != null)
					ois.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Object cloneObject(Object obj){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;

		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(out);
			oos.writeObject(obj);

			in = new ByteArrayInputStream(out.toByteArray());
			ois = new ObjectInputStream(in);
			return ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(oos != null)
					oos.close();
				if(ois != null)
					ois.close();
				if(in != null)
					in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
}
