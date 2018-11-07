M.add('msgp-manage/check_scanner', function(Y){
    Y.namespace('msgp.manage').check_scanner = check_scanner;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_scanner');

    var everPaged_action = false,
        totalPage_action,
        totalCount_action;

    var everPaged_log = false,
        totalPage_log,
        totalCount_log;
    function check_scanner( key, func1, func2 ){
        appkey = key;
        showOverlay = func1;
        showContent = func2;
        showContent(wrapper);
        wrapper.delegate('click', function(){
            everPaged_action = false;
            everPaged_log = true;
            getJobCheck();
            getActionCheck(1);
            getScannerLog(1);
        }, '#refresh_table');
        getAvailability();
        getJobCheck();
        getActionCheck(1);
        getScannerLog(1);
    }

    function getAvailability() {
        var url = "/manage/scanner/availability";
        Y.io(url, {
            method : 'get',
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        setAvailability(ret.data)
                    }else{
                        resetAvailability();
                    }
                }
            },
                failure : function(){
                    Y.one("#scannner_availability").set("text", "100.0000%");
                }
        });
    }

    function setAvailability(data) {
        Y.one("#scannner_availability").set("text", data[0]);
        Y.one("#scannner_master_availability").set("text", data[1]);
        Y.one("#scannner_detector_availability").set("text", data[2]);
        Y.one("#scannner_updater_availability").set("text", data[3]);
    }
    
    function resetAvailability() {
        Y.one("#scannner_availability").set("text", "100.0000%");
        Y.one("#scannner_master_availability").set("text", "100.0000%");
        Y.one("#scannner_detector_availability").set("text", "100.0000%");
        Y.one("#scannner_updater_availability").set("text", "100.0000%");
    }

    function getJobCheck() {
        var wrapper = Y.one('#scanner_job');
        var mbody = wrapper.one('tbody');
        var url = "/manage/job/check?appkeys=com.sankuai.octo.scanner&appkeys=com.sankuai.inf.octo.scannermaster";
        var colspan = 5;
        showOverlay(wrapper);

        Y.io(url, {
            method : 'get',
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    if( ret.isSuccess ){
                        if( data && data.length > 0 ){
                            var template = [
                                '<% Y.Array.each(this.data, function(item, index){ %>',
                                '<tr>',
                                '<td><%= item.identifier %></td>',
                                '<td><%= item.job %></td>',
                                '<td><%= Y.mt.date.formatDateByString( new Date(item.stime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
                                '<td><%= item.cost %></td>',
                                '<td><%= item.content %></td>',
                                '</tr>',
                                '<% }); %>'
                            ].join('');
                            fillTable( data, template, mbody, wrapper);
                        }else{
                            emptyOrError(false, mbody, wrapper, colspan);
                        }
                    }
                },
                failure : function(){
                    emptyOrError(true, mbody, wrapper, colspan);
                }
            }
        });
    }

    function getActionCheck(pageNo) {
        var wrapper = Y.one('#scanner_action');
        var mbody = wrapper.one('tbody');
        var pbody = wrapper.one('#paginator_action');
        var url = "action/check";
        var colspan = 4;
        showOverlay(wrapper);

        Y.io(url, {
            method : 'get',
            data : {
                pageNo : pageNo,
                pageSize : 20
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    var pobj = ret.page;
                    if( ret.isSuccess ){
                        if( data && data.length > 0 ){
                            var template = [
                                '<% Y.Array.each(this.data, function(item, index){ %>',
                                '<tr>',
                                '<td><%= Y.mt.date.formatDateByString( new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
                                '<td><%= item.entityType %></td>',
                                '<td><%= item.appkey %></td>',
                                '<td><%= item.desc %></td>',
                                '</tr>',
                                '<% }); %>'
                            ].join('');
                            fillTable( data, template, mbody, wrapper);
                            if( !everPaged_action || totalPage_action !== pobj.totalPageCount || totalCount_action !== pobj.totalCount ) {
                                refreshPaginator(pbody, pobj, function (params){
                                    getActionCheck( params.page );
                                });
                            }
                        }else{
                            emptyOrError(false, mbody, wrapper, colspan);
                        }
                        everPaged_action = true;
                        totalPage_action = pobj.totalPageCount;
                        totalCount_action = pobj.totalCount;
                    }
                },
                failure : function(){
                    emptyOrError(true, mbody, wrapper, colspan);
                }
            }
        });
    }

    function getScannerLog(pageNo) {
        var wrapper = Y.one('#scanner_log');
        var mbody = wrapper.one('tbody');
        var pbody = wrapper.one('#paginator_log');
        var url = "scanner/log";
        var colspan = 6;
        showOverlay(wrapper);

        Y.io(url, {
            method : 'get',
            data : {
                pageNo : pageNo,
                pageSize : 20
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    var pobj = ret.page;
                    if( ret.isSuccess ){
                        if( data && data.length > 0 ){
                            var template = [
                                '<% Y.Array.each(this.data, function(item, index){ %>',
                                '<tr>',
                                '<td><%= item.appkey %></td>',
                                '<td><% if(item.env == 3){ %> prod <% }else if(item.env == 2){ %> stage <% }else if(item.env == 1){ %> test <% }else{%> multi-env <%}%></td>',
                                '<td><%= item.provider %></td>',
                                '<td><%= item.category %></td>',
                                '<td><%= item.content %></td>',
                                '<td><%= Y.mt.date.formatDateByString( new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
                                '</tr>',
                                '<% }); %>'
                            ].join('');
                            fillTable( data, template, mbody, wrapper);
                            if( !everPaged_log || totalPage_log !== pobj.totalPageCount || totalCount_log !== pobj.totalCount ) {
                                refreshPaginator(pbody, pobj, function (params){
                                    getScannerLog( params.page );
                                });
                            }
                        }else{
                            emptyOrError(false, mbody, wrapper, colspan);
                        }
                        everPaged_log = true;
                        totalPage_log = pobj.totalPageCount;
                        totalCount_log = pobj.totalCount;
                    }
                },
                failure : function(){
                    emptyOrError(true, mbody, wrapper, colspan);
                }
            }
        });
    }

    function fillTable(data, template, mbody, wrapper) {
        var micro = new Y.Template();
        var html = micro.render(template, {data: data});
        mbody.setHTML( html );
        showContent(wrapper);
    }

    function emptyOrError( isError, mbody, wrapper, colspan) {
        var html = '<tr><td colspan="'+ colspan +'">'+ (isError ? '获取失败':'没有内容');
        mbody.setHTML( html );
        showContent(wrapper);
    }

    function refreshPaginator( pbody, pobj , changePage){
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback : changePage
        });
    }

}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-date',
        'w-paginator',
        'mt-date'
    ]
});