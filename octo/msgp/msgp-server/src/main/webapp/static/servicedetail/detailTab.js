M.add('msgp-servicedetail/detailTab', function (Y) {
    Y.namespace('msgp.servicedetail').detailTab = detailTab;
    var appkey;
    var map = {
        outline: 'detailOutline',
        supplier: 'detailSupplier',
        consumer: 'detailConsumer',
        component: 'cmpt_detail',
        oncall: 'oncall'
    };

    function detailTab(key, tab) {
        appkey = key;
        if (map[tab]) {
            Y.msgp.servicedetail[map[tab]](appkey, showOverlay, showContent);
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
        'mt-base',
        'msgp-servicedetail/detailOutline',
        'msgp-servicedetail/detailSupplier-version0.0.6',
        'msgp-servicedetail/detailConsumer-version0.0.2',
        'msgp-servicedetail/oncall',
        'msgp-component/cmpt_detail'
    ]
});
