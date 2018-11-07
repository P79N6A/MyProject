/**
 * highchart
 */
(function () {
    M.add('msgp-hchartutil', function (Y) {
        Y.namespace('msgp');
        var $Util = Y.mt.util,
            $IO = Y.mt.io,
            $NL = Y.NodeList,
            $N = Y.Node,
            $Base = Y.mt.poi,
            $Path = M.urlPrefix;

        //全局常量
        var toolTip = "<span style='color:{color}'>{name}</span>:<b>{value}</b>";
        var Highcharts = window.Highcharts;
        Y.msgp.HChartUtil = Y.Base.create('hchartutil', Y.Base, [], {
            /**
             * 获取x轴
             * @private
             * @method
             * @param ${Object} data
             * @return ${return}
             */
            _getXAxis: function (data) {
                var xAxis_cg = [];
                for (var i = 0; i < data.length; ++i) {
                    var d = data[i];
                    //添加x轴
                    xAxis_cg.push(d.time.split(" ")[1]);
                }
                return xAxis_cg;
            },
            /**
             * 获取series对象
             * @private
             * @method _getSeriesInstance
             * @param ${Object} data
             * @return ${return}
             */
            _getSeriesInstance: function (data, name, field, hasXaxis) {
                var array = [];
                var deltTime = 8 * 3600 * 1000;
                for (var i = 0; i < data.length; ++i) {
                    var d = data[i];
                    array.push({y: parseFloat(d[field], 10), x: (hasXaxis ? undefined : d.millisecond + deltTime), date: d.second});
                }
                return array;
            },
            /**
             * 获取列数据
             * @param data
             * @param name
             * @param field
             * @param hasXaxis
             * @return {${return}}
             * @private
             */
            _getSeriesData: function (data, name, field, hasXaxis) {
                return this._getSeriesInstance(data, name, field, hasXaxis);
            },
            /**
             * 获取chart配置参数
             * @private
             * @method _getChartConf
             * @param ${Object} data
             * @return ${return}
             */
            _getChartConf: function (data, type) {
                var chartConf = {
                    chart: {
                        type: type,
                        renderTo: this.get("nodeId")
                    },
                    rangeSelector: {
                        selected: 3
                    },
                    title: {
                        text: null
                    },
                    //xAxis: {
                    //categories: this._getXAxis(data)
                    //},
                    yAxis: [
                        { // Primary yAxis
                            labels: {
                                formatter: function () {
                                    return this.value + ' ';
                                },
                                style: {
                                    color: '#89A54E'
                                }
                            },
                            title: {
                                text: null,
                                style: {
                                    color: '#89A54E'
                                }
                            }
                        }
                    ],
                    legend: {
                        backgroundColor: '#FFFFFF',
                        enabled: true,
                        align: 'right',
                        borderWidth: 2,
                        layout: 'vertical',
                        verticalAlign: 'top',
                        y: 100
                    },
                    credits: {
                        enabled: true,
                        text: "meituan.com",
                        href: "http://www.meituan.com"
                    },
                    tooltip: {
                        formatter: function () {
                            var points = this.points;
                            var tip = [points[0].point.date];
                            for (var i = 0; i < points.length; i++) {
                                var point = points[i];
                                tip.push(Y.Lang.sub(toolTip, {
                                    name: point.series.name,
                                    value: point.y + "",
                                    color: point.series.color
                                }));
                            }
                            return tip.join("<br />");
                        },
                        //pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b><br />',
                        shared: true
                    },
                    plotOptions: {
                        column: {
                            dataLabels: {
                                enabled: true,
                                color: 'white'
                            }
                        },
                        line: {
                            events: {
                                legendItemClick: function () {
                                    var series = this.chart.series;
                                    var name = this.name;
                                    for (var i = 0; i < series.length; i++) {
                                        var s = series[i];
                                        if (s.name === name + "(昨)" && s !== this) {
                                            if (this.visible) {
                                                s.hide();
                                            } else {
                                                s.show();
                                            }
                                            break;
                                        }
                                    }
                                    //return false;
                                    // <== returning false will cancel the default action
                                }
                            }
                        }
                    },
                    global: {
                        useUTC: false
                    },
                    crosshair: true,
                    zoomType: 'x'
                };
                if (!data || data.length === 0) {
                    chartConf.title = {
                        text: "暂无数据"
                    };
                }
                if (type === "line") {
                    chartConf.tooltip.crosshairs = [true];
                }
                return chartConf;
            },
            /**
             * 创建Series
             * @private
             * @method _createSeries
             * @param ${Object} data
             * @return ${Array}
             */
            _createSeries: function (series, data, metas, hideInLegend, isYesterday, hasXaxis) {
                var type = "line";
                for (var i = 0; i < metas.length; i++) {
                    var line = metas[i];
                    series.push({
                        name: line['name'],
                        showInLegend: !hideInLegend,
                        type: type,
                        data: this._getSeriesData(data, line['name'], line['field'], hasXaxis)
                    });
                }
                ;
            },
            /**
             * 创建HighChart实例
             * @method createChart
             * @param ${Object} data
             * @return ${return}
             */
            createChart: function (data, metas) {
                var chartConf = this._getChartConf(data, 'line');
                var series = [];
                this._createSeries(series, data, metas);
                chartConf.series = series;
                return new Highcharts.StockChart(chartConf);
            },
            /**
             * 创建对比HighChart实例
             * @method createChart
             * @param ${Object} data
             * @return ${return}
             */
            createGroupChart: function (dataToday, dataYesterday, metas) {
                var chartConf = this._getChartConf(dataYesterday, 'line');
                chartConf.xAxis = {
                    categories: this._getXAxis(dataYesterday)
                };
                var series = [];
                this._createSeries(series, dataToday, metas, false, false, true);
                this._createSeries(series, dataYesterday, metas, true, true, true);
                chartConf.series = series;
                return new Highcharts.Chart(chartConf);
            }
        }, {
            ATTRS: {
                nodeId: {
                    value: ''
                }
            }
        });

    }, '1.0.0', { requires: ["mt-base", "w-base"] });

})();
