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

package com.qihoo.common.ormapping;

/**
 * Created by renjihai on 2015/3/2.
 */
public abstract class ApkInfoDDL extends BaseDDL {

    /**
     * 
     */
    private static final long serialVersionUID = 3867525996557351432L;

    @DDLColumn(notNull = true)
    public String pkgName;
    
    @DDLColumn
    public String tag;

    @DDLColumn
    public String option1;
    @DDLColumn
    public String option2;
    @DDLColumn
    public String option3;
    @DDLColumn
    public String option4;
    @DDLColumn
    public String option5;

    public abstract void process(Object superObject);
    
}
