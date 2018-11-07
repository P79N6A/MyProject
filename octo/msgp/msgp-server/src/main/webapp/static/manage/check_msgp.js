M.add('msgp-manage/check_msgp', function(Y){
    Y.namespace('msgp.manage').check_msgp = msgpCheck;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_msgp'),
        colspan = 3;

    function msgpCheck( key, func1, func2 ) {
        appkey = key;
        showOverlay = func1;
        showContent = func2;
        wrapper.delegate('click', function(){
            getScheduleCheck();
        }, '#refresh_supplier');
        getScheduleCheck();
        getRouteCheck();
    }

    function getScheduleCheck() {
        var wrapper = Y.one('#msgp_schedule');
        var mbody = wrapper.one('tbody');
        var url = '/manage/'+ appkey + '/msgpSelfCheck';

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
                                '<td><%= item.desc %></td>',
                                '<td>',
                                '<% Y.Array.each(item.detail, function(iterator, index){ %>',
                                '<%= iterator %><br/>',
                                '<% }); %>',
                                '</td>',
                                //'<td><%= item.other %></td>',
                                '</tr>',
                                '<% }); %>'
                            ].join('');
                            fillTable( data, template, mbody, wrapper);
                        }else{
                            emptyOrError(false, mbody, wrapper);
                        }
                    }
                },
                failure : function(){
                    emptyOrError(true, mbody, wrapper);
                }
            }
        });
    }

    function getRouteCheck() {
        var url = '/manage/route/checker?envId=3';
        var wrapper = Y.one('#msgp_route');
        var mbody = wrapper.one('tbody');

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
                                '<td><%= item.appkey %></td>',
                                '<td><%= item.desc %></td>',
                                //'<td></td>',
                                '</tr>',
                                '<% }); %>'
                            ].join('');
                            fillTable( data, template, mbody, wrapper );
                        }else{
                            emptyOrError(false, mbody, wrapper);
                        }
                    }
                },
                failure : function(){
                    emptyOrError(true, mbody, wrapper);
                }
            }
        });
    }

    function fillTable( data, template, mbody, wrapper) {
        var micro = new Y.Template();
        var html = micro.render(template, {data: data});
        mbody.setHTML( html );
        showContent(wrapper);
    }

    function emptyOrError( isError, mbody, wrapper ){
        var html = '<tr><td colspan="'+ colspan +'">'+ (isError ? '获取失败':'没有内容') +'<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        mbody.setHTML( html );
        showContent(wrapper);
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io'
    ]
});