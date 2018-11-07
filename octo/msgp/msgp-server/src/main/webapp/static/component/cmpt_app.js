/**
 * Created by yves on 16/9/27.
 */
M.add('msgp-component/cmpt_app', function (Y) {
    var tab_style = "";
    var appWrappper;
    var appCountWrapper;
    var appActivenessWrapper;
    var dept;
    Y.namespace('msgp.component').cmpt_app = cmpt_app;
    function cmpt_app(_tab_style, _groupId, _artifactId) {
        document.title = '线上应用分布';
        tab_style = _tab_style;
        appWrappper = Y.one("#div_cmpt_app");
        initWidget();
        bindWidget();
        initCharts();
        refreshData();
    }

    function initWidget() {
        pbody = appWrappper.one('#paginator_version_wrapper');
        appWrappper.one('#business').set('value', "");
        everPaged = false;
    }

    function bindWidget() {
        appWrappper.one('#searchBtn').on('click', function () {
            refreshData();
        });

        appWrappper.one('#business').on('change', function () {
            var business = this.get('value');
            if(business == "") {
                appWrappper.one('#owt').empty();
                appWrappper.one('#pdl').empty();
                appWrappper.one('#owt').append('<option value=all>all</option>');
                appWrappper.one('#pdl').append('<option value=all>all</option>');
                refreshData();
            }else {
                getOwt(business);
            }
        });

        appWrappper.one('#owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                appWrappper.one('#pdl').empty();
                appWrappper.one('#pdl').append('<option value=all>all</option>');
                refreshData();
            }else{
                getPdl(owt);
            }
        });

        appWrappper.one('#pdl').on('change', function () {
            refreshData();
        });

        appWrappper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            refreshData();
        }, '#base_type');
    }

    function getOwt(business) {
        appWrappper.one('#owt').empty();
        appWrappper.one('#owt').append('<option value=all>all</option>');
        appWrappper.one('#pdl').empty();
        appWrappper.one('#pdl').append('<option value=all>all</option>');
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
                            appWrappper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        refreshData();
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

    function getPdl(owt) {
        appWrappper.one('#pdl').empty();
        appWrappper.one('#pdl').append('<option value=all>all</option>');
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
                            appWrappper.one('#pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                        refreshData();
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
        appCountWrapper = echarts.init(appWrappper.one("#cmpt_app_count").getDOMNode());
        appActivenessWrapper = echarts.init(appWrappper.one("#cmpt_activeness_count").getDOMNode());
    }

    function includeChineseChar(str){
        var reg = /[\u4E00-\u9FA5\uF900-\uFA2D]/;
        return reg.test(str);
    }

    function refreshData() {
        var base = appWrappper.one('#base_select a.btn-primary').getAttribute('value');
        var business = appWrappper.one("#business").get('value');
        var owt = appWrappper.one("#owt").get('value') == "all"  ? "": appWrappper.one("#owt").get('value');
        var pdl = appWrappper.one("#pdl").get('value') == "all"  ? "": appWrappper.one("#pdl").get('value');
        var searchData = {
            "base": base,
            "business":business,
            "owt":owt,
            "pdl":pdl
        };
        if(business == "") {
            dept = "所有部门";
        }else if(owt == "") {
            dept = business;
        }else if(pdl == ""){
            dept = business + '-' + owt;
        }else{
            dept = business + '-' + owt + '-' + pdl;
        }

        getAppCountChartData(appCountWrapper, searchData);
        getAppActivenessChartData(appActivenessWrapper, searchData);
    }

    function getAppCountChartData(echart, searchData) {
        echart.showLoading({
            text: "loading"
        });
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/app_count';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.length == 0){
                            echart.hideLoading();
                            echart.clear();
                        }else {
                            fillAppCountCharts(data, echart);
                        }
                    } else {
                        echart.clear();
                        echart.hideLoading();
                        Y.msgp.utils.msgpHeaderTip('error', '获取应用分布数据失败', 3);
                    }
                },
                failure: function () {
                    echart.clear();
                    echart.hideLoading();
                    Y.msgp.utils.msgpHeaderTip('error', '获取应用分布数据失败', 3);
                }
            }
        });
    }

    function getAppActivenessChartData(echart, searchData) {
        echart.showLoading({
            text: "loading"
        });
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/app_activeness';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.length == 0){
                            echart.hideLoading();
                            echart.clear();
                        }else {
                            fillAppActivenessCharts(data, echart);
                        }
                    } else {
                        echart.clear();
                        echart.hideLoading();
                        Y.msgp.utils.msgpHeaderTip('error', '获取活跃度数据失败', 3);
                    }
                },
                failure: function () {
                    echart.clear();
                    echart.hideLoading();
                    Y.msgp.utils.msgpHeaderTip('error', '获取活跃度数据失败', 3);
                }
            }
        });
    }

    function fillAppCountCharts(data, echart) {
        var seriesData = [];
        Y.Array.each(data.names,function(name, index){
            seriesData.push({
                name: name,
                value: data.appCounts[index]
            })
        });
        var option = {
            title : {
                text: '应用分布统计',
                subtext: dept,
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: function (params) {
                    var name = params.name;
                    var value = params.value;
                    var percent = params.percent + "%";
                    var result =
                        '<table> ' +
                        '</caption>' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">名称: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + name + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">数量: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">比例: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + percent + '</strong></td> ' +
                        '</tr> ' +
                        '</table>' ;

                    return result
                },
                padding: 15
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
                orient: 'horizontal',
                x: 'center',
                y: 'bottom',
                show: false,
                data: data.names
            },
            series : [
                {
                    name: '应用分布统计',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data: seriesData,
                    itemStyle: {
                        normal : {
                            label : {
                                show : true,
                                formatter: function (params) {
                                    var name = params.name;
                                    if(includeChineseChar(name)) {
                                        name = name.slice(0, 4)
                                    }
                                    return name + ': ' + params.value + ' (' + params.percent + '%)'
                                }
                            },
                            labelLine : {
                                show : true
                            }
                        },
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ]
        };

        echart.hideLoading();
        echart.clear();
        echart.setOption(option);
    }

    function fillAppActivenessCharts(data, echart) {
        var seriesData = [];
        Y.Array.each(data.names,function(name, index){
            seriesData.push({
                name: name,
                value: data.apps[index]
            })
        });
        var option = {
            title : {
                text: '周活跃应用',
                subtext: dept,
                x:'center'
            },
            tooltip : {
                trigger: 'axis',
                formatter: function (params) {
                    var name = params[0].name;
                    var value = params[0].value;
                    var result =
                        '<table> ' +
                        '</caption>' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">应用名: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + name + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">活跃数: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '</table>' ;
                    return result
                },
                padding: 15
            },
            legend: {
                data: ['周活跃次数'],
                show: false
            },
            toolbox: {
                show : true,
                feature : {
                    mark : {show: true},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            calculable : true,
            xAxis : [
                {
                    type : 'value',
                    boundaryGap : [0, 0.01]
                }
            ],
            yAxis : [
                {
                    type : 'category',
                    data : data.apps,
                    axisLabel:{
                        show: false
                    }
                }
            ],
            series : [
                {
                    name:'周活跃次数',
                    type:'bar',
                    data:data.counts,
                    itemStyle: {
                        normal: {
                            label: {
                                formatter: function (params) {
                                    var simpleName= params.name;
                                    var lastIndex= simpleName.lastIndexOf('.');
                                    if(lastIndex != -1) {
                                        simpleName = simpleName.slice(lastIndex + 1)
                                    }
                                    return simpleName + ": " + params.value
                                },
                                show: true,
                                barBorderWidth: 6,
                                barBorderRadius: 3,
                                textStyle: {
                                    color: '#FFFFFF '
                                },
                                position: 'insideRight'
                            }
                        }
                    }
                }
            ]
        };
        echart.hideLoading();
        echart.clear();
        echart.setOption(option);
    }

}, '0.0.1', {
    requires: [
        "w-tree",
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'template',
        'msgp-utils/common',
        'w-paginator',
        'msgp-utils/msgpHeaderTip'
    ]
});
