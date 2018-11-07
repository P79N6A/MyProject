M.add('msgp-data/data_performance_span', function (Y) {
    Y.namespace('msgp.data').data_performance_span = data_performance_span;

    var appkey;
    var hourType = true;
    var _type, _unit;//day
    var end;//结束时间
    var chartsData;
    var _minute = 180;//分钟粒度的获取最近3小时
    var data_type = [{"value": "qps", "cvalue": "compare_qps", "name": "QPS"},
        {"value": "tp50", "cvalue": "compare_tp50", "name": "50%耗时"}];

    var data_desc = [{date: "", desc: "环比"}, {date: "", desc: "同比"}];
    var dayInput;
    var hourInput;
    var wrapper;
    var chatrsTemplate;

    function data_performance_span(_appkey, spannamesParam, type, unit) {
        appkey = _appkey;
        _type = type;
        _unit = unit;
        document.title = "性能指标-接口详情";

        initNodes();
        initEvent();
        initDom(type, unit);
        initDatePicker(false);
        autoCompleteList(spannamesParam);
        getChartsData();
    }

    function initNodes() {
        dayInput = Y.one('#day');
        hourInput = Y.one('#hour');
        wrapper = Y.one('#charts_outer');
        chatrsTemplate = [
            '<div class="charts-wrapper-out">',
            '<p></p>',
            '<div class="charts-wrapper"></div>',
            '</div>'
        ].join('');
    }

    function initEvent() {
        Y.one("#supplier_env_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            Y.all('#supplier_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getSpannamesAndServer();
        }, "a");

        Y.one('#day_query_btn').on('click', function () {
            _type = 0;
            getChartsData();
            Y.all('#day_week_btn a').removeClass('btn-primary');
            this.addClass("btn-primary");
        });

        Y.one('#week_query_btn').on('click', function () {
            _type = 1;
            getChartsData();
            Y.all('#day_week_btn a').removeClass('btn-primary');
            this.addClass("btn-primary");
        });

        Y.one('#unit_switch_minute').on('click', function () {
            dayInput.hide()
            hourInput.show()
            hourType = false;
        });
        Y.one('#unit_switch_hour').on('click', function () {
            dayInput.show();
            hourInput.hide();
            hourType = true;
        });
    }

    function initDom(type, unit){
        if(unit=="Hour"){
            hourType = true;
        }else{
            hourType = false;
        }
        Y.all('#day_week_btn a').removeClass('btn-primary');
        if(type==1){
            Y.one('#week_query_btn').addClass("btn-primary");
        }else{
            Y.one('#day_query_btn').addClass("btn-primary");
        }
    }

    function initDatePicker() {
        sdate = new Y.mt.widget.Datepicker({
            node: dayInput,
            showSetTime: false
        });

        hourDate = new Y.mt.widget.Datepicker({
            node: hourInput,
            showSetTime: true
        });
    }

    function autoCompleteList(obj) {
        new Y.mt.widget.AutoCompleteList({
            id: "spanSelect_auto",
            node: Y.one("#spanSelect"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            more: ""
        });
        Y.one("#spanSelect_auto").one(".widget-autocomplete-complete-list").setStyle("height", "200px");
        Y.one("#spanSelect_auto").one(".widget-autocomplete-tip").setHTML("输入接口名搜索或向下滚动选择");
        Y.one("#spanSelect_auto").one(".widget-autocomplete-menu-operator").remove();
    }

    /**
     * @param type 0:环比,1:同比
     */
    function getChartsData() {
        var yesterday, weekday, start;
        if (Y.one('#unit_switch_minute:checked')) {
            _unit = "Minute";
            hourType = false;
            var hourDay = hourInput.get("value");
            if(hourDay.length==0){
                Y.msgp.utils.msgpHeaderTip('error', '请选择日期！', 3);
                return;
            }
            var date = new Date(Date.parse(hourDay.replace(/-/g, "/")));
            var start_time = new Date(date.getTime() - _minute*60 * 1000);
            end = new Date(date.getTime());
            yesterday = Y.mt.date.formatDateByString(new Date((date.getTime() - 86400000)), 'yyyy-MM-dd');
            weekday = Y.mt.date.formatDateByString(new Date((date.getTime() - 7 * 86400000)), 'yyyy-MM-dd');
            data_desc[0].date = yesterday;
            data_desc[1].date = weekday;
            start = Y.mt.date.formatDateByString(start_time, 'yyyy-MM-dd hh:mm:ss');
            end = Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:ss');
        } else {
            _unit = 'Hour';
            hourType = true;
            var day = dayInput.get("value");
            if(day.length==0){
                Y.msgp.utils.msgpHeaderTip('error', '请选择日期！', 3);
                return;
            }
            var date = new Date(Date.parse(day.replace(/-/g, "/")));
            var now = new Date();
            start = day + " 00:00:00";
            end = new Date(now.getTime() - (3599) * 1000);
            if ((now.getTime() - date.getTime()) > 86400000) {
                end = new Date(date.getTime() + (86400 - 1) * 1000);
            }
            yesterday = Y.mt.date.formatDateByString(new Date((end.getTime() - 86400000)), 'yyyy-MM-dd');
            weekday = Y.mt.date.formatDateByString(new Date((end.getTime() - 7 * 86400000)), 'yyyy-MM-dd');
            end = Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:ss');
            data_desc[0].date = yesterday;
            data_desc[1].date = weekday;
        }


        var data = {
            appkey: appkey,
            start: start,
            end: end,
            env: Y.one("#supplier_env_select a.btn-primary").getAttribute('value'),
            source: 'server',
            group: 'span',
            localhost: "all",
            type: _type,
            unit: _unit,
            spanname: Y.one("#spanSelect").get('value')
        };

        var conf = {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            chartsData = arr;

                            if (!hourType) {
                                for (var i = 0, l = chartsData.length; i < l; i++) {
                                    var chartData = chartsData[i];
                                    for (var i = 0, l = data_type.length; i < l; i++) {
                                        chartData[data_type[i].value] = getHourMinuteData(chartData[data_type[i].value])
                                        chartData[data_type[i].cvalue] = getHourMinuteData(chartData[data_type[i].cvalue])
                                    }

                                }
                            }
                            fillCharts();
                        } else {
                            showEmptyErrorMsg();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    searching = false;
                    showEmptyErrorMsg(!0);
                }
            }
        };
        doAjax(conf);
    }

    function getHourMinuteData(arr_data) {
        var new_Data = [];
        for (var i = 0, l = arr_data.length; i < l; i++) {
            var charObj = arr_data[i];
            var x = charObj.x;
            var time = new Date(parseInt(x) * 1000)
            var hour = time.getHours();
            var minutes = time.getMinutes();
            charObj.x = hour + ":" + minutes;
            new_Data.push(charObj);
        }
        return new_Data;
    }

    function fillCharts() {
        clearWaitMsg();
        insertChartsWrapper(chartsData.length * 2);
        // delay charts init
        setTimeout(function () {
            var nds = Y.all('.charts-wrapper-out');
            for (var i = 0, l = chartsData.length; i < l; i++) {
                fillChartsReal(nds, chartsData[i], i);
            }
        }, 100);
    }
    function insertChartsWrapper(n) {
        var node;
        for (var i = 0; i < n; i++) {
            if (node && node instanceof Y.Node) {
                node = Y.all(node).concat(Y.Node.create(chatrsTemplate));
            } else if (node instanceof Y.NodeList) {
                node = node.concat(Y.Node.create(chatrsTemplate));
            } else {
                node = Y.Node.create(chatrsTemplate);
            }
        }
        wrapper.append(node);
    }

    function clearWaitMsg() {
        wrapper.setHTML('');
    }

    function showEmptyErrorMsg(isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        wrapper.setHTML(html);
    }

    function doAjax(conf) {
        searching = true;
        var url = '/data/daily_compared';
        Y.io(url, conf);
    }

    /**
     * 填充2次
     * 一次QPS, 一次top90
     */
    function fillChartsReal(doms, data, position) {
        for (var i = 0; i < 2; i++) {
            var index = position * 2 + i;
            var dom = doms.item(index);
            var opt = wrapOption(data, i);
            var title = data.tags['spanname'] + "&nbsp;&nbsp;" + data_type[i].name + data_desc[_type].desc;
            dom.one('p').setHTML(title);
            var inner = dom.one('.charts-wrapper').getDOMNode();
            var ec = echarts.init(inner);
            ec.setOption(opt);
            dom.setData('chartData', ec.getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
        }
    }

    function getSpannamesAndServer() {
        Y.one("#spanSelect").set('value', "");
        var env = Y.one("#supplier_env_select a.btn-primary").getAttribute('value');
        var data = {
            appkey: appkey,
            env: env,
            source: 'server'
        };
        var conf = {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var retData = ret.data;
                        autoCompleteList(retData.spannames);
                    } else {
                        autoCompleteList([], []);
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    autoCompleteList([]);
                    searching = false;
                    showEmptyErrorMsg(!0);
                }
            }
        };
        var url = '/data/tags';
        Y.io(url, conf);
    }


    /**
     *
     * @param obj 数据对象
     * @param _type 比较对象 ,0:环比, 1:同比
     * @param dtype  数据类型,0:qps, 1:tp90
     */
    function wrapOption(obj, dtype) {
        var day = "";
        if (hourType) {
            day = dayInput.get("value");
        } else {
            var hour = hourInput.get("value");
            var date = new Date(Date.parse(hour.replace(/-/g, "/")));
            day = Y.mt.date.formatDateByString(date, 'yyyy-MM-dd');
        }
        var endTime = new Date(Date.parse(end.replace(/-/g, "/")));
        var xAxis = getXAxis(endTime);
        var compare_day = data_desc[_type].date;
        var data_obj = data_type[dtype];
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
                data: [day, compare_day]
            },
            xAxis: [
                {
                    type: 'category',
                    data: xAxis
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: data_obj.name
                }
            ],
            series: [
                {
                    name: compare_day,
                    type: 'line',
                    smooth: true,
                    data: getData(xAxis, obj[data_obj.cvalue], 3)
                },
                {
                    name: day,
                    type: 'line',
                    smooth: true,
                    data: getData(xAxis, obj[data_obj.value], 3)
                }
            ]
        };

        return option;
    }

    function getXAxis(endTime) {
        var result = [];
        if (!hourType) {
            for (var i = _minute; i > 0; i--) {
                var time = new Date(endTime.getTime() - i * 60 * 1000);
                var hour = time.getHours();
                var minutes = time.getMinutes();
                result.push(hour + ":" + minutes);
            }
        } else {
            var hour = endTime.getHours();
            for (var i = 0; i <= hour; i++) {
                result.push(i);
            }
        }
        return result;
    }

    //指定小时的 x周坐标
    function getData(xAxis, arr_data, precision) {
        var result = [];
        if (arr_data.length < 1) {
            return result;
        }
        var length = xAxis.length;
        for (var i = 0; i < length; i++) {
            result[i] = "-";
        }
        for (var i = 0; i < length; i++) {
            for (var j = 0; j < arr_data.length; j++) {
                if (xAxis[i] == arr_data[j].x) {
                    mathed = true;
                    var data = NaN;
                    if (precision) {
                        var y = arr_data[j].y;
                        if (typeof(y) != "undefined") {
                            data = (y).toFixed(2);
                        }
                    } else {
                        data = arr_data[j].y;
                    }
                    result[i] = data
                    break;
                }
            }

        }
        return result;
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete'
    ]
});