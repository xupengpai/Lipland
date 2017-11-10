package lipland.demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("qihoo.com.pluginloader", appContext.getPackageName());
    }


    public void dex2oat(String file,String targetFile){
        String cmd = String.format("dex2oat --debuggable --compiler-filter=speed --dex-file=%s --oat-file=%s",file,targetFile);
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String cmd = "dex2oat --debuggable --compiler-filter=speed --dex-file=/data/user/0/com.qihoo.haosou/files/patchs/global.jar --oat-file=/data/user/0/com.qihoo.haosou/files/dex/global/1495613371291/global.dex


    }

}
