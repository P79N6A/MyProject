M.add('msgp-serviceopt/optControl', function(Y){
    Y.namespace('msgp.serviceopt').optControl = detailControl;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_control');
    function detailControl( key, func1, func2 ){
        appkey = key;
        showOverlay = func1;
        showContent = func2;
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io'
    ]
});
