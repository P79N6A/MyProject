M.add('msgp-manage/data_portrait', function (Y) {
    Y.namespace('msgp.manage').data_portrait = data_portrait;
    var appkey;
    var portraitWrapper, listWrapper;
    var graphsTemplate = [
        /* jshint indent: false */
        '<div id = "<%= this.id%>" class="charts-wrapper-out">',
        '<p style="word-break: break-all; overflow: hidden;width: 500px"></p>',
        '<div class="charts-wrapper" style="border: 0"></div>',
        '</div>'
    ].join('');

    var searching = false;

    function data_portrait(_appkey) {
        appkey = _appkey;
        document.title = "服务画像";

        initParams();
        initEvent();
    }

    function initParams() {
        portraitWrapper = Y.one('#div_data_portrait');
        listWrapper = portraitWrapper.one('#kpi_list');
        Y.msgp.service.setEnvText('portrait_env_select');
    }

    function initEvent() {
        portraitWrapper.one('#query_btn').delegate('click', function () {
            if (searching) return;
            portraitWrapper.all("#query_btn .btn").removeClass("btn-primary");
            this.addClass("btn-primary");
            getGraphsData("");
        }, ".btn");

        portraitWrapper.one("#portrait_env_select").delegate('click', function () {
            var el = this;
            if (el.hasClass('btn-primary')) return;
            portraitWrapper.all('#portrait_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getGraphsData("");
        }, "a");

        clearWaitMsg(listWrapper);
        //listWrapper && showWaitMsg(listWrapper);
        getGraphsData("");
        //getGraphsData("Weekday");
        //getGraphsData("Weekend");
    }

    function showWaitMsg(node) {
        var html = '<div style="margin:40px;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node.setHTML(html);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="margin:40px;font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

    function getGraphsData(weekProperty) {
        var env = portraitWrapper.one("#portrait_env_select a.btn-primary").getAttribute('value');
        var queryBtn = portraitWrapper.all("#query_btn .btn-primary");

        var data = {
            appkey: appkey,
            env: env,
            weekProperty: weekProperty
        };

        Y.msgp.utils.urlAddParameters(data);
        var conf = {
            method: 'get',
            data: data,
            on: {
                start: function () {
                    queryBtn.set('disabled', true);
                    queryBtn.setStyle('background', '#ccc');
                },
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr != null) {
                            fillGraphs(arr, weekProperty);
                        } else {
                            showEmptyErrorMsg(listWrapper);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(listWrapper, !0);
                    }
                },
                failure: function () {
                    searching = false;
                    showEmptyErrorMsg(listWrapper, !0);
                },
                complete: function () {
                    queryBtn.set('disabled', false);
                    queryBtn.setStyle('');
                }
            }
        };
        doAjax(conf);
    }

    function fillGraphs(data, weekProperty) {
        var id = "charts-wrapper-out-" + weekProperty;
        //var node = Y.Node.create(graphsTemplate);

        var macor = new Y.Template();
        var node = macor.render(graphsTemplate,
            {id: id});
        listWrapper.append(node);
        var node1 = portraitWrapper.one("#"+id);
        fillGraphsReal(node1, data, weekProperty)
    }

    function fillGraphsReal(dom, data, weekProperty) {
        var opt = wrapOption(data);
        //var title = "QPS " + weekProperty + " 画像"
        var title = "Load1min"
        dom.one('p').setHTML(title);
        var inner = dom.one('.charts-wrapper').getDOMNode();
        var ec = echarts.init(inner);
        ec.setOption(opt);
        dom.setData('chartData', ec.getOption().series);
    }

    function doAjax(conf) {
        searching = true;
        var url = '/manage/data/portrait';
        Y.io(url, conf);
    }

    function wrapOption(obj) {
        var markAreaData = [];
        for (var i in obj.markZone) {
            markAreaData.push([{
                xAxis: obj.markZone[i][0]
            }, {
                xAxis: obj.markZone[i][1]
            }])
        }

        var series = [
            {
                name: 'total-load',
                type: 'line',
                symbol: 'none',
                smooth: true,
                markArea: {
                    silent: true,
                    data: markAreaData
                },
                data: obj.series
            },
            {
                name: 'dx-load',
                type: 'line',
                smooth: true,
                data: obj.dx_series
            }
        ];

        var legend = {
            bottom: 0,
            data: [{name: '', icon: 'line'}]
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
                    data: obj.xAxis,
                    splitLine: {
                        show: false
                    }
                }
            ],
            yAxis: [
                {
                    type: 'value',
                    name: '',
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

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete',
        'template',
        'msgp-service/commonMap',
        'msgp-utils/msgpHeaderTip'
    ]
});
