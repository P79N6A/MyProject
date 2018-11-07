M.add('msgp-dashboard/status', function(Y){
    Y.namespace('msgp.dashboard').status = status;
    var searchInput = Y.one('#search_input');
    var everPaged = false,
        totalPage = 0,
        totalCount= 0,
        statusWrap = Y.one('#wrap_status'),
        tbody = statusWrap.one('tbody'),
        pbody = statusWrap.one('#paginator_dashboard_status'),
        colspan = 4,
        curEnv;
    function status(statusParam) {
        Y.msgp.service.setEnvText('supplier_env_select');
        var param = Y.msgp.utils.urlParameters();
        if (!param.type) {
            statusWrap.all('#supplier_thrift_http a').item(2).addClass('btn-primary');
        }
        if (!param.env) {
            statusWrap.all('#supplier_env_select a').item(3).addClass('btn-primary');
        }
        if (!param.status) {
            statusWrap.all('#all-enabled button').item(3).addClass('btn-primary');
        }
        getStatusDetail(param.pageNo || 1, param.status || (statusParam ? getStatus(statusParam) : ''), param.type, param.env);
        outlineDisplay(param.status);
    }
    function getStatusDetail(pageNo, status, type, env, appkey) {
        bindEvent(status, type, env);
        setLoading();
        getEnv();
        type = type || (statusWrap.one('#supplier_thrift_http a.btn-primary') ? statusWrap.one('#supplier_thrift_http a.btn-primary').getAttribute('value') : '');
        env = env || (statusWrap.one('#supplier_env_select a.btn-primary') ? statusWrap.one('#supplier_env_select a.btn-primary').getAttribute('value') : '');
        if (0 !== status) {
            status = status || (statusWrap.one('#all-enabled button.btn-primary') ? statusWrap.one('#all-enabled button.btn-primary').getAttribute('value') : '');
        }
        var param = {
            status:status,
            type: type,
            env: env,
            appkey: appkey,
            pageNo: pageNo,
            pageSize: 20
        };
        if (!type || '-1' === type) {
            delete param.type;
        }
        if (!env || '-1' === env) {
            delete param.env;
        }
        if (!status || '-1' === status) {
            delete param.status;
        }
        if (!appkey) {
            delete param.appkey;
        }
        Y.msgp.utils.urlAddParameters(param);
        var url = '/appkey/provider/status';
        Y.io(url, {
            method : 'get',
            data : param,
            on : {
                success : function(id,o){
                    //showContent();
                    var ret = Y.JSON.parse(o.responseText);
                    if(ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillTableContent(data, status, type, env);
                            //没有分页过，或者页数有变化，或者总条数有变化，刷新分页
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pobj,status);
                            }
                        } else if (data.length === 0) {
                            setEmpty(status, type, env);
                        }
                        //记录本次分页信息，在下次请求时确定是否需要刷新分页
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                        statusWrap.show();
                    }
                }
            }
        });
    }

    function fillTableContent(data, status, type, env) {
        var html = '',
            d,
            businessStr,
            ownerStr;
        for (var i = 0, l = data.length; i < l; i++) {
            d = data[i];
            if (d.owt) {
                businessStr = d.owt + ( d.pdl ? ' - ' + d.pdl : '' );
            } else {
                businessStr = d.businessName + ( d.group ? ' - ' + d.group : '' );
            }
            html += ['<tr data-category="' + d.category + '" data-intro="' + d.intro + '" data-tags="' + d.tags + '">',
            '<td><a href="/service/detail?appkey=' + d.appkey + '#supplier" class="see-details">' + d.appkey + '</a></td>',
            '<td><span class="t-ellipsis" title="' + getStatusDesc(d.status) + '">' + getStatusDesc(d.status) + '</span></td>',
            '<td><span class="t-ellipsis" title="' + d.nodeCount + '">' + d.nodeCount + '</span></td>',
            '<td><span class="t-ellipsis" title="' + d.hostCount + '">' + d.hostCount + '</span></td>',
            '</tr>'].join('');
        }
        tbody.setHTML(html);
        pbody.show();
    }
    function setEmpty(status,type,env) {
        var emptyHtml = '<tr><td colspan="' + colspan + this.colspan + '">没有内容<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(emptyHtml);
        pbody.empty();
    }
    function bindEvent(status, type, env) {
        var curBtn = statusWrap.one('#all-enabled .btn-primary');
        var typeBtn = statusWrap.one('#supplier_thrift_http a.btn-primary');
        var envBtn = statusWrap.one('#supplier_env_select a.btn-primary');
        if(!curBtn || (status && '-1' !== status && '-1' === curBtn.get('value'))) {
            var btns = statusWrap.all('#all-enabled .btn');
            btns.each(function(item){
                if(parseInt(status) === parseInt(item.getAttribute('value'))) {
                    btns.removeClass('btn-primary');
                    item.addClass('btn-primary');
                    return;
                }
            });
        }
        if(!typeBtn || (type && '-1' !== type && '-1' === typeBtn.getAttribute('value'))) {
            var btns = statusWrap.all('#supplier_thrift_http a');
            btns.each(function(item){
                if(type === item.getAttribute('value')){
                    btns.removeClass('btn-primary');
                    item.addClass('btn-primary');
                    return;
                }
            });
        }
        if(!envBtn || (env && '-1'!== env && '-1' === envBtn.getAttribute('value'))) {
            var btns = statusWrap.all('#supplier_env_select a');
            btns.each(function(item){
                if(parseInt(env) === parseInt(item.getAttribute('value'))){
                    btns.removeClass('btn-primary');
                    item.addClass('btn-primary');
                    return;
                }
            });
        }
        statusWrap.delegate('click',function() {
            if (this.hasClass('btn-primary')) {
                return;
            }
            this.ancestor('div').all('.btn').removeClass('btn-primary');
            this.addClass('btn-primary');
            getStatusDetail(1,this.get('value'));
            outlineDisplay();
        } , '#all-enabled .btn');
        statusWrap.delegate('click',function() {
            if (this.hasClass('btn-primary')) {
                return;
            }
            this.ancestor('div').all('.btn').removeClass('btn-primary');
            this.addClass('btn-primary');
            outlineDisplay();
            getStatusDetail(1,status);
        } , '#supplier_thrift_http a');
        statusWrap.delegate('click',function() {
            if (this.hasClass('btn-primary')) {
                return;
            }
            this.ancestor('div').all('.btn').removeClass('btn-primary');
            this.addClass('btn-primary');
            getStatusDetail(1,status);
            outlineDisplay();
        } , '#supplier_env_select a');
        statusWrap.delegate('click', function () {
            getAgain();
        }, '.get-again');
        statusWrap.delegate('click',function() {
            var searchInput = statusWrap.one('#statusAppkey'),
                appkey = searchInput.get('value');
            getStatusDetail(1, null, null, null, appkey);
        },'#statusSearchBtn');
    }
    function refreshPaginator(pobj, status) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: function (params) {
                changePage(params,status);
            }
        });
    }
    function changePage(params,status) {
        getStatusDetail(params.page,status);
    }
    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
        tbody.setHTML(loadingHtml);
        pbody.hide();
    }
    function getAgain() {
        var status = statusWrap.one('#all-enabled.btn-primary').get('value');
        getStatusDetail(1,status);
    }
    function getStatus(statusDesc) {
        var msg = 0;
        switch (statusDesc) {
            case "未启动":
                msg = 0;
                break;
            case "正常":
                msg = 2;
                break;
            case "禁用":
                msg = 4;
                break;
            default:
                msg = '';
                break;
        }
        return msg;
    }
    function getStatusDesc(status) {
        var msg = "启用";
        switch (parseInt(status)) {
            case 0:
                msg = "未启动";
                break;
            case 2:
                msg = "正常";
                break;
            case 4:
                msg = "禁用";
                break;
            default:
                msg = "所有";
                break;
        }
        return msg;
    }
    function getEnv( ){
        var url = '/common/online';
        Y.io( url, {
            method : 'get',
            on : {
                success : function(id, o) {
                    var res = Y.JSON.parse( o.responseText );
                    if( res.isSuccess ){
                        curEnv = res.data;
                    }
                }
            }
        });
    }
    function getEnvDesc(env) {
        var msg = "";
        switch (env) {
            case 3:
                msg = curEnv ? "prod":"dev";
                break;
            case 2:
                msg = curEnv ? "staging":"ppe";
                break;
            case 1:
                msg = "test";
                break;
            default:
                msg = "";
                break;
        }
        return msg;
    }
    function getEnvInt(env) {
        var msg = null;
        switch (env) {
            case 'prod':
            case 'dev':
                msg = 3;
                break;
            case 'staging':
            case 'ppe':
                msg = 2;
                break;
            case 'test':
                msg = 1;
                break;
        }
        return msg;
    }
    function outlineDisplay() {
        var url = '/appkey/provider/outline/status';
        var env = Y.one('#supplier_env_select a.btn-primary');
        var status = Y.one('#all-enabled button.btn-primary');
        var type = Y.one('#supplier_thrift_http a.btn-primary');
        var outlineTypeChart = echarts.init(document.getElementById('outlineTypeChart'));
        var outlineEnvChart = echarts.init(document.getElementById('outlineEnvChart'));
        var outlineStatusChart = echarts.init(document.getElementById('outlineStatusChart'));
        outlineTypeChart.showLoading({
            text: '正在加载',
            effect: 'bar',
            textStyle: {
                fontSize: 20
            }
        });
        outlineEnvChart.showLoading({
            text: '正在加载',
            effect: 'bar',
            textStyle: {
                fontSize: 20
            }
        });
        outlineStatusChart.showLoading({
            text: '正在加载',
            effect: 'bar',
            textStyle: {
                fontSize: 20
            }
        });
        outlineTypeChart.on('click',typeConsole);
        outlineEnvChart.on('click',envConsole);
        outlineStatusChart.on('click',statusConsole);
        var param = {
                env: env ? env.getAttribute('value'):'3',
                status: status ? status.getAttribute('value'):-1,
                type: type ? type.getAttribute('value'):'1'
        };
        if(!env || '-1' === env.getAttribute('value')) {
            delete param.env;
        }
        if(!status ||  '-1' === status.getAttribute('value')) {
            delete param.status;
        }
        if(!type || '-1' === type.getAttribute('value')) {
            delete param.type;
        }
        Y.io(url, {
            method: 'get',
            data: param,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var typelegend = data.typeList;
                        var typeseries = [];
                        var envlegend = data.envList;
                        var envseries = [];
                        var statuslegend = data.statusList;
                        var statusseries = [];
                        for (var i = 0; i < typelegend.length; i++) {
                            typeseries.push({value: data.typeCount[i], name: typelegend[i]});
                        }
                        for (var i = 0; i < envlegend.length; i++) {
                            envlegend[i] = getEnvDesc(envlegend[i]);
                            envseries.push({value: data.envCount[i], name: envlegend[i]});
                        }
                        for (var i = 0; i < statuslegend.length; i++) {
                            statuslegend[i] = getStatusDesc(statuslegend[i]);
                            statusseries.push({value: data.statusCount[i], name: statuslegend[i]});
                        }
                        fillPie(outlineTypeChart, "类型", typelegend, typeseries);
                        fillPie(outlineEnvChart, "环境", envlegend, envseries);
                        fillPie(outlineStatusChart, "状态", statuslegend, statusseries);
                    }
                }
            }
        });
    }

    function fillPie(chart, title, legend, series) {
        var option = {
//        color: ['#238e68','#00ff7f','#3299cc'],
            animation: false,
            title : {
                text: title,
                padding: [5,5,15,5],
                x:'center',
                y:'bottom'
            },
            legend: {
                orient: 'vertical',
                x: 'left',
                data: legend
            },
            tooltip: {
                show: true,
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)",
                position: 'inside',
                textStyle: {
                    fontSize: '12'
                }
            },
            series: [
                {
                    type :'pie',
                    radius : '55%',
                    center : ['50%', '45%'],
                    avoidLabelOverlap: false,
                    data: series
                }
            ],
            labelLine: {
                normal: {
                    show: false
                }
            },
            itemStyle: {
                emphasis: {
                    shadowBlur: 0,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        };
        chart.setOption(option);
        chart.hideLoading();
    }
    function typeConsole(param) {
        getStatusDetail(1,null,param.name);
        outlineDisplay();
    }
    function envConsole(param) {
        getStatusDetail(1,null,null,getEnvInt(param.name));
        outlineDisplay();
    }
    function statusConsole(param) {
        statusWrap.one('#all-enabled').all('.btn').removeClass('btn-primary');;
        getStatusDetail(1,getStatus(param.name));
        outlineDisplay();
    }
}, '0.0.1', {
    requires : [
        'w-base',
        'mt-base',
        'mt-io',
        'w-paginator',
        'msgp-service/commonMap',
        'msgp-utils/common'
    ]
});
