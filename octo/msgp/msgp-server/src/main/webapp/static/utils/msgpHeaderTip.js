M.add('msgp-utils/msgpHeaderTip', function(Y){
    Y.namespace('msgp.utils').msgpHeaderTip = init;
    var hTip, st;
    function init( type, msg, time ){
        hTip = Y.mt.widget.HeaderTip( type, msg );
        if( !time || !Y.Lang.isNumber(time) ) return;
        st = setTimeout(function(){
            clearTimeout(st);
            if( hTip ) hTip.remove( true );
        }, time*1000);
    }
}, '0.0.1', {
    requires : [
        'w-base',
        'mt-base'
    ]
});
