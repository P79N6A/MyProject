package com.sankuai.inf.octo.mns.listener;

import java.io.BufferedInputStream;

/**
 * Created by lhmily on 06/15/2016.
 */
public interface IFileChangeListener {
    void changed(String fileName, BufferedInputStream oriFile, BufferedInputStream newFile);
}
