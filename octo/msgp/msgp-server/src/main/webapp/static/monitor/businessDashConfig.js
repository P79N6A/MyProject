M.add('msgp-monitor/businessDashConfig', function (Y) {
        Y.namespace('msgp.monitor').businessDashConfig = businessDashConfig;

        var _owtName = {
            web: '到店餐饮BG_团购业务线',
            mobile: '到店餐饮BG_移动端',
            meishi: '到店餐饮BG_美食业务线',
            srq: '到店餐饮BG_智能餐厅',

            sjst: '餐饮生态平台BG_商家生态',

            waimai: '外卖配送BG_外卖业务线',
            banma: '外卖配送BG_配送业务线',
            shangchao: '外卖配送BG_商超业务线',
            sc: '外卖配送BG_商超业务线',//废弃
            wmarch: '外卖配送BG_外卖基础架构',
            xianguo: '外卖配送BG_鲜果',
            retail: '外卖配送BG_零售研发',

            movie: '猫眼电影BG_电影业务线',

            hotel: '酒店旅游BG_酒店业务线',
            trip: '酒店旅游BG_门票业务线',
            tower: '酒店旅游BG_目的地业务线',
            flight: '酒店旅游BG_机票业务线',
            ia: '酒店旅游BG_智能住宿业务线',
            hotel_train: '酒店旅游BG_火车票业务线',
            train: '酒店旅游BG_火车票业务线',
            travel: '酒店旅游BG_酒旅平台',
            hbdata: '酒店旅游BG_酒店数据',
            tdc: '酒店旅游BG_TDC', //废弃
            icb: '酒店旅游BG_创新业务',
            oversea: '酒店旅游BG_酒店海外',

            xm: '企业平台BG_大象业务线',
            mit: '企业平台BG_技术学院',
            it: '企业平台BG_企业平台',

            pay: '金融服务BG_支付业务线',
            fd: '金融服务BG_金融发展',
            zc: '金融服务BG_招财猫',

            adp: '广告BG_广告平台',

            bp: '平台BG_商户挖据',
            giant: '平台BG_流量',
            fe: '平台BG_平台终端',
            dataapp: '平台BG_数据应用',
            wpt: '平台BG_服务平台',

            rc: '技术工程BG_风控',
            inf: '技术工程BG_基础架构',
            mx: '技术工程BG_基础终端',
            dba: '技术工程BG_DBA',
            tair: '技术工程BG_TAIR',
            cloudprivate: '技术工程BG_私有云',
            cloudpublic: '技术工程BG_公有云',
            sec: '技术工程BG_信息安全',
            cloudoffice: '技术工程BG_办公云',
            cloud: '技术工程BG_云计算',
            sre: '技术工程BG_服务运维',
            ep: '技术工程BG_工程质量',
            ee: '技术工程BG_工程效率',
            network: '技术工程BG_network',//废弃
            data: '技术工程BG_数据组',
            gct: '到店综合BG_综合品类'
        };

        var twrap = Y.one('#triggers_wrap');
        var tbody = twrap.one('tbody');

        var owtAutoComplete, addMetricDialog, editMetricDialog;
        var metricsMap = [];

        function businessDashConfig(owtList, owt) {
            var owts = [];
            Y.Array.each(owtList, function (item, index) {
                var name = _owtName[item] ? _owtName[item] : item;
                owts.push({id: item, name: name});
            });
            owts.sort(function (a, b) {
                return a.name > b.name;
            });
            var defaultName = _owtName[owt] ? _owtName[owt] : owt;
            autoCompleteOwts(owts.sort(), {id: owt, name: defaultName});
            bindAdd();
            getMetricsAndScreen();
            bindEditDeleteMetric();
        }

        function bindAdd() {
            Y.one('#add_metric').on('click', function () {
                var flag = 'add';
                addMetricDialog = addMetricDialog ? addMetricDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_metric_dialog',
                    title: '新增指标',
                    width: 640,
                    btn: {
                        pass: function () {
                            return doAddBusinessDashMetric(flag);
                        }
                    }
                });
                var micro = new Y.Template();
                var str = micro.render(dialogContentStr, {flag: flag});
                addMetricDialog.setContent(str);
                initModeSelect(flag);
                initCheck(flag);
                AutoCompleteApps(flag);
                addMetricDialog.show();
            });
        }

        function bindEditDeleteMetric() {
            // edit
            tbody.delegate('click', function () {
                var tr = this.ancestor('tr');
                var id = tr.getData('id');
                var flag = 'edit';

                var dataCache = metricsMap[id];
                gId = dataCache.id;
                gAuth = dataCache.auth;
                editMetricDialog = editMetricDialog ? editMetricDialog : new Y.mt.widget.CommonDialog({
                    id: 'edit_metric_dialog',
                    title: '修改指标',
                    width: 640,
                    btn: {
                        pass: function () {
                            return doAddBusinessDashMetric(flag);
                        }
                    }
                });
                var micro = new Y.Template();
                var str = micro.render(dialogContentStr, {data: dataCache, flag: flag});
                editMetricDialog.setContent(str);

                initModeSelect(flag);

                var node = Y.one("#edit_metric_form");

                var selectNode = node.one("#source_mode");
                if (dataCache.category == "appkey") {
                    AutoCompleteApps(flag);
                    node.one("#apps_select").set('value', dataCache.appkey);
                    getMetricAndAuto(dataCache.appkey, flag);
                    node.one("#metrics_select").set('value', dataCache.metric);
                    node.one("#metrics_select_id").set('value', dataCache.id);
                    selectNode.set("selectedIndex", 0);
                } else if (dataCache.category == "endpoint") {
                    node.one('#endpoint_title').set('value', dataCache.title);
                    node.one('#input_endpoint').set('value', dataCache.endpoint);
                    node.one('#input_metric').set('value', dataCache.metric);
                    node.one('#endpoint_sample_mode').set('value', dataCache.sampleMode);
                    selectNode.set("selectedIndex", 1);
                } else {
                    node.one('#serverNode_title').set('value', dataCache.title);
                    node.one('#input_serverNode').set('value', dataCache.serverNode);
                    node.one('#input_serverNode_metric').set('value', dataCache.metric);
                    node.one('#serverNode_sample_mode').set('value', dataCache.sampleMode);
                    selectNode.set("selectedIndex", 2);
                }
                selectNode.simulate("change");
                selectNode.set("disabled", true);

                initCheck(flag);

                editMetricDialog.show();
            }, '#edit_metric');

            // delete
            tbody.delegate('click', function () {
                var tr = this.ancestor('tr');
                var id = tr.getData('id');

                var delMetric = new Y.mt.widget.CommonDialog({
                    id: 'del_metric_dialog',
                    title: '删除指标',
                    content: '确认删除\"' + metricsMap[id].title + '\"指标?',
                    width: 300,
                    btn: {
                        pass: doDelMetric
                    }
                });
                delMetric.show();
                function doDelMetric() {
                    var url = '/monitor/business/dash/config?id=' + id;
                    Y.io(url, {
                        method: 'delete',
                        on: {
                            success: function () {
                                tr.transition({
                                    opacity: 0,
                                    duration: 0.6
                                }, function () {
                                    tr.remove();
                                });
                                delete tr;
                                Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                            }
                        }
                    });
                }

            }, '#delete_metric');
        }

        function autoCompleteOwts(owts, defaultValue) {
            owtAutoComplete = new Y.mt.widget.AutoCompleteList({
                id: "owtSelect_auto",
                node: Y.one("#owtSelect"),
                listParam: 'name',
                objList: owts,
                showMax: owts.length,
                matchMode: 'fuzzy',
                more: "",
                callback: function () {
                    getMetricsAndScreen(0);
                }
            });
            Y.one("#owtSelect_auto").one(".widget-autocomplete-complete-list").setStyle("height", "200px");
            Y.one("#owtSelect_auto").one(".widget-autocomplete-tip").setHTML("输入业务线名搜索或向下滚动选择");
            Y.one("#owtSelect_auto").one(".widget-autocomplete-menu-operator").remove();

            Y.one('#owtSelect').set("value", defaultValue.name);
            Y.one('#owtSelect_hidden').set("value", defaultValue.id);
        }

        function doAddBusinessDashMetric(flag) {
            var node = Y.one("#" + flag + "_metric_form");
            var mode = node.one("#source_mode").get('value');
            var owt = owtAutoComplete.getValue();

            var data = {
                category: mode,
                title: '',
                owt: owt.id,
                metricId: 0,
                endpoint: '',
                serverNode: '',
                metric: '',
                sampleMode: ''
            };

            //initCheck(flag);

            if (mode == "appkey") {
                var metricId = node.one('#metrics_select_id').get('value');

                data.metricId = metricId;
            }

            if (mode == "endpoint") {
                if (!endpointTitle.node.getData('status')
                    || !endpointInput.node.getData('status')
                    || !endpointMetricInput.node.getData('status')) {
                    endpointTitle.showMsg();
                    endpointInput.showMsg();
                    endpointMetricInput.showMsg();
                    return true;
                }
                data.title = node.one('#endpoint_title').get('value');
                data.endpoint = node.one('#input_endpoint').get('value');
                data.metric = node.one('#input_metric').get('value');
                data.sampleMode = node.one('#endpoint_sample_mode').get('value');
            }

            if (mode == "serverNode") {
                if (!serverNodeTitle.node.getData('status')
                    || !serverNodeInput.node.getData('status')
                    || !serverNodeMetricInput.node.getData('status')) {
                    serverNodeTitle.showMsg();
                    serverNodeInput.showMsg();
                    serverNodeMetricInput.showMsg();
                    return true;
                }

                data.title = node.one('#serverNode_title').get('value');
                data.serverNode = node.one('#input_serverNode').get('value');
                data.metric = node.one('#input_serverNode_metric').get('value');
                data.sampleMode = node.one('#serverNode_sample_mode').get('value');
            }
            var url = '/business/add';
            var method = 'post';
            if (flag == 'edit') {
                data.id = gId;
                data.auth = gAuth;
                url = '/data/business/metric';
                method = 'put';
            }
            Y.io(url, {
                method: method,
                data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        getMetricsAndScreen();
                    },
                    failure: function () {
                    }
                }
            });
        }

        function initModeSelect(flag) {
            var node = Y.one("#" + flag + "_metric_form");
            node.one("#source_mode").on('change', function () {
                var mode = this.get('value');
                node.all('.div_form').setStyle('display', 'none');
                node.one('#' + mode + '_form').setStyle('display', 'block');
            });
        }

        function initCheck(flag) {
            var node = Y.one("#" + flag + "_metric_form");
            endpointTitle = Y.msgp.utils.check.init(node.one('#endpoint_title'), {
                type: 'string',
                chineseOk: true,
                warnElement: node.one('#endpoint_title').next('')
            });
            endpointInput = Y.msgp.utils.check.init(node.one('#input_endpoint'), {
                type: 'string',
                chineseOk: true,
                warnElement: node.one('#input_endpoint').next('')
            });
            endpointMetricInput = Y.msgp.utils.check.init(node.one('#input_metric'), {
                type: 'string',
                warnElement: node.one('#input_metric').next('')
            });

            serverNodeTitle = Y.msgp.utils.check.init(node.one('#serverNode_title'), {
                type: 'string',
                chineseOk: true,
                warnElement: node.one('#serverNode_title').next('')
            });
            serverNodeInput = Y.msgp.utils.check.init(node.one('#input_serverNode'), {
                type: 'string',
                warnElement: node.one('#input_serverNode').next('')
            });
            serverNodeMetricInput = Y.msgp.utils.check.init(node.one('#input_serverNode_metric'), {
                type: 'string',
                warnElement: node.one('#input_serverNode_metric').next('')
            });
        }

        function AutoCompleteApps(flag) {
            var node = Y.one("#" + flag + "_metric_form");
            var apps = getApps();
            var appsArray = [];
            Y.Array.each(apps, function (item, index) {
                appsArray.push({'id': index, 'name': item})
            });

            new Y.mt.widget.AutoCompleteList({
                id: "apps_select_auto",
                node: node.one("#apps_select"),
                listParam: 'name',
                objList: appsArray,
                showMax: appsArray.length,
                matchMode: 'fuzzy',
                width: '310px',
                more: "",
                callback: function (data) {
                    var appkey = data.name;
                    getMetricAndAuto(appkey, flag);
                }
            });
            Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "300px");
            Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("overflow", "auto");
            Y.one("#apps_select_auto").one(".widget-autocomplete-tip").setHTML("输入服务名搜索");
            Y.one("#apps_select_auto").one(".widget-autocomplete-menu-operator").remove();
        }

        function getMetricAndAuto(appkey, flag) {
            var metrics = getMetric(appkey);

            var metricArray = [];
            Y.Array.each(metrics, function (item, index) {
                metricArray.push({'id': item.id, 'name': item.metric})
            });
            AutoCompleteMetrics(metricArray, flag);
        }

        function getApps() {
            var owt = owtAutoComplete.getValue();
            var owtId = owt.id;

            var result = [];

            var url = '/service/owt/apps?owt=' + owtId.split("_")[0];
            Y.io(url, {
                method: 'get',
                sync: true,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            result = ret.data;
                        } else if (ret.msg) {
                            result = [];
                        } else {
                            result = [];
                        }
                    },
                    failure: function () {
                        result = [];
                    }
                }
            });
            return result;
        }

        function AutoCompleteMetrics(obj, flag) {
            var node = Y.one("#" + flag + "_metric_form");
            new Y.mt.widget.AutoCompleteList({
                id: "metrics_select_auto",
                node: node.one("#metrics_select"),
                listParam: 'name',
                objList: obj,
                showMax: obj.length,
                matchMode: 'fuzzy',
                width: '310px',
                more: ""
            });
            Y.one("#metrics_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "300px");
            Y.one("#metrics_select_auto").one(".widget-autocomplete-complete-list").setStyle("overflow", "auto");
            Y.one("#metrics_select_auto").one(".widget-autocomplete-tip").setHTML("输入指标名搜索");
            Y.one("#metrics_select_auto").one(".widget-autocomplete-menu-operator").remove();
        }

        function getMetric(appkey) {
            var result = [];
            var url = '/data/app/metrics?appkey=' + appkey;
            Y.io(url, {
                method: 'get',
                sync: true,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        result = ret.data
                    },
                    failure: function () {
                        result = ret.data
                    }
                }
            });
            return result;
        }

        function getMetricsAndScreen() {
            var owt = owtAutoComplete.getValue();

            //var end_time = Y.mt.date.formatDateByString(new Date(se.end), 'yyyy-MM-dd hh:mm:00');
            //var start_time = Y.mt.date.formatDateByString(new Date(se.start), 'yyyy-MM-dd hh:mm:00');
            //window.history.pushState({}, 0, 'http://' + window.location.host + "/business?start=" + start_time + "&end=" + end_time + "&owt=" + owt.id);

            var url2 = '/business/metrics?owt=' + owt.id;
            Y.io(url2, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var data = ret.data;
                        if (ret.isSuccess) {
                            if (data && data.length > 0) {
                                updateMetricsMap(data);
                                fillTriggerItems(data);
                            } else {
                                emptyOrError(false);
                            }
                        } else {
                            emptyOrError(true);
                        }
                    },
                    failure: function () {
                    }
                }
            });
        }

        function emptyOrError(isError) {
            var colspan = 6;
            var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
            tbody.setHTML(html);
            showContent();
        }

        function fillTriggerItems(arr) {
            var micro = new Y.Template();
            var html = micro.render(triggerTemplate, {data: arr});
            tbody.setHTML(html);
            showContent();
        }

        function updateMetricsMap(data) {
            Y.Array.each(data, function (item) {
                var id = item.id;
                metricsMap[id] = item;
            });
        }

        function showContent() {
            twrap.one('.content-overlay').hide();
            twrap.one('.content-body').show();
        }

        var dialogContentStr = [
            '<div id="<%= this.flag %>_metric_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label">数据来源：</label>',
            '<div class="controls">',
            '<select id="source_mode">',
            '<option value="appkey" selected="selected" >appkey</option>',
            '<option value="endpoint" >endpoint</option>',
            '<option value="serverNode" >serverNode(服务树单元)</option>',
            '</select>',
            '</div>',
            '</div>',
            '<div id="appkey_form" class="div_form">',
            '<div class="control-group"><label class="control-label">appkey：</label>',
            '<div class="controls">',
            '<input style="width: 300px" class="mb5 span6" type="text" id="apps_select" value="" autocomplete="off"/>',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">指标名：</label>',
            '<div class="controls">',
            '<input style="width: 300px" class="mb5 span6" type="text" id="metrics_select" value="" autocomplete="off"/>',
            '<input type="hidden" class="f-text" id="metrics_select_id" value="">',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '</div>',
            '<div id="endpoint_form" class="div_form" style="display: none">',
            '<div class="control-group"><label class="control-label">标题：</label>',
            '<div class="controls">',
            '<input id="endpoint_title" type="text" value="" placeholder="标题，必填" />',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">endpoint：</label>',
            '<div class="controls">',
            '<input style="width: 300px" id="input_endpoint" class="mb5 span6" type="text" placeholder="如 dx-inf-msgp04,dx-inf-msgp05" value=""/>',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">指标名：</label>',
            '<div class="controls">',
            '<input style="width: 300px" id="input_metric" class="mb5 span6" type="text" value=""/>',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">采样方法：</label>',
            '<div class="controls">',
            '<select id="endpoint_sample_mode">',
            '<option value="sum" selected="selected">sum</option>',
            '<option value="avg" >avg</option>',
            '</select>',
            '</div>',
            '</div>',
            '</div>',
            '<div id="serverNode_form" class="div_form" style="display: none">',
            '<div class="control-group"><label class="control-label">标题：</label>',
            '<div class="controls">',
            '<input id="serverNode_title" type="text" value="" placeholder="标题，必填" />',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">服务树单元：</label>',
            '<div class="controls">',
            '<input style="width: 300px" id="input_serverNode" placeholder="如 corp=meituan&owt=inf&pdl=octo&srv=msgp" class="mb5 span6" type="text" value=""/>',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">指标名：</label>',
            '<div class="controls">',
            '<input style="width: 300px" id="input_serverNode_metric" class="mb5 span6" type="text" value=""/>',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">采样方法：</label>',
            '<div class="controls">',
            '<select id="serverNode_sample_mode">',
            '<option value="sum" selected="selected">sum</option>',
            '<option value="avg" >avg</option>',
            '</select>',
            '</div>',
            '</div>',
            '</div>',
            '</div>'
        ].join('');

        var triggerTemplate = [
            '<% Y.Array.each(this.data, function(item, index){ %>',
            '<tr data-id="<%= item.id %>" >',
            '<td><%= item.category %></td>',
            '<td style="word-break: break-all; overflow: hidden;"><%= item.title %></td>',
            '<td style="word-break: break-all; overflow: hidden;"><%= item.appkey %><%= item.endpoint %><%= item.serverNode %></td>',
            '<td style="word-break: break-all; overflow: hidden;"><%= item.metric %></td>',
            '<td style="word-break: break-all; overflow: hidden;"><%= item.sampleMode %></td>',
            '<td ><% if(item.category != "appkey") {%> <a id="edit_metric" href="javascript:;">编辑</a>',
            '<a id="delete_metric" class="ml20" href="javascript:;">删除</a>',
            '<%} else {%> <a id="delete_metric" href="javascript:;">删除</a> <%}%>',
            '</td>',
            '</tr>',
            '<% }); %>'
        ].join('');
    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'w-base',
            'w-date',
            'mt-date',
            'template',
            'transition',
            'mt-base',
            'msgp-utils/msgpHeaderTip',
            'msgp-utils/check',
            'w-autocomplete',
            'node-event-simulate'
        ]
    }
);
M.use('msgp-monitor/businessDashConfig', function (Y) {
    Y.msgp.monitor.businessDashConfig(owtList, owt);
});
