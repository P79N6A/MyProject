/**
 * 遮罩插件
 * @module p-mask
 */
M.add('p-mask', function(Y){

    Y.namespace('mt.plugin');
    /**
     * 遮罩插件
     * @class mask
     * @namespace mt.plugin
     * @extends Plugin.Base
     * @constructor
     */
    var mask = Y.Base.create('mask', Y.Plugin.Base, [], {
        /**
         * 初始化
         * @method initializer
         * @param {Object} config 参数设置
         */
        initializer:function(config) {
            this.ndDoc = this.get('host');
            this.renderUI();
        },

        /**
         * 销毁函数
         * @method destructor
         */
        destructor:function() {
            this.close();
        },

        /**
         * 渲染界面
         * @method renderUI
         */
        renderUI:function() {
            this.open();
            this.buildContent();
        },

        /**
         * 生成内容
         * @method buildContent
         */
        buildContent: function() {
            var width = this.ndDoc.get('offsetWidth'),
                height = this.ndDoc.get('winHeight'),
                content = this.get('content'),
                ndContent = this.ndDoc.one('.overlay-mask-content'),
                styles = this.get('styles');

            if(content) {
                ndContent.setHTML(content);
                var ndWidth = ndContent.get("offsetWidth");
                var ndHeight = ndContent.get("offsetHeight");
                var left = (width - ndWidth)/2;
                var ndTop = (height - ndHeight)/2 + this.ndDoc.get("docScrollY");
                var top = ndTop <= 0 ?  10 : ndTop;
                ndContent.setXY([left, top]);
                if (styles) {
                    ndContent.setStyles(styles);
                }
            } else {
                ndContent.addClass('overlay-mask-hidden');
            }
        },

        /**
         * 打开遮罩
         * @method open
         */
        open: function() {
            var template = {
                width: this.ndDoc.get('docWidth'),
                height:this.ndDoc.get('docHeight'),
                mzIndex:parseInt(this.get('zIndex'), 10) - 1,
                izIndex:parseInt(this.get('zIndex'), 10) - 2
            };
            var maskStr = Y.Lang.sub(mask.TEMPLATE.mask, template);
            this.ndDoc.prepend(maskStr);
        },

        /**
         * 关闭遮罩
         * @method close
         */
        close: function() {
            var ndMask = this.ndDoc.one('.plugin-mask');
            if(ndMask) ndMask.remove();
        }
    }, {
        NS: 'mask',
        TEMPLATE: {
            mask: '<div class="plugin-mask">' +
                '<div id="overlay-mask" class="overlay-mask" style="width:{width}px; height:{height}px; z-index:{mzIndex}; visibility:visible;"></div>' +
                '<div class="overlay-mask-content"></div>' +
                '<iframe id="overlay-iframe" class="overlay-iframe" style="width:{width}; height:{height}; z-index:{izIndex}; visibility:visible;"></iframe>' +
            '</div>'
        },
        ATTRS: {
            /**
             * 设置宿主
             * @attribute doc
             * @type {Selector|HTMLEment|Node}
             */
            doc: {
                value: Y.one(document.body)
            },
            /**
             * 设置内容
             * @attribute content
             * @type {Selector|HTMLElement|Node}
             */
            content: {
                value: ''
            },
            /**
             * 设置遮罩层的堆叠层级
             * @attribute zIndex
             * @type {Number}
             */
            zIndex: {
                value: 10
            },
            /**
             * 设置样式
             * @attribute styles
             * @type {Object}
             */
            styles: {
                value: null
            }
        }
    });

    Y.mt.plugin.Mask = mask;
},

'1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'node'
    ],
    skinnable: true
});
 
