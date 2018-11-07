M.add('msgp-serviceopt/operation-version0.0.13', function (Y) {
    Y.namespace('msgp.serviceopt').operation = init;
    var appkey;
    var tab;
    //save the tab last init time
    var last = {};
    //every tab wrapper dom node
    var validHash = ['config'
        , 'routes'
        , 'thriftCutFlow'
        /*
                , 'httpCutFlow'
        */
        , 'httpConfig'
        , 'accessCtrl'
        , 'appkeyAuth'
        , 'hulkOption'
        //, 'hostManage'
        , 'xmdlog'
        , 'syslog'];
    var validHashDesc = ['配置管理'
        , '服务分组'
        , 'Thrift截流'
        // , 'HTTP截流'
        , 'HTTP设置'
        , '访问控制'
        , '服务鉴权'
        , '弹性伸缩'
        //, '主机诊断'//主机管理
        , '日志级别调整'
        , '操作记录'];

    function init(key) {
        appkey = key;
        initPageHash();
        initTab();
        initHashChange();
    }

    function initPageHash() {
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#config';
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
        var str = '服务运营 - ' + validHashDesc[Y.Array.indexOf(validHash, hash)];
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
        Y.msgp.serviceopt.optTab(appkey, hash);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'msgp-utils/hashchange',
        'msgp-serviceopt/optTab-version0.4.0',
        'msgp-service/commonMap'
    ]
});
