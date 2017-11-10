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

package com.qihoo.plugin.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.ConnectTimeoutException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Xml;
import android.webkit.URLUtil;

import com.qihoo.plugin.Config;
import com.qihoo.plugin.IPluginUpdateListener;
import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.base.PluginHelper;
import com.qihoo.plugin.base.PluginProcessListener;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.bean.PluginUpdateConfig;
import com.qihoo.plugin.bean.UpdateInfo;
import com.qihoo.plugin.bean.UpdateRule;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.http.BaseDownLoadLisenter;
import com.qihoo.plugin.http.HttpClientSession;
import com.qihoo.plugin.install.InstallManager;
import com.qihoo.plugin.util.ApkUtil;
import com.qihoo.plugin.util.IO;
import com.qihoo.plugin.util.MD5Util;
import com.qihoo.plugin.util.NetworkManager;
import com.qihoo.plugin.util.NetworkManager.INetworkChange;
import com.qihoo.plugin.util.PluginUtil;
import com.qihoo.plugin.util.RefUtil;

/**
 * 插件更新管理
 * 1、比对版本,选择合适的插件更新
 * 2、线程池下载插件
 * 3、已安装插件信息和更新信息管理
 * 
 * @author xupengpai 
 * @date 2014年12月9日 下午6:43:49
 *
 */
public class UpdateManager implements INetworkChange{

    public final static String TAG = "PluginUpdateManager";
    public final static String PLUGIN_DIR = "plugin";
    public final static String PLUGIN_UPDATE_XML = "plugin/config/update.xml";
    public final static String PLUGIN_ASSETS_DEFAULT_INSTALL = Config.PLUGIN_ASSETS_DEFAULT_INSTALL_CONFIG_FILE;
    private final static String DEFAULT_ENCODING = "utf-8";
    
    //插件更新工作目录
    public final static String PLUGIN_UPDATE_WORK_DIR = "plugin/work_update";
    
    private final static int DEFAULT_THREAD_COUNT = 5;
    public static final String FORDER_NAME = "plugin";
    
    private NetworkManager networkManager;
    private boolean onlyWifi;

    private InstallManager installManager;
    private static UpdateManager instance;
    private Context context;
    private int appVersion;
    
    private String defaultXmlPath;
    private String downloadWorkDir;
    private String pluginDir;
    private HttpClientSession httpSession;
    private GlobalUpdateListener globalUpdateListener;
    private UpdateFilter defaultUpdateFilter;
    private PluginUpdateConfig config;
    
    public static UpdateManager getInstance(){
        if(instance == null){
            try {
                instance = new UpdateManager();
                instance.setOnlyWifi(true);
            } catch (Exception e) {
               Log.e(UpdateManager.TAG, e);
            }
        }
        return instance;
    }
    
    private UpdateManager(){
    	networkManager = NetworkManager.getInstance(HostGlobal.getBaseApplication());
    	defaultUpdateFilter = new DefaultUpdateFilter();
    	globalUpdateListener= new DefaultGlobalUpdateHandler();
    }
    
    public void setOnlyWifi(boolean onlyWifi) {
		this.onlyWifi = onlyWifi;
		if(onlyWifi){
	    	networkManager.addNetworkChangeListener(this);
		}else{
			networkManager.removeNightModeListener(this);
		}
	}
    
    public boolean isOnlyWifi() {
		return onlyWifi;
	}
    
    
    /**
     * 初始化插件更新管理器
     * 该方法会消耗一定时间，需要延迟异步调用
     * 
     * @param context
     * @throws XmlPullParserException
     * @throws IOException
     * @throws NameNotFoundException
     */
    public void init(InstallManager insallManager) throws XmlPullParserException, IOException, NameNotFoundException{

    	this.installManager = insallManager;
    	this.context = HostGlobal.getBaseApplication();
    	String filesPath = context.getFilesDir().getAbsolutePath();
//        String installedPluginConfig = "/sdcard/installed.xml";
//        String updateInfoConfig = "/sdcard/update.xml";
        downloadWorkDir = filesPath + "/" + PLUGIN_UPDATE_WORK_DIR;
        pluginDir = filesPath + "/" + PLUGIN_DIR;
//        pluginDir = Config.getPluginDir();


        String updateInfoConfig = filesPath + "/" + PLUGIN_UPDATE_XML;
        
        defaultXmlPath = updateInfoConfig;
        appVersion = HostGlobal.getVersionCode();

//        load();
        
    }

