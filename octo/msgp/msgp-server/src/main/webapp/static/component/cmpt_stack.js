M.add('msgp-component/cmpt_stack', function (Y) {
    Y.namespace('msgp.component').cmpt_stack = cmpt_stack;
    var stackWrapper;
    var stackChartsWrapper;
    var stackTipsWrapper;
    
    function cmpt_stack() {
        document.title = '事业群技术栈';
        stackWrapper = Y.one("#div_cmpt_stack");
        stackChartsWrapper = stackWrapper.one("#cmpt_stack_charts");
        stackTipsWrapper = stackWrapper.one("#cmpt_stack_tips");
        stackWrapper.one('#business').set('value', "技术工程及基础数据平台");
        bindWidget();
        getChartsData();
    }

    function bindWidget() {
        stackWrapper.one('#searchBtn').on('click', function () {
            getChartsData();
        });

        stackWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getChartsData();
        }, '#base_type');
    }

    function getChartsData() {
        stackChartsWrapper.empty();
        showWaitMsg(stackTipsWrapper);
        var base = stackWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = stackWrapper.one("#business").get('value');
        var category = stackWrapper.one("#category").get('value') == "all"  ? "": stackWrapper.one("#category").get('value');

        var searchData = {
            "base": base,
            "category": category,
            "business" : business
        };
        var url = '/component/category/outline';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.length == 0){
                            showEmptyErrorMsg(stackTipsWrapper, false);
                        }else {
                            clearWaitMsg(stackTipsWrapper);
                            fillCharts(data);
                        }
                    } else {
                        showEmptyErrorMsg(stackTipsWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(stackTipsWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillCharts(data) {
        //Generate Charts Wapper
        Y.Array.each(data,function(element){
            stackChartsWrapper.append("<div style='margin-bottom: 20px; margin-top: 20px; text-align: center'><div id='" + element.category + "_charts"  + "' style='width: 600px; height: 300px; float: left'" + "></div>" +
                "<div id='" + element.category + "_charts_detail'"  + " style='width: 600px; height: 300px; float: left; padding-left: 30px; text-align: center; font-size:14px; vertical-align: middle;'>点击右图组件查看版本详情</div></div>");
            stackChartsWrapper.append("<div style='padding-top: 30px; padding-bottom: 30px'><HR style='FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;' width='100%;' color=#987cb9 SIZE=1></div>");
            var chartsDOMNode = getChartsNode(stackWrapper.one("#" + element.category + "_charts"));
            chartsDOMNode.clear();
            chartsDOMNode.setOption(getChartsOption(element));
            var aa  = Math.max.apply(null, element.count);
            var maxIndex = element.count.indexOf(Math.max.apply(null, element.count));
            var dependency = element.components[maxIndex];
            getComponentDetail(dependency.groupId, dependency.artifactId, element.category);

            //绑定事件
            chartsDOMNode.on("click", function (parama) {
                var groupId = parama.name.split(":")[0];
                var artifactId = parama.name.split(":")[1];
                var category = parama.seriesName;
                getComponentDetail(groupId, artifactId, category)
            });
        });
    }

    function getChartsNode(node) {
        var DOMNode = node.getDOMNode();
        return echarts.init(DOMNode);
    }

    function getChartsOption(data) {
        var title = data.categoryName + "组件分布";
        var appCount = data.appCount;
        var appCountMax = appCount < 100 ? parseInt(appCount / 10) * 10 + 10 : parseInt(appCount / 100) * 100 + 100;
        var business = stackWrapper.one('#business option:checked').get('text');
        var legendData = [];
        Y.Array.each(data.components,function(element){
            legendData.push(element.groupId + ":" + element.artifactId)
        });
        var seriesData = data.count;
        option = {
            title: {
                text: title,
                //subtext: business,
                x: 'center'
            },
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    var groupId = params[0].name.split(":")[0];
                    var artifactId = params[0].name.split(":")[1];
                    var value = params[0].value;
                    var result =
                        '<table> ' +
                        '</caption>' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">GroupId: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + groupId + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">ArtifactId: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + artifactId + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">使用数: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '</table>' ;

                    return result
                },
                padding: 15
            },
            legend: {
                show: false,
                data: legendData
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {
                        show: true
                    },
                    dataView: {
                        show: false,
                        readOnly: false
                    },
                    magicType: {
                        show: false,
                        type: ['line', 'bar']
                    },
                    restore: {
                        show: false
                    },
                    saveAsImage: {
                        show: true
                    }
                }
            },
            calculable: true,
            xAxis: [{
                type: 'category',
                data: legendData,
                axisLabel: {
                    show: false
                },
                axisTick: {
                    interval: 0
                }
            }],
            yAxis: [{
                type: 'value',
                max: appCountMax
            }],
            series: [{
                name: data.category,
                type: 'bar',
                markLine: {
                    data: [
                        {
                            name: '事业群应用总数(' + appCount + ')',
                            yAxis: appCount
                        }
                    ],
                    label:{
                        normal: {
                            show: true,
                            position: 'middle',
                            formatter: '{b}'
                        }
                    }
                },
                itemStyle: {
                    normal: {
                        color: '#4fbba9',
                        barBorderRadius: [5, 5, 0, 0],
                        label: {
                            formatter: function (params) {
                                return params.name.split(':')[1] + ": " + params.value
                            },
                            show: true,
                            barBorderWidth: 6,
                            barBorderRadius: 3,
                            textStyle: {
                                color: '#FB7754 '
                            },
                            position: 'top'
                        }
                    }
                },
                data: seriesData
            }]
        };
        return option
    }

    function getComponentDetail(groupId, artifactId, category) {
        var business = stackWrapper.one("#business").get('value');
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId,
            "base": 'all',
            "business": business,
            "owt": "",
            "pdl": ""
        };
        var url = '/component/version_count';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.length == 0){
                        }else {
                            fillComponentDetail(data, groupId, artifactId, category);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillComponentDetail(data, groupId, artifactId, category) {
        var chartsDOMNode = getChartsNode(stackWrapper.one("#" + category + "_charts_detail"));
        var series = [];
        var legendData = [];
        var total = 0;
        Y.Array.each(data,function(element){
            series.push({
                name: element.version,
                value: element.count
            });
            legendData.push(element.version);
            total += element.count;
        });
        var option = {
            title : {
                text: '典型组件版本分布 [使用总数: ' + total + ']',
                subtext: groupId + ', ' + artifactId,
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: function (params) {
                    var versionName = params.name;
                    var value = params.value;
                    var percent = params.percent + "%";
                    var result =
                        '<table> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">版本名称: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + versionName + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">使用数量: </strong></td> ' +
                        '<td style="text-align: left; padding-left: 10px; color: #ffffff;">' + value + '</strong></td> ' +
                        '</tr> ' +
                        '<tr> ' +
                        '<td style="text-align: right; padding-left: 10px; color: #C0C0C0;">使用比例: </strong></td> ' +
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
                show: false,
                orient: 'vertical',
                x: 'left',
                data: legendData
            },
            series : [
                {
                    name: '版本分布',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data:series,
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ]
        };
        chartsDOMNode.clear();
        chartsDOMNode.setOption(option);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    function showWaitMsg(node) {
        var html = '<div style="text-align: center;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node.setHTML(html);
    }

    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="text-align: center;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'mt-date',
        'w-date'
    ]
});