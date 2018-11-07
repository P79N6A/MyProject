/**
* autocomplete组件, 提供中文拼音自动搜索、级联、多选、list增强等功能
* @module w-autocomplete
*/
M.add('w-autocomplete', function(Y){
    var $N = Y.Node;
    var $IO = Y.io;
    var isIE =  Y.UA.ie;
    var isIE6 = (isIE === 6);
    var isArray = Y.Lang.isArray;
    var trim = Y.Lang.trim;

    var $WidgetUtil = Y.mt.widget.util;
    var $Util = Y.mt.util;

    var EVENT_NAME = {
        select: 'AutoComplete.select',
        hide: 'AutoComplete.hide',
        update: 'AutoComplete.updateData',
        updateComplete: 'AutoComplete.updateComplete',
        beforeShowResultBox: 'beforeShowResultBox',
        beforeBuildCheckbox: 'beforeBuildCheckbox',
        afterShowResultBox: 'afterShowResultBox',
        afterBuildCheckbox: 'afterBuildCheckbox',
        getMatchList: 'getMatchList',
        beforeUpdateData: 'beforeUpdateData',
        getObjListSuccess: 'afterGetDataSuccess',
        dataInitSuccess: 'dataInitSuccess',
        beforeShow: 'beforeShow'
    };

    var CSS_NAME = {
        cssCompleteList: 'widget-autocomplete-complete-list',
        cssTip: 'widget-autocomplete-tip',
        cssList: 'widget-autocomplete-list',
        cssMore: 'widget-autocomplete-more',
        cssIE6BugFixed: 'widget-autocomplete-ie6bugfix',
        cssLoading: 'widget-autocomplete-loading',
        cssMenuOperator: 'widget-autocomplete-menu-operator',
        cssScrollList: 'widget-autocomplete-list--scroll'
    };

    Y.namespace('mt.widget');
    /**
     * 自动完成组件, 支持拼音搜索, 异步获取数据, 多级Autocomplete级联, 多选等
     * @class AutoCompleteList
     * @namespace mt.widget
     * @constructor
     */
    var AutoCompleteList = Y.Base.create('Autocomplete', Y.Widget, [Y.WidgetPosition, Y.WidgetPositionAlign, Y.WidgetStack, Y.mt.extension.List], {
        initializer: function() {
            var _this = this;
            this.onceAfter('initializedChange', function() {
                _this.bBox = _this.get('boundingBox');
                _this.cBox = _this.get('contentBox');
                var allNode = _this.get('node');
                _this.allNode = allNode instanceof Y.NodeList ? allNode : Y.all(allNode);
                _this.node = _this.allNode.item(0);
                _this.ndDoc = _this.get('ndDoc');
                // 使用List扩展中的列表触发器
                _this._ndListTrigger = _this.node;
                //选中的itemids
                _this.checkedItemIds = {};
                //改变的checkbox item id列表
                _this.checkboxChanged = {};
                //搜索框上次输入字符的长度
                _this.preLen = 0;
                // 计时器
                _this.timer = null;
                // 是否正在加载数据
                _this.isLoading = false;
                // 缓存常用的查询
                _this.listCache = {};

                _this._handle = {
                    nodeEvent: null
                };

                //数据成功初始化完毕再 渲染
                _this.on(EVENT_NAME.dataInitSuccess, _this.render);
                _this._dataInit();
                // 原有值
                _this.oldSelect = _this.getValue();
            });
        },
        /**
         * 数据成功后的处理函数
         * @method _dataInit
         * @private
         */
        _dataInit: function() {
            this.addAyncSearchPlugin();
            this.addCascadePlugin();
            this._configAttr();
            this._beforeRender();
        },
        /**
         * render 前处理
         * @method _beforeRender
         * @private
         */
        _beforeRender: function() {
            var dataUrl = this.get('dataUrl');
            var _this = this;
            //如果数据需要异步获取
            if(dataUrl) {
                Y.mt.io.get(dataUrl, null, function(res) {
                    _this.fire(EVENT_NAME.getObjListSuccess, { res: res});
                    if(res.data) {
                        _this.set("objList", res.data);
                    }
                    _this.fire(EVENT_NAME.dataInitSuccess, {objList: res.data});
                }, null, true);
            } else {
                this.fire(EVENT_NAME.dataInitSuccess);
            }
        },
        /**
         * 根据需求加入cascade 插件
         * @method addCascadePlugin
         */
        addCascadePlugin: function() {
            var cascadeParams = this.get('cascade');
            //cascade 入口
            if(cascadeParams) {
                if(cascadeParams.nds && isArray(cascadeParams.nds)) {
                    this.set('node', cascadeParams.nds[0]);
                }
                if(cascadeParams.actions && isArray(cascadeParams.actions)) {
                    var action = cascadeParams.actions[0];
                    if(action !== null || action !== "") {
                        this.set('dataUrl', action);
                    }
                }
                this.plug(Y.mt.plugin.autocomplete.Cascade, cascadeParams);
            }
        },
        /**
         * 根据异步搜索插件
         * @method addAyncSearchPlugin
         */
        addAyncSearchPlugin: function() {
            var ayncsearch = this.get('ayncsearch');
            if(ayncsearch) {
                this.plug(Y.mt.plugin.autocomplete.AyncSearch, { ayncsearch: ayncsearch });
            }
        },
        /**
         * 配置初始化属性
         * @method _configAttr
         * @private
         */
        _configAttr: function() {
            // 为节点添加id
            if (!this.node.get('id')) this.node.set('id', Y.guid());
            //关闭浏览器中的autocompelte
            if(!this.node.getAttribute('autocomplete')) {
                this.node.setAttribute('autocomplete', 'off');
            }
            //分离searchKey(需要将中文根据字库转换成拼音) && 显示值
            var listParam = this.get('listParam');
            if(this.get('searchKey') === '') {
                this.set('searchKey', listParam);
            }
            //处理匹配轮询参数
            this._setPolling(this.get('searchKey'));
        },
        /**
         * 设置polling, 轮询时objList中的字段
         * @method _setPolling
         * @private
         * @param {String} searchKey=name 查询数据字段
         */
        _setPolling: function(searchKey) {
            var polling = this.get('polling');
            if(Y.Lang.isArray(polling) ) {
                if(polling.length === 0 || !Y.mt.lang.inArray(searchKey, polling)) {
                    polling.push(searchKey);
                }
                this.set('polling',  polling);
            }
        },
        /**
         * objList、searchKey改变时更新参数
         * @method _updataObjList
         * @private
         */
        _updataObjList: function(e) {
            var objList = this.get('objList'),
                searchKey = this.get('searchKey');

            if(e.attrName === "searchKey") {
                this._setPolling(searchKey);
            } else if (e.attrName === "objList") {
                this.listCache = {};
            }

            //转换拼音数据
            Y.mt.pinyin.objListToPinyin(objList, searchKey);
        },
        /**
         * 设置高度
         * @method setWidth
         */
        setWidth: function() {
            var ndWidth = this.node.getStyle('width');
            var boxWidth = this.get('width');
            if(ndWidth !== 'auto') {
                ndWidth = parseInt(ndWidth.replace('px', ''), 10) + 8;
            } else {
                ndWidth = boxWidth;
            }
            var bBoxBorderWidth = 2;
            var nodeWidth = this.node.get('offsetWidth') - bBoxBorderWidth || ndWidth;
            if(boxWidth === 142) this.set('width', nodeWidth);
        },
        /**
         * 生成匹配结果容器
         * @method renderUI
         */
        renderUI: function() {
            this.after('searchKeyChange', function(e) {
                this._updataObjList(e);
            });

            this.after('objListChange', function(e) {
                this._updataObjList(e);
            });
            this.after('nodeChange', function() {
                this._bindQueryNodeHandler();
            });

            this.after('menuItemChange', function(e) {
                this._addMenuItem(e.newVal);
            });
            //转换拼音数据
            Y.mt.pinyin.objListToPinyin(this.get('objList'), this.get('searchKey'));

            var id = this.get('id');
            var menuItem = this.get('menuItem');

            if(id) this.bBox.setAttribute("id", id);
            this.setWidth();

            // 添加iframe，修正IE6下select位于list之上的bug
            if (isIE6) {
                this.ndIframe = $N.create(Y.Lang.sub(AutoCompleteList.TEMPLATE.iframe, CSS_NAME));
                this.bBox.appendChild(this.ndIframe);
            }

            var listTemplate = this.get('listTemplate');
            var listMarkup = Y.Lang.sub(listTemplate, Y.mix({
                more: this.get('more'),
                msg: this.get('msg')
            }, CSS_NAME));

            var listContainer = $N.create(listMarkup);
            this.cBox.appendChild(listContainer);

            //是否在面板中添加更多功能处理
            if (menuItem && menuItem.length > 0) {
                this._addMenuItem(menuItem);
            }
            this.ndUl = listContainer.one('ul');
            //listContainer 有时候会取不到ul，添加一段保护性代码
            if (!this.ndUl) {
                this.ndUl = this.cBox.one('ul');
            }
            this.ndMore = this._getOperatorNode().more;
        },
        /**
         * ui相关事件处理
         * @method bindUI
         */
        bindUI: function() {
            var _this = this;
            if(this.get('placeholder')) {
                this.node.plug(Y.mt.plugin.Placeholder);
            }
            this._bindQueryNodeHandler();

            //如果需要进行多选, 添加确定按钮
            if(this.get('showCheckbox')) {
                this._initCheckboxHandler();
            }

            Y.Global.on(EVENT_NAME.update, function(data) {
                if (data.id === _this.node.get('id')) {
                    _this.updateData(data);
                }
            });
        },
        /**
         * 设置关联input的值
         * @method setValue
         */
        setValue: function(checkObj) {
            this.fillTargetInput(checkObj);
        },
        /**
         * 获得隐藏input, 真正保存值的input
         * @method getHiddenInput
         */
        getHiddenInput: function() {
            return this.node.get('parentNode').one('input[type="hidden"]');
        },
        /**
         * 获得关联的input的值
         * @method getValue
         */
        getValue: function() {
            var val = {};
            val.name = trim(this.node.get('value'));
            var hiddenInput = this.getHiddenInput();
            if (hiddenInput) {
                val.id = trim(hiddenInput.get('value'));
            }
            val.hiddenInput = hiddenInput;
            return val;
        },
        /**
         * 清空关联的input的值
         * @method clearValue
         */
        clearValue: function() {
            this.node.set('value', "");
            var hiddenInput = this.getHiddenInput();
            if (hiddenInput) {
                hiddenInput.set('value', '');
            }
        },
        /**
         * 多选的默认处理函数
         * @method _initCheckboxHandler
         * @private
         */
        _initCheckboxHandler: function() {
            var _this = this;
            this.set('menuItem',[{
                title: '确定',
                callback: function() {
                    _this._resetCheckboxChanged(true);
                    _this.getCheckedNodes();
                    _this.hide();
                }
            }]);
        },
        /**
         * 获得操作列表
         * @method _getOperatorNode
         * @private
         */
        _getOperatorNode: function() {
            return {
                menuOperator: this.bBox.one("." + CSS_NAME.cssMenuOperator),
                more: this.bBox.one('.' + CSS_NAME.cssMore)
            };
        },
        /**
         * 添加node对象的鼠标事件
         * @method _bindQueryNodeHandler
         * @private
         */
        _bindQueryNodeHandler: function() {
            var _this = this;
            // 输入框发生click事件时显示list
            this._handle.nodeEvent = this.allNode.each(function(item) {
                item.purge();
                item.on({
                    click: showList,
                    focus: showList
                });
                item.once({
                    keydown: showList
                });
            });
            //展示列表
            function showList(e) {
                //点击了其它input,则切换this.node
                if (_this.node !== this) {
                    //将其他input隐藏
                    _this.hide();
                    _this.node = this;
                    _this._ndListTrigger = _this.node;
                    // 为节点添加id
                    if (!_this.node.get('id')) _this.node.set('id', Y.guid());
                    // 切换原有值
                    _this.oldSelect = _this.getValue();

                    e.halt();
                    _this.show();
                } else if (_this.bBox.getStyle('display') === 'none') {
                    // 使用显示状态标志位，防止多次点击input时初始化多次
                    //_this.oldSelect = _this.node.get('value');
                    e.halt();
                    _this.show();
                }
            }
        },
        /**
         * 添加更多处理菜单， 针对每种操作的点击事件进行回调
         * @method _addMenuItem
         * @private
         */
        _addMenuItem: function(menuItem) {
            var menuOperator = this._getOperatorNode().menuOperator,
                itemOfMenu;
            menuOperator.set('text', '');
            for( var i = 0, length = menuItem.length; i < length; ++i ) {
                itemOfMenu = $N.create('<li class="btn btn-block btn-primary">' + menuItem[i].title + '</li>');
                this._handle.menuItem = itemOfMenu.on('click', menuItem[i].callback);
                menuOperator.appendChild(itemOfMenu);
            }
        },
        /**
         * 动态修改数据
         * @method modifyData
         */
        modifyData: function(data){
            this.set('objList', data);
        },
        /**
         * 设置oldSelect
         * @method setOldSelect
         */
        setOldSelect: function(id, name) {
            this.oldSelect = {};
            this.oldSelect.id = id;
            this.oldSelect[this.get('listParam')] = name;
        },
        /**
         * 重置选中的值
         * @method resetOldSelectValue
         */
        resetOldSelectValue: function(){
            this.oldSelect = {};
        },
        /**
         * 离开控件
         * @method _leave
         * @private
         */
        _leave: function() {
            if(this.get('showCheckbox')) {
                this._resetCheckboxChanged();
                this.getCheckedNodes();
            }else{
                this._selectUnique();
            }
            this.hide();
        },
        /**
         * 获得所有选中节点的数据
         * @method getCheckedNodes
         */
        getCheckedNodes: function() {
            var checkedItems = [];
            var checkedItemIds = this.checkedItemIds;
            for(var p in checkedItemIds) {
                if(checkedItemIds.hasOwnProperty(p)) {
                    var item = checkedItemIds[p];
                    if(item.checked) {
                        checkedItems.push(item);
                    }
                }
            }
            //填充到目标节点
            if (this.get('fillTarget') && checkedItems.length > 0) {
                this.fillTargetInput(checkedItems);
            }
            this.resetOldSelectValue();
            return checkedItems;
        },
        /**
         * 设置input元素以及隐藏input元素的值
         * @method fillTargetInput
         * @param {Array | Object} checkedObj 选中的数据
         */
        fillTargetInput: function(checkedObj) {
            var textArr = [],
                valueArr = [],
                hiddenInput = this.getHiddenInput();
            if(Y.Lang.isArray(checkedObj)) {
                var length = checkedObj.length;
                for( var i = 0; i < length; i++ ) {
                    var id = checkedObj[i].id;
                    var name = checkedObj[i].name;
                    textArr.push(name);
                    valueArr.push(id);
                    //主动调用的情况
                    this.checkedItemIds['checked-' + id]  = {
                        id: id,
                        name: name,
                        checked: true
                    };
                }
            } else {
                textArr[0] = checkedObj[this.get('listParam')];
                valueArr[0] = checkedObj.id;
            }
            if (this.get('showCheckbox') && this.get('rememberable')) {
                this.setOldSelect(valueArr.join(','), textArr.join(','));
            }
            this.node.set("value",textArr.join(','));
            if (hiddenInput) {
                hiddenInput.set('value', valueArr.join(','));
            }
        },
        /**
         * 动态获取异步查询参数
         * @private
         * @method _getQueryParam
         * @param {}
         * @return {object}
         */
        _getQueryParam: function () {
            var query = {};
            var queryNode = this.get("queryNode");
            if (queryNode) {
                var ndQuery;
                for (var i = 0; i < queryNode.length; i++) {
                    ndQuery = queryNode[i];
                    if (ndQuery) {
                        query[ndQuery.get("name")] = ndQuery.get("value");
                    }
                }
            }
            return query;
        },
        /**
         * 更新列表数据
         * @method updateData
         * @param {object} data 需要更新的列表数据
         * @param {boolean} keepOldSelect 保持文本框原有值不变
         */
        updateData: function(data, keepOldSelect) {
            var _this = this;

            //默认清空原有值
            if(!keepOldSelect) {
                this.clearValue();
                //清空oldSelect
                this.resetOldSelectValue();
            }

            if (data.data) {
                update.call(this, data.data);
            } else if (data.action) {
                if (this.async && this.async.isInProgress()) {
                    this.async.abort();
                }
                this.isLoading = true;
                //动态获取异步查询参数
                var queryParam = this._getQueryParam();
                Y.mix( queryParam, { q: this.node.get('value') }, true);
                this.async = $IO(data.action, {
                    method:'GET',
                    data: queryParam,
                    on: {
                        success:function(id, o) {
                            _this.isLoading = false;
                            var res = $Util.getEvalRes(o);
                            if (res.status) {
                                update.call(_this, res.data, res);
                            } else {
                                Y.config.win.alert(res.msg);
                            }
                        },
                        failure:function(id, o) {
                            if (o.statusText && o.statusText === 'abort') return;
                            _this.isLoading = false;
                            _this.cBox.setHTML('网络超时，请重试');
                        }
                    }
                });
            }

            function update(d, res) {
                if(!Y.Lang.isObject(d)) d = [];
                this.fire(EVENT_NAME.beforeUpdateData, { objList: d, res: res } );
                if (this.get("defaultItem") && isArray(d)) {
                    d.unshift(this.get("defaultItem"));
                }
                this.set('objList', d);
                //当列表显示时能自动更新列表的内容
                if (_this.bBox && _this.bBox.getStyle('display') === 'block') {
                    this.displayList();
                }
                Y.Global.fire(EVENT_NAME.updateComplete, {});
            }
        },
        /**
         * 显示list
         * @method show
         */
        show: function() {
            //默认禁止手动输入
            if (this.get("forbidWrite")) {
                this.clearValue();
            }
            this._showList();

            //避免重复绑定
            if (this.valueChangeHandler) {
                this.valueChangeHandler.detach();
            }
            this.valueChangeHandler = this.node.on("valueChange", this._inputValueChange, this);
        },
        /**
         * @method _inputValueChange
         * @private
         */
        _inputValueChange: function(e) {
            var newVal = e.newVal,
                delay = this.get("searchInterval"),
                _this = this,
                reqFun;

            //避免Enter输入触发valueChange，导致重发请求
            if (e['_event'] && e['_event']['keyCode'] === 13) {
                return;
            }
            if (newVal.trim().length > 0) {
                reqFun = function() {
                    if(_this.get('action')) {
                        // 若数据为空，先异步获取数据, 否则显示无数据
                        _this.updateData({action: _this.get('action')}, true);
                    } else {
                        _this._showList();
                    }
                };
                // 连续输入清除定时器，输入完毕后才发送请求
                if (delay) {
                    clearTimeout(this.timer);
                    this.timer = setTimeout(reqFun, delay);
                } else {
                    reqFun();
                }
            } else {
                clearTimeout(this.timer);
            }
        },
        /**
         * @method _showList
         * @private
         */
        _showList: function() {
            var params = { show: true };
            this.fire(EVENT_NAME.beforeShow, params);
            if(params.show) this.displayList();
        },
        /**
         * 重置list
         * @method resetList
         */
        resetList: function() {
            this.ndUl.get('children').remove();
            if (this.ndMore) {
                this.ndMore.setStyle('display', 'none');
            }
        },
        /**
         * 根据输入内容显示下拉列表
         * @method displayList
         */
        displayList: function() {
            var query = this.getQueryString();
            var noDataTips = this.get('noDataTips');
            var objList = this.get('objList');
            var ndLi, nl, matchList = [], thred, cBox = this.get('contentBox');
            //默认在有maxShowItemCount时候设置thred
            thred = this.get('maxShowItemCount') ? this.get('maxShowItemCount') : 0;
            //重置list
            this.resetList();
            this.fire(EVENT_NAME.beforeShowResultBox, { objList: objList || [] } );
            if (isArray(objList) && objList.length === 0 && this.get("defaultItem")) {
                objList.unshift(this.get("defaultItem"));
            }
            if (this.isLoading) {
                // 正在加载数据
                ndLi = $N.create(Y.Lang.sub(AutoCompleteList.TEMPLATE.loading, CSS_NAME));
                matchList.push(ndLi);
            } else if (Y.Object.isEmpty(objList)) {
                if (noDataTips !== "") {
                    ndLi = $N.create('<li>' + noDataTips + '</li>');
                    matchList.push(ndLi);
                }
            } else {
                //匹配数据, 先在已缓存过的数据中找
                matchList = this.getMatchList(query);
            }
            nl = new Y.NodeList(matchList);
            if (nl.size() > thred) {
                cBox.one('.' + CSS_NAME.cssCompleteList).addClass(CSS_NAME.cssScrollList);
            } else {
                cBox.one('.' + CSS_NAME.cssCompleteList).removeClass(CSS_NAME.cssScrollList);
            }
            this.fire(EVENT_NAME.getMatchList, {matchList: nl});

            //如果需要多选, 则构造checkbox input节点
            if( this.get('showCheckbox') ) {
                this.fire(EVENT_NAME.beforeBuildCheckbox, { matchList: nl, checkItems: this.checkedItemIds });
                this._buildCheckboxNode(nl);
                this.fire(EVENT_NAME.afterBuildCheckbox, { matchList: nl, checkItems: this.checkedItemIds });
            }
            this.ndUl.appendChild(nl.toFrag());

            this.showResultBox();

            this.fire(EVENT_NAME.afterShowResultBox, { ulBox: this.ndUl });

            if (this.ndIframe) {
                this.resizeIframe();
            }
        },
        /**
         * 创建对选的checkbox选择框
         * @method _buildCheckboxNode
         * private
         */
        _buildCheckboxNode:function(nl) {
            var _this = this;
            nl.each(function(item) {
                var objData = item.getData('objData');
                if (!objData)  return;
                var id = item.getData('objData').id;
                var checkedItem = '';
                if(_this.checkedItemIds['checked-' + id]  && _this.checkedItemIds['checked-' + id].checked) {
                    checkedItem = 'checked="checked"';
                }
                var checkbox = $N.create(Y.Lang.sub(AutoCompleteList.TEMPLATE.checkbox, {checked: checkedItem}));
                var ndCheckbox = item.one('input[type="checkbox"]');
                if (!ndCheckbox) {
                    checkbox.append(item.getHTML());
                    item.setHTML(checkbox);
                } else {
                    if (checkedItem) {
                        ndCheckbox.setAttribute('checked', 'checked');
                    } else {
                        ndCheckbox.removeAttribute('checked');
                    }
                }
            });
        },
        /**
         * 处理query的转换
         * @method getQueryString
         */
        getQueryString: function() {
            var query = trim(this.node.get('value'));
            var toLowerCase = this.get('toLowerCase');
            return toLowerCase ? query.toLowerCase() : query;
        },
        /**
         * 所得满足条件的list
         * @method getMatchList
         */
        getMatchList: function(query) {
            var _this = this;
            function getCachedList(q) {
                var cacheItem = _this.listCache[q];
                if(cacheItem.showMax && _this.ndMore)  _this.ndMore.setStyle('display', 'block');
                return cacheItem.matchList;
            }

            if (query === '' && this.listCache.noData) {
                return getCachedList('noData');
            } else if (this.listCache[query]) {
                return getCachedList(query);
            } else {
                return this.buildList();
            }
        },
        /**
         * 获得成功搜索匹配的list
         * @method buildList
         */
        buildList: function() {
            var i, index = 0;
            var item, text, showMax = false;
            var ndLi, matchList = [], itemMarkup;

            var query = this.getQueryString(),
                template = this.get('itemTemplate'),
                highlightFirstMatch = this.get('highlightFirstMatch'),
                objList = this.get('objList'),
                listParam = this.get('listParam');

            for (i in objList) {
                if (objList.hasOwnProperty(i)) {
                    item = objList[i];
                    //显示值
                    text = item[listParam];
                    if (this.isMatchModeReg(item, query)) {
                        itemMarkup = Y.Lang.sub(template, Y.merge(item, {
                            text: text,
                            className: index === 0 && highlightFirstMatch ? 'current' : ''     // 默认高亮第1个匹配到的item
                        }));
                        ndLi = $N.create(itemMarkup);
                        ndLi.setData('objData', item);
                        matchList.push(ndLi);
                        index++;
                    }
                    // 有maxShowItemCount选项 仍然需要buildList
                    if (this.get('maxShowItemCount')) continue;

                    if (index >= this.get('showMax')) {
                        // 显示更多选项提示
                        if (this.ndMore) {
                            this.ndMore.setStyle('display', 'block');
                        }
                        showMax = true;
                        break;
                    }
                }
            }
            //缓存已访问过的数据
            var cacheItem = { showMax: showMax, matchList: matchList };
            if (query === '') {
                this.listCache.noData = cacheItem;
            } else {
                this.listCache[query] = cacheItem;
            }
            return matchList;
        },
        /**
         * 是否匹配设定的匹配模式
         * @method isMatchModeReg
         */
        isMatchModeReg: function(item, query) {
            var matchMode = this.get('matchMode'),
                pollingAttrs = this.get('polling'),
                reg = $WidgetUtil.getRegByMode(query, matchMode),
                longPin = item.PY_ALL || '',
                shortPin = item.PY_SHORT || '';

            if (matchMode === 'none') {
                return true;
            }
            if (query === "" ||  reg.test(longPin) || reg.test(shortPin)) {
                return true;
            } else {
                //polling item 里面是否有属性值匹配
                var length = pollingAttrs.length;
                for (var j = 0; j < length; j++) {
                    var pollingItems = trim(item[pollingAttrs[j]]);
                    //结合polling一起使用时
                    if(!pollingItems) break;

                    if(this.get('toLowerCase')) {
                        pollingItems = item[pollingAttrs[j]].toLowerCase();
                    }
                    if(reg.test(pollingItems)) {
                        return true;
                    }
                }
            }
            return false;
        },
        /**
         * 设置box的位置
         * @method showResultBox
         */
        showResultBox: function() {
            this.bBox.setStyles({'display': 'block', position: 'absolute'});
            this.set('align', { node: this.node, points: ['tl', 'bl'] });
        },
        /**
         * 扩展中需要实现的方法
         * @method _select
         * @private
         */
        _select: function(ndItem) {
            if (!this.get('showCheckbox') && ndItem.get("nodeName") !== "INPUT" ) {
                this.selectHandler(ndItem);
            } else {
                this._changeCheckcboxState(ndItem);
            }
        },
        /**
         * 记忆input type=checkbox的状态
         * @method _changeCheckcboxState
         * @private
         */
        _changeCheckcboxState: function(ndItem) {
            var ndLi = (ndItem.get("nodeName") === "LI" ) ? ndItem : ndItem.ancestor('li');
            //如果是点击的操作则停止
            if(ndLi.ancestor('.' + CSS_NAME.cssMenuOperator)) return;

            var item = ndLi.getData('objData');
            if (!item) return;
            this._markCheckboxChanged('checked-' + item.id, ndLi.one('input'));
            this.checkedItemIds['checked-' + item.id] = {
                checked: ndLi.one('input').get('checked'),
                id: item.id,
                name: item[this.get('listParam')]
            };
        },
        /**
         * 记录点击确定前修改过的checkbox
         * @_markCheckboxChanged
         * @private
         */
        _markCheckboxChanged: function(itemId, li){
            if(itemId in this.checkboxChanged){
                delete this.checkboxChanged[itemId];
            }
            else{
                this.checkboxChanged[itemId] = li;
            }
        },
        /**
         * 复原修改过的checkbox
         * @_resetCheckboxChanged
         * @private
         */
        _resetCheckboxChanged: function(hasSubmit){
            if(hasSubmit){
                this.checkboxChanged = {};
                return;
            }
            for (var id in this.checkboxChanged) {
                if (this.checkboxChanged.hasOwnProperty(id)) {
                    this.checkedItemIds[id].checked = !this.checkedItemIds[id].checked;
                    this.checkboxChanged[id].set('checked', this.checkedItemIds[id].checked);
                }
            }
            this.checkboxChanged = {};
        },
        /**
         * 选中列表中某项的事件处理
         * @method selectHandler
         */
        selectHandler: function(item) {
            var objData = item.getData('objData');
            if (!objData || !item || item.get('text') === this.get("noDataTips")) return;

            var itemId = objData.id,
                selectVal = objData[this.get('listParam')];

            this.fillTargetInput(objData);
            this.hide();
            // 当当前选项不为空并且与之前的选项不一致时，触发监听事件
            if (selectVal !== this.oldSelect[this.get('listParam')]) {
                this.setOldSelect(itemId, selectVal);
                this.preLen = selectVal.length;
            }
            //处理的优先级为: callback/redirect

            this.fire(EVENT_NAME.select, { item: item, ins: this });
            this.node.fire('widgetvaluechange', {item: item, data: item.getData('objData'), ins: this});
            this.callback(objData);
            this.redirectPage(itemId);
        },
        /**
         * 如果url存在,根据url进行重定位
         * @method redirectPage
         */
        redirectPage: function(itemId) {
            var baseUrl = this.get('url');
            if (baseUrl && baseUrl !== "") {
                var url = /&/.test(baseUrl) ? (baseUrl + '&id=' + itemId) : (baseUrl + '?id=' + itemId);
                window.location.href = url;
            }
        },
        /**
         * 处理选中的方法回调
         * @method callback
         * @param {Object} objData 当前列表中选中项
         */
        callback: function(objData) {
            var cascadeParams = this.get('cascade');
            var callback = this.get('callback');
            if (!callback && cascadeParams && cascadeParams.callback) {
                callback = cascadeParams.callback[0];
            }
            if (callback) {
                callback(objData, this.node);
            }
            Y.Global.fire(EVENT_NAME.select, { id: this.node.get('id'), data: objData});
        },
        /**
         * 隐藏list
         * @method hide
         */
        hide: function() {
            if (this.node.get('value') === "") {
                //只有设定为记忆状态才恢复原来状态
                if (this.get('rememberable') === true) {
                    this.setValue(this.oldSelect);
                } else {
                    //清空隐藏的值
                    this.clearValue();
                }
            }
            this.node.blur();
            this.bBox.setStyle('display', 'none');
            if(this.timer) window.clearInterval(this.timer);

            //如果checkbox有显示，要全部清空
            if ( this.get('showCheckbox') ) {
                this.bBox.all(".checkbox").each(function(){
                    this.set("checked",false);
                });
            }
            this.fire(EVENT_NAME.hide, {id: this.node.get('id')});
        },
        /**
         * 输入文本时查询是否唯一匹配, 如果唯一则选中唯一项
         * @_selectUnique
         * @private
         */
        _selectUnique: function(){
            var text = this.node.get('value');
            if(text === ""){return;}
            var query = this.getQueryString();
            var list = this.getMatchList(query);
            if(list.length === 1){
                this._select(list[0]);
            }
        },
        /**
         * 修正IE6中iframe宽高问题
         * @method resizeIframe
         */
        resizeIframe: function() {
            this.ndIframe.setStyle('width', parseInt(this.ndUl.getComputedStyle('width'), 10) + 2 + 'px');
            this.ndIframe.setStyle('height', parseInt(this.ndUl.getComputedStyle('height'), 10) + 2 + 'px');
        },
        destructor: function() {
            $Util.detachHandle(this._handle);
        },
        /**
         * @method _setNode
         * @private
         */
        _setNode: function(val) {
            this.node = Y.one(val);
            this._ndListTrigger = this.node;
            if(this.node) {
                this.oldSelect = this.getValue();
            }
            this.allNode = val instanceof Y.NodeList ? val: Y.all(val);
            return val;
        }
    }, {

        CSS_PREFIX: 'widget-autocomplete',
        /**
         * @property EVENT_NAME
         * @type Object
         */
        EVENT_NAME: EVENT_NAME,
        /**
         * @property CSS_NAME
         * @type Object
         */
        CSS_NAME: CSS_NAME,
        /**
         * @property TEMPLATE
         * @type Object
         */
        TEMPLATE: {
            checkbox: '<label><input type="checkbox" name="widgetCheckbox" {checked} value=""/></label>',
            loading: '<p class="{cssLoading}"></p>',
            iframe: '<iframe class="{cssIE6BugFixed}" border="0"></iframe>'
        },
        /**
         * @property ATTRS
         * @type Object
         */
        ATTRS : {
            /**
             * 放入到的页面文档
             * @attribute ndDoc
             * @type Node
             * @default document.body
             */
            ndDoc: {
                value: Y.one(Y.config.doc)
            },
            /**
             * 默认提示信息
             * @attribute msg
             * @type String
             * @default '中文|拼音|首拼'
             */
            msg: {
                value: '中文|拼音|首拼'
            },
            /**
             * 默认转为小写进行匹配
             * @attribute toLowerCase
             * @type Boolean
             * @default true
             */
            toLowerCase: {
                value: true
            },
            /**
             * 当可显示的结果数超过showMax的值时显示
             * @attribute more
             * @type String
             * @default '更多选项请搜索'
             */
            more: {
                value: '更多选项请搜索'
            },
            /**
             * 最多显示数量
             * @attribute showMax
             * @type Number
             * @default 15
             */
            showMax: {
                value: 15
            },
            /**
             * 用来显示的对象列表
             * @attribute objList
             * @type Array
             * @default {}
             */
            objList:{
                value: []
            },
            /**
             * 是否记忆原来的选择情况,鼠标离开的时候，回复原来记忆情况
             * @attribute rememberable
             * @type Boolean
             * @default true
             */
            rememberable:{
                value: true
            },
            /**
             * 列表中显示对象中的字段名
             * @attribute listParam
             * @type String
             * @default 'name'
             */
            listParam: {
                value: 'name'
            },
            /**
             * 列表中条目的模板
             * @attribute itemTemplate
             * @type String
             * @default 'name'
             */
            itemTemplate: {
                value: '<li class="{className}"><span>{text}</span></li>'
            },
            /**
             * 列表的模板
             * @attribute listTemplate
             * @type String
             * @default 'name'
             */
            listTemplate: {
                value: '<div class="{cssCompleteList}">' +
                        '<p class="{cssTip}">{msg}</p>'+
                        '<ul class="{cssList}"></ul>'+
                        '<p class="{cssMore}">{more}</p>'+
                        '<ul class="{cssMenuOperator}"></ul>'+
                    '</div>'
            },
            /**
             * 是否自动高亮第1个匹配项
             * @attribute highlightFirstMatch
             * @type Boolean
             * @default false
             */
            highlightFirstMatch: {
                value: false
            },
            /**
             * 用于自动匹配的字段, 默认与listParam相同
             * @attribute searchKey
             * @type String
             * @default ""
             */
            searchKey: {
                value: ''
            },
            /**
             * 匹配模式: fuzzy|start|strict
             * fuzzy:模糊搜索
             * start：首字母开始匹配搜索
             * strict：严格搜索（必须完全匹配才搜得到）
             * none: 不匹配
             * @attribute matchMode
             * @type String
             * @default 'start'
             */
            matchMode: {
                value: 'start'
            },
            /**
             * 显示checkbox的开关
             * @attribute showCheckbox
             * @type Boolean
             * @default false
             */
            showCheckbox: {
                value: false
            },
            /**
             * 多选后是否将多个结果用逗号分开打印在nd里
             * @attribute fillTarget
             * @type boolean
             * @default true
             */
            fillTarget: {
                value: true
            },
            /**
             *  添加至列表下方的可定制的menu，接受的形式如下：
             *    menuItem = [
             *    {
             *        title:'menu 1',
             *        callback: function(){}
             *    },
             *    {
             *        title:'menu 2',
             *        callback: function(){}
             *    },
             *    ...
             *    ]
             *  其中title将显示在界面上，callback为点击title后调用的响应函数。
             * @attribute menuItem
             * @type Array
             */
            menuItem: {
                value: null
            },
            /**
             * 点击选项后调用的回调函数,也是多选checkbox后点击其它地方隐藏此控件时调用的回调函数。
             * @attribute callback
             * @type Function
             */
            callback: {
                value: null
            },
            /**
             * input在默认情况下的宽度
             * @attribute width
             * @type String | Number
             * @default 142
             */
            width: {
                value:  142
            },
            /**
             * 选择后重定向页面的url地址
             * @attribute url
             * @type String
             */
            url: {
                value: ''
            },
            /**
             * 无数据时的提示
             * @attribute noDataTips
             * @type String
             * @default '无数据'
             */
            noDataTips: {
                value: '无数据'
            },
            /**
             * 搜索是匹配list中某些属性，默认与listParam相同
             * @attribute polling
             * @type Array
             */
            polling: {
                value: []
            },
            /**
             * input节点的父级节点，包含hidden input
             * @attribute node
             * @type Node
             */
            node: {
                value: null,
                setter: '_setNode'
            },
            /**
             * 是否有下级级联，传入参数
             * @attribute cascade
             * @type Object
             */
            cascade: {
                value: null
            },
            /**
             * 异步数据获取
             * @attribute dataUrl
             * @type String
             */
            dataUrl: {
                value: null
            },
            /**
             * 外部异步更新数据
             * @attribute action
             * @type String
             */
            action: {
                value: null
            },
            /**
             * 外部异步更新数据时获取参数的结点
             * @attribute queryNode
             * @type String
             */
            queryNode: {
                value: null
            },
            /**
             * 异步搜索
             * @attribute ayncsearch
             * @type Boolean
             */
            ayncsearch: {
                value: false
            },
            /**
             * 是否需要input的placeholder提示
             * @attribute placeholder
             * @type String
             */
            placeholder: {
                value: ''
            },
            /**
             * 默认值选项
             * @attribute defaultItem
             * @type Object
             */
            defaultItem: {
                value: {name: "无", id: "0"}
            },
            /**
             * 禁止手动输入
             * @attribute forbidWrite
             * @type Boolean
             * @default true
             */
            forbidWrite: {
                value: true
            },
            /**
             * 搜索的间隔时间
             * @attribute searchInterval
             * @type Number
             * @default 100
             */
            searchInterval: {
                value: 500
            },
            zIndex: {
                value: 100
            },
            maxShowItemCount: {
                value: 0
            }
        }
    });

    Y.mt.widget.AutoCompleteList = AutoCompleteList;

}, '1.0.0', {
    requires: [
        'mt-base',
        'mt-pinyin',
        'mt-io',
        'w-core',
        'base-build',
        'widget',
        'widget-position',
        'widget-position-align',
        'widget-stack',
        'p-node-placeholder',
        'w-autocomplete/p-cascade',
        'w-autocomplete/p-asyncsearch',
        'node',
        'io',
        'event-valuechange'
    ],
    skinnable: true
});
 
