M.add('msgp-data/dataTabNavigation_1.1', function (Y) {
    Y.namespace('msgp.data').dataTabNavigation = dataTabNavigation;
    var appkey;
    var tab;
    var mtraceUrl;
    var validHash = ['dashboard', 'operation', 'performance', 'source', 'destination', 'host', 'secondLevel', 'stream', 'trace', 'tag'];
    var preHash;

    function dataTabNavigation(_appkey, _mtraceUrl) {
        appkey = _appkey;
        mtraceUrl = _mtraceUrl;
        initPageHashAndTab();
        Y.msgp.utils.hashchange(hashChangeCallback);
    }

    function initPageHashAndTab() {
        //init hash
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#' + 'performance';
        }
        //init tab
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
    }

    function hashChangeCallback(hash) {
        if (preHash == hash) {
            return;
        }

        switchToTab(hash);
        tab.select({
            trigger: Y.one('a[href="#' + hash + '"]').ancestor('li'),
            sheet: Y.one('#wrap_data_' + hash)
        });
        //隐藏当前hash,显示第一个hash
        if(preHash) {
            Y.one('#div_data_' + preHash).hide();
            Y.one('#wrap_data_' + preHash).hide();
        }
        Y.one('#div_data_' + hash).show();
        Y.one('#wrap_data_' + hash).show();
        preHash = hash;
    }

    function switchToTab(hash) {
        var hashIndex = Y.Array.indexOf(validHash, hash);
        if (hashIndex === -1) {
            throw new Error('Invalid hash value');
        }   
        if (hash) {
            var isFirstIn = false;
            if ($("#div_data_" + hash + ":has(div)").length == 0) {
                var div_content = Y.one('#div_data_' + hash);
                var content = Y.one('#text_data_' + hash).get("value");
                div_content.setHTML(content);
                isFirstIn = true;
            }
            Y.msgp.data['data_' + hash](appkey, isFirstIn, mtraceUrl);
        } else {
            throw new Error('Invalid hash value');
        }
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'msgp-utils/hashchange',
        'msgp-data/data_dashboard_1.1',
        'msgp-data/data_host',
        'msgp-data/data_secondLevel',
        'msgp-data/data_source_1.0',
        'msgp-data/data_destination_1.0',
        'msgp-data/data_performance_1.1',
        'msgp-data/data_operation',
        'msgp-data/data_trace',
        'msgp-data/data_stream',
        'msgp-data/data_tag'
    ]
});