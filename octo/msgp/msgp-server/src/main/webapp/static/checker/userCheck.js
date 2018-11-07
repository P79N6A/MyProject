M.add('msgp-checker/userCheck', function (Y) {
    Y.namespace('msgp.checker').userCheck = init;
    var appkey;
    var tab;
    //save the tab last init time
    var last = {};
    //every tab wrapper dom node
    var validHash = ['checkerHostInfo'
        , 'checkerService'
        , 'checkerConfig'
        , 'thriftCheck'
    ];
    var validHashDesc = ['主机信息'
        , '服务列表自检'
        , '配置获取自检'
        , 'Mtthrift自检'
    ];

    function init(key) {
        appkey = key;
        initPageHash();
        initTab();
        initHashChange();
    }

    function initPageHash() {
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#checkerHostInfo';
        }
    }

    function initTab() {
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
    }

    function initHashChange() {
        Y.msgp.utils.hashchange(hashChangeCallback);
    }

    function hashChangeCallback(hash) {
        switchToTab(hash);
        //set the tab current when the change is caused by back or forward
        //but this will be excess twice when user click the tab trigger
        tab.select({
            trigger: Y.one('a[href="#' + hash + '"]').ancestor('li'),
            sheet: Y.one('#wrap_' + hash)
        });
        setDocumentTitle(hash);
    }

    function setDocumentTitle(hash) {
        var str = '主机信息 - ' + validHashDesc[Y.Array.indexOf(validHash, hash)];
        document.title = str;
    }

    function switchToTab(hash) {
        if (Y.Array.indexOf(validHash, hash) === -1) {
            throw new Error('Invalid hash value');
        }
        var now = +new Date();
        // switch tab in 30 seconds does not send the request
        if (last[hash] && ( now - last[hash] < 30 * 1000 )) {
            return;
        }
        last[hash] = +new Date();
        Y.msgp.checker.checkerTab(appkey, hash);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'msgp-utils/hashchange',
        'msgp-checker/checkerTab',
        'msgp-service/commonMap'
    ]
});