    public HttpClientSession getHttpSession(){
//        if(httpSession == null)
            httpSession = new HttpClientSession(DEFAULT_ENCODING,DEFAULT_THREAD_COUNT);
        return httpSession;
    }
 
    public synchronized void load(String path){
    	Log.i(UpdateManager.TAG, "load:: path = "+path);
    	this.defaultXmlPath = path;
        File file = new File(path);
        try{
	        if(file.exists()){
	            FileInputStream in = new FileInputStream(file);
	            try{
	                config = parse(in);
	            }finally{
	                if(in != null)
	                    in.close();
	            }
	        }else{
	        	Log.e(UpdateManager.TAG, "file not found,"+path);
	        }
        }catch(Exception e){
        	Log.e(UpdateManager.TAG, e);
        }
    	
    }
    
    public void reload(String path) throws XmlPullParserException, IOException{
    	Log.i(UpdateManager.TAG, "reload:: path = "+path);
        load(path);
    }
    
    public void reload() throws XmlPullParserException, IOException{
    	Log.i(UpdateManager.TAG, "reload:: defaultXmlPath = "+defaultXmlPath);
        load(defaultXmlPath);
    }
    
    
    @SuppressLint("DefaultLocale")
    public boolean matchRange(String minVer,String maxVer,String version){
        
        String[] verBits = version.split("\\.");
        String[] minVerBits = minVer.split("\\.");
        String[] maxVerBits = maxVer.split("\\.");
        

        int verBit1 = Integer.parseInt(verBits[0]);
        int verBit2 = Integer.parseInt(verBits[1]);
        int verBit3 = Integer.parseInt(verBits[2]);

        int minVerBit1 = Integer.parseInt(minVerBits[0]);
        int minVerBit2 = Integer.parseInt(minVerBits[1]);
        int minVerBit3 = Integer.parseInt(minVerBits[2]);

        int maxVerBit1 = Integer.parseInt(maxVerBits[0]);
        int maxVerBit2 = Integer.parseInt(maxVerBits[1]);
        int maxVerBit3 = Integer.parseInt(maxVerBits[2]);

        long ver = Long.parseLong(String.format("%03%03d%03d", verBit1,verBit2,verBit3));
        long min = Long.parseLong(String.format("%03d%03d%03d", minVerBit1,minVerBit2,minVerBit3));
        long max = Long.parseLong(String.format("%03d%03d%03d", maxVerBit1,maxVerBit2,maxVerBit3));
        
        return (ver>=min && ver<=max);
        
    }
    


    public static boolean match(int min,int max,int ver){
    	return (ver>=min && ver<=max);
    }
    
    private boolean applyRules(List<UpdateRule> rules){
        
        if(rules == null || rules.size() == 0){
        	return true;
        }
        
        for(UpdateRule rule : rules){
            
            String type = rule.getType();
            String minVer = rule.getMinVer();
            String maxVer = rule.getMaxVer();
            String vers = rule.getVers();
            String ignoreVers = rule.getIgnoreVers();
            String version = PluginManager.VERSION;

            Log.d(TAG, "applyRules(),type="+type+",minVer="+minVer+",maxVer="+maxVer+",vers="+",ignoreVers="+ignoreVers+",version="+version);
            int verCode = 0;
            if(UpdateRule.TYPE_ANDROID.equals(type)){
                verCode = Build.VERSION.SDK_INT;
                version = verCode + "";
            } 
            else if(UpdateRule.TYPE_APP.equals(type)){
                verCode = appVersion;
                version = verCode + "";
            }
            
            if(ignoreVers != null){
                String[] ivers = ignoreVers.split(",");
                for(String iver : ivers){
                    //如果当前容器版本在忽略的版本列表内，则直接忽略，并无视其他规则
                    if(version.equals(iver)){
//                        updateList.remove(tag);
                        return false;
                    }
                }
                continue;
            }

//            if(match)
//                continue;
//            if(updateList.containsKey(tag))
//                continue;
            
//            boolean inWhitelist = false;
            if(vers != null){
                String[] verlist = vers.split(",");
                boolean inVers = false;
                for(String v : verlist){
                    //如果当前容器版本在白名单内，则直接加入更新列表
                    if(version.equals(v)){
//                        updateList.put(tag, update);
//                        match = true;
//                        inWhitelist = true;
//                        break;
                    	inVers = true;
                    	break;
                    }
                }
                
                if(inVers)
                	continue;
                
            }

            //如果白名单已经匹配，则无视其他规则
            if(minVer!=null&&!minVer.trim().equals("") &&maxVer!=null&&!maxVer.trim().equals("")){
                if(UpdateRule.TYPE_HOST.equals(type)){
                    //范围匹配，固定三位
                    if(matchRange(minVer,maxVer,version)){
//                        updateList.put(tag, update);
//                        match = true;
                        continue;
                    }
                }else{
                    if(verCode >= Integer.parseInt(minVer) && verCode <= Integer.parseInt(maxVer)){
//                        updateList.put(tag, update);
//                        match = true;
                        continue;
                    }
                }
            }
            
            //只要一条规则匹配不通过就不能匹配更新

//            if(!match)
            	return false;

        }                    
                
        return true;
        
    }
    
