/* jshint indent: false */
M.add('msgp-servicedetail/list-version0.1.1', function (Y) {
    var list = {
        everPaged: false,
        totalPage: 0,
        totalCount: 0,

        tbody: Y.one('#table_wrapper tbody'),
        pbody: Y.one('#paginator_wrapper'),

        colspan: 6,
        isSearch: false,
        pdled: false,
        keyword: '',
        delete_click: 1,
        subscribe_click: 1,
        _business: -1,
        init: function () {
            document.title = "我的服务";
            this.setLoading();
            this.initTable();
            this.bindGetAgain();
            this.bindSearch();
            this.bindClassifyTab();
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
            // do exactly the search do
            var val = Y.Lang.trim(Y.one('#searchBox').get('value'));
            var urlparam = Y.msgp.utils.urlParameters();
            if ('' !== val) {
                this.triggerClick();
            } else {
                if (urlparam.type) {
                    var _business = urlparam.business ? urlparam.business : -1;
                    var pageNo =urlparam.pageNo?urlparam.pageNo:1;
                    this.getTablePage(pageNo, urlparam.type, _business);
                    this.setDefaultFilterType(urlparam.type);
                } else {
                    this.getTablePage(1, '3',-1);
                    this.setDefaultFilterType('3');
                }
            }
        },
        bindGetAgain: function () {
            var self = this;
            this.tbody.delegate('click', function () {
                var val = Y.Lang.trim(Y.one('#searchBox').get('value'));
                var type = self.getFilterType();
                if ('' !== val && (!type || '4' === type)) {
                    self.triggerClick();
                } else if ('' !== type) {
                    self.getTablePage(1, type,-1);
                    self.setDefaultFilterType(type);
                } else {
                    self.initTable();
                }
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
        triggerClick: function (pageNo) {
            pageNo = pageNo || 1;
            var val = Y.Lang.trim(Y.one('#searchBox').get('value'));
            this.keyword = val || '';
            this.isSearch = '' !== val;
            this.getTablePage(pageNo,"",-1);
            this.setDefaultFilterType('4');
        },
        getTablePage: function (pageNo, filterType, business) {
            var self = this;
            var opt = filterType ? {
                type: filterType,
                business: business,
                pageNo: pageNo,
                pageSize: 20
            } : {
                keyword: Y.one("#searchBox").get("value"),
                pageNo: pageNo,
                pageSize: 20
            };
            var url = filterType ? '/service/filter' : this.isSearch ? '/service/search' : '/service/list';
            Y.msgp.utils.urlAddParameters(opt);
            self.delete_click = 1;
            self.subscribe_click = 1;
            this.setLoading();
            Y.io(url, {
                method: 'get',
                data: filterType ? {
                    business: business,
                    type: filterType,
                    pageNo: pageNo,
                    pageSize: 20
                } : this.isSearch ? {
                    keyword: this.keyword,
                    pageNo: pageNo,
                    pageSize: 20
                } : {
                    pageNo: pageNo,
                    pageSize: 20,
                    pdled: this.pdled
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            var pobj = ret.page;
                            if (Y.Lang.isArray(data) && data.length !== 0) {
                                var subscribedAppkeys = [];
                                $.ajax({
                                    type:"GET",
                                    url: '/subscribe/report/subscribedAppkey',
                                    async: false,
                                    success:function(ret){  //function1()
                                        subscribedAppkeys = ret.data;
                                    }
                                });
                                self.fillTableContent(data, subscribedAppkeys);
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
        fillTableContent: function (data, subscribedAppkeys) {
            var html = '';
            var d,
                businessStr,
                regLimit,
                ownerStr;
            for (var i = 0, l = data.length; i < l; i++) {
                d = data[i];
                if (d.owt) {
                    businessStr = d.owt + ( d.pdl ? ' - ' + d.pdl : '' );
                } else {
                    businessStr = d.businessName + ( d.group ? ' - ' + d.group : '' );
                }
                if(d.regLimit==0){
                    regLimit="非强制";
                }else{
                    regLimit="强制";
                }
                var subscribed = subscribedAppkeys.contains(d.appkey)? 1 : 0;
                var subscribeDesc = subscribed == 0 ? '订阅报表':'取消订阅';
                
                ownerStr = this.getOwners(d.owners);
                html += ['<tr data-category="' + d.category + '" data-intro="' + d.intro + '" data-tags="' + d.tags + '">',
                    '<td><a href="/service/detail?appkey=' + d.appkey + '#supplier" class="see-details">' + d.appkey + '</a></td>',
                    '<td><span class="t-ellipsis" title="' + businessStr + '">' + businessStr + '</span></td>',
                    '<td><span class="t-ellipsis" title="' + ownerStr + '">' + ownerStr + '</span></td>',
                    '<td><span class="t-ellipsis" title="' + regLimit + '">' + regLimit + '</span></td>',
                    '<td><span class="t-ellipsis" title="' + d.intro + '">' + d.intro + '</span></td>',
                    '<td>',
                    '<a id="subscribe_service" data-appkey="' + d.appkey +'" data-subscribe="' + subscribed + '" href="javascript:void(0); class="config-panel-delete">'+ subscribeDesc +'</a>',
                    '<a href="/service/desc?appkey=' + d.appkey + '&force=true" class="see-details ml20">编辑</a>',
                    '<a href="/service/detail?appkey=' + d.appkey + '#outline" class="see-details ml20">详情</a>',
/*
                    '<a id="delete_service" data-appkey=' + d.appkey + ' href="javascript:void(0);" class="config-panel-delete" style="display: none;" > &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span >删除</span> </a>',
*/
                    '</td>',
                    '</tr>'].join('');
            }
            this.tbody.setHTML(html);
           /* this.tbody.delegate('click', function clickFun(parent) {
                return function () {
                    var self = this;
                    clickDelete(parent, self);
                }
            }(this), '#delete_service');*/

            this.tbody.delegate('click', function clickFun(parent) {
                return function () {
                    var self = this;
                    clickObserver(parent, self);
                }
            }(this), '#subscribe_service');

            this.pbody.show();
        },
        myFunc: function () {
            alert("hello");
        },
        getOwners: function (owners) {
            var arr = [];
            for (var i = 0, l = owners.length; i < l; i++) {
                arr.push(owners[i].name);
            }
            return arr.join(',');
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
            var val = Y.Lang.trim(Y.one('#searchBox').get('value'));
            var filterType = this.getFilterType();
            if ('' !== val && ('4' || null || undefined) === filterType) {
                this.triggerClick(params.page);
            } else {
                this.getTablePage(params.page, filterType,-1);
            }

        },
        bindClassifyTab: function () {
            var self = this;
            Y.one('#searchForm').delegate('click', function () {
                if (this.ancestor('li').hasClass('current')) {
                    return;
                }
                this.ancestor('li').ancestor('ul').all('li').removeClass('current');
                this.ancestor('li').addClass('current');
                var filterType = self.getFilterType();
                var val = Y.Lang.trim(Y.one('#searchBox').get('value'));
                if ('4' === filterType && '' !== val) {
                    self.triggerClick();
                } else {
                    self.getTablePage(1, filterType,-1);
                    Y.one('#searchBox').set('value', '')
                }
            }, '.classify_btn');
        },
        getFilterType: function () {
            var val = Y.Lang.trim(Y.one('#searchBox').get('value'));
            var tabs = Y.all('.classify_btn');
            var filterType = null;
            tabs.each(function (node) {
                if (node.ancestor('li').hasClass('current')) {
                    filterType = node._node.getAttribute('value');
                }
            });
            return filterType;
        },
        setDefaultFilterType: function (type) {
            var tabs = Y.all('.classify_btn');
            tabs.each(function (node) {
                if (type === node._node.getAttribute('value')) {
                    node.ancestor('li').addClass('current');
                } else {
                    node.ancestor('li').removeClass('current');
                }
            });
        }
    };
    Y.namespace('msgp.servicedetail').list = list;

    function clickDelete(parent, self) {
        if (parent.delete_click % 2 == 0) {
            return;
        }
        parent.delete_click++;
        var line = self.ancestor('tr');
        var p = line.ancestor('tbody');
        var appkey = self.getData("appkey");
        var delServiceDialog = new Y.mt.widget.CommonDialog({
            id: 'del_service_dialog',
            title: '删除服务',
            content: '确认删除服务' + appkey + '?',
            width: 300,
            btn: {
                pass: doDelService,
                unpass: doCancel
            }
        });
        delServiceDialog.show();
        function doDelService() {
            parent.delete_click = 1;
            var url = '/service/delete?appkey=' + appkey + '&force=true';
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            p.removeChild(line);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    }
                }
            });
        }

        function doCancel() {
            parent.delete_click = 1;
        }
    }

    //订阅
    function clickObserver(parent, self) {
        if (parent.subscribe_click % 2 == 0) {
            return;
        }
        parent.subscribe_click++;
        var line = self.ancestor('tr');
        var p = line.ancestor('tbody');
        var appkey = self.getData("appkey");
        var subscribe = self.getData("subscribe");
        var dialogContent = ((subscribe == '0') ? '确认订阅服务' + appkey + '?' :  '确认取消订阅' + appkey + '?');
        var subscribeServiceDialog = new Y.mt.widget.CommonDialog({
            id: 'subscribe_service_dialog',
            title: '订阅服务',
            content: dialogContent,
            width: 300,
            btn: {
                pass: doSubscribeService,
                unpass: doCancel
            }
        });
        subscribeServiceDialog.show();
        function doSubscribeService() {
            parent.subscribe_click = 1;
            console.log('subscribe: ' + subscribe);
            var url =  ((subscribe == '0') ? '/subscribe/report/subscribe' : '/subscribe/report/unsubscribe');
            Y.io(url, {
                method: 'get',
                data: {
                    appkey: appkey
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '操作成功', 3);
                            Y.msgp.servicedetail.list.setLoading();
                            Y.msgp.servicedetail.list.initTable();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '操作失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '操作失败', 3);
                    }
                }
            });
        }

        function doCancel() {
            parent.subscribe_click = 1;
        }
    }

    Array.prototype.contains = function(obj) {
        var i = this.length;
        while (i--) {
            if (this[i] === obj) {
                return true;
            }
        }
        return false;
    }

}, '0.0.1', {
    requires: [
        'w-base',
        'mt-base',
        'mt-io',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/common'
    ]
});
M.use('msgp-servicedetail/list-version0.1.1', function (Y) {
    Y.msgp.servicedetail.list.init();
});
