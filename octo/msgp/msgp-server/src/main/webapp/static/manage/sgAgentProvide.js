M.add('msgp-manage/sgAgentProvide', function (Y) {
    Y.namespace('msgp.manage').sgAgentProvide = sgAgentProvide;
    var appkey = "aaa";
    var lwrap = Y.one('#log_wrap'),
        tbody = lwrap.one('tbody');
    var colspan = 4;

    var trTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr>',
        '<td><%= item.name %>(<%= item.ip %>):<%= item.port %></td>',
        '<td><%= item.envDesc %></td>',
        '<td><%= item.role==0?"主机":"备用" %></td>',
        '<td><%= item.statusDesc %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    function emptyOrError( isError ){
        var html = '<tr><td colspan="'+ colspan +'">'+ (isError ? '获取失败':'没有内容') +'<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML( html );
    }

    function sgAgentProvide(key, version, envId, thrifttype,region) {
        var url = '/manage/com.sankuai.inf.sg_agent/provideGroupByVersion?envId=' + envId + "&version=" + version+ "&thrifttype=" + thrifttype+"&region="+region;
        Y.io(url, {
            method : 'get',
            on : {
                success : function(id,o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        var providerList = ret.data.providerList;
                        function func( obj ) {
                            for (var i2 = 0, l2 = providerList.length; i2 < l2; i2++) {
                                var tmp = providerList[i2];
                                tmp.statusDesc = obj.status[tmp.status] || tmp.status;
                                tmp.envDesc = obj.env[tmp.env] || tmp.env;
                                tmp.roleDesc = obj.role[tmp.role] || tmp.role;
                            }
                        }
                        Y.msgp.service.commonMap( func );
                        var micro = new Y.Template();
                        var html = micro.render(trTemplate, {data: providerList});
                        tbody.setHTML( html );
                    }else{
                        emptyOrError(true);
                    }
                },
                failure : function(){
                    emptyOrError(true);
                }
            }
        });
    }



}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'mt-base',
        'msgp-service/commonMap'
    ]
});