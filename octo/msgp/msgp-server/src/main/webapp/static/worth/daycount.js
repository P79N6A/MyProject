M.add('msgp-worth/daycount', function (Y) {
    var _report_style = "";
    var _echart = null;
    var dataType = 0;
    var title = "";
    var uvOptionCache;
    var pvOptionCache;
    var newSearch;
    Y.namespace('msgp.worth').daycount = daycount;
    function daycount(report_style) {
        _report_style = report_style;
        document.title = '总流量日趋势';
        dataType = 0;
        title = "PV";
        initDatePicker();
        initCharts();
        bindTypeSelect();
        bindSearch();
        getChartData(dataType);
        /*try{
            getChartData();
        }catch(e) {
            console.log(e);
            Raven.captureException(e)
        }*/
    }

    function bindSearch() {
        Y.one('#searchForm').delegate('click', function () {
            getChartData(dataType);
            newSearch = true;
        }, '#searchBtn');
    }

    function bindTypeSelect() {
        Y.one('#count_type_select_all').delegate('click', function () {
            Y.all('#count_type_select_all a').removeClass('btn-primary');
            this.addClass("btn-primary");
            dataType = parseInt(this.getAttribute('value'));
            changeDataType(dataType);
        }, "a")
    }

    function initCharts() {
        var node = Y.one("#daycount_charts").getDOMNode();
        _echart = echarts.init(node);
    }

    function initDatePicker() {
        var now = new Date();
        var startInput = Y.one('#start');
        var endInput = Y.one('#end');
        var end_day = new Date(now - 24 * 60 * 60 * 1000);
        var start_day = new Date(end_day - 30*24 * 60 * 60 * 1000);
        new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: false
        });
        new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: false
        });
        startInput.set('value', Y.mt.date.formatDateByString(start_day, 'yyyy-MM-dd'));
        endInput.set('value', Y.mt.date.formatDateByString(end_day, 'yyyy-MM-dd'));
    }

    function  changeDataType(dataType) {
        if(pvOptionCache == null || uvOptionCache == null || newSearch){
            title = (dataType == 0) ? "PV" : "UV";
            getChartData(dataType);
            newSearch = false;
        }else{
            if(dataType == 0) {
                title = "PV";
                _echart.setOption(pvOptionCache);
            }else {
                title = "UV";
                _echart.setOption(uvOptionCache);
            }
        }
    }

    function getChartData(dataType) {
        _echart.showLoading({
            text: "loading"
        });
        var start = Y.one("#start").get('value');
        var end = Y.one("#end").get('value');
        var searchData = {
            "start": start,
            "end": end,
            "datatype": dataType
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/worth/daily/total';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.counts.length == 0){
                            _echart.hideLoading();
                            _echart.clear();
                            Y.msgp.utils.msgpHeaderTip('info', '未产生数据', 3);
                        }else {
                            fillCharts(data);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                }
            }
        });
    }

    function fillCharts(data) {
        var maxMap = data.max;
        Y.one("#pv_value").set("text", maxMap["pv"].count);
        Y.one("#pv_date").set("text", maxMap["pv"].date);
        Y.one("#uv_value").set("text", maxMap["uv"].count);
        Y.one("#uv_date").set("text", maxMap["uv"].date);

        var series = [{
            name: title,
            type: 'line',
            symbol: 'emptyCircle',
            data: data.counts,
            symbolSize : 4 | 8,
            smooth: true,
            showAllSymbol: true,
            sampling: 'average',
            markPoint : {
                data : [
                    {type : 'max', name: '最大值'},
                    {type : 'min', name: '最小值'}
                ]
            },
            markLine : {
                data : [
                    {type : 'average', name: '平均值'}
                ]
            }
        }];
        var legendData = [{
            icon: 'line',
            name: data.dates
        }];
        var legend = {
            bottom: 0,
            data: legendData
        };

        var option = {
            title: {
                show: true,
                text: title + "流量趋势",
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
                },
                formatter: function (params) {
                    if(params.componentType == "markLine"){
                        return  params.seriesName + params.name+": " + params.value;
                    }else if(params.componentType == "markPoint") {
                        return  params.seriesName + params.name + ": " + params.value;
                    } else{
                        var tableTitle = params[0].name;
                        tableTitle += ", 星期" + "天一二三四五六".charAt(new Date(tableTitle).getDay());
                        var sortedValue =
                            '<table> ' +
                            '<caption style="color: #3fab99;">' + tableTitle +
                            '</caption>' +
                            '<tr> ' +
                            '<td style="text-align: left; padding-left: 10px; color: #ffffff;">当天'+ title +'流量:</strong></td> ' +
                            '<td style="text-align: left; padding-left: 10px; color: #ffffff;"><strong>'+ params[0].value +'</strong></td> ' +
                            '</tr> ' +
                            '</table>' ;
                        return sortedValue;
                    }
                },
                enterable: true,  //echart版本过低,无法支持enterable
                padding: 15
            },
            toolbox: {
                show : true,
                feature : {
                    mark : {show: true},
                    dataView : {show: true, readOnly: false},
                    magicType : {show: true, type: ['line', 'bar']},
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            xAxis: [
                {
                    type: 'category',
                    data: data.dates,
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
                    }
                }
            ],
            series: series,
            legend: legend
        };

        if(dataType == 0){
            pvOptionCache = option;
        }else {
            uvOptionCache = option;
        }
        
        _echart.hideLoading();
        _echart.clear();
        _echart.setOption(option);
    }
}, '0.0.1', {
    requires: [
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
