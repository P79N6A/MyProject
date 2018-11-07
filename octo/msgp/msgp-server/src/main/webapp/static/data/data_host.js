M.add('msgp-data/data_host', function (Y) {
    Y.namespace('msgp.data').data_host = data_host;
    var appkey;
    var startInput, endInput;
    var sdate, edate;
    var hostWrapper;
    var listWrapper;
    var chartsData;
    var lastTitleKey;
    var previous = null;
    var keyResverd = ['spanname', 'localhost'];
    var _idc, _localhost, _spanname, _idcLocalHosts = {hostnames: "*", hosts: "*"};
    var localhostMap = {}, idcHostMap = {};
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
    // save dialog object to reuse
    var compareDialog;
    // save last xAxis for charts compare dialog
    var lastXAxis;
    // save for charts compare dialog
    var lastLegend = [];
    var compareData = [];

    // save can't search dialog to reuse
    var searching = false;

    function data_host(_appkey, _isFirstIn) {
        appkey = _appkey;
        document.title = "主机分析";
        if (!_isFirstIn) {
            return;
        }

        initNodes();
        initDatePicker();
        initIdcAndHost();
        initParams();
        initEvent();
    }

    function initNodes() {
        hostWrapper = Y.one('#div_data_host');
        startInput = hostWrapper.one('#start_time');
        endInput = hostWrapper.one('#end_time');
        listWrapper = hostWrapper.one('.host_kpi_list');
        compareWrap = hostWrapper.one('#compare_wrap');
        Y.msgp.service.setEnvText('host_env_select');
    }

    function initIdcAndHost() {
        var se = getStartEnd();
        if (!se) return;
        var url = '/data/idc_host';
        var env = hostWrapper.one("#host_env_select a.btn-primary").getAttribute('value');
        var requestPara = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env
        };
        Y.io(url, {
            method: 'get',
            data: requestPara,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var tagsData = ret.data;
                        setTags(tagsData);
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

    /**
     * 设置接口和主机的数据来源
     * @param tagsData
     */
    function setTags(tagsData) {
        var spannameList = tagsData.spannames;
        var idc2Host = tagsData.idcLocalHosts;
        var hostDescList = tagsData.localHosts;

        //设置接口列表
        $("#spanname").autocomplete({
            source: spannameList,
            minLength: 0,
            width: '290px'
        });

        //建立 ip->hostname的映射关系
        localhostMap = [];
        if (hostDescList) {
            Y.Array.forEach(hostDescList, function (item) {
                localhostMap[item.ip] = item.name;
            });
        }

        //建立 idc->hostname的映射关系
        var idcList = [];
        //清空
        idcHostMap = [];
        for (var idc in idc2Host) {
            if (idc2Host.hasOwnProperty(idc)) {
                var hosts = idc2Host[idc];
                var hostnames = [];
                Y.Array.each(hosts, function (host) {
                    if (host != '*' && host != 'all') {
                        var hostname = localhostMap[host];
                        hostnames.push(hostname)
                    } else {
                        hostnames.push(host)
                    }
                });
                idcHostMap[idc] = {hostnames: hostnames, hosts: hosts};
                idcList.push(idc);
            }
        }

        //设置idc列表
        hostWrapper.one('#host_idc_select').empty();
        Y.Array.forEach(idcList, function (item) {
            hostWrapper.one('#host_idc_select').append('<option value=' + item + '>' + item + '</option>');
        });

        //设置主机列表
        $("#localhost").autocomplete({
            minLength: 0,
            source: idcHostMap['all'].hostnames,
            width: '310px'
        }).focus(function () {
            $(this).autocomplete("search");
        });


        //设置默认值
        hostWrapper.one('#host_idc_select').set('value', 'all');
        hostWrapper.one('#localhost').set('value', '*');
        hostWrapper.one('#spanname').set('value', 'all');
    }

    function initParams() {
        _idc = hostWrapper.one('#host_idc_select').get('value');
        _idcLocalHosts = idcHostMap[_idc];
        var hostname = hostWrapper.one("#localhost").get("value");
        var spanname = hostWrapper.one("#spanname").get("value");

        _localhost = hostname.substring(hostname.indexOf("(") + 1, hostname.indexOf(")"));
        if (_localhost == "") {
            _localhost = hostname;
        }
        _spanname = spanname ? spanname : "all"
    }

    function initEvent() {
        hostWrapper.one('#query_btn').delegate('click', function () {
            if (searching) return;
            hostWrapper.all("#query_btn .btn").removeClass("btn-primary");
            this.addClass("btn-primary");
            initParams();
            this.hasClass("chart") ? getShowData(true, listWrapper) : getGraphsData();
        }, ".btn");

        hostWrapper.one("#host_env_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            hostWrapper.all('#host_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
        }, "a");

        hostWrapper.one('#host_idc_select').on('change', function () {
            //设置idc的autocompele
            var idc = Y.one("#host_idc_select option:checked").get("text");
            $("#localhost").autocomplete("option", "source", idcHostMap[idc].hostnames);
            hostWrapper.one('#localhost').set('value', '*');
            initParams();
        });

        listWrapper.delegate('click', function () {
            var now = new Date();
            if (!previous || (now - previous > 300)) {
                var _id = $(this).attr('id');
                if ($("." + _id).length) {
                    $("." + _id).remove();
                } else {
                    $("#showGraph").remove();
                    var str = '<tr id="showGraph" class=' + _id + '><td colspan="8" style="text-align: center;"><div id="charts_outer" class="clearfix" style="display: inline-block;"></div></td></tr>';
                    $(".widget-table-choose").after(str);
                    var wrapper = Y.one("#charts_outer");
                    _spanname = $(this).attr('data-spanname');
                    var hostname = $(this).attr('data-localhost');
                    _localhost = hostname.substring(hostname.indexOf("(") + 1, hostname.indexOf(")"));
                    if (_localhost == "") {
                        _localhost = hostname;
                    }
                    getShowData(false, wrapper);
                }
                previous = now;
            }
        }, '.day_graph_click');


        Y.one('#compare_button').on('click', function () {
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
        Y.one("#query_btn .btn-primary").hasClass("chart") ? getShowData(true, listWrapper) : getGraphsData();
    }


    function fillList(data) {
        var micro = new Y.Template();
        var n = data.length;
        for (var i = 0; i < n; i++) {
            var kpiData = data[i];
            kpiData.tags.localhost =
                localhostMap[kpiData.tags.localhost] ? localhostMap[kpiData.tags.localhost] : kpiData.tags.localhost;
        }
        var str = micro.render(template(), {data: data});
        Y.one(".host_kpi_list").setHTML(str);
        Y.mt.widget.init();
    }

    function getShowData(merge, node) {
        var se = getStartEnd();
        if (!se) return;
        var hosts = _idcLocalHosts == undefined || (_idcLocalHosts != undefined && _idcLocalHosts.hosts == undefined) ? "*" : _idcLocalHosts.hosts
        var env = Y.one("#host_env_select a.btn-primary").getAttribute('value');
        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            role: 'server',
            idc: _idc || '',
            localhost: _localhost || '*',
            idcLocalHosts: hosts.toString() || '*',
            spanname: _spanname,
            group: 'spanLocalhost',
            merge: merge
        };
        var eq = checkData(data);
        if (eq) return;
        showWaitMsg(node);
        var opt = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            idc: _idc || '',
            localhost: _localhost,
            spanname: _spanname || '',
            merge: merge
        };

        Y.msgp.utils.urlAddParameters(opt);
        var conf = {
            method: 'POST',
            data: data,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            if (node === listWrapper) {
                                fillList(arr);
                            } else if (!merge) {
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
                }
            }
        };
        doAjax(merge, conf);
    }

    function getGraphsData() {
        var se = getStartEnd();
        if (!se) return;
        var hosts = _idcLocalHosts == undefined || (_idcLocalHosts != undefined && _idcLocalHosts.hosts == undefined) ? "*" : _idcLocalHosts.hosts
        var env = Y.one("#host_env_select a.btn-primary").getAttribute('value');
        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            role: 'server',
            idc: _idc || '',
            localhost: _localhost || '*',
            idcLocalHosts: hosts.toString() || '',
            spanname: _spanname,
            group: 'spanLocalhost'
        };
        var eq = checkData(data);
        if (eq) return;
        showWaitMsg(listWrapper);
        var opt = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            idc: _idc || '',
            localhost: _localhost,
            spanname: _spanname || '',
            merge: false
        };

        Y.msgp.utils.urlAddParameters(opt);
        var conf = {
            method: 'post',
            data: data,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            chartsData = arr;
                            fillGraphs();
                        } else {
                            showEmptyErrorMsg(listWrapper, 0);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(listWrapper, !0);
                    }
                },
                failure: function () {
                    searching = false;
                    showEmptyErrorMsg(listWrapper, !0);
                }
            }
        };
        doAjax(false, conf);
    }

    function checkData(data) {
        if (data['localhost'] === '*' && data['spanname'] === '*') {
            Y.msgp.utils.msgpHeaderTip('error', '不能同时选择所有主机和所有接口', 3);
            return true;
        } else {
            return false;
        }
    }

    function fillCharts() {
        var wrapper = Y.one("#charts_outer");
        if (wrapper) {
            clearWaitMsg(wrapper);
            insertChartsWrapper(2);

        }
        // delay charts init
        setTimeout(function () {
            var nds = hostWrapper.all('.charts-wrapper-out');
            if (nds) {
                nds.item(0) && fillChartsReal(nds.item(0), chartsData[0], 0);
                nds.item(1) && fillCountReal(nds.item(1), chartsData[0], 1);
            }
        }, 100);
    }

    function fillGraphs() {
        clearWaitMsg(listWrapper);
        insertGraphsWrapper(chartsData.length);
        // delay charts init
        setTimeout(function () {
            var nds = hostWrapper.all('.charts-wrapper-out');
            for (var i = 0, l = chartsData.length; i < l; i++) {
                fillGraphsReal(nds.item(i), chartsData[i], i);
            }
        }, 100);
    }

    function fillChartsReal(dom, data, index) {
        var opt = wrapOption(data);
        var title = '';
        title = data.tags[lastTitleKey];
        var inner = dom.one('.charts-wrapper').getDOMNode();
        var ec = echarts.init(inner);
        ec.setOption(opt);
        dom.setData('chartData', ec.getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
    }

    function fillGraphsReal(dom, data, index) {
        var opt = wrapOption(data);
        var title = '';
        var tmpArr = [];
        Y.Array.each(keyResverd, function (item, index) {
            if ('localhost' === item) {
                var localhost = data.tags[item];
                var hostname = localhostMap[localhost];
                hostname = hostname || localhost;
                data.tags[item] = hostname;
            }
            tmpArr.push(item + ':' + data.tags[item]);
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
            }]

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
        var wrapper = Y.one('#charts_outer');
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
        if (s == "") {
            var start = new Date(now - 63 * 60 * 1000);
            startInput.set('value', Y.mt.date.formatDateByString(start, 'yyyy-MM-dd hh:mm:00'));
        }

        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });
        var e = endInput.get('value');
        if (e == "") {
            var end = new Date(now - 3 * 60 * 1000);
            endInput.set('value', Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:00'));
        }
        sdate.on('Datepicker.select', function () {
            initIdcAndHost();
            initParams()
        });
        edate.on('Datepicker.select', function () {
            initIdcAndHost();
            initParams()
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

    function doAjax(merge, conf) {
        searching = true;
        var url = '/data/host';
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
            data: [{name: '50%耗时', icon: 'line'}, {name: '90%耗时', icon: 'line'}, {
                name: '99%耗时',
                icon: 'line'
            }, {name: 'QPS', icon: 'line'}]
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
            return (num / (1000 * 60)).toFixed(2) + 'min';
        } else if (num >= 1000000000) {
            return (num / (1000 * 60 * 60)).toFixed(2) + 'h';
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
                if (typeof(y) == "undefined" || y == null) {
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
        Y.msgp.service.setEnvText('host_env_select');
        return '<table id="kpi_table" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th>主机</th>' +
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
            '<td style="white-space: nowrap; overflow: hidden;width: 20%"><%= item.tags.localhost %></td>' +
            '<td style="white-space: nowrap; overflow: hidden;width: 19%"><%= item.tags.spanname %></td>' +
            '<td style="width: 12%"><%= item.count %></td>' +
            '<td style="width: 12%"><%= item.qps %></td>' +
            '<td style="width: 10%"><%= item.tp50 %></td>' +
            '<td style="width: 10%"><%= item.tp90 %></td>' +
            '<td style="width: 10%"><%= item.tp99 %></td>' +
            '<td style="width: 7%"> <a class="day_graph_click" style="width:20px" href="javascript:void(0);" data-localhost="<%= item.tags.localhost %>" data-spanname="<%= item.tags.spanname %>" title="显示趋势图"> <img src="data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCA1MTIgNTEyIiB3aWR0aD0iMjRweCIgaGVpZ2h0PSIyNHB4Ij4KICA8Zz4KICAgIDxnPgogICAgICA8cG9seWdvbiBwb2ludHM9IjE2Ni4zLDI4Ni4yIDI1MS44LDM3Mi44IDQxMi4yLDIxNC4zIDQxMS4zLDMxNC40IDQzMi4yLDMxNC40IDQzMy4yLDE3Ny44IDI5Ny43LDE3Ni44IDI5Ny43LDE5Ny42IDM5OC45LDE5OC41ICAgICAyNTIuOSwzNDMuNiAxNjYuMywyNTcgMzAuOCwzOTUuNyA0NS40LDQxMC4zICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICAgIDxwb2x5Z29uIHBvaW50cz0iNDgwLjEsMTEgNDgwLjEsNDgwLjEgMTEsNDgwLjEgMTEsNTAxIDUwMSw1MDEgNTAxLDExICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K" /> </a></td>' +
            '</tr>' +
            '<% }); %>' +
            '</tbody> ' +
            '</table>';
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


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete',
        'msgp-utils/common',
        'template',
        'msgp-service/commonMap'
    ]
});
