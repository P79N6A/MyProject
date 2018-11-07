M.add('msgp-serviceopt/optHulkPolicy-version0.0.3', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkPolicy = detailHulkPolicy;
    var inited = false;
    var hulkPolicyWrapper = Y.one('#hulkPolicy_content'),
    //  hulkScalingGroupTableWrapper = hulkPolicyWrapper.one('#hulkScalingGroup_table'),
    //   hulkScalingGroupTableTbody = hulkScalingGroupTableWrapper.one('tbody');
        pbody = hulkPolicyWrapper.one('#manuScale_wrapper');
    var startInput = hulkPolicyWrapper.one('#hulk_start_time'),
        endInput = hulkPolicyWrapper.one('#hulk_end_time');
    var logWrapper = hulkPolicyWrapper.one('#hulkpolicy_log_wrap'),
        logTbody = logWrapper.one('tbody');
    var logPbody = logWrapper.one('#paghulk_wrapper');
    var colspan = 5;
    var hulkScalingPolicyTableWrapper = hulkPolicyWrapper.one('#hulkScalingPolicy_table'),
        hulkScalingPolicyTableTbody = hulkScalingPolicyTableWrapper.one('tbody');
    var hulkPeriodicPolicyTableWrapper = hulkPolicyWrapper.one('#hulkPeriodicPolicy_table'),
        hulkPeriodicPolicyTableTbody = hulkPeriodicPolicyTableWrapper.one('tbody');
    var appkey,
        showOverlay,
        showContent,
        curEnv = 3;
    var addHULKScalingGroupDialog,
        updateScalingGroupDialog;
    var addScalingPolicyDialog,
        updateScalingPolicyDialog,
        addNewScalingPolicyDialog,
        updatePeriodicPolicyDialog,
        addModifySgGroupDialog,
        addNewPeriodicPolicyDialog;
    var selectedIDCs = [];
    var selectedSPUpdateIDCs = [];
    var selectedSPAddIDCs = [];
    var envMap = {3: "prod", 2: "stage", 1: "test"};
    var cpuTypes = [
        {key: 1, value: "1核"},
        {key: 2, value: "2核"},
        {key: 4, value: "4核"},
        {key: 6, value: "6核"},
        {key: 8, value: "8核"},
        {key: 12, value: "12核"},
        {key: 16, value: "16核"}
    ];
    var cpuMap = {"1核": 1, "2核": 2, "4核": 4, "6核": 6, "8核": 8, "12核": 12, "16核": 16};
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
    var memMap = {
        "1G": 1024,
        "2G": 2 * 1024,
        "4G": 4 * 1024,
        "6G": 6 * 1024,
        "8G": 8 * 1024,
        "12G": 12 * 1024,
        "16G": 16 * 1024
    };
    var hdTypes = [
        {key: 50, value: "50G"},
        {key: 100, value: "100G"},
        {key: 200, value: "200G"},
        {key: 300, value: "300G"}
        //{id: 1000, value: "1000G"},
        //{id: 2000, value: "2000G"}
    ];
    var idcsMap = [
        {key: "办公云", value: '办公云'},
        {key: "大兴", value: '大兴'},
        {key: "永丰", value: '永丰'},
        {key: "光环", value: '光环'},
        {key: "次渠", value: '次渠'},
        {key: "上海桂桥", value: '上海桂桥'}
    ]
    var hdMap = {"50G": 50, "100G": 100, "200G": 200, "300G": 300};
    var sgStatus = {0: '待审核', 1: '审核未通过', 2: '初始化中', 3: '运行中', 4: '停止', 5: '暂停中'};
    var sgIDCList = [];
    var realSGIDCList = [];
    var addSGInitData = {};
    addSGInitData.cpuType = cpuTypes[2].value;
    addSGInitData.memType = memTypes[4].value;
    addSGInitData.hdType = hdTypes[3].value;
    //var idcEngToChaMap = {"mos": "办公云", "dx": "大兴", "yf": "永丰", "gh": "光环", "cq": "次渠", "gq": "上海桂桥"};
    var idcChaToEnvMap = {"办公云": "mos", "大兴": "dx", "永丰": "yf", "光环": "gh", "次渠": "cq", "上海桂桥": "gq"};
    var monitorMap = {"平均CPU利用率": 0, "平均内存利用率": 1, "平均单机QPS": 2};
    // var monitorTypes = [
    //     {key: 0, value: "平均CPU利用率"},
    //     {key: 1, value: "平均内存利用率"},
    //     {key: 2, value: "平均单机QPS"}
    // ];
    var monitorTypes = [
        {key: 0, value: "平均单机QPS"},
        // {key: 1, value: "平均单机QPS"},
        // {key: 2, value: "平均单机QPS"}
    ];
    var serviceSGInfo = [];
    var serviceSPInfo = [];
    var peakQPS = 0;
    var peakQPSFlag = false;
    var everPaged = false,
        totalPage,
        totalCount,
        queryAll = true;
    var hulkLogTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-trigger="<%= item %>">',
        '<td><%= Y.mt.date.formatDateByString(new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td><%= item.idc %></td>',
        '<td>弹性策略<%= (item.eventType == 3?"触发扩容":(item.eventType == 7?"触发缩容":item.eventType == 4?"成功扩容":(item.eventType == 8?"成功缩容":item.eventType == 5?"扩容失败":(item.eventType == 9?"缩容失败":"未知操作")))) %> <%= (item.scaleNum<0?(-item.scaleNum):item.scaleNum) %>台实例</td>',
        '<td><%= item.detail %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');
    var operatorType = [
        {key: 0, value: "扩容"},
        {key: 1, value: "缩容"}
    ];
    var idcList = ["大兴", "永丰", "光环", "次渠", "上海桂桥", "月浦","上海徐汇"];
    var periodicPolicyDateList = ["星期一","星期二","星期三","星期四","星期五","星期六","星期日"];
    var periodicPolicyDateMap = {"星期一": 0, "星期二": 1, "星期三": 2,"星期四": 3,"星期五": 4,"星期六": 5,"星期日": 6};
    var periodicPolicyDateToStrMap = {0:"星期一",1:"星期二",2:"星期三",3:"星期四",4:"星期五",5:"星期六",6:"星期日"};
    var idcListOfRecord = [];
    var urlOfRecord = '/hulk/idc/get';
    var isImagineExist = 1;
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
            bindRefresh();
            bindEnvSelect();
            bindHULKPolicy();
            bindAddScalingPolicy();
            initSelector();
            fillSelector('hulk_operatorType', operatorType);
            fillSelector('hulk_idc', idcListOfRecord);
            initDatePicker();
            refreshData();
        }
        //获取数据并显示数据
        var peakQPSUrl = '/hulk/peakQPS/' + appkey + '/prod/get';
        $.ajax({
            type: "get",
            url: peakQPSUrl,
            async: true,
            success: function (ret) {
                peakQPS = ret.data.qpsMax;
                peakQPSFlag = true;
            }
        });
        var urlOfIsImagineExist = '/hulk/checkIsImagineExist/' + appkey + '/' + curEnv + '/get';
        $.ajax({
            type: "get",
            url: urlOfIsImagineExist,
            async: true,
            success: function (ret) {
                isImagineExist = ret.data;
            }
        });
        getHulkScalingPolicyAndGroup(1);
        getHulkPeriodicPolicyAndGroup(1);
    }
    function bindRefresh() {
        hulkPolicyWrapper.delegate('click', function () {
            // getHulkScalingGroup(1);
            getHulkScalingPolicyAndGroup(1);
            getHulkPeriodicPolicyAndGroup(1);
            doGetOperations(1);
        }, '#refreshHulkPolicy');
    }
    function bindEnvSelect() {
        Y.msgp.service.setEnvText('hulkScalingGroup_env_select');
        hulkPolicyWrapper.delegate('click', function () {
            hulkPolicyWrapper.all('#hulkScalingGroup_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            curEnv = Number(this.getAttribute("value"));
            //  getHulkScalingGroup(1);
            getHulkScalingPolicyAndGroup(1);
            getHulkPeriodicPolicyAndGroup(1);
            doGetOperations(1);
            var urlOfIsImagineExist = '/hulk/checkIsImagineExist/' + appkey + '/' + curEnv + '/get';
            $.ajax({
                type: "get",
                url: urlOfIsImagineExist,
                async: true,
                success: function (ret) {
                    isImagineExist = ret.data;
                }
            });
        }, "#hulkScalingGroup_env_select a")
    }
    function bindHULKPolicy() {
        hulkPolicyWrapper.delegate('click', function () {
            // getHulkScalingGroup(1);
            //  getHulkScalingPolicy(1);
            getHulkScalingPolicyAndGroup(1);
            getHulkPeriodicPolicyAndGroup(1);
        }, '#refreshHulkPolicy');
        bindUpdateScalingGroup();
        bindUpdatePeriodicScalingGroup();
        bindAddScalingGroup();
    }
    function bindAddScalingGroup() {
        addHULKScalingGroupDialog = addHULKScalingGroupDialog ? addHULKScalingGroupDialog : new Y.mt.widget.CommonDialog({
            id: 'addHULKScalingGroup_view',
            title: '创建机房配置',
            width: 1024,
            btn: {
                pass: doAddHULKScalingGroup
            }
        });
        hulkPolicyWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            data = Y.JSON.parse(data);
            var initValue = parseInt(peakQPS * 6 / 10);
            var initLowerValue = parseInt(peakQPS * 3 / 10);
            var micro = new Y.Template();
            var template = Y.one('#text_addScalingGroup_dialog').get('value');
            var str = micro.render(template, {
                peakQPS: peakQPS,
                initValue: initValue,
                initLowerValue: initLowerValue,
                sgAndSgData: data
            });
            addHULKScalingGroupDialog.setContent(str);
            initUpdateSPIDCSelector(sgIDCList, realSGIDCList, "s_add_sp_idcs");
            addHULKScalingGroupDialog.show();
            var addBody = addHULKScalingGroupDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');
            $("#s_container_cpu").autocomplete({
                source: cpuTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_container_cpu');
            $("#s_container_mem").autocomplete({
                source: memTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_container_mem');
            $("#s_container_disk").autocomplete({
                source: hdTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_container_disk');
        }, '#addScalingGroup');
    }
    function doAddHULKScalingGroup(btn, container) {
        var urlP = '/hulk/scalingPolicy/' + appkey + '/save';
        getSPUpdateIDCs('s_add_sp_idcs');
        var oldData = Y.one('#addScalingGroup_dialog').getData('info');
        if (Y.Lang.isString(oldData)) {
            oldData = Y.JSON.parse(oldData);
        }
        var schedule = {
            "startDay": 0,
            "endDay": 0,
            "startTime": 0,
            "endTime": 0
        };
        var monitor = {
            "gteType": 0,
            "mType": monitorMap[container.one("#s_update_sp_mtype").get('value')],
            "value": parseInt(container.one("#s_update_sp_value").get('value')),
            "spanName": "",
            "monitorValueLower": parseInt(container.one("#s_update_sp_monitorValueLower").get('value')),
            "enableNonScalein": 0,
            "startTime": 0,
            "endTime": 0
        };
        var newData = {
            "id": oldData.id,
            "appkey": appkey,
            "env": curEnv,
            "idcs": selectedSPUpdateIDCs,
            "pType": oldData.pType,
            "schedule": schedule,
            "monitor": monitor,
            "state": oldData.state,
            "esgNum": parseInt(container.one("#s_update_sp_esgNum").get('value')),
            "esgNumScaleIn": parseInt(container.one("#s_update_sp_esgNumScaleIn").get('value'))
        };
        if (checkSP(newData)) {
            Y.io(urlP, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(newData),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '伸缩策略创建成功', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略创建失败', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '伸缩策略创建失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                }
            });
        }
        var urlG = '/hulk/scalingGroup/' + appkey + '/create';
        var data = {
            "appkey": appkey,
            "env": envMap[curEnv],
            "zone": container.one("#s_idcList").get('value'),
            "serviceType": 0,
            "minInstanceNum": parseInt(container.one("#s_sg_minimumInstance").get('value')),
            "maxInstanceNum": parseInt(container.one("#s_sg_maximumInstance").get('value')),
            "userLogin": "HULK",
            "cpu": cpuMap[container.one("#s_container_cpu").get('value')],
            "mem": memMap[container.one("#s_container_mem").get('value')],
            "hd": hdMap[container.one("#s_container_disk").get('value')],
            "healthCheckFlag": 0,
            "cooldown": parseInt(container.one("#s_sg_cooldown").get('value'))
        };
        if (checkAddSGInfo(data)) {
            Y.io(urlG, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            addHULKScalingGroupDialog.close();
                            //  getHulkScalingGroup(1);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                    }
                }
            });
        }
    }
    //更新弹性策略与伸缩组
    function bindUpdateScalingGroup() {
        updateScalingGroupDialog = updateScalingGroupDialog ? updateScalingGroupDialog : new Y.mt.widget.CommonDialog({
            id: 'updateScalingGroup_view',
            title: '更新策略配置',
            width: 1024,
            btn: {
                pass: doUpdateScalingGroupRatio
            }
        });
        hulkPolicyWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            data = Y.JSON.parse(data);
            var initValue = parseInt(peakQPS * 6 / 10);
            var initLowerValue = parseInt(peakQPS * 3 / 10);
            var micro = new Y.Template();
            var template = Y.one('#text_updateScalingGroup_dialog').get('value');
            var str = micro.render(template, {
                peakQPS: peakQPS,
                initValue: initValue,
                initLowerValue: initLowerValue,
                sgAndSpData: data
            });
            updateScalingGroupDialog.setContent(str);
            //initUpdateSPIDCSelector(sgIDCList, data.policyRow.idcs, "s_update_sp_idcs");
            updateScalingGroupDialog.show();
            var addBody = updateScalingGroupDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');
            $("#s_container_cpu").autocomplete({
                source: cpuTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_container_cpu');
            $("#s_container_mem").autocomplete({
                source: memTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_container_mem');
            $("#s_container_disk").autocomplete({
                source: hdTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_container_disk');
        }, '#updateScalingGroup');
    }
    function doUpdateScalingGroupRatio(btn, container) {
        var urlG = '/hulk/scalingGroup/' + appkey + '/update';
        // getSPUpdateIDCs('s_update_sp_idcs');
        var oldData = Y.one('#updateScalingGroup_dialog').getData('info');
        if (Y.Lang.isString(oldData)) {
            oldData = Y.JSON.parse(oldData);
        }
        var newData = JSON.parse(JSON.stringify(oldData));
        newData.groupRow.minimumInstance = parseInt(container.one("#s_sg_minimumInstance").get('value'));
        newData.groupRow.maximumInstance = parseInt(container.one("#s_sg_maximumInstance").get('value'));
        newData.groupRow.cooldown = parseInt(container.one("#s_sg_cooldown").get('value'));
        newData.groupRow.cpu = cpuMap[container.one("#s_container_cpu").get('value')];
        newData.groupRow.mem = memMap[container.one("#s_container_mem").get('value')];
        newData.groupRow.hd = hdMap[container.one("#s_container_disk").get('value')];
        //编辑菜单中的禁止缩容时间限制jj
        var noScaleinTimeString = container.one("#s_update_sp_noScaleInTime").get('value').toString();
        var noScaleinStartTime = noScaleinTimeString.split("-")[0];
        var noScaleinEndTime = noScaleinTimeString.split("-")[1];
        var startHour = noScaleinStartTime.split(":")[0];
        var startMinute = noScaleinStartTime.split(":")[1];
        var endHour = noScaleinEndTime.split(":")[0];
        var endMinute = noScaleinEndTime.split(":")[1];
        var stJJ = parseInt(startHour) * 60 + parseInt(startMinute);
        var etJJ = parseInt(endHour) * 60 + parseInt(endMinute);
        if (checkSG(oldData.groupRow, newData.groupRow)) {
            Y.io(urlG, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(newData.groupRow),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            // Y.msgp.utils.msgpHeaderTip('success', '机房配置修改成功', 3);
                            var urlP = '/hulk/scalingPolicy/' + appkey + '/save';
                            var schedule = {
                                "startDay": 0,
                                "endDay": 0,
                                "startTime": 0,
                                "endTime": 0
                            };
                            var monitor = {
                                "gteType": 0,
                                "mType": oldData.policyRow.monitor.mType,
                                "value": parseInt(container.one("#s_update_sp_value").get('value')),
                                "spanName": "",
                                "monitorValueLower": parseInt(container.one("#s_update_sp_monitorValueLower").get('value')),
                                "enableNonScalein": 1,
                                "startTime": stJJ,
                                "endTime": etJJ
                            };
                            newidc = oldData.policyRow.idcs;
                            var newDataP = {
                                "id": oldData.policyRow.id,
                                "appkey": appkey,
                                "env": oldData.policyRow.env,
                                "idcs": newidc,
                                "pType": oldData.policyRow.pType,
                                "schedule": schedule,
                                "monitor": monitor,
                                "state": oldData.policyRow.state,
                                "esgNum": parseInt(container.one("#s_update_sp_esgNum").get('value')),
                                "esgNumScaleIn": parseInt(container.one("#s_update_sp_esgNumScaleIn").get('value'))
                            };
                            if (oldData.policyRow.idcs.length > 1) {
                                newidc = [];
                                for (i = 0; i < oldData.policyRow.idcs.length; i++) {
                                    if (oldData.policyRow.idcs[i] != oldData.groupRow.idc) newidc.push(oldData.policyRow.idcs[i]);
                                }
                                newDataP = {
                                    "id": oldData.policyRow.id,
                                    "appkey": appkey,
                                    "env": oldData.policyRow.env,
                                    "idcs": newidc,
                                    "pType": oldData.policyRow.pType,
                                    "schedule": oldData.policyRow.schedule,
                                    "monitor": oldData.policyRow.monitor,
                                    "state": oldData.policyRow.state,
                                    "esgNum": oldData.policyRow.esgNum,
                                    "esgNumScaleIn": oldData.policyRow.esgNumScaleIn
                                };
                            }
                            insertidcs = [];
                            insertidcs.push(oldData.groupRow.idc);
                            var insertData = {
                                "id": 0,
                                "appkey": appkey,
                                "env": oldData.policyRow.env,
                                "idcs": insertidcs,
                                "pType": oldData.policyRow.pType,
                                "schedule": schedule,
                                "monitor": monitor,
                                "state": oldData.policyRow.state,
                                "esgNum": parseInt(container.one("#s_update_sp_esgNum").get('value')),
                                "esgNumScaleIn": parseInt(container.one("#s_update_sp_esgNumScaleIn").get('value'))
                            };
                            if (checkSP(newDataP)) {
                                Y.io(urlP, {
                                    method: 'post',
                                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                                    data: Y.JSON.stringify(newDataP),
                                    on: {
                                        success: function (id, o) {
                                            var ret = Y.JSON.parse(o.responseText);
                                            if (ret.isSuccess) {
                                                //  Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                                                if (oldData.policyRow.idcs.length > 1) {
                                                    Y.io(urlP, {
                                                        method: 'post',
                                                        headers: {'Content-Type': "application/json;charset=UTF-8"},
                                                        data: Y.JSON.stringify(insertData),
                                                        on: {
                                                            success: function (id, o) {
                                                                var ret = Y.JSON.parse(o.responseText);
                                                                if (ret.isSuccess) {
                                                                    // addNewScalingPolicyDialog.close();
                                                                    Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                                                                    getHulkScalingPolicyAndGroup(1);
                                                                    getHulkPeriodicPolicyAndGroup(1);
                                                                } else {
                                                                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '更新失败', 10);
                                                                }
                                                            },
                                                            failure: function () {
                                                                Y.msgp.utils.msgpHeaderTip('error', '更新失败', 3);
                                                            }
                                                        }
                                                    });
                                                    getHulkScalingPolicyAndGroup(1);
                                                    getHulkPeriodicPolicyAndGroup(1);
                                                } else {
                                                    Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                                                    getHulkScalingPolicyAndGroup(1);
                                                    getHulkPeriodicPolicyAndGroup(1);
                                                }
                                            } else {
                                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略更新失败', 3);
                                                getHulkScalingPolicyAndGroup(1);
                                                getHulkPeriodicPolicyAndGroup(1)
                                            }
                                        },
                                        failure: function () {
                                            Y.msgp.utils.msgpHeaderTip('error', '伸缩策略更新失败', 3);
                                            getHulkScalingPolicyAndGroup(1);
                                            getHulkPeriodicPolicyAndGroup(1);
                                        }
                                    }
                                });
                            }
                            getHulkScalingPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '机房配置修改失败', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '机房配置修改失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                }
            });
        }
    }
    function bindUpdatePeriodicScalingGroup() {
        updatePeriodicPolicyDialog = updatePeriodicPolicyDialog ? updatePeriodicPolicyDialog : new Y.mt.widget.CommonDialog({
            id: 'updatePeriodicGroup_view',
            title: '更新策略配置',
            width: 1024,
            btn: {
                pass: doUpdatePeriodicScalingGroupRatio
            }
        });
        hulkPolicyWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            data = Y.JSON.parse(data);
            var initValue = parseInt(peakQPS * 6 / 10);
            var initLowerValue = parseInt(peakQPS * 3 / 10);
            var micro = new Y.Template();
            var template = Y.one('#text_updatePeriodicScalingGroup_dialog').get('value');
            var str = micro.render(template, {
                peakQPS: peakQPS,
                initValue: initValue,
                initLowerValue: initLowerValue,
                sgAndSpData: data
            });
            updatePeriodicPolicyDialog.setContent(str);
            //initUpdateSPIDCSelector(sgIDCList, data.policyRow.idcs, "s_update_sp_idcs");
            updatePeriodicPolicyDialog.show();
            var addBody = updatePeriodicPolicyDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');
            $("#periodic_policy_start_date").autocomplete({
                source: periodicPolicyDateList,
                minLength: 0
            });
            $("#periodic_policy_end_date").autocomplete({
                source: periodicPolicyDateList,
                minLength: 0
            });
            $("#periodic_container_cpu").autocomplete({
                source: cpuTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#periodic_container_cpu');
            $("#periodic_container_mem").autocomplete({
                source: memTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#periodic_container_mem');
            $("#periodic_container_disk").autocomplete({
                source: hdTypes,
                minLength: 0
            });
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#periodic_container_disk');
        }, '#updatePeriodicScalingGroup');
    }
    function doUpdatePeriodicScalingGroupRatio(btn, container) {
        var urlG = '/hulk/scalingGroup/' + appkey + '/update';
        // getSPUpdateIDCs('s_update_sp_idcs');
        var oldData = Y.one('#updatePeriodicGroup_dialog').getData('info');
        if (Y.Lang.isString(oldData)) {
            oldData = Y.JSON.parse(oldData);
        }
        var newData = JSON.parse(JSON.stringify(oldData));
        newData.groupRow.minimumInstance = parseInt(container.one("#periodic_sg_minimumInstance").get('value'));
        newData.groupRow.maximumInstance = parseInt(container.one("#periodic_sg_maximumInstance").get('value'));
        newData.groupRow.cooldown = parseInt(container.one("#periodic_sg_cooldown").get('value'));
        newData.groupRow.cpu = cpuMap[container.one("#periodic_container_cpu").get('value')];
        newData.groupRow.mem = memMap[container.one("#periodic_container_mem").get('value')];
        newData.groupRow.hd = hdMap[container.one("#periodic_container_disk").get('value')];
        if (checkSG(oldData.groupRow, newData.groupRow)) {
            Y.io(urlG, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(newData.groupRow),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            // Y.msgp.utils.msgpHeaderTip('success', '机房配置修改成功', 3);
                            var urlP = '/hulk/scalingPolicy/' + appkey + '/save';
                            var schedule = {
                                "startDay": periodicPolicyDateMap[container.one("#periodic_policy_start_date").get('value')],
                                "endDay": periodicPolicyDateMap[container.one("#periodic_policy_end_date").get('value')],
                                "startTime": parseInt(container.one("#periodic_policy_start_time").get('value').split(":")[0])*60+parseInt(container.one("#periodic_policy_start_time").get('value').split(":")[1]),
                                "endTime": parseInt(container.one("#periodic_policy_end_time").get('value').split(":")[0])*60+parseInt(container.one("#periodic_policy_end_time").get('value').split(":")[1])
                            };
                            var monitor = {
                                "gteType": 0,
                                "mType": 0,
                                "value": 0,
                                "spanName": "",
                                "monitorValueLower": 0,
                                "enableNonScalein": 0,
                                "startTime": 0,
                                "endTime": 0
                            };
                            newidc = oldData.policyRow.idcs;
                            var newDataP = {
                                "id": oldData.policyRow.id,
                                "appkey": appkey,
                                "env": oldData.policyRow.env,
                                "idcs": newidc,
                                "pType": oldData.policyRow.pType,
                                "schedule": schedule,
                                "monitor": monitor,
                                "state": oldData.policyRow.state,
                                "esgNum": parseInt(container.one("#periodic_update_sp_esgNum").get('value')),
                                "esgNumScaleIn": parseInt(container.one("#periodic_update_sp_esgNum").get('value'))
                            };
                            if (oldData.policyRow.idcs.length > 1) {
                                newidc = [];
                                for (i = 0; i < oldData.policyRow.idcs.length; i++) {
                                    if (oldData.policyRow.idcs[i] != oldData.groupRow.idc) newidc.push(oldData.policyRow.idcs[i]);
                                }
                                newDataP = {
                                    "id": oldData.policyRow.id,
                                    "appkey": appkey,
                                    "env": oldData.policyRow.env,
                                    "idcs": newidc,
                                    "pType": oldData.policyRow.pType,
                                    "schedule": oldData.policyRow.schedule,
                                    "monitor": oldData.policyRow.monitor,
                                    "state": oldData.policyRow.state,
                                    "esgNum": oldData.policyRow.esgNum,
                                    "esgNumScaleIn": oldData.policyRow.esgNumScaleIn
                                };
                            }
                            insertidcs = [];
                            insertidcs.push(oldData.groupRow.idc);
                            var insertData = {
                                "id": 0,
                                "appkey": appkey,
                                "env": oldData.policyRow.env,
                                "idcs": insertidcs,
                                "pType": oldData.policyRow.pType,
                                "schedule": schedule,
                                "monitor": monitor,
                                "state": oldData.policyRow.state,
                                "esgNum": parseInt(container.one("#periodic_update_sp_esgNum").get('value')),
                                "esgNumScaleIn": parseInt(container.one("#periodic_update_sp_esgNum").get('value'))
                            };
                            if (checkSP(newDataP)) {
                                Y.io(urlP, {
                                    method: 'post',
                                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                                    data: Y.JSON.stringify(newDataP),
                                    on: {
                                        success: function (id, o) {
                                            var ret = Y.JSON.parse(o.responseText);
                                            if (ret.isSuccess) {
                                                //  Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                                                if (oldData.policyRow.idcs.length > 1) {
                                                    Y.io(urlP, {
                                                        method: 'post',
                                                        headers: {'Content-Type': "application/json;charset=UTF-8"},
                                                        data: Y.JSON.stringify(insertData),
                                                        on: {
                                                            success: function (id, o) {
                                                                var ret = Y.JSON.parse(o.responseText);
                                                                if (ret.isSuccess) {
                                                                    // addNewScalingPolicyDialog.close();
                                                                    Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                                                                    getHulkScalingPolicyAndGroup(1);
                                                                    getHulkPeriodicPolicyAndGroup(1);
                                                                } else {
                                                                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '更新失败', 10);
                                                                }
                                                            },
                                                            failure: function () {
                                                                Y.msgp.utils.msgpHeaderTip('error', '更新失败', 3);
                                                            }
                                                        }
                                                    });
                                                    getHulkScalingPolicyAndGroup(1);
                                                    getHulkPeriodicPolicyAndGroup(1);
                                                } else {
                                                    Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                                                    getHulkScalingPolicyAndGroup(1);
                                                    getHulkPeriodicPolicyAndGroup(1);
                                                }
                                            } else {
                                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略更新失败', 3);
                                                getHulkScalingPolicyAndGroup(1);
                                                getHulkPeriodicPolicyAndGroup(1)
                                            }
                                        },
                                        failure: function () {
                                            Y.msgp.utils.msgpHeaderTip('error', '伸缩策略更新失败', 3);
                                            getHulkScalingPolicyAndGroup(1);
                                            getHulkPeriodicPolicyAndGroup(1);
                                        }
                                    }
                                });
                            }
                            getHulkScalingPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '机房配置修改失败', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '机房配置修改失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                }
            });
        }
    }
    function checkAddSGInfo(data) {
        if (data != undefined && data.minInstanceNum > data.maxInstanceNum) {
            Y.msgp.utils.msgpHeaderTip('error', "机房配置错误：最小实例数不能大于最大实例数", 3);
            return false;
        }
        if (data != undefined && data.zone == "") {
            Y.msgp.utils.msgpHeaderTip('error', "未选择机房", 3);
            return false;
        }
        if (data != undefined && (data.zone.trim() == "请选择机房")) {
            Y.msgp.utils.msgpHeaderTip('error', "新建策略需选择机房：请选择机房", 3);
            return false;
        }
        if (data != undefined && data.minInstanceNum < 0) {
            Y.msgp.utils.msgpHeaderTip('error', "机房配置错误：最小实例数不能小于0", 3);
            return false;
        }
        if (data != undefined && data.zone.trim() != "大兴" && data.zone.trim() != "光环" && data.zone.trim() != "永丰" && data.zone.trim() != "次渠" && data.zone.trim() != "上海桂桥" && data.zone.trim() != "月浦") {
            Y.msgp.utils.msgpHeaderTip('error', "请选择正确机房名称", 3);
            return false;
        }
        if (data != undefined && data.maxInstanceNum > 50) {
            Y.msgp.utils.msgpHeaderTip('error', "最大实例数不能超过50", 3);
            return false;
        }
        return true;
    }
    function bindAddScalingPolicy() {
        hulkPolicyWrapper.delegate('click', function () {
            if (isImagineExist == "imageExist") {
                addNewScalingPolicyDialog = addNewScalingPolicyDialog ? addNewScalingPolicyDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_HULKScalingPolicy_dialog',
                    title: '创建弹性策略 ',
                    width: 1024,
                    btn: {
                        pass: doAddNewHULKScalingPolicy
                    }
                });
                var micro = new Y.Template();
                var template = Y.one('#text_addNewScalingPolicy_form').get('value');
                var initValue = parseInt(peakQPS * 6 / 10);
                var initLowerValue = parseInt(peakQPS * 3 / 10);
                var str = micro.render(template, {
                    peakQPS: peakQPS,
                    initValue: initValue,
                    initLowerValue: initLowerValue,
                    initMtype: monitorTypes[0].value
                });
                addNewScalingPolicyDialog.setContent(str);
                // initIDCSelector(sgIDCList, "s_sp_idcs");
                //initUpdateSPIDCSelector(sgIDCList, realSGIDCList, "s_sp_idcs");
                var addBody = addNewScalingPolicyDialog.getBody();
                $("#s_sp_mtype").autocomplete({
                    source: monitorTypes,
                    minLength: 0,
                    select: function (event, ui) {
                        switch (ui.item.value) {
                            case "平均单机QPS":
                                addBody.one("#s_sp_value").set("value", parseInt(peakQPS * 6 / 10));
                                addBody.one("#s_sp_monitorValueLower").set("value", parseInt(peakQPS * 3 / 10));
                                break;
                            case "平均CPU利用率":
                                addBody.one("#s_sp_value").set("value", 60);
                                addBody.one("#s_sp_monitorValueLower").set("value", 30);
                                break;
                            case "平均内存利用率":
                                addBody.one("#s_sp_value").set("value", 60);
                                addBody.one("#s_sp_monitorValueLower").set("value", 30);
                                break;
                            default:
                                addBody.one("#s_sp_value").set("value", 0);
                                addBody.one("#s_sp_monitorValueLower").set("value", 0);
                        }
                    }
                });
                addBody.detachAll('click');
                addBody.detachAll('change');
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_sp_mtype');
                $("#s_sp_idcs").autocomplete({
                    source: idcList,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_sp_idcs');
                $("#s_container_cpu").autocomplete({
                    source: cpuTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_container_cpu');
                $("#s_container_mem").autocomplete({
                    source: memTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_container_mem');
                $("#s_container_disk").autocomplete({
                    source: hdTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_container_disk');
                $("#s_container_cpu").autocomplete({
                    source: cpuTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_container_cpu');
                $("#s_container_mem").autocomplete({
                    source: memTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_container_mem');
                $("#s_container_disk").autocomplete({
                    source: hdTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_container_disk');
                addNewScalingPolicyDialog.show();
            } else if (isImagineExist == "imageNotExist") {
                Y.msgp.utils.msgpHeaderTip('error', "该服务镜像未创建，请参考新建策略旁边的帮助文档，按要求到plus构建镜像，", 8);
            } else if (isImagineExist == "notOwer") {
                Y.msgp.utils.msgpHeaderTip('error', "创建策略失败：必须是服务负责人", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "创建策略失败", 3);
            }
        }, '#addNewScalingPolicy');
        hulkPolicyWrapper.delegate('click', function () {
            if (isImagineExist == "imageExist") {
                addNewPeriodicPolicyDialog = addNewPeriodicPolicyDialog ? addNewPeriodicPolicyDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_HULKPeriodicPolicy_dialog',
                    title: '创建弹性策略 ',
                    width: 1024,
                    btn: {
                        pass: doAddNewHPeriodicPolicy
                    }
                });
                var micro = new Y.Template();
                var template = Y.one('#text_addNewPeriodicPolicy_form').get('value');
                var initValue = parseInt(peakQPS * 6 / 10);
                var initLowerValue = parseInt(peakQPS * 3 / 10);
                var str = micro.render(template, {
                    peakQPS: peakQPS,
                    initValue: initValue,
                    initLowerValue: initLowerValue,
                    initMtype: monitorTypes[0].value
                });
                addNewPeriodicPolicyDialog.setContent(str);
                // initIDCSelector(sgIDCList, "s_sp_idcs");
                //initUpdateSPIDCSelector(sgIDCList, realSGIDCList, "s_sp_idcs");
                var addBody = addNewPeriodicPolicyDialog.getBody();
                $("#s_sp_mtype").autocomplete({
                    source: monitorTypes,
                    minLength: 0,
                    select: function (event, ui) {
                        switch (ui.item.value) {
                            case "平均单机QPS":
                                addBody.one("#s_sp_value").set("value", parseInt(peakQPS * 6 / 10));
                                addBody.one("#s_sp_monitorValueLower").set("value", parseInt(peakQPS * 3 / 10));
                                break;
                            case "平均CPU利用率":
                                addBody.one("#s_sp_value").set("value", 60);
                                addBody.one("#s_sp_monitorValueLower").set("value", 30);
                                break;
                            case "平均内存利用率":
                                addBody.one("#s_sp_value").set("value", 60);
                                addBody.one("#s_sp_monitorValueLower").set("value", 30);
                                break;
                            default:
                                addBody.one("#s_sp_value").set("value", 0);
                                addBody.one("#s_sp_monitorValueLower").set("value", 0);
                        }
                    }
                });
                addBody.detachAll('click');
                addBody.detachAll('change');
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#s_sp_mtype');
                $("#periodic_policy_start_date").autocomplete({
                    source: periodicPolicyDateList,
                    minLength: 0
                });
                $("#periodic_policy_end_date").autocomplete({
                    source: periodicPolicyDateList,
                    minLength: 0
                });
                $("#periodic_sp_idcs").autocomplete({
                    source: idcList,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_sp_idcs');
                $("#periodic_container_cpu").autocomplete({
                    source: cpuTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_container_cpu');
                $("#periodic_container_mem").autocomplete({
                    source: memTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_container_mem');
                $("#periodic_container_disk").autocomplete({
                    source: hdTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_container_disk');
                $("#periodic_container_cpu").autocomplete({
                    source: cpuTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_container_cpu');
                $("#periodic_container_mem").autocomplete({
                    source: memTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_container_mem');
                $("#periodic_container_disk").autocomplete({
                    source: hdTypes,
                    minLength: 0
                });
                addBody.delegate('change', function () {
                    addBody.one('tbody').all('tr').remove();
                }, '#periodic_container_disk');
                addNewPeriodicPolicyDialog.show();
            } else if (isImagineExist == "imageNotExist") {
                Y.msgp.utils.msgpHeaderTip('error', "该服务镜像未创建，请参考新建策略旁边的帮助文档，按要求到plus构建镜像，", 8);
            } else if (isImagineExist == "notOwer") {
                Y.msgp.utils.msgpHeaderTip('error', "创建策略失败：必须是服务负责人", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "创建策略失败", 3);
            }
        }, '#addNewPeriodicPolicy');
        /**增加修改实例上限的功能sjj*/
        hulkPolicyWrapper.delegate('click', function () {
            var icurEnv = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
            var urlOfIsImagineExist = '/hulk/checkIsImagineExist/' + appkey + '/' + icurEnv + '/get';
            $.ajax({
                type: "get",
                url: urlOfIsImagineExist,
                async: true,
                success: function (ret) {
                    isImagineExist = ret.data;
                }
            });
            if (isImagineExist == "imageExist") {
                addModifySgGroupDialog = addModifySgGroupDialog ? addModifySgGroupDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_ModifySgGroup_dialog',
                    title: '资源自助修改 ',
                    width: 768,
                });
                var micro = new Y.Template();
                var template = Y.one('#text_addModifySgGroup_form').get('value');
                var str = micro.render(template, {
                    appkey: appkey,
                });
                addModifySgGroupDialog.setContent(str);
                //获取Dialog的内容
                getModifySgGroupAndGroup();

                addModifySgGroupDialog.show();
                doUpdateListenerModifyMaxmum();
                doCreateListenerModifyMaxmum();
            } else if (isImagineExist == "imageNotExist") {
                Y.msgp.utils.msgpHeaderTip('error', "该服务镜像未创建，请参考新建策略旁边的帮助文档，按要求到plus构建镜像，", 8);
            } else if (isImagineExist == "notOwer") {
                Y.msgp.utils.msgpHeaderTip('error', "修改失败：必须是服务负责人", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "修改失败", 3);
            }
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
                            getHulkPeriodicPolicyAndGroup(1);
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
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');
            doChangedPolicyEnabled(el, enabled);
        }, '#hulk-policy-one-enabled .btn');
        hulkPolicyWrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');
            doChangedPeriodicPolicyEnabled(el, enabled);
        }, '#hulk-periodic-policy-one-enabled .btn');
        // bindUpdateScalingPolicy();
    }
    //更新最大实例上限sjj
    function doUpdateListenerModifyMaxmum() {
        var commonDialogComponent = addModifySgGroupDialog.getBody();
        commonDialogComponent.delegate('click', function() {
            var btn_id = $(this).attr("id");
            var num_id = btn_id.split("_")[1];
            var idc = commonDialogComponent.one("#somethingIdc_"+num_id).get('value');
            var maxmuminstance = commonDialogComponent.one("#inputMaxmumInstance_"+num_id).get('value');
            var maxmumcpu = commonDialogComponent.one("#inputMaxmumCpu_"+num_id).get('value');
            var maxmummem = commonDialogComponent.one("#inputMaxmumMem_"+num_id).get('value');
            var maxmumhd = commonDialogComponent.one("#inputMaxmumHd_"+num_id).get('value');
            var ioldData = JSON.parse(JSON.stringify(serviceSGInfo_usetoupdate));
            var realData = [];
            var jilu = [];
            var flagjj = false;
            for (var i = 0; i < ioldData.length; i++) {
                if (ioldData[i].idc == idc) {
                    jilu = ioldData[i];
                    flagjj = checkMaxmum(jilu, parseInt(maxmuminstance), parseInt(maxmumcpu), parseInt(maxmummem), parseInt(maxmumhd));
                    if(flagjj){
                        realData.push(ioldData[i]);
                        realData[0].maximumInstance = parseInt(maxmuminstance);
                        realData[0].cpu = parseInt(maxmumcpu);
                        realData[0].mem = parseInt(maxmummem) * 1024;
                        realData[0].hd = parseInt(maxmumhd);
                    }
                    break;
                }
            }
            if(flagjj) {

                //更新数据jj
                Y.io('/hulk/scalingGroup/' + appkey + '/update', {
                    method: 'post',
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(realData[0]),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                addModifySgGroupDialog.close();
                                Y.msgp.utils.msgpHeaderTip('success', ret.msg || '修改机房资源上限成功', 3);
                                getHulkScalingPolicyAndGroup(1);
                                getHulkPeriodicPolicyAndGroup(1);
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '机房配置修改失败', 3);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '资源上限修改失败', 3);
                        }
                    }
                });
            }
            //else{
            //    Y.msgp.utils.msgpHeaderTip('error', '资源上限修改失败', 3);
            //}
        }, "#tr #td_update .btn");
    }
    //在scaling_group中插入一条新纪录sjj
    function doCreateListenerModifyMaxmum() {
        var commonDialogComponent = addModifySgGroupDialog.getBody();
        var ienv = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
        commonDialogComponent.delegate('click', function() {
            var btn_id = $(this).attr("id");
            var num_id = btn_id.split("_")[1];
            var idc = commonDialogComponent.one("#somethingIdc_"+num_id).get('value');
            var maxmuminstance = commonDialogComponent.one("#inputMaxmumInstance_"+num_id).get('value');
            var maxmumcpu = commonDialogComponent.one("#inputMaxmumCpu_"+num_id).get('value');
            var maxmummem = commonDialogComponent.one("#inputMaxmumMem_"+num_id).get('value');
            var maxmumhd = commonDialogComponent.one("#inputMaxmumHd_"+num_id).get('value');
            var dataSJJ_old = {
                "appkey": appkey,
                "env": envMap[ienv],
                "zone": idc,
                "serviceType": 0,
                "minInstanceNum": 0,
                "maxInstanceNum": 6,
                "userLogin": "HULK",
                "cpu": 2,
                "mem": 8 * 1024,
                "hd": 50,
                "healthCheckFlag": 0,
                "cooldown": 120
            };
            var dataSJJ = {
                "appkey": appkey,
                "env": envMap[ienv],
                "zone": idc,
                "serviceType": 0,
                "minInstanceNum": 0,
                "maxInstanceNum": parseInt(maxmuminstance),
                "userLogin": "HULK",
                "cpu": parseInt(maxmumcpu),
                "mem": parseInt(maxmummem) * 1024,
                "hd": parseInt(maxmumhd),
                "healthCheckFlag": 0,
                "cooldown": 120
            };

            if(checkMaxmum(dataSJJ_old, parseInt(maxmuminstance), parseInt(maxmumcpu), parseInt(maxmummem), parseInt(maxmumhd))) {
                //更新数据jj
                Y.io('/hulk/scalingGroup/' + appkey + '/create', {
                    method: 'post',
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(dataSJJ),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                addModifySgGroupDialog.close();
                                Y.msgp.utils.msgpHeaderTip('success', ret.msg || '创建机房成功', 3);
                                getHulkScalingPolicyAndGroup(1);
                                getHulkPeriodicPolicyAndGroup(1);
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                        }
                    }
                });
            }
        }, "#tr #td_create .btn");
    }
    //检查最大实例数值/cpu/mem/hd是否正确sjj
    function checkMaxmum(oldData, maximumInstance, cpu, mem, hd) {
        if (maximumInstance > oldData.maximumInstance && maximumInstance > 20) {
            Y.msgp.utils.msgpHeaderTip('error', "最大实例数<=20时任意调整，大于20时只允许向下调整", 3);
            return false;
        }
        if (maximumInstance > 50) {
            Y.msgp.utils.msgpHeaderTip('error', "最大实例数不能超过50", 3);
            return false;
        }
        if (parseInt(maximumInstance) < 0) {
            Y.msgp.utils.msgpHeaderTip('error', "最小实例数设置为正整数", 3);
            return false;
        }
        if (cpu > oldData.cpu && cpu > 8) {
            Y.msgp.utils.msgpHeaderTip('error', "容器CPU配置<=8核时任意调整，大于8核时只允许向下调整", 3);
            return false;
        }
        if (mem * 1024 > oldData.mem && mem > 12 ) {
            Y.msgp.utils.msgpHeaderTip('error', "容器内存配置<=12G时任意调整，大于12G时只允许向下调整", 3);
            return false;
        }
        if (hd > oldData.hd && hd > 1000) {
            Y.msgp.utils.msgpHeaderTip('error', "容器硬盘配置<=1000G时任意调整，大于1000G时只允许向下调整", 3);
            return false;
        }
        return true;
    }
    //添加弹性策略与伸缩组
    function doAddNewHULKScalingPolicy(btn, container) {
        //处理时间jj
        var noScaleinTimeString = container.one("#s_sp_noScaleInTime").get('value').toString();
        var noScaleinStartTime = noScaleinTimeString.split("-")[0];
        var noScaleinEndTime = noScaleinTimeString.split("-")[1];
        var startHour = noScaleinStartTime.split(":")[0];
        var startMinute = noScaleinStartTime.split(":")[1];
        var endHour = noScaleinEndTime.split(":")[0];
        var endMinute = noScaleinEndTime.split(":")[1];
        var stJJ = parseInt(startHour) * 60 + parseInt(startMinute);
        var etJJ = parseInt(endHour) * 60 + parseInt(endMinute);
        var urlP = '/hulk/scalingPolicy/' + appkey + '/save';
        // getSPAddIDCs();
        var schedule = {
            "startDay": 0,
            "endDay": 0,
            "startTime": 0,
            "endTime": 0
        };
        var monitor = {
            "gteType": 0,
            "mType": monitorMap[container.one("#s_sp_mtype").get('value')],
            "value": parseInt(container.one("#s_sp_value").get('value')),
            "spanName": "",
            "monitorValueLower": parseInt(container.one("#s_sp_monitorValueLower").get('value')),
            "enableNonScalein": 1,
            "startTime": stJJ,
            "endTime": etJJ
        };
        var dataP = {
            "id": 0,
            "appkey": appkey,
            "env": curEnv,
            "idcs": [container.one("#s_sp_idcs").get('value')],
            "pType": 1,
            "schedule": schedule,
            "monitor": monitor,
            // "state": parseInt(container.one("#s_sp_state").get('value')),
            "state": 0,
            "esgNum": parseInt(container.one("#s_sp_esgNum").get('value')),
            "esgNumScaleIn": parseInt(container.one("#s_sp_esgNumScaleIn").get('value'))
        };
        var urlG = '/hulk/scalingGroup/' + appkey + '/create';
        var dataG = {
            "appkey": appkey,
            "env": envMap[curEnv],
            "zone": container.one("#s_sp_idcs").get('value'),
            "serviceType": 0,
            "minInstanceNum": parseInt(container.one("#s_sg_minimumInstance").get('value')),
            "maxInstanceNum": parseInt(container.one("#s_sg_maximumInstance").get('value')),
            "userLogin": "HULK",
            "cpu": cpuMap[container.one("#s_container_cpu").get('value')],
            "mem": memMap[container.one("#s_container_mem").get('value')],
            "hd": hdMap[container.one("#s_container_disk").get('value')],
            "healthCheckFlag": 0,
            "cooldown": parseInt(container.one("#s_sg_cooldown").get('value'))
        };
        if (checkAddSGInfo(dataG) && checkSP(dataP)) {
            Y.io(urlG, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(dataG),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            addHULKScalingGroupDialog.close();
                            if (checkSP(dataP)) {
                                Y.io(urlP, {
                                    method: 'post',
                                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                                    data: Y.JSON.stringify(dataP),
                                    on: {
                                        success: function (id, o) {
                                            var ret = Y.JSON.parse(o.responseText);
                                            if (ret.isSuccess) {
                                                addNewScalingPolicyDialog.close();
                                                Y.msgp.utils.msgpHeaderTip('success', '伸缩策略新建成功', 3);
                                                getHulkScalingPolicyAndGroup(1);
                                                getHulkPeriodicPolicyAndGroup(1);
                                            } else {
                                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                                            }
                                        },
                                        failure: function () {
                                            Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                                        }
                                    }
                                });
                            }
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        } else if (ret.msg == "伸缩组已经存在" || ret.msg == "sgId: -1") {     //更新机房增加策略
                            var url = '/hulk/scalingGroup/' + appkey + '/' + curEnv + '/get';
                            Y.io(url, {
                                method: 'get',
                                on: {
                                    success: function (id, o) {
                                        var ret = Y.JSON.parse(o.responseText);
                                        if (ret.isSuccess) {
                                            var data = ret.data;
                                            serviceSGInfo = JSON.parse(JSON.stringify(data));
                                            newSgInfo = [];
                                            for (i = 0; i < serviceSGInfo.length; i++) {
                                                if (serviceSGInfo[i].idc == dataG.zone) {
                                                    newSgInfo.push(serviceSGInfo[i]);
                                                    newSgInfo[0].minimumInstance = parseInt(container.one("#s_sg_minimumInstance").get('value'));
                                                    newSgInfo[0].maximumInstance = parseInt(container.one("#s_sg_maximumInstance").get('value'));
                                                    newSgInfo[0].cooldown = parseInt(container.one("#s_sg_cooldown").get('value'));
                                                    newSgInfo[0].cpu = cpuMap[container.one("#s_container_cpu").get('value')];
                                                    newSgInfo[0].mem = memMap[container.one("#s_container_mem").get('value')];
                                                    newSgInfo[0].hd = hdMap[container.one("#s_container_disk").get('value')];
                                                }
                                            }
                                            Y.io('/hulk/scalingGroup/' + appkey + '/update', {
                                                method: 'post',
                                                headers: {'Content-Type': "application/json;charset=UTF-8"},
                                                data: Y.JSON.stringify(newSgInfo[0]),
                                                on: {
                                                    success: function (id, o) {
                                                        var ret = Y.JSON.parse(o.responseText);
                                                        if (ret.isSuccess) {
                                                            addHULKScalingGroupDialog.close();
                                                            if (checkSP(dataP)) {
                                                                Y.io(urlP, {
                                                                    method: 'post',
                                                                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                                                                    data: Y.JSON.stringify(dataP),
                                                                    on: {
                                                                        success: function (id, o) {
                                                                            var ret = Y.JSON.parse(o.responseText);
                                                                            if (ret.isSuccess) {
                                                                                addNewScalingPolicyDialog.close();
                                                                                Y.msgp.utils.msgpHeaderTip('success', '伸缩策略新建成功', 3)
                                                                                getHulkScalingPolicyAndGroup(1);
                                                                                getHulkPeriodicPolicyAndGroup(1);
                                                                            } else {
                                                                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                                                                            }
                                                                        },
                                                                        failure: function () {
                                                                            Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        } else {
                                                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '机房配置修改失败', 3);
                                                            getHulkScalingPolicyAndGroup(1);
                                                            getHulkPeriodicPolicyAndGroup(1);
                                                        }
                                                    },
                                                    failure: function () {
                                                        Y.msgp.utils.msgpHeaderTip('error', '机房配置修改失败', 3);
                                                        getHulkScalingPolicyAndGroup(1);
                                                        getHulkPeriodicPolicyAndGroup(1);
                                                    }
                                                }
                                            })
                                        } else {
                                            emptyOrError(true);
                                        }
                                    },
                                    failure: function () {
                                        emptyOrError(true);
                                    }
                                }
                            });
                        }
                        else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                    }
                }
            });
        }
        return true;
    }
    function doAddNewHPeriodicPolicy(btn, container) {
        var urlP = '/hulk/scalingPolicy/' + appkey + '/save';
        // getSPAddIDCs();
        var schedule = {
            "startDay": periodicPolicyDateMap[container.one("#periodic_policy_start_date").get('value')],
            "endDay": periodicPolicyDateMap[container.one("#periodic_policy_end_date").get('value')],
            "startTime": parseInt(container.one("#periodic_policy_start_time").get('value').split(":")[0])*60+parseInt(container.one("#periodic_policy_start_time").get('value').split(":")[1]),
            "endTime": parseInt(container.one("#periodic_policy_end_time").get('value').split(":")[0])*60+parseInt(container.one("#periodic_policy_end_time").get('value').split(":")[1])
        };
        var monitor = {
            "gteType": 0,
            "mType": 0,
            "value":0,
            "spanName": "",
            "monitorValueLower": 0,
            "enableNonScalein": 0,
            "startTime": 0,
            "endTime": 0
        };
        var dataP = {
            "id": 0,
            "appkey": appkey,
            "env": curEnv,
            "idcs": [container.one("#periodic_sp_idcs").get('value')],
            "pType": 0,
            "schedule": schedule,
            "monitor": monitor,
            // "state": parseInt(container.one("#s_sp_state").get('value')),
            "state": 0,
            "esgNum": parseInt(container.one("#periodic_policy_scaleNum_value").get('value')),
            "esgNumScaleIn": parseInt(container.one("#periodic_policy_scaleNum_value").get('value'))
        };
        var urlG = '/hulk/scalingGroup/' + appkey + '/create';
        var dataG = {
            "appkey": appkey,
            "env": envMap[curEnv],
            "zone": container.one("#periodic_sp_idcs").get('value'),
            "serviceType": 0,
            "minInstanceNum": parseInt(container.one("#periodic_sg_minimumInstance").get('value')),
            "maxInstanceNum": parseInt(container.one("#periodic_sg_maximumInstance").get('value')),
            "userLogin": "HULK",
            "cpu": cpuMap[container.one("#periodic_container_cpu").get('value')],
            "mem": memMap[container.one("#periodic_container_mem").get('value')],
            "hd": hdMap[container.one("#periodic_container_disk").get('value')],
            "healthCheckFlag": 0,
            "cooldown": parseInt(container.one("#periodic_sg_cooldown").get('value'))
        };
        if (checkAddSGInfo(dataG) && checkSP(dataP)) {
            Y.io(urlG, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(dataG),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            addNewPeriodicPolicyDialog.close();
                            if (checkSP(dataP)) {
                                Y.io(urlP, {
                                    method: 'post',
                                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                                    data: Y.JSON.stringify(dataP),
                                    on: {
                                        success: function (id, o) {
                                            var ret = Y.JSON.parse(o.responseText);
                                            if (ret.isSuccess) {
                                                addNewPeriodicPolicyDialog.close();
                                                Y.msgp.utils.msgpHeaderTip('success', '伸缩策略新建成功', 3);
                                                getHulkScalingPolicyAndGroup(1);
                                                getHulkPeriodicPolicyAndGroup(1);
                                            } else {
                                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                                            }
                                        },
                                        failure: function () {
                                            Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                                        }
                                    }
                                });
                            }
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        } else if (ret.msg == "伸缩组已经存在" || ret.msg == "sgId: -1") {     //更新机房增加策略
                            var url = '/hulk/scalingGroup/' + appkey + '/' + curEnv + '/get';
                            Y.io(url, {
                                method: 'get',
                                on: {
                                    success: function (id, o) {
                                        var ret = Y.JSON.parse(o.responseText);
                                        if (ret.isSuccess) {
                                            var data = ret.data;
                                            serviceSGInfo = JSON.parse(JSON.stringify(data));
                                            newSgInfo = [];
                                            for (i = 0; i < serviceSGInfo.length; i++) {
                                                if (serviceSGInfo[i].idc == dataG.zone) {
                                                    newSgInfo.push(serviceSGInfo[i]);
                                                    newSgInfo[0].minimumInstance = parseInt(container.one("#periodic_sg_minimumInstance").get('value'));
                                                    newSgInfo[0].maximumInstance = parseInt(container.one("#periodic_sg_maximumInstance").get('value'));
                                                    newSgInfo[0].cooldown = parseInt(container.one("#periodic_sg_cooldown").get('value'));
                                                    newSgInfo[0].cpu = cpuMap[container.one("#periodic_container_cpu").get('value')];
                                                    newSgInfo[0].mem = memMap[container.one("#periodic_container_mem").get('value')];
                                                    newSgInfo[0].hd = hdMap[container.one("#periodic_container_disk").get('value')];
                                                }
                                            }
                                            Y.io('/hulk/scalingGroup/' + appkey + '/update', {
                                                method: 'post',
                                                headers: {'Content-Type': "application/json;charset=UTF-8"},
                                                data: Y.JSON.stringify(newSgInfo[0]),
                                                on: {
                                                    success: function (id, o) {
                                                        var ret = Y.JSON.parse(o.responseText);
                                                        if (ret.isSuccess) {
                                                            addNewPeriodicPolicyDialog.close();
                                                            if (checkSP(dataP)) {
                                                                Y.io(urlP, {
                                                                    method: 'post',
                                                                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                                                                    data: Y.JSON.stringify(dataP),
                                                                    on: {
                                                                        success: function (id, o) {
                                                                            var ret = Y.JSON.parse(o.responseText);
                                                                            if (ret.isSuccess) {
                                                                                addNewPeriodicPolicyDialog.close();
                                                                                Y.msgp.utils.msgpHeaderTip('success', '伸缩策略新建成功', 3)
                                                                                getHulkScalingPolicyAndGroup(1);
                                                                                getHulkPeriodicPolicyAndGroup(1);
                                                                            } else {
                                                                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                                                                            }
                                                                        },
                                                                        failure: function () {
                                                                            Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        } else {
                                                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '机房配置修改失败', 3);
                                                            getHulkScalingPolicyAndGroup(1);
                                                            getHulkPeriodicPolicyAndGroup(1);
                                                        }
                                                    },
                                                    failure: function () {
                                                        Y.msgp.utils.msgpHeaderTip('error', '机房配置修改失败', 3);
                                                        getHulkScalingPolicyAndGroup(1);
                                                        getHulkPeriodicPolicyAndGroup(1);
                                                    }
                                                }
                                            })
                                        } else {
                                            emptyOrError(true);
                                        }
                                    },
                                    failure: function () {
                                        emptyOrError(true);
                                    }
                                }
                            });
                        }
                        else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                    }
                }
            });
        }
        return true;
    }
    function doAddHULKScalingPolicy(btn, container) {
        var url = '/hulk/scalingPolicy/' + appkey + '/save';
        getSPUpdateIDCs('s_add_sp_idcs');
        var oldData = Y.one('#addScalingPolicy_dialog').getData('info');
        if (Y.Lang.isString(oldData)) {
            oldData = Y.JSON.parse(oldData);
        }
        var schedule = {
            "startDay": 0,
            "endDay": 0,
            "startTime": 0,
            "endTime": 0
        };
        var monitor = {
            "gteType": 0,
            "mType": monitorMap[container.one("#s_update_sp_mtype").get('value')],
            "value": parseInt(container.one("#s_update_sp_value").get('value')),
            "spanName": "",
            "monitorValueLower": parseInt(container.one("#s_update_sp_monitorValueLower").get('value')),
            "enableNonScalein": 0,
            "startTime": 0,
            "endTime": 0
        };
        var newData = {
            "id": oldData.id,
            "appkey": appkey,
            "env": curEnv,
            "idcs": selectedSPUpdateIDCs,
            "pType": oldData.pType,
            "schedule": schedule,
            "monitor": monitor,
            "state": oldData.state,
            "esgNum": parseInt(container.one("#s_update_sp_esgNum").get('value')),
            "esgNumScaleIn": parseInt(container.one("#s_update_sp_esgNumScaleIn").get('value'))
        };
        if (checkSP(newData)) {
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(newData),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '伸缩策略创建成功', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略创建失败', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '伸缩策略创建失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                }
            });
        }
    }
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
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            data = Y.JSON.parse(data);
            var initValue = parseInt(peakQPS * 6 / 10);
            var initLowerValue = parseInt(peakQPS * 3 / 10);
            var micro = new Y.Template();
            var template = Y.one('#text_updateScalingPolicy_dialog').get('value');
            var str = micro.render(template, {
                peakQPS: peakQPS,
                initValue: initValue,
                initLowerValue: initLowerValue,
                spData: data
            });
            updateScalingPolicyDialog.setContent(str);
            initUpdateSPIDCSelector(sgIDCList, data.idcs, "s_update_sp_idcs");
            updateScalingPolicyDialog.show();
        }, '#updateScalingPolicy');
    }
    function doUpdateScalingPolicyRatio(btn, container) {
        var url = '/hulk/scalingPolicy/' + appkey + '/save';
        getSPUpdateIDCs('s_update_sp_idcs');
        var oldData = Y.one('#updateScalingPolicy_dialog').getData('info');
        if (Y.Lang.isString(oldData)) {
            oldData = Y.JSON.parse(oldData);
        }
        var schedule = {
            "startDay": 0,
            "endDay": 0,
            "startTime": 0,
            "endTime": 0
        };
        var monitor = {
            "gteType": 0,
            "mType": oldData.monitor.mType,
            "value": parseInt(container.one("#s_update_sp_value").get('value')),
            "spanName": "",
            "monitorValueLower": parseInt(container.one("#s_update_sp_monitorValueLower").get('value')),
            "enableNonScalein": 0,
            "startTime": 0,
            "endTime": 0
        };
        var newData = {
            "id": oldData.id,
            "appkey": appkey,
            "env": oldData.env,
            "idcs": selectedSPUpdateIDCs,
            "pType": oldData.pType,
            "schedule": schedule,
            "monitor": monitor,
            "state": oldData.state,
            "esgNum": parseInt(container.one("#s_update_sp_esgNum").get('value')),
            "esgNumScaleIn": parseInt(container.one("#s_update_sp_esgNumScaleIn").get('value'))
        };
        if (checkSP(newData)) {
            Y.io(url, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(newData),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '伸缩策略更新成功', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略更新失败', 3);
                            getHulkScalingPolicyAndGroup(1);
                            getHulkPeriodicPolicyAndGroup(1);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '伸缩策略更新失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                }
            });
        }
    }
    function doChangedPolicyEnabled(el, enabledd) {
        var url = '/hulk/scalingPolicy/' + appkey + '/save';
        var line = el.ancestor('tr');
        var data = line.getData('info');
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        //启用停用jj
        var AStartTime = parseInt(data.policyRow.monitor.startTime.toString().split(":")[0])*60+parseInt(data.policyRow.monitor.startTime.toString().split(":")[1]);
        var BEndTime = parseInt(data.policyRow.monitor.endTime.toString().split(":")[0])*60+parseInt(data.policyRow.monitor.endTime.toString().split(":")[1]);
        data.policyRow.state = enabledd;
        data.policyRow.monitor.startTime = AStartTime;
        data.policyRow.monitor.endTime = BEndTime;
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data.policyRow),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '伸缩策略修改成功', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略修改失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '伸缩策略修改失败', 3);
                    getHulkScalingPolicyAndGroup(1);
                    getHulkPeriodicPolicyAndGroup(1);
                }
            }
        });
        return true;
    }
    function doChangedPeriodicPolicyEnabled(el, enabledd) {
        var url = '/hulk/scalingPolicy/' + appkey + '/save';
        var line = el.ancestor('tr');
        var data = line.getData('info');
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        data.policyRow.schedule.startDay = periodicPolicyDateMap[data.policyRow.schedule.startDay];
        data.policyRow.schedule.endDay = periodicPolicyDateMap[data.policyRow.schedule.endDay];
        data.policyRow.schedule.startTime = parseInt((data.policyRow.schedule.startTime).split(":")[0]*60)+parseInt((data.policyRow.schedule.startTime).split(":")[1]);
        data.policyRow.schedule.endTime = parseInt((data.policyRow.schedule.endTime).split(":")[0]*60)+parseInt((data.policyRow.schedule.endTime).split(":")[1]);
        data.policyRow.state = enabledd;
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data.policyRow),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '伸缩策略修改成功', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '伸缩策略修改失败', 3);
                        getHulkScalingPolicyAndGroup(1);
                        getHulkPeriodicPolicyAndGroup(1);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '伸缩策略修改失败', 3);
                    getHulkScalingPolicyAndGroup(1);
                    getHulkPeriodicPolicyAndGroup(1);
                }
            }
        });
        return true;
    }
    //获取弹性策略和伸缩组的信息
    function getHulkScalingPolicyAndGroup(pageNo) {
        showContent(hulkPolicyWrapper);
        var env = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
        var url = '/hulk/scalingPolicyAndGroup/' + appkey + '/' + env + '/get';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (data.length > 0) {
                            serviceSPInfo = JSON.parse(JSON.stringify(data));
                            fillHULKScalingPolicyAndGroupTable(data);
                        } else {
                            if (peakQPSFlag) {
                                var schedule = {
                                    "endDay": 0,
                                    "endTime": 0,
                                    "startDay": 0,
                                    "startTime": 0
                                };
                                var monitor = {
                                    "gteType": 0,
                                    "mType": 2,
                                    "value": parseInt(peakQPS * 6 / 10),
                                    "spanName": "",
                                    "monitorValueLower": parseInt(peakQPS * 3 / 10),
                                    "enableNonScalein": 0,
                                    "startTime": 0,
                                    "endTime": 0
                                };
                                var idcs = realSGIDCList.length > 0 ? realSGIDCList : sgIDCList;
                                var fakePolicy = {
                                    "id": 0,
                                    "appkey": appkey,
                                    "env": env,
                                    "idcs": idcs,
                                    "pType": 1,
                                    "schedule": schedule,
                                    "monitor": monitor,
                                    "state": 1,
                                    "esgNum": 3,
                                    "esgNumScaleIn": 1,
                                    "fakeFlag": 1
                                };
                                var fakePolicyList = [];
                                fakePolicyList.push(fakePolicy);
                                a = [];
                                fillHULKScalingPolicyAndGroupTable(a);
                            } else {
                                // getPeakQPS  sgIDCList
                                var url = '/hulk/peakQPS/' + appkey + '/prod/get';
                                Y.io(url, {
                                    method: 'get',
                                    on: {
                                        success: function (id, o) {
                                            var ret = Y.JSON.parse(o.responseText);
                                            if (ret.isSuccess) {
                                                peakQPS = ret.data;
                                                peakQPSFlag = true;
                                                var schedule = {
                                                    "endDay": 0,
                                                    "endTime": 0,
                                                    "startDay": 0,
                                                    "startTime": 0
                                                };
                                                var monitor = {
                                                    "gteType": 0,
                                                    "mType": 2,
                                                    "value": parseInt(peakQPS * 6 / 10),
                                                    "spanName": "",
                                                    "monitorValueLower": parseInt(peakQPS * 3 / 10),
                                                    "enableNonScalein": 0,
                                                    "startTime": 0,
                                                    "endTime": 0
                                                };
                                                var fakePolicy = {
                                                    "id": 0,
                                                    "appkey": appkey,
                                                    "env": env,
                                                    "idcs": sgIDCList,
                                                    "pType": 1,
                                                    "schedule": schedule,
                                                    "monitor": monitor,
                                                    "state": 1,
                                                    "esgNum": 3,
                                                    "esgNumScaleIn": 1,
                                                    "fakeFlag": 1
                                                };
                                                var fakePolicyList = [];
                                                fakePolicyList.push(fakePolicy);
                                                a = [];
                                                fillHULKScalingPolicyAndGroupTable(a);
                                            }
                                        },
                                        failure: function () {
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }
    function getHulkPeriodicPolicyAndGroup(pageNo) {
        showContent(hulkPolicyWrapper);
        var env = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
        var url = '/hulk/periodicPolicyAndGroup/' + appkey + '/' + env + '/get';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (data.length > 0) {
                            serviceSPInfo = JSON.parse(JSON.stringify(data));
                            var i = 0;
                            for(i=0;i<data.length;i++){
                                data[i].policyRow.schedule.startDay = periodicPolicyDateToStrMap[data[i].policyRow.schedule.startDay];
                                data[i].policyRow.schedule.endDay = periodicPolicyDateToStrMap[data[i].policyRow.schedule.endDay];
                                data[i].policyRow.schedule.startTime = parseInt((data[i].policyRow.schedule.startTime/60))+":"+(data[i].policyRow.schedule.startTime%60<10?("0"+data[i].policyRow.schedule.startTime%60):(data[i].policyRow.schedule.startTime%60));
                                data[i].policyRow.schedule.endTime = parseInt((data[i].policyRow.schedule.endTime/60))+":"+(data[i].policyRow.schedule.endTime%60<10?("0"+data[i].policyRow.schedule.endTime%60):(data[i].policyRow.schedule.endTime%60) );
                            }
                            fillHULKPeriodicPolicyAndGroupTable(data);
                        } else {
                            if (peakQPSFlag) {
                                var schedule = {
                                    "endDay": 0,
                                    "endTime": 0,
                                    "startDay": 0,
                                    "startTime": 0
                                };
                                var monitor = {
                                    "gteType": 0,
                                    "mType": 2,
                                    "value": parseInt(peakQPS * 6 / 10),
                                    "spanName": "",
                                    "monitorValueLower": parseInt(peakQPS * 3 / 10),
                                    "enableNonScalein": 0,
                                    "startTime": 0,
                                    "endTime": 0
                                };
                                var idcs = realSGIDCList.length > 0 ? realSGIDCList : sgIDCList;
                                var fakePolicy = {
                                    "id": 0,
                                    "appkey": appkey,
                                    "env": env,
                                    "idcs": idcs,
                                    "pType": 1,
                                    "schedule": schedule,
                                    "monitor": monitor,
                                    "state": 1,
                                    "esgNum": 3,
                                    "esgNumScaleIn": 1,
                                    "fakeFlag": 1
                                };
                                var fakePolicyList = [];
                                fakePolicyList.push(fakePolicy);
                                a = [];
                                fillHULKPeriodicPolicyAndGroupTable(a);
                            } else {
                                // getPeakQPS  sgIDCList
                                var url = '/hulk/peakQPS/' + appkey + '/prod/get';
                                Y.io(url, {
                                    method: 'get',
                                    on: {
                                        success: function (id, o) {
                                            var ret = Y.JSON.parse(o.responseText);
                                            if (ret.isSuccess) {
                                                peakQPS = ret.data;
                                                peakQPSFlag = true;
                                                var schedule = {
                                                    "endDay": 0,
                                                    "endTime": 0,
                                                    "startDay": 0,
                                                    "startTime": 0
                                                };
                                                var monitor = {
                                                    "gteType": 0,
                                                    "mType": 2,
                                                    "value": parseInt(peakQPS * 6 / 10),
                                                    "spanName": "",
                                                    "monitorValueLower": parseInt(peakQPS * 3 / 10),
                                                    "enableNonScalein": 0,
                                                    "startTime": 0,
                                                    "endTime": 0
                                                };
                                                var fakePolicy = {
                                                    "id": 0,
                                                    "appkey": appkey,
                                                    "env": env,
                                                    "idcs": sgIDCList,
                                                    "pType": 1,
                                                    "schedule": schedule,
                                                    "monitor": monitor,
                                                    "state": 1,
                                                    "esgNum": 3,
                                                    "esgNumScaleIn": 1,
                                                    "fakeFlag": 1
                                                };
                                                var fakePolicyList = [];
                                                fakePolicyList.push(fakePolicy);
                                                a = [];
                                                fillHULKPeriodicPolicyAndGroupTable(a);
                                            }
                                        },
                                        failure: function () {
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }
    var serviceSGInfo_usetoupdate = [];
    //获取appkey所部署的机房列表以及各机房对应的实例上限sjj
    function getModifySgGroupAndGroup() {
        var ienv = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
        var url = '/hulk/scalingGroup/' + appkey + "/"+ ienv +"/get";
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        serviceSGInfo_usetoupdate = JSON.parse(JSON.stringify(data));
                        var micro = new Y.Template();
                        var template = Y.one('#idc_info').get('value');
                        var str = micro.render(template, {data: serviceSGInfo_usetoupdate});
                        $("#tbody_sg").html(str);
                        //找出没有部署服务的机房
                        var youidcs = [];
                        var leftIdcs = [];
                        for(var k=0; k<serviceSGInfo_usetoupdate.length; k++){
                            youidcs[k] = serviceSGInfo_usetoupdate[k].idc;
                        }

                        var myIdcs = ["大兴", "永丰", "光环", "次渠", "上海桂桥", "上海徐汇", "月浦"];
                        var countI = 0;
                        for(var i=0; i<myIdcs.length; i++){
                            //没有部署的idc
                            if(youidcs.indexOf(myIdcs[i]) == -1) {
                                var serviceSGInfo_usetoupdate1 =
                                {
                                    "id":50000 + countI,
                                    "appkey":appkey,
                                    "idc":myIdcs[i],
                                    "env":ienv,
                                    "userLogin":"HULK",
                                    "status":0,
                                    "maximumInstance":6,
                                    "minimumInstance":0,
                                    "desireInstance":0,
                                    "cooldown":120,
                                    "created":0,
                                    "runtime":0,
                                    "cpu":2,
                                    "mem":8192,
                                    "hd":50,
                                    "followers":[],
                                    "defaultImageId":null,
                                    "healthCheckFlag":null,
                                    "imagetags":[],
                                    "fakeFlag":1
                                };
                                leftIdcs[countI] = serviceSGInfo_usetoupdate1;
                                countI = countI + 1;
                            }
                        }
                        template = Y.one('#idc_info_not').get('value');
                        var str_not = micro.render(template, {data: leftIdcs});
                        $("#tbody_sg_not").html(str_not);
                    }
                },
                failure: function () {
                    alert("error");
                }
            }
        });
    }
    function getServicePeakQPS() {
        var url = '/huk/peakQPS/' + appkey + '/prod/get';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        peakQPS = ret.data;
                        peakQPSFlag = true;
                    }
                },
                failure: function () {
                }
            }
        });
    }
    function checkSG(oldData, newData) {
        if (newData.maximumInstance > oldData.maximumInstance && newData.maximumInstance > 20) {
            Y.msgp.utils.msgpHeaderTip('error', "最大实例数<=20时任意调整，大于20时只允许向下调整", 3);
            return false;
        }
        if (newData.maximumInstance > 50) {
            Y.msgp.utils.msgpHeaderTip('error', "最大实例数不能超过50", 3);
            return false;
        }
        if (parseInt(newData.minimumInstance) < 0) {
            Y.msgp.utils.msgpHeaderTip('error', "最小实例数设置为正整数", 3);
            return false;
        }
        if (newData != undefined && newData.minimumInstance > newData.maximumInstance) {
            Y.msgp.utils.msgpHeaderTip('error', "机房配置错误：最小实例数不能大于最大实例数", 3);
            return false;
        }
        if (newData.cpu > oldData.cpu && newData.cpu > 8) {
            Y.msgp.utils.msgpHeaderTip('error', "容器CPU配置<=8核时任意调整，大于8核时只允许向下调整", 3);
            return false;
        }
        if (newData.mem > oldData.mem && newData.mem > 12 * 1024) {
            Y.msgp.utils.msgpHeaderTip('error', "容器内存配置<=12G时任意调整，大于12G时只允许向下调整", 3);
            return false;
        }
        return true;
    }
    function checkSP(data) {
        if (data != undefined && data.monitor.value < data.monitor.monitorValueLower) {
            Y.msgp.utils.msgpHeaderTip('error', "策略配置错误：扩容阈值不能小于缩容阈值", 3);
            return false;
        }
        if (data != undefined && data.esgNum > 30) {
            Y.msgp.utils.msgpHeaderTip('error', "扩容数量不能超过30", 3);
            return false;
        }
        //jj缩容范围不能超过3小时end
        var chaValue = Math.abs(data.monitor.startTime - data.monitor.endTime);
        if(chaValue > 180 || data.monitor.startTime > 1440 || data.monitor.endTime > 1440){
            Y.msgp.utils.msgpHeaderTip('error', "禁止缩容时间设置错误", 3);
            return false;
        }
        for (var i in data.idcs) {
            for (var j in serviceSGInfo) {
                if (serviceSGInfo[j].idc == data.idcs[i] && serviceSGInfo[j].fakeFlag == 1) {
                    Y.msgp.utils.msgpHeaderTip('error', "策略配置错误：请先创建" + data.idcs[i] + "机房配置", 3);
                    return false;
                }
                if (serviceSGInfo[j].idc == data.idcs[i] && serviceSGInfo[j].minimumInstance < 0) {
                    Y.msgp.utils.msgpHeaderTip('error', "策略配置错误：" + data.idcs[i] + "机房最小实例数设置为正整数", 3);
                    return false
                }
            }
        }
        return true;
    }
    function fillHULKScalingPolicyAndGroupTable(arr) {
        var micro = new Y.Template();
        var template = Y.one('#text_spandsp_template').get('value');
        for(var i=0; i<arr.length; i++){
            var AStartTime = parseInt(arr[i].policyRow.monitor.startTime / 60) + ":" + parseInt(arr[i].policyRow.monitor.startTime % 60);
            var BEndTime = parseInt(arr[i].policyRow.monitor.endTime / 60) + ":" + parseInt(arr[i].policyRow.monitor.endTime % 60);
            if(parseInt(arr[i].policyRow.monitor.startTime % 60) < 10){
                AStartTime = parseInt(arr[i].policyRow.monitor.startTime / 60) + ":0" + parseInt(arr[i].policyRow.monitor.startTime % 60);
            }
            if(parseInt(arr[i].policyRow.monitor.endTime % 60) < 10){
                BEndTime = parseInt(arr[i].policyRow.monitor.endTime / 60) + ":0" + parseInt(arr[i].policyRow.monitor.endTime % 60);
            }
            arr[i].policyRow.monitor.startTime = AStartTime;
            arr[i].policyRow.monitor.endTime = BEndTime;
        }
        var str = micro.render(template, {data: arr});
        hulkScalingPolicyTableTbody.setHTML(str);
        showContent(hulkPolicyWrapper);
    }
    function fillHULKPeriodicPolicyAndGroupTable(arr) {
        var micro = new Y.Template();
        var template = Y.one('#text_spandPeriodicsp_template').get('value');
        var str = micro.render(template, {data: arr});
        hulkPeriodicPolicyTableTbody.setHTML(str);
        showContent(hulkPolicyWrapper);
    }
    function initIDCSelector(idcList, nodeId) {
        $('#' + nodeId).multiselect({
            selectAllText: "选择全部",
            allSelectedText: "已选择全部机房",
            nonSelectedText: "未选择机房",
            placeholder: "请选择机房",
            buttonWidth: '220px',
            includeSelectAllOption: true,
            selectAllNumber: true,
            buttonText: function (options, select) {
                var total = $('#' + nodeId + ' option').length;
                if (options.length === 0) {
                    return '未选择机房';
                }
                else if (options.length < total && options.length > 1) {
                    return '已选择' + options.length + '个机房';
                } else if (options.length == total) {
                    return '已选择全部机房(' + total + '个)';
                } else {
                    var labels = [];
                    options.each(function () {
                        if ($(this).attr('label') !== undefined) {
                            labels.push($(this).attr('label'));
                        }
                        else {
                            labels.push($(this).html());
                        }
                    });
                    return labels.join(', ') + '';
                }
            }
        });
        var options = [];
        $.each(idcList, function (i, span) {
            options.push({
                key: span,
                value: span
            })
        });
        $("#" + nodeId).multiselect('dataprovider', options);
    }
    //新建策略选择机房
    function initUpdateSPIDCSelector(idcList, selectedIDCs, nodeId) {
        $('#' + nodeId).multiselect({
            selectAllText: "选择全部",
            allSelectedText: "已选择全部机房",
            nonSelectedText: "未选择机房",
            placeholder: "请选择机房",
            buttonWidth: '220px',
            includeSelectAllOption: true,
            selectAllNumber: true,
            buttonText: function (options, select) {
                var total = $('#' + nodeId + ' option').length;
                if (options.length === 0) {
                    //  Y.msgp.utils.msgpHeaderTip('error', "只能选择一个机房", 3);
                    return '未选择机房';
                }
                else if (options.length < total && options.length > 1) {
                    Y.msgp.utils.msgpHeaderTip('error', "只能选择一个机房", 3);
                    //    return false;
                    //     return '已选择' + options.length + '个机房';
                } else if (options.length == total) {
                    Y.msgp.utils.msgpHeaderTip('error', "只能选择一个机房", 3);
                    //   return false;
                    //   // return '已选择全部机房(' + total + '个)';
                } else {
                    var labels = [];
                    options.each(function () {
                        if ($(this).attr('label') !== undefined) {
                            labels.push($(this).attr('label'));
                        }
                        else {
                            labels.push($(this).html());
                        }
                    });
                    return labels.join(', ') + '';
                }
            }
        });
        var options = [];
        idcList = ["大兴", "永丰", "光环", "次渠", "上海桂桥", "月浦"];
        $.each(idcList, function (i, span) {
            options.push({
                key: span,
                value: span
            })
        });
        $("#" + nodeId).multiselect('dataprovider', options);
        $("#" + nodeId).multiselect('select', selectedIDCs);
    }
    function getSPAddIDCs() {
        selectedSPAddIDCs = [];
        $('#s_sp_idcs option:selected').map(function (a, item) {
            selectedSPAddIDCs.push(item.value);
        });
    }
    function getSPUpdateIDCs(nodeId) {
        selectedSPUpdateIDCs = [];
        $('#' + nodeId + ' option:selected').map(function (a, item) {
            selectedSPUpdateIDCs.push(item.value);
        });
    }
    // 记录
    function doGetOperations(pageNo) {
        var curEnv = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');
        var se = getStartEnd();
        if (!se) return;
        showLogOverlay();
        var operatorType = Y.one('#hulk_operatorType').get('value');
        operatorType = (operatorType == "全部") ? 100 : operatorType;
        var idcType = Y.one('#hulk_idc').get('value');
        idcType = (idcType == "全部") ? "all" : idcType;
        var url = '/hulk/scalingRecord/' + appkey + '/' + curEnv + '/get';
        Y.io(url, {
            method: 'get',
            data: {
                pageNo: pageNo,
                pageSize: 20,
                start: se.start,
                end: se.end,
                operatorType: operatorType,
                idcType: idcType
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    var pobj = ret.page;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            fillOperations(data);
                            // }else{
                            //     emptyOrErrorForLog();
                            // }
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
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    //  emptyOrError(true);
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
        Y.one("#hulk_idc").on('change', function () {
            doGetOperations(1);
        });
    }
    function refreshData() {
        doGetOperations(1);
    }
    function fillSelector(type, data) {
        Y.one("#" + type).empty();
        Y.one("#" + type).append("<option value='全部' selected='selected'>全部</option>");
        Y.Array.each(data, function (item) {
            Y.one("#" + type).append('<option value=' + item.key + '>' + item.value + '</option>');
            // Y.one("#" + type).append('<option value=' + 1 + '>缩容</option>');
        });
    }
    function showLogContent() {
        logWrapper.one('.content-overlay').hide();
        logWrapper.one('.content-body').show();
    }
    function showLogOverlay() {
        logWrapper.one('.content-body').hide();
        logWrapper.one('.content-overlay').show();
    }
    function emptyOrErrorForLog(isError) {
        logWrapper.one('.content-overlay').hide();
        logWrapper.one('.content-body').show();
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        logTbody.setHTML(html);
        logPbody.empty();
        showContent(logWrapper);
    }
    function emptyOrError(isError) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        logWrapper.setHTML(html);
        pbody.empty();
        showContent(hulkPolicyWrapper);
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
        'msgp-service/commonMap'
    ]
});
