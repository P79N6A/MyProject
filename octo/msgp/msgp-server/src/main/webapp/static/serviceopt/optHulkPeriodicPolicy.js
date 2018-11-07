M.add('msgp-serviceopt/optHulkPeriodicPolicy', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkPeriodicPolicy = showHulkPeriodicPolicy;

    //全局的appkey
    var appkey;

    var addPeriodicPolicyDialog;
    var updatePeriodicPolicyTagsDialog;
    var updatePeriodicPolicyInWeekDialog;
    var updatePeriodicPolicyBtwDatetimeDialog;
    var checkAccess = 0;

    //构造函数
    function showHulkPeriodicPolicy() {
        appkey = Y.msgp.serviceopt.optHulkAppkey();
        if(checkAccess == 0) {
            checkAccess = Y.msgp.serviceopt.optHulkCheckIsAccessIn();
        }
        showPeriodicPolicies();
    }

    //刷新周期策略列表
    function showPeriodicPolicies() {
        var urlP = "/hulk/periodicPolicy/getAllPolicyByAppkeyAndEnv?appkey=" + appkey + "&env=" + Y.msgp.serviceopt.optHulkEnv();
        $.ajax({
            type: "get",
            url: urlP,
            async: false,
            success: function (ret) {
                enableAllBtn();
                if (ret.errorCode == 0) {
                    var periodicPolicies = ret.periodicPolicyList;
                    for (var index = 0; index < periodicPolicies.length; index++) {
                        if (!periodicPolicies[index].tags) {
                            continue;
                        }
                        var tags = Y.JSON.parse(periodicPolicies[index].tags);
                        if (tags) {
                            var tagNameArr = [];
                            for (var tagIndex = 0; tagIndex < tags.length; tagIndex++) {
                                var tag = tags[tagIndex];
                                if (tag && tag.tagName) {
                                    tagNameArr.push(tag.tagName);
                                }
                            }
                            periodicPolicies[index]['tagNameArr'] = tagNameArr;
                        }
                    }
                    var micro = new Y.Template();
                    var template = Y.one('#text_periodic_policy_template').get('value');
                    var html = micro.render(template, {data: periodicPolicies});
                    Y.one("#periodic_policy_table_tbody").setHTML(html);

                    //绑定事件
                    bindPeriodicPolicyEvents();

                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '没有周期策略', 3);
                }
            }
        });
        enableAllBtn();
    }

    //恢复所有按钮可用性
    function enableAllBtn(){
        $("#updatePeriodicPolicyState_*").prop('disabled', false);
        $("#updatePeriodicPolicyTags_*").prop('disabled', false);
        $("#deletePeriodicPolicyTags_*").prop('disabled', false);
    }

    //绑定修改日常策略、修改活动策略、编辑周期策略、启用禁用周期策略事件
    function bindPeriodicPolicyEvents() {
        if(checkAccess == 3) {
            //新增
            bindAddPeriodicPolicyEvent();

            //修改日常策略
            $("#updatePoliciesInWeek_*").click(function () {
                updatePeriodicPolicyInWeekDialog = updatePeriodicPolicyInWeekDialog ? updatePeriodicPolicyInWeekDialog : new Y.mt.widget.CommonDialog({
                    id: 'update_periodic_policy_in_week_dialog',
                    title: '编辑日常策略',
                    width: 1024,
                    btn: {
                        passName: "保存",
                        pass: updatePeriodicPolicy
                    }
                });
                var el = $(this);
                var line = el.closest("tr");
                var data = line.attr('data-info');
                $("#cur_update_periodic_policy").val(data);
                data = Y.JSON.parse(data);
                var policiesInWeek = [];
                if (data.policiesInWeek) {
                    policiesInWeek = Y.JSON.parse(data.policiesInWeek)
                }

                var micro = new Y.Template();
                var template = Y.one('#text_periodic_policy_in_week_template').get('value');
                var str = micro.render(template, {
                    data: policiesInWeek
                });
                updatePeriodicPolicyInWeekDialog.setContent(str);
                updatePeriodicPolicyInWeekDialog.show();

                //添加单条日常策略
                $("#add_period_policy_in_week").click(function () {
                    var micro = new Y.Template();
                    var template = Y.one('#text_periodic_policy_in_week_add_one_template').get('value');
                    var str = micro.render(template, {});
                    $("#periodic_policy_in_week_table_tbody").append(str);
                    bindPoliciesInWeekEvents();
                });

                bindPoliciesInWeekEvents();
            });

            //修改活动策略
            $("#updatePoliciesBtwDatetime_*").click(function () {
                updatePeriodicPolicyBtwDatetimeDialog = updatePeriodicPolicyBtwDatetimeDialog ? updatePeriodicPolicyBtwDatetimeDialog : new Y.mt.widget.CommonDialog({
                    id: 'update_periodic_policy_btw_datetime_dialog',
                    title: '编辑活动策略',
                    width: 1024,
                    btn: {
                        passName: "保存",
                        pass: updatePeriodicPolicy
                    }
                });
                var el = $(this);
                var line = el.parent().parent();
                var data = line.attr('data-info');
                $("#cur_update_periodic_policy").val(data);
                data = Y.JSON.parse(data);
                var policiesBtwDatetime = [];
                if (data.policiesBtwDatetime) {
                    policiesBtwDatetime = Y.JSON.parse(data.policiesBtwDatetime);
                }

                var micro = new Y.Template();
                var template = Y.one('#text_periodic_policy_btw_datetime_template').get('value');
                var str = micro.render(template, {
                    data: policiesBtwDatetime
                });
                updatePeriodicPolicyBtwDatetimeDialog.setContent(str);
                updatePeriodicPolicyBtwDatetimeDialog.show();

                //添加单条活动策略
                $("#add_period_policy_btw_datetime").click(function () {
                    var micro = new Y.Template();
                    var template = Y.one('#text_periodic_policy_btw_datetime_add_one_template').get('value');
                    var str = micro.render(template, {});
                    $("#periodic_policy_btw_datetime_table_tbody").append(str);
                    bindPoliciesBtwDatetimeEvents();
                });

                bindPoliciesBtwDatetimeEvents();
            });

            //修改绑定的分组信息
            $("#updatePeriodicPolicyTags_*").click(function () {
                updatePeriodicPolicyTagsDialog = updatePeriodicPolicyTagsDialog ? updatePeriodicPolicyTagsDialog : new Y.mt.widget.CommonDialog({
                    id: 'update_periodic_policy_tags_dialog',
                    title: '配置分组',
                    width: 1024,
                    btn: {
                        passName: "保存",
                        pass: updatePeriodicPolicy
                    }
                });
                var el = $(this);
                var line = el.closest("tr");
                var data = line.attr('data-info');
                $("#cur_update_periodic_policy").val(data);
                data = Y.JSON.parse(data);
                //已经绑定的tagId
                var policyTagIdArr = [];
                if (data.tags) {
                    var policyTags = Y.JSON.parse(data.tags);
                    for (var index = 0; index < policyTags.length; index++) {
                        policyTagIdArr.push(policyTags[index].id);
                    }
                }
                //获取但前appkey和env下的所有tagInfo，根据当前策略的tags设置复选框
                var urlP = "/hulk/getAllTagByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv();
                Y.io(urlP, {
                    method: 'get',
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(""),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.errorCode == 0) {
                                var tagList = ret.tagInfoList;
                                if (tagList) {
                                    for (var index = 0; index < tagList.length; index++) {
                                        var tagInfo = tagList[index];
                                        tagInfo["region"] = Y.msgp.serviceopt.optHulkRegionName(tagInfo["region"]);
                                        tagInfo["idc"] = Y.msgp.serviceopt.optHulkIdcName(tagInfo["idc"]);
                                        //已经绑定该策略的tagInfo，checkbox选中
                                        if ($.inArray(tagInfo.id, policyTagIdArr) !== -1) {
                                            tagInfo['related'] = true;
                                        } else {
                                            tagInfo['related'] = false;
                                        }
                                        //已经被其他周期策略绑定的tagInfo不可绑定
                                        if (tagInfo.periodicPolicies && parseInt(tagInfo.periodicPolicies) != data.id) {
                                            tagInfo['binded'] = true;
                                        } else {
                                            tagInfo['binded'] = false;
                                        }
                                        var isOnline = Y.msgp.serviceopt.optHulkIsOnline();
                                        if(isOnline) {
                                            tagInfo["isOnline"] = "true";
                                        } else {
                                            tagInfo["isOnline"] = "true";
                                        }
                                    }
                                }
                                var micro = new Y.Template();
                                var template = Y.one('#text_periodic_policy_tags_template').get('value');
                                var str = micro.render(template, {
                                    data: tagList,
                                    isOnline: Y.msgp.serviceopt.optHulkIsOnline()
                                });
                                updatePeriodicPolicyTagsDialog.setContent(str);
                                updatePeriodicPolicyTagsDialog.show();
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '请求分组信息失败', 3);
                            }
                            $(".tag_periodic_in_").each(function(){
                                if($(this).attr("disabled")) {
                                    Y.msgp.utils.addTooltipWithContent($(this).parent(), '已被其他策略绑定');
                                }
                            });
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '请求分组信息失败', 3);
                        }
                    }
                });
            });

            //修改策略状态
            $("#updatePeriodicPolicyState_*").click(function () {
                var curBtn = $(this);
                if (curBtn.hasClass('active')) {
                    Y.msgp.utils.msgpHeaderTip('info', '当前已经是' + curBtn.text() + '状态', 3);
                    return;
                }
                var line = curBtn.closest("tr");
                var data = line.attr('data-info');
                data = Y.JSON.parse(data);
                //修改策略状态
                var state = parseInt(curBtn.attr('data-enabled'));
                updatePeriodicPolicyState(data.id, state);
            });

            //删除周期策略
            $("#deletePeriodicPolicy_*").click(function () {
                var el = $(this);
                var line = el.closest("tr");
                var data = line.attr('data-info');
                data = Y.JSON.parse(data);

                if (data.state == 1) {
                    Y.msgp.utils.msgpHeaderTip('warn', '删除前请先停用该周期策略', 3);
                } else if (data.tags) {
                    Y.msgp.utils.msgpHeaderTip('warn', '删除前请先解绑该周期策略的分组', 3);
                } else {
                    $("#deletePeriodicPolicyTags_*").prop('disabled', true);
                    var dataP = {'policyId': data.id, 'user': Y.msgp.serviceopt.optHulkUser()};
                    var urlP = "/hulk/periodicPolicy/removePolicy";
                    Y.io(urlP, {
                        method: 'post',
                        headers: {'Content-Type': "application/json;charset=UTF-8"},
                        data: Y.JSON.stringify(dataP),
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (ret.errorCode == 0) {
                                    Y.msgp.utils.msgpHeaderTip('success', '删除周期策略成功', 3);
                                    showPeriodicPolicies();
                                } else {
                                    Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '删除周期策略失败', 3);
                                }
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '删除周期策略失败', 3);
                            }
                        }
                    });
                }
            });
        } else if (checkAccess == 2) {
            Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
        } else if (checkAccess == 1) {
            Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
        } else {
            Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
        }
    }
    function bindPoliciesInWeekEvents() {
        //编辑单条日常策略
        $("button[data-action=editPolicyInWeek]").click(function () {
            var curBtn = $(this);
            var curTr = curBtn.closest("tr");
            curTr.find(":input").css('background-color', 'white');
            curTr.find(":input").prop('disabled', false);
        });
        //删除单条日常策略
        $("button[data-action=removePolicyInWeek]").click(function () {
            var curBtn = $(this);
            var curTr = curBtn.closest("tr");
            curTr.remove();
        });
    }

    function bindPoliciesBtwDatetimeEvents() {
        //编辑单条活动策略
        $("button[data-action=editPolicyBtwDatetime]").click(function () {
            var curBtn = $(this);
            var curTr = curBtn.closest("tr");
            curTr.find("input").css('background-color', 'white');
            curTr.find("input").prop('disabled', false);
        });
        //删除单条活动策略
        $("button[data-action=removePolicyBtwDatetime]").click(function () {
            var curBtn = $(this);
            var curTr = curBtn.closest("tr");
            curTr.remove();
        });
    }

    //更新周期策略
    function updatePeriodicPolicy(btn, container) {
        var validate = true;
        var periodicPolicy = $("#cur_update_periodic_policy").val();
        var dataP = Y.JSON.parse(periodicPolicy);
        dataP['policyId'] = dataP.id;
        //之前的tags保存到tagList中
        var tagList = [];
        if (dataP.tags) {
            //从tags中抽取tagId
            var tagArr = Y.JSON.parse(dataP.tags);
            for (var index = 0; index < tagArr.length; index++) {
                tagList.push(parseInt(tagArr[index].id));
            }
        }
        dataP['tagList'] = tagList;

        if (dataP['policiesInWeek']) {
            dataP['policiesInWeek'] = Y.JSON.parse(dataP['policiesInWeek']);
        }

        if (dataP['policiesBtwDatetime']) {
            dataP['policiesBtwDatetime'] = Y.JSON.parse(dataP['policiesBtwDatetime']);
        }

        var dialogName = "";
        var id = $(container).attr("id");
        if (id && id.indexOf("in_week") >= 0) {
            //仅修改policiesInWeek
            dialogName = "in_week";
            var policiesInWeek = generatePoliciesInWeekFromTable("#periodic_policy_in_week_table_tbody");
            if (!policiesInWeek || policiesInWeek.length == 0) {
                dataP.policiesInWeek = "";
            }else{
                dataP.policiesInWeek = policiesInWeek;
            }
        } else if (id && id.indexOf("btw_datetime") >= 0) {
            //仅修改policiesBtwDatetime
            dialogName = "btw_datetime";
            var policiesBtwDatetime = generatePoliciesBtwDatetimeFromTable("#periodic_policy_btw_datetime_table_tbody");
            if (policiesBtwDatetime.length == 0) {
                dataP.policiesBtwDatetime = "";
            }else{
                dataP.policiesBtwDatetime = policiesBtwDatetime;
            }
        } else if (id && id.indexOf("tags") >= 0) {
            //仅修改tags
            dialogName = "tags";
            //获取选定的tag
            var newTagList = generatePeriodicPolicyTagsFromTable("#update_periodic_policy_tags_table_tbody");
            dataP['tagList'] = newTagList;
        }
        if (!validate) {
            return;
        }
        //删除多余的属性
        delete dataP.tagInfos;
        delete dataP.tags;
        dataP['user'] = Y.msgp.serviceopt.optHulkUser();
        var urlP = '/hulk/periodicPolicy/updatePolicy';
        if (checkPeriodicPolicy(dataP)) {
            Y.io(urlP, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(dataP),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.errorCode == 0) {
                            Y.msgp.utils.msgpHeaderTip('success', '修改周期策略成功', 3);
                            if (dialogName == 'in_week') {
                                updatePeriodicPolicyInWeekDialog.close();
                            } else if (dialogName == 'btw_datetime') {
                                updatePeriodicPolicyBtwDatetimeDialog.close();
                            } else if (dialogName == 'tags') {
                                updatePeriodicPolicyTagsDialog.close();
                            }
                            showPeriodicPolicies();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '修改周期策略失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '修改周期策略失败', 3);
                    }
                }
            });
        }
        return true;
    }

    //修改策略状态
    function updatePeriodicPolicyState(policyId,state){
        //修改前禁用按钮
        $("#updatePeriodicPolicyState_*").prop('disabled', true);
        var dataP = {"appkey":appkey, 'policyId': policyId, "state": state, 'user': Y.msgp.serviceopt.optHulkUser()};
        Y.io('/hulk/periodicPolicy/updatePolicyState', {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(dataP),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.errorCode == 0) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改策略状态成功', 3);
                        showPeriodicPolicies();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '修改策略状态失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改策略状态失败', 3);
                }
            }
        });
    }

    function generatePoliciesInWeekFromTable(tableId) {
        var policiesInWeek = [];
        //通过table构建policiesInWeek对象
        $(tableId).find("tr").each(function (index, element) {
            var inputs = $(element).find(":input");
            var policyInWeek = {
                "startDay": inputs[0].value,
                "endDay": inputs[1].value,
                "startTime": inputs[2].value,
                "endTime": inputs[3].value,
                "scaleStep": inputs[4].value
            };
            policiesInWeek.push(policyInWeek);
        });
        return policiesInWeek;
    }

    function generatePoliciesBtwDatetimeFromTable(tableId) {
        var policiesBtwDatetime = [];
        //通过table构建policiesBtwDatetime对象
        $(tableId).find("tr").each(function (index, element) {
            var inputs = $(element).find("input");
            var policyBtwDatetime = {
                "startDatetime": inputs[0].value,
                "endDatetime": inputs[1].value,
                "scaleStep": inputs[2].value
            };
            policiesBtwDatetime.push(policyBtwDatetime);
        });
        return policiesBtwDatetime;
    }

    function generatePeriodicPolicyTagsFromTable(tableId) {
        var newTagList = [];
        var checkboxes = $(tableId).find("input[type='checkbox'][name='periodicPolicyTags']");
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

    //新增周期策略
    function bindAddPeriodicPolicyEvent() {
        $("#add_periodic_policy").click(function () {
            if(checkAccess == 3) {
                addPeriodicPolicyDialog = addPeriodicPolicyDialog ? addPeriodicPolicyDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_periodic_policy_dialog',
                    title: '创建周期策略',
                    width: 1024,
                    btn: {
                        passName: "保存",
                        pass: addPeriodicPolicy
                    }
                });
                //获取但前appkey和env下的所有tagInfo，根据当前策略的tags设置复选框
                var urlP = "/hulk/getAllTagByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv();
                Y.io(urlP, {
                    method: 'get',
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(""),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.errorCode == 0) {

                                var tagList = ret.tagInfoList;
                                if (tagList) {
                                    for (var index = 0; index < tagList.length; index++) {
                                        var tagInfo = tagList[index];
                                        tagInfo["region"] = Y.msgp.serviceopt.optHulkRegionName(tagInfo["region"]);
                                        tagInfo["idc"] = Y.msgp.serviceopt.optHulkIdcName(tagInfo["idc"]);
                                        tagInfo['related'] = false;
                                        //已经被其他周期策略绑定的tagInfo不可绑定
                                        if (tagInfo.periodicPolicies) {
                                            tagInfo['binded'] = true;
                                        } else {
                                            tagInfo['binded'] = false;
                                        }
                                        var isOnline = Y.msgp.serviceopt.optHulkIsOnline();
                                        if(isOnline) {
                                            tagInfo['isOnline'] = "true";
                                        } else {
                                            tagInfo['isOnline'] = "false";
                                        }
                                    }
                                }

                                var micro = new Y.Template();
                                var template = Y.one('#text_add_periodic_policy_form').get('value');
                                var str = micro.render(template, {
                                    'tagList': tagList,
                                    'isOnline': Y.msgp.serviceopt.optHulkIsOnline()
                                });
                                addPeriodicPolicyDialog.setContent(str);
                                addPeriodicPolicyDialog.show();

                                //添加单条日常策略
                                $("#new_add_period_policy_in_week").click(function () {
                                    var micro = new Y.Template();
                                    var template = Y.one('#text_periodic_policy_in_week_add_one_template').get('value');
                                    var str = micro.render(template, {});
                                    $("#new_periodic_policy_in_week_table_tbody").append(str);
                                    bindPoliciesInWeekEvents();
                                });

                                //添加单条活动策略
                                $("#new_add_period_policy_btw_datetime").click(function () {
                                    var micro = new Y.Template();
                                    var template = Y.one('#text_periodic_policy_btw_datetime_add_one_template').get('value');
                                    var str = micro.render(template, {});
                                    $("#new_periodic_policy_btw_datetime_table_tbody").append(str);
                                    bindPoliciesBtwDatetimeEvents();
                                });

                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '请求分组信息失败', 3);
                            }
                            $(".tag_periodic_in_").each(function(){
                                if($(this).attr("disabled")) {
                                    Y.msgp.utils.addTooltipWithContent($(this).parent(), '已被其他策略绑定');
                                }
                            });
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '请求分组信息失败', 3);
                        }
                    }
                });
            } else if (checkAccess == 2) {
                Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
            } else if (checkAccess == 1) {
                Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
            }
        });
    }

    function addPeriodicPolicy(btn, container) {
        var dataP = {};
        dataP['state'] = 1;
        dataP['channelType'] = 1;
        dataP['appkey'] = appkey;
        dataP['env'] = Y.msgp.serviceopt.optHulkEnv();
        var policiesInWeek = generatePoliciesInWeekFromTable("#new_periodic_policy_in_week_table_tbody");
        dataP['policiesInWeek'] = policiesInWeek;
        var policiesBtwDatetime = generatePoliciesBtwDatetimeFromTable("#new_periodic_policy_btw_datetime_table_tbody");
        dataP['policiesBtwDatetime'] = policiesBtwDatetime;
        var tagList = generatePeriodicPolicyTagsFromTable("#new_update_periodic_policy_tags_table_tbody");
        dataP['tagList'] = tagList;
        dataP['user'] = Y.msgp.serviceopt.optHulkUser();
        var urlP = '/hulk/periodicPolicy/addPolicy';
        if (checkPeriodicPolicy(dataP)) {
            Y.io(urlP, {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(dataP),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.errorCode == 0) {
                            addPeriodicPolicyDialog.close();
                            Y.msgp.utils.msgpHeaderTip('success', '周期策略新建成功', 3);
                            showPeriodicPolicies();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '周期策略创建失败', 10);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '周期策略创建失败', 3);
                    }
                }
            });
        }
        return true;
    }

    function checkPeriodicPolicy(data) {
        if(data.policiesInWeek.length == 0 && data.policiesBtwDatetime.length == 0){
            Y.msgp.utils.msgpHeaderTip('error', "策略数据没有填写", 3);
            return false;
        }
        var policiesInWeek = data.policiesInWeek;
        if(policiesInWeek != null && policiesInWeek.length > 10){
            Y.msgp.utils.msgpHeaderTip('error', "日常策略数量不能超过10条", 3);
            return false;
        }
        for (var index = 0; policiesInWeek != null && index < policiesInWeek.length; index++) {
            var policyInWeek = policiesInWeek[index];
            if (!(policyInWeek.startDay)) {
                if (policyInWeek.startDay != 0) {
                    Y.msgp.utils.msgpHeaderTip('error', "日常策略数据不完整", 3);
                    return false;
                }
            }
            if (!policyInWeek.endDay) {
                if (policyInWeek.endDay != 0) {
                    Y.msgp.utils.msgpHeaderTip('error', "日常策略数据不完整", 3);
                    return false;
                }
            }
            if (!policyInWeek.startTime || !policyInWeek.endTime || !policyInWeek.scaleStep) {
                Y.msgp.utils.msgpHeaderTip('error', "日常策略数据不完整", 3);
                return false;
            }
            if (!checkPolicyInWeekTime(policyInWeek.startTime) || !checkPolicyInWeekTime(policyInWeek.endTime)) {
                Y.msgp.utils.msgpHeaderTip('error', "日常策略数据不合法，正确格式为HH:mm", 3);
                return false;
            }
            if (policyInWeek.startTime > policyInWeek.endTime) {
                Y.msgp.utils.msgpHeaderTip('error', "日常策略数据不合法，开始时间应小于等于结束时间", 3);
                return false;
            }

            if(!checkPolicyInWeekTimeRange(policyInWeek.startTime, policyInWeek.endTime)){
                Y.msgp.utils.msgpHeaderTip('error', "日常策略数据不合法，开始时间应和结束时间相差不能超过16小时", 3);
                return false;
            }
        }

        var policiesBtwDatetime = data.policiesBtwDatetime;
        if(policiesBtwDatetime != null && policiesBtwDatetime.length > 10){
            Y.msgp.utils.msgpHeaderTip('error', "活动策略数量不能超过10条", 3);
            return false;
        }
        for (var index = 0; policiesBtwDatetime != null && index < policiesBtwDatetime.length; index++) {
            var policyBtwDatetime = policiesBtwDatetime[index];
            if (!policyBtwDatetime.startDatetime || !policyBtwDatetime.endDatetime || !policyBtwDatetime.scaleStep) {
                Y.msgp.utils.msgpHeaderTip('error', "活动策略数据不完整", 3);
                return false;
            }
            if (!checkPolicyBtwDatetime(policyBtwDatetime.startDatetime) || !checkPolicyBtwDatetime(policyBtwDatetime.endDatetime)) {
                Y.msgp.utils.msgpHeaderTip('error', "活动策略数据日期不合法，正确格式为MM-dd HH:mm", 3);
                return false;
            }
            if (policyBtwDatetime.startDatetime > policyBtwDatetime.endDatetime) {
                Y.msgp.utils.msgpHeaderTip('error', "活动策略数据日期不合法，开始日期应小于等于结束日期", 3);
                return false;
            }
        }

        return true;
    }

    //日常策略时间格式HH:mm
    function checkPolicyInWeekTime(obj) {
        var re = /^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/;
        return re.test(obj);
    }

    //活动策略时间格式MM-dd HH:mm
    function checkPolicyBtwDatetime(obj) {
        var re = /^(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$/;
        return re.test(obj);
    }

    //日常策略开始时间和结束时间不能大于16小时
    function checkPolicyInWeekTimeRange(startTime, endTime){
        var startMinutes = parseInt(startTime.split(":")[0]) * 60 + parseInt(startTime.split(":")[1]);
        var endMinutes = parseInt(endTime.split(":")[0]) * 60 + parseInt(endTime.split(":")[1]);
        if((endMinutes - startMinutes) > 16*60){
            return false;
        }else{
            return true;
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
        'msgp-serviceopt/optHulkUtils'
    ]
});