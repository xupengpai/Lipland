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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.qihoo.plugin.core.Log;

import android.text.TextUtils;

/**
 * HttpClientSession 提供简单的api可方便的从http服务器拉取数据和下载文件
 * 
 * 支持同步、异步的方式拉取http文本数据 支持异步的方式下载文件 支持cookie管理和自定义cookie
 * cookie的生命周期与它的HttpClientSession对象绑定
 * 
 * 目前只支持GET方式和HTTP协议
 * 
 * 
 * @author xupengpai
 * @date 2014年12月16日 下午2:18:48
 * 
 */
public class HttpClientSession {

	public static String TAG = "HttpClientSession";

	/** 默认编码 */
	private static String DEFAULT_ENCODING = "UTF-8";

	/** 默认线程池大小 */
	private static final int DEFAULT_MAX_THREAD_COUNT = 50;

	/** 默认超时时间 */
	private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	private static final int DEFAULT_REQUEST_TIMEOUT = 5000;

	private DefaultHttpClient httpClient;
	private String encoding;
	private Map<String, String> headers;

	/** 连接线程池对象 */
	private ExecutorService connectThreadPool;

	public void initDefaultHeaders() {
		headers.put("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Charset", encoding);
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put(
				"User-Agent",
				"Mozilla/5.0 (Linux; U; Android 4.4.4; zh-cn; MI 3W Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36 XiaoMi/MiuiBrowser/2.1.1");
	}

	public HttpClientSession(String defaultEncoding, int maxThreadCount) {
		headers = new HashMap<String, String>();

		HttpParams params = new BasicHttpParams();
		SSLSocketFactory sf = SSLSocketFactory.getSocketFactory();
		sf.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER); 
		
//		try {
//			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//			trustStore.load(null, null);
//			sf = new SSLSocketFactoryEx(trustStore);
//			sf.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER); // 允许所有主机的验证
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		SchemeRegistry schReg = new SchemeRegistry();

		// 设置一些基本参数
		// HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// HttpProtocolParams.setContentCharset(params,
		// CHARSET);
		// HttpProtocolParams.setUseExpectContinue(params, true);
		// HttpProtocolParams
		// .setUserAgent(
		// params,
		// "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
		// +
		// "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
		// 超时设置
		/* 从连接池中取连接的超时时间 */
		ConnManagerParams.setTimeout(params, 1000);
		// /* 连接超时 */
		// HttpConnectionParams.setConnectionTimeout(params, 2000);
		// /* 请求超时 */
		// HttpConnectionParams.setSoTimeout(params, 4000);

		// 设置我们的HttpClient支持HTTP和HTTPS两种模式
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		 schReg.register(new Scheme("https", SSLSocketFactory
		 .getSocketFactory(), 443));
		
