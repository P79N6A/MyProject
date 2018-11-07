package com.sankuai.meituan.config.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ByteUtil {
    public static BufferedInputStream wrapToStream(byte[] data) {
        return data != null ? new BufferedInputStream(new ByteArrayInputStream(data)) : null;
    }
}
