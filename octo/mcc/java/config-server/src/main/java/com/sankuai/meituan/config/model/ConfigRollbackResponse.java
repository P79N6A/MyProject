package com.sankuai.meituan.config.model;

import java.util.Collection;

/**
 * Created by liangchen on 2017/9/18.
 */
public class ConfigRollbackResponse {
    private int ret;//500：失败  200 ：成功且配置有变化 304：成功且配置无变化
    private boolean enableRollback;//除本次回滚外，是否还有可回滚的配置
    Collection<? extends PropertyValue> data;//回滚的配置
    Collection<? extends PropertyValue> addData;//回滚会增加的配置
    Collection<? extends PropertyValue> deleteData;//回滚会删除的配置
    Collection<? extends PropertyValue> updateData;//回滚会更新的配置

    public boolean isEnableRollback() {
        return enableRollback;
    }

    public void setEnableRollback(boolean enableRollback) {
        this.enableRollback = enableRollback;
    }

    public Collection<? extends PropertyValue> getAddData() {
        return addData;
    }

    public void setAddData(Collection<? extends PropertyValue> addData) {
        this.addData = addData;
    }

    public Collection<? extends PropertyValue> getDeleteData() {
        return deleteData;
    }

    public void setDeleteData(Collection<? extends PropertyValue> deleteData) {
        this.deleteData = deleteData;
    }

    public Collection<? extends PropertyValue> getUpdateData() {
        return updateData;
    }

    public void setUpdateData(Collection<? extends PropertyValue> updateData) {
        this.updateData = updateData;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public Collection<? extends PropertyValue> getData() {
        return data;
    }

    public void setData(Collection<? extends PropertyValue> data) {
        this.data = data;
    }

}
