/**
 * 对平时比较复杂的form操作的拆分, 如form中数据toObject、toString、 checkbox toggle等等功能
 * @module mt-form
 */
M.add('mt-form', function(Y){

    Y.namespace('mt.form');

    var $Util = Y.mt.util,
        isStr = Y.Lang.isString,
        $Lang = Y.mt.lang,
        trim = Y.Lang.trim,
        isNum = Y.Lang.isNumber,
        isArray = Y.Lang.isArray,
        isObject = Y.Lang.isObject,
        isEmpty = Y.Object.isEmpty,
        $Macro = Y.mt.macro,
        eUC = encodeURIComponent,
        $IO = Y.io;

    var FieldMethod = {
        /**
         * 设置表单项, 名称匹配的所有表单项都会被赋值
         * @param {Node} ndForm
         * @param {String} name
         * @param {String|Array} value
         */
        _setValue: function(ndForm, selector, value) {
            var nlFields = ndForm.all(selector);
            var _this = this;
            value = trim(value);
            nlFields.each(function(item) {
                var type = item.get('type');
                var itemValue = item.get('value');
                switch (type) {
                    case 'select-one':
                        _this._setSelectValue(item, value);
                        break;
                    case 'radio':
                    case 'checkbox':
                        if(_this._matchValue(itemValue, value)) {
                            item.set('checked', 'checked');
                        }
                        break;
                    default:
                        item.set('value', value);
                }
            });
        },
        /**
         * 针对所有名称匹配的表单项进行值的打包
         * @param {Node} ndForm
         * @param {String} name
         * @return {Array} res
         */
        _getValue: function(ndForm, selector, encode) {
            var fields = ndForm.all(selector);
            var res = [], val;
            var _this = this;
            fields.each(function(item) {
                var type = item.get('type');
                if(!_this._checkType(type))  return;
                switch (type) {
                    case 'select-one':
                        val = item.get('value');
                        res.push(val);
                        break;
                    case 'select-multiple':
                        item.get('options').each(function(opt) {
                            if (opt.get('selected')) {
                                val = opt.get('value') || opt.get('text');
                                res.push(val);
                            }
                        });
                        break;
                    case 'radio':
                    case 'checkbox':
                        if (item.get("checked")) {
                            val = item.get('value');
                            res.push(val);
                        }
                        break;
                    default:
                        val = item.get('value');
                        if(val !== undefined) {
                            res.push(val);
                        }
                }
            });
            Y.Array.each(res, function(val, index) {
                val = trim(val);
                if(encode) res[index] = eUC(val);
            });
            return res;
        },
        /**
         * 设置/获取form中某些表单项的值
         * @method field
         * @param {Node | Selector} ndForm 表单对象
         * @param {String} name 表单项name
         * @param {String | Array} value 需要设置的值
         * @return {String | Array | Boolean} form某字段的值,若为设置模式,则返回是否设置成功
         */
        _field: function(ndForm, selector, value) {
            var field, type, val;
            if(this._checkValue(value)) {
                return this._setValue(ndForm, selector, value);
            } else {
                field = ndForm.one(selector);
                if(!field) return;
                type = field.get('type');
                if(!this._checkType(type))  return;
                val = this._getValue(ndForm, selector);
                switch (type) {
                    case 'select-multiple':
                    case 'checkbox':
                        return val;
                    default:
                        if(val.length === 0) return;
                        if(val.length === 1) return val[0];
                        return val;
                }
            }
        },
        /**
         * 判断itemValue是否在value所表示的字符串或者数组中
         * @param {String} cv current value
         * @param {String | Array} value
         * @param {String} multipleJoin
         * @return {Boolean} true|false
         */
        _matchValue: function(cv, value, multipleJoin) {
            multipleJoin = multipleJoin || ',';
            if(!isArray(value)) value = value.toString().split(multipleJoin);
            return $Lang.inArray(cv, value, false);
        },
        /**
         * 设置select的值
         * @method _setSelectValue
         * @param {Node} ndField
         * @param {String} value 设置等于此值的某个option为选中状态
         * @return {Boolean} 设置是否成功
         */
        _setSelectValue: function(ndField, value) {
            var setFlag = false;
            var _this = this;
            ndField.get('options').some(function(opt) {
                if(_this._matchValue(opt.get('value'), value)) {
                    opt.set('selected', 'selected');
                    setFlag = true;
                    return true;
                }
            });
            return setFlag;
        },
        /**
         * name组合后获得的节点
         * @method _getFieldsByNames
         * @param {Node} ndForm
         * @param {Array|String} fField 组合的表单项名字
         * @return {NodeList}
         * @private
         */
        _getFieldsByNames: function(ndForm, fField) {
            var selectors = [];
            if (fField) {
                //只将表单中某些field转化为object
                if (isArray(fField)) {
                    Y.Array.each(fField, function(item) {
                        selectors.push('[name="' + item + '"]');
                    });
                } else if (isStr(fField)) {
                    selectors.push('[name="' + fField + '"]');
                }
                return ndForm.all(selectors.join(','));
            }
            return null;
        },
        /**
         * 检查表单项的值是否正确
         * @method _checkValue
         * @private
         */
        _checkValue: function(value) {
            return value !== "undefined" && ( isArray(value) || isStr(value) || isNum(value));
        },
        /**
         * 不支持的表单项类型
         * @method _checkType
         * @param {String} type
         * @return {Boolean} true|false
         */
        _checkType: function(type) {
            switch(type) {
                case 'file':
                case undefined:
                case 'reset':
                case 'button':
                    return false;
                default:
                    return true;
            }
        }
    };

    /**
     * 基于YUI3的form常用操作的扩展
     * @class form
     * @namespace mt
     * @static
     */
    Y.mt.form = {
        /**
         * 获得表单转化后的对象, 可以转化部分表单字段，也可以转换整个表单(fField为空)
         * @method toObject
         * @param {Node | Selector} ndForm
         * @param {String | Array | null} [fField] 某些表单字段
         * @param {Boolean} [isEncode=true] 表单字段编码,默认为true
         * @param {String} [joinKey] 值间的连接符
         * @param {String} multipleJoin 多选等表单项值进行使用multipleJoin进行join
         * @param {Boolean} includeDisabled 是否需要打包disabled的表单项
         * @return {Object} 表单或其部分字段转化成的键值对
         */
        toObject: function( ndForm, fField, isEncode, joinKey, multipleJoin, includeDisabled) {
            ndForm = Y.one(ndForm);
            //默认为true;
            if(isEncode !== false) isEncode = true;
            var ndFields = FieldMethod._getFieldsByNames(ndForm, fField);
            return this._serialize(ndForm, ndFields, includeDisabled, isEncode, joinKey, multipleJoin).formObj;
        },
        /**
         * 获得form或者某些字段序列化后的字符串
         * @method toString
         * @param {Node | Selector} ndForm
         * @param {Array | String} [fField] 转化某些表单项
         * @param {String} [isEncode=true] 是否需要编码
         * @param {String} [joinKey] 值的连接符
         * @param {Boolean} includeDisabled 是否需要打包disabled的表单项
         * @return {String} form或者某些字段序列化后的字符串
         */
        toString: function(ndForm, fField, isEncode, joinKey, includeDisabled) {
            ndForm = Y.one(ndForm);
            var ndFields = FieldMethod._getFieldsByNames(ndForm, fField);
            return this._serialize(ndForm, ndFields, includeDisabled, isEncode, joinKey).formStr;
        },
        /**
         * 带层级关系的form 转化为JSON对象
         * @method toJSON
         * @param {Node | Selector} ndForm
         * @param {Boolean} isEncode 是否对表单项进行编码
         * @param {Boolean} includeDisabled 是否需要打包disabled的表单项
         * @param {String} multipleJoin 多选等表单项值进行使用multipleJoin进行join
         * @return {JSON}
         */
        toJSON: function(ndForm, includeDisabled, isEncode, multipleJoin) {
            ndForm = Y.one(ndForm);
            if (!ndForm) return;
            if(typeof isEncode === 'undefined') isEncode = false;
            if(typeof multipleJoin === 'undefined') multipleJoin = ',';
            var formObj = this._serialize(ndForm, null, includeDisabled, isEncode).formObj;
            var result = {}, _this = this;
            Y.Object.each(formObj, function(obj, p) {
                if(multipleJoin && isArray(obj)) obj = obj.join(multipleJoin);
                _this._parseArr(result, p, obj);
            });
            return result;
        },
        /**
         * 设置/获取form中某些表单项的值
         * @method field
         * @param {Node | Selector} ndForm 表单对象
         * @param {String} name 表单项name
         * @param {String | Array} value 需要设置的值
         * @return {String | Array | Boolean} form某字段的值，若为设置模式，则返回是否设置成功
         */
        field: function(ndForm, name, value) {
            ndForm = Y.one(ndForm);
            if(!ndForm) return;
            var selector = '[name="'+ name +'"]';
            return FieldMethod._field(ndForm, selector, value);
        },
        /**
         * 采用seletor来获得表单项
         * 设置/获取form中某些表单项的值
         * @method fieldBySeletor
         */
        fieldBySeletor: function(ndForm, selector, value) {
            ndForm = Y.one(ndForm);
            if(!ndForm) return;
            return FieldMethod._field(ndForm, selector, value);
        },
        /**
         * 填充select框的html对象
         * @method setOptions
         * @param {Selector} selector
         * @param {Object | Array} list 两种数据格式
         * <ul>
         *     <li>format1: {data: {'123': 'text', '234': 'text1'}}</li>
         *     <li>format2: {data: [{key: '123', value: "text"}, {key:'234', value:'text1'}], key: 'key', value: 'value' }
         * </ul>
         * @param {String} [tips] 提示信息
         * @param {Node} [scale] 限制select的获得范围
         */
        setOptions: function(selector, list, tips, scale) {
            var ndSelect = scale ? scale.one(selector) : Y.one(selector);
            var _this = this;
            var listData = list.data;
            if (!ndSelect) return;

            ndSelect.empty();
            var options = Y.Node.getDOMNode(ndSelect).options;

            if (tips) {
                options.add(this._buildOption('', tips));
            }

            if (isArray(listData)){
                //default key value
                var key = list.key || list.keyField || 'keyField';
                var value = list.value || list.valueField || 'valueField';
                Y.Array.each(listData, function(item) {
                    options.add(_this._buildOption(item[key], item[value]));
                });
            } else if (!isEmpty(listData)) {
                for(var p in listData) {
                    if (listData.hasOwnProperty(p)) {
                        options.add(this._buildOption(p, listData[p]));
                    }
                }
            }
        },
        /**
         * 创建option
         * @method _buildOption
         * @private
         */
        _buildOption: function(value, text) {
            return new Option(text, value);
        },
        /**
         * 返回所要的一组checkbox中被选中的那部分的某个属性
         * @method getCheckedBoxesAttr
         * @param {Selector} selector 匹配一组checkbox，默认为页面所有checkbox
         * @param {String} [attr="value"] 要获取的checkbox的属性，默认值为'value'
         * @param {String | Node} [scale] 某个范围内的checkbox，默认为整个页面
         * @return {Array} 获得的一组checkbox中被选中的部分的属性数组
         */
        getCheckedBoxesAttr : function( selector, attr, scale ) {
            attr = attr || 'value';
            selector = selector || 'input[type=checkbox]';
            var allSelect = scale ? Y.one(scale).all(selector): Y.all(selector);
            var arr = [];
            allSelect.each(function(item) {
                //只处理类型为checkbox的对象
                if ( item.get('type') === 'checkbox' && item.get('checked') ) {
                    arr.push(item.get(attr) || item.getAttribute(attr));
                }
            });
            return arr;
        },
        /**
         * 返回所要的一组radio中被选中那个的某个属性
         * @method getRadioAttr
         * @param {String} selector 匹配一组radio，默认为页面所有radio
         * @param {String} [attr="value"] 要获取的radio的属性，默认值为'value'
         * @param {String | Node} [scale] 某个范围内的radio，默认为整个页面
         * @return {String} 获得的一组radio中被选中者的属性值
         */
        getRadioAttr: function( selector, attr, scale ) {
            attr = attr || 'value';
            selector = selector || 'input[type="radio"]';
            var allRadio = scale ? Y.one(scale).all(selector): Y.all(selector);
            var ret = '';
            allRadio.some(function(item) {
                if (item.get('type') === 'radio' && item.get('checked')) {
                    ret = (trim(item.get(attr)) || item.getAttribute(attr));
                    return true;
                }
            });
            return ret;
        },
        /**
         * 获取select中选中的内容
         * @method getOptionsText
         * @param {Node | Selector} ndForm  所属的form
         * @param {String} name select的name值
         * @return {String} select中选中的项的内容
         */
        getOptionsText: function(ndForm, name) {
            if (!ndForm) return null;
            var s = ndForm.all('select[name="'+name+'"]');
            if (s.size() === 0) return null;
            var text = [];
            s.each(function(item) {
                item.get('options').some(function(opt) {
                    if (opt.get('selected')) {
                        text.push(opt.getHTML() || "");
                        return true;
                    }
                });
            });
            return text.length === 1 ? text[0] : text;
        },
        /**
         * 点击节点toggle给定的checkbox节点
         * @method toggleCheckbox
         * @param {Node | Selector} trigger elTarget: 事件对象
         * @param {Selector} selector  全部选中或者全部不选中的对象
         * @param {Node | Selector} scale 某个范围内的checkbox，默认为整个页面
         */
        toggleCheckbox: function(trigger, selector, scale) {
            trigger = Y.one(trigger);
            if (!trigger) return;

            trigger.on('click', function() {
                var nlCheckbox = scale ? Y.one(scale).all(selector) : Y.all(selector);
                if (trigger.get('checked')) {
                    nlCheckbox.set('checked', 'checked');
                } else {
                    nlCheckbox.set('checked', false);
                }
            });
        },
        /**
         * 阻止回车提交表单行为
         * @method
         * @param {Node | Selector} ndForm
         */
        stopEnterSubmit: function(ndForm) {
            ndForm.delegate('keydown', function (e) {
                if (e.keyCode === $Macro.key.ENTER) {
                    e.halt();
                }
            }, 'input[type]');
        },
        /**
         * Form可能有多个不同action，通过不同的按钮触发
         * @method multiAction
         * @param {Node | Selector} ndForm
         * @param {Object} actions 所有action及其对应的行为
         * [{
         *  selector: ".button1",
         *  action: "/myaction",
         *  callback: myfunction
         * }]
         */
        multiAction: function (ndForm, actions) {
            ndForm = Y.one(ndForm);
            if(!ndForm) return;
            if (!actions || !Y.Lang.isArray(actions) || actions.length === 0) {
                return;
            }
            var selectors = getSelectors(actions);
            ndForm.delegate("click", function () {
                for (var i = 0; i < actions.length; i++) {
                    var action = actions[i];
                    if (this.test(action.selector)) {
                        ndForm.set("action", action.action);
                        if (action.callback && typeof action.callback === "function") {
                            action.callback(ndForm, this);
                        }
                        return;
                    }
                }
            }, selectors);
            function getSelectors (actions) {
                var selectors = [];
                for (var i = 0; i < actions.length; i++) {
                    var action = actions[i];
                    selectors.push(action.selector);
                }
                return selectors.join(",");
            }
        },
        /**
         * ajax表单提交
         * @method AjaxForm
         * @param {Node} ndForm
         * @param {Object} params 参数设置
         *
         * <dl>
         *     <dt>action{String}</dt>
         *     <dd>post的表单地址</dd>
         *     <dt>method{Function}</dt>
         *     <dd>表单提交的方式</dd>
         *     <dt>query{Object}</dt>
         *     <dd>同时允许的查询参数</dd>
         *     <dt>buttonPost{Selector|Node}</dt>
         *     <dd>表单提交的触发按钮</dd>
         *     <dt>beforeAjax{Function}</dt>
         *     <dd>发请求前的自定义验证</dd>
         *     <dt>handleSuccess{Function}</dt>
         *     <dd>成功的处理函数</dd>
         *     <dt>handleFail{Function}</dt>
         *     <dd>失败的处理函数</dd>
         * </dl>
         */
        AjaxForm: function(ndForm, params) {
            var _this = this;
            ndForm = Y.one(ndForm);
            if(!ndForm) return;
            var action = ndForm.get('action') || params.action;
            var method = params.method || ndForm.get('method') || "POST";
            var trigger = params.buttonPost ? Y.one(params.buttonPost) : ndForm;
            var triggerEvent = params.buttonPost ? 'click' : 'submit';
            var multiAction = params.multiAction;

            if (!ndForm.test('form')) {
                throw new Error("请保证你所提交的对象是一个form");
            } else if (trim(action) === '') {
                throw new Error("表单提交地址(action)不能为空");
            }

            if (multiAction) {
                var triggers = this._getMultiTriggers(multiAction);
                ndForm.delegate('click', function (e) {
                    e.halt();
                    // 根据trigger不同发送请求到不同的Action
                    _this.sendRequest2ActionByTrigger(this, params, method, ndForm);
                }, triggers);
            } else {
                trigger.on(triggerEvent, function(e) {
                    e.halt();
                    if (params.beforeAjax) {
                        //验证通过才能发请求
                        if(params.beforeAjax()) {
                            _this.sendRequest(action, method, params, ndForm);
                        }
                    } else {
                        _this.sendRequest(action, method, params, ndForm);
                    }
                });
            }
        },
        /**
         * 获取触发submit事件的trigger选择器
         * @private
         * @method
         */
        _getMultiTriggers: function(multiAction) {
            var s = [];
            for (var ac in multiAction) {
                if (multiAction.hasOwnProperty(ac)) {
                    s.push(ac);
                }
            }
            return s.join(",");
        },
        /**
         * 根据trigger不同发送请求到不同的Action
         * @private
         * @method
         * @param param
         * @return return
         */
        sendRequest2ActionByTrigger: function(ndTrigger, params, method, ndForm) {
            //设置对应的action
            var _this = this;
            var action;
            var beforeAjaxs = params.beforeAjaxs;
            var multiAction = params.multiAction;
            for (var ac in multiAction) {
                if (multiAction.hasOwnProperty(ac)) {
                    if (ndTrigger.test(ac)) {
                        action = multiAction[ac];
                        if (beforeAjaxs && beforeAjaxs[ac]) {
                            //验证通过才能发请求
                            if (beforeAjaxs[ac](ndTrigger)) {
                                _this.sendRequest(action, method, params, ndForm);
                            }
                        } else {
                            _this.sendRequest(action, method, params, ndForm);
                        }
                    }
                }
            }
        },
        /**
         * 发送请求
         * @private
         * @method
         * @param param
         * @return return
         */
        sendRequest: function (action, method, params, ndForm) {
            $IO(action, {
                method: method,
                data: Y.mt.util.toPostData(params.query || {}),
                on: {
                    success: function(id, o) {
                        var res = $Util.getEvalRes(o, '权限不够或者网络有问题，请稍后重试。');
                        if (res.status) {
                            if (params.handleSuccess) {
                                params.handleSuccess(res);
                            }
                        } else {
                            if(params.handleFail) {
                                params.handleFail(res);
                            } else {
                                Y.config.win.alert(res.msg);
                            }
                        }
                    },
                    failure: function() {
                        Y.config.win.alert('权限不够或者网络有问题，请稍后重试。');
                    }
                },
                form: { id: ndForm.get('id') }
            });
        },
        /**
         * 清空form
         * @method clear
         * @param {Node | Selector} ndForm
         */
        clear: function(ndForm, simple, exclude) {
            ndForm = Y.one(ndForm);
            if (!ndForm) return;
            var textType = ["hidden", "text", "number", "email", "url", "search"];
            ndForm.all('input,select,textarea').each(function(item) {
                var t = item.get('type'), tag = item.get('nodeName').toLowerCase();
                if ($Lang.inArray(t, textType) || tag === 'textarea') {
                    if (exclude && item.test(exclude)) {
                        return;
                    }
                    item.set('value','');
                } else if (!simple) {
                    if (t === 'checkbox' || t === 'radio') {
                        item.set('checked', false);
                    } else if (tag === 'select') {
                        item.set('selectedIndex', -1);
                    }
                }
            });
        },
        /**
         * 还原form初始值
         * @method reset
         * @param {Node | Selector} ndForm
         */
        reset: function(ndForm) {
            ndForm = Y.one(ndForm);
            if (!ndForm) return;
            if (ndForm.get('nodeName').toLowerCase() === 'form') ndForm.reset();
        },
        /**
         * 嵌套对象转化为平铺对象
         *
         * {"a": {"b": ["c", "d"]}} ==> {"a.b[0]": "c", "a.b[1]": "d"}
         *
         * @private
         * @method _nestedObject2Flat
         * @param {Object} object 嵌套的object
         * @return {Object} 平铺的object
         */
        _nestedObject2Flat: function (object) {
            if (!isObject(object)) {
                return object;
            }
            function object2Flat(flatObj, object, prefix) {
                Y.Object.each(object, function (value, key) {
                    var fieldName = prefix ? prefix + "." + key : key;
                    toFlatObj(flatObj, value, fieldName);
                });
            }
            function array2FlatObject(flatObj, array, prefix) {
                Y.Array.each(array, function (value, index) {
                    var fieldName = prefix + "[" + index + "]";
                    toFlatObj(flatObj, value, fieldName);
                });
            }
            function toFlatObj(flatObj, value, fieldName) {
                if (isArray(value)) {
                    array2FlatObject(flatObj, value, fieldName);
                } else if (isObject(value)) {
                    object2Flat(flatObj, value, fieldName);
                } else {
                    flatObj[fieldName] = value;
                }
            }
            var flatObj = {};
            object2Flat(flatObj, object);
            return flatObj;
        },
        /**
         * 填充各个表单域的值
         * @method fillForm
         * @param {Node | Selector} form 表单或表单的选择器
         * @param {Object} values 各个表单域对应的值，为json格式
         * @return {Boolean} 执行结果
         */
        fillForm: function (form, values) {
            var ndForm = Y.one(form);
            if(!ndForm || !values) {
                return false;
            }
            if(!Y.Object.isEmpty(values)) {
                // 嵌套对象转化为平铺对象
                values = this._nestedObject2Flat(values);
                for( var fName in values ) {
                    if (values.hasOwnProperty(fName)) {
                        this.field(ndForm, fName, values[fName]);
                    }
                }
            }
            return true;
        },
        /**
         * 序列化整个form
         * @method serialize
         * @param {String} formId form的id值
         * @param {Boolean} [includeDisabled=false] 是否序列化disabled元素，默认为false
         * @param {Boolean} [isEncode=true] 是否进行编码，默认true
         * @param {String} [joinKey] 值的连接符
         * @return {String} 序列化的form字符串
         */
        serialize: function(form, includeDisabled, isEncode, joinKey) {
            if(!(form instanceof Y.Node)) {
                form = Y.one('#' + form);
            }
            return this._serialize(form, null, includeDisabled, isEncode, joinKey).formStr;
        },
        /**
         * @method _serialize
         * @param {Node} ndForm
         * @param {NodeList} ndFields 选择性打包
         * @param {String} [joinKey='&'] form转String需要值链接参数
         * @param {String} multipleJoin form转Object需要
         * @param {Boolean} [includeDisabled=false] 是否序列化disabled元素，默认为false
         * @param {Boolean} [isEncode=true] 是否进行编码，默认true
         * @private
         */
        _serialize: function(ndForm, ndFields, includeDisabled, isEncode, joinKey, multipleJoin) {
            //默认为true;
            if(isEncode !== false) isEncode = true;
            //joinKey 默认为&
            if(joinKey === undefined || !Y.Lang.isString(joinKey)) {
                joinKey = "&";
            }
            var formObj = {}, arr = [],
                name, _nameCache = {}, value, selector;

            ndFields = ndFields || ndForm.all('input, select, textarea');
            ndFields.each(function(item) {
                name = item.get('name');
                selector = '[name="'+ name +'"]';
                if((!item.get('disabled') || includeDisabled) && name) {
                    if (_nameCache[name]) return;
                    value = FieldMethod._getValue(ndForm, selector, isEncode);
                    //组合为a=b, 并根据条件进行编码
                    if(value.length !== 0) {
                        if(multipleJoin) {
                            value = value.join(multipleJoin);
                            //由于名称中有可能出现[]有意义的字符, name暂时不编码
                            arr.push(name + '=' + value);
                        } else {
                            Y.Array.each(value, function(val) {
                                arr.push(name + '=' + val);
                            });
                        }
                        if(value.length === 1) value = value[0];
                        formObj[name] = value;
                    }
                    _nameCache[name] = true;
                }
            });
            return { formStr: arr.join(joinKey), formObj: formObj };
        },
        /**
         * 禁用某一个节点，用于防止表单重复提交
         * @method disableSubmit
         * @param {Selector | HTMLElement | Node} ndDisabledNode 要被禁用的节点
         * @param {String} [className] 禁用要添加的样式类，请注意设置其优先级，避免被覆盖看不到效果
         * @param {Object} [styles] 样式列表，用于设置内联样式
         */
        disableSubmit: function(ndDisabledNode, className, styles) {
            var backData = {},
                defaultStyles = {'background-color': '#EEE'},
                currentStyle = '';
            ndDisabledNode = Y.one(ndDisabledNode);
            styles = styles || {};
            if (!className) {
                Y.mix(styles, defaultStyles);
            }
            className = className ? className : 'disabled';
            if (!ndDisabledNode) {
                return;
            }
            //保存原有样式
            backData.className = className;
            backData.styles = '';
            currentStyle = Y.Node.getDOMNode(ndDisabledNode).getAttribute('style');
            if (typeof currentStyle === 'string') {
                backData.styles = currentStyle;
            } else {
                //针对ie6/7 特别处理 set&getAttribute 在ie6/7 下对集合属性返回对象 style 用 cssText
                backData.styles = currentStyle = currentStyle ? currentStyle.cssText : '';
            }
            ndDisabledNode.setData('backupStyles', backData);
            //禁用 并添加相应的样式
            ndDisabledNode.setAttribute('disabled', 'disabled');
            ndDisabledNode.addClass(className);
            for (var p in styles) {
                if (styles.hasOwnProperty(p)) {
                    currentStyle += ';' + p +':'+ styles[p];
                }
            }
            // 标准浏览器设置,在ie6/7中无效
            ndDisabledNode.removeAttribute('style');
            ndDisabledNode.setAttribute('style', currentStyle);
            // 兼容 ie6/7
            ndDisabledNode._node.style.cssText = currentStyle;
        },
        /**
         * 启用被禁用的节点
         * @method enableSubmit
         * @param {Selector | HTMLElement | Node} ndEnabledNode 要被启用的节点
         */
        enableSubmit: function(ndEnabledNode) {
            var backupStyles = null;
            ndEnabledNode = Y.one(ndEnabledNode);
            if (!ndEnabledNode) {
                return;
            }
            //启用
            ndEnabledNode.removeAttribute('disabled');
            backupStyles = ndEnabledNode.getData('backupStyles');
            if (backupStyles) {
                ndEnabledNode.removeClass(backupStyles.className);
                // 如果不先removeAttribute的话 在Ie 里面会引起后面设置的样式被已有样式影响
                ndEnabledNode.removeAttribute('style');
                ndEnabledNode.setAttribute('style', backupStyles.styles);
                //兼容 ie 6 , 7  之前的设置在ie67中无效
                ndEnabledNode._node.style.cssText = backupStyles.styles;
            }
        },
        /**
         * 将字符串匹配为配合转化为json对象的数组
         * @method _execStr
         * @private
         */
        _execStr: function(str) {
            var result;
            var e = /([\w\u4e00-\u9fa5]+)(["']?)(\]?)(\[?)(["']?)/g;
            var arrR = [];
            //依次匹配类似“.a[”或“[1]”的部分，一层层递归
            while ((result = e.exec(str)) !== null) {
                arrR.push(result);
                //如果匹配到的不是最后一层，则将正则对象的lastIndex向前移动，因为每个要匹配的值与前一层都有重叠
                if (result[3]) {
                    e.lastIndex = e.lastIndex - result[3].length;
                }
            }
            return arrR;
        },
        /**
         * parse exec data
         * @method _parseArr
         * @private
         */
        _parseArr: function(s, str, val) {
            var ss = s;
            //ss类似指针，指向json对象的当前操作层级
            str = str.replace(/\.([\w\u4e00-\u9fa5]+)/g, '["$1"]');
            var arr = this._execStr(str);
            Y.Array.each(arr, function(i){
                var main = i[1];
                //将空白字符串转为0
                var whiteSpace = /^\s*$/;
                if (whiteSpace.test(main)) {
                    main = 0;
                }
                if (ss[main] === undefined) {
                    if (i[4] && i[5]) {
                        //对象
                        ss[main] = {};
                    } else if (i[4]){
                        ss[main] = [];
                    } else {
                        ss[main] = val || '';
                    }
                }
                ss = ss[main];
            });
        }
    };

}, '1.0.0', {
    requires: [
        'mt-base',
        'io-base',
        'node'
    ]
});

