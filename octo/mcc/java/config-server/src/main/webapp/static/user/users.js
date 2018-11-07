/*jshint camelcase:false, expr:true, eqeqeq:false, unused:false */
YUI.add('config-user/users', function(Y, NAME) {

    var mtconfig = Y.namespace('mt.config');
    var popup = Y.namespace('mt.config.popup');
    function noop() {}

    var users = {
        middleware: middleware,
        render: render,
        uiSetFilterContainerNode: uiSetFilterContainerNode,
        uiSetUserList: uiSetUserList,
        bindUIAdd: bindUIAdd,
        bindUIDelete: bindUIDelete,
        add: add,
        'delete': deleteUser,
        exists: exists,
        getUserList: getUserList
    };

    function render() {
        this.contentNode.empty();

        this.source = {};
        // alias
        this.data.users = this.data.items;
        this.data.user = null;

        this.data.css_prefix = 'config-user';
        this.data.canUpdate = Y.Object.owns(this, 'apis');
        this.contentNode.setHTML(this.template(this.data));
        Y.fire('loaded');

        this.filterContainerNode = this.contentNode.one('.' + this.data.css_prefix + '-filter-container');
        var filterNode = this.contentNode.one('.' + this.data.css_prefix + '-filter');
        this.filterNode = filterNode;

        this.getUserList();
        var self = this;
        Y.one('win').on(NAME + '|scroll', Y.throttle(function() {
            self.uiSetFilterContainerNode();
        }, 50));

        if (this.data.canUpdate) {
            this.bindUIAdd();
            this.bindUIDelete();
        }

        this.uiSetFilterContainerNode();
    }


    function bindUIAdd() {
        var addNode = this.contentNode.one('.' + this.data.css_prefix + '-add');
        addNode.on('click', function(e) {
            e.preventDefault();
            this.add();
        }, this);
    }

    function bindUIDelete() {
        this.contentNode.delegate('click', function(e) {
            e.preventDefault();
            this['delete'](e.target.ancestor('li').getData('id'));
        }, '.' + this.data.css_prefix + '-delete', this);
    }

    function exists(id) {
        if (arguments.length === 0) {
            id = this.data.user.id;
        }
        var ids = Y.Array.map(this.data.users, function(user) {
            return user.id;
        });
        return Y.Array.indexOf(ids, id) !== -1;
    }

    function add() {
        if (!this.data.user) {
            popup.alert('用户不存在');
            return;
        }
        if (this.exists()) {
            popup.alert('用户已存在');
            return;
        }
        var self = this;
        this.apis.req = this.req;
        this.apis.add(this.data.user.id, function(err) {
            if (err) {
                return popup.alert('添加失败');
            }
            self.data.users.push(self.data.user);
            self.uiSetUserList();
            // self.data.user = null;
            popup.alert('添加成功', 700);
        });
    }

    function deleteUser(id) {
        this.apis.req = this.req;
        var self = this;
        id = parseInt(id, 10);
        var user = Y.Array.find(this.data.users, function(user) { return user.id == id; });
        var name = user.name + ' - ' + user.login;
        popup.confirm('确定删除【' + name + '】？', function() {
            self.apis['delete'](id, function(err) {
                if (err) {
                    return popup.alert('删除失败');
                }
                var ids = Y.Array.map(self.data.users, function(user) { return user.id; });
                var idx = Y.Array.indexOf(ids, id);
                self.data.users.splice(idx, 1);
                self.uiSetUserList({ type: 'removeItem', id: id });
                popup.alert('删除成功', 2000);
            });
        });
    }

    function uiSetUserList(opts) {
        opts || (opts = {});
        if (opts.type === 'removeItem') {
            this.contentNode.all('[data-id="' + opts.id + '"]').remove();
        } else {
            var template = [
                '<li <% Y.Array.each(Y.Object.keys(data), function(key) { %>',
                '        data-<%= key %>="<%= data[key] %>"<% }) %>>',
                '    <%= data.name %> - <%= data.login %>',
                '    <i class="fa fa-trash-o ' + this.data.css_prefix + '-delete"></i>',
                '</li>'
            ].join('\n');
            // register
            Y.Template.register('userItem', Y.Template.Micro.compile(template));
            this.contentNode.one('.' + this.data.css_prefix + '-list').append(Y.Template.render('userItem', this.data.user));
        }
    }

    function uiSetFilterContainerNode() {
        var css_prefix = this.data.css_prefix;
        if (this.filterContainerNode.get('docScrollY') <= 100) {
            this.filterContainerNode.removeClass(css_prefix + '-fixed');
        } else {
            this.filterContainerNode.addClass(css_prefix + '-fixed');
        }
    }

    function getUserList() {
        var self = this;

        new Y.mt.widget.AutoCompleteList({
            highlightFirstMatch: true,
            node: self.filterNode,
            action: "/config/employee",
            ayncsearch: true,
            msg: "输入 中文|拼音|首拼 搜索",
            listParam: "name",
            showMax: 20,
            defaultItem: false,
            matchMode: "none",
            searchInterval: 500,
            callback: function(data) {
                self.data.user = data;
            },
        });
    }


    function middleware(req, res, next) {
        Y.mix(res, users, true,
            [
                'render',
                'uiSetFilterContainerNode',
                'uiSetUserList',
                'bindUIAdd',
                'bindUIDelete',
                'add',
                'delete',
                'exists',
                'getUserList'
            ]);
        var spaceName = req.params.spaceName;
        if (spaceName) {
            res.title = spaceName;
            res.url = '/space/' + spaceName + '/user/get';
        }
        next();
    }

    Y.namespace('mt.config.user').users = users;

}, '', {
    requires: [
        'yui-throttle',
        'array-extras',
        'event-valuechange',
        'config-popup',
        'w-autocomplete'
    ]
});
