package com.sankuai.meituan.config.service;

import com.sankuai.meituan.borp.BorpService;
import com.sankuai.meituan.borp.vo.Action;
import com.sankuai.meituan.borp.vo.ActionType;
import com.sankuai.meituan.borp.vo.Detail;
import com.sankuai.meituan.config.constant.EntityType;
import com.sankuai.octo.config.model.ConfigGroup;
import com.sankuai.octo.config.model.FilelogRequest;
import com.sankuai.octo.config.service.MtConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by liangchen on 2017/7/27.
 */
@Service
public class FilelogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilelogService.class);

    @Resource
    private OperationRecordService operationRecordService;

    @Resource
    private MtConfigService.Iface mtConfigService;

    @Resource
    private BorpService borpService;



    public void createFilelog(FilelogRequest filelogRequest, String actionId, byte[] oldFileContent, byte[] newFileContent) throws TException {
        String entityType = genEntityType(filelogRequest.type);
        // 保存到borp
        Action action = operationRecordService.createAction(ActionType.UPDATE, entityType, actionId);
        action.setOperatorName(filelogRequest.userName);
        List<Detail> details = new ArrayList<>();
        Detail groupDetail = operationRecordService.createDetail(action);
        groupDetail.setFieldName("groupname");
        ConfigGroup group = mtConfigService.getGroupInfo(filelogRequest.appkey, filelogRequest.env, filelogRequest.groupId).getGroup();
        String groupname = (null == group) ? "未知分组" : group.getName();
        groupDetail.setNewValue(groupname);
        details.add(groupDetail);

        Detail fileDetail = operationRecordService.createDetail(action);
        fileDetail.setFieldName("filename");
        fileDetail.setNewValue(filelogRequest.getFilename());
        details.add(fileDetail);

        if (StringUtils.equals(EntityType.FILE_DISTRIBUTE.toString(), entityType)){
            if (null != filelogRequest.successList && !filelogRequest.successList.isEmpty()) {
                Detail successDetail = operationRecordService.createDetail(action);
                successDetail.setFieldName("dSuccessList");
                successDetail.setNewValue(StringUtils.join(filelogRequest.successList.toArray(), "; "));
                details.add(successDetail);
            }
            if (null != filelogRequest.dErrList && !filelogRequest.dErrList.isEmpty()) {
                Detail dErrDetail = operationRecordService.createDetail(action);
                dErrDetail.setFieldName("dErrList");
                dErrDetail.setNewValue(StringUtils.join(filelogRequest.dErrList.toArray(), "; "));
                details.add(dErrDetail);
            }

            if (null != filelogRequest.eErrList && !filelogRequest.eErrList.isEmpty()) {
                Detail eErrDetail = operationRecordService.createDetail(action);
                eErrDetail.setFieldName("eErrList");
                eErrDetail.setNewValue(StringUtils.join(filelogRequest.eErrList.toArray(), "; "));
                details.add(eErrDetail);
            }
        } else if (StringUtils.equals(EntityType.FILE_UPDATE.toString(), entityType)) {
            Detail fileContentDetail = operationRecordService.createDetail(action);
            fileContentDetail.setFieldName("fileContent");
            fileContentDetail.setOldValue(new String(oldFileContent));
            fileContentDetail.setNewValue(new String(newFileContent));
            details.add(fileContentDetail);
        } else if (StringUtils.equals(EntityType.FILE_DELETE.toString(), entityType)) {
            Detail fileContentDetail = operationRecordService.createDetail(action);
            fileContentDetail.setFieldName("oldfileContent");
            fileContentDetail.setOldValue(new String(oldFileContent));
            details.add(fileContentDetail);
        }

        action.setDetails(details);
        borpService.save(action);
        LOGGER.info("action saved");
    }

    private String genEntityType(String type){
        String entityType = "";
        switch (type) {
            case "FILE_DISTRIBUTE":
                entityType = EntityType.FILE_DISTRIBUTE.toString();
                break;
            case "FILE_UPDATE":
                entityType = EntityType.FILE_UPDATE.toString();
                break;
            case "FILE_DELETE":
                entityType = EntityType.FILE_DELETE.toString();
                break;
            case "FILE_ADD":
                entityType = EntityType.FILE_ADD.toString();
                break;
        }
        return entityType;
    }
}
