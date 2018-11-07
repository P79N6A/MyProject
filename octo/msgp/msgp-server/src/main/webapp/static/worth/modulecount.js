M.add('msgp-worth/modulecount', function (Y) {
    var _report_style = "";
    var _echart = null;
    var chartTitle = "";
    var title = "";
    var levelType = "";
    var dataType = 0;
    var uvOptionCache;
    var pvOptionCache;
    var newSearch;
    var legend;
    var tableTitle;
    var names = [], values = [], defaultNames = [], defaultValues = [];
    Y.namespace('msgp.worth').modulecount = modulecount;
    function modulecount(report_style) {
        _report_style = report_style;
        document.title = '模块使用日趋势';
        setDefaultValue();
        initDatePicker();
        initCharts();
        bindSearch();
        bindTypeSelect();
        getChartData(dataType);
    }

    function setDefaultValue() {
        dataType = 0;
        title = "PV";
        defaultNames = [];
        defaultValues = [];
        $("#module option").each(function () {
            var name = $(this).text(); //获取单个text
            defaultNames.push(name);
            defaultValues.push('--');
        });
        defaultNames.splice($.inArray('所有',defaultNames),1);
        defaultValues.pop();
        names = defaultNames;
        values = defaultValues;
    }
    
    
    function bindSearch() {
        Y.one('#searchForm').delegate('click', function () {
            getChartData(dataType);
            newSearch = true;
        }, '#searchBtn');
    }

    function bindTypeSelect() {
        Y.one('#count_type_select_module').delegate('click', function () {
            Y.all('#count_type_select_module a').removeClass('btn-primary');
            this.addClass("btn-primary");
            dataType = parseInt(this.getAttribute('value'));
            changeDataType(dataType);
        }, "a")
    }

    function  changeDataType(dataType) {
        if(pvOptionCache == null || uvOptionCache == null || newSearch ){
            title = (dataType == 0) ? "PV" : "UV";
            resetTable();
            getChartData(dataType);
            newSearch = false;
        }else{
            if(dataType == 0) {
                title = "PV";
                resetTable();
                _echart.setOption(pvOptionCache);
            }else {
                title = "UV";
                resetTable();
                _echart.setOption(uvOptionCache);
            }
        }
    }

    function initCharts() {
        var node = Y.one("#modulecount_charts").getDOMNode();
        _echart = echarts.init(node);
    }

    function initDatePicker() {
        var now = new Date();
        var startInput = Y.one('#start');
        var endInput = Y.one('#end');
        var end_day = new Date(now - 24 * 60 * 60 * 1000);
        var start_day = new Date(end_day - 30 * 24 * 60 * 60 * 1000);
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

    function getChartData(dataType) {
        _echart.showLoading({
            text: "loading"
        });
        var start = Y.one("#start").get('value');
        var end = Y.one("#end").get('value');
        var module = Y.Lang.trim(Y.one('#module').get('value'));
        var searchData = {
            "start": start,
            "end": end,
            "module": module,
            "datatype": dataType
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/worth/daily/module';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.mcount.length == 0){
                            _echart.hideLoading();
                            _echart.clear();
                            Y.msgp.utils.msgpHeaderTip('info', '未产生数据', 3);
                        }else {
                            fillCharts(data);
                            resetTable();
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

    function sum(array) {
        Array.prototype.sum = function (){
            return this.reduce(function (partial, value){
                return partial + value;
            })
        };
        return array.sum()
    }

    function fillCharts(data) {
        levelType = "功能";
        defaultNames = [];
        defaultValues = [];
        var series = [];
        var legendData = [];
        var dataSize = data.mcount.length;
        if (dataSize == 1) {
            var item = data.mcount;
            chartTitle = item[0].module + "使用日趋势" + "(" + title + ")";
            var countDesc = item[0].countDesc;
            for (var key in countDesc) {
                if (countDesc.hasOwnProperty(key)) {
                    series.push({
                        name: key,
                        type: 'line',
                        symbol: 'none',
                        data: countDesc[key],
                        smooth: true,
                        sampling: 'average'
                    });
                    legendData.push({
                        icon: 'line',
                        name: key
                    });
                    defaultNames.push(key);
                    defaultValues.push('--')
                }
            }
        } else {
            levelType = "模块";
            Y.Array.each(data.mcount, function (item) {
                series.push({
                    name: item.module,
                    type: 'line',
                    symbol: 'none',
                    data: item.countModule,
                    smooth: true,
                    sampling: 'average'
                });
                legendData.push({
                    icon: 'line',
                    name: item.module
                });
                defaultNames.push(item.module);
                defaultValues.push('--')
            });
            chartTitle = "各模块使用日趋势" + "(" + title + ")";
        }
        legend = {
            data: legendData,
            x: 'center',
            y: 'bottom'
        };

        var option = {
            title: {
                show: true,
                text: chartTitle,
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
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true}
                }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    lineStyle: {
                        type: "dashed"
                    }
                },
                formatter: function (params) {
                    names = [];
                    values = [];
                    var length = params.length;
                    for (var i = 0; i < length; i++) {
                        names[i] = params[i].seriesName;
                        values[i] = params[i].value;
                    }
                    var date = params[0].name;
                    tableTitle = params[0].name + ", 星期" + "天一二三四五六".charAt(new Date(date).getDay());                    refreshTable();
                    var result =
                        '<table> ' +
                        '<caption style="color: #3fab99;">' + tableTitle +
                        '</caption>' +
                        '<tr> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + levelType + ' ' +  title +'流量分布 见表 ---></strong></td> ' +
                        '</tr> '+
                        '</table>';
                    return result;
                },
                padding: 15
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

    function resetTable() {
        var data = [];
        var length = defaultNames.length;
        for (var i = 0; i < length; i++) {
            data.push({name: defaultNames[i], value: defaultValues[i]})
        }
        Y.one("#tableTitle").setHTML(levelType + "当日使用详情");
        fillTable(data)
    }

    function  refreshTable() {
        var data = [];
        var length = names.length;
        for (var i = 0; i < length; i++) {
            data.push({name: names[i], value: values[i]})
        }
        data.sort(function(x, y){
            return x.value < y.value ? 1:-1;
        });
        Y.one("#tableTitle").setHTML(tableTitle);
        fillTable(data)
    }

    function fillTable(data) {
        var template =
            '<table id="count_list" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th><%=this.levelType %>名称</th> ' +
            '<th><%=this.title %>流量</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody id="span_list"> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<td style="width: 70%"><%=item.name %></td>' +
            '<td style="width: 30%"><%=item.value %></td>' +
            '</tr>' +
            '<% }); %>'+
            '</tbody> ' +
            '</table>';
        var micro = new Y.Template();
        var str = micro.render(template, {data : data, title: title, levelType: levelType});
        Y.one("#count_list").setHTML("");
        Y.one("#count_list").setHTML(str);
        Y.mt.widget.init();
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
