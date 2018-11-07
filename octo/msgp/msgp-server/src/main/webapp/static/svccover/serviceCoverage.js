/**
 * Created by huoyanyu on 2017/7/14.
 */

M.add('msgp-svccover/serviceCoverage', function (Y) {
    var everPaged = false,
        totalPage,
        totalCount;
    var pbody;
    var coverageWrapper;
    var checkListWrapper;
    var history_count;
    var current_count;
    var base;
    var business;
    var owt;
    var cmptVal;
    var state;
    var date;
    Y.namespace('msgp.svccover').serviceCoverage = serviceCoverage;

    function serviceCoverage() {
        document.title = '服务治理覆盖率';
        coverageWrapper = Y.one('#div_service_coverage');
        checkListWrapper = coverageWrapper.one("#check_list_wrapper");
        history_count = echarts.init(document.getElementById('service_history_count'), 'macarons');
        current_count = echarts.init(document.getElementById('service_current_count'), 'macarons');
        state = coverageWrapper.one('#state_select a.btn-primary').getAttribute('value');
        initDatePicker(6);
        bindWidget();
        setDefaulValue();

    }

    function initDatePicker(days) {
        var now = new Date();
        var startInput = Y.one('#start');
        var endInput = Y.one('#end');
        var end_day = new Date(now - 24 * 60 * 60 * 1000);
        var start_day = new Date(end_day - days * 24 * 60 * 60 * 1000);
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

    function setDefaulValue() {
        pbody = coverageWrapper.one('#paginator_checklist_wrapper');
        everPaged = false;
        getBusiness();
        getChartAndTableData(-1);

    }

    function bindWidget() {
        coverageWrapper.one('#searchBtn').on('click', function () {
            Y.all('#date_select a').removeClass('btn-primary');
            getChartAndTableData(-1);
        });
        coverageWrapper.one('#business').on('change', function () {
            var business = this.get('value');
            if (business == "") {
                coverageWrapper.one('#owt').empty();
                coverageWrapper.one('#owt').append('<option value="all">all</option>');
                getChartAndTableData(-1);

            } else {
                getOwt(business);
            }
        });
        coverageWrapper.one('#owt').on('change', function () {
            getChartAndTableData(-1);

        });
        coverageWrapper.one('#cmpt').on('change', function () {
            getChartAndTableData(-1);

        });
        coverageWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            var business = coverageWrapper.one("#business").get('value');
            if (business != "") {
                getOwt(business);
            }
            getChartAndTableData(-1);
        }, '.base_type');
        coverageWrapper.delegate('click', function () {
            Y.all('#state_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            state = coverageWrapper.one('#state_select a.btn-primary').getAttribute('value');
            if (state == "history") {
                Y.all(".date").show();
                coverageWrapper.one("#service_history_count").show();
                coverageWrapper.one("#service_current_count").hide();
                coverageWrapper.one("#check_list_wrapper").hide();
                coverageWrapper.one("#paginator_checklist_wrapper").hide();
            }
            if (state == "current") {
                Y.all(".date").hide();
                coverageWrapper.one("#service_current_count").show();
                coverageWrapper.one("#service_history_count").hide();
                coverageWrapper.one("#check_list_wrapper").show();
                coverageWrapper.one("#paginator_checklist_wrapper").show();
            }
            getChartAndTableData(-1);
        }, '.state_type');

        coverageWrapper.delegate('click', function () {
            Y.all('#date_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            date = coverageWrapper.one('#date_select a.btn-primary').getAttribute('value');
            if (date == "week") {
                initDatePicker(6);
            }
            if (date == "month") {
                initDatePicker(30);
            }
            if (date == "quarter") {
                initDatePicker(90);
            }
            getChartAndTableData(-1);
        }, '.date_type');
    }

    function getBusiness() {
        var url = '/svccover/business';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function (item) {
                            coverageWrapper.one('#business').append('<option value=' + item + '>' + item + '</option>');
                        });
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取事业群失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取事业群失败', 3);
                }
            }
        });
    }

    function getOwt(business) {
        var base = coverageWrapper.one('#base_select a.btn-primary').getAttribute('value');
        coverageWrapper.one('#owt').empty();
        coverageWrapper.one('#owt').append('<option value="all">all</option>');
        var url = '/svccover/owt';
        Y.io(url, {
            method: 'get',
            data: {
                "business": business,
                "base": base
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function (item) {
                            coverageWrapper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartAndTableData(-1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                }
            }
        });
    }

    function fillCharts(outline) {
        history_count.clear();
        current_count.clear();
        if (state == "history") {
            history_count.setOption(getHistoryChartOption(outline.dates, outline.outlinePeriods));
            history_count.hideLoading();
        }
        if (state == "current") {
            current_count.setOption(getCurrentChartOption(outline.dates, outline.outlinePeriods));
            current_count.hideLoading();
        }
    }

    function getCurrentChartOption(dates, outlinePeriods) {
        var series = [];
        var count;
        var sum;
        Y.Array.each(outlinePeriods, function (item) {
            count = item.counts[0];
            sum = item.sums[0];
            series.push({
                name: item.cmptVal,
                type: 'pie',
                radius: '65%',
                center: ['50%', '60%'],
                data: [
                    {name: '已接入', value: Number(item.rates[0])},
                    {name: '未接入', value: (100 - item.rates[0])}
                ],
                itemStyle: {
                    normal: {
                        label: {
                            show: true,
                            formatter: '{b}:{c}%'
                        }
                    },
                    labelLine: {
                        show: true
                    }
                },
                emphasis: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            });
        });
        var chartTitle = cmptVal + "服务治理覆盖率概要";

        var option = {
            title: {
                text: chartTitle,
                subtext: business + " " + owt + " " + dates[0],
                x: 'center'
            },
            noDataLoadingOption: {
                text: '暂无数据'
            },
            tooltip: {
                trigger: 'item',
                padding: 15,
                formatter: function (params) {
                    var name = params.name;
                    var value = params.value + "%";
                    var cmptVal = params.seriesName;
                    var result =
                        '<table> ' +
                        '</caption>' +
                        '<tr> ' +
                        '<td colspan="2" style="padding-left: 10px; color: #C0C0C0;">' + cmptVal + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">' + name + '</strong></td> '
                    if (name == "已接入") {
                        result += '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + count + '</strong></td> </tr>';
                    }
                    else {
                        result += '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + (sum - count) + '</strong></td></tr> ';
                    }
                    result += '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">总数：</strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + sum + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">比例: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '</table>';
                    return result;
                }
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: false},
                    restore: {show: false},
                    saveAsImage: {show: true}
                }
            },
            legend: {
                show: false,
                orient: 'vertical',
                x: 'left',
            },
            series: series
        };
        return option;

    }

    function getHistoryChartOption(dates, outlinePeriods) {
        var series = [];
        var legendData = [];
        var counts = [];
        var sums = [];
        Y.Array.each(outlinePeriods, function (item) {
            counts = item.counts;
            sums = item.sums;
            series.push({
                name: item.cmptVal,
                type: 'line',
                stack: '总量',
                symbol: 'emptyCircle',
                symbolSize: 10,
                showAllSymbol: true,
                data: item.rates
            });
            legendData.push({
                icon: 'line',
                name: item.cmptVal
            });
        });
        var chartTitle = cmptVal + "服务覆盖率趋势";

        var legend = {
            data: legendData,
            x: 'center',
            y: 'bottom'
        };

        return option = {
            title: {
                text: chartTitle,
                subtext: business + " " + owt,
                left: 'center',
                textStyle: {
                    color: '#636363'
                }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'line',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                },
                formatter: function (params) {
                    var name = params[0].name;
                    var value = params[0].value + "%";
                    var cmptVal = params[0].seriesName;
                    var index = params[0].dataIndex
                    var result =
                        '<table> ' +
                        '</caption>' +
                        '<tr> ' +
                        '<td colspan="2" style="padding-left: 10px; color: #C0C0C0;">' + name + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td colspan="2" style="padding-left: 10px; color: #C0C0C0;">' + cmptVal + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="color: #C0C0C0;">已接入：</strong></td> ' +
                        '<td style="color: #ffffff;">' + counts[index] + '</strong></td> ' +
                        '</tr>' +
                        '<tr> ' +
                        '<td style="color: #C0C0C0;">总数：</strong></td> ' +
                        '<td style="color: #ffffff;">' + sums[index] + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="color: #C0C0C0;">比例: </strong></td> ' +
                        '<td style="color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '</table>';
                    return result;
                }
            },
            legend: legend,
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
                    name: "天",
                    type: 'category',
                    data: dates,
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: [
                {
                    name: "百分比",
                    type: 'value',
                    axisLabel: {
                        formatter: '{value} %'
                    },
                    scale: true,
                    axisLine: {
                        show: false
                    }
                }
            ],
            series: series
        };

    }

    function getChartAndTableData(pageNo) {
        if (pageNo == -1) {
            history_count.showLoading({
                text: '正在加载',
                effect: 'bar',
                textStyle: {
                    fontSize: 20
                }
            });
            current_count.showLoading({
                text: '正在加载',
                effect: 'bar',
                textStyle: {
                    fontSize: 20
                }
            });
        } else {
            showWaitMsg(checkListWrapper);
        }
        base = coverageWrapper.one('#base_select a.btn-primary').getAttribute('value');
        business = coverageWrapper.one("#business").get('value');
        owt = coverageWrapper.one("#owt").get('value') == "all" ? " " : coverageWrapper.one("#owt").get('value');
        cmptVal = coverageWrapper.one("#cmpt").get('value');
        if (state == "history") {
            var startTime = coverageWrapper.one("#start").get('value');
            var endTime = coverageWrapper.one("#end").get('value');
        }
        if (state == "current") {
            var startT = new Date(new Date() - 24 * 60 * 60 * 1000);
            var startTime = Y.mt.date.formatDateByString(startT, 'yyyy-MM-dd')
            var endTime = startTime;
        }
        var searchData = {
            "state": state,
            "base": base,
            "business": business,
            "owt": owt,
            "cmptVal": cmptVal,
            "startTime": startTime,
            "endTime": endTime,
            "pageNo": pageNo,//页码是-1
            "pageSize": 15
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/svccover/data';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if ("history" == state) {
                            fillCharts(data.outline);
                        } else {
                            if (data.details.length == 0) {
                                showEmptyErrorMsg(checkListWrapper, false);
                                Y.msgp.utils.msgpHeaderTip('info', '未产生数据', 3);
                            } else {
                                if (pageNo == -1) {
                                    fillCharts(data.outline);
                                }
                                fillTable(data.details);
                                if ($('#check_list').length > 0) {
                                    if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                        refreshPaginator(pbody, pobj);
                                    }
                                }
                            }
                            everPaged = true;
                            totalPage = pobj.totalPageCount;
                            totalCount = pobj.totalCount;
                        }
                    } else {
                        showEmptyErrorMsg(checkListWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);


                    }
                },
                failure: function () {
                    showEmptyErrorMsg(checkListWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillTable(data) {
        $(".tip").remove();
        $("#check_list tbody").empty();
        for (var i = 0; i < data.length; i++) {
            $("#check_list tbody").append(function () {
                var htmlText = "<tr><td style='width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>" + data[i].statdate + "</td>" +
                    "<td style='width: 10%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>" + data[i].owt + "</td>" +
                    "<td style='width: 20%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>" + data[i].appkey + "</td>";
                if (data[i].isUsed) {
                    htmlText += "<td style='width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>" + cmptVal + "</td>";
                } else if (!data[i].isUsed) {
                    htmlText += "<td style='width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>未接入</td>";
                }
                return htmlText;
            });

        }
    }

    function refreshPaginator(pbody, pobj) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: changePage
        });
    }

    function changePage(params) {
        getChartAndTableData(params.page);
    }

    function showEmptyErrorMsg(node, isError) {
        history_count.hideLoading();
        current_count.hideLoading();
        history_count.clear();
        current_count.clear();
        $(".tip").remove();
        $("#check_list tbody").empty();
        $("#paginator_checklist_wrapper").empty();
        var html = '<div class="tip" style="text-align: center; font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.append(html);
    }

    function showWaitMsg(node) {
        var html = '<div class="tip" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">' +
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>' +
            '</div>';
        node.append(html);
    }

}, '0.0.1', {
    requires: [
        "w-tree",
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'w-paginator',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
