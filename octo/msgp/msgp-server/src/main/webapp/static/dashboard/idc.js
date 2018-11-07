M.add('msgp-dashboard/idc', function(Y) {
    Y.namespace('msgp.dashboard').idc = idc;
    var searchInput = Y.one('#search_input'),
        everPaged = false,
        totalPage = 0,
        totalCount = 0,
        idcWrap = Y.one('#wrap_idc'),
        tbody = idcWrap.one('tbody'),
        pbody = idcWrap.one('#paginator_dashboard_idc'),
        colspan = 4,
        curEnv;
    function idc(idcparam) {
        Y.msgp.service.setEnvText('supplier_env_idc_select');
        var param = Y.msgp.utils.urlParameters();
        if (!param.type) {
            idcWrap.all('#supplier_thrift_http a').item(2).addClass('btn-primary');
        }
        if (!param.env) {
            idcWrap.all('#supplier_env_idc_select a').item(3).addClass('btn-primary');
        }
        getIdcDetail(param.pageNo || 1, param.idc || idcparam, param.type, param.env);
        outlineDisplay(param.idc);
    }

    function getIdcDetail(pageNo, idc, type, env, appkey) {
        bindEvent(idc, type, env);
        setLoading();
        getEnv();
        type = type || (idcWrap.one('#supplier_thrift_http a.btn-primary') ? idcWrap.one('#supplier_thrift_http a.btn-primary').getAttribute('value') : '');
        env = env || (idcWrap.one('#supplier_env_idc_select a.btn-primary') ? idcWrap.one('#supplier_env_idc_select a.btn-primary').getAttribute('value') : '');
        var param =  {
            type: type,
            env: env,
            idc: decodeURIComponent(idc),
            appkey: appkey,
            pageNo: pageNo,
            pageSize: 20
        };
        if (!type || '-1' === type) {
            delete param.type;
        }
        if (!idc) {
            delete param.idc;
        }
        if (!env || '-1' === env) {
            delete param.env;
        }
        if (!appkey) {
            delete param.appkey;
        }
        Y.msgp.utils.urlAddParameters(param);
        var url = '/appkey/provider/idc';
        Y.io(url, {
            method : 'get',
            data : param,
            on : {
                success : function (id, o) {
                    //showContent();
                    var ret = Y.JSON.parse( o.responseText );
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillTableContent(data,idc,type,env);
                            //没有分页过，或者页数有变化，或者总条数有变化，刷新分页
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pobj,idc);
                            }
                        } else if (data.length === 0) {
                            setEmpty(idc,type,env);
                        }
                        //记录本次分页信息，在下次请求时确定是否需要刷新分页
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                        idcWrap.show();
                    }
                }
            }
        });
    }

    function fillTableContent(data) {
        var html = '',
            d,
            i,
            l;
        for (i = 0, l = data.length; i < l; i++) {
            d = data[i];
            if (d.owt) {
                businessStr = d.owt + ( d.pdl ? ' - ' + d.pdl : '' );
            } else {
                businessStr = d.businessName + ( d.group ? ' - ' + d.group : '' );
            }
            html += ['<tr data-category="' + d.category + '" data-intro="' + d.intro + '" data-tags="' + d.tags + '">',
                '<td><a href="/service/detail?appkey=' + d.appkey + '#supplier" class="see-details">' + d.appkey + '</a></td>',
                '<td><span class="t-ellipsis" title="' + d.idc + '">' + d.idc + '</span></td>',
                '<td><span class="t-ellipsis" title="' + d.nodeCount + '">' + d.nodeCount + '</span></td>',
                '<td><span class="t-ellipsis" title="' + d.hostCount + '">' + d.hostCount + '</span></td>',
                '</tr>'].join('');

        }
        tbody.setHTML(html);
        pbody.show();
    }
    function setEmpty() {
        var emptyHtml = '<tr><td colspan="' + colspan + this.colspan + '">没有内容<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(emptyHtml);
        pbody.empty();
    }
    function bindEvent(idc, type, env) {
        var typeBtn = idcWrap.one('#supplier_thrift_http a.btn-primary'),
            envBtn = idcWrap.one('#supplier_env_idc_select a.btn-primary');
        if (!typeBtn || (type && '-1' !== type && '-1' === typeBtn.getAttribute('value'))) {
            var btns = idcWrap.all('#supplier_thrift_http a');
            btns.each(function (item) {
                if (type === item.getAttribute('value')) {
                    btns.removeClass('btn-primary');
                    item.addClass('btn-primary');
                }
            });
        }
        if (!envBtn || (env && '-1' !== env && '-1' === envBtn.getAttribute('value'))) {
            var btns = idcWrap.all('#supplier_env_idc_select a');
            btns.each(function (item) {
                if (parseInt(env) === parseInt(item.getAttribute('value'))) {
                    btns.removeClass('btn-primary');
                    item.addClass('btn-primary');
                }
            });
        }
        idcWrap.delegate('click', function() {
            if (this.hasClass('btn-primary')) {
                return;
            }
            this.ancestor('div').all('.btn').removeClass('btn-primary');
            this.addClass('btn-primary');
            outlineDisplay();
            getIdcDetail(1, idc);
        }, '#supplier_thrift_http a');
        idcWrap.delegate('click',function() {
            if (this.hasClass('btn-primary')) {
                return;
            }
            this.ancestor('div').all('.btn').removeClass('btn-primary');
            this.addClass('btn-primary');
            getIdcDetail(1,idc);
            outlineDisplay();
        }, '#supplier_env_idc_select a');
        idcWrap.delegate('click',function() {
            location.href = '/manage/dashboard';
        }, '#status_detail_return');
        idcWrap.delegate('click', function () {
            getAgain();
        }, '.get-again');
        idcWrap.delegate('click',function() {
            var searchInput = idcWrap.one('#idcAppkey'),
                appkey = searchInput.get('value');
            getIdcDetail(1, null, null, null, appkey);
        }, '#idcSearchBtn');
    }
    function refreshPaginator(pobj, idc) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: function (params) {
                changePage(params,idc);
            }
        });
    }
    function changePage(params, idc) {
        getIdcDetail(params.page, idc);
    }
    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
        tbody.setHTML(loadingHtml);
        pbody.hide();
    }
    function getAgain() {
        var type = idcWrap.one('#supplier_env_idc_select a.btn-primary').get('value');
        var env = idcWrap.one('#supplier_thrift_http a.btn-primary').get('value');
        getIdcDetail(1,null,type,env);
    }
    function getEnv( ){
        var url = '/common/online';
        Y.io( url, {
            method : 'get',
            on : {
                success : function(id, o){
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
    function outlineDisplay(idc) {
        idc = idc || (Y.msgp.utils.urlParameters().idc ? Y.msgp.utils.urlParameters().idc : '');
        var url = '/appkey/provider/outline/idc';
        var env = Y.one('#supplier_env_idc_select a.btn-primary');
        var type = Y.one('#supplier_thrift_http a.btn-primary');
        var outlineTypeChart = echarts.init(document.getElementById('outlineIdcTypeChart'));
        var outlineEnvChart = echarts.init(document.getElementById('outlineIdcEnvChart'));
        var outlineIdcChart = echarts.init(document.getElementById('outlineIdcChart'));
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
        outlineIdcChart.showLoading({
            text: '正在加载',
            effect: 'bar',
            textStyle: {
                fontSize: 20
            }
        });
        outlineTypeChart.on('click',typeConsole);
        outlineEnvChart.on('click',envConsole);
        outlineIdcChart.on('click',idcConsole);
        var param = {
            env: env ? env.getAttribute('value'):'3',
            type: type ? type.getAttribute('value'):'1',
            idc: idc ? decodeURIComponent(idc):''
        };
        if(!env || '-1' === env.getAttribute('value')) {
            delete param.env;
        }
        if(!type || '-1' === type.getAttribute('value')) {
            delete param.type;
        }
        if(!idc) {
            delete param.idc;
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
                        var idclegend = data.idcList;
                        var idcseries = [];
                        for (var i = 0; i < typelegend.length; i++) {
                            typeseries.push({value: data.typeCount[i], name: typelegend[i]});
                        }
                        for (var i = 0; i < envlegend.length; i++) {
                            envlegend[i] = getEnvDesc(envlegend[i]);
                            envseries.push({value: data.envCount[i], name: envlegend[i]});
                        }
                        for (var i = 0; i < idclegend.length; i++) {
                            idcseries.push({value: data.idcCount[i], name: idclegend[i]});
                        }
                        fillPie(outlineTypeChart, "类型", typelegend, typeseries);
                        fillPie(outlineEnvChart, "环境", envlegend, envseries);
                        fillPie(outlineIdcChart, "机房", idclegend, idcseries);
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
                    center : ['55%', '45%'],
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
        getIdcDetail(1, null, param.name);
        outlineDisplay();
    }
    function envConsole(param) {
        getIdcDetail(1, null, null, getEnvInt(param.name));
        outlineDisplay();
    }
    function idcConsole(param) {
        getIdcDetail(1, param.name);
        outlineDisplay(param.name);
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
