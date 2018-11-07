/* jshint indent : false */
M.add('msgp-monitor/monitorLog', function (Y) {

    Y.namespace('msgp.monitor').monitorLog = monitorLog;
    var appkey;
    var applist = Y.one('#apps_select');
    var startInput = Y.one('#start_time'),
        endInput = Y.one('#end_time');
    var lwrap = Y.one('#log_wrap'),
        tbody = lwrap.one('tbody');
    var pbody = Y.one('#paginator_wrapper');

    var colspan = 6;

    var everPaged = false,
        totalPage,
        totalCount;

    var trTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-trigger="<%= item.item %>" data-threshold="<%= item.threshold %>" data-function="<%= item.function %>" data-itemdesc="<%= item.itemDesc %>" data-functiondesc="<%= item.functionDesc %>">',
        '<td><%= Y.mt.date.formatDateByString( new Date(item.createTime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td><% if(item.status == 0) { %> 是 <%= item.ackUser %> <%= Y.mt.date.formatDateByString( new Date(item.ackTime), "yyyy-MM-dd hh:mm:ss" ) %> <% } else { %> 否 <% } %></td>',
        '<td><%= item.side %></td>',
        '<td><%= item.spanname %></td>',
        '<td><%= item.item %></td>',
        '<td><%= item.message %><br/></td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    function monitorLog(key) {
        appkey = key;
        initEvent();
        initDatePicker();
        doGetEvents(1);
    }

    function initEvent() {
        Y.one("#get_trigger_event").on('click', function () {
            doGetEvents(1);
        })
    }

    function initDatePicker() {
        var now = new Date();
        var yestoday = new Date(now - 7 * 24 * 60 * 60 * 1000);
        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });
        startInput.set('value', Y.mt.date.formatDateByString(yestoday, 'yyyy-MM-dd hh:mm:ss'));
        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });
        endInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd hh:mm:ss'));
    }

    function doGetEvents(pageNo) {
        var se = getStartEnd();
        if (!se) return;
        showOverlay();
        tbodyOverlay();
        getEvent(se, pageNo);
    }

    function getEvent(se, pageNo) {
        var selectedAppkey = applist.get('value');
        var url = '/monitor/' + selectedAppkey + '/log';
        var data = {
            pageNo: pageNo,
            pageSize: 20,
            start: se.start,
            end: se.end
        }
        Y.io(url, {
            method: 'get',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var Data = ret.data;
                    var pobj = ret.page;
                    if (ret.isSuccess) {
                        if (Data && Data.length > 0) {
                            fillEvents(Data);
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pbody, pobj);
                            }
                        } else {
                            emptyOrError();
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }

    function getStartEnd() {
        var obj = {
            start: '',
            end: ''
        };
        var s = startInput.get('value'),
            e = endInput.get('value');
        var reg = /^\d{4}(-\d{2}){2} \d{2}:\d{2}:\d{2}$/;
        if (s && reg.test(s)) {
            obj.start = s;
        }
        reg.lastIndex = 0;
        if (e && reg.test(e)) {
            obj.end = e;
        }
        if (s !== obj.start || e !== obj.end) {
            Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
            return null;
        }
        if (obj.start > obj.end) {
            Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
            return null;
        }
        return obj;
    }

    function tbodyOverlay() {
        var html = '<tr><td colspan="' + colspan + '">' + '<i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>';
        tbody.setHTML(html);
    }

    function fillEvents(arr) {
        var micro = new Y.Template();
        var html = micro.render(trTemplate, {data: arr});
        tbody.setHTML(html);
        showContent();
    }

    function emptyOrError(isError) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容');
        tbody.setHTML(html);
        pbody.empty();
        showContent();
    }

    function showContent() {
        lwrap.one('.content-overlay').hide();
        //lwrap.one('#notify_lable').show();
        lwrap.one('.content-body').show();
    }

    function showOverlay() {
        lwrap.one('.content-body').hide();
        lwrap.one('.content-overlay').show();
    }

    function refreshPaginator(pbody, pobj) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: changePage
        });
    }

    function changePage(params) {
        doGetEvents(params.page);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'w-date',
        'mt-date',
        'template',
        'transition',
        'mt-base',
        'w-paginator',
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip'
    ]
});
M.use('msgp-monitor/monitorLog', function (Y) {
    Y.msgp.monitor.monitorLog(key);
});