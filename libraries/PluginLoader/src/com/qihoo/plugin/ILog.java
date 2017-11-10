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


public interface ILog {

    public int v(String tag, String msg);

    public int v(String tag, String msg, Throwable tr);

    public int d(String tag, String msg);
    
    public int d(String msg);

    public int d(String tag, String msg, Throwable tr);

    public int i(String tag, String msg);
    
    public int i(String msg);

    public int i(String tag, String msg, Throwable tr);


    public int w(String tag, String msg);

    public int w(String tag, String msg, Throwable tr);

    public int w(String tag, Throwable tr);

    public int e(String tag, String msg);
    
    public int e(Throwable e);

    public int e(String tag, String msg, Throwable tr);
    
    public int e(String msg, Throwable tr);

    public int wtf(String tag, String msg);

    public int wtfStack(String tag, String msg);

    public int wtf(String tag, Throwable tr);

    public int wtf(String tag, String msg, Throwable tr);

    int wtf(int logId, String tag, String msg, Throwable tr, boolean localStack);
    
}
