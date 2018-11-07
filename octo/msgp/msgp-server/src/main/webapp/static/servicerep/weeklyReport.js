M.add('msgp-servicerep/weeklyReport', function (Y) {
    Y.namespace('msgp.servicerep').weeklyReport = weeklyReport;
    var day, username, appkeyList, type;
    var appkeySelected;
    var startInput = Y.one('#start_time');
    var reportContentWrapper;
    function weeklyReport(_day, _username, _appkeyList) {

        day = _day;
        username = _username;
        appkeyList = _appkeyList;
        document.title = '服务治理周报';

        initDatePicker();
        initEvent();
        type = "echart";
        appkeySelected = '';
        // 获取趋势图 数据
        getTrendData(type, appkeySelected)
    }

    function initDatePicker() {
        var sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: false
        });
        sdate.on('Datepicker.select', function () {
            getTrendData(type, appkeySelected)
        });
        startInput.set('value', day);
    }

    function initEvent(){
        reportContentWrapper = Y.one('#showGraph');
        Y.one('#btn_table').on('click', function () {
            Y.one("#btn_echart").removeClass('btn-primary');
            this.addClass('btn-primary');
            type = "table";
            getTrendData(type, appkeySelected)
        });
        Y.one('#btn_echart').on('click', function () {
            Y.all("#btn_table").removeClass('btn-primary');
            this.addClass('btn-primary');
            type = "echart";
            getTrendData(type, appkeySelected)
        });
        $("#appkeys").autocomplete({
            source: appkeyList,
            minLength: 0,
            select: function(event, ui) {
                appkeySelected = ui.item.value;
                getTrendData(type, appkeySelected);
            }
        });
    }

    function getTrendData(type, _appkey) {
        showWaitMsg(reportContentWrapper);
        var date = startInput.get("value");
        var appkey = Y.one("#appkeys").get("value");
        if(_appkey != ''){
            appkey = _appkey;
        }else{
            appkey = 'all';
        }
        showWeekTips(date);
        var url = '/repservice/weekly/data';
        var data = {
            username: username,
            appkey: appkey,
            dataType: type,
            day: date
        };

        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        Y.one("#showGraph").empty();
                        if (arr.week_data.length !== 0) {
                            if(type=="echart"){
                                fillCharts(arr);
                            }else{
                                fillTable(arr)
                            }

                        } else {
                            showDialogEmptyMsg(0);
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

    function fillTable(data){
        Y.one("#showGraph").empty();
        var micro = new Y.Template();
        var template = Y.one('#text_table').get('value');
        var weekData = data.week_data;
        var days = data.xAxis;
        var str = micro.render(template, {days: days,data:weekData});
        Y.one("#showGraph").setHTML(str)
    }

    function fillCharts(data) {
        var micro = new Y.Template();
        var template = Y.one('#text_echart').get('value');
        var xAxis = data.xAxis;
        data.week_data.forEach(function (item) {
            var appkey = item.title.replace(/\./g, "_");
            var seriesDatas = item.series;
            var str = micro.render(template, {appkey: appkey});
            Y.one("#showGraph").append(str);

            var successRatioData = seriesDatas['successRatio'][0]['data'];
            showCharts(appkey, 'successRatio', successRatioData, xAxis);

          /*  var countData = seriesDatas['count'][0]['data'];
            showCharts(appkey, 'count', countData, xAxis);*/

            var qpsData = seriesDatas['qps'][0]['data'];
            showCharts(appkey, 'qps', qpsData, xAxis);

            var tp5090Data = [seriesDatas['tp50'][0]['data'], seriesDatas['tp90'][0]['data']];
            showCharts(appkey,'tp5090', tp5090Data, xAxis);

            var tp999Data = seriesDatas['tp999'][0]['data'];
            showCharts(appkey,'tp999', tp999Data, xAxis);
        })
    }

    function showCharts(appkey, name, data, xAxis) {
        var app_node = Y.one("#week_" + appkey);
        var node = app_node.one("#screen_" + name) && app_node.one("#screen_" + name).getDOMNode();
        var ec = node && echarts.init(node);
        if (ec) {
            ec.showLoading({
                text: "loading"
            });
            var kpiOption = wrapOption({data: data, xAxis: xAxis}, name);
            ec.hideLoading();
            ec.setOption(kpiOption);
        }
    }

    function wrapOption(obj, item) {
        var legend = [];
        var series = [];
        var yAxis = [];
        var unit = '%';
        if (item === 'count') {

            yAxis = [{
                type: 'value',
                name: '调用量',
                axisLine: {
                    show: false
                },
                axisLabel: {
                    formatter: numFormatter
                }
            }];
            series = [
                {
                    name: '调用量',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.data, 2)
                }
            ];
            unit = '';
            legend = ['调用量'];
        }
        else if (item === 'qps') {

            yAxis = [{
                type: 'value',
                name: 'QPS',
                axisLine: {
                    show: false
                },
                axisLabel: {
                    formatter: numFormatter
                }
            }];
            series = [
                {
                    name: 'QPS',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.data, 2)
                }
            ];
            unit = '';
            legend = ['QPS'];
        }
        else if (item === 'tp5090') {
            yAxis = [{
                type: 'value',
                name: 'tp50/tp90',
                axisLine: {
                    show: false
                },
                axisLabel: {
                    formatter: timeFormatter
                }
            }];
            series = [
                {
                    name: '50%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.data[0], 2)
                },
                {
                    name: '90%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.data[1], 2)
                }
            ];
            unit = '';
            legend = ['50%耗时', '90%耗时'];
        }else if (item === 'tp999') {
            yAxis = [{
                type: 'value',
                name: 'tp999耗时',
                axisLine: {
                    show: false
                },
                axisLabel: {
                    formatter: timeFormatter
                }
            }];
            series = [
                {
                    name: '99.9%耗时',
                    type: 'line',
                    symbol: 'none',
                    smooth: true,
                    data: getData(obj.data, 2)
                }
            ];
            unit = '';
            legend = ['99.9%耗时'];
        } else  {
            var available  = [];
            for(var i in obj.data) {
                available.push(obj.data[i]/100)
            }
            var max = Math.max.apply(null, available);
            var min = Math.min.apply(null, available);
            var maxyaxis = max;
            var minyAxis = max - (max - min) * 2;
            if (minyAxis == 100 || minyAxis < 0) {
                minyAxis = 0;
            }
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
                    data: getData(available, 0)
                }
            ];
            legend = ['可用率'];
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
                                unit = "ms";
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
            legend: {
                data: legend
            },
            xAxis: [
                {
                    type: 'category',
                    data: obj.xAxis,
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

    function showWeekTips(day) {
        var days = getWeek(day);
        Y.one('#weekly_tips').setHTML("<b>报告周期:</b> " + days[0] + " 至 " + days[days.length - 1])
    }

    function getWeek(day) {
        var url = '/repservice/week';
        var request = Y.io(url, {
            method: 'get',
            sync: true,
            data: {day: day}

        });
        var days = [];
        var ret = Y.JSON.parse(request.responseText);
        if (ret.isSuccess) {
            var data = ret.data;
            if (Y.Lang.isArray(data) && data.length !== 0) {
                days = data;
            }
        }
        return days;
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


    function timeFormatter(num) {
        if (num < 1000) {
            return num + 'ms';
        } else if (num >= 1000 && num < 1000000) {
            return Math.round(num / 1000) + 's';
        }
    }

    function showDialogEmptyMsg(isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '未产生数据') + '</div>';
        Y.one("#showGraph").setContent(html);
    }

    function showWaitMsg(node) {
        var html = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>'+
            '</div>';
        node.append(html);
    }

},
'0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'mt-date',
        'w-date',
        'template',
        'w-autocomplete',
        'msgp-utils/msgpHeaderTip'
    ]
});