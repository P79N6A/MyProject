package com.sankuai.meituan.config;

import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.meituan.config.listener.FileChangeListener;
import com.sankuai.octo.config.model.ConfigFile;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileClientTest {


    private static String remoteAppkey = "com.sankuai.octo.tmy";
    @Before
    public void setUp() {
        //MnsInvoker.getInstance("192.168.41.239:5266");
        //MnsInvoker.getInstance("10.4.244.156:5266");
    }

    @Test
    public void testGet() throws IOException {
        FileConfigClient client = new FileConfigClient();
        client.setAppkey(remoteAppkey);
        client.init();
        BufferedInputStream stream = client.getFile("settings.xml");
        printFile(stream);
        Assert.assertNotNull(stream);
    }

    @Test
    public void testChange() throws IOException, InterruptedException {
        FileConfigClient client = new FileConfigClient();
        client.setAppkey(remoteAppkey);
        client.init();
        ConfigFile file = client.getConfigFile("settings.xml");
        System.out.println(file.getErr_code());
//        final FileConfigClient client = new FileConfigClient();
//        client.setAppkey("com.sankuai.inf.sg_agent");
//        client.init();
//        // 必须先get才能触发
//        BufferedInputStream stream = client.getFile("test.conf");
//        printFile(stream);
//        client.addListener("test.conf", new FileChangeListener() {
//            @Override
//            public void changed(String fileName, BufferedInputStream oriFile, BufferedInputStream newFile) {
//                try {
//                    System.out.println(fileName);
//                    printFile(oriFile);
//                    printFile(newFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        new Thread() {
//            public void run() {
//                while (true) {
//                    try {
//                        BufferedInputStream stream = client.getFile("test.conf");
//                        printFile(stream);
//                        Thread.sleep(1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
//        Thread.sleep(6000);
    }


    private void printFile(BufferedInputStream stream) throws IOException {
        byte[] buffer = new byte[1000];
        stream.read(buffer, 0, 1000);
        String str = new String(buffer);
        System.out.println(str);
    }

}
