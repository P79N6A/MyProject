M.add('msgp-oauth2/test', function (Y) {

    Y.namespace('msgp.oauth2').test = test;

    var register_button = Y.one('#register_button');
    var editRouteDialog;

    function test() {
        bindEvent();
    }

    function bindEvent() {
        register_button.on('click', doCallback);
    }

    function doCallback() {
        editRouteDialog = new Y.mt.widget.CommonDialog({
            id: 'edit_route_dialog',
            title: '服务授权',
            width: 760
        });

        editRouteDialog.setContent('<iframe style="width:760px; border: 0 none; height: 360px" src="/oauth2/authorize?response_type=token&client_id=com.sankuai.inf.kms&redirect_uri=http://kms.sankuai.com/"></iframe>');
        editRouteDialog.show();
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'node-event-simulate',
        'mt-io',
        'template'
    ]
});
M.use('msgp-oauth2/test', function (Y) {
    Y.msgp.oauth2.test();
});
