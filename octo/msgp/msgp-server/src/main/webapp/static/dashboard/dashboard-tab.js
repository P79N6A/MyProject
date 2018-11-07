M.add('msgp-dashboard/dashboard-tab', function (Y) {
    Y.namespace('msgp.dashboard').dashboardTab = dashboardTab;
    var tab;
    //save the tab last init time
    var last = {};
    //every tab wrapper dom node
    var validHash = ['dashboard', 'status', 'idc'];
    var validHashDesc = ['OCTO大盘', '按状态分布', '按机房分布'];

    function dashboardTab() {
        initPageHash();
        initTab();
        initHashChange();
    }

    function initPageHash() {
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#dashboard';
        }
    }

    function initTab() {
        tab = Y.mt.widget.Tab('#tab_trigger li', '#dashboard_wrapper .sheet', {
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
        var str = validHashDesc[Y.Array.indexOf(validHash, hash)];
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
        if ('dashboard' === hash) {
            Y.one('#wrap_dashboard').show();
            window.history.pushState({}, 0, window.location.pathname + window.location.hash);
        }
        Y.msgp.dashboard[hash]();
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'msgp-utils/hashchange',
        'msgp-dashboard/dashboard-version0.0.1',
        'msgp-dashboard/status',
        'msgp-dashboard/idc'
    ]
});
M.use('msgp-dashboard/dashboard-tab', function (Y) {
    Y.msgp.dashboard.dashboardTab();
});