package com.sankuai.meituan.config.service.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.sankuai.meituan.borp.vo.Action;
import com.sankuai.meituan.borp.vo.ActionType;
import com.sankuai.meituan.borp.vo.Detail;
import com.sankuai.meituan.config.constant.EntityType;
import com.sankuai.meituan.config.interceptorfilter.AbstractOperationRecordInterceptor;
import com.sankuai.meituan.config.mapper.ConfigRollbackMapper;
import com.sankuai.meituan.config.model.Env;
import com.sankuai.meituan.config.model.Operator;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.service.ConfigNodeService;
import com.sankuai.meituan.config.service.OperationRecordService;
import com.sankuai.meituan.config.service.SpaceConfigService;
import com.sankuai.meituan.config.service.Xm;
import com.sankuai.meituan.config.util.Common;
import com.sankuai.meituan.config.util.HttpUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.sankuai.meituan.config.constant.ParamName.ALL_ENV;
import static com.sankuai.meituan.config.model.Setting.ENABLE_XM_ALERT;

@Service
public class ConfigNodeServiceAdapter extends ConfigNodeService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigNodeServiceAdapter.class);

    private String octoAppkeyDescUrlPre = Common.isOnline() ? "http://octo.sankuai.com/api/service?appkey=" : "http://octo.test.sankuai.info/api/service?appkey=";

    @Resource
    private OperationRecordService operationRecordService;

    @Resource
    private SpaceConfigService spaceConfigService;

    @Resource
    private ConfigRollbackMapper configRollbackMapper;

    @Override
    public void add(String spacePath) {
        super.add(spacePath);
        Action action = operationRecordService.createAction(ActionType.INSERT, EntityType.ADD_SPACE.toString(), spacePath);
        if (super.isAddRoot(spacePath)) {
            for (String env : ALL_ENV) {
                Detail detail = operationRecordService.createDetail(action);
                detail.setFieldName("env");
                detail.setNewValue(env);
            }
        }
        operationRecordService.addAction(action);
    }

    @Override
    public boolean delete(String spacePath) {
        if (super.delete(spacePath)) {
            operationRecordService.createAction(ActionType.DELETE, EntityType.DELETE_SPACE.toString(), spacePath);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset(String spacePath, Collection<PropertyValue> propertyValues, int version) throws Exception{
        Stat status = new Stat();
        Map<String, PropertyValue> oriDataByKey = super.getDataMap(spacePath, status);
        super.reset(spacePath, propertyValues, version);
        handleNewVersion(spacePath);

        Map<String, PropertyValue> newDataByKey = Maps.uniqueIndex(propertyValues, new Function<PropertyValue, String>() {
            @Override
            public String apply(PropertyValue input) {
                return input.getKey();
            }
        });

        MapDifference<String, PropertyValue> difference = Maps.difference(oriDataByKey, newDataByKey);
        Map<String, MapDifference.ValueDifference<PropertyValue>> updateMap = difference.entriesDiffering();
        Map<String, PropertyValue> delMap = difference.entriesOnlyOnLeft();
        Map<String, PropertyValue> addMap = difference.entriesOnlyOnRight();

        handleUpdateData(spacePath, updateMap);
        handleDelete(spacePath, delMap);
        handleNew(spacePath, addMap);

        xmAlert(spacePath, updateMap, delMap, addMap);
    }



    private void xmAlert(String spacePath,
                         Map<String, MapDifference.ValueDifference<PropertyValue>> updateMap,
                         Map<String, PropertyValue> delMap,
                         Map<String, PropertyValue> addMap) {
        if (!spaceConfigService.getBoolSetting(spacePath,ENABLE_XM_ALERT,true)) {
            return;
        }
        String[] spacePathArr = spacePath.split("/");

        if (spacePathArr.length >= 2) {
            String appkey = "";
            int appkeyIndex = 0;
            for (int i = 0; i < spacePathArr.length; ++i) {
                if (StringUtils.isNotEmpty(spacePathArr[i])) {
                    appkey = spacePathArr[i];
                    appkeyIndex = i;
                    break;
                }
            }
            int envIndex = appkeyIndex + 1;
            if (envIndex < spacePathArr.length && ALL_ENV.contains(spacePathArr[envIndex])) {
                List<String> users = getAppkeyOwners(appkey);
                if (!users.isEmpty() && ((!addMap.isEmpty()) || (!delMap.isEmpty()) || (!updateMap.isEmpty()))) {
                    StringBuilder sb = new StringBuilder();
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sb.append(format.format(date))
                            .append("\nMCC配置变更提醒(")
                            .append(Common.isOnline() ? "线上" : "线下")
                            .append(")\nAppkey:")
                            .append("[")
                            .append(appkey)
                            .append("|");
                    String domain = Common.getOctoUrl();

                    sb.append(domain)
                            .append("serverOpt/operation?appkey=")
                            .append(appkey)
                            .append("#config")
                            .append("]")
                            .append("\nenv:")
                            .append(Env.correctShowEnv(spacePathArr[envIndex]));
                    Operator operator = AbstractOperationRecordInterceptor.operator.get();

                    if (null != operator) {
                        String operatorName = operator.getName();
                        sb.append("\n操作人:")
                                .append(StringUtils.isEmpty(operatorName) ? "sgAgent" : operatorName);
                    }
                    if (!addMap.isEmpty()) {
                        sb.append("\n【新增配置】");
                        for (PropertyValue entry : addMap.values()) {
                            handleAlertContent(sb, entry);
                        }
                    }
                    if (!delMap.isEmpty()) {
                        sb.append("\n【删除配置】");
                        for (PropertyValue entry : delMap.values()) {
                            handleAlertContent(sb, entry);
                        }
                    }

                    if (!updateMap.isEmpty()) {
                        sb.append("\n【修改配置】");
                        for (MapDifference.ValueDifference<PropertyValue> updateData : updateMap.values()) {
                            sb.append("\n变更前:");
                            handleAlertContent(sb, updateData.leftValue());
                            sb.append("\n变更后:");
                            handleAlertContent(sb, updateData.rightValue());
                        }
                    }
                    sendXM(users, sb.toString());
                }
            }

        }
    }


    private void sendXM(List<String> users, String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        try {
            String tempStr = msg;
            while (tempStr.length() > 2000) {
                Xm.send(users, tempStr.substring(0, 2000));
                tempStr = tempStr.substring(2000);
            }
            Xm.send(users, tempStr);

        } catch (Exception e) {
            LOG.warn("Failed to send xm message.", e);
        }
    }

    private void handleAlertContent(StringBuilder sb, PropertyValue entry) {

        sb.append("\n#");
        String comment = entry.getComment();
        sb.append(null == comment ? "" : comment);
        sb.append("\n");
        sb.append(entry.getKey());
        sb.append(" = ");
        String value = entry.getValue();
        if (StringUtils.isNotEmpty(value) && value.length() > 150) {
            value = value.substring(0, 150) + "...";
        }
        sb.append(null == value ? "" : value);
    }



    private List<String> getAppkeyOwners(String appkey) {
        List<String> userEmails = new ArrayList<String>();
        String url = octoAppkeyDescUrlPre + appkey;
        try {
            String appkeyDesc = HttpUtil.get(url);
            JSONObject jsonObject = JSON.parseObject(appkeyDesc);
            JSONObject dataJson= jsonObject.getJSONObject("data");
            JSONArray array = dataJson.getJSONArray("owners");
            if(StringUtils.equalsIgnoreCase(dataJson.getString("owt"),"waimai")){
                //digger is public xm account.
                userEmails.add("digger@meituan.com");
            }

            for (int i = 0; i < array.size(); ++i) {
                userEmails.add(array.getJSONObject(i).get("login").toString() + "@meituan.com");
            }
        } catch (Exception e) {
            LOG.warn("failed to get owners from octo url={}", url);
        }

        return userEmails;
    }

    private void handleNewVersion(String spacePath) {
        Stat stat = super.getPathDirectStat(spacePath);
        Action action = operationRecordService.createAction(ActionType.UPDATE, EntityType.UPDATE_CONFIG_VERSION.toString(), spacePath);
        Detail versionDetail = operationRecordService.createDetail(action);
        versionDetail.setFieldName("version");
        versionDetail.setNewValue(Long.valueOf(stat.getMzxid()).toString());
    }

    private void handleNew(String spacePath, Map<String, PropertyValue> added) {
        for (PropertyValue newData : added.values()) {
            Action action = operationRecordService.createAction(ActionType.UPDATE, EntityType.ADD_CONFIG.toString(), spacePath);
            Detail keyDetail = operationRecordService.createDetail(action);
            keyDetail.setFieldName("key");
            keyDetail.setNewValue(newData.getKey());
            Detail valueDetail = operationRecordService.createDetail(action);
            valueDetail.setFieldName("value");
            valueDetail.setNewValue(newData.getValue());
            if (StringUtils.isNotEmpty(newData.getComment())) {
                Detail commentDetail = operationRecordService.createDetail(action);
                commentDetail.setFieldName("comment");
                commentDetail.setNewValue(newData.getComment());
            }

        }
    }

    private void handleDelete(String spacePath, Map<String, PropertyValue> deleted) {
        for (PropertyValue deleteData : deleted.values()) {
            Action action = operationRecordService.createAction(ActionType.UPDATE, EntityType.DELETE_CONFIG.toString(), spacePath);
            Detail keyDetail = operationRecordService.createDetail(action);
            keyDetail.setFieldName("key");
            keyDetail.setOldValue(deleteData.getKey());

            if (StringUtils.isNotEmpty(deleteData.getValue())) {
                Detail valueDetail = operationRecordService.createDetail(action);
                valueDetail.setFieldName("value");
                valueDetail.setOldValue(deleteData.getValue());
            }

            if (StringUtils.isNotEmpty(deleteData.getComment())) {
                Detail commentDetail = operationRecordService.createDetail(action);
                commentDetail.setFieldName("comment");
                commentDetail.setOldValue(deleteData.getComment());
            }
        }
    }

    private void handleUpdateData(String spacePath, Map<String, MapDifference.ValueDifference<PropertyValue>> difference) {
        for (MapDifference.ValueDifference<PropertyValue> updateData : difference.values()) {
            Action action = operationRecordService.createAction(ActionType.UPDATE, EntityType.UPDATE_CONFIG.toString(), spacePath);
            Detail keyDetail = operationRecordService.createDetail(action);
            keyDetail.setFieldName("key");
            keyDetail.setOldValue(updateData.leftValue().getKey());
            keyDetail.setNewValue(updateData.rightValue().getKey());
            boolean isValueChanged = !StringUtils.equals(updateData.leftValue().getValue(), updateData.rightValue().getValue());
            boolean isCommentChanged = !updateData.leftValue().isCommentEqual(updateData.rightValue());
            if (isValueChanged || isCommentChanged) {
                Detail valueDetail = operationRecordService.createDetail(action);
                valueDetail.setFieldName("value");

                String oldValue = updateData.leftValue().getValue();
                String newValue = updateData.rightValue().getValue();

                oldValue = (null == oldValue) ? "" : oldValue;
                newValue = (null == newValue) ? "" : newValue;

                valueDetail.setOldValue(oldValue);
                valueDetail.setNewValue(newValue);

                Detail commentDetail = operationRecordService.createDetail(action);
                commentDetail.setFieldName("comment");


                String oldComment = updateData.leftValue().getComment();
                String newComment = updateData.rightValue().getComment();

                oldComment = (null == oldComment) ? "" : oldComment;
                newComment = (null == newComment) ? "" : newComment;
                commentDetail.setOldValue(oldComment);
                commentDetail.setNewValue(newComment);
            }
        }
    }
}
