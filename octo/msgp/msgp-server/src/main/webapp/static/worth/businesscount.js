M.add('msgp-worth/businesscount', function (Y) {
    var _report_style = "";
    var _echart = null;
    var userCountDaily = [];
    var title = "";
    var chartTitle = "";
    var dataType = 0;
    var newSearch;
    var pvOptionCache, uvOptionCache;
    var legend;
    var tableTitle;
    var names = [], values = [], defaultNames = [], defaultValues = [];
    Y.namespace('msgp.worth').businesscount = businesscount;
    function businesscount(report_style) {
        _report_style = report_style;
        document.title = '部门使用日趋势';
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
        $("#business option").each(function () {
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
        Y.one('#count_type_select_business').delegate('click', function () {
            Y.all('#count_type_select_business a').removeClass('btn-primary');
            this.addClass("btn-primary");
            dataType = parseInt(this.getAttribute('value'));
            changeDataType(dataType);
        }, "a")
    }

    function  changeDataType(dataType) {
        if(pvOptionCache == null || uvOptionCache == null || newSearch){
            title = (dataType == 0) ? "PV" : "UV";
            getChartData(dataType);
            newSearch = false;
        }else{
            if(dataType == 0) {
                title = "PV";
                _echart.clear();
                resetTable();
                _echart.setOption(pvOptionCache);
            }else {
                title = "UV";
                _echart.clear();
                resetTable();
                _echart.setOption(uvOptionCache);
            }
        }
    }

    function initCharts() {
        var node = Y.one("#businesscount_charts").getDOMNode();
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

    function sum(array) {
        Array.prototype.sum = function (){
            return this.reduce(function (partial, value){
                return partial + value;
            })
        };
        return array.sum()
    }

    function getChartData(dataType) {
        _echart.showLoading({
            text: "loading"
        });
        var start = Y.one("#start").get('value');
        var end = Y.one("#end").get('value');
        var business = Y.Lang.trim(Y.one('#business').get('value'));
        var searchData = {
            "start": start,
            "end": end,
            "business": business,
            "datatype": dataType
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/worth/daily/business';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.bcount.length == 0){
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

    function fillCharts(data) {
        var series = [];
        var legendData = [];
        var dataSize = data.bcount.length;
        defaultNames = [];
        defaultValues = [];
        Y.Array.each(data.bcount, function (item) {
            series.push({
                name: item.business,
                type: 'line',
                symbol: 'none',
                data: item.countBusiness,
                smooth: true,
                sampling: 'average'
            });
            legendData.push({
                icon: 'line',
                name: item.business
            });
            defaultNames.push(item.business);
            defaultValues.push('--')
        });
        legend = {
            data: legendData,
            x: 'center',
            y: 'bottom'
        };
        chartTitle = "各部门使用日趋势" + "(" + title + ")";
        //设置前十名用户的访问统计数据
        if (dataSize == 1) {
            var item = data.bcount;
            chartTitle = item[0].business + "使用日趋势" + "(" + title + ")";
            var bcountuser = item[0].bcountuser;
            if(dataType == 0){ //PV情况下才清空之前的数据
                userCountDaily = [];
                Y.each(bcountuser, function (item) {
                    userCountDaily.push(item);
                });
            }
        }
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
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    lineStyle: {
                        type: "dashed"
                    }
                },
                enterable: true,
                formatter: function (params){
                    names = [];
                    values = [];
                    var length = params.length;
                    var date = params[0].name;
                    tableTitle = params[0].name + ", 星期" + "天一二三四五六".charAt(new Date(date).getDay());
                    if (length == 1) {
                        if(dataType == 0){
                            var user = userCountDaily[params[0].dataIndex];
                            names = user.username;
                            values = user.usercount;
                            var length = names.length;
                            if(length == 0){
                                names.push("--");
                                values.push('--');
                            }
                            refreshTable();
                            var result =
                                '<table> ' +
                                '<caption style="color: #3fab99;">' + tableTitle +
                                '</caption>' +
                                '<tr> ' +
                                '<td style="text-align: left; padding-left: 10px; color: #ffffff;">当天部门'+ title +'流量:</strong></td> ' +
                                '<td style="text-align: left; padding-left: 10px; color: #ffffff;"><strong>'+ sum(values) +'</strong></td> ' +
                                '</tr> '+
                                '</table>';
                            return result;

                        }else {
                            names.push(params[0].seriesName);
                            values.push(params[0].value);
                            refreshTable();
                            var result =
                                '<table> ' +
                                '<caption style="color: #3fab99;">' + tableTitle +
                                '</caption>' +
                                '<tr> ' +
                                '<td style="text-align: left; padding-left: 10px; color: #ffffff;">当天部门'+ title +'流量:</strong></td> ' +
                                '<td style="text-align: left; padding-left: 10px; color: #ffffff;"><strong>'+ params[0].value +'</strong></td> ' +
                                '</tr> '+
                                '</table>';
                            return result;
                        }

                    } else {
                        var length = params.length;
                        for (var i = 0; i < length; i++) {
                            names[i] = params[i].seriesName;
                            values[i] = params[i].value;
                        }
                        var result =
                            '<table> ' +
                            '<caption style="color: #3fab99;">' + tableTitle +
                            '</caption>' +
                            '<tr> ' +
                            '<td style="text-align: left; padding-left: 10px; color: #ffffff;">当天'+ title +'总流量:</strong></td> ' +
                            '<td style="text-align: left; padding-left: 10px; color: #ffffff;"><strong>'+ sum(values) +'</strong></td> ' +
                            '</tr> '+
                            '</table>';
                        refreshTable();
                        return result;
                    }
                },
                padding: 15
            },
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
        Y.one("#tableTitle").setHTML("部门当日使用详情");
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
            '<th>部门/用户</th> ' +
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
        var str = micro.render(template, {data : data, title: title});
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
