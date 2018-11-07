M.add('msgp-worth/userreport', function (Y) {
    var userreport = {
        everPaged: false,
        totalPage: 0,
        totalCount: 0,

        tbody: Y.one('#table_wrapper tbody'),
        pbody: Y.one('#paginator_wrapper'),
        models: [],
        searchData: {
            pageNo: 1,
            pageSize: 20
        },
        colspan: 5,
        init: function (models) {
            this.models = models;
            this.colspan = models.length + 3;
            this.initDatePicker()
            this.setLoading();
            this.initTable();
            this.bindGetAgain();
            this.bindSearch();
        },
        setLoading: function () {
            var loadingHtml = '<tr><td colspan="' + this.colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
            this.tbody.setHTML(loadingHtml);
            this.pbody.hide();
        },
        setError: function () {
            var errorHtml = '<tr><td colspan="' + this.colspan + '">获取失败<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
            this.tbody.setHTML(errorHtml);
            this.pbody.empty();
        },
        setEmpty: function () {
            var emptyHtml = '<tr><td colspan="' + this.colspan + '">没有内容<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
            this.tbody.setHTML(emptyHtml);
            this.pbody.empty();
        },
        initTable: function () {
            this.triggerClick();
        },
        initDatePicker: function () {
            var now = new Date();
            var startInput = Y.one('#day')
            var yestoday = new Date(now - 24 * 60 * 60 * 1000);
            sdate = new Y.mt.widget.Datepicker({
                node: startInput,
                showSetTime: false
            });
            startInput.set('value', Y.mt.date.formatDateByString(yestoday, 'yyyy-MM-dd'));
        },
        bindGetAgain: function () {
            var self = this;
            this.tbody.delegate('click', function () {
                self.initTable();
            }, '.get-again');
        },
        bindSearch: function () {
            var self = this;
            Y.one('#searchForm').delegate('click', function () {
                self.triggerClick();
            }, '#searchBtn');
            Y.one('#searchForm').delegate('keyup', function (e) {
                if (e.keyCode === 13) {
                    self.triggerClick();
                }
            }, '#searchBox');
        },
        triggerClick: function () {
            this.searchData['username'] = Y.Lang.trim(Y.one('#username').get('value'));
            this.searchData['dtype'] = Y.Lang.trim(Y.one('#dtype').get('value'));
            this.searchData['day'] = Y.Lang.trim(Y.one('#day').get('value'));
            this.getTablePage(1);
        },
        getTablePage: function (pageNo) {
            var self = this;
            this.setLoading();
            this.searchData['pageNo'] = pageNo;
            var url = '/worth/count/list';
            Y.io(url, {
                method: 'get',
                data: this.searchData,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            var pobj = ret.page;
                            if (Y.Lang.isArray(data) && data.length !== 0) {
                                self.fillTableContent(data);
                                //没有分页过，或者页数有变化，或者总条数有变化，刷新分页
                                if (!self.everPaged || self.totalPage !== pobj.totalPageCount || self.totalCount !== pobj.totalCount) {
                                    self.refreshPaginator(pobj);
                                }
                            } else if (data.length === 0) {
                                self.setEmpty();
                            }
                            //记录本次分页信息，在下次请求时确定是否需要刷新分页
                            self.everPaged = true;
                            self.totalPage = pobj.totalPageCount;
                            self.totalCount = pobj.totalCount;
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                            self.setError();
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                        self.setError();
                    }
                }
            });
        },

        fillTableContent: function (data) {
            var html = '';
            var d;
            for (var i = 0, l = data.length; i < l; i++) {
                d = data[i];
                var modelData = Array();
                for (var j = 0, x = this.models.length; j < x; j++) {
                    var value = d.modelCount[this.models[j]]
                    value = undefined == value ? 0 : value;
                    modelData[j] = '<td><span class="t-ellipsis" >' + value + '</span></td>';
                }
                modelData[this.models.length] = '</tr>'
                html += ['<tr>',
                    '<td><span class="t-ellipsis" >' + d.username + '</span></td>',
                    '<td><span class="t-ellipsis" >' + d.day + '</span></td>',
                    '<td><span class="t-ellipsis" >' + d.totalCount + '</span></td>',
                ].join('');
                html += modelData.join('')
            }
            this.tbody.setHTML(html);
            this.pbody.show();
        },

        refreshPaginator: function (pobj) {
            var self = this;
            new Y.mt.widget.Paginator({
                contentBox: this.pbody,
                index: pobj.pageNo || 1,
                max: pobj.totalPageCount || 1,
                pageSize: pobj.pageSize,
                totalCount: pobj.totalCount,
                callback: function (params) {
                    self.changePage(params);
                }
            });
        },
        changePage: function (params) {
            this.getTablePage(params.page);
        }
    };
    Y.namespace('msgp.worth').userreport = userreport;
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
