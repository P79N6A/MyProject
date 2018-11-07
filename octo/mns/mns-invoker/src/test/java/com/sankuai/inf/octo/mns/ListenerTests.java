package com.sankuai.inf.octo.mns;

import java.io.BufferedInputStream;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/9/22
 * Description:
 */
public class ListenerTests implements IFileChangeListener {
    @Override
    public void changed(String fileName, BufferedInputStream oriFile, BufferedInputStream newFile) {
        System.out.println(fileName);

    }
}
