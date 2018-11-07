/**
 * 表单验证组件
 * @module w-form-validate
 */
M.add('w-form-validate', function(Y) {

    var ValidateBase = Y.mt.widget.ValidateBase;

    var CSS_NAME = {
        tipInfo: 'widget-validate-tip-info',
        inlineTip: 'inline-widget-validate-tip-info',
        redBorder: 'border-red'
    };
    var errorTpl = '<span class="error-icon"></span><span class="' + CSS_NAME.inlineTip +'">{msg}</span>';

    /**
     * 表单验证组件
     * @class FormValidate
     * @namespace mt.widget
     * @constructor
     * @extends Widget
     */
    var FormValidate = Y.Base.create('w-form-validate', ValidateBase, [], {
        /**
         * 初始化
         * @method initializer
         */
        initializer: function() {
            this.bubbleTips = {};
            this.on("ValidateBase:ADD_TIP", this.addTip);
            this.on("ValidateBase:REMOVE_TIP", this.removeTip);
            this.on("ValidateBase:REMOVE_ALL_TIPS", this.removeAllTips);
        },
        /**
         * 设置提示tip
         * @method addTip
         * @param {Object} data 数据对象包含name，node，msg等信息
         */
        addTip: function(data) {
            var name = data.name,
                msg = data.msg,
                errTpl = Y.Lang.sub(errorTpl, {msg: msg}),
                node = data.node,
                _this = this,
                nodeType,
                ndGroup,
                nlNode,
                ndNode,
                ndTip;


            if (!node) return;

            node.addClass(CSS_NAME.redBorder);
            nodeType = node.get('type');

            if (nodeType === 'radio' || nodeType === 'checkbox') {
                nlNode = this.get('form').all('[name="' + name + '"]');
                ndNode = nlNode.item(nlNode.size()-1);
                ndGroup = ndNode.ancestor();
            } else {
                ndNode = node;
                ndGroup = node.ancestor();
            }
            ndTip = ndGroup.one('.' + CSS_NAME['tipInfo'] );

            if (!ndTip) {
                if (this.get('tipType') === 'right') {
                    ndGroup.append('<span class="' + CSS_NAME.tipInfo + '">' + errTpl + '</span>');
                } else if (this.get('tipType') === 'bottom') {
                    ndGroup.append('<div class="' + CSS_NAME.tipInfo + '">' + errTpl + '</div>');
                } else if (this.get('tipType') === 'bubble') {
                    if (_this.bubbleTips[name]) {
                        _this.bubbleTips[name].setTip(msg);
                        _this.bubbleTips[name].show();
                    } else {
                        var bubbleTip = new Y.mt.widget.BubbleTip({
                            node: ndNode,
                            alignment: 'right',
                            distance: 20,
                            tip: msg,
                            show:'true',
                            showType: 'click',
                            forceClose: 'true'
                        }).render();
                        _this.bubbleTips[name] = bubbleTip;
                    }
                }
            } else {
                ndTip.one('.' + CSS_NAME.inlineTip).setHTML(msg);
            }
            this.fire('addTipsSuccess', { error: data });
        },
        /**
         * 移除所有提示
         * @method removeAllTip
         */
        removeAllTips: function() {
            if (this.get('tipType') === 'bubble') {
                Y.Object.each(this.bubbleTips, function(item) {
                    item.close();
                });
                this.bubbleTips = {};
            } else {
                this.get('form').all('.'+ CSS_NAME['tipInfo']).remove();
            }
        },
        /**
         * 移除提示tip
         * @method removeTip
         * @param {Object} data 数据对象包含name，node，msg等信息
         */
        removeTip: function(data) {
            var name = data.name,
                ndNode = data.node,
                nlNode = this.get('form').all('[name="'+name+'"]'),
                size = nlNode.size();


            var ndGroup = size > 1 ? nlNode.item(nlNode.size() - 1).ancestor() : ndNode.ancestor();
            var ndTip = ndGroup.one('.' + CSS_NAME['tipInfo'] );
            if(ndNode.hasClass(CSS_NAME.redBorder)) {
                ndNode.removeClass(CSS_NAME.redBorder);
            }
            if (ndTip) {
                ndTip.remove();
            }
            if (this.bubbleTips[name]) {
                this.bubbleTips[name].close();
            }
        }
    }, {
        CSS_NAME: CSS_NAME,

        RULES: {
            required: true
        },
        ATTRS: {
            /**
             * 提示信息
             *
             */
            tipType: {
                value: 'right',
                writeOnce: true
            }
        }

    });
    FormValidate.hasFailured = function(ndContainer) {
        if (!ndContainer) {
            return;
        }
        return ndContainer.one("."+CSS_NAME.tipInfo);
    };
    FormValidate.getFailured = function(ndContainer) {
        if (!ndContainer) {
            return;
        }
        return ndContainer.all("."+CSS_NAME.tipInfo);
    };
    FormValidate.clearFailured = function(ndContainer) {
        if (!ndContainer) return;
        var tipInfo = CSS_NAME.tipInfo;
        ndContainer.all("." + tipInfo).remove();
        ndContainer.all('.' + CSS_NAME.redBorder).removeClass(CSS_NAME.redBorder);
    };

    Y.mt.widget.FormValidate = FormValidate;

},'1.0.0', { requires: ['w-core', 'mt-base', 'w-bubbletip', 'w-validate-base'], skinnable: true });

