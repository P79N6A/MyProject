/**
 * Created by lhmily on 08/21/2015.
 */
M.add('msgp-manage/sgagent/agentTabNavigation-version0.0.1', function (Y) {
    Y.namespace('msgp.manage').agentTabNavigation = agentTabNavigation;

    var appkey;
    var tab;
    //save the tab last init time
    var last = {};
    //every tab wrapper dom node
    var validHash = ['availabilityData', 'agentChecker', 'agentSwitch', 'httpAuth'];
    var validHashDesc = ['可用率数据', 'SGAgent巡检' , '开关', 'http接口鉴权'];

    var map = {
        availabilityData: 'availabilityData',
        agentChecker: 'agentChecker',
        agentSwitch: 'agentSwitch',
        httpAuth: 'httpAuth'
    };

    function agentTabNavigation(key) {
        appkey = key;
        initPageHashAndTab();
        Y.msgp.utils.hashchange(hashChangeCallback);
    }

    function initPageHashAndTab() {
        //init hash
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#availabilityData';
        }

        //init tab
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
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
        if (map[hash]) {
            Y.msgp.manage[map[hash]](appkey, showOverlay, showContent);
        } else {
            throw new Error('Invalid hash value');
        }

        function showOverlay(wrapper) {
            wrapper.one('.content-body').hide();
            wrapper.one('.content-overlay').show();
        }

        function showContent(wrapper) {
            wrapper.one('.content-overlay').hide();
            wrapper.one('.content-body').show();
        }
    }

    function setDocumentTitle(hash) {
        document.title = '服务治理管理 - ' + validHashDesc[Y.Array.indexOf(validHash, hash)];
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'msgp-utils/hashchange',
        'msgp-manage/sgagent/availabilityData',
        'msgp-manage/sgagent/agentChecker',
        'msgp-manage/sgagent/agentSwitch',
        'msgp-manage/sgagent/httpAuth'
    ]
});
M.use('msgp-manage/sgagent/agentTabNavigation-version0.0.1', function (Y) {
    Y.msgp.manage.agentTabNavigation(key);
});
