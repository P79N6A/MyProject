package com.sankuai.octo.mworth.common.model;



import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 价值事件
 */
public class WorthEvent {

    private String project;
    private String model;

    private String functionName;
    private String functionDesc;
    //操作源类型
    private OperationSourceType operationSourceType = OperationSourceType.HUMAN;
    //操作源, 机器/人
    private String operationSource;
    //目标appkey
    private String targetAppkey;

    private int business = -1;


    private String appkeyOwt;
    //签名,主要是用于有开关状态的服务
    private String signid;
    //创建时间
    private Long startTime;
    //结束时间
    private Long endTime;
    //创建时间
    private Date createTime = new Date();


    public WorthEvent(){

    }
    public WorthEvent(String project,String model,String functionDesc,String functionName){
        this.project = project;
        this.model = model;
        this.functionDesc = functionDesc;
        this.functionName = functionName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFunctionDesc() {
        return functionDesc;
    }

    public void setFunctionDesc(String functionDesc) {
        this.functionDesc = functionDesc;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public OperationSourceType getOperationSourceType() {
        return operationSourceType;
    }

    public void setOperationSourceType(OperationSourceType operationSourceType) {
        this.operationSourceType = operationSourceType;
    }

    public String getOperationSource() {
        return operationSource;
    }

    public void setOperationSource(String operationSource) {
        this.operationSource = operationSource;
    }

    public String getTargetAppkey() {
        return targetAppkey;
    }

    public void setTargetAppkey(String targetAppkey) {
        this.targetAppkey = targetAppkey;
    }

    public String getSignid() {
        return signid;
    }

    public void setSignid(String signid) {
        this.signid = signid;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public int getBusiness() {
        return business;
    }

    public void setBusiness(int business) {
        this.business = business;
    }
    public String getAppkeyOwt() {
        return appkeyOwt;
    }

    public void setAppkeyOwt(String appkeyOwt) {
        this.appkeyOwt = appkeyOwt;
    }


}