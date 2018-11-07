M.add('msgp-service/getAvailableIps', function(Y){
    Y.namespace('msgp.service').getAvailableIps = getAvailableIps;
    var dialog;
    var isOpen;
    function getAvailableIps( appkey, env, callback ){
        showDialog();
        var url = '/service/' + appkey + '/config/ips';
        Y.io(url,{
            method : 'get',
            data : {
                env : env
            },
            on : {
                success : function(id, o){
                    if( !isOpen ) return;
                    hideDialog();
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        if( ret.data.length !== 0 ){
                            callback && callback( ret.data );
                        }else{
                            Y.msgp.utils.msgpHeaderTip('error', '该服务还没有已部署的节点', 5);
                        }
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '加载数据失败', 3);
                    }
                },
                failure : function(){
                    hideDialog();
                    Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
                }
            }
        });
    }
    function showDialog(){
        dialog = dialog ? dialog : new Y.mt.widget.CommonDialog({
            width : 400,
            title : '加载数据',
            content : '<i class="fa fa-spinner fa-spin text-blue mr10"></i>正在获取服务节点列表...',
            closeCallback : function(){
                isOpen = false;
            }
        });
        dialog.show();
        isOpen = true;
    }
    function hideDialog(){
        dialog.close();
    }
},'0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-base',
        'msgp-utils/msgpHeaderTip'
    ]
});
