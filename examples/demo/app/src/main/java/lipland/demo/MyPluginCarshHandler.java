package lipland.demo;


import com.qihoo.plugin.base.PluginCarshHandler;
import com.qihoo.plugin.bean.Plugin;

/**
 * Created by xupengpai on 2017/5/26.
 */

public class MyPluginCarshHandler implements PluginCarshHandler {
    @Override
    public void uncaughtException(Plugin plugin, Thread thread, Throwable thr, boolean active, String remarks) {
        if(thr != null)
            thr.printStackTrace();
    }
}
