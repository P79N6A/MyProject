YUI.add('config-panel', function(Y, NAME) {

    var config = Y.namespace('mt.config');
    var popup = config.popup;
    var tpl = config.tpl;

    var getCN = Y.namespace('mt.cos').makeGetCN('J-' + NAME);

    var deletedCls = getCN('item-deleted');
    var modifiedCls = getCN('item-modified');
    var addedCls = getCN('item-added');

    var trim = Y.Lang.trim;

    /**
     * @param {Tree.Node} node
     */
    function Panel(userConfig) {
        this.container = Y.one(userConfig.container || '#J-config-container-panel');
        this.contentNode = this.container.one(userConfig.contentNode || '.' + getCN('tbody'));
        this.addNode = this.container.one(userConfig.addNode || '.' + getCN('add'));
        this.nodeValidator = new config.Tree.NodeValidatable();
        this.nodeCurrentClassName = '.config-tree-node-current';

        Y.mix(this, userConfig, true, [ 'node' ]);
        this.plugins = {};

        this.initData();
        this.version = userConfig.node.version;

        Y.log(this);
    }

    Panel.prototype = {

        constractor: Panel,

        resetVersion: function (version) {
            this.version = version;
        },

        initData: function(data) {
            if (arguments.length === 0) {
                data = this.node.data || [];
            }
            data = Y.Array.map(data, function(item) {
                if (item.deleted) {
                    return null;
                }
                if (item.comment === null) {
                    item.comment = '';
                }
                return item;
            });
            this.data = Y.Array.filter(data, function(v) {
                return !!v;
            });
            this.originData = Y.clone(this.data);
        },
        plug: function(plugin, cfg) {
            if (!plugin.ns) {
                return this;
            }
            if (this.plugins[plugin.ns]) {
                return this;
            }
            this.plugins[plugin.ns] = true;
            this[plugin.ns] = plugin.create();
            this[plugin.ns].host = this;
            if (typeof plugin.initializer === 'function') {
                plugin.initializer(cfg);
            }
            return this;
        },
        unplug: function(ns) {
            if (arguments.length === 0) {
                Y.Object.each(this.plugins, function(flag, key) {
                    this.unplug(key);
                }, this);
                return this;
            }
            var plugin = this[ns];
            if (typeof plugin.destructor === 'function') {
                plugin.destructor();
            }
            delete this.plugins[ns];
            delete this[ns];
            return this;
        },
        render: function() {
            this.contentNode.setHTML(tpl.panelTrList(this.node));
            this.bindUI();
            this.syncUI();
        },

        bindUI: function() {
            this.addNode.on('click', this._afterAddNodeClick, this);
            this.contentNode.delegate('click', this._afterDeleteClick, '.' + getCN('delete'), this);
            this.contentNode.delegate('valuechange', this._afterValuechange, 'input, textarea', this);
            this.container.one('form').on('submit', this._afterSubmit, this);
        },

        syncUI: function() {
            this.clean();
            this.contentNode.setHTML(tpl.panelTrList(this.node));
        },

        clean: function() {
            // first delete
            this.contentNode.all('.' + deletedCls).each(function(itemNode) {
                this.uiSetDelete(itemNode, true);
            }, this);
            // then modify
            this.contentNode.all('.' + getCN('item')).each(function(itemNode, index) {
                var originItem = this.originData[index];
                this.uiSetModify(itemNode, originItem);
            }, this);
            this.contentNode.all('.' + addedCls).removeClass(addedCls);
        },

        getItemIndex: function(itemNode) {
            var itemNodeList = this.contentNode.all('.' + getCN('item'));
            var index = itemNodeList.indexOf(itemNode);
            return index;
        },

        _afterAddNodeClick: function(e) {
            e.halt();
            this.data.push({
                key: '',
                value: '',
                comment: ''
            });
            this.uiSetAdd();
        },

        _afterDeleteClick: function(e) {
            var itemNode = getItemNode(e.target);
            if (!itemNode) {
                return;
            }
            e.halt();

            var index = this.getItemIndex(itemNode);

            var originItem = this.originData[index];
            var item;
            if (originItem) {
                item = this.data[index];
                // toggle
                item.deleted = !item.deleted;

                this.uiSetDelete(itemNode);

                var deleted = itemNode.hasClass(deletedCls);
                if (!deleted) {
                    this.uiSetModify(itemNode, originItem);
                }
            } else {
                this.data.splice(index, 1);
                this.uiSetDelete(itemNode, true);
            }
        },

        _afterValuechange: function(e) {
            var itemNode = getItemNode(e.target);
            var index = this.getItemIndex(itemNode);

            var role = e.target.ancestor('.J-control-group').get('role');
            this.data[index][role] = trim(e.newVal);

            if (itemNode.hasClass(getCN('item-deleted'))) {
                return;
            }
            var originItem = this.originData[index];
            if (!originItem) {
                return;
            }
            this.uiSetModify(itemNode, originItem);
        },

        uiSetAdd: function() {
            var itemNode = Y.Node.create(tpl.panelTr());
            this.contentNode.append(itemNode);
            itemNode.addClass(addedCls);
        },

        /**
         * @param {Node} itemNode
         */
        uiSetDelete: function(itemNode, remove) {
            if (remove) {
                itemNode.remove();
                return;
            }
            var modified = itemNode.hasClass(modifyCls);
            itemNode.toggleClass(deletedCls);

            var modifyCls = getCN('item-modified');
            if (itemNode.hasClass(modifyCls)) {
                itemNode.removeClass(modifyCls);
            }

            if (!modified) {
                uiSetRestore(itemNode);
            }
        },

        uiSetModify: function(itemNode, originItem) {
            if (!originItem) {
                return;
            }
            var modified = itemNode.all('input, textarea').some(function(valueNode) {
                var role = valueNode.ancestor('.J-control-group').get('role');
                return valueNode.get('value') !== originItem[role];
            });
            if (modified) {
                itemNode.addClass(modifiedCls);
            } else {
                itemNode.removeClass(modifiedCls);
            }
        },

        uiSetInvalids: function(invalids) {
            Y.Array.each(invalids, function(nd) {
                var ndGroup = nd.ancestor('.J-control-group');
                if (ndGroup) {
                    ndGroup.addClass('error');
                }
            });
        },

        _afterSubmit: function(e) {
            e.preventDefault();

            var invalids = [];
            var ndKeys = e.target.all('.' + getCN('key') + ' input');
            var vals = [];
            ndKeys.each(function(nd) {
                var val = trim(nd.get('value'));
                if (!this.nodeValidator.validateNodeDataKey(val)) {
                    nd.setData('errormsg', '错误 key：' + val + '，key 只能是以下字符的组合：a-zA-Z0-9_-.');
                    invalids.push(nd);
                } else if (vals.indexOf(val) !== -1) {
                    nd.setData('errormsg', '有重复的 key：' + val);
                    invalids.push(nd);
                }
                vals.push(val);
            }, this);

            if (invalids.length > 0) {
                popup.alert(Y.Array.map(invalids, function(nd) {
                    return nd.getData('errormsg');
                }).join('<br>'));
                this.uiSetInvalids(invalids);
                return;
            }
            var nodeData = Y.Array.reduce(this.data, [], function(prev, item) {
                if (item.deleted) {
                    return prev;
                }
                var newItem = {key:item.key, value:item.value, comment:item.comment};
                prev.push(newItem);
                return prev;
            });
            nodeData = Y.JSON.stringify(nodeData);
            var nodeName = this.node.nodeName;
            var self = this;
            config.rest.put('/node/' + nodeName, this.node.spaceName, {
                nodeData: nodeData,
                version: self.version
            }, function(/*results*/) {
                popup.alert('修改节点数据成功！', 1000);
                self.initData(self.data);
                self.clean();
                self.getLatestVersion(false);
            });
        },
        getLatestVersion: function (reload) {
            var spaceName = this.node.spaceName;
            var nodeName = this.node.nodeName;
            var self = this;
            var successCallback = function (data) {
                self.resetVersion(data.version);
            };
            var failureCallback = function (msg) {
                Y.msgp.config.popup.alert(msg);
            };
            config.rest.newGet("/node/"+ nodeName, spaceName, successCallback, failureCallback);
        },

        destroy: function() {
            this.contentNode.empty();
            // detachAll
            this.addNode.detachAll('click');
            this.contentNode.detachAll('click');
            this.contentNode.detachAll('valuechange');
            this.container.one('form').detachAll('submit');
            this.unplug();
        }
    };

    config.Panel = Panel;

    function getItemNode(itemNode) {
        var itemCls = '.' + getCN('item');
        if (!itemNode.test(itemCls)) {
            itemNode = itemNode.ancestor(itemCls);
        }
        if (itemNode) {
            return itemNode;
        }
        return null;
    }


    function uiSetRestore(itemNode) {
        var deleted = itemNode.hasClass(deletedCls);
        var modified = itemNode.hasClass(modifiedCls);
        var text;
        var previous;
        var current;
        if (deleted || modified) {
            previous = 'fa-trash-o';
            current = 'fa-undo';
            text = '恢复';
        } else {
            current = 'fa-trash-o';
            previous = 'fa-undo';
            text = '删除';
        }
        var iconNode = itemNode.one('.' + getCN('delete') + ' > .fa');
        iconNode.removeClass(previous).addClass(current);
        var textNode = itemNode.one('.' + getCN('delete-text'));
        textNode.set('text', text);
    }

}, '', { requires: [
    'config-tpl',
    'config-tree/validatable',
    'config-rest',
    'config-panel/synclog',
    'config-popup',
    'json',
    'cos-make-getcn',
    'event-valuechange'
]});
