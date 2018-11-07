package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.v1.CacheConfigV1;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-29
 */
public class SnapshotServiceTest {
    private SnapshotService snapshotService = new SnapshotService("/tmp/meituan/config_snapshot");

    @Test
    public void saveSnapshot() {
        CacheConfigV1 config = new CacheConfigV1();
        config.setNodeName("mtconfig.test.group1.xxx");
        snapshotService.saveSnapshot(config);

        CacheConfigV1 config1 = snapshotService.getSnapshot("mtconfig.test.group1.xxx");
        System.out.println(config1.toString());
    }

    @Test
    public void getNotExistSnapshot() {
        CacheConfigV1 config1 = snapshotService.getSnapshot("mtconfig.test.group1.xxx__notexist");
        System.out.println(config1);
    }

    @Test
    public void folderAuth() throws IOException {
        File root = new File("/usr");
        System.out.println(root.exists());
        System.out.println(root.canWrite());
        System.out.println(root.isDirectory());
        System.out.println(root.mkdirs());
        File downloads = new File("/Users/Jason/Downloads");
        System.out.println(downloads.exists());
        System.out.println(downloads.canWrite());
    }
}
