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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qihoo.plugin.IPluginLoadListener;
import com.qihoo.plugin.R;
import com.qihoo.plugin.base.Actions;
import com.qihoo.plugin.bean.Plugin;
import com.qihoo.plugin.bean.PluginInfo;
import com.qihoo.plugin.bean.PluginPackage;
import com.qihoo.plugin.core.Log;
import com.qihoo.plugin.core.PluginManager;
import com.qihoo.plugin.install.InstallManager;
import com.qihoo.plugin.util.AndroidUtil;
import com.qihoo.plugin.util.IO;

import java.io.File;

/**
 * Created by xupengpai on 2017/5/15.
 */

public class PluginListFragment extends Fragment {

    private PendingInstallPluginItemAdapter pendingInstallPluginItemAdapter;
    private PluginItemAdapter pluginItemAdapter;
    private PluginInstallReceiver pluginInstallReceiver;


    private ProgressDialog showProgressDialog(String msg) {
    /* @setProgress 设置初始进度
     * @setProgressStyle 设置样式（水平进度条）
     * @setMax 设置进度最大值
     */
        final int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog =
                new ProgressDialog(getActivity());

        progressDialog.setProgress(0);

        progressDialog.setTitle(msg);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.show();
        return progressDialog;
    }

    private void showMenuDialog(final String tag,String title) {
        final String[] items = { "加载","插件详情","耗时统计"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(this.getActivity());
        listDialog.setTitle(title);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent;

                switch (which){
                    case 0:
                        if(!PluginManager.getInstance().isLoaded(tag)) {
                            final ProgressDialog progressDialog = showProgressDialog("插件加载中");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    PluginManager.getInstance().load(tag, new IPluginLoadListener() {
                                        @Override
                                        public void onStart(String tag) {

                                        }

                                        @Override
                                        public void onComplete(String tag, Plugin plugin) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pluginItemAdapter.refresh();
                                                    progressDialog.dismiss();
                                                    Toast.makeText(getActivity(), "插件加载完成", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onLoading(String tag, final int pos) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.setProgress(pos);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String tag, final int code) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.cancel();
                                                    Toast.makeText(getActivity(), "插件加载失败,errCode:" + code, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onThrowException(String tag, final Throwable thr) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.cancel();
                                                    Toast.makeText(getActivity(), "插件加载失败" + AndroidUtil.getExceptionStackTrace(thr), Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                        }
                                    });
                                }
                            }).start();

                        }else{
                            Toast.makeText(getActivity(), "插件已经加载", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        intent = new Intent(PluginsActivity.ACTION_FRAGMENT_SWITCH_SHOW_DETAIL);
                        intent.putExtra("tag",tag);
                        getActivity().sendBroadcast(intent);
                        break;
                    case 2:
                        intent = new Intent(PluginsActivity.ACTION_FRAGMENT_SWITCH_TIME_STATISTICS);
                        intent.putExtra("tag",tag);
                        getActivity().sendBroadcast(intent);
                        break;
                }
            }
        });
        listDialog.show();
    }


    private class PluginInstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(Actions.ACTION_PLUGIN_INSTALLED.equals(intent.getAction())){
                //接收到插件安装成功消息
                pluginItemAdapter.refresh();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getActivity().registerReceiver(pluginInstallReceiver, new IntentFilter(Actions.ACTION_PLUGIN_INSTALLED));
        }catch(Throwable thr){
            thr.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(pluginInstallReceiver);
        }catch(Throwable thr){
            thr.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plugin_list,container, false);
        Button btnImportPlugin = (Button)view.findViewById(R.id.btn_import_plugin);
        ListView listView = (ListView)view.findViewById(R.id.lv_plugins);
        pluginItemAdapter = new PluginItemAdapter(this.getContext());
        listView.setAdapter(pluginItemAdapter);


        ListView lvPendingInstall = (ListView)view.findViewById(R.id.lv_pending_install);
        pendingInstallPluginItemAdapter = new PendingInstallPluginItemAdapter(this.getContext());
        lvPendingInstall.setAdapter(pendingInstallPluginItemAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                PluginPackage pluginPackage = (PluginPackage)pluginItemAdapter.getItem(position);
//
//                Intent intent = new Intent(PluginsActivity.ACTION_FRAGMENT_SWITCH_SHOW_DETAIL);
//                intent.putExtra("tag",pluginPackage.pi.tag);
//                getActivity().sendBroadcast(intent);

                PluginPackage pluginPackage = (PluginPackage)pluginItemAdapter.getItem(position);
                showMenuDialog(pluginPackage.pi.tag,pluginPackage.pi.name);

            }
        });

//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                PluginPackage pluginPackage = (PluginPackage)pluginItemAdapter.getItem(position);
//                showMenuDialog(pluginPackage.pi.tag,pluginPackage.pi.name);
//                return false;
//            }
//        });

//        item.setOnLongClickListener(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//                return false;
//            }
//        });
        btnImportPlugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });

        return view;
    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * * Checks if the app has permission to write to device storage
     * *
     * * If the app does not has permission then the user will be prompted to
     * * grant permissions
     * *
     * * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


    /**
     * * Checks if the app has permission to write to device storage
     * *
     * * If the app does not has permission then the user will be prompted to
     * * grant permissions
     * *
     * * @param activity
     */
    public static void verifyCameraPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA},
                    1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                AppOpsManager a;
                verifyStoragePermissions(getActivity());

                Uri uri = data.getData();
                String str[] = uri.getPath().toString().split(":");
                String path = str[str.length-1];
                if(str.length>1){
                    path = Environment.getExternalStorageDirectory() + "/" + path;
                }
                final String installPath = path;

                View dlg = View.inflate(getActivity(), R.layout.dlg_input_plugin_info, null);
                final EditText tagET = (EditText)dlg.findViewById(R.id.et_tag);
                final EditText verET = (EditText)dlg.findViewById(R.id.et_version);

                PackageManager packageManager = getActivity().getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, 0);
                if(packageInfo != null){
                    verET.setText(packageInfo.versionName);
                }

                tagET.setText(new File(path).getName().replace(".apk",""));


                new AlertDialog.Builder(getActivity())
                        .setTitle("请输入")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(dlg)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                String tag = tagET.getEditableText().toString();
                                String version = verET.getEditableText().toString();
//                String apkPath = getActivity().getCacheDir().getAbsolutePath()+"/" + new File(path).getName();
//                IO.copy(path,apkPath);
                                if(PluginManager.getInstance().install(tag, version, installPath)){

                                    pluginItemAdapter.refresh();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            pluginItemAdapter.refresh();
                                        }
                                    },1000);
                                }
//                Toast.makeText(this, "文件路径："+uri.getPath().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", null)
                        .show();

            }
        }
    }
}






