package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.util.ContextUtil;
import org.junit.Test;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/8/15
 * Description:
 */
public class ContextUtilTest {
    @Test
    public void initialTest() {
        System.out.println(ContextUtil.getGlobalContext());
        ContextUtil.clearContext();
        System.out.println(ContextUtil.getGlobalContext());

    }
}