    private PluginInfo getInstalledPluginInfo(String tag){
        PluginPackage installedPlugin = installManager.getInstalledPlugin(tag);
    	if(installedPlugin != null)
    		return installedPlugin.pi;
    	return null;
    }

    //只有存在于黑名单里面，一律忽略
    //存在于白名单里面但是不在黑名单里面的，一律更新
    
    public List<UpdateInfo> checkUpdate(){
        
//        List<UpdateInfo> updateList = new ArrayList<UpdateInfo>();
        
        Map<String,UpdateInfo> updateList = new HashMap<String, UpdateInfo>();

        for(UpdateInfo update:config.getUpdates()){
            
            PluginInfo pluginInfo = update.getPluginInfo();
            String tag = update.getTag();

            Log.d(TAG,"tag=" + tag + "," + pluginInfo.versionName + ", update.getVersion()=" + update.getVersion());

            PluginInfo installedPlugin = getInstalledPluginInfo(tag);
            if(
            		installedPlugin != null && 1 != PluginUtil.verCompare(update.getVersion(),installedPlugin.versionName) &&
            				new File(installedPlugin.path).isFile()
            		
            		){
                Log.d(TAG,"continue,continue,continue");
            	continue;
            }

            boolean match = applyRules(update.getRules());
            Log.d(TAG, "match=" + match);
            
            //如果一个插件匹配上了多个更新包，则使用版本号最大的更新包
            if(match){
            	UpdateInfo uiInList = updateList.get(tag);
            	if(uiInList == null || PluginUtil.verCompare(uiInList.getVersion(),update.getVersion()) == -1){
            		updateList.put(update.getTag(), update);
            	}
            }
            
//            
//            if(match){
//                //如果更新包规则匹配通过，则判断 本地是否已经安装该更新，未安装或者安装的版本与更新包不匹配则将更新包添加到更新列表
//                PluginInfo ipi = installedPluginManager.getPlugin(tag);
//                if(ipi == null || !new File(ipi.path).isFile()
//                        || !ipi.version.equals(update.getVersion())){
//                    updateList.add(update);
//                }
//            }
               
        }
        
//        return updateList;
        
        return new ArrayList<UpdateInfo>(updateList.values());
        
    }
    
    public UpdateInfo checkUpdate(String tag){
        
        for(UpdateInfo update:config.getUpdates()){
            
            PluginInfo pluginInfo = update.getPluginInfo();
            
            if(pluginInfo.tag.equals(tag)){

                boolean match = applyRules(update.getRules());
                if(match){
                    PluginInfo ipi = getInstalledPluginInfo(tag);
                    if(ipi == null || !new File(ipi.path).isFile()
                            || !ipi.versionName.equals(pluginInfo.versionName)){
                        return update;
                    }
                }
            }
        }
        return null;
    }
    
    public void setGlobalUpdateListener(GlobalUpdateListener globalUpdateListener){
    	if(globalUpdateListener == null && this.globalUpdateListener != null){
    		this.globalUpdateListener.cancel();
    	}
    	this.globalUpdateListener = globalUpdateListener;
    }
//
//    private void install(UpdateInfo updateInfo) throws IOException{
//
//    }

    public void uninstall(String tag){

    }
    
    /**
     * 启动定时更新插件线程
     * @param filter 更新过滤器，通过该接口可动态的决定插件要不要更新
     * @param when 第一次启动延时，以毫秒为单位
     * @param period 定时间隔，以毫秒为单位
     */
    public void startUpdateTimer(final UpdateFilter filter,int when,int period){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
            	doUpdate(filter);
            }
        }, when, period);
    }
    
    public void setDefaultUpdateFilter(UpdateFilter defaultUpdateFilter) {
		this.defaultUpdateFilter = defaultUpdateFilter;
	}

    public void doUpdate(UpdateFilter filter){
    	 // TODO Auto-generated method stub
    	
		this.defaultUpdateFilter = filter;
    	try{
            if(onlyWifi && filter.onCheckUpdate()){
                update(filter,globalUpdateListener);
            }
    	}catch(Exception e){
    		Log.e(UpdateManager.TAG,e);
    	}
    }
    
    /**
     * 使用默认更新策略更新
     * 默认在wifi下才会更新插件
     * 并且在当前网络切换到wifi后还会检测一次更新
     */
    public void doUpdate(){
	   	doUpdate(defaultUpdateFilter);
    }
    
    private Map<String,UpdateInfo> downloadingMap = new HashMap<String, UpdateInfo>();

    //如果插件不在安装目录下，则删除，主要用于清理下载垃圾
    private void deleteApkIfNoInstalled(String path){
        File file = new File(path);
        if(!file.getParentFile().equals(new File(Config.getPluginDir()))){
            file.delete();
        }
    }

    public void installPlugin(UpdateInfo updateInfo){
        try {
            Log.d(UpdateManager.TAG, "installPlugin(), tag="+updateInfo.getPluginInfo().tag);
            Log.d(UpdateManager.TAG, "installPlugin(), path="+updateInfo.getPluginInfo().path);
            Log.d(UpdateManager.TAG, "installPlugin(), versionName="+updateInfo.getPluginInfo().versionName);
            Log.d(UpdateManager.TAG, "installPlugin(), md5="+updateInfo.getPluginInfo().md5);

            PluginInfo pluginInfo = new PluginInfo();
            RefUtil.cloneObject(updateInfo.getPluginInfo(), pluginInfo);

            pluginInfo.md5 = updateInfo.getMd5();
            pluginInfo.url = updateInfo.getUrl();
            pluginInfo.icon = updateInfo.getIcon();
            pluginInfo.versionName = updateInfo.getVersion();
            pluginInfo.loadOnAppStarted = updateInfo.isLoadOnAppStarted() ? 1 : 0;
            installManager.install(pluginInfo,false);
            Log.d(UpdateManager.TAG, "installPlugin(), install path="+pluginInfo.path);

//            install(updateInfo);
            deleteApkIfNoInstalled(updateInfo.getPluginInfo().path);

        } catch (Exception e) {
            Log.e(UpdateManager.TAG,e);
        }
    }

    private void updatePlugin(final UpdateInfo updateInfo){

        final PluginManager pluginManager = PluginManager.getInstance();
        final String tag = updateInfo.getTag();
        final String path = updateInfo.getPluginInfo().path;
        final String md5 = updateInfo.getMd5();
        final File file = new File(path);

        //设置文件临时保存的目录，便于延迟安装
        updateInfo.setTmpPath(path);

        if(!pluginManager.isInstalled(tag)){
            Log.d(UpdateManager.TAG, "update::download::onComplete,updatePlugin(),install(updateInfo),isInstalled=false tag="+updateInfo.getPluginInfo().tag);
            installPlugin(updateInfo);
        }else{

//            try{
//                PluginPackage pluginPackage = installManager.getInstalledPlugin(updateInfo.getTag());
//                if(pluginPackage == null){
//                    Log.e(UpdateManager.TAG,"update::download::onComplete,updatePlugin(),tag="+updateInfo.getTag()+",pluginPackage="+pluginPackage);
//                }else{
//                    Log.i(UpdateManager.TAG,"update::download::onComplete,updatePlugin(),tag="+updateInfo.getTag()+",delete "+pluginPackage.pi.path);
//                    new File(pluginPackage.pi.path).delete();
//                }
//            }catch(Exception e){
//                Log.e(UpdateManager.TAG, e);
//            }

            if(pluginManager.isPluginProcess()){

                if(!pluginManager.isLoaded(tag))
                {
                    Log.d(UpdateManager.TAG, "update::download::onComplete,updatePlugin(),isPluginProcess=true,isLoaded=false,install(updateInfo),tag="+updateInfo.getPluginInfo().tag);
                    installPlugin(updateInfo);

                }else{
                    //如果已经加载，则保存更新信息，下次启动时进行安装
                    String updateFilePath = Config.getPluginPendingInstallDir()+"/" + tag + ".update";
                    Log.d(UpdateManager.TAG, "update::download::onComplete,updatePlugin(),isPluginProcess=true,isLoaded=true,serialize,tag="+tag+",updateFilePath="+updateFilePath);
                    try {
                        IO.serialize(updateInfo,updateFilePath);
                    } catch (IOException e) {
                        Log.e(UpdateManager.TAG, e);
                    }
                }
            }else {

                PluginHelper.startPluginProcess(new PluginProcessListener() {
                    @Override
                    public void onConnected() {
                        if (!PluginHelper.isPluginLoaded(tag)) {

                            Log.d(UpdateManager.TAG, "update::download::onComplete,updatePlugin(),onConnected,isPluginProcess=false,isLoaded=false,install(updateInfo),tag="+updateInfo.getPluginInfo().tag);
                            installPlugin(updateInfo);

                        } else {
                            //如果已经加载，则保存更新信息，下次启动时进行安装
                            String updateFilePath = Config.getPluginPendingInstallDir()+"/" + tag + ".update";
                            Log.d(UpdateManager.TAG, "update::download::onComplete,updatePlugin(),onConnected,isPluginProcess=false,isLoaded=true,serialize,tag="+tag+",updateFilePath="+updateFilePath);
                            try {
                                IO.serialize(updateInfo,updateFilePath);
                            } catch (IOException e) {
                                Log.e(UpdateManager.TAG, e);
                            }
                        }
                    }

                    @Override
                    public void onReady() {

                    }

                    @Override
                    public void onDisconnected() {

                    }

                    @Override
                    public void onException(Exception e) {
                        Log.e(TAG, "updatePlugin(),onException(),serialize()..tag=" + tag ,e);
                        String updateFilePath = Config.getPluginPendingInstallDir()+"/" + tag + ".update";
                        Log.d(UpdateManager.TAG, "update::download::onComplete,updatePlugin(),onException,isPluginProcess=false,isLoaded=?,serialize,tag="+tag+",updateFilePath="+updateFilePath);
                        try {
                            IO.serialize(updateInfo,updateFilePath);
                        } catch (IOException e1) {
                            Log.e(UpdateManager.TAG, e1);
                        }
                    }
                });
            }

        }

//        //将新插件移动到插件目录
//        if(!file.renameTo(saveFile)){
//            //文件移动失败处理
//            Log.w(UpdateManager.TAG,"update::download::onComplete,tag="+updateInfo.getTag()+",renameTo failure");
//            Log.w(UpdateManager.TAG,"update::download::onComplete,tag="+updateInfo.getTag()+"workFile="+workFile+",saveFile="+saveFile.getAbsolutePath());
//            IO.copy(workFile, saveFile.getAbsolutePath());
//        }

//        updateInfo.getPluginInfo().path = saveFile.getAbsolutePath();
//        try {
//
//
//            Log.i(UpdateManager.TAG, "update::download::onComplete,install(updateInfo), tag="+updateInfo.getPluginInfo().tag);
//            //安装更新包
//            install(updateInfo);
//            IO.writeString(updateInfo.getPluginInfo().path + ".md5",md5);
//        } catch (IOException e) {
//            Log.e(UpdateManager.TAG, e);
//        }
//
//
//        updateListener.onComplete(updateInfo, saveFile, fileSize);
//
//        if(globalUpdateListener != null)
//            globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_SUCCESSED, updateInfo);


    }
      //  Pending installation

    
    public void update(final UpdateInfo updateInfo,final IPluginUpdateListener updateListener,final GlobalUpdateListener globalUpdateListener){

		Log.i(UpdateManager.TAG,"update::tag="+updateInfo.getTag());
		Log.i(UpdateManager.TAG,"update::url="+updateInfo.getUrl());
        Log.i(UpdateManager.TAG,"update::md5="+updateInfo.getMd5());
        Log.i(UpdateManager.TAG,"update::isInstallIfNot="+updateInfo.isInstallIfNot());

        boolean isInstalled = installManager.isInstalled(updateInfo.getPluginInfo().tag);
        Log.i(UpdateManager.TAG,"update::isInstalled="+isInstalled);

//        (isInstalled || (!isInstalled && updateInfo.isInstallIfNot()))

        if((isInstalled || updateInfo.isInstallIfNot()) && updateListener.onUpdate(isInstalled,updateInfo)){

        	boolean downloading = downloadingMap.containsKey(updateInfo.getUrl());
    		Log.i(UpdateManager.TAG,"update::begin,downloading="+downloading+",url="+updateInfo.getUrl());
        	if(!downloading){

	            final String fileName = URLUtil.guessFileName(updateInfo.getUrl(), "", "");
	            final String workFile = downloadWorkDir + "/" + fileName;
	            new File(downloadWorkDir).mkdirs();	    		
	    		Log.i(UpdateManager.TAG,"update::updateInfo.getTag()="+updateInfo.getTag());
	    		Log.i(UpdateManager.TAG,"update::fileName="+fileName);
                Log.i(UpdateManager.TAG,"update::workFile="+workFile);
                Log.i(UpdateManager.TAG,"update::updateInfo.getUrl()="+updateInfo.getUrl());

	            getHttpSession().download(updateInfo.getUrl(), workFile, new BaseDownLoadLisenter(){
	                @Override
	                public void onComplete(File file, long fileSize) {

	    	    		Log.i(UpdateManager.TAG,"update::download::onComplete,tag="+updateInfo.getTag());
	                	new File(pluginDir).mkdirs();
//	                    File saveFile = new File(pluginDir + "/" + fileName);

	    	    		Log.i(UpdateManager.TAG,"update::file="+file.getAbsolutePath());
	                    byte[] bytes = null;
						try {
							bytes = IO.readBytes(file);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							Log.e(UpdateManager.TAG, e1);
							return;
						}
	                    
						if(bytes == null){
							Log.e(UpdateManager.TAG, "update::download::onComplete,error,file bytes=null");
							return;
						}
						
						
	                    String md5 = MD5Util.md5str(bytes);

	                	downloadingMap.remove(updateInfo.getUrl());

	    	    		Log.i(UpdateManager.TAG,"update::download::onComplete,tag="+updateInfo.getTag()+",file md5="+md5+",update md5="+updateInfo.getMd5());
	                    if(md5 != null && md5.equals(updateInfo.getMd5())){

                            updateInfo.getPluginInfo().path = file.getAbsolutePath();
                            updateInfo.getPluginInfo().md5 = md5;
                            updatePlugin(updateInfo);

                            updateListener.onComplete(updateInfo, file, fileSize);

                            if(globalUpdateListener != null)
                                globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_SUCCESSED, updateInfo);
	                    }else{
                    		Log.e(UpdateManager.TAG, "update::download::onComplete,tag="+updateInfo.getTag()+",MD5 authentication fails");
	                    	file.delete();
	                    	onThrowException(new RuntimeException("MD5 authentication fails"));
		                    if(globalUpdateListener != null)
		                    	globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_ERROR, updateInfo);
	                    }
	                }
	                
	                @Override
	                public void onDownloading(File file, long pos, int size, long fileTotalSize) {
	                    // TODO Auto-generated method stub
	    	    		Log.d(UpdateManager.TAG,"onDownloading..........file="+file.getAbsolutePath()+",pos="+pos);
	                    updateListener.onDownloading(updateInfo, file, pos, size, fileTotalSize);
	                }
	                
	                @Override
	                public void onThrowException(Exception e) {
	                    // TODO Auto-generated method stub
	    	    		Log.d(UpdateManager.TAG,"onThrowException.........."+e);
	                	downloadingMap.remove(updateInfo.getUrl());
	                    updateListener.onThrowException(updateInfo,e);
	                    if(globalUpdateListener != null)
	                    	globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_EXCEPTION, updateInfo);
	                }
	                
	                @Override
	                protected void onTimeout(ConnectTimeoutException e) {
	    	    		Log.d(UpdateManager.TAG,"onTimeout.........."+e.getMessage());
	                    // TODO Auto-generated method stub
	                	downloadingMap.remove(updateInfo.getUrl());
	                    updateListener.onTimeout(updateInfo, e);
	                    if(globalUpdateListener != null)
	                    	globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_EXCEPTION, updateInfo);
	                }
	                
	                @Override
	                public void onStart(File file, long fileSize) {
	    	    		Log.d(UpdateManager.TAG,"onStart.........."+fileSize + ",file="+file.getAbsolutePath());
	                    // TODO Auto-generated method stub
	                	downloadingMap.put(updateInfo.getUrl(), updateInfo);
	                    updateListener.onStart(updateInfo, file, fileSize);
	                }
	            });
        	
        	}
        }      
       
        
    }
    
    public void setDefaultXmlPath(String defaultXmlPath) {
		this.defaultXmlPath = defaultXmlPath;
	}
    
    public UpdateFilter getDefaultUpdateFilter() {
		return defaultUpdateFilter;
	}
    
    public String getDefaultXmlPath() {
		return defaultXmlPath;
	}
    
    public void update(final IPluginUpdateListener updateListener,GlobalUpdateListener globalUpdateListener){

		Log.i(UpdateManager.TAG, "update()::defaultXmlPath="+defaultXmlPath);
    	if(config == null)
    		load(defaultXmlPath);

		Log.i(UpdateManager.TAG, "update()::config="+config);
		
    	if(config == null){
    		Log.e(UpdateManager.TAG, "update::update.xml load fail,path="+defaultXmlPath);
        	globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_SUCCESSED, null);
    		return;
    	}
    	
        //检测更新
        List<UpdateInfo> updateList = checkUpdate();
		Log.i(UpdateManager.TAG, "update()::updateList="+updateList);
		Log.i(UpdateManager.TAG, "update()::updateList.size()="+updateList.size());
        
        //如果有可更新的插件则将其插入下载队列
        if(updateList.size() > 0){
            
            if(updateListener.onUpdateList(updateList)){
            	if(globalUpdateListener != null)
            		globalUpdateListener.onBeginUpdate(updateList);
                for(final UpdateInfo updateInfo : updateList){
                	try{
                		Log.i(UpdateManager.TAG, "update()::begin update "+updateInfo.getTag());
                		update(updateInfo,updateListener,globalUpdateListener);
                	}catch(Exception e){
                		Log.e(UpdateManager.TAG, e);
                	}
                }
            }
        }else{
        	globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_SUCCESSED, null);
        }
        
