YUI.add('config-tree', function(Y, NAME) {

    var config = Y.namespace('mt.config');
    var rest = config.rest;
    var each = Y.Object.each;
    var map = Y.Array.map;
    var reduce = Y.Array.reduce;
    var tpl = config.tpl;
    var create = Y.Node.create;
    var popup = config.popup;
    var sub = Y.Lang.sub;

    var TEXT = {
        nodeComfirmDelete: '确定删除节点 {nodeName} 吗，此操作不可逆转',
        nodeTipDeleteSuccess: '删除节点 {nodeName} 成功',
        spacesTipAddSuccess: '添加空间 {spaceName} 成功',
        spacesPromptAdd: '请输入新空间名：',
    };

    function Tree(rootNode, container) {
        var self = this;
        var tree = new Y.Tree({
            rootNode: rootNode,
            nodeExtensions: [ config.Tree.NodeValidatable ]
        });
        this.tree = tree;
        this.container = container;
        rootNode = tree.rootNode;
        self.renderRootNode(rootNode);
        self.bindNode();
    }

    Tree.prototype = {
        constructor: Tree,
        renderRootNode: function(rootNode) {
            var domNode = create(tpl.rootNode({
                nodeLastName: rootNode.nodeName,
                isLeaf: rootNode.isLeaf
            }));
            this.tieNode(rootNode, domNode);
            this.container.setHTML(domNode);
            this.toggleNodeHandler(rootNode);
            rootNode.isFold = true;
        },
        toggleNodeHandler: function(node) {
            var self = this;
            this.getNodeData(node, function() {
                if (node.children) {
                    node.isLeaf = node.children.length === 0;
                }
                if (!node.isLeaf) {
                    if (node.isFold) {
                        self.unfoldNode(node);
                    } else {
                        self.foldNode(node);
                    }
                }
                self.showPanel(node);
            });
        },
        editNodeHandler: function(node) {
            var self = this;
            this.getNodeData(node, function() {
                self.showPanel(node);
            });
        },
        /**
         * /config/space/:spaceName/node/clientsynclog
         */
        synclogHandler: function(node) {
            var self = this;
            var pending = 2;
            this.getNodeData(node, testEnd);
            rest.get('/config/space/' + node.spaceName + '/node/operationrecord?nodeName=' + node.nodeName, function(data) {
                node.synclog = data;
                testEnd();
            });
            function testEnd() {
                pending--;
                if (pending === 0) {
                    self.showPanel(node, 'synclog');
                    Y.config.win.location.href = Y.config.win.location.href.replace(/#[\s\S]+$/, '') + '#' + 'config-node-synclog-anchor';
                }
            }
        },
        addNodeHandler: function(node, nodeName) {
            var self = this;
            var spaceName = node.spaceName;
            rest.newPost('/node', spaceName, {
                nodeName: nodeName
            }, function() {
                self.getNodeData(node, function() {
                    self.unfoldNode(node);
                    self.showPanel(Y.Array.find(node.children, function(item) {
                        return item.nodeName === nodeName;
                    }));
                    popup.alert('添加节点成功', 1000);
                });
            });
        },
        deleteNodeHandler: function(node) {
            var nodeName = node.nodeName;
            var spaceName = node.spaceName;
            rest.newDelete('/node/' + nodeName, spaceName, function() {
                popup.alert(sub(TEXT.nodeTipDeleteSuccess, {
                    nodeName: nodeName
                }), 1000);
                node.tree.removeNode(node);
                node.domNode.remove();
            });
        },
        bindNode: function() {
            var self = this;
            // toggle click
            this.delegateContainer('.J-config-tree-toggle', this.toggleNodeHandler);
            // add click
            var nodeValidator = new config.Tree.NodeValidatable();
            this.delegateContainer('.J-config-tree-controller-add', function(node) {
                popup.prompt('添加的节点名称：', function(result) {
                    Y.log('添加的节点名称为: ' + result, 'info', NAME);
                    if (!nodeValidator.validateNodeName(result)) {
                        popup.error('添加的节点名称不符合规范，请修改');
                        return false;
                    }
                    var nodeName = node.nodeName + '.' + result;
                    self.addNodeHandler(node, nodeName);
                });
            });
            // delete click
            this.delegateContainer('.J-config-tree-controller-delete', function(node) {
                var nodeName = node.nodeName;
                if (node.isRoot()) {
                    popup.alert('根节点不可删除');
                } else {
                    popup.confirm(sub(TEXT.nodeComfirmDelete, {
                        nodeName: nodeName
                    }), function() {
                        self.deleteNodeHandler(node);
                    });
                }
            });
            // edit click
            this.delegateContainer('.J-config-tree-controller-update', this.editNodeHandler);
            this.delegateContainer('.J-config-tree-controller-synclog', this.synclogHandler);
            this.delegateContainer('.J-config-tree-node', this.editNodeHandler);
        },
        unfoldNode: function(node) {
            var self = this;
            var domNode = node.domNode;
            var domNodeUl = domNode.one('ul');
            var domNodeToggleFa = domNode.one('.J-config-tree-toggle-fa');
            var newDomNodeUl = reduce(node.children, create('<ul></ul>'), function(prev, item) {
                var node = create(tpl.node({
                    nodeLastName: self.getLastName(item.nodeName),
                    isLeaf: item.isLeaf
                }));
                self.tieNode(item, node.one('.J-config-tree-wrapper'));
                prev.append(node);
                item.isFold = true;
                return prev;
            });
            domNodeToggleFa.empty();
            domNodeToggleFa.addClass('fa');
            domNodeToggleFa.replaceClass('fa-caret-right', 'fa-caret-down');
            domNodeUl.replace(newDomNodeUl);
            node.isFold = false;
        },
        foldNode: function(node) {
            var domNode = node.domNode;
            var domNodeUl = domNode.one('ul');
            var domNodeToggleFa = domNode.one('.J-config-tree-toggle-fa');
            domNodeToggleFa.replaceClass('fa-caret-down', 'fa-caret-right');
            domNodeUl.replace(create('<ul></ul>'));
            node.isFold = true;
        },
        tieNode: function(treeNode, domNode) {
            this.setNodeData(treeNode, {
                domNode: domNode
            });
            domNode.setData('treeNode', treeNode);
        },
        /**
         * @param {Tree.Node} node
         * @param {String} plugin
         */
        showPanel: function(node, plugin) {
            Y.all('.config-tree-node-current').removeClass('config-tree-node-current');
            node.domNode.one('.J-config-tree-node').addClass('config-tree-node-current');
            var opts = { node: node };
            if (!this.panel) {
                this.panel = new config.Panel(opts);
                this.panel.render();
            } else {
                this.panel.node = node;
                this.panel.initData();
                this.panel.syncUI();
            }
            this.panel.resetVersion(node.version);
            if (!plugin) {
                Y.Array.forEach(Y.Object.keys(this.panel.plugins), function(key) {
                    this.panel[key].hide();
                }, this);
                return;
            }
            if (!this.panel[plugin]) {
                this.panel.plug(config.panel[plugin]);
                this.panel[plugin].render();
            } else {
                this.panel[plugin].syncUI();
            }
        },
        getNodeData: function(node, callback) {
            var self = this;
            var spaceName = node.spaceName;
            var nodeName = node.nodeName;
            rest.newGet('/node/' + nodeName, spaceName, function (data) {
                var Node = Y.Tree.Node;
                var children = map(data.childrenNodes, function(item) {
                    return new Node(node.tree, {
                        spaceName: spaceName,
                        nodeName: nodeName + '.' + item.name,
                        isLeaf: item.leaf
                    });
                });
                self.setNodeData(node, {
                    data: data.data,
                    children: children,
                    version: data.version
                });
                callback();
            });
        },
        setNodeData: function(node, data) {
            each(data, function(value, key) {
                node[key] = value;
            });
        },
        getLastName: function(name) {
            return name.slice(name.lastIndexOf('.') + 1);
        },
        delegateContainer: function(selector, handler) {
            var self = this;
            this.container.delegate('click', function(e) {
                var node = e.target.ancestor('.J-config-tree-wrapper').getData('treeNode');
                handler.call(self, node);
                e.stopImmediatePropagation();
            }, selector);
        },
        destroy: function() {
            this.panel.destroy();
            this.container.empty();
            this.container.detachAll('click');
        }
    };


    Y.mix(Tree, config.Tree);

    config.Tree = Tree;

}, '', { requires: [
    'tree',
    'config-rest',
    'collection',
    'config-tpl',
    'node',
    'config-panel',
    'config-popup',
    'config-tree/validatable'
]});
