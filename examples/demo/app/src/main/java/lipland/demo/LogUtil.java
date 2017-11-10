package lipland.demo;

import android.util.Log;

import com.qihoo.plugin.base.DefaultLogHandler;


/**
 * Created by xupengpai on 2017/5/26.
 *
 */

public class LogUtil {

    public static int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, Throwable thr) {
        return Log.e(tag, "",thr);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }


    //插件的日志处理器，这里设置为全部调用宿主的日志处理，将他们统一起来
    public static class MyPluginLogHandler extends DefaultLogHandler {

        public int d(String tag, String msg) {
            return LogUtil.d(tag,msg);
        }

        public int d(String tag, String msg, Throwable tr) {
            return LogUtil.d(tag,msg,tr);
        }

        public int i(String tag, String msg) {
            return LogUtil.i(tag, msg);
        }

        public int i(String tag, String msg, Throwable tr) {
            return LogUtil.i(tag, msg, tr);
        }

        public int e(String tag, String msg) {
            return LogUtil.e(tag, msg);
        }

        public int e(String tag, Throwable thr) {
            return LogUtil.e(tag, "",thr);
        }

        public int e(String tag, String msg, Throwable tr) {
            return LogUtil.e(tag, msg, tr);
        }
    }


}
