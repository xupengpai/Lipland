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

package com.qihoo.plugin.base.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qihoo.plugin.Config;
import com.qihoo.plugin.R;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.bean.UpdateInfo;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.install.InstallManager;
import com.qihoo.plugin.update.UpdateManager;
import com.qihoo.plugin.util.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupengpai on 2017/5/15.
 */

public class PendingInstallPluginItemAdapter extends BaseAdapter {

    private final static String TAG = "PendingInstallPluginItemAdapter";

    private List<UpdateInfo> data;
    private Context context;

    public final class ViewHolder {
        public TextView tvTag;
        public TextView tvName;
        public TextView tvVer;
        public TextView tvStatus;
    }

    public PendingInstallPluginItemAdapter(Context context) {
        this.context = context;
        refresh();
    }

    public void refresh(){
        data = new ArrayList<>();

        try {
            String path = Config.getPluginPendingInstallDir();
            Log.d(TAG, "refresh()...path="+path);
            String files[] = new File(path).list();
            if (files != null && files.length > 0) {
                for(String fileName : files){
                    String file = path + "/" + fileName;
                    Log.d(TAG, "refresh()...file="+file);
                    try {
                        UpdateInfo updateInfo = (UpdateInfo) IO.unserialize(file);
                        Log.d(TAG, "refresh()...updateInfo="+updateInfo);
                        if (updateInfo != null) {
                            data.add(updateInfo);
                        }
                    }catch (Exception e){
                        Log.e(TAG, e);
                    }
                }
            }
        }catch(Exception e){
            Log.e(TAG, e);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int pos) {
        // TODO Auto-generated method stub
        return data.get(pos);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final UpdateInfo updateInfo = data.get(position);
        ViewHolder holder = null;
        if (convertView == null) {

            holder = new ViewHolder();
            View item = View.inflate(context, R.layout.item_plugin_summary,null);


            TextView tvTag = (TextView) item.findViewById(R.id.tv_tag);
            TextView tvName = (TextView) item.findViewById(R.id.tv_name);
            TextView tvVer = (TextView) item.findViewById(R.id.tv_ver);
            TextView tvStatus = (TextView) item.findViewById(R.id.tv_status);

//				LinearLayout.LayoutParams tvLP = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//				tvLP.weight = 1.0f;
//				tagTextView.setLayoutParams(tvLP);
////				tagTextView.setBackgroundColor(Color.RED);
//				nameTextView.setLayoutParams(tvLP);
////				nameTextView.setBackgroundColor(Color.GREEN);
////				nameTextView.setLayoutParams(new LayoutParams(220*2, LayoutParams.WRAP_CONTENT));
//				verTextView.setLayoutParams(tvLP);
////				verTextView.setBackgroundColor(Color.BLUE);

            holder.tvTag = tvTag;
            holder.tvName = tvName;
            holder.tvVer = tvVer;
            holder.tvStatus = tvStatus;
            convertView = item;
            convertView.setTag(holder);



        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.tvTag.setText(updateInfo.getTag());
        holder.tvName.setText(updateInfo.getPluginInfo().name);
        holder.tvVer.setText(updateInfo.getVersion());

//        PluginManager pm = PluginManager.getInstance();

//        holder.tvStatus.setText(pm.isLoaded(pluginPackage.pi.tag)?"已加载":"未加载");

        return convertView;
    }

}