package com.sankuai.octo.msgp.model;

/**
 *EmployeeInfo用于保存从EmpSimpleVo中获取到的id和name，EmpSimpleVo.name赋给EmployeeInfo中的id和text，EmpSimpleVo.id赋给idNum
 *select2默认存储的是id,而我们需要存储的是EmpSimpleVo中的name，所以在这里转换变量名
 */
public class EmployeeInfo {
    private String id;
    private String text;
    private Integer idNum;

    public Integer getIdNum() {
        return idNum;
    }

    public void setIdNum(Integer idNum) {
        this.idNum = idNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
