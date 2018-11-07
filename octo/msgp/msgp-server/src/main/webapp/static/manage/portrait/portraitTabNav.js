M.add('msgp-manage/portrait/portraitTabNav', function (Y) {
    Y.namespace('msgp.manage').portraitTabNav = portraitTabNav;
    // 后期会重写这一部分，来重新获取appkey，后期重写
    var tab;
    var validHash = ["serviceStatus", "serviceProperty", "serviceResource"];
    var validHashDesc = ["服务状态", "服务性能", "服务资源"];
    var appkeysToFind;
    var key;

    var map = {
        // serviceStatus: 'serviceStatus',
        serviceProperty: 'serviceProperty',
        serviceResource: 'serviceResource'
    };

    function portraitTabNav(appkeytmp) {

        initParam(appkeytmp, key1);

        // 选择具体的标签页
        initPageHashAndTab();
        Y.msgp.utils.hashchange(hashChangeCallback);

        oneAppkeyAnswer([key]);
    }

    function initParam(appkeytmp, key1){
        key = key1;
        appkeytmp = [key1];
    }

    function oneAppkeyAnswer(appkeytmp) {
        var hash = location.hash.slice(1);
        // 找到接下来需要调用的函数
        Y.msgp.manage[map[hash]](appkeytmp);
    }

    function getAppkeys(data) {    //得到appkey
        appkeysToFind = [];        //暂存get到的appkey
        // for(var i=0, l=data.length; i<l; i++){
        for(var i=0, l=data.length; i<l&&i<30; i++){
            var saveData = data[i];
            appkeysToFind.push(saveData.appkey);
        }
    }

    function initPageHashAndTab() {
        // 选择具体的标签页，初始化默认设置服务状态标签页
        var hash = location.hash.slice(1); //hash中保存了路径中的路由信息
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#serviceResource';
        }

        //init tab
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
        oneAppkeyAnswer(getAppkey());

    }

    function hashChangeCallback(hash) {
        // 如果页面有改变，做一个回调函数，用来确定将要转到的页面
        // $(appkeytmp).ready(switchToTab(hash));
        // initParam(appkeytmp);
        switchToTab(hash);
        //set the tab current when the change is caused by back or forward
        //but this will be excess twice when user click the tab trigger
        tab.select({
            trigger: Y.one('a[href="#' + hash + '"]').ancestor('li'),
            sheet: Y.one('#wrap_' + hash)
        });

        // 设置标签
        setDocumentTitle(hash);
        // 调用点击事件
        var appkeytmp = getAppkey();
        oneAppkeyAnswer(appkeytmp);

    }

    function switchToTab(hash) {
        // 选择具体的标签页
        if (Y.Array.indexOf(validHash, hash) === -1) {
            throw new Error('Invalid hash value');
        }
        if (map[hash]) {
            var appkeytmp = getAppkey();
            // 跳转的参数是否需要这些，后期进行分析
            Y.msgp.manage[map[hash]](appkeytmp, showOverlay, showContent);
        } else {
            throw new Error('Invalid hash value');
        }


        // 下面两个函数可能并不需要
        function showOverlay(wrapper) {
            wrapper.one('.content-body').hide();
            wrapper.one('.content-overlay').show();
            console.log("come to 1");
        }

        function showContent(wrapper) {
            wrapper.one('.content-overlay').hide();
            wrapper.one('.content-body').show();
            console.log("come to 2");

        }
    }

    function setDocumentTitle(hash) {
        document.title = '服务画像 - ' + validHashDesc[Y.Array.indexOf(validHash, hash)];
    }

    function getAppkey() {
        var appkey = key1;
        return appkey;
    }

    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
        tbody.setHTML(loadingHtml);
    }

    function setError() {
        // var errorHtml = '<tr><td colspan="' + colspan + '">获取失败</td></tr>';
        // tbody.setHTML(errorHtml);
        console.log("获取失败");
        // alert("获取失败");
    }

    function setEmpty() {
        // var emptyHtml = '<tr><td colspan="' + colspan + '">没有内容</td></tr>';
        // tbody.setHTML(emptyHtml);
        console.log("没有内容");
        // alert("没有内容");
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'mt-date',
        'w-date',
        'w-autocomplete',
        'msgp-utils/hashchange',
        // 'msgp-manage/portrait/serviceStatus',
        'msgp-manage/portrait/serviceProperty',
        'msgp-manage/portrait/serviceResource'
    ]
});
M.use('msgp-manage/portrait/portraitTabNav', function (Y) {
    Y.msgp.manage.portraitTabNav(appkeytmp);
});
