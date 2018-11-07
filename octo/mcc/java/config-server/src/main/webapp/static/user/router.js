YUI.add('config-user/router', function(Y) {

    function PRouter(config) {
        var router = new Y.Router(config);
        router.setAttrs({
            root: '/config'
        });
        return router;
    }

    Y.namespace('mt.config.User').Router = PRouter;

}, '', {
    requires: [
        'router'
    ]
});
