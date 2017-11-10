package lipland.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.qihoo.plugin.base.BasePluginProcessListener;
import com.qihoo.plugin.base.ui.PluginsActivity;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn_start_sample_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.qihoo.plugin.base.PluginHelper.startPluginProcess(new BasePluginProcessListener(){

                    public void onReady() {
                        Intent intent = new Intent();
                        intent.setClassName("lipland.sample_plugin","lipland.sample_plugin.MainActivity");
//                intent.setAction("action.sample_plugin.main");
                        startActivity(intent);

                    }

                });
            }
        });



        //插件管理页面一般调试和测试插件时使用，产品发布时应该隐藏入口
        findViewById(R.id.btn_start_plugin_manager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.qihoo.plugin.base.PluginHelper.startPluginProcess(new BasePluginProcessListener(){

                    public void onReady() {
                        Intent intent = new Intent(MainActivity.this,PluginsActivity.class);
                        startActivity(intent);
                    }

                });
            }
        });



        /*
        findViewById(R.id.btn_start_image_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setClassName("com.qihoo.browser.imageplugin","com.qihoo.browser.imageplugin.activity.MainActivity");
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_start_safebarcode_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setClassName("com.qihoo360.saoma","com.qihoo360.plugins.barcode.a.MainActivity");
                startActivity(intent);
            }
        });
        */

    }
}
