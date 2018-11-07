/**
 * 动态表单, 根据配置输出各种类型的表单项, 可以进行表单嵌套，也可以在基础上开发自定义插件
 * @module w-dynamic-form
 */
M.add('w-dynamic-form', function(Y) {
    var $Form = Y.mt.form;
    var isArray = Y.Lang.isArray;
    var isEmpty = Y.Object.isEmpty;
    var trim = Y.Lang.trim;
    var $Util = Y.mt.util;
    var $Lang = Y.mt.lang;
    var $N = Y.Node;

    //表单项属性对应的字段名
    var defaultDataRelation = {
        name: '',
        id: '',
        type: 'text',
        desc: '',
        value : '',
        validator: '',
        varUrl: '',
        dataList: 'resultData',
        data: null ,
        trCssName: '',
        itemCssName: '',
        required: false,
        editable: true,
        readonly: false,
        tips: '',
        placeholder: '',
        isShow: true,
        isMultiple: false,
        showPattern: '',
        mixed: false,
        style: '',
        config: null,
        groupIndex: null
    };

    //表单项的类型
    var defaultType = {
        radio: 'radio',
        checkbox: 'checkbox',
        select: 'select',
        simpleText: 'simpleText',
        date: 'date',
        headline: 'headline',
        textarea: 'textarea'
    };

    var EVENT_NAME = {
        beforeBuildItem: 'beforeBuildItem',
        afterBuildItem: 'afterBuildItem',
        beforeBuildForm: 'beforeBuildForm',
        afterBuildForm: 'afterBuildForm',
        beforeSetValue: 'beforeSetValue',
        afterSetValue: 'afterSetValue',
        getFormDataSuccess: 'getFormDataSuccess',
        getFormDataFailure: 'getFormDataFailure',
        setValueFail: 'setValueFail',
        beforeGetFormData: "beforeGetFormData",
        beforeGetFormJSONData: "beforeGetFormJSONData",
        afterGetFormJSONData: "afterGetFormJSONData"
    };

    var CSS_NANE = {
        tableView: 'widget-dynamic-table-view',
        itemBoxName: 'widget-dynamic-form-fieldbox',
        required: 'widget-dynamic-form-required'
    };

    /**
     * @class DynamicForm
     * @namespace mt.widget
     * @constructor
     */
    var DynamicForm = Y.Base.create('dynamic-form', Y.Widget, [], {
        initializer: function() {
            this.cBox = this.get('contentBox');
            var formData = this.get('formData');
            var _this = this;
            var node = this.get('node');
            var autoRender = this.get('autoRender');
            //在实例中也存一份
            this.TEMPLATE = DynamicForm.TEMPLATE;

            //存储meta转化后的obj
            this.metaList = {};
            //初始化验证
            if (this.get('validatorConf')) {
                this.initValidator();
            }

            //初始化数据
            this.set('typeDefine', this.get('typeDefine'));
            this.isEditable = false;
            //支持手动render
            if(formData) {
                if (formData.data) {
                    formData.data = this._formatFormData(formData.data);
                    if (autoRender) {
                        _this.render(node);
                    }
                } else {
                    this.fillFormData(function(data) {
                        formData.data = data;
                        if (autoRender) {
                            _this.render(node);
                        }
                    });
                }
            } else if(autoRender) {
                this.render(node);
            }
        },
        /**
         * 初始化验证
         * @method initValidator
         */
        initValidator: function() {
            var validatorConf = this.get('validatorConf');
            if(!validatorConf.form) {
                validatorConf.form = this.get('node');
            }
            if(!validatorConf.tipType) {
                validatorConf.tipType = 'bottom';
            }
            this.validator = new Y.mt.widget.FormValidate(this.get('validatorConf'));
            //添加验证规则
            this.on(EVENT_NAME.afterSetValue, this._addValidatorRule, this);
        },
        /**
         * 加入验证规则
         * @method _addValidatorRule
         */
        _addValidatorRule: function(e) {
            var params = e.params,
                type = params.type,
                formItem = e.formItem,
                inputWhiteList = ['radio', 'checkbox', 'text', 'date'],
                node = null,
                rulesManager = this.validator.rulesManager;

            if($Lang.inArray(type, inputWhiteList)) {
                node = formItem.one('input');
            } else if (type === 'textarea') {
                node = formItem.one('textarea');
            }
            if (!node || !rulesManager.getRules(params.name)) {
                return;
            }
            //转化validator为JSON格式
            var validator = rulesManager.getRulesStr(params.name);
            //初始化验证
            node.setAttribute("data-rules", validator);
        },
        bindUI: function () {
        },
        renderUI: function() {
            var temp = '';
            var data = this.get('data');
            this.beforeBuildItem = this.get('beforeBuildItem');
            this.showColon = this.get('showColon');
            if(this.get('groups')) {
                temp = this.renderGroup();
            } else {
                temp = this.renderFrom(data);
            }
            this.fire(EVENT_NAME.beforeBuildForm, {temp: temp, data: data});
            this.get('contentBox').setHTML(temp);
            this.fire(EVENT_NAME.afterBuildForm, {temp: temp, data: data});

            this._renderTableDescUI();
            this._setColumnWidth();
        },
        /**
         * 获取对象的第一个key
         * @private
         * @method
         * @param {Object}
         * @return Object
         */
        getFirstKeyOfJSON: function (json) {
            return Y.Object.keys(json)[0];
        },
        /**
         * 渲染table的相关附加结构, 如是否为必填项等
         * @method _renderTableDescUI
         */
        _renderTableDescUI: function() {
            var cBox = this.get('contentBox');
            var allRequired = cBox.all('[required="true"]');
            allRequired.each(function(item) {
                var th = item.ancestor('td').previous('th');
                if(th && !th.one('.' + CSS_NANE.required)) {
                    th.prepend(DynamicForm.TEMPLATE.required);
                }
            });
        },
        /**
         * 如果是grid的状态
         * @method _setGridWidth
         */
        _setColumnWidth: function() {
            var columnConf = this.get('columnConf');
            var ndTable = this.get('contentBox').one('table');
            if(!ndTable) return;
            var tableTrs = ndTable.get('rows');
            var width = columnConf.width;
            if(width) {
                tableTrs.each(function(tr, index) {
                    tr.get('cells').each(function(td, tdIndex) {
                        if(index === 0) {
                            td.setStyle('width', width[tdIndex] || "");
                        }
                        var tdInput = td.one('input.widget-dynamic-form-text');
                        if(tdInput && tdInput.getAttribute('type') === "text") {
                            if (!tdInput.getAttribute('data-width')) {
                                tdInput.setStyle("width", width[tdIndex] || "");
                            } else {
                                tdInput.setStyle("width", tdInput.getAttribute('data-width') || "");
                            }
                        }
                    });
                });
            }
        },
        /**
         * 构建GroupsUI
         * @method renderGroup
         * groups格式如下
         * {groups1: { desc: '', name: ''}, ...}
         */
        renderGroup: function() {
            var groups = this.get('groups');
            var _this = this;
            var temp = [], tempNode, data;
            Y.Array.each(groups, function(item, index) {
                data = item.data || item.config;
                if(item.name === undefined) {
                    item.name = 'name' + index;
                }
                tempNode = Y.Node.create(Y.Lang.sub(DynamicForm.TEMPLATE.tab, {
                    desc: item.desc,
                    dataName: item.name
                }));
                if (!_this.get("hideTab")) {
                    tempNode.prepend(Y.Lang.sub(DynamicForm.TEMPLATE.tabTitle, {
                        desc: item.desc
                    }));
                }
                tempNode.one('.widget-dynamic-form-content').append(_this.renderFrom(data, index));
                temp.push(tempNode);
            });
            return new Y.NodeList(temp);
        },
        /**
         * 添加进metaList
         * @method _addMetaList
         */
        _addMetaList: function(params, groupIndex) {
            //存储metaList
            if(groupIndex !== undefined) {
                this.metaList[groupIndex] = this.metaList[groupIndex] || {};
                this.metaList[groupIndex][params.name] = params;
            } else {
                this.metaList[params.name] = params;
            }
        },
        /**
         * 构建formUI
         * @method renderFrom
         */
        renderFrom: function(data, groupIndex) {
            var _this = this;
            var tableCss = this.get('tableClassName');
            //针对textarea的特殊处理
            var newLineForTextarea = this.get('newLineForTextarea');
            var textareaList = [], useData = [], hideData = [];
            if (this.get('plainText')) tableCss += (' ' + CSS_NANE.tableView);
            var ndTable = Y.Node.create(Y.Lang.sub(DynamicForm.TEMPLATE.tableContainer, {className: tableCss}));
            //过滤不显示的数据
            Y.Array.each(data, function(item, index) {
                var params = _this.formatFormItemData(item, groupIndex, index);
                _this._addMetaList(params, groupIndex);
                //textarea类型新开一行处理
                if (params.isShow) {
                    if(newLineForTextarea && params.type === 'textarea') {
                        textareaList.push(params);
                    } else {
                        useData.push(params);
                    }
                } else {
                    hideData.push(params);
                }
            });
            this.hideData = hideData;
            var commonTr = this._buildCommonItem(useData);
            var textareaTr = this._showTextareaToEnd(textareaList);
            ndTable.one('tbody').append(new Y.NodeList(commonTr.concat(textareaTr)));

            return ndTable;
        },
        /**
         * 生成普通的表单项
         * @method _buildCommonItem
         */
        _buildCommonItem: function(useData) {
            var length = useData.length;
            var columnConf = this.get('columnConf');
            var trTemp = DynamicForm.TEMPLATE.tr;
            var _this = this;
            var tr = null, colspan = 0, trs = [], tds = [];
            var fullLineTextarea = this.get('fullLineTextarea');
            var fullLineLastItem = this.get('fullLineLastItem');
            var newLineForTextarea = false;
            var newTr = null;
            //正常数据显示
            Y.Array.each(useData, function(params, index) {
                tr = null;
                colspan = 0;
                newLineForTextarea = fullLineTextarea && (params.type === 'textarea');
                newTr = null;

                if (newLineForTextarea) {
                    //textarea可配置单独显示为一行
                    var newColspan = (columnConf.count) * 2 - 1;
                    newTr = Y.Node.create(Y.Lang.sub(trTemp, { trCssName: params.trCssName, colspan: newColspan}));
                    var td = _this._buildTableItem(params);
                    td.one('td').setAttribute('colspan', newColspan);
                    newTr.append(td);
                } else {
                    //构建table的tr以及其中的表单项
                    tds.push(_this._buildTableItem(params));
                }

                if((tds.length % columnConf.count === 0) || newLineForTextarea) {
                    tr = Y.Node.create(Y.Lang.sub(trTemp, { trCssName: params.trCssName }));
                } else if (index === length-1 && tds.length !== 0) {
                    if(tds.length < columnConf.count && fullLineLastItem) {
                        colspan = (columnConf.count - tds.length) * 2 + 1;
                    }
                    tr = Y.Node.create(Y.Lang.sub(trTemp, { trCssName: params.trCssName, colspan: colspan }));
                }

                if (tr) {
                    //最后一行td加入colspan
                    if (tds.length > 0) {
                        if (params.type !== "headline" && !params.originalData.isHiddenName) {
                            tds[tds.length-1].one('td').setAttribute('colspan', colspan);
                        }
                        tr.append(new Y.NodeList(tds));
                        trs.push(tr);
                    }
                    if (newTr) trs.push(newTr);
                    tds = [];
                }
            });
            return trs;
        },
        /**
         * 将textarea类型显示到末尾行
         * @method _showTextareaToEnd
         */
        _showTextareaToEnd: function(textareaList) {
            var trs = [], tr = null, td = null;
            var trTemp = DynamicForm.TEMPLATE.tr;
            var columnConf = this.get('columnConf');
            var _this = this;

            Y.Array.each(textareaList, function(params) {
                tr = Y.Node.create(Y.Lang.sub(trTemp, { trCssName: params.trCssName }));
                //构建table的tr以及其中的表单项
                td = _this._buildTableItem(params);
                td.one('td').setAttribute('colspan', columnConf.count * 2 - 1);
                tr.append(td);
                trs.push(tr);
            });
            return trs;
        },
        /**
         * 获取Groups的key
         * @private
         * @method
         * @param ${}
         * @return ${return}
         */
        _getGroupKeys: function() {
            var groups = this.get('groups');
            var parentName = [];
            if(groups) {
                parentName = Y.Object.keys(groups);
            }
            return parentName;
        },
        /**
         * 获得表单的最后数据, 用于存储等
         * @method getFormData
         */
        getFormData: function(groupsName, isSimple) {
            var parentName = [];
            var result = {}, tempParent = null;
            var groups = this.get('groups');
            var _this = this;
            if(groupsName) {
                parentName.push(groupsName);
            } else if(groups) {
                parentName = Y.Object.keys(groups);
            } else {
                return this._getFormDataByParent(this.get('contentBox'), isSimple);
            }

            Y.Array.each(parentName, function(item) {
                tempParent = Y.one('[data-name="' + groups[item].name + '"]');
                result[item] = _this._getFormDataByParent(tempParent, isSimple);
                _this.fire(EVENT_NAME.beforeGetFormData, { result: result[item], isSimple: isSimple, ndParent: tempParent});
            });
            return result;
        },
        /**
         *@method _getFormDataByParent
         */
        _getFormDataByParent: function(ndParent, isSimple) {
            var isEncode = this.get('isEncode');
            if (isEncode !== true) isEncode = false;
            // FIXME: 默认的joinkey是&, 如果value含&，则会产生bug
            var joinKey = 'a12dcb07543dc79713233e0cf77fff1a';
            var formData = $Form.toObject(ndParent, null, isEncode, joinKey, ',');
            if(isSimple) return formData;
            var tempArr = [];
            Y.Object.each(formData, function(item, p) {
                tempArr.push({ attrCode: p, attrValue: item });
            });
            return tempArr;
        },
        /**
         * 获得json格式的表单数据
         * 暂不支持group
         * @method _getFormJSONData
         */
        _getFormJSONData: function(ndParent) {
            var _this = this;
            var result = {}, tempParent = null;
            _this.fire(EVENT_NAME.beforeGetFormJSONData, { result: result, ndParent: ndParent});
            // 获取Groups的key
            var groupKeys = this._getGroupKeys();
            var groups = this.get('groups');
            if (!groups) {
                result = $Form.toJSON(ndParent, true);
            } else {
                Y.Array.each(groupKeys, function(item) {
                    tempParent = ndParent.one('[data-name="' + groups[item].name + '"]');
                    result[item] = $Form.toJSON(tempParent, true);
                });
            }
            _this.fire(EVENT_NAME.afterGetFormJSONData, { result: result, ndParent: ndParent});

            return result;
        },
        /**
         * 构建table的tr以及其中的表单项
         * @method _buildTableItem
         * @private
         */
        _buildTableItem: function(params) {
            //创建标题元素
            if (params.type === "headline") {
                // 创建headline类型的Cell
                return this._buildHeadLineCell(params);
            }
            // 创建正常类型的Cell
            return this._buildCommonCell(params);
        },
        /**
         * 创建正常类型的Cell
         * @private
         * @method _buildCommonCell
         * @param {}
         * @return
         */
        _buildCommonCell: function (params) {
            var tdTemplate = params.originalData.isHiddenName ? DynamicForm.TEMPLATE.noTitleTd :  DynamicForm.TEMPLATE.td;
            var desc = this.showColon ? params.desc + ':' : params.desc;
            //创建普通td元素
            var ndTd = Y.Node.create(Y.Lang.sub(tdTemplate, {desc: desc, attrcode: params.originalData.attrCode || ""}));
            //生成表单项
            var field = this._buildFormItem(params);
            //只展示纯文本
            if (this.get("plainText") && !(params.varUrl || params.url)) {
                //用表单元素的值替换它本身
                this._replaceInputWithValue(params, field);
            }
            ndTd.one('.widget-dynamic-form-fieldbox').append(field);
            return ndTd;
        },
        /**
         * 创建headline类型的Cell
         * @private
         * @method _buildHeadLineCell
         * @return
         */
        _buildHeadLineCell: function (params) {
            var tdTemplate = DynamicForm.TEMPLATE.headline;
            var ndTd = Y.Node.create(Y.Lang.sub(tdTemplate, {attrcode: params.originalData.attrCode || ""}));
            var headType = params.showPattern ? params.showPattern : '<h3>{name}<h3>';
            var headTypeStr = Y.Lang.sub(headType, {name: params.desc});
            ndTd.one(".widget-dynamic-form-headline").append(headTypeStr);
            return ndTd;
        },
        /**
         * 用表单元素的值替换它本身
         * @private
         * @method _fillRadioCheckboxSelect
         * @param {}
         * @return {}
         */
        _replaceInputWithValue: function (params, field) {
            var valueBox = DynamicForm.TEMPLATE.valueBox;
            if (params.type === "radio") {
                // 替换radio
                this._replaceRadio(field, valueBox);
            } else if (params.type === "checkbox") {
                // 替换checkbox
                this._replaceCheckbox(field, valueBox);
            } else if (params.type === "select") {
                // 替换select，包括multiple
                this._replaceSelect(field, valueBox);
            } else {
                // 替换文本框与textarea
                this._replaceTexts(field, valueBox);
            }
        },
        /**
         * 替换所有Label内的Input
         * @private
         * @method _replaceAllInputsInLabel
         * @param {}
         * @return {}
         */
        _replaceAllInputsInLabel: function (ndBox) {
            var nlMixedText = ndBox.ancestor().all("input[type=text]");
            if (nlMixedText.size() > 0) {
                nlMixedText.each(function (ndInput) {
                    ndInput.ancestor().append("&nbsp;" + ndInput.get("value") + "&nbsp;");
                    ndInput.hide();
                });
            }
        },
        /**
         * 替换radio
         * @private
         * @method _replaceRadio
         * @param {Node} field
         * @param {String} valueBox
         */
        _replaceRadio: function (field) {
            var _this = this;
            field.all("input[type=radio]").each(function (ndRadio) {
                if (!ndRadio.get("checked")) {
                    ndRadio.ancestor().hide();
                } else {
                    ndRadio.hide();
                    //替换所有Label内的Input
                    _this._replaceAllInputsInLabel(ndRadio);
                }
            });
        },
        /**
         * 替换checkbox
         * @private
         * @method _replaceCheckbox
         * @param {Node} field
         * @param {String} valueBox
         */
        _replaceCheckbox: function (field, valueBox) {
            field.all("input[type=checkbox]").each(function (ndCheckbox) {
                if (!ndCheckbox.get("checked")) {
                    ndCheckbox.ancestor().hide();
                } else {
                    ndCheckbox.ancestor().append(Y.Lang.sub(valueBox, {
                        value: "&nbsp;&nbsp;"
                    }));
                    ndCheckbox.hide();
                }
            });
        },
        /**
         * 替换select，包括multiple
         * @private
         * @method _replaceSelect
         * @param {Node} field
         * @param {String} valueBox
         */
        _replaceSelect: function (field, valueBox) {
            var ndSelect = field.one("select");
            var select = ndSelect.getDOMNode();
            var options = select.options;
            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                if (option.selected) {
                    var val = Y.Lang.sub(valueBox, {
                        value: option.text || option.value
                    });
                    ndSelect.insertBefore(val, ndSelect);
                }
            }
            ndSelect.hide();
        },
        /**
         * 替换文本框与textarea
         * @private
         * @method _replaceTexts
         * @param {Node} field
         * @param {String} valueBox
         */
        _replaceTexts: function (field, valueBox) {
            //空值返回
            var reg = /^\s*$/;
            if (!reg.test(field)) {
                //获取对应的表单元素
                var ndItem;
                var ndInput = field.one("input");
                if (ndInput) {
                    ndItem = ndInput;
                } else {
                    ndItem = field.one("textarea");
                }
                if (!ndItem) {
                    return;
                }
                //设值
                ndItem.hide();
                var itemValue = Y.Lang.sub(valueBox, {
                    value: this.get('allowHTML') ? ndItem.get("value") : Y.Escape.html(ndItem.get('value'))
                });
                ndItem.insertBefore(itemValue, ndItem);
            }
        },
        /**
         * 填写表单数据
         * @method formatFormData
         * data数据格式如下：
         * {data: { formData: [{ attrValue: '', attrCode: '' }]}
         */
        formatAjaxFormData: function(data) {
            if(!data) return null;
            var formData = data.formData;
            if(!formData) return null;
            var useData = this._formatFormData(formData);
            return useData;
        },
        _formatFormData: function(formData) {
            var useData = {};
            if(formData.length === 0) {
                //没有用？
                this.isEditable = false;
            } else {
                this.isEditable = true;
                if (Y.Lang.isArray(formData) && formData[0].attrCode !== 'undefined') {
                    Y.Array.each(formData, function(item) {
                        var value = Y.Lang.trim(item.attrValue);
                        var arrayReg = /^\[.*\]$/;
                        var objReg = /^\{.*\}$/;
                        if (arrayReg.test(value) || objReg.test(value)) {
                            try {
                                value = Y.JSON.parse(value);
                            } catch(e) {
                                value = "";
                            }
                        } else if (value.indexOf(',') > -1) {
                            value = value.split(',');
                        }
                        useData[item.attrCode] = value;
                    });
                } else {
                    return formData;
                }
            }
            return useData;
        },
        /**
         * 转换控件内使用的名称
         * @method formatFormItemData
         */
        formatFormItemData: function(itemParams, groupIndex, dataIndex) {
            var dataRelation = this.get('dataRelation');
            var res = {};
            var groups = this.get('groups');
            var data = this.get('data');
            //用于mtoa系统中，今后考虑删除
            itemParams.type = itemParams.rangeset ? 'radio' : itemParams.type;
            Y.Object.each(defaultDataRelation, function(item, p) {
                if(dataRelation[p] === undefined) {
                    dataRelation[p] = p;
                }
                res[p] = (itemParams[dataRelation[p]] !== undefined && itemParams[dataRelation[p]] !== null) ? itemParams[dataRelation[p]] : item;
            });
            //处理required
            if(res.validator === "required") res.required = true;
            this.formatFormItemType(res);
            //保存model原始值
            if(groups && groupIndex !== undefined) {
                res.groupIndex = groupIndex;
                res.originalData = groups[groupIndex].data[dataIndex];
            } else {
                //存放所有未处理前的数据
                res.originalData = data[dataIndex];
            }
            return res;
        },
        _getFormItem: function (nodeType, params) {
            var formItem;
            if (DynamicForm.TEMPLATE[nodeType] && !params.mixed && !params.showPattern) {
                var tempString = Y.Lang.sub(DynamicForm.TEMPLATE[nodeType], params);
                formItem = Y.Node.create(tempString);
            } else {//复杂类型
                //修改后直接为对象,EVENT_NAME.beforeBuildItem
                formItem = params.formItem;
            }
            return formItem;
        },

        _getFormData: function (formData) {
            var data = {};
            if(formData && formData.data) {
                data = formData.data;
            }
            return data;
        },
        _buildFormItemValidator: function(params) {
            var name = params.name;
            var validator = params.validator;
            var rulesManager = this.validator.rulesManager;
            if(params.required) {
                rulesManager.addRuleByName(name, {rule: 'required'});
            }
            if(validator) {
                rulesManager.addRules(name, validator);
            }
        },
        /**
         * 生成表单项
         * @method _buildFormItem
         * @param {String} type 表单项类型
         * @param itemParams itemParams 模板参数
         * param.type的类型有: select, checkbox, radio, text, textarea,
         * @private
         * @return Array
         */
        _buildFormItem: function(params) {
            var _this = this;
            var nodeType;
            var formData = this.get('formData');
            var data = this._getFormData(formData);
            var formItem;
            if(!params.type || !params.name) return null;

            var multiple = params.isMultiple ? 'multiple=multiple' : '';
            Y.mix(params, { multiple: multiple });

            //构造表单前的事件
            this.fire(EVENT_NAME.beforeBuildItem, { params: params, handle: _this, data: data, isViewMode: _this.get('plainText') });
            if (this.get('beforeBuildItem')) {
                this.get('beforeBuildItem')({ params: params, data: data, handle: _this, isViewMode: _this.get('plainText') });
            }

            nodeType = this.getTplType(params.type);
            if(!nodeType) {
                throw new Error('加载失败');
            }

            if (DynamicForm.TEMPLATE[nodeType] || params.formItem) {
                formItem = this._getFormItem(nodeType, params);
                //最基本的类型
                this._buildFormItemDesc(formItem, params);

                //构建表单项的验证规则
                if (this.validator) {
                    this._buildFormItemValidator(params);
                    if (params.required) {
                        formItem.setAttribute('required', true);
                    }
                }

                //绑定change事件
                this._bindChangeHandler(formItem, params);
                //构造表单后的事件
                this.fire(EVENT_NAME.afterBuildItem, { params: params });

                //填充表单值前的事件
                this.fire(EVENT_NAME.beforeSetValue, { params: params, formItem: formItem, data: data });
                //复合类型
                if (params.mixed || params.type === 'group') {
                    this.fire(EVENT_NAME.afterSetValue, { params: params, formItem: formItem, data: data });
                    return params.buildTypeResult || params.formItem;
                }
                // 常规表单项的填值
                this._setCommonFieldValue(formItem, data, params);

            }
            return params.buildTypeResult || formItem;
        },
        /**
         * 常规表单项的填值
         * @private
         * @method
         * @param {}
         * @return
         */
        _setCommonFieldValue: function (formItem, data, params) {
            if(params.varUrl) {
                this.setValueByUrl(formItem, params);
            } else {
                //复杂类的表单项初始化
                this._buildComplexInput(formItem, params);
                //设置value
                this._setItemValue(formItem, params);
                this.fire(EVENT_NAME.afterSetValue, { params: params, formItem: formItem, data: data });
                if (this.get('afterSetValue')) {
                    this.get('afterSetValue').call(this, { params: params, formItem: formItem, data: data });
                }
            }
        },
        /**
         * 绑定表单valuechange的事件和处理回调
         * @method _bindChangeHandler
         */
        _bindChangeHandler: function(formItem, params) {
            var _this = this;
            var valueChangeCallback = this.get('valueChangeCallback');
            var changParams = {};
            if(!params.type) {
                return;
            }
            if (!formItem || ! (formItem instanceof Y.Node)) {
                return;
            }
            //监听change事件
            formItem.delegate('change', function(e) {
                var oldValue = formItem.getData('oldValue');
                var value = $Form.field(formItem, params.name);
                formItem.setData('oldValue', value);
                changParams = { params: params, formItem: formItem, e: e, newVal: value, oldVal: oldValue };
                if(valueChangeCallback && oldValue !== value) {
                    valueChangeCallback.call(_this, changParams);
                }
                _this.fire('valueChange', changParams);
            }, 'select, textarea, input');
        },
        /**
         * 绑定datepicker select事件
         * @method _bindDatepickerHandler
         */
        _bindDatepickerHandler: function(formItem, params, datepicker) {
            var valueChangeCallback = this.get('valueChangeCallback');
            var _this = this;
            datepicker.on('Datepicker.select', function(e) {
                var oldValue = formItem.getData('oldValue');
                var value = e.inputValue;
                formItem.setData('oldValue', value);
                var changParams = {params: params, formItem: formItem, e: e, inputValue: value, newVal: value, oldVal: oldValue };
                if(valueChangeCallback) {
                    valueChangeCallback.call(_this, changParams);
                }
                _this.fire('valueChange', changParams);
                //手动执行表单验证
                if (e.input && _this.validator) {
                    _this.validator.validatorWhileBlur(e.input);
                }
            });
        },
        /**
         * 构建item的一些描述信息
         * @method _buildFormItemDesc
         */
        _buildFormItemDesc: function(formItem, params) {
            var tips = params.tips,
                placeholder = params.placeholder,
                formItemInput = formItem.one('input[type="text"], textarea, input[type="search"], input[type="url"], input[type="tel"], input[type="email"], input[type="password"]');
            if(tips) {
                //支持\n进行多行显示
                if(tips.indexOf('\n') !== -1) {
                    tips = tips.replace(/\n/g, '<br />');
                }
                formItem.append(Y.Lang.sub(DynamicForm.TEMPLATE.tips, {tips: tips}));
            }
            if (placeholder && formItemInput) {
                formItemInput.setAttribute('placeholder', placeholder);
            }
        },
        /**
         * @method fillFormData
         * @param {Function} callback 获取值
         */
        fillFormData: function(callback) {
            var formData = this.get('formData');
            var _this = this;
            if(!formData.url) return;
            Y.io(formData.url, {
                method: 'GET',
                data: formData.query || {},
                on:  {
                    success: function(id, o) {
                        var res = $Util.getEvalRes(o);
                        _this.fire(EVENT_NAME.getFormDataSuccess, {res: res});
                        if(res.status) {
                            callback(_this.formatAjaxFormData(res.data));
                        }
                    },
                    failure: function(id, o) {
                        _this.fire(EVENT_NAME.getFormDataFailure, {res: o});
                    }
                }
            });
        },
        /**
         * 获得模板的类型
         * @method getTplType
         */
        getTplType: function(type) {
            if(type === 'radio' || type === 'checkbox') {
                return 'specialInput';
            }
            return type;
        },
        /**
         * 类型转换
         * @method formatFormItemType
         */
        formatFormItemType: function(params) {
            var typeDefine = this.get('typeDefine');
            var type = params.type;
            //存储原始类型
            params.originalType = type;
            Y.Object.some(typeDefine, function(item, p) {
                item = trim(item);
                if(item.indexOf(',') !== -1) {
                    Y.Array.each(item.split(','), function(item) {
                        item = trim(item);
                        if(item === type) {
                            params.type = p;
                        }
                    });
                } else if(item === type) {
                    params.type = p;
                }
            });
        },
        /**
         * 根据url获得表单数据
         * @method getValueByUrl
         */
        setValueByUrl: function(nd, params) {
            var actionParams = this.get('actionParams');
            var _this = this;
            Y.io(params.varUrl || params.url, {
                method: 'GET',
                data: actionParams,
                on: {
                    success: function(id, o) {
                        var res = $Util.getEvalRes(o);
                        if(res) {
                            params.data = res.data;
                            params.value = res.data;
                            //复杂类的表单项初始化
                            _this._buildComplexInput(nd, params);
                            //设置value
                            _this._setItemValue(nd, params);
                            if (_this.get("plainText")) {
                                _this._replaceInputWithValue(params, nd);
                            }
                            _this.fire(EVENT_NAME.afterSetValue, { params: params, formItem: nd,  data: res.data });
                        }
                    },
                    failure: function(id, o) {
                        _this.fire(EVENT_NAME.setValueFail, { nd: nd, res: o });
                    }
                }
            });
        },
        /**
         * 生成复杂表单元素
         * @private
         * @method _buildComplexInput
         * @param {}
         * @return {}
         */
        _buildComplexInput: function (nd, params) {
            var type = params.type;
            if (type === 'select') {
                this._buildSelectOption(nd, params);
            } else if (type === 'radio') {
                nd.prepend(this._buildRadio(params));
            } else if (type === 'checkbox') {
                nd.prepend(this._buildCheckbox(params));
            } else if (type === 'date') {
                Y.mix(params, {node: nd.one('input')});
                this._buildDatepicker(nd, params);
            }
        },
        /**
         * 设置value
         * @method _setItemValue
         * @params {Object} params
         * params.data的格式
         * <ul>
         *     <li>format1: {data: {'123': 'text', '234': 'text1'}}</li>
         *     <li>format2: {data: [{key: '123', value: "text"}, {key:'234', value:'text1'}], key: 'key', value: 'value' }
         * </ul>
         */
        _setItemValue: function(nd, params) {
            var value = this._getItemData(params.name, params.value, params.groupIndex);
            params.value = value;
            if (params.editable === false) {
                this.setDiseditable(nd);
            }
            if (params.readonly === true) {
                this.setReadonly(nd);
            }
            if (params.type === "checkbox" && Y.Lang.isString(value)) {
                value = value.split(",");
            }
            if (value !== "")  $Form.field(nd, params.name, value);
            nd.setData('oldValue', value);
        },
        /**
         * 获得当前表单项应该被填充的值
         * @method _getItemData
         */
        _getItemData: function(name, value, groupIndex) {
            value = this._getDefaultValue(name, groupIndex);
            var formData = this.get('formData');
            if(formData) {
                var data = formData.data;
                if(data && data[name] !== undefined ) {
                    return data[name];
                } else if (formData[name]) {
                    return formData[name];
                }
            }
            if (value === undefined) value = "";
            return value;
        },
        /**
         * 获得默认值
         * @method _getDefaultValue
         */
        _getDefaultValue: function(name, groupIndex) {
            return (groupIndex !== undefined && groupIndex !== null) ? this.metaList[groupIndex][name].value : this.metaList[name].value;
        },
        setDiseditable: function(nd) {
            nd.all('input, select, textarea').setAttribute('disabled', 'disabled').addClass("widget-dynamic-form-readonly");
        },
        setReadonly: function(nd) {
            nd.all('input, select, textarea').setAttribute('readonly', 'readonly');
        },
        /**
         * 时间控件
         * @method _buildDatepicker
         */
        _buildDatepicker: function(nd, params) {
            var datepicker = new Y.mt.widget.Datepicker(params);
            this._bindDatepickerHandler(nd, params, datepicker);
        },
        /**
         * 构建select option
         * @method _buildSelectOption
         */
        _buildSelectOption: function(nd, params) {
            if(params.data && params.data.resultData) {
                params.data.data = params.data.resultData;
            }
            $Form.setOptions(nd.one('select'), params.data);
        },
        /**
         * 构建radio
         * @method _buildRadio
         */
        _buildRadio: function(params) {
            return this._buildSpecialInput(params, 'radio');
        },
        /**
         * 构建checkbox
         * @method _buildCheckbox
         */
        _buildCheckbox: function(params) {
            return this._buildSpecialInput(params, 'checkbox');
        },
        _getDataList: function (params) {
            var list = params.data;
            var dataList = list[params.dataList];
            return dataList;
        },
        _getKeyField: function (params) {
            var list = params.data;
            var key = list["keyField"] || 'keyField';
            return key;
        },
        _getValueField: function (params) {
            var list = params.data;
            var value = list["valueField"] || 'valueField';
            return value;
        },
        getFieldValue: function(name, groupIndex) {
            return this._getItemData(name, null, groupIndex);
        },
        /**
         * 生成特殊的input
         * @method _buildSpecialInput
         */
        _buildSpecialInput: function(params) {
            var list = params.data;

            if(!list) return null;
            //params.dataList 默认为resultData
            var dataList = this._getDataList(params),
                inputTemp = DynamicForm.TEMPLATE.input,
                temp = [];

            if (!dataList) return null;
            if (isArray(dataList)){
                //default key value
                var key = this._getKeyField(params);
                var value = this._getValueField(params);
                Y.Array.each(dataList, function(item) {
                    if (item.hide) {
                        return;
                    }
                    Y.mix(params, {valueField: item[value], keyField: item[key]}, true);
                    var inputString = Y.Lang.sub(inputTemp, params);
                    var ndInput = $N.create(inputString);
                    temp.push(ndInput);
                });
                return new Y.NodeList(temp).toFrag();
            } else if (!isEmpty(dataList)) {
                Y.Object.each(dataList, function(dataValue, dataKey) {
                    Y.mix(params, {valueField: dataValue, keyField: dataKey}, true);
                    temp.push(Y.Lang.sub(inputTemp,  params));
                });
                return temp.join("");
            }
        },
        /**
         * 构造简单文本框
         * @private
         * @method
         * @param {}
         * @return {}
         */
        _buildInput: function (param) {
            var text = DynamicForm.TEMPLATE.cleantext;
            param.width = parseInt(param.width, 10) || 50;
            if (!param.type) {
                param.type = "";
            }
            if (!param.hasOwnProperty("value")) {
                param.value = "";
            }
            return Y.Lang.sub(text, param);
        },
        /**
         * 设置type的对应关系
         * @method setTypeDefine
         */
        setTypeDefine: function(types) {
            Y.mix(types, defaultType);
            return types;
        },
        /**
         * 将textarea放到最后
         * @method changeTextareaPosition
         */
        changeTextareaPosition: function() {

        }
    }, {
        CSS_PREFIX: 'widget-dynamic-form',
        ATTRS: {
            /**
             * 容器
             * @attribute node
             * @type Node
             */
            node: {
                value: null
            },
            /**
             * 表单项数据请求时一起发到后台的query参数
             * @attribute actionParams
             */
            actionParams: {
                value: {}
            },
            /**
             * 数据和控件中使用数据之间的对应关系
             * @attribute dataRelation
             * @type Object
             */
            dataRelation: {
                value: {}
            },
            /**
             * 类型定义
             * @attribute typeDefine
             * @type Object
             */
            typeDefine: {
                value: {},
                setter: 'setTypeDefine'
            },
            /**
             * 控件数据
             * @attribute data
             * @type Object
             */
            data: {
                value: {}
            },
            /**
             * groups信息
             * @attribute groups
             * @type Object
             */
            groups: {
                value: null
            },
            /**
             * 设置table的class
             * @attribute tableClassName
             * @type String
             */
            tableClassName: {
                value: 'widget-dynamic-table'
            },
            /**
             * @attribute formData
             * @type Object
             */
            formData: {
                value: null
            },
            /**
             * 验证相关的参数设置
             * @attribute validatorConf
             * @type Object
             */
            validatorConf: {
                value: null
            },
            /**
             * 展示的列的个数和宽度等配置
             * @attribute columnConf
             */
            columnConf: {
                value: {
                    count: 1
                }
            },
            /**
             * TODO 待修改，名字不准确
             * 是否将textarea单独占一行并显示到末尾
             * @attribute newLineForTextarea
             */
            newLineForTextarea: {
                value: false
            },
            /**
             * 是否将textarea单独占一行显示
             * @attribute fullLineTextarea
             */
            fullLineTextarea: {
                value: false
            },
            /**
             * 是否将最后一个item的colspan设为占满该行剩余部分
             * @attribute fullLineTextarea
             */
            fullLineLastItem: {
                value: true
            },
            /**
             * 在每个item构建前的自定义方法
             * @attribute beforeBuildItem
             */
            beforeBuildItem: {
                value: null
            },
            /**
             * 在每个item赋值后的自定义方法
             * @attribute afterSetValue
             */
            afterSetValue: {
                value: null
            },
            /**
             * 是否自动render
             * @attribute autoRender
             */
            autoRender: {
                value: true
            },
            /**
             * 是否只显示纯文本
             * @attribute plainText
             */
            plainText: {
                value: false
            },
            valueChangeCallback: {
                value: false
            },
            //是否对表单数据进行编码
            isEncode: {
                value: false
            },
            //是否显示冒号
            showColon: {
                value: false
            },
            //是否展示tab头
            hideTab: {
                value: false
            },
            // Whether skips the security step of HTML escaping the value
            allowHTML: {
                value: true
            }
        },
        CSS_NANE: CSS_NANE,
        TEMPLATE: {
            form: '<form class="{formCssName}" method="post" action="{action}"></form>',
            tab: '<div data-name="{dataName}" class="widget-dynamic-form-tab"><div class="widget-dynamic-form-content"></div></div>',
            tabTitle: '<ul class="nav nav-tabs widget-dynamic-form-desc"><li class="active"><a href="#">{desc}</a></li></ul>',
            specialInput: '<div id="{id}" class="{itemCssName}"></div>',
            select: '<span id="{id}" class="{itemCssName}"><select name={name} {multiple}></select></span>',
            date: '<span id="{id}" class="{itemCssName}"><input type="text" name="{name}" value="{value}" class="f-text widget-dynamic-form-text"/></span>',
            text: '<span id="{id}" class="{itemCssName}"><input type="text" name="{name}" value="{value}" class="f-text widget-dynamic-form-text" /><span>',
            input: '<label style="{style}" class="widget-dynamic-form-inputbox {type}"><input type="{type}" class="widget-dynamic-form-input" name="{name}" value="{keyField}" />{valueField}</label>',
            textarea: '<span id="{id}" class="{itemCssName}"><textarea name="{name}" class="f-text" value="{value}"></textarea></span>',
            cleantext: '<span><input type="text" class="f-text" name="{name}" style="width:{width}px;" value="{value}" widget-type="{type}" /></span>',
            td: '<th>{desc}</th><td><div style="position: relative;" data-attrcode="{attrcode}" class="' + CSS_NANE.itemBoxName + '"></div></td>',
            headline: '<th colspan="2"><div class="widget-dynamic-form-headline" style="position: relative;" data-attrcode="{attrcode}"></div></th>',
            noTitleTd: '<td colspan="2"><div class="widget-dynamic-form-noTitleTd ' + CSS_NANE.itemBoxName + '" style="position: relative;" data-attrcode="{attrcode}"></div></td>',
            tr: '<tr class="{trCssName}"></tr>',
            tableContainer: '<table class="{className}"><tbody></tbody></table>',
            required: '<span class="'+ CSS_NANE.required +'">*</span>',
            valueBox: '<span class="widget-dynamic-form-valuebox">{value}</span>',
            tips: '<div class="widget-dynamic-form-tips">{tips}</div>'
        },
        EVENT_NAME: EVENT_NAME
    });

    Y.namespace("mt.widget").DynamicForm = DynamicForm;


}, 1.0, { requires:["mt-base","mt-form", 'w-form-validate', 'w-tab', 'w-date', 'escape', 'widget', 'node', 'json', 'io'], skinnable: true });
