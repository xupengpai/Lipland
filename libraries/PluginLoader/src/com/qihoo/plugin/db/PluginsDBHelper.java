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

package com.qihoo.plugin.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.qihoo.common.ormapping.BaseDDL;
import com.qihoo.common.ormapping.DDLOperations;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.core.Log;

/**
 * renjihai 2015年2月28日
 */
public class PluginsDBHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DB_NAME = "plugins.db";
    private static final int version = 0x03; //0x02 installed表没有loadOnAppStarted字段,0x03加上该字段
    private static final String TAG = PluginsDBHelper.class.getSimpleName();

    private PluginsDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public PluginsDBHelper(Context context) {
        this(context, DB_NAME, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db, oldVersion, newVersion);
        createTables(db);
        if(newVersion == 0x03){
            String sql =  "ALTER TABLE 'installed' ADD  'loadOnAppStarted' INT";
            db.execSQL(sql);
        }
    }

    public void createTables(SQLiteDatabase db) {
        List<BaseDDL> baseDDLs = getBaseDDLs();
        for (BaseDDL baseDDL : baseDDLs) {
            try {
                String sql = baseDDL.CREATE();
                Log.i(TAG, sql);
                db.execSQL(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sql = new PluginInfo().CREATE();
        Log.i(TAG, sql);
        db.execSQL(sql);
    }

    private List<BaseDDL> getBaseDDLs() {
        List<BaseDDL> ddls = new ArrayList<BaseDDL>();
        return ddls;
    }


    private void deleteOneApplication(SQLiteDatabase db, String packageName) {
        List<BaseDDL> baseDDLs = getBaseDDLs();
        if (baseDDLs == null) {
            return;
        }
        for (BaseDDL baseDDL : baseDDLs) {
            try {
                DDLOperations.delete(db, baseDDL.getTabName(), "pkgName=?",
                        new String[] { packageName });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void dropTables(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<BaseDDL> baseDDLs = getBaseDDLs();
        for (BaseDDL baseDDL : baseDDLs) {
            try {
                String sql = baseDDL.DROP();
                db.execSQL(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
