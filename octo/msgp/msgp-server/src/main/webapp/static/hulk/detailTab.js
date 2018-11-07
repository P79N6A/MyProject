M.add('msgp-hulk/detailTab', function(Y){
    Y.namespace('msgp.hulk').detailTab = detailTab;
    var appkey;
    var map = {
        outline : 'detailOutline',
        scalingGroup : 'scalingGroup'
        //vmQuota : 'detailOutline',
        //containerQuota : 'detailOutline',
        //review : 'detailOutline'
    };

    function detailTab( key, tab ){
        appkey = key;
        if( map[tab] ){
            Y.msgp.hulk[ map[tab] ]( appkey, showOverlay, showContent );
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
        'msgp-hulk/detailOutline',
        'msgp-hulk/scalingGroup',
    ]
});
