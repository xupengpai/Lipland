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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qihoo.plugin.R;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.install.InstallManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xupengpai on 2017/5/15.
 */

public class PluginItemAdapter extends BaseAdapter {

    private List<PluginPackage> data;
    private Context context;



    public final class ViewHolder {
        public TextView tvTag;
        public TextView tvName;
        public TextView tvVer;
        public TextView tvStatus;
    }

    public PluginItemAdapter(Context context) {
        this.context = context;
        refresh();
    }

    public void refresh(){
        InstallManager installManager = PluginManager.getInstance() .getInstallManager();
        data = new ArrayList<PluginPackage>();
        data.addAll(installManager.getInstalledPlugins().values());
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

        final PluginPackage pluginPackage = data.get(position);
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


        holder.tvTag.setText(pluginPackage.pi.tag);
        holder.tvName.setText(pluginPackage.pi.name);
        holder.tvVer.setText(pluginPackage.pi.versionName);

        PluginManager pm = PluginManager.getInstance();

        holder.tvStatus.setText(pm.isLoaded(pluginPackage.pi.tag)?"已加载":"未加载");

        return convertView;
    }

}