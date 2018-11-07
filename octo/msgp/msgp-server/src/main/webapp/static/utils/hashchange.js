M.add('msgp-utils/hashchange', function(Y){
    Y.namespace( 'msgp.utils' ).hashchange = hashchange;
    function hashchange( callback, context ){
        if( !callback || !Y.Lang.isFunction( callback ) ){
            throw new Error( 'Need a callback function!' );
        }
        context = context ? ( Y.isObject( context ) ? context : null ) : null;
        window.onhashchange = function(){
            var hash = location.hash.slice( 1 );
            callback.call( context, hash );
        };
        initPageHash( callback, context );
    }
    function initPageHash( callback, context ){
        var hash = location.hash.slice( 1 );
        callback.call( context, hash );
    }
}, '0.0.1', {
    requires : [
        'mt-base'
    ]
});
