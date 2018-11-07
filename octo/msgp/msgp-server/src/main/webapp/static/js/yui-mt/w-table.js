/**
 * 通用、排序、异步三种表格控件
 * @module w-table
 */
M.add('w-table', function(Y) {
    Y.namespace("mt.widget");

    var trim = Y.Lang.trim;
    var $Util = Y.mt.util;
    var $CommonTable = null;
    var $SortTable = null;
    var $AjaxTable = null;
    var $N = Y.Node;

    /**
     * 通用表格效果
     * @class CommonTable
     * @namespace mt.widget
     * @constructor
     * @param {Object} params
     * @extends Widget mt.extention.Table
     */
    Y.mt.widget.CommonTable = Y.Base.create('commontable', Y.Widget, [Y.mt.extention.Table], {
        /**
         * 初始化入口
         * @method initializer
         */
        initializer: function(conf) {
            this.bBox = this.get('boundingBox');
            this.cBox = this.get('contentBox');
            this.ndTable = this.get('node');

            this.chooseIndex = null;
            this.rs = this.getDataRows();

            this.render();
        },
        /**
         * 清除handle
         * @method destructor
         */
        destructor: function() {
            $Util.detachHandle(this._handle);
        }
    }, {
        /**
         * @property CSS_PREFIX
         * @type String
         * @static
         */
        CSS_PREFIX : 'widget-table',
        /**
         * @property ATTRS
         * @type Object
         * @static
         */
        ATTRS : {
            /**
             * @attribute node
             * @type Node
             */
            node: {
                value: null
            }
        }
    });

    $CommonTable = Y.mt.widget.CommonTable;

    /**
     * 排序表格
     * @class SortTable
     * @namespace mt.widget
     * @constructor
     * @param {object} params format:{ unsortable:bool, sortInitOrder:'asc'|'desc', applyCommonTableEffect:bool }
     * @extends widget mt.extention.Table
     */
    Y.mt.widget.SortTable = Y.Base.create('sorttable', Y.Widget, [Y.mt.extention.Table], {
        /**
         * 初始化入口
         * @method initializer
         */
        initializer: function(conf) {
            this.bBox = this.get('boundingBox');
            this.cBox = this.get('contentBox');
            this.ndTable = this.get('node');

            this.ps = this.buildParserSet();
            this.rs = this.getDataRows();

            this.tableParams = conf || {};
            //设置叶子th，即rowspan和colspan不大于1的th
            this.setAllLeafTh();

            this.config = this.setConfig();

            this.chooseIndex = null;

            this.render();
        },
        /**
         * @method destructor
         */
        destructor: function() {
        },
        /**
         * @method renderUI
         */
        renderUI: function() {
            this.triggers = this.addControl();
            if(!this.config.paginator) return;
            this.initPaginator(this.config.paginator);
        },
        /**
         * @method bindUI
         */
        bindUI: function() {
            var _this = this;
            this.after("refreshChange", function(e){
                this.refreshSortTable();
                if(!_this.config.paginator) return;
                this.refreshPaginator();
            });
        },
        /**
         * 异步更新表格数据
         * @method syncTableContent
         * @param {Number} index
         * @private
         */
        syncTableContent: function(index) {
            var config = this.config;
            var _this = this;
            var rowConf = this.config[index];

            //改变头部排序类型标志
            rowConf.sortOrder = this.changeSortOrder(index);
            var orderInfo = {
                sortType: rowConf.sortOrder,
                orderName: rowConf.name
            };

            //异步回来的page信息
            if (config.syncPage) Y.mix(config.syncPage, orderInfo, true);
            this.getTableData(config.url, config.syncPage);

        }
    }, {
        /**
         * @property CSS_PREFIX
         * @type String
         * @static
         */
        CSS_PREFIX : 'widget-table',
        /**
         * @property ATTRS
         * @type Object
         * @static
         */
        ATTRS : {
            /**
             * @attribute node
             * @type Node
             */
            node: {
                value: null
            },
            /**
             * 各列是否默认为可排序
             * @attribute unsortable
             * @type Boolean
             * @default false
             */
            unsortable: {
                value: false
            },
            /**
             * 默认初始化排序方式
             * @attribute sortInitOrder
             * @type String
             * @default 'asc'
             */
            sortInitOrder: {
                value: 'asc'
            },
            /**
             * 格式化每一列数据
             * @attribute colFormat
             * @type Function
             */
            colFormat: {
                value: null
            },
            /**
             * 排序范围
             * @attribute sortScope
             * @type String
             * @default 'part'
             */
            sortScope: {
                value: 'part'
            },
            /**
             * 异步请求数据的方法，默认POST
             * @attribute sourceMethod
             * @type String
             * @default 'POST'
             */
            sourceMethod: {
                value: 'POST'
            }
        }
    });


    $SortTable = Y.mt.widget.SortTable;

    /**
     * 动态表格头部初始化 异步数据初始化表格生成的对象中，table属性保存了对表格的引用
     * @class AjaxTable
     * @namespace mt.widget
     * @constructor
     * @param {Object} params 配置信息
     * @extends Widget mt.extention.Table
     */
    Y.mt.widget.AjaxTable = Y.Base.create('ajaxtable', Y.Widget, [Y.mt.extention.Table], {
        /**
         * 初始化入口
         * @method initializer
         */
        initializer: function(conf) {
            this.bBox = this.get('boundingBox');
            this.cBox = this.get('contentBox');

            this.ps = this.buildParserSet();
            this.tableParams = conf;

            this.ndContainer = Y.one(this.get('container'));
            this.ndTable = null;

            this.chooseIndex = null;
            this.render();
        },
        /**
         * @method destructor
         */
        destructor: function() {
        },
        /**
         * @method renderUI
         */
        renderUI: function() {
            //table初始化
            this.ndTable = this.initTableHeader();
            //重新设置thParam，如果存在嵌套表头，则只有叶子th是需要的
            this._resetThParam();
            //设置所有叶子TH
            this.setAllLeafTh();

            this.ndContainer.setHTML("");
            this.ndContainer.appendChild(this.ndTable);

            this.rs = this.getDataRows();

            //配置参数
            this.config = this.setConfig();

            //请求异步数据初始表格内容
            this.initTableContent();

            //若有设置显示checkbox列，则显示
            //TODO checkbox插件
            if (this.get("checkboxColumn")) {
                this.plug(Y.mt.plugin.Table);
                var tablePlugin = this["plugin-table"];
                tablePlugin.insertCheckboxCol(this.config.container, this.get("checkboxColumn"));
                tablePlugin.addToolbar(this);
            }

        },
        /**
         * @method bindUI
         */
        bindUI: function() {
            this.triggers = this.addControl();
            var _this = this;
            this.after("refreshChange", function(e){
                this.refreshSortTable();
                if(!_this.config.paginator) return;
                this.refreshPaginator();
            });
        },
        /**
         * 异步更新table内容
         * @method syncTableContent
         * @param {Number} index
         * @private
         */
        syncTableContent: function(index) {
            var config = this.config;
            var _this = this;
            var rowConf = this.config[index];

            //改变头部排序类型标志
            rowConf.sortOrder = this.changeSortOrder(index);

            var orderInfo = {
                sortType: rowConf.sortOrder,
                orderName: rowConf.name
            };

            //异步回来的page信息
            if (config.syncPage) Y.mix(config.syncPage, orderInfo, true);
            this.getTableData(config.url, config.syncPage);

        },
        /**
         * 异步获取数据并刷新表格
         * @method initTableContent
         * @private
         */
        initTableContent: function() {
            //异步获取数据并刷新表格
            if (this.config.url) {
                this.getTableData(this.config.url, this.config.syncPage);
            }
        },
        /**
         * 重新设置thParam，如果存在嵌套表头，则只有叶子th是需要的
         * @private
         * @method _resetThParam
         */
        _resetThParam: function() {
            var config = this.tableParams;
            //目前仅新表头配置方式支持多表头
            if (config.title) {
                return;
            }
            config.thParams = this._getAllLeafThParam(config.thParams);
            this.set("thParams", config.thParams);
        },
        /**
         * 初始化表格头部
         * @method initTableHeader
         * @return {Node} ndTable
         * @private
         */
        initTableHeader: function() {
            //初始化表格头部
            var config = this.tableParams, ths = [];
            var ndTable = Y.Node.create(Y.Lang.sub($TEMPLATE.table, {
                tableClass: this.get("tableClass")
            }));

            var ndThead = ndTable.one("thead");
            var tr = Y.one(document.createElement('tr'));
            tr.addClass("widget-table-thead");

            var frag;
            if(config.title) {
                for(var p in config.title){
                    if (config.title.hasOwnProperty(p)) {
                        var item = config.title[p];
                        var th = Y.one(document.createElement('th'));
                        th.setHTML(item);
                        if (config.thParams && config.thParams[p]) {
                            var thParam = config.thParams[p];
                            if (thParam.styles) th.setStyles(thParam.styles);
                            if (thParam.width) th.setStyle("width", thParam.width + 'px');
                            if (thParam.className) th.addClass(thParam.className);
                            th.setData('name', p);
                        }
                        ths.push(th);
                    }
                }
                var nl = new Y.NodeList(ths);
                frag = nl.toFrag();
                tr.append(frag);
                ndThead.append(tr);
            } else {
                //根据thParams生成Header
                frag = this._createThByParam(config.thParams);
                ndThead.append(frag);
            }
            return ndTable;
        },
        /**
         * 添加一行
         * @private
         * @method addRow
         * @param {Node} row
         */
        addRow: function(row) {
            this["plugin-table"]._addRow(row);
        },
        /**
         * 添加一行
         * @private
         * @method addRow
         * @param {Node} row
         */
        modifyRow: function(row) {
            this["plugin-table"]._modifyRow(row);
        },
        /**
         * 删除一行
         * @private
         * @method deleteRow
         * @param {Node} row
         */
        deleteRow: function(row) {
            this["plugin-table"]._deleteRow(row);
        },
        /**
         * 获取所有被选中的行
         * @private
         * @method getSelectedRow
         */
        getSelectedRow: function() {
            return this["plugin-table"].getSelectedRow();
        },
        /**
         * 生成th
         * @private
         * @method _createThByParam
         */
        _createThByParam: function(thParams) {
            var array = this._createThArray(thParams);
            var trBox = [];
            for (var i = 0; i < array.length; i++) {
                var rowParam = array[i];
                var ndTr = $N.create("<tr></tr>");
                var ths = [];
                //创建行
                for (var j = 0; j < rowParam.length; j++) {
                    var thParam = rowParam[j];
                    if (thParam.hide) {
                        continue;
                    }
                    var ndTh = $N.create("<th>"+thParam.label+"</th>");
                    if (thParam.styles) ndTh.setStyles(thParam.styles);
                    if (thParam.width) ndTh.setStyle("width", thParam.width + 'px');
                    if (thParam.className) ndTh.addClass(thParam.className);
                    //针对跨行的th设置colspan
                    this._setColspan(ndTh, thParam);
                    //针对非最后一行的叶子节点设置rowspan
                    this._setRowspanExceptLastRow(ndTh, thParam, array, i);
                    ndTh.setData('name', thParam.name);
                    ths.push(ndTh);
                }
                ndTr.append(new Y.NodeList(ths).toFrag());
                trBox.push(ndTr);
            }
            var nl = new Y.NodeList(trBox);
            return nl.toFrag();
        },
        /**
         * 针对跨行的th设置colspan
         * @method _setColspan
         */
        _setColspan: function(ndTh, thParam) {
            var colspan = this._getAllLeafThParam(thParam.children).length;
            if (colspan > 1) {
                ndTh.setAttribute("colspan", colspan);
                ndTh.setStyle("textAlign", "center");
            }
        },
        /**
         * 针对非最后一行的叶子节点设置rowspan
         * @method _setRowspanExceptLastRow
         */
        _setRowspanExceptLastRow: function(ndTh, thParam, array, rowIndex, colIndex) {
            if (!thParam.children || thParam.children.length === 0) {
                if (rowIndex < array.length-1) {
                    ndTh.setAttribute("rowspan", array.length);
                }
            }
        },
        /**
         * 将所有子节点转为二维数组
         * @method _createThArray
         */
        _createThArray: function(params) {
            var array = [];
            //取所有的子节点
            var sub = [];
            for (var i = 0; i < params.length; i++) {
                var param = params[i];
                if (param.children && param.children.length > 0) {
                    sub = sub.concat(this._createThArray(param.children));
                }
            }
            //合并为一维数组
            sub = this._concatArray(sub);
            //统一加入到二维数组
            array.push(params);
            if (sub.length > 0) {
                array.push(sub);
            }
            return array;
        },
        /**
         * 合并二维数组
         * @method _concatArray
         */
        _concatArray: function(sub) {
            var temp=[];
            for (var i = 0; i < sub.length; i++) {
                var s = sub[i];
                temp = temp.concat(s);
            }
            return temp;
        },

        /**
         * 获取所有叶子节点
         * @method _getAllLeafThParam
         */
        _getAllLeafThParam: function(params) {
            if (!params) {
                return [];
            }
            var array = [];
            for (var j = 0; j < params.length; ++j) {
                var param = params[j];
                if (param.children && param.children.length > 0) {
                    array = array.concat(this._getAllLeafThParam(param.children));
                } else {
                    array.push(param);
                }
            }
            return array;
        }
    }, {
        /**
         * @property CSS_PREFIX
         * @type String
         * @static
         */
        CSS_PREFIX : 'widget-table',
        /**
         * @property ATTRS
         * @type Object
         * @static
         */
        TEMPLATE: {
            table: '<table class="{tableClass}">'+
                        '<thead></thead>'+
                        '<tbody></tbody>'+
                    '</table>'
        },
        ATTRS : {
            /**
             * @attribute node
             * @type Node
             */
            node: {
                value: null
            },
            /**
             * 装载表格的容器
             * @attribute container
             * @type Node
             */
            container: {
                value: null
            },
            /**
             * @attribute node
             * @type Node
             */
            tableClass: {
                value: 'common-table'
            },
            /**
             * 各列是否默认为可排序
             * @attribute unsortable
             * @type Boolean
             * @default false
             */
            unsortable: {
                value: false
            },
            /**
             * 各列是否默认为可排序
             * @attribute checkboxColumn
             * @type Object
             * @default {}
             */
            checkboxColumn: {
                value: false
            },
            /**
             * 默认初始化排序方式
             * @attribute thParams
             * @type Array
             */
            thParams: {
                value: null
            },
            /**
             * 默认初始化排序方式
             * @attribute title
             * @type Object
             */
            title: {
                value: null
            },
            /**
             * 默认初始化排序方式
             * @attribute sortInitOrder
             * @type String
             * @default 'asc'
             */
            sortInitOrder: {
                value: 'asc'
            },
            /**
             * 排序范围
             * @attribute sortScope
             * @type String
             * @default 'part'
             */
            sortScope: {
                value: 'part'
            },
            /**
             * 格式化每一列数据
             * @attribute colFormat
             * @type Function
             */
            colFormat: {
                value: null
            },
            /**
             * 整体格式化数据
             * @attribute dataFormat
             * @type Function
             */
            dataFormat: {
                value: null
            },
            /**
             * 工具栏
             * @attribute toobar
             * @type Array
             */
            toolbar: {
                value: []
            },
            /**
             * 刷新flag
             * @attribute refresh
             * @type Boolean
             * @default true
             */
            refresh: {
                value: true
            },
            /**
             * 异步请求数据的方法，默认POST
             * @attribute sourceMethod
             * @type String
             * @default 'POST'
             */
            sourceMethod: {
                value: 'POST'
            }
        }
    });

    $AjaxTable = Y.mt.widget.AjaxTable;
    var $TEMPLATE = $AjaxTable.TEMPLATE;

}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'widget',
        'w-table/p-table',
        'e-table',
        'node'
    ],
    skinnable: true
});
 
