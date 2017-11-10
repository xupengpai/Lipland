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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;


/**
 * Created by renjihai on 2015/3/3.
 */
public class DDLUtils {
	
	private final static String TAG = DDLUtils.class.getSimpleName();

    public static String getColumnNameByFieldName(Field field) {
        DDLColumn ddlColumn = null;
        if (field == null || (ddlColumn = field.getAnnotation(DDLColumn.class)) == null) {
            return null;
        }
        String columnName = null;
        if (TextUtils.isEmpty((columnName = ddlColumn.name()))) {
            columnName = field.getName();
        }
        return columnName;
    }

    /**
     * 查找某个类中的字段对应于数据库中的列名
     * 
     * @param clazz
     * @param fieldName
     * @return
     */
    public static String getColumnNameByFieldName(Class<? extends BaseDDL> clazz, String fieldName) {
        DDLColumn ddlColumn = null;
        Field field = getIterateField(clazz, fieldName);
        if (field == null || (ddlColumn = field.getAnnotation(DDLColumn.class)) == null) {
            return null;
        }
        String columnName = null;
        if (TextUtils.isEmpty((columnName = ddlColumn.name()))) {
            columnName = field.getName();
        }
        return columnName;
    }

    /**
     * 查询类中某个字段，如果没有，查询父类中是否有该字段
     * 
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getIterateField(Class<?> clazz, String fieldName) {
        if (clazz == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }
        Class<?> superClass = clazz;
        do {
            try {
                Field declaredField = superClass.getDeclaredField(fieldName);
                if (declaredField != null) {
                    return declaredField;
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        } while (!(superClass = clazz.getSuperclass()).equals(Object.class));
        return null;
    }

    public static String getPrimaryKey(Class<? extends BaseDDL> ddlClass) {
        List<Field> ddlColumnFields = DDLUtils.getDDLColumnFields(ddlClass);
        if (ddlColumnFields == null || ddlColumnFields.size() == 0) {
            return null;
        }
        for (Field field : ddlColumnFields) {
            DDLColumn annotation = field.getAnnotation(DDLColumn.class);
            if (annotation == null) {
                continue;
            }
            if (annotation.isPrimary()) {
                return field.getName();
            }
        }
        return null;
    }

    // /**
    // * 获取某个包下的所有集成自T的类
    // *
    // * @param context
    // * @param clazz
    // * @param pkgName
    // * @return
    // */
    // @SuppressWarnings("unchecked")
    // public static <T> List<Class<T>> getClassFromPkg(Context context,
    // Class<T> clazz, String pkgName) {
    // if (context == null || TextUtils.isEmpty(pkgName)) {
    // return null;
    // }
    // List<Class<T>> classes = new ArrayList<Class<T>>();
    // try {
    // String path =
    // context.getPackageManager().getApplicationInfo(context.getPackageName(),
    // 0).sourceDir;
    // DexFile dexfile = new DexFile(path);
    // Enumeration<String> entries = dexfile.entries();
    // while (entries.hasMoreElements()) {
    // String name = (String) entries.nextElement();
    // if (name.startsWith(pkgName)) {
    // try {
    // Class<?> class1 = Class.forName(name);
    // if (isSubClassOf(class1, clazz)) {
    // Class<T> classT = (Class<T>) class1;
    // classes.add(classT);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // } catch (NameNotFoundException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return classes;
    // }

    /**
     * 判断某个类是否为另一个类的子类
     * 
     * @param subClass
     * @param parentClass
     * @return
     */
    public static boolean isSubClassOf(Class<?> subClass, Class<?> parentClass) {
        if (subClass == null || parentClass == null) {
            return false;
        }
        if (subClass.getName().equals(parentClass.getName())) {
            return true;
        } else {
            return isSubClassOf(subClass.getSuperclass(), parentClass);
        }
    }

    /**
     * 获取类中所有声明为{@link DDLColumn}的字段
     * 
     * @param clazz
     * @return
     */
    public static List<Field> getDDLColumnFields(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Field[] declaredFields = clazz.getFields();
//        Field[] declaredFields = clazz.getDeclaredFields();
        int length = 0;
        if (declaredFields == null || (length = declaredFields.length) == 0) {
            return null;
        }

        List<Field> fields = new ArrayList<Field>();
        for (int i = 0; i < length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            DDLColumn ddlColumn = field.getAnnotation(DDLColumn.class);
            if (ddlColumn != null) {
                fields.add(field);
            }
        }
        return fields;
    }


    /**
     * 根据声明拼写表中某个字段的sql语句
     * 
     * @param ddlColumn
     * @return
     */
    public static String getColumnSql(DDLColumn ddlColumn, Field field) {
        if (ddlColumn == null) {
            return null;
        }
        String name = ddlColumn.name();
        String type = ddlColumn.type();
        return name + " " + type;
    }

    public static <T extends BaseDDL> T toObject(Cursor cursor,Class<T> cls){
    	return DDLOperations.initFromCursor(cursor, cls);
    }
    
    /**
     * 根据实体类获取数据库对应的值
     * @param baseDDL
     * @return
     */
    public static ContentValues getContentValues(Object obj){
    	ContentValues values = new ContentValues();
    	List<Field> fields = DDLUtils.getDDLColumnFields(obj.getClass());
    	for(Field field: fields){
    		String colName = DDLUtils.getColumnNameByFieldName(field);
    		Object value = null;
			try {
				field.setAccessible(true);
				value = field.get(obj);
			} catch (Exception e) {
				com.qihoo.plugin.core.Log.e(TAG, e);
			}
    		Class<?> fClass = field.getType();
    		
    		if(fClass.equals(Integer.class) || fClass.equals(int.class))
    			values.put(colName, (Integer)value);
    		else if(fClass.equals(long.class)||fClass.equals(Long.class))
    			values.put(colName, (Long)value);
    		else if(fClass.equals(String.class))
    			values.put(colName, (String)value);
    		else if(fClass.equals(Boolean.class)||fClass.equals(boolean.class))
    			values.put(colName, (Boolean)value);
    		
    	}
    	return values;
    }
    
}
