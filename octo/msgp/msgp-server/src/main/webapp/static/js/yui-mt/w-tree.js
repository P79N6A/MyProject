/*jshint white:true,unused:true*/
/**
 * 树形结构
 * @module w-tree
 */
M.add('w-tree', function (Y) {

    Y.namespace('mt.widget');
    var $Lang = Y.Lang,
        isObject = $Lang.isObject,
        isString = $Lang.isString,
        isNumber = $Lang.isNumber;
    var $Plugin = Y.mt.plugin.tree;
    var BaseTree = Y.mt.widget.BaseTree;

    // 由于有循环依赖，定义转移到了 BaseTree，但保留 Y.mt.widget.Tree.LIFECYCLE api
    var LIFECYCLE = BaseTree.LIFECYCLE;

    /**
     * tree展示结构、悬挂、支持搜索、多选、编辑、分级加载等
     * @class Tree
     * @namespace mt.widget
     * @constructor
     */
    Y.mt.widget.Tree = Y.Base.create('tree', Y.mt.widget.BaseTree, [], {
        initializer: function () {
            var _this = this,
                async = this.get("async"),
                autoRender = this.get('autoRender');
            // 同步功能的实现
            if (async && async.url) {
                Y.mt.io.get(async.url, null, function (data) {
                    if (data.length === 0) {
                        data = {name: "无数据", id: "", isLeaf: true};
                    }
                    _this.set("data", _this.formatData(data));
                    if (autoRender) {
                        _this.render();
                    }
                }, null, true);
            } else {
                if (autoRender) {
                    _this.render();
                }
            }
            this.on(LIFECYCLE.BEFORE_TREE_SHOW, this._beforeTreeShowHandle, this);
        },
        /**
         * 根据参数加载具体的插件
         * @method addPlugin
         */
        addPlugin: function () {
            var canSearch = this.get("search"),
                searchAsync = this.get("searchAsync"),
                levelAsync = this.get("levelAsync"),
                showCheckbox = this.get("showCheckbox"),
                editable = this.get('editable');

            // 作为plugin的config参数必须是对象或数组
            if (canSearch) {
                if (!isObject(canSearch)) {
                    canSearch = {};
                }
                this.plug($Plugin.Search, canSearch);
            }

            if (showCheckbox) {
                if (!isObject(showCheckbox)) {
                    showCheckbox = {};
                }
                this.plug($Plugin.ShowCheckbox, showCheckbox);
            }

            if (levelAsync) {
                this.plug($Plugin.LevelAsync, levelAsync);
            }

            if (searchAsync) {
                this.plug($Plugin.SearchAsync, searchAsync);
            }

            if (editable) {
                if (!isObject(editable)) {
                    editable = {addNode: {}, editNode: {desc: '123'}, deleteNode: {}};
                }
                this.plug($Plugin.Edit, editable);
            }
        },
        _beforeTreeShowHandle: function () {
            var _this = this;
            var async = this.get("async");
            if (async.realTime) {
                Y.mt.io.get(async.url, null, function (data) {
                    if (data.length === 0) {
                        data = {name: "无数据", id: "", isLeaf: true};
                    }
                    _this.set("data", _this.formatData(data));
                    _this.get("contentBox").empty();
                    _this.renderTreeUI();
                }, null, true);
            }
        },
        /**
         * 构造树
         * @method renderUI
         */
        renderUI: function () {
            this.addPlugin();
            this.renderBaseTree();
        },
        /**
         ** 获取和tree关联的hidden input的值
         ** @method getValue
         **/
        getValue: function (target) {
            var matchInput = this.getMatchInput(target);
            if (!matchInput) return;
            if (matchInput[1]) {
                return matchInput[1].get('value');
            }
        },
        /**
         * 设置和tree关联的input的值
         * @method setValue
         */
        setValue: function (nodeData, target) {
            var matchInput = this.getMatchInput(target);
            if (!matchInput) return;
            matchInput[0].set('value', nodeData.name);
            //存储起来方便记忆功能的实现
            matchInput[0].setData('value', nodeData);
            if (matchInput[1]) {
                matchInput[1].set('value', nodeData.id);
            }
        },
        /**
         * 清空tree的value和hiddenvalue的值
         * @method clearValue
         */
        clearValue: function () {
            var matchInput = this.getMatchInput();
            if (!matchInput) return;
            matchInput[0].set('value', "");
            if (matchInput[1]) {
                matchInput[1].set('value', "");
            }
        },
        /**
         * 闭合所有的节点
         * @method foldAll
         */
        foldAll: function () {
            this._toggle(null, false);
        },
        /**
         *  展开所有的节点
         * @method expandAll
         */
        expandAll: function () {
            this._toggle(null, true);
        },
        /**
         * 根据某个节点的展开
         * @method expandByNode
         * @param {Node | String} o id或node节点
         */
        expandByNode: function (o) {
            if (isString(o) || isNumber(o)) {
                o = this.getNodeById(o);
            }
            if (!o) return;
            this._toggle(o, true);
        },
        /**
         * 获得当前的一对input的值, 有可能hiddenInput不存在
         * @method getMatchInput
         * @param {Node} target 和tree关联的input
         */
        getMatchInput: function (target) {
            var eventTarget = target || this.get('eventTarget');
            if (!eventTarget) return null;

            var hiddenInput = eventTarget.get('parentNode').one('input[type="hidden"]');
            return [eventTarget, hiddenInput];
        },
        /**
         * 改变具体节点的样式, 可配合单选事件使用
         * @method addNodeStyle
         * @param {String|Number} nodeId 节点id
         * @param {Object} cssStyles 需要给文本设置的样式
         */
        addNodeStyle: function (nodeId, cssStyles) {
            if (!isString(nodeId) && !Y.Lang.isNumber(nodeId)) return;
            if (!Y.Lang.isObject(cssStyles)) return;

            var node = this.getNodeById(nodeId);
            this.getNodeContent(node).setStyles(cssStyles);
        },
        getNodeContent: function (node) {
            return node.one('>p>a');
        },
        /**
         * 为指定节点添加class,同时保证其他树节点没有该class,可用于点击节点改变样式等
         * @method addNodeClass
         * @param {String|Number} nodeId 节点id
         * @param {String}  className 需要给文本设置的css
         */
        addNodeClass: function (nodeId, className) {
            if (!isString(className) || !nodeId) {
                return;
            }
            var node = this.getNodeById(nodeId);
            this.bBox.all('.' + className).removeClass(className);
            if (node) {
                node.one('>p>a').addClass(className);
            }
        },
        /**
         * 触发节点的单击事件
         * @method clickNode
         * @param {String|Node} 节点或节点ID
         */
        clickNode: function (nodeId) {
            if (!isString(nodeId)) return;
            var node = this.getNodeById(nodeId);
            if (node) {
                this._nodeClickHandler(node);
            }
        },
        /**
         * 获取Root节点下的第一个子节点
         * @method getFirstChild
         * @return Node
         */
        getFirstChild: function () {
            var rootCss = BaseTree.CLASS_NAME['NODE_ROOT'];
            var itemCss = BaseTree.CLASS_NAME['NODE_ITEM'];
            return this.bBox.one('.' + rootCss + ' > .' + itemCss);
        },
        /**
         * 通过id获得tree上的某个节点
         * @method getNodeById
         * @param {String|Number} nodeId 节点id
         */
        getNodeById: function (id) {
            if (!$Lang.isNumber(id) && !isString(id)) {
                return  null;
            }
            return this.bBox.one('#catof' + id);
        },
        /**
         * 获得根节点
         * @method getRootNode
         */
        getRootNode: function () {
            return this.get('contentBox').one('.' + BaseTree.CLASS_NAME['NODE_ROOT']);
        },
        /**
         * 显示当前节点, 以及所有的节点
         * @method showNode
         * @param {Node|String} o id或node节点
         */
        showNode: function (o) {
            if (isString(o) || isNumber(o)) {
                o = this.getNodeById(o);
            }
            if (!o) return;
            o.show();
            o.ancestors().each(function (item) {
                item.show();
            });
        },
        /**
         * 根据id或者当前node的总结点获得node的text文本
         * @method getTextNode
         * @param {Node|String} o id或node节点
         */
        getTextNode: function (o) {
            if (isString(o) || isNumber(o)) {
                o = this.getNodeById(o);
            }
            if (!o) return null;
            return o.one('.' + BaseTree.CLASS_NAME['LINK_TEXT']);
        },
        /**
         * 设置多个响应input目标
         * @method setMultiTarget
         * @param {Seletor} seletor 一组和tree关联的target
         * @param {Seletor} scale 作用的范围
         */
        setMultiTarget: function (seletor, scale) {
            var _this = this;
            scale = scale || Y.one('body');
            scale.all(seletor).each(function (item) {
                if (item) {
                    item.detach();
                }
                //注册事件
                _this._setTarget(item);
            });

            //点击其它位置自动隐藏
            if (!this._isEmbedInPage()) {
                this._autoHide(scale.all(seletor));
            }
        },
        /**
         * 是否显示的孩子节点数
         * @method isShowLeafNumber
         * @param {Boolean} show 是否显示
         */
        isShowLeafNumber: function (show) {
            var allText = this.get('boundingBox').all('a');
            var itemCss = Y.mt.widget.BaseTree.CLASS_NAME['NODE_ITEM'];
            allText.each(function (item) {
                if (show) {
                    var ancestor = item.ancestor('.' + itemCss);
                    var allSibling = ancestor.all('>.' + itemCss);
                    item.set('text', item.get('text') + '(' + allSibling.size() + ')');
                } else {
                    item.set('text', item.get('text').replace(/\(\w+\)/, ''));
                }
            });
        }
    }, {
        LIFECYCLE: LIFECYCLE,
        ATTRS: {
            /**
             * 一次性异步获得所有数据
             * value格式： { url: '', realTime: false }
             * @attribute async
             * @type Object
             */
            async: {
                value: {}
            },
            /**
             * 分级异步，亦即一次只加载第一层子节点
             * 第二层子节点在点击其父节点时异步加载
             *  value格式：
             *  { url: '', query: '' }
             * @attribute levelAsync
             * @type Object
             */
            levelAsync: {
                value: null
            },
            /**
             * 显示checkbox的开关
             * 参数格式：
             * { checkedItems: [...], afterBoxChecked: function () {...} }
             * checkedItems：如果显示checkbox，显示的时候会根据这个数组提前将数组内对应的checkbox打勾
             * afterBoxChecked：有两个调用时机：
             *  1）如果是固定在页面上的tree，则点击提供的按钮后执行
             *  2）非固定的tree（不提供按钮），在点击其它地方tree消失时执行
             * @attribute showCheckbox
             * @type Object
             */
            showCheckbox: {
                value: null
            },
            /**
             * 是否可以搜索
             * @attribute search
             * @type Boolean
             * @default true
             */
            search: {
                value: true
            },
            /**
             * 异步搜索
             *  value格式：
             *  { url: '' }
             */
            searchAsync: {
                value: null
            },
            /**
             * 可编辑树的配置信息
             * 参数格式:
             * {add: {desc:'',action:function (){}},delete: {desc:'',action:function (){}, ....}
             * @attribute editable
             * @type Boolean|Object
             * @default false
             */
            editable: {
                value: false
            },
            /**
             * 当树加载完并显示前调用的回调函数
             * @attribute onload
             * @type Function
             */
            onload: {
                value: null
            },
            /**
             * 是否自动render
             * @attribute autoRender
             * @type Boolean
             * @default true
             */
            autoRender: {
                value: true
            }
        }
    });


}, '1.0.0', {
    requires: [
        'mt-io',
        'w-tree-base',
        'w-tree/p-checkbox',
        'w-tree/p-search',
        'w-tree/p-searchasync',
        'w-tree/p-levelasync',
        'w-tree/p-edit',
        'node'
    ]
});
 
