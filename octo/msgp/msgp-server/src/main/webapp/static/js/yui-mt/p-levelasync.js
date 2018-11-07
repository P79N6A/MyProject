/*jshint white:true,unused:true*/
/**
 * @module w-tree/plevelasync
 */
M.add('w-tree/p-levelasync', function (Y) {

    Y.namespace('mt.plugin.tree');

    var BaseTree = Y.mt.widget.BaseTree;

    /**
     * 分级异步树
     * @class LevelAsync
     * @namespace mt.plugin.tree
     * @constructor
     * @extends Plugin.Base
     */
    var LevelAsync = Y.Base.create('tree-levelasync', Y.Plugin.Base, [], {
        initializer: function () {
            var host = this.get("host");
            // 不显示子节点数目
            host.set("showLeafNumber", false);
            var lifeCycle = Y.mt.widget.Tree.LIFECYCLE;
            this.doAfter(lifeCycle.AFTER_BUILD_MAIN_PANEL, this.buildRoot);
            this.doAfter(lifeCycle.AFTER_APPEND_MAIN_PANEL, this._bindGetChildren);
            this.doBefore(lifeCycle.BEFORE_CREATE_TREE_NODE, this.beforeCreateTreeNode);
            this.doAfter(lifeCycle.AFTER_CREATE_TREE_NODE, this.afterCreateTreeNode);
            this.doAfter(lifeCycle.AFTER_CREATE_FISRTLEVEL_CHILDREN, this.afterCreateFirstLevelChildren);
        },
        /**
         * 每层加载的时候, 去掉unload标识
         * @method afterCreateFirstLevelChildren
         */
        afterCreateFirstLevelChildren: function (params) {
            //移除未加载的标识
            params.ndParent.removeClass(LevelAsync.CLASS_NAME['unload']);
        },
        /**
         * 创建root相关节点信息
         * @method buildRoot
         */
        buildRoot: function (params) {
            this._getChildrenAsync(params.root);
        },
        /**
         * 在创建节点之前先去取数据
         * @method beforeCreateTreeNode
         */
        beforeCreateTreeNode: function (params) {
            //是否是叶子节点, 如果是按照叶子节点展示
            var data = params.data;
            var isLeaf = data[this.get('leafFlag')];
            var expand = data.members ? (!! data.members.length) : false;
            //非叶子节点
            if (!isLeaf) {
                params.cssParam['subLength'] = 1;
                //由于节点下面的子节点还没有加载, 所以需要改变tree的折叠icon的状态
                params.cssParam['expand'] = expand;
                params.tplParam['expand'] = expand;
            }
        },
        /**
         * 创建节点完成后给节点增加unload的标识
         * @method afterCreateTreeNode
         */
        afterCreateTreeNode: function (params) {
            var ndItem = params.node,
                nextSibling = params.ndParent.next();

            ndItem.addClass(LevelAsync.CLASS_NAME['unload']);
            if (nextSibling) {
                ndItem.addClass(BaseTree.LINE);
            }
        },
        /**
         * 分级异步加载时，为节点绑定异步加载子节点事件
         * @method _bindGetChildren
         * @private
         */
        _bindGetChildren: function () {
            var _this = this,
                unloadCss = LevelAsync.CLASS_NAME['unload'],
                groupCss = BaseTree.CLASS_NAME['NODE_GROUP'],
                stateCss = BaseTree.CLASS_NAME['NODE_STATE'],
                nodeItemCss = BaseTree.CLASS_NAME['NODE_ITEM'],
                host = this.get('host'),
                bBox = host.get('contentBox');

            //点击icon加载树节点
            bBox.delegate('click', function (e) {
                var target = e.target,
                    ndItem = target.ancestor('.' + unloadCss),
                    ndWidgetTreeItem = ndItem.one('.' + nodeItemCss);

                if (!ndItem.one('.' + groupCss)) return;

                //非叶子节点, 点击非节点文本时
                if (!host.get('searchAsync')) {
                    if (_this._canLoad(ndItem, target)) {
                        _this._getChildrenAsync(ndItem);
                    }
                //已经有了下级节点，并且当前节点是收缩状态
                } else if (ndWidgetTreeItem && ndWidgetTreeItem.getStyle('display') === 'none') {
                    //删除下级节点，重新请求数据构建
                    ndItem.all('.' + nodeItemCss).remove();
                    _this._getChildrenAsync(ndItem);
                //无下级节点
                } else if (!ndWidgetTreeItem) {
                    _this._getChildrenAsync(ndItem);
                }
            }, '.' + unloadCss + ' .' + groupCss + ', .' + unloadCss + ' .' + stateCss);
        },
        /**
         * 判断是否为叶子节点, 不需要加载数据
         * @method _canLoad
         */
        _canLoad: function (node, target) {
            //如果有query说明是需要向
            var cssText = BaseTree.CLASS_NAME['LINK_TEXT'];
            return  (node.getData('data')[this.get('canLoadFlag')] || !node.getData('data').leaf) &&
                    !(node.one('.' + Y.mt.widget.BaseTree.CLASS_NAME['ICON_LEAF']) || target.hasClass(cssText));
        },
        /**
         * 强制刷新节点,获取其子节点
         * @method refreshNode
         * @param {String} id 待刷新的节点id
         */
        refreshNode: function (id) {
            var toRefreshNode = this.get('host').getNodeById(id);
            if (!toRefreshNode) {
                return;
            }
            toRefreshNode.all('div').remove();
            toRefreshNode.addClass(LevelAsync.CLASS_NAME['unload']);
            this._getChildrenAsync(toRefreshNode);
        },
        /**
         * 无数据的处理函数
         * @method _dealNoDataMsg
         * @private
         */
        _dealNoDataMsg: function (ndItem) {
            var loadingLabel = ndItem.one('.loading');
            if (loadingLabel) {
                loadingLabel.setStyle('background', 'url()');
                loadingLabel.setHTML("无数据").show();
            }
        },
        /**
         * 创建某个节点下面的树节点
         * @method _getChildrenAsync
         * @private
         */
        _getChildrenAsync: function (ndItem) {
            var _this = this,
                nodeData = ndItem.getData("data"),
                host = this.get("host");

            //兼容处理, 推动后端干掉
            var postData = Y.clone(nodeData, true);
            if (nodeData.dataId) {
                postData.id = nodeData.dataId;
                postData.orgId = nodeData.id;
            }
            Y.mix(postData, postData.query || this.get('query') || {}, true);
            var config = {
                method: 'GET',
                data:  postData,
                on: {
                    start: function () {
                        var loadingContainer = ndItem.one('p') || ndItem;
                        Y.mt.io.loading(loadingContainer, '', false, true);
                    },
                    complete: function () {
                        //Y.mt.io.clearLoading(ndItem);
                        if (ndItem.one(".loading")) {
                            ndItem.one('.loading').remove();
                        }
                    },
                    success: function (id, res) {
                        res = res.responseText;
                        var data = res.length !== 0 ? Y.JSON.parse(res) : [];
                        //可以对回调的数据进行处理
                        _this.fire(LevelAsync.EVENT_NAME['beforeLoad'], {data: data});

                        //构建sub tree
                        data = host.formatData(data);
                        if (!data.members || (data.members && data.members.length === 0)) {
                            _this._dealNoDataMsg(ndItem);
                            return;
                        }
                        host._buildSubPanel(ndItem, data.members);

                        var onload = host.get('onload');
                        if (onload) onload(host);
                        host._afterAppendMainPanel();
                    },
                    failure: function () {
                        ndItem.one('.loading').setHTML("Error...").show();
                    }
                }
            };

            Y.io(this.get("url"), config);
        }
    }, {
        CLASS_NAME: {
            unload: 'node-unload'
        },
        EVENT_NAME: {
            beforeLoad: 'beforeLoad',
            afterLoad: 'afterLoad'
        },
        NS: 'plugin-tree-levelasync',
        ATTRS: {
            /**
             * 异步请求的url
             * @attribute url
             * @type String
             */
            url: {
                value: null
            },
            /**
             * 发送到后台的数据
             * @attribute query
             * @type Object
             */
            query: {
                value: null
            },
            /**
             * 是否可以加载子级树
             * @attribute canLoadFlag
             * @type Boolean
             */
            canLoadFlag: {
                value: 'query'
            },
            /**
             * 如果是叶子页面的数据字段标示
             * @attribute leafFlag
             * @type String
             */
            leafFlag: {
                value: 'leaf'
            }
        }
    });
    Y.mt.plugin.tree.LevelAsync = LevelAsync;

}, '1.0.0', {
    requires: [
        'mt-base',
        'mt-io',
        'base-build',
        'plugin',
        'w-tree',
        'w-tree-base',
        'oop',
        'json',
        'io'
    ]
});
 
