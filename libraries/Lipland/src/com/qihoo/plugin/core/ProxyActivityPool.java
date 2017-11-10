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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.base.ActivityStub;
import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.bean.ActivityStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xupengpai on 2017/6/8.
 */

public class ProxyActivityPool {

    public final static String TAG = "ProxyActivityPool";

    private Map<Integer,List<ActivityStatus>> activities;

    public void add(Integer type,ActivityStatus status){
        List<ActivityStatus> list = null;
        if(!activities.containsKey(type)){
            list = new ArrayList();
            activities.put(type,list);
        }else{
            list = activities.get(type);
        }
        list.add(status);
    }

    public ActivityStatus getIdleActivity(Integer type){
        Log.d(TAG,"getIdleActivity()...type="+type);
        List<ActivityStatus> list = activities.get(type);
        if(list != null){
            for(ActivityStatus status : list){
                if(status.isIdle) {
                    Log.d(TAG,"getIdleActivity()...return className="+status.className);
                    return status;
                }
            }
        }
        return null;
    }

    public void setIdel(String className,boolean idel){
        Iterator<List<ActivityStatus>> iter = activities.values().iterator();
        while(iter.hasNext()){
            List<ActivityStatus> list = iter.next();
            for(ActivityStatus status : list){
                if(status.className.equals(className)){
                    status.isIdle = idel;
                    break;
                }
            }
        }
    }

    public void reset(){
        Log.d(TAG,"reset()...");
        if(activities != null) {
            Iterator<List<ActivityStatus>> iter = activities.values().iterator();
            while (iter.hasNext()) {
                List<ActivityStatus> list = iter.next();
                for (ActivityStatus status : list) {
                    status.isIdle = true;
                }
            }
        }
    }

    //注册监听
    private void registerMonitor(Context context){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACTION_ACTIVITY_STUB_STATUS);
        filter.addAction(Actions.ACTION_ACTIVITY_STUB_RESET);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                Log.d(TAG,"onReceive()...action=" + action);
                if(action.equals(Actions.ACTION_ACTIVITY_STUB_STATUS)) {
                    String className = intent.getStringExtra(Actions.DATA_CLASS_NAME);
                    boolean idel = intent.getBooleanExtra(Actions.DATA_IDLE, false);
                    Log.d(TAG,"onReceive()...className=" + className);
                    Log.d(TAG,"onReceive()...idel=" + idel);
                    if (className != null) {
                        setIdel(className, idel);
                    }
                }else if (action.equals(Actions.ACTION_ACTIVITY_STUB_RESET)){
                    reset();
                }
            }
        },filter);
    }

    public void init(Context context){

        Log.d(TAG,"init()...");

        activities = new HashMap<>();

        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(),PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG,"",e);
            return;
        }

        Log.d(TAG,"init()...pi="+pi);
        //从ProxyActivity的子类中寻找代理类
        Class[] proxyActivities = ProxyActivity.class.getDeclaredClasses();
        if(proxyActivities != null){
            for(Class cls : proxyActivities){
                ActivityStub stub = (ActivityStub) cls.getAnnotation(ActivityStub.class);
                if(stub != null){

                    if(pi.activities != null) {
                        //只有注册了的坑才有效
                        Log.d(TAG,"init()...activities.length="+pi.activities.length);
                        for (ActivityInfo info : pi.activities) {
//                            Log.d(TAG,"init()...activity="+info.name);
                            if (info.name.equals(cls.getName())) {
                                ActivityStatus status = new ActivityStatus();
                                status.className = cls.getName();
                                status.isIdle = true;
                                Log.d(TAG,"add()..." + stub.launchMode() + "," + status.className);
                                add(stub.launchMode(), status);
                                break;
                            }
                        }
                    }
                }
            }
        }

        ActivityStatus status = new ActivityStatus();
        status.className = ProxyActivity.class.getName();
        status.isIdle = true;
        add(ActivityInfo.LAUNCH_MULTIPLE,status);

        registerMonitor(context);


    }

    public static void notifyIdle(Context context,String className,boolean idle){
        Intent activityIdelIntent = new Intent();
        activityIdelIntent.setPackage(HostGlobal.getPackageName());
        activityIdelIntent.setAction(Actions.ACTION_ACTIVITY_STUB_STATUS);
        activityIdelIntent.putExtra(Actions.DATA_CLASS_NAME,className);
        activityIdelIntent.putExtra(Actions.DATA_IDLE,idle);
        context.sendBroadcast(activityIdelIntent);
    }

    public static void notifyReset(Context context){
        Intent activityIdelIntent = new Intent(Actions.ACTION_ACTIVITY_STUB_RESET);
        activityIdelIntent.setPackage(HostGlobal.getPackageName());
        context.sendBroadcast(activityIdelIntent);
    }

}
