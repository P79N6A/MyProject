M.add('msgp-worth/coveragecount', function (Y) {
    var _report_style = "";
    var _echart = null;
    var orgId = 100046;
    var orgName = "技术工程及基础数据平台";
    var devs = ["算法开发", "系统开发", "前端开发", "后台开发", "运维", "QA"];
    var selectedDevs = [];
    var chartTitle = "";
    Y.namespace('msgp.worth').coveragecount = coveragecount;
    function coveragecount(report_style) {
        _report_style = report_style;
        document.title = '部门覆盖度统计';
        console.log(_report_style);
        Y.one("#business").set("text", orgName);
        Y.one("#selecttips").set("text", orgName);
        initOrgTree();
        initCharts();
        initDevSelector();
        initDatePicker();
        bindSearch();
        getChartData();
    }

    function initOrgTree(){
        var levelUrl = '/worth/daily/coverage/orgTreeLevel';
        var searchUrl = '/worth/daily/coverage/orgTreeSearch';
        new Y.mt.widget.Tree({
            elTarget: '#orgtree',
            position: 'inner',
            maxHeight: '600',
            skin: 'widget-tree-big',
            search: false,
            leafSelectableOnly: false,
            placeholder: '请输入拼音或中文搜索',
            levelAsync: {
                url: levelUrl
            },
            searchAsync: {
                url: searchUrl
            },
            afterChoose: function(nodeData) { // nodeData与mtorg-remote-service中的OrgTreeNodeVo对应
                orgId = nodeData.dataId;
                orgName = nodeData.name;
                Y.one("#business").set("text", orgName);
                Y.one("#selecttips").set("text", orgName);
                console.log("dataID: "+ nodeData.dataId + ", nodeName: "+ nodeData.name + " is selected.")
                //Y.one('#business').value = nodeData.name
            },
            noDataMsg: '用户不存在或者没有该应用的权限'
        });
    }

    function initCharts() {
        var node = Y.one("#coveragecount_charts").getDOMNode();
        _echart = echarts.init(node);

    }

    function bindSearch() {
        Y.one('#searchForm').delegate('click', function () {
            getChartData();
        }, '#searchBtn');
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

    function initDevSelector() {
        jQuery('#dev').multiselect({
            selectAllText: "选择全部",
            allSelectedText: "已选择全部",
            nonSelectedText: "未选择",
            placeholder: "职位类型",
            buttonWidth: '140px',
            includeSelectAllOption: true,
            selectAllNumber: true,
            buttonText: function (options, select) {
                var total = jQuery('#filter option').length;
                if (options.length === 0) {
                    return '全部类型';
                }
                else if (options.length < total && options.length > 3) {
                    return '已选择' + options.length + '类职位';
                } else if (options.length == total) {
                    return '已选择全部(' + total + ')';
                } else {
                    var labels = [];
                    options.each(function () {
                        if (jQuery(this).attr('label') !== undefined) {
                            var value = jQuery(this).attr('label').substr(0,2);
                            labels.push(value);
                        }
                        else {
                            var value =jQuery(this).html().substr(0,2);
                            labels.push(value);
                        }
                    });
                    return labels.join(', ') + '';
                }
            }
        });
    }

    function getDevs() {
        selectedDevs = [];
        jQuery('#dev option:selected').map(function (a, item) {
            selectedDevs.push(item.text);
        });
        if(selectedDevs.length == 0){
            selectedDevs = devs
        }
        return selectedDevs
    }

    function getChartData() {
        _echart.showLoading({
            text: "loading"
        });
        var start = Y.one("#start").get('value');
        var end = Y.one("#end").get('value');
        var devPos = getDevs();
        var searchData = {
            "start": start,
            "end": end,
            "orgid": orgId,
            "devpos": devPos
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/worth/daily/coverage';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.ccount.length == 0){
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

    function sort(values, index, length) {
        for (var i = 0; i < length; i++) {
            for (var j = 0; j < length - i - 1; j++) {
                if (values[j] < values[j + 1]) {
                    values[j] = values[j] ^ values[j + 1];
                    values[j + 1] = values[j] ^ values[j + 1];
                    values[j] = values[j] ^ values[j + 1];

                    index[j] = index[j] ^ index[j + 1];
                    index[j + 1] = index[j] ^ index[j + 1];
                    index[j] = index[j] ^ index[j + 1];
                }
            }
        }
    }

    function fillCharts(data) {
        var series = [];
        var legendData = [];
        var devTotal = [];
        var devNum = [];
        Y.Array.each(data.ccount, function (item) {
            series.push({
                name: item.orgname,
                type: 'line',
                symbol: 'none',
                data: item.rates,
                smooth: true,
                sampling: 'average'
            });
            legendData.push({
                icon: 'line',
                name: item.orgname
            });
            devTotal.push(item.devtotal);
            devNum.push(item.devnum);
        });
        chartTitle = orgName + " 覆盖率趋势统计";
        if(selectedDevs.length == devs.length){
            chartTitle += ' [所有开发者职位]'
        }else {
            chartTitle += ' [' + selectedDevs.join("/") + ']';
        }
        var legend = {
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
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    lineStyle: {
                        type: "dashed"
                    }
                },
                formatter: function (params) {
                    var names = [];
                    var values = [];
                    var index = [];
                    var tableTitle = params[0].name;
                    tableTitle += ", 星期" + "天一二三四五六".charAt(new Date(tableTitle).getDay());
                    var length = params.length;
                    for (var i = 0; i < length; i++) {
                        names[i] = params[i].seriesName;
                        values[i] = params[i].value * 10000;
                        index[i] = i;
                    }
                    sort(values, index, length);
                    var tableHeader =
                        '<table> ' +
                        '<caption>' + tableTitle +
                        '</caption>' +
                        '<tr> ' +
                        '<th style="text-align: left; padding-left: 15px; color: #3fab99;"><strong>部门名称</strong></th> ' +
/*
                        '<th style="text-align: left; padding-left: 15px; color: #3fab99;"><strong>开发者总人数</strong></th> ' +
*/
                        '<th style="text-align: left; padding-left: 15px; color: #3fab99;"><strong>活跃开发者</strong></th> ' +
                        '<th style="text-align: left; padding-left: 15px; color: #3fab99;"><strong>部门覆盖率</strong></th> ' +
                        '</tr> ' ;

                    for (var j = 0; j < length; j++) {
                        tableHeader += '<tr>' +
                            '<td style="width: auto; padding-left: 15px">' + names[index[j]]+ '</td>' +
/*
                            '<td style="width: auto; padding-left: 15px">' + devTotal[index[j]] + '</td>' +
*/
                            '<td style="width: auto; padding-left: 15px">' + devNum[index[j]][params[j].dataIndex] + '</td>' +
                            '<td style="width: auto; padding-left: 15px">' + Number(values[j] / 100 ).toFixed(2) + "%" + '</td>' +
                            '</tr>' ;
                    }
                    tableHeader += '</table>';
                    return tableHeader;
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
        _echart.hideLoading();
        _echart.clear();
        _echart.setOption(option);
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
