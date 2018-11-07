/*jshint white:true,unused:true*/
/**
 * p-checkbox
 * @module w-tree/p-checkbox
 */
M.add('w-tree/p-checkbox', function (Y) {

    Y.namespace('mt.plugin.tree');

    var $N = Y.Node,

        $Lang = Y.Lang,
        isArray = $Lang.isArray,
        BaseTree = Y.mt.widget.BaseTree;
    /**
     * plugin tree for checkbox
     * @class ShowCheckbox
     * @namespace mt.plugin.tree
     * @constructor
     * @extends Plugin.Base
     */
    var ShowCheckbox = Y.Base.create('tree-showcheckbox', Y.Plugin.Base, [], {
        initializer: function () {
            var lifeCycle = Y.mt.widget.Tree.LIFECYCLE;
            this.doAfter(lifeCycle.AFTER_CREATE_TREE_NODE, this._addCheckbox);
            this.doAfter(lifeCycle.AFTER_APPEND_MAIN_PANEL, this._afterBuildTree);
            this.doAfter(lifeCycle.TREE_SHOW, this.resetValue);
            this.doAfter(lifeCycle.TREE_HIDE, this._afterTreeHide);
        },
        /**
         * 给节点添加checkbox
         * @method _addCheckbox
         * @param {Object} params 需要初始化用到的参数
         */
        _addCheckbox: function (params) {
            var checkbox = $N.create(ShowCheckbox.TEMPLATE['CHECKBOX']);
            var cssName = BaseTree.CLASS_NAME;
            var node = params.node;
            var nodeData = params.nodeData;

            if (nodeData && nodeData[this.get('checkedKey')]) {
                checkbox.set('checked', true);
            }
            node.one('.' + cssName['ITEM_LINE'] + ' .' + cssName['NODE_ICON']).insert(checkbox, 'after');

            this.selectedCheckboxEvent(node);
        },
        /**
         * 初始化确认选择按钮
         * @method _initOkBtn
         * @private
         */
        _initOkBtn: function () {
            var _this = this,
                afterBoxChecked = this.get("afterBoxChecked");

            var treeFoot = this.get('host').get("contentBox").one('.' + ShowCheckbox.CLASS_NAME['NODE_FOOTER']);
            if (!treeFoot) {
                return;
            }
            treeFoot.setStyle("display", "block");
            var okBtn = $N.create(Y.Lang.sub(ShowCheckbox.TEMPLATE['OK_BTN'], ShowCheckbox.CLASS_NAME));
            treeFoot.prepend(okBtn);
            okBtn.on('click', function () {
                var checkNodes;
                if (_this.get("uncheckSyncAncestor")) {
                    checkNodes = _this.getCheckedNodeSimple();
                } else {
                    checkNodes = _this.getCheckedNodes();
                }
                if (afterBoxChecked) {
                    afterBoxChecked(checkNodes);
                }
                _this.fire("Tree.boxchecked", checkNodes);
            });
        },
        /**
         * 修改确定按钮样式
         * @method modifyOkBtnStyle
         * @param {Object} styles okbtn的样式
         */
        modifyOkBtnStyle: function (styles) {
            var treeFoot = this.getFooter();
            if (!treeFoot) {
                return;
            }
            treeFoot.one('.' + ShowCheckbox.CLASS_NAME['OK_BTN']).setStyles(styles);
        },
        /**
         * 当页面没有okBtn的时候需要在tree hide的时候调用选择回调函数
         * @method _afterTreeHide
         * @private
         */
        _afterTreeHide: function () {
            var after = this.get("afterBoxChecked");
            if (after) {
                var checkNodes;
                if (this.get("uncheckSyncAncestor")) {
                    checkNodes = this.getCheckedNodeSimple();
                } else {
                    checkNodes = this.getCheckedNodes();
                }
                after(checkNodes);
            }
        },
        /**
         * 预选checkbox
         * @method _preCheckItems
         * @private
         */
        _preCheckItems: function () {
            var checkedItems = this.get("checkedItems");
            if (isArray(checkedItems) && checkedItems.length > 0) {
                this.setNodesChecked(checkedItems);
            }
        },
        /**
         * 树构建好针对checnbox做的处理
         * @method _afterBuildTree
         * @private
         */
        _afterBuildTree: function () {
            this._preCheckItems();
            if (this.get("hasCheckboxBtn")) {
                this._setTreeFooter();
            }
            this._initOkBtn();
        },
        /**
         * checkbox被选中时, 使得下级的所有checkbox都被选中
         * @method selectedCheckboxEvent
         * @param {Node} [catItem] 当前选中的checkbox
         */
        selectedCheckboxEvent: function (catItem) {
            var checked = null,
                checkboxes = null,
                currentInput = null;
            var itemCss = BaseTree.CLASS_NAME['NODE_ITEM'];
            var textCss = BaseTree.CLASS_NAME['LINK_TEXT'];
            var _this = this;

            catItem.one("p").delegate('click', function () {
                checkboxes = this.ancestor("." + itemCss).all('input[type=checkbox]');
                currentInput = this.ancestor("p").one("input[type=checkbox]");
                checked = currentInput.get('checked');
                //如果是点击text文本，相关于需要toggle 当前input
                if (this.hasClass(textCss)) {
                    checked = !checked;
                }
                if (_this.get("cascade")) {
                    checkboxes.set("checked", checked);
                }
                // 取消勾选父级节点
                if (!checked) {
                    if (_this.get("uncheckSyncAncestor")) {
                        _this.uncheckAncestorCheckbox(this);
                    }
                }
            }, 'input[type=checkbox], .' + textCss);
            var ndCheckbox = catItem.one("p").one("." + ShowCheckbox.CLASS_NAME.CHECKBOX);
            if (!ndCheckbox) {
                return;
            }
            if (this.get("uncheckSyncAncestor")) {
                // 取消勾选父级节点
                ndCheckbox.on("unchecked", function () {
                    ndCheckbox.set("checked", false);
                    _this.uncheckAncestorCheckbox(ndCheckbox);
                });
            }
        },
        /**
         * 取消勾选父级节点
         * @private
         * @method
         * @param ${}
         * @return
         */
        uncheckAncestorCheckbox: function (ndCheckbox) {
            var ndPackage = this._getAncestorByCheckbox(ndCheckbox);
            if (ndPackage) {
                var ndAnceCheckbox = ndPackage.one("." + ShowCheckbox.CLASS_NAME.CHECKBOX);
                if (ndAnceCheckbox) {
                    ndAnceCheckbox.fire("unchecked");
                }
            }
        },
        /**
         * 根据checkbox获取对应的级节点
         * @private
         * @method
         * @param ${}
         * @return
         */
        _getAncestorByCheckbox: function (ndCheckbox) {
            var ndNodeItem = ndCheckbox.ancestor("." + BaseTree.CLASS_NAME.NODE_ITEM);
            var ndNodePackage;
            if (ndNodeItem) {
                ndNodePackage = ndNodeItem.ancestor("." + BaseTree.CLASS_NAME.NODE_PACKAGE);
            }
            if (ndNodePackage) {
                return ndNodePackage.one("." + BaseTree.CLASS_NAME.ITEM_PACKAGE);
            }
            return null;
        },
        /**
         * 获得选中的checkbox行的对象
         * @method getCheckedNodes
         */
        getCheckedNodes: function (ndContainer) {
            ndContainer = ndContainer || this.get('host').get("contentBox");
            var nodes = ndContainer.all('input[type="checkbox"]'),
                itemCss = BaseTree.CLASS_NAME['NODE_ITEM'],
                checkedNodes = [];

            nodes.each(function (item) {
                if (item.get('checked')) {
                    checkedNodes.push(item.ancestor('.' + itemCss).getData('data'));
                }
            });
            return checkedNodes;
        },
        /**
         * 获得选中的checkbox行的对象
         * @method getCheckedNodeSimple
         */
        getCheckedNodeSimple: function () {
            var ndContainer = this.get('host').get("contentBox");
            var nlPackage = ndContainer.all("." + BaseTree.CLASS_NAME.ITEM_PACKAGE),
                itemCss = BaseTree.CLASS_NAME['NODE_ITEM'],
                checkedNodes = [];

            var _this = this;
            nlPackage.each(function (ndPackage) {
                var ndCheckbox = _this.getCheckboxInPackage(ndPackage);
                if (ndCheckbox.get('checked')) {
                    //如果父节点已经是勾选的了，则跳过当前节点
                    var ndAncestorNodeItem = ndPackage.ancestor("." + itemCss).ancestor("." + BaseTree.CLASS_NAME.NODE_PACKAGE);
                    if (ndAncestorNodeItem) {
                        var ndAncestorPackage = ndAncestorNodeItem.one("." + BaseTree.CLASS_NAME.ITEM_PACKAGE);
                        if (ndAncestorPackage) {
                            var ndAncestorCheckbox = _this.getCheckboxInPackage(ndAncestorPackage);
                            if (ndAncestorCheckbox.get("checked")) {
                                return;
                            }
                        }
                    }
                    checkedNodes.push(ndCheckbox.ancestor('.' + itemCss).getData('data'));
                } else {
                    var nlLeafPackage = ndPackage.siblings("." + BaseTree.CLASS_NAME['NODE_LEAF']);
                    nlLeafPackage.each(function (ndLeafPackage) {
                        var ndCheckbox = _this.getCheckboxInPackage(ndLeafPackage);
                        if (ndCheckbox && ndCheckbox.get("checked")) {
                            checkedNodes.push(ndLeafPackage.getData('data'));
                        }
                    });
                }
            });
            return checkedNodes;
        },
        /**
         * 获取package内的checkbox
         * @private
         * @method
         * @param ${}
         * @return
         */
        getCheckboxInPackage: function (ndPackage) {
            return ndPackage.one("." + ShowCheckbox.CLASS_NAME.CHECKBOX);
        },
        /**
         * 设置searchInput的宽度
         * @method _setFooterWidth
         * @param {Node} treeFoot 树脚的容器
         */
        _setFooterWidth: function (treeFoot) {
            var host = this.get("host"),
                paddingLeft, paddingRight, width = host._getUseWidth();

            paddingLeft = parseInt(treeFoot.getStyle('paddingLeft'), 10);
            paddingRight = parseInt(treeFoot.getStyle('paddingRight'), 10);
            width = (width.indexOf('%') === -1) ?  (parseInt(width, 10) - paddingRight - paddingLeft) + 'px': width;
            treeFoot.setStyle('width', width);
        },
        /**
         * 设置选择确定按钮
         * @method _setTreeFooter
         * @private
         */
        _setTreeFooter: function () {
            var host = this.get('host'),
                treeFoot = Y.Node.create(Y.Lang.sub(ShowCheckbox.TEMPLATE['TREE_FOOT'], ShowCheckbox.CLASS_NAME));

            if (host._isEmbedInPage()) {
                treeFoot.removeClass(ShowCheckbox.CLASS_NAME['FOOTER_UNEMBED']);
            }
            host.get('contentBox').append(treeFoot);
            this._setFooterWidth(treeFoot);
        },
        /**
         * 获得脚部容器
         * @method getFooter
         * @private
         */
        getFooter: function () {
            return this.get('host').get('contentBox').one('.' + ShowCheckbox.CLASS_NAME['NODE_FOOTER']);
        },
        /**
         * 根据itemArray将相应的checkbox选中
         * @method setNodesChecked
         * @param {Object} itemArray 选中条目的id
         */
        setNodesChecked: function (itemArray) {
            var host = this.get('host');
            var cBox = host.get('contentBox');
            var textArr = [];
            if (!isArray(itemArray) || itemArray.length === 0) return;

            //查找和数组中对应的tree节点
            for (var i = 0, l = itemArray.length; i < l; i++) {
                var nodeId = itemArray[i];
                var nodeItem = cBox.one('#catof' + nodeId);
                if (nodeItem) {
                    var checkNode = nodeItem.one('input[type="checkbox"]');
                    textArr.push(host.getTextNode(nodeId).get('text'));
                    if (checkNode) {
                        checkNode.set('checked', true);
                    }
                }
            }
            this.setValue(itemArray.join(','), textArr.join(','));
        },
        /**
         * 设置多选的tree的value
         * @method setValue
         */
        setValue: function (id, name) {
            var host = this.get('host');
            host.setValue({id: id, name: name });
        },
        /**
         * 清空所有选项
         * @method resetValue
         */
        resetValue: function () {
            var cBox = this.get('host').get('contentBox');
            var nlCheckbox = cBox.all('input[type="checkbox"]');
            var host = this.get('host');
            if (nlCheckbox.size() > 0) {
                nlCheckbox.set('checked', false);
            }
            host.setValue({id: "", name: ""});
        }
    }, {
        NS: 'plugin-tree-showcheckbox',
        CLASS_NAME: {
            NODE_FOOTER: 'widget-tree-foot',
            FOOTER_UNEMBED: 'unembed-tree-foot',
            OK_BTN: 'widget-tree-ok-btn',
            CHECKBOX: 'widget-tree-checkbox'
        },
        TEMPLATE: {
            TREE_FOOT: '<div class="{NODE_FOOTER} {FOOTER_UNEMBED}"></div>',
            OK_BTN: '<input type="button" class="btn btn-primary btn-block {OK_BTN}" value="确定" />',
            CHECKBOX: '<input type="checkbox" name="checkbox" class="widget-tree-checkbox" />'
        },
        ATTRS: {
            /**
             * 选中点击确定后的回调
             * @attribute afterBoxChecked
             * @type Function
             */
            afterBoxChecked: {
                value: null
            },
            /**
             * 取消勾选是否关联父节点
             * @private
             * @method
             * @param ${}
             * @return
             */
            uncheckSyncAncestor: {
                value: false
            },
            /**
             * 初始化checkbox
             * @attribute checkedItems
             * @type Array
             */
            checkedItems: {
                value: null
            },
            /**
             * 选中父节点是否级联选中子节点
             * @attribute checkedKey
             * @type String
             * @default 'checked'
             */
            cascade: {
                value: true
            },
            /**
             * 和数据字段对应的选中
             * @attribute checkedKey
             * @type String
             * @default 'checked'
             */
            checkedKey: {
                value: 'checked'
            },
            /**
             * 最下方是否设置与checkbox对应绑定的'确定'按钮
             * @attribute hasCheckboxBtn
             * @type 是否显示确定按钮
             * @default true
             */
            hasCheckboxBtn: {
                value: true
            }
        }
    });

    Y.mt.plugin.tree.ShowCheckbox = ShowCheckbox;


}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'w-tree',
        'node',
        'w-tree-base'
    ]
});
 
