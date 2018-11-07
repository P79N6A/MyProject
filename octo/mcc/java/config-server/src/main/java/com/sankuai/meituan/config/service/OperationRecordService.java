package com.sankuai.meituan.config.service;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.sankuai.meituan.borp.BorpService;
import com.sankuai.meituan.borp.vo.*;
import com.sankuai.meituan.borp.vo.Page;
import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.constant.EntityType;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;

@Service
public class OperationRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationRecordService.class);

    private static final ThreadLocal<Operation> currentOperation = new ThreadLocal<>();

    private static final List<String> defaultGetActionTypes = new ArrayList<String>();

    static {
        defaultGetActionTypes.add("ADD_CONFIG");
        defaultGetActionTypes.add("DELETE_CONFIG");
        defaultGetActionTypes.add("UPDATE_CONFIG");
    }

    @Resource
    private BorpService borpService;

    @Resource
    private ConfigNodeService configNodeService;

    public void createOperation(OperationRecord operationRecord, Operator operator) {
        if (null == operator) return;
        if (currentOperation.get() != null) {
            LOGGER.error("有Operation没有清空:{}", currentOperation.get());
        }
        LOGGER.debug("当前请求操作者id:{},type:{}", operator.getId(), operator.getType());
        Operation operation = new Operation();
        operation.setOperationId(UUID.randomUUID().toString());
        operation.setOperationType(operationRecord.type());
        operation.setOperationDesc(operationRecord.desc());

        operation.setOperatorId(operator.getId());
        operation.setOperatorType(operator.getType().getIndex());
        operation.setOperatorName(operator.getName());

        operation.setActions(Lists.<Action>newArrayList());
        currentOperation.set(operation);
    }

    private Action createAction(ActionType actionType) {
        Operation operation = getCurrentOperation();
        Action action = new Action();
        action.setActionId(UUID.randomUUID().toString());
        action.setActionType(actionType.getIndex());
        action.setOperationId(operation.getOperationId());
        action.setOperationType(operation.getOperationType());

        action.setOperatorId(operation.getOperatorId());
        action.setOperatorType(operation.getOperatorType());
        action.setOperatorName(operation.getOperatorName());

        action.setAddTime(new Date());
        action.setDetails(Lists.<Detail>newArrayList());

        operation.getActions().add(action);
        return action;
    }

    public Action createAction(ActionType actionType, String entityType, String entityId) {
        Action action = createAction(actionType);
        action.setEntityType(entityType);
        action.setEntityId(entityId);
        return action;
    }

    public Action createAction(ActionType actionType, Class<?> entityClass, String entityId) {
        return this.createAction(actionType, entityClass.getName(), entityId);
    }

    public Detail createDetail(Action action) {
        Detail detail = new Detail();
        detail.setDetailId(UUID.randomUUID().toString());
        detail.setActionId(action.getActionId());
        detail.setEntityId(action.getEntityId());
        detail.setEntityType(action.getEntityType());
        detail.setAddTime(new Date());
        action.getDetails().add(detail);
        return detail;
    }

    public Action createInsertAction(final Object insertObject, Integer objectId) {
        return createInsertAction(insertObject, objectId.toString());
    }

    public Action createInsertAction(final Object insertObject, String objectId) {
        final Action action = createAction(ActionType.INSERT, insertObject.getClass(), objectId);

        ReflectionUtils.doWithFields(insertObject.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                field.setAccessible(true);
                if (isBaseType(field)) {
                    Detail detail = createDetail(action);
                    detail.setFieldName(field.getName());
                    Object value = field.get(insertObject);
                    detail.setNewValue(value != null ? value.toString() : null);
                    action.getDetails().add(detail);
                }
            }
        });
        return action;
    }

    public <T> Action createSelectiveUpdateAction(final T oriObject, final T updateObject, Integer objectId) {
        return createSelectiveUpdateAction(oriObject, updateObject, objectId.toString());
    }

    public <T> Action createSelectiveUpdateAction(final T oriObject, final T updateObject, String objectId) {
        final Action action = createAction(ActionType.UPDATE, oriObject.getClass(), objectId);

        ReflectionUtils.doWithFields(updateObject.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                field.setAccessible(true);
                if (isBaseType(field)) {
                    Object newValue = field.get(updateObject);
                    Object oldValue = field.get(oriObject);
                    if (newValue != null && !newValue.equals(oldValue)) {
                        Detail detail = createDetail(action);
                        detail.setFieldName(field.getName());
                        detail.setOldValue(oldValue != null ? oldValue.toString() : null);
                        detail.setNewValue(newValue.toString());
                        action.getDetails().add(detail);
                    }
                }
            }
        });
        return action;
    }

    public Action createDeleteAction(final Object insertObject, Integer objectId) {
        return createDeleteAction(insertObject, objectId.toString());
    }

    public Action createDeleteAction(final Object insertObject, String objectId) {
        final Action action = createAction(ActionType.DELETE, insertObject.getClass(), objectId);

        ReflectionUtils.doWithFields(insertObject.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                field.setAccessible(true);
                if (isBaseType(field)) {
                    Detail detail = createDetail(action);
                    detail.setFieldName(field.getName());
                    Object value = field.get(insertObject);
                    detail.setOldValue(value != null ? value.toString() : null);
                    action.getDetails().add(detail);
                }
            }
        });
        return action;
    }

    public <T> Action createSelectiveDeleteAction(final T oriObject, final T deleteObject, Integer objectId) {
        return createSelectiveUpdateAction(oriObject, deleteObject, objectId.toString());
    }

    public <T> Action createSelectiveDeleteAction(final T oriObject, final T deleteObject, String objectId) {
        final Action action = createAction(ActionType.DELETE, oriObject.getClass(), objectId);

        ReflectionUtils.doWithFields(deleteObject.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                field.setAccessible(true);
                if (isBaseType(field)) {
                    Object newValue = field.get(deleteObject);
                    Object oldValue = field.get(oriObject);
                    if (newValue != null && !newValue.equals(oldValue)) {
                        Detail detail = createDetail(action);
                        detail.setFieldName(field.getName());
                        detail.setOldValue(oldValue != null ? oldValue.toString() : null);
                        detail.setNewValue(newValue.toString());
                        action.getDetails().add(detail);
                    }
                }
            }
        });
        return action;
    }

    public boolean isBaseType(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            return true;
        } else if (String.class.isAssignableFrom(fieldType)) {
            return true;
        } else if (Number.class.isAssignableFrom(fieldType)) {
            return true;
        } else if (Date.class.isAssignableFrom(fieldType)) {
            return true;
        } else if (Boolean.class.isAssignableFrom(fieldType)) {
            return true;
        } else if (Character.class.isAssignableFrom(fieldType)) {
            return true;
        }
        return false;
    }

    private Operation getCurrentOperation() {
        Operation operation = currentOperation.get();
        if (operation == null) {
            LOGGER.warn("当前线程没有开启Operation,如果需要记录操作历史,需要先调用createOperation,或者添加OperationRecord(参考OperationRecordInterceptor)");
            operation = new Operation();
            operation.setActions(Lists.<Action>newArrayList());
        }
        return operation;
    }

    public void addAction(Action action) {
        getCurrentOperation().getActions().add(action);
    }

    public void addActions(Collection<Action> actions) {
        getCurrentOperation().getActions().addAll(actions);
    }

    public void removeLastAction() {
        List<Action> actions = getCurrentOperation().getActions();
        int lastIndex = actions.size() - 1;
        if (lastIndex > 0) {
            actions.remove(lastIndex);
        }
    }

    public void sendOperation() {
        Operation operation = currentOperation.get();
        if (operation != null) {
            try {
                if (CollectionUtils.isNotEmpty(operation.getActions())) {
                    borpService.saveAsyn(operation);
                    LOGGER.debug("保存的OperationId:{}", operation.getOperationId());
                } else {
                    LOGGER.warn("当前线程没有具体Action记录!");
                }
            } catch (Exception e) {
                LOGGER.error(MessageFormat.format("保存操作记录失败,保存内容为:{0}", ToStringBuilder.reflectionToString(operation)), e);
            } finally {
                currentOperation.remove();
            }
        } else {
            LOGGER.warn("当前线程没有开启操作记录");
        }
    }

    /**
     * 批量查询，将/appkey/env下的所有子节点转换成entityId
     * @param spacePath
     * @return
     */
    public List<String> spacePath2EntityId(String spacePath) {
        List<String> entityIds = new ArrayList<String>();
        int baseIndex = ParamName.CONFIG_BASE_PATH.length();
        for (String path: configNodeService.getAllChildrenPath(spacePath)) {
            entityIds.add(path.substring(baseIndex));
        }
        return entityIds;
    }

    public Collection<OperationLog> getOperationRecord(List<String> entityIds, Date startTime, Date endTime, com.sankuai.meituan.config.model.Page page) {
        BorpResponse<Action> borpResponse = borpService.getBorpResponse(
                BorpRequest.builder()
                        .mustIn("entityId", entityIds)
                        .mustIn("entityType", defaultGetActionTypes)
                        .beginDate(startTime)
                        .endDate(endTime)
                        .from(page.getStart())
                        .size(page.getPageSize())
                        .build(), Action.class);
        if (null == borpResponse) {
            return Collections.EMPTY_LIST;
        }
        List<Action> actions = borpResponse.getResult();
        Page borpResponsePage = borpResponse.getPage();
        page.setTotalCount(borpResponsePage.getTotalCount());

        Set<String> actionIds = Sets.newHashSet(Iterables.transform(actions, new Function<Action, String>() {
            @Override
            public String apply(Action input) {
                return input.getActionId();
            }
        }));
        List<Detail> details = borpService.getByBorpRequest(BorpRequest.builder()
                .mustIn("actionId", actionIds)
                .build(), Detail.class);
        return borp2OperatorLog(actions, details);

    }

    private Collection<OperationLog> borp2OperatorLog(List<Action> actions, List<Detail> details) {
        Multimap<String, Detail> detailByActionId = ArrayListMultimap.create(Multimaps.index(details, new Function<Detail, String>() {
            @Override
            public String apply(Detail input) {
                return input.getActionId();
            }
        }));

        Collection<OperationLog> logs = Lists.newArrayList();
        for (Action action : actions) {
            try {
                EntityType operationType = EntityType.valueOf(action.getEntityType());
                OperationLog actionLog = new OperationLog();
                actionLog.setPath(action.getEntityId());
                actionLog.setOperator(action.getOperatorName());
                actionLog.setTime(action.getAddTime());
                actionLog.setType(operationType.getName());
                Collection<Detail> curDetails = detailByActionId.get(action.getActionId());
                switch (operationType) {
                    case ADD_CONFIG:
                        actionLog.setDetail(operationDetail2StrForAddDelConfig(curDetails, false, actionLog));
                        actionLog.setTypeInt(1);
                        logs.add(actionLog);
                        break;
                    case DELETE_CONFIG:
                        actionLog.setDetail(operationDetail2StrForAddDelConfig(curDetails, true, actionLog));
                        logs.add(actionLog);
                        actionLog.setTypeInt(2);
                        break;
                    case UPDATE_CONFIG:
                        actionLog.setDetail(operationDetail2StrForUpdateConfig(curDetails, actionLog));
                        actionLog.setTypeInt(3);
                        logs.add(actionLog);
                        break;
                    default:

                        break;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }

        }
        return logs;
    }

    private String operationDetail2StrForUpdateConfig(Collection<Detail> curDetails, OperationLog actionLog) {
        String key = "";
        String oldValue = "";
        String oldComment = "";
        String newValue = "";
        String newComment = "";
        for (Detail item : curDetails) {
            String filed = item.getFieldName();
            if ("key".equalsIgnoreCase(filed)) {
                key = item.getOldValue();
            } else if ("value".equalsIgnoreCase(filed)) {
                oldValue = item.getOldValue();
                newValue = item.getNewValue();
            } else if ("comment".equalsIgnoreCase(filed)) {
                oldComment = item.getOldValue();
                newComment = item.getNewValue();
            }
        }
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("修改前: ");
        strBuf.append(key);
        actionLog.setKey(key);
        actionLog.setOldValue(oldValue);
        actionLog.setOldComment(oldComment);
        enrichStr(strBuf, oldValue, false);
        enrichStr(strBuf, oldComment, true);
        strBuf.append("\n修改后: ");
        strBuf.append(key);
        enrichStr(strBuf, newValue, false);
        enrichStr(strBuf, newComment, true);
        actionLog.setNewValue(newValue);
        actionLog.setNewComment(newComment);
        return strBuf.toString();
    }

    private void enrichStr(StringBuffer strBuf, String str, boolean isComment) {
        if (!"".equals(str)) {
            strBuf.append(isComment ? " #" : " = ");
            strBuf.append(str);
        }
    }

    private String operationDetail2StrForAddDelConfig(Collection<Detail> curDetails, boolean isOldVale, OperationLog actionLog) {
        String key = "";
        String value = "";
        String comment = "";
        for (Detail item : curDetails) {
            String filed = item.getFieldName();
            String curValue = isOldVale ? item.getOldValue() : item.getNewValue();
            if ("key".equalsIgnoreCase(filed)) {
                key = curValue;
            } else if ("value".equalsIgnoreCase(filed)) {
                value = curValue;
            } else if ("comment".equalsIgnoreCase(filed)) {
                comment = curValue;
            }
        }

        actionLog.setKey(key);
        actionLog.setNewValue(value);
        actionLog.setNewComment(comment);
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(key);
        if (!"".equals(value)) {
            strBuf.append(" = ");
            strBuf.append(value);
        }

        if (!"".equals(comment)) {
            strBuf.append(" #");
            strBuf.append(comment);
        }
        return strBuf.toString();
    }

    /*操作记录*/
    public Collection<OperationFileLog> getOperationFileRecord(String spacePath, Date startTime, Date endTime, com.sankuai.meituan.config.model.Page page) {
        //最初保存的一部分文件下发日志是ADD_CONFIG类型的，后面统一改为FILE_DISTRIBUTE
        List<String> entityTypes = new ArrayList<String>();
        entityTypes.add("ADD_CONFIG");
        entityTypes.add("FILE_DISTRIBUTE");
        entityTypes.add("FILE_UPDATE");
        entityTypes.add("FILE_DELETE");
        entityTypes.add("FILE_ADD");
        BorpResponse<Action> borpResponse = borpService.getBorpResponse(
                BorpRequest.builder()
                        .mustEq("entityId", spacePath)//"/filelog/com.sankuai.octo.tmy/prod"
                        .mustIn("entityType", entityTypes)
                        .beginDate(startTime)
                        .endDate(endTime)
                        .from(page.getStart())
                        .size(page.getPageSize())
                        .build(), Action.class);
        if (null == borpResponse) {
            return Collections.EMPTY_LIST;
        }
        List<Action> actions = borpResponse.getResult();
        Page borpResponsePage = borpResponse.getPage();
        page.setTotalCount(borpResponsePage.getTotalCount());

        Set<String> actionIds = Sets.newHashSet(Iterables.transform(actions, new Function<Action, String>() {
            @Override
            public String apply(Action input) {
                return input.getActionId();
            }
        }));
        List<Detail> details = borpService.getByBorpRequest(BorpRequest.builder()
                .mustIn("actionId", actionIds)
                .build(), Detail.class);
        return borp2OperatorFileLog(actions, details);

    }

    /**
     * 包装成operationfilelog对象返回到页面展示
     * @param actions
     * @param details
     * @return
     */
    private Collection<OperationFileLog> borp2OperatorFileLog(List<Action> actions, List<Detail> details) {
        Multimap<String, Detail> detailByActionId = ArrayListMultimap.create(Multimaps.index(details, new Function<Detail, String>() {
            @Override
            public String apply(Detail input) {
                return input.getActionId();
            }
        }));

        Collection<OperationFileLog> logs = Lists.newArrayList();
        for (Action action : actions) {
            try {
                Collection<Detail> curDetails = detailByActionId.get(action.getActionId());
                OperationFileLog actionLog = genOperationFileLog(action.getEntityType());
                actionLog.setOperator(action.getOperatorName());
                actionLog.setTime(action.getAddTime());
                actionLog.setType(action.getEntityType());
                for(Detail detail : curDetails){
                    String field = detail.getFieldName();
                    String newValue = detail.getNewValue();
                    if ("groupname".equalsIgnoreCase(field)){
                        actionLog.setGroupname(newValue);
                    } else if ("filename".equalsIgnoreCase(field)){
                        actionLog.setFilename(newValue);
                    }
                    if (actionLog instanceof OperationFileLogWithIPs){
                        if ("dSuccessList".equalsIgnoreCase(field)) {
                            ((OperationFileLogWithIPs) actionLog).setdSuccessList(newValue);
                        } else if ("dErrList".equalsIgnoreCase(field)) {
                            ((OperationFileLogWithIPs) actionLog).setdErrorList(newValue);
                        } else if ("eErrList".equalsIgnoreCase(field)) {
                            ((OperationFileLogWithIPs) actionLog).seteErrorList(newValue);
                        }
                    }
                    if (actionLog instanceof OperationFileLogWithContent){
                        if ("fileContent".equalsIgnoreCase(field)) {
                            ((OperationFileLogWithContent) actionLog).setOldFileContent(detail.getOldValue());
                            ((OperationFileLogWithContent) actionLog).setNewFileContent(newValue);
                        } else if ("oldfileContent".equalsIgnoreCase(field)) {
                            ((OperationFileLogWithContent) actionLog).setOldFileContent(detail.getOldValue());
                        }
                    }
                }
                logs.add(actionLog);
            } catch (IllegalArgumentException e) {
                continue;
            }

        }
        return logs;
    }


    /**
     * 根据type生成不同类型的返回log对象
     * @param type
     * @return
     */
    private OperationFileLog genOperationFileLog(String type){
        return "FILE_DISTRIBUTE".equals(type) ? new OperationFileLogWithIPs() : new OperationFileLogWithContent();
    }


    }
