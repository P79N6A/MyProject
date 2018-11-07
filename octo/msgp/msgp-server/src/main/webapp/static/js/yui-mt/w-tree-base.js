/**
 * 基本tree，不依赖于HTML结构
 * @module w-tree
 */
M.add('w-tree-base', function(Y) {

    Y.namespace('mt.widget');
    var $Widget = Y.mt.widget,
        $WidgetUtil = $Widget.util;
    var $Lang = Y.Lang,
        isArray = $Lang.isArray,
        isString = $Lang.isString,
        isNumber = $Lang.isNumber;

    var $Macro = Y.mt.macro;
    var $N = Y.Node;

    /**
     * 生命周期名称宏定义, 方便widget里面引用
     * @attribute LIFECYCLE
     * @type Object
     */
    var LIFECYCLE = {
        // 将主面板附加至容器
        BEFORE_BUILD_MAIN_PANEL: "beforeBuildMainPanel",
        AFTER_BUILD_MAIN_PANEL: "afterBuildMainPanel",
        AFTER_APPEND_MAIN_PANEL: "afterAppendMainPanel",
        AFTER_CREATE_FISRTLEVEL_CHILDREN: 'afterCreateFirstLevelChildren',

        BEFORE_CREATE_TREE_NODE: "beforeCreateTreeNode",
        AFTER_CREATE_TREE_NODE: "afterCreateTreeNode",

        // 创建子面板
        // TODO 多级子面板能分级处理吗？可将处于第几级作为参数传入
        BEFORE_BUILD_SUB_PANEL: "beforeBuildSubPanel",
        AFTER_BUILD_SUB_PANEL: "afterBuildSubPanel",

        // 其它的一些事件
        // TREE_SHOW 只在悬浮tree被显示时触发，嵌入页面的tree不触发
        TREE_SHOW: 'treeShow',
        TREE_HIDE: 'treeHide',
        TREE_BLUR: "treeBlur",
        BEFORE_TREE_SHOW: 'beforeTreeShow'
    };

    /**
     * 构建树形结构最主要的逻辑
     * @class BaseTree
     * @namespace mt.widget
     * @constructor
     * @extends Widget
     */
    var BaseTree = Y.Base.create('base-tree', Y.Widget, [], {
        initializer: function() {
            this.bBox = this.get('boundingBox');

            this.on(LIFECYCLE['AFTER_CREATE_FISRTLEVEL_CHILDREN'], this._afterCreateFirstLevelChildren);
            this.on(LIFECYCLE['AFTER_APPEND_MAIN_PANEL'], this._afterAppendMainPanel);
        },
        destructor: function() {
            $WidgetUtil.detachHideNodeByClickDocument(this.bBox);
            this.get("contentBox").detachAll();
            this.bBox.remove();
        },
        /**
         * 初始化basetree
         * @method renderBaseTree
         */
        renderBaseTree: function() {
            var onload = this.get('onload');

            // 渲染树的UI结构
            this.renderTreeUI();
            //决定树是否展示
            if ( this._isEmbedInPage()) {
                this.show();
            } else {
                this.bBox.hide();
            }
            // 基本树加载完毕
            if (onload) {
                onload(this);
            }
        },
        /**
         * 渲染树的UI结构
         * @private
         * @method
         * @param param
         * @return return
         */
        renderTreeUI: function () {
            var id = this.get("id"),
                ndTreeContainer = this.get("contentBox");

            if (id) {
                ndTreeContainer.set("id", id);
            }
            this._setSkin(this.get('skin'));

            //初始化面板
            this._initPanel();

            //初始化折叠状态
            this.hideByLevel();
            this.get("ndDoc").prepend(this.bBox);
        },
        /**
         * 初始化basetree UI 结构
         * @method bindUI
         */
        bindUI: function() {
            var eventTarget = this.get("eventTarget");

            //为折叠icon绑定事件
            this._initIconEvent();
            //需要绑定的事件类型和事件目标，只针对悬浮tree
            if(eventTarget && !this._isEmbedInPage()) {
                //默认创建hidden input元素
                this._createHiddenInput();
                //默认ndTreeContainer为隐藏状态
                this.initEvent();
            }

            //点击其它位置自动隐藏
            if(!this._isEmbedInPage()) {
                this._autoHide();
            }
            //初始化节点上的点击事件
            this._initTextEvent();
        },
        /**
         * 将所有数据格式统一格式化为一种格式，不会对源数据进行修改
         * @method formatData
         * @param {Object} data 需要格式化的数据
             主要包括如下两种格式：
             格式一：{name: '', id: '', members: []}
             格式二： [ {name: 'text5', id: 0, parentId: 0}, {name: 'text1', id: 0, parentId: 0}]
         * @return 格式化好的对象如下格式
            {id: 0, parentId: 0, name: rootTitle, members: [
                {id:..., parentId:0, name:..., members: [
                    {id:..., parentId:..., name:..., members:[]},
                    {id:..., parentId:..., name:..., members:[]},
                ]},
                {id:..., parentId:0, name:..., members:[]}
            ]}
         */
        formatData: function(data) {
            var rootTitle = this.get("rootTitle"),
                allDataList = Y.clone(data),
                tempList = {};
            var defaultData = {name: rootTitle, id:'root', parentId: 'root', members:[]};
            //异步数据方式
            if (!data) {
                return Y.clone(defaultData);
            }
            //非数组, 已经是{name: '', id: '', members: []}的形式，不需要转换
            if (!isArray(data)) {
                return data;
            } else {
                //第一个的members字段存在
                if(data[0] && data[0].members) {
                    return { name: rootTitle, id: 'root', members: data };
                }
            }
            //构建带有层级的数据结构, 利用对象间的引用关系构建
            for(var i = 0; i< allDataList.length; i++) {
                var item = allDataList[i];
                item.members = item.members || [];
                tempList[item.id] = item;
            }
            for(i = 0; i< allDataList.length; i++) {
                var treeItem = allDataList[i];
                var pid = treeItem.parentId;
                //当当前节点pid不等于当前节点id, 同时父亲节点存在时
                if(pid !== treeItem.id && tempList[pid] !== undefined) {
                    tempList[pid].members.push(treeItem);
                }
                //当节点的父节点不存在时
                if(tempList[pid] === undefined || pid === treeItem.id) {
                    defaultData.members.push(tempList[treeItem.id]);
                }
            }
            //找不到根节点的, 作为第一层节点
            return  defaultData;
        },
        /**
         * 初始化面板，包括子面板的初始化
         * @method _initPanel
         * @private
         */
        _initPanel: function() {
            // *生命周期*
            this.fire(LIFECYCLE.BEFORE_BUILD_MAIN_PANEL, {});

            var ndTreeContainer = this.get("contentBox"),
                ndRoot = this._buildRootNode(),
                dataList = this.get('data').members;

            // *生命周期*
            this.fire(LIFECYCLE.AFTER_BUILD_MAIN_PANEL, {root: ndRoot});

            // 创建子节点
            if(dataList && dataList.length > 0) {
                // *生命周期* 创建子面板
                this.fire(LIFECYCLE.BEFORE_BUILD_SUB_PANEL, {});

                this._buildSubPanel(ndRoot, dataList);

                this.fire(LIFECYCLE.AFTER_BUILD_SUB_PANEL, {});
            }

            ndTreeContainer.appendChild(ndRoot);

            this.fire(LIFECYCLE.AFTER_APPEND_MAIN_PANEL);
        },
        /**
         * 每层tree创建完毕的处理函数
         * @method _afterCreateFirstLevelChildren
         * @private
         */
        _afterCreateFirstLevelChildren: function(data) {
            var ndParent = data.ndParent;
            var nlChildren = ndParent.get('children');
            if(!ndParent.hasClass(BaseTree.CLASS_NAME['NODE_ROOT'])) {
                nlChildren.each(function (item) {
                    if (!item.hasClass(BaseTree.CLASS_NAME['ITEM_LINE'])) {
                        item.addClass(BaseTree.CLASS_NAME['LINE']);
                    }
                });
            }
        },
        /**
         * 创建main panel后的处理, 同时提供给分级异步树独立调用
         * @method _afterAppendMainPanel
         * @private
         */
        _afterAppendMainPanel: function() {
            var ndRoot = this.getRootNode();

            if(!ndRoot.hasChildNodes()) {
                ndRoot.addClass(BaseTree.CLASS_NAME['ROOT_NODATA']);
                ndRoot.setHTML(this.get("noDataMsg"));
                return;
            }
            //如果有hasRootDetail
            if (!this.get("hasRootDetail")) {
                ndRoot.all("> ."+ BaseTree.CLASS_NAME['NODE_ITEM']).setStyle("paddingLeft", "0");
            }
        },
        /**
         * 获得使用的width, 应用到input和root元素上
         * @method _getUseWidth
         * @private
         */
        _getUseWidth: function(width) {
            var eventTarget = this.get('eventTarget');
            width = width || this.get('width');
            if(!width) {
                if(eventTarget) {
                    width = eventTarget.get('offsetWidth');
                } else {
                    return '95%';
                }
            }
            //减去border;
            return (width - 2) + 'px';
        },
        /**
         * 是否嵌入页面中
         * @method _isEmbedInPage
         * @private
         */
        _isEmbedInPage : function() {
            return this.get('position') === 'inner';
        },
        /**
         * 初始化root, 默认情况下id为0
         * @method _buildRootNode
         * @private
         */
        _buildRootNode: function() {
            var rootData = this.get('data'),
                dataList = rootData.members,
                maxHeight = this.get("maxHeight"),
                height = parseInt(this.get("height"), 10);

            var hasRootDetail = this.get("hasRootDetail"),
                ndRootDetail,
                ndRoot;

            ndRoot = this._createNodeByTpl('ROOT', { rootId: rootData.id, width: this._getUseWidth()});
            //有根结点的树
            if (hasRootDetail) {
                ndRootDetail = this._createRootDetailNode(rootData, dataList);
                ndRoot.appendChild(ndRootDetail);
            }
            if (this._isEmbedInPage()) {
                ndRoot.removeClass(BaseTree.CLASS_NAME['ROOT_BG']).removeClass(BaseTree.CLASS_NAME['ROOT_UNEMBED']);
            }
            if (height && height >= 0) {
                ndRoot.setStyle('height', height);
            }
            ndRoot.setStyle("maxHeight", maxHeight + 'px');
            ndRoot.setData("data", rootData);
            return ndRoot;
        },
        /**
         * 创建描述根节点信息的子节点并返回
         * @method _createRootDetailNode
         * @param {Node} rootElement 根节点
         * @param {Object}  dataList tree的数据列表
         * @param {Boolean} expand 是否展开根节点
         * @private
         */
        _createRootDetailNode: function(rootElement, dataList, expand) {
            expand = expand === false ? false : true;

            var params = {},
                nodeCss = BaseTree.CLASS_NAME,
                displayLength = isNumber(+rootElement['count']) ? rootElement['count'] : dataList.length;

            var nodeIconCss = expand ? nodeCss.ROOT_OPEN : nodeCss.ROOT_FOLD;
            params.nodeCss = nodeIconCss + ' ' + nodeCss['NODE_GROUP'];
            params.nodeState = expand ? nodeCss.ICON_OPEN : nodeCss.ICON_FOLD;
            if (this.get("showLeafNumber")) {
                params.text = rootElement.name + '<span>(' + displayLength + ')</span>';
            } else {
                params.text = rootElement.name;
            }

            return this._createNodeByTpl('ROOT_DETAIL', params);
        },
        /**
         * 创建所有的树节点
         * @method _buildSubPanel 
         * @param {Object} ndItem 当前节点的父级节点数据
         * @param {Object} dataList 当前树的说有节点数据
         * @private
         */
        _buildSubPanel: function(ndItem, dataList) {
            if(!dataList) {
                return;
            }
            var dataLength = dataList.length,
                TEMPLATE = BaseTree.TEMPLATE,
                elTarget = this.get("elTarget"),
                tempNode,
                tplParam,
                currentData = null,
                subDataList,
                cssParam,
                subLength;

            for (var i = 0; i < dataLength; i++ ) {
                currentData = dataList[i];
                subDataList = currentData['members'] ? currentData['members'] : [];
                subLength = subDataList.length;
                cssParam = {index: i, length: dataLength, subLength: subLength};
                tempNode = null;
                tplParam = {};

                // 此事件给予开发人员根据节点信息自定义节点的能力
                // 修改模板后，可通过对tplParam添加需要的参数来传递数据
                this.fire(LIFECYCLE.BEFORE_CREATE_TREE_NODE, {data:currentData, elTarget:elTarget, TEMPLATE:TEMPLATE, tplParam:tplParam, ndParent:ndItem, cssParam: cssParam});
                //创建树节点
                tempNode = this._createTreeNode(currentData, cssParam, tplParam, tplParam.expand);

                this.fire(LIFECYCLE.AFTER_CREATE_TREE_NODE, { nodeData: currentData, node: tempNode, elTarget: elTarget, ndParent:ndItem});

                tempNode.setData("data", currentData);

                ndItem.appendChild(tempNode);

                //一层节点创建完毕
                if(i === dataLength - 1) {
                    this.fire(LIFECYCLE.AFTER_CREATE_FISRTLEVEL_CHILDREN, {ndParent: ndItem});
                }
                //递归调用
                if (subDataList.length > 0) {
                    this._buildSubPanel(tempNode, subDataList);
                }
            }
        },
        /**
         * 创建tree的节点
         * @method _createTreeNode
         * @param {Object} nodeData 当前节点的数据
         * @param {Object}  cssParam 可用来初始化当前节点计算的参数
         * @param {Object} custParam 可用来改变计算的css值， 对其进行覆盖
         * @param {Boolean} expand 当前节点是否是展开状态
         * @private
         */
        _createTreeNode: function(nodeData, cssParam, custParam, expand) {
            expand = expand === false ? false : true;

            var nodeCss = BaseTree.CLASS_NAME,
                subLength = cssParam.subLength,
                showLeafNumber = this.get('showLeafNumber'),
                displayLength = isNumber(+nodeData['count']) ? nodeData['count'] : subLength,
                nodeClass;

            var params = {
                id: nodeData.id,
                className: nodeData['className'] || "",
                href: nodeData['href'] || "javascript:void(0)",
                target: nodeData['target'] || "",
                text: nodeData.name || ""
            };
            nodeClass = this._getNodeCss(cssParam);
            if (subLength !== 0) {
                params['nodeClass'] = nodeCss.NODE_GROUP + " " + nodeClass;
                params['nodeState'] = expand ? nodeCss.ICON_OPEN : nodeCss.ICON_FOLD;
                params['ITEM_TYPE'] = nodeCss.ITEM_PACKAGE;
                params['NODE_TYPE'] = nodeCss.NODE_PACKAGE;
            } else {
                params['nodeClass'] = nodeClass;
                params['nodeState'] = nodeCss.ICON_LEAF;
                params['ITEM_TYPE'] = nodeCss.ITEM_LEAF;
                params['NODE_TYPE'] = nodeCss.NODE_LEAF;
            }

            if ( showLeafNumber && ( subLength !== 0) ) {
                params['length'] = '<span>(' + displayLength +')</span>' ;
            } else {
                params['length'] = "";
            }
            // 自定义的custParam对象可以对默认配置进行添加覆盖修改
            Y.mix(params, custParam, true);
            return this._createNodeByTpl('TREE_NODE', params);
        },
         /**
          * 初始化隐藏第expandLevel层后的数据
          * @method hideByLevel
          * @params {Number} level 展开到的层级, 也可以说隐藏第几层以下的节点
          */
        hideByLevel: function(level) {
            level = (level !== undefined) ? level : this.get("expandLevel");
            if(!isNumber(level)) return;

            //重置折叠状态
            this.expandAll();
            if(level === 0) {
                this.foldAll();
            } else if(level > 0) {
                this._toggle(level, false);
            }
        },
        /**
         * @method 初始化键盘事件：在树上用up down enter键等进行操作
         * @param 事件的绑定对象
         */
        initKeyControl: function( ndTarget ) {
            var _this = this,
                ndContainer = this.get("contentBox"),
                ndBoundingBox = this.bBox,
                after = this.get("afterChoose");
            ndTarget = Y.one(ndTarget);
            var datas = ndContainer.all(".text");
            //若为异步，在这里很可能得到的是长度为0
            var dataSize = datas.size();
            var preIndex = 0;//保存上一个焦点的index
            var currentIndex = -1;

            //将currentIndex复位,并去掉上次选中的选项的样式
            ndTarget.on('focus', function() {
                if ( dataSize === 0 ) {
                    datas = ndContainer.all(".text");
                    dataSize = datas.size();
                }
                if ( currentIndex >= 0 ) {
                    datas.item(currentIndex).setStyles({"color":"","background":""});
                }
                currentIndex = -1;
            });
            //键盘响应事件
            ndTarget.on('keydown', function(e) {
                switch( e.keyCode ) {
                case $Macro.key.DOWN:
                    e.halt();
                    moveTarget("down");
                    break;
                case $Macro.key.UP:
                    e.halt();
                    moveTarget("up");
                    break;
                case $Macro.key.BACKSPACE:
                    //每次消除输入的时候currentIndex复位
                    if ( currentIndex >= 0 ) {
                        datas.item(currentIndex).setStyles({"color":"","background":""});
                    }
                    currentIndex = -1;
                    break;
                case $Macro.key.ENTER:
                    e.halt();
                    //和直接点击选项所发生的事件是一样的,但不支持checkbox的点击
                    if (!_this._isEmbedInPage() && after) {
                        ndBoundingBox.hide();
                    }
                    if( currentIndex >= 0 && after) {
                        var nodeData = datas.item(currentIndex).ancestor('.item').getData('data') || {};
                        after(nodeData, _this);
                    }
                    break;
                case $Macro.key.ESC:
                    if (!_this._isEmbedInPage()) {
                        e.halt();
                        ndBoundingBox.hide();
                    }
                    break;
                default:
                    break;
                }
            });
            //移动选项
            function moveTarget( direction ) {
                preIndex = currentIndex;
                if( currentIndex < -1 ) {
                    currentIndex = -1;
                }
                //原本是:direction === "down" ? currentIndex++ ; currentIndex--;但JSLint不让通过……
                currentIndex += ( direction === "down"? 1 : -1 );
                while ( currentIndex > -1 && currentIndex < dataSize &&
                        ( datas.item(currentIndex).get("nodeName") !== "A" || datas.item(currentIndex).ancestor("div").getStyle("display") === "none" ) ) {
                    currentIndex += ( direction === "down"? 1 : -1 );
                }
                if ( currentIndex >= dataSize ) {
                    currentIndex = preIndex;
                }
                if ( currentIndex > -1 && currentIndex < dataSize ) {
                    datas.item(currentIndex).setStyles({"color":"#fff","background":"#0f91cf"});
                    // 元素看不见时使树自动滚动
                    datas.item(currentIndex).scrollIntoView();
                    if ( currentIndex !== preIndex &&  preIndex >= 0 ) {
                        datas.item( preIndex ).setStyles({"color":"","background":""});//将上一个焦点的样式去除
                    }
                }
                if ( currentIndex === -1 && preIndex !== -1 ) {
                    datas.item( preIndex ).setStyles({"color":"","background":""});//将上一个焦点的样式去除
                }
            }
        },
        /**
         * 自动计算当前节点的样式
         * @method
         * @param {Object} cssParam 具体的参数, 包括如下几个属性
         * i: 当前节点的位置在本层级中的位置
         * dataLength: 本组节点的长度
         * subLength: 子节点的长度
         * state: 是否折叠
         * @private
         */
        _getNodeCss: function(cssParam) {
            var nodeCss = BaseTree.CLASS_NAME,
                dataLength = cssParam.length,
                expand = cssParam.expand,
                subLength = cssParam.subLength,
                index = cssParam.index;

            index++;
            expand = expand === false ? false : true;
            //最后一个节点
            if(index === dataLength) {
                if(subLength !== 0) {
                    return expand ? nodeCss.ROOT_OPEN : nodeCss.ROOT_FOLD;
                } else {
                    return nodeCss.ICON_BOTTOM;
                }
            } else {
                if(subLength !== 0) {
                    return expand ? nodeCss.ROOT_OPEN : nodeCss.ROOT_FOLD;
                } else {
                    return nodeCss.ICON_MID;
                }
            }
        },
        /**
         * 设置tree的显示位置
         * @method setPosition
         */
        setPosition: function(position) {
            var elTarget = this.get('elTarget');
            var eventTarget = this.get("eventTarget");
            Y.mt.widget.util.setPosition(this.bBox, position || eventTarget, elTarget, this.get("ndDoc"));
        },
        /**
         * 给tree中的节点图片添加事件,进行展开和折叠
         * @method _initIconEvent
         * @private
         */
        _initIconEvent: function() {
            var ndTreeContainer = this.get("contentBox"),
                _this = this;

            ndTreeContainer.delegate('click', function() {
                _this._toggle(this.ancestor('.' + BaseTree.CLASS_NAME['NODE_ITEM']));
            }, '.' + BaseTree.CLASS_NAME['NODE_GROUP']);

            ndTreeContainer.delegate('click', function() {
                var item = this.ancestor('.' + BaseTree.CLASS_NAME['NODE_ITEM']);
                if(item.one('.' + BaseTree.CLASS_NAME['NODE_GROUP'])) {
                    _this._toggle(item);
                }
            }, '.' + BaseTree.CLASS_NAME['NODE_STATE']);
        },
        /**
         * 点击文本进行事件绑定
         * @method _initTextEvent
         * @private
         */
        _initTextEvent: function() {
            var cBox = this.get("contentBox");
            var _this = this;
            var leafSelectableOnly = this.get('leafSelectableOnly');
            var classSelectable = '';
            if (leafSelectableOnly) {
                // 如果只有叶子节点可以选择
                classSelectable = '.' + BaseTree.CLASS_NAME.NODE_LEAF + ' .' + BaseTree.CLASS_NAME.LINK_TEXT;
                // 非叶子节点行为为展开/收缩（toggle）
                cBox.delegate('click', function() {
                    var item = this.ancestor('.' + BaseTree.CLASS_NAME.NODE_ITEM);
                    if (item.one('.' + BaseTree.CLASS_NAME.NODE_GROUP)) _this._toggle(item);
                }, '.' + BaseTree.CLASS_NAME.NODE_PACKAGE + ' .' + BaseTree.CLASS_NAME.LINK_TEXT);
            } else {
                // 所有节点都可以选择的情况
                classSelectable = '.' + BaseTree.CLASS_NAME.LINK_TEXT;
            }
            cBox.delegate("click", function() {
                _this._nodeClickHandler(this);
            }, classSelectable);
        },
        /**
         * @method nodeClickHandler
         * @public
         */
        nodeClickHandler: function(node) {
            this._nodeClickHandler(node);
        },
        /**
         * tree上面的节点点击后的事件处理函数
         * @method _nodeClickHandler
         * @param {Node} node 当前点击的节点
         * @private
         */
        _nodeClickHandler: function(node) {
            var after = this.get("afterChoose"),
                bBox = this.get('boundingBox'),
                eventTarget = this.get('eventTarget'),
                showCheckbox = this.get('showCheckbox');

            var nodeData = this._getItemData(node) || {};
            if (after) {
                /**
                 * tree上节点的点击事件，供外部监测
                 * @event tree:nodeSelected
                 * @param 被点击的某个节点
                 */
                Y.Global.fire("tree:nodeSelected", {node: node});
                var returnValue = after(nodeData, this);
                if (returnValue !== false && eventTarget) {
                    eventTarget.setData('value', nodeData);
                }
            } else if (!showCheckbox) {
                this.setValue(nodeData);
            }
            //当tree是和input关联并且没有多选的情况下
            if (!this._isEmbedInPage() && !showCheckbox) {
                bBox.hide();
            }
        },
        hide: function () {
            this.bBox.hide();
        },
        /**
         * 创建hidden元素, 存放需要发送到后端去的值
         * @method _createHiddenInput
         * @private
         */
        _createHiddenInput: function() {
            var eventTarget = this.get('eventTarget'),
                hiddenInput = eventTarget.get('parentNode').one('input[type="hidden"]'),
                hiddenParams = Y.mt.util.decodeJSON(eventTarget.getAttribute('data-params') || "{}"),
                name = hiddenParams.name,
                nodeId = hiddenParams.nodeId,
                hiddenValue,
                node,
                nodeData;

            if(!hiddenInput && name !== '') {
                var tempData = { name: name, nodeId: nodeId || "" };
                hiddenInput = Y.Node.create(Y.Lang.sub(BaseTree.TEMPLATE['HIDDEN_INPUT'], tempData));
                eventTarget.insert(hiddenInput, 'after');
            } else {
                //初始化value data
                hiddenValue = hiddenInput.get('value');
                node = this.getNodeById(hiddenValue);
                if(hiddenValue !== "" && node) {
                    nodeData = this._getItemData(node);
                } else {
                    nodeData = { name: eventTarget.get('value'), id: hiddenInput.get('value') };
                }
                eventTarget.setData('value', nodeData);
            }
        },
        /**
         * 点击页面其它位置自动隐藏
         * @method _autoHide
         * @private
         */
        _autoHide: function(nl) {
            var _this = this;
            var eventTarget = nl || this.get('eventTarget');
            $WidgetUtil.hideNodeByClickDocument(this.bBox, eventTarget, function() {
                _this.fire(LIFECYCLE.TREE_HIDE);
            });
        },
        /**
         * UI事件绑定
         * @method initEvent
         */
        initEvent: function() {
            var ndBoundingBox = this.get("boundingBox"),
                initEvent = this.get("initEvent"),
                eventTarget = this.get('eventTarget'),
                before = this.get("before"),
                _this = this;

            eventTarget.on(initEvent, function() {
                _this.set('eventTarget', this);
                _this.fire(LIFECYCLE.BEFORE_TREE_SHOW);
                if (before) {
                    if (!before()) return;
                }
                if (ndBoundingBox.getStyle("display") === 'none') {
                    _this.show();
                    _this.fire(LIFECYCLE.TREE_SHOW);
                } else {
                    ndBoundingBox.hide();
                }
            });
            eventTarget.on("focus",function(e) {
                e.halt();
                _this.set('eventTarget', this);
                _this.clearValue();
            });

            eventTarget.on("blur",function(e) {
                e.halt();
                var isInputCorrect = false;
                var data = null;
                var value = this.get("value");
                var nodes = _this.get("contentBox").all(".item");

                //比较输入的值是否时正确的
                for (var i = 0, l = nodes.size(); i < l; ++i) {
                    data = nodes.item(i).getData("data");
                    if( data && (value === data["name"] || value === "" )) {
                        if( value !== "") {
                            isInputCorrect = true;
                            this.setData('value', data);
                        }
                        break;
                    }
                }
                //输入错误，将值恢复为初始值
                data = this.getData('value');
                if (data && !_this.get('showCheckbox')) {
                    _this.setValue(data, this);
                }
                _this.fire(LIFECYCLE.TREE_BLUR, {data: data});
            });

            this.initKeyControl(eventTarget);
        },
        /**
         * 展示tree
         * @method show
         */
        show: function() {
            this.bBox.show();
            this.setPosition(this.get('position'));
        },
        /**
         * 折叠和展开功能
         * @method _toggle
         * @param {Boolean} expand 展开还是收起，true是展开，false为收起
         * @param {String| Number}  level 未指定代表展开或折叠所有, level为数字代表指定开展到某层级，为字符串或node对象代表指定展开某node节点下面的节点对象
         * @private
         */
        _toggle: function(level, expand, ndParent) {
            var nodeCss = BaseTree.CLASS_NAME,
                root = this.get("contentBox").one('.' + BaseTree.CLASS_NAME['NODE_ROOT']),
                icons = root.all('.' + BaseTree.CLASS_NAME['NODE_GROUP']),
                itemCss = BaseTree.CLASS_NAME['NODE_ITEM'],
                hasRootDetail = this.get('hasRootDetail'),
                reg = new RegExp(nodeCss.EXPAND, 'g'),
                computedSelector = [],
                iconCss,
                fileUseCss,
                itemUseCss,
                ndLevel,
                nodeStates,
                operatorNode,
                fileIcon,
                ndIcon,
                i;
            //toggle的指定层级处理
            if(isNumber(level) && level !== -1) {
                for(i = 0; i < level; i++) {
                    computedSelector.push('>.' + itemCss);
                }
                computedSelector.push('.' + itemCss);
                if(!ndParent) {
                    ndParent = root;
                }
                operatorNode = ndParent.all(computedSelector.join(' '));
                expand  = false;
            //toggle的根元素存在
            } else if(isString(level) || level instanceof Y.Node) {
                ndLevel = Y.one(level);
                ndIcon = ndLevel.one('.' + BaseTree.CLASS_NAME['NODE_GROUP']);
                if(!ndIcon) return;

                operatorNode = ndLevel.all('>.' + itemCss);

                //当点击节点toggle的时候, 只处理自身的icon的样式
                icons = new Y.NodeList(ndIcon);

                //当未指定expand时, 根据icon的状态进行猜测
                iconCss = ndIcon.get("className");
                if(!expand) {
                    expand = reg.test(iconCss) ? true: false;
                }
            } else {
                //自定义的总根结点处理
                //如果没有根结点, 处理第一层的节点
                if(expand) {
                    operatorNode = root.all('.' + itemCss);
                } else {
                    operatorNode = hasRootDetail ? root.all('.' + BaseTree.CLASS_NAME['NODE_ITEM']) : root.all('>.' + itemCss + ' .' + itemCss);
                }
            }
            //当operatorNode无存在
            if(!operatorNode) {
                Y.error('level参数有问题, 请检查!');
                return;
            }
            if(expand || level === -1) {
                operatorNode.show();
                if(ndLevel !== undefined) {
                    ndLevel.show();
                    ndLevel.ancestors().each(function(item) {
                        item.show();
                    });
                }
                fileUseCss = [nodeCss.CLOSE, nodeCss.OPEN];
                itemUseCss = [nodeCss.EXPAND, nodeCss.COLLAPSE];
            } else {
                operatorNode.hide();
                fileUseCss = [nodeCss.OPEN, nodeCss.CLOSE];
                itemUseCss = [nodeCss.COLLAPSE, nodeCss.EXPAND];
            }
            //图标样式的改变
            icons.each(function(item) {
                iconCss = item.get('className');
                fileIcon = item.ancestor('.' + itemCss).one('.' + BaseTree.CLASS_NAME['NODE_STATE']);
                nodeStates = fileIcon.get('className');
                item.set('className', iconCss.replace(itemUseCss[0], itemUseCss[1]));
                fileIcon.replaceClass(fileUseCss[0], fileUseCss[1]);
            });
        },
        /**
         * 是否叶子节点
         * @private
         * @method
         * @param ${}
         * @return ${return}
         */
        isLeafNode: function(data) {
            if (!data.members) {
                return true;
            }
            return data.members.length === 0;
        },
        /**
         * 由模板创建节点
         * @method _createNodeByTpl
         * @param {String} tplName 模板在TEMPLATE中的名字
         * @param {Object} param 应用于模板的参数
         * @private
         */
        _createNodeByTpl: function(tplName, param) {
            var tpl = BaseTree.TEMPLATE[tplName];
            Y.mix(param, BaseTree.CLASS_NAME);
            var code = param ? Y.Lang.sub(tpl, param) : tpl;
            return $N.create(code);
        },
        /**
         * 获得tree节点上绑定的数据
         * @method _getItemData
         * @param {Node} node 获得当前节点上面绑定的data数据
         * @private
         */
        _getItemData: function(node) {
            var cssItem = BaseTree.CLASS_NAME['NODE_ITEM'];
            node = node.hasClass(cssItem) ? node : node.ancestor('.' + cssItem);
            return node.getData('data');
        },
        /**
         * 更改eventTarget的接口
         * @method _setTarget
         * @private
         */
        _setTarget: function( eventTarget) {
            this.set('eventTarget', eventTarget);
            this._createHiddenInput();
            this.initEvent();
            if(this['plugin-tree-search'] ) {
                this['plugin-tree-search'].bindSearchInput();
            }
        },
        /**
         * skin参数处理
         * @method _setSkin
         * @private
         */
        _setSkin: function(skin) {
            this.bBox = this.get('boundingBox');
            //换肤
            if (skin) {
                this.bBox.replaceClass('widget-tree-small', skin);
            }
            return skin;
        },
        /**
         * eventTarget参数处理
         * @method _setEventTarget
         * @param {Node} eventTarget 和tree关联的input
         * @private
         */
        _setEventTarget: function(eventTarget) {
            //初始化eventTarget的相关事件
            if (eventTarget && isString(eventTarget)) {
                return Y.one(eventTarget);
            }
        },
        /**
         * elTarget setter
         * @method _setElTarget
         * @private
         */
        _setElTarget: function(elTarget) {
            if (elTarget && isString(elTarget)) {
                return Y.one(elTarget);
            }
        },
        /**
         * maxHeight setter
         * @method _setMaxHeight
         * @private
         */
        _setMaxHeight: function(maxHeight) {
            return parseInt(maxHeight, 10);
        },
        /**
         * width setter
         * @method _setWidth
         * @private
         */
        _setWidth: function(width) {
            return (isString(width) && width.indexOf('%') !== -1)  ? width : parseInt(width, 10);
        },
        /**
         * data setter
         * @method _setData
         * @private
         */
        _setData: function(dataList) {
            // 将数据统一为一种格式进行处理
            return this.formatData(dataList);
        }
    }, {
        LIFECYCLE: LIFECYCLE,
        /**
         * 目前提供两种皮肤：widget-tree-small(字体12,图片为18*18px), widget-tree-big(字体为14px, 图片为24*24px)
         * @property CSS_PREFIX
         * @type String
         */
        CSS_PREFIX: 'widget-tree-small',
        CLASS_NAME: {
            OPEN: "fa-folder-open",
            CLOSE: "fa-folder",
            EXPAND: "right",
            COLLAPSE: "down",
            LINE: "widget-tree-line",
            ICON_MID:"widget-tree-icon-mid",
            ICON_TOP : "widget-tree-icon-top",
            ICON_BOTTOM : "widget-tree-icon-bottom",
            ROOT_OPEN : "fa-caret-down",//"icon-plus-sign",//"icon-expand-alt",
            ROOT_FOLD : "fa-caret-right",//"icon-minus-sign",//"icon-collapse-alt",
            ICON_OPEN:  "fa-folder-open",
            ICON_FOLD: "fa-folder",
            ICON_LEAF: "fa-file-o",
            ITEM_LINE: "widget-tree-itemline",
            ITEM_PACKAGE: 'widget-tree-p-package',
            ITEM_LEAF: 'widget-tree-p-leaf',
            NODE_PACKAGE: 'widget-tree-node-package',
            NODE_LEAF: 'widget-tree-node-leaf',
            LINK_TEXT: "widget-tree-text",
            NODE_ICON: "widget-tree-icon",
            NODE_GROUP: "widget-tree-group",
            NODE_ROOT: 'widget-tree-root',
            NODE_ITEM: 'widget-tree-item',
            ROOT_BG: 'widget-tree-bg',
            ROOT_UNEMBED: 'widget-tree-unembed',
            ROOT_DETAIL: 'widget-tree-root-detail',
            CLEAR: 'widget-tree-clear',
            ROOT_NODATA: 'widget-tree-root-nodata',
            NODE_STATE:'widget-tree-state'
        },
        TEMPLATE: {
            TREE_NODE: '<div id="catof{id}" class="{NODE_ITEM} catof{id} {NODE_TYPE}">' +
                            '<p  class="{ITEM_LINE} {ITEM_TYPE}">' +
                                '<span class="fa {NODE_ICON} {nodeClass}"></span>' +
                                '<span class="fa {NODE_STATE} {nodeState}"></span>' +
                                '<a class="{LINK_TEXT} {className}" target="{target}" href="{href}">{text} {length}</a>' +
                            '</p>' +
                        '</div>',
            ROOT: '<div class="{NODE_ROOT} {NODE_ITEM} {ROOT_BG} {ROOT_UNEMBED} catof{rootId}" id="catof{rootId}" style="width: {width};"></div>',
            ROOT_DETAIL: '<p class="{ROOT_DETAIL} {ITEM_LINE}">' +
                                '<span class="{NODE_ICON} {nodeCss}"></span>' +
                                '<span class="{NODE_STATE} {nodeState}"></span>' +
                                '<span class="{LINK_TEXT}">{text}</span>' +
                            '</p>',
            HIDDEN_INPUT: '<input type="hidden" value="{nodeId}" name="{name}" />'
        },
        ATTRS: {
            /**
             * 这个属性在Widget基类里定义，与 Internationalization 组件一起使用可以支持本地化
             * @attribute strings
             * @type TODO
             */
            strings: {

            },
            /**
             * 替换默认样式
             * @attribute skin
             * @type String
             */
            skin: {
                value: null,
                setter: '_setSkin'
            },
            /**
             * 默认依附对象
             * @attribute ndDoc
             * @type Node
             * @default document.body
             */
            ndDoc: {
                value: Y.one(document.body)
            },
            /**
             * 定位控件，也可设置elTarget参数来控制位置, 值可以是:center 放于doc中央, inner 放于elTarget里面
             * @attribute position
             * @type String
             */
            position: {
                value: null,
                setter: 'setPosition'
            },
            /**
             * 在页面只有一颗树的情况
             * @attribute id
             * @type String
             */
            id: {
                value: null
            },
            /**
             * tree放入的容器
             * @attribute elTarget
             * @type Node
             * @default docuemnt.body
             */
            elTarget: {
                value: Y.one(document.body),
                setter: '_setElTarget'
            },
            /**
             * 无数据是提示信息
             * @attribute noDataMsg
             * @type String
             * @default '无数据'
             */
            noDataMsg: {
                value: '无数据'
            },
            /**
             * 宽度
             * @attribute width
             * @type Number
             */
            width: {
                value: null,
                setter: '_setWidth'
            },
            /**
             * 显示根节点信息，这个点不会显示checkbox，也不能点击
             * @attribute hasRootDetail
             * @type Boolean
             */
            hasRootDetail: {
                value: null
            },
            /**
             * 当没有根节点时，此值作为自动生成的根节点的name属性
             * @attribute rootTitle
             * @type String
             * @default 'root'
             */
            rootTitle: {
                value: 'root'
            },
            /**
             * 添加input框的placeholder
             * @attribute placeholder
             * @type String
             */
            placeholder: {
                value: ''
            },
            /**
             * 展开程度，-1表示全部展开，0表示展开0层，即全部收缩。1,2,3...分别表示展开1，2，3层
             * @attribute expandLevel
             * @type Number
             */
            expandLevel: {
                value: -1
            },
            /**
             * 用来显示在Tree上的数据
             * @attribute data
             * @type Object
             */
            data: {
                value: null,
                setter: '_setData'
            },
            /**
             * 给eventTarget绑定的事件,触发该事件则显示或隐藏tree
             * @attribute initEvent
             * @type String
             * @default 'click'
             */
            initEvent: {
                value: 'click'
            },
            /**
             * tree所依附的对象
             * @attribute eventTarget
             * @type Node
             */
            eventTarget: {
                value: null,
                setter: '_setEventTarget'
            },
            /**
             * 如果设置了initEvent,则在initEvent事件被触发时调用
             * @attribute before
             * @type Function
             */
            before: {
                value: null
            },
            /**
             * tree中的选项点击后调用（点击checkbox不会调用）
             * @attribute afterChoose
             * @type Function
             */
            afterChoose: {
                value: null
            },
            /**
             * tree的最大高度
             * @attribute maxHeight
             * @type Number
             * @default 300
             */
            maxHeight: {
                value: 300,
                setter: '_setMaxHeight'
            },
            /**
             * 默认的高度
             * @attribute height
             * @type Number | String
             */
            height: {
                value: 'auto'
            },
            /**
             * 搜索匹配模式
             * @attribute sMode
             * @type String
             */
            sMode: {
                value: 'fuzzy'
            },
            /**
             * 显示单层子节点个数的开关
             * @attribute showLeafNumber
             * @type Boolean
             */
            showLeafNumber: {
                value: true,
                setter: 'isShowLeafNumber'
            },
            /**
             * 是否只有叶子节点才能选择；若为 true，非叶子节点的点击事件将为展开/收缩
             * @attribute leafSelectableOnly
             * @type Boolean
             */
            leafSelectableOnly: {
                value: false
            }
        }
    });

    Y.mt.widget.BaseTree = BaseTree;

}, '1.0.0', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'w-core',
        'base-build',
        'widget',
        'oop',
        'node'
    ],
    skinnable: true
});

