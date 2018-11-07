/* jshint indent : false */
M.add('msgp-manage/operationLog', function (Y) {

    Y.namespace('msgp.manage').operationLog = operationLog;
    var appkey;
    var applist = Y.one('#apps_select');
    var startInput = Y.one('#start_time'),
        endInput = Y.one('#end_time');
    var lwrap = Y.one('#log_wrap'),
        tbody = lwrap.one('tbody');
    var pbody = Y.one('#paginator_wrapper');
    var colspan = 5;

    var everPaged = false,
        totalPage,
        totalCount;

    var trTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-trigger="<%= item.item %>" data-threshold="<%= item.threshold %>" data-function="<%= item.function %>" data-itemdesc="<%= item.itemDesc %>" data-functiondesc="<%= item.functionDesc %>">',
        '<td><%= Y.mt.date.formatDateByString( new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td><%= item.operatorName %></td>',
        '<td><%= item.entityType %></td>',
        '<td><%= item.appkey %></td>',
        '<td>',
        '<% Y.Array.each(item.desc, function(iterator, index){ %>',
        '<%= iterator %><br/>',
        '<% }); %>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    function operationLog(key) {
        appkey = key;

        initDatePicker();
        //initEvent();
        doGetOperations(1);
    }
    function initDatePicker(){
        var now = new Date();
        var yestoday = new Date( now - 7*24*60*60*1000 );
        sdate = new Y.mt.widget.Datepicker({
            node : startInput,
            showSetTime : true
        });
        sdate.on('Datepicker.select',function(){
            doGetOperations(1);
        });
        startInput.set( 'value', Y.mt.date.formatDateByString( yestoday, 'yyyy-MM-dd hh:mm:ss' ) );
        edate = new Y.mt.widget.Datepicker({
            node : endInput,
            showSetTime : true
        });
        edate.on('Datepicker.select',function(){
            doGetOperations(1);
        });
        endInput.set( 'value', Y.mt.date.formatDateByString( now, 'yyyy-MM-dd hh:mm:ss' ) );
    }
    
    function doGetOperations(pageNo){
        var se = getStartEnd();
        if( !se ) return;
        showOverlay();
        var url = '/manage/operation/log';
        Y.io(url, {
            method : 'get',
            data : {
                pageNo : pageNo,
                pageSize : 20,
                start : se.start,
                end : se.end
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    var pobj = ret.page;
                    if( ret.isSuccess ){
                        if( data && data.length > 0 ){
                            fillOperations( data );
                            if( !everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount ) {
                                refreshPaginator(pbody, pobj);
                            }
                        }else{
                            emptyOrError();
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;
                    }else{
                        emptyOrError( true );
                    }
                },
                failure : function(){
                    emptyOrError( true );
                }
            }
        });
    }
    function getStartEnd(){
        var obj = {
            start : '',
            end : ''
        };
        var s = startInput.get('value'),
            e = endInput.get('value');
        var reg = /^\d{4}(-\d{2}){2} \d{2}:\d{2}:\d{2}$/;
        if( s && reg.test(s) ){
            obj.start = s;
        }
        reg.lastIndex = 0;
        if( e && reg.test(e) ){
            obj.end = e;
        }
        if( s !== obj.start || e !== obj.end ){
            Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
            return null;
        }
        if( obj.start > obj.end ){
            Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
            return null;
        }
        return obj;
    }
    function fillOperations( arr ){
        var micro = new Y.Template();
        var html = micro.render( trTemplate, {data:arr} );
        tbody.setHTML( html );
        showContent();
    }
    function emptyOrError( isError ){
        var html = '<tr><td colspan="'+ colspan +'">'+ (isError ? '获取失败':'没有内容');
        tbody.setHTML( html );
        pbody.empty();
        showContent();
    }
    function showContent(){
        lwrap.one('.content-overlay').hide();
        lwrap.one('.content-body').show();
    }
    function showOverlay(){
        lwrap.one('.content-body').hide();
        lwrap.one('.content-overlay').show();
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
        doGetOperations( params.page );
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
M.use('msgp-manage/operationLog', function (Y) {
    Y.msgp.manage.operationLog(key);
});
