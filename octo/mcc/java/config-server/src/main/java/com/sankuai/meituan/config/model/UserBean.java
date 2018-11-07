package com.sankuai.meituan.config.model;

import com.sankuai.meituan.org.remote.vo.EmployeeInfo;
import com.sankuai.meituan.org.remote.vo.OrgTreeNodeVo;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-7
 */
public class UserBean {
    private Integer id;
    private String name;
    private String login;
    private String mobile;

    public UserBean() {
    }

    public UserBean(OrgTreeNodeVo node) {
        this.id = node.getDataId();
        this.name = node.getName();
        this.login = node.getEnName();
    }

    public UserBean(EmployeeInfo employeeInfo) {
        this.setId(employeeInfo.getId());
        this.setName(employeeInfo.getName());
        this.setLogin(employeeInfo.getLogin());
        this.setMobile(employeeInfo.getMobile());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
