M.add('msgp-manage/octoSelfCheck', function (Y) {
    Y.namespace('msgp.manage').octoSelfCheck = octoSelfCheck;
    var appkey;
    var tab;
    //save the tab last init time
    var last = {};
    //every tab wrapper dom node
    var validHash = [ 'msgp', 'sgAgent', 'mcc', 'thrift',  'scanner', 'daily', 'banner'];
    var validHashDesc = [ 'msgp', 'sgAgent','mcc', 'thrift', 'scanner', 'daily', 'banner'];

    function octoSelfCheck( key ){
        appkey = key;
        initPageHash();
        //initSubTitle();
        initTab();
        initHashChange();
        //initCommonMap();
    }
    function getAppKey(){
        var result = '';
        var search = location.search.slice(1);
        var arr = search.split('&');
        for(var i=0,l=arr.length; i<l; i++){
            if( arr[i].indexOf('appkey=') === 0 ){
                result = arr[i].split('=')[1];
                break;
            }
        }
        return result;
    }
    function initPageHash(){
        var hash = location.hash.slice(1);
        if( hash === '' || Y.Array.indexOf( validHash, hash ) === -1 ){
            location.hash = '#msgp';
        }
    }
    function initSubTitle(){
        Y.one('#sub_title .app-key').setHTML( '- ' + appkey );
    }
    function initTab(){
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
    }
    /*
     function initLast(){
     var hash = location.hash.slice(1);
     last[ hash ] = +new Date();
     }
     */
    function initHashChange(){
        Y.msgp.utils.hashchange( hashChangeCallback );
    }
    function hashChangeCallback( hash ){
        switchToTab( hash );
        //set the tab current when the change is caused by back or forward
        //but this will be excess twice when user click the tab trigger
        tab.select({
            trigger : Y.one('a[href="#'+ hash +'"]').ancestor('li'),
            sheet : Y.one('#wrap_' + hash)
        });

        setDocumentTitle( hash );
    }
    function setDocumentTitle( hash ){
        var str = '系统自检 - ' + validHashDesc[ Y.Array.indexOf( validHash, hash ) ];
        document.title = str;
    }
    function switchToTab( hash ){
        if( Y.Array.indexOf( validHash, hash ) === -1 ){
            throw new Error('Invalid hash value');
        }
        var now = +new Date();
        // switch tab in 30 seconds does not send the request
        if( last[hash] && ( now - last[hash] < 30*1000 ) ){
            return;
        }
        last[hash] = +new Date();
        Y.msgp.manage.check_tab( appkey, hash );
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-tab',
        'msgp-utils/hashchange',
        'msgp-manage/check_tab',
        'msgp-service/commonMap'
    ]
});
