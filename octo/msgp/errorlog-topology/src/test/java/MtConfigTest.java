import com.sankuai.logparser.util.MtConfig;
import com.sankuai.meituan.config.MtConfigClient;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by yves on 16/12/28.
 * test for Config
 */
public class MtConfigTest {

    private static String testStr;

    private static MtConfigClient mtConfigClient;

    @Test
    public void testHashCode() {
        int round = 10000;

        String[] appkeys = {"com.sankuai.logparser.util.MtConfig",
                "com.sankuai.meituan.config.v2.MtConfigClientV2",
                "com.sankuai.inf.octo.mns.cache.MnsCacheManager",
                "org.reflections.Reflections"};
        List<String> appkeyList = new ArrayList<>();
        for (int i = 0; i < round; i++) {
            for (String appkey : appkeys) {
                int max = round;
                int min = 20;
                Random random = new Random();
                int s = random.nextInt(max) % (max - min + 1) + min;
                appkeyList.add(appkey + s);
            }
        }

        Long begin = System.currentTimeMillis();
        for (int i = 0; i < round; i++) {
            for (String appkey : appkeyList) {
                int hash = appkey.hashCode();
                int hashId = hash % 10;
                //System.out.println("hash: " + hash + ", hash id: " + hashId);
            }
        }
        Long end = System.currentTimeMillis();
        System.out.println("It cost " + (end - begin) + " ms");

        //reustl: 10000 times cost 3600ms
    }

    @Test
    public void multiThread() {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                mtConfigClient = MtConfig.getTopologyCfgClient();
                System.out.println(mtConfigClient.getValue("appkey.blacklist.dynamic"));
            }
        }, "t1");
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                mtConfigClient = MtConfig.getTopologyCfgClient();
            }
        }, "t2");
        t1.start();
        t2.start();
        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
