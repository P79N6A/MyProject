package com.sankuai.meituan.config.listener;

import java.io.BufferedInputStream;

public interface FileChangeListener {
    void changed(String fileName, BufferedInputStream oriFile, BufferedInputStream newFile);
}
