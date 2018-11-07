YUI.add('config-user/middlewares/fetch', function(Y) {

    var rest = Y.namespace('mt.config.rest').create('/config/');

    function fetch(url) {
        return function(req, res, next) {
            if (res.url) {
                url = res.url;
            }
            rest.get(url, function(data) {
                res.data = {
                    items: data
                };
                next();
            }, function(msg) {
                Y.fire('loaded');
                Y.mt.config.popup.alert(msg);
            });
        };
    }

    Y.namespace('mt.config.user.middlewares').fetch = fetch;

}, '', {
    requires: [
        'config-rest',
        'config-popup'
    ]
});
