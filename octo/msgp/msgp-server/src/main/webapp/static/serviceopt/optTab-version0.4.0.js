M.add('msgp-serviceopt/optTab-version0.4.0', function (Y) {
    Y.namespace('msgp.serviceopt').optTab = detailTab;
    var appkey;
    var map = {
        config: 'optConfig',
        routes: 'optRoutes',
        thriftCutFlow: 'optThriftCutFlow',
        httpConfig: 'optHttpConfig',
        accessCtrl: 'optAccessCtrl',
        appkeyAuth: 'optAppkeyAuth',
        hulkOption: 'optHulkOption',
        //hostManage: 'optHostManage',
        xmdlog: 'optXmdlog',
        syslog: 'optSyslog'
    };

    function detailTab(key, tab) {
        appkey = key;
        if (map[tab]) {
            Y.msgp.serviceopt[map[tab]](appkey, showOverlay, showContent);
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
        , 'msgp-serviceopt/optRoutes-version0.0.18'
        , 'msgp-serviceopt/optConfig-version0.1.1'
        , 'msgp-serviceopt/optThriftCutFlow-version0.1.0'
        , 'msgp-serviceopt/optHttpConfig0.1.0'
        , 'msgp-serviceopt/optAccessCtrl'
        , 'msgp-serviceopt/optControl'
        , 'msgp-serviceopt/optAppkeyAuth'
        , 'msgp-serviceopt/optHulkOption'
        , 'msgp-serviceopt/optSyslog'
        , 'msgp-serviceopt/optXmdlog'
        // , 'msgp-serviceopt/optHostManage-version0.0.10'
    ]
});