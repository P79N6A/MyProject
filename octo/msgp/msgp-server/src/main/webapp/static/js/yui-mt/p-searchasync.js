/*jshint white:true,unused:true*/
/**
 * @module w-tree/p-searchasync
 */
M.add('w-tree/p-searchasync', function (Y) {

    Y.namespace('mt.plugin.tree');
    var $Macro = Y.mt.macro;
    /**
     * @class Search
     * @namespace mt.plugin.tree
     * @constructor
     * @extends Plugin.Base
     */
    var SearchAsync = Y.Base.create('tree-searchasync', Y.Plugin.Base, [], {
        initializer: function () {
            //加入searchbox
            this.afterHostMethod('bindUI', this.bindSearchInput);
            this.afterHostMethod('_setTarget', this.bindSearchInput);
            this.doAfter(Y.mt.widget.Tree.LIFECYCLE.AFTER_CREATE_FISRTLEVEL_CHILDREN, this.afterCreateFirstLevelChildren);
        },
        /**
         * 每层加载的时候, 添加unload标识
         * @method afterCreateFirstLevelChildren
         */
        afterCreateFirstLevelChildren: function (params) {
            //移除未加载的标识
            params.ndParent.addClass(SearchAsync.CLASS_NAME['unload']);
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

            //响应输入框进行搜索
            if (ndTarget) {
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
                            _this.searchByUrl(ndTarget.get("value"));
                        }, 400);
                    }
                });
            }
        },
        /**
         * 根据拼音搜索
         * @method searchByUrl
         * @param {String} inputPinyin 搜索的关键字
         */
        searchByUrl: function (inputPinyin) {
            //删除无数据的提示信息
            var host = this.get('host'),
                rootNode = host.getRootNode();

            //如果为空，展开所有结点并退出
            if (inputPinyin === '') {
                host.expandAll();
                host.hideByLevel();
                return;
            }
            //全部收起
            host._toggle(0, false);
            var loadingContainer = rootNode;
            Y.mt.io.loading(loadingContainer, '', false, true);
            var url = this.get('url');
            var queryObject = this._getQueryObject(url, inputPinyin);
            Y.mt.io.get(url.split("?")[0], queryObject, function (dataObj) {
                rootNode.setHTML('');
                var data = host.formatData(dataObj);
                //构建sub tree
                host._buildSubPanel(rootNode, data.members);

                var onload = host.get('onload');
                if (onload) onload(host);
                host._afterAppendMainPanel();
                host.expandAll();
            });
        },
        //获取query object
        _getQueryObject: function (url, inputPinyin) {
            var query = url.split("?")[1] || '';
            var queryArray = query.split("&");
            var queryObject = {keyWord: inputPinyin};
            Y.Array.each(queryArray, function (param) {
                var key = param.split("=")[0];
                var value = param.split("=")[1];
                queryObject[key] = value;
            });
            return queryObject;
        }
    }, {
        NS: 'pugin-tree-search',
        CLASS_NAME: {
            unload: 'node-unload'
        },
        ATTRS: {
            /**
             * placeholder的提示信息
             * @attribute tips
             * @type String
             */
            url: {
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

    Y.mt.plugin.tree.SearchAsync = SearchAsync;
}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'w-core',
        'p-node-placeholder',
        'w-tree',
        'mt-io'
    ]
});

