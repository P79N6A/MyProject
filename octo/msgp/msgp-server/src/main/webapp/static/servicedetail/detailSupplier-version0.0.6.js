/* jshint indent : false */
M.add('msgp-servicedetail/detailSupplier-version0.0.6', function (Y) {
    Y.namespace('msgp.servicedetail').detailSupplier = detailSupplier;
    var inited = false;
    var appkey,
        wrapper,
        tbody,
        pbody,
        provider_status_content,
        provider_idc_content;

    var idcchart, statuschart;

    var addSupplierDialog,
        changeWeightDialog;
    var ipInput,
        portInput,
        weightInput,
        isSearch = 0,
        searchInput;

    var colspan = 14;
    var everPaged = false,
        totalPage,
        totalCount,
        pageSize = 20;
    var currentSupplierAjax, isAbort = false;
    var idcname = '';
    var opt = {};

    var templateStr = [
        '<% Y.Array.each(this.data, function( item, index ){ %>',
        '<tr data-info="<%= Y.JSON.stringify(item) %>" class="tr_machine_node">',
        '<td><input id="one-checkbox" type="checkbox"></td>',
        '<td>',
        '<% if(item.name.substring(0,item.name.indexOf(".")) != "" && !isNaN(item.name.substring(0,item.name.indexOf(".")))){%>',
        '<%= item.name %>',
        '<%} else {%>',
        '<%= item.name %>',
        '<% }%></td>',
        '<td><%= item.ip %></td>',
        '<td >',
        '<% if("thrift"==item.protocol){%>',
        '<a class="add-white-list" id="port_<%= index %>"><i class="fa" ><%= item.port %></i></a>',
        '<%} else {%>',
        '<%= item.port %>',
        '<% }%>',
        '</td>',
        '<td>',
        '<div id="one-role" class="btn-group btn-role">',
        '<button data-role="0" class="btn btn-mini btn-alive <%= item.role==0?"active":"" %>">主用</button>',
        '<button data-role="1" class="btn btn-mini btn-dead <%= item.role==1?"active":"" %>">备机</button>',
        '</div>',
        '</td>',
        '<td><%= item.version %></td>',
        '<td><span id="one-weight" class="change-weight" title="点击修改"><%= item.weight %></span></td>',
        '<td class="status status-<%= item.status %>"><%= item.statusDesc %><% if(item.status == 0){ %><a target="_blank" style="padding-left:5px" id="indicator" href="https://123.sankuai.com/km/page/28354561" ><i class="fa fa-question-circle"></i></a><% } %></td>',
        // '<td>',
        // '<% if(2==Number(item.type)){%>',
        // 'OCTO',
        // '<%} else {%>',
        // '<%=item.extend.split("|")[0]%>',
        // '<% }%></td>',
        // '<td><%= item.swimlane %></td>',
        '<% if(isOffline=="true") {%>',
        '<td><%= item.cell %></td><td><%= item.swimlane %></td>',
        '<%} else {%>',
        '<td><%=item.cell%></td>',
        '<% }%>',
        '<td class="last-update-time"><%= Y.mt.date.formatDateByString( new Date(item.lastUpdateTime*1000), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td>',
        '<div id="one-enabled" class="btn-group btn-enabled">',
        '<button data-enabled="0" class="btn btn-mini btn-alive <%= (item.enabled==0 || !item.enabled)?"active":"" %>">启用</button>',
        '<button data-enabled="1" class="btn btn-mini btn-dead <%= item.enabled==1?"active":"" %>">禁用</button>',
        '</div>',
        '</td>',
        '<td>',
        '<button id="del_supplier" class="btn btn-mini btn-del" title="删除节点">删除</button>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');
    var envSelectStr = [
        '<option value="0" selected>全部</option>',
        '<% Y.Object.each( this.data.env, function( value, key ){ %>',
        '<option value="<%= key %>"><%= value %></option>',
        '<% }); %>'
    ].join('');
    var dialogContentStr = [
        '<div id="add_supplier_form" class="form-horizontal">',
        '<div class="control-group"><label class="control-label">Ip：</label>',
        '<div class="controls">',
        '<input id="s_ip" type="text" value="" placeholder="Ip，必填" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">Port：</label>',
        '<div class="controls">',
        '<input id="s_port" type="text" value="" placeholder="Port，必填" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">版本：</label>',
        '<div class="controls">',
        '<input id="s_version" type="text" readonly value="original" placeholder="original" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">权重：</label>',
        '<div class="controls">',
        '<input id="s_weight" type="text" value="10" placeholder="默认为10，必须纯数字" />',
        '<span class="tips">默认为10，必须纯数字</span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">状态：</label>',
        '<div class="controls">',
        '<select id="s_status">',
        '<% Y.Object.each( this.data.status, function( value, key ){ %>',
        '<option value="<%= key %>" <%= key==0?"selected":"" %>><%= value %></option>',
        '<% }); %>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">角色：</label>',
        '<div class="controls">',
        '<select id="s_role">',
        '<% Y.Object.each( this.data.role, function( value, key ){ %>',
        '<option value="<%= key %>" <%= key==0?"selected":"" %>><%= value %></option>',
        '<% }); %>',
        '</select>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    function detailSupplier(key) {
        if (!inited) {
            appkey = key;
            initParams();
            bindGetAgain();
            bindSearch();
            bindPageSize();
            bindAllClickButtons();
            bindClickCheckBox();
            bindChangeButtons();
            bindSelect();
            bindAddSupplier();
            bindChangeWeightDialog();
            bindDelSupplier();
            bindSupplierType();
            bindSorted();
            bindOutlineBtn();
            inited = true;
            idcchart = echarts.init(document.getElementById('providerIDC'));
            statuschart = echarts.init(document.getElementById('providerStatus'));
            wrapper.one('#searchBox').on('keyup', searchOnkeyup);
            Y.msgp.service.setEnvText('supplier_env_select');
            displayOutline('table');
        }
        //getSupplier(1);
        if ("com.sankuai.inf.sg_agent" === appkey || "com.sankuai.inf.kms_agent" === appkey) {
            wrapper.one('.overview-btn').hide();
        }
        wrapper.one('.menu-special')._node.style.display = 'none';
        doSearch();
        getUtilization();
    }

    function initParams() {
        wrapper = Y.one('#wrap_supplier');
        tbody = wrapper.one('#supplier_content_body');
        pbody = wrapper.one('#paginator_supplier');
        provider_status_content = wrapper.one('#provider_status_content');
        provider_idc_content = wrapper.one('#provider_idc_content');
    }

    function getUtilization() {
        var isOffline = Y.one('#isOffline').get("value");
        //正式环境下才有资源利用率
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        if (isOffline == 'false' && env == 'prod') {
            var url = '/data/utilization/get';
            Y.io(url, {
                method: 'get',
                data: {
                    appkey: appkey
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            if (data.corp == '') {
                                Y.one('#resource_utilization_wrapper').setStyle('display', 'none');
                            } else {
                                Y.one("#resource_utilization_rate").set("text", (data.utilizationRate * 100).toFixed(2) + '%');
                                var lowRateDesc = (data.lowRate * 100).toFixed(2) + '%';

                                if (data.lowRate < 0.3) {
                                    Y.one("#resource_vacancy_rate").setStyle('color', '#394');
                                    lowRateDesc += '(正常)'
                                } else if (data.lowRate > 0.7) {
                                    Y.one("#resource_vacancy_rate").setStyle('color', '#f33');
                                    lowRateDesc += '(很差)'
                                } else {
                                    Y.one("#resource_vacancy_rate").setStyle('color', '#f90');
                                    lowRateDesc += '(良好)'
                                }
                                Y.one("#resource_vacancy_rate").set("text", lowRateDesc);
                                Y.one('#resource_utilization_link').setAttribute("href", data.opsLink);
                                Y.one('#resource_utilization_wrapper').setStyle('display', 'inherit');
                            }
                        } else {
                            Y.one('#resource_utilization_wrapper').setStyle('display', 'none');
                        }
                    },
                    failure: function () {
                        Y.one('#resource_utilization_wrapper').setStyle('display', 'none');
                    }
                }
            });
        } else {
            Y.one('#resource_utilization_wrapper').setStyle('display', 'none');
        }
    }


    function searchOnkeyup(e) {
        if (e.keyCode === 13) {
            doSearch();
        }
    }

    function bindSupplierType() {
        wrapper.delegate('click', function () {
            Y.all('#supplier_thrift_http a').removeClass('btn-primary');
            this.addClass("btn-primary");
            doSearch();
            outlineOpenProcess('all')
        }, '#supplier_type');
    }

    function bindDelSupplier() {
        wrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = patchDataForChangeLine(line);
            var delSupplierDialog = new Y.mt.widget.CommonDialog({
                id: 'del_supplier_dialog',
                title: '删除提供者',
                content: '确认删除 ' + data.name + '(' + data.ip + ')' + ':' + data.port + '?<br/>提示：如果想彻底删除服务节点请确保程序不再会运行，否则会被自动注册',
                width: 300,
                btn: {
                    pass: doDelSupplier
                }
            });
            delSupplierDialog.show();

            function doDelSupplier() {
                var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
                Y.io('/service/' + appkey + '/provider/' + type + '/del', {
                    method: 'post',
                    headers: {'Content-Type': "application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(data),
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                                getSupplierWithSearch(1);
                                outlineOpenProcess('all');
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                            }
                        },
                        failure: function () {
                            Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                        }
                    }
                });
            }
        }, '#del_supplier');
    }

    function bindSearch() {
        wrapper.delegate('click', function () {
            idcname = '';
            wrapper.one('#currentIdc').hide();
            doSearch();
        }, '#searchBtn');
    }

    function doSearch() {
        //获取输入文档
        searchInput = Y.Lang.trim(wrapper.one('#searchBox').get('value'));

        if (searchInput) {
            //调用后台
            getSearchSupplier(1);
        } else {
            getSupplier(1);
        }
    }

    function bindPageSize() {
        Y.all("#pageSize a").on('click', function (e) {
            var size = e.target.getData('value');
            console.log(size);
            pageSize = Number(size);
            doSearch();
        })
    }

    function getSupplierWithSearch(pageNo, pieClickUrl) {
        searchInput = Y.Lang.trim(wrapper.one('#searchBox').get('value'));
        if (searchInput) {
            //调用后台
            getSearchSupplier(1, pieClickUrl);
        } else {
            getSupplier(1, pieClickUrl);
        }
    }

    function getSearchSupplier(pageNo, pieClickUrl) {
        isSearch = 1;
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        var status = Y.one('#supplier_status_select a.btn-primary').getAttribute('value');
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        showContent('loading');
        var url = pieClickUrl ? pieClickUrl : '/service/' + appkey + '/provider/search';
        var sortBtn = wrapper.one('#table_supplier .fa-sort-asc');
        opt = {
            appkey: Y.one("#apps_select").get("value"),
            type: type,
            env: env,
            status: status,
            keyword: Y.one("#searchBox").get("value")
        };
        Y.msgp.utils.urlAddParameters(opt);
        if (!sortBtn) {
            sortBtn = wrapper.one('#table_supplier .fa-sort-desc');
        }
        var sortValue = Number(sortBtn.ancestor('th').getAttribute('value'));
        if ('undefined' != typeof(currentSupplierAjax)) {
            isAbort = true;
            currentSupplierAjax.abort();
        }
        isAbort = false;
        if ('undefined' != typeof(currentSupplierAjax)) {
            isAbort = true;
            currentSupplierAjax.abort();
        }
        currentSupplierAjax = Y.io(url, {
            method: 'get',
            data: {
                type: type,
                env: env,
                status: status,
                keyword: searchInput,
                pageNo: pageNo,
                pageSize: pageSize,
                sort: sortValue
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillSupplier(data);
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pbody, pobj);
                            }
                        } else if (data.length === 0) {
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
                    if (!isAbort)
                        emptyOrError(true);
                }
            }
        });
    }

    function bindGetAgain() {
        tbody.delegate('click', function () {
            getSupplierWithSearch(1);
        }, '.get-again');
    }

    function bindAllClickButtons() {
        //角色
        wrapper.delegate('click', function () {
            var isActive = this.hasClass('active');
            if (isActive) {
                return;
            } else {
                var role = +this.getData('role');
                //找到所有的checked,设置role，返回给后端
                var allCheckedTr = wrapper.all("#one-checkbox:checked");
                if (allCheckedTr.size() === 0) {
                    return;
                } else {
                    var dataList = [];
                    allCheckedTr.each(function (item, index) {
                        var line = item.ancestor('tr');
                        var data = patchDataForChangeLine(line);
                        data.role = role;
                        dataList.push(data);
                    });
                    doChangeAll(dataList);
                }
            }
        }, '#all-role .btn');

        //启用、停用
        wrapper.delegate('click', function () {
            var isActive = this.hasClass('active');
            if (isActive) {
            } else {
                var enabled = +this.getData('enabled');
                //找到所有的checked,设置role，返回给后端
                var allCheckedTr = wrapper.all("#one-checkbox:checked");
                if (allCheckedTr.size() === 0) {
                    Y.msgp.config.popup.alert('请至少选择一项',2000);
                } else {
                    var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
                    //http环境且为禁用操作
                    if (type == 2 && enabled == 1) {
                        //只有一个存活节点
                        var enabledHlbNodes = getEnabledNodes();
                        if (enabledHlbNodes <= 1) {
                            Y.msgp.utils.msgpHeaderTip('error', '至少要保证一个HTTP节点存活', 5);
                            return;
                        } else {
                            //有多个存活节点
                            var enabledHlbNodesChecked = [];
                            allCheckedTr.each(function (item) {
                                var line = item.ancestor('tr');
                                var info = line.getData('info');
                                if (Y.Lang.isString(info)) {
                                    info = Y.JSON.parse(info);
                                }
                                if (info.status == 2) {
                                    enabledHlbNodesChecked.push(info)
                                }
                            });
                            //如果选择禁用的HLB节点大于存货的HLB节点, 则不允许此操作
                            if (enabledHlbNodesChecked.length >= enabledHlbNodes) {
                                Y.msgp.utils.msgpHeaderTip('error', '至少要保证一个HTTP节点存活', 5);
                                return;
                            }
                        }
                    }

                    var dataList = [];
                    allCheckedTr.each(function (item, index) {
                        var line = item.ancestor('tr');
                        var data = patchDataForChangeLine(line);
                        data.enabled = enabled;
                        if (enabled == 2) {
                            data.enabled = 0;
                            data.status = 2;
                        }
                        dataList.push(data);
                    });
                    var msg = "启用";
                    switch (enabled) {
                        case 0:
                            msg = "启动";
                            break;
                        case 1:
                            msg = "禁用";
                            break;
                        case 2:
                            msg = "正常";
                            break;
                    }
                    if (window.confirm('你确定要修改主机状态为' + msg + '吗?')) {
                        doChangeAll(dataList);
                    }
                }
            }
        }, "#all-enabled .btn");

        //删除
        wrapper.one("#del-all-supplier").on('click', function () {
            var allCheckedTr = wrapper.all("#one-checkbox:checked");
            if (allCheckedTr.size() === 0) {
                Y.msgp.utils.msgpHeaderTip('info', '未选中任何提供者', 3);
            } else {
                var delSupplierDialog = new Y.mt.widget.CommonDialog({
                    id: 'del_all_supplier_dialog',
                    title: '删除提供者',
                    content: '确认删除选中的提供者?<br/>提示：如果想彻底删除服务节点请确保程序不再会运行，否则会被自动注册',
                    width: 300,
                    btn: {
                        pass: doDelAllSupplier
                    }
                });
                delSupplierDialog.show();

                function doDelAllSupplier() {
                    var dataList = [];
                    allCheckedTr.each(function (item, index) {
                        var line = item.ancestor('tr');
                        var data = patchDataForChangeLine(line);
                        dataList.push(data);
                    });
                    doDeleteAll(dataList);
                }
            }
        });
    }

    function doChangeAll(dataList) {
        refreshAllLineData(dataList, successCallback, errorCallback);

        function successCallback(newData) {
            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
            //清空全选
            wrapper.one('#all-check').set('checked', false);
            getSupplierWithSearch(1);
            outlineOpenProcess('all');
        }

        function errorCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg || '修改失败', 3);
        }
    }

    function doDeleteAll(dataList) {
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        var url = '/service/' + appkey + '/provider/' + type + '/list/del';
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(dataList),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        //清空全选
                        wrapper.one('#all-check').set('checked', false);
                        getSupplierWithSearch(1);
                        outlineOpenProcess('all');
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                }
            }
        });
    }

    function refreshAllLineData(data, sc, ec) {
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        var url = '/service/' + appkey + '/provider/' + type + '/list';
        Y.io(url, {
            method: 'put',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    outlineOpenProcess('all');
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        sc && sc(ret.data);
                    } else {
                        ec && ec(ret.msg);
                    }
                },
                failure: function () {
                    ec && ec();
                }
            }
        });
    }

    function bindClickCheckBox() {
        //全选
        wrapper.delegate('click', function () {
            var ifChecked = this.get("checked");
            //单选与全选保持一致
            wrapper.all('#one-checkbox').set("checked", ifChecked);
        }, '#all-check');
        //单选
        wrapper.delegate('click', function () {
            //全选与单选保持一致
            var allOneCheck = wrapper.all('#one-checkbox');
            var allOneChecked = wrapper.all('#one-checkbox:checked');
            if (allOneChecked.size() === 0) {
                wrapper.one('#all-check').set("checked", false);
            } else {
                if (allOneCheck.size() === allOneChecked.size()) {
                    wrapper.one('#all-check').set("checked", true);
                }
            }
        }, '#one-checkbox');
    }

    function bindChangeButtons() {
        wrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');
            var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
            //http环境下, 执行禁用检查: 存活一个节点时, 不允许禁用操作
            var line = el.ancestor('tr');
            var info = line.getData('info');
            if (Y.Lang.isString(info)) {
                info = Y.JSON.parse(info);
            }
            var status = info.status;
            //当禁用节点为最后一个存活节点时, 不允许此操作
            if (type == 2 && enabled == 1 && status == 2) {
                var enabledHlbNodes = getEnabledNodes();
                if (enabledHlbNodes <= 1) {
                    Y.msgp.utils.msgpHeaderTip('error', '至少要保证一个HTTP节点存活', 5);
                    return;
                }
            }
            doChangeEnabled(el, enabled);
        }, '#one-enabled .btn');
        wrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var role = +el.getData('role');
            doChangeRole(el, role);
        }, '#one-role .btn');
    }

    function bindSelect() {
        wrapper.delegate('click', function () {
            Y.all('#supplier_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            //清空搜索框
            getSupplierWithSearch(1);
            idcname = '';
            wrapper.one('#currentIdc').hide();
            outlineOpenProcess('all')
        }, "#supplier_env_select a");

        wrapper.delegate('click', function () {
            Y.all('#supplier_status_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            //清空搜索框
            getSupplierWithSearch(1);
            idcname = '';
            wrapper.one('#currentIdc').hide();
            outlineOpenProcess('all')
        }, "#supplier_status_select a")
    }

    function bindSorted() {
        wrapper.delegate('click', function () {
            var isDesc = true;
            if (this.one("i").hasClass("fa-sort-desc")) {
                isDesc = false;
            } else if (this.one("i").hasClass("fa-sort-asc")) {
                isDesc = true;
            } else {
                isDesc = true;
            }
            var allNodes = this.ancestor("tr").all("i");
            allNodes.removeClass("fa-sort");
            allNodes.addClass("fa-sort");
            allNodes.removeClass("fa-sort-desc");
            allNodes.removeClass("fa-sort-asc");
            this.one("i").removeClass("fa-sort");
            this.one("i").addClass(isDesc ? "fa-sort-desc" : "fa-sort-asc");
            var nodeValue = Number(this.getAttribute('value'));
            nodeValue = isDesc ? -Math.abs(nodeValue) : Math.abs(nodeValue);
            this.setAttribute('value', nodeValue);
            getSupplierWithSearch();
        }, "#table_supplier .sorttable-header-bg");
    }

    function bindAddSupplier() {
        wrapper.delegate('click', function () {
            addSupplierDialog = addSupplierDialog ? addSupplierDialog : new Y.mt.widget.CommonDialog({
                id: 'add_supplier_dialog',
                title: '增加提供者',
                width: 640,
                btn: {
                    pass: doAddSupplier
                }
            });
            Y.msgp.service.commonMap(showDialogAfter);

            function showDialogAfter(obj) {
                var micro = new Y.Template();
                var str = micro.render(dialogContentStr, {data: obj});
                addSupplierDialog.setContent(str);
                addSupplierDialog.show();
                initAddSupplierDialog();
            }
        }, '#add_supplier');
    }

    function bindChangeWeightDialog() {
        wrapper.delegate('click', function () {
            changeWeightDialog = changeWeightDialog ? changeWeightDialog : new Y.mt.widget.CommonDialog({
                id: 'change_weight_dialog',
                title: '修改权重',
                width: 300,
                btn: {
                    pass: doChangeWeights
                }
            });
            Y.msgp.service.commonMap(showDialogAfter);

            function showDialogAfter(obj) {
                var micro = new Y.Template();
                var str = ['<div class="control-group"><label class="control-label">权重：</label>',
                    '<div class="controls">',
                    '<input id="input_weight" type="text" value="10" placeholder="默认为10，必须纯数字" />',
                    '<span class="tips">默认为10，必须纯数字</span>',
                    '</div>'].join('');
                changeWeightDialog.setContent(str);
                changeWeightDialog.show();
                initChangeWeightDialog();
            }
        }, '#data-weight');
    }

    function initAddSupplierDialog() {
        ipInput = Y.msgp.utils.check.init(Y.one('#s_ip'), {
            type: 'custom',
            //customRegExp : /^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$/g,
            customRegExp: /^((25[0-5]|2[0-4][0-9]|1[0-9]{1,2}|[2-9][0-9]|[0-9])\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{1,2}|[2-9][0-9]|[0-9])$/g,
            warnMsg: '格式错误',
            warnElement: Y.one('#s_ip').next('')
        });
        portInput = Y.msgp.utils.check.init(Y.one('#s_port'), {
            type: 'int',
            minValue: 1,
            maxValue: 65535,
            warnMsg: '纯数字1~65535',
            warnElement: Y.one('#s_port').next('')
        });
        weightInput = Y.msgp.utils.check.init(Y.one('#s_weight'), {
            type: 'custom',
            customRegExp: /^\d+$/g,
            emptyOk: true,
            warnMsg: '必须是纯数字',
            warnElement: Y.one('#s_weight').next('')
        });
    }

    function initChangeWeightDialog() {
        weightInput = Y.msgp.utils.check.init(Y.one('#input_weight'), {
            type: 'custom',
            customRegExp: /^(\d{1,2}|100)$/g,
            emptyOk: true,
            warnMsg: '必须0~100的数字',
            warnElement: Y.one('#input_weight').next('')
        });
    }

    function doAddSupplier() {
        if (!ipInput.node.getData('status') || !portInput.node.getData('status') || !weightInput.node.getData('status')) {
            ipInput.showMsg();
            portInput.showMsg();
            weightInput.showMsg();
            return true;
        }
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        var url = '/service/' + appkey + '/provider/' + type;
        var data = {
            appkey: appkey,
            version: Y.one('#s_version').get('value') !== "" ? Y.one('#s_version').get('value') : "1",
            ip: Y.one('#s_ip').get('value'),
            port: +Y.one('#s_port').get('value'),
            weight: Y.one('#s_weight').get('value') !== "" ? +Y.one('#s_weight').get('value') : 10,
            status: +Y.one('#s_status').get('value'),
            role: +Y.one('#s_role').get('value'),
            env: +getEnvIntegerByDesc(Y.one('#supplier_env_select a.btn-primary').getAttribute('value')),
            lastUpdateTime: ~~(new Date() / 1000),
            unixtime: ~~(new Date() / 1000),
            extend: ''
        };
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
                        addSupplierDialog.close();
                        //清空搜索框
                        wrapper.one('#searchBox').set('value', "");
                        getSupplier(1);
                        outlineOpenProcess('all');
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                }
            }
        });
        return true;
    }

    //批量修改权重
    function doChangeWeights() {
        var weight = Y.one('#input_weight').get('value') !== "" ? +Y.one('#input_weight').get('value') : 10;
        var allCheckedTr = wrapper.all("#one-checkbox:checked");
        if (allCheckedTr.size() === 0) {
            return;
        } else {
            var dataList = [];
            allCheckedTr.each(function (item, index) {
                var line = item.ancestor('tr');
                var data = patchDataForChangeLine(line);
                data.weight = weight;
                data.fweight = weight;
                dataList.push(data);
            });
            if (window.confirm('你确定要修改权重吗?')) {
                doChangeAll(dataList);
            }
        }
    }

    /**
     *
     *  从后端获取总数
     */
    function getEnabledNodes() {
        var count = 0;
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        var url = '/provider/count';
        Y.io(url, {
            sync: true,
            method: 'get',
            data: {
                appkey: appkey,
                env: env,
                status: 2,
                type: type

            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    count = ret
                }
            }
        });
        return count;
    }

    function doChangeEnabled(el, enabled) {
        var line = el.ancestor('tr');
        var data = patchDataForChangeLine(line);
        data.enabled = enabled;
        refreshLineData(data, successCallback, errorCallback);

        function successCallback(newData) {
            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
            refreshLineUI(line, newData);
            outlineOpenProcess('all');
        }

        function errorCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg || '修改失败', 3);
        }
    }

    function doChangeRole(el, role) {
        var line = el.ancestor('tr');
        var data = patchDataForChangeLine(line);
        data.role = role;
        refreshLineData(data, successCallback, errorCallback);

        function successCallback(newData) {
            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
            refreshLineUI(line, newData);
        }

        function errorCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg || '修改失败', 3);
        }
    }


    function emptyOrError(isError) {
        var html = '<tr class="supplier_error"><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        showContent('error', html);
        pbody.empty();
        //showContent(wrapper);
    }

    function getSupplier(pageNo, pieClickUrl) {
        isSearch = 0;
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        var status = Y.one('#supplier_status_select a.btn-primary').getAttribute('value');
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');

        showContent('loading');
        //var url = '/service/' + appkey + '/provider';

        var url = pieClickUrl ? pieClickUrl : '/service/' + appkey + '/provider';
        opt = {
            appkey: Y.one("#apps_select").get("value"),
            type: type,
            env: env,
            status: status,
            keyword: Y.one("#searchBox").get("value")
        };
        Y.msgp.utils.urlAddParameters(opt);
        url = ('' !== idcname) ? ('/service/' + appkey + '/provider/querybyidc?idcname=' + idcname) : url;
        env = (pieClickUrl || '' !== idcname) ? getEnvIntegerByDesc(env) : env;
        isAbort = false;
        var sortBtn = wrapper.one('#table_supplier .fa-sort-asc');
        if (!sortBtn) {
            sortBtn = wrapper.one('#table_supplier .fa-sort-desc');
        }
        var sortValue = Number(sortBtn.ancestor('th').getAttribute('value'));
        if ('undefined' != typeof(currentSupplierAjax)) {
            isAbort = true;
            currentSupplierAjax.abort();
        }
        currentSupplierAjax = Y.io(url, {
            method: 'get',
            data: {
                type: type,
                env: env,
                status: status,
                pageNo: pageNo,
                pageSize: pageSize,
                sort: sortValue
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillSupplier(data, type);
                            //wrapper.one('.menu-special').show();
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pbody, pobj);
                            }
                        } else if (data.length === 0) {
                            emptyOrError();
                            //wrapper.one('.menu-special').hide();
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;

                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    if (!isAbort)
                        emptyOrError(true);
                }
            }
        });
    }


    function showContent(type, data) {
        var supplier_error = tbody.one('.supplier_error');
        if (null != supplier_error) {
            supplier_error.remove();
        }
        tbody.one('#content_overlay').hide();
        pbody.show();
        switch (type) {
            case 'error':
                tbody.append(data);
                break;
            case 'loading':
                tbody.all('.tr_machine_node').remove();
                tbody.one('#content_overlay').show();
                pbody.hide();
                break;
            case 'data':
                tbody.append(data);
                break;
        }
    }

    function fillSupplier(arr, type) {
        Y.msgp.service.commonMap(fillSupplierAfter);

        function fillSupplierAfter(obj) {
            var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
            for (var i = 0, l = arr.length; i < l; i++) {
                var tmp = arr[i];
                tmp.statusDesc = obj.status[tmp.status] || tmp.status;
                tmp.type = type;
                tmp.envDesc = obj.env[tmp.env] || tmp.env;
                tmp.roleDesc = obj.role[tmp.role] || tmp.role;
            }
            var html = wrapData(arr);
            showContent('data', html);
            initLocalEditWeight();
            if (type == 1) {
                for (var i = 0, l = arr.length; i < l; i++) {
                    var tmp = arr[i];
                    var serviceInfo = tmp.serviceInfo;
                    var map = new Map();
                    Y.Object.each(serviceInfo, function (value, key) {
                        var end = key.lastIndexOf("\.");
                        var package = key.substring(0, end);
                        var interface = key.substring(end + 1);
                        if (map.has(package)) {
                            map.get(package).push(interface);
                        } else {
                            var interfaceArray = [];
                            interfaceArray.push(interface);
                            map.set(package, interfaceArray);
                        }
                    });

                    var interfaceTip = '';
                    map.forEach(function (item, key) {
                        var rows = item.length;
                        interfaceTip += '<p>包名：' + key + '</p><p>接口类：' + item[0] + '</p>';
                        for (var i = 1; i < rows; i++) {
                            interfaceTip += '<p style="margin-left: 56px">' + item[i] + '</p>';
                        }
                    })
                    $("#port_" + i).tooltip({
                        html: true,
                        title: interfaceTip,
                        delay: {
                            hide: 100
                        },
                        container: $("#port_" + i)
                    });
                }
            }

        }
    }

    function wrapData(arr) {
        var micro = new Y.Template();
        var str = micro.render(templateStr, {data: arr});
        return str;
    }

    function initLocalEditWeight() {
        var reg = /^(\d{1,2}|100)$/g;
        var msg = '0-100之间的整数';
        Y.msgp.utils.localEdit('#one-weight', doChangeWeight, reg, msg);
    }

    function doChangeWeight(node, oldValue, newValue) {
        var line = node.ancestor('tr');
        var data = patchDataForChangeLine(line);
        data.weight = +newValue;
        data.fweight = +newValue;
        refreshLineData(data, successCallback, errorCallback);

        function successCallback(newData) {
            Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
            refreshLineUI(line, newData);
        }

        function errorCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg || '修改失败', 3);
            node.setHTML(oldValue);
        }
    }

    function refreshLineData(data, sc, ec) {
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        var url = '/service/' + appkey + '/provider/' + type + '/' + data.ip + ':' + data.port;
        Y.io(url, {
            method: 'put',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        sc && sc(ret.data);
                    } else {
                        ec && ec(ret.msg);
                    }
                },
                failure: function () {
                    ec && ec();
                }
            }
        });
    }

    function patchDataForChangeLine(line) {
        var info = line.getData('info');
        if (Y.Lang.isString(info)) {
            info = Y.JSON.parse(info);
        }
        var infoApp = {
            appkey: info.appkey,
            ip: info.ip,
            port: info.port,
            name: info.name,
            env: info.env,
            extend: info.extend
        };

        return infoApp;
    }

    function refreshLineUI(line, data) {
        line.setData('info', data);
        //更新table中最后更新时间这一列
        line.one('.last-update-time').setHTML(Y.mt.date.formatDateByString(new Date(data.lastUpdateTime * 1000), "yyyy-MM-dd hh:mm:ss"));
        //更新是否启用状态
        line.one('.btn-enabled .active').removeClass('active');
        line.one('.btn-enabled [data-enabled="' + data.enabled + '"]').addClass('active');
        //更新状态
        var td = line.one('.status');
        td.setHTML(data.statusDesc);
        td.removeAttribute('class');
        td.addClass('status status-' + data.status)
        if (data.status == 0) {
            td.setHTML(data.statusDesc + "<a target=\"_blank\" style=\"padding-left:5px\" id=\"indicator\" href=\"https://123.sankuai.com/km/page/28354561\" ><i class=\"fa fa-question-circle\"></i></a>")
        }
        //更新角色
        line.one('.btn-role .active').removeClass('active');
        line.one('.btn-role [data-role="' + data.role + '"]').addClass('active');
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
        if (isSearch) {
            getSearchSupplier(params.page);
        } else {
            getSupplier(params.page);
        }
    }

    function bindOutlineBtn() {
        wrapper.delegate('click', function (e) {
            if (this.one('span').hasClass('fa-angle-down')) {
                this.one('span').removeClass('fa-angle-down').addClass('fa-angle-up');
                wrapper.one('.menu-special')._node.style.display = 'block';
                outlineOpenProcess('all')
            } else {
                this.one('span').removeClass('fa-angle-up').addClass('fa-angle-down');
                idcname = '';
                wrapper.one('#currentIdc').hide();
                getSupplier(1);
                wrapper.one('.menu-special')._node.style.display = 'none';
            }
        }, '.overview-btn');
    }

    function displayOutline(outline_type) {
        var url = '/service/' + appkey + '/provider/outline';
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        env = getEnvIntegerByDesc(env);
        var status = Y.one('#supplier_status_select a.btn-primary').getAttribute('value');
        var type = Y.one('#supplier_thrift_http a.btn-primary').getAttribute('value');
        var sortBtn = wrapper.one('#table_supplier .fa-sort-asc');

        if (!sortBtn) {
            sortBtn = wrapper.one('#table_supplier .fa-sort-desc');
        }
        var sortValue = Number(sortBtn.ancestor('th').getAttribute('value'));
        Y.io(url, {
            method: 'get',
            data: {
                env: env,
                status: status,
                type: type,
                sort: sortValue,
                pageSize: -1
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        switch (outline_type) {
                            case 'all':
                                var clone_ret = cloneObj(ret)
                                displayOutlineCharts(ret, status, type, sortValue);
                                displayOutlineTable(clone_ret);
                                break;
                            case 'chart':
                                displayOutlineCharts(ret, status, type, sortValue);
                                break;
                            default:
                                displayOutlineTable(ret);

                        }
                    } else {
                        switch (outline_type) {
                            case 'all':
                                idcchart.hideLoading();
                                statuschart.hideLoading();
                                resetOutlineTableContent(provider_idc_content);
                                resetOutlineTableContent(provider_status_content);
                                break;
                            case 'chart':
                                idcchart.hideLoading();
                                statuschart.hideLoading();
                                break;
                            default:
                                resetOutlineTableContent(provider_idc_content);
                                resetOutlineTableContent(provider_status_content);
                        }
                    }
                }
            }
        });
    }

    function displayOutlineCharts(ret, status, type, sortValue) {
        if (ret.data.idcList === null || ret.data.idcList.length === 0 || ret.data.statusList === null || ret.data.statusList.length === 0) {
            wrapper.one('.overview-btn').one('span').removeClass('fa-angle-up').addClass('fa-angle-down');
            wrapper.one('.menu-special').hide();
            idcchart.hideLoading();
            statuschart.hideLoading();
        } else {
            wrapper.one('.overview-btn').one('span').removeClass('fa-angle-down').addClass('fa-angle-up');
            wrapper.one('.menu-special').show();
            var idcseries = [];
            var idclegend = ret.data.idcList;
            for (var i = 0; i < idclegend.length; i++) {
                idcseries.push({value: ret.data.idcCount[i], name: idclegend[i]});
            }
            var statusseries = [];
            var statuslegend = ret.data.statusList;
            for (var j = 0; j < statuslegend.length; j++) {
                statuslegend[j] = getStatusDesc(statuslegend[j]);
                statusseries.push({value: ret.data.statusCount[j], name: statuslegend[j]});
            }
            setOutlinePieContent(idcchart, "机房分布统计", idclegend, idcseries, status, type, sortValue);
            setOutlinePieContent(statuschart, "节点状态统计", statuslegend, statusseries, status, type, sortValue);
        }
    }

    function setOutlinePieContent(chart, title, legend, series, status, type, sortValue) {
        var option = {
//        color: ['#238e68','#00ff7f','#3299cc'],
            animation: false,
            title: {
                text: title,
                x: 'center'
            },
            legend: {
                orient: 'vertical',
                x: 'left',
                data: legend
            },
            tooltip: {
                show: true,
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)",
                position: 'inside',
                textStyle: {
                    fontSize: '12'
                }
            },
            series: [
                {
                    type: 'pie',
                    selectMode: 'single',
                    radius: '55%',
                    center: ['50%', '60%'],
                    data: series
                }
            ],
            labelLine: {
                normal: {
                    show: false
                }
            },
            itemStyle: {
                emphasis: {
                    shadowBlur: 0,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        };
        chart.setOption(option);
        chart.hideLoading();

        //绑定点击饼图点击事件
        function eConsole(param) {
            var methodName = "机房" === title ? "querybyidc" : "querybystatus";
            var typeParam = "机房" === title ? "?idcname=" + param.name : "";
            var url = '/service/' + appkey + '/provider/' + methodName + typeParam;
            idcname = ("机房" === title) ? param.name : '';
            if ("机房" === title) {
                wrapper.one('#currentIdc').show();
                wrapper.one('#currentIdc label').set('text', "当前机房是:" + idcname);
            } else {
                wrapper.one('#currentIdc').hide();
            }
            if (methodName === 'querybystatus') {
                var alist = wrapper.all('#supplier_status_select a');
                alist.each(function (node) {
                    if (node.getAttribute('value') == getStatus(param.name)) {
                        node.addClass("btn-primary");
                    } else {
                        node.removeClass("btn-primary");
                    }
                })
            }
            //清空搜索框
            getSupplierWithSearch(1, url);
        }

        chart.on("click", eConsole);
    }

    function displayOutlineTable(ret) {
        if (ret.data.idcList === null || ret.data.idcList.length === 0 || ret.data.statusList === null || ret.data.statusList.length === 0) {
            resetOutlineTableContent(provider_idc_content);
            resetOutlineTableContent(provider_status_content);
        } else {
            var idcNames = ret.data.idcList;
            var idcCount = ret.data.idcCount;
            var hostCount = ret.data.hostCount;
            var idcCountExtended = [];
            idcCount.forEach(function (item, index) {
                idcCountExtended.push(item + ' / ' + hostCount[index])
            });
            var statusList = ret.data.statusList;
            var statusNames = [];
            var statusCount = ret.data.statusCount;
            statusList.forEach(function (item) {
                statusNames.push(getStatusDesc(item))
            });
            setOutlineTableContent(provider_idc_content, idcNames, idcCountExtended);
            setOutlineTableContent(provider_status_content, statusNames, statusCount);
        }
    }

    function setOutlineTableContent(node, names, values) {
        var data = [];
        var width = parseInt(100 / names.length);
        names.forEach(function (name, index) {
            var value = values[index];
            data.push({
                name: name,
                value: value,
                width: width
            })
        });
        var outline_talbe_Template =
            '<table style="width: 100%">' +
            '<tbody>' +
            '<tr style="text-align: center">' +
            '<% Y.Array.each(this.data, function(item, index){ %>' +
            '<td style="width: <%= item.width %>%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><span style="font-weight: bold"><%= item.name %></span><span style="padding-left: 10px">: <%= item.value %></span></td>' +
            '<% }); %>' +
            '</tr>' +
            '</tbody>' +
            '</table>';
        var micro = new Y.Template();
        var str = micro.render(outline_talbe_Template, {data: data});
        node.setHTML(str);
    }

    function resetOutlineTableContent(node) {
        var str = '<div style="text-align: center">无数据 </div>';
        node.setHTML(str);
    }

    function getStatusDesc(status) {
        var msg = "启用";
        switch (status) {
            case 0:
                msg = "未启动";
                break;
            case 2:
                msg = "正常";
                break;
            case 4:
                msg = "禁用";
                break;
        }
        return msg;
    }

    function getStatus(statusDesc) {
        var msg = 0;
        switch (statusDesc) {
            case "未启动":
                msg = 0;
                break;
            case "正常":
                msg = 2;
                break;
            case "禁用":
                msg = 4;
                break;
        }
        return msg;
    }

    function outlineOpenProcess() {
        if (wrapper.one('.overview-btn').one('span').hasClass('fa-angle-up')) {
            displayOutline('all');
        } else {
            displayOutline('table');
        }
    }

    function getEnvIntegerByDesc(envDesc) {
        switch (envDesc) {
            case 'prod':
                return 3;
            case 'stage':
                return 2;
            case 'test':
                return 1;
            default:
                break;
        }
    }

    function cloneObj(obj) {
        var str, newobj = obj.constructor === Array ? [] : {};
        if (typeof obj !== 'object') {
            return;
        } else if (window.JSON) {
            str = JSON.stringify(obj), //系列化对象
                newobj = JSON.parse(str); //还原
        } else {
            for (var i in obj) {
                newobj[i] = typeof obj[i] === 'object' ?
                    cloneObj(obj[i]) : obj[i];
            }
        }
        return newobj;
    };

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
        'msgp-utils/common',
        'msgp-config/popup'
    ]
});
