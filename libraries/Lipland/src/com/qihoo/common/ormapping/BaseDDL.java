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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * Created by renjihai on 2015/3/2.
 */
public abstract class BaseDDL implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3867525996557351432L;


    /**
     * 插入或者更新
     * 
     * @param db
     * @return
     */
    public final long inseartOrUpdate(SQLiteDatabase db) {
        return -1L;
    }

    /**
     * 创建表语句
     * 
     * @return
     */
    public final String CREATE() {
        Class<? extends BaseDDL> class1 = getClass();
        String sqlString = getSqlByClassFields(class1);
        String sqlByDDLColumns = getSqlByDDLColumns(getExtraColumns());
        if (TextUtils.isEmpty(sqlString)) {
            sqlString += sqlByDDLColumns;
        } else if (!TextUtils.isEmpty(sqlByDDLColumns)) {
            sqlString += "," + getSqlByDDLColumns(getExtraColumns());
        }
        if (!class1.getName().equals(BaseDDL.class.getName())) {
            String string = getSqlByClassFields(BaseDDL.class);
            if (TextUtils.isEmpty(sqlString)) {
                sqlString += string;
            } else if (!TextUtils.isEmpty(string)) {
                sqlString += "," + string;
            }
        }

        String tabName = getTabName();
        return "create table if not exists " + tabName + "( " + sqlString + ")";
    }

    /**
     * 删除表语句
     * 
     * @return
     */
    public final String DROP() {
        String tableName = getTabName();
        return "drop table if exists " + tableName;
    }

    /**
     * 获取声明为数据库字段的所有属性,拼成建表sql语句
     * 
     * @return
     */
    private String getSqlByClassFields(Class<?> clazz) {
        List<Field> ddlColumnFields = DDLUtils.getDDLColumnFields(clazz);
        if (ddlColumnFields == null || ddlColumnFields.size() == 0) {
            return "";
        }
        String sql = "";
        int length = ddlColumnFields.size();
        for (int i = 0; i < length; i++) {
            Field field = ddlColumnFields.get(i);
            sql += getSqlByField(field) + (i == length - 1 ? " " : " ,");
        }
        return sql;
    }

    /**
     * 获取声明为数据库字段的所有属性,拼成建表sql语句
     * 
     * @return
     */
    private String getSqlByDDLColumns(List<DDLColumn> ddlColumns) {
        if (ddlColumns == null) {
            return "";
        }
        String sql = "";
        int length = ddlColumns.size();
        for (int i = 0; i < length; i++) {
            DDLColumn ddlColumn = ddlColumns.get(i);
            sql += (getSqlByDDLColumn(ddlColumn) + (i == length - 1 ? " " : ", "));
        }
        return sql;
    }

    /**
     * 获取单个属性,拼成建表字段
     * 
     * @param field
     * @return
     */
    private String getSqlByField(Field field) {
        DDLColumn ddlColumn = field.getAnnotation(DDLColumn.class);
        if (ddlColumn == null) {
            return null;
        }
        String name = ddlColumn.name();
        if (TextUtils.isEmpty(name)) {
            name = field.getName();
        }
        String type = ddlColumn.type();
        boolean primary = ddlColumn.isPrimary();
        boolean autoIncrement = ddlColumn.isAutoIncrement();
        boolean unique = ddlColumn.isUnique();
        boolean notNull = ddlColumn.notNull();
        
        if (field.getName().contains("id")) {
            System.err.println();
        }
        
        // 自动匹配类型
        if (TextUtils.isEmpty(type) || "text".equals(type)) {
            if (field.getType() == int.class || field.getType() == Integer.class) {
                type = "integer";
            } else if (field.getType() == Long.class || field.getType() == long.class) {
                type = "long";
            } else {
                type = "text";
            }
        }
        
        String result = name + " " + type;

        if (primary) {
            result += " PRIMARY KEY";
        }

        if (autoIncrement) {
            result += " AUTOINCREMENT";

        }

        if (unique) {
            result += " UNIQUE";
        }

        if (notNull) {
            result += " NOT NULL";
        }

        return result;
    }


    /**
     * 获取表名
     * 
     * @return
     */
    public final String getTabName() {
        Class<?> clazz = getClass();
        DDLTable ddlTable = clazz.getAnnotation(DDLTable.class);
        if (ddlTable != null && !TextUtils.isEmpty(ddlTable.name())) {
            return ddlTable.name();
        }
        return clazz.getSimpleName();
    }

    /**
     * 获取额外的数据库字段声明,拼成建表语句
     * 
     * @param ddlColumn
     * @return
     */
    private String getSqlByDDLColumn(DDLColumn ddlColumn) {
        if (ddlColumn == null) {
            return null;
        }
        String name = ddlColumn.name();
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        String type = ddlColumn.type();
        return name + " " + type;
    }

    /**
     * 如果建表时需要额外声明字段,则重写该方法
     * 
     * @return
     */
    protected List<DDLColumn> getExtraColumns() {
        return null;
    }

    /**
     * 该方法在映射之后被调用,可以在该方法内处理一些业务逻辑
     * 
     * @param superObject
     */
    public abstract void process(Object superObject);
}
