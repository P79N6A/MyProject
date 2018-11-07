/*jshint white:true, unused:true*/
/**
 * 气泡提示控件
 * @module w-bubbletip
 */
M.add('w-bubbletip', function (Y) {
    var $Util = Y.mt.util;

    var CONTENT_TPL = '<div class="content">{tip}</div><span class="triangle-border"></span><span class="triangle-fill"></span>',
        WITH_CLOSE_CONTENT_TPL = '<div class="content">{tip}</div><a class="close" title="关闭">×</a><span class="triangle-border"></span><span class="triangle-fill"></span>',
        WidgetPositionAlign = Y.WidgetPositionAlign,
        alignMap = {
            top:    [WidgetPositionAlign.BC, WidgetPositionAlign.TC],
            right:  [WidgetPositionAlign.LC, WidgetPositionAlign.RC],
            bottom: [WidgetPositionAlign.TC, WidgetPositionAlign.BC],
            left:   [WidgetPositionAlign.RC, WidgetPositionAlign.LC],
            // 以下为特殊的对齐方式
            // 如：top-left位于目标结点上方，且与该节点左对齐
            'top-left': [WidgetPositionAlign.BL, WidgetPositionAlign.TL],
            'top-right': [WidgetPositionAlign.BR, WidgetPositionAlign.TR],
            'right-top': [WidgetPositionAlign.TL, WidgetPositionAlign.TR],
            'right-bottom': [WidgetPositionAlign.BL, WidgetPositionAlign.BR],
            'bottom-left': [WidgetPositionAlign.TL, WidgetPositionAlign.BL],
            'bottom-right': [WidgetPositionAlign.TR, WidgetPositionAlign.BR],
            'left-top': [WidgetPositionAlign.TR, WidgetPositionAlign.TL],
            'left-bottom': [WidgetPositionAlign.BR, WidgetPositionAlign.BL],
            // 以下为另一类特殊的对齐方式
            // 如：top-left-area为目标结点上方的左侧区域
            'top-left-area': [WidgetPositionAlign.BR, WidgetPositionAlign.TC],
            'top-right-area': [WidgetPositionAlign.BL, WidgetPositionAlign.TC],
            'right-top-area': [WidgetPositionAlign.BL, WidgetPositionAlign.RC],
            'left-top-area': [WidgetPositionAlign.BR, WidgetPositionAlign.LC],
            'right-bottom-area': [WidgetPositionAlign.TL, WidgetPositionAlign.RC],
            'left-bottom-area': [WidgetPositionAlign.TR, WidgetPositionAlign.LC],
            'bottom-left-area': [WidgetPositionAlign.TR, WidgetPositionAlign.BC],
            'bottom-right-area': [WidgetPositionAlign.TL, WidgetPositionAlign.BC]
        };

    /**
     * @class BubbleTip
     * @namespace mt.widget
     * @extends Overlay
     * @constructor
     */
    var BubbleTip = function () {
        BubbleTip.superclass.constructor.apply(this, arguments);
    };

    /**
     * @property NAME
     * @type String
     * @static
     */
    BubbleTip.NAME = 'BubbleTip';
    /**
     * @property CSS_PREFIX
     * @type String
     * @static
     */
    BubbleTip.CSS_PREFIX = 'widget-bubbletip';
    /**
     * @property STATUS_HIDE
     * @type Number
     * @static
     */
    BubbleTip.STATUS_HIDE = 0;
    /**
     * @property STATUS_SHOW
     * @type Number
     * @static
     */
    BubbleTip.STATUS_SHOW = 1;
    /**
     * @property ATTRS
     * @type Object
     * @static
     */
    BubbleTip.ATTRS = {
        /**
         * 触发节点
         * @attribute node
         * @type Selector|HTMLElement|Node
         */
        node: {
            value: null,
            setter: Y.one,
            writenOnce: true
        },
        /**
         * 冒泡提示层ID
         * @attribute id
         * @type String
         */
        id: {
            value: '',
            writeOnce: true
        },
        /**
         * 提示内容
         * @attribute tip
         * @type String
         */
        tip: {
            value: '',
            writeOnce: true
        },
        /**
         * 对齐方式
         * @attribute alignment
         * @type String ['top', 'right', 'bottom', 'left',
         * 'left-top', 'left-bottom', 'right-top', 'right-bottom',
         * 'top-left', 'top-right', 'bottom-left', 'bottom-right',
         * 'left-top-area', 'left-bottom-area', 'right-top-area', 'right-bottom-area',
         * 'top-left-area', 'top-right-area', 'bottom-left-area', 'bottom-right-area']
         * @default 'bottom'
         */
        alignment: {
            value: 'bottom'
        },
        /**
         * 与触发节点间距，支持负数值
         * @attribute distance
         * @type Number
         * @default 5
         */
        distance: {
            value: 5
        },
        /**
         * 初始状态是否显示
         * @attribute show
         * @type Boolean
         * @default false
         */
        show: {
            value: false
        },
        /**
         * 显示延迟时间(ms)
         * @attribute showDelay
         * @type Number
         * @default 200
         */
        showDelay: {
            value: 200
        },
        /**
         * 隐藏延迟时间(ms)
         * @attribute hideDelay
         * @type Number
         * @default 500
         */
        hideDelay: {
            value: 500
        },
        /**
         * 是否显示关闭按钮
         * @attribute showClose
         * @type Boolean
         * @default false
         */
        showClose: {
            value: false
        },
        /**
         * 是否需要点击关闭按钮才能关闭提示
         * @attribute forceClose
         * @type Boolean
         * @default false
         */
        forceClose: {
            value: false
        },
        /**
         * tip的显示触发事件
         * @attribute showType
         * @type String
         * @default 'mouseover'
         */
        showType: {
            value: 'mouseover'
        },
        /**
         * tip的隐藏触发事件
         * @attribute hideType
         * @type String
         * @default 'mouseout'
         */
        hideType: {
            value: 'mouseout'
        },
        /**
         * show/hide/close时是否使用Anim动画
         * @attribute anim
         * @type Boolean
         * @default true
         */
        anim: {
            value: true
        },
        /**
         * 主题色，默认为浅黄色背景深色文字，可赋为"dark"即深色背景浅色文字
         * NOTE: 鉴于主站设计规范需求，新增两套主题："common"为白色背景灰色边框的主题，"noarrow"为白色背景灰色边框并没有三角图标的主题
         * @attribute theme
         * @type String
         * @default light 
         */
        theme: {
            value: 'light'
        }
    };

    Y.extend(BubbleTip, Y.Overlay, {
        /**
         * 初始化
         * @method initializer
         * @private
         */
        initializer: function () {
            var instance = this;

            instance._timer = null;
            instance._status = BubbleTip.STATUS_HIDE;
            instance._isMouseOver = false;
            instance._handles = {};

            instance.publish('show', {
                emitFacade: true,
                defaultFn: instance._defShowFn
            });
            instance.publish('hide', {
                emitFacade: true,
                defaultFn: instance._defHideFn
            });
            // 点击关闭按钮关闭气泡
            instance.publish('close', {
                emitFacade: true,
                defaultFn: instance._defCloseFn
            });
        },
        /**
         * 析构
         * @method destructor
         */
        destructor: function () {
            $Util.detachHandle(this._handles);
        },
        /**
         * 渲染UI
         * @method renderUI
         */
        renderUI: function () {
            var instance = this,
                id = instance.get('id'),
                tip = instance.get('tip'),
                contentBox = instance.get('contentBox'),
                show = instance.get('show'),
                showClose = instance.get('showClose'),
                forceClose = instance.get('forceClose'),
                zIndex = instance.get('zIndex'),
                theme = instance.get('theme');

            if (showClose || forceClose) {
                contentBox.setHTML(Y.Lang.sub(WITH_CLOSE_CONTENT_TPL, { tip: tip }));
                contentBox.addClass(instance.getClassName('with-close'));
            } else {
                contentBox.setHTML(Y.Lang.sub(CONTENT_TPL, { tip: tip }));
            }
            if (theme) {
                contentBox.addClass('theme-' + theme);
            }

            instance.set('zIndex', zIndex || 9999);
            instance.set('visible', false);
            if (id) {
                instance.get('boundingBox').set('id', id);
            }

            if (show) {
                instance.show();
            }
        },
        /**
         * 绑定UI事件
         * @method bindUI
         */
        bindUI: function () {
            var instance = this,
                ndTrigger = instance.get('node'),
                ndContentBox = instance.get('contentBox'),
                forceClose = instance.get('forceClose'),
                showType = instance.get('showType'),
                hideType = instance.get('hideType'),
                ndWin = Y.one(window);

            // 记录鼠标是否在触发节点或提示节点上
            instance._handles.mouseOver = Y.all([ndTrigger, ndContentBox]).on({
                mouseover: function () {
                    instance._isMouseOver = true;
                },
                mouseout: function () {
                    instance._isMouseOver = false;
                }
            });

            instance._handles.showByTrigger = ndTrigger.on(showType, function () {
                if (showType === hideType && instance._status === BubbleTip.STATUS_SHOW) return;
                instance._delayOperation('show', instance.show);
            });
            if (!forceClose) {
                instance._handles.hideByTrigger = ndTrigger.on(hideType, function () {
                    setHide();
                });
                instance._handles.hideByContent = ndContentBox.on(hideType, function () {
                    setHide();
                });
            }
            instance._handles.close = ndContentBox.delegate('click', function () {
                instance.close();
            }, '.close');
            instance._handles.resize = ndWin.on('resize', $Util.throttle(function () {
                instance._setAlign();
            }));
            instance._handles.pageheight = Y.Global.on('PageHeight:change', $Util.throttle(function () {
                instance._setAlign();
            }));

            function setHide() {
                if (showType === hideType && instance._status === BubbleTip.STATUS_HIDE) return;

                if (hideType  === 'mouseout') {
                    instance._delayOperation('hide', instance.hide);
                } else {
                    instance.close();
                }
            }
        },
        /**
         * 延迟执行方法
         * @method _delayOperation
         * @private
         * @param {String} operate
         * @param {Function} callback
         */
        _delayOperation: function (operate, callback) {
            var instance = this,
                delay = instance.get(operate + 'Delay');

            if (instance._timer) {
                window.clearTimeout(instance._timer);
                instance._timer = null;
            }

            instance._timer = window.setTimeout(function () {
                callback.call(instance);
            }, delay);
        },
        /**
         * 显示气泡
         * @method show
         * @return {Object}
         */
        show: function () {
            this.fire('show');
            return this;
        },
        /**
         * 显示气泡默认方法
         * @method _defShowFn
         * @private
         */
        _defShowFn: function () {
            var instance = this,
                ndContentBox = instance.get('contentBox'),
                anim;

            if (instance._status === BubbleTip.STATUS_SHOW) return;

            ndContentBox.setStyle('opacity', 0);
            instance.set('visible', true);
            instance._status = BubbleTip.STATUS_SHOW;
            instance._setAlign();
            if (instance.get('anim')) {
                anim = new Y.Anim({
                    node: ndContentBox,
                    to: { opacity: 1 },
                    duration: 0.15
                });
                anim.run();
            } else {
                ndContentBox.setStyle('opacity', 1);
            }
        },
        /**
         * 隐藏气泡
         * @method hide
         * @param {Boolean} force
         * @return {Object}
         */
        hide: function (force) {
            if (force || !this._isMouseOver) {
                this.fire('hide');
            }
            return this;
        },
        /**
         * 隐藏气泡默认方法
         * @method _defHideFn
         * @private
         * @param {Object} e
         */
        _defHideFn: function () {
            var instance = this,
                ndContentBox = instance.get('contentBox'),
                anim;
            if (instance._status === BubbleTip.STATUS_HIDE) return;

            if (instance.get('anim')) {
                anim = new Y.Anim({
                    node: ndContentBox,
                    to: { opacity: 0 },
                    duration: 0.15
                });

                anim.on('end', function () {
                    instance.set('visible', false);
                    instance._isMouseOver = false;
                    instance._status = BubbleTip.STATUS_HIDE;
                });
                anim.run();
            } else {
                ndContentBox.setStyle('opacity', 0);
                instance.set('visible', false);
                instance._isMouseOver = false;
                instance._status = BubbleTip.STATUS_HIDE;
            }
        },
        /**
         * 关闭气泡
         * @method close
         * @return {Object}
         */
        close: function () {
            this.fire('close');
            return this;
        },
        /**
         * 关闭气泡默认方法
         * @method _defCloseFn
         * @private
         * @param {Object} e
         */
        _defCloseFn: function () {
            this.hide(true);
        },
        /**
         * 设置气泡对齐位置
         * @method _setAlign
         * @private
         */
        _setAlign: function () {
            var instance = this,
                alignment = instance.get('alignment'),
                specialAlignment = alignment.split('-'),
                distance = instance.get('distance'),
                ndContentBox = instance.get('contentBox'),
                node = instance.get('node'),
                offsets = {
                    top: 'auto',
                    right: 'auto',
                    bottom: 'auto',
                    left: 'auto'
                };

            ndContentBox.addClass(instance.getClassName(specialAlignment[0]));
            //如果是特殊的对齐方式
            if (specialAlignment[1]) {
                ndContentBox.addClass(instance.getClassName(alignment));
            }
            if (alignMap[alignment]) {
                instance.set('align', {
                    node: node,
                    points: alignMap[alignment]
                });
            }
            //distance支持负值
            distance = -1 * parseInt(distance, 10);
            offsets[specialAlignment[0]] = distance + 'px';
            ndContentBox.setStyles(offsets);
        },
        /*
         * 动态设置tip内容
         * @method setTip
         * @param {String|Node} tip 要设置的内容
         */
        setTip: function (tip) {
            if (!tip) return;
            var contentBox = this.get('contentBox'),
                ndCont = contentBox.one('.content');

            this.set('tip', tip);
            if (ndCont) {
                ndCont.setHTML(tip);
            }
        }
    });
    Y.mt.widget.BubbleTip = BubbleTip;

}, '1.0.0', {
    requires: [
        'mt-base',
        'anim',
        'overlay',
        'node'
    ],
    skinnable: true
});
 
