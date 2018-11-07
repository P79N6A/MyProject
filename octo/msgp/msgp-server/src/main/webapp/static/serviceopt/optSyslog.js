/* jshint indent : false */
M.add('msgp-serviceopt/optSyslog', function (Y) {
    Y.namespace('msgp.serviceopt').optSyslog = detailSyslog;
    var appkey;
    var wrapper = Y.one('#wrap_syslog');
    var startInput = wrapper.one('#start_time'),
        endInput = wrapper.one('#end_time');
    var lwrap = wrapper.one('#log_wrap'),
        tbody = lwrap.one('tbody');
    var pbody = wrapper.one('#paginator_wrapper');
    var colspan = 5;

    var everPaged = false,
        totalPage,
        totalCount,
        queryAll = true;

    var trTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-trigger="<%= item.item %>" data-threshold="<%= item.threshold %>" data-function="<%= item.function %>" data-itemdesc="<%= item.itemDesc %>" data-functiondesc="<%= item.functionDesc %>">',
        '<td><%= Y.mt.date.formatDateByString( new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td><%= item.operatorName %></td>',
        '<td><%= item.entityType %></td>',
        '<td>',
        '<% Y.Array.each(item.desc, function(iterator, index){ %>',
        '<%= iterator %><br/>',
        '<% }); %>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    function detailSyslog(key) {
        appkey = key;
        initDatePicker();
        initSelector();
        refreshData();
    }

    function refreshData() {
        getAllEntityType();
        getAllOperator();
        doGetOperations(1);
    }

    function initSelector() {
        Y.one("#entityType").on('change', function () {
            doGetOperations(1);
        });
        Y.one("#operator").on('change', function () {
            doGetOperations(1);
        });
    }

    function getAllOperator() {
        var se = getStartEnd();
        var url = '/service/operation/' + appkey + '/operator';
        Y.io(url, {
            method: 'get',
            data: {
                start: se.start,
                end: se.end
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        fillSelector(data, "operator")
                    } else {
                        emptyOrError(true);
                        return null
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }


    function getAllEntityType() {
        var se = getStartEnd();
        var url = '/service/operation/' + appkey + '/entity';
        Y.io(url, {
            method: 'get',
            data: {
                start: se.start,
                end: se.end
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        fillSelector(data, "entityType")
                    } else {
                        emptyOrError(true);
                        return null
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }

    function fillSelector(data, type) {
        Y.one("#" + type).empty();
        Y.one("#" + type).append("<option value='选择全部' selected='selected'>选择全部</option>");
        Y.Array.each(data, function (item) {
            Y.one("#" + type).append('<option value=' + item + '>' + item + '</option>');
        });
    }

    function initDatePicker() {
        var now = new Date();
        var yestoday = new Date(now - 1 * 24 * 60 * 60 * 1000);
        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: true
        });
        sdate.on('Datepicker.select', function () {
            refreshData();
        });
        startInput.set('value', Y.mt.date.formatDateByString(yestoday, 'yyyy-MM-dd hh:mm:ss'));
        edate = new Y.mt.widget.Datepicker({
            node: endInput,
            showSetTime: true
        });
        edate.on('Datepicker.select', function () {
            refreshData();
        });
        endInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd hh:mm:ss'));
    }


    function doGetOperations(pageNo) {
        var se = getStartEnd();
        if (!se) return;
        showOverlay();
        var entityType = Y.one('#entityType').get('value');
        var operator = Y.one('#operator').get('value');
        entityType = (entityType == "选择全部") ? "" : entityType;
        operator = (operator == "选择全部") ? "" : operator;
        var url = '/service/operation/' + appkey + '/log';
        Y.io(url, {
            method: 'get',
            data: {
                pageNo: pageNo,
                pageSize: 20,
                start: se.start,
                end: se.end,
                entityType: entityType,
                operator: operator
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    var pobj = ret.page;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            fillOperations(data);
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

    function fillOperations(arr) {
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
        doGetOperations(params.page);
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
    