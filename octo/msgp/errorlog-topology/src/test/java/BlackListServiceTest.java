import com.sankuai.logparser.service.BlackListService;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by emma on 2017/8/6.
 */
public class BlackListServiceTest {
    BlackListService blackListService = BlackListService.getInstance();

    @Test
    public void testSetBlackListNotifyMsg() throws InterruptedException {
        int i = 0;
        while (i++ < 20000) {
            // 收到限制通知
            blackListService.countAppkeyLogPerMin("com.sankuai.kv.test");
        }
        Thread.sleep(60000);
        i = 0;
        while (i++ < 100) {
            // 收到恢复通知
            blackListService.countAppkeyLogPerMin("com.sankuai.kv.test");
        }
        Thread.sleep(61000);
    }

    @Test
    public void testAddBlackList() throws InterruptedException {
        blackListService.deleteDynamicBlacklist("appkey1");
        Assert.assertEquals(true, blackListService.addDynamicBlacklist("appkey1"));
        Thread.sleep(2000);

        Assert.assertEquals(false, blackListService.addDynamicBlacklist("appkey1"));

        Thread.sleep(2000);
        Assert.assertEquals(true, blackListService.isInDynamicBlackList("appkey1"));
        Assert.assertEquals(true, blackListService.deleteDynamicBlacklist("appkey1"));
    }

    @Test
    public void testDeleteBlackList() throws InterruptedException {
        blackListService.addDynamicBlacklist("appkey1");
        Thread.sleep(2000);

        Assert.assertEquals(true, blackListService.deleteDynamicBlacklist("appkey1"));
        Thread.sleep(2000);
        Assert.assertEquals(false, blackListService.isInDynamicBlackList("appkey1"));
    }
}