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


import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

import com.qihoo.plugin.base.HostGlobal;

public class NetworkManager {
	

	public interface INetworkChange {
	    public void onNetworkChanged(int type);
	}
	
    private static volatile NetworkManager sInstance;
    private Context appContext;
    
	// using event bus is more flexible
    private ArrayList<INetworkChange> listeners;
    private final static int TYPE_DEFAULT = -2;
    public final static int TYPE_MOBILE = 0;
    public final static int TYPE_WIFI = 1;
    //such as bluetooth
    public final static int TYPE_OTHERS = 2;
    public final static int TYPE_NONE = -1;
    //ConnectivityManager constants
    private int type = TYPE_DEFAULT;
    // TelephonyManager constants
    private int subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

    public static final String MOBILE_2G = "mobile_2g";
    public static final String MOBILE_3G = "mobile_3g";
    public static final String MOBILE_4G = "mobile_3g";
    public static final String MOBILE_UNKNOWN = "mobile_unknown";
    
    // 字符串型网络状态
    /**
     * 手机网络,没有区分2/3G
     */
    public static final String NETWORK_MOBILE = "NETWORK_MOBILE";
    /**
     * wifi网络
     */
    public static final String NETWORK_WIFI = "NETWORK_WIFI";
    /**
     * 网络无连接
     */
    public static final String NETWORK_DISABLE = "NETWORK_DISABLE";
    /**
     * 未知网络
     */
    public static final String NETWORK_UNKNOWN = "NETWORK_UNKNOWN";

    public void addNetworkChangeListener(INetworkChange listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeNightModeListener(INetworkChange listener) {
        listeners.remove(listener);
    }

    private void notifyNetworkChanged(int type) {
        if (listeners == null) {
            return;
        }

        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onNetworkChanged(type);
            } catch (Exception e) {
                listeners.remove(i);
            }
        }
    }

    
    private NetworkManager(Context ctx) {
    	
    	//还未监听到一次
    	type = -2;
    	
    	appContext = ctx.getApplicationContext();

    	listeners = new ArrayList<INetworkChange>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        AppGlobal.getBaseApplication().registerReceiver(mNetworkStateReceiver, filter);
        appContext.registerReceiver(mNetworkStateReceiver, filter);
    }

    public static final NetworkManager getInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (NetworkManager.class) {
                if (sInstance == null) {
                    sInstance = new NetworkManager(ctx);
                }
            }
        }

        return sInstance;
    }

    /**
     * @param type ConnectivityManager.TYPE_MOBILE or
     *             ConnectivityManager.TYPE_WIFI or ConnectivityManager.TYPE_NONE
     */
    private void onNetworkChanged(int type) {
        switch (type) {
            case TYPE_MOBILE:
                notifyNetworkChanged(TYPE_MOBILE);
                break;
            case TYPE_WIFI:
                notifyNetworkChanged(TYPE_WIFI);
                break;
            case TYPE_NONE:
                notifyNetworkChanged(TYPE_NONE);
                break;
            default:
                notifyNetworkChanged(TYPE_OTHERS);
        }
    }
    
    public int getType(){
    	if(type == -2 || type == -1){
    		ConnectivityManager cm = (ConnectivityManager) HostGlobal.getBaseApplication()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
                type = -1;
            else
            	return  activeNetworkInfo.getType();
    	}
    	return type;
    }

    private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                type = -1;
                subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                onNetworkChanged(TYPE_NONE);
            }else {
                type = activeNetworkInfo.getType();
                subType = activeNetworkInfo.getSubtype();
                switch (type) {
                    case ConnectivityManager.TYPE_WIFI:
                        subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                        onNetworkChanged(TYPE_WIFI);
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        subType = activeNetworkInfo.getSubtype();
                        onNetworkChanged(TYPE_MOBILE);
                        break;
                    default:
                        subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                        onNetworkChanged(TYPE_OTHERS);
                }
            }
        }
    };

    public boolean isMobileNetwork() {
        return type == ConnectivityManager.TYPE_MOBILE;
    }

    public boolean isMobileNetwork2G() {
        return subType == TelephonyManager.NETWORK_TYPE_CDMA
                        || subType == TelephonyManager.NETWORK_TYPE_EDGE
                        || subType == TelephonyManager.NETWORK_TYPE_GPRS ;
    }

    public String getMobileType() {
        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return MOBILE_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return MOBILE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case 17: // td-scdma
                return MOBILE_4G;
            default:
                return MOBILE_UNKNOWN;
        }
    }
    /**
     * 返回字符串型网络状态
     * @Deprecated
     * @return
     */
    public String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) appContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            type = -1;
            subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }else {
            type = activeNetworkInfo.getType();
            subType = activeNetworkInfo.getSubtype();
            switch (type) {
                case ConnectivityManager.TYPE_WIFI:
                    subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    subType = activeNetworkInfo.getSubtype();
                    break;
                default:
                    subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
            }
        }
        if (isMobileNetwork()) {
            return NETWORK_MOBILE;
        } else if (isWifiNetwork()) {
            return NETWORK_WIFI;
        } else if (isNoNetwork()) {
            return NETWORK_DISABLE;
        }
        return NETWORK_UNKNOWN;
    }
    public boolean isWifiNetwork() {
        return type == ConnectivityManager.TYPE_WIFI;
    }

    public boolean isNoNetwork() {
        return type == TYPE_NONE;
    }

    public boolean HasNetWork() {
        return type != TYPE_NONE;
    }
}
