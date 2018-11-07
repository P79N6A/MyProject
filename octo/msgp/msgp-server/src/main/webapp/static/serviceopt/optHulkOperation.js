M.add('msgp-serviceopt/optHulkOperation', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkOperation = showHulkOperationList;

    var pageNo = 1, pageSize = 20;
    var operationStartInput = Y.one('#hulk_start_datetime'), operationEndInput = Y.one('#hulk_end_datetime');
    var unifiedPolicyActionTypes = [{"value":1,"name":"创建监控策略"},{"value":2,"name":"修改监控策略"},{"value":3,"name":"启用监控策略"},
        {"value":4,"name":"停用监控策略"},{"value":5,"name":"删除监控策略"}];
    var periodicPolicyActionTypes = [{"value":6,"name":"创建周期策略"},{"value":7,"name":"修改周期策略"},{"value":8,"name":"启用周期策略"},
        {"value":9,"name":"停用周期策略"},{"value":10,"name":"删除周期策略"}];
    var tagInfoActionTypes = [{"value":11,"name":"创建分组"},{"value":13,"name":"删除分组"}];
    var initialized = false, pageInitialized = false, operationTotalPage, operationTotalCount;
    var showActionDetailsDialog;

    //构造函数
    function showHulkOperationList() {
        if(!initialized){
            initialized = true;
            bindEvents();
            showOperationList(pageNo, pageSize);
        }
    }

    // 操作记录
    function showOperationList(pageNo, pageSize) {
        //查询前设置内容加载中
        $("#hulk_operation_action_tbody").html("<tr><td colspan='5' style='text-align: center;'>内容加载中...</td></tr>");
        var dateTime = getStartAndEndTime();
        if (!dateTime) return;
        var entityType = $('#hulk_operation_entity_type_select').find(":selected").val();
        var actionType = $('#hulk_operation_action_type_select').find(":selected").val();
        var url = '/hulk/operation/actions';
        $.ajax({
            type: "get",
            url: url,
            async: true,
            data: {
                appkey: Y.msgp.serviceopt.optHulkAppkey(),
                env: Y.msgp.serviceopt.optHulkEnv(),
                entityType: entityType,
                actionType: actionType,
                pageNo: pageNo,
                pageSize: pageSize,
                start: dateTime.start,
                end: dateTime.end
            },
            success: function (response) {
                if (!response || response.errorCode != 0) {
                    emptyOrErrorForOperation(true);
                    return;
                }
                var opRecords = response.opRecords;
                var result = opRecords.result;
                var page = opRecords.page;
                if (result && result.length > 0) {
                    for(var resultIndex = 0;resultIndex < result.length ;resultIndex++){
                        var action = result[resultIndex];
                        var actionType = action.actionType;
                        action.actionTypeName = getActionTypeName(actionType);
                        var operationDate = new Date(action.addTime);
                        action.operationTime = Y.mt.date.formatDateByString(operationDate, 'yyyy-MM-dd hh:mm:ss');
                    }
                    var micro = new Y.Template();
                    var template = Y.one('#text_hulk_operation_action').get('value');
                    var html = micro.render(template, {data:result});
                    $("#hulk_operation_action_tbody").html(html);
                    //绑定操作详情事件
                    bindShowActionDetails();
                    //分页
                    if(!pageInitialized || operationTotalPage !== page.totalPageCount || operationTotalCount !== page.totalCount){
                        refreshOperationPaginator(page);
                    }
                    pageInitialized = true;
                    operationTotalPage = page.totalPageCount;
                    operationTotalCount = page.totalCount;
                } else {
                    emptyOrErrorForOperation();
                }
            }
        });
    }

    //绑定事件：修改日期、操作对象、操作类型都会重新加载操作记录
    function bindEvents(){
        initDatePicker();
        $("#hulk_operation_entity_type_select").change(function () {
            var entityType = $('#hulk_operation_entity_type_select').find(":selected").val();
            setActionTypes(entityType);
            showOperationList(pageNo, pageSize);
        });
        $("#hulk_operation_action_type_select").change(function () {
            showOperationList(pageNo, pageSize);
        });
    }

    //绑定操作详情事件
    function bindShowActionDetails(){
        $("#operation_action_details_*").click(function(){
            var line = $(this).closest("tr");
            var data = line.attr('data-info');
            data = Y.JSON.parse(data);
            showActionDetailsDialog = showActionDetailsDialog ? showActionDetailsDialog : new Y.mt.widget.CommonDialog({
                id: 'showActionDetailsDialog',
                title: '操作详情',
                width: 768
            });
            var jsonPretty = JSON.stringify(data.details, null, '\t');
            while(jsonPretty.indexOf("com.sankuai.inf.hulk.bannerapi") >= 0){
                jsonPretty = jsonPretty.replace("com.sankuai.inf.hulk.bannerapi",Y.msgp.serviceopt.optHulkAppkey());
            }
            jsonPretty = jsonPretty.replace(/entityType/g, '操作对象类型').replace(/periodic_policy/g, '周期策略').replace(/unified_policy/g, '监控策略').replace(/tag_info/g, '分组');
            jsonPretty = jsonPretty.replace(/entityId/g, '操作对象id').replace(/fieldName/g, '属性').replace(/oldValue/g, '旧值').replace(/newValue/g, '新值');
            var html = "<pre>" + jsonPretty + "</pre>";
            showActionDetailsDialog.setContent(html);
            showActionDetailsDialog.show();
        });
    }

    function setActionTypes(entityType){
        var actionTypes = [];
        if(entityType == "unified_policy"){
            actionTypes = unifiedPolicyActionTypes;
        }else if(entityType == "periodic_policy"){
            actionTypes = periodicPolicyActionTypes;
        }else if(entityType == "tag_info"){
            actionTypes = tagInfoActionTypes;
        }
        var micro = new Y.Template();
        var template = Y.one('#text_hulk_operation_action_type').get('value');
        var html = micro.render(template, {data:actionTypes});
        $("#hulk_operation_action_type_select").html(html);
    }

    function getActionTypeName(actionTypeValue){
        var entityType = $('#hulk_operation_entity_type_select').find(":selected").val();
        var actionTypes = [];
        if(entityType == "unified_policy"){
            actionTypes = unifiedPolicyActionTypes;
        }else if(entityType == "periodic_policy"){
            actionTypes = periodicPolicyActionTypes;
        }else if(entityType == "tag_info"){
            actionTypes = tagInfoActionTypes;
        }
        for(var index = 0; index < actionTypes.length; index++){
            if(actionTypes[index].value == actionTypeValue){
                return actionTypes[index].name;
            }
        }
    }

    //分页
    function refreshOperationPaginator(page) {
        new Y.mt.widget.Paginator({
            contentBox: Y.one("#hulk_operation_action_page_wrapper"),
            index: page.pageNo || 1,
            max: page.totalPageCount || 1,
            pageSize: page.pageSize,
            totalCount: page.totalCount,
            callback: function (params) {
                showOperationList(params.page, pageSize);
            }
        });
    }

    function getStartAndEndTime() {
        var obj = {
            start: '',
            end: ''
        };
        var s = operationStartInput.get('value'),
            e = operationEndInput.get('value');
        var reg = /^\d{4}(-\d{2}){2} \d{2}:\d{2}:\d{2}$/;
        if (s && reg.test(s)) {
            obj.start = s;
        }
        reg.lastIndex = 0;
        if (e && reg.test(e)) {
            obj.end = e;
        }
        if (s !== obj.start || e !== obj.end) {
            Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
            return null;
        }
        if (obj.start > obj.end) {
            Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
            return null;
        }
        return obj;
    }

    function initDatePicker() {
        var now = new Date();
        var yestoday = new Date(now - 2 * 24 * 60 * 60 * 1000);
        var sdate = new Y.mt.widget.Datepicker({
            node: operationStartInput,
            showSetTime: true
        });
        sdate.on('Datepicker.select', function () {
            showOperationList(pageNo, pageSize);
        });
        operationStartInput.set('value', Y.mt.date.formatDateByString(yestoday, 'yyyy-MM-dd hh:mm:ss'));
        var edate = new Y.mt.widget.Datepicker({
            node: operationEndInput,
            showSetTime: true
        });
        edate.on('Datepicker.select', function () {
            showOperationList(pageNo, pageSize);
        });
        operationEndInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd hh:mm:ss'));
    }

    function emptyOrErrorForOperation(isError) {
        var html = '<tr><td colspan="5">' + (isError ? '获取失败' : '没有内容') +
            '<a href="javascript:void(0)" onclick="showOperationList(pageNo,pageSize)" class="get-again">重新获取</a></td></tr>';
        $("#hulk_operation_action_tbody").html(html);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'template',
        'w-base',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap',
        'msgp-serviceopt/optHulkUtils'
    ]
});