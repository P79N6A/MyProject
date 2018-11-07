package com.sankuai.msgp.common.utils;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;

public class UserUtil {
    public static Integer getCurrentUserId() {
        User user = UserUtils.getUser();
        return user != null ? user.getId() : 0;
    }
}