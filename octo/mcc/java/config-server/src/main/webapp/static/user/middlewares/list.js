/*jshint unused:false, expr:true*/
YUI.add('config-user/middlewares/list', function(Y) {

    function list(config) {
        config || (config = {});
        return function(req, res, next) {
            res.template = Y.namespace('mt.config.user.template.list');
            res.data.canUpdate = config.canUpdate;
            if (Y.Object.owns(config, 'apis')) {
                res.apis = config.apis;
            }
            res.render();
        };
    }

    Y.namespace('mt.config.user.middlewares').list = list;

}, '', {
    requires: [
        'config-user/template/list-tpl'
    ]
});
