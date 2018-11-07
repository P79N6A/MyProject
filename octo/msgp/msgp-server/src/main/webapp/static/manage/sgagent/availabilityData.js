M.add('msgp-manage/sgagent/availabilityData', function (Y) {
    Y.namespace('msgp.manage').availabilityData = availabilityData;

    var appkey;
    var wrapper = Y.one('#wrap_availabilityData');
    var allAvailabilityData = wrapper.one('#allAvailability');
    var oneAvailabilityData = wrapper.one('#oneAvailability');
    //var content_overlay = wrapper.one('.content-overlay');
    var availabilityChartWrapperOfMccServer = wrapper.one(getWholeNameOfServer('#availabilityChartOuter', 'mcc'));
    var availabilityChartWrapperOfMnsc = wrapper.one(getWholeNameOfServer('#availabilityChartOuter', 'mns'));

    function availabilityData(app) {
        appkey = app;
        bind();
        wrapper.one('#all_or_one a').simulate('click');
        getAvailabilityChartData("com.sankuai.cos.mtconfig", "mcc");
        getAvailabilityChartData("com.sankuai.inf.mnsc", "mns");
    }
    function bind() {
        wrapper.delegate('click', function () {
            var type = Number(this.getAttribute("value"));
            this.ancestor('div').all('a').removeClass('btn-primary');
            allAvailabilityData.hide();
            oneAvailabilityData.hide();
            switch (type) {
                case 1:
                    this.addClass('btn-primary');
                    oneAvailabilityData.show();
                    break;
                case 2:
                    this.addClass('btn-primary');
                    allAvailabilityData.show();
                    break;
            }
        }, '#all_or_one a');
    }
    function getWholeNameOfServer(name, type) {
        var wholeName = '';
        switch (type)
        {
            case 'mcc':
                wholeName = name + 'OfMccServer';
                break;
            case 'mns':
                wholeName = name + 'OfMnsc';
                break;
        }
        return wholeName;
    }

    function getAvailabilityChartData(appkey, type) {
        var url = '/manage/agent/availabilityData';
        var data = {
            appkey: appkey,
            serverType: type
        };
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            fillAvailabilityCharts(arr, type);
                        } else {
                            showDialogEmptyMsg();
                        }
                    } else {
                        showDialogEmptyMsg(!0);
                    }
                },
                failure: function () {
                    showDialogEmptyMsg(!0);
                }
            }
        });
    }

    function fillAvailabilityCharts(data, type) {
        var availabilityEc = echarts.init(document.getElementById(getWholeNameOfServer('showAvailability', type)));
        if (availabilityEc) {
            availabilityEc.showLoading({
                text: "loading"
            });

            var availabilityOption = availabilityWrapOption(data, type);
            availabilityEc.on("dataZoom", function(){
                setAverageAvailability(this, data, type);
            });
            availabilityEc.hideLoading();
            availabilityEc.setOption(availabilityOption, true);
            setAverageAvailability(availabilityEc, data, type);
        }
        ;
    }

    function setAverageAvailability(availabilityEc, data, type) {
        var opt = availabilityEc.getOption();
        var dz = opt.dataZoom[0];
        var start = dz.startValue;
        var end = dz.endValue;
        var startTime = opt.xAxis[0].data[start];
        var endTime = opt.xAxis[0].data[end];
        var html = '<table>' +
            '<tr><td style="font-size: 12px; color: #9d9d9d" colspan="2">'+startTime+'<span> - </span>'+endTime+'</td></tr>' +
            '<tr><td>平均成功率：</td>' +
            '<td style="font-size: 20px; color: #843534">'+ data.successCountPercent.toFixed(6)+ '%</td></tr>';
        if ('mcc' == type) {
            html += '<tr><td>HTTP接口：</td>' +
            '<td style="font-size: 20px; color: #843534">'+  data.thriftSuccessCountPercent.toFixed(6)+  '%</td></tr>' +
            '<tr><td>THRIFT接口：</td>' +
            '<td style="font-size: 20px; color: #843534">'+  data.httpSuccessCountPercent.toFixed(6)+  '%</td></tr>';
        }
        html += '</table>'
        $(getWholeNameOfServer("#averageAvailability", type)).html(html);
    }

    function availabilityWrapOption(data, type) {
        var legend;
        var series;
        var yAxis;
        series = [
            {
                name: '成功率',
                type: 'line',
                symbol: 'emptyCircle',
                showAllSymbol: true,
                symbolSize: 8,
                data: data.availabilityBasedClient
            }];
        var availabilityBasedClientDouble = [];
        for(var item in data.availabilityBasedClient){
            if(data.availabilityBasedClient[item] != "-")
                availabilityBasedClientDouble.push(data.availabilityBasedClient[item]*1);
        }
        var max = Math.max.apply(Math,availabilityBasedClientDouble);
        var min = Math.min.apply(Math,availabilityBasedClientDouble);
        var maxyAxis = max;
        var minyAxis = max - (max - min) * 2;
        if (minyAxis == 100 || minyAxis < 0) {
            minyAxis = 0;
        }
        yAxis = [
            {
                type: 'value',
                name: '成功率',
                scale: true,
                axisLine: {
                    show: true
                },
                axisLabel: {
                    show: true,
                    formatter: '{value}%'
                },
                min: minyAxis,
                max: maxyAxis,
            }];
        var option = {
            title: {
                text: '成功率趋势图',
                subtext: appkey,
                x: 'center'
            },
            animation: false,
            tooltip: {
                trigger: 'axis',
                formatter: function (params) {
                    var tableTitle = params[0].name;
                    tableTitle += ", 星期" + "天一二三四五六".charAt(new Date(tableTitle).getDay());
                    var index = params[0].dataIndex
                    var result =
                        '<div>' +
                        '<table style="background-color:rgba(0,0,0,0)"> ' +
                        '<caption>' + tableTitle +
                        '</caption>';
                    if ('mcc' == type) {
                        result += '<tr>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + params[0].seriesName + ':</td>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + params[0].value + '%' + '</td>' +
                            '</tr><tr>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'http接口' + ':</td>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.httpAvailabilityBasedClient[index] + '%'  + '</td>' +
                            '</tr><tr>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + 'thrift接口' + ':</td>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + data.thriftAvailabilityBasedClient[index] + '%'  + '</td>' +
                            '</tr>' ;
                    } else {
                        result += '<tr>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; color: #cccccc;">' + params[0].seriesName + ':</td>' +
                            '<td style="width: auto; padding-left: 15px; background-color:rgba(0,0,0,0); border:none; margin:none; color: #ffffff;">' + params[0].value + '%' + '</td>' +
                            '</tr>' ;
                    }

                    result += '</table></div>';
                    return result;
                },
                padding: 10
            },
            toolbox: {
                show: true,
                feature: {
                    mark: {show: true},
                    dataView: {show: true, readOnly: false},
                    magicType: {show: true, type: ['line', 'bar']},
                    restore: {show: true},
                    saveAsImage: {show: true},
                }
            },
            calculable: true,
            xAxis: [
                {
                    type: 'category',
                    data: data.dates,
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: yAxis,
            dataZoom: {
                show: true,
                start: 0,
            },
            series: series
        };
        return option;
    }

    function showDialogEmptyMsg(isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        availabilityChartWrapperOfMccServer.setContent(html);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'w-date',
        'mt-date',
        'transition',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/checkIP',
        'msgp-service/commonMap'
    ]
});