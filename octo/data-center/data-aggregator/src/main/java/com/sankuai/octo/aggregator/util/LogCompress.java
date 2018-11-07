package com.sankuai.octo.aggregator.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

public class LogCompress {
    private static int BUFFER = 1024;

    public static byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        GZIPInputStream gis = new GZIPInputStream(is);

        int count;
        byte temp[] = new byte[BUFFER];
        while ((count = gis.read(temp, 0, BUFFER)) != -1) {
            os.write(temp, 0, count);
        }

        gis.close();

        data = os.toByteArray();

        os.flush();
        os.close();

        is.close();

        return data;
    }
}
