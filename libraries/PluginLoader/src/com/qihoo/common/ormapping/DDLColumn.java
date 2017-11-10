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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by renjihai on 2015/3/2.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD })
public @interface DDLColumn {

    /**
     * 字段名
     * 
     * @return
     */
    public String name() default "";

    /**
     * 是否为主键
     * 
     * @return
     */
    public boolean isPrimary() default false;

    /**
     * 字段类型 text integer ...
     * 
     * @return
     */
    public String type() default "text";

    /**
     * 是否自增长
     * 
     * @return
     */
    public boolean isAutoIncrement() default false;

    /**
     * 是否允许为空
     * 
     * @return
     */
    public boolean notNull() default false;

    /**
     * 是否唯一
     * 
     * @return
     */
    public boolean isUnique() default false;

}
