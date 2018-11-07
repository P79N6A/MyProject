package com.meituan.service.mobile.mtthrift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by jiguang on 15/3/25.
 */
public class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    public static String readStringFromFile(String path){
        File file = null;
        InputStream in = null;
        BufferedReader br = null;
        String line = "";
        StringBuffer sb = new StringBuffer();
        try {
            file = new File(path);
            in = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            if (null != br) {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException e) {
            LOG.debug("readStringFromFile failed...", e);
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.debug("close failed...", e);
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.debug("close failed...", e);
                }
            }
        }
        return sb.toString().trim();
    }

    public static void writeString2File(String path, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            fw.write(content, 0, content.length());
            fw.flush();
        } catch (IOException e) {
            LOG.debug("writeString2File failed...", e);
        } finally {
            if(null != fw) {
                try {
                    fw.close();
                } catch (IOException e) {
                    LOG.debug("close failed...", e);
                }
            }
        }
    }
}
