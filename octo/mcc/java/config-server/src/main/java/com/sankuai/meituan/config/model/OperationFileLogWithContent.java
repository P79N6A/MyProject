package com.sankuai.meituan.config.model;

import java.util.Date;

/**
 * Created by liangchen on 2017/9/8.
 */
public class OperationFileLogWithContent extends OperationFileLog{
    private String oldFileContent;
    private String newFileContent;


    public String getOldFileContent() {
        return oldFileContent;
    }


    public void setOldFileContent(String oldFileContent) {
        this.oldFileContent = oldFileContent;
    }


    public String getNewFileContent() {
        return newFileContent;
    }


    public void setNewFileContent(String newFileContent) {
        this.newFileContent = newFileContent;
    }
}
