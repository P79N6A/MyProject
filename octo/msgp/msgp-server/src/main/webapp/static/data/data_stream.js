M.add('msgp-data/data_stream', function (Y) {
    Y.namespace('msgp.data').data_stream = data_stream;

    var startInput, endInput;
    var _appkey;
    var streamWrapper;
    var _kpiData = {};
    function data_stream(appkey, _isFirstIn) {
        _appkey = appkey;
        document.title = "上下游分析";
        if (!_isFirstIn) {
            return;
        }

        streamWrapper = Y.one('#div_data_stream');
        startInput = streamWrapper.one('#start_time');
        endInput = streamWrapper.one('#end_time');
        initDatePicker();
        //填充表格
        getData(appkey);

        streamWrapper.one('#query_btn').on('click', function () {
            getData(_appkey)
        });
    }

    function getData(appkey) {
        //role,group,remoteAppkey
        getDependService(appkey, "server","server","SpanRemoteApp","*");
        getDependService(appkey, "client","client","SpanRemoteApp","*");
        getKpiData(appkey, "app","server","spanLocalhost","all");

        var start = startInput.get("value");
        var end = endInput.get("value");
        var opt = {
            appkey: appkey,
            start: start,
            end: end
        };
        Y.msgp.utils.urlAddParameters(opt);
    }

    function initDatePicker() {
        new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });

        new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });

        var e = endInput.get('value');
        if (e == "") {
            var end = new Date();
            endInput.set('value', Y.mt.date.formatDateByString(end, 'yyyy-MM-dd hh:mm:00'));
        }

        var s = startInput.get('value');
        if (s == "") {
            var start = new Date(end.getTime() - 60 * 10 * 1000);
            startInput.set('value', Y.mt.date.formatDateByString(start, 'yyyy-MM-dd hh:mm:00'));
        }
    }

    function getDependService(appkey, source,role,group,remoteAppkey) {
        //清空内容
        _kpiData[source] = [];
        streamWrapper.one('#tbody_' + source).setHTML("");
        getKpiData(appkey, source,role,group,remoteAppkey)
    }

    function getKpiData(appkey, source,role,group,remoteAppkey) {
        var start = startInput.get("value");
        var end = endInput.get("value");
        var url = '/data/service/kpi?appkey=' + appkey + "&start=" + start + "&end=" + end + "&role="+role+"&group="+group+"&remoteAppkey="+remoteAppkey;
        var queryBtn = streamWrapper.one('#query_btn');
        Y.io(url, {
            method: 'get',
            on: {
                start: function (id, o) {
                    queryBtn.set("disabled", true);
                    queryBtn.setStyle('background', '#ccc');
                },
                complete: function (id, o) {
                    queryBtn.set("disabled", false);
                    queryBtn.setStyle('background', '#3fab99');
                },
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        //输出到模板
                        //收集数据,然后统一输出
                        if ("app" == source) {
                            fillAppKpi(ret.data, source)
                        } else {
                            for(var i = 0, l = ret.data.length; i < l; i++) {
                                var data = ret.data[i];
                                if(data.appkey!="all"){
                                    _kpiData[source].push(data)
                                }
                            }
                            showKpiData(source)
                        }
                    }
                }
            }
        });
    }

    var kpiTemplate = [
        '<% Y.Array.each(this.data, function(item,index){ %>',
        '<tr>',
        '<% node = encodeURIComponent(item.node )%>',
        '<% var displayName = item.appkey%>',
        '<% if(this.source!="app") {%>',
        '<td><% if(item.appkey!="unknownService"){%><a title="<%= item.appkey %>" style="cursor:pointer" target="_blank" href="/service/detail?appkey=<%= item.appkey %>" ><%= displayName %></a> <%}else{%> <%= displayName %> <a href="https://123.sankuai.com/km/page/28354817" target="_blank"><i class="fa fa-question-circle"></i></a> <%}%></td>',
        '<% }%>',
        '<td><%= item.count %></td>',
        '<td><%= item.tp90 %></td>',
        '<td><% if(item.appkey!="unknownService"){%><a style="cursor:pointer <% if(item.falconCount>0){%> ;color: red <%}%> " target="_blank" href="http://p.falcon.sankuai.com/api/alarm/dashboard/?node=<%= node %>"><%= item.falconCount %></a><%}else{%> <%= item.falconCount %> <%}%></td>',
        '<td><% if(item.appkey!="unknownService"){%><a style="cursor:pointer <% if(item.octoCount>0){%> ;color: red <%}%> " target="_blank" href="/monitor/log?appkey=<%= item.appkey %>"><%= item.octoCount %><%}else{%> <%= item.octoCount %> <%}%></td>',
        '<td><% if(item.appkey!="unknownService"){%><a style="cursor:pointer <% if(item.errlogCount>100){%> ;color: red <%}%> " target="_blank" href="/log/report?appkey=<%= item.appkey %>" ><%= item.errorCount %></a><%}else{%> <%= item.errorCount %> <%}%></td>',
        '</tr>',
        '<% }); %>'
    ].join('');


    function fillAppKpi(data, source) {
        var micro = new Y.Template();
        var dom = streamWrapper.one('#tbody_' + source);
        var html = micro.render(kpiTemplate, {data: data, source: source});
        dom.setHTML(html);
        streamWrapper.one("#depend_"+source);
        if("app"!=source){
            new Y.msgp.utils.TableSorter("depend_"+source);
        }
        Y.mt.widget.init();
    }
    //倒序
    var sortBy = function (name, minor) {
        return function (o, p) {
            var a, b;
            if (o && p && typeof o === 'object' && typeof p === 'object') {
                a = o[name];
                b = p[name];
                if (a === b) {
                    return typeof minor === 'function' ? minor(o, p) : 0;
                }
                if (typeof a === typeof b) {
                    return a > b ? -1 : 1;
                }
                return typeof a > typeof b ? -1 : 1;
            } else {
                throw("error");
            }
        }
    };

    function showKpiData(source) {
        var sourceData = _kpiData[source].sort(sortBy('falconCount', sortBy('octoCount', sortBy('errorCount',sortBy('count')))))
        fillAppKpi(sourceData, source)

    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'template',
        'msgp-utils/tableSorter',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
