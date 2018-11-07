M.add('msgp-data/data_secondLevel', function (Y) {
    Y.namespace('msgp.data').data_secondLevel = data_secondLevel;
    var appkey;
    var startInput, endInput;
    var sdate, edate;
    var secondLevelWrapper;
    var listWrapper;
    var chartsData;
    var _localhost, _idcLocalHosts = {hostnames: "*", hosts: "*"};
    var ip2Host_IP = {}, idcHostMap = {};
    var compareWrap;

    var graphsTemplate = [
        /* jshint indent: false */
        '<div class="charts-wrapper-out">',
        '<p style="word-break: break-all; overflow: hidden;width: 500px"></p>',
        '<div class="charts-wrapper" style="border: 0"></div>',
        '</div>'
    ].join('');

    // save can't search dialog to reuse
    var searching = false;

    function data_secondLevel(_appkey, _isFirstIn) {
        appkey = _appkey;
        document.title = "秒级指标";
        if (!_isFirstIn) {
            return;
        }

        initNodes();
        initDatePicker();
        initIdcAndHost();
        //确保主机获取并填充
        setTimeout(function(){
            initParams();
            initEvent();
            getGraphsData();
        },2000);

    }

    function initNodes() {
        secondLevelWrapper = Y.one('#div_data_secondLevel');
        startInput = secondLevelWrapper.one('#start_time');
        endInput = secondLevelWrapper.one('#end_time');
        listWrapper = secondLevelWrapper.one('.secondLevel_kpi_list');
        compareWrap = secondLevelWrapper.one('#compare_wrap');
        Y.msgp.service.setEnvText('secondLevel_env_select');
    }

    function initIdcAndHost() {
        var se = getStartEnd();
        if (!se) return;
        var url = '/data/idc_host';
        var env = secondLevelWrapper.one("#secondLevel_env_select a.btn-primary").getAttribute('value');
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
     * 设置主机的数据来源
     * @param tagsData
     */
    function setTags(tagsData) {
        // 其中idc中有'all'-> 包含【ip1, ip2, .., ipn, '*', 'all'】
        var idc2IPs = tagsData.idcLocalHosts;
        var hostDescList = tagsData.localHosts;


        //建立 ip->hostname(ip)的映射关系
        ip2Host_IP = [];
        if (hostDescList) {
            Y.Array.forEach(hostDescList, function (item) {
                ip2Host_IP[item.ip] = item.name;
            });
        }

        // 建立 idc->hostname的映射关系
        //清空
        idcHostMap = [];
        for (var idc in idc2IPs) {
            if (idc2IPs.hasOwnProperty(idc)) {
                var ips = idc2IPs[idc];
                var hostnames = [];
                Y.Array.each(ips, function (ip) {
                    if(ip != "external") {
                        if (ip != '*' && ip != 'all') {
                            var hostname = ip2Host_IP[ip];
                            hostnames.push(hostname)
                        } else {
                            hostnames.push(ip)
                        }
                    }
                });
                idcHostMap[idc] = {hostnames: hostnames, hosts: ips};
            }
        }


        //设置主机列表
        $("#localhost").autocomplete({
            minLength: 0,
            source: idcHostMap['all'].hostnames,
            width: '310px'
        }).focus(function () {
            $(this).autocomplete("search");
        });

        //设置默认值
        secondLevelWrapper.one('#localhost').set('value', 'all');
    }

    function initParams() {
        _idcLocalHosts = idcHostMap['all'];
        var hostname = secondLevelWrapper.one("#localhost").get("value");

        _localhost = hostname.substring(hostname.indexOf("(") + 1, hostname.indexOf(")"));
        if (_localhost == "") {
            _localhost = hostname;
        }
    }

    function initEvent() {
        secondLevelWrapper.one('#query_btn').delegate('click', function () {
            if (searching) return;
            initParams();
            getGraphsData();

        }, ".btn");

        secondLevelWrapper.one("#secondLevel_env_select").delegate('click', function () {
            var el = this;
            //if (el.hasClass('btn-primary')) return;
            secondLevelWrapper.all('#secondLevel_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");

            initIdcAndHost();
            setTimeout(function(){
                initParams();
               // getGraphsData();
            },2000);
        }, "a");

    }



    function getGraphsData() {
        var se = getStartEnd();
        if (!se) return;
        var env = Y.one("#secondLevel_env_select a.btn-primary").getAttribute('value');

        var data = {
            appkey: appkey,
            start: se.start,
            end: se.end,
            env: env,
            ip: _localhost || 'all'
        }

        showWaitMsg(listWrapper);

        var conf = {
            method: 'post',
            data: data,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr != null && arr.length !== 0) {
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


    function fillGraphs() {
        clearWaitMsg(listWrapper);
        insertGraphsWrapper(chartsData.length);
        // delay charts init
        setTimeout(function () {
            var nds = secondLevelWrapper.all('.charts-wrapper-out');
            for (var i = 0, l = chartsData.length; i < l; i++) {
                fillGraphsReal(nds.item(i), chartsData[i], i);
            }
        }, 100);
    }



    function fillGraphsReal(dom, data, index) {
        var opt = wrapOption(data);
        var title = '';
        if(data.ip != "all") {
            var localhost = data.ip;
            var hostname = ip2Host_IP[localhost];
            hostname = hostname || hostname;
            title += 'localhost' + ': ' + hostname;
        } else {
            title = "all"
        }

        dom.one('p').setHTML(title);
        var inner = dom.one('.charts-wrapper').getDOMNode();
        var ec = echarts.init(inner);
        ec.setOption(opt);
        dom.setData('chartData', ec.getOption().series).setData('chartTitle', title).setAttribute('dindex', index);
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
            var start = new Date(now - 4 * 60 * 1000);
            startInput.set('value', Y.mt.date.formatDateByString(start, 'yyyy-MM-dd hh:mm:00'));
        }

        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });
        var e = endInput.get('value');
        if (e == "") {
            var end = new Date(now);
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

        var now = new Date();
        var before = new Date(now - 5*60*1000);
        var formateBefore = Y.mt.date.formatDateByString(before, 'yyyy-MM-dd hh:mm:ss');

        if(obj.start < formateBefore) {
            Y.msgp.utils.msgpHeaderTip('error', '请查询5分钟内数据', 3);
            return null;
        }

        return obj;
    }

    function doAjax(merge, conf) {
        searching = true;
        var url = '/data/second';
        Y.io(url, conf);
    }

    function wrapOption(obj) {
        var series = [
            {
                name: 'QPS',
                type: 'line',
                symbol: 'none',
                smooth: true,
                data: getData(obj.qpsList, 2)
            }
        ];

        var legend = {
            bottom: 0,
            data: [{name: 'QPS', icon: 'line'}]
        };

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
                    data: getXAxis(obj.start, obj.end),
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: [
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

    function getXAxis(start, end) {
        var result = [];
        for (var i = start; i <= end; i++) {
            result.push(Y.mt.date.formatDateByString(new Date(i * 1000), 'yyyy-MM-dd hh:mm:ss'));
        }
        return result;
    }

    function getData(list, precision) {
        var result = [];
        for (var i = 0, l = list.length; i < l; i++) {
            if (precision) {
                var y = list[i];
                if (typeof(y) == "undefined" || y == null) {
                    result.push(NaN);
                } else {
                    result.push((y).toFixed(2));

                }
            } else {
                result.push(list[i]);
            }
        }
        return result;
    }

    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node.setHTML(html);
       // compareWrap.addClass('hidden');
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
