var searchData = {
    pageNo: 1,
    pageSize: 20
}, _report_style = "org_owt", colspan = 4;
var everPaged = false,
    totalPage= 0,
    totalCount= 0;
var _tableTd = "owt";
var tbody,pbody;
M.add('msgp-worth/modelreport', function (Y) {
    Y.namespace('msgp.worth').modelreport = modelreport;
    function modelreport(report_style) {
        _report_style = report_style;
        tbody = Y.one('#table_wrapper tbody');
        pbody = Y.one('#paginator_wrapper');
        initDatePicker();
        initTable()
        bindSearch()
    }

    function bindSearch() {
        Y.one('#searchForm').delegate('click', function () {
            initTable();
        }, '#searchBtn');
        Y.one('#searchForm').delegate('keyup', function (e) {
            if (e.keyCode === 13) {
                initTable();
            }
        }, '#searchBox');
    }

    function initDatePicker() {
        var now = new Date();
        var startInput = Y.one('#day')
        var yestoday = new Date(now - 24 * 60 * 60 * 1000);
        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: false
        });
        startInput.set('value', Y.mt.date.formatDateByString(yestoday, 'yyyy-MM-dd'));
    }

    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
        tbody.setHTML(loadingHtml);
        pbody.hide();
    }

    function setError() {
        var errorHtml = '<tr><td colspan="' + colspan + '">获取失败<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(errorHtml);
        pbody.empty();
    }

    function setEmpty() {
        var emptyHtml = '<tr><td colspan="' + colspan + '">没有内容<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(emptyHtml);
        pbody.empty();
    }


    function initTable() {
        searchData['model'] = Y.Lang.trim(Y.one('#model option:checked').get('value'));

        searchData['method'] = Y.Lang.trim(Y.one('#method').get('value'));
        _tableTd = "functionName";

        searchData['dtype'] = Y.Lang.trim(Y.one('#dtype').get('value'));
        searchData['day'] = Y.Lang.trim(Y.one('#day').get('value'));
        getTablePage(1);
    }

    function getTablePage(pageNo) {
        var self = this;
        setLoading();
        var method = _report_style.replace("_", "");
        searchData['pageNo'] = pageNo;
        var url = '/worth/count/' + method;
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillTableContent(data);
                            //没有分页过，或者页数有变化，或者总条数有变化，刷新分页
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pobj);
                            }
                        } else if (data.length === 0) {
                            setEmpty();
                        }
                        //记录本次分页信息，在下次请求时确定是否需要刷新分页
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
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
    }

    function fillTableContent(data) {
        var html = '';
        var d;
        for (var i = 0, l = data.length; i < l; i++) {
            d = data[i];
            html += ['<tr>',
                '<td><span class="t-ellipsis" >' + d.model + '</span></td>',
                '<td><span class="t-ellipsis" >' + d.functionName + '</span></td>',
                '<td><span class="t-ellipsis" >' + d.count + '</span></td>',
                '</tr>'
            ].join('');
        }
        tbody.setHTML(html);
        pbody.show();
    }

    function refreshPaginator(pobj) {
        var self = this;
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: function (params) {
                changePage(params);
            }
        });
    }

    function changePage(params) {
        getTablePage(params.page);
    }
}, '0.0.1', {
    requires: [
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip'
    ]
});
