/*jshint camelcase:false*/
/**
 * @module e-table
 */
M.add('e-table', function(Y){
    var trim = Y.Lang.trim;
    var $Table = null;
    var $N = Y.Node;
    Y.namespace('mt.extention');

    //扩展类构造器方法优于需要扩展的类的初始化方法执行
    Y.mt.extention.Table = function() {

        this.recordPrefix = "record_";
        this._handle = {
            trMouseOut: null,
            trMouseOver: null,
            docClick: null,
            trClick:null
        };

        Y.after(this._renderTableUI, this, 'renderUI');
        Y.after(this._bindTableUI, this, 'bindUI');
        Y.after(this._afterCellStyleSet, this, 'setCellStyle');
        Y.after(this._afterRowsCLassAdd, this, 'addRowsClass');

        //动态同步row的Style(用于异步分页和排序)
        Y.after(this._syncCellStyle, this, 'refreshSortTable');
        Y.after(this._syncCellStyle, this, 'refresh');
        Y.after(this._syncCellStyle, this, 'refreshPaginator');

        //动态同步row的class(用于异步分页和排序)
        Y.after(this._syncRowsClass, this, 'refreshSortTable');
        Y.after(this._syncRowsClass, this, 'refresh');
        Y.after(this._syncRowsClass, this, 'refreshPaginator');

        //刷新table的样式
        Y.after(this.refreshCommonTable, this, 'changeTableContent');
    };

    Y.mt.extention.Table.ATTRS = {
        enableHoverEffect: {
            value: true
        },
        enableOddEffect: {
            value: true
        },
        enableClickEffect: {
            value: true
        },
        enableMultiChooseEffect: {
            value: true
        },
        applyCommonTableEffect: {
            value: true
        },
        syncRowsClass: {
            value: []
        },
        syncStyles: {
            value: []
        },
        noDataTips: {
            value: '表格无数据'
        },
        paginator: {
            value: null
        },
        showHeader: {
            value: 'true'
        }
    };

    Y.mt.extention.Table.CLASS_NAME = {
        css_hover: 'widget-table-hover',
        css_odd: 'widget-table-odd',
        css_choose: 'widget-table-choose',
        css_headerbg: 'sorttable-header-bg',
        css_headerdesc: 'sorttable-header-desc',
        css_headerasc: 'sorttable-header-asc',
        css_icon_sort: 'fa fa-sort',
        css_icon_sort_down: 'fa fa-sort-asc',
        css_icon_sort_up: 'fa fa-sort-desc',
        css_tips: 'widget-table-tips',
        css_noSort: 'no-sort'
    };

    Y.mt.extention.Table.prototype = {
        //推断表格类型
        _renderTableUI: function() {
            if(!this.get('applyCommonTableEffect')) return;

            this.enableClickEffect = this.get('enableClickEffect');
            this.enableHoverEffect = this.get('enableHoverEffect');
            this.enableOddEffect = this.get('enableOddEffect');

            if (this.enableOddEffect) {
                this.addOddEffect();
            }
            var container = this.ndContainer ? this.ndContainer : this.ndTable;

            //将table放入页面指定的位置, 即tableContainer的位置
            container.insert(this.bBox, 'after');

            //初始化表格tips容器
            this.tips = this.createTips("");
            container.append(this.tips);

            this.cBox.append(container);
        },
        _bindTableUI: function() {
            if(!this.get('applyCommonTableEffect')) return;

            if (this.enableHoverEffect) {
                this.addHoverEffect();
            }
            if (this.enableClickEffect) {
                this.addClickEffect();
                this.addClickListener();
            }
        },
        //猜测table的所有列的paser
        guessAllRowsParser: function() {
            var _this = this;

            this.ndTable.get('rows').item(0).get('cells').each( function(item, i) {
                if (_this.get("checkboxColumn")) {
                    if (i === 0) {
                        return;
                    } else {
                        i = i-1;
                    }
                }
                var parser = _this.setSortParser(_this.config, i);
                _this.config[i].sortParser = parser;
            });
        },
        //猜测每列的数据内容的内容，用每列从上到下第一个有有用内容的值作为猜测的依据
        guessParser: function(index) {
            if(!this.rs) return false;

            var _this = this;
            var gp = this.getParserByName('string');
            var i = 0, j;
            //用于保存第一个index列中有用内容
            var colData = null;
            var invalidReg = /^([-\.\/\\\$%]+)|(N\/A)$/g;
            while (!colData && i < this.rs.length) {

                var nlTd = this.rs[i].get('cells');
                var ndTd = _this.get("checkboxColumn") ? nlTd.item(index+1): nlTd.item(index);
                colData = trim(ndTd.get('text'));
                // 剔除一些无用字符
                colData = invalidReg.test(colData) ? null : colData;
                i++;
            }
            //如果全部都是无用字符, 默认类型为string
            if (!colData) return gp;

            for (j = 0; j < this.ps.parsers.length; ++j) {
                var parser = this.ps.parsers[j];
                if (parser.is(colData)) {
                    gp = parser;
                    break;
                }
            }
            return gp;
        },
        // 添加隔行效果
        addOddEffect: function() {
            var css_odd = $Table.CLASS_NAME.css_odd;
            for (var i = 0; i < this.rs.length; ++i) {
                var row = this.rs[i];
                var hasClass = row.hasClass(css_odd);
                if (i % 2 && hasClass) {
                    row.removeClass(css_odd);
                } else if (i % 2 === 0 && !hasClass){
                    row.addClass(css_odd);
                }
            }
        },
        // 添加点击行效果
        addClickEffect: function() {
            var css_choose = $Table.CLASS_NAME.css_choose;
            var css_hover = $Table.CLASS_NAME.css_hover;
            var _this = this;

            this._handle.trClick = this.ndTable.delegate('click', function() {

                var currentIndex = this.getData('index');
                if (currentIndex < 0) return;
                if (currentIndex !== _this.chooseIndex) {
                    // 点击不同行
                    if (_this.chooseIndex !== null) {
                        _this.rs[_this.chooseIndex].removeClass(css_choose);
                    }
                    this.replaceClass(css_hover, css_choose);
                    _this.chooseIndex = currentIndex;
                } else {
                    // 点击同一行
                    this.addClass(css_choose);
                }
            }, 'tbody tr');
        },
        // 添加多行选择效果
        addMultiChooseEffect: function() {
            // todo
        },
        //获得当前选中行
        currentSelectedRow: function() {
            return this.ndTable.one('tr.choose');
        },
        // 选中从索引为from到to的所有数据行
        addChooseStatus: function(from, to) {
            var css_choose = $Table.CLASS_NAME.css_choose;
            var row;
            if (!from) return;
            if (!to) {
                row = this.rs[from];
                if (!row) return;
                row.addClass(css_choose);
            } else {
                if (from > to) return;
                for (var i = from; i <= to; ++i) {
                    row = this.rs[i];
                    if (!row) return;
                    row.addClass(css_choose);
                }
            }
        },
        // 添加悬浮行效果
        addHoverEffect: function() {
            var css_hover = $Table.CLASS_NAME.css_hover;
            var css_choose = $Table.CLASS_NAME.css_choose;
            this._handle.trMouseOver = this.ndTable.delegate('mouseover', function() {
                if (this.getData('index') >= 0 && !this.hasClass(css_choose)) {
                    this.addClass(css_hover);
                }
            }, 'tr');
            this._handle.trMouseOut = this.ndTable.delegate('mouseout', function() {
                this.removeClass(css_hover);
            }, 'tr');
        },
        // 监听点击事件，如在table外则取消选中效果
        addClickListener: function() {
            var _this = this;
            this._handle.docClick = Y.one(document).on('click', function(e) {
                var region = _this.ndTable.get('region');
                var pos = e.target.getXY();
                if (!(pos[0] >= region.left && pos[0] <= region.right && pos[1] >= region.top && pos[1] <= region.bottom)) {
                    _this.removeChooseStatus();
                }
            });
        },
        // 去除已选中状态
        removeChooseStatus: function() {
            for (var i = 0; i < this.rs.length; ++i) {
                var row = this.rs[i];
                row.removeClass($Table.CLASS_NAME.css_choose);
            }
        },
        // 保存页面初始化时设置的表格样式， 为异步分页和查询刷新时使用
        _afterCellStyleSet: function(style, fn, type) {
            var syncStyle = { style: style, fn: fn, type: type};
            this.get('syncStyles').push(syncStyle);
        },
        //动态表格样式化
        _syncCellStyle: function() {
            var _this = this;
            var syncStyles = this.get('syncStyles');
            if (syncStyles.length > 0 ) {
                Y.Array.each(syncStyles, function(item) {
                    _this.setCellStyle(item.style, item.fn, item.type);
                });
            }
        },
        // 保存页面初始化时设置的表格某行的className， 为异步分页和查询刷新时使用
        _afterRowsCLassAdd: function(className, fn) {
            var syncRow = { className: className, fn: fn};
            this.get('syncRowsClass').push(syncRow);
        },
        //动态表格样式化
        _syncRowsClass: function() {
            var _this = this;
            var syncRows = this.get('syncRowsClass');
            if (syncRows.length > 0 ) {
                Y.Array.each(syncRows, function(item) {
                    _this.addRowsClass(item.className, item.fn);
                });
            }
        },
        /**
         * @description 设置表格某列某行或某格的样式
         * @param String type row、column, cell(default)
         * @param fn test 校验当前type是否符合需要设置样式的需求
         */
        setCellStyle: function(styles, fn, type) {
            var rows = this.ndTable.get('rows');
            var _this = this;
            var index = [];
            if(!type) type = 'cell';

            //计算有哪些列需要设置样式
            if (type === 'column') {
                var firstRowCells = rows.item(0).get('cells');
                firstRowCells.each( function(cell, i) {
                    if (fn(cell, i)) index.push(i);
                });
            }
            rows.each( function(row) {
                if (type === 'row') {
                    //设置行的样式
                    _this.setStyles(row, styles, fn);
                } else {
                    var cells = row.get('cells');
                    if (type === 'column') {
                        //设置列的样式
                        Y.Array.each(index, function(i) {
                            cells.item(i).setStyles(styles);
                        });
                    } else {
                        cells.each( function(cell) {
                            //设置单元格样式
                            _this.setStyles(cell, styles, fn);
                        });
                    }
                }
            });
        },
        //添加不排序的标记
        addNoSortFlag: function(fn) {
            this.addRowsClass($Table.CLASS_NAME.css_noSort, fn);
        },
        //添加表格某行的css类
        addRowsClass: function(className, fn) {
            var rows = this.ndTable.get('rows');
            rows.each( function(row, i) {
                //传给函数当前的row, 当前索引, 所有行的长度
                if(fn(row, i, rows.size())) {
                    row.addClass(className);
                }
            });
        },
        /**
         * @description 设置表格节点样式
         */
        setStyles: function(nd, styles, fn) {
            if(fn(nd) && !Y.Object.isEmpty(styles)) {
                if(nd.get('tagName') === 'TR') {
                    nd.get('cells').setStyles(styles);
                } else {
                    nd.setStyles(styles);
                }
            }
        },
        //commonTable重置
        refreshCommonTable: function() {
            if (this.enableOddEffect) {
                this.rs = this.getDataRows();
                this.addOddEffect();
            }
            if (this.enableClickEffect) {
                this.removeChooseStatus();
            }
            this.chooseIndex = null;
        },
        // 获取数据行数, 除去表头
        getDataRows: function() {
            var dr = [];
            dr.sortRows = [];
            dr.commonRows = [];
            if (this.ndTable) {
                var hasThead = !!this.ndTable.one('thead');
                //不处理table嵌套的情况
                this.ndTable.all(">tbody >tr").each( function(row, i) {
                    var index;
                    if (hasThead) {
                        index = i;
                    } else {
                        index = i-1;
                    }
                    row.setData('index', index);
                    //判断是否符合是数据行而不是表头
                    var shouldPush = hasThead ? true : i > 0;
                    if (shouldPush) {
                        if (row.hasClass('no-sort')) {
                            dr.commonRows.push(row);
                        } else {
                            dr.sortRows.push(row);
                        }
                        dr.push(row);
                    }
                });
            }
            return dr;
        },
        // 由name获得parser
        getParserByName: function(name){
            var p = null;
            if (name) {
                for (var i = 0; i < this.ps.parsers.length; ++i) {
                    var parser = this.ps.parsers[i];
                    if (parser.name === name.toLowerCase()) {
                        p = parser;
                        break;
                    }
                }
            }
            return p;
        },
        // 添加触发器
        addControl: function() {
            var leafRow = this.leafTh;
            var triggerList = [];
            var _this = this;

            if (leafRow) {
                for (var index = 0; index < leafRow.length; index++) {
                    var item = leafRow[index];
                    item.setData('index', index);
                    if (!_this.config[index].unsortable) {
                        item.addClass($Table.CLASS_NAME.css_headerbg);
                        item.append("<i class='"+$Table.CLASS_NAME.css_icon_sort+"'></i>");
                        triggerList[index] = item;
                        //绑定排序事件
                        _this._bindSortEvent(item, index);
                    }
                }
            }
            return triggerList;
        },
        //绑定排序事件
        _bindSortEvent: function(item, index) {
            var _this = this;
            item.on('click', function(e) {
                Y.Global.fire('SortTable:click', { sortTable: _this, event: e, index: index });
                _this.update(this.getData('index'));
                Y.Global.fire('SortTable:update', { sortTable: _this, event: e, index: index, sortOrder: _this.config[index].sortOrder });
                _this._columnSortedEffect(index);
            });
        },
        //添加排序列的效果
        _columnSortedEffect: function (index) {
            var nlTr = this.ndTable.all("tbody > tr");
            var _this = this;
            nlTr.each(function (item) {
                var nlTd = item.all("td");
                var ndTdSorted;
                nlTd.removeClass("table-column-sorted");
                if (_this.get("checkboxColumn")) {
                    ndTdSorted = nlTd.item(index+1);
                } else {
                    ndTdSorted = nlTd.item(index);
                }
                ndTdSorted.addClass("table-column-sorted");
            });
        },
        update: function(index) {
            this.updateRows(index);
            this.updateControl(index);
        },
        // 使用排序的行组更新表格
        updateRows: function(index) {
            //支持异步全局排序
            if(this.get('sortScope') && this.get('sortScope') === "global") {
                this.syncTableContent(index);
            } else {
                this.changeTableContent(index);
            }
        },
        /**
         * 获得所有叶子TH元素
         * @private
         * @method
         * @param {}
         * @return {}
         */
        setAllLeafTh: function() {
            var nlThs = this.ndTable.all("tr th");
            var leafTh = [];
            nlThs.each(function(item) {
                if (item.ancestor("tr").hasClass('no-sort')) {
                    return;
                }
                var colspan = item.getAttribute("colspan");
                var rowspan = item.getAttribute("rowspan");
                if ((!colspan || colspan < 2) && (!rowspan || rowspan < 2)) {
                    leafTh.push(item);
                }
            });
            this.leafTh = leafTh;
        },
        // 设定config
        setConfig: function() {
            var i, conf = Y.clone(this.tableParams);

            conf = this.setPaginatorConfig(conf);

            // 计算列数
            var thCells = this.leafTh;
            var ndTh, param;
            for (i = 0; i < thCells.length; ++i) {
                ndTh = thCells[i];
                // 标题th中data-params格式：{ unsortable:boolean, parser:string, order:'asc'|'desc' }
                var name = ndTh.getData('name');
                var thParams = this.getColumnDataByName(conf, name);
                param = Y.mt.util.decodeJSON(ndTh.getAttribute('data-params')) || thParams || {};
                conf[i] = param;
                param.unsortable = this.isSortHeader(param);
                param.sortParser = this.setSortParser(conf, i);
                param.sortOrder = param.order || this.get('sortInitOrder');
                param.name = param.name || name || "";
            }
            // 当前排序列索引
            conf.sortCurrentCol = null;
            return conf;
        },
        //根据列的name，获取其所代表thParam值
        getColumnDataByName: function(conf, name) {
            var thParams = conf.thParams;
            if (!this.get("title")) {
                if (!thParams) {
                    //sorttable
                    return null;
                }
                //ajaxtable
                for (var i = 0; i <thParams.length; i++) {
                    var thp = thParams[i];
                    if (thp.name === name) {
                        return thp;
                    }
                }
            } else {
                if (conf.thParams) {
                    return conf.thParams[name];
                }
            }
            return null;
        },
        isSortHeader: function(thParam) {
            if(thParam.unsortable)  {
                return thParam.unsortable;
            } else {
                return (thParam.unsortable === false) ? thParam.unsortable : this.get('unsortable');
            }
        },
        setSortParser: function(conf, i) {
            var parserName = conf[i] && conf[i].parser;
            return parserName ? this.getParserByName(parserName) : this.guessParser(i);
        },
        // 更新控制th样式
        updateControl: function(index) {
            var config = this.config;
            var css_headerbg = $Table.CLASS_NAME.css_headerbg;
            var css_headerasc = $Table.CLASS_NAME.css_headerasc;
            var css_headerdesc = $Table.CLASS_NAME.css_headerdesc;
            var css_icon_sort = $Table.CLASS_NAME.css_icon_sort;
            var css_icon_sort_up = $Table.CLASS_NAME.css_icon_sort_up;
            var css_icon_sort_down = $Table.CLASS_NAME.css_icon_sort_down;

            if (index !== this.config.sortCurrentCol) {
                if (config.sortCurrentCol !== null) {
                    var oldTrigger = this.triggers[config.sortCurrentCol];
                    oldTrigger.removeClass(css_headerasc);
                    oldTrigger.removeClass(css_headerdesc);
                    oldTrigger.addClass(css_headerbg);
                    oldTrigger.one("i").removeClass(css_icon_sort_up);
                    oldTrigger.one("i").removeClass(css_icon_sort_down);
                    oldTrigger.one("i").addClass(css_icon_sort);
                }
                config.sortCurrentCol = index;
            }

            var trigger = this.triggers[index];

            switch (config[index].sortOrder) {
            case 'desc':
                trigger.removeClass(css_headerbg);
                trigger.removeClass(css_headerasc);
                trigger.addClass(css_headerdesc);
                trigger.one("i").removeClass(css_icon_sort);
                trigger.one("i").removeClass(css_icon_sort_up);
                trigger.one("i").addClass(css_icon_sort_down);
                break;
            case 'asc':
                trigger.removeClass(css_headerbg);
                trigger.removeClass(css_headerdesc);
                trigger.addClass(css_headerasc);
                trigger.one("i").removeClass(css_icon_sort);
                trigger.one("i").removeClass(css_icon_sort_down);
                trigger.one("i").addClass(css_icon_sort_up);
                break;
            default:
                break;
            }
        },
        //将数据格式化为table的格式
        _formatDataAuto: function(data) {
            var arr = [];
            if (!data || data.length === 0) {
                return arr;
            }
            var thParams = this.config.thParams;
            if (!thParams) {
                //获取thParams
                thParams = this._getThParams();
            }
            for (var i = 0; i < data.length; ++i) {
                var d = data[i];
                //根据列的配置，依次填充对应的值
                var columnValue = this._getColumnValueByThParam(d, thParams);
                arr.push(columnValue);
            }
            return arr;
        },
        //获取thParams
        _getThParams: function () {
            var nlThead = this.bBox.all("th[data-params]");
            var thParams = [];
            nlThead.each(function (ndTd) {
                var dataParam = ndTd.getAttribute("data-params");
                thParams.push(Y.mt.util.decodeJSON(dataParam));
            });
            return thParams;
        },
        //根据列的配置，依次填充对应的值
        _getColumnValueByThParam: function(data, thParams) {
            var td = [];
            for (var i = 0; i < thParams.length; i++) {
                var p = thParams[i];
                //不显示该行
                if (p.hide) {
                    continue;
                }
                var value;
                if (typeof this.get("colFormat") === "function") {
                    value = this.get("colFormat").apply(this, [p.name, data[p.name], data, p]);
                } else {
                    value = data[p.name];
                }
                td.push(value);
            }
            return td;
        },
        //格式化数据
        formatDataToTable: function(data) {
            var formatData;
            if (this.config.dataFormat) {
                formatData = this.config.dataFormat.apply(this, [data, this]);
            } else {
                formatData = this._formatDataAuto(data);
            }
            return formatData;
        },
        //更新排序表内容
        refreshSortTable: function() {
            var data = this.config.tableData;
            //给数据添加标识
            this._setAllDataID(data);
            //格式化数据
            var formatData = this.formatDataToTable(data);

            if (!Y.Lang.isArray(formatData)) return;
            //对无数据的表格进行处理
            if (formatData.length === 0 ) {
                this.ndTable.setStyle('marginBottom', 0);
                this.ndTable.all('tr').hide();
                this.ndTable.one('tr').show();
                //显示无数据
                this.createTips(this.get('noDataTips'));
                //是否显示表头
                if(!this.get('showHeader')) {
                    this.ndTable.one('tr').hide();
                }
            } else {
                this.bBox.one('.'+$Table.CLASS_NAME['css_tips']).hide();
                if (this.get("checkboxColumn")) {
                    for (var i = 0; i < formatData.length; i++) {
                        var fdata = formatData[i];
                        fdata.unshift("<input type='checkbox' name='checkbox' />");
                    }
                }
                this.fillTable(formatData);
                this.ndTable.setStyle('marginBottom', '10px');
                //如果为当前页面排序, 根据异步数据更新parser
                if (this.get("sortScope") === "part" ) {
                    this.guessAllRowsParser();
                }
            }
            //如果需要在刷新后进行回调
            if (this.config.callback) this.config.callback.call(this);
        },
        /**
         * 将record转换为对应的tr添加到table中
         * @private
         * @method _createRowWithRecord
         * @param {Object} record
         * @return {}
         */
        _createRowWithRecord: function(ndTable, record, recordIndex) {
            var newTr;
            if(record.length > 0 ) {
                newTr = $N.create("<tr></tr>");
                Y.Array.each(record, function(item) {
                    var newTd = $N.create("<td></td>");
                    newTd.setHTML(item);
                    newTr.appendChild(newTd);
                });
            }
            //给每条记录添加ID
            this._addRecordId(newTr, recordIndex);
            return newTr;
        },
        //根据格式化的表格数据重新绘制表格
        fillTable: function(formatData) {
            var _this = this;
            var ndTable = this.ndTable;
            if (ndTable.one('thead')) {
                ndTable.one('tbody').setHTML('');
            } else {
                ndTable.one('tbody').setHTML(ndTable.one('tr'));
            }
            var frag = [];
            Y.Array.each(formatData, function(record, i) {
                // 将record转换为对应的tr添加到table中
                var newTr = _this._createRowWithRecord(ndTable, record, i);
                frag.push(newTr);
            });
            var nl = new Y.NodeList(frag);
            ndTable.one('tbody').append(nl.toFrag());

            this.refreshCommonTable();

            //触发refresh事件
            Y.fire('AjaxTable.refresh', { ndTable:ndTable, values: formatData.values });
        },
        /**
         * 给每条记录添加ID
         * @private
         * @method _addRecordId
         * @param {}
         * @return {}
         */
        _addRecordId: function(ndTr, index) {
            ndTr.setAttribute("recordId", this.recordPrefix+index);
        },
        //开放给外部的freshTable
        refresh: function(conf) {
            if (conf) {
                if (conf.query) {
                    Y.mix(this.config.syncPage, conf.query, true);
                }
                // 有时需要切换数据源，但表格结构不变
                if (conf.url) {
                    this.config.url = conf.url;
                }
            }
            this.getTableData(this.config.url, this.config.syncPage);
        },
        //无数据是显示
        createTips: function(msg) {
            var tips = this.cBox.one('.'+ $Table.CLASS_NAME['css_tips']);
            if (!tips) {
                tips = Y.Node.create('<div class="' + $Table.CLASS_NAME["css_tips"] + '"></div>');
            }
            if (msg && msg !== "") {
                tips.setHTML(msg).show();
            } else {
                tips.hide();
            }
            return tips;
        },
        //更新sortOrder
        changeSortOrder: function(index) {
            return this.config[index].sortOrder === 'asc' ? 'desc' : 'asc';
        },
        //初始化分页控件
        initPaginator: function(page) {
            var _this = this;
            if(!page) return;
            //支持分页
            var paginator = new Y.mt.widget.Paginator({
                contentBox: '#'+ page.id,
                max: parseInt(page.totalPageCount, 10) || 1,
                index: parseInt(page.pageNo, 10) || 1,
                totalCount: parseInt(page.totalCount, 10) || 0,
                page: parseInt(page.page, 10) || parseInt(page.totalCount, 10) || 1,
                step: page.step || 5,
                jump: page.hasOwnProperty("jump") ? page.jump : true,
                pageSize: parseInt(page.pageSize, 10) || 20,
                showPageSize: page.showPageSize || false,
                //翻页前的自定义处理
                beforeGoPage: page.beforeGoPage,
                skin: page.skin,
                autoHide: page.autoHide,
                callback: function(page) {
                    page.pageNo = page.page;
                    page.pageSize = page.pageSize;
                    page.totalPageCount = page.max;
                    delete page.page;
                    Y.mix(_this.config.syncPage, page, true);

                    //加入外部Paginator自定义回调方法
                    if (_this.config.paginator.callback) {
                        _this.config.paginator.callback.apply(_this, [_this.config.url, _this.config.syncPage, paginator]);
                        return;
                    }
                    _this.getTableData(_this.config.url, _this.config.syncPage);
                }
            });
        },
        //刷新分页条
        refreshPaginator: function() {
            var tableData = this.config.tableData;
            if (tableData && tableData.length === 0) {
                var paginator = Y.one('#' + this.config.paginator['id']);
                if (paginator) paginator.setHTML("");
                return;
            }
            var page = Y.mix({id: this.config.paginator.id}, this.config.syncPage, true);
            this.initPaginator(page);
        },
        //分页参数设置
        setPaginatorConfig: function(conf) {
            //存放异步请求时的参数
            conf.syncPage = {};
            if (conf.paginator && !conf.paginator.pageNo) conf.paginator.pageNo = 1;
            //请求查询参数
            if (conf.query) Y.mix(conf.syncPage, conf.query, true);
            //分页参数
            if (conf.paginator) Y.mix(conf.syncPage, conf.paginator, true);
            if (conf.clearQuery) {
                delete conf.syncPage.id;
            }
            return conf;
        },
        //对当前表格排序使内容交换
        changeTableContent: function(index) {
            var config = this.config,
                parser = config[index].sortParser,
                order = config[index].sortOrder,
                _this = this;

             // 更新sortOrder
            config[index].sortOrder = order === 'asc' ? 'desc' : 'asc';
            order = config[index].sortOrder;

            // 按正序排序
            this.rs['sortRows'].sort( function(a, b) {
                var cellIndex = _this.get("checkboxColumn") ? index + 1 : index;
                var x = trim(a.get('cells').item(cellIndex).get('text'));
                var y = trim(b.get('cells').item(cellIndex).get('text'));
                return _this.sortFun(x, y, parser);
            });

            if (order === 'desc') {
                this.rs['sortRows'] = this.rs['sortRows'].reverse();
            }

            // 更新table
            var nlSortRow = new Y.NodeList(this.rs['sortRows']);
            var nlCommonRow = new Y.NodeList(this.rs['commonRows']);

            var tbody = this.ndTable.get('tBodies').item(0);
            if (!this.ndTable.one('thead')) {
                tbody.setHTML(this.ndTable.one('tr'));
            } else {
                tbody.setHTML('');
            }
            tbody.appendChild(nlSortRow.toFrag());
            tbody.appendChild(nlCommonRow.toFrag());
        },
        // 排序函数
        sortFun: function(a, b, parser){
            var x = parser.format(a);
            var y = parser.format(b);
            switch (parser.type) {
            case 'pinyin':
                return x.localeCompare(y);
            case 'numeric':
                if (isNaN(x)) x = -1 * Number.MAX_VALUE;
                if (isNaN(y)) y = -1 * Number.MAX_VALUE;
                return x - y;
            case 'text':
                return x.localeCompare(y);
            default:
                return x - y;
            }
        },
        //异步获得表格数据
        getTableData: function(url, postParams) {
            var conf = this.config;
            var method = this.get('sourceMethod');
            var _this = this;

            postParams = Y.mt.util.toPostData(postParams);
            Y.io(url, {
                method :method,
                on : {
                    start: function() {
                        if (conf.afterStart) conf.afterStart();
                    },
                    success: function(id,o) {
                        var res = Y.mt.util.getEvalRes(o);
                        _this.fire('getDataSuccess', {res: res});
                        if (!res.status)  return;

                        //这里如果采用fire事件, 在一个页面有多个表格的情况下会触发监听函数调用多次
                        //数据必须有ID属性
                        _this.config.tableData = res.data;
                        //保存异步回来的分页参数
                        Y.mix(_this.config.syncPage, res.page, true);
                        _this.set('refresh', !_this.get('refresh'));
                    },
                    failure: function() {},
                    end: function() {
                        if (conf.afterEnd) conf.afterEnd();
                    }
                },
                data: postParams
            });
        },
        //给数据添加标识
        _setAllDataID: function(data) {
            for (var i = 0; data && i < data.length; i++) {
                var d = data[i];
                this._setDataID(d, i);
            }
        },
        _setDataID: function(data, index) {
            data.recordId= this.recordPrefix+index;
        },
        // 创建解析器集合
        buildParserSet: function(){
            var ps = {
                parsers:[],
                addParser: function(parser){
                    var isIn = false;
                    for (var i = 0; i < this.parsers.length; ++i) {
                        if (this.parsers[i].name === parser.type.toLowerCase()) {
                            isIn = true;
                            break;
                        }
                    }
                    if (!isIn) {
                        parser.name = parser.name.toLowerCase();
                        this.parsers.push(parser);
                    }
                }
            };
            /**
             * 添加解析器
             * 注意：添加顺序影响推断数据类型结果，
             * 包含更大范围的解析器应排在靠后位置，如number排在int,float之后
             */
            // 整型
            ps.addParser({
                name:'int',
                is: function(s){
                    s = s.replace(/,/g, '');
                    return (/^-?\d+$/).test(s);
                },
                format: function(s){
                    s = s.replace(/,/g, '');
                    return parseInt(s, 10);
                },
                type:'numeric'
            });
            // 浮点型
            ps.addParser({
                name:'float',
                is: function(s){
                    s = s.replace(/,/g, '');
                    return (/^-?(\d+)?\.\d+$/).test(s);
                },
                format: function(s){
                    s = s.replace(/,/g, '');
                    return parseFloat(s);
                },
                type:'numeric'
            });
            // 包含数字的类型，按数字排序
            ps.addParser({
                name:'number',
                is: function(s){
                    s = s.replace(/,/g, '');
                    return (/^\D*?(-?\d+|-?\d+\.\d+|-?\.\d+)\D*$/).test(s);
                },
                format: function(s){
                    s = s.replace(/^\D*?(-?\d+|-?\d+\.\d+|-?\.\d+)\D*$/, '$1');
                    s = s.replace(/,/g, '');
                    return parseFloat(s);
                },
                type:'numeric'
            });
            // 形如YYYY[-/.]MM[-/.]DD的日期类型
            ps.addParser({
                name:'date',
                is: function(s){
                    return (/^\d{2,4}[-\/\.]\d\d?[-\/\.]\d\d?(?:\s+\d\d?:\d\d?:\d\d?)?$/).test(s);
                },
                format: function(s){
                    return Y.mt.lang.strtotime(s);
                },
                type:'numeric'
            });
            // 中文
            ps.addParser({
                name:'chinese',
                is: function(s){
                    return (/[\u4e00-\u9fa5]/).test(s);
                },
                format: function(s){
                    return s;
                },
                type:'pinyin'
            });
            // 字符串
            ps.addParser({
                name:'string',
                is: function(s){
                    return (/^\w+$/).test(s);
                },
                format: function(s){
                    var mc = s.match(/(\w+)/);
                    return mc ? mc[1].toLocaleLowerCase() : '';
                },
                type:'text'
            });

            return ps;
        }
    };

    $Table = Y.mt.extention.Table;
}, '1.0.0', {
    requires: [
        'mt-base',
        'io-base',
        'w-paginator',
        'node',
        'oop',
        'event-custom'
    ]
});
 
