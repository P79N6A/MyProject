/* jshint indent : false */
M.add('msgp-serviceopt/optAppkeyAuth', function(Y){
    Y.namespace('msgp.serviceopt').optAppkeyAuth = optAppkeyAuth;
    Y.namespace('msgp.serviceopt').optAppkeys = optAppkeys;

    var appkey, showOverlay, showContent;
    var appkeyAuthWrapper;
    var appkeyWhiteListCache = [], appkeyAuthListCache = [], spanAuthListCache = [];
    var appkeyWhiteListTableWrapper, appkeyAuthListTableWrapper, spanAuthListTableWrapper;
    var appkeyWhiteListAddDialog, appkeyAuthListAddDialog, spanAuthListAddDialog;
    var curEnv;
    var isOnline = false;
    var startInput, endInput;
    var clientAppkeyInput, serverAppkeyInput, clientIP_input, serverIP_input;
    var authWrapper, listWrapper;
    var clientAppkey, serverAppkey,  clentIP, serverIP;
    var sdate, edate;

    var prod_patriot_url = "https://patriot.sankuai.com/diagnose";
    var stage_patriot_url = "https://patriot.inf.st.sankuai.com/diagnose";

    var dev_patriot_url = "http://patriot.inf.dev.sankuai.com/diagnose";
    var test_patriot_url = "http://patriot.inf.test.sankuai.com/diagnose";


    var wrapper = Y.one('#wrap_appkeyAuth');
    var searching = false;

    function optAppkeyAuth( key, f1, f2 ){
        appkey = key;
        showOverlay = f1;
        showContent = f2;

        curEnv = '3';
        appkeyAuthWrapper = Y.one('#wrap_appkeyAuth');
        appkeyWhiteListTableWrapper = Y.one('#appkey-white-list-table');
        appkeyAuthListTableWrapper = Y.one('#appkey-auth-list-table');
        spanAuthListTableWrapper = Y.one('#span-auth-list-table');

        initNodes();
        initDatePicker();
        initOnline();

        initEvent();

        getAppkeyWhiteList();
        getAppkeyAuthList();
        getSpanAuthList();

        wrapper.one('#appkey_auth_loading').hide();
        wrapper.one('#appkey_auth_content').show();
    }
    function initNodes() {
        startInput = Y.one('#start_time');
        //endInput = Y.one('#end_time');
        clientAppkeyInput = Y.one("#clientAppkey");
        serverAppkeyInput = Y.one("#serverAppkey");
        clientIP_input = Y.one("#clientIP");
        serverIP_input = Y.one("#serverIP");
        authWrapper = Y.one('#auth_diagnosis_param');
        listWrapper = Y.one('#auth_diagnosis_table');
    }

    function initDatePicker() {
        var now = new Date();

        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });
        var s = startInput.get('value');
        if (s == "") {
            var start = new Date(now - 60 * 60 * 1000);
            startInput.set('value', Y.mt.date.formatDateByString(start, 'yyyy-MM-dd hh:mm:00'));
        }
        // todo：保留，后续可能需要加上
        // edate = new Y.mt.widget.Datepicker({
        //     node: endInput,
        //     showSetTime: true
        // });
        // var e = endInput.get('value');
        // if (e == "") {
        //     var end = new Date(now);
        //     endInput.set('value', Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:00'));
        // }
    }

    function optAppkeys(key, list) {
        var obj = [];
        for (var i = 0, l = list.length; i < l; i++) {
            obj.push({id: i, name: list[i]});
        }
        if (obj.length && Y.one('#clientAppkey') != null && Y.one('#serverAppkey') != null) {
            AutoCompleteList(obj);

            Y.one("#clientAppkey").set("value", key);
            Y.one("#serverAppkey").set("value", key);
        }

    }

    function AutoCompleteList(obj) {
        new Y.mt.widget.AutoCompleteList({
            id: "apps_select_client",
            node: Y.one("#clientAppkey"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            forbidWrite: false,
            more: "",
            tabSelect: true,
            width: 320,
            callback: function () {
            }
        });


        new Y.mt.widget.AutoCompleteList({
            id: "apps_select_server",
            node: Y.one("#serverAppkey"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            forbidWrite: false,
            more: "",
            tabSelect: true,
            width: 320,
            callback: function () {
            }
        });

        Y.one("#apps_select_client").one(".widget-autocomplete-complete-list").setStyle("height", "300px");
        Y.one("#apps_select_client").one(".widget-autocomplete-tip").setHTML("输入服务名搜索或向下滚动选择");
        Y.one("#apps_select_client").one(".widget-autocomplete-menu-operator").remove();

        Y.one("#apps_select_server").one(".widget-autocomplete-complete-list").setStyle("height", "300px");
        Y.one("#apps_select_server").one(".widget-autocomplete-tip").setHTML("输入服务名搜索或向下滚动选择");
        Y.one("#apps_select_server").one(".widget-autocomplete-menu-operator").remove();
    }

    function initOnline() {
        var url = '/common/online'
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    isOnline = ret.data;
                },
                failure: function () {},
            }
        });
    };


    function initEvent() {
        Y.one('#auth_query_btn').on('click', function () {
            if (searching) return;
            getTableData(listWrapper);
        }, ".btn");

        Y.msgp.service.setEnvText('auth_env_select');
        appkeyAuthWrapper.delegate('click', function () {
            appkeyAuthWrapper.all('#auth_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            curEnv = this.getAttribute("value");
            getAppkeyWhiteList();
            getAppkeyAuthList();
            getSpanAuthList();
        }, "#auth_env_select a");

        Y.one('#appkey-white-list-add').on('click', function () {
            appkeyWhiteListAddDialog = appkeyWhiteListAddDialog ? appkeyWhiteListAddDialog : new Y.mt.widget.CommonDialog({
                title: '增加服务白名单',
                width: 600,
                modal: true,
                drag: function () {
                },
                btn: {
                    pass: function () {
                        var selectedAppkeyList = [];
                        $('#appkey-white-list-add-dialog-remoteAppkey option:selected').map(function (a, item) {
                            selectedAppkeyList.push(item.value);
                        });
                        var inputedAppkey = Y.one('#appkey-white-list-add-dialog-remoteAppkey-input').get('value');
                        if(inputedAppkey != null && inputedAppkey != ''){
                            selectedAppkeyList.push(inputedAppkey);
                        }
                        distinctArray(selectedAppkeyList);
                        selectedAppkeyList.forEach(function (item) {
                            if(appkeyWhiteListCache.indexOf(item) == -1){
                                appkeyWhiteListCache.push(item);
                            }
                        });
                        updateAppkeyWhiteList();
                    }
                }
            });
            var micro = new Y.Template();
            var str = micro.render(Y.one('#text-appkey-white-list-add-dialog').get('value'));
            appkeyWhiteListAddDialog.setContent(str);

            var appkeySpannames = getAppkeyList();
            var appkeyList = appkeySpannames['appkeyList'];

            initAppkeySelector(appkeyList, 'appkey-white-list-add-dialog-remoteAppkey');
            $('#appkey-white-list-add-dialog-remoteAppkey').multiselect('select', appkeyWhiteListCache);

            var selectedOptions = $('#appkey-white-list-add-dialog-remoteAppkey option').filter(function() {
                return $(this).is(':selected');
            });

            selectedOptions.each(function() {
                var input = $('input[value="' + $(this).val() + '"]');
                input.prop('disabled', true);
                input.parent('li').addClass('disabled');
            });

            Y.one('#show-appkey-white-list-add-dialog-remoteAppkey-input').on('click', function () {
                //$('#appkey-white-list-add-dialog-remoteAppkey-input')[0].style.display = 'inherit';
                Y.one('#appkey-white-list-add-dialog-remoteAppkey-input').setStyle('display', 'inherit');
            });


            appkeyWhiteListAddDialog.show();
        });

        Y.one('#appkey-white-list-refresh').on('click', function () {
            getAppkeyWhiteList();
        });

        appkeyWhiteListTableWrapper.one("tbody").delegate('click', function () {
            var tr = this.ancestor('tr');
            var appkeyDelete = tr.getData('appkey');
            //从缓存数组中删除
            removeArrayItem(appkeyWhiteListCache, appkeyDelete);
            //更新KMS中保存的数值
            updateAppkeyWhiteList();
        }, '.appkey-white-list-delete');

        //服务鉴权
        Y.one('#appkey-auth-list-add').on('click', function () {
            appkeyAuthListAddDialog = appkeyAuthListAddDialog ? appkeyAuthListAddDialog : new Y.mt.widget.CommonDialog({
                title: '增加服务鉴权配置',
                width: 600,
                modal: true,
                drag: function () {
                },
                btn: {
                    pass: function () {
                        var selectedAppkeyList = [];
                        $('#appkey-auth-list-add-dialog-remoteAppkey option:selected').map(function (a, item) {
                            selectedAppkeyList.push(item.value);
                        });
                        var inputedAppkey = Y.one('#appkey-auth-list-add-dialog-remoteAppkey-input').get('value');
                        if(inputedAppkey != null && inputedAppkey != ''){
                            selectedAppkeyList.push(inputedAppkey);
                        }
                        distinctArray(selectedAppkeyList);
                        selectedAppkeyList.forEach(function (item) {
                            if(appkeyAuthListCache.indexOf(item) == -1){
                                appkeyAuthListCache.push(item);
                            }
                        });
                        updateAppkeyAuthList();
                    }
                }
            });
            var micro = new Y.Template();
            var str = micro.render(Y.one('#text-appkey-auth-list-add-dialog').get('value'));
            appkeyAuthListAddDialog.setContent(str);

            var appkeySpannames = getAppkeyList();
            var appkeyList = appkeySpannames['appkeyList'];
            initAppkeySelector(appkeyList, 'appkey-auth-list-add-dialog-remoteAppkey');

            $('#appkey-auth-list-add-dialog-remoteAppkey').multiselect('select', appkeyAuthListCache);

            var selectedOptions = $('#appkey-auth-list-add-dialog-remoteAppkey option').filter(function() {
                return $(this).is(':selected');
            });

            selectedOptions.each(function() {
                var input = $('input[value="' + $(this).val() + '"]');
                input.prop('disabled', true);
                input.parent('li').addClass('disabled');
            });


            Y.one('#show-appkey-auth-list-add-dialog-remoteAppkey-input').on('click', function () {
                //$('#appkey-white-list-add-dialog-remoteAppkey-input')[0].style.display = 'inherit';
                Y.one('#appkey-auth-list-add-dialog-remoteAppkey-input').setStyle('display', 'inherit');
            });

            appkeyAuthListAddDialog.show();
        });

        Y.one('#appkey-auth-list-refresh').on('click', function () {
            getAppkeyAuthList();
        });

        Y.one('#appkey-auth-list-sync').on('click', function () {
            updateAppkeyAuthList();
        });

        appkeyAuthListTableWrapper.one("tbody").delegate('click', function () {
            var tr = this.ancestor('tr');
            var appkeyDelete = tr.getData('appkey');
            //从缓存数组中删除
            removeArrayItem(appkeyAuthListCache, appkeyDelete);
            //更新KMS中保存的数值
            updateAppkeyAuthList();
        }, '.appkey-auth-list-delete');


        //接口鉴权
        Y.one('#span-auth-list-add').on('click', function () {
            initSpanAuthListAddDialog();
            spanAuthListAddDialog.show();
        });

        Y.one('#span-auth-list-refresh').on('click', function () {
            getSpanAuthList();
        });

        Y.one("#span-auth-list-sync").on('click', function () {
            updateSpanAuthList();
        });


        spanAuthListTableWrapper.one("tbody").delegate('click', function () {
            var tr = this.ancestor('tr');
            var selectedSpan = tr.getData('span');
            //把要删除的tr绑定到dialog上
            initSpanAuthListAddDialog();
            var existedAppkeyList = [];
            var existed = false;
            spanAuthListCache.forEach(function (item) {
                if(item.spanname == selectedSpan) {
                    existedAppkeyList = item.appkeyList;
                    existed = true;
                }
            });

            //禁用已经存在的
            $('#span-auth-list-add-dialog-spanname').val(selectedSpan);
            $('#span-auth-list-add-dialog-remoteAppkey').multiselect('select', existedAppkeyList);
            var selectedOptions = $('#span-auth-list-add-dialog-remoteAppkey option').filter(function() {
                return $(this).is(':selected');
            });

            selectedOptions.each(function() {
                var input = $('input[value="' + $(this).val() + '"]');
                input.prop('disabled', true);
                input.parent('li').addClass('disabled');
            });

            spanAuthListAddDialog.show();
            //设置已经选中的值
        }, '.span-appkey-auth-list-add');

        spanAuthListTableWrapper.one("tbody").delegate('click', function () {
            var tr = this.ancestor('tr');
            var span = tr.getData('span');

            for(var i=0; i< spanAuthListCache.length; i++) {
                if(spanAuthListCache[i].spanname == span){
                    spanAuthListCache.splice(i, 1);
                    break;
                }
            }
            updateSpanAuthList();
        }, '.span-auth-list-delete');

        spanAuthListTableWrapper.one("tbody").delegate('click', function () {
            var tr = this.ancestor('tr');
            var span = tr.getData('span');
            var appkey = tr.getData('appkey');

            for(var i=0; i< spanAuthListCache.length; i++) {
                if(spanAuthListCache[i].spanname == span){
                    var appkeyWhiteList = spanAuthListCache[i].appkeyList;
                    removeArrayItem(appkeyWhiteList, appkey);
                    if(appkeyWhiteList.length == 0){
                        spanAuthListCache.splice(i, 1);
                    }else{
                        spanAuthListCache[i].appkeyList = appkeyWhiteList;
                    }
                    break;
                }
            }
            updateSpanAuthList();
        }, '.span-appkey-auth-list-delete');
    }

    function getTableData(node) {
        var se = getStartEnd();
        if (!se) return;
        var queryBtn =  Y.one('#auth_query_btn');
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        clientAppkey = clientAppkeyInput.get('value');
        clentIP = clientIP_input.get('value').trim();
        serverAppkey = serverAppkeyInput.get('value');
        serverIP = serverIP_input.get('value').trim();

        var url = prod_patriot_url;

        switch(curEnv)
        {
            case "1":
                curEnv = "test";
                url = test_patriot_url;
                break;
            case "2":
                if(isOnline) {
                    curEnv = "staging";
                    url = stage_patriot_url;
                }else {
                    curEnv = "ppe";
                    url = dev_patriot_url;
                }
                break;
            case "3":
                if(isOnline) {
                    curEnv = "prod";
                    url = prod_patriot_url;
                }else {
                    curEnv = "dev";
                    url = dev_patriot_url;
                }
                break;
            default:
                curEnv = "prod";
        }

        var requestParam = {
            clientNs: clientAppkey,
            clientIp: clentIP,
            serverNs: serverAppkey,
            serverIp: serverIP,
            env: curEnv,
            time: se.start
        };


        node && showWaitMsg(node);

        //var url = 'http://10.24.83.38:8416/diagnose';
        searching = true;
        Y.io(url, {
            method: 'POST',
            headers: {
                "auth-key": "octo",
                "auth-token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjbGllbnQiOiJvY3RvIn0.TUHVkvBrB8dLpZjKt7bvgcGqGIQa4HcpDoMYNzdXzOo",
                'Content-Type': "application/json;charset=UTF-8",
            },
            data: Y.JSON.stringify(requestParam),
            on: {
                start: function () {
                    queryBtn.set('disabled', true);
                    queryBtn.setStyle('background', '#ccc');
                },
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.success) {
                        var data = ret.data;
                        if(data != null) {
                            fillDiagnosisData(data);
                        } else {
                            showEmptyErrorMsg(node, 0);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败, 重试或更改查询条件', 3);
                    }
                },
                failure: function () {
                    searching = false;
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败, 重试或更改查询条件', 3);
                },
                complete: function () {
                    queryBtn.set('disabled', false);
                    queryBtn.setStyle('');
                }
            }
        });
    }
    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node.setHTML(html);
    }
    function clearWaitMsg(node) {
        node.setHTML('');
    }
    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

    function getStartEnd() {
        var obj = {
            start: ''
            //end: ''
        };
        var s = startInput.get('value');
            //e = endInput.get('value');
        var reg = /^\d{4}(-\d{2}){2} \d{2}:\d{2}:\d{2}$/;
        if (s && reg.test(s)) {
            obj.start = s;
        }

        if (s !== obj.start) {
            Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
            return null;
        }

        // reg.lastIndex = 0;
        // if (e && reg.test(e)) {
        //     obj.end = e;
        // }
        // if (s !== obj.start || e !== obj.end) {
        //     Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
        //     return null;
        // }
        // if (obj.start > obj.end) {
        //     Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
        //     return null;
        // }

        var start = new Date(obj.start);  //时间戳 new Date(obj.start).getTime()

        //获取年月日时分秒
        var year = start.getFullYear();
        var month = start.getMonth()+1;
        var date = start.getDate();
        var hours = start.getHours();
        var minutes = start.getMinutes();
        //var seconds = start.getSeconds();

        //月，日，时，分，秒 小于10时，补0
        if(month<10){
            month = "0" + month;
        }
        if(date<10){
            date = "0" + date;
        }
        if(hours <10){
            hours = "0" + hours;
        }
        if(minutes <10){
            minutes = "0" + minutes;
        }

        //拼接日期格式【yyyymmddhhss】
        var fomateTime = year+month+date+hours+minutes;

        obj.start = fomateTime;
        //obj.end = new Date(obj.end).getTime();
        return obj;
    }

    function conclusionTemplate() {
        return '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>'+
            '<td><%= item %></td>' +
            '</tr>' +
            '<% }); %>'
    }

    function serverTableTemplate() {
        return '<tr>'+
        '<td><%= this.data.status.server.namespace %></td>' +
        '<td><%= this.data.status.server.mtthriftVersion %></td>' +
        '<td><%= this.data.status.server.kmsVersion %></td>' +
        '<td><%= this.data.status.server.unifiedProto %></td>' +
        '<td><%= this.data.status.server.grayAuth %></td>' +
        '<td><%= this.data.status.server.authType %></td>' +
        '<td><%= this.data.status.server.authedClients %></td>' +
        '</tr>'
    }

    function clientTableTemplate() {
        //'<% Y.Array.each(this.data, function( item, index ){ %>' +
        return '<tr>' +
            '<td style="width: 10%"><%= this.data.status.client.namespace %></td>' +
            '<td style="width: 10%"><%= this.data.status.client.mtthriftVersion %></td>' +
            '<td style="width: 10%"><%= this.data.status.client.kmsVersion %></td>' +
            '<td style="width: 10%"><%= this.data.status.client.unifiedProto %></td>' +
            '</tr>'
    }

    // 填充鉴权诊断具体信息
    function fillDiagnosisData(data) {
        var micro = new Y.Template();

        var conclusion_dom = Y.one('#tbody_conclusion');
        var conclusion_html = micro.render(conclusionTemplate(), {data: data});
        conclusion_dom.setHTML(conclusion_html);

        // todo：保留，后续可能需要加上
        // var server_dom = Y.one('#tbody_server');
        // var server_html = micro.render(serverTableTemplate(), {data: data});
        // server_dom.setHTML(server_html);
        //
        // var client_dom = Y.one('#tbody_client');
        // var client_html = micro.render(clientTableTemplate(), {data: data});
        // client_dom.setHTML(client_html);

        Y.mt.widget.init();
    }



    function initSpanAuthListAddDialog() {
        spanAuthListAddDialog = spanAuthListAddDialog ? spanAuthListAddDialog : new Y.mt.widget.CommonDialog({
            title: '增加接口鉴权配置',
            width: 800,
            modal: true,
            drag: function () {
            },
            btn: {
                pass: function () {
                    var selectedSpan = Y.one('#span-auth-list-add-dialog-spanname').get('value');
                    var selectedAppkeyList = [];
                    $('#span-auth-list-add-dialog-remoteAppkey option:selected').map(function (a, item) {
                        selectedAppkeyList.push(item.value);
                    });

                    var inputedAppkey = Y.one('#span-auth-list-add-dialog-remoteAppkey-input').get('value');
                    if(inputedAppkey != null && inputedAppkey != ''){
                        selectedAppkeyList.push(inputedAppkey);
                    }
                    distinctArray(selectedAppkeyList);

                    var existedAppkeyList = [];
                    var existed = false;
                    spanAuthListCache.forEach(function (item) {
                        if(item.spanname == selectedSpan) {
                            existedAppkeyList = item.appkeyList;
                            existed = true;
                        }
                    });
                    //更新
                    if(!existed){
                        spanAuthListCache.push({spanname: selectedSpan, appkeyList: selectedAppkeyList});
                    }else{
                        spanAuthListCache.forEach(function (item) {
                            if(item.spanname == selectedSpan) {
                                selectedAppkeyList.forEach(function (item) {
                                    //保证加入的新appkey在最下方
                                   if(existedAppkeyList.indexOf(item) == -1){
                                       existedAppkeyList.push(item);
                                   }
                                });

                                item.appkeyList = existedAppkeyList;
                            }
                        });
                    }
                    fillSpanAuthListTable(spanAuthListCache);
                    updateSpanAuthList();
                }
            }
        });

        var micro = new Y.Template();
        var str = micro.render(Y.one('#text-span-auth-list-add-dialog').get('value'));
        spanAuthListAddDialog.setContent(str);

        var appkeySpannames = getAppkeyList();
        var appkeyList = appkeySpannames['appkeyList'];
        initAppkeySelector(appkeyList, 'span-auth-list-add-dialog-remoteAppkey');

        var spannames = appkeySpannames['spannames'];
        $("#span-auth-list-add-dialog-spanname").autocomplete({
            source: spannames,
            minLength: 0
        });

        Y.one('#show-span-auth-list-add-dialog-remoteAppkey-input').on('click', function () {
            $('#span-auth-list-add-dialog-remoteAppkey-input')[0].style.display = 'inherit';
            //Y.one('#span-auth-list-add-dialog-remoteAppkey-input').setStyle('display', 'inherit');
        });
    }

    function initAppkeySelector(appkeyList, nodeId) {
        $('#' + nodeId).multiselect({
            selectAllText: "选择全部",
            allSelectedText: "已选择全部服务",
            nonSelectedText: "未选择服务",
            placeholder: "请选择服务",
            buttonWidth: '300px',
            maxHeight: 200,
            filterPlaceholder: '搜索服务',
            enableFiltering: true,
            includeSelectAllOption: true,
            selectAllNumber: true,
            buttonText: function (options, select) {
                var total = $('#'+ nodeId + ' option').length;
                if (options.length === 0) {
                    return '未选择服务';
                }
                else if (options.length < total && options.length > 1) {
                    return '已选择' + options.length + '个服务';
                } else if (options.length == total) {
                    return '已选择全部服务(' + total + '个)';
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
        $.each(appkeyList, function(i, span) {
            options.push({
                label: span,
                value: span
            })
        });
        $("#" + nodeId).multiselect('dataprovider', options);
    }

    function initAppkeyInput(appkeyList, nodeId) {
        $('#' + nodeId).autocomplete({
            source: appkeyList,
            select: function( event, ui ){
                console.log(ui.item.value);
            }
        });
    }

    function getAppkeyWhiteList() {
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var params = {appkey: appkey, env: curEnv};
        var url = '/serverOpt/auth/appkey/whitelist/get';
        Y.io(url, {
            method: 'get',
            data: params,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillAppkeyWhiteListTable(ret.data);
                        appkeyWhiteListCache = ret.data;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取白名单失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取白名单失败', 3);
                }
            }
        });
    }

    function updateAppkeyWhiteList() {
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var params = {"appkey": appkey, "whitelist": appkeyWhiteListCache, "env": curEnv};
        var url = '/serverOpt/auth/appkey/whitelist/update';
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(params),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillAppkeyWhiteListTable(ret.data);
                        appkeyWhiteListCache = ret.data;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '更新服务白名单失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '更新服务白名单失败', 3);
                }
            }
        });
    }

    function fillAppkeyWhiteListTable(appkeyWhiteList) {
        var micro = new Y.Template();
        var addCoreSavedTbodyTemplate = Y.one('#text-appkey-white-list-table-tbody').get('value');
        var html = micro.render(addCoreSavedTbodyTemplate, {data: appkeyWhiteList});
        appkeyWhiteListTableWrapper.one("tbody").setHTML(html);
    }


    function getAppkeyAuthList() {
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var params = {appkey: appkey, env: curEnv};
        var url = '/serverOpt/auth/appkey/auth/get';
        Y.io(url, {
            method: 'get',
            data: params,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillAppkeyAuthListTable(ret.data);
                        appkeyAuthListCache = ret.data;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取服务鉴权数据失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取服务鉴权数据失败', 3);
                }
            }
        });
    }

    function updateAppkeyAuthList() {
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var params = {"appkey": appkey, "allAuthList": appkeyAuthListCache, "env": curEnv};
        var url = '/serverOpt/auth/appkey/auth/update';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(params),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillAppkeyAuthListTable(ret.data);
                        appkeyAuthListCache = ret.data;
                        Y.msgp.utils.msgpHeaderTip('success', '更新服务鉴权数据成功', 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '更新服务鉴权数据失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '更新服务鉴权数据失败', 3);
                }
            }
        });
    }

    function fillAppkeyAuthListTable(appkeyAuthList) {
        var appkeyAuthListAdjusted = appkeyAuthList == null ? [] : appkeyAuthList;
        var micro = new Y.Template();
        var appkeyAuthListTableTbodyTemplate = Y.one('#text-appkey-auth-list-table-tbody').get('value');
        var html = micro.render(appkeyAuthListTableTbodyTemplate, {data: appkeyAuthListAdjusted});
        appkeyAuthListTableWrapper.one("tbody").setHTML(html);
    }


    function getSpanAuthList() {
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var params = {appkey: appkey, env: curEnv};
        var url = '/serverOpt/auth/span/auth/get';
        Y.io(url, {
            method: 'get',
            data: params,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var spanAuthListWrapper = ret.data;
                        var appkey = spanAuthListWrapper.appkey;
                        var spanAuthListMap = spanAuthListWrapper.interfaceTokens;
                        var spanAuthListCacheTemp = [];
                        for (var spanname in spanAuthListMap) {
                            if(spanAuthListMap.hasOwnProperty(spanname)){
                                spanAuthListCacheTemp.push({
                                    spanname: spanname,
                                    appkeyList: spanAuthListMap[spanname]
                                })
                            }
                        }
                        spanAuthListCache = spanAuthListCacheTemp;
                        fillSpanAuthListTable(spanAuthListCache);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取接口鉴权数据失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取接口鉴权数据失败', 3);
                }
            }
        });
    }

    function updateSpanAuthList() {
        var interfaceTokens = {};
        spanAuthListCache.forEach(function (item) {
           interfaceTokens[item.spanname] = item.appkeyList;
        });
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var params = {spanAuthData: {appkey: appkey, interfaceTokens: interfaceTokens}, env: curEnv};
        var url = '/serverOpt/auth/span/auth/update';
        Y.io(url, {
            method: 'POST',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(params),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var spanAuthListMap = ret.data;
                        var spanAuthListCacheTemp = [];
                        for (var spanname in spanAuthListMap) {
                            if(spanAuthListMap.hasOwnProperty(spanname)){
                                spanAuthListCacheTemp.push({
                                    spanname: spanname,
                                    appkeyList: spanAuthListMap[spanname]
                                })
                            }
                        }
                        spanAuthListCache = spanAuthListCacheTemp;
                        fillSpanAuthListTable(spanAuthListCache);
                        Y.msgp.utils.msgpHeaderTip('success', '更新服务鉴权数据成功', 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '更新服务鉴权数据失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '更新服务鉴权数据失败', 3);
                }
            }
        });
    }

    function fillSpanAuthListTable(spanAuthList) {
        var spanAuthListAdjusted = spanAuthList == null ? [] : spanAuthList;
        //var defaultValue = [{"spanname": "spanname1", "appkeyList":["appkey1", "appkey2", "appkey3"]}, {"spanname": "spanname2", "appkeyList":["appkey4", "appkey5", "appkey6"]}];
        var micro = new Y.Template();
        var spanAuthListTableTbodyTemplate = Y.one('#text-span-auth-list-table-tbody').get('value');
        var html = micro.render(spanAuthListTableTbodyTemplate, {data: spanAuthListAdjusted});
        spanAuthListTableWrapper.one("tbody").setHTML(html);
    }


    function getAppkeyList() {
        curEnv = appkeyAuthWrapper.one('#auth_env_select a.btn-primary').getAttribute('value');
        var spannames = [], appkeyList = [];
        var requestPara = {
            appkey: appkey,
            env: curEnv,
            source: 'server'
        };
        $.ajax({
            type:"GET",
            data: requestPara,
            url: '/data/appkey_spanname',
            async: false,
            success:function(ret){  //function1()
                ret.data.remoteAppKeys.forEach(function (item) {
                  if(item != 'all' && item != 'unknownService'){
                      appkeyList.push(item);
                  }
                });
                ret.data.spannames.forEach(function (item) {
                    if(item != 'all' && item != '*'){
                        spannames.push(item);
                    }
                });
            }
        });
        return {appkeyList: appkeyList, spannames: spannames}
    }

    function distinctArray(arr){
        var self = arr;
        var _a = arr.concat().sort();
        _a.sort(function(a,b){
            if(a == b){
                var n = self.indexOf(a);
                self.splice(n,1);
            }
        });
        return self;
    }

    function removeArrayItem(arr, item) {
        var index = arr.indexOf(item);
        if(index >= 0 ){
            return arr.splice(index, 1)
        }else{
            return arr
        }
    }

}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete'
    ]
});
M.use('msgp-serviceopt/optAppkeyAuth', function (Y) {
    Y.msgp.serviceopt.optAppkeys(appkeyCache.key, appkeyCache.list);
});
