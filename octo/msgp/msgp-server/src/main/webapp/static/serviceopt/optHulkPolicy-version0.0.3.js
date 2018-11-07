M.add('msgp-serviceopt/optHulkPolicy-version0.0.3', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkPolicy = detailHulkPolicy;
    var inited = false;
    var hulkPolicyWrapper = Y.one('#hulkPolicy_content');
    var startInput = hulkPolicyWrapper.one('#hulk_start_time'),
        endInput = hulkPolicyWrapper.one('#hulk_end_time');
    var logWrapper = hulkPolicyWrapper.one('#hulkpolicy_log_wrap'),
        logTbody = logWrapper.one('tbody');
    var logPbody = logWrapper.one('#paghulk_wrapper');
    var colspan = 5;
    var hulkScalingPolicyTableWrapper = hulkPolicyWrapper.one('#hulkScalingPolicy_table'),
        hulkScalingPolicyTableTbody = hulkScalingPolicyTableWrapper.one('tbody');
    var appkey = Y.one('#apps_select').getAttribute('value'),
        showOverlay,
        showContent,
        curEnv = 3;
    var showSelectedIndexDialog,
        updateScalingPolicyDialog,
        addNewScalingPolicyDialog;
    var StateChangeSS = {1: "开启策略", 0: "停用策略"}
    var cpuTypes = [
        {key: 1, value: "1核"},
        {key: 2, value: "2核"},
        {key: 4, value: "4核"},
        {key: 6, value: "6核"},
        {key: 8, value: "8核"},
        {key: 12, value: "12核"},
        {key: 16, value: "16核"}
    ];
    var memTypes = [
        {key: 1024, value: "1G"},
        {key: 2 * 1024, value: "2G"},
        {key: 4 * 1024, value: "4G"},
        {key: 6 * 1024, value: "6G"},
        {key: 8 * 1024, value: "8G"},
        //{id: 10 * 1024, value: "10G"},
        {key: 12 * 1024, value: "12G"},
        {id: 16 * 1024, value: "16G"},
        //{id: 20 * 1024, value: "20G"},
        //{id: 40 * 1024, value: "40G"}
    ];
    var hdTypes = [
        {key: 50, value: "50G"},
        {key: 100, value: "100G"},
        {key: 200, value: "200G"},
        {key: 300, value: "300G"}
    ];

    var user = Y.msgp.serviceopt.optHulkUser();

    var addSGInitData = {};
    addSGInitData.cpuType = cpuTypes[2].value;
    addSGInitData.memType = memTypes[4].value;
    addSGInitData.hdType = hdTypes[3].value;

    var everPaged = false,
        totalPage,
        totalCount;
    var hulkLogTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-trigger="<%= item %>">',
        '<td style="width: 15%;"><%= Y.mt.date.formatDateByString(new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td style="width: 15%;"><%= item.operatorName %></td>',
        '<td style="width: 10%;"><%= item.tagName %></td>',
        '<td style="width: 20%;"><%= item.entityType %></td>',
        '<td style="width: 40%;"><%= item.newValue  %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');
    var operatorTypeOring = ["触发监控策略", "触发周期策略", "监控策略新增", "监控策略更新", "周期策略新增", "周期策略更新", "分组创建", "分组更新"];
    var idcListOfRecord = [];
    var urlOfRecord = '/hulk/idc/get';
    var checkAccess = 0;
    var checkIsConf = 0;
    var isOnLineEnv = true;
    var allIndexArray = [];
    function detailHulkPolicy(key, func1, func2) {
        if (!inited) {
            inited = true;
            appkey = key;
            showOverlay = func1;
            showContent = func2;
            $.ajax({
                type: "get",
                url: urlOfRecord,
                async: false,
                success: function (ret) {
                    idcListOfRecord = ret.data;
                }
            });
            bindIsRightForHULK();
            bindEnvSelect();
            bindHULKPolicy();
            bindAddScalingPolicy();
            bindDeleteScalingPolicy();
            initSelector();
            fillSelector(operatorTypeOring, 'hulk_operatorType');
            //fillSelector('hulk_idc', idcListOfRecord);
            initDatePicker();
            refreshData();
            checkAccess = Y.msgp.serviceopt.optHulkCheckIsAccessIn();
            checkIsConf = Y.msgp.serviceopt.optHulkCheckIsConf();
            bindShowScaleAndOperationRecords();
        }
        bindEnvSelect();
        getHulkScalingPolicyAndGroup(1);
        bindIsRightForHULK();
        checkAccess = Y.msgp.serviceopt.optHulkCheckIsAccessIn();
        checkIsConf = Y.msgp.serviceopt.optHulkCheckIsConf();
        //getHulkPeriodicPolicyAndGroup(1);
    }

    //弹性记录和操作记录
    function bindShowScaleAndOperationRecords(){
        $("#btn_show_hulk_scale_records").click(function(){
            $("#btn_show_hulk_scale_records").addClass("btn-primary");
            $("#btn_show_hulk_operation_records").removeClass("btn-primary");
            $("#div_hulk_scale_records").show();
            $("#div_hulk_operation_records").hide();
        });
        $("#btn_show_hulk_operation_records").click(function(){
            $("#btn_show_hulk_scale_records").removeClass("btn-primary");
            $("#btn_show_hulk_operation_records").addClass("btn-primary");
            $("#div_hulk_scale_records").hide();
            $("#div_hulk_operation_records").show();
            Y.msgp.serviceopt.optHulkOperation();
        });
    }

    //验证服务可否接入弹性策略
    function bindIsRightForHULK() {
        $.ajax({
            type: "get",
            url: "/hulk/stableImage/check?appkey=" + appkey + "&env="+ Y.msgp.serviceopt.optHulkEnv(),
            success: function (ret) {
                if (ret.code == 200) {
                    if (ret.data == [] || ret.data == null) {
                        Y.msgp.utils.msgpHeaderTip('error', "稳定镜像为空,请参考:<a href='https://123.sankuai.com/km/page/59021307'>HULK2.0 弹性页面操作手册</a>", 5);
                    }
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', "提示: 无法接入弹性; 原因:" + ret.msg + " 自查wiki:<a href='https://123.sankuai.com/km/page/62792377'>HULK2.0 接入问题排查</a>", 60);
                    $("#addNewScalingPolicy").attr("disabled", "true");
                    $("#addNewPeriodicPolicy").attr("disabled", "true");
                    $("#addModifySgGroup").attr("disabled", "true");
                    $("#scalingpolicy_add").css("display", "none");
                }
            },
            failure: function (ret) {
                Y.msgp.utils.msgpHeaderTip('error', "请求接入弹性失败 || 请联系HULK", 3);
            }
        });
    }

    //环境切换
    function bindEnvSelect() {
        Y.msgp.service.setEnvText('hulkScalingGroup_env_select');
        isOnLineEnv = Y.msgp.serviceopt.optHulkIsOnline();
        hulkPolicyWrapper.delegate('click', function () {
            hulkPolicyWrapper.all('#hulkScalingGroup_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            curEnv = Number(this.getAttribute("value"));
            //  getHulkScalingGroup(1);
            getHulkScalingPolicyAndGroup(1);
            doGetOperations(1);
        }, "#hulkScalingGroup_env_select a")
    }

    function bindHULKPolicy() {
        hulkPolicyWrapper.delegate('click', function () {
            getHulkScalingPolicyAndGroup(1);
        }, '#refreshHulkPolicy');
        bindUpdateScalingPolicy();
        bindIsRightForHULK();
        bindAddOptWithBtn();
    }

    // 增加统一监控策略时的处理函数
    function bindAddScalingPolicy() {
        hulkPolicyWrapper.delegate('click', function () {

            $("#table_supplier_scalingpolicy").css("display", "block");
            $("#table_supplier_periodpolicy").css("display", "none");
            $("#table_supplier_group").css("display", "none");
            $("#addNewScalingPolicy").addClass("btn-primary");
            $("#addNewPeriodicPolicy").removeClass("btn-primary");
            $("#addModifySgGroup").removeClass("btn-primary");
            bindAddOptWithBtn();
            getHulkScalingPolicyAndGroup();
        }, '#addNewScalingPolicy');
        hulkPolicyWrapper.delegate('click', function () {
            $("#table_supplier_scalingpolicy").css("display", "none");
            $("#table_supplier_periodpolicy").css("display", "block");
            $("#table_supplier_group").css("display", "none");
            $("#addNewPeriodicPolicy").addClass("btn-primary");
            $("#addNewScalingPolicy").removeClass("btn-primary");
            $("#addModifySgGroup").removeClass("btn-primary");
            bindAddOptWithBtn();
            //周期策略
            Y.msgp.serviceopt.optHulkPeriodicPolicy();
        }, '#addNewPeriodicPolicy');
        /**分组功能*/
        hulkPolicyWrapper.delegate('click', function () {
            $("#table_supplier_scalingpolicy").css("display", "none");
            $("#table_supplier_periodpolicy").css("display", "none");
            $("#table_supplier_group").css("display", "block");
            $("#addModifySgGroup").addClass("btn-primary");
            $("#addNewPeriodicPolicy").removeClass("btn-primary");
            $("#addNewScalingPolicy").removeClass("btn-primary");
            bindAddOptWithBtn();
            //分组信息
            Y.msgp.serviceopt.optHulkTagInfo();
        }, '#addModifySgGroup');
        //删除
        hulkPolicyWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            var spId = Y.JSON.parse(data).id;
            Y.io('/hulk/scalingPolicy/' + appkey + '/' + spId + '/delete', {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '伸缩策略删除成功', 3);
                            getHulkScalingPolicyAndGroup(1);
                            //getHulkPeriodicPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '伸缩策略获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '伸缩策略删除异常', 3);
                    }
                }
            });
        }, '#deleteScalingPolicy');
        //禁用启用
        hulkPolicyWrapper.delegate('click', function () {
            if(checkAccess == 3 && checkIsConf == 1) {
                var el = this;
                if (el.hasClass('active')) return;
                var enabled = +el.getData('enabled');
                doChangedPolicyEnabled(el, enabled);
            } else if (checkAccess == 2) {
                Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
            } else if (checkAccess == 1) {
                Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
            } else if (checkIsConf == 0){
                Y.msgp.utils.msgpHeaderTip('error', "服务配置过高!!!请联系管理员录入策略", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
            }
        }, '#hulk-policy-one-enabled .btn');
        // bindUpdateScalingPolicy();
    }

    //绑定添加操作的按钮,弹出对应的策略添加页面JJ
    function bindAddOptWithBtn() {
        $("#scalingpolicy_add").click(function () {
            if (checkAccess == 3 && checkIsConf == 1) {
                addNewScalingPolicyDialog = addNewScalingPolicyDialog ? addNewScalingPolicyDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_HULKScalingPolicy_dialog',
                    title: '创建弹性策略 ',
                    width: 1024,
                    btn: {
                        pass: doAddNewHULKScalingPolicy
                    }
                });
                var all_id_coll = [];
                var noScaleInTimeNum = 0;
                var micro = new Y.Template();
                var template = Y.one('#text_addNewScalingPolicy_form').get('value');
                var init_startTime = "";
                var init_endTime = "";
                allIndexArray = obtainAllIndex();
                var str = micro.render(template, {
                    init_startTime: init_startTime,
                    init_endTime: init_endTime,
                    isOnLineEnv: isOnLineEnv,
                    allIndexArray: allIndexArray
                });
                addNewScalingPolicyDialog.setContent(str);
                //获取数据,进行填充
                $.ajax({
                    type: "get",
                    url: "/hulk/getAllTagByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv(),
                    async: false,
                    success: function (ret) {
                        var temp_tagInfo = ret.tagInfoList;
                        var user_group_show_body = Y.one('#add_monitor_policy_groupshow').one('tbody');
                        var template1 = Y.one("#text_update_unified_policy_tag").get("value");
                        var tag_id_fake = 0;
                        for (var i = 0; i < temp_tagInfo.length; i++) {
                            if (temp_tagInfo[i].policyId) {
                                temp_tagInfo[i]["binded"] = true;
                                all_id_coll.push(temp_tagInfo[i].id);
                            } else {
                                temp_tagInfo[i]["binded"] = false;
                            }
                            tag_id_fake = tag_id_fake + 1;
                            temp_tagInfo[i].regionName = Y.msgp.serviceopt.optHulkRegionName(temp_tagInfo[i].region);
                            temp_tagInfo[i].idcName = Y.msgp.serviceopt.optHulkIdcName(temp_tagInfo[i].idc);
                            temp_tagInfo[i].tag_id_fake = tag_id_fake;
                            temp_tagInfo[i].isOnLineEnv = isOnLineEnv;
                        }
                        var s2 = micro.render(template1, {data: temp_tagInfo});
                        user_group_show_body.setContent(s2);
                    }
                });
                $("#add_monitor_policy_groupshow_click").click(function () {
                    if ($("#add_monitor_policy_groupshow_click").is(':checked')) {
                        $("#add_monitor_policy_groupshow").css("display", "block");
                    } else {
                        $("#add_monitor_policy_groupshow").css("display", "none");
                    }
                });
                addNewScalingPolicyDialog.show();
                $(".one_click_for_group_").each(function(){
                    if($(this).attr("disabled")) {
                        Y.msgp.utils.addTooltipWithContent($(this).parent(), '已被其他策略绑定');
                    }
                });

                $("#multi_noScaleInTime_add_btn").click(function () {
                    noScaleInTimeNum = noScaleInTimeNum + 1;
                    var noScaleInPeriodsTimestr = '' +
                        '<div class="controls" id="allNoScaleInTime_' + noScaleInTimeNum + '">' +
                        '<input style="margin-top: 3px;" type="text" value="" class="s_sp_noScaleInTime_startTime" id="s_sp_noScaleInTime_startTime">&nbsp;&nbsp;- &nbsp;' +
                        '<input style="margin-top: 3px;" type="text" value="" class="s_sp_noScaleInTime_endTime" id="s_sp_noScaleInTime_endTime">' +
                        '<input style="margin-top: 3px; margin-left: 4px;" type="button" value="删除" class="delNoScaleInTime" id="' + noScaleInTimeNum + '"/>' +
                        '</div>';
                    $("#s_sp_noScaleInTime_content").append(noScaleInPeriodsTimestr);
                    $(".delNoScaleInTime").click(function () {
                        var thisId = "allNoScaleInTime_" + ($(this).attr("id"));
                        $("#" + thisId).hide();
                    });
                });
            } else if (checkAccess == 2) {
                Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
            } else if (checkAccess == 1) {
                Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
            } else if (checkIsConf == 0){
                Y.msgp.utils.msgpHeaderTip('error', "服务配置过高!!请联系管理员录入策略", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
            }
        });
    }

    //添加弹性策略与伸缩组 确定按钮
    //阈值设置默认(权宜之计)
    var qps_ul_value = '"qps":{"lower": 100, "upper": 500}';
    var cpu_ul_value = '"cpu":{"lower":"20", "upper":80}';
    var mem_ul_value = '"mem":{"lower": 20, "upper":80}';
    var qps_ul_value_after = '';
    var cpu_ul_value_after = '';
    var mem_ul_value_after = '';

    function doAddNewHULKScalingPolicy(btn, container) {
        var metrics_bound = "{";
        //获取选了哪些quota并且进行上下阈值的设置
        if ($("#qps_radio").is(":checked")) {
            if (qps_ul_value_after != '') {
                metrics_bound += qps_ul_value_after + ",";
            } else {
                metrics_bound += qps_ul_value + ",";
            }
        }
        if ($("#cpu_radio").is(":checked")) {
            metrics_bound += cpu_ul_value + ",";
        }
        if ($("#mem_radio").is(":checked")) {
            metrics_bound += mem_ul_value + ",";
        }
        metrics_bound = metrics_bound.substring(0, metrics_bound.length - 1);
        metrics_bound += "}";
        var ss_time = exchangeTimeToMinutes($('#s_sp_noScaleInTime_startTime').val());
        var ee_time = exchangeTimeToMinutes($('#s_sp_noScaleInTime_endTime').val());
        var no_scale_time = [];
        var no_scale_start_time = [];
        var no_scale_end_time = [];
        var timeIsOk = true;
        $(".s_sp_noScaleInTime_startTime").each(function () {
            if($(this).is(':visible')) {
                if (checkRegixTime($(this).val())) {
                    no_scale_start_time.push(exchangeTimeToMinutes($(this).val()));
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', '时间格式错误 HH:mm', 3);
                    timeIsOk = false;
                }
            }
        });
        $(".s_sp_noScaleInTime_endTime").each(function () {
            if($(this).is(':visible')) {
                if (checkRegixTime($(this).val())) {
                    no_scale_end_time.push(exchangeTimeToMinutes($(this).val()));
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', '时间格式错误 HH:mm', 3);
                    timeIsOk = false;
                }
            }
        });
        var nonono_s = [];
        var nonono_e = [];
        for (var i = 0; i < no_scale_start_time.length; i++) {
            if((no_scale_start_time[i] == -1 && no_scale_end_time[i] != -1) || (no_scale_start_time[i] != -1 && no_scale_end_time[i] == -1)) {
                Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
                timeIsOk = false;
                break;
            } else if(no_scale_end_time[i] != -1 && no_scale_start_time[i] != -1) {
                nonono_s.push(no_scale_start_time[i]);
                nonono_e.push(no_scale_end_time[i]);
            } else {
                //none
            }
        }
        if(!checkIsAcross(nonono_s, nonono_e)){
            timeIsOk = false;
        }
        if (nonono_s.length > 0) {
            for (var k = 0; k < nonono_s.length; k++) {
                var aItem = {
                    "startTime": nonono_s[k],
                    "endTime": nonono_e[k]
                };
                no_scale_time.push(aItem);
            }
        } else {
            no_scale_time = [];
        }
        var monitorPolicy = {
            "appkey": appkey,
            "env": Y.msgp.serviceopt.optHulkEnv(),
            "state": 1,
            "metricsBound": metrics_bound,
            "coolingTime": 120,
            "tagList": generateUnifiedPolicyTagsFromTable("add_unified_policy_tags_table"),
            "noScaleinSwitch": 1,
            "noScaleinPeriods": no_scale_time,
            "channelType": 1,
            "user": Y.msgp.serviceopt.optHulkUser()
        };
        var sendUrl = "/hulk/unifiedPolicy/addUniPolicy";
        if (timeIsOk && checkAddUniPolicyData(monitorPolicy)) {

            //var uniPolicyStr = monitorPolicy.appkey + "" + monitorPolicy.env + "" + monitorPolicy.metricsBound + "" + monitorPolicy.noScaleinPeriods + "" + monitorPolicy.tagList;
            //uniPolicyStr = uniPolicyStr.replace(/\"/g,"").replace(/\s/g,"");
            $.ajax({
                type: "get",
                url: "/hulk/unifiedPolicy/getAllPolicyByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv(),
                async: true,
                success: function (ret) {
                    var all_policy_str = "";
                    //for (var i = 0; i < ret.length; i++) {
                    //    if(ret[i].state != -1) {
                    //        all_policy_str += ret[i].appkey + "" + ret[i].env + "" + ret[i].metricsBound + "" + ret[i].noScaleinPeriods + "" + ret[i].tags;
                    //    }
                    //}
                    if (monitorPolicy.merticsBound == "{") {
                        Y.msgp.utils.msgpHeaderTip('error', '策略配置问题', 3);
                        return false;
                    }
                    if (monitorPolicy.noScaleinPeriods == '[{') {
                        Y.msgp.utils.msgpHeaderTip('error', '策略配置问题', 3);
                        return false;
                    }
                    //all_policy_str = all_policy_str.replace(/\"/g,"").replace(/\s/g,"");
                    //if (all_policy_str.indexOf(uniPolicyStr) != -1) {
                    //    Y.msgp.utils.msgpHeaderTip('error', '该策略已经配置过', 3);
                    //    return false;
                    //}
                    Y.io(sendUrl, {
                        method: 'post',
                        headers: {'Content-Type': "application/json;charset=UTF-8"},
                        data: Y.JSON.stringify(monitorPolicy),
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (ret.errorCode == 0) {
                                    addNewScalingPolicyDialog.close();
                                    getHulkScalingPolicyAndGroup();
                                    Y.msgp.utils.msgpHeaderTip('success', '增加统一监控策略成功', 10);
                                } else {
                                    Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '增加统一监控策略失败', 10);
                                }
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '增加统一监控策略失败', 3);
                            }
                        }
                    });
                }
            });
        }
        return true;
    }

    function checkTime(raw_start, raw_end, startTime, endTime) {
        var reg_ = /^(20|21|22|23|[0-1]\d):[0-5]\d$/;
        var reg = new RegExp(reg_);
        if ((raw_start && reg.test(raw_start) && raw_end && reg.test(raw_end))) {
            if (startTime > endTime || (endTime - startTime) > 180) {
                Y.msgp.utils.msgpHeaderTip('error', '禁止缩容时间设置过长', 3);
                return false;
            }
        } else {
            Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
            return false;
        }
        return true;
    }

    function checkAddUniPolicyData(data) {
        if (data.metricsBound == "{" || data.metricsBound == "{}" || data.metricsBound == "}") {
            Y.msgp.utils.msgpHeaderTip('error', '请选择一个监控指标', 3);
            return false;
        }
        return true;
    }

    var isUpdatingData;

    function bindUpdateScalingPolicy() {
        updateScalingPolicyDialog = updateScalingPolicyDialog ? updateScalingPolicyDialog : new Y.mt.widget.CommonDialog({
            id: 'updateScalingPolicy_view',
            title: '更新伸缩策略',
            width: 1024,
            btn: {
                pass: doUpdateScalingPolicyRatio
            }
        });
        hulkPolicyWrapper.delegate('click', function () {
            var timeflag = "true";
            if (checkAccess == 3 && checkIsConf == 1) {
                var el = this;
                var line = el.ancestor('tr');
                var data = line.getData('info');
                data = Y.JSON.parse(data);
                var noScaleinPeriods = eval(data.noScaleinPeriods);
                if(noScaleinPeriods.length == 0) {
                    timeflag = "false";
                } else {
                    for (var item = 0; item < noScaleinPeriods.length; item++) {
                        if (item == 0) {
                            noScaleinPeriods[item].startTime = exchangeMinutesToTime(noScaleinPeriods[item].startTime);
                            noScaleinPeriods[item].endTime = exchangeMinutesToTime(noScaleinPeriods[item].endTime);
                            noScaleinPeriods[item].flag = "true";
                        } else {
                            noScaleinPeriods[item].startTime = exchangeMinutesToTime(noScaleinPeriods[item].startTime);
                            noScaleinPeriods[item].endTime = exchangeMinutesToTime(noScaleinPeriods[item].endTime);
                            noScaleinPeriods[item].flag = "false";
                        }
                    }
                }
                data.noScaleinPeriods = noScaleinPeriods;
                var micro = new Y.Template();
                var template = Y.one('#text_updateScalingPolicy_dialog').get('value');
                allIndexArray = obtainAllIndex();
                var str = micro.render(template, {
                    spData: data,
                    isOnLineEnv: isOnLineEnv,
                    allIndexArray: allIndexArray,
                    timeflag: timeflag
                });
                isUpdatingData = data;
                updateScalingPolicyDialog.setContent(str);
                //获取数据,进行填充
                var user_group_update_show_body = Y.one('#update_monitor_policy_groupshow2').one('tbody');
                var template1 = Y.one("#text_update_unified_policy_tag").get("value");

                //显示分组并设置绑定状态
                $.ajax({
                    type: "get",
                    url: "/hulk/getAllTagByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv(),
                    async: false,
                    success: function (ret) {
                        var showTagData = ret.tagInfoList;
                        var policyTagIdArr = getTagIdArr(data.tags);
                        for (var i = 0; i < showTagData.length; i++) {
                            showTagData[i].regionName = Y.msgp.serviceopt.optHulkRegionName(showTagData[i].region);
                            showTagData[i].idcName = Y.msgp.serviceopt.optHulkIdcName(showTagData[i].idc);
                            if ($.inArray(showTagData[i].id, policyTagIdArr) !== -1) {
                                showTagData[i]['related'] = true;
                            } else {
                                showTagData[i]['related'] = false;
                            }
                            //已经被其他监控策略绑定的tagInfo不可绑定
                            if (showTagData[i].policyId && parseInt(showTagData[i].policyId) != data.id) {
                                showTagData[i]['binded'] = true;
                            } else {
                                showTagData[i]['binded'] = false;
                            }
                        }
                        var s2 = micro.render(template1, {data: showTagData});
                        user_group_update_show_body.setContent(s2);
                    }
                });
                $(".one_click_for_group_").each(function(){
                    if($(this).attr("disabled")) {
                        Y.msgp.utils.addTooltipWithContent($(this).parent(), '已被其他策略绑定');
                    }
                });
                $("#add_monitor_policy_look_value").click(function () {
                    showSelectedIndexDialog = showSelectedIndexDialog ? showSelectedIndexDialog : new Y.mt.widget.CommonDialog({
                        id: 'show_selectedIndex_dialog',
                        title: '所选指标阈值 ',
                        width: 768,
                    });
                    var micro_showIndex = new Y.Template();
                    var template_showIndex = Y.one('#text_userSelectedIndex_form').get('value');
                    var quota_index_value_ = data.metricsBound;
                    var index_json = [];
                    var j = {};
                    $("input[name='set_cell_switch']:checked").each(function () {
                        var qps_low = 0;
                        var qps_high = 0;
                        var cpu_low = 20;
                        var cpu_high = 80;
                        var mem_low = 20;
                        var mem_high = 80;
                        if (JSON.parse(quota_index_value_).qps != null) {
                            var qps_agent = JSON.parse(JSON.stringify(JSON.parse(quota_index_value_).qps));
                            qps_low = qps_agent.lower;
                            qps_high = qps_agent.upper;
                        }
                        if (JSON.parse(quota_index_value_).cpu != null) {
                            var cpu_agent = JSON.parse(JSON.stringify(JSON.parse(quota_index_value_).cpu));
                            cpu_low = cpu_agent.lower;
                            cpu_high = cpu_agent.upper;
                        }
                        if (JSON.parse(quota_index_value_).mem != null) {
                            var mem_agent = JSON.parse(JSON.stringify(JSON.parse(quota_index_value_).mem));
                            mem_low = mem_agent.lower;
                            mem_high = mem_agent.upper;
                        }
                        if ($(this).attr("id") == "update_qps_radio") {
                            j.index = "qps";
                            j.low = qps_low;
                            j.high = qps_high;
                            index_json.push(j);
                            j = {};
                        }
                        if ($(this).attr("id") == "update_cpu_radio") {
                            j.index = "cpu";
                            j.low = cpu_low;
                            j.high = cpu_high;
                            index_json.push(j);
                            j = {};
                        }
                        if ($(this).attr("id") == "update_mem_radio") {
                            j.index = "mem";
                            j.low = mem_low;
                            j.high = mem_high;
                            index_json.push(j);
                            j = {};
                        }
                    });
                    var www = micro_showIndex.render(template_showIndex, {
                        data: index_json
                    });
                    //text_userSelectedIndex_form
                    showSelectedIndexDialog.setContent(www);
                    showSelectedIndexDialog.show();
                    $("#cancle_btn_index").click(function () {
                        showSelectedIndexDialog.close();
                    });
                    //修改阈值
                    $("#add_btn_index").click(function () {//#TODO add check
                        var mb = "{";
                        if (document.getElementsByName("qps_low")[0] != null) {
                            var qps_low = document.getElementsByName("qps_low")[0].value;
                            var qps_high = document.getElementsByName("qps_high")[0].value;
                            mb += '"qps":{"lower": "' + qps_low + '", "upper": "' + qps_high + '"},';
                        }
                        if (document.getElementsByName("cpu_low")[0] != null) {
                            var cpu_low = document.getElementsByName("cpu_low")[0].value;
                            var cpu_high = document.getElementsByName("cpu_high")[0].value;
                            mb += '"cpu":{"lower": "' + cpu_low + '", "upper": "' + cpu_high + '"},';
                        }
                        if (document.getElementsByName("mem_low")[0] != null) {
                            var mem_low = document.getElementsByName("mem_low")[0].value;
                            var mem_high = document.getElementsByName("mem_high")[0].value;
                            mb += '"mem":{"lower": "' + mem_low + '", "upper": "' + mem_high + '"},';
                        }
                        mb = mb.substring(0, mb.length - 1) + "}";
                        data.metricsBound = mb;
                        isUpdatingData = data;
                        data.policyId = data.id;
                        data.tagList = getTagIds(data.tags + "");
                        data.user = Y.msgp.serviceopt.optHulkUser();
                        //Y.io("/hulk/unifiedPolicy/updateUniPolicy", {
                        //    method: 'post',
                        //    headers: {'Content-Type': "application/json;charset=UTF-8"},
                        //    data: Y.JSON.stringify(data),
                        //    on: {
                        //        success: function (id, o) {
                        //            writeInOpContent(0, data.id, 2, "update unipolicy at" + " policy id = " + data.id, getCurrentTime());
                        //            var ret = Y.JSON.parse(o.responseText);
                        //            if (ret.errorCode == 0) {
                        //                showSelectedIndexDialog.close();
                        //                Y.msgp.utils.msgpHeaderTip('success', '更新阈值成功', 3);
                        //                getHulkScalingPolicyAndGroup(1);
                        //                //getHulkPeriodicPolicyAndGroup(1);
                        //            }
                        //            //else {
                        //            //    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '更新阈值失败', 3);
                        //            //    getHulkScalingPolicyAndGroup(1);
                        //            //    //getHulkPeriodicPolicyAndGroup(1);
                        //            //}
                        //        },
                        //        failure: function () {
                        //            showSelectedIndexDialog.close();
                        //            Y.msgp.utils.msgpHeaderTip('error', '更新请求失败', 3);
                        //            getHulkScalingPolicyAndGroup(1);
                        //            //getHulkPeriodicPolicyAndGroup(1);
                        //        }
                        //    }
                        //});
                        //更新对应quota的阈值
                        showSelectedIndexDialog.close();
                    });
                });
                $("#update_monitor_policy_groupshow_click").click(function () {
                    if ($("#update_monitor_policy_groupshow_click").is(':checked')) {
                        $("#update_monitor_policy_groupshow").css("display", "block");
                    } else {
                        $("#update_monitor_policy_groupshow").css("display", "none");
                    }
                });
                updateScalingPolicyDialog.show();
                var quota_str = data.metricsBound;
                if (quota_str.indexOf("qps") != -1) {
                    $("#update_qps_radio").attr("checked", "true");
                }
                if (quota_str.indexOf("cpu") != -1) {
                    $("#update_cpu_radio").attr("checked", "true");
                }
                if (quota_str.indexOf("mem") != -1) {
                    $("#update_mem_radio").attr("checked", "true");
                }
                if (quota_str.indexOf("load") != -1) {
                    $("#update_load_radio").attr("checked", "true");
                }
                if (quota_str.indexOf("mq") != -1) {
                    $("#update_mq_radio").attr("checked", "true");
                }
                if (quota_str.indexOf("db") != -1) {
                    $("#update_db_radio").attr("checked", "true");
                }
                var noScaleInTimeNum = 0;
                $("#multi_noScaleInTime_update_btn").click(function () {
                    noScaleInTimeNum = noScaleInTimeNum + 1;
                    var noScaleInPeriodsTimestr = '' +
                        '<div class="controls" style="margin-top: 3px;">' +
                        '<input type="text" value="" class="stt_update_sp_noScaleInTime" id="stt_update_sp_noScaleInTime" onchange="">&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;' +
                        '<input type="text" value="" class="edt_update_sp_noScaleInTime" id="edt_update_sp_noScaleInTime" onchange="">' +
                        '<input style="margin-left: 5px;" id="delNoScaleInTime" class="delNoScaleInTime_" type="button" value="删除"/>' +
                        '</div>';
                    $("#s_update_sp_noScaleInTime_content").append(noScaleInPeriodsTimestr);
                    $(".delNoScaleInTime_").click(function () {
                        $(this).parent().hide();
                    });
                });
                $(".delNoScaleInTime_").click(function () {
                    $(this).parent().hide();
                });
            } else if (checkAccess == 2) {
                Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
            } else if (checkAccess == 1) {
                Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
            } else if (checkIsConf == 0){
                Y.msgp.utils.msgpHeaderTip('error', "服务配置过高!!请联系管理员录入策略", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
            }

        }, '#updateScalingPolicy');
    }

    //删除统一监控策略
    function bindDeleteScalingPolicy() {
        hulkPolicyWrapper.delegate('click', function () {
            if(checkAccess == 3 && checkIsConf == 1) {
                var el = this;
                var line = el.ancestor('tr');
                var data = line.getData('info');
                data = Y.JSON.parse(data);
                data.policyId = data.id;
                if (data.state == 1) {
                    Y.msgp.utils.msgpHeaderTip('warn', '删除前请先停用该监控策略', 3);
                } else if (data.tags) {
                    Y.msgp.utils.msgpHeaderTip('warn', '删除前请先解绑该监控策略的分组', 3);
                } else {
                    var dataP = {"appkey": appkey, "policyId": data.id, "user": Y.msgp.serviceopt.optHulkUser()};
                    Y.io("/hulk/unifiedPolicy/removePolicy", {
                        method: 'post',
                        headers: {'Content-Type': "application/json;charset=UTF-8"},
                        data: Y.JSON.stringify(dataP),
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (ret.errorCode == 0) {
                                    Y.msgp.utils.msgpHeaderTip('success', '删除统一监控策略成功', 10);
                                    getHulkScalingPolicyAndGroup();
                                } else {
                                    Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '删除统一监控策略失败', 10);
                                }
                            }
                        }
                        });
                }


            } else if (checkAccess == 2) {
                Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
            } else if (checkAccess == 1) {
                Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
            } else if (checkIsConf == 0){
                Y.msgp.utils.msgpHeaderTip('error', "服务配置过高!!请联系管理员录入策略", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
            }
        }, '#removeScalingPolicy');
    }

    //根据表格中选中的tag生成tagList
    function generateUnifiedPolicyTagsFromTable(tableId) {
        var newTagList = [];
        var checkboxes = $("#" + tableId).find("input[type='checkbox'][name='unifiedPolicyTags']");
        if (checkboxes) {
            for (var index = 0; index < checkboxes.length; index++) {
                var checkbox = $(checkboxes[index]);
                if (checkbox.prop('checked')) {
                    newTagList.push(parseInt(checkbox.attr('data-info')));
                }
            }
        }
        return newTagList;
    }

    function getTagIds(tagsStr) {
        var tagIdArr = [];
        if (tagsStr) {
            var tags = Y.JSON.parse(tagsStr);
            if (tags) {
                for (var tagIndex = 0; tagIndex < tags.length; tagIndex++) {
                    var tag = tags[tagIndex];
                    if (tag && tag.id) {
                        tagIdArr.push(tag.id);
                    }
                }
            }
        }
        return tagIdArr;
    }

    //更新监控策略
    function doUpdateScalingPolicyRatio(btn, container) {
        var url = '/hulk/unifiedPolicy/updateUniPolicy';
        var oldData = isUpdatingData;
        var oldMetricsBound = JSON.parse(oldData.metricsBound);
        var metricsBound = "";
        $("input[name='set_cell_switch']:checked").each(function () {
            if($(this).val() == "qps") {
                if(oldMetricsBound.qps == null || oldMetricsBound.qps == undefined){
                    metricsBound += qps_ul_value + ",";
                }else {
                    metricsBound += '"qps":' + JSON.stringify(oldMetricsBound.qps) + ",";
                }
            }
            if($(this).val() == "cpu") {
                if(oldMetricsBound.cpu == null || oldMetricsBound.cpu == undefined){
                    metricsBound += cpu_ul_value + ",";
                }else {
                    metricsBound += '"cpu":' + JSON.stringify(oldMetricsBound.cpu) + ",";
                }
            }
            if($(this).val() == "mem") {
                if(oldMetricsBound.mem == null || oldMetricsBound.mem == undefined){
                    metricsBound += mem_ul_value + ",";
                }else {
                    metricsBound += '"mem":' + JSON.stringify(oldMetricsBound.mem) + ",";
                }
            }
        });
        var newMetricsBound = "{" + metricsBound.substring(0, metricsBound.length - 1) + "}";
        var no_scale_start_time = [];
        var no_scale_end_time = [];
        var no_scale_time = [];
        var timeIsOk = true;
        $(".stt_update_sp_noScaleInTime").each(function () {
            if($(this).is(':visible')) {
                if (checkRegixTime($(this).val())) {
                    no_scale_start_time.push(exchangeTimeToMinutes($(this).val()));
                } else {
                    timeIsOk = false;
                }
            }
        });
        $(".edt_update_sp_noScaleInTime").each(function () {
            if($(this).is(':visible')) {
                if (checkRegixTime($(this).val())) {
                    no_scale_end_time.push(exchangeTimeToMinutes($(this).val()));
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', '时间格式 HH:mm', 3);
                    timeIsOk = false;
                }
            }
        });
        var nonono_s = [];
        var nonono_e = [];
        for (var i = 0; i < no_scale_start_time.length; i++) {
            if((no_scale_start_time[i] == -1 && no_scale_end_time[i] != -1) || (no_scale_start_time[i] != -1 && no_scale_end_time[i] == -1)) {
                Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
                timeIsOk = false;
                break;
            } else if(no_scale_end_time[i] != -1 && no_scale_start_time[i] != -1) {
                nonono_s.push(no_scale_start_time[i]);
                nonono_e.push(no_scale_end_time[i]);
            } else {
                //none
            }
        }
        if(!checkIsAcross(nonono_s, nonono_e)){
            timeIsOk = false;
        }
        if (nonono_s.length > 0) {
            for (var k = 0; k < nonono_s.length; k++) {
                var aItem = {
                    "startTime": nonono_s[k],
                    "endTime": nonono_e[k]
                };
                no_scale_time.push(aItem);
            }
        } else {
            no_scale_time = [];
        }
        var isHave = 0;
        if (oldData.metricsBound != newMetricsBound && newMetricsBound != "{}") {
            oldData.metricsBound = newMetricsBound;
            isHave = 1;
        }
        var isHaveIndex = true;
        if(newMetricsBound == "{}"){
            Y.msgp.utils.msgpHeaderTip('error', '请选择监控指标', 3);
            isHaveIndex = false;
        }
        if (oldData.noScaleinPeriods != no_scale_time) {
            oldData.noscaleinPeriods = no_scale_time;
            isHave = 2;
        }
        oldData.policyId = oldData.id;
        var oldTagList = getTagIdArr(oldData.tags);
        var newTagList = generateUnifiedPolicyTagsFromTable("update_unified_policy_tags_table");
        oldData.tagList = newTagList;
        oldData.user = Y.msgp.serviceopt.optHulkUser();
        if(!(oldTagList.toString() == newTagList.toString())) {
            isHave = 3;
        }
        if (isHave > 0 && timeIsOk && isHaveIndex) {
            //检查传输文本的正确性
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(oldData),
                on: {
                    success: function (id, o) {
                        writeInOpContent(0, oldData.id, 2, "update unipolicy at" + " policy id = " + oldData.id, getCurrentTime());
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.errorCode == 0) {
                            updateScalingPolicyDialog.close();
                            Y.msgp.utils.msgpHeaderTip('success', '监控策略更新成功', 3);
                            getHulkScalingPolicyAndGroup(1);
                            //getHulkPeriodicPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '监控策略更新失败', 3);
                            getHulkScalingPolicyAndGroup(1);
                            //getHulkPeriodicPolicyAndGroup(1);
                        }
                    },
                    failure: function () {
                        updateScalingPolicyDialog.close();
                        Y.msgp.utils.msgpHeaderTip('error', '监控策略更新失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        //getHulkPeriodicPolicyAndGroup(1);
                    }
                }
            });
        } else if (isHave <= 0) {
            Y.msgp.utils.msgpHeaderTip('error', '无变化', 3);
        }
        return true;
    }

    function doChangedPolicyEnabled(el, enabledd) {
        var url = '/hulk/unifiedPolicy/updatePolicyState';
        var line = el.ancestor('tr');
        var data = line.getData('info');
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        data.policyId = data.id;
        data.state = enabledd;
        data.user = Y.msgp.serviceopt.optHulkUser();
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.errorCode == 0) {
                        Y.msgp.utils.msgpHeaderTip('success', '更新策略状态成功', 3);
                        writeInOpContent(0, data.id, 2, StateChangeSS[enabledd] + " policy id = " + data.id, getCurrentTime());
                        getHulkScalingPolicyAndGroup(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '更新策略状态失败', 3);
                        writeInOpContent(0, data.id, 2, "更新策略状态失败", getCurrentTime());
                        getHulkScalingPolicyAndGroup(1);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '更新策略状态失败', 3);
                    writeInOpContent(0, data.id, 2, "更新策略状态失败", getCurrentTime());
                    getHulkScalingPolicyAndGroup(1);
                }
            }
        });
        return true;
    }

    //获取弹性策略和伸缩组的信息
    function getHulkScalingPolicyAndGroup(pageNo) {
        //showContent(hulkPolicyWrapper);
        $.ajax({
            type: "get",
            url: "/hulk/unifiedPolicy/getAllPolicyByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv(),
            async: true,
            success: function (ret) {
                for (var i = 0; i < ret.length; i++) {
                    var s1 = ret[i].noScaleinPeriods + "";
                    var noScaleInTimeArray = eval(s1);
                    var json_str = s1.substring(s1.indexOf("{"), s1.indexOf("}") + 1);
                    var str_quota = ret[i].metricsBound;
                    var quota_key = "";
                    for (var key in JSON.parse(str_quota)) {
                        quota_key += key + "; ";
                    }
                    ret[i].quota = quota_key;
                    if(getTagInfoNameArr(ret[i].tags) == 0) {
                        ret[i].tagNameArr = "null";
                    } else {
                        ret[i].tagNameArr = getTagInfoNameArr(ret[i].tags);
                    }
                    var noScaleInTimeStr = "";
                    for(var item = 0; item < noScaleInTimeArray.length; item++) {
                        var st = exchangeMinutesToTime(noScaleInTimeArray[item].startTime);
                        var et = exchangeMinutesToTime(noScaleInTimeArray[item].endTime);
                        noScaleInTimeStr += st + "-" + et + ";";
                    }
                    noScaleInTimeStr = noScaleInTimeStr.substring(0, noScaleInTimeStr.length - 1);
                    ret[i].noScaleInTimeAfter = noScaleInTimeStr;
                }
                var micro = new Y.Template();
                var template = Y.one('#text_spandsp_template').get('value');
                var policy_id_fake = 0;
                for (var j = 0; j < ret.length; j++) {
                    policy_id_fake = policy_id_fake + 1;
                    ret[j].policy_id_fake = policy_id_fake;
                }
                var www = micro.render(template, {data: ret});
                hulkScalingPolicyTableTbody.setHTML(www);
            }
        });

    }

    function getCurrentTime() {
        var currentTime = new Date().getTime();
        return currentTime;
    }

    function writeInOpContent(type, policyId, opTyps, changeDetail, createTime) {
        var dataSend = {
            "type": type,
            "policyId": policyId,
            "user": user,
            "operateType": opTyps,
            "changeDetail": changeDetail,
            "createTime": createTime
        };
        var url_fill_ops = "/hulk/insertPolicyOpRecord";
        Y.io(url_fill_ops, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(dataSend),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.errorCode == 0 || ret.errorMsg == "success") {
                        refreshData();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '更新策略状态插入记录失败', 10);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '访问记录失败', 3);
                }
            }
        });
    }

    // 操作记录 JJ
    function doGetOperations(pageNo) {
        var se = getStartEnd();
        if (!se) return;
        showLogOverlay();
        var entityType = Y.one('#hulk_operatorType').get('value');
        var operator = "";
        entityType = (entityType == "全部策略执行") ? "" : entityType;
        var url = '/hulk/operation/' + appkey + '/log';
        Y.io(url, {
            method: 'get',
            data: {
                pageNo: pageNo,
                pageSize: 20,
                start: se.start,
                end: se.end,
                entityType: entityType,
                operator: operator,
                env: Y.msgp.serviceopt.optHulkEnv()
            },
            on: {
                success: function (id, o) {
                    if(o == null || o.responseText == ""){
                        emptyOrErrorForLog(true);
                    }else {
                        try {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret != null && ret.data != null && ret.data.length > 0 && ret != "") {
                                var data = ret.data;
                                var pobj = ret.page;
                                if (ret.isSuccess) {
                                    if (data && data.length > 0) {
                                        fillOperations(data);
                                        if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                            refreshPaginator(logPbody, pobj);
                                        }
                                    } else {
                                        emptyOrErrorForLog();
                                    }
                                    everPaged = true;
                                    totalPage = pobj.totalPageCount;
                                    totalCount = pobj.totalCount;
                                } else {
                                    emptyOrErrorForLog(true);
                                }
                            } else {
                                emptyOrErrorForLog(true);
                            }
                        }catch (e){
                            emptyOrErrorForLog(true);
                        }
                    }
                },
                failure: function () {
                    emptyOrErrorForLog(true);
                }
            }
        });

    }

    function fillOperations(arr) {
        var micro = new Y.Template();
        var html = micro.render(hulkLogTemplate, {data: arr});
        logTbody.setHTML(html);
        showLogContent();
    }

    function getStartEnd() {
        var obj = {
            start: '',
            end: ''
        };
        var s = startInput.get('value'),
            e = endInput.get('value');
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
        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });
        sdate.on('Datepicker.select', function () {
            refreshData();
        });
        startInput.set('value', Y.mt.date.formatDateByString(yestoday, 'yyyy-MM-dd hh:mm:ss'));
        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });
        edate.on('Datepicker.select', function () {
            refreshData();
        });
        endInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd hh:mm:ss'));
    }

    function initSelector() {
        Y.one("#hulk_operatorType").on('change', function () {
            doGetOperations(1);
        });
    }

    function refreshData() {
        doGetOperations(1);
    }

    function fillSelector(data, type) {
        Y.one("#" + type).empty();
        Y.one("#" + type).append("<option value='全部策略执行' selected='selected'>全部策略执行</option>");
        Y.Array.each(data, function (item) {
            Y.one("#" + type).append('<option value=' + item + '>' + item + '</option>');
            // Y.one("#" + type).append('<option value=' + 1 + '>缩容</option>');
        });
    }

    function showLogContent() {
        $("#hulkpolicy_log_wrap .content-body").show();
        $("#hulkpolicy_log_wrap .content-overlay").hide();
    }

    function showLogOverlay() {
        $("#hulkpolicy_log_wrap .content-body").hide();
        $("#hulkpolicy_log_wrap .content-overlay").show();
    }

    function emptyOrErrorForLog(isError) {
        logWrapper.one('.content-overlay').hide();
        logWrapper.one('.content-body').show();
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a id="emptyOrErrorForLog_refresh" class="get-again">重新获取</a></td></tr>';
        logTbody.setHTML(html);
        logPbody.empty();
        showContent(logWrapper);
        refreshOperations();
    }

    function emptyOrError(isError) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a id="emptyOrError_refresh" class="get-again">没有内容,重新获取</a></td></tr>';
        logWrapper.setHTML(html);
        logPbody.empty();
        showContent(hulkPolicyWrapper);
    }

    function logNull(isError) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a id="logNull_refresh" class="get-again">没有内容,重新获取</a></td></tr>';
        logWrapper.setHTML(html);
    }

    function refreshOperations() {
        $("#emptyOrErrorForLog_refresh").click(function(){
            doGetOperations(1);
        });
    }

    function refreshPaginator(pbody, pobj) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: changePage
        });
    }

    function changePage(params) {
        doGetOperations(params.page);
    }

    //JJ 装换成
    var quota_list = "";

    function handleQuota(receiveQuota) {
        var tem_q = eval("(" + SON.stringify(receiveQuota) + ")");
        $.each(tem_q, function (key, value) {
            quota_list = quota_list + key + " ; ";
        });

    }

    function getTagIdArr(receiveGroup) {
        var tagIdArr = [];
        if (receiveGroup) {
            var tags = Y.JSON.parse(receiveGroup);
            if (tags) {
                for (var tagIndex = 0; tagIndex < tags.length; tagIndex++) {
                    var tag = tags[tagIndex];
                    if (tag && tag.id) {
                        tagIdArr.push(tag.id);
                    }
                }
            }
        }
        return tagIdArr;
    }

    function getTagInfoNameArr(receiveGroup) {
        var tagNameArr = [];
        if (receiveGroup) {
            var tags = Y.JSON.parse(receiveGroup);
            if (tags) {
                for (var tagIndex = 0; tagIndex < tags.length; tagIndex++) {
                    var tag = tags[tagIndex];
                    if (tag && tag.tagName) {
                        tagNameArr.push(tag.tagName);
                    }
                }
            }
        }
        return tagNameArr;
    }

    function exchangeTimeToMinutes(time) {
        if(time == "" || time == null) {
            return -1;
        }
        var _h = parseInt(time.split(":")[0]) * 60;
        var _m = parseInt(time.split(":")[1]);
        return (_h + _m);
    }

    function exchangeMinutesToTime(minutes) {
        var _h = parseInt(parseInt(minutes) / 60);
        var _m = parseInt(parseInt(minutes) % 60);
        if (_h < 10 && _m < 10) {
            return "0" + _h + ":0" + _m;
        } else if (_h < 10) {
            return "0" + _h + ":" + _m;
        } else if (_m < 10) {
            return _h + ":0" + _m;
        } else {
            return _h + ":" + _m;
        }
    }

    function obtainAllIndex() {
        var url = '/hulk/getAllIndex';
        var data = [];
        $.ajax({
            type: "get",
            url: url,
            async: false,
            success: function (ret) {
                data = JSON.parse(ret).data;
            }
        });
        return data;
    }

    function checkRegixTime(time) {
        if(time == "" || time == null) {
            return true;
        }
        var re = /^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/;
        return re.test(time);
    }

    function checkIsAcross(startTimes, endTimes) {
        if (startTimes.length >= 2) {
            for (var i = 1; i < startTimes.length; i++) {
                if (startTimes[i] > endTimes[i]) {
                    Y.msgp.utils.msgpHeaderTip('error', '开始时间大于结束时间', 3);
                    return false;
                } else if(endTimes[i] - startTimes[i] > 180) {
                    Y.msgp.utils.msgpHeaderTip('error', '时间段超过3小时', 3);
                    return false;
                }else {
                    for (var j = 0; j < i; j++) {
                        if ((startTimes[i] > startTimes[j] && startTimes[i] < endTimes[j])
                            || (startTimes[i] < startTimes[j] && endTimes[i] > startTimes[j])
                            || (startTimes[i] > startTimes[j] && endTimes[i] < endTimes[j])
                            || (startTimes[i] < startTimes[j] && endTimes[i] > endTimes[j])
                            || (startTimes[i] == startTimes[j])
                            || (endTimes[i] == endTimes[j])) {
                            Y.msgp.utils.msgpHeaderTip('error', '禁止缩容时间段存在交集', 3);
                            return false;
                        }
                        continue;
                    }
                }
            }

        } else {
            if (startTimes[0] > endTimes[0]) {
                Y.msgp.utils.msgpHeaderTip('error', '开始时间大于结束时间', 3);
                return false;
            } else if(endTimes[0] - startTimes[0] > 180) {
                Y.msgp.utils.msgpHeaderTip('error', '时间段超过3小时', 3);
                return false;
            }
            return true;
        }
        return true;
    }

    Array.prototype.remove = function (val) {
        var index = this.indexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };
    Array.prototype.distinct = function () {
        if (this.length <= 0)return;
        this.sort();
        var re = [this[0]];
        for (var i = 1; i < this.length; i++) {
            if (this[i] !== re[re.length - 1]) {
                re.push(this[i]);
            }
        }
        this.length = re.length;
        for (var i = 0; i < re.length; i++) {
            this[i] = re[i];
        }
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
        'msgp-serviceopt/optHulkPeriodicPolicy',
        'msgp-serviceopt/optHulkTagInfo',
        'msgp-serviceopt/optHulkOperation',
        'msgp-serviceopt/optHulkUtils'
    ]
});
