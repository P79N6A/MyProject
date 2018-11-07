M.add('msgp-log', function (Y) {
    Y.namespace("msgp.log");

    // feed
    var _feedTimeoutHandle;
    var _feedCurrentId = 0;
    var _feedMinTimeInterval = 3000; //ms
    var _feedTimeInterval = _feedMinTimeInterval;    //ms
    var _feedColors = new Array('red', 'blue');
    var _feedColorIndex = 0;

    // filter
    var _currentSelectFilterId = null;
    var _currentSelectEnabledFilter = false;
    var _enabledFilterIds = [];
    var _disabledFilterIds = [];

    var pbody = Y.one('#paginator_supplier');

    var appkey;
    var host;
    var pst, pet;
    var isSetService;

    var startInput = Y.one('#start_time'),
        endInput = Y.one('#end_time');

    var listHtml = '<ul id="log-list" class="unstyled">' +
        '</ul>' +
        '<div id="paginator_supplier"></div>';

    var getSetCountUrl = "/log/group/by/set";
    var getHostCountUrl = "/log/group/by/host";
    var getFilterCountUrl = "/log/group/by/filter";
    var getTimeCountUrl = "/log/group/by/time";
    var getErrorDetailUrl = "/log/get/error";

    function initDatePicker() {
        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });

        sdate.on('Datepicker.select', function () {
            $('#exception').val('');
            changeHostSet();
        });

        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });

        edate.on('Datepicker.select', function () {
            $('#exception').val('');
            changeHostSet();
        });
    }

    function initTimeLine() {
        Y.all("#timeline a").on('click', function (e) {
            var now = new Date();
            var end = now.getTime();
            var start = end - 60 * 60 * 1000;
            var time = e.target.getData('value');
            switch (time) {
                case 'today':
                    start = getStartOfDay(now).getTime();
                    break;
                case 'one_hour_before':
                    //default
                    break;
                case 'six_hour_before':
                    start = end - 6 * 60 * 60 * 1000;
                    break;
                case 'twenty_four_hour_before':
                    start = end - 24 * 60 * 60 * 1000;
                    break;
                case 'yesterday':
                    end = getStartOfDay(now).getTime();
                    start = end - 1000 * 60 * 60 * 24;
                    break;
                case 'minus_one_day':
                    var startOfToday = getStartOfDay(now).getTime();
                    var endDate = new Date(endInput.get('value'));
                    var startOfEndDate = getStartOfDay(endDate).getTime();
                    if (startOfToday >= startOfEndDate) {
                        end = startOfEndDate - 1000 * 60 * 60 * 24;
                    } else {
                        end = startOfToday;
                    }
                    start = end - 1000 * 60 * 60 * 24;
                    break;
                case 'plus_one_day':
                    var startOfToday = getStartOfDay(now).getTime();
                    var endDate = new Date(endInput.get('value'));
                    var startOfEndDate = getStartOfDay(endDate).getTime();
                    if (startOfEndDate >= startOfToday) {
                        end = startOfToday;
                    } else {
                        end = startOfEndDate + 1000 * 60 * 60 * 24;
                    }
                    start = end - 1000 * 60 * 60 * 24;
                    break;
                default:
                    break;
            }
            startInput.set('value', Y.mt.date.formatDateByString(new Date(start), 'yyyy-MM-dd hh:mm:ss'));
            endInput.set('value', Y.mt.date.formatDateByString(new Date(end), 'yyyy-MM-dd hh:mm:ss'));
            $('#exception').val('');
            changeHostSet();
        })
    }

    function getStartOfDay(date) {
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        date.setMilliseconds(0);
        return date
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
        if (new Date(s) > new Date(e)) {
            Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
            return null;
        }
        return obj;

    }

    function refreshPaginator(pobj) {
        var totalCountInfo = pobj.totalCount || 1;
        if (parseInt(totalCountInfo) > 10000) {
            totalCountInfo = totalCountInfo + "(最多显示1w)";
        }
        new Y.mt.widget.Paginator({
            contentBox: Y.one('#paginator_supplier'),
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: totalCountInfo,

            callback: changePage
        });

    }

    function queryButton() {
        var se = getStartEnd();
        if (!se) return;
        var message = $('#exception').val().trim();
        $('#query_btn').attr('disabled', true);
        if (!message) {
            $('.row-fluid .box').show();
            changeHostSet();
        } else {
            var s = startInput.get('value'),
                e = endInput.get('value');
            var stime = new Date(s);
            var etime = new Date(e);
            var sday = stime.getFullYear() + "" + (stime.getMonth() + 1) + stime.getDate();
            var eday = etime.getFullYear() + "" + (etime.getMonth() + 1) + etime.getDate();

            if (sday != eday) {
                Y.msgp.utils.msgpHeaderTip('error', '异常信息不为空时, 时间范围不能跨天', 3);
                $('#query_btn').attr('disabled', false);
                return;
            }
            $('.row-fluid .box').hide();
            changePage({});
        }
    }

    function changePage(parames) {
        var hostSet = $('#sets ul li.active').attr("data-set");
        var host = $('#hosts ul li.active').attr("data-host");
        var filterId = $('#filter ul li.active').attr("data-id");
        var exceptionName = $('#filter ul li.active').attr("data-filter");
        var url = getErrorDetailUrl;
        var start = $('#start_time').val();
        var end = $('#end_time').val();
        var message = $('#exception').val().trim();
        var data = {
            appkey: appkey,
            start: start,
            end: end,
            hostSet: hostSet || 'All',
            host: host || 'All',
            filterId: filterId || '-1',
            exceptionName: exceptionName || 'All',
            pageNo: parames.page || '1',
            pageSize: parames.pageSize || '20',
            message: message
        }
        //(url.indexOf('?') > -1) ? url = url + '&' + name + '=' + value : url = url + '?' + name + '=' + value
        var url_params = ""
        $.each(data, function (name, value) {
            url_params += (name + '=' + value + "&");
        })
        if (url.indexOf('?') > -1) {
            url = url + url_params
        } else {
            url = url + '?' + url_params
        }
        $.ajax({
            url: url,
            type: 'get',
            beforeSend: function () {
                showWaitMsg($('.text-shalow'));
            },
            success: function (ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    var page = ret.page;
                    page.url = parames.url || getErrorDetailUrl;
                    if (parames.message) page.message = parames.message;
                    if (data.length) {
                        fillList(data);
                        refreshPaginator(page);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '无详细数据', 3);
                        $('.text-shalow').html(listHtml);

                    }
                }
            },
            complete: function () {
                clearWaitMsg($('.text-shalow'));
                $('#query_btn').attr('disabled', false);
                $('#exception_search').attr('disabled', false);
            }
            // ...
        });
    }

    function formatDate(date, fmt) {
        var o = {
            "M+": date.getMonth() + 1, //月份
            "d+": date.getDate(), //日
            "h+": date.getHours(), //小时
            "m+": date.getMinutes(), //分
            "s+": date.getSeconds(), //秒
            "q+": Math.floor((date.getMonth() + 3) / 3), //季度
            "S": date.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }

    function showWaitMsg(node) {
        var html = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.7); z-index: 999;" class="showLoading">' +
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #4fbba9;"><i class="fa fa-spinner fa-spin fa-3x" style="color: #4fbba9;"></i><span class="ml20">获取数据中...</span></div>' +
            '</div>';
        node.append(html);
    }

    function clearWaitMsg(node) {
        node.find('.showLoading').remove();
    }

    function formatData(data) {
        var seriesData = [];
        var axisData = [];
        $.each(data, function (index, item) {
            seriesData.push(parseInt(item.count));
            axisData.push(formatDate(new Date(item.time * 1000), 'MM-dd hh:mm'));
        })
        return [seriesData, axisData];
    }

    function initData(data) {
        var legend = [];
        var series = [];
        var i;
        var legendItem;
        $.each(data, function (index, item) {
            if (item.host != 'All') {
                legendItem = item.host + '(' + item.count + ')';

                legend.push(legendItem);
                i = legend.indexOf(legendItem);
                series[i] = {};
                series[i].value = item.count;
                series[i].name = legendItem;
            }
        });
        return [legend, series];
    }

    function checkDetail() {
        $('#log-list').off('click');
        $('#log-list').on("click", 'div.pull-right', function (e) {
            var self = $(this);
            var btn = self.find('a.btn');
            var detail = self.parents('li').find('.detail');
            var hideHeight = detail[0].scrollHeight;
            var html = btn.html();
            if (html == '查看详情') {
                btn.html('关闭详情');
                detail.css({
                    'height': hideHeight,
                    'opacity': '1',
                    'transition-property': 'height, opacity',
                    'transition-duration': '.8s, .8s'
                })
            } else {
                btn.html('查看详情');
                detail.css({
                    'height': '0',
                    'opacity': '0',
                    'transition-property': 'height, opacity',
                    'transition-duration': '.8s, .8s'
                })
            }
        })
    }

    function fillList(data) {
        var html = '';

        $.each(data, function (index, item) {
            // error_exception是从error_message以\n换行符截断的(对应数据组的message, exception字段), 数据组新的日志解析逻辑不再截断(简单的\n截断会误提取), 但导致没有
            // error_exception字段, 所以这里增加判断如果error_exception为空, 则从error_message截断提取, 兼容之前的展示
            if (!item.error_exception) {
                var errorInfo = item.error_message ? item.error_message : item.message;
                var errorMsg = errorInfo ? errorInfo.split("\n")[0] : "";
                item.error_exception = errorInfo.substr(errorMsg.length);
                item.error_message = errorMsg;
            }
            var errorMsgShow = handleStr2Html(item.error_message);
            var errorDetail = handleStr2Html(item.error_exception);
            var detailInfo;
            if (item.error_trace_id == "0") {
                detailInfo = '<span class="label">异常详情：</span><pre>' + (errorDetail || "无异常详细信息") + '</pre>';
            } else {
                detailInfo = '<span class="label">MTrace ID：</span>' + item.error_trace_id + '<br/>' +
                    '<span class="label">异常详情：</span><pre>' + (errorDetail || "无异常详细信息") + '</pre>';
            }
            var hostInfo = item.error_host;
            if (isSetService == "true") {
                hostInfo += " (Set分组: " + item.error_host_set + ")";
            }

            html = html +
                '<li style="padding: 20px 0; border-bottom: 1px solid #ccc;" class="clearfix">' +
                '<div class="pull-right">' +
                '<a href="javascript:;" class="btn">查看详情</a>' +
                '</div>' +
                '<div style="float:left; padding-left: 20px; word-break: break-all;">' +
                '<span class="label">时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间：</span>' + formatDate(new Date(parseInt(item.error_time)), "yyyy-MM-dd hh:mm:ss") + '<br/>' +
                '<span class="label">日志位置：</span>' + item.error_location + '<br/>' +
                '<span class="label">日志信息：</span>' + errorMsgShow + '<br/>' +
                '<span class="label">主机信息：</span>' + hostInfo + '<br/>' +
                '<div class="detail" style="height: 0; opacity: 0; background: #f9f9f9; border-radius: 4px; overflow: hidden;">' + detailInfo + '</div>' +
                '</div>' +
                '</li>';
        })
        $('#log-list').html(html);
        checkDetail();
    }

    function handleStr2Html(str) {
        var newStr = str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/'/g, "&#39;").replace(/"/g, "&quot;").replace(/\r\n/g, "<br/>").replace(/\n/g, "<br/>");
        return newStr;
    }

    function replacePartialChar(str) {
        return str.replace(/'/g, "&#39;").replace(/"/g, "&quot;");
    }

    function fillFilter(data, ffilterId, fexceptionName) {
        var html = '';
        var flag;
        if (ffilterId && fexceptionName) {
            var filterId = ffilterId;
            var exceptionName = fexceptionName;
        }
        $.each(data, function (index, item) {
            if (item.id == filterId && item.name == exceptionName) {
                flag = true;
            }
        });
        $.each(data, function (index, item) {
            var alarmClass = item.alarm ? "alarm" : "";
            if (flag) {
                if (item.id == filterId && item.name == exceptionName) {
                    html = html + '<li class="active ' + alarmClass + '" data-id="' + item.id + '" data-filter="' + item.name + '">' + item.name + '(' + item.count + ')' + '</li>';
                } else {
                    html = html + '<li class="' + alarmClass + '" data-id="' + item.id + '" data-filter="' + item.name + '">' + item.name + '(' + item.count + ')' + '</li>';
                }
            } else {
                if (index == 0) {
                    html = html + '<li class="active ' + alarmClass + '" data-id="' + item.id + '" data-filter="' + item.name + '">' + item.name + '(' + item.count + ')' + '</li>';
                } else {
                    html = html + '<li class="' + alarmClass + '" data-id="' + item.id + '" data-filter="' + item.name + '">' + item.name + '(' + item.count + ')' + '</li>';
                }
            }
        });
        $('#filter ul').html(html);
        Y.msgp.utils.addTooltipWithContent("#filter li.alarm", "有报警的异常分类");
        $("#filter li.alarm").css("color", "coral");
        $("#filter li.active").removeAttr("style");
        initDownmore(data, 'filter');
        setTimeout(function () {
            if ($('.row-fluid .box').css('display') != 'none') {
                drawPieGraph(ffilterId, fexceptionName);
                drawLineGraph(ffilterId, fexceptionName);
                changePage({});
            } else {
                changePage({});
            }
        }, 0);
        addUrlParameters();
    }

    function fillHostSet(data, hostSet, host, filterId, exceptionName) {
        var html = '';
        hostSet = hostSet || 'All';
        $.each(data, function (index, item) {
            if (item.hostSet == hostSet) {
                html = html + '<li class="active" data-set="' + item.hostSet + '">' + item.hostSet + '(' + item.count + ')' + '</li>';
            } else {
                html = html + '<li data-set="' + item.hostSet + '">' + item.hostSet + '(' + item.count + ')' + '</li>';
            }

        });
        $('#sets ul').html(html);
        initDownmore(data, 'sets');
        changeHost(hostSet, host, filterId, exceptionName);
    }

    function fillHost(data, hostSet, host, filterId, exceptionName) {
        var html = '';
        host = host || 'All';
        $.each(data, function (index, item) {
            if (item.host == host) {
                html = html + '<li class="active" data-host="' + item.host + '">' + item.host + '(' + item.count + ')' + '</li>';
            } else {
                html = html + '<li data-host="' + item.host + '">' + item.host + '(' + item.count + ')' + '</li>';
            }

        });
        $('#hosts ul').html(html);
        initDownmore(data, 'hosts');
        changeFilter(hostSet, host, filterId, exceptionName);
    }


    function fillPie(legend, series) {
        if (legend.length >= 14) {
            var num = Math.ceil(legend.length / 17);
            var lWidth = num * 308 + 'px';
            $('#hostReports').css({
                'height': '600px',
                'width': lWidth
            });
        } else {
            $('#hostReports').css({
                'height': '400px',
                'width': '600px'
            });
        }
        var idcchart = echarts.init(document.getElementById('hostReports'));
        var option = {
            animation: false,
            title: {
                text: '异常分布(主机)',
                x: 'center'
            },
            legend: {
                orient: 'vertical',
                data: legend
            },
            tooltip: {
                show: true,
                trigger: 'item',
                formatter: "{b}: {c}({d}%)",
                position: 'inside',
                textStyle: {
                    fontSize: '12'
                }
            },
            series: [
                {
                    type: 'pie',
                    label: {
                        normal: {
                            position: 'left'
                        },
                        emphasis: {
                            show: true,
                            position: 'left'
                        }
                    },
                    selectMode: 'single',
                    data: series
                }
            ],
            labelLine: {
                normal: {
                    show: false
                }
            },
            itemStyle: {
                emphasis: {
                    shadowBlur: 0,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        };
        if (legend.length <= 6) {
            option.legend.left = 'left';
            option.series[0].radius = '50%';
            option.series[0].center = ['50%', '50%'];
        } else {
            option.legend.bottom = 'bottom';
            option.legend.left = 'left';
            option.legend.height = '50%';
            option.series[0].radius = '30%';
            option.series[0].center = ['50%', '30%'];
            if (legend.length >= 14) {
                option.series[0].label.normal.show = false;
                option.legend.itemWidth = 15;
                option.legend.itemHeight = 10;
                option.legend.itemGap = 5
            }
            if (legend.length > 51) {
                option.series[0].center = ['10%', '30%'];
                option.title.x = '5%';
            }
        }
        idcchart.setOption(option);
    }

    function fillLine(data) {
        var drawData = formatData(data);
        var series = [
            {
                name: '',
                type: 'line',
                symbol: 'circle',
                symbolSize: 8,
                smooth: true,
                itemStyle: {
                    normal: {
                        color: '#4fbba9'
                    }
                },
                data: drawData[0]
            }
        ];
        var option = {
            title: {
                text: '异常日志统计',
                left: 'center',
                textAlign: 'center'
            },
            animation: false,
            tooltip: {
                trigger: 'axis',
                formatter: '{b}<br/>异常日志数量: {c}'
            },
            toolbox: {
                show: false
            },
            xAxis: [
                {
                    type: 'category',
                    data: drawData[1],
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: '异常日志数量',
                    axisLine: {
                        show: false
                    }
                }
            ],
            series: series
        };
        var ec = echarts.init(document.getElementById('hourReports'));
        ec.setOption(option);
    }

    function drawLineGraph(ffilterId, fexceptionName) {
        var hostSet = $('#sets ul li.active').attr("data-set");
        var host = $('#hosts ul li.active').attr("data-host");
        var filterId = ffilterId || $('#filter ul li.active').attr("data-id");
        var exceptionName = fexceptionName || $('#filter ul li.active').attr("data-filter");
        var url = getTimeCountUrl;
        var start = $('#start_time').val();
        var end = $('#end_time').val();
        var data = {
            appkey: appkey,
            start: start,
            end: end,
            filterId: filterId || '-1',
            exceptionName: exceptionName || '',
            host: host || 'All',
            hostSet: hostSet || 'All'
        };
        $.each(data, function (name, value) {
            (url.indexOf('?') > -1) ? url = url + '&' + name + '=' + value : url = url + '?' + name + '=' + value
        })
        $.ajax({
            type: 'get',
            url: url,
            beforeSend: function () {
                showWaitMsg($('.row-fluid .box-content'))
            },
            success: function (ret) {
                if (ret.isSuccess) {
                    var lineMessage = ret.data;
                    if (lineMessage.length) {
                        fillLine(lineMessage);

                    } else {
                        $('#hourReports').html('');
                    }


                }
            },
            complete: function () {
                clearWaitMsg($('.row-fluid .box-content'));
            }
        });
    }

    function drawPieGraph(ffilterId, fexceptionName) {
        var hostSet = $('#sets ul li.active').attr("data-set");
        var host = $('#hosts ul li.active').attr("data-host");
        var filterId = ffilterId || $('#filter ul li.active').attr("data-id");
        var exceptionName = fexceptionName || $('#filter ul li.active').attr("data-filter");
        if (host == 'All' && filterId != '-1') {
            $('#hostReports').css({
                'display': 'block',
                'width': '600px'
            });
            var url = getHostCountUrl;
            var start = $('#start_time').val();
            var end = $('#end_time').val();
            var data = {
                appkey: appkey,
                start: start,
                end: end,
                hostSet: hostSet || 'All',
                filterId: filterId || '0',
                exceptionName: exceptionName || ''
            };
            $.each(data, function (name, value) {
                (url.indexOf('?') > -1) ? url = url + '&' + name + '=' + value : url = url + '?' + name + '=' + value
            })
            $.ajax({
                type: 'get',
                url: url,
                beforeSend: function () {
                    showWaitMsg($('.row-fluid .box-content'))
                },
                success: function (ret) {
                    if (ret.isSuccess) {
                        var message = ret.data;
                        if (message.length) {
                            var finalData = initData(message);
                            if (finalData[0].length) {
                                fillPie(finalData[0], finalData[1]);
                            } else {
                                $('#hostReports').css({
                                    'display': 'none',
                                    'width': '0'
                                });
                            }
                        }


                    }
                },
                complete: function () {
                    clearWaitMsg($('.row-fluid .box-content'));
                }
            });
        } else {
            $('#hostReports').css({
                'display': 'none',
                'width': '0'
            });
        }

    }

    function changeFilter(hostSet, host, filterId, exceptionName) {
        var url = getFilterCountUrl;
        var start = $('#start_time').val();
        var end = $('#end_time').val();
        var params = {
            appkey: appkey,
            start: start,
            end: end,
            hostSet: hostSet || 'All',
            host: host || 'All'
        };
        $.each(params, function (name, value) {
            (url.indexOf('?') > -1) ? url = url + '&' + name + '=' + value : url = url + '?' + name + '=' + value
        });
        $.ajax({
            type: 'get',
            url: url,
            success: function (ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    if (data.length) {
                        fillFilter(data, filterId, exceptionName)
                    } else {
                        $('#query_btn').attr('disabled', false);
                        $('#filter ul').html('');
                        $('#hostReports').css({
                            'display': 'none',
                            'width': '0'
                        });
                        $('#hourReports').html('');
                        $('.text-shalow').html(listHtml);

                    }
                }
            }
        });
    }

    function changeHost(hostSet, host, filterId, exceptionName) {
        var url = getHostCountUrl;
        var start = $('#start_time').val();
        var end = $('#end_time').val();
        var params = {
            appkey: appkey,
            start: start,
            end: end,
            hostSet: hostSet || 'All',
            filterId: '-1'
        };
        $.each(params, function (name, value) {
            (url.indexOf('?') > -1) ? url = url + '&' + name + '=' + value : url = url + '?' + name + '=' + value
        });
        $.ajax({
            type: 'get',
            url: url,
            success: function (ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    if (data.length) {
                        fillHost(data, hostSet, host, filterId, exceptionName)
                    }
                }
            }
        });
    }

    function changeHostSet(hostSet, host, filterId, exceptionName) {
        if (isSetService != "true") {
            changeHost("All", host, filterId, exceptionName);
            return;
        }
        var url = getSetCountUrl;
        var start = $('#start_time').val();
        var end = $('#end_time').val();
        var params = {
            appkey: appkey,
            start: start,
            end: end,
        };
        $.each(params, function (name, value) {
            (url.indexOf('?') > -1) ? url = url + '&' + name + '=' + value : url = url + '?' + name + '=' + value
        });
        $.ajax({
            type: 'get',
            url: url,
            success: function (ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    if (data.length) {
                        fillHostSet(data, hostSet, host, filterId, exceptionName)
                    }
                }
            }
        });
    }

    function addUrlParameters() {
        var opt = {
            appkey: $('#apps_select').val(),
            start: $('#start_time').val(),
            end: $('#end_time').val(),
            hostSet: $('#sets ul li.active').attr("data-set") || 'All',
            host: $('#hosts ul li.active').attr("data-host"),
            filterId: $('#filter ul li.active').attr("data-id") || '0',
            exceptionName: $('#filter ul li.active').attr("data-filter") || 'Others',
            message: $('#exception').val().trim()

        };
        Y.msgp.utils.urlAddParameters(opt);
    }

    function bindFilterClick() {
        $('#filter').on('click', 'ul li', function (e) {
            var self = $(this);
            if (self.hasClass('active')) return;
            var se = getStartEnd();
            if (!se) return;
            $('#exception').val('');
            $('.row-fluid .box').show();
            var host = self.attr('data-host');
            $('#filter ul li').removeClass('active');
            self.addClass('active');
            $("#filter li.alarm").css("color", "coral");
            self.removeAttr("style");
            drawPieGraph();
            drawLineGraph();
            changePage({});
            addUrlParameters();

        });
    }

    function bindHostClick() {
        $('#hosts').on('click', 'ul li', function (e) {
            var self = $(this);
            if (self.hasClass('active')) return;
            var se = getStartEnd();
            if (!se) return;
            $('#exception').val('');
            $('.row-fluid .box').show();
            var host = self.attr('data-host');
            $('#hosts ul li').removeClass('active');
            self.addClass('active');
            changeFilter("", host);
        });
    }

    function bindHostSetClick() {
        $('#sets').on('click', 'ul li', function (e) {
            var self = $(this);
            if (self.hasClass('active')) return;
            var se = getStartEnd();
            if (!se) return;
            $('#exception').val('');
            $('.row-fluid .box').show();
            var hostSet = self.attr('data-Set');
            $('#sets ul li').removeClass('active');
            self.addClass('active');
            changeHost(hostSet);
        });
    }

    function bindQueryBtn() {
        Y.one('#query_btn').on('click', function () {
            queryButton();
        });
    }

    function initDownmore(data, id) {
        if (data.length > 4) {
            $('#' + id + ' .down-more').css({
                'visibility': 'visible'
            });
        }
    }

    function initSwitch() {
        var switchBtnHtml = "<div style='margin-left:30px;display: inline-block;'>" +
            "<button id='errorLogSwitch' class='btn btn-sm btn-primary' style='" +
            "padding: 2.5px 5px; font-size: 10px; line-height: 1.5; border-radius: 3px;  margin-bottom: 7px;'>" +
            "异常日志服务开关</button></div>";
        $("h3.page-header input").after(switchBtnHtml);

        var dialog = new Y.mt.widget.CommonDialog({
            content: Y.one("#errorLogSwitchDialog"),
            title: "Octo异常日志服务开关",
            height: 100,
            width: 550
        });
        $("#errorLogSwitch").on("click", function (e) {
            var url = "/log/status";
            $.ajax({
                url: url,
                type: 'get',
                data: {appkey: appkey},
                success: function (ret) {
                    var status;
                    if (ret.isSuccess) {
                        status = ret.data;
                        fillSwitchDialog(status);
                        dialog.show();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取开关状态失败', 3);
                    }
                },
                error: function (ret) {
                    Y.msgp.utils.msgpHeaderTip('error', '获取开关状态失败', 3);
                }
            });
        });
        $("#errorLogSwitchDialog .startErrorLog").on("click", function (e) {
            var url = "/log/start";
            $.ajax({
                url: url,
                type: 'post',
                data: {appkey: appkey},
                success: function (ret) {
                    var status;
                    if (ret.isSuccess) {
                        alert("已提交启动，约10min生效，10min后建议到kafka.data.sankuai.com确认是否有error." + appkey);
                        dialog.close();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '启动服务失败', 3);
                    }
                },
                error: function (ret) {
                    Y.msgp.utils.msgpHeaderTip('error', '启动服务失败', 3);
                }
            });
        });

        $("#errorLogSwitchDialog .stopErrorLog").on("click", function (e) {
            var url = "/log/stop";
            $.ajax({
                url: url,
                type: 'post',
                data: {appkey: appkey},
                success: function (ret) {
                    var status;
                    if (ret.isSuccess) {
                        alert("已停止");
                        dialog.close();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '停止服务失败', 3);
                    }
                },
                error: function (ret) {
                    Y.msgp.utils.msgpHeaderTip('error', '停止服务失败', 3);
                }
            });
        });
    }

    function fillSwitchDialog(status) {
        if (status == "START") {
            $("#errorLogSwitchDialog #statusVal").val("START");
            $(".statusDiv span").html("启动状态（若没有日志请在kafka.data.sankuai.com确认是否有error." + appkey + "）");
            $(".startDiv").hide();
            $(".stopDiv").show();
        } else if (status == "STOP") {
            $("#errorLogSwitchDialog #statusVal").val("STOP");
            $(".statusDiv span").html("停止状态（根据最新的统计判断，若有误请联系RD）");
            $(".startDiv").show();
            $(".startDiv span").html("说明：资源有限将不自动启动日志服务，启动代表启动日志中心的Parser服务(10min生效)，若启动后没有日志请在kafka.data.sankuai.com确认是否有error." + appkey);
            $(".stopDiv").hide();
        } else {
            $("#errorLogSwitchDialog #statusVal").val("UNKNOWN");
            $(".statusDiv span").html("未知状态（无法判断当前日志服务状态，请根据实际情况启动或停止）");
            $(".startDiv").show();
            $(".stopDiv").show();
        }
    }

    function bindReportPage() {
        if (isSetService == "true") {
            bindHostSetClick();
        }
        bindHostClick();
        bindFilterClick();
        bindQueryBtn();
    }

    Y.msgp.Log = {
        report: function (hostSet, host, exceptionName, filterId, setServcie) {
            if (isOffline == "false") {
                initSwitch();
            }
            initDatePicker();
            initTimeLine();
            appkey = $('#apps_select').val();
            isSetService = setServcie;
            if (isSetService == "true") {
                $("#sets").show();
            } else {
                $("#sets").hide();
            }
            bindReportPage();
            changeHostSet(hostSet, host, filterId, exceptionName);
            $('.control-group .down-more').on('click', function (e) {
                var self = $(this);
                if (self.find('i').hasClass('fa-angle-down')) {
                    self.find('span').html('收起');
                    self.find('i').removeClass('fa-angle-down').addClass('fa-angle-up');
                    self.prev('div.list').find('ul').css({
                        'height': 'auto'
                    })
                } else {
                    self.find('span').html('更多');
                    self.find('i').removeClass('fa-angle-up').addClass('fa-angle-down');
                    self.prev('div.list').find('ul').css({
                        'height': '50px'
                    })
                }
            });
        },
        formatDate: function (date, fmt) {
            var o = {
                "M+": date.getMonth() + 1, //月份
                "d+": date.getDate(), //日
                "h+": date.getHours(), //小时
                "m+": date.getMinutes(), //分
                "s+": date.getSeconds(), //秒
                "q+": Math.floor((date.getMonth() + 3) / 3), //季度
                "S": date.getMilliseconds() //毫秒
            };
            if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        },
        /**
         * 表单验证
         * @param form
         * @return boolean
         */
        validateConfigurationForm: function (type) {
            var form;
            if (type == "add") {
                form = Y.one("#addConfigurationForm");
            } else {
                form = Y.one("#editConfigurationForm");
            }
            var errorLogAppkey = form.one("#appkey").get("value");
            if (!errorLogAppkey) {
                Y.msgp.utils.msgpHeaderTip('error', "应用appkey不能为空", 3);
                return false;
            }
            var MIN_GAP_SECONDS = Y.one("#MIN_GAP_SECONDS").get("value");
            var gapSeconds = form.one("#gapSeconds").get("value");
            if (!gapSeconds || parseInt(gapSeconds) < parseInt(MIN_GAP_SECONDS)) {
                Y.msgp.utils.msgpHeaderTip('error', "报警时间不能为空，且最小" + MIN_GAP_SECONDS + "s", 3);
                return false;
            }

            var trapper = form.one("#trapper").get("value");
            if (!trapper) {
                Y.msgp.utils.msgpHeaderTip('error', "Trapper不能为空", 3);
                return false;
            }

            var severityOk = parseInt(form.one("#severityOk").get("value"));
            var severityWarning = parseInt(form.one("#severityWarning").get("value"));
            var severityError = parseInt(form.one("#severityError").get("value"));
            var severityDisaster = parseInt(form.one("#severityDisaster").get("value"));
            if (severityOk < 0 || severityOk > severityWarning
                || severityWarning > severityError || severityError > severityDisaster) {
                Y.msgp.utils.msgpHeaderTip('error', "请确保报警级别非负，且高级别>=低级别", 3);
                return false;
            }
            return true;
        },
        configurationList: function () {
            appkey = $('#apps_select').val();
            this.startAlarm();
            this.stopAlarm();
            this.restartAlarm();
        },
        addConfiguration: function () {
            appkey = $('#apps_select').val();
            var _this = this;
            Y.one("#addConfigurationBtn").on("click", function (e) {
                if (!_this.validateConfigurationForm("add")) {
                    return;
                }
                var url = "/log/configuration/add";
                $.ajax({
                    type: 'post',
                    url: url,
                    data: $("#addConfigurationForm").serialize(),
                    dataType: 'json',
                    success: function (ret) {
                        if (ret.isSuccess) {
                            window.location = "/log/configuration/list?appkey=" + appkey;
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                        }
                    }
                });
            });
        },
        startAlarm: function () {
            var _this = this;
            var link = Y.one("#startAlarmLink");
            if (link == null) {
                return;
            }
            link.on("click", function (e) {
                var errorLogAppkey = Y.one("#appkey").get("value");
                if (errorLogAppkey == null || errorLogAppkey == "") {
                    Y.msgp.utils.msgpHeaderTip('error', "请先选定应用", 3);
                    return;
                }
                if (!_this.validateConfigurationForm()) {
                    return
                }
                var startAlarmDialog = new Y.mt.widget.CommonDialog({
                    id: 'start_alarm_dialog',
                    title: '启动报警',
                    content: '确定启动监控报警吗？<br/>启动后将在30s内生效，不要着急哦^.^~',
                    width: 300,
                    btn: {
                        pass: doStartAlarm
                    }
                });
                startAlarmDialog.show();

                function doStartAlarm() {
                    var url = "/log/startAlarm";
                    _this.submitForm(url, errorLogAppkey);
                }
            });
        },
        stopAlarm: function () {
            var _this = this;
            var link = Y.one("#stopAlarmLink");
            if (link == null) {
                return;
            }
            link.on("click", function (e) {
                var errorLogAppkey = Y.one("#appkey").get("value");
                if (errorLogAppkey == null || errorLogAppkey == "") {
                    Y.msgp.utils.msgpHeaderTip('error', "请先选定应用", 3);
                    return;
                }
                if (!_this.validateConfigurationForm()) {
                    return
                }
                var stopAlarmDialog = new Y.mt.widget.CommonDialog({
                    id: 'stop_alarm_dialog',
                    title: '停止报警',
                    content: '确定停止报警吗？停止后将在30s内生效',
                    width: 300,
                    btn: {
                        pass: doStopAlarm
                    }
                });
                stopAlarmDialog.show();

                function doStopAlarm() {
                    var url = "/log/stopAlarm";
                    _this.submitForm(url, errorLogAppkey);
                }
            });
        },
        restartAlarm: function () {
            var _this = this;
            var link = Y.one("#restartAlarmLink");
            if (link == null) {
                return;
            }
            link.on("click", function (e) {
                var errorLogAppkey = Y.one("#appkey").get("value");
                if (errorLogAppkey == null || errorLogAppkey == "") {
                    Y.msgp.utils.msgpHeaderTip('error', "请先选定应用", 3);
                    return;
                }
                if (!_this.validateConfigurationForm()) {
                    return
                }

                var restartAlarmDialog = new Y.mt.widget.CommonDialog({
                    id: 'restart_alarm_dialog',
                    title: '重启报警',
                    content: '确定重启报警吗？',
                    width: 300,
                    btn: {
                        pass: doRestartAlarm
                    }
                });
                restartAlarmDialog.show();

                function doRestartAlarm() {
                    var url = "/log/restartAlarm";
                    _this.submitForm(url, errorLogAppkey);
                }
            });
        },

        submitForm: function (url) {
            $.ajax({
                type: 'post',
                url: url,
                data: $("#editConfigurationForm").serialize(),
                dataType: 'json',
                success: function (ret) {
                    if (ret.isSuccess) {
                        window.location = "/log/configuration/list?appkey=" + appkey;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }
                }
            });
        },

        /**
         * feed
         */
        feed: function () {
            appkey = $('#apps_select').val();
            var _this = this;
            _feedCurrentId = Y.one("#currentId").get("value");
            _this.feedRunning();
            _this.feedStop();
            _this.feedClear();
        },
        feedRunning: function () {
            var _this = this;
            _feedTimeoutHandle = Y.later(_feedTimeInterval, window, function () {
                var errorLogAppkey = Y.one("#appkey").get("value");
                Y.mt.io.get("/log/feed/pull", {appkey: errorLogAppkey, id: _feedCurrentId}, function (res) {
                    if (res.data != undefined && res.data != null) {
                        var logs = res.data;
                        var color = _feedColors[_feedColorIndex];
                        _feedColorIndex = 1 - _feedColorIndex;
                        Y.Array.each(logs, function (log) {
                            if (log.id > _feedCurrentId) {
                                _feedCurrentId = log.id;
                            }
                            var params = {
                                color: color,
                                logTime: _this.formatDate(new Date(log.logTime), "yyyy-MM-dd hh:mm:ss"),
                                location: log.location,
                                message: log.message,
                                exceptionMessage: log.exceptionMessage,
                                level: log.level,
                                host: log.host,
                                ip: log.ip,
                                rowkey: log.rowkey
                            };
                            var node = Y.Node.create(Y.Lang.sub(_this.template.logItemTemplate4Feed, params));
                            Y.one("#topLog").insert(node, "after");
                        });
                    }
//                        if(res.currentId != undefined && res.currentId != null) {
//                            // update _feedCurrentId
//                            _feedCurrentId = res.currentId;
//                        }
                });
            }, [], true);
        },
        feedStop: function () {
            var _this = this;
            Y.one("#logFeedControlBtn").on("click", function (e) {
                var start = e.target.getAttribute("data-start");
                var pause = e.target.getAttribute("data-pause");
                var value = e.target.get("value");
                if (value == start) {
                    _this.feedRunning();
                    e.target.set("value", pause);
                } else {
                    _feedTimeoutHandle.cancel();
                    e.target.set("value", start);
                }
            });
        },
        feedClear: function () {
            var _this = this;
            Y.one("#logFeedClearBtn").on("click", function (e) {
                Y.one("#logList").set("innerHTML", "<li id='topLog' style='display: none;'></li>");
            });
        },
        /**
         * filter
         */
        filterList: function () {
            appkey = $('#apps_select').val();
            _currentSelectFilterId = null;
            _currentSelectEnabledFilter = false;
            this.initFilterList();
            this.initFilterContent();
            this.initDeleteFilter();
            this.initMoveFilter();
        },
        initFilterList: function () {
            _currentSelectFilterId = null;
            _currentSelectEnabledFilter = false;
            _enabledFilterIds.length = 0;
            _disabledFilterIds.length = 0;
            Y.one("#enabled-filter-list").setContent("");
            Y.one("#disabled-filter-list").setContent("");
            var _this = this;
            var errorLogAppkey = Y.one("#appkey").get("value");
            Y.mt.io.get("/log/filter/getFilters", {appkey: errorLogAppkey}, function (res) {
                if (res.data != undefined && res.data != null) {
                    var filters = res.data;
                    var enabledFilterNodes = [];
                    var disabledFilterNodes = [];
                    Y.Array.each(filters, function (filter) {
                        var prefix = filter.alarm ? "【报警】" : "【不报警】"
                        var postfix = filter.alarm ? "[阈值" + filter.threhold + "/" + filter.thresholdMin + "min]" : "";
                        var params = {
                            id: filter.id,
                            name: prefix + '[' + handleStr2Html(filter.name) + ']' + postfix
                        };
                        if (filter.enabled) {
                            enabledFilterNodes.push(Y.Node.create(Y.Lang.sub(_this.template.enabledFilterItemTemplate, params)));
                            _enabledFilterIds.push(filter.id);
                        } else {
                            disabledFilterNodes.push(Y.Node.create(Y.Lang.sub(_this.template.disabledFilterItemTemplate, params)));
                            _disabledFilterIds.push(filter.id);
                        }
                    });
                    Y.one("#enabled-filter-list").setContent(new Y.NodeList(enabledFilterNodes).toFrag());
                    Y.one("#disabled-filter-list").setContent(new Y.NodeList(disabledFilterNodes).toFrag());

                    var filter_sortable = $('#enabled-filter-list').sortable({
                        group: 'enabled-filter',
                        itemSelector: 'a',
                        onDrop: function ($item, container, _super) {
                            var data = filter_sortable.sortable("serialize").get()[0];
                            var ids = []
                            for (var i = 0; i < data.length; i++) {
                                ids.push(data[i].id)
                            }
                            _this.sortFilter(ids, "移动");
                        }
                    });
                }
                _this.initOnSelectFilter();
            });
        },
        initOnSelectFilter: function () {
            var _this = this;
            Y.one("#enableFilter").set("disabled", true);
            Y.one("#disableFilter").set("disabled", true);
            Y.one("#upFilter").set("disabled", true);
            Y.one("#downFilter").set("disabled", true);
            Y.all(".enabled-filter").on("click", function (e) {
                Y.all(".filter").removeClass("btn-primary");
                e.target.addClass("btn-primary");
                _currentSelectFilterId = e.target.getAttribute("data-id");
                _currentSelectEnabledFilter = true;
                Y.one("#enableFilter").set("disabled", true);
                Y.one("#disableFilter").set("disabled", false);
                if (e.target.previous() == null) {
                    Y.one("#upFilter").set("disabled", true);
                } else {
                    Y.one("#upFilter").set("disabled", false);
                }
                if (e.target.next() == null) {
                    Y.one("#downFilter").set("disabled", true);
                } else {
                    Y.one("#downFilter").set("disabled", false);
                }
            });
            Y.all(".disabled-filter").on("click", function (e) {
                Y.all(".filter").removeClass("btn-primary");
                e.target.addClass("btn-primary");
                _currentSelectFilterId = e.target.getAttribute("data-id");
                _currentSelectEnabledFilter = false;
                Y.one("#enableFilter").set("disabled", false);
                Y.one("#disableFilter").set("disabled", true);
                if (e.target.previous() == null) {
                    Y.one("#upFilter").set("disabled", true);
                } else {
                    Y.one("#upFilter").set("disabled", false);
                }
                if (e.target.next() == null) {
                    Y.one("#downFilter").set("disabled", true);
                } else {
                    Y.one("#downFilter").set("disabled", false);
                }
            });
        },
        initFilterContent: function () {
            $(".fireAlarm").on("change", function () {
                if ($(".fireAlarm").is(':checked')) {
                    $(".alarmCond").show();
                } else {
                    $(".alarmCond").hide();
                }
            })
            var _this = this;
            var dialog = new Y.mt.widget.CommonDialog({
                content: Y.one("#filterContentDialog"),
                height: 400,
                width: 450
            });
            $("#filterContentDialog #save").on("click", function () {
                if ($("#filterContentDialog").attr("oper") == "add") {
                    _this.saveFilter(dialog, "ADD");
                } else if ($("#filterContentDialog").attr("oper") == "edit") {
                    _this.saveFilter(dialog, "EDIT");
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', "没有匹配操作,清缓存刷新无效后请联系平台负责人", 3);
                }
            });

            $("#addFilter").on("click", function (e) {
                dialog.setTitle("添加过滤器");
                $("#filterContentDialog").attr("oper", "add");
                _this.resetAddFilterContentDialog();
                dialog.show();
            });

            $("#editFilter").on("click", function (e) {
                if (_currentSelectFilterId == null) {
                    Y.msgp.utils.msgpHeaderTip('error', "请选择过滤器", 3);
                    return;
                }
                dialog.setTitle("编辑过滤器");
                $("#filterContentDialog").attr("oper", "edit");
                Y.mt.io.get("/log/filter/getFilter", {filterId: _currentSelectFilterId}, function (res) {
                    if (!res.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', res.msg, 3);
                        return;
                    }
                    var filter = res.data;
                    _this.fillEditFilterDialog(filter);
                    dialog.show();
                });
            });
        },
        saveFilter: function (dialog, operType) {
            var _this = this;

            var dialogNode = Y.one("#filterContentDialog");
            var name = dialogNode.one("#name").get("value");
            if (_this.isBlank(name)) {
                Y.msgp.utils.msgpHeaderTip('error', "过滤器名称不能为空", 3);
                return;
            }
            var errorLogAppkey = Y.one("#appkey").get("value");
            var terminate = false;
            if (dialogNode.one("#terminate").get("checked")) {
                terminate = true;
            }
            var alarm = false;
            if (dialogNode.one(".fireAlarm").get("checked")) {
                alarm = true;
            }
            var threshold = dialogNode.one(".logCount").get("value");
            var thresholdMin = dialogNode.one(".logCountMin").get("value");
            var messageRules = [];
            var exceptionRules = [];
            var hostnameRules = [];
            var isReturn = false;
            dialogNode.all(".rule-item").each(function (item) {
                var rule = item.one("#rule").get("value");
                if (_this.isBlank(rule)) {
                    Y.msgp.utils.msgpHeaderTip('error', "过滤器条件不能为空", 3);
                    isReturn = true;
                    return;
                }
                if (item.one("#target").get("value") == "message") {
                    messageRules.push(rule);
                } else if (item.one("#target").get("value") == "exception") {
                    exceptionRules.push(rule);
                } else if (item.one("#target").get("value") == "hostname") {
                    hostnameRules.push(rule);
                }
            });
            if (isReturn) {
                return;
            }
            var ruleCondition = dialogNode.one("#ruleCondition").get("value");
            var enabled = false;
            if (dialogNode.one("#enabled").get("checked")) {
                enabled = true;
            }
            var rules = {};
            if (messageRules.length > 0) {
                rules["message"] = messageRules;
            }
            if (exceptionRules.length > 0) {
                rules["exception"] = exceptionRules;
            }
            if (hostnameRules.length > 0) {
                rules["hostname"] = hostnameRules;
            }
            var params = {
                id: dialogNode.one("#edit-id").get("value"), // 编辑才有该值
                name: name,
                appkey: errorLogAppkey,
                terminate: terminate,
                alarm: alarm,
                threhold: threshold,
                thresholdMin: thresholdMin,
                rules: Y.JSON.stringify(rules),
                ruleCondition: ruleCondition,
                enabled: enabled,
                oldEnabled: _currentSelectEnabledFilter
            };
            var reqUrl = "";
            var typeMesg = "";
            if (operType == "ADD") {
                reqUrl = "/log/filter/add";
                typeMesg = "添加";
            } else if (operType == "EDIT") {
                reqUrl = "/log/filter/update";
                typeMesg = "修改";
            }
            Y.mt.io.post(reqUrl, params, function (res) {
                if (res.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('success', typeMesg + "成功", 3);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', typeMesg + "失败：" + res.msg, 3);
                    return;
                }
                dialog.close();
                _this.initFilterList();
            });
        },

        resetAddFilterContentDialog: function () {
            var _this = this;
            var dialogNode = Y.one("#filterContentDialog");
            $("#edit-id").val(null)
            dialogNode.one("#enabled").set("checked", true);
            dialogNode.one("#terminate").set("checked", true);
            dialogNode.one(".fireAlarm").set("checked", true);
            $(".alarmCond").show();
            $(".alarmCond .logCountMin").val(1);
            $(".alarmCond .logCount").val(1);
            dialogNode.one("#name").set("value", "");
            var first = true;
            dialogNode.one("#ruleCondition").get("children").each(function (optionNode) {
                if (first) {
                    optionNode.selected = true;
                    first = false;
                } else {
                    optionNode.selected = false;
                }
            });
            dialogNode.one("#rules").setContent(
                Y.Node.create(Y.Lang.sub(_this.template.ruleItemTemplate4MessageSelected, {rule: ""})));
            _this.initAddDeleteRule();
        },
        fillEditFilterDialog: function (filter) {
            var _this = this;
            var dialogNode = Y.one("#filterContentDialog");
            dialogNode.one("#edit-id").set("value", filter.id);
            if (filter.enabled) {
                dialogNode.one("#enabled").set("checked", true);
            } else {
                dialogNode.one("#enabled").set("checked", false);
            }
            if (filter.terminate) {
                dialogNode.one("#terminate").set("checked", true);
            } else {
                dialogNode.one("#terminate").set("checked", false);
            }
            if (filter.alarm) {
                dialogNode.one(".fireAlarm").set("checked", true);
                $(".alarmCond").show();
            } else {
                dialogNode.one(".fireAlarm").set("checked", false);
                $(".alarmCond").hide();
            }
            dialogNode.one(".logCount").set("value", filter.threhold);
            dialogNode.one(".logCountMin").set("value", filter.thresholdMin);
            dialogNode.one("#name").set("value", filter.name);
            dialogNode.one("#ruleCondition").get("children").each(function (optionNode) {
                if (optionNode.get("value") == filter.ruleCondition) {
                    optionNode.set("selected", true);
                } else {
                    optionNode.set("selected", false);
                }
            });
            // rules
            dialogNode.one("#rules").setHTML("");
            var rules = Y.JSON.parse(filter.rules);
            if (rules.message != null) {
                Y.Array.each(rules.message, function (rule) {
                    var params = {
                        rule: replacePartialChar(rule)
                    };
                    dialogNode.one("#rules").appendChild(
                        Y.Node.create(Y.Lang.sub(_this.template.ruleItemTemplate4MessageSelected, params)));
                });
            }
            if (rules.exception != null) {
                Y.Array.each(rules.exception, function (rule) {
                    var params = {
                        rule: replacePartialChar(rule)
                    };
                    dialogNode.one("#rules").appendChild(
                        Y.Node.create(Y.Lang.sub(_this.template.ruleItemTemplate4ExceptionSelected, params)));
                });
            }
            if (rules.hostname != null) {
                Y.Array.each(rules.hostname, function (rule) {
                    var params = {
                        rule: rule
                    };
                    dialogNode.one("#rules").appendChild(
                        Y.Node.create(Y.Lang.sub(_this.template.ruleItemTemplate4HostnameSelected, params)));
                });
            }
            _this.initAddDeleteRule();
        },
        initDeleteFilter: function () {
            var _this = this;
            Y.one("#deleteFilter").on("click", function (e) {
                if (_currentSelectFilterId == null) {
                    Y.msgp.utils.msgpHeaderTip('error', "请选择过滤器", 3);
                    return;
                }

                var delSupplierDialog = new Y.mt.widget.CommonDialog({
                    id: 'del_filter_dialog',
                    title: '删除过滤器',
                    content: '确认删除选中的过滤器?',
                    width: 300,
                    btn: {
                        pass: doDelAllSupplier
                    }
                });
                delSupplierDialog.show();

                function doDelAllSupplier() {
                    var errorLogAppkey = Y.one("#appkey").get("value");
                    Y.mt.io.post("/log/filter/delete", {
                        filterId: _currentSelectFilterId,
                        appkey: errorLogAppkey
                    }, function (res) {
                        if (res.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            _this.initFilterList();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', "删除失败：" + res.msg, 3);
                        }
                    });
                }
            });
        },
        initMoveFilter: function () {
            var _this = this;
            Y.one("#enableFilter").on("click", function (e) {
                if (_currentSelectFilterId == null || _currentSelectEnabledFilter) {
                    Y.msgp.utils.msgpHeaderTip('error', "请选择可用过滤器", 3);
                    return;
                }
                var errorLogAppkey = Y.one("#appkey").get("value");
                Y.mt.io.post("/log/filter/enable", {filterId: _currentSelectFilterId, appkey: errorLogAppkey}, function (res) {
                    if (!res.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('error', "启用失败：" + res.msg, 3);
                    } else {
                        _this.initFilterList();
                    }
                });
            });
            Y.one("#disableFilter").on("click", function (e) {
                if (_currentSelectFilterId == null || !_currentSelectEnabledFilter) {
                    Y.msgp.utils.msgpHeaderTip('error', "请选择活动过滤器", 3);
                    return;
                }
                var errorLogAppkey = Y.one("#appkey").get("value");
                Y.mt.io.post("/log/filter/disable", {filterId: _currentSelectFilterId, appkey: errorLogAppkey}, function (res) {
                    if (!res.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('error', "废弃失败：" + res.msg, 3);
                    } else {
                        _this.initFilterList();
                    }
                });
            });
            Y.one("#upFilter").on("click", function (e) {
                if (_currentSelectFilterId == null) {
                    Y.msgp.utils.msgpHeaderTip('error', "请选择过滤器", 3);
                    return;
                }
                if (Y.one("#filter_link_" + _currentSelectFilterId).previous() == null) {
                    Y.msgp.utils.msgpHeaderTip('error', "已经在顶部", 3);
                    return;
                }
                if (_currentSelectEnabledFilter) {
                    _this.upFilter(_enabledFilterIds, _currentSelectFilterId);
                } else {
                    _this.upFilter(_disabledFilterIds, _currentSelectFilterId);
                }
            });
            Y.one("#downFilter").on("click", function (e) {
                if (_currentSelectFilterId == null) {
                    Y.msgp.utils.msgpHeaderTip('error', "请选择过滤器", 3);
                    return;
                }
                if (Y.one("#filter_link_" + _currentSelectFilterId).next() == null) {
                    Y.msgp.utils.msgpHeaderTip('error', "已经在底部", 3);
                    return;
                }
                if (_currentSelectEnabledFilter) {
                    _this.downFilter(_enabledFilterIds, _currentSelectFilterId);
                } else {
                    _this.downFilter(_disabledFilterIds, _currentSelectFilterId);
                }
            });
        },
        upFilter: function (ids, id) {
            for (var i = 1; i < ids.length; i++) {
                if (ids[i] == id) {
                    var tmp = ids[i - 1];
                    ids[i - 1] = ids[i];
                    ids[i] = tmp;
                    break;
                }
            }
            this.sortFilter(ids, "上移");
        },
        downFilter: function (ids, id) {
            for (var i = 0; i < ids.length - 1; i++) {
                if (ids[i] == id) {
                    var tmp = ids[i + 1];
                    ids[i + 1] = ids[i];
                    ids[i] = tmp;
                    break;
                }
            }
            this.sortFilter(ids, "下移");
        },
        sortFilter: function (ids, message) {
            var _this = this;
            var errorLogAppkey = Y.one("#appkey").get("value");
            Y.mt.io.get("/log/filter/sort", {filterIds: ids, appkey: errorLogAppkey}, function (res) {
                if (!res.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('error', message + "出错：" + res.msg, 3);
                }
                _this.initFilterList();
            });
        },
        initAddDeleteRule: function () {
            var _this = this;
            var nodes = Y.all(".addRuleItem");
            if (nodes != null) {
                nodes.each(function (node) {
                    node.purge(false, "click");
                });
                nodes.on("click", function (e) {
                    if (e.target.ancestor("#rules") != null) {
                        e.target.ancestor("#rules").appendChild(
                            Y.Node.create(Y.Lang.sub(_this.template.ruleItemTemplate4MessageSelected, {rule: ""}))
                        );
                    }
                    _this.initAddDeleteRule();
                });
            }
            nodes = Y.all(".deleteRuleItem");
            if (nodes != null) {
                nodes.each(function (node) {
                    node.purge(false, "click");
                });
                nodes.on("click", function (e) {
                    var ruleItemNode = e.target.ancestor(".rule-item");
                    if (ruleItemNode.previous() == null && ruleItemNode.next() == null) {
                        Y.msgp.utils.msgpHeaderTip('error', "最少要有一个条件", 3);
                        return;
                    }
                    ruleItemNode.remove(true);
                    _this.initAddDeleteRule();
                });
            }
        },
        isBlank: function (str) {
            if (str == null || str == undefined || str == '') {
                return true;
            }
            return false;
        },

        AutoCompleteList: function (id, obj) {
            new Y.mt.widget.AutoCompleteList({
                id: "apps_select_auto",
                node: Y.one("#apps_select"),
                listParam: 'name',
                objList: obj,
                showMax: obj.length,
                matchMode: 'fuzzy',
                forbidWrite: false,
                more: "",
                tabSelect: true,
                callback: function (data) {
                    var akey = data.name;
                    location.search = 'appkey=' + akey;
                }
            });
        },


        template: {
            logItemTemplate4Feed: "<li>" +
            "<div style=\"float: left; padding-left: 20px; color: {color};\">" +
            "时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间：{logTime}" +
            "<br/>" +
            "日志位置：{location}" +
            "<br/>" +
            "日志信息：{message}" +
            "<br/>" +
            "异常信息：{exceptionMessage}" +
            "<br/>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
            "{level}" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
            "{host}" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
            "{ip}" +
            "</div>" +
            "<div style='float: right;'>" +
            "<a href='/log/detail?uniqueKey={rowkey}'>查看详情</a>" +
            "</div>" +
            "</li><hr class='hr-normal' />",
            enabledFilterItemTemplate: "<a id='filter_link_{id}' class='enabled-filter filter btn btn-block' \
                    data-id={id} href='javascript:void(0);' style='text-align: left'>{name}</a>",
            disabledFilterItemTemplate: "<a id='filter_link_{id}' class='disabled-filter filter btn btn-block' \
                    data-id={id} href='javascript:void(0);' style='text-align: left'>{name}</a>",
            ruleItemTemplate4MessageSelected: "<div class='rule-item'>" +
            "<select id='target' style='width: 100px;'>" +
            "<option value='message' selected='selected'>Message</option> " +
            "<option value='exception'>Exception</option> " +
            "<option value='hostname'>Hostname</option> " +
            "</select> " +
            "包含 " +
            "<input id='rule' type='text' value='{rule}'/> " +
            "&nbsp;<a class='addRuleItem' href='javascript:void(0);' style='font-size: 24px;'>+</a>&nbsp;&nbsp;" +
            "<a class='deleteRuleItem' href='javascript:void(0);' style='font-size: 24px;'>-</a>" +
            "</div>",
            ruleItemTemplate4ExceptionSelected: "<div class='rule-item'>" +
            "<select id='target' style='width: 100px;'>" +
            "<option value='message'>Message</option> " +
            "<option value='exception' selected='selected'>Exception</option> " +
            "<option value='hostname'>Hostname</option> " +
            "</select> " +
            "包含 " +
            "<input id='rule' type='text' value='{rule}'/> " +
            "&nbsp;<a class='addRuleItem' href='javascript:void(0);' style='font-size: 24px;'>+</a>&nbsp;&nbsp;" +
            "<a class='deleteRuleItem' href='javascript:void(0);' style='font-size: 24px;'>-</a>" +
            "</div>",
            ruleItemTemplate4HostnameSelected: "<div class='rule-item'>" +
            "<select id='target' style='width: 100px;'>" +
            "<option value='message'>Message</option> " +
            "<option value='exception'>Exception</option> " +
            "<option value='hostname' selected='selected'>Hostname</option> " +
            "</select> " +
            "包含 " +
            "<input id='rule' type='text' value='{rule}'/> " +
            "&nbsp;<a class='addRuleItem' href='javascript:void(0);' style='font-size: 24px;'>+</a>&nbsp;&nbsp;" +
            "<a class='deleteRuleItem' href='javascript:void(0);' style='font-size: 24px;'>-</a>" +
            "</div>"
        }
    };
}, "1.0", {
    requires: [
        "mt-base"
        , "mt-io"
        , "w-table"
        , "mt-form"
        , "w-autocomplete"
        , "mt-date"
        , 'w-paginator'
        , 'msgp-utils/msgpHeaderTip'
        , 'msgp-utils/common'
    ]
});
