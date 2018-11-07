M.add('msgp-hulk/hulkdetail', function(Y){
    Y.namespace('msgp.hulk').hulkdetail = init;
    var appkey;
    var tab;
    //save the tab last init time
    var last = {};
    //every tab wrapper dom node
    var validHash = [ 'outline', 'scalingGroup'];//,'vmQuota','containerQuota','review'];
    var validHashDesc = [ '概要', '配置'];//,'vm实例配置','容器实例配置','审核'];

    function init( key ){

        appkey = key;
        initPageHash();
        initTab();
        initHashChange();
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
            location.hash = '#outline';
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
        var str = '配置详情 - ' + validHashDesc[ Y.Array.indexOf( validHash, hash ) ];
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
        Y.msgp.hulk.detailTab( appkey, hash );
    }
    function initCommonMap(){
        Y.msgp.service.commonMap();
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-tab',
        'msgp-utils/hashchange',
        'msgp-hulk/detailTab',
        'msgp-service/commonMap'
    ]
});
