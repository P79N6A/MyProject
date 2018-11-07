/**
 * Created by yves on 16/8/11.
 */

M.add('msgp-component/cmpt_query', function (Y) {
    var tab_style = "";
    var limitNumber = 10;
    var groupIdCache = {};
    var groupId;
    var artifactIdCache= {};
    var artifactId;
    var everPaged = false,
        totalPage,
        totalCount;
    var pbody;
    var queryListWrapper;
    var queryWrapper;

    Y.namespace('msgp.component').cmpt_query = cmpt_query;

    function cmpt_query(_tab_style, _groupId, _artifactId) {
        tab_style = _tab_style;
        document.title = '组件综合检索';
        groupId = _groupId;
        artifactId = _artifactId;
        queryWrapper = Y.one('#div_cmpt_query');
        queryListWrapper = queryWrapper.one("#query_list_wrapper");
        initWidget();
        bindWidget();
        setDefaulValue();
    }

    function setDefaulValue() {
        pbody = queryWrapper.one('#paginator_query_wrapper');
        everPaged = false;
        queryWrapper.one("#groupId" ).set("value", groupId);
        queryWrapper.one("#artifactId" ).set("value", artifactId);
        initCmpt();
    }

    function initCmpt() {
        queryWrapper.one('#cmpt').empty();
        var url = '/component/cmpt';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            queryWrapper.one('#cmpt').append('<option value=' + item.groupId +',' + item.artifactId + '>' + item.artifactId + '</option>');
                        });
                        queryWrapper.one('#cmpt').set('value',groupId+',' + artifactId);
                        getVersion(groupId, artifactId);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                }
            }
        });
    }

    function initWidget() {
        $("#groupId" ).autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in groupIdCache ){
                    response(groupIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/group_id", {
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    groupIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                groupId = ui.item.value;
                $("#artifactId" ).attr("value", "");
                $('#version').empty();
                $('#version').append('<option value=all>all</option>');
            }
        });
        $("#artifactId" ).autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in artifactIdCache ){
                    response(artifactIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/artifact_id", {
                    groupId: groupId,
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    artifactIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                artifactId = ui.item.value;
                getVersion(groupId, artifactId);
            }
        });
    }

    function getVersion(groupId, artifactId) {
        queryWrapper.one('#version').empty();
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId
        };
        var url = '/component/version';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        queryWrapper.one('#version').append('<option value=all>all</option>');
                        Y.each(ret.data, function (item) {
                            queryWrapper.one('#version').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getTableData(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                }
            }
        });
    }

    function bindWidget() {
        queryWrapper.one('#searchBtn').on('click', function () {
            getTableData(1);
        });
        queryWrapper.one('#business').on('change', function () {
            var business = this.get('value');
            if(business == "-1") {
                queryWrapper.one('#owt').empty();
                queryWrapper.one('#pdl').empty();
                /*queryWrapper.one('#app').empty();*/
                queryWrapper.one('#owt').append('<option value=all>all</option>');
                queryWrapper.one('#pdl').append('<option value=all>all</option>');
                /*queryWrapper.one('#app').append('<option value=all>all</option>');*/
                getTableData(1);
            }else{
                getOwt(business);
            }
        });
        queryWrapper.one('#owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                queryWrapper.one('#pdl').empty();
                queryWrapper.one('#pdl').append('<option value=all>all</option>');
                getTableData(1);
            }else{
                getPdl(owt);
            }
        });
       /* queryWrapper.one('#pdl').on('change', function () {
            var pdl =  queryWrapper.one('#pdl').get('value');
            if(pdl == "all") {
                queryWrapper.one('#app').empty();
                queryWrapper.one('#app').append('<option value=all>all</option>');
                getTableData(1);
            }else{
                var owt = queryWrapper.one('#owt').get('value');
                getApp(owt,pdl);
            }
        });

        queryWrapper.one('#app').on('change', function () {
            getTableData(1);
        });*/

        queryWrapper.one('#pdl').on('change', function () {
            getTableData(1);
        });

        queryWrapper.one('#cmpt').on('change', function () {
            var cmpt = this.get('value').split(",");
            groupId = cmpt[0];
            artifactId = cmpt[1];
            queryWrapper.one("#groupId" ).set("value", groupId);
            queryWrapper.one("#artifactId" ).set("value", artifactId);
            getVersion(groupId, artifactId);
        });

        queryWrapper.one('#version').on('change', function () {
            getTableData(1);
        });

        queryWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getTableData(1);
        }, '#base_type');
    }

    function getOwt(business) {
        queryWrapper.one('#owt').empty();
        queryWrapper.one('#owt').append('<option value=all>all</option>');
        queryWrapper.one('#pdl').empty();
        queryWrapper.one('#pdl').append('<option value=all>all</option>');
      /*  queryWrapper.one('#app').empty();
        queryWrapper.one('#app').append('<option value=all>all</option>');*/
        var url = '/component/owt';
        Y.io(url, {
            method: 'get',
            data: {
                "business" : business
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            queryWrapper.one('#owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getTableData(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                }
            }
        });
    }

    function getPdl(owt) {
        queryWrapper.one('#pdl').empty();
        queryWrapper.one('#pdl').append('<option value=all>all</option>');
       /* queryWrapper.one('#app').empty();
        queryWrapper.one('#app').append('<option value=all>all</option>');*/
        var url = '/component/pdl';
        Y.io(url, {
            method: 'get',
            data: {
                "owt" : owt
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            queryWrapper.one('#pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getTableData(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                }
            }
        });
    }

    function getApp(owt, pdl) {
        queryWrapper.one('#app').empty();
        queryWrapper.one('#app').append('<option value=all>all</option>');
        var url = '/component/app';
        Y.io(url, {
            method: 'get',
            data: {
                "owt" : owt,
                "pdl" : pdl
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            queryWrapper.one('#app').append('<option value=' + item + '>' + item + '</option>');
                        });
                        getTableData(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取服务失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取服务失败', 3);
                }
            }
        });
    }

    function getTableData(pageNo) {
        showWaitMsg(queryListWrapper);
        var base = queryWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = queryWrapper.one("#business").get('value');
        var owt = queryWrapper.one("#owt").get('value') == "all"  ? "": queryWrapper.one("#owt").get('value');
        var pdl = queryWrapper.one("#pdl").get('value') == "all"  ? "": queryWrapper.one("#pdl").get('value');
        var version =  queryWrapper.one("#version").get('value') == "all"  ? "" : queryWrapper.one("#version").get('value');
        /*var app =  queryWrapper.one("#app").get('value') == "all" ? "" : queryWrapper.one("#app").get('value');*/
        artifactId = Y.Lang.trim(queryWrapper.one("#artifactId").get('value'));
        groupId =  Y.Lang.trim(queryWrapper.one("#groupId").get('value'));

        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId,
            "version": version,
            "base": base,
            "business":business,
            "owt": owt,
            "pdl": pdl,
            "pageNo" : pageNo,
            "pageSize" : 15
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/details';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if(data.length == 0){
                            pbody.empty();
                            showEmptyErrorMsg(queryListWrapper, false);
                        }else {
                            fillTable(data);
                            if($('#query_list').length > 0) {
                                if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                    refreshPaginator(pbody, pobj);
                                }
                               // $('html,body').animate({scrollTop: $('#query_list_wrapper').offset().top}, 800);
                            }
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    } else {
                        showEmptyErrorMsg(queryListWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(queryListWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function fillTable(data) {
        var template =
            '<table id="query_list" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th style="width: 8%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">事业群</th> ' +
            '<th style="width: 5%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">业务线</th> ' +
            '<th style="width: 5%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">产品线</th> ' +
            '<th style="width: 12%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">项目名</th> ' +
            '<th style="width: 16%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">Appkey</th> ' +
            '<th style="width: 14%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">GroupId</th> ' +
            '<th style="width: 14%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">ArtifactId</th> ' +
            '<th style="width: 14%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">Version</th> ' +
            '<th style="width: 12%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">更新时间</th> ' +

            '</tr> ' +
            '</thead> ' +
            '<tbody> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<td style="width: 8%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.business %></td>' +
            '<td style="width: 5%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.owt %></span></td>' +
            '<td style="width: 5%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.pdl %></span></td>' +
            '<td style="width: 12%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.appArtifactId %></span></td>' +
            '<td style="width: 16%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><a href="http://octo.sankuai.com/service/detail?appkey=' + '<%=item.appkey %>#outline" target="_Blank"><%=item.appkey %></a></span></td>' +
            '<td style="width: 14%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.groupId %></td>' +
            '<td style="width: 14%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.artifactId %></td>' +
            '<td style="width: 14%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.version %></td>' +
            '<td style="width: 12%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"><%=item.uploadTime %></td>' +
            '</tr>' +
            '<% }); %>'+
            '</tbody> ' +
            '</table>';

        var micro = new Y.Template();
        var str = micro.render(template, {data : data});
        queryListWrapper.setHTML(str);
        Y.mt.widget.init();
    }

    function refreshPaginator(pbody, pobj ){
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
        getTableData( params.page );
    }
    
    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="text-align: center; font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    function showWaitMsg(node) {
        var html = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>'+
            '</div>';
        node.append(html);
    }

}, '0.0.1', {
    requires: [
        "w-tree",
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'w-paginator',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
