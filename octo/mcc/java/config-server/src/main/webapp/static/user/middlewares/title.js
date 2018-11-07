YUI.add('config-user/middlewares/title', function(Y) {

    function title(_title) {
        return function(req, res, next) {
            var t = _title;
            if (res.title) {
                t += ' / ' + res.title;
            }
            res.titleNode.set('text', t);
            next();
        };
    }

    Y.namespace('mt.config.user.middlewares').title = title;

}, '', {
    requires: [
    ]
});
