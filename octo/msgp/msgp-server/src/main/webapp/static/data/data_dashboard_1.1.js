M.add('msgp-data/data_dashboard_1.1', function (Y) {
    Y.namespace('msgp.data').data_dashboard = data_dashboard;

    var chartsWrapper, dashboardWrapper, availabilityChartWrapper;
    var minute = 60, day = 9 * 24 * 60;
    var appkey, keyDiv, keyTbody, perfTbody, alarmTbody;
    var ec = [];

    function data_dashboard(key, _isFirstIn) {
        appkey = key;
        document.title = "数据总览";
        if (!_isFirstIn) {
            return;
        }

        dashboardWrapper = Y.one('#div_data_dashboard');
        chartsWrapper = dashboardWrapper.one('#charts_outer');
        availabilityChartWrapper = dashboardWrapper.one('#availabilityChartOuter');
        keyDiv = dashboardWrapper.one('#key_metric');
        keyTbody = keyDiv.one('#key').one('tbody');
        perfTbody = keyDiv.one('#perf').one('tbody');
        alarmTbody = keyDiv.one('#falcon_alarm').one('tbody');

        initKeyMetric();
        getChartsData(appkey);
        getAvailabilityChartData();
    }

    /**
     * 获得appkey all接口的可用率指标，展示为chart图
     */

    function getAvailabilityChartData() {
        var spanname = "all";
        var env = "prod";
        var url = '/data/dailyAvailability';
        var data = {
            appkey: appkey,
            spanname: spanname,
            env: env,
            source: "server"
        };
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            fillAvailabilityCharts(arr);
                        } else {
                            showDialogEmptyMsg();
                        }
                    } else {
                        showDialogEmptyMsg(!0);
                    }
                },
                failure: function () {
                    showDialogEmptyMsg(!0);
                }
            }
        });
    }

    function fillAvailabilityCharts(data) {
        var availabilityEc = echarts.init(document.getElementById('showAvailability'));
        if (availabilityEc) {
            availabilityEc.showLoading({
                text: "loading"
            });
            var availabilityOption = availabilityWrapOption(data);
            availabilityEc.on("click", function (param) {
                window.location.href = "/data/tabNav?day=" + param.name + "&appkey=" + appkey + "&type=performance#performance"
            });
            availabilityEc.on("dataZoom", function(){
                setAverageAvailability(this, data);
            });
            availabilityEc.hideLoading();
            availabilityEc.setOption(availabilityOption, true);
            setAverageAvailability(availabilityEc, data);
        }
        ;
    }

    function setAverageAvailability(availabilityEc, data) {
        var opt = availabilityEc.getOption();
        var dz = opt.dataZoom[0];
        var start = dz.startValue;
        var end = dz.endValue;
        var startTime = opt.xAxis[0].data[start];
        var endTime = opt.xAxis[0].data[end];
        var totalSuccessCount = 0;
        var totalCount = 0;
        var day = 0;
        for (var i = start;i <= end;i++){
            var daySuccessCount = data.successCountBasedClient[i];
            var dayCount = data.countBasedClient[i];
            totalSuccessCount += daySuccessCount;
            totalCount += dayCount;
            day++;
        }
        var averageAvailability = (totalCount == 0)? 0 : (totalSuccessCount/totalCount*100).toFixed(6);
        var averageCount = (day == 0)? 0 : (totalCount/day).toFixed(0);
        $("#averageAvailability").html("<table>" +
            "<tr><td style='font-size: 12px; color: #9d9d9d' colspan='2'>"+startTime+"<span> - </span>"+endTime+"</td></tr>" +
            "<tr><td>平均可用率：</td>" +
            "<td style='font-size: 20px; color: #843534'>"+averageAvailability+"%</td></tr>" +
            "<tr><td>日平均调用数：</td>" +
            "<td style='font-size: 20px; color: #843534'>"+averageCount+"</td></tr></table>");
    }

    function availabilityWrapOption(data) {
        var legend;
        var series;
        var yAxis;
        series = [
            {
                name: '可用率',
                type: 'line',
                symbol: 'emptyCircle',
                showAllSymbol: true,
                symbolSize: 8,
                data: data.availabilityBasedClient
            }];
        var availabilityBasedClientDouble = [];
        for(var item in data.availabilityBasedClient){
            if(data.availabilityBasedClient[item] != "-")
                availabilityBasedClientDouble.push(data.availabilityBasedClient[item]*1);
        }
        var max = Math.max.apply(Math,availabilityBasedClientDouble);
        var min = Math.min.apply(Math,availabilityBasedClientDouble);
        var maxyAxis = max;
        var minyAxis = max - (max - min) * 2;
        if (minyAxis == 100 || minyAxis < 0) {
            minyAxis = 0;
        }
        yAxis = [
            {
                type: 'value',
                name: '可用率',
                scale: true,
                axisLine: {
                    show: true
                },
                axisLabel: {
                    show: true,
                    formatter: '{value}%'
                },
                min: minyAxis,
                max: maxyAxis,
            }];
        var option = {
            title: {
                text: '可用率趋势图',
                subtext: appkey,
                x: 'center'
            },
            animation: false,
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    var tableTitle = params[0].name;
                    tableTitle += ", 星期" + "天一二三四五六".charAt(new Date(tableTitle).getDay());
                    var index = params[0].dataIndex
                    var result =
                        '<div>' +
                        '<table style="background-color:rgba(0,0,0,0)"> ' +
                        '<caption>' + tableTitle +
                        '</caption>';
                    result += '<tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + params[0].seriesName + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + params[0].value + '%' + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + '调用总数' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.countBasedClient[index] + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + '成功调用数' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.successCountBasedClient[index] + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'exceptionCount' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.exceptionCountBasedClient[index] + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'timeoutCount' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.timeoutCountBasedClient[index] + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'dropCount' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.dropCountBasedClient[index] + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'http4xxCount' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.http4xxCountBasedClient[index] + '</td>' +
                        '</tr><tr>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'http5xxCount' + ':</td>' +
                        '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.http5xxCountBasedClient[index] + '</td>' +
                        '</tr>';
                    result += '</table></div>';
                    return result;
                },
                padding: 10
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true},
                }
            },
            calculable: true,
            xAxis: [
                {
                    type: 'category',
                    data: data.dates,
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: yAxis,
            dataZoom: {
                show: true,
                start: 83,
            },
            series: series
        };
        return option;
    }

    function showDialogEmptyMsg(isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        availabilityChartWrapper.setContent(html);
    }

    /**
     * 根据订阅的appkey获取chart图
     * 1:获取分钟粒度的数据,2,获取天粒度的数据,
     */
    function getChartsData(appkey) {
        insertChartsWrapper(2);
        getChartData(getChartParam(appkey, minute), "实时60分钟", 0);
        getChartData(getChartParam(appkey, day), "最近9天", 1);
        window.setInterval(function (key) {
            return function () {
                getChartData(getChartParam(key, minute), "实时60分钟", 0);
                getChartData(getChartParam(key, day), "最近9天", 1);
            }
        }(appkey), 60000)
    }

    function getChartData(data, title, index) {
        var conf = {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var jsonData = ret.data;
                        if(jsonData.length != 0){
                            fillCharts(jsonData, title, index);
                        } else {
                            showEmptyErrorMsg(0, title, index);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(!0, title, index);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(!0, title, index);
                }
            }
        };
        doAjax(conf);
    }


    function doAjax(conf) {
        var url = '/data/kpi';
        Y.io(url, conf);
    }

    /**
     *
     * @param appkey
     * @param start
     */
    function getChartParam(appkey, minute) {
        var now = new Date();
        var start = Y.mt.date.formatDateByString(new Date(now.getTime() - (minute + 5) * 60 * 1000), 'yyyy-MM-dd hh:mm:ss');
        var end = Y.mt.date.formatDateByString(new Date(now.getTime() - 5 * 60 * 1000), 'yyyy-MM-dd hh:mm:ss');
        return {
            appkey: appkey,
            start: start,
            end: end,
            env: "prod",
            source: "server",
            group: "span",
            spanname: "all",
            merge: false,
            localhost: "all"
        };
    }

    function fillCharts(jsonData, title, index) {
        // delay charts init
        setTimeout(function () {
            var nds = chartsWrapper.all('.charts-wrapper-out');
            for (var i = 0, l = jsonData.length; i < l; i++) {
                fillChartsReal(nds.item(index), jsonData[i], title, index);
            }
        }, 100);
    }

    function insertChartsWrapper(n) {
        chartsWrapper.setHTML("");
        var node;
        for (var i = 0; i < n; i++) {
            if (node && node instanceof Y.Node) {
                node = Y.all(node).concat(Y.Node.create(chartsTemplate));
            } else if (node instanceof Y.NodeList) {
                node = node.concat(Y.Node.create(chartsTemplate));
            } else {
                node = Y.Node.create(chartsTemplate);
            }
        }
        chartsWrapper.append(node);
        var nds = chartsWrapper.all('.charts-wrapper-out');
        for (var i = 0; i < n; i++){
            var inner = nds.item(i).one('.charts-wrapper').getDOMNode();
            ec[i] = echarts.init(inner);
            ec[i].showLoading({
                text: '正在加载',
                effect: 'bar',
                textStyle: {
                    fontSize: 20
                }
            });
        }
    }

    function initKeyMetric() {
        // 性能数据
        var url1 = '/data/perf/metric?appkey=' + appkey;
        Y.io(url1, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillPerfMetric(ret.data);
                    } else {
                        emptyOrError(true, 7, perfTbody)
                    }
                }
            }
        });

        // gc、jvm
        var url2 = '/data/key/metric?appkey=' + appkey;
        Y.io(url2, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillKeyMetric(ret.data);
                    } else {
                        emptyOrError(true, 3, keyTbody)
                    }
                }
            }
        });

        // falcon all alarm
        var now = new Date();
        var url3 = '/data/all/alarm?appkey=' + appkey + "&endTime=" + Math.round(now.getTime() / 1000);
        Y.io(url3, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillFalconAlarm(ret.data);
                    } else {
                        emptyOrError(true, 3, alarmTbody)
                    }
                }
            }
        });
    }

    function emptyOrError(isError, colspan, dom) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容');
        dom.setHTML(html);
    }


    var perfTemplate = [
        '<tr>',
        '<td><%= Y.mt.date.formatDateByString( new Date(this.data.time), "yyyy-MM-dd hh:mm" ) %></td>',
        '<td><%= this.data.qps %></td>',
        '<td><%= this.data.qpsPerHost %></td>',
        '<td><%= this.data.maxQpsHost %> : <%= this.data.maxQps %></td>',
        '<td><%= this.data.tp90 %></td>',
        '<td><%= this.data.eventCount %></td>',
        '<td><%= this.data.errorCount %></td>',
        '</tr>'
    ].join('');


    function fillPerfMetric(data) {
        var micro = new Y.Template();
        var html = micro.render(perfTemplate, {data: data});
        perfTbody.setHTML(html);
    }

    var trTemplate = [
        '<tr>',
        '<td>',
        '<% Y.Array.each(this.data.load, function(item, index){ %>',
        '<%= item.endpoint %> : ' + '<%= item.value.value %> <br/>',
        '<% }); %>',
        '</td>',
        '<td>',
        '<% Y.Array.each(this.data.fullGc, function(item, index){ %>',
        '<%= item.endpoint %> : ' + '<%= item.value.value %> <br/>',
        '<% }); %>',
        '</td>',
        '<td>',
        '<% Y.Array.each(this.data.gc, function(item, index){ %>',
        '<%= item.endpoint %> : ' + '<%= item.value.value %> <br/>',
        '<% }); %>',
        '</td>',
        '</tr>'
    ].join('');


    function fillKeyMetric(data) {
        var micro = new Y.Template();
        var html = micro.render(trTemplate, {data: data});
        keyTbody.setHTML(html);
    }

    var alarmTemplate = [
        '<tr>',
        '<td><%= Y.mt.date.formatDateByString( new Date(this.data.endTime * 1000), "yyyy-MM-dd hh:mm" ) %></td>',
        '<td><%= this.data.nodeSearch %></td>',
        '<td><a target="_blank" href="<%= this.url %>"><%= this.data.data.data.alarm_data.length %></a></td>',
        '</tr>'
    ].join('');


    function fillFalconAlarm(data) {
        var micro = new Y.Template();
        var url = "http://p.falcon.sankuai.com/api/alarm/dashboard/?node=" + data.nodeSearch.replace(/&/g, "%26").replace(/=/g, "%3D");
        var html = micro.render(alarmTemplate, {data: data, url: url});
        alarmTbody.setHTML(html);
    }

    var dialogContentStr = [
        '<div id="add_supplier_form" class="form-horizontal">',
        '<div class="control-group"><label class="control-label">标题：</label>',
        '<div class="controls">',
        '<input id="s_title" type="text" value="" placeholder="标题，必填" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">falcon指标名：</label>',
        '<div class="controls">',
        '<input id="s_metric" type="text" value="" placeholder="metric，必填" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    function fillChartsReal(dom, data, title, index) {
        dom.one('p').setHTML(title);
        if (data.count) {
            var opt = wrapOption(data);
            ec[index].setOption(opt);
            ec[index].hideLoading();
            dom.setData('chartData', ec[index].getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
        } else {
            dom.setData('chartTitle', title).setAttribute('dindex', index);
        }
    }


    function showEmptyErrorMsg(isError, title, index) {
        var nds = chartsWrapper.all('.charts-wrapper-out');
        var dom = nds.item(index);
        dom.one('p').setHTML(title);
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有数据') + '</div>';
        dom.one('.charts-wrapper').setHTML(html);
    }

    function wrapOption(obj) {
        var option = {
            animation: false,
            tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            calculable: true,
            legend: {
                data: ['50%耗时', '90%耗时', '99%耗时', 'QPS']
            },
            xAxis: [
                {
                    type: 'category',
                    data: getXAxis(obj.count)
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: '耗时',
                    axisLabel: {
                        formatter: '{value}ms'
                    },
                    splitLine: 'true'
                },
                {
                    type: 'value',
                    name: 'QPS'
                }
            ],
            series: [
                {
                    name: '50%耗时',
                    type: 'line',
                    smooth: true,
                    data: getData(obj.tp50, 2)
                },
                {
                    name: '90%耗时',
                    type: 'line',
                    smooth: true,
                    data: getData(obj.tp90, 2)
                },
                {
                    name: '99%耗时',
                    type: 'line',
                    smooth: true,
                    data: getData(obj.tp99, 2)
                },
                {
                    name: 'QPS',
                    type: 'line',
                    smooth: true,
                    yAxisIndex: 1,
                    data: getData(obj.qps, 2)
                }
            ]
        };
        return option;
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

    var chartsTemplate = [
        '<div class="charts-wrapper-out" style="margin:10px 50px 30px">',
        '<p style="word-break: break-all; overflow: hidden;width: 500px"></p>',
        '<div class="charts-wrapper"></div>',
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
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip',
        'mt-base'
    ]
});
