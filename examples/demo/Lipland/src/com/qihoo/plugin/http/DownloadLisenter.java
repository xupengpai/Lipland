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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

/**
 * 文件下载回调接口
 * @author xupengpai 
 * @date 2014年12月18日 下午3:14:50
 *
 */
public abstract class DownloadLisenter extends ResponseLisenter{

    public static String TAG = "DownloadLisenter";
    public static int DEFAULT_BUF_SIZE = 1024*8; //缓冲区以内存中的一页为单位

	protected String savePath;
	
	public abstract void onStart(File file,long fileSize);
	public abstract void onDownloading(File file,long pos,int size,long fileTotalSize);
	public abstract void onComplete(File file,long fileSize); 
	public abstract void onThrowException(Exception e);
	
	
	@Override
	final protected void onResponse(HttpResponse httpResponse) {
		// TODO Auto-generated method stub
	     if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){  
	          org.apache.http.HttpEntity httpEntity = httpResponse.getEntity();  
	          OutputStream out = null;
	          InputStream in = null;
	          try{
		          in = httpEntity.getContent();  
		          long length = httpEntity.getContentLength();
		          byte[] buf = new byte[DEFAULT_BUF_SIZE];
		          int size = 0;
		          long pos = 0;
		          File file = new File(savePath);
		          
		          onStart(file, length);
		          
		          out = new FileOutputStream(file);
		          while((size = in.read(buf)) > 0){
			          onDownloading(file, pos, size,length);
		        	  out.write(buf,0,size);
		        	  pos += size;
		          }
		          out.close();
		          in.close();
		          out = null;
		          in = null;
		          onComplete(file, length);
	          }catch(Exception e){
	        	  e.printStackTrace();
	        	  this.onThrowException(e);
	          }finally{
	        	  try{
		        	  if(out != null)
		        		  out.close();
		        	  if(in != null)
		        		  in.close();  
	        	  }catch(Exception e){
	        		  e.printStackTrace();
	        	  }
	          }  
	     }else{ 
	    	 
//	            String data = httpResponse.get.getText();
	    	 onThrowException(new RuntimeException("服务器错误："+httpResponse.getStatusLine().getReasonPhrase()+","+httpResponse.getStatusLine().getStatusCode()));
	     }
	}
	
}

