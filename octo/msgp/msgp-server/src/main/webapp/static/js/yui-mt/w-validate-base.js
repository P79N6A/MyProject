/**
 * 表单验证组件, 仅返回验证数据
 * @module w-validate-base
 */
M.add('w-validate-base', function(Y) {

    Y.namespace('mt.widget');
    var Validator = Y.mt.extension.Validator;

    /**
     * 表单验证组件
     * @class ValidateBase
     * @namespace mt.widget
     * @constructor
     * @extends Widget
     */
    var ValidateBase = Y.Base.create('w-validate-base', Validator, [], {
        initializer: function() {
            var form = this.get('form');
            if(form) {
                form.addClass(ValidateBase.CSS_PREFIX);
                this.set('container', form);
            }
            this.bindUI();
        },
        /**
         * 绑定事件
         * @method bindUI
         */
        bindUI: function() {
            //form 提交的时候进行验证
            this.formSubmitHandle();

            if (this.get('focusAction') !== false) {
                this.focusEvt();
            }

            if (this.get('blurAction') !== false) {
                this.clickEvt();
                this.blurEvt();
            }
        },
        /**
         * 触发节点单击事件
         * @method formSubmitHandle
         */
        formSubmitHandle: function() {
            var saveAction = this.get('saveAction');
            var ndForm = this.get('form');
            var _this = this;
            if(saveAction && saveAction.nd) {
                var ndNode = this.rulesManager.getNode(saveAction.nd);
                ndNode.on('click', function(e) {
                    _this._submitHandler(e);
                });

            } else {
                ndForm.on('submit', function(e) {
                    _this._submitHandler(e);
                });
            }
        },
        /**
         * @method _submitHandler
         */
        _submitHandler: function(e) {
            var res, callback;

            if (this.get('isHaltAll')) {
                e.halt(true);
            }
            res = this.validateAllRules();
            if(res.result === false) {
                //出错时阻止默认行为, 对表单submit生效
                if(this.get('isHalt')) e.halt(true);
            }
            //执行callback
            callback = this._getCallback();
            if (callback) {
                callback(res);
            }
        },
        /**
         * 获得提交时候的处理的callback
         * @method getCallback
         */
        _getCallback: function() {
            var saveAction = this.get('saveAction');
            if (!saveAction) {
                return null;
            }
            var isFn = Y.Lang.isFunction(saveAction);
            return isFn ? saveAction : saveAction.callback;
        },
        /**
         * 验证所有的节点规则
         * @method validateAllRules
         */
        validateAllRules: function(container) {
            var errorArr, res;
            var _this = this;

            //_this.fire('ValidateBase:REMOVE_ALL_TIPS');
            res = _this.validate(container);

            if (res.result === false) {
                errorArr = res.errorArr;
                _this.fire('ValidateBase:FAILURE', {error: errorArr});

                Y.Array.each(errorArr, function(item) {
                    _this.fire('ValidateBase:ADD_TIP', item);
                });
            } else {

                _this.fire('ValidateBase:SUCCESS');
            }
            return res;
        },
        /**
         * 获得焦点的事件处理函数
         * 获得焦点时候将tips相关信息交由上层处理
         * @method focusEvt
         */
        focusEvt: function() {
            var ndForm = this.get('form'),
                focusAction = this.get('focusAction'),
                _this = this;

            ndForm.delegate('focus', function() {
                var curName = this.getAttribute('name'),
                    tips = _this.rulesManager.getTips(curName),
                    node = this;

                var res = { curName: curName, node: node, msg: tips};
                _this.fire('ValidateBase:REMOVE_TIP', res);
                if (tips) {
                    _this.fire("ValidateBase:ADD_TIP", res);
                    if (Y.Lang.isFunction(focusAction)) {
                        focusAction(res);
                    }
                }
            }, "input[type=text], select, textarea");
        },
        /**
         * radio和checkbox监听单击事件
         * @method clickEvt
         */
        clickEvt: function() {
            var ndForm = this.get('form'),
                _this = this,
                curName,
                tips;

            ndForm.delegate('change', function(e) {
                curName = this.getAttribute('name');
                _this.validatorWhileBlur(e.target);
                tips = _this.rulesManager.getTips(curName);
                if (tips) {
                    _this.fire("ValidateBase:ADD_TIP", { node: e.target, name: curName, msg: tips});
                }
            }, "input[type=radio], input[type=checkbox], select");
        },
        /**
         * 失去焦点事件处理函数
         * @method blurEvt
         */
        blurEvt: function() {
            var _this = this;
            var ndForm = this.get('form');
            ndForm.delegate('blur', function() {
                Y.later(200, this, function() {
                    _this.validatorWhileBlur(this);
                });
            }, "input[type=text], textarea");
        },
        validatorWhileBlur: function (node) {
            var curName = node.getAttribute('name');
            var blurAction = this.get('blurAction');
            if (node.test("input[type=radio], input[type=checkbox]")) {
                var ndForm = this.get("form");
                node = ndForm.one("input[name='"+curName+"']");
            }
            var errorFind = this.validateByNode(node);
            if (errorFind) {
                this.fire("ValidateBase:ADD_TIP", errorFind);
                if (Y.Lang.isFunction(blurAction)) {
                    blurAction(errorFind);
                }
            } else {
                this.fire("ValidateBase:REMOVE_TIP", {name: curName, node:node});
                if (Y.Lang.isFunction(blurAction)) {
                    blurAction({name: curName, node: node});
                }
            }
        }
    }, {
        CSS_PREFIX: 'widget-validate-base',
        RULES: {
            required: true
        },
        ATTRS: {
            /**
             * 目标表单
             * @attribute form
             * @type Node
             */
            form: {
                value: null,
                setter: function(val) {
                    return Y.one(val);
                }
            },
            /**
             * 获得焦点时处理函数
             * @attribute focusAction
             * @type Function
             */
            focusAction: {
                value: true
            },
            /**
             * 失去焦点时处理函数
             * @attribute blurAction
             * @type Function
             */
            blurAction: {
                value: true
            },
            /**
             * 保存时处理函数
             * @attribute saveAction
             * @type Function
             */
            saveAction: {
                value: null
            },
            /**
             * @attribute isHalt
             * 校验失败时是否停止冒泡
             */
            isHalt: {
                value: true
            },
            /**
             * @attribute isHaltAll
             * 校验成功或失败都停止冒泡
             */
            isHaltAll: {
                value: false
            }
        }
    });
    Y.mt.widget.ValidateBase = ValidateBase;

},'1.0.0', { requires: ['w-core', 'mt-base', 'e-validator', 'widget', 'node', 'e-validator/rules']});

