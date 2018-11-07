import com.sankuai.octo.errorlog.mock.Bootstrap;
import com.sankuai.octo.errorlog.mock.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by yves on 16/12/16.
 *
 */
public class MockTest {
    public static void main(String[] args) throws  Exception{
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        Bootstrap bootstrap = (Bootstrap)ac.getBean("bootstrap");
        System.out.println(bootstrap);
        Thread.sleep(10000L);
    }
}
