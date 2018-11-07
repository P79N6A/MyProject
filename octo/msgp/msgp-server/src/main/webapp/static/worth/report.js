var _data = {};
function getData(project, business) {
    $('#business').val(business);
    ajaxReport(project, business);

    $('#business').change(function(){
        business = $('#business').val();
        ajaxReport(project, business);
    })
}
function ajaxReport(project, business){
    $.ajax({
        type: "GET",
        url: "report?project=" + project + "&business=" + business,
        dataType: "JSON"
    }).done(function (res) {
        if (res.isSuccess === true) {
            paintChart(dataFormat(res.data));
        } else {
            alert('error');
        }
    });
}
/**
 * 获取数据
 * @return {[type]} [description]
 */


/**
 * 数据格式处理
 * @param  {[type]} data [description]
 * @return {[type]}      [description]
 */
function dataFormat(data) {
    //_data = data;
    _data = [];
    $.each(data, function (key, value) {
        if (value.length < 8) {
            var len = 8 - value.length;
            for (var i = 0; i < len; i++) {
                value.push(0);
            }
        }
        _data.push({name: key, values: value});
    })
    return _data;
}


/**
 * 绘制柱状图
 * @param  {[type]} data [description]
 * @return {[type]}      [description]
 */
function paintChart(data) {
    var theme = new Theme();
    echarts.registerTheme('macarons', theme.getTheme('macarons'));
    var chartMain = echarts.init(document.getElementById('j-chart-main'), 'macarons')
    var option = getEChartOption();
    chartMain.setOption(option);
}


function getEChartOption() {
    var legend = getLegend();
    var series = getSeries();
    var business = $('#business').find("option:selected").text();
    return {
        title: {
            x: 'center',
            text:  business +'2016服务治理支出'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            },
            formatter: function (params, ticket, callback) {
                // dataIndex -- Q1:[0-2], Q2: [3-5], Q3: [6-8], Q4: [9-11]
                var dataIndex = params[0].dataIndex;

                // 季度
                var quarter = '';

                if (dataIndex >= 0 && dataIndex <= 2) {
                    quarter = 'Q1';
                } else if (dataIndex >= 3 && dataIndex <= 5) {
                    quarter = 'Q2';
                } else if (dataIndex >= 6 && dataIndex <= 8) {
                    quarter = 'Q3';
                } else if (dataIndex >= 9 && dataIndex <= 11) {
                    quarter = 'Q4';
                }

                var str = quarter + '-' + params[0].name + '<br/>';

                params.forEach(function (item) {
                    str += item.seriesName + ': ' + item.data + '<br/>';
                });

                return str;
            }
        },
        legend: legend,
        calculable: false,
        xAxis: [
            {
                type: 'category',
                splitLine: {show: false},
                data: ['未接入', '实际', '未接入', '实际', '未接入', '实际', '未接入', '实际']
            }
        ],
        yAxis: [
            {
                type: 'value',
                splitArea: {show: false}
            }
        ],
        series: series
    }
}
/**
 * 主题设置 echarts3.0暂不支持直接配置主题
 */
function Theme() {
    var macarons = {
        color: [
            '#2ec7c9', '#b6a2de', '#5ab1ef', '#ffb980', '#d87a80',
            '#8d98b3', '#e5cf0d', '#97b552', '#95706d', '#dc69aa',
            '#07a2a4', '#9a7fd1', '#588dd5', '#f5994e', '#c05050',
            '#59678c', '#c9ab00', '#7eb00a', '#6f5553', '#c14089'
        ]
    };

    this.getTheme = function (name) {
        if (name === 'macarons') {
            return macarons;
        }
    };
}
function getLegend() {
    var nameData = [];
    for (var i = 0; i < _data.length; i++) {
        nameData[i] = _data[i].name
    }
    return {
        x: 'right',
        y: 'top',
        top:'22',
        data: nameData
    };
}


function getSeries() {
    var series = [];
    for (var i = 0; i < _data.length; i++) {
        series[i] = {
            name: _data[i].name,
            type: 'bar',
            stack: 'cat',
            data: _data[i].values,
            itemStyle: {
                normal: {
                    barBorderRadius: 0,
                    label: {
                        show: true,
                        position: 'inside',
                        formatter: function (cat) {
                            //cat -- 每一列
                            if (cat.value === 0) {
                                return '';
                            }
                            return cat.value;
                        }
                    }
                }
            }
        }
    }
    return series;
}

