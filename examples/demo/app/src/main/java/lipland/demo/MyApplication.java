package lipland.demo;

import android.app.Application;
import android.content.Context;

/**
 * Created by xupengpai on 2017/5/26.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        PluginHelper.init(this);


    }

}
