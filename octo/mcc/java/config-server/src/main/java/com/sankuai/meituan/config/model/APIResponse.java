package com.sankuai.meituan.config.model;

public class APIResponse {

    public static final String RESPONSE_STATUS_SUCCESS = "success";
    public static final String RESPONSE_STATUS_FAILURE = "failure";
    private String status;

    private String msg;

    private Object data;

    public APIResponse(String status, Object data){
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public APIResponse withErrorMessage(String msg){
        this.msg = msg;
        return this;
    }

    public static APIResponse newResponse(boolean success, Object data) {

        return new APIResponse(success ? RESPONSE_STATUS_SUCCESS : RESPONSE_STATUS_FAILURE, data);
    }

    public static APIResponse newResponse( Object data) {

        return newResponse(true, data);
    }

    public static APIResponse newResponse( boolean success) {

        return newResponse(success, null);
    }

    public boolean isSuccess() {
        return RESPONSE_STATUS_SUCCESS.equals(this.status);
    }
}
