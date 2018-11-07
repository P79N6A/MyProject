M.add('msgp-data/data_operation', function (Y) {
        Y.namespace('msgp.data').data_operation = data_operation;

        var appkey,
            metricsMap = [],
            addMetricDialog,
            editMetricDom,
            editMetricDialog,
            appScreen,
            titleInput,
            metricInput,
            currentId,
            echartsObjectMap = [];

        var startInput, endInput;
        var screenCharts;
        var operationWrapper;

        function data_operation(_appkey, _isFirstIn) {
            appkey = _appkey;
            document.title = "业务指标";
            if (!_isFirstIn) {
                return;
            }

            initParams();
            initTimeLine();
            bindAddMetric();
            initDatePicker();
            bindSearch();
            getMetricsAndScreen();
        }

        function initParams() {
            operationWrapper = Y.one('#div_data_operation');
            appScreen = operationWrapper.one('#app_screen');
            startInput = operationWrapper.one('#start_time');
            endInput = operationWrapper.one('#end_time');
            screenCharts = appScreen.one('#screen_charts');
        }

        function bindAddMetric() {
            operationWrapper.one('#add_metric').on('click', function () {
                addMetricDialog = addMetricDialog ? addMetricDialog : new Y.mt.widget.CommonDialog({
                    id: 'add_metric_dialog',
                    title: '增加指标',
                    width: 640,
                    btn: {
                        pass: doAddScreen
                    }
                });
                var micro = new Y.Template();
                var data = {
                    title: '',
                    metric: '',
                    sampleMode: 'sum'
                };
                var str = micro.render(dialogContentStr, {data: data, isEdit: 'add'});
                addMetricDialog.setContent(str);
                addMetricDialog.show();
                initAddMetricDialog();
            });

            function initAddMetricDialog() {
                var addBody = addMetricDialog.getBody();
                titleInput = Y.msgp.utils.check.init(addBody.one('#s_title'), {
                    type: 'string',
                    chineseOk: true,
                    warnElement: addBody.one('#s_title').next('')
                });
                metricInput = Y.msgp.utils.check.init(addBody.one('#s_metric'), {
                    type: 'string',
                    warnElement: addBody.one('#s_metric').next('')
                });
            }

            function doAddScreen() {
                if (!titleInput.node.getData('status') || !metricInput.node.getData('status')) {
                    titleInput.showMsg();
                    metricInput.showMsg();
                    return true;
                }
                var operationBody = addMetricDialog.getBody();
                var operationDom = operationBody.one('#add_metric_form');
                var data = {
                    appkey: appkey,
                    title: operationDom.one('#s_title').get('value'),
                    metric: operationDom.one('#s_metric').get('value'),
                    sampleMode: operationDom.one('#s_sample_mode').get('value')
                };

                var url = '/data/business/metric';
                Y.io(url, {
                    method: 'post',
                    data: data,
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                var se = getStartEnd();
                                if (!se) return;
                                var metricId = ret.data;
                                data.id = metricId;
                                updateMetricsMapAndScreenCharts(data);
                                getScreen(se, metricId, 0);
                            } else {
                                Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                            }
                        },
                        failure: function () {
                        }
                    }
                });
            }
        }

        function initDatePicker() {
            var now = new Date();
            //1分钟之前
            var end = new Date(now.getTime() - 60 * 1000);
            //4小时之前
            var start = new Date(end.getTime() - 4 * 60 * 60 * 1000);

            new Y.mt.widget.Datepicker({
                node: startInput,
                showSetTime: true
            });
            startInput.set('value', Y.mt.date.formatDateByString(start, 'yyyy-MM-dd hh:mm:00'));

            new Y.mt.widget.Datepicker({
                node: endInput,
                showSetTime: true
            });
            endInput.set('value', Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:00'));
        }

        function bindSearch() {
            operationWrapper.one('#query_btn').on('click', function () {
                screenCharts.empty();
                getMetricsAndScreen();
            });
        }

        function getMetricsAndScreen() {
            // 首先获取所有的screen配置
            var url2 = '/data/app/metrics?appkey=' + appkey;

            Y.io(url2, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        doGetScreen(ret.data);
                    },
                    failure: function () {
                    }
                }
            });
            var se = getStartEnd()
            var opt = {
                appkey: appkey,
                start: se ? se.start : "",
                end: se ? se.end : ""
            };
            Y.msgp.utils.urlAddParameters(opt);

            function doGetScreen(data) {
                var se = getStartEnd();
                if (!se) return;
                Y.Array.each(data, function (item, index) {
                    updateMetricsMapAndScreenCharts(item);
                    getScreen(se, item.id, item.auth);
                });
            }
        }

        function updateMetricsMap(item) {
            var id = item.id;
            metricsMap[id] = item;
        }

        function updateMetricsMapAndScreenCharts(item) {
            updateMetricsMap(item);

            var micro = new Y.Template();
            var html = micro.render(screenTml, {index: item.id});
            var screenNode = Y.Node.create(html);
            screenCharts.append(screenNode);

            var node = appScreen.one("#screen" + item.id).getDOMNode();
            var ec = echarts.init(node);
            ec.showLoading({
                text: "loading"
            });
            echartsObjectMap["#screen" + item.id] = ec;

            bindEditDeleteMetric(screenNode);
        }

        function bindEditDeleteMetric(node) {
            // edit
            node.delegate('click', function () {
                var id = this.getData('id');
                currentId = id;
                var dataCache = metricsMap[id];

                editMetricDialog = editMetricDialog ? editMetricDialog : new Y.mt.widget.CommonDialog({
                    id: 'edit_metric_dialog',
                    title: '修改指标',
                    width: 640,
                    btn: {
                        pass: doEditScreen
                    }
                });
                var micro = new Y.Template();
                var str = micro.render(dialogContentStr, {data: dataCache, isEdit: 'edit'});
                editMetricDialog.setContent(str);
                editMetricDialog.show();
                editMetricDom = Y.one("#edit_metric_dialog")
                initEditMetricDialog();
                function initEditMetricDialog() {

                    titleInput = Y.msgp.utils.check.init(editMetricDom.one('#s_title'), {
                        type: 'string',
                        chineseOk: true,
                        warnElement: editMetricDom.one('#s_title').next('')
                    });
                    metricInput = Y.msgp.utils.check.init(editMetricDom.one('#s_metric'), {
                        type: 'string',
                        warnElement: editMetricDom.one('#s_metric').next('')
                    });
                }

                function doEditScreen() {
                    if (!titleInput.node.getData('status') || !metricInput.node.getData('status')) {
                        titleInput.showMsg();
                        metricInput.showMsg();
                        return true;
                    }

                    var dialogDom = editMetricDom.one('#edit_metric_form');
                    var putData = {
                        id: currentId,
                        appkey: appkey,
                        title: dialogDom.one('#s_title').get('value'),
                        metric: dialogDom.one('#s_metric').get('value'),
                        sampleMode: dialogDom.one('#s_sample_mode').get('value'),
                        auth: +dialogDom.one('#s_auth').get('value')
                    };

                    var url = '/data/business/metric';
                    Y.io(url, {
                        method: 'put',
                        data: putData,
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (ret.isSuccess) {
                                    var se = getStartEnd();
                                    if (!se) return;

                                    var id = "#screen" + currentId;
                                    var ec = echartsObjectMap[id];
                                    ec.showLoading({
                                        text: "loading"
                                    });

                                    updateMetricsMap(putData);
                                    getScreen(se, putData.id, putData.auth);
                                } else {
                                    Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                                }
                            },
                            failure: function () {
                            }
                        }
                    });
                }
            }, '#edit_metric');

            // delete
            node.delegate('click', function () {
                var id = this.getData('id');

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
                    var url = '/data/business/metric?id=' + id;
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

        function getScreen(se, id, auth) {
            // 异步获取各个screen的数据
            var data = {
                appkey: appkey,
                id: id,
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
                        fillScreen(ret.data, auth);
                    },
                    failure: function () {
                    }
                }
            });
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
                        name: "Value/分钟",
                        type: 'value',
                        axisLine: {
                            show: false
                        },
                        axisLabel: {
                            formatter: numFormatter
                        }
                    }
                ],
                series: series,
                legend: legend
            };
            if (auth == 1) {
                option.yAxis[0].axisLabel.show = false;
                option.tooltip.formatter = '{b}<br/>{a}';
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

        var dialogContentStr = [
            '<div id="<%= this.isEdit %>_metric_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label">标题：</label>',
            '<div class="controls">',
            '<input id="s_title" type="text" value="<%= this.data.title %>" placeholder="标题，必填" />',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">falcon指标名：</label>',
            '<div class="controls">',
            '<input id="s_metric" type="text" value="<%= this.data.metric %>" placeholder="metric，必填" />',
            '<span class="tips"></span>',
            '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label">采样方法：</label>',
            '<div class="controls">',
            '<select id="s_sample_mode">',
            '<option value="sum" <% if(this.data.sampleMode == "sum"){%> selected="selected" <%}%> >sum</option>',
            '<option value="avg" <% if(this.data.sampleMode == "avg"){%> selected="selected" <%}%> >avg</option>',
            '</select>',
            '</div>',
            '</div>',
            '<input id="s_auth" type="hidden" value="<%= this.data.auth %>"/>',
            '</div>'
        ].join('');

        var screenTml = [
            '<div class="charts-wrapper-out">',
            '<div class="btn-group">',
            '<a value="1" type="button" data-id="<%= this.index %>" id="edit_metric" class="btn btn-primary" href="javascript:void(0)">编辑</a>',
            '<a value="2" type="button" data-id="<%= this.index %>" id="delete_metric" class="btn btn-primary" href="javascript:void(0)">删除</a>',
            '</div>',
            '<div id="screen<%= this.index %>" class="charts-wrapper" style="width: 420px;border: 0"></div>',
            '</div>'
        ].join('');

        function initTimeLine() {
            operationWrapper.all("#timeline a").on('click', function (e) {
                var time = e.target.getData('value');
                var now = new Date();
                var end = now.getTime() - 60 * 1000;
                var start = (end - time * 3600 * 1000);

                var end_time = Y.mt.date.formatDateByString(new Date(end), 'yyyy-MM-dd hh:mm:00');
                var start_time = Y.mt.date.formatDateByString(new Date(start), 'yyyy-MM-dd hh:mm:00');
                startInput.set('value', start_time);
                endInput.set('value', end_time);
                operationWrapper.one('#query_btn')._node.click();
            })
        }
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
            'msgp-utils/check',
            'msgp-utils/common',
            'msgp-utils/msgpHeaderTip',
            'mt-base'
        ]
    }
);