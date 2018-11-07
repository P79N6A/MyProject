/*jshint white:true,unused:true*/
/**
 * Node节点的placeholder插件
 * @module p-node-placeholder
 */
M.add('p-node-placeholder', function (Y) {
    var isIE =  Y.UA.ie,
        trim = Y.Lang.trim;

    var $Util = Y.mt.util,
        ATTR_INPUT_TYPE = "data-placeholder-type",
        changeType = function (el, type) {
            try {
                // IE6/7 not work
                el.type = type;
                return true;
            } catch (e) {
                return false;
            }
        };

    /**
     * Node节点的placeholder插件
     * @class Placeholder
     * @namespace mt.plugin
     * @constructor
     * @extends Plugin.Base
     * @param config {Object}
     */
    function Placeholder() {
        this.handler = {
            submit: null
        };
        Placeholder.superclass.constructor.apply(this, arguments);
    }
    /**
     * @property NAME
     * @type String
     */
    Placeholder.NAME = 'placeholderPlugin';
    /**
     * @property NS
     * @type String
     */
    Placeholder.NS = 'placeholder';
    Placeholder.ATTRS = {
        /**
         * 配置信息
         * @attribute placeholder
         * @type Object
         */
        placeholder: {
            valueFn: '_getPlaceholderValue',
            readOnly: true
        }
    };
    Placeholder.PLACEHOLDER_CLASS = 'placeholder';

    Y.extend(Placeholder, Y.Plugin.Base, {
        /**
         * 初始化
         * @method initializer
         * @private
         */
        initializer: function () {
            var plugin = this,
                host = plugin.get('host'),
                ndForm;

            if (host) {
                ndForm = host.ancestor('form');
            }
            if (!(host instanceof Y.Node) || $Util.isSupportPlaceholder()) return;

            plugin.onHostEvent('focus', plugin.hide);
            plugin.onHostEvent('blur', function () {
                plugin.show(false);
            });
            if (ndForm) {
                // NOTE Submit失败后所有placeholder都被清除，没有恢复，需要手动调用show
                plugin.handler.submit = ndForm.on('submit', function () {
                    plugin.hide();
                });
            }
            if (isIE) {
                // 刷新页面时，IE会在页面load时设置表单记忆值
                $Util.addLoadListener(function () {
                    if (host.get('value')) {
                        host.removeClass(Placeholder.PLACEHOLDER_CLASS);
                    }
                    plugin.show(true);
                });
            }

            plugin.show(true);
        },
        /**
         * 销毁时执行
         * @method destructor
         * @private
         */
        destructor: function () {
            this.hide();
            if (this.handler.submit) {
                this.handler.submit.detach();
            }
        },
        /**
         * 显示placeholder
         * @method show
         * @param {Boolean} loading
         */
        show: function (loading) {
            var host = this.get('host'),
                el = host.getDOMNode(),
                placeholder = this.get('placeholder'),
                type;

            if (host.get('value') === '' || (loading && this._isValueEqualPlaceholder())) {
                // password polyfill
                type = host.getAttribute(ATTR_INPUT_TYPE);
                if (type) {
                    el.type = 'text';
                } else if (host.get('type') === 'password') {
                    if (!changeType(el, 'text')) return;
                    host.setAttribute(ATTR_INPUT_TYPE, 'password');
                }
                host.set('value', placeholder);
                host.addClass(Placeholder.PLACEHOLDER_CLASS);
            }
        },
        /**
         * 隐藏placeholder
         * @method hide
         */
        hide: function () {
            var host = this.get('host'),
                type;
            if (this._isValueEqualPlaceholder()) {
                // password polyfill
                type = host.getAttribute(ATTR_INPUT_TYPE);
                if (type) {
                    host.set('type', type);
                }
                this._removePlaceholder();
                host.removeClass(Placeholder.PLACEHOLDER_CLASS);
            }
        },
        /**
         * 判断host的值是否等于placeholder
         * @method _isValueEqualPlaceholder
         * @private
         * @return {Boolean}
         */
        _isValueEqualPlaceholder: function () {
            return this.get('host').get('value') === this.get('placeholder');
        },
        /**
         * 删除host的值中可能包含的placeholder
         * @method _removePlaceholder
         * @private
         */
        _removePlaceholder: function () {
            var host = this.get('host'),
                placeholder = this.get('placeholder'),
                value = trim(host.get('value')),
                placeholderLength = placeholder.length,
                inputLength = value.length - placeholderLength,
                i,
                str;

            if (placeholder === value) {
                host.set('value', '');
                return;
            }

            // 处理浏览器未及时清理placeholder的情况
            if (value.indexOf(placeholder) === 0) {
                host.set('value', value.substring(placeholderLength));
                return;
            }
            if (value.lastIndexOf(placeholder) === inputLength) {
                host.set('value', value.substring(0, inputLength));
                return;
            }
            for (i = 1; i < (placeholderLength - 1); ++i) {
                str = value.substring(0, i) + value.substring(i + inputLength);
                if (str === placeholder) {
                    host.set('value', value.substring(i, i + inputLength));
                    return;
                }
            }
        },
        /**
         * 获取host的placeholder值
         * @method _getPlaceholderValue
         * @private
         * @return {String}
         */
        _getPlaceholderValue: function () {
            return this.get('host').getAttribute('placeholder');
        }
    });

    Y.namespace('mt.plugin').Placeholder = Placeholder;
},
//version
'1.0.0',
//dependency
{
    requires: ['mt-base', 'plugin']
});
 
