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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.db.PluginsDBHelper;

/**
 * renjihai 2015年3月4日
 */
public class DDLOperations {
    /**
     * 插入操作
     * 
     * @param db
     * @return
     */
    public synchronized static long inseart(SQLiteDatabase db, BaseDDL baseDDL) {
        String table = baseDDL.getTabName();
        ContentValues values = getContentValues(baseDDL);
        return db.insert(table, null, values);
    }

    /**
     * 插入操作
     * 
     * @param db
     * @return
     */
    public synchronized static long inseart(Context context, BaseDDL baseDDL) {
        String table = baseDDL.getTabName();
        ContentValues values = getContentValues(baseDDL);
        SQLiteDatabase sqLiteDatabase = new PluginsDBHelper(context).getWritableDatabase();
        try {
            return sqLiteDatabase.insert(table, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
        return -1;
    }

    public synchronized static <T extends BaseDDL> long insertOrUpdate(SQLiteDatabase db,
            Class<T> clazz, T baseDDL) {
        String primaryKey = DDLUtils.getPrimaryKey(baseDDL.getClass());
        if (primaryKey != null) {
            try {
                Field field = baseDDL.getClass().getField(primaryKey);
                Object object = field.get(baseDDL);
                List<T> query = query(db, clazz, primaryKey + "=?",
                        new String[] { object.toString() + "" });
                if (query == null || query.size() == 0) {
                    return inseart(db, baseDDL);
                } else {
                    return update(db, baseDDL, primaryKey + "=?", new String[] { object.toString()
                            + "" });
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return inseart(db, baseDDL);
    }

    public synchronized static <T extends BaseDDL> long insertOrUpdate(Context context,
            Class<T> clazz, T baseDDL) {
        SQLiteDatabase db = null;
        try {
            db = new PluginsDBHelper(context).getWritableDatabase();
            String primaryKey = DDLUtils.getPrimaryKey(baseDDL.getClass());
            if (primaryKey != null) {
                try {
                    Field field = baseDDL.getClass().getField(primaryKey);
                    Object object = field.get(baseDDL);
                    List<T> query = query(db, clazz, primaryKey + "=?",
                            new String[] { object.toString() + "" });
                    if (query == null || query.size() == 0) {
                        return inseart(db, baseDDL);
                    } else {
                        return update(db, baseDDL, primaryKey + "=?",
                                new String[] { object.toString() + "" });
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            return inseart(db, baseDDL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return -1;
    }

    /**
     * 更新操作
     * 
     * @param db
     * @return
     */
    public synchronized static long update(SQLiteDatabase db, BaseDDL baseDDL, String whereClause,
            String[] whereArgs) {
        String table = baseDDL.getTabName();
        ContentValues values = getContentValues(baseDDL);
        return db.update(table, values, whereClause, whereArgs);
    }

    /**
     * 删除操作
     * 
     * @param db
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public synchronized static long delete(SQLiteDatabase db, String table, String whereClause,
            String[] whereArgs) {
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * 只查询数据库中满足条件的第一条
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> T queryFirst(SQLiteDatabase db, Class<T> clazz,
            String table, String selection, String[] selectionArgs) {
        return queryFirst(db, clazz, table, selection, selectionArgs, null, null, null);
    }

    /**
     * 只查询数据库中满足条件的第一条
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> T queryFirst(SQLiteDatabase db, Class<T> clazz,
            String table, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        Cursor cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                return initFromCursor(cursor, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public synchronized static <T extends BaseDDL> T queryFirst(Context context, Class<T> clazz,
            String selection, String[] selectionArgs) {
        if (clazz == null) {
            return null;
        }
        T newInstance = null;
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String tabName = newInstance.getTabName();
        return queryFirst(context, clazz, tabName, selection, selectionArgs, null, null, null);
    }

    public synchronized static <T extends BaseDDL> T queryFirst(Context context, Class<T> clazz,
            String table, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        SQLiteDatabase sqLiteDatabase = new PluginsDBHelper(context).getReadableDatabase();
        try {
            return queryFirst(sqLiteDatabase, clazz, table, selection, selectionArgs, groupBy,
                    having, orderBy);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
        return null;
    }

    /**
     * 只查询数据库中满足条件的第一条
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> T queryFirst(SQLiteDatabase db, Class<T> clazz,
            String selection, String[] selectionArgs) {
        if (clazz == null) {
            return null;
        }
        T newInstance = null;
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String tabName = newInstance.getTabName();
        return queryFirst(db, clazz, tabName, selection, selectionArgs);
    }

    /**
     * 数据库查询
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> List<T> query(SQLiteDatabase db, Class<T> clazz,
            String table, String selection, String[] selectionArgs) {
        return query(db, clazz, table, selection, selectionArgs, null, null, null);
    }

    /**
     * 数据库查询
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> List<T> query(Context context, Class<T> clazz,
            String selection, String[] selectionArgs) {
        if (clazz == null) {
            return null;
        }
        T newInstance = null;
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String tabName = newInstance.getTabName();
        return query(context, clazz, tabName, selection, selectionArgs, null, null, null);
    }

    /**
     * 数据库查询
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> List<T> query(Context context, Class<T> clazz,
            String table, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        SQLiteDatabase readableDatabase = new PluginsDBHelper(context).getReadableDatabase();
        try {
            return query(readableDatabase, clazz, table, selection, selectionArgs, groupBy, having,
                    orderBy);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (readableDatabase != null) {
                readableDatabase.close();
            }
        }
        return null;
    }

    /**
     * 数据库查询
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> List<T> query(SQLiteDatabase db, Class<T> clazz,
            String table, String selection, String[] selectionArgs, String groupBy, String having,
            String orderBy) {
        Cursor cursor = db.query(table, null, selection, selectionArgs, groupBy, having, orderBy);
        List<T> list = new ArrayList<T>();
        try {
            while (cursor.moveToNext()) {
                T t = initFromCursor(cursor, clazz);
                if (t != null) {
                    list.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 数据库查询
     * 
     * @param db
     * @param clazz
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public synchronized static <T extends BaseDDL> List<T> query(SQLiteDatabase db, Class<T> clazz,
            String selection, String[] selectionArgs) {
        if (clazz == null) {
            return null;
        }
        T newInstance = null;
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String tabName = newInstance.getTabName();
        return query(db, clazz, tabName, selection, selectionArgs);
    }

    /**
     * 从cursor中取出数据,进行对象映射
     * 
     * @param cursor
     * @param clazz
     * @return
     */
    static synchronized <T extends BaseDDL> T initFromCursor(Cursor cursor, Class<T> clazz) {
        try {
            T t = clazz.newInstance();
            List<Field> ddlColumnFields = DDLUtils.getDDLColumnFields(clazz);
            for (Field field : ddlColumnFields) {
                DDLColumn ddlColumn = field.getAnnotation(DDLColumn.class);
                String columnName = ddlColumn.name();
                if (TextUtils.isEmpty(columnName)) {
                    columnName = field.getName();
                }
                initFieldFromCursor(cursor, columnName, t, field);
            }

            if (DDLUtils.isSubClassOf(clazz, BaseDDL.class)) {
                List<Field> ddlColumnFields2 = DDLUtils.getDDLColumnFields(BaseDDL.class);
                if (ddlColumnFields2 != null) {
                    for (Field field : ddlColumnFields2) {
                        DDLColumn ddlColumn = field.getAnnotation(DDLColumn.class);
                        String columnName = ddlColumn.name();
                        if (TextUtils.isEmpty(columnName)) {
                            columnName = field.getName();
                        }
                        initFieldFromCursor(cursor, columnName, t, field);
                    }
                }
            }
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param cursor
     * @param object
     * @param field
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    static synchronized void initFieldFromCursor(Cursor cursor, String columnName, Object object,
            Field field) throws IllegalAccessException, IllegalArgumentException {
        field.setAccessible(true);
        Object value = null;
        String typeName = field.getType().getName();
        if (Integer.class.getName().equals(typeName) || "int".equals(typeName)) {
            value = cursor.getInt(cursor.getColumnIndex(columnName));
        } else if (Short.class.getName().equals(typeName) || "short".equals(typeName)) {
            value = cursor.getShort(cursor.getColumnIndex(columnName));
        } else if (Long.class.getName().equals(typeName) || "long".equals(typeName)) {
            value = cursor.getFloat(cursor.getColumnIndex(columnName));
        } else if (Float.class.getName().equals(typeName) || "float".equals(typeName)) {
            value = cursor.getFloat(cursor.getColumnIndex(columnName));
        } else if (Double.class.getName().equals(typeName) || "double".equals(typeName)) {
            value = cursor.getDouble(cursor.getColumnIndex(columnName));
        } else {
            value = cursor.getString(cursor.getColumnIndex(columnName));
        }
        field.set(object, value);
    }

    /**
     * 将baseddl或其子类,转化成数据库数据表
     * 
     * @param baseDDL
     * @return
     */
    static synchronized ContentValues getContentValues(BaseDDL baseDDL) {
        ContentValues contentValues = new ContentValues();
        updateContentValues(contentValues, baseDDL, baseDDL.getClass());
        if (!baseDDL.getClass().getName().equals(BaseDDL.class.getName())) {
            updateContentValues(contentValues, baseDDL, BaseDDL.class);
        }
        return contentValues;
    }

    static synchronized void updateContentValues(ContentValues values, BaseDDL baseDDL,
            Class<? extends BaseDDL> clazz) {
        List<Field> ddlColumnFields = DDLUtils.getDDLColumnFields(clazz);
        if (ddlColumnFields == null || ddlColumnFields.size() == 0) {
            return;
        }
        for (Field field : ddlColumnFields) {
            try {
                DDLColumn ddlColumn = field.getAnnotation(DDLColumn.class);
                String key = null;
                if (!TextUtils.isEmpty(ddlColumn.name())) {
                    key = ddlColumn.name();
                } else {
                    key = field.getName();
                }
                Object object = field.get(baseDDL);
                if (ddlColumn.isAutoIncrement()) {
                    continue;
                } else if (object != null) {
                    String value = object.toString();
                    values.put(key, value);
                } else {
                    values.put(key, "");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    
    public static  void listAllActivity(Context context) {

    	PluginsDBHelper dbhelper = new PluginsDBHelper(context);
        SQLiteDatabase db = dbhelper.getReadableDatabase(); 
        Cursor cursor = db.query("XmlActivity", null, null, null, null, null, null);
        try {
        	int colCount = cursor.getColumnCount();
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            	Log.i("listAllActivity", "--------------------------------");
            	for(int i=0;i<colCount;i++){
            		String value = cursor.getString(i);
            		String name = cursor.getColumnName(i);
                	Log.i("listAllActivity", name+"="+value);
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
	    	if(db != null)
	    		db.close();
        }
    }
    

}