//        if(globalUpdateListener!=null)
//        	globalUpdateListener.onUpdateFinish(UpdateStatus.UPDATE_STATUS_SUCCESSED, null);
        
    }
    
    //解析插件信息部分
    private PluginInfo parsePluginInfo(XmlPullParser parser) throws XmlPullParserException, IOException{
        
        PluginInfo pluginInfo = new PluginInfo();  
        pluginInfo.tag = (parser.getAttributeValue(null, "tag"));
        
        int eventType = parser.next();  
        
        boolean isEndTag = false;
        while(!isEndTag){
            switch (eventType) {  
                case XmlPullParser.START_TAG: 
                    String tagName = parser.getName();
                    eventType = parser.next();  
                    if (tagName.equals("packageName")) {  
                        pluginInfo.packageName = (parser.getText());  
                    } else if (tagName.equals("name")) {  
                        pluginInfo.name = parser.getText();  
                    } else if (tagName.equals("tag")) {  
                        pluginInfo.tag = (parser.getText());
                    } else if (tagName.equals("desc")) {  
                        pluginInfo.desc = (parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:  
                    if (parser.getName().equals("plugin")) {  
                        return pluginInfo;
                    } 
            }
            eventType = parser.nextTag();
        }
        return null;
    }
    

    //解析插件更新实体的规则列表
    private List<UpdateRule> parseRules(XmlPullParser parser) throws XmlPullParserException, IOException{

        List<UpdateRule> rules = new ArrayList<UpdateRule>();
        UpdateRule rule = null;
        int eventType = parser.next();  
        
        boolean isRulesEndTag = false;
        while(!isRulesEndTag){
            switch (eventType) {  
                case XmlPullParser.START_TAG: 
                    if (parser.getName().equals("rule")) {  
                        rule = new UpdateRule();
                        rule.setType(parser.getAttributeValue(null, "type"));
                        rule.setMinVer(parser.getAttributeValue(null, "minVer"));
                        rule.setMaxVer(parser.getAttributeValue(null, "maxVer"));
                        rule.setVers(parser.getAttributeValue(null, "vers"));
                        rule.setIgnoreVers(parser.getAttributeValue(null, "ignoreVers"));
                        eventType = parser.next();  
                    }
                    break;
                case XmlPullParser.END_TAG:  
                    if (parser.getName().equals("rule")) {  
                        rules.add(rule);  
                        rule = null;      
                    }else if (parser.getName().equals("rules")) {  
                        isRulesEndTag = true;
                        return rules;
                    }  
                default:
                    eventType = parser.next();  
                    break;
            }
        }
        return null;
    }
    

    //解析更新实体信息
    private UpdateInfo parseUpdateInfo(XmlPullParser parser) throws XmlPullParserException, IOException{
        
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.setInstallIfNot(true);
        updateInfo.setTag(parser.getAttributeValue(null, "tag"));
        updateInfo.setVersion(parser.getAttributeValue(null, "version"));
        
        int eventType = parser.nextTag();  
        
        boolean isEndTag = false;
        while(!isEndTag){
            switch (eventType) {  
                case XmlPullParser.START_TAG: 
                    String tagName = parser.getName();
                    eventType = parser.next(); 
                    if (tagName.equals("icon")) {  
                        updateInfo.setIcon(parser.getText());
                    } else if (tagName.equals("force")) {  
                        updateInfo.setForceUpdate(Boolean.parseBoolean(parser.getText()));  
                    } else if (tagName.equals("md5")) {  
                        updateInfo.setMd5(parser.getText());
                    } else if (tagName.equals("desc")) {  
                        updateInfo.setDesc(parser.getText());
                    } else if (tagName.equals("url")) {
                        updateInfo.setUrl(parser.getText());
                    } else if (tagName.equals("loadOnAppStarted")) {
                        boolean loadOnAppStarted = false;
                        String value = parser.getText();
                        if(value != null) {
                            try {
                                loadOnAppStarted = Boolean.valueOf(value);
                            }catch(Throwable thr){

                            }
                        }
                        updateInfo.setLoadOnAppStarted(loadOnAppStarted);
                    } else if (tagName.equals("installIfNot")) {
                        //默认 未安装就进行安装
                        boolean installIfNot = true;
                        String value = parser.getText();
                        if(value != null) {
                            try {
                                installIfNot = Boolean.valueOf(value);
                            }catch(Throwable thr){

                            }
                        }
                        updateInfo.setInstallIfNot(installIfNot);
                    }else if (tagName.equals("rules")) {
                        updateInfo.setRules(parseRules(parser));
                    }
                    break;
                case XmlPullParser.END_TAG:  
                    if (parser.getName().equals("update")) {  
                        return updateInfo;  
                    }
            }
            
            eventType = parser.nextTag();
            
        }
        
        return null;
    }
    
    
    //解析插件更新配置文件,获取配置对象
    private PluginUpdateConfig parse(InputStream in) throws XmlPullParserException, IOException {  

        List<PluginInfo> pluginInfos = null;  
        List<UpdateInfo> updateInfos = null;  
        PluginUpdateConfig config = new PluginUpdateConfig();
        
        XmlPullParser parser = Xml.newPullParser(); 
        parser.setInput(in, "UTF-8");               
  
        int eventType = parser.getEventType();  
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
                pluginInfos = new ArrayList<PluginInfo>();  
                updateInfos = new ArrayList<UpdateInfo>();  
                break;  
            case XmlPullParser.START_TAG: 
                if (parser.getName().equals("plugin")) {  
                    pluginInfos.add(parsePluginInfo(parser));
                    
                } else if (parser.getName().equals("update")) {  
                    updateInfos.add(parseUpdateInfo(parser));
                }
                break;  
            case XmlPullParser.END_TAG:  
                if (parser.getName().equals("plugins")) {  
                    eventType = parser.next();
                    continue;
                }  
                break;  
            }  
            eventType = parser.nextTag();  
        }  
        
        
        config.setPlugins(pluginInfos);
        config.setUpdates(updateInfos);
        
        Map<String,PluginInfo> tmpMap = new HashMap<String,PluginInfo>();
        
        for(PluginInfo pi:pluginInfos){
            tmpMap.put(pi.tag, pi);
        }

        List<UpdateInfo> exceptionUI = new ArrayList<UpdateInfo>();
        if(updateInfos != null){
	        for(UpdateInfo ui:updateInfos){
	        	PluginInfo pi = tmpMap.get(ui.getTag());
	        	if(pi != null)
	        		ui.setPluginInfo(pi);
	        	else
	        		exceptionUI.add(ui);
	        }
	        updateInfos.removeAll(exceptionUI);
        }
        
        
        
        return config;
        
    }

	@Override
	public void onNetworkChanged(int type) {
		// TODO Auto-generated method stub
//		if(type == ConnectivityManager.TYPE_WIFI)
//			doUpdate();
	}  
    
    
    
    
    
}
