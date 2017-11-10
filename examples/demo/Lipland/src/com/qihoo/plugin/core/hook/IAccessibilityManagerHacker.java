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

package com.qihoo.plugin.core.hook;

import android.app.INotificationManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.IAccessibilityManager;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.core.Log;

import java.lang.reflect.Method;

/**
 * Created by xupengpai on 2017/7/19.
 */

public class IAccessibilityManagerHacker extends ServiceInterfaceProxy{

    private static final String TAG = "IAccessibilityManagerHacker";
    private static final String SERVICE_NAME = "accessibility";
    private static IAccessibilityManagerHacker instance;

    protected IAccessibilityManagerHacker() {
        super(SERVICE_NAME, IAccessibilityManager.class);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void invokeHandle() {
        // TODO Auto-generated method stub

        //此处主要解决小米手机上Toast无法弹出的问题
        getDProxy().setHookHandlerByName("sendAccessibilityEvent", new ProxyHandler.HookHandler() {
            @Override
            public boolean onBefore(Object origin, Method method, Object[] args) {
                // TODO Auto-generated method stub
                try {
                    AccessibilityEvent event = (AccessibilityEvent)args[0];
                    if (event != null) {
                        event.setPackageName(HostGlobal.getPackageName());
                    }
                }catch (Throwable thr){
                    Log.e(TAG,thr);
                }
                return super.onBefore(origin, method, args);
            }
        });

    }

    public static void hook(){
        if(instance == null){
            instance = new IAccessibilityManagerHacker();
        }
        instance.doHook();
    }
}
