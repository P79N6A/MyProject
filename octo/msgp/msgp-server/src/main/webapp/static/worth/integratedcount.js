/**
 * Created by yves on 16/7/14.
 */
M.add('msgp-worth/integratedcount', function (Y) {
    var _report_style = "";
    var tbody,thead,colspan = 4,allIsEmpty;
    var userMIS = "";
    var everPaged = false,
        totalPage,
        totalCount;
    var pbody;
    Y.namespace('msgp.worth').integratedcount = integratedcount;
    var template =
        '<table id="count_list" class="table table-striped table-hover " data-widget="sortTable"> ' +
        '<thead> ' +
        '<tr> ' +
        '<th>部门</th> ' +
        '<th>用户</th> ' +
        '<th>模块</th> ' +
        '<th>功能</th> ' +
        '<th>访问量</th> ' +
        '</tr> ' +
        '</thead> ' +
        '<tbody id="span_list"> ' +
        '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
                //'<td style="word-break: break-all; overflow: hidden;width: 30%"><a href="/data/kpi/span?appkey=<%= item.appkey %>&spanname=<%= item.spanname %>"><%= item.spanname %></a></td>' +
                '<td style="width: 20%"><%=item.business %></td>' +
                '<td style="width: 20%"><%=item.username %></td>' +
                '<td style="width: 20%"><%=item.module %></td>' +
                '<td style="width: 20%"><%=item.functionDesc %></td>' +
                '<td style="width: 20%"><%=item.count %></td>' +
            '</tr>' +
        '<% }); %>'+
        '</tbody> ' +
        '</table>';


    function integratedcount(report_style) {
        _report_style = report_style;
        document.title = '统计信息综合检索';
        initDatePicker();
        bindWidegt();
        initTable();
        getUserList();
    }

    function initTable() {
        pbody = Y.one('#paginator_wrapper');
        everPaged = false;
        refreshData(1);
    }


    function initDatePicker() {
        var now = new Date();
        var startInput = Y.one('#start');
        var start_day = new Date(now - 24 * 60 * 60 * 1000);
        new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: false
        });
        startInput.set('value', Y.mt.date.formatDateByString(start_day, 'yyyy-MM-dd'));
    }

    function bindWidegt() {
        Y.one('#searchForm').delegate('click', function () {
            refreshData(1);
        }, '#searchBtn');
        Y.one('#module').on('change', function () {
            var module = this.get('value');
            if(module == -1) {
                Y.one('#function_desc').empty();
                Y.one('#function_desc').append('<option value=-1>选择全部</option>');
            }else{
                getFunction(module);
            }
        });
    }

    function getFunction(module) {
        Y.one('#function_desc').empty();
        var date = Y.one("#start").get('value');
        var url = '/worth/daily/function';
        Y.io(url, {
            method: 'get',
            data: {
                "date" : date,
                "module" : module
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.one('#function_desc').append('<option value=-1>选择全部</option>');
                        Y.each(ret.data, function(item, index) {
                            Y.one('#function_desc').append('<option value=' + index + '>' + item + '</option>');
                        });
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取功能名失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取功能名失败', 3);
                }
            }
        });
    }


    function refreshData(pageNo) {
        var start = Y.one("#start").get('value');
        var business = Y.one("#business option:checked").get('value');
        var module = Y.Lang.trim(Y.one('#module option:checked').get('value'));
        //var username = Y.Lang.trim(Y.one('#username').get('value'));
        var function_desc = Y.Lang.trim(Y.one('#function_desc option:checked').get('text'));

        var username = Y.Lang.trim(Y.one('#user-list').get('value'));
        var pattern =new RegExp("\((.*)\)", "g");
        if(username.length > 0 && pattern.test(username)){
            username = userMIS;
        }else {
            username = "";
        }
        allIsEmpty = (business == -1 && module == -1 && username == "" && function_desc == "选择全部");
        var searchData = {
                "date": start,
                "business": business,
                "username": username,
                "module": module,
                "function_desc" : function_desc,
                "pageNo" : pageNo,
                "pageSize" : 18
        };
        Y.msgp.utils.urlAddParameters(searchData);
        getTableList(searchData, pageNo);
    }

    function getTableList(searchData, pageNo) {
        var url = '/worth/daily/details';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (data.icount.length !== 0) {
                            fillList(data.icount)
                            if( !everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount ) {
                                refreshPaginator(pbody, pobj);
                            }
                            $('html,body').animate({scrollTop:$('#count_list').offset().top}, 800);
                        } else {
                            showEmptyErrorMsg(0);
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '数据获取失败', 3);
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(!0);
                }
            }
        });
    }

    function fillList(data) {
        var micro = new Y.Template();
        var str = micro.render(template, {data : data});
        Y.one("#count_list").empty();
        Y.one("#count_list").setHTML(str);
        Y.mt.widget.init();
    }

    function showEmptyErrorMsg(isError) {
        var html = '<tr><td colspan="12">' + (isError ? '数据获取失败' : '未产生数据') + '</td></tr>'
        Y.one("#count_list").empty();
        var micro = new Y.Template();
        var str = micro.render( template, {data : []});
        Y.one("#count_list").setHTML(str);
        Y.one("#span_list").setHTML(html)
    }

    function getUserList() {
        var ndUserList = Y.one('#user-list');
        new Y.mt.widget.AutoCompleteList({
            searchInterval: 500,
            node: ndUserList,
            action: '/userorg/user/search',   // 异步请求的后端url，具体到后端查询的url为"/userRole/employee?q=xxx"
            ayncsearch: true,   // 异步搜索
            msg: "输入 中文|拼音|首拼 搜索",
            highlightFirstMatch: true,
            listParam: "name",
            showMax: 12,
            matchMode: "none",
            defaultItem: false,
            noDataTips: "",
            rememberable: false,
            callback: function(data){
                userMIS = data.login;
            }
        });
    }

    function refreshPaginator( pbody, pobj ){
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback : changePage
        });
    }
    function changePage(params){
        refreshData(params.page);
    }

}, '0.0.1', {
    requires: [
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'template',
        'msgp-utils/common',
        "node-event-simulate",
        'w-autocomplete',
        'w-paginator',
        'msgp-utils/msgpHeaderTip'
    ]
});
