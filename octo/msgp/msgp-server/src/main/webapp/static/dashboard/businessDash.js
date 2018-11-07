M.add('msgp-dashboard/businessDash', function (Y) {
        Y.namespace('msgp.dashboard').businessDash = businessDash;

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

        var owtAutoComplete, addMetricDialog;

        var startInput = Y.one('#start_time'),
            endInput = Y.one('#end_time');
        var sdate, edate, echartsObjectMap = [];

        var appScreen = Y.one('#app_screen');
        var screenCharts = appScreen.one('#screen_charts');

        function businessDash(owtList, owt) {
            document.title = '服务治理平台-业务大盘';
            initTimeLine();
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
            initDatePicker();
            bindSearch();
            getMetricsAndScreen(0);
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
                    screenCharts.empty();
                    getMetricsAndScreen(0);
                }
            });
            Y.one("#owtSelect_auto").one(".widget-autocomplete-complete-list").setStyle("height", "200px");
            Y.one("#owtSelect_auto").one(".widget-autocomplete-tip").setHTML("输入业务线名搜索或向下滚动选择");
            Y.one("#owtSelect_auto").one(".widget-autocomplete-menu-operator").remove();

            Y.one('#owtSelect').set("value", defaultValue.name);
            Y.one('#owtSelect_hidden').set("value", defaultValue.id);
        }

        function bindAdd() {
            Y.one('#add_metric').on('click', function () {
                var owt = owtAutoComplete.getValue();
                window.open("/monitor/business/dash/config?owt=" + owt.id);
                //var apps = getApps();
                //var appArray = [];
                //Y.Array.each(apps, function (item, index) {
                //    appArray.push({'id': index, 'name': item.appkey})
                //});
                //
                //addMetricDialog = addMetricDialog ? addMetricDialog : new Y.mt.widget.CommonDialog({
                //    id: 'add_metric_dialog',
                //    title: '增加指标',
                //    width: 640,
                //    btn: {
                //        pass: doAddBusinessDashMetric
                //    }
                //});
                //var micro = new Y.Template();
                //var str = micro.render(dialogContentStr);
                //addMetricDialog.setContent(str);
                //initModeSelect();
                //initCheck();
                //AutoCompleteApps(appArray);
                //addMetricDialog.show();
            });

            function getApps() {
                var owt = owtAutoComplete.getValue();
                var owtId = owt.id;

                var result = [];

                var url = 'service/owt/apps?owt=' + owtId.split("_")[0];
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

            function initModeSelect() {
                Y.one("#source_mode").on('change', function () {
                    var mode = this.get('value');
                    Y.one('#add_metric_form').all('.div_form').setStyle('display', 'none');
                    Y.one('#' + mode + '_form').setStyle('display', 'block');
                });
            }

            function initCheck() {
                endpointTitle = Y.msgp.utils.check.init(Y.one('#endpoint_title'), {
                    type: 'string',
                    chineseOk: true,
                    warnElement: Y.one('#endpoint_title').next('')
                });
                endpointInput = Y.msgp.utils.check.init(Y.one('#input_endpoint'), {
                    type: 'string',
                    chineseOk: true,
                    warnElement: Y.one('#input_endpoint').next('')
                });
                endpointMetricInput = Y.msgp.utils.check.init(Y.one('#input_metric'), {
                    type: 'string',
                    warnElement: Y.one('#input_metric').next('')
                });

                serverNodeTitle = Y.msgp.utils.check.init(Y.one('#serverNode_title'), {
                    type: 'string',
                    chineseOk: true,
                    warnElement: Y.one('#serverNode_title').next('')
                });
                serverNodeInput = Y.msgp.utils.check.init(Y.one('#input_serverNode'), {
                    type: 'string',
                    warnElement: Y.one('#input_serverNode').next('')
                });
                serverNodeMetricInput = Y.msgp.utils.check.init(Y.one('#input_serverNode_metric'), {
                    type: 'string',
                    warnElement: Y.one('#input_serverNode_metric').next('')
                });
            }

            function AutoCompleteApps(obj) {
                var autoCompleteWidget = new Y.mt.widget.AutoCompleteList({
                    id: "apps_select_auto",
                    node: Y.one("#apps_select"),
                    listParam: 'name',
                    objList: obj,
                    showMax: obj.length,
                    matchMode: 'fuzzy',
                    width: '310px',
                    more: "",
                    callback: function (data) {
                        var appkey = data.name;
                        var metrics = getMetric(appkey);

                        var metricArray = [];
                        Y.Array.each(metrics, function (item, index) {
                            metricArray.push({'id': item.id, 'name': item.metric})
                        });
                        AutoCompleteMetrics(metricArray);
                    }
                });
                Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "300px");
                Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("overflow", "auto");
                Y.one("#apps_select_auto").one(".widget-autocomplete-tip").setHTML("输入服务名搜索");
                Y.one("#apps_select_auto").one(".widget-autocomplete-menu-operator").remove();
            }

            function AutoCompleteMetrics(obj) {
                new Y.mt.widget.AutoCompleteList({
                    id: "metrics_select_auto",
                    node: Y.one("#metrics_select"),
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

            function doAddBusinessDashMetric() {
                var mode = Y.one("#source_mode").get('value');
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

                if (mode == "appkey") {
                    var metricId = Y.one('#metrics_select_id').get('value');

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
                    data.title = Y.one('#endpoint_title').get('value');
                    data.endpoint = Y.one('#input_endpoint').get('value');
                    data.metric = Y.one('#input_metric').get('value');
                    data.sampleMode = Y.one('#endpoint_sample_mode').get('value');
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

                    data.title = Y.one('#serverNode_title').get('value');
                    data.serverNode = Y.one('#input_serverNode').get('value');
                    data.metric = Y.one('#input_serverNode_metric').get('value');
                    data.sampleMode = Y.one('#serverNode_sample_mode').get('value');
                }

                var url = '/business/add';
                Y.io(url, {
                    method: 'post',
                    data: data,
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.data != null) {
                                screenCharts.empty();
                                getMetricsAndScreen(0);
                            }
                        },
                        failure: function () {
                        }
                    }
                });
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
        }

        function initDatePicker() {
            sdate = new Y.mt.widget.Datepicker({
                node: startInput,
                showSetTime: true
            });
            edate = new Y.mt.widget.Datepicker({
                node: endInput,
                showSetTime: true
            });
        }

        function bindSearch() {
            Y.one('#query_btn').on('click', function () {
                screenCharts.empty();
                getMetricsAndScreen(0);
            });
        }

        function getMetricsAndScreen(metricId) {
            var owt = owtAutoComplete.getValue();

            var se = getStartEnd();
            if (!se) return;

            var end_time = Y.mt.date.formatDateByString(new Date(se.end), 'yyyy-MM-dd hh:mm:00');
            var start_time = Y.mt.date.formatDateByString(new Date(se.start), 'yyyy-MM-dd hh:mm:00');
            window.history.pushState({}, 0, 'https://' + window.location.host + "/business?start=" + start_time + "&end=" + end_time + "&owt=" + owt.id);

            var url2 = '/business/metrics?owt=' + owt.id + '&metricId=' + metricId;
            Y.io(url2, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.data.length) {
                            doGetScreen(ret.data);
                        }
                    },
                    failure: function () {
                    }
                }
            });

            function doGetScreen(data) {
                var se = getStartEnd();
                if (!se) return;
                Y.Array.each(data, function (item, index) {
                    updateScreenCharts(item);
                    getScreen(se, item);
                });
            }
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

        function getScreen(se, item) {
            // 异步获取各个screen的数据
            var data = {
                appkey: item.appkey,
                id: item.id,
                start: Math.round(new Date(se.start) / 1000),
                end: Math.round(new Date(se.end) / 1000)
            };
            var url = '/data/business/metric';
            Y.io(url, {
                method: 'get',
                data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        fillScreen(ret.data, item.auth);
                    },
                    failure: function () {
                    }
                }
            });
        }

        function updateScreenCharts(item) {
            var micro = new Y.Template();
            var html = micro.render(screenTml, {data: item});
            var screenNode = Y.Node.create(html);
            screenCharts.append(screenNode);

            var node = appScreen.one("#screen" + item.id).getDOMNode();
            var ec = echarts.init(node);
            ec.showLoading({
                text: "loading"
            });
            echartsObjectMap["#screen" + item.id] = ec;

            bindDeleteMetric(screenNode);
        }

        function fillScreen(data, auth) {
            Y.Array.each(data, function (item, index) {
                var id = "#screen" + item.id;
                var ec = echartsObjectMap[id];
                var opt = wrapOption(item, auth);
                ec.hideLoading();
                ec.setOption(opt);
            })
        }

        function wrapOption(itemP, auth) {
            var series = [];
            var legendData = [];
            Y.Array.each(itemP.series, function (item, index) {
                series.push({
                    name: item.name,
                    type: 'line',
                    symbol: 'none',
                    data: item.data,
                    smooth: true,
                    sampling: 'average'
                });
                legendData.push({
                    icon: 'line',
                    name: item.name
                })
            });
            var legend = {
                bottom: 0,
                data: legendData
            };

            var option = {
                title: {
                    show: true,
                    text: itemP.title,
                    textStyle: {
                        color: '#333',
                        fontStyle: 'normal',
                        fontWeight: 'normal',
                        fontFamily: 'sans-serif',
                        fontSize: 14
                    },
                    left: 'center'
                },
                animation: false,
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        lineStyle: {
                            type: "dashed"
                        }
                    }
                },
                toolbox: {
                    show: false
                },
                xAxis: [
                    {
                        type: 'category',
                        data: itemP.xAxis,
                        splitLine: {
                            show: false
                        }
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        scale: true,
                        axisLine: {
                            show: false
                        },
                        axisLabel: {
                            formatter: numFormatter
                        }
                    }
                ],
                series: series,
                legend: legend,
                color: ['#c23531', '#4fbba9']
            };
            if (auth == 1) {
                option.yAxis[0].axisLabel.show = false;
                option.tooltip.formatter = '{b}<br/>{a0}<br/>{a1}';
            }
            return option;
        }

        function numFormatter(num) {
            if (num < 1000) {
                return num;
            } else if (num >= 1000 && num < 1000000) {
                return Math.round(num / 1000) + 'k';
            } else if (num >= 1000000 && num < 1000000000) {
                return Math.round(num / 1000000) + 'M';
            } else if (num >= 1000000000) {
                return Math.round(num / 1000000000) + 'G';
            }
        }

        function bindDeleteMetric(node) {
            // delete
            node.delegate('click', function () {
                var id = this.getData('id');
                var title = this.getData('title');

                var delMetric = new Y.mt.widget.CommonDialog({
                    id: 'del_metric_dialog',
                    title: '删除指标',
                    content: '确认从业务大盘中移除 \"' + title + '\" 指标?',
                    width: 300,
                    btn: {
                        pass: doDelMetric
                    }
                });
                delMetric.show();
                function doDelMetric() {
                    var owt = owtAutoComplete.getValue();

                    var url = '/business/metrics?owt=' + owt.id + '&id=' + id;
                    Y.io(url, {
                        method: 'delete',
                        on: {
                            success: function () {
                                Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                                var node = appScreen.one("#screen" + id).getDOMNode();
                                screenCharts.removeChild(node.parentNode);
                            },
                            failure: function () {
                            }
                        }
                    });
                }

            }, '#delete_metric');
        }

        function initTimeLine() {
            var allTime = Y.all("#timeline a");
            //allTime.each(function (item, index) {
            //    if(item.getData('value') == timeRange) {
            //        item.addClass("current-css");
            //    }
            //
            //});

            allTime.on('click', function (e) {
                var time = e.target.getData('value');
                var now = new Date();
                var end = now.getTime() - 60 * 1000;
                var start = (end - time * 3600 * 1000);
                var end_time = Y.mt.date.formatDateByString(new Date(end), 'yyyy-MM-dd hh:mm:00');
                var start_time = Y.mt.date.formatDateByString(new Date(start), 'yyyy-MM-dd hh:mm:00');
                var owt = owtAutoComplete.getValue();
                window.location.href = "/business?start=" + start_time + "&end=" + end_time + "&owt=" + owt.id;
            })
        }

        var dialogContentStr = [
            '<div id="add_metric_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label">数据来源：</label>',
            '<div class="controls">',
            '<select id="source_mode">',
            '<option value="appkey" selected="selected" >appkey</option>',
            '<option value="endpoint" >endpoint</option>',
            '<option value="serverNode" >服务树单元</option>',
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

        var screenTml = [
            '<div class="charts-wrapper-out">',
            '<div class="btn-group">',
            '</div>',
            '<div id="screen<%= this.data.id %>" class="charts-wrapper" style="width: 400px;height: 340px;border: 0"></div>',
            '</div>'
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
            'w-autocomplete'
        ]
    }
);
M.use('msgp-dashboard/businessDash', function (Y) {
    Y.msgp.dashboard.businessDash(businessDash.owtList, businessDash.owt);
});
