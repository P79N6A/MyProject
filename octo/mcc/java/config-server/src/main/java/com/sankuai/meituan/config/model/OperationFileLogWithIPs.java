package com.sankuai.meituan.config.model;

/**
 * Created by liangchen on 2017/9/8.
 */
public class OperationFileLogWithIPs extends OperationFileLog {
    private String dSuccessList;
    private String dErrorList;
    private String eErrorList;

    public String getdSuccessList() {
        return dSuccessList;
    }

    public String getdErrorList() {
        return dErrorList;
    }

    public String geteErrorList() {
        return eErrorList;
    }



    public void setdSuccessList(String dSuccessList) {
        this.dSuccessList = dSuccessList;
    }

    public void setdErrorList(String dErrorList) {
        this.dErrorList = dErrorList;
    }

    public void seteErrorList(String eErrorList) {
        this.eErrorList = eErrorList;
    }
}
