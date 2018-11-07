M.add('msgp-manage/sgagent/agentChecker', function(Y){
    Y.namespace('msgp.manage').agentChecker = agentChecker;

    var wrap = Y.one('#wrap_agentChecker');
    var tbody = wrap.one('tbody');
    var colspan = 5;
    var addRouteDialog,
        dNameInput,
        appsInput;
    var successCallback = function(data) {
        if( data && data.length > 0 ) {
            var micro = new Y.Template();
            var html = micro.render(listTemplateDetail, {data: data});
            addRouteDialog.getBody().one('#providers_input_add').setHTML(html);
        }
    };
    var dialogTemplate = [
        '<div id="add_supplier_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label"><b>检测名称：</b></label>',
                '<div class="controls">',
                    '<input id="name_input_add" type="text" placeholder="名称必填，不可重复" />',
                    '<span class="tips"></span>',
                '</div>',
            '</div>',
            '<div class="control-group mb0">',
                '<div class="row-fluid">',
                    '<div class="span6 consumer-wrapper">',
                        '<h4 class="text-center mt0 mb10">提供者</h4><span id="span_provider_input_add" class="tips"></span>',
                        '<div class="box row-fluid dialog-ul-wrapper" style="height: 400px;">',
                            '<a style="padding-left:1em">环境：</a>',
                            '<div id="supplier_env_select" class="btn-group">',
                                '<a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>',
                                '<a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>',
                                '<a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>',
                            '</div>',
                            '<a style="padding-left:0.5em"></a>',
                            '<div class="input-append">',
                                '<input id="searchBox" class="span3" placeholder="主机 port:x 主用 ..." type="text" style="width:220px" />',
                                '<button class="btn btn-primary" type="button" id="searchBtn">查询</button>',
                            '</div>',
                            '<div id="providers_input_add" style="height: 338px" class="box dialog-ul-wrapper-inner ips-ul-wrapper" style="border-left:0 none;">',
                            '</div>',
                        '</div>',
                    '</div>',
                    '<div class="span6 provider-wrapper">',
                        '<h4 class="text-center mt0 mb10">服务节点<span id="span_apps_input_add" class="tips"></span></h4>',
                        '<div class="box dialog-ul-wrapper provider-ul-wrapper" style="height: 400px;">',
                            '<textarea style="width: 96%; height: 97%;" id="apps_input_add" placeholder="多个appkey，英文逗号分割" >',
                            '</textarea>',
                            '<span class="tips"></span>',
                        '</div>',
                    '</div>',
                '</div>',
            '</div>',
        '</div>'
    ].join('');

    var trTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-info=<%= Y.JSON.stringify(item) %> >',
        '<td><%= item.name %></td>',
        '<td><%= item.protocol %></td>',
        '<td style="word-break: break-all; overflow: hidden;width: 30%"><%= item.providers %></td>',
        '<td style="word-break: break-all; overflow: hidden;width: 30%"><%= item.apps %></td>',
        '<td><%= Y.mt.date.formatDateByString( new Date(item.time), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '<td>',
        '<div id="one-enabled" class="btn-group btn-enabled">',
        '<button data-status="1" class="btn btn-mini btn-alive <%= (item.status==1)?"active":"" %>">启用</button>',
        '<button data-status="0" class="btn btn-mini btn-dead <%= item.status==0?"active":"" %>">停用</button>',
        '</div>',
        '<button id="del_check" class="btn btn-mini btn-del" style="background:#f82c2c;color:#fff;" title="删除节点">删除</button>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    var listTemplateDetail = [
        '<ul class="unstyled dialog-ul">',
        '<li class="">',
        '<label class="checkbox mb0">',
        '<input id="all-check" type="checkbox"/>',
            '<span>全选</span>',
        '</label>',
        '</li>',
        '<% Y.Array.each(this.data, function(item,index){ %>',
        '<li class="">',
        '<label class="checkbox mb0">',
        '<input data-ip="<%= item.ip %>" id="one-checkbox" type="checkbox"/>',
            '<span><%= item.name %>(<%= item.ip %>)</span>',
        '</label>',
        '</li>',
        '<% }); %>',
        '</ul>'
    ].join('');

    function agentChecker() {
        bindEvent();
        bindTableEvent();
        fillTable();
    }

    function bindEvent() {
        wrap.delegate('click', function(){
            addRouteDialog = addRouteDialog ? addRouteDialog : new Y.mt.widget.CommonDialog({
                id : 'add_check_dialog',
                title : '增加检查',
                width : 760,
                btn : {
                    pass : doAddCheck
                }
            });
            var micro = new Y.Template();
            var html = micro.render( dialogTemplate);
            addRouteDialog.setContent( html );
            bindDialogEvent(addRouteDialog);
            initCheck();
            addRouteDialog.show();
        },'#add_check');
    }

    function initCheck() {
        dNameInput = Y.msgp.utils.check.init(Y.one( '#name_input_add' ), {
            type : 'string',
            warnMsg : '不能为空',
            warnElement : Y.one( '#name_input_add' ).next()
        });
        appsInput = Y.msgp.utils.check.init(Y.one( '#apps_input_add' ), {
            type : 'string',
            spaceOk : false,
            warnMsg : '不能为空',
            warnElement : Y.one( '#span_apps_input_add' )
        });
    }

    function bindDialogEvent(addRouteDialog) {
        var node = addRouteDialog.getBody();

        node.delegate('click', function() {
            //清空搜索框
            node.one('#searchBox').set('value', "");
            node.all('#supplier_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            var env = this.getAttribute('value');
            getProviders(env, successCallback);
        }, '#supplier_env_select .btn');

        node.delegate('click', function() {
            //全选
                var ifChecked = this.get("checked");
                //单选与全选保持一致
                node.all('#one-checkbox').set("checked",ifChecked);
        }, '#all-check');

        node.delegate('click', function(){
            //全选与单选保持一致
            var allOneCheck = node.all('#one-checkbox');
            var allOneChecked = node.all('#one-checkbox:checked');
            if(allOneChecked.size() === 0) {
                node.one('#all-check').set("checked", false);
            } else {
                if(allOneCheck.size() === allOneChecked.size()) {
                    node.one('#all-check').set("checked", true);
                }
            }
        }, '#one-checkbox');

        node.delegate('click', function(){
            doSearch(node);
        }, '#searchBtn');

        getProviders(3, successCallback);
    }

    function doSearch(node) {
        //获取输入文档
        searchInput = Y.Lang.trim( node.one('#searchBox').get('value') );
        if( searchInput ){
            //调用后台
            getSearchSupplier();
        }
    }

    function getSearchSupplier( ){
        var env = Y.one('#supplier_env_select a.btn-primary').getAttribute('value');
        var url = '/manage/agent/checker/search';
        Y.io(url, {
            method : 'get',
            data : {
                env : env,
                keyword : searchInput
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ) {
                        var data = ret.data;
                        var pobj = ret.page;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            successCallback(data);
                        }
                    }
                }
            }
        });
    }

    function getProviders(env, sc) {
        var url = "/manage/agent/checker/providers?env=" + env;
        Y.io(url, {
            method : 'get',
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    if( ret.isSuccess ){
                        sc(data)
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '获取失败' , 3);
                }
            }
        });
    }

    function doAddCheck() {
        var name = dNameInput.node.get('value');
        var checkedProvider = addRouteDialog.getBody().all('#one-checkbox:checked');
        var apps = appsInput.node.get('value');
        if(name == '' || checkedProvider.size() == 0 || apps == '') {
            Y.msgp.utils.msgpHeaderTip('error', '各项不能为空', 3);
            return true;
        }

        var dataList = new Array();
        checkedProvider.each(function (item, index) {
            var data = item.getData('ip');
            dataList.push(data);
        });
        var providers = dataList.join(",");

        var obj = {
            id : 0,
            name : name,
            providers : providers,
            apps : apps,
            status : 0,
            time : (new Date()).valueOf()
        };
        var url = "/manage/agent/checker/add";
        Y.io(url, {
            method : 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(obj),
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    if( ret.isSuccess ){
                            Y.msgp.utils.msgpHeaderTip('success', '添加成功', 2);
                            fillTable()
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '添加失败 ' + ret.msg, 4);
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '添加失败' , 3);
                }
            }
        });
    }

    function fillTable() {
        var url = "/manage/agent/checker/get";
        Y.io(url, {
            method : 'get',
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    var data = ret.data;
                    if( ret.isSuccess ){
                        if( data && data.length > 0 ){
                            var micro = new Y.Template();
                            var html = micro.render( trTemplate, {data:data} );
                            tbody.setHTML( html );
                        } else {
                            emptyOrError();
                        }
                    }else{
                        emptyOrError( true );
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '获取失败' , 3);
                }
            }
        });
    }

    function bindTableEvent() {
        function successCallback( ){
            Y.msgp.utils.msgpHeaderTip( 'success', '删除成功', 3 );
            fillTable()
        }
        function errorCallback(ret){
            Y.msgp.utils.msgpHeaderTip( 'error', '删除失败，'+ ret.msg, 3 );
        }
        wrap.delegate('click', function(){
            var el = this;
            if( el.hasClass('active') ) {
                return;
            }
            var status = +el.getData('status');
            doChangeEnabled( el, status);
        }, '#one-enabled .btn');

        wrap.delegate('click', function(){
            //删除
            var line = this.ancestor('tr');
            var data = Y.JSON.parse(line.getData('info'));
            delCheck(data, successCallback, errorCallback);
        }, '#del_check');
    }

    function doChangeEnabled( el, status ){
        var line = el.ancestor('tr');
        var data = Y.JSON.parse(line.getData('info'));
        data.status = status;
        refreshLineData(data, successCallback, errorCallback);
        function successCallback( ){
            Y.msgp.utils.msgpHeaderTip( 'success', '修改成功', 3 );
            fillTable()
        }
        function errorCallback(ret){
            Y.msgp.utils.msgpHeaderTip( 'error', '修改失败', 3 );
        }
    }

    function delCheck(data, sc, ec) {
        var url = '/manage/agent/checker/del';
        Y.io( url, {
            method : 'delete',
            data : {
                id : data.id
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        sc && sc();
                    }else{
                        ec && ec(ret);
                    }
                },
                failure : function(){
                    ec && ec();
                }
            }
        });
    }

    function refreshLineData(data, sc, ec){
        var url = '/manage/agent/checker/update';
        Y.io( url, {
            method : 'put',
            data : Y.JSON.stringify( data ),
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        sc && sc();
                    }else{
                        ec && ec();
                    }
                },
                failure : function(){
                    ec && ec();
                }
            }
        });
    }

    function emptyOrError( isError ){
        var html = '<tr><td colspan="'+ colspan +'">'+ (isError ? '获取失败':'没有内容');
        tbody.setHTML( html );
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'node-event-simulate',
        'template',
        'transition',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check'
    ]
});