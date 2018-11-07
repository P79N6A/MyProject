package com.sankuai.octo.msgp.domain;

/**
 * Created by zava on 16/5/30.
 */
public class UserSubscribeMonitor {

    private String appkey;
    private Long triggerId;
    private String mode;
    private int newStatus = 0;
    private Integer userId;
    private String userLogin;
    private String userName;

    public UserSubscribeMonitor() {

    }

    public UserSubscribeMonitor(String appkey, Long triggerId, String mode, int newStatus, Integer userId, String userLogin, String userName) {
        this.appkey = appkey;
        this.triggerId = triggerId;
        this.mode = mode;
        this.newStatus = newStatus;
        this.userId = userId;
        this.userLogin = userLogin;
        this.userName = userName;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(int newStatus) {
        this.newStatus = newStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String toString() {
        return new StringBuffer().append("UserSubscribeMonitor appkey=")
                .append(appkey)
                .append(" userLogin=")
                .append(userLogin)
                .append(" mode=")
                .append(mode)
                .append(" newStatus=")
                .append(newStatus)
                .toString();
    }

}
