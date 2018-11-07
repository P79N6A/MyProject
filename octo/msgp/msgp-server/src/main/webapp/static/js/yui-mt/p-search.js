/*jshint white:true,unused:true*/
/**
 * @module w-tree/p-search
 */
M.add('w-tree/p-search', function (Y) {

    Y.namespace('mt.plugin.tree');
    var trim = Y.Lang.trim;
    var $Macro = Y.mt.macro;
    var BaseTree = Y.mt.widget.BaseTree;
    /**
     * @class Search
     * @namespace mt.plugin.tree
     * @constructor
     * @extends Plugin.Base
     */
    var Search = Y.Base.create('tree-search', Y.Plugin.Base, [], {
        initializer: function () {
            //加入searchbox
            var host = this.get('host');
            var eventTarget = host.get('eventTarget');

            if (!eventTarget || eventTarget.get('tagName') !== 'INPUT') {
                this.afterHostMethod("renderBaseTree", this._initSearchBox);
            } else {
                this.afterHostMethod('bindUI', this.bindSearchInput);
            }
            this.doAfter(Y.mt.widget.Tree.LIFECYCLE.AFTER_CREATE_TREE_NODE, this._afterCreateTreeNode);
        },
        /**
         * 构建树之后的拼音转换处理
         * @method _afterCreateTreeNode
         * @private
         */
        _afterCreateTreeNode: function (data) {
            //加入搜索时需要用到的拼音转化
            var nodeData = data.nodeData;
            var pinyin = Y.mt.pinyin.toPinyin(nodeData.name).split(',');
            nodeData['PY_ALL'] = pinyin[0] || "";
            nodeData['PY_SHORT'] = pinyin[1] || "";
        },
        /**
         * 键盘输入进行搜索的接口
         * @method bindSearchInput
         */
        bindSearchInput: function () {
            var host = this.get('host'),
                ndTarget = host.get('eventTarget'),
                ndBoundingBox = host.get('boundingBox'),
                _this = this;

            //加入placeholder支持
            this.plugPlaceHolder(ndTarget, host.get('placeholder') || this.get('tips') || ndTarget.get('placeholder'));
            //响应输入框进行搜索
            ndTarget.on("keyup", function (e) {
                e.halt();
                if (ndBoundingBox.getStyle('display') === 'none') {
                    host.show();
                }
                if (e.keyCode === $Macro.key.ESC && !host._isEmbedInPage()) {
                    ndBoundingBox.hide();
                    return;
                }

                if (!(e.keyCode === $Macro.key.UP || e.keyCode === $Macro.key.DOWN)) {
                    if (_this.timer) window.clearTimeout(_this.timer);
                    _this.timer = window.setTimeout(function () {
                        _this.searchByPinyin(ndTarget.get("value"));
                    }, 100);
                }
            });
        },
        /**
         * 创建searchbox, 同时绑定searchBox的事件
         * @method _initSearchBox
         * @private
         */
        _initSearchBox: function () {
            var host = this.get("host"),
                contentBox = host.get("contentBox");

            var tpls = Search.TEMPLATE,
                ndSearchBox = Y.Node.create(Y.Lang.sub(tpls.SEARCH_BOX, Search.CLASS_NAME)),
                ndSearchInput = ndSearchBox.one("input[type=text]");

            contentBox.prepend(ndSearchBox);
            this.setSearchInputWidth(ndSearchInput);
            if (host.get("searchAsync")) {
                //支持异步搜索则指定eventTarget
                host.set("eventTarget", ndSearchInput);
            } else {
                this.bindEvent(ndSearchInput);
            }
        },
        /**
         * 设置searchInput的宽度
         * @method
         * @param {String} ndSearchInput 搜索框
         */
        setSearchInputWidth: function (ndSearchInput) {
            var host = this.get("host"),
                paddingLeft, paddingRight, width = host._getUseWidth();

            paddingLeft = parseInt(ndSearchInput.getStyle('paddingLeft'), 10);
            paddingRight = parseInt(ndSearchInput.getStyle('paddingRight'), 10);
            width = (width.indexOf('%') === -1) ?  (parseInt(width, 10) - paddingRight - paddingLeft) + 'px': width;
            ndSearchInput.setStyle('width', width);
        },
        /**
         * 绑定搜索框的搜索事件
         * @method
         */
        bindEvent: function (ndSearchInput) {
            var host = this.get("host"),
                searchTip = this.get("tips"),
                _this = this;
            //加入placeholder支持
            this.plugPlaceHolder(ndSearchInput, host.get('placeholder') || searchTip || ndSearchInput.get('placeholder'));
            ndSearchInput.on("keyup", function (e) {
                e.halt();
                var searchValue = this.get('value');
                if (_this.timer) window.clearTimeout(_this.timer);
                _this.timer = window.setTimeout(function () {
                    _this.searchByPinyin(searchValue);
                }, 100);
            });
            host.initKeyControl(ndSearchInput);
        },
        /**
         * 根据拼音搜索
         * @method searchByPinyin
         * @param {String} inputPinyin 搜索的关键字
         */
        searchByPinyin: function (inputPinyin) {
            //删除无数据的提示信息
            var host = this.get('host'),
                rootNode = host.getRootNode(),
                ndTreeContainer = host.get("contentBox"),
                query = trim(inputPinyin.toLowerCase()),
                sMode = host.get("sMode"),
                noDataFlag = true;

            var ndDes = rootNode.one('.' + Search.CLASS_NAME['SEARCH_NODATA']);
            if (ndDes) ndDes.remove();

            //如果为空，展开所有结点并退出
            if (inputPinyin === '') {
                host.expandAll();
                host.hideByLevel();
                return;
            }
            //全部收起
            host._toggle(0, false);
            ndTreeContainer.all('.' + BaseTree.CLASS_NAME['NODE_ITEM']).each(function () {
                var itemParams = this.getData('data');
                if (itemParams) {
                    var longPin = itemParams['PY_ALL'] || '';
                    var shortPin = itemParams['PY_SHORT'] || '';
                    var name = itemParams['name'].toLowerCase();
                    var reg = Y.mt.widget.util.getRegByMode(query, sMode);
                    if (reg.test(name) || reg.test(longPin) || reg.test(shortPin)) {
                        noDataFlag = false;
                        this.show();
                        this.ancestors().show();
                    }
                }
            });
            if (noDataFlag) {
                rootNode.appendChild(Y.Lang.sub(Search.TEMPLATE['NO_DATA'], Search.CLASS_NAME));
            }
        },
        /**
         * 给tree搜索框加入placeholder
         * @method plugPlaceHolder
         * @param {String} node 添加插件的节点
         * @param {String} tips 说明字符串
         */
        plugPlaceHolder: function (node, tips) {
            node.setAttribute('placeholder', tips);
            node.plug(Y.mt.plugin.Placeholder);
        }
    }, {
        NS: 'plugin-tree-search',
        CLASS_NAME: {
            SEARCH_BOX: 'widget-tree-search-box',
            SEARCH_NODATA: 'widget-tree-search-nodata'
        },
        TEMPLATE: {
            SEARCH_BOX: '<div class="{SEARCH_BOX}"><input type="text" value=""/></div>',
            NO_DATA: '<span class="{SEARCH_NODATA}">无匹配结果</span>'
        },
        ATTRS: {
            /**
             * placeholder的提示信息
             * @attribute tips
             * @type String
             */
            tips: {
                value: ''
            },
            /**
             * 无数据时的提醒
             * @attribute noData
             * @type String
             * @default '无数据'
             */
            noData: {
                value: '无数据'
            }
        }
    });

    Y.mt.plugin.tree.Search = Search;


}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'w-core',
        'mt-pinyin',
        'p-node-placeholder',
        'w-tree',
        'node'
    ]
});
 
