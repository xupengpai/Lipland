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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;

import com.qihoo.plugin.R;


public class PluginsActivity extends FragmentActivity {

	private FragmentManager fm;
	private PluginListFragment pluginListFragment;
	private PluginDetailFragment pluginDetailFragment;
	private FragmentControlReceiver fragmentControlReceiver;
	private TimeStatisticsFragment timeStatisticsFragment;
	private Fragment currentFragment;

	public final static int MSG_SHOW_PLUGIN_DETAIL = 1;
	public final static String ACTION_FRAGMENT_SWITCH_SHOW_DETAIL = "action.fragment.showdetail";
	public final static String ACTION_FRAGMENT_SWITCH_TIME_STATISTICS = "action.fragment.time_statistics";

	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_SHOW_PLUGIN_DETAIL){
				Bundle bundle = msg.getData();
				if(bundle != null){
					String tag = bundle.getString("tag");
					if(tag != null){
						pluginDetailFragment.setArguments(bundle);
						switchFragment(pluginDetailFragment,false);
					}
				}
			}
			super.handleMessage(msg);
		}
	}
	private Handler handler = new Handler();



	@Override
	protected void onDestroy() {
		unregisterReceiver(this.fragmentControlReceiver);
		super.onDestroy();
	}

	private class FragmentControlReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(ACTION_FRAGMENT_SWITCH_SHOW_DETAIL.equals(action)){
				String tag = intent.getStringExtra("tag");
				if(tag != null){
					Bundle bundle = pluginDetailFragment.getArguments();
					if(bundle == null) {
						bundle = new Bundle();
						pluginDetailFragment.setArguments(bundle);
					}
					bundle.putString("tag",tag);
					switchFragment(pluginDetailFragment,false);
				}
			}
			else if(ACTION_FRAGMENT_SWITCH_TIME_STATISTICS.equals(action)){
				String tag = intent.getStringExtra("tag");
				if(tag != null){
					Bundle bundle = timeStatisticsFragment.getArguments();
					if(bundle == null) {
						bundle = new Bundle();
						timeStatisticsFragment.setArguments(bundle);
					}
					bundle.putString("tag",tag);
					switchFragment(timeStatisticsFragment,false);
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plugin_list);
		fm = getSupportFragmentManager();

		pluginListFragment = new PluginListFragment();
		pluginDetailFragment = new PluginDetailFragment();
		timeStatisticsFragment = new TimeStatisticsFragment();
		switchFragment(new PluginListFragment(),true);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_FRAGMENT_SWITCH_SHOW_DETAIL);
		filter.addAction(ACTION_FRAGMENT_SWITCH_TIME_STATISTICS);
		fragmentControlReceiver = new FragmentControlReceiver();
		registerReceiver(fragmentControlReceiver,filter);

	}

	private void switchFragment(Fragment fragment, boolean init){
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.container, fragment);
		if(!init)
			ft.addToBackStack(null);
		ft.commit();
		currentFragment = fragment;
	}

//	@Override
//	public void onBackPressed() {
//		if(currentFragment == pluginDetailFragment){
//			switchFragment(pluginListFragment,false);
//		}else {
//			super.onBackPressed();
//		}
//	}
}
