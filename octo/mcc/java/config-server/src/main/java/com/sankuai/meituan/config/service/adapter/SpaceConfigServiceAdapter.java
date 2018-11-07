package com.sankuai.meituan.config.service.adapter;

import com.sankuai.meituan.borp.vo.Action;
import com.sankuai.meituan.borp.vo.ActionType;
import com.sankuai.meituan.borp.vo.Detail;
import com.sankuai.meituan.config.constant.EntityType;
import com.sankuai.meituan.config.model.ConfigNode;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.service.OperationRecordService;
import com.sankuai.meituan.config.service.SpaceConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class SpaceConfigServiceAdapter extends SpaceConfigService {
    @Resource
    private OperationRecordService operationRecordService;

    @Override
    protected void create(String spacePath) {
        super.create(spacePath);
        operationRecordService.createAction(ActionType.INSERT, EntityType.CREATE_SPACE_CONFIG.toString(), spacePath);
    }

    @Override
    protected void setData(String spacePath, ConfigNode configNode, int version) throws Exception {
        Map<String, String> oriData = super.getAll(spacePath);
        super.setData(spacePath, configNode, version);
        Action action = operationRecordService.createAction(ActionType.INSERT, EntityType.UPDATE_SPACE_CONFIG.toString(), spacePath);
        for (PropertyValue propertyValue : configNode.getData()) {
            if (oriData.containsKey(propertyValue.getKey())) {
                if (! oriData.get(propertyValue.getKey()).equals(propertyValue.getValue())) {
                    Detail detail = operationRecordService.createDetail(action);
                    detail.setFieldName(propertyValue.getKey());
                    detail.setNewValue(propertyValue.getValue());
                    detail.setOldValue(oriData.get(propertyValue.getKey()));
                }
            } else {
                Detail detail = operationRecordService.createDetail(action);
                detail.setFieldName(propertyValue.getKey());
                detail.setNewValue(propertyValue.getValue());
            }
        }
    }

    @Override
    public void delete(String spacePath) {
        super.delete(spacePath);
        operationRecordService.createAction(ActionType.DELETE, EntityType.DELETE_SPACE_CONFIG.toString(), spacePath);
    }
}
