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

import com.qihoo.plugin.bean.InstallTimeInfo;
import com.qihoo.plugin.bean.LoadTimeInfo;
import com.qihoo.plugin.bean.StartTimeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xupengpai on 2017/5/16.
 */

public class TimeStatistics {

    private static Map<String,LoadTimeInfo> loadTimeInfoMap = new HashMap<>();
    private static Map<String,InstallTimeInfo> installTimeInfoMap = new HashMap<>();
    private static Map<String,StartTimeInfo> startTimeInfoMap = new HashMap<>();

    public static void putLoadTime(String tag,LoadTimeInfo time){
        loadTimeInfoMap.put(tag,time);
    }

    public static void putInstallTime(String tag,InstallTimeInfo time){
        installTimeInfoMap.put(tag,time);
    }

    public static void updateStartTime(String tag,StartTimeInfo time){
        StartTimeInfo ct = getOrNewStartTimeInfo(tag);
        ct.activity_name = time.activity_name;
        ct.activity_onCreate_host = time.activity_onCreate_host;
        ct.activity_onCreate = time.activity_onCreate;
        ct.activity_onCreate_total = time.activity_onCreate_total;
        ct.activity_newActivity = time.activity_newActivity;
    }

    public static StartTimeInfo getOrNewStartTimeInfo(String tag){
        StartTimeInfo st;
        if(!startTimeInfoMap.containsKey(tag)){
            st = new StartTimeInfo();
            startTimeInfoMap.put(tag,st);
        }else{
            st = startTimeInfoMap.get(tag);
        }
        return st;
    }

    public static LoadTimeInfo getLoadTimeInfo(String tag){
        return loadTimeInfoMap.get(tag);
    }

}
