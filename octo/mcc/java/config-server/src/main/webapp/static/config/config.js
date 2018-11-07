YUI.add('config-config', function(Y) {

    Y.namespace('mt.config');
    var each = Y.Object.each;
    var one = Y.one;
    var config = Y.mt.config;
    var rest = config.rest;
    var tpl = config.tpl;
    var filter = Y.Array.filter;
    var sub = Y.Lang.sub;
    var popup = config.popup;
    var Tree = Y.mt.config.Tree;
    var cookie = Y.mt.cookie;
    var SELECTOR = {
        container: {
            header: '#J-config-container-header',
            tree: '#J-config-container-tree',
            panel: '#J-config-container-panel',
            headerH1: '#J-config-header-h1',
            headerUl: '#J-config-header-ul',
            headerDelete: '#J-config-header-delete',
            headerAdd: '#J-config-header-add',
            headerSpaceSettings: '#J-config-header-space-settings',
            headerSpaceAdmin: '#J-config-header-space-admin'
        }
    };
    var TEXT = {
        spacesComfirmDelete: '确定删除空间 {spaceName} 吗，此操作不可逆转',
        spacesTipDeleteSuccess: '删除空间 {spaceName} 成功',
        spacesTipAddSuccess: '添加空间 {spaceName} 成功',
        spacesPromptAdd: '请输入新空间名：',
    };

    function Config() {
        this.container = this.getNodesObj(SELECTOR.container);
        this.tpl = tpl;
        this.initHeader();
    }

    Config.prototype = {
        constructor: Config,
        // 初始化 header
        initHeader: function() {
            var self = this;
            var lastSpace = cookie.get('config_lastSpace');
            this.bindHeader();
            rest.get('/spaces', function(data) {
                self.spacesList = data;
                if (lastSpace) {
                    Y.Array.some(data, function(item) {
                        if (item.name === lastSpace) {
                            self.spaceName = lastSpace;
                            return true;
                        }
                    });
                }
                if (typeof self.spaceName === 'undefined') {
                    self.spaceName = data[0].name;
                }
                self.syncHeader();
            });
        },
        // 初始化 tree
        initTree: function() {
            var spaceName = this.spaceName;
            var nodeName = spaceName;
            var tree = new Tree({
                spaceName: spaceName,
                nodeName: nodeName,
                isLeaf: true
            }, this.container.tree);
            this.tree = tree;
        },
        destroyTree: function() {
            if (this.tree) {
                this.tree.destroy();
            }
        },
        // 绑定 header
        bindHeader: function() {
            var self = this;
            // 绑定删除按钮
            var headerDelete = this.container.headerDelete;
            headerDelete.on('click', function() {
                var spaceName = self.spaceName;
                popup.confirm(sub(TEXT.spacesComfirmDelete, {
                    spaceName: spaceName
                }), function() {
                    rest.delete('/spaces/' + spaceName, function() {
                        popup.alert(sub(TEXT.spacesTipDeleteSuccess, {
                            spaceName: spaceName
                        }));
                        var spacesList = self.spacesList;
                        spacesList = filter(spacesList, function(item) {
                            return item.name !== spaceName;
                        });
                        self.spacesList = spacesList;
                        self.spaceName = spacesList[0].name;
                        self.syncHeader();
                    });
                });
            });
            // 绑定添加按钮
            var headerAdd = this.container.headerAdd;
            headerAdd.on('click', function() {
                popup.prompt(TEXT.spacesPromptAdd, function(spaceName) {
                    rest.post('/spaces', {
                        spaceName: spaceName
                    }, function() {
                        popup.alert(sub(TEXT.spacesTipAddSuccess, {
                            spaceName: spaceName
                        }));
                        var spacesList = self.spacesList;
                        spacesList.push({
                            name: spaceName
                        });
                        self.spacesList = spacesList;
                        self.spaceName = spaceName;
                        self.syncHeader();
                    });
                });
            });
            // 绑定空间管理按钮
            var headerSpaceSettings = this.container.headerSpaceSettings;
            headerSpaceSettings.on('click', function() {
                var spaceName = self.spaceName;
                window.location.href = "/config/spaces/" + spaceName + "/settings";
            });
            // 绑定空间管理员配置按钮
            var headerSpaceAdmin = this.container.headerSpaceAdmin;
            headerSpaceAdmin.on('click', function() {
                var spaceName = self.spaceName;
                window.location.href = "/config/space/" + spaceName + "/admins";
            });

        },
        // 同步数据到 header
        syncHeader: function() {
            var container = this.container;
            container.headerH1.setHTML(this.spaceName);
            cookie.set('config_lastSpace', this.spaceName);
            var self = this;

            new Y.mt.widget.AutoCompleteList({
                node: Y.one("#space-text"),
                listParam: "name",
                objList: self.spacesList,
                showMax: self.spacesList.length,
                callback: function(data) {
                    self.spaceName = data.name;
                    self.syncHeader();
                }
            });
            Y.all(".widget-autocomplete-complete-list").setStyle("height", "400px");
            Y.all(".widget-autocomplete-complete-list").setStyle("overflow", "auto");
            this.destroyTree();
            this.initTree();
        },
        // 通过 selector 获取 node
        getNodesObj: function(selectorObj) {
            var containerObj = {};
            each(selectorObj, function(value, key) {
                containerObj[key] = one(value);
            });
            return containerObj;
        }
    };
    new Config();

}, '', { requires: [
    'config-tpl',
    'node',
    'config-rest',
    'collection',
    'config-popup',
    'config-tree',
    'mt-cookie',
    'w-autocomplete'
]});
