package lipland.demo;


import com.qihoo.plugin.update.UpdateFilter;

/**
 * Created by xupengpai on 2017/5/26.
 * 监听和控制插件更新过程
 */

public class MyPluginUpdateFilter extends UpdateFilter {
    @Override
    public boolean onCheckUpdate() {
        return true;
    }
}