		schReg.register(new Scheme("https", sf, 443));

		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
				params, schReg);

		httpClient = new DefaultHttpClient(cm, params);

		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT,
				DEFAULT_CONNECT_TIMEOUT);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				DEFAULT_REQUEST_TIMEOUT);
		httpClient.getParams().setParameter(
				ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
		connectThreadPool = Executors.newFixedThreadPool(maxThreadCount);

		this.encoding = defaultEncoding;
		initDefaultHeaders();
	}

	public HttpClientSession() {
		this(DEFAULT_ENCODING, DEFAULT_MAX_THREAD_COUNT);
	}

	/**
	 * 设置请求超时时间
	 * 
	 * @param time
	 *            超时时间,以毫秒为单位
	 */
	public void setTimeout(int time) {
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, time);
	}

	/**
	 * 获取apache HttpClient原始对象
	 * 
	 * @return DefaultHttpClient对象
	 */
	public DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * 以Get的方式同步拉取字符串数据
	 * 
	 * @param url
	 *            需要访问的URL
	 * @param params
	 *            请求参数列表，可为null
	 * @return 本次请求响应的字符串内容
	 */
	public String httpGetSync(final String url, final Map<String, String> params) {

		HttpResponseSyncLisenter lisenter = new HttpResponseSyncLisenter(url,
				Thread.currentThread(), encoding);

		Thread thread = createHttpGetSeesionThread(url, params, encoding,
				lisenter);

		synchronized (url) {
			thread.start();
			try {
				url.wait();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		HttpResponse response = lisenter.getHttpResponse();
		Exception e = lisenter.getException();
		// if(e != null)
		// throw e;
		if (e != null)
			return null;

		return lisenter.getContent();

	}

	/**
	 * 以Get的方式同步拉取字符串数据
	 * 
	 * @param url
	 *            需要访问的URL
	 * @return 本次请求响应的字符串内容
	 */
	public String httpGetSync(final String url) {
		return httpGetSync(url, null);
	}

	// 异步拉取数据
	/**
	 * 以Get的方式发送异步请求, 响应的内容的转换根据传入的lisenter不同而进行不同的处理.
	 * 
	 * @param url
	 *            请求的URL
	 * @param params
	 *            请求的参数，可为null
	 * @param lisenter
	 *            响应监听器
	 */
	public void httpGet(String url, Map<String, String> params,
			ResponseLisenter listener) {
		connectThreadPool.execute(createHttpGetSeesionThread(url, params,
				encoding, listener));
	}

	public void httpPost(String url, Map<String, String> params,
			ResponseLisenter listener) {
		connectThreadPool.execute(createHttpPostSeesionThread(url, params,
				encoding, listener));
	}

	/**
	 * @see {@link #httpGet(String, Map, ResponseLisenter)}
	 * 
	 */
	public void httpGet(String url, ResponseLisenter lisenter) {
		connectThreadPool.execute(createHttpGetSeesionThread(url, null,
				encoding, lisenter));
	}

	// public void httpGet(String url,final StringResponseLisenter lisenter){
	// connectThreadPool.execute(createHttpGetSeesionThread(url,null,encoding,lisenter));
	// }

	/**
	 * 从网络下载文件
	 * 
	 * @param url
	 *            文件url
	 * @param savePath
	 *            文件保存路径
	 * @param lisenter
	 *            文件下载监听器
	 */
	public void download(String url, final String savePath,
			final DownloadLisenter lisenter) {
		lisenter.savePath = savePath;
		Log.d(TAG,"download(),savePath="+savePath);
		connectThreadPool.execute(createHttpGetSeesionThread(url, null,
				encoding, lisenter));
	}

	/**
	 * 创建httpGet请求线程
	 * 
	 * @param url
	 * @param params
	 * @param paramCharset
	 * @param lisenter
	 * @return
	 */
	private Thread createHttpGetSeesionThread(final String url,
			final Map<String, String> params, final String paramCharset,
			final ResponseLisenter lisenter) {
		Log.d(TAG,"createHttpGetSeesionThread(),url="+url);
		return new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				Log.d(TAG,"createHttpGetSeesionThread(),Thread,url="+url);
				List<BasicNameValuePair> list = new LinkedList<BasicNameValuePair>();
				String r_url = url;
				if (params != null) {
					Iterator<String> iter = params.keySet().iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						String value = params.get(key);
						list.add(new BasicNameValuePair(key, value));
					}

					// 编码参数
					String param = URLEncodedUtils.format(list, paramCharset);

					// 拼接URL
					r_url = r_url + "?" + param;
				}

				Log.d(TAG,"createHttpGetSeesionThread(),Thread,r_url="+r_url);
				HttpGet getMethod = new HttpGet(r_url);

				lisenter.url = url;
				lisenter.defualtEncoding = paramCharset;
				lisenter.params = params;
				lisenter.requestUrl = r_url;
				lisenter.session = HttpClientSession.this;

				Iterator<String> keyIter = headers.keySet().iterator();
				while (keyIter.hasNext()) {
					String key = keyIter.next();
					getMethod.addHeader(key, headers.get(key));
				}

				HttpResponse response = null;
				// httpClient.addRequestInterceptor(new HttpRequestInterceptor()
				// {
				//
				// @Override
				// public void process(HttpRequest request, HttpContext arg1)
				// throws HttpException, IOException {
				// // TODO Auto-generated method stub
				//
				// // request.setHeader("Cookie","");
				// 
				// }
				// });
				try {

					Log.d(TAG,"createHttpGetSeesionThread(),Thread,httpClient.execute(),r_url="+r_url);
					response = httpClient.execute(getMethod);
					Log.d(TAG,"createHttpGetSeesionThread(),Thread,r_url="+r_url+",response="+response);
					lisenter.onResponse(response);
				} catch (ConnectTimeoutException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e);
					lisenter.onTimeout(e);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.e(TAG,e);
					lisenter.onThrowException(e);
				}finally {
					if(response != null){
						try {
							response.getEntity().getContent().close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	/**
	 * 创建httpGet请求线程
	 * 
	 * @param url
	 * @param params
	 * @param paramCharset
	 * @param lisenter
	 * @return
	 */
	private Thread createHttpPostSeesionThread(final String url,
			final Map<String, String> params, final String paramCharset,
			final ResponseLisenter lisenter) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				lisenter.url = url;
				lisenter.defualtEncoding = paramCharset;
				lisenter.params = params;
				lisenter.requestUrl = url;
				lisenter.session = HttpClientSession.this;

				HttpResponse response = null;

				HttpPost httPost = new HttpPost(url);

				Iterator<String> keyIter = headers.keySet().iterator();
				while (keyIter.hasNext()) {
					String key = keyIter.next();
					httPost.addHeader(key, headers.get(key));
				}

				List<NameValuePair> nvps = new ArrayList<NameValuePair>();

				if (params != null) {
					Set<String> keySet = params.keySet();
					for (String key : keySet) {
						nvps.add(new BasicNameValuePair(key, params.get(key)));
					}

					try {
						httPost.setEntity(new UrlEncodedFormEntity(nvps,
								HTTP.UTF_8));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {

					response = httpClient.execute(httPost);
					lisenter.onResponse(response);

				} catch (ConnectTimeoutException e) {
					// TODO Auto-generated catch block
					lisenter.onTimeout(e);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					lisenter.onThrowException(e);
				}
			}
		});
	}

	/**
	 * 添加自定义cookie
	 * 
	 * @param name
	 *            要添加的cookie名称
	 * @param value
	 *            要添加的cookie的值
	 */
	public void addCookie(String name, String value) {
		httpClient.getCookieStore().addCookie(
				new BasicClientCookie(name, value));
	}

	public void addCookie(String domain, String path, String name, String value) {
		BasicClientCookie bccookie = new BasicClientCookie(name, value);
		bccookie.setDomain(domain);
		bccookie.setPath(path);
		httpClient.getCookieStore().addCookie(bccookie);
	}

	/**
	 * 添加标准字符串格式cookie， 例如：user=zhangsan;pwd=qwqwqw
	 * 
	 * @param cookies
	 *            标志格式的cookie字符串
	 */
	public void addCookie(String domain, String path, String cookies) {
		System.out.println("------------------addCookie-------------");
		System.out.println("domain=" + domain);
		System.out.println("path=" + path);
		System.out.println("cookies=" + cookies);
		List<Cookie> list = CookieUtil.parseCookie(domain, path, cookies);
		for (Cookie c : list) {
			httpClient.getCookieStore().addCookie(c);
		}
	}

	/**
	 * 设置Header项
	 * 
	 * @param name
	 *            Header项名称
	 * @param value
	 *            Header项值
	 */
	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void printCookies() {
		System.out
				.println("---------------------------------------------------");
		for (Cookie c : httpClient.getCookieStore().getCookies()) {
			System.out.println("domain:" + c.getDomain());
			System.out.println("path:" + c.getPath());
			System.out.println(c.getName() + ":" + c.getValue());
		}
	}

	/**
	 * 同步请求监听器
	 * 
	 * @author xupengpai
	 * 
	 */
	private static class HttpResponseSyncLisenter extends ResponseLisenter {

		private HttpResponse httpResponse;
		private String content;
		private Exception exception;
		private Object syncObject;
		private Thread callThread;
		private String charset;

		public HttpResponseSyncLisenter(Object syncObject, Thread callThread,
				String charset) {
			this.syncObject = syncObject;
			this.callThread = callThread;
			this.charset = charset;
		}

		@Override
		public void onResponse(HttpResponse response) {
			// TODO Auto-generated method stub
			httpResponse = response;
			try {
				content = EntityUtils.toString(response.getEntity(), charset);
			} catch (Exception e) {
				onThrowException(e);
				return;
			}
			synchronized (syncObject) {
				syncObject.notifyAll();
			}
		}

		@Override
		public void onThrowException(Exception e) {
			// TODO Auto-generated method stub
			this.exception = e;
			synchronized (syncObject) {
				syncObject.notifyAll();
			}
		}

		public String getContent() {
			return content;
		}

		public HttpResponse getHttpResponse() {
			return httpResponse;
		}

		public Exception getException() {
			return exception;
		}

		@Override
		protected void onTimeout(ConnectTimeoutException e) {
			// TODO Auto-generated method stub
			Log.e(TAG, e.getMessage());
			this.exception = e;
			synchronized (syncObject) {
				syncObject.notifyAll();
			}
		}

	}
}
