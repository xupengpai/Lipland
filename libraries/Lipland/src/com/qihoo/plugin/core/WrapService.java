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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;

import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.core.hook.ActivityManagerHacker;
import com.qihoo.plugin.util.RefUtil;

public class WrapService extends Service {
	
    public static final String TAG = "WrapService";
     
    private AidlDispatcher aidlDispatcher;

    private Map<String,CachedService> cachedServices = new HashMap<String,CachedService>();

    public static final String ORI_INTENT = PluginManager.KEY_ORIGIN_INTENT;

    /**
     * 为保持托管service常驻,需要在使用service对象时,使用该属性进行实现插件service的生命周期,当需要stop插件service时,
     * 继续使用startService,该参数设置为 {@link #STOP_SERVICE}
     */
    public static final String SERVICE_COMMAND = "startup_service_command";
    public static final String SERVICE_START_ID = "__start_id";
    
    /**
     * 预留字段,暂时无需设置,直接使用{@link PluginService#startService(Intent)即可}
     */
    public static final int START_SERVICE = 0x00;

    /**
     * 因为stopService会直接退出托管service,所以插件在stopService时,需要使用将属性
     * {@link #SERVICE_COMMAND} 设置为当前值
     */
    public static final int STOP_SERVICE = 0x01;
    /**
     * 预留字段,暂时无需设置,直接使用
     * {@link PluginService#bindService(Intent, android.content.ServiceConnection, int)
     * 即可}
     */
    public static final int BIND_SERVICE = 0x02;
    /**
     * 预留字段,暂时无需设置,直接使用
     * {@link PluginService#unbindService(android.content.ServiceConnection)即可}
     */
    public static final int UNBIND_SERVICE = 0x03;

    PluginManager pluginManager;

    Handler mHandler = new Handler(Looper.getMainLooper());
    
    private String genKey(String tag,String className){
    	if(tag==null)
    		return className;
    	else
    		return className + "|" + tag;
    }
    
    private String getTagByKey(String key){
    	String[] cs = key.split("\\|");
    	if(cs.length>1)
    		return cs[1];
    	return null;
    }

