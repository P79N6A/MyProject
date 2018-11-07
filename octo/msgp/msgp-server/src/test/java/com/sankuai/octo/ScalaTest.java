package com.sankuai.octo;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.msgp.common.model.ServiceModels.Desc;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import org.junit.Test;
import scala.collection.immutable.List;

import java.util.HashMap;
import java.util.Map;

public class ScalaTest {

    @Test
    public void testZk() {
        List<Desc> list = ServiceCommon.listService();
        System.out.println(list);
    }

    @Test
    public void testService() {
        ServiceCommon.listService();
    }

    @Test
    public void testJson() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "test");
        System.out.println(JsonHelper.dataJson(map));
    }

    static class TestHolder {
        private static ThreadLocal<Boolean> isTraced = null;

        public void start() {
            isTraced = new ThreadLocal<Boolean>();
            isTraced.set(true);
        }

        public void end() {
            if (null == isTraced || null == isTraced.get() || !isTraced.get()) {
                System.out.println("Ping");
            }
            if(null != isTraced) {
                isTraced.remove();
                isTraced = null;
            }
        }
    }

    @Test
    public void testTL() {
    }
}
