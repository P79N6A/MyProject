M.add('msgp-serviceopt/optManuScaleOut', function (Y) {
    Y.namespace('msgp.serviceopt').optManuScaleOut = ScalingGroupAndRunningSet;
    var inited = false;
    var hulkPolicyWrapper = Y.one('#manuScale_content');
    pbody = hulkPolicyWrapper.one('#manuScale_wrapper');


    var startInput = hulkPolicyWrapper.one('#manuscale_start_time'),
        endInput = hulkPolicyWrapper.one('#manuscale_end_time');

    var logWrapper = hulkPolicyWrapper.one('#manuscale_log_wrap'),
        logTbody = logWrapper.one('tbody');

    var logPbody = logWrapper.one('#manuScale_wrapper');

    var colspan = 5;
    var addSupplierDialog;

    var hulkScalingPolicyTableWrapper = hulkPolicyWrapper.one('#ScalingGroupAndRunningSet_table'),
        hulkScalingPolicyTableTbody = hulkScalingPolicyTableWrapper.one('tbody');

    var appkey,
        showOverlay,
        showContent,
        curEnv = 3;
    var everPaged = false,
        totalPage,
        totalCount,
        queryAll = true;

    var idcMap = {"光环": "gh", "大兴": "dx", "永丰": "yf", "次渠": "cq", "贤人":"xr", "月浦": "yp", "桂桥": "gq", "徐汇": "xh", "嘉定": "jd", "办公云": "bgy"};
    var charTMap = {"gh": "光环", "dx": "大兴", "yf": "永丰", "cq": "次渠", "xr":"贤人", "yp": "月浦", "gq": "桂桥", "xh": "徐汇", "jd": "嘉定"};

    var isSelectAllSet = false;
    var latestImageBuildTime = 000;

    var scaleOutStr = [
        '<div id="scaleout_form" class="form-horizontal">',
        '<div class="control-group"><label class="control-label">机房：</label>',
        '<div class="controls">',
        '<select id="scaleout_idc" type="text">',
        '</select>',
        '<i style="font-size: 0.6em;color: red;margin-left: 5px;">*</i>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">扩容数量：</label>',
        '<div class="controls">',
        '<input id="scaleout_num" type="text" value="1" onkeyup="cky(this)" placeholder="请填写扩容数量，必须大于0" onchange="javascript:this.value=this.value.replace(/[^\d]/g,"");if(this.value<=0){this.value=1;}"/>',
        '<i style="font-size: 0.6em;color: red;margin-left: 5px;" id="num_for_scale_out_tip"></i>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">CPU(核)：</label>',
        '<div class="controls">',
        '<select id="cpu_selector" type="text">',
        '</select>',
        '<i style="font-size: 0.6em;color: red;margin-left: 5px;">*</i>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">内存(G)：</label>',
        '<div class="controls">',
        '<select id="mem_selector" type="text">',
        '</select>',
        '<i style="font-size: 0.6em;color: red;margin-left: 5px;">*</i>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">磁盘(G)：</label>',
        '<div class="controls">',
        '<select id="hd_selector" type="text">',
        '</select>',
        '<i style="font-size: 0.6em;color: red;margin-left: 5px;">*</i>',
        '</div>',
        '</div>',
        '<div class="control-group" id="set_select_content_div" style="display: none;"><label class="control-label">' +
        '<a href="https://123.sankuai.com/km/page/60968019" target="_blank">&nbsp;<i class="fa fa-question-circle"></i>&nbsp;&nbsp;</a>',
        '<input id="set_checkbox" type="checkbox" style="margin-bottom: 6px;margin-right: 2px;"/>SET标识：</label>',
        '<div class="controls">',
        '<select id="set_info_add" type="text" style="display: none" placeholder="请填写set标识"/>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">' +
        '<a href="https://123.sankuai.com/km/page/59746961" target="_blank">&nbsp;<i class="fa fa-question-circle"></i>&nbsp;&nbsp;</a>' +
        'commitId列表：</label>',
        '<div class="controls"">',
        '<select id="latest_image_list" type="text" value ="" style="width: 80%;"></select>' +
            //'</select><button class="btn" style="margin-left: 5px;">查看</button>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    var trLogTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-trigger="<%= item %>">',
        '<td><%= Y.mt.date.formatDateByString(new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td><%= item.operatorName %></td>',
        '<td><%= item.entityType %></td>',
        '<td><%= item.newValue  %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    var entityTypeOring = ["扩容", "缩容"];

    var entityMap = {"扩容": "扩容", "缩容": "缩容"};

    var latistImageBuildTime = "";
    var idcList = [];
    var url = '/hulk/idc/get';
    var isHaveRight = false;
    var isCanUseManualScale = true;
    var requestListMessage = "";

    function ScalingGroupAndRunningSet(key, func1, func2) {
        if (!inited) {
            $.ajax({
                type: "get",
                url: url,
                async: false,
                success: function (ret) {
                    idcList = ret.data;
                }
            });
            inited = true;
            appkey = key;
            showOverlay = func1;
            showContent = func2;
            bindCheckIsHulk();
            bindCheckIsRight();
            bindrefresh();
            bindEnvSelect();
            bindScaleOut();//禁止一键扩容操作
            bindScaleIn();
            initSelector();
            bindClickCheckBox();
            // initSelector();
            fillSelector(entityTypeOring, 'manu_operatorType');
            //fillSelector('manu_idc', idcList);
            initDatePicker();
            refreshData();
        }
        bindEnvSelect();
        bindCheckIsHulk();
        bindCheckIsRight();
        bindScaleOut();
        getScalingGroupAndRunningSet(1);
    }

    function bindCheckIsHulk() {
        var urlOfGetStableImageList = '/kapi/repository/image/list?appkey=' + appkey + '&env=' + Y.msgp.serviceopt.optHulkEnv2();
        $.ajax({
            type: "get",
            url: urlOfGetStableImageList,
            async: false,
            success: function (ret) {
                if (ret.code == 200 && ret.data != null) {
                    isCanUseManualScale = true;
                } else if(ret.code == 200 &&(ret.data == null || ret.data.length == 0 || JSON.stringify(ret.data) === '[]' || ret.data == false)) {
                    requestListMessage = "commitId列表为空";
                    isCanUseManualScale = false;
                    $("#scale_out").css("display", "none");
                } else {
                    requestListMessage = ret.msg;
                    isCanUseManualScale = false;
                    $("#scale_out").css("display", "none");
                }
            }
        });;
    }

    function bindCheckIsRight() {
        var urlOfCheckIsRight = '/kapi/check/isRight?appkey=' + appkey + '&env=' + Y.msgp.serviceopt.optHulkEnv2();
        $.ajax({
            type: "get",
            url: urlOfCheckIsRight,
            async: false,
            success: function (ret) {
                if (ret.code == 0 && ret.errorInfo == "existed") {
                    isHaveRight = true;
                } else {
                    //鉴权失败
                }
            },
            failure: function () {
                Y.msgp.utils.msgpHeaderTip('error', "鉴权失败", 30);

            }
        });
    }

    function bindEnvSelect() {
        Y.msgp.service.setEnvText('manScale_env_select');
        hulkPolicyWrapper.delegate('click', function () {
            hulkPolicyWrapper.all('#manScale_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            curEnv = Number(this.getAttribute("value"));
            getScalingGroupAndRunningSet(1);
            doGetOperations(1);
        }, "#manScale_env_select a")
    }

    function getScalingGroupAndRunningSet(pageNo) {
        showContent(hulkPolicyWrapper);
        var url = '/kapi/app/instance?appkey=' + appkey + '&env=' + Y.msgp.serviceopt.optHulkEnv2();
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.code == 0) {
                        var data = JSON.parse(JSON.stringify(ret)).data;
                        if (data.length > 0) {
                            fillScalingGroupAndRunningSetTable(data);
                        } else {
                            fillScalingGroupAndRunningSetTable(data);
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

    var manuConfigParams = {};

    function bindScaleOut() {
        if(isHaveRight && isCanUseManualScale) {
            hulkPolicyWrapper.delegate('click', function () {
                addSupplierDialog = addSupplierDialog ? addSupplierDialog : new Y.mt.widget.CommonDialog({
                    id: 'scaleout_dialog',
                    title: '一键扩容',
                    width: 640,
                    btn: {
                        pass: doScaleOut
                    }
                });
                //获取一键扩容的参数
                $.ajax({
                    type: "get",
                    url: "/kapi/mcc/manuconfig",
                    success: function (ret) {
                        manuConfigParams = JSON.parse(ret).data;
                        var scaleOutIdcList = JSON.parse(manuConfigParams).idc_list.split(",");
                        var idcListView = [];
                        if (scaleOutIdcList.length == 0 || scaleOutIdcList == null) {
                            idcListView.push("无可扩容机房");
                        } else {
                            for (var i = 0; i < scaleOutIdcList.length; i++) {
                                idcListView.push(charTMap[scaleOutIdcList[i]]);
                            }
                        }

                        Y.msgp.service.commonMap(showDialogAfter);
                        function showDialogAfter(obj) {
                            var micro = new Y.Template();
                            var str = micro.render(scaleOutStr, {data: obj});
                            addSupplierDialog.setContent(str);
                            addSupplierDialog.show();
                            var addBody = addSupplierDialog.getBody();
                            addBody.detachAll('click');
                            addBody.detachAll('change');
                            $("#num_for_scale_out_tip").text("* 单次扩容最大数量: " + parseInt(JSON.parse(manuConfigParams).scale_num));
                            //idc
                            var optionstringidc = "";
                            for (var item = 0; item < idcListView.length; item++) {
                                optionstringidc = "<option value='" + idcListView[item] + "'>" + idcListView[item] + "</option>";
                                $("#scaleout_idc").append(optionstringidc);
                            }

                            //cpu
                            var optionstringcpu = "";
                            var cpuParamsArray = (JSON.parse(manuConfigParams).cpu).split(",");
                            for (var item = 0; item < cpuParamsArray.length; item++) {
                                if (parseInt(cpuParamsArray[item]) == 4) {
                                    optionstringcpu = "<option selected='selected' value='" + cpuParamsArray[item] + "'>" + cpuParamsArray[item] + "</option>";
                                } else {
                                    optionstringcpu = "<option value='" + cpuParamsArray[item] + "'>" + cpuParamsArray[item] + "</option>";
                                }
                                $("#cpu_selector").append(optionstringcpu);
                            }
                            //mem
                            var optionstringmem = "";
                            var memParamsArray = (JSON.parse(manuConfigParams).mem).split(",");
                            for (var item = 0; item < memParamsArray.length; item++) {
                                if (parseInt(memParamsArray[item]) == 4) {
                                    optionstringmem = "<option selected='selected' value='" + memParamsArray[item] + "'>" + memParamsArray[item] + "</option>";
                                } else {
                                    optionstringmem = "<option value='" + memParamsArray[item] + "'>" + memParamsArray[item] + "</option>";
                                }
                                $("#mem_selector").append(optionstringmem);
                            }
                            //hd
                            var optionstringhd = "";
                            var hdParamsArray = (JSON.parse(manuConfigParams).hd).split(",");
                            for (var item = 0; item < hdParamsArray.length; item++) {
                                if (parseInt(hdParamsArray[item]) == 100) {
                                    optionstringhd = "<option selected='selected' value='" + hdParamsArray[item] + "'>" + hdParamsArray[item] + "</option>";
                                } else {
                                    optionstringhd = "<option value='" + hdParamsArray[item] + "'>" + hdParamsArray[item] + "</option>";
                                }
                                $("#hd_selector").append(optionstringhd);
                            }
                            var urlOfGetStableImageList = '/kapi/repository/image/list?appkey=' + appkey + '&env=' + Y.msgp.serviceopt.optHulkEnv2();
                            var optionstring = "";
                            var correntEnv = Y.msgp.serviceopt.optHulkEnv2();
                            if(correntEnv == "prod" || correntEnv == "staging") {
                                $("#set_select_content_div").css("display", "block");
                                $("#set_checkbox").click(function () {
                                    if ($(this).is(":checked")) {
                                        $("#set_info_add").css("display", "block");
                                        var optionstringSet = "";
                                        var cellList = Y.msgp.serviceopt.optHulkCellList();
                                        optionstringSet += "<option selected='selected' value=''>" + "请选择SET标识" + "</option>";
                                        for (var item = 0; item < cellList.length; item++) {
                                            optionstringSet += "<option value='" + cellList[item].name + "'>" + cellList[item].name + "</option>";
                                        }
                                        $("#set_info_add").append(optionstringSet);
                                    } else {
                                        $("#set_info_add").css("display", "none");
                                    }
                                });
                            }
                            $.ajax({
                                type: "get",
                                url: urlOfGetStableImageList,
                                async: false,
                                success: function (ret) {
                                    if (ret.code == 200) {
                                        var imageList = ret.data;
                                        var listLength = parseInt(JSON.parse(manuConfigParams).image_list_num) > imageList.length ? imageList.length : JSON.parse(manuConfigParams).image_list_num;
                                        if (imageList != null && imageList.length > 0) {
                                            for (var i = 0; i < listLength; i++) {
                                                if (imageList[i].stable && imageList[i].oneSet) {
                                                    optionstring = "<option value='" + imageList[i].imageUrlV2 + "'>" + "(稳定镜像)" + imageList[i].commit + "-" + imageList[i].pkgName + "</option>";
                                                    $('#latest_image_list').append(optionstring);
                                                } else {
                                                    optionstring = "<option value='" + imageList[i].imageUrlV2 + "'>" + imageList[i].commit + "-" + imageList[i].pkgName + "</option>";
                                                    $('#latest_image_list').append(optionstring);
                                                }
                                            }
                                        }
                                    } else {
                                        Y.msgp.utils.msgpHeaderTip('error', ret.errorInfo || '获取CommitId列表错误', 3);
                                    }
                                }
                            });
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '一键扩容初始化失败 || 请联系HULK', 3);
                    }
                });

            }, '#scale_out');
        }else if(!isHaveRight){
            Y.msgp.utils.msgpHeaderTip('error', '非服务负责人', 3);
        }else if(!isCanUseManualScale){
            Y.msgp.utils.msgpHeaderTip('error', "提示:无法接入一键扩容; 原因: "+ requestListMessage + " 请自查:<a href='https://123.sankuai.com/km/page/59850039' target='_blank'>一键扩容问题梳理</a>", 60);
        }
    }

    function doScaleOut() {
        var curZone = Y.one('#scaleout_idc').get('value');
        var url = '/kapi/scaleout';
        var data = {
            appkey: appkey,
            env: Y.msgp.serviceopt.optHulkEnv2(),
            idc: idcMap[curZone] == null ? "" : idcMap[curZone],
            num: +Y.one('#scaleout_num').get('value'),
            image: $("#latest_image_list").find("option:selected").val(),
            cpu: parseInt($("#cpu_selector").find("option:selected").val()),
            mem: parseInt($("#mem_selector").find("option:selected").val()) * 1024,
            hd: parseInt($("#hd_selector").find("option:selected").val()),
            set: $("#set_info_add").find("option:selected").val(),
            user: ""
        };
        if (checkscaleOutInfo(data)) {
            var urlJudge = "/kapi/scaleout/judge";
            var judgeData = {
                appkey: appkey,
                env: Y.msgp.serviceopt.optHulkEnv2(),
                idc: idcMap[curZone],
                num: +Y.one('#scaleout_num').get('value')

            };
            $.ajax({
                type: "post",
                url: urlJudge,
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: Y.JSON.stringify(judgeData),
                success: function (o) {
                    var ret = o.data;
                    if (true == ret.result) {
                        $.ajax({
                            method: 'post',
                            url: url,
                            headers: {'Content-Type': "application/json;charset=UTF-8"},
                            data: Y.JSON.stringify(data),
                            success: function (o) {
                                if (o.code == 200) {
                                    Y.msgp.utils.msgpHeaderTip('success', '一键扩容指令已经发出,正在扩容中...', 3);
                                    addSupplierDialog.close();
                                    hulkPolicyWrapper.one('#all-check').set('checked', false);
                                    getScalingGroupAndRunningSet(1);
                                } else {
                                    Y.msgp.utils.msgpHeaderTip('error', ret.errorInfo || '一键扩容错误', 3);
                                    addSupplierDialog.close();
                                    getScalingGroupAndRunningSet(1);
                                }
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '一键扩容失败', 3);
                                getScalingGroupAndRunningSet(1);
                            }
                        });
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.failedDetails || '一键扩容参数错误', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '预判,一键扩容失败', 3);
                }

            });
        }
        return true;
    }


    function bindClickCheckBox() {
        //全选
        hulkPolicyWrapper.delegate('click', function () {
            var ifChecked = this.get("checked");
            //单选与全选保持一致
            hulkPolicyWrapper.all('#one-checkbox').set("checked", ifChecked);
        }, '#all-check');
        //单选
        hulkPolicyWrapper.delegate('click', function () {
            //全选与单选保持一致
            var allOneCheck = hulkPolicyWrapper.all('#one-checkbox');
            var allOneChecked = hulkPolicyWrapper.all('#one-checkbox:checked');
            if (allOneChecked.size() === 0) {
                hulkPolicyWrapper.one('#all-check').set("checked", false);
            } else {
                if (allOneCheck.size() === allOneChecked.size()) {
                    hulkPolicyWrapper.one('#all-check').set("checked", true);
                }
            }
        }, '#one-checkbox');
    }

    function bindScaleIn() {

        function patchDataForChangeLine(line) {
            var info = line.getData('info');
            if (Y.Lang.isString(info)) {
                info = Y.JSON.parse(info);
            }
            var infoApp = {
                appkey: info.appkey,
                ip: info.ip
            };
            return infoApp;
        }

        //删除
        if (isHaveRight) {
            hulkPolicyWrapper.one("#scale_delete").on('click', function () {
                var allCheckedTr = hulkPolicyWrapper.all("#one-checkbox:checked");
                if (allCheckedTr.size() === 0) {
                    Y.msgp.utils.msgpHeaderTip('info', '未选中任何主机，缩容请选择主机', 3);
                } else {
                    var dataList = [];
                    allCheckedTr.each(function (item, index) {
                        var line = item.ancestor('tr');
                        var data = patchDataForChangeLine(line);

                        dataList.push(data);
                    });
                    /**
                     * first 获取v1,v2在这appkey下分别的所有机器,一次获取,多次删除
                     * 或者,每次查询是哪一类,确定之后进行删除,多次查询,多次删除
                     * 获取v1或者v2,用所有 - v1/v2
                     */
                    var instanceIpsArray = [];//获取界面上所选择的所有要下线的机器
                    for (var i = 0; i < dataList.length; i++) {
                        instanceIpsArray.push(dataList[i].ip);
                    }
                    //instanceIpsArray = ["10.23.131.209", "10.23.131.225"];//["11.22.33.256", "11.22.33.257", "10.23.131.110"];
                    //查询该服务该环境的所有机器
                    $.ajax({
                        type: "get",
                        url: "/kapi/app/instance?appkey=" + appkey + "&env=" + Y.msgp.serviceopt.optHulkEnv2(),
                        headers: {'Content-Type': "application/json;charset=UTF-8"},
                        async: true,
                        success: function (ret) {
                            ret = JSON.parse(JSON.stringify(ret));
                            if (0 == ret.code) {
                                var v1_ip_list = [];
                                var v2_ip_list = [];
                                var all_ips_map = {};
                                var acceptDataArray = JSON.parse(JSON.stringify(ret.data));
                                for (var c = 0; c < acceptDataArray.length; c++) {
                                    all_ips_map["" + acceptDataArray[c].ip] = acceptDataArray[c].setType;
                                }
                                for (var k = 0; k < instanceIpsArray.length; k++) {
                                    if (1 == all_ips_map[instanceIpsArray[k]]) {
                                        v1_ip_list.push(instanceIpsArray[k]);
                                    }
                                    if (2 == all_ips_map[instanceIpsArray[k]]) {
                                        v2_ip_list.push(instanceIpsArray[k]);
                                    }
                                }
                                if (null != v1_ip_list && 0 != v1_ip_list.length) {
                                    executeV1Del(v1_ip_list);
                                }
                                if (null != v2_ip_list && 0 != v2_ip_list.length) {
                                    //v2执行v2的删除逻辑for
                                    executeV2Del(v2_ip_list);
                                }
                                if ((null == v1_ip_list && null == v2_ip_list) || (0 == v1_ip_list.length && 0 == v2_ip_list.length)) {//选了机器,却没有1/2的机器
                                    Y.msgp.utils.msgpHeaderTip('error', '机器ip错误!请联系HULK', 3);
                                }
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', '获取机器失败', 3);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '获取机器失败', 3);
                            getScalingGroupAndRunningSet(1);
                        }
                    });
                    return true;
                }
            });
        } else {
            Y.msgp.utils.msgpHeaderTip('error', '非服务负责人', 3);
        }
    }

    function executeV1Del(v1_ip_list) {
        var url = '/hulk/scalingIn/' + appkey;
        var data = {
            appkey: appkey,
            instanceIps: v1_ip_list,
            user: ""
        };
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', 'octo手动缩容指令已经发出,正在缩容中...', 3);
                        //Y.msgp.utils.msgpHeaderTip('info', '提示:目前2.0删除功能暂未提供操作记录,此持续到7月13号为止.验证机器是否删除成功,可在删除功能执行1分钟后,刷新页面检查.', 10);
                        getScalingGroupAndRunningSet(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || 'octo手动缩容', 3);
                        getScalingGroupAndRunningSet(1);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', 'octo手动缩容', 3);
                    getScalingGroupAndRunningSet(1);
                }
            }
        });
    }

    function executeV2Del(v2_ip_list) {
        var url = '/kapi/scalein';
        var dataQ = {
            appkey: appkey,
            setIps: v2_ip_list,
            env: Y.msgp.serviceopt.optHulkEnv2(),
            operator: ""
        };
        $.ajax({
            type: "post",
            url: url,
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(dataQ),
            success: function (ret) {
                var ret = Y.JSON.parse(JSON.stringify(ret));
                if (200 == ret.code) {
                    Y.msgp.utils.msgpHeaderTip('success', 'octo手动缩容指令已经发出,正在缩容中...', 3);
                    //Y.msgp.utils.msgpHeaderTip('info', '提示:目前2.0删除功能暂未提供操作记录,此持续到7月13号为止.验证机器是否删除成功,可在删除功能执行1分钟后,刷新页面检查.', 10);
                    getScalingGroupAndRunningSet(1);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.errorInfo || 'octo手动缩容失败 || 正在冷却期', 3);
                    getScalingGroupAndRunningSet(1);
                }
            },
            failure: function () {
                Y.msgp.utils.msgpHeaderTip('error', 'octo手动缩容', 3);
                getScalingGroupAndRunningSet(1);
            }
        });

    }

    function bindrefresh() {
        hulkPolicyWrapper.delegate('click', function () {
            getScalingGroupAndRunningSet(1);
            doGetOperations(1);
        }, '#refreshmannuscale');
    }

    function fillScalingGroupAndRunningSetTable(arr) {
        for (var i = 0; i < arr.length; i++) {
            arr[i].idc = charTMap[arr[i].idc];
            arr[i].time = formatDate(arr[i].timestamp);
        }
        var micro = new Y.Template();
        var template = Y.one('#text_scaleout_template').get('value');
        var str = micro.render(template, {data: arr});
        hulkScalingPolicyTableTbody.setHTML(str);
        showContent(hulkPolicyWrapper);
    }

    function checkscaleOutInfo(scaleOutInfo) {
        if (scaleOutInfo != undefined && scaleOutInfo.num == 0) {
            Y.msgp.utils.msgpHeaderTip('error', "扩容数量不能为0: 请填写扩容数量", 3);
            return false;
        }
        if (scaleOutInfo.num > JSON.parse(manuConfigParams).scale_num) {
            Y.msgp.utils.msgpHeaderTip('error', "扩容数量设置超限制", 3);
            return false;
        }
        if (scaleOutInfo != undefined && scaleOutInfo.num == "") {
            Y.msgp.utils.msgpHeaderTip('error', "扩容数量不能为空 : 请填写扩容数量", 3);
            return false;
        }
        if (scaleOutInfo != undefined && (scaleOutInfo.idc == "请选择机房" || scaleOutInfo.idc == "")) {
            Y.msgp.utils.msgpHeaderTip('error', "扩容需选择机房：请选择机房", 3);
            return false;
        }
        if (scaleOutInfo != undefined && (scaleOutInfo.image == "" || scaleOutInfo.image == null)) {
            Y.msgp.utils.msgpHeaderTip('error', "错误!没有运行镜像", 3);
            return false;
        }

        return true;
    }

    function doGetOperations(pageNo) {
        var se = getStartEnd();
        if (!se) return;
        showLogOverlay();
        var entityType = Y.one('#manu_operatorType').get('value');
        entityType = (entityType == "选择全部") ? "" : entityMap[entityType];
        var operator = "";
        var url = '/kapi/operation/' + appkey + '/log';
        //var data1 = {
        //    pageNo: pageNo,
        //    pageSize: 20,
        //    start: se.start,
        //    end: se.end,
        //    entityType: entityType,
        //    operator: idcType
        //};
        Y.io(url, {
            method: 'get',
            data: {
                pageNo: pageNo,
                pageSize: 20,
                start: se.start,
                end: se.end,
                entityType: entityType,
                operator: operator
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    var pobj = ret.page;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            fillOperations(data);
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pbody, pobj);
                            }
                        } else {
                            emptyOrError();
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
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

    function fillOperations(arr) {
        var micro = new Y.Template();
        var html = micro.render(trLogTemplate, {data: arr});
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
        Y.one("#manu_operatorType").on('change', function () {
            doGetOperations(1);
        });
    }


    function refreshData() {
        getAllEntityType();
        doGetOperations(1);
    }

    function getAllEntityType() {
        var se = getStartEnd();
        var url = '/service/operation/' + appkey + '/entity';
        Y.io(url, {
            method: 'get',
            data: {
                start: se.start,
                end: se.end
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        fillSelector(entityTypeOring, "manu_operatorType")
                    } else {
                        emptyOrError(true);
                        return null
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }

    function getAllOperator() {
        var se = getStartEnd();
        var url = '/service/operation/' + appkey + '/operator';
        Y.io(url, {
            method: 'get',
            data: {
                start: se.start,
                end: se.end
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        fillSelector(data, "manu_idc")
                    } else {
                        emptyOrError(true);
                        return null
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }


    function fillSelector(data, type) {
        Y.one("#" + type).empty();
        Y.one("#" + type).append("<option value='选择全部' selected='selected'>选择全部</option>");
        Y.Array.each(data, function (item) {
            Y.one("#" + type).append('<option value=' + item + '>' + item + '</option>');
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
        logTbody.setHTML(html);
        // pbody.empty();
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

    function formatDate(timestamp) {
        return new Date(timestamp).toLocaleString().replace(/年|月/g, "-").replace(/日/g, " ");
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
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap',
        'msgp-serviceopt/optHulkUtils'
    ]
});
