M.add('msgp-serviceopt/optHulkTagInfo', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkTagInfo = showHulkTagInfoList;

    //全局的appkey和env
    var appkey;
    var cellList;//SET标识列表
    var addTagGroupDialog,fastAddTagGroupDialog,updateUserSelfGroupDialog;
    var checkAccess = 0;
    //获取有资源的机房列表
    var richIdcOrigin = "policy";

    //构造函数
    function showHulkTagInfoList() {
        appkey = Y.msgp.serviceopt.optHulkAppkey();
        if(checkAccess == 0){
            checkAccess = Y.msgp.serviceopt.optHulkCheckIsAccessIn();
        }
        showTagInfoList();
        cellList = Y.msgp.serviceopt.optHulkCellList();
    }

    //刷新分组列表
    function showTagInfoList() {
        $.ajax({
            type: "get",
            url: "/hulk/getAllTagByAppkeyAndEnv/" + appkey + "/" + Y.msgp.serviceopt.optHulkEnv(),
            async: true,
            success: function (ret) {
                var micro = new Y.Template();
                var template = Y.one('#text_spandUsergp_template').get('value');
                if (ret.tagInfoList == null) {
                    var www = micro.render(template, {data: null});
                    Y.one("#tag_info_table_tbody").setHTML(www);
                } else {
                    var taglist_ = JSON.parse(JSON.stringify(ret.tagInfoList));
                    var tag_id_fake = 0;
                    for (var i = 0; i < taglist_.length; i++) {
                        tag_id_fake = tag_id_fake + 1;
                        taglist_[i].regionName = Y.msgp.serviceopt.optHulkRegionName(taglist_[i].region);
                        taglist_[i].idcName = Y.msgp.serviceopt.optHulkIdcName(taglist_[i].idc);
                        taglist_[i].tag_id_fake = tag_id_fake;
                        var isOnline = Y.msgp.serviceopt.optHulkIsOnline();
                        if(isOnline) {
                            taglist_[i].isOnline = "true";
                        } else {
                            taglist_[i].isOnline = "false";
                        }
                    }
                    var www = micro.render(template, {
                        data: taglist_,
                        isOnline: Y.msgp.serviceopt.optHulkIsOnline()
                    });
                    Y.one("#tag_info_table_tbody").setHTML(www);
                    bindTagInfoEvents();
                }
            }
        });
    }

    //绑定事件
    function bindTagInfoEvents(){
        if(checkAccess == 3) {
            bindAddTagInfo();
            bindFastAddTagInfo();
            bindUpdateUserSelfGroup();
            bindRemoveTagInfo();
        } else if (checkAccess == 2) {
            Y.msgp.utils.msgpHeaderTip('error', "非服务负责人", 8);
        } else if (checkAccess == 1) {
            Y.msgp.utils.msgpHeaderTip('error', "弹性策略2.0正在内测,详情:<a href='https://123.sankuai.com/km/page/59021307'>弹性接入手册</a>", 3);
        } else {
            Y.msgp.utils.msgpHeaderTip('error', "无权限操作该服务", 3);
        }
    }

    //添加高级分组
    function bindAddTagInfo(){
        $("#create_tag_info").click(function () {
            if(checkAccess == 3) {
                addTagGroupDialog = addTagGroupDialog ? addTagGroupDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_group_dialog',
                    title: '添加高级分组',
                    width: 768
                });
                var micro = new Y.Template();
                var template = Y.one('#text_add_tag_info').get('value');
                var data = {"idcList":Y.msgp.serviceopt.optHulkRichIdcList("bj", richIdcOrigin),"cellList":cellList};
                var str = micro.render(template, {
                    data: data
                });
                addTagGroupDialog.setContent(str);
                //设置当前境且不能更改
                $("#tag_env_input").val(Y.msgp.serviceopt.optHulkEnv());
                $("#tag_env_input").css('background-color', '#d1d1d1');
                $("#tag_env_input").prop('disabled', true);
                //分组区域和机房联动
                $("#tag_region_select").change(function(){
                    var tagRegion = $('#tag_region_select').find(":selected").val();
                    var micro = new Y.Template();
                    var template = Y.one('#tag_info_idc_list').get('value');
                    var idcList = Y.msgp.serviceopt.optHulkRichIdcList(tagRegion, richIdcOrigin);
                    var str = micro.render(template, {data:idcList});
                    $("#tag_idc_select").html(str);
                });
                addTagGroupDialog.show();
                //线上环境无泳道，线下环境无SET标识
                if(Y.msgp.serviceopt.optHulkIsOnline()){
                    $("#tag_cell_tr").show();
                    $("#tag_swimlane_tr").hide();
                }else{
                    $("#tag_cell_tr").hide();
                    $("#tag_swimlane_tr").show();
                }
                $('#tag_cell_checkbox').change(function () {
                    var showTagCell = $("#tag_cell_checkbox").prop('checked');
                    if(showTagCell){
                        $("#tag_cell_th").show();
                    }else{
                        $("#tag_cell_th").hide();
                    }
                });
                //保存事件
                $("#btn_add_group").click(function () {
                    var c_tagName = $("#tag_tagName").val().trim();
                    var c_env = $("#tag_env_input").val();
                    var c_region = $("#tag_region_select").val() == null ? "" : $("#tag_region_select").val();
                    var c_idc = $("#tag_idc_select").val() == null ? "" : $("#tag_idc_select").val();
                    var c_cell = "";
                    //选中了SET标识才传入SET
                    if($("#tag_cell_checkbox").prop('checked')){
                        c_cell = $("#tag_cell_select").val() == null ? "" : $("#tag_cell_select").val();
                    }
                    var c_swimlane = $("#tag_swimlane_input").val() == null ? "" : $("#tag_swimlane_input").val().trim();
                    var dataTAG = {
                        "tagName":c_tagName,
                        "appkey": appkey,
                        "env": c_env,
                        "region": c_region,
                        "idc": c_idc,
                        "cell": c_cell,
                        "swimlane": c_swimlane,
                        "preTasks": "",
                        "scaleResult": 0,
                        "periodicPreTasks": "",
                        "periodicScaleResult": 0,
                        "state": 1,
                        "version": 0,
                        "periodicPolicies": "",
                        "user": Y.msgp.serviceopt.optHulkUser()
                    };
                    //验证数据合法性
                    if(checkTagInfo(dataTAG)){
                        Y.io('/hulk/addTagRecord', {
                            method: 'post',
                            headers: {'Content-Type': "application/json;charset=UTF-8"},
                            data: Y.JSON.stringify(dataTAG),
                            on: {
                                success: function (id, o) {
                                    var ret = Y.JSON.parse(o.responseText);
                                    if (ret.errorCode == 0 || ret.errorMsg == "success") {
                                        addTagGroupDialog.close();
                                        Y.msgp.utils.msgpHeaderTip('success', '创建分组成功', 3);
                                        showTagInfoList();
                                    } else {
                                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '创建分组失败', 10);
                                    }
                                },
                                failure: function () {
                                    Y.msgp.utils.msgpHeaderTip('error', '创建分组失败', 3);
                                }
                            }
                        });
                    }
                });
                $("#btn_cancel_group").click(function () {
                    addTagGroupDialog.close();
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

    //添加普通分组
    function bindFastAddTagInfo(){
        $("#fast_create_tag_info").click(function () {
            if(checkAccess == 3) {
                fastAddTagGroupDialog = fastAddTagGroupDialog ? fastAddTagGroupDialog : new Y.mt.widget.CommonDialog({
                    id: 'fast_add_group_dialog',
                    title: '添加普通分组',
                    width: 768
                });
                var micro = new Y.Template();
                var template = Y.one('#text_fast_add_tag_info').get('value');
                var str = micro.render(template, {
                    data: Y.msgp.serviceopt.optHulkRichIdcList("", richIdcOrigin)
                });
                fastAddTagGroupDialog.setContent(str);
                //全局radio选中事件
                $("#fast_tag_global_tr input[type=radio]").click(function () {
                    var global = ($('input[name=fast_global]:checked', '#fast_tag_global_tr').val() == 'true');
                    if (global) {
                        $("#fast_tag_region_tr").hide();
                        $("#fast_tag_idc_tr").hide();
                    } else {
                        $("#fast_tag_region_tr").show();
                        $("#fast_tag_idc_tr").show();
                    }
                });
                //分组区域和机房联动
                $("#fast_tag_region_select").change(function () {
                    var tagRegion = $('#fast_tag_region_select').find(":selected").val();
                    var micro = new Y.Template();
                    var template = Y.one('#fast_tag_info_idc_list').get('value');
                    var idcList = Y.msgp.serviceopt.optHulkRichIdcList(tagRegion, richIdcOrigin);
                    var str = micro.render(template, {data: idcList});
                    $("#fast_tag_idc_th").html(str);
                });
                fastAddTagGroupDialog.show();
                //保存事件
                $("#btn_fast_add_group").click(function () {
                    var global = ($('input[name=fast_global]:checked', '#fast_tag_global_tr').val() == 'true');
                    var idcList = [];
                    $('#fast_tag_idc_th input:checked').each(function () {
                        idcList.push(this.value);
                    });
                    var dataTAG = {
                        "appkey": appkey,
                        "env": Y.msgp.serviceopt.optHulkEnv(),
                        "global": global,
                        "idcList": idcList,
                        "user": Y.msgp.serviceopt.optHulkUser()
                    };
                    //验证数据合法性
                    if (checkFastTagInfo(dataTAG)) {
                        Y.io('/hulk/fastAddTagRecord', {
                            method: 'post',
                            headers: {'Content-Type': "application/json;charset=UTF-8"},
                            data: Y.JSON.stringify(dataTAG),
                            on: {
                                success: function (id, o) {
                                    var ret = Y.JSON.parse(o.responseText);
                                    if (ret.errorCode == 0 || ret.errorMsg == "success") {
                                        fastAddTagGroupDialog.close();
                                        Y.msgp.utils.msgpHeaderTip('success', '创建分组成功', 3);
                                        showTagInfoList();
                                    } else {
                                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '创建分组失败', 10);
                                    }
                                },
                                failure: function () {
                                    Y.msgp.utils.msgpHeaderTip('error', '创建分组失败', 3);
                                }
                            }
                        });
                    }
                });
                $("#btn_fast_cancel_group").click(function () {
                    fastAddTagGroupDialog.close();
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

    //编辑分组名称
    function bindUpdateUserSelfGroup() {
        $("#updateUserSelfGroup_*").click(function(){
            updateUserSelfGroupDialog = updateUserSelfGroupDialog ? updateUserSelfGroupDialog : new Y.mt.widget.CommonDialog({
                id: 'text_updateUserSelfGroup_dialog',
                title: '更新分组名称',
                width: 798,
                btn: {
                    pass: updateTagInfoName
                }
            });

            var el = $(this);
            var line = el.closest("tr");
            var data = line.attr('data-info');
            data = Y.JSON.parse(data);
            var micro = new Y.Template();
            var template = Y.one('#text_updateUserSelfGroup_dialog').get('value');
            var str = micro.render(template, {});
            updateUserSelfGroupDialog.setContent(str);
            updateUserSelfGroupDialog.show();
            //线上环境无泳道，线下环境无SET标识
            if(Y.msgp.serviceopt.optHulkIsOnline()){
                $("#updatetag_cell_tr").show();
                $("#updatetag_swimlane_tr").hide();
            }else{
                $("#updatetag_cell_tr").hide();
                $("#updatetag_swimlane_tr").show();
            }
            //获取数据,进行填充
            $("#updatetag_id").val(data.id);
            $("#updatetag_tagName_input").val(data.tagName);
            $("#updatetag_env_input").val(data.env);
            $("#updatetag_region_input").val(data.regionName);
            $("#updatetag_idc_input").val(data.idcName);
            $("#updatetag_cell_input").val(data.cell);
            $("#updatetag_swimlane_input").val(data.swimlane);
            //除了tagName其余禁止修改
            $("#updateUserSelfGroup_form").find("input").css('background-color', '#d1d1d1');
            $("#updateUserSelfGroup_form").find("input").prop('disabled', true);
            $("#updatetag_tagName_input").css('background-color', 'white');
            $("#updatetag_tagName_input").prop('disabled', false);
        });
    }

    //修改分组名称
    function updateTagInfoName() {
        var tagId = $("#updatetag_id").val();
        var tagName = $("#updatetag_tagName_input").val().trim();
        if(!tagName){
            Y.msgp.utils.msgpHeaderTip('error', '分组名称不能为空', 3);
            return false;
        }
        if(tagName.length > 15){
            Y.msgp.utils.msgpHeaderTip('error', '分组名称超出最大长度(15)限制', 3);
            return false;
        }
        Y.io("/hulk/updateTagInfoName", {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: {"tagId":tagId,"tagName":tagName},
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.errorCode == 0 || ret.errorMsg == "success") {
                        Y.msgp.utils.msgpHeaderTip('success', '分组名称更新成功', 3);
                        showTagInfoList();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '分组名称更新失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '分组名称更新失败', 3);
                }
            }
        });
    }

    //删除tagInfo
    function bindRemoveTagInfo(){
        $("#removeTagInfo_*").click(function(){
            var el = $(this);
            var line = el.closest("tr");
            var data = line.attr('data-info');
            data = Y.JSON.parse(data);
            if(data.preTasks || data.periodicPreTasks){
                Y.msgp.utils.msgpHeaderTip('error', "该分组还有弹性伸缩任务没有执行完，请稍后", 3);
                return;
            }
            if(data.scaleResult > 0 || data.periodicScaleResult > 0){
                Y.msgp.utils.msgpHeaderTip('error', "该分组存在弹性扩容的机器", 3);
                return;
            }
            if(data.policyId || data.periodicPolicies){
                Y.msgp.utils.msgpHeaderTip('error', "该分组绑定了弹性策略，请先解绑", 3);
                return;
            }
            Y.io("/hulk/removeTagInfo", {
                method: 'post',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: {"tagId":data.id, "user":Y.msgp.serviceopt.optHulkUser()},
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.errorCode == 0 || ret.errorMsg == "success") {
                            Y.msgp.utils.msgpHeaderTip('success', '分组删除成功', 3);
                            showTagInfoList();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '分组删除成功', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', ret.errorMsg || '分组删除失败', 3);
                    }
                }
            });
        });
    }

    function checkTagInfo(data){
        if(!data.tagName){
            Y.msgp.utils.msgpHeaderTip('error', "分组名称不能为空", 3);
            return false;
        }

        if(data.tagName.length > 15){
            Y.msgp.utils.msgpHeaderTip('error', "分组名称超出最大长度(15)限制", 3);
            return false;
        }

        if(!data.env){
            Y.msgp.utils.msgpHeaderTip('error', "环境不能为空", 3);
            return false;
        }

        if(!data.region){
            Y.msgp.utils.msgpHeaderTip('error', "区域不能为空", 3);
            return false;
        }

        if(!data.idc){
            Y.msgp.utils.msgpHeaderTip('error', "机房不能为空", 3);
            return false;
        }

        return true;
    }

    function checkFastTagInfo(data){
        if(data.tagName && data.tagName.length > 15){
            Y.msgp.utils.msgpHeaderTip('error', "分组名称超出最大长度(15)限制", 3);
            return false;
        }

        if(!data.global && (!data.idcList || data.idcList.length == 0)){
            Y.msgp.utils.msgpHeaderTip('error', "非全局模式下机房不能为空", 3);
            return false;
        }
        return true;
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