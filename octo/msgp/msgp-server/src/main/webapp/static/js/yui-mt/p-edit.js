/*jshint white:true,unused:true*/
/**
 * @module w-tree/p-edit
 */
M.add('w-tree/p-edit', function (Y) {


    Y.namespace('mt.plugin.tree');
    var BaseTree = Y.mt.widget.BaseTree;

    /**
     * 主要用来修改树的结构，默认提供增删改方法
     * @class Edit
     * @namespace mt.plugin.tree
     * @constructor
     * @extends Plugin.Base
     */
    var Edit = Y.Base.create('tree-edit', Y.Plugin.Base, [], {
        initializer: function (config) {
            this.config = config;
            //每次都用，故初始化一次
            this.actionLinks = this._generateHTML();
            this.doAfter('bindUI', this._hoverAndAction);
        },
        /**
         * 根据配置信息生成节点html,同时格式话配置信息
         * @method _generateHTML
         * @private
         */
        _generateHTML: function () {
            var config = this.config,
                editable = this.get('editable'),
                tpls = Edit.TEMPLATE,
                defaultAction  = editable['defaultAction'],
                p = null,
                actionLinks = '';
            //删除host指向, 避免误画
            if (config.host) {
                delete config.host;
            }
            //处理默认的增删改动作
            for (p in editable) {
                if (p !== 'defaultAction' && config[p]) {
                    Y.mix(config[p], editable[p], false, null, 0, true);
                }
            }
            //处理自定义的动作
            for (p in config) {
                if (config.hasOwnProperty(p)) {
                    Y.mix(config[p], defaultAction, false, null, 0, true);
                    config[p].actionName = p;
                    actionLinks += Y.Lang.sub(tpls.ACTION_BTN, config[p]);
                }
            }
            return actionLinks;
        },
        /**
         * 鼠标悬浮效果并绑定事件处理
         * @method _hoverAndAction
         * @private
         */
        _hoverAndAction: function () {
            var host = this.get('host'),
                cssName = BaseTree.CLASS_NAME,
                root = host.getRootNode(),
                hoverClass = 'line-hover',
                openHoverEffect = this.get('openHoverEffect'),
                _this = this;

            if (!root || !openHoverEffect) return;
            //创建鼠标悬浮效果
            root.delegate('mouseenter', function (e) {
                var currentTarget = e.currentTarget;
                currentTarget.addClass(hoverClass);
                currentTarget.append(_this.actionLinks);
                //设置第一个action-link 偏离40px的距离
                currentTarget.one('a.action-links').setStyle('marginLeft', '40px');
            }, '.' + cssName['NODE_ITEM'] + ' p');
            //鼠标离开效果
            root.delegate('mouseleave', function (e) {
                var currentTarget = e.currentTarget;
                currentTarget.all('.action-links').remove();
                currentTarget.removeClass(hoverClass);
            }, '.' + cssName['NODE_ITEM'] + ' p');
            //按钮点击处理逻辑
            root.delegate('click', function (e) {
                var ndCurrent = e.currentTarget;
                _this.config[ndCurrent.getAttribute('action')].action(ndCurrent.ancestor('.' + cssName['NODE_ITEM']), _this, host);
            }, 'a.action-links');
        },
        /**
         * 添加节点, 维护树的ui完整,并展现出当前节点, 处理事件
         * @method addNode
         * @param {String} parentId 父节点的Id
         * @param {Object} nodeData 新添加的节点信息
         */
        addNode: function (parentId, nodeData) {
            var host = this.get('host');
            var ndItem = host.getNodeById(parentId);
            if (!ndItem) {
                return;
            }
            var cssName = BaseTree.CLASS_NAME,
                leafIcon = ndItem.one('>p .' + cssName.ICON_LEAF),
                expandIcon = ndItem.one('>p .' + cssName.NODE_ICON),
                tempData = ndItem.getData('data'),
                expandClass = cssName.NODE_ICON + " " + cssName.NODE_GROUP + " ";
            //判断是否为叶子节点 若是则改为非叶子节点
            if (leafIcon) {
                leafIcon.replaceClass(cssName.ICON_LEAF, cssName.ICON_OPEN);
                expandIcon.set('className', '');
                if (ndItem.next('.' + cssName['NODE_ITEM'])) {
                    expandClass += cssName.ROOT_OPEN;
                } else {
                    expandClass += cssName.ROOT_OPEN;
                }
                expandClass += ' ' + 'fa';
                expandIcon.addClass(expandClass);
                //维护members to do
                if (!tempData.members) {
                    tempData.members = [nodeData];
                }
                ndItem.replaceClass(cssName.NODE_LEAF, cssName.NODE_PACKAGE);
                ndItem.one("." + cssName['ITEM_LINE']).replaceClass(cssName.ITEM_LEAF, cssName.ITEM_PACKAGE);
            } else {
                if (/fold/.test(expandIcon.get('className'))) {
                    expandIcon.simulate('click');
                }
                //非叶子节点添加子节点，则最后一个子节点添加样式line, 且由bottom 换为 mid样式
                var lastChild = ndItem.get('lastChild'),
                    lastExpandIcon = lastChild.one('span'),
                    lastIconClass = lastExpandIcon.get("className");
                lastExpandIcon.replaceClass(lastIconClass, lastIconClass.replace("bottom", "mid"));
                lastChild.get('childNodes').each(function (child) {
                    if (!child.hasClass(cssName.ITEM_PACKAGE)) {
                        child.addClass(cssName.LINE);
                    }
                });
            }
            //使当前节点可见
            host._toggle(host.getNodeById(parentId), true);
            //设置其父节点
            nodeData.parentId = parentId;
            host._buildSubPanel(ndItem, [nodeData]);
            //当前节点的下面的节点数加1
            if (host.get('showLeafNumber')) {
                var regex = /\d+\s*\)/,
                    text = ndItem.one('>p a'),
                    description = text.getHTML();
                if (!regex.test(description)) {
                    description = description + '<span>(1)</span>';
                } else {
                    description = description.replace(regex, function (count) {
                        return (parseInt(count, 10) + 1) + ')';
                    });
                }
                text.setHTML(description);
            }
        },
        /**
         * 据据传入的id ,找到节点，并删除该节点及下面的节点。但是要维护好因删除引起的树的样式改变。
         * @method deleteNode
         * @param {String} id 节点id
         */
        deleteNode: function (id) {
            //找到节点
            var host = this.get('host');
            var deletedNode = host.getNodeById(id);
            var cssName = BaseTree.CLASS_NAME;
            if (!deletedNode) {
                return;
            }
            //如果删除的是最后一个节点
            //维护样式
            if (/bottom/.test(deletedNode.one('> p .' + cssName.NODE_ICON).get('className')) || !deletedNode.next('div.' + cssName.NODE_ITEM)) {
                var previousNode = deletedNode.previous('.' + cssName['NODE_ITEM']);
                if (previousNode) {
                    var previousIcon = previousNode.one('>p .' + cssName.NODE_ICON);
                    previousIcon.set('className', previousIcon.get('className').replace('mid', 'bottom'));
                    previousNode.get('childNodes').removeClass(cssName.LINE);
                }
            }
            //如果删除的是最后一个叶子节点，维护样式
            var packageNode = deletedNode.ancestor('.' + cssName.NODE_PACKAGE);
            if (packageNode) {
                var leafNum = packageNode.all('> div.' + cssName['NODE_ITEM']).size();
                if (leafNum === 1) {
                    var nodeIcon = packageNode.one('.' + cssName['NODE_ICON']);
                    packageNode.replaceClass(cssName.NODE_PACKAGE, cssName.NODE_LEAF);
                    packageNode.one("." + cssName['ITEM_LINE']).replaceClass(cssName.ITEM_PACKAGE, cssName.ITEM_LEAF);
                    if (packageNode.next('div.' + cssName['NODE_ITEM'])) {
                        nodeIcon.replaceClass(cssName.ROOT_FOLD, cssName.ICON_MID);
                    } else {
                        nodeIcon.replaceClass(cssName.ROOT_FOLD, cssName.ICON_BOTTOM);
                    }
                    packageNode.one('.' + cssName['NODE_STATE']).replaceClass(cssName.ICON_OPEN, cssName.ICON_LEAF);
                }
            }
            //当前节点的父节点的叶子节点数减1
            if (host.get('showLeafNumber')) {
                var regex = /\d+\s*\)/;
                if (packageNode) {
                    var text = packageNode.one('>p a'),
                        description = text.getHTML();
                    if (/\(1\)/.test(description)) {
                        text.one('span').remove();
                    } else {
                        description = description.replace(regex, function (count) {
                            return (parseInt(count, 10) - 1) + ')';
                        });
                        text.setHTML(description);
                    }
                }
            }
            //删除节点
            deletedNode.remove();
        },
        /**
         * 根据传入的节点信息，修改节点数据,仅有 nodeData.name 会导致节点信息变化其余知识存储在节点上，和原有数据合并，同名则覆盖原有数据
         * @method updateNode
         * @param {Object} nodeData 节点信息必须带id,否则函数无效
         */
        updateNode: function (nodeData) {
            var host = this.get('host');
            var tempNode = host.getNodeById(nodeData.id);
            if (!tempNode) {
                return;
            }
            var tempData = tempNode.getData('data');
            //更新绑定的节点信息
            Y.mix(tempData, nodeData, true);
            tempNode.one('>p a').setHTML(tempData.name);
        },
        destructor: function () {
            delete this.config;
            delete this.actionLinks;
        }
    }, {
        NS: 'plugin-tree-edit',
        TEMPLATE: {
            ACTION_BTN: '<a href="javascript:void(0);" class="action-links" action="{actionName}">{desc}</a>'
        },
        ATTRS: {
            /**
             * 配置按钮的描述信息和处理逻辑
             * @attribute editable
             * @type Object
             * 格式如下：
             * {
             *   addNode: {desc:'', action:function (currentNode, treeInstance) {}},
             *   deleteNode: {desc:'', action:function (currentNode, treeInstance) {}},
             *   updateNode: {desc:'', action:function (currentNode, treeInstance) {}},
             *   userDefine: {desc:'', action: function () {}}
             * }
             *
             * 增加节点默认事件
             * currentNode 当前节点实例
             * treeInstance 树实例
             *
             */
            editable: {
                value: {
                    addNode: {
                        desc: '增加',
                        action: function (currentNode, treeInstance) {
                            //默认的处理动作
                            treeInstance.addNode(currentNode.getData('data').id, {id: Y.guid(), name: '新节点'});
                        }
                    },
                    deleteNode: {
                        desc: '删除',
                        action: function (currentNode, treeInstance) {
                            treeInstance.deleteNode(currentNode.getData('data').id);
                        }
                    },
                    updateNode: {
                        desc: '更新',
                        action: function (currentNode, treeInstance) {
                            //默认的处理动作
                            var data = currentNode.getData('data');
                            data.name = '请设置更新信息';
                            treeInstance.updateNode(data);
                        }
                    },
                    defaultAction: {
                        desc: '用户自定义',
                        action: function () {
                            //默认什么都不做
                        }
                    }
                }
            },
            /**
             * 增加hover的编辑按钮效果开关
             * @attribute openHoverEffect
             * @type Boolean
             * @default true
             */
            openHoverEffect: {
                value: true
            }
        }
    });

    Y.mt.plugin.tree.Edit = Edit;

}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'w-tree-base'
    ]
});