    private String getClassNameByKey(String key){
   		return key.split("\\|")[0];
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pluginManager = PluginManager.getInstance();
        aidlDispatcher = new AidlDispatcher(this);
    }
    
    
    private void stopService(CachedService cachedService,int startId){

    	//引用计数递减
    	if(cachedService.refCount > 0){
        	cachedService.refCount--;
    	}
    	
    	if(cachedService.refCount == 0)
    		cachedService.isStoped = true;
    	
		//还有binder对象，则不销毁服务
    	if(cachedService.binder == null){
    		//引用计数为0是销毁服务
    		if(startId < 0 || cachedService.isStoped)
    			destroyService(cachedService);
		}
    	
    	
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
    	Log.i(TAG, "WrapService  onStartCommand");
      
        if (intent == null || !intent.getBooleanExtra(PluginManager.KEY_IS_PLUGIN_INTENT, false)) {
        	Log.e(TAG, "onStartCommand() error");
            return Service.START_STICKY;
        }
        
        Parcelable parcelableExtra = intent.getParcelableExtra(ORI_INTENT);
        if (parcelableExtra == null) {
        	Log.e(TAG, "onStartCommand() error");
            return Service.START_STICKY;
        }
        
        Intent oriIntent = (Intent) parcelableExtra;
        String tag = intent.getStringExtra(PluginManager.KEY_PLUGIN_TAG);
        String className = intent.getStringExtra(PluginManager.KEY_TARGET_CLASS_NAME);

        if(TextUtils.isEmpty(className) || TextUtils.isEmpty(tag)){
        	Log.e(TAG, "tag/className can't be null");
            return Service.START_STICKY;
        }
        
        CachedService cachedService = null;
        int command = intent.getIntExtra(SERVICE_COMMAND, START_SERVICE);
        switch(command){
        case STOP_SERVICE:
    		cachedService = getCachedService(tag,className,false);
    		
            if (cachedService != null) {
            	
        		int stop_startId = intent.getIntExtra(SERVICE_START_ID, -1);
        		stopService(cachedService,stop_startId);
        		
            }else{
            	Log.e(TAG, "onStartCommand()::error,STOP_SERVICE,service not found,tag="+tag+",className="+className);
            }
        	break;
        case BIND_SERVICE:
        	bindPluginService(tag,className,oriIntent);
        	break;
        case UNBIND_SERVICE:
        	unbindPluginService(tag,className,oriIntent);
        	break;
        	default:
        		cachedService = getCachedService(tag,className,true);
                if(cachedService != null){
            		cachedService.isStoped = false;
                    cachedService.isStarted = true;
                    cachedService.refCount++;
                    cachedService.proxyService.onStartCommand(oriIntent, flags, startId);
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    

    private CachedService getCachedService(String tag,String className,boolean create) {
    	String key = genKey(tag, className);
        CachedService cachedService = null;
    	 if (cachedServices.containsKey(key)) {
         	cachedService = cachedServices.get(key);
         }else if(create){
         	cachedService = createCachedService(tag, className);
         	if(cachedService != null){
	         	cachedService.proxyService.onCreate();
	         	cachedServices.put(key, cachedService);
         	}
         }
    	 return cachedService;
    }
    
    
    public void bindPluginService(String tag,String className,Intent intent){
        Log.d(TAG, "bindPluginService(),tag="+tag+",className="+className+",intent="+intent);
        CachedService cachedService = getCachedService(tag,className,true);
        Log.d(TAG, "bindPluginService(),cachedService="+cachedService);
        if (cachedService!= null) {
        	if(cachedService.binder == null){
            	IBinder binder = (IBinder)cachedService.proxyService.onBind(intent);
                Log.d(TAG, "bindPluginService(),binder="+binder);
            	if(binder != null){
            		cachedService.binder = binder;
                    Log.d(TAG, "bindPluginService(),binder="+binder);
            		try {
                        Log.d(TAG, "bindPluginService(),binder.getInterfaceDescriptor()="+binder.getInterfaceDescriptor());
	            		this.aidlDispatcher.addBinder(binder.getInterfaceDescriptor(),(Binder)binder);
                        Log.d(TAG, "bindPluginService(),aidlDispatcher="+aidlDispatcher);
					} catch (RemoteException e) {
                        Log.e(TAG, "bindPluginService(),aidlDispatcher", e);
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
        	}
        }
    }
    
    
    public void unbindPluginService(String tag,String className,Intent intent){
        CachedService cachedService = getCachedService(tag,className,false);
        if (cachedService!= null) {
        	if(cachedService.binder != null){
            	cachedService.proxyService.onUnbind(intent);
            	cachedService.binder = null;
            	
            	//如果在此之前调用过stopService，则销毁服务
            	if(cachedService.isStoped || cachedService.refCount <= 0){
            		destroyService(cachedService);
            	}
//            	stopService(cachedService, -1);
            	
        	}
        }
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "WrapService  onStart");
        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "WrapService  onBind");
        return aidlDispatcher;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "WrapService  onRebind");
    } 
 
    @Override
    public boolean onUnbind(Intent intent) {
        
    	Log.i(TAG, "WrapService  onUnbind");
        synchronized (cachedServices) {
            for (CachedService cachedService : cachedServices.values()) {
            	if(cachedService.binder != null){
            		cachedService.proxyService.onUnbind(intent);
            		cachedService.binder = null;
            	}
            }
        }
       return super.onUnbind(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        synchronized (cachedServices) {
            for (CachedService cachedService : cachedServices.values()) {
                cachedService.proxyService.onTaskRemoved(rootIntent);
            }
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "WrapService  onDestroy");
        // TODO 缓存,如果有需要自动重启的service时,下次启动托管service将这些service也启动
        synchronized (cachedServices) {
            for (CachedService cachedService : cachedServices.values()) {
                cachedService.proxyService.onDestroy();
            }
            cachedServices.clear();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        synchronized (cachedServices) {
            for (CachedService cachedService : cachedServices.values()) {
                cachedService.proxyService.onLowMemory();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        synchronized (cachedServices) {
            for (CachedService cachedService : cachedServices.values()) {
                cachedService.proxyService.onTrimMemory(level);
            }
        }
    }

//    class ServiceFinalMethodInvokeHandler implements InvocationHandler {
//
//        private Object activityManager;
//
//        public ServiceFinalMethodInvokeHandler(Object iActivityManager) {
//            this.activityManager = iActivityManager;
//        }
//
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            if ("stopServiceToken".equals(method.getName()) && args != null && args.length == 3) {
//                synchronized (cachedServices) {
//                    CachedService stopCachedService = null;
//                    for (CachedService cachedService : cachedServices.values()) {
//                        if (args[1] == cachedService.tokenBinder) {
//                            stopCachedService = cachedService;
//                            break;
//                        }
//                    }
//                    if (stopCachedService != null) {
 //						stopService();
//                    	destroyService(stopCachedService);
//                        return true;
//                    }
//                }
//                return false;
//            }
//            return method.invoke(activityManager, args);
//        }
//
//        public IActivityManager create() {
//            Object newProxyInstance = Proxy.newProxyInstance(getClassLoader(), activityManager
//                    .getClass().getInterfaces(), this);
//            return (IActivityManager) newProxyInstance;
//        }
//
//    }

    private void destroyService(CachedService cachedService) {
    	String key = genKey(cachedService.pluginTag, cachedService.serviceName);
    	cachedService.proxyService.onDestroy();
    	cachedService.isStarted = false;
    	cachedService.isStoped = true;
    	cachedServices.remove(key);
    }

    public final void attach(Object pluginService, Context context, Object activityThreadObject,
            String className, IBinder token, Application application) {
        try {
            Method attachBaseContextMethod = ContextWrapper.class.getDeclaredMethod(
                    "attachBaseContext", Context.class);
            attachBaseContextMethod.setAccessible(true);
            attachBaseContextMethod.invoke(pluginService, context);

            RefUtil.setFieldValueDeep(pluginService, "mThread", activityThreadObject);
            RefUtil.setFieldValueDeep(pluginService, "mClassName", className);
            RefUtil.setFieldValueDeep(pluginService, "mToken", token);
            RefUtil.setFieldValueDeep(pluginService, "mApplication", application);
            IActivityManager activityManager = ActivityManagerHacker.hook().getProxy();//new ServiceFinalMethodInvokeHandler(getActivityManager()).create();
            RefUtil.setFieldValueDeep(pluginService, "mActivityManager", activityManager);
            RefUtil.setFieldValueDeep(pluginService, "mStartCompatibility",
                    getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.ECLAIR);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            ((InvocationTargetException) e).getTargetException().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Object getActivityManager() {
        try {
            Class<?> forNameActivityManagerNative = Class
                    .forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = forNameActivityManagerNative.getDeclaredMethod("getDefault");
            getDefaultMethod.setAccessible(true);
            return getDefaultMethod.invoke(getDefaultMethod);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 得到插件对象，没有加载则先加载
     * @param tag
     * @return
     */
	private Plugin getPlugin(String tag){
		PluginManager pluginManager = PluginManager.getInstance();
		if(!pluginManager.isLoaded(tag)){
			return pluginManager.load(tag);
		}
		return pluginManager.getPlugin(tag);
	}

	/**
	 * 创建服务
	 * @param tag
	 * @param className
	 * @return
	 */
    private CachedService createCachedService(String tag,String className) {
    	
        Plugin plugin = getPlugin(tag);
        if (plugin == null) {
            Log.e(TAG, "插件未成功加载");
            return null;
        }

        Service service = null;
        try {
            java.lang.ClassLoader cl = plugin.getCl();
            service = (Service) cl.loadClass(className).newInstance();
        } catch (Exception e) {
            Log.e(TAG, e);
            Log.e(TAG, "plugin.tag="+plugin.getTag());
            Log.e(TAG, "plugin.path="+plugin.getPath());
            Log.e(TAG, "plugin.cl="+plugin.getCl());
            Log.e(TAG, "plugin="+plugin);
            return null;
        }

        Application app = plugin.getApplication();
        Binder tokenBinder = new Binder();
        attach(service, app, null, className, tokenBinder, app);
        final CachedService cachedService = new CachedService();
        cachedService.pluginTag = tag;
        cachedService.serviceName = className;
        cachedService.proxyService = service;
        cachedService.tokenBinder = tokenBinder;
        cachedService.isStarted = false;
        cachedService.refCount = 0;
        
        return cachedService;
    }

    class CachedService {
        public String pluginTag;
        public String pluginPkg;
        public String serviceName;
        public int bindCount;
        public int refCount;
        public boolean isStarted;
        public boolean isStoped;
        public Service proxyService;
        public Binder tokenBinder;
        // TODO 当service被系统干掉后,重启策略
        public int reStartFlag;
        public IBinder binder;
    }

}
