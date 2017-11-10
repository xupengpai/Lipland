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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qihoo.plugin.R;
import com.qihoo.plugin.bean.LoadTimeInfo;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.bean.StartTimeInfo;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.core.TimeStatistics;
import com.qihoo.plugin.install.InstallManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by xupengpai on 2017/5/15.
 */

public class PluginDetailFragment extends Fragment {

    private String getSizeText(long size){
        if(size < 1024)
            return size + "B";

        if(size < 1024 * 1024)
            return (size / 1024) + "K";

        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(((float)size / 1024.0f / 1024.0f)) + "M";

    }

    private void addTextView(LinearLayout contentView,String text){
        TextView tmp = new TextView(getContext());
        tmp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
        tmp.setText(text);
        contentView.addView(tmp);
    }



    public interface PluginProcessListener{

        public void onConnected();

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_detail,container, false);
        LinearLayout contentView = (LinearLayout)view.findViewById(R.id.content);
        TextView tvPluginInfo = new TextView(getContext());
        InstallManager im = PluginManager.getInstance().getInstallManager();

        String tag = (String) getArguments().get("tag");

        if(!TextUtils.isEmpty(tag)) {
            PluginPackage pluginInfo = im.getInstalledPlugin(tag);

            if(pluginInfo != null) {
                tvPluginInfo.setText("tag：" + tag + "\r\n"
                        + "packageName：" + pluginInfo.pkg.packageName + "\r\n"
                        + "name：" + pluginInfo.pi.name + "\r\n"
                        + "desc：" + pluginInfo.pi.desc + "\r\n"
                        + "path：" + pluginInfo.pi.path + "\r\n"
                        + "size：" + getSizeText(new File(pluginInfo.pi.path).length()) + "\r\n"

                        + "\r\n"
                );
                contentView.addView(tvPluginInfo);

                addTextView(contentView, "Activity");

                final ArrayList<PackageParser.Activity> activities = pluginInfo.pkg.activities;

                if (activities != null){
                    for (final PackageParser.Activity activity : activities) {
                        TextView tvActivity = new TextView(getContext());
                        tvActivity.setText(activity.getComponentName().getClassName());
                        contentView.addView(tvActivity);
                        tvActivity.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {



                                String MMAP_SDK_KEY = "241ac0eca3d4f5e45d59";
                                String app_url = "openapp://com.qihoo.msearch.qmap/navigate?action=map&from=haosou&msoAppVersion=4.2.4"
                                        //                                      + version
                                        + "&ak="+MMAP_SDK_KEY;

                                Intent oriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(app_url));

                                Toast.makeText(getContext(),"startActivity()\r\n" + activity.getComponentName().getClassName(),Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.setClassName(activity.getComponentName().getPackageName(),activity.getComponentName().getClassName());
                                startActivity(intent);
                            }
                        });
                    }
                }


                TextView tvTitleService = new TextView(getContext());
                tvTitleService.setText("\r\nService");
                contentView.addView(tvTitleService);

                ArrayList<PackageParser.Service> services = pluginInfo.pkg.services;

                if (services != null && services.size() > 0){
                    for (PackageParser.Service service : services) {
                        TextView tv = new TextView(getContext());
                        tv.setText(service.getComponentName().getClassName());
                        contentView.addView(tv);
                    }
                }else{
                    addTextView(contentView,"(None)");
                }


                TextView tvTitleBroadcastReceiver = new TextView(getContext());
                tvTitleBroadcastReceiver.setText("\nBroadcastReceiver");
                contentView.addView(tvTitleBroadcastReceiver);

                ArrayList<PackageParser.Activity> receivers = pluginInfo.pkg.receivers;

                if (receivers != null && receivers.size() > 0){
                    for (PackageParser.Activity receiver : receivers) {
                        TextView tv = new TextView(getContext());
                        tv.setText(receiver.getComponentName().getClassName());
                        contentView.addView(tv);
                    }
                }else{
                    addTextView(contentView,"(None)");
                }

                addTextView(contentView,"\nContentProvider");

                ArrayList<PackageParser.Provider> providers = pluginInfo.pkg.providers;

                if (providers != null && providers.size() > 0){
                    for (PackageParser.Provider provider : providers) {
                        addTextView(contentView,provider.info.authority + "("+provider.getComponentName().getClassName()+")");
                    }
                }else{
                    addTextView(contentView,"(None)");
                }
            }
        }
        return view;
    }


}
