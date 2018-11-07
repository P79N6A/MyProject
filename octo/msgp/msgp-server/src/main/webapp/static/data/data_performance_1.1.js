M.add('msgp-data/data_performance_1.1', function (Y) {
    Y.namespace('msgp.data').data_performance = data_performance;
    var appkey, _dayData, _graphDialog, _echartsObjectMap = {};
    var mtraceUrl;
    var dayInput;
    var hourInput;
    var kpiTable, kpiTableWrapper, performanceWrapper;
    var previous = null;
    var timeUnit;
    var availabilityDetailsDialog, serverFailureDetailsDialog;
    var serverFailureDetails = [];

    function data_performance(_appkey, _isFirstIn, _mtraceUrl) {
        appkey = _appkey;
        mtraceUrl = _mtraceUrl;
        document.title = "性能指标";
        if (!_isFirstIn) {
            return;
        }

        performanceWrapper = Y.one('#div_data_performance');
        kpiTableWrapper = performanceWrapper.one("#kpi_table_wrapper");
        dayInput = Y.one('#day');
        hourInput = Y.one('#hour');
        initEvent();
        refreshData();
    }

    function refreshData() {
        //获取当前天天粒度信息
        var day = dayInput.get("value");
        var hour = hourInput.get("value");
        var role = Y.one("#server_select a.btn-primary").getAttribute('value');
        var env = Y.one("#performance_env_select a.btn-primary").getAttribute('value');
        timeUnit = Y.one("#time_unit_select a.btn-primary").getAttribute('value');


        if (timeUnit == 'hour') {
            getChartsData(appkey, hour, env, initHourDataList, true, role);
        } else {
            getDayKpi(appkey, day, env, initDailyDataList, true, role);
            var date = new Date(Date.parse(day.replace(/-/g, "/")));
            var yesterday = new Date(date.getTime() - 86400 * 1000);
            yesterday = Y.mt.date.formatDateByString(yesterday, 'yyyy-MM-dd');
            var week = new Date(date.getTime() - 7 * 86400 * 1000);
            week = Y.mt.date.formatDateByString(week, 'yyyy-MM-dd');
            //获取环比信息
            if (role == "server") {
                setTimeout(function () {
                    getDayKpi(appkey, yesterday, env, function (dayType) {
                        return function (data) {
                            appendCompare(data, dayType)
                        }
                    }(1), false, role);
                    //获取同比信息
                    getDayKpi(appkey, week, env, function (dayType) {
                        return function (data) {
                            appendCompare(data, dayType)
                        }
                    }(2), false, role)

                }, 200);
            }
        }

        var opt = {
            appkey: appkey,
            role: role,
            env: env,
            day: day,
            hour: hour,
            timeUnit: timeUnit
        };
        Y.msgp.utils.urlAddParameters(opt);
    }

    function initEvent() {
        Y.msgp.service.setEnvText('performance_env_select');
        Y.msgp.utils.addTooltip({
            "#env_tooltip": 'https://123.sankuai.com/km/page/28253677',
            "#indicator": 'https://123.sankuai.com/km/page/28327894',
            "#time-consuming": 'https://123.sankuai.com/km/page/28354578',
            "#comparePre": 'https://123.sankuai.com/km/page/28354844'
        });
        $("a").on('click', function (e) {
            e.preventDefault;
            $("*").tooltip('hide');
        });
        Y.one("#server_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            performanceWrapper.all('#server_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
        }, "a");

        Y.one("#performance_env_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            performanceWrapper.all('#performance_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
        }, "a");

        Y.one("#time_unit_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            performanceWrapper.all('#time_unit_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            if (this.getAttribute('value') == 'day') {
                hourInput.hide();
                dayInput.show();
            } else {
                dayInput.hide();
                hourInput.show();
            }
        }, "a");

        initDatePicker();

        Y.one('#query_btn').on('click', function () {
            refreshData();
        });

    }

    function initDatePicker() {
        new Y.mt.widget.Datepicker({
            node: dayInput,
            showSetTime: false
        });

        new Y.mt.widget.Datepicker({
            node: hourInput,
            showSetTime: true
        });
    }

    function initDailyDataList(data) {
        Y.Array.each(data, function (item, index) {
            serverFailureDetails.push({
                spanname: item.spanname,
                exceptionCount: item.failureDetails.exceptionCount,
                timeoutCount: item.failureDetails.timeoutCount,
                dropCount: item.failureDetails.dropCount,
                http4XXCount: item.failureDetails.http4XXCount,
                http5XXCount: item.failureDetails.http5XXCount
            });
        });

        _dayData = converMap(data);
        var micro = new Y.Template();
        var str = micro.render(template(), {data: data});
        kpiTableWrapper.setHTML(str);
        Y.mt.widget.init();
        //绑定 click事件
        var day = dayInput.get("value");
        performanceWrapper.all(".spanname_click").on('click', function (e) {
            var url = e.target.getAttribute("url");
            url += "&day=" + day;
            var win = window.open(url, '_blank');
        });
        initClickGraph();
    }

    function initHourDataList(data) {
        var micro = new Y.Template();
        var str = micro.render(template(), {data: data});
        kpiTableWrapper.setHTML(str);
        Y.mt.widget.init();
        initClickGraph();
    }

    //触发点击图
    function initClickGraph() {
        kpiTable = Y.one('#kpi_table');
        kpiTable.delegate('click', function () {
            var value = this.getData("spanname");
            //弹出图标
            //console.log(value);
            var now = new Date();
            if (!previous || (now - previous > 600)) {
                var _id = $(this).attr('id');
                if ($("." + _id).length) {
                    $("." + _id).remove();
                } else {
                    $("#showGraph").remove();
                    str = '<tr id="showGraph" class=' + _id + '><td colspan="14" style="text-align: center;"></td></tr>';
                    setTimeout(function () {
                        $(".widget-table-choose").after(str);
                        showWaitMsg(Y.one("#showGraph td"));
                        getTrendData(value);
                    }, 0);
                }
                previous = now;
            } else {
                return;
            }
        }, '.day_graph_click');

        kpiTable.delegate('click', function () {
            setTimeout(function () {
                var spanname = Y.one(".widget-table-choose td").get('text');
                getAvailabilityDetails(spanname);
            }, 0)
        }, '.failureClientCountClick');
        kpiTable.delegate('click', function () {
            setTimeout(function () {
                var spanname = Y.one(".widget-table-choose td").get('text');
                var serverFailureData = [];
                if (spanname == 'all') {
                    serverFailureData = serverFailureDetails;
                } else {
                    Y.Array.each(serverFailureDetails, function (item, index) {
                        if (item.spanname == spanname) {
                            serverFailureData.push(item);
                        }
                    });
                }
                showServerFailureDetails(serverFailureData);
            }, 0)
        }, '.failureServerCountClick');
    }

    /**
     * 比较qps,tp90
     * @param data
     */
    function appendCompare(data, dayType) {
        //数据和选择的日志对比然后输出结果集
        var len = data.length;
        for (var i = 0; i < len; i++) {
            var comData = data[i];
            if (_dayData == undefined) {
                break;
            }
            var dayData = _dayData[comData.spanname];
            if (dayData != undefined) {
                var dData = dayData.data;
                dData.qps = dData.qps < 1 ? 1 : dData.qps;
                comData.qps = comData.qps < 1 ? 1 : comData.qps;

                dData.topPercentile.upper999 = dData.topPercentile.upper999 == 0 ? 1 : dData.topPercentile.upper999;
                comData.topPercentile.upper999 = comData.topPercentile.upper999 == 0 ? 1 : comData.topPercentile.upper999;

                var qratio = Math.floor((dData.qps - comData.qps) / comData.qps * 100);
                var uratio = Math.floor((dData.topPercentile.upper999 - comData.topPercentile.upper999) / comData.topPercentile.upper999 * 100);

                var value = document.getElementById("kpi_table_tbody").rows[dayData.index].cells[4];
                if (value == undefined) {
                    return;
                }
                var td_data = value.innerHTML.split(",");
                td_data[dayType] = qratio + "%";
                value.innerHTML = td_data.join(",");
                if (Math.abs(qratio) > 10) value.style.color = 'red';

                value = document.getElementById("kpi_table_tbody").rows[dayData.index].cells[8];
                td_data = value.innerHTML.split(",");
                td_data[dayType] = uratio + "%";
                value.innerHTML = td_data.join(",");
                if (Math.abs(uratio) > 10) value.style.color = 'red';
            }

        }
    }

    function converMap(data) {
        var len = data.length;
        var map = {};
        var allCount = 0;
        for (var i = 0; i < len; i++) {
            var dayData = data[i];
            map[dayData.spanname] = {data: dayData, index: i};

            if (dayData.spanname != 'all') {
                allCount += dayData.count * dayData.costMean
            }
        }
        for (var i = 0; i < len; i++) {
            var dayData = data[i];
            dayData.costRatio = allCount == 0 ? 'NaN' : ((dayData.count * dayData.costMean * 100) / allCount).toFixed(2) + "%"
        }
        return map;
    }

    /**
     * 获取天粒度数据
     * @param dataCallBack 回调函数
     */
    function getDayKpi(appkey, day, env, dataCallBack, emptyed, side) {
        var url = "/data/performance";
        Y.io(url, {
            method: 'get',
            //sync : true,
            data: {
                appkey: appkey,
                day: day,
                env: env,
                source: side
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            dataCallBack(arr)
                        } else if (emptyed) {
                            showEmptyErrorMsg();
                        }
                    } else {
                        //Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(!0);
                }
            }
        });
    }

    function getAvailabilityDetails(spanname) {
        var url = "/data/availability_details";
        var day = dayInput.get("value");
        var env = Y.one("#performance_env_select a.btn-primary").getAttribute('value');
        var dialogLoading = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">' +
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>' +
            '</div>';
        showAvailabilityDetailsDialog(dialogLoading);
        Y.io(url, {
            method: 'get',
            //sync : true,
            data: {
                appkey: Y.one("#apps_select").get("value"),
                day: day,
                env: env,
                spanname: spanname
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        showAvailabilityDetails(data)
                    } else {
                        //Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(!0);
                }
            }
        });
    }

    function initAvailabilityDetailsDialog() {
        var title = '客户端调用详情';
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'availability_detail_dialog',
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

    function showAvailabilityDetailsDialog(content) {
        availabilityDetailsDialog = availabilityDetailsDialog ? availabilityDetailsDialog : initAvailabilityDetailsDialog();
        availabilityDetailsDialog.setContent(content);
        availabilityDetailsDialog.show();
    }

    function showAvailabilityDetails(data) {
        var message = "";
        if (data.length == 0) {
            message = '<div style="text-align: center;font-size:20px; padding-top: 20px;">无详情数据。</div>' +
                '<div style="color: gray;text-align: center;font-size:14px; padding-top: 20px;">目前客户端错误数详情仅支持Thrift接口, 若错误数不为0, 但详情为空, 请确认此接口是否为HTTP接口。</div>' +
                '<div style="color: gray;text-align: center;font-size:14px;">HTTP接口对应的错误数一般为5xx错误, 少数为4xx错误, 可前往"来源分析"查看(选好接口和服务后, 点击调用量)。</div>';
        } else {
            var micro = new Y.Template();
            message = micro.render(clientFailureTemplate, {data: data});
        }
        showAvailabilityDetailsDialog(message);
    }

    function showServerFailureDetailsDialog(content) {
        serverFailureDetailsDialog = serverFailureDetailsDialog ? serverFailureDetailsDialog : initServerFailureDetailsDialog();
        serverFailureDetailsDialog.setContent(content);
        serverFailureDetailsDialog.show();
    }

    function showServerFailureDetails(data) {
        var micro = new Y.Template();
        var message = micro.render(serverFailureTemplate, {data: data});
        showServerFailureDetailsDialog(message);
    }

    function initServerFailureDetailsDialog() {
        var title = '失败数详情';
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'server_failure_detail_dialog',
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


    function showEmptyErrorMsg(isError) {

        var html = '';
        if (isError) {
            html = '<tr><td colspan="14">获取失败</td></tr>';
        } else {
            html = '<tr><td colspan="14">没有产生数据, 请点击<a href="https://123.sankuai.com/km/page/28354540" target="_Blank">数据分析问题自查</a>快速解决。</td></tr>';
        }
        var micro = new Y.Template();
        var str = micro.render(template(), {data: []});
        kpiTableWrapper.setHTML(str);
        Y.one("#kpi_table_tbody").setHTML(html)
    }


    function getTrendData(spanname) {
        var env = Y.one("#performance_env_select a.btn-primary").getAttribute('value');
        var unit = Y.one("#time_unit_select a.btn-primary").getAttribute('value');
        var day = dayInput.get("value");
        var url = '/data/kpi';
        var data = {};
        if (unit == "day") {
            url = '/data/daily_kpi';
            data = {
                appkey: appkey,
                start: day,
                spanname: spanname,
                env: env,
                source: "server"
            }
        } else {
            var date = new Date(Date.parse(startDate.replace(/-/g, "/")));
            var twoWeekBefore = new Date(date.getTime() - 2 * 86400 * 1000);
            var dayTimeString = Y.mt.date.formatDateByString(date, 'yyyy-MM-dd hh:00:00');
            var twoWeekBeforeString = Y.mt.date.formatDateByString(twoWeekBefore, 'yyyy-MM-dd hh:00:00');
            data = {
                appkey: appkey,
                start: twoWeekBeforeString,
                end: dayTimeString,
                env: env,
                unit: unit,
                role: "server",
                group: "span",
                spanname: spanname,
                localhost: "all"
            };
        }
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            fillCharts(arr);
                        } else {
                            showDialogEmptyMsg();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showDialogEmptyMsg(!0);
                    }
                },
                failure: function () {
                    showDialogEmptyMsg(!0);
                }
            }
        });
    }

    function fillCharts(data) {
        //数据拆分成 1：count报表，2：qps,tp,时间报表
        var micro = new Y.Template();
        var template = Y.one('#text_graph').get('value');
        var str = micro.render(template);
        Y.one("#showGraph td").setHTML(str);
        var graphs = Array("availability", "count", "qps");
        graphs.forEach(function (item) {
            var node = Y.one("#screen_" + item) && Y.one("#screen_" + item).getDOMNode();
            var ec = node && echarts.init(node);
            if (ec) {
                ec.showLoading({
                    text: "loading"
                });
                var kpiOption = wrapOption(data, item);
                ec.hideLoading();
                ec.setOption(kpiOption);
            }
        });
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
        if (num < 100) {
            return num + 'ms';
        } else if (num >= 100 && num < 1000000) {
            // return Math.round(num / 1000) + 's';
            return (num / 1000).toFixed(1) + 's';
        }
    }

    function wrapOption(obj, item) {
        var legend = [];
        var series = [];
        var yAxis = [];
        var unit = '';
        if (item === 'qps') {
            legend = {
                bottom: -5,
                itemGap: 10,
                data: [{name: '50%耗时', icon: 'bar'}, {name: '90%耗时', icon: 'bar'}, {name: '95%耗时', icon: 'bar'},
                    {
                        name: '99%耗时',
                        icon: 'bar'
                    }, {name: 'QPS', icon: 'bar'}]
            };
            yAxis = [
                {
                    type: 'value',
                    name: '耗时',
                    axisLine: {
                        show: false
                    },
                    axisLabel: {
                        //formatter: '{value}ms',
                        formatter: timeFormatter
                    }
                },
                {
                    type: 'value',
                    name: 'QPS',
                    axisLine: {
                        show: false
                    }
                }
            ];
            series = [
                {
                    name: '50%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.kpiTrend.tp50, 2)
                },
                {
                    name: '90%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.kpiTrend.tp90, 2)
                },
                {
                    name: '95%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.kpiTrend.tp95, 2)
                },
                {
                    name: '99%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.kpiTrend.tp99, 2)
                },
                {
                    name: 'QPS',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    yAxisIndex: 1,
                    data: getData(obj.kpiTrend.qps, 2)
                }
            ];
            unit = '';
        } else if (item === 'count') {
            legend = {
                bottom: 0,
                data: [{name: '调用量(服务端)', icon: 'bar'}, {name: '调用量(客户端)', icon: 'bar'}]
            };
            yAxis = [
                {
                    type: 'value',
                    name: '调用量',
                    axisLine: {
                        show: true
                    },
                    axisLabel: {
                        formatter: numFormatter
                    }
                }];
            series = [
                {
                    name: '调用量(服务端)',
                    type: 'line',
                    symbol: 'none',
                    clickable: false,
                    smooth: true,
                    data: getData(obj.countTrend.countBasedServer, 0)
                },
                {
                    name: '调用量(客户端)',
                    type: 'line',
                    symbol: 'none',
                    clickable: false,
                    smooth: true,
                    data: getData(obj.countTrend.countBasedClient, 0)
                }
            ];
            unit = '';
        } else {
            var max = Math.max.apply(null, obj.availabilityTrend.successRatioBasedClient);
            var min = Math.min.apply(null, obj.availabilityTrend.successRatioBasedClient);
            var maxyaxis = max;
            var minyAxis = max - (max - min) * 2;
            if (minyAxis == 100 || minyAxis < 0) {
                minyAxis = 0;
            }
            legend = {
                bottom: 0,
                data: [{name: '可用率', icon: 'bar'}]
            };
            yAxis = [
                {
                    type: 'value',
                    name: '可用率',
                    max: maxyaxis,
                    min: minyAxis,
                    axisLine: {
                        show: true
                    },
                    axisLabel: {
                        show: true,
                        formatter: '{value}%'
                    }
                }];
            series = [
                {
                    name: '可用率',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.availabilityTrend.successRatioBasedClient, 0)
                }]
        }

        var option = {
            animation: false,
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    if (params.componentType == "markPoint") {
                        return params.seriesName + '<br>' + params.name + ': ' + params.value + '%';
                    } else {
                        var length = params.length;
                        var tableTitle = params[0].name;
                        tableTitle += ", 星期" + "天一二三四五六".charAt(new Date(tableTitle).getDay());
                        var result =
                            '<div>' +
                            '<table style="background-color:rgba(0,0,0,0)"> ' +
                            '<caption>' + tableTitle +
                            '</caption>';

                        for (var j = 0; j < length; j++) {
                            if (params[j].seriesName.indexOf("耗时") > -1) {
                                console.log('params[j].seriesName: ' + params[j].seriesName);
                                unit = 'ms';
                            } else if (params[j].seriesName.indexOf("可用率") > -1) {
                                unit = '%';
                            } else {
                                unit = '';
                            }
                            result += '<tr>' +
                                '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + params[j].seriesName + ':</td>' +
                                '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + params[j].value + unit + '</td>' +
                                '</tr>';
                        }
                        result += '</table></div>';
                        return result;
                    }
                },
                padding: 10
            },
            toolbox: {
                show: false
            },
            calculable: true,
            legend: legend,
            xAxis: [
                {
                    type: 'category',
                    data: obj.dates,
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: yAxis,
            series: series
        };
        return option;
    }

    function getData(arr, precision) {
        var result = [];
        for (var i = 0, l = arr.length; i < l; i++) {
            if (precision) {
                var y = (arr[i]);
                if (typeof(y) == "undefined") {
                    result.push(NaN);
                } else {
                    result.push((y).toFixed(2));

                }
            } else {
                result.push(arr[i]);
            }
        }
        return result;
    }

    function getChartsData(appkey, day, env, dataCallBack, emptyed, role) {
        var data = {
            appkey: appkey,
            start: day,
            end: day,
            env: env,
            role: role,
            group: "span",
            spanname: "*",
            unit: timeUnit,
            merge: true
        };

        var conf = {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            dataCallBack(arr)
                        } else if (emptyed) {
                            showEmptyErrorMsg();
                        }
                    } else {
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(!0);
                }
            }
        };
        doAjax(conf);
    }

    function doAjax(conf) {
        searching = true;
        var url = '/data/kpi';
        Y.io(url, conf);
    }

    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node && node.setHTML(html);
    }

    function clearWaitMsg(node) {
        node && node.setHTML('');
    }

    function showDialogEmptyMsg(isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        _graphDialog.setContent(html);
    }

    function template() {
        var side = Y.one("#server_select a.btn-primary").getAttribute('value');
        var timeUnit = Y.one("#time_unit_select a.btn-primary").getAttribute('value');
        var day = dayInput.get("value");
        var start = day + " 00:00:00";
        var end = day + " 23:59:59";
        var now = new Date();
        var env = Y.one("#performance_env_select a.btn-primary").getAttribute('value');
        now = Y.mt.date.formatDateByString(now, 'yyyy-MM-dd');

        var tableHeader = '';
        var tableContent = '';
        if (day != now && env == 'prod') {
            //前一天prod环境下数据显示客户端调用和可用率
            tableHeader = '<th style="width: 11%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">调用量(客户端)</th> '
                + '<th style="width: 6%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">可用率</th> ';

            tableContent = '<td style="width: 11%;"><%= item.countClient%>, <a class="failureClientCountClick" href="javascript:;"><%= item.failCountClient%></a>' +
                '<% if(item.failCountClient == 0) { %>' +
                ', 0%' + '</a></td>' +
                '<% } else { %>' +
                ', <%= (item.failCountClient/item.countClient*100).toFixed(4) %>%' + '</a></td>' +
                '<% } %>' +
                '<% if((item.failCountClient/item.countClient*100) > 1) { %>' +
                '<td style="width: 6%; color: red;"><%= item.successCountClientPer%></a></td>' +
                '<% } else { %>' +
                '<td style="width: 6%"><%= item.successCountClientPer%></a></td>' +
                '<% } %>';
        } else {
            //显示错误数
            tableHeader = '<th style="width: 11%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">失败数/百分比</th> '
                + '<th style="width: 6%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis; display: none;">可用率</th> ';

            tableContent = '<td style="width: 11%;"><a class="failureServerCountClick" href="javascript:;"><%= item.failCount%></a>' +
                '<% if(item.failCount == 0) { %>' +
                ', 0%' + '</td>' +
                '<% } else { %>' +
                ', <%= (item.failCount/item.count*100).toFixed(4) %>%' + '</td>' +
                '<% } %>' +
                '<td style="width: 6%; display: none;"><%= item.successCountPer%></td>';
        }

        var tpl;
        if (timeUnit == 'hour') {
            tpl =
                '<table id="kpi_table" class="table table-striped table-hover " data-widget="sortTable"> ' +
                '<thead> ' +
                '<tr> ' +
                '<th>接口</th> ' +
                '<th>调用量(次数)</th> ' +
                '<th>失败数/百分比</th> ' +
                '<th>QPS(次/秒)</th> ' +
                '<th>TP50耗时(毫秒)</th> ' +
                '<th>TP90耗时(毫秒)</th> ' +
                '<th>TP99耗时(毫秒)</th> ' +
                //'<th>趋势</th> ' +
                '</tr> ' +
                '</thead> ' +
                '<tbody id="kpi_table_tbody"> ' +
                '<% Y.Array.each(this.data, function( item, index ){ %>' +
                '<tr>' +
                '<td style="word-break: break-all; overflow: hidden;width: 20%"><%= item.tags.spanname %></td>' +
                '<td style="width: 10%"><%= item.count %></td>' +
                '<td style="width: 10%"><%= item.failCount%>, <%= item.failCountPer%></td>' +
                '<td style="width: 10%"><%= item.qps %></td>' +
                '<td style="width: 10%"><%= item.tp50 %></td>' +
                '<td style="width: 10%"><%= item.tp90 %></td>' +
                '<td style="width: 10%"><%= item.tp99 %></td>' +
                //'<td style="width: 10%"> <a class="day_graph_click" style="width:20px" href="javascript:void(0);" data-spanname="<%= item.spanname %>" title="显示趋势图"> <img src="data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCA1MTIgNTEyIiB3aWR0aD0iMjRweCIgaGVpZ2h0PSIyNHB4Ij4KICA8Zz4KICAgIDxnPgogICAgICA8cG9seWdvbiBwb2ludHM9IjE2Ni4zLDI4Ni4yIDI1MS44LDM3Mi44IDQxMi4yLDIxNC4zIDQxMS4zLDMxNC40IDQzMi4yLDMxNC40IDQzMy4yLDE3Ny44IDI5Ny43LDE3Ni44IDI5Ny43LDE5Ny42IDM5OC45LDE5OC41ICAgICAyNTIuOSwzNDMuNiAxNjYuMywyNTcgMzAuOCwzOTUuNyA0NS40LDQxMC4zICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICAgIDxwb2x5Z29uIHBvaW50cz0iNDgwLjEsMTEgNDgwLjEsNDgwLjEgMTEsNDgwLjEgMTEsNTAxIDUwMSw1MDEgNTAxLDExICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K" /> </a></td>' +
                '</tr>' +
                '<% }); %>' +
                '</tbody> ' +
                '</table>';
        } else {
            if (side == "server") {
                tpl =
                    '<table id="kpi_table" class="table table-striped table-hover " data-widget="sortTable" style="margin-bottom: 0;"> ' +
                    '<thead> ' +
                    '<tr > ' +
                    '<th style="width: 18%;vertical-align: top;    overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">接口</th> ' +
                    '<th style="width: 7%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">调用量</th> ' +
                    tableHeader +
                    '<th style="width: 8%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">QPS</th> ' +
                    '<th style="width: 5%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP90</th> ' +
                    '<th style="width: 5%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP95</th> ' +
                    '<th style="width: 5%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP99</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP999</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP4个9</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP5个9</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">TP6个9</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">avg</th> ' +
                    // '<th style="width: 9%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">平均耗时占比</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">max</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">趋势</th> ' +
                    '<th style="width: 3%; vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">调用链</th> ' +
                    '</tr> ' +
                    '</thead> ' +
                    '<tbody id="kpi_table_tbody"> ' +
                    '<% Y.Array.each(this.data, function( item, index ){ %>' +
                    '<tr>' +
                    '<td style="width: 18%; text-overflow:ellipsis; overflow: hidden; word-break: break-all"><a href="javascript:void(0);" class="spanname_click"  style="margin-left: 2px;" target="_blank" url="/data/performance/span?appkey=<%= item.appkey %>&spanname=<%= item.spanname %>"><%= item.spanname %></a></td>' +
                    '<td style="width: 7%"><%= item.count %></td>' +
                    tableContent +
                    '<td style="width: 8%;"><%= item.qps %>,NaN,NaN</td>' +
                    '<td style="width: 5%"><%= item.topPercentile.upper90 %></td>' +
                    '<td style="width: 5%"><%= item.topPercentile.upper95 %></td>' +
                    '<td style="width: 5%"><%= item.topPercentile.upper99 %></td>' +
                    '<td style="width: 3%; word-break: break-all"><%= item.topPercentile.upper999 %></td>' +
                    '<td style="width: 3%"><%= item.topPercentile.upper9999 %></td>' +
                    '<td style="width: 3%"><%= item.topPercentile.upper99999 %></td>' +
                    '<td style="width: 3%"><%= item.topPercentile.upper999999 %></td>' +
                    '<td style="width: 3%"><%= parseInt(item.costMean) %></td>' +
                    // '<td style="width: 9%"><%= item.costRatio %></td>' +
                    '<td style="width: 3%"><%= item.upper %></td>' +
                    '<td style="width: 3%"> <a class="day_graph_click" style="width:20px" href="javascript:void(0);" data-spanname="<%= item.spanname %>" title="显示趋势图"> <img src="data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCA1MTIgNTEyIiB3aWR0aD0iMjRweCIgaGVpZ2h0PSIyNHB4Ij4KICA8Zz4KICAgIDxnPgogICAgICA8cG9seWdvbiBwb2ludHM9IjE2Ni4zLDI4Ni4yIDI1MS44LDM3Mi44IDQxMi4yLDIxNC4zIDQxMS4zLDMxNC40IDQzMi4yLDMxNC40IDQzMy4yLDE3Ny44IDI5Ny43LDE3Ni44IDI5Ny43LDE5Ny42IDM5OC45LDE5OC41ICAgICAyNTIuOSwzNDMuNiAxNjYuMywyNTcgMzAuOCwzOTUuNyA0NS40LDQxMC4zICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICAgIDxwb2x5Z29uIHBvaW50cz0iNDgwLjEsMTEgNDgwLjEsNDgwLjEgMTEsNDgwLjEgMTEsNTAxIDUwMSw1MDEgNTAxLDExICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K" /> </a></td>' +
                    '<td style="width: 3%"><a style="margin-left: 23px" target="_blank" href="' + mtraceUrl + '?appkey=<%= item.appkey %>&methodName=<%= item.spanname %>&timeStart=' + start + "&timeEnd=" + end + '&source=octoAnalysis"><i class="fa fa-external-link fa-lg"></i></a></td>' +
                    '</tr>' +
                    '<% }); %>' +
                    '</tbody> ' +
                    '</table>';
            } else {
                tpl =
                    '<table id="kpi_table" class="table table-striped table-hover " data-widget="sortTable"> ' +
                    '<thead> ' +
                    '<tr> ' +
                    '<th style="vertical-align: top;">接口</th> ' +
                    '<th style="vertical-align: top;">调用量(次数)</th> ' +
                    '<th style="vertical-align: top;">失败数/百分比</th> ' +
                    '<th style="vertical-align: top;">QPS(次/秒)</th> ' +
                    '<th style="vertical-align: top;">TP90</th> ' +
                    '<th style="vertical-align: top;">TP99</th> ' +
                    '<th style="vertical-align: top;">TP999</th> ' +
                    '<th style="vertical-align: top;">TP4个9</th> ' +
                    '<th style="vertical-align: top;">TP5个9</th> ' +
                    '<th style="vertical-align: top;">TP6个9</th> ' +
                    '<th style="vertical-align: top;">平均耗时</th> ' +
                    '<th style="vertical-align: top;">最大耗时</th> ' +
                    '</tr> ' +
                    '</thead> ' +
                    '<tbody id="kpi_table_tbody"> ' +
                    '<% Y.Array.each(this.data, function( item, index ){ %>' +
                    '<tr>' +
                    '<td style="word-break: break-all; overflow: hidden;width: 15%"><%= item.spanname %></td>' +
                    '<td style="width: 10%"><%= item.count %></td>' +
                    '<td style="width: 10%"><%= item.failCount%>,' +
                    '<% if(item.failCount == 0) { %>' +
                    ', 0%' + '</a></td>' +
                    '<% } else { %>' +
                    ', <%= (item.failCount/item.count*100).toFixed(4) %>%' + '</a></td>' +
                    '<% } %>' +
                    '<td style="width: 9%"><%= item.qps %></td>' +
                    '<td style="width: 7%"><%= item.topPercentile.upper90 %></td>' +
                    '<td style="width: 7%"><%= item.topPercentile.upper99 %></td>' +
                    '<td style="width: 7%"><%= item.topPercentile.upper999 %></td>' +
                    '<td style="width: 7%"><%= item.topPercentile.upper9999 %></td>' +
                    '<td style="width: 7%"><%= item.topPercentile.upper99999 %></td>' +
                    '<td style="width: 7%"><%= item.topPercentile.upper999999 %></td>' +
                    '<td style="width: 7%"><%= parseInt(item.costMean) %></td>' +
                    '<td style="width: 7%"><%= item.upper %></td>' +
                    '</tr>' +
                    '<% }); %>' +
                    '</tbody> ' +
                    '</table>';
            }
        }

        return tpl;
    }

    var clientFailureTemplate =
        '<table class="table table-striped table-hover " data-widget="sortTable">' +
        '<thead>' +
        '<tr>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">客户端</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">接口名</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">调用量</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">成功数/百分比</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">失败数/百分比</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">Exception/Timeout/Drop</th>' +
        '</tr>' +
        '</thead>' +
        '<tbody>' +
        '<% Y.Array.each(this.data, function( item, index ){ %>' +
        '<tr>' +
        '<td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.remoteAppkey %></td>' +
        '<td style="width: 25%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.spanname %></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.count %></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.successCount %>, </span><span style="color: #3fab99; font-weight: bold;"><%= item.successCountPer %>%</span></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.failureCount %>, </span><span style="color: #3fab99; font-weight: bold;"><%= item.failureCountPer %>%</span></td>' +
        '<td style="width: 10%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.exceptionCount %>, <%= item.timeoutCount %>, <%= item.dropCount %></td>' +
        '</tr>' +
        '<% }); %>' +
        '</tbody>' +
        '</table>';

    var serverFailureTemplate =
        '<table class="table table-striped table-hover " data-widget="sortTable">' +
        '<thead>' +
        '<tr>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">接口名</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">Exception</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">Timeout</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">Drop</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">HTTP4xx</th>' +
        '<th style="text-overflow:ellipsis; overflow: hidden;white-space: nowrap; background-color: #ffffff;">HTTP5xx</th>' +

        '</tr>' +
        '</thead>' +
        '<tbody>' +
        '<% Y.Array.each(this.data, function( item, index ){ %>' +
        '<tr>' +
        '<td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.spanname %></td>' +
        '<td style="width: 25%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.exceptionCount %></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.timeoutCount %></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.dropCount %></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.http4XXCount %></td>' +
        '<td style="width: 15%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><span><%= item.http5XXCount %></td>' +
        '</tr>' +
        '<% }); %>' +
        '</tbody>' +
        '</table>' +
        '<span style="color: gray;padding-top: 20px;">注: HTTP4xx 和 HTTP5XX用来表示HTTP接口的失败数, exception、timeout和drop用来表示Thrift接口的失败数。</span>';


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/common',
        'msgp-service/commonMap'
    ]
});
