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


import android.content.Context;
import android.content.IClipboard;

import com.qihoo.plugin.base.HostGlobal;
import com.qihoo.plugin.core.Log;

import java.lang.reflect.Method;

/**
 * Created by xupengpai on 2017/5/18.
 */

public class ITelephonyHacker extends ServiceInterfaceProxy {

    private static final String SERVICE_NAME = Context.TELEPHONY_SERVICE;
    private static final String TAG = "ITelephonyHacker";

    private static ITelephonyHacker instance;

    protected ITelephonyHacker() {

        super(SERVICE_NAME, IClipboard.class);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void invokeHandle() {
        // TODO Auto-generated method stub
        getDProxy().setHookHandlerByName("getAllCellInfo", new ProxyHandler.HookHandler() {
            @Override
            public boolean onBefore(Object origin, Method method, Object[] args) {
                // TODO Auto-generated method stub
                if(args != null && args.length != 0){
                    String packageName = (String)args[0];
                    Log.d(TAG,"getAllCellInfo(),packageName="+packageName);
                }
                return super.onBefore(origin, method, args);
            }
        });
    }

    public static void hook(){
        try {
            if(instance == null){
                instance = new ITelephonyHacker();
            }
            instance.doHook();
        }catch(Throwable thr){
            Log.e(TAG, thr);
        }
    }
}
