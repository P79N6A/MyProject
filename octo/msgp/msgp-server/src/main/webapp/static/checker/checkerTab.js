M.add('msgp-checker/checkerTab', function (Y) {
    Y.namespace('msgp.checker').checkerTab = detailTab;
    var appkey;
    var map = {
        checkerHostInfo: 'checkerHostInfo',
        checkerService: 'checkerService',
        checkerConfig: 'checkerConfig',
        thriftCheck: 'thriftCheck',
    };

    function detailTab(key, tab) {
        appkey = key;
        if (map[tab]) {

            Y.msgp.checker[map[tab]](appkey, showOverlay, showContent);
        } else {
            throw new Error('Invalid hash value');
        }
    }

    function showOverlay(wrapper) {
        wrapper.one('.content-body').hide();
        wrapper.one('.content-overlay').show();
    }

    function showContent(wrapper) {
        wrapper.one('.content-overlay').hide();
        wrapper.one('.content-body').show();
    }
}, '0.0.1', {
    requires: [
        'mt-base'
        , 'msgp-checker/checkerHostInfo'
        , 'msgp-checker/checkerService'
        , 'msgp-checker/checkerConfig'
        , 'msgp-checker/thriftCheck'
    ]
});