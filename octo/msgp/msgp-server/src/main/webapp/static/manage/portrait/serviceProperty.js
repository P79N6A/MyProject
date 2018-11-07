M.add('msgp-manage/portrait/serviceProperty', function(Y){
    Y.namespace('msgp.manage').serviceProperty = serviceProperty;
    var tbody = Y.one('#wrap_serviceProperty tbody'),
        colspan = 7;
    var previous = null;

    function serviceProperty(appkeytmp) {
        initTable(appkeytmp)
    }

    function initTable(appkeytmp) {
        setLoading();
        initClickGraph();

        // 自己设定的后端接口
        var url = '/manage/data/portrait/servicePropertyQpsFeatureData';
        Y.io(url, {
            method: 'get',
            data: {appkey: appkeytmp},
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (data.length !== 0) {
                            fillTableContent(appkeytmp, data);
                        } else if (data.length === 0) {
                            setEmpty();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                        setError();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                    setError();
                }
            }
        });
    }

    function fillTableContent(appkey, data) {
        // 数据格式在这里定义，自己仔细设计
        var html = '';
        var i = 0;
        var showData = data;
        var tdDatas = Array();
        tdDatas.push(""); //暂时占位
        tdDatas.push(appkey);
        tdDatas.push("&ensp;&ensp;&ensp;&ensp;&ensp;"+ judgeData(showData.qps_max));
        tdDatas.push("&ensp;&ensp;&ensp;&ensp;&ensp;"+ judgeData(showData.qps_min));
        var text="";
            text = "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;开发中";
        tdDatas.push(text);

        // 图片的标签
        var pic = "<a class='graph_click' id = "+"serviceProperty"+i+" name="+ appkey +" href='javascript:void(0);'>" + "&ensp;&ensp;&ensp;&ensp;"+
            "<img src='data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCA1MTIgNTEyIiB3aWR0aD0iMjRweCIgaGVpZ2h0PSIyNHB4Ij4KICA8Zz4KICAgIDxnPgogICAgICA8cG9seWdvbiBwb2ludHM9IjE2Ni4zLDI4Ni4yIDI1MS44LDM3Mi44IDQxMi4yLDIxNC4zIDQxMS4zLDMxNC40IDQzMi4yLDMxNC40IDQzMy4yLDE3Ny44IDI5Ny43LDE3Ni44IDI5Ny43LDE5Ny42IDM5OC45LDE5OC41ICAgICAyNTIuOSwzNDMuNiAxNjYuMywyNTcgMzAuOCwzOTUuNyA0NS40LDQxMC4zICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICAgIDxwb2x5Z29uIHBvaW50cz0iNDgwLjEsMTEgNDgwLjEsNDgwLjEgMTEsNDgwLjEgMTEsNTAxIDUwMSw1MDEgNTAxLDExICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K'>"+"</a>";
        tdDatas.push(pic);

        var text="";
            text = "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;开发中";
        tdDatas.push(text);

        html+=  "<tr id="+appkey+"><td>" + tdDatas.join("</td><td>") + "</td></tr>";

        tbody.setHTML(html);
    }

    function judgeData(data) {
        if(data<0 || ""==(data)){
            data = "-";
        }
        return data;
    }

    //触发点击图
    function initClickGraph() {
        // 将图片添加到当前行下面，并做到点击出现，再点击移除的功能
        var kpiTable = Y.one('#tagTableNew');
        kpiTable.delegate('click', function () {
            var value = $(this).attr("name");
            var now = new Date();
            if (!previous || (now - previous > 600)) {
                var _id = $(this).attr('id');
                if ($("." + _id).length) {
                    $("." + _id).remove();
                } else {
                    $("#showGraph").remove();
                    var str = '<tr id="showGraph" class=' + _id + '><td colspan="7" style="text-align: center;"></td></tr>';
                    setTimeout(function () {
                        // $(".widget-table-choose").after(str);
                        $("#"+  _id ).parent().parent().after(str);
                        showWaitMsg(Y.one("#showGraph td"));
                        addGraphDialog(value);
                    }, 0);
                }
                previous = now;
            } else {
                return;
            }
        }, '.graph_click');

        kpiTable.delegate('click', function () {
            var value = $(this).attr("name");
            var now = new Date();
            if (!previous || (now - previous > 600)) {
                var _id = $(this).attr('id');
                if ($("." + _id).length) {
                    $("." + _id).remove();
                } else {
                    $("#showGraph").remove();
                    var str = '<tr id="showGraph" class=' + _id + '><td colspan="7" style="text-align: center;"></td></tr>';
                    setTimeout(function () {
                        // $(".widget-table-choose").after(str);
                        $("#"+  _id ).parent().parent().after(str);
                        showWaitMsg(Y.one("#showGraph td"));
                        addGraphDialog(value);
                    }, 0);
                }
                previous = now;
            } else {
                return;
            }
        }, '.graph_click1');

    }

    function addGraphDialog(appkey) {
        getTrendData(appkey)
    }

    function getTrendData(appkey) {
        // 添加折线图
        var url = '/manage/data/portrait/servicePropertyQPSPicData';
        var data = {
            appkey: appkey
        };

        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    // alert(ret);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0 && ! $.isEmptyObject(arr)) {
                            fillCharts(arr);
                            // setLoading();       // 点击按钮弹出事件的过程是对的。而且再入的过程是对的
                        } else {
                            showDialogEmptyMsg();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showDialogEmptyMsg(!0);
                    }
                },
                failure: function () {
                    showDialogEmptyMsg(!0);
                }
            }
        });
    }

    function showDialogEmptyMsg(isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有数据') + '</div>';
        Y.one("#showGraph td").setContent(html);
        // _graphDialog.setContent(html);
    }

    function fillCharts(data) {
        // 填充图表，输入数据，下面进行分配
        var micro = new Y.Template();
        var template = Y.one('#text_graph').get('value');
        var str = micro.render(template);
        // Y.one("#showGraph").setHTML(str);
        Y.one("#showGraph td").setHTML(str);
        var graphs = Array("qps");
        // 如果想添加多张图表，就采用下面的方式来定义
        // var graphs = Array("availability", "count", "qps");
        graphs.forEach(function (item) {
            var node = Y.one("#screen_" + item) && Y.one("#screen_" + item).getDOMNode();
            var ec = node && echarts.init(node);
            if (ec) {
                ec.showLoading({
                    text: "loading"
                });
                var kpiOption = wrapOption(data);
                // var kpiOption = wrapOption(data, item);
                ec.hideLoading();
                ec.setOption(kpiOption);
            }
        });
    }

    function timeFormatter(num) {
        if (num < 1000) {
            return num + 'ms';
        } else if (num >= 1000 && num < 1000000) {
            return Math.round(num / 1000) + 's';
        }
    }

    function changeTime(nums){
        var hours = new Array();
        for(var i=0; i<nums.length; i++){
            var num = nums[i];
            var hour = parseInt(num/60);
            var half = num/30%2;
            hours.push(hour+":"+(half>0 ? '30' : '00'))
        }
        return hours
    }

    function changeShowDataFormat(seriesdata) {
        if(seriesdata != null){
            for(var i=0; i<seriesdata.length; i++){
                seriesdata[i] = seriesdata[i].toFixed(2);
            }
        }
        return seriesdata;
    }

    function wrapOption(obj) {
        // 开始描绘折线图
        var markAreaData = [];
        for (var i in obj.markZone) {
            markAreaData.push([{
                xAxis: obj.markZone[i][0]
            }, {
                xAxis: obj.markZone[i][1]
            }])
        }

        // 折线数值设置
        var series = [
            {
                name: 'QPS5Minutes',
                type: 'line',
                symbol: 'none',
                smooth: true,
                markArea: {
                    silent: true,
                    data: markAreaData
                },
                data: changeShowDataFormat(obj.all_qps5min_series)
            },
            {
                name: 'DXqps5Minutes',
                type: 'line',
                smooth: true,
                data: changeShowDataFormat(obj.dx_qps5min_series)
            },
            {
                name: 'YFqps5Minutes',
                type: 'line',
                smooth: true,
                data: changeShowDataFormat(obj.yf_qps5min_series)
            },
            {
                name: 'CQqps5Minutes',
                type: 'line',
                smooth: true,
                data: changeShowDataFormat(obj.cq_qps5min_series)
            },
            {
                name: 'GQqps5Minutes',
                type: 'line',
                smooth: true,
                data: changeShowDataFormat(obj.gq_qps5min_series)
            },
            {
                name: 'GHqps5Minutes',
                type: 'line',
                smooth: true,
                data: changeShowDataFormat(obj.gh_qps5min_series)
            }
        ];

        var legend = {
            bottom: 0,
            data: ['QPS5Minutes', 'DXqps5Minutes', 'YFqps5Minutes', 'CQqps5Minutes', 'GQqps5Minutes', 'GHqps5Minutes']
            // data: ['all_qps5min_series', 'dx_qps5min_series', 'yf_qps5min_series', 'cq_qps5min_series', 'gq_qps5min_series', 'gh_qps5min_series']
        };

        // 坐标轴的设置
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
                    name: '时间',
                    // data: (obj.xAxis),
                    data: changeTime(obj.xAxis),
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
                        show: true
                    }
                    ,
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

    function getData(arr, precision) {
        var result = [];
        for (var i = 0, l = arr.length; i < l; i++) {
            if (precision) {
                var y = (arr[i]);
                if (typeof(y) == "undefined") {
                    result.push(NaN);
                } else {
                    result.push((y).toFixed(2));

                }
            } else {
                result.push(arr[i]);
            }
        }
        return result;
    }

    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中waiting</span></div>';
        node && node.setHTML(html);
    }

    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中loading</td></tr>';
        tbody.setHTML(loadingHtml);
    }

    function setError() {
        var errorHtml = '<tr><td colspan="' + colspan + '">获取失败</td></tr>';
        tbody.setHTML(errorHtml);
    }

    function setEmpty() {
        var emptyHtml = '<tr><td colspan="' + colspan + '">没有数据</td></tr>';
        tbody.setHTML(emptyHtml);
    }

    function setHint() {
        var emptyHtml = '<tr><td colspan="' + colspan + '">暂无数据</td></tr>';
        tbody.setHTML(emptyHtml);
    }

}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'msgp-utils/msgpHeaderTip',
        'mt-date',
        'w-date',
        'msgp-utils/common',
        'msgp-service/commonMap'
    ]
});