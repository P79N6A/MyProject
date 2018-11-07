/**
 * 针对table的插件
 * @module w-table/p-table
 **/
M.add('w-table/p-table', function(Y) {
    Y.namespace('mt.plugin');
    var $N = Y.Node;
    var $Lang = Y.Lang;

    var $CSS = {
        selectAll: 'widget-table-selectAll',
        checkbox: 'widget-table-checkbox',
        toolbar: 'widget-table-toolbar',
        columnForm: 'widget-table-columnForm',
        columnFormBox: 'widget-table-colmunFormBox',
        diagBtn: 'widget-table-diagBtn',
        diagOK: 'widget-table-diagOK',
        diagCancel: 'widget-table-diagCancel'
    };
    var $TEMPLATE = {
        toolbar: '<div class="{toolbar}"></div>',
        toolButton: '<input type="button" value="{btnName}" class="btn" />',
        columnForm: '<div class="{columnFormBox}">'+
                        '<div class="{columnForm}"></div>'+
                        '<div class="{diagBtn}">'+
                            '<input type="button" class="formbutton {diagOK}" value="确定" />'+
                            '<input type="button" class="formbutton {diagCancel}" value="取消" />'+
                        '</div>'+
                    '</div>'
    };

    /**
    * @description 针对Table的插件类
    */
    Y.mt.plugin.Table = Y.Base.create('plugin-table', Y.Plugin.Base, [], {
        initializer: function(config) {
            this.host = this.get("host");
            this.checkboxColumn = this.host.get("checkboxColumn");
            this.toolbar = this.host.get("toolbar");
            this.ioConfig = {
                method: "post",
                query: "data" //设置参数的名称
            };
        },
        /**
         * 添加工具栏
         * @method addToolbar
         * @param {}
         * @return {}
         */
        addToolbar: function(host) {

            //生成toolbar容器
            var toolbarBox = this._createToolbarBox();
            //生成“添加”按钮
            for (var i = 0; i < this.toolbar.length; i++) {
                var btn = this.toolbar[i];
                if (btn.name === "add") {
                    var ndAdd = this._appendAddButton(toolbarBox, btn);
                    //创建添加对话框
                    this._createAddDialog(btn);
                    // 单击Dialog确定按钮添加一行记录
                    this._bindAddRow(btn);
                } else if (btn.name === "delete") {
                    //生成“删除”按钮
                    var ndDelete = this._appendDeleteButton(toolbarBox, btn);
                } else if (btn.name === "modify") {
                    //生成“修改”按钮
                    var ndModify = this._appendModifyButton(toolbarBox, btn);
                    this._createModifyDialog(btn);
                    // 单击Dialog确定按钮修改一行记录
                    this._bindModifyRow(btn);
                } else {
                    this._appendCustomButton(toolbarBox, btn);
                }
            }
            return toolbarBox;
        },
        _appendCustomButton: function(toolbarBox, btn) {
            var _this = this;
            this._appendButton(toolbarBox, btn, function() {
                btn.callback(_this.host, _this);
            });
        },
        // 单击Dialog确定按钮修改一行记录
        _bindModifyRow: function(btn) {
            var _this = this;
            this.modifyDiagBox.delegate('click', function() {
                if (this.test("."+$CSS.diagOK)) {
                    var formData = _this.modifyDynamicForm.getFormData();
                    //根据dynamicForm返回数据生成DataTable行数据
                    var row = _this._getDataTableRowByFormData(formData);
                    //添加IO默认值
                    if (btn.action) {
                        Y.mix(btn, _this.ioConfig);
                        var query = {};
                        query[btn.formQueryName] = encodeURIComponent(Y.JSON.stringify(row));
                        Y.mix(query, btn.query, true);
                        Y.mt.io[btn.method](btn.action, query, function(o) {
                            if(o.status) {
                                //修改一行
                                _this._modifyRow(row);
                            }
                        });
                    } else {
                        //修改一行
                        _this._modifyRow(row);
                    }
                }
                _this.modifyDiag.close();
            }, '.'+$CSS.diagBtn+' .formbutton');
        },
        /**
         * 修改一行
         * @private
         * @method _modifyRow
         * @param {}
         * @return {}
         */
        _modifyRow: function(row) {
            var records = this.getSelectedRow();
            if (records.length > 1) {
                alert("一次只能修改一条记录");
                return;
            } else if (records.length === 0) {
                alert("请选择一条记录");
                return;
            }
            var currentModifiedRecord = records[0];
            //填充默认数据
            Y.mix(row, currentModifiedRecord, false);
            //修改table对应的行
            this._replaceRowValue(currentModifiedRecord, row);
            //同步tableData
            Y.mix(currentModifiedRecord, row, true);
        },
        //修改指定的行
        _replaceRowValue: function(currentModifiedRecord, row) {
            //转化为数组
            var columnValue = this.host._getColumnValueByThParam(row, this.host.config.thParams);
            var currentTr = this.host.ndTable.one("tr[recordid='"+currentModifiedRecord.recordId+"']");
            currentTr.all("td").each(function(td, index) {
                if (index === 0) {
                    return;
                }
                td.setHTML(columnValue[index-1]);
            });
        },
        // 单击Dialog确定按钮添加一行记录
        _bindAddRow: function(btn) {
            var _this = this;
            this.addDiagBox.delegate('click', function() {
                if (this.test("."+$CSS.diagOK)) {
                    var formData = _this.addDynamicForm.getFormData();
                    //根据dynamicForm返回数据生成Table行数据
                    var row = _this._getDataTableRowByFormData(formData);
                    //添加IO默认值
                    if (btn.action) {
                        Y.mix(btn, _this.ioConfig);
                        var query = {};
                        query[btn.formQueryName] = encodeURIComponent(Y.JSON.stringify(row));
                        Y.mix(query, btn.query, true);
                        Y.mt.io[btn.method](btn.action, query, function(o) {
                            row.id = o.data;
                            if(o.status) {
                                _this._addRow(row);
                            }
                        });
                    } else {
                        _this._addRow(row);
                    }
                }
                _this.addDiag.close();
            }, '.'+$CSS.diagBtn+' .formbutton');
        },
        /**
         * 添加一行
         * @private _addRow
         * @method
         * @param {}
         * @return {}
         */
        _addRow: function(row) {
            var _this = this;
            //转化为数组
            var columnValue = _this.host._getColumnValueByThParam(row, _this.host.config.thParams);
            //将record转换为对应的tr添加到table中
            var ndTable = _this.host.ndTable;
            var records = _this.host.config.tableData || _this.host.tableData;
            var ndTr = _this.host._createRowWithRecord(ndTable, columnValue, records.length);
            ndTable.one('tbody').prepend(ndTr);
            // 生成td里的checkbox
            _this._addCheckbox4tr(ndTr);
            //同步tableData
            _this.host._setDataID(row, records.length);
            records.push(row);
            //刷新状态
            this.host.refreshCommonTable();
        },
        /**
         * 根据dynamicForm返回数据生成DataTable行数据
         * @private
         * @method _getDataTableRowByFormData
         * @param {Array}
         * @return {Object}
         */
        _getDataTableRowByFormData: function(formData) {
            var row = {};
            for (var i = 0; i < formData.length; i++) {
                var data = formData[i];
                row[data.attrCode] = window.decodeURIComponent(data.attrValue);
            }
            return row;
        },
        /**
         * 获取column模板
         * @private
         * @method _getColumnTemplate
         * @return {}
         */
        _getColumnTemplate: function(columns, record) {
            var conf = [];
            var dynamicFormConfig = this.get('host').config.dymicForm;
            var data = dynamicFormConfig.data;
            for (var i = 0; i < columns.length; i++) {
                var col = columns[i];
                var object = col.dymicForm || {};
                object[dynamicFormConfig.dataRelation['desc']] = col.label;
                //必须使用col的name
                object[dynamicFormConfig.dataRelation['name']] = col.name;
                //有对应的record，说明是在编辑
                if (record) {
                    object.value = record[col.name];
                    object.id = record.id;
                }
                Y.mix(object, col);
                conf.push(object);
            }
            return conf;
        },
        getSelectedRow: function() {
            var records = this.host.config.tableData || this.host.tableData;
            var array = [];
            for (var i = 0; i < records.length; i++) {
                var r = records[i];
                if (r._selected) {
                    array.push(r);
                }
            }
            return array;
        },
        /**
         * 生成“删除”按钮
         * @private
         * @method _appendButton
         */
        _appendButton: function(toolbarBox, btn, callback) {
            var btnString = Y.Lang.sub($TEMPLATE.toolButton, {
                btnName: btn.title
            });
            var ndBtn = $N.create(btnString);
            if (callback) {
                ndBtn.on('click', callback);
            }
            toolbarBox.append(ndBtn);
            return ndBtn;
        },
        /**
         * 生成“删除”按钮
         * @private
         * @method _appendDeleteButton
         */
        _appendDeleteButton: function(toolbarBox, btn) {
            var _this = this;
            var ndBtn = this._appendButton(toolbarBox, btn, function() {
                var records = _this.getSelectedRow();
                if (records.length > 0) {
                    if (!window.confirm("确定删除吗?")) {
                        return;
                    }
                }
                var ndTable = _this.host.ndTable;
                //删除一行
                _this._deleteRow(records);
                var idBox = [];
                for (var i = 0; i < records.length; i++) {
                    idBox.push(records[i].id);
                }
                //添加IO默认值
                if (btn.action) {
                    Y.mix(btn, _this.ioConfig);
                    var query = {};
                    query[btn.query] = idBox.join(",");
                    Y.mt.io[btn.method](btn.action, query, function(o) {

                    }, function() {});
                }
            });
            return ndBtn;
        },
        //生成“修改”按钮
        _appendModifyButton: function(toolbarBox, btn) {
            var _this = this;
            var ndBtn = this._appendButton(toolbarBox, btn, function() {
                var records = _this.getSelectedRow();
                if (records.length > 1) {
                    alert("一次只能修改一条记录");
                    return;
                } else if (records.length === 0) {
                    alert("请选择一条记录");
                    return;
                }
                var record = records[0];
                _this.currentModifiedRecord = record;

                //TODO 有问题
                var columnConf= _this._getColumnTemplate(_this.host.get("thParams"), record);
                //清空diag
                var ndFormBox = _this.modifyDiagBox.one("."+$CSS.columnForm);
                ndFormBox.setHTML('');
                //生成Column表单
                _this._createFormByColumn(ndFormBox, 'modifyDynamicForm', columnConf);
                _this.modifyDiag.show();
            });
            return ndBtn;
        },
        /**
         * 删除一行
         * @private _deleteRow
         * @method
         * @param {}
         * @return {}
         */
        _deleteRow: function(records) {
            var ndTable = this.host.ndTable;
            for (var i = 0; i < records.length; i++) {
                var r = records[i];
                ndTable.one('tbody tr[recordid='+r.recordId+']').remove();
                //使该数据无效
                r._selected = false;
            }
            //刷新状态
            this.host.refreshCommonTable();
        },
        _createToolbarBox: function() {
            var toolbar = Y.Lang.sub($TEMPLATE.toolbar, {
                toolbar: $CSS.toolbar
            });
            var ndToolbar = $N.create(toolbar);
            this.get("host").cBox.append(ndToolbar);
            return ndToolbar;
        },
        /**
         * 创建“添加”dialog
         * @private
         * @method _createAddDialog
         * @param {}
         */
        _createAddDialog: function(btn) {
            var object = {
                diagName: "addDiag",
                title: "添加记录"
            };
            Y.mix(object, btn.dialogParam);
            this._createDialog(object);
        },
        /**
         * 创建“修改”dialog
         * @private
         * @method _createModifyDialog
         * @param {}
         */
        _createModifyDialog: function(btn) {
            var object = {
                diagName: "modifyDiag",
                title: "修改记录"
            };
            Y.mix(object, btn.dialogParam);
            this._createDialog(object);
        },
        /**
         * 创建dialog
         * @private
         * @method _createDialog
         * @param {}
         */
        _createDialog: function(opt) {
            opt = opt || {
                diagName: 'addDiag',
                title: "添加记录"
            };
            this[opt.diagName+"Box"] = $N.create(Y.Lang.sub($TEMPLATE.columnForm, {
                columnForm: $CSS.columnForm,
                columnFormBox: $CSS.columnFormBox,
                diagBtn: $CSS.diagBtn,
                diagOK: $CSS.diagOK,
                diagCancel: $CSS.diagCancel
            }));
            var dialogParam = {
                width: 600,
                target: 'center'
            };
            Y.mix(opt, dialogParam);
            opt.content = this[opt.diagName+"Box"];
            this[opt.diagName] = this[opt.diagName] || new Y.mt.widget.CommonDialog(opt);
        },
        /**
         * 生成“添加按钮”
         * @private
         * @method _appendAddButton
         */
        _appendAddButton: function(toolbarBox, btn) {
            var _this = this;
            var ndBtn = this._appendButton(toolbarBox, btn, function() {
                //清空diag
                var ndFormBox = _this.addDiagBox.one("."+$CSS.columnForm);
                ndFormBox.setHTML('');
                //生成Column表单
                var colmunConf = _this._getColumnTemplate(_this.get("host").get("thParams"));
                _this._createFormByColumn(ndFormBox, "addDynamicForm", colmunConf);
                _this.addDiag.show();
            });
            return ndBtn;
        },
        /**
         * 生成Column表单
         * @private
         * @method _createFormByColumn
         * @return {}
         */
        _createFormByColumn: function(ndFormBox, formName, columnConf) {
            var dynamicFormConfig = this.get('host').config.dymicForm;
            dynamicFormConfig.data = columnConf;
            dynamicFormConfig.node = ndFormBox;
            dynamicFormConfig.columnConf = {count: 1};
            this[formName]= new Y.mt.widget.DynamicForm(dynamicFormConfig);
        },
        /**
         * @description 为多个表格分别添加checkbox列，通过调用insertCheckboxCol实现
         * @param {Array} tableArr 保存了想要添加这个plugin的表格的外围div的id的字符串/对象数组
         *                  例如：['tableDiv1', '#tableDiv2', ndTableDiv3](支持3种形式)
         * @param {Object} conf 配置如何插入checkbox列
         */
        insertCheckboxColForMul: function( tableArr, conf ){
            if( $Lang.isArray(tableArr) ) {
                for (var i = 0, l = tableArr.length; i < l; ++i) {
                    this.insertCheckboxCol(tableArr[i], conf);
                }
            }
        },
        /**
         * @description 为表格添加一列checkbox。该表格（包括CommonTable）必须由一个拥有id的div包围。
         * @param {String} id 包围表格的div的id，用来确定需要添加checkbox的表格
         * @param {Object} conf 配置如何插入checkbox列
         */
        insertCheckboxCol: function( id, conf ){
            var _this = this;

            //id的格式可以加“#”也可以不加
            if ( !Y.Lang.isString(id) ) {
                id = id.get("id");
            }
            id = id.trim();
            if( id.indexOf("#") === 0 ) {
                id = id.substring(1);
            }

            //如果是CommonTable,则直接调用insert函数
            var table = Y.one("#"+id+" table");
            if( table ) {
                insert({ndTable: table});
            }
            Y.Global.on('AjaxTable.refresh', insert);

            // 'AjaxTable.refresh'触发的回调函数，用于向注册了的table添加checkbox列
            // e对象包含的参数有：ndTable，注册的table对象；values，保存了用于添加至checkbox的value属性的值的数组。
            function insert( e ) {
                var ndTable = e.ndTable;

                //只有包围table的div的id与输入的相同，才会给这个table添加checkbox列
                if( ndTable.ancestor('div').get("id") === id ) {

                    //获得checkbox的id数组
                    var values = e.values ? e.values : [];

                    var ndTheadTr = ndTable.one("thead tr");
                    var checkboxValue;
                    var thBox;

                    var ndCheckAll = $N.create('<th style="width:15px; text-align:left;"><input type="checkbox" class="check-all"/></th>');
                    ndCheckAll.setAttribute("rowspan", ndTable.all("thead tr").size());
                    ndTheadTr.prepend(ndCheckAll);

                    // 生成td里的checkbox
                    var nlTr = ndTable.all("tbody tr");
                    _this._addCheckbox4Alltr(nlTr, values);

                    //给th里的checkbox添加多选功能
                    thBox = ndCheckAll.one('.check-all');
                    thBox.detach();//避免多次刷新后thBox上注册了多个回调函数
                    thBox.on("click", function() {
                        var status = this.get("checked");
                        var checkboxes = ndTable.all("input[name=checkbox]");
                        for (var j = 0, l = checkboxes.size(); j < l; ++j) {
                            var ndCheckbox = checkboxes.item(j);
                            ndCheckbox.set("checked", status);
                            //设置对应的record记录
                            _this._toggleRecordSelected(ndCheckbox.ancestor("tr"), ndCheckbox);
                        }
                    });
                    //切换checkbox状态
                    ndTable.delegate('click', function() {
                        thBox.set("checked", false);
                        thBox.set("checked", false);
                        //设置对应的record记录
                        _this._toggleRecordSelected(this.ancestor("tr"), this);
                    }, 'input[name=checkbox]');
                    ndTable.delegate('click', function(e) {
                        if (e.target.test("input[type=checkbox]")) {
                            return;
                        }
                        if (this.one("th")) {
                            return;
                        }
                        var ndCheckbox = this.one("input[type=checkbox]");
                        ndCheckbox.set("checked", !ndCheckbox.get("checked"));
                        thBox.set("checked", false);
                        //设置对应的record记录
                        _this._toggleRecordSelected(this, ndCheckbox);
                    }, 'tr');
                }
            }

        },
        //给单行增加checkbox
        _addCheckbox4tr: function(tr) {
            // 生成td里的checkbox
            var td = tr.all("td").item(0);
            var checkbox = $N.create('<td><input type="checkbox" name="checkbox" /></td>');
            td.insert(checkbox, 'before');
        },
        //给每一行增加checkbox
        _addCheckbox4Alltr: function(nlTr, values) {
            // 生成td里的checkbox
            for(var i= 0, l=nlTr.size(); i<l; ++i) {
                this._addCheckbox4tr(nlTr.item(i));
            }
        },
        //设置对应的record记录
        _toggleRecordSelected: function(ndTr, ndCheckbox) {
            var recordId = ndTr.getAttribute("recordId");
            var record = this._getRecordById(recordId);
            record._selected = !!ndCheckbox.get("checked");
        },
        //根据recordId获取对应的record
        _getRecordById: function(recordId) {
            var records = this.host.config.tableData || this.host.tableData;
            for (var i = 0; i < records.length; i++) {
                var record = records[i];
                if (record.recordId === recordId) {
                    return record;
                }
            }
            return null;
        }
    }, {
        NS: 'plugin-table',
        ATTRS: {
            /**
             */
            nds: {
                value: null
            }
        }
    });
}, '1.0.0', { requires: ['mt-base', 'w-base', 'plugin', 'w-dynamic-form', 'json', 'node'] });
 
