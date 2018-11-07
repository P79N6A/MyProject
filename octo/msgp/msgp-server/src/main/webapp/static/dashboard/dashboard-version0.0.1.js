M.add('msgp-dashboard/dashboard-version0.0.1', function(Y) {
    Y.namespace('msgp.dashboard').dashboard = dashboard;
    var searchInput = Y.one('#search_input');
    var serviceCount = Y.one('#serviceCount'),
        serviceByBusiness = Y.one('#serviceByBusiness'),
        instanceCount = Y.one('#instanceCount'),
        instanceByStatus = Y.one('#instanceByStatus'),
        instanceByDC = Y.one('#instanceByDC'),
        requestCountToday = Y.one('#requestCountToday'),
        requestCountBar = Y.one('#requestCountBar'),
        serviceGBLocation = Y.one('#serviceGBLocation'),
        serviceGBType = Y.one('#serviceGBType');
    function dashboard() {
        bindSearch();
        initDashBoard();
        showOverlay();
    }
    function showOverlay() {
        Y.one('.content-body').hide();
        Y.one('.content-overlay').show();
    }
    function showContent() {
        Y.one('.content-overlay').hide();
        Y.one('.content-body').show();
    }
    function bindSearch() {
        searchInput.on('keyup', function(e) {
            if (e.keyCode === 13) {
                doSearch();
            }
        });
        Y.one('#search_button').on('click', doSearch);
    }
    function doSearch() {
        var keyword = Y.Lang.trim(searchInput.get('value'));
        location.href = '/service?keyword=' + keyword;
    }
    function initDashBoard() {
        var url = '/dashboard/overview';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    showContent();
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillDashBoard(ret.data);
                    }
                }
            }
        });
    }
    function fillDashBoard(data) {
        serviceCount.setHTML(data.serviceCount);
        instanceCount.setHTML(data.instanceCount);
        echarts.init(serviceByBusiness.getDOMNode()).setOption(wrapOption(data.serviceByBusiness, '按部门归类'));
        var statusEcharts = echarts.init(instanceByStatus.getDOMNode());
        statusEcharts.setOption(wrapOption(data.instanceByStatus,'按状态归类'));
        statusEcharts.on('click',statusConsole);
        var idcEcharts = echarts.init(instanceByDC.getDOMNode());
        idcEcharts.setOption(wrapOption( data.instanceByDC, '按机房归类'));
        idcEcharts.on('click',idcConsole);
        requestCountToday.setHTML(data.requestCountToday);
        echarts.init(requestCountBar.getDOMNode()).setOption(setOption(data.requestCount, '最近两周请求量'));

        echarts.init(serviceGBLocation.getDOMNode()).setOption(wrapOption(data.serviceGBLocation, '按地点归类'));
        echarts.init(serviceGBType.getDOMNode()).setOption(wrapOption(data.serviceGBType, '按类型归类'));
    }
    function wrapOption( obj, title ){
        var option = {
            animation: false,
            title : {
                text: title,
                textStyle: {
                    fontSize: 14,
                    color: '#6a6a6a'
                },
                padding: [5,5,15,5],
                x:'center',
                y:'bottom'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)"
            },
            //legend : {
            //    orient : 'vertical',
            //    x : 'left',
            //    data : Y.Object.keys( obj )
            //},
            toolbox : {
                show : true,
                feature : {
                    dataView : {show: true, readOnly: false},
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            calculable : true,
            series : [
                {
                    type :'pie',
                    radius : '55%',
                    center : ['50%', '45%'],
                    data : getOptionData( obj )
                }
            ]
        };
        return option;
    }

    function setOption( obj, title) {
        var option = {
            title : {
                text: title,
                textStyle: {
                    fontSize: 14,
                    color: '#6a6a6a'
                },
                padding: [5, 5, 15, 5],
                x: 'center',
                y: 'bottom'
            },
            grid : {
                x: 100,
                y: 30
            },
            tooltip : {
                trigger: 'axis'
            },
            legend: {
                data: obj.legend
            },
            toolbox: {
                show : true,
                feature : {
                    dataView : {show: true, readOnly: false},
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },
            calculable : true,
            xAxis : [
                {
                    type : 'category',
                    data : obj.data
                }
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series :  getSeriesData(obj.series)
        };
        return option;
    }
    function getSeriesData(series){
        var series_array = [];
        Y.Array.each(series,function(item,index){
            series_array.push({
                name: item.name,
                type: 'line',
                data: item.data,
                clickable: false
            });
        })
        return series_array;
    }
    function getOptionData(obj) {
        var arr = [];
        Y.Object.each(obj, function (val, key) {
            arr.push({
                name : key,
                value : val
            });
        });
        return arr;
    }

    function statusConsole(param) {
        var tab = Y.one('#tab_trigger');
        tab.all('li').removeClass('current');
        tab.all('li').item(1).addClass('current');
        location.hash = 'status';
        Y.one('#wrap_status').show();
        Y.msgp.dashboard.status(param.name);
    }

    function idcConsole(param) {
        var tab = Y.one('#tab_trigger');
        tab.all('li').removeClass('current');
        tab.all('li').item(1).addClass('current');
        location.hash = 'idc';
        Y.one('#wrap_idc').show();
        Y.msgp.dashboard.idc(param.name);
    }

}, '0.0.1', {
    requires : [
        'w-base',
        'mt-base',
        'mt-io',
        'w-paginator',
        'msgp-service/commonMap',
        'msgp-utils/common',
        'msgp-dashboard/status',
        'msgp-dashboard/idc'
    ]
});