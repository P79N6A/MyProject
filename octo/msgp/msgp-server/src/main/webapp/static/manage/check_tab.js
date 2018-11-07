M.add('msgp-manage/check_tab', function(Y){
    Y.namespace('msgp.manage').check_tab = check_tab;
    var appkey;
    var map = {
        msgp : 'check_msgp',
        sgAgent : 'check_sgAgent',
        mcc : 'check_mcc',
        thrift : 'check_thrift',
        scanner : 'check_scanner',
        daily :  'daily',
        banner : 'banner'
    };
    function check_tab( key, tab ){
        appkey = key;
        if( map[tab] ){
            Y.msgp.manage[ map[tab] ]( appkey, showOverlay,  showContent);
        }else{
            throw new Error('Invalid hash value');
        }
    }
    function showOverlay(wrapper){
        wrapper.one('.content-body').hide();
        wrapper.one('.content-overlay').show();
    }
    function showContent(wrapper){
        wrapper.one('.content-overlay').hide();
        wrapper.one('.content-body').show();
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'msgp-manage/check_msgp',
        'msgp-manage/check_sgAgent',
        'msgp-manage/check_mcc',
        'msgp-manage/check_thrift',
        'msgp-manage/check_scanner',
        'msgp-manage/daily',
        'msgp-manage/banner'
    ]
});
