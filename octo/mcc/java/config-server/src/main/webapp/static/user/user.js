/*jshint noempty:false, unused:false */
YUI.add('config-user', function(Y, NAME) {

    var gConf = Y.config[NAME];
    var mtconfig = Y.namespace('mt.config');
    var apis = mtconfig.user.apis;
    var middlewares = mtconfig.user.middlewares;

    function User(config) {
        this.initializer(config);
        if (config.render === true) {
            this.render();
        }
    }

    User.prototype.initializer = function(config) {
        Y.mix(this, config, true,
            [ 'container', 'title' ]);
        this.container = Y.one(this.container);
        this.titleNode = this.container.one('.J-config-user-title');
        this.contentNode = this.container.one('.J-config-user-content');

        this.router = this.initRouter(config.router);
    };

    User.prototype.initRouter = function(config) {
        var self = this;
        this.router = new mtconfig.User.Router(config);
        this.router.route('*', function(req, res, next) {
            res.titleNode = self.titleNode;
            res.contentNode = self.contentNode;
            next();
        });
        this.router.route('/user/admin',
                mtconfig.user.users.middleware,
                middlewares.title('超级管理员列表'),
                middlewares.fetch('/user/admin/get'),
                middlewares.list({ apis: apis.admins }));
        this.router.route('/space/:spaceName/admins',
                mtconfig.user.users.middleware,
                middlewares.title('空间管理员列表'),
                middlewares.fetch(),
                middlewares.list({ apis: apis.spaceAdmins }));

        this.router.dispatch();
        return this.router;
    };

    User.prototype.render = function() {
        this.uiSetTitle(this.title);
        Y.on(NAME + '|loaded', function() {
            this.uiSetLoading(false);
        }, this);
    };

    User.prototype.uiSetTitle = function(title) {
        this.titleNode.set('text', title);
    };

    User.prototype.uiSetLoading = function(loading) {
        if (loading) {

        } else {
            this.container.one('.loading').remove();
        }
    };


    if (gConf.render) {
        var user = new User(gConf);
        Y.mix(user, mtconfig.user);
        mtconfig.user = user;
    }

    Y.mix(User, mtconfig.User);
    mtconfig.User = User;

}, '', {
    skinnable: true,
    requires: [
        'config-user/router',
        'config-user/users',
        'config-user/apis',
        'config-user/middlewares/title',
        'config-user/middlewares/fetch',
        'config-user/middlewares/list'
    ]
});
