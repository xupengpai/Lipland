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

package com.qihoo.plugin;

import com.qihoo.plugin.bean.Plugin;

/**
 * renjihai 2015年3月12日
 */
public class DefaultPluginLoadListenner implements IPluginLoadListener {
    private boolean forResult;
    private int requestCode;

    private boolean isManual;

    @Override
    public void onStart(String tag) {

    }

    @Override
    public void onComplete(String tag, Plugin plugin) {

    }

    @Override
    public void onLoading(String tag, int pos) {

    }

    @Override
    public void onError(String tag, int code) {

    }

    @Override
    public void onThrowException(String tag, Throwable thr) {

    }

    public boolean isForResult() {
        return forResult;
    }

    public void setForResult(boolean forResult) {
        this.forResult = forResult;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean isManual) {
        this.isManual = isManual;
    }

}
