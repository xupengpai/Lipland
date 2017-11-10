package lipland.demo;

import android.app.Application;
import android.os.Build;

import com.qihoo.plugin.core.PluginManager;


/**
 * Created by xupengpai on 2017/5/26.
 */

public class PluginHelper {

    private static final String TAG = "PluginHelper";
    private static final String PLUGIN_PROCESS = ":plugin";

    public final static String PLUGIN_UPDATE_XML = "/files/plugin/config/update.xml";

    private static Application app;


    public static boolean isPluginProcess(){
        return PluginManager.getInstance().isPluginProcess();
    }

    //支持的android最低版本为4.0.3
    public static boolean isPluginsSupport(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    //初始化插件框架相关设置
    public static boolean init(Application app){

        PluginHelper.app = app;

        if (!isPluginsSupport()) {
            return false;
        }

        //安装插件管理器，必须的步骤
        PluginManager.setup(app);

        PluginManager pluginManager = PluginManager.getInstance();

        //开启调试模式，调试模式下，打开日志。
        //不验证插件签名
        pluginManager.setDebug(true);
        pluginManager.setVerifySign(false);

        //配置插件管理器，可选
        configure();

        return true;

    }


    /**
     * 开始插件更新检测
     */
    public static void startUpdate(){
        PluginManager.getInstance().startUpdate(true,MyPluginUpdateFilter.class);
    }

    //对插件管理器进行配置和，非必选
    //包括对捆包插件的初始化、安装/加载/更新插件的监听、crash信息处理、日志处理、优化设置等等
    public static void configure(){

        PluginManager pluginManager = PluginManager.getInstance();

        //设置插件的日志处理器，可以帮助与宿主的日志处理同步
        pluginManager.setLogHandler(new LogUtil.MyPluginLogHandler());
//        pluginManager.addPluginSign("xxxx"); //添加签名验证md5

        //为避免影响主程序的性能，插件相关的操作全部放在插件进程。
        if(isPluginProcess()) {

            //设置插件crash处理器
            pluginManager.enableCrashHandler(new MyPluginCarshHandler());

            //检测更新
            startUpdate();

            //测试时用，设置为true，捆包的插件每次都会重新安装，否则只安装一次
            //pluginManager.setForceInstallDefaultPlugin(true);
//
//            //设置默认插件加载监听
//            pluginManager.setDefaultPluginLoadListener(new HaosouPluginLoadListener(app));
//
//            //为地图插件单独设置加载监听
//            pluginManager.setDefaultPluginLoadListener(PluginConstans.TAG_MAP, MapPluginLoadListener.getInstance());
//
//            //检测更新
//            startUpdate();
//
//            app.registerReceiver(mUpdatePluginReceiver,
//                    new IntentFilter(Actions.ACTION_PLUGIN_INSTALLED));
//
//
//            app.registerReceiver(pluginUpdateMonitor,
//                    new IntentFilter(Actions.ACTION_UPDATE_GLOBAL_UPDATED));
        }

    }

}
