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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qihoo.plugin.R;
import com.qihoo.plugin.bean.LoadTimeInfo;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.bean.StartTimeInfo;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.core.TimeStatistics;
import com.qihoo.plugin.install.InstallManager;

import java.io.File;

/**
 * Created by xupengpai on 2017/5/16.
 */

public class TimeStatisticsFragment extends Fragment{

    private void addTextView(LinearLayout contentView, String text){
        TextView tmp = new TextView(getContext());
        tmp.setText(text);
        contentView.addView(tmp);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_detail, container, false);
        LinearLayout contentView = (LinearLayout) view.findViewById(R.id.content);

        InstallManager im = PluginManager.getInstance().getInstallManager();

        String tag = (String) getArguments().get("tag");

        if (!TextUtils.isEmpty(tag)) {

            PluginPackage pluginInfo = im.getInstalledPlugin(tag);
            addTextView(contentView, "\r\n" + pluginInfo.pi.name + "\r\n");

            if (pluginInfo != null) {

                LoadTimeInfo loadTimeInfo = TimeStatistics.getLoadTimeInfo(tag);
                StartTimeInfo startTimeInfo = TimeStatistics.getOrNewStartTimeInfo(tag);

                addTextView(contentView, "加载耗时");
                if (loadTimeInfo != null) {
                    addTextView(contentView, "copyToWork：" + loadTimeInfo.copyToWork + "ms");
                    addTextView(contentView, "verifySign：" + loadTimeInfo.verifySign + "ms");
                    addTextView(contentView, "parseApk：" + loadTimeInfo.parseApk + "ms");
                    addTextView(contentView, "createClassLoader：" + loadTimeInfo.createClassLoader + "ms");
                    addTextView(contentView, "loadResources：" + loadTimeInfo.loadResources + "ms");
                    addTextView(contentView, "registerReceivers：" + loadTimeInfo.registerReceivers + "ms");
                    addTextView(contentView, "unzipLibs：" + loadTimeInfo.unzipLibs + "ms");
                    addTextView(contentView, "createApplication：" + loadTimeInfo.createApplication + "ms");
                    addTextView(contentView, "hookContext：" + loadTimeInfo.hookContext + "ms");
                    addTextView(contentView, "getInstalledPlugin：" + loadTimeInfo.getInstalledPlugin + "ms");
                    addTextView(contentView, "total：" + loadTimeInfo.total() + "ms");
                }else{
                    addTextView(contentView, "(未加载)");
                }
                addTextView(contentView, "");

                addTextView(contentView, "启动耗时");
                if (startTimeInfo != null && startTimeInfo.activity_name != null) {
                    addTextView(contentView, "activity_name：" + startTimeInfo.activity_name);
                    addTextView(contentView, "activity_newActivity：" + startTimeInfo.activity_newActivity + "ms");
                    addTextView(contentView, "activity_onCreate：" + startTimeInfo.activity_onCreate + "ms");
                    addTextView(contentView, "activity_onCreate_host：" + startTimeInfo.activity_onCreate_host + "ms");
                    addTextView(contentView, "total：" + startTimeInfo.activity_onCreate_total + "ms");
                }else {
                    addTextView(contentView, "(未启动)");
                }
                addTextView(contentView, "");

            }
        }
        return view;
    }
}
