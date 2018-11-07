M.add('msgp-dashboard/bindSearch', function(Y){
    Y.namespace('msgp.dashboard').bindSearch = bindSearch;
    var searchInput = Y.one('#search_input');

    function bindSearch(){
        searchInput.on('keyup', function(e){
            if( e.keyCode === 13 ){
                doSearch();
            }
        });
        Y.one('#search_button').on('click', doSearch);
    }
    function doSearch(){
        var keyword = Y.Lang.trim( searchInput.get('value') );
        location.href = '/service?keyword=' + keyword;
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io'
    ]
});
M.use('msgp-dashboard/bindSearch', function (Y) {
    Y.msgp.dashboard.bindSearch();
});
