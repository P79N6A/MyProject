M.add('msgp-manage/portrait/serviceResource', function(Y){
    Y.namespace('msgp.manage').serviceResource = serviceResource;
    var tbody = Y.one('#wrap_serviceResource tbody'),
        colspan = 9;
    var previous = null;

    function serviceResource(appkeytmp) {
        initTable(appkeytmp)
    }

    function initTable(appkeytmp) {
        setLoading();
        initClickGraph();

        // 自己设定的后端接口
        var url = '/manage/data/portrait/serviceResourceLoadFeatureData';
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

        setToolTip();
    }

    function setToolTip(){
        $(".load1min_perCPU").tooltip();
        $(".net_in").tooltip();
        $(".net_out").tooltip();
        $(".net_total").tooltip();
        $(".jvm_count").tooltip();
        $(".jvm_running").tooltip();
    }

    function fillTableContent(appkey, data) {
        // 数据格式在这里定义，自己仔细设计
        var html = '';
            var showData = data;
            var tdDatas = Array();
            tdDatas.push("");
            tdDatas.push(appkey);

            tdDatas.push("" + judgeData(showData.load1min_max) + "/" + judgeData(showData.load1min_min) + "/" + judgeData(showData.load1min_avg));

            tdDatas.push("" + showDataToM(showData.net_in_max) + "/" + showDataToM(showData.net_in_min) + "/" + showDataToM(showData.net_in_avg));

            tdDatas.push("" + showDataToM(showData.net_out_max) + "/" + showDataToM(showData.net_out_min) + "/" + showDataToM(showData.net_out_avg));

            tdDatas.push("" + showDataToM(showData.net_total_max) + "/" + showDataToM(showData.net_total_min) + "/" + showDataToM(showData.net_total_avg));

            tdDatas.push("" + judgeData(showData.thread_num_max) + "/" + judgeData(showData.thread_num_min) + "/" + judgeData(showData.thread_num_avg));

            tdDatas.push("" + judgeData(showData.thread_runnable_num_max) + "/" + judgeData(showData.thread_runnable_num_min) + "/" + judgeData(showData.thread_runnable_num_avg));

            // 图片的标签
            var pic = "<a class='graph_click' id = "+"serviceResource" + " name="+ appkey +" href='javascript:void(0);'>" + "&ensp;&ensp;"+
                "<img src='data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4KPCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj4KPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCA1MTIgNTEyIiB3aWR0aD0iMjRweCIgaGVpZ2h0PSIyNHB4Ij4KICA8Zz4KICAgIDxnPgogICAgICA8cG9seWdvbiBwb2ludHM9IjE2Ni4zLDI4Ni4yIDI1MS44LDM3Mi44IDQxMi4yLDIxNC4zIDQxMS4zLDMxNC40IDQzMi4yLDMxNC40IDQzMy4yLDE3Ny44IDI5Ny43LDE3Ni44IDI5Ny43LDE5Ny42IDM5OC45LDE5OC41ICAgICAyNTIuOSwzNDMuNiAxNjYuMywyNTcgMzAuOCwzOTUuNyA0NS40LDQxMC4zICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICAgIDxwb2x5Z29uIHBvaW50cz0iNDgwLjEsMTEgNDgwLjEsNDgwLjEgMTEsNDgwLjEgMTEsNTAxIDUwMSw1MDEgNTAxLDExICAgIiBmaWxsPSIjMzJjMmM3Ii8+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K'>"+"</a>";
            tdDatas.push(pic);

            html += "<tr id="+showData.appkey+"><td>" + tdDatas.join("</td><td>") + "</td></tr>";

            tbody.setHTML(html);
    }

    function judgeData(data) {
        if(data<0 || ""==(data)){
            data = "-";
        }
        return data;
    }

    function showDataToM(data) {
        if(""==(data)){
            return "-";
        }
        if(parseInt(data)>1000000){
            return (parseInt(data)/1000000).toFixed(2).toString()+"M";
        }else if(parseInt(data)>1000){
            return (parseInt(data)/1000).toFixed(2).toString()+"K";
        }else{
            return data;
        }
    }

    //触发点击图
    function initClickGraph() {
        var kpiTable = Y.one('#tagTableNewNew');
        kpiTable.delegate('click', function () {
            var value = $(this).attr("name");
            var now = new Date();
            if (!previous || (now - previous > 600)) {
                var _id = $(this).attr('id');
                if ($("." + _id).length) {
                    $("." + _id).remove();
                } else {
                    $("#showGraph").remove();
                    str = '<tr id="showGraph" class=' + _id + '><td colspan="9" style="text-align: center;"></td></tr>';
                    setTimeout(function () {
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

    }

    function addGraphDialog(appkey) {
        getTrendData(appkey)
    }

    function getTrendData(appkey) {
        var url = '/manage/data/portrait/serviceResourceLoadPicData';
        var data = {
            appkey: appkey
        };

        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0  && ! $.isEmptyObject(arr)) {
                            // 到目前为止，数据部分都是对的。但是要记得前面的appkey需要自己取出来
                            fillCharts(arr);
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
    }

    function fillCharts(data) {
        //数据拆分成 1：count报表，2：qps,tp,时间报表
        var micro = new Y.Template();
        var template = Y.one('#text_graph').get('value');
        var str = micro.render(template);
        // Y.one("#showGraph").setHTML(str);
        Y.one("#showGraph td").setHTML(str);
        var graphs = Array("qps");
        // var graphs = Array("availability", "count", "qps");
        graphs.forEach(function (item) {
            var node = Y.one("#screen_" + item) && Y.one("#screen_" + item).getDOMNode();
            var ec = node && echarts.init(node);
            if (ec) {
                ec.showLoading({
                    text: "loading"
                });
                // 此处的数据都已经变成要使用的
                var kpiOption = wrapOption(data);
                // var kpiOption = wrapOption(data, item);
                ec.hideLoading();
                ec.setOption(kpiOption);
            }
        });
    }

    function wrapOption(obj) {
        // 进行主要的提取数据过程，用于画图像。要想修改图像的样式，就在这里面进行修改就好了
        // 主要使用了baidu的echarts，具体使用可以百度
        var markAreaData = [];
        // 选择区域去重点显示
        for (var i in obj.markZone) {
            markAreaData.push([{
                xAxis: obj.markZone[i][0]
            }, {
                xAxis: obj.markZone[i][1]
            }])
        }

        // 拿到所有的展示数据
        var series = [
            // 分项去添加数据
            {
                name: 'total-load',
                type: 'line',
                symbol: 'none',
                smooth: true,
                markArea: {
                    silent: true,
                    data: markAreaData
                },
                data: obj.all_load1min_series
            },
            {
                name: 'dx-load',
                type: 'line',
                smooth: true,
                data: obj.dx_load1min_series
            },
            {
                name: 'cq-load',
                type: 'line',
                smooth: true,
                data: obj.cq_load1min_series
            },
            {
                name: 'gq-load',
                type: 'line',
                smooth: true,
                data: obj.gq_load1min_series
            },
            {
                name: 'yf-load',
                type: 'line',
                smooth: true,
                data: obj.yf_load1min_series
            },
            {
                name: 'gh-load',
                type: 'line',
                smooth: true,
                data: obj.gh_load1min_series
            }
        ];

        // 图标
        var legend = {
            bottom: 0,
            // data: [{name: '', icon: 'line'}]
            data: ['total-load', 'dx-load', 'cq-load', 'gq-load', 'yf-load', 'gh-load']
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
            // 修改横坐标属性
            xAxis: [
                // 修改横坐标属性
                {
                    type: 'category',
                    data: obj.xAxis,
                    name: 'time',
                    splitLine: {
                        show: false
                    }
                }
            ],
            // 修改纵坐标属性
            yAxis: [
                // 纵坐标属性的修改
                {
                    type: 'value',
                    name: 'Load',
                    axisLine: {
                        show: true
                    }
                    ,
                    axisLabel: {
                        formatter: numFormatter
                    }
                }
            ],
            // 指定图标类型
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

    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中</span></div>';
        node && node.setHTML(html);
    }

    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中</td></tr>';
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
        'mt-date',
        'w-base',
        'w-date',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/common',
        'msgp-service/commonMap'
    ]
});