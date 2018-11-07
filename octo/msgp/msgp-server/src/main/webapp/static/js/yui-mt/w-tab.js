/*jshint white:true, unused:true*/
/**
 * 通用tab切换代码，不依赖于HTML结构
 *
 * @module w-tab
 */
M.add('w-tab', function (Y) {
    var CE_TAB_ONSHOW = 'Tab.onShow';

    Y.namespace('mt.widget');

    /**
     * 原理：一个tab对象分为控制部分(trigger)，内容部分(sheet)。当trigger被触发时，显示对应的sheet。
     * 使用：
     * <ul id="t">
     *  <li id="t1"></li>
     *  <li id="t2"></li>
     * </ul>
     * <div id="s">
     *  <div id="s1"></div>
     *　<div id="s2"></div>
     * </div>
     * 编码
     * 示例1 var tab = new Tab(Y.all('#t li'), Y.all('#s div'));
     * 示例2 var tab = new Tab(['t1','t2'], ['s1','s2']);
     * 示例3 var tab = new Tab(['t1','t2'], ['s1','s2'], { triggerEvent:'mouseover',slideEnabled:true});
     * 扩展：已经实现自动切换功能(默认关闭)，另外可以通过onShow自定义事件扩展。
     * @class TabBase
     * @namespace mt.widget
     * @constructor
     * @extends Widget
     */
    var TabBase = Y.Base.create('tab', Y.Widget, [], {
        /**
         * 初始化
         * @method initializer
         * @private
         */
        initializer: function () {

            //初始化tabs数组
            this.tabs = this.addTabs(this.get('nlTrigger'), this.get('nlSheet'));
            if (this.tabs.length === 0) return;

            //vertical tab
            this.arrowMap = { left: '&#x25b6;', down: '&#x25bc;' };
            //当前tab
            this.currentTab = null;
            //控件是否位于动态切换过程中
            this.inAnimate = false;
            //是否为第一帧
            this.firstShow = true;

            //是否自动初始化
            if (this.get('autoInit')) this.render();
        },
        /**
         * @method renderUI
         */
        renderUI: function () {
            this._setSkin(this.get('defaultSkin'));
            if (this.get('vertical')) {
                this._renderVerticalUI();
            }
        },
        /**
         * 默认的皮肤，若需要换肤，可以直接改变skin即可
         * @method _setSkin
         * @param {Boolean} skin
         * @private
         */
        _setSkin: function (skin) {
            var node = this.get('node'),
                tabCss = '';

            //自定义普通tab皮肤
            if (skin) {
                tabCss = TabBase.CSS_NAME.cssTab;
            }
            //verticalTab Skin
            if (this.get('vertical')) {
                tabCss = TabBase.CSS_NAME.cssVertical;
            }
            if (node) {
                node.addClass(tabCss);
            }
        },
        /**
         * @method bindUI
         */
        bindUI: function () {
            var _this = this;
            var tabs = this.tabs;
            var node = this.get('node');
            var index = this.get('defaultIndex');

            this.on(CE_TAB_ONSHOW, function (data) {
                //切换完成的自定义事件
                _this.get('onShow')(data.tab);
            });

            this.after('verticalChange', function () {
                this._renderVerticalUI();
                if (node) {
                    node.addClass(TabBase.CSS_NAME.cssVertical);
                }
            });

            if (this.get('slideEnabled')) this.slide();

            Y.Array.each(tabs, function (tab) {
                tab.sheet.setStyle('display', 'none');
                tab.trigger.on(_this.get('triggerEvent'), function () {
                    if (!_this.inAnimate) {
                        _this.select.call(_this, tab);
                    }
                });
            });
            //tab初始选中
            this.select(tabs[index]);
        },
        /**
         * 重置样式
         * @method _resetSelect
         * @private
         */
        _resetSelect: function () {
            var _this = this;
            Y.Array.each(this.tabs, function (tab) {
                if (tab.trigger.hasClass('current')) {
                    _this.currentTab = tab;
                    tab.trigger.removeClass('current');
                }
                if (_this.get('vertical')) {
                    _this._changeIcon(tab, 'left');
                }
                tab.sheet.setStyle('display', 'none');
            });
        },
        /**
         * 默认选中
         * @method _setIndex
         * @param {Number} index
         * @private
         */
        _setIndex: function (index) {
            this.select(this.tabs[index]);
        },
        /**
         * 增加tab数组
         * @method addTabs
         * @param {NodeList} nlTrigger
         * @param {NodeList} nlSheet
         * @return {Array}
         */
        addTabs: function (nlTrigger, nlSheet) {
            var tabs = [];
            nlTrigger.each(function (item, index) {
                if (nlSheet.item(index)) {
                    tabs.push({trigger: item, sheet: nlSheet.item(index)});
                }

            });
            return tabs;
        },
        /**
         * 设置tab为当前tab并显示
         * @method select
         * @param {object} tab  tab对象 {trigger:HTMLElement,sheet:HTMLElement}
         */
        select: function (tab) {
            if (!tab) return;
            //重复点击已选中的trigger的效果
            if (tab.trigger.hasClass('current')) {
                if (this.get('vertical')) {
                    //toggle的效果
                    this._toggleVerticalTab(tab);
                    return;
                }
            }

            this._resetSelect();
            tab.trigger.addClass('current');

            var _this = this;
            if (this.get('animStyle') === 'fade') {
                if (this.currentTab) {
                    var anim = new Y.Anim({
                        node: this.currentTab.sheet,
                        to: { opacity: 0 },
                        duration: this.get('animDuriation'),
                        easing: Y.Easing.easeIn
                    });
                    anim.on('end', function () {
                        _this.inAnimate = false;
                        _this.show.call(_this, tab);
                    });
                    anim.run();
                    this.inAnimate = true;
                } else {
                    this.show(tab);
                }
            } else {
                tab.sheet.setStyle('display', '');
                if (this.get('vertical')) {
                    this._changeIcon(tab, 'down');
                }
                this.fire(CE_TAB_ONSHOW, { tab: tab });
            }
        },
        /**
         * 显示当前被选中的tab
         * @method show
         * @param {Object} tab
         * @return {Boolean}
         */
        show: function (tab) {
            var _this = this;
            if (this.firstShow) {
                this.firstShow = false;
                // 第一帧不需要动画载入
                if (!(this.get('animInit'))) {
                    tab.sheet.setStyle('display', '');
                    this.fire(CE_TAB_ONSHOW, { tab: tab });
                    return true;
                }
            }
            tab.sheet.setStyles({ 'opacity': 0, 'display': '' });
            var anim = new Y.Anim({
                node: tab.sheet,
                to: { opacity: 1 },
                duration: this.get('animDuriation'),
                easing: Y.Easing.easeOut
            });
            anim.on('end', function () {
                _this.inAnimate = false;
                this.fire(CE_TAB_ONSHOW, { tab: tab });
            });
            anim.run();
            this.inAnimate = true;
            return true;
        },
        /**
         * 自动切换
         * @method slide
         */
        slide: function () {
            var _this = this,
                intervalId,
                delayId;
            Y.Array.each(this.tabs, function (tab) {
                Y.on('mouseover', clear, [tab.trigger, tab.sheet]);
                Y.on('mouseout', delay, [tab.trigger, tab.sheet]);
            });
            start();
            function start() {
                var i = Y.Array.indexOf(_this.tabs, _this.currentTab);
                if (i === -1) i = 0;
                intervalId = window.setInterval(function () {
                    var tab = _this.tabs[++i % _this.tabs.length];
                    if (tab) {
                        _this.select(tab);
                    }
                }, _this.get('slideInterval'));
            }
            function clear() {
                window.clearTimeout(delayId);
                window.clearInterval(intervalId);
            }
            function delay() {
                delayId = window.setTimeout(start, _this.get('slideDelay'));
            }
        },
        /**
         * @method _renderVerticalUI
         * @private
         */
        _renderVerticalUI: function () {
            var arrow = Y.Lang.sub(TabBase.TEMPLATE.verticalIcon, Y.mix(TabBase.CSS_NAME, { iconName: this.arrowMap.left}));
            this.get('nlTrigger').each(function (trigger) {
                trigger.insertBefore(arrow, trigger.get('firstChild'));
                trigger.setAttribute('title', '点击显示/隐藏');
                trigger.addClass(TabBase.CSS_NAME.cssTabTrigger);
            });
            this.get('nlSheet').each(function (sheet) {
                sheet.addClass(TabBase.CSS_NAME.cssTabSheet);
            });
        },
        /**
         * @method _changeIcon
         * @private
         */
        _changeIcon: function (tab, direction) {
            var ndIcon = tab.trigger.one('.' + TabBase.CSS_NAME.cssIcon);
            ndIcon.setHTML(this.arrowMap[direction]);
        },
        /**
         * verticalTab是需要toggle
         * @method _toggleVerticalTab
         * @param {Object} tab
         * @private
         */
        _toggleVerticalTab: function (tab) {
            if (tab.sheet.getStyle('display') === 'block') {
                tab.sheet.setStyle('display', 'none');
                this._changeIcon(tab, 'left');
            } else {
                tab.sheet.setStyle('display', '');
                this._changeIcon(tab, 'down');
            }
        }
    }, {

        CSS_PREFIX: 'widget-tab',
        CSS_NAME: {
            cssIcon: 'widget-tab-vertical-arrow',
            cssTab: 'widget-edit-tab',
            cssTabTrigger: 'widget-tab-trigger',
            cssTabSheet: 'widget-tab-sheet',
            cssVertical: 'widget-tab-vertical'
        },
        TEMPLATE: {
            verticalIcon: '<span class="{cssIcon}">{iconName}</span>'
        },
        ATTRS : {
            nlTrigger: {
                value: null
            },
            nlSheet: {
                value: null
            },
            //默认的tab索引
            defaultIndex: {
                value: 0,
                setter: '_setIndex'
            },
            //默认的触发事件
            triggerEvent: {
                value: 'click'
            },
            //是否自动切换
            slideEnabled: {
                value: false
            },
            //切换时间间隔
            slideInterval: {
                value: 3000
            },
            //鼠标离开tab继续切换的延时
            slideDelay: {
                value: 300
            },
            //是否自动初始化
            autoInit: {
                value: true
            },
            //切换类型 'flat'|'fade' TODO 'slide'
            animStyle: {
                value: 'flat'
            },
            node: {
                value: null
            },
            //渐变时间
            animDuriation: {
                value: 0.4
            },
            //首个tab是否使用动画载入
            animInit: {
                value: false
            },
            //默认的onShow事件处理函数
            onShow: {
                value: function () {}
            },
            defaultSkin: {
                value: false,
                setter: '_setSkin'
            },
            vertical: {
                value: false
            }
        }
    });

    /**
     * @class Tab
     * @namespace mt.widget
     * @static
     */
    var Tab = function (nlTrigger, nlSheet, config) {
        if (!nlTrigger || !nlSheet) return false;
        if (!(nlTrigger instanceof Y.NodeList)) {
            nlTrigger = Y.all(nlTrigger);
        }
        if (!(nlSheet instanceof Y.NodeList)) {
            nlSheet = Y.all(nlSheet);
        }

        if (nlTrigger.size() < 1) {
            Y.error("传入HTML结构引用有误, 请检查!");
        }

        if (!config) config = {};

        config.nlTrigger = nlTrigger;
        config.nlSheet = nlSheet;

        //如果是ul的列表, 默认转换node对象
        config.node = config.node || nlTrigger.item(0).ancestor('ul') || null;

        return new TabBase(config);
    };

    /**
     * 支持data-widget初始化
     * @class TabIndex
     * @namespace mt.widget
     * @static
     */
    var TabIndex = function (nd, config) {
        config.node = nd;
        return new Tab(config.nlTrigger, config.nlSheet, config);
    };

    Y.mt.widget.Tab = Tab;
    Y.mt.widget.TabIndex = TabIndex;

}, '1.0.0', {
    requires: [
        'mt-base',
        'anim',
        'base-build',
        'widget',
        'node',
        'event-custom'
    ],
    skinnable: true
});
 
