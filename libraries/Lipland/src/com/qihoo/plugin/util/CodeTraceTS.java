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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xupengpai on 2017/5/16.
 */

public class CodeTraceTS {

    private static Map<Long,CodeTrace> cts = new HashMap<>();

    private static Map<String,CodeTrace> strcts = new HashMap<>();


    public static CodeTrace begin(){
        long tid = Thread.currentThread().getId();
        CodeTrace ct = cts.get(tid);
        if(ct == null){
            ct = new CodeTrace();
            cts.put(tid,ct);
        }
        ct.begin();
        return ct;
    }

    public static CodeTrace end(){
        long tid = Thread.currentThread().getId();
        CodeTrace ct = cts.get(tid);
        if(ct != null){
            ct.end();







        }
        return ct;
    }

    public static CodeTrace begin(String id){
        CodeTrace ct = strcts.get(id);
        if(ct == null){
            ct = new CodeTrace();
            strcts.put(id,ct);
        }
        ct.begin();
        return ct;
    }

    public static CodeTrace end(String id){
        CodeTrace ct = strcts.get(id);
        if(ct != null){
            ct.end();
            strcts.remove(id);
        }
        return ct;
    }
}
