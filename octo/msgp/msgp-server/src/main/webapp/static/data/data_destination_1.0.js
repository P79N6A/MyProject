M.add('msgp-data/data_destination_1.0', function (Y) {
    Y.namespace('msgp.data').data_destination = data_destination;
    var appkey;
    var startInput, endInput;
    var destinationWrapper;
    var listWrapper;
    var chartsData;
    var sdate, edate;
    var previous = null;
    var hostDetailsDialog;
    var compareWrap;
    var chartsTemplate = [
        /* jshint indent: false */
        '<div class="charts-wrapper-out">',
        '<div class="charts-wrapper" style="border: 0"></div>',
        '</div>'
    ].join('');
    var graphsTemplate = [
        /* jshint indent: false */
        '<div class="charts-wrapper-out">',
        '<p style="word-break: break-all; overflow: hidden;width: 500px"></p>',
        '<div class="charts-wrapper" style="border: 0"></div>',
        '</div>'
    ].join('');
    var dialogChartsTemplate = [
        /* jshint indent: false */
        '<div class="dialog-charts-out"><p>50%耗时</p><div class="dialog-charts"></div></div>',
        '<div class="dialog-charts-out"><p>90%耗时</p><div class="dialog-charts"></div></div>',
        '<div class="dialog-charts-out"><p>99%耗时</p><div class="dialog-charts"></div></div>',
        '<div class="dialog-charts-out"><p>QPS</p><div class="dialog-charts"></div></div>'
    ].join('');
    var compareDialog;
    var lastXAxis;
    var lastLegend = [];
    var compareData = [];

    var searching = false;

    var keyResverd =['spanname', 'remoteApp'];
    var _remoteApps,_spannames;

    function data_destination(_appkey, _isFirstIn) {
        appkey = _appkey;
        document.title = "去向分析";
        if (!_isFirstIn) {
            return;
        }
        initParams();
        initAppkeyTags();
        initDatePicker();
        initEvent();
    }

    function initParams() {
        destinationWrapper = Y.one('#div_data_destination');
        startInput = destinationWrapper.one('#start_time');
        endInput = destinationWrapper.one('#end_time');
        listWrapper = destinationWrapper.one('.destination_kpi_list');
        compareWrap = destinationWrapper.one('#compare_wrap');
        Y.msgp.service.setEnvText('destination_env_select');

        destinationWrapper.one('#remoteApp').set('value', '*');
        destinationWrapper.one('#spanname').set('value', 'all');
    }

    /**
     * 获取该appkey的remoteAppkey和spanname
     */
    function initAppkeyTags() {
        var url = '/data/appkey_spanname';
        var env = destinationWrapper.one("#destination_env_select a.btn-primary").getAttribute('value');
        var requestPara = {
            appkey: appkey,
            env: env,
            source: 'client'

        };
        Y.io(url, {
            method: 'get',
            data: requestPara,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        if(ret.data.remoteAppKeys.length == 0 || ret.data.spannames == 0){
                            Y.msgp.utils.msgpHeaderTip('info', '该环境下无有效数据', 3);
                        }else{
                            _remoteApps = ret.data.remoteAppKeys;
                            _spannames = ret.data.spannames;
                            $("#remoteApp").autocomplete({
                                source: _remoteApps,
                                minLength: 0
                            });
                            $("#spanname").autocomplete({
                                source: _spannames,
                                minLength: 0
                            });
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败, 请重刷页面', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败, 请重刷页面', 3);
                }
            }
        });
    }


    function initEvent() {
        destinationWrapper.one('#query_btn').delegate('click', function () {
            if (searching) return;
            destinationWrapper.all("#query_btn .btn").removeClass("btn-primary");
            this.addClass("btn-primary");
            this.hasClass("chart") ?  getShowData(true, listWrapper,{}) : getGraphsData();
        }, ".btn");

        destinationWrapper.one("#destination_env_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            destinationWrapper.all('#destination_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            initAppkeyTags();
        }, "a");


        listWrapper.delegate('click', function () {
            var now = new Date();
            if (!previous || (now - previous > 300)) {
                var _id = $(this).attr('id');
                if($("." + _id).length) {
                    $("." + _id).remove();
                } else {
                    $("#showGraph").remove();
                    var str = '<tr id="showGraph" class=' + _id +'><td colspan="8" style="text-align: center;"><div id="charts_outer" class="clearfix" style="display: inline-block;"></div></td></tr>';
                    $(".widget-table-choose").after(str);
                    var wrapper = destinationWrapper.one("#charts_outer");
                    var spanname = $(this).attr('data-spanname');
                    var remoteApp = $(this).attr('data-remoteapp');
                    var params = {spanname:spanname,remoteApp:remoteApp};
                    getShowData(false, wrapper,params);
                }
                previous = now;
            }
        },'.day_graph_click');

        listWrapper.delegate('click', function () {
            var tableRow = this;
            setTimeout(function () {
                var spanname = $(tableRow).attr('data-spanname');
                var remoteApp = $(tableRow).attr('data-remoteapp');
                var count = $(tableRow).attr('data-count');
                getHostDetailData(remoteApp, spanname, count);
            }, 0)
        }, '.host_detail_click');


        destinationWrapper.one('#compare_button').on('click', function () {
            lastLegend = [];
            compareData = [];
            compareDialog = compareDialog ? compareDialog : new Y.mt.widget.CommonDialog({
                id: 'compare_dialog',
                width: 615,
                title: '数据对比'
            });
            compareDialog.setContent(dialogChartsTemplate);
            compareDialog.show();
        });

        //console.log(destinationWrapper.one('#query_btn'))
        destinationWrapper.one("#query_btn .btn-primary").hasClass("chart") ?  getShowData(true, listWrapper,{}) : getGraphsData();
    }


    function initList(data) {
        var micro = new Y.Template();
        var str = micro.render(template(), {data: data});
        destinationWrapper.one(".destination_kpi_list").setHTML(str);
        Y.mt.widget.init();
    }

    function checkData(data) {
        if(data['remoteApp'] === '*' && data['spanname'] === '*') {
            Y.msgp.utils.msgpHeaderTip('error', '不能同时选择所有服务和所有接口', 3);
            return true;
        } else {
            return false;
        }
    }

    //v保留位小数
    function round(v, e) {
        var t = 1;
        for (; e > 0; t *= 10, e--);
        for (; e < 0; t /= 10, e++);
        return Math.round(v * t) / t;
    }

    function initHostDetailsDialog(title) {
        var title = '调用服务' + appkey + '的详情';
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'host_detail_dialog',
            title: title,
            width: 1200,
            height: 400,
            overflow: scroll,
            drag: function () {
            },
            refresh: 1
        });
        return dialog;
    }

    function showHostDetailsDialog(content) {
        hostDetailsDialog = hostDetailsDialog ? hostDetailsDialog : initHostDetailsDialog();
        hostDetailsDialog.setContent(content);
        hostDetailsDialog.show();
    }

    function showHostDetail(data, appkey, totalCount) {
        if (data.length == 0) {
            var message = '<div style="text-align: center;font-size:30px; padding-bottom: 20px; padding-top: 20px;">没有数据</div>';
            showHostDetailsDialog(message);
        } else {
            var detailData = [];
            Y.Array.each(data, function (item, index) {
                var percent = round((item.count / totalCount) * 100, 4);
                var realSuccessCount = item.successCount + item.http2XXCount + item.http3XXCount;
                var successCountPercent = (realSuccessCount * 100 /parseFloat(item.count)).toFixed(4);

                var failCount = item.count - realSuccessCount;
                var failCountPercent = (100 - successCountPercent).toFixed(4);

                var failureDetails = item.failureDetails;

                detailData.push({
                    localhost: item.localHostDesc.name,
                    count: item.count,
                    countPercent: percent + '%',
                    successCount: realSuccessCount,
                    successCountPercent: successCountPercent + '%',
                    failCount: failCount,
                    failCountPercent: failCountPercent + '%',
                    exceptionCount: failureDetails.exceptionCount,
                    timeoutCount: failureDetails.timeoutCount,
                    dropCount: failureDetails.dropCount,
                    http4xxCount: failureDetails.http4XXCount,
                    http5xxCount: failureDetails.http4XXCount
                })
            });

            var micro = new Y.Template();
            var message = micro.render(hostDetailTemplate, {data: detailData, totalCount: totalCount});
            showHostDetailsDialog(message);
        }
    }


    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node.setHTML(html);
        compareWrap.addClass('hidden');
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

    function getShowData(merge, node,params) {
        var se = getStartEnd();
        var queryBtn = destinationWrapper.all("#query_btn .btn-primary");
        if (!se) return;
        var env = destinationWrapper.one("#destination_env_select a.btn-primary").getAttribute('value');
        if(!params.remoteApp){
            var remoteApp = destinationWrapper.one("#remoteApp").get("value");
            var p_spanname =  destinationWrapper.one("#spanname").get("value");
            params['remoteApp']  = remoteApp;
            params['spanname'] = p_spanname;
        }

        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            role: 'client',
            group: 'SpanRemoteApp',
            dataSource: 'hbase',
            remoteApp: params['remoteApp'],
            spanname: params['spanname'],
            merge:merge

        };
        var eq = checkData(data);
        if(eq) return;
        showWaitMsg(node);
        var opt = {
            appkey: appkey,
            env: env,
            start: se.start,
            end: se.end,
            remoteApp: params['remoteApp'],
            spanname: params['spanname'],
            merge: true
        };

        Y.msgp.utils.urlAddParameters(opt);
        var conf = {
            method: 'get',
            data: data,
            on: {
                start: function () {
                    queryBtn.set('disabled', true);
                    queryBtn.setStyle('background', '#ccc');
                },
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            if(node === listWrapper) {
                                initList(arr);
                            } else if(!merge) {
                                chartsData = arr;
                                fillCharts();
                            }
                        } else {
                            showEmptyErrorMsg(node, 0);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(node, !0);
                    }
                },
                failure: function () {
                    searching = false;
                    showEmptyErrorMsg(node, !0);
                },
                complete: function () {
                    queryBtn.set('disabled', false);
                    queryBtn.setStyle('background', '');
                }
            }
        };
        doAjax(conf);
    }

    function getGraphsData() {
        var se = getStartEnd();
        if (!se) return;
        var params = {};
        var env = destinationWrapper.one("#destination_env_select a.btn-primary").getAttribute('value');
        var queryBtn = destinationWrapper.all("#query_btn .btn-primary");
        if(!params.remoteApp){
            var remoteApp = destinationWrapper.one("#remoteApp").get("value");
            var spanname =  destinationWrapper.one("#spanname").get("value");
            params['remoteApp']  = remoteApp;
            params['spanname'] = spanname;
        }

        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            role: 'client',
            group: 'SpanRemoteApp',
            dataSource: 'hbase',
            remoteApp: params['remoteApp'],
            spanname: params['spanname']
        };

        var eq = checkData(data);
        if(eq) return;
        listWrapper && showWaitMsg(listWrapper);
        var opt = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            remoteApp: params['remoteApp'],
            spanname: params['spanname'],
            merge: false
        };
        Y.msgp.utils.urlAddParameters(opt);
        var conf = {
            method: 'get',
            data: data,
            on: {
                start: function () {
                    queryBtn.set('disabled', true);
                    queryBtn.setStyle('background', '#ccc');
                },
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            chartsData = arr;
                            fillGraphs();
                        } else {
                            showEmptyErrorMsg(listWrapper);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(listWrapper, !0);
                    }
                },
                failure: function () {
                    searching = false;
                    showEmptyErrorMsg(listWrapper, !0);
                },
                complete: function () {
                    queryBtn.set('disabled', false);
                    queryBtn.setStyle('');
                }
            }
        };
        doAjax(conf);
    }

    function getHostDetailData(remoteApp, spanname, count) {
        //获取流量分布
        var se = getStartEnd();
        if (!se) return;
        var dialogLoading = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>'+
            '</div>';
        showHostDetailsDialog(dialogLoading);
        var env = destinationWrapper.one("#destination_env_select a.btn-primary").getAttribute('value');
        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            role: 'client',
            group: 'LocalHostRemoteApp',
            dataSource: 'hbase',
            remoteApp: remoteApp,
            remoteHost: 'all',
            localhost: '*',
            spanname: 'all',
            merge: true
        };
        var url = '/data/host_detail';
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            showHostDetail(arr, remoteApp, count)
                        } else {
                            showHostDetail([], remoteApp, count)
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(listWrapper, !0);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(listWrapper, !0);
                }
            }
        });
    }


    function fillCharts() {
        var wrapper = destinationWrapper.one("#charts_outer");
        if (wrapper) {
            clearWaitMsg(wrapper);
            insertChartsWrapper(2);
        }
        // delay charts init
        setTimeout(function () {
            var nds = destinationWrapper.all('.charts-wrapper-out');
            nds.item(0) && fillChartsReal(nds.item(0), chartsData[0], 0);
            nds.item(1) && fillCountReal(nds.item(1),chartsData[0], 1);
        }, 100);
    }

    function fillGraphs() {
        clearWaitMsg(listWrapper);
        insertGraphsWrapper(chartsData.length);
        // delay charts init
        setTimeout(function () {
            var nds = destinationWrapper.all('.charts-wrapper-out');
            for (var i = 0, l = chartsData.length; i < l; i++) {
                fillGraphsReal(nds.item(i), chartsData[i], i);
            }
        }, 100);
    }

    function fillChartsReal(dom, data, index) {
        var opt = wrapOption(data);
        var title = '';
        var inner = dom.one('.charts-wrapper').getDOMNode();
        var ec = echarts.init(inner);
        ec.setOption(opt);
        dom.setData('chartData', ec.getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
    }

    function fillGraphsReal(dom, data, index) {
        var opt = wrapOption(data);
        var title = '';
        var tmpArr = [];
        //title = 'localhost:' + data.tags.localhost + ' , ' + 'spanname:' + data.tags.spanname;
        Y.Array.each(keyResverd, function (item, index) {
            tmpArr.push(item + ':' + data.tags[item]);
            //tmpArr.push(data.tags[ item ] );
        });
        title = tmpArr.join(' , ');
        dom.one('p').setHTML(title);
        var inner = dom.one('.charts-wrapper').getDOMNode();
        var ec = echarts.init(inner);
        ec.setOption(opt);
        dom.setData('chartData', ec.getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
    }

    function fillCountReal(dom, obj, index) {
        var title = "Count";
        var legend = {
            bottom: 0,
            data: [{name: '调用量', icon: 'line'}]
        };
        var yAxis = [
            {
                type: 'value',
                name: '调用量',
                axisLine: {
                    show: false
                },
                axisLabel: {
                    formatter: numFormatter
                }
            }];
        var series = [
            {
                name: '调用量',
                type: 'line',
                symbol: 'none',
                smooth: true,
                data: getData(obj.count, 0)
            }];

        var opt = {
            animation: false,
            tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                show: false
            },
            calculable: true,
            legend: legend,
            xAxis: [
                {
                    type: 'category',
                    data: getXAxis(obj.count),
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: yAxis,
            series: series
        };
        var inner = dom.one('.charts-wrapper').getDOMNode();
        var ec = echarts.init(inner);
        ec.setOption(opt);
        dom.setData('chartData', ec.getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
    }

    function insertChartsWrapper(n) {
        var node;
        var wrapper = destinationWrapper.one('#charts_outer');
        for (var i = 0; i < n; i++) {
            if (node && node instanceof Y.Node) {
                node = Y.all(node).concat(Y.Node.create(chartsTemplate));
            } else if (node instanceof Y.NodeList) {
                node = node.concat(Y.Node.create(chartsTemplate));
            } else {
                node = Y.Node.create(chartsTemplate);
            }
        }
        wrapper.append(node);
    }

    function insertGraphsWrapper(n) {
        var node;
        for (var i = 0; i < n; i++) {
            if (node && node instanceof Y.Node) {
                node = Y.all(node).concat(Y.Node.create(graphsTemplate));
            } else if (node instanceof Y.NodeList) {
                node = node.concat(Y.Node.create(graphsTemplate));
            } else {
                node = Y.Node.create(graphsTemplate);
            }
        }
        listWrapper.append(node);
    }

    function initDatePicker() {
        var now = new Date();

        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });
        var s = startInput.get('value');
        if(s==""){
            var start = new Date(now - 63 * 60 * 1000);
            startInput.set('value', Y.mt.date.formatDateByString(start, 'yyyy-MM-dd hh:mm:ss'));
        }

        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });
        var e = endInput.get('value');
        if(e==""){
            var end = new Date(now - 3 * 60 * 1000);
            endInput.set('value', Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:ss'));
        }

        sdate.on('Datepicker.select', function () {
            changeTags()
        });
        edate.on('Datepicker.select', function () {
            changeTags();
        });
    }

    function changeTags() {
        var url = '/data/tags';
        var se = getStartEnd();
        if (!se) return;
        var env = destinationWrapper.one("#destination_env_select a.btn-primary").getAttribute('value');
        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            source: 'client'
        };
        $.ajax({
            type: 'get',
            url: url,
            data: data,
            success: function (ret) {
                if (ret.isSuccess) {
                    var tags = ret.data;
                    _remoteApps = tags.remoteAppKeys;
                    _spannames = tags.spannames;
                    initAppkeyTags();
                }
            }
        });

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

    function doAjax(conf) {
        searching = true;
        var url = '/data/kpi';
        Y.io(url, conf);
    }

    function wrapOption(obj) {
        var series = [
            {
                name: '50%耗时',
                type: 'line',
                symbol: 'none',
                smooth: true,
                data: getData(obj.tp50, 2)
            },
            {
                name: '90%耗时',
                type: 'line',
                symbol: 'none',
                smooth: true,
                data: getData(obj.tp90, 2)
            },
            {
                name: '99%耗时',
                type: 'line',
                symbol: 'none',
                smooth: true,
                data: getData(obj.tp99, 2)
            },
            {
                name: 'QPS',
                type: 'line',
                symbol: 'none',
                smooth: true,
                yAxisIndex: 1,
                data: getData(obj.qps, 2)
            }
        ];

        var legend = {
            bottom: 0,
            data: [{name:'50%耗时', icon: 'line' }, {name:'90%耗时', icon: 'line' }, {name:'99%耗时', icon: 'line' }, {name:'QPS', icon: 'line' }]
        };

        if (typeof(obj.qps_drop) != "undefined" && obj.qps_drop.length != 0) {
            series.push({
                name: '截流QPS',
                type: 'line',
                smooth: true,
                yAxisIndex: 1,
                data: getData(obj.qps_drop, 2)
            });
            legend.data.push('截流QPS');
        }

        var option = {
            animation: false,
            tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                show: false
            },
            calculable: true,
            legend: legend,
            xAxis: [
                {
                    type: 'category',
                    data: getXAxis(obj.count),
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: '耗时',
                    axisLine: {
                        show: false
                    },
                    axisLabel: {
                        formatter: timeFormatter
                    },
                    splitLine: 'true'
                },
                {
                    type: 'value',
                    name: 'QPS',
                    axisLine: {
                        show: false
                    },
                    axisLabel: {
                        formatter: numFormatter
                    }
                }
            ],
            series: series
        };
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

    function timeFormatter(num) {
        if (num < 1000) {
            return num + 'ms';
        } else if (num >= 1000 && num < 1000000) {
            return (num / 1000).toFixed(2) + 's';
        } else if (num >= 1000000 && num < 1000000000) {
            return (num / (1000*60)).toFixed(2) + 'min';
        } else if (num >= 1000000000) {
            return (num / (1000*60*60)).toFixed(2) + 'h';
        }
    }

    function getXAxis(arr) {
        var result = [];
        for (var i = 0, l = arr.length; i < l; i++) {
            result.push(arr[i].x);
        }
        lastXAxis = result;
        return result;
    }

    function getData(arr, precision) {
        var result = [];
        for (var i = 0, l = arr.length; i < l; i++) {
            if (precision) {
                var y = (arr[i].y);
                if (typeof(y) == "undefined") {
                    result.push(NaN);
                } else {
                    result.push((y).toFixed(2));

                }
            } else {
                result.push(arr[i].y);
            }
        }
        return result;
    }

    function template() {
        return '<table id="kpi_table" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th>服务</th>' +
            '<th>接口</th> ' +
            '<th>调用量</th> ' +
            '<th>QPS</th> ' +
            '<th>TP50耗时(毫秒)</th> ' +
            '<th>TP90耗时(毫秒)</th> ' +
            '<th>TP99耗时(毫秒)</th> ' +
            '<th>趋势</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody id="span_list"> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<% if (item.tags.remoteApp == "unknownService") {%>' +
            '<td style="width: 18%"><%= item.tags.remoteApp %> <a href="https://123.sankuai.com/km/page/28354817" target="_blank"><i class="fa fa-question-circle"></i></a></td>' +
            '<% } else { %>' +
            '<td style="width: 18%"><%= item.tags.remoteApp %></td>' +
            '<% } %>' +
            '<td style="width: 18%"><%= item.tags.spanname %></td>' +
            '<% if(item.tags.spanname == "all") { %>' +
            '<td style="width: 10%"><a class="host_detail_click" style="width:20px" href="javascript:void(0);" data-count="<%= item.count %>" data-remoteApp="<%= item.tags.remoteApp %>" data-spanname="<%= item.tags.spanname %>" title="显示主机调用详情"><%= item.count %></td>' +
            '<% }else{ %>' +
            '<td style="width: 10%"><%= item.count %></td>' +
            '<% } %>' +
            '<td style="width: 10%"><%= item.qps %></td>' +
            '<td style="width: 12%"><%= item.tp50 %></td>' +
            '<td style="width: 12%"><%= item.tp90 %></td>' +
            '<td style="width: 12%"><%= item.tp99 %></td>' +
            '<td style="width: 8%"> <a class="day_graph_click" style="width:20px" href="javascript:void(0);" data-remoteApp="<%= item.tags.remoteApp %>" data-spanname="<%= item.tags.spanname %>" title="显示趋势图"> <img src="data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCA1MTIgNTEyIiB3aWR0aD0iMjRweCIgaGVpZ2h0PSIyNHB4Ij4KICA8Zz4KICAgIDxnPgogICAgICA8cG9seWdvbiBwb2ludHM9IjE2Ni4zLDI4Ni4yIDI1MS44LDM3Mi44IDQxMi4yLDIxNC4zIDQxMS4zLDMxNC40IDQzMi4yLDMxNC40IDQzMy4yLDE3Ny44IDI5Ny43LDE3Ni44IDI5Ny43LDE5Ny42IDM5OC45LDE5OC41ICAgICAyNTIuOSwzNDMuNiAxNjYuMywyNTcgMzAuOCwzOTUuNyA0NS40LDQxMC4zICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICAgIDxwb2x5Z29uIHBvaW50cz0iNDgwLjEsMTEgNDgwLjEsNDgwLjEgMTEsNDgwLjEgMTEsNTAxIDUwMSw1MDEgNTAxLDExICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K" /> </a></td>' +
            '</tr>' +
            '<% }); %>' +
            '</tbody> ' +
            '</table>';
    }

    var hostDetailTemplate =
        '<table class="table table-striped table-hover " data-widget="sortTable">' +
        '<thead>' +
        '<tr>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">去向IP(客户端)</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">调用量/总调用量占比</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">成功数/百分比</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">失败数/百分比</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">Exception/Timeout/Drop/Http4XX/Http5XX</th>' +
        '</tr>' +
        '</thead>' +
        '<tbody>' +
        '<% Y.Array.each(this.data, function( item, index ){ %>' +
        '<tr>' +
        '<td style="width: 23%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.localhost %></td>' +
        '<td style="width: 16%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.count %>, <span style="color: #3fab99; font-weight: bold;"><%= item.countPercent %></span></td>' +
        '<td style="width: 16%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.successCount%>, </span><span style="color: #3fab99; font-weight: bold;"><%= item.successCountPercent %></span></td>' +
        '<td style="width: 16%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.failCount%>, </span><span style="color: #3fab99; font-weight: bold;"><%= item.failCountPercent %></span></td>' +
        '<td style="width: 27%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.exceptionCount %>, <%= item.timeoutCount %>, <%= item.dropCount %>, <%= item.http4xxCount %>, <%= item.http5xxCount %></td>' +
        '</tr>' +
        '<% }); %>' +
        '</tbody>' +
        '</table>';


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete',
        'template',
        'msgp-service/commonMap'
    ]
});

