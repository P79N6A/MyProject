/**
 * Created by yves on 16/8/8.
 */

M.add('msgp-component/cmpt_trend', function (Y) {
    var tab_style = "";
    var _echart = null;
    var limitNumber = 10;
    var groupIdCache = {};
    var groupId;
    var artifactIdCache= {};
    var artifactId;
    var version;
    var dailyTotal;
    var trendWrapper;
    var trendTipsWrapper;
    
    Y.namespace('msgp.component').cmpt_trend = cmpt_trend;
    function cmpt_trend(_tab_style, _groupId, _artifactId) {
        tab_style = _tab_style;
        document.title = '组件使用趋势';
        groupId = _groupId;
        artifactId = _artifactId;
        trendWrapper = Y.one("#div_cmpt_trend");
        trendTipsWrapper = trendWrapper.one("#cmpt_trend_tips");
        initWidget();
        initCharts();
        bindWidget();
        setDefaulValue();
    }

    function setDefaulValue() {
        trendWrapper.one("#groupId" ).set("value", groupId);
        trendWrapper.one("#artifactId" ).set("value", artifactId);
        getVersion(groupId, artifactId);
    }

    function getOwt(business) {
        trendWrapper.one('#owt').empty();
        trendWrapper.one('#owt').append('<option value=all>all</option>');
        trendWrapper.one('#pdl').empty();
        trendWrapper.one('#pdl').append('<option value=all>all</option>');
        var url = '/component/owt';
        Y.io(url, {
            method: 'get',
            data: {
                "business" : business
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            trendWrapper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartData();
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

    function initWidget() {
        $("#groupId" ).autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in groupIdCache ){
                    response(groupIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/group_id", {
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    groupIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                groupId = ui.item.value;
                $("#artifactId" ).attr("value", "");
                $('#version').empty();
                $('#version').append('<option value=all>all</option>');
            }
        });

        $("#artifactId").autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in artifactIdCache ){
                    response(artifactIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/artifact_id", {
                    groupId: groupId,
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    artifactIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                artifactId = ui.item.value;
                getVersion(groupId, artifactId);
            }
        });
        initCmpt();
        initDatePicker();
    }

    function initCmpt() {
        trendWrapper.one('#cmpt').empty();
        var url = '/component/cmpt';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            trendWrapper.one('#cmpt').append('<option value=' + item.groupId +',' + item.artifactId + '>' + item.artifactId + '</option>');
                        });
                        trendWrapper.one('#cmpt').set('value',groupId+',' + artifactId);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                }
            }
        });
    }

    function getVersion(groupId, artifactId) {
        trendWrapper.one('#version').empty();
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId
        };
        var url = '/component/version';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        trendWrapper.one('#version').append('<option value=all>all</option>');
                        Y.each(ret.data, function (item) {
                            trendWrapper.one('#version').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartData();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                }
            }
        });
    }

    function bindWidget() {
        trendWrapper.one('#searchBtn').on('click', function () {
            getChartData();
        });

        trendWrapper.one('#business').on('change', function () {
            var business = this.get('value');
            if(business == "-1") {
                trendWrapper.one('#owt').empty();
                trendWrapper.one('#pdl').empty();
                trendWrapper.one('#owt').append('<option value=all>all</option>');
                trendWrapper.one('#pdl').append('<option value=all>all</option>');
                getChartData();
            }else{
                getOwt(business);
            }
        });
        
        trendWrapper.one('#owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                trendWrapper.one('#pdl').empty();
                trendWrapper.one('#pdl').append('<option value=all>all</option>');
                getChartData();
            }else{
                getPdl(owt);
            }
        });

        trendWrapper.one('#pdl').on('change', function () {
            getChartData();
        });

        trendWrapper.one('#cmpt').on('change', function () {
            var cmpt = this.get('value').split(",");
            groupId = cmpt[0];
            artifactId = cmpt[1];
            trendWrapper.one("#groupId" ).set("value", groupId);
            trendWrapper.one("#artifactId" ).set("value", artifactId);
            getVersion(groupId, artifactId);
            getChartData();
        });

        trendWrapper.one('#version').on('change', function () {
            getChartData();
        });

        trendWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getChartData();
        }, '#base_type');
    }

    function getPdl(owt) {
        trendWrapper.one('#pdl').empty();
        trendWrapper.one('#pdl').append('<option value=all>all</option>');
        var url = '/component/pdl';
        Y.io(url, {
            method: 'get',
            data: {
                "owt" : owt
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            trendWrapper.one('#pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getChartData();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                }
            }
        });
    }

    function initCharts() {
        var node = trendWrapper.one("#cmpt_trend_charts").getDOMNode();
        _echart = echarts.init(node);
    }

    function initDatePicker() {
        var now = new Date();
        var startInput = trendWrapper.one('#start');
        var endInput = trendWrapper.one('#end');
        var end_day = new Date(now - 24 * 60 * 60 * 1000);
        var start_day = new Date(end_day - 15*24 * 60 * 60 * 1000);
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
    
    function getChartData() {
        _echart.showLoading({
            text: "loading"
        });
        var start = trendWrapper.one("#start").get('value');
        var end = trendWrapper.one("#end").get('value');
        var base = trendWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = trendWrapper.one("#business").get('value');
        var owt = trendWrapper.one("#owt").get('value') == "all"  ? "": trendWrapper.one("#owt").get('value');
        var pdl = trendWrapper.one("#pdl").get('value') == "all"  ? "": trendWrapper.one("#pdl").get('value');
        version = trendWrapper.one("#version").get('value') == "all" ? "": trendWrapper.one("#version").get('value');
        artifactId = Y.Lang.trim(trendWrapper.one("#artifactId").get('value'));
        groupId =  Y.Lang.trim(trendWrapper.one("#groupId").get('value'));
        var searchData = {
            "start": start,
            "end": end,
            "base": base,
            "business":business,
            "owt": owt,
            "pdl": pdl,
            "groupId": groupId,
            "artifactId": artifactId,
            "version": version
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/trend';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.trends.length == 0){
                            _echart.hideLoading();
                            _echart.clear();
                            showEmptyErrorMsg(trendTipsWrapper, false);
                        }else {
                            trendTipsWrapper.empty();
                            fillCharts(data);
                        }
                    } else {
                        _echart.clear();
                        _echart.hideLoading();
                        showEmptyErrorMsg(trendTipsWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    _echart.clear();
                    _echart.hideLoading();
                    showEmptyErrorMsg(trendTipsWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillCharts(data) {
        var value = [];
        var series = [];
        var legendData = [];
        dailyTotal = data.dailyTotal;
        Y.Array.each(data.trends,function(element){
            var name = element.name.length > 4 ? element.name.substring(0,4) : element.name;
            series.push({
                name: name,
                type:'line',
                symbol: 'none',
                data: element.counts,
                smooth: true,
                sampling: 'average'
            });
            legendData.push({
                icon: 'line',
                name: name
            });
        });
        
        var legend = {
            data: legendData,
            x: 'center',
            y: 'bottom'
        };
        var subTitle = version == "all"  ? "" : ", " + version;
        subTitle = groupId + ', ' + artifactId + subTitle;
        var option = {
            title : {
                text: '组件使用趋势变化',
                subtext: subTitle,
                left: 'center'
            },
            grid: {
                borderWidth: 0
            },
            calculable : true,
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
                    scale: false,
                    axisLine: {
                        show: false
                    }
                }
            ],
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
            series: series,
            legend: legend,
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    var arr = [];
                    var length = params.length;
                    for (var i = 0; i < length; i++) {
                        arr.push({
                           name: params[i].seriesName,
                           value: params[i].value
                        });
                    }
                    arr.sort(function(x, y){
                        return x.value < y.value ? 1 : -1;
                    });
                    var tableTitle = params[0].name;
                    tableTitle += ", 星期" + "天一二三四五六".charAt(new Date(tableTitle).getDay());
                    var total = dailyTotal[params[0].dataIndex];
                    tableTitle += ", 总量: " + total;
                    var type = (trendWrapper.one("#owt").get("value") == "all" || trendWrapper.one("#owt").get("value") == "")? "业务线": "产品线";

                    var tooltipStr =
                        '<table> ' +
                        '<caption>' + tableTitle +
                        '</caption>' +
                        '<tr> ' +
                        '<th style="text-align: left; padding-left: 15px; color: #3fab99;"><strong>' + type + '</strong></th> ' +
                        '<th style="text-align: left; padding-left: 15px; color: #3fab99;"><strong>使用量</strong></th> ' +
                        '</tr> ' ;

                    for (var j = 0; j < length; j++) {
                        tooltipStr += '<tr>' +
                            '<td style="width: auto; padding-left: 15px">' + arr[j].name+ '</td>' +
                            '<td style="width: auto; padding-left: 15px">' + arr[j].value + '</td>' +
                            '</tr>' ;
                    }
                    tooltipStr += '</table>';
                    return tooltipStr;
                },
                padding: 15
            }
        };
        _echart.hideLoading();
        _echart.clear();
        _echart.setOption(option);    
    }

    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="text-align: center;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

    }, '0.0.1', {
    requires: [
        "w-tree",
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
