/* jshint maxcomplexity: false */
/**
 * 实现单独的分页展示效果、以及分页事件处理，提供默认的分页点击效果
 * @module w-paginator
 */
M.add('w-paginator', function (Y) {

    var Lang = Y.Lang,
        $Macro = Y.mt.macro;

    Y.namespace('mt.widget');
    /**
     * 分页控件
     * @class Paginator
     * @namespace mt.widget
     * @constructor
     * @example
        //参数handlePage与callback二选一
        var paginator = new Y.mt.widget.Paginator({
            contentBox: '#page2',   // 页面中所对应的dom节点的selector
            max: 20,    // 当前显示最大页数
            index: 10,  // 当前所在的页数¬
            page: 10,
            step: 5,    // 页面跨度
            jump: true,     // 是否需要对页码进行搜索
            callback: function(e){
                Y.log(e.page);
                console.log(e);
            },
            handlePage: {
                url: '/mis/help/widgetyui3',    // 页面刷新的url
                formId: 'test',     // formId所在form的查询参数
                query: {a: '123'}   // url后面跟的query参数
            }
        });
     */
    var Paginator = Y.Base.create('paginator', Y.Widget, [], {
        /**
         * 初始化方法
         * @method initializer
         */
        initializer: function (config) {
            //初始化条件判断
            this._eventHandles = {
                delegate: null
            };
            var totalCount = config.totalCount,
                max = config.max,
                pageSize = this.get('pageSize');
            if (!max && totalCount && pageSize) {
                this.set('max', Math.ceil(totalCount / pageSize));
            }
            this.cBox = this.get('contentBox');
            this.render();
        },
        /**
         * 初始化控件结构
         * @method renderUI
         */
        renderUI: function () {
            //初始化UI结构
            this._buildContainer();
            //跳到多少页
            this._renderJumpUI(this.get('jump'));
            //调整每页显示数量
            this._renderShowPageSizeUI(this.get('showPageSize'));
            //生成每页的UI
            this._renderPage();
            this.setTotalDes(this.get('totalCount'));
        },
        /**
         * 初始化控件总体结构
         * @method _buildContainer
         * @private
         */
        _buildContainer: function() {
            if (this.get('skin') === 'web') {
                Paginator.CSS_CLASS.cssSkin = 'paginator-web';
            }
            this.ndContainer = Y.Node.create(Y.Lang.sub(Paginator.TEMPLATE.pageContainer, Y.mix(Paginator.CSS_CLASS)));
            this.cBox.setHTML(this.ndContainer);
            if (this.get("autoHide") && this.get("max") === 1) {
                this.ndContainer.hide();
            } else {
                this.ndContainer.show();
            }
        },
        /**
         * 初始化控件事件
         * @method bindUI
         */
        bindUI: function () {
            this.after("maxChange", function(){
                this._renderPage();
            });
            this.after("indexChange", function() {
                this._renderPage();
            });
            this.after("totalCountChange", function() {
                this._renderPage();
            });
            this.after("pageSizeChange", function() {
                this._renderPage();
            });

            this._bindMainDelegate();
        },
        /**
         * 设置totalCount
         * @method setTotalDes
         * @param {Number} totalCount
         */
        setTotalDes: function(totalCount) {
            if (totalCount) {
                var ndTotalCount = this.ndContainer.one('.' + Paginator.CSS_CLASS.cssTotalCount);
                ndTotalCount.one('em').setHTML(totalCount);
                ndTotalCount.setStyle('display', 'inline');
            }
        },
        /**
         * 初始化输入页码搜索
         * @method _renderJumpUI
         * @param {Boolean} jump
         * @private
         */
        _renderJumpUI: function (jump) {
            if (jump && !this.ndContainer.one('.paginator-jump')) {
                this.ndContainer.append(Lang.sub(Paginator.TEMPLATE.jump, Y.mix(Paginator.CSS_CLASS)));
            }
        },
        /**
         * 初始化调整每页显示数量
         * @method _renderShowPageSizeUI
         * @param {Boolean} jump
         * @private
         */
        _renderShowPageSizeUI: function (showPageSize) {
            if (!showPageSize) {
                return;
            }
            var pageSize = this.get('pageSize'),
                optionSelected = {
                    option10: '',
                    option20: '',
                    option30: '',
                    option50: ''
                };
            optionSelected['option' + pageSize] = 'selected';
            if (!this.ndContainer.one('.paginator-page-size')) {
                this.ndContainer.prepend(Lang.sub(Paginator.TEMPLATE.pageSize, Y.mix(Y.clone(Paginator.CSS_CLASS), optionSelected)));
            }
        },
        /**
         * 创建一般链接
         * @method _buildButtonLink
         * @param {String} href
         * @param {String} className
         * @param {String} text
         * @param {String} title
         * return {HTMLElement}
         * @private
         */
        _buildButtonLink: function(href, className, text, title) {
            title = title ? title : text;
            return Y.Lang.sub(Paginator.TEMPLATE.link, {classname: className, text: text, title: title, href: href});
        },
        /**
         * 创建默认带分页的链接
         * @method  _buildPageLink
         * @param {Number} i
         * @return {HTMLElements}
         * @private
         */
        _buildPageLink: function(i) {
            var index = this.get('index');
            var href = '#page-'+ i;
            var className = (index === i) ? 'current' : '';
            var title = '第' + i + '页';
            var text = i;
            return this._buildButtonLink(href, className, text, title);
        },
        /**
         * 生成分页的具体页码结构
         * @method _renderPage
         * @private
         */
        _renderPage: function () {
            var step = this.get('step'),
                index = parseInt(this.get('index'), 10),
                max = parseInt(this.get('max'), 10),
                ndContainer = this.ndContainer,
                pageMain = [];

            if(max <= 0 || index <= 0) return;
            //pre page
            var indexMin = (index === 1);
            pageMain.push('<span class="page-prev">');
            if (this.get('skin') === 'web') {
                pageMain.push(this._buildButtonLink("#", indexMin ? "page-disabled" : "page-prev", "<i class='tri'></i>上一页"));
            } else {
                pageMain.push(this._buildButtonLink("#", indexMin ? "page-disabled" : "page-prev", '«'));
            }
            pageMain.push("</span>");

            pageMain.push('<span class="page-main">');
            //render Middle
            var i;
            //跨步数大于最大页数
            if (step >= max) {
                for (i = 1; i <= max; i++) {
                    pageMain.push(this._buildPageLink(i));
                }
            } else {
                //当index处于step内的几页时
                if (index <= step) {
                    for (i = 1; i <= step; i++) {
                        var text = this._buildPageLink(i);
                        pageMain.push(text);
                    }
                    pageMain.push('<em>...</em>');
                    pageMain.push(this._buildPageLink(max));
                //当index处理末尾的step步之内
                } else if (index > max - step) {
                    //刚好step比max小1
                    if((max - 1) !== step) {
                        pageMain.push(this._buildPageLink(1));
                        pageMain.push('<em>...</em>');
                    }
                    for (i = max - step; i <= max; i++) {
                        pageMain.push(this._buildPageLink(i));
                    }
                //index处于中间的时候
                } else {
                    pageMain.push(this._buildPageLink(1));
                    pageMain.push('<em>...</em>');
                    for (i = index - Math.floor(step / 2); i <= index + Math.floor(step / 2) - (step % 2 ? 0 : 1); i++) {
                        pageMain.push(this._buildPageLink(i));
                    }
                    pageMain.push('<em>...</em>');
                    pageMain.push(this._buildPageLink(max));
                }
            }
            pageMain.push('</span>');

            //render Right
            var indexMax = (index === max);
            pageMain.push('<span class="page-next">');
            if (this.get('skin') === 'web') {
                pageMain.push(this._buildButtonLink("#", indexMax ? "page-disabled" : "page-next", "下一页<i class='tri'></i>"));
            } else {
                pageMain.push(this._buildButtonLink("#", indexMax ? "page-disabled" : "page-next", '»'));
            }
            pageMain.push("</span>");

            ndContainer.one('.' + Paginator.CSS_CLASS.cssItem).setHTML(pageMain.join(''));
        },
        /**
         * 绑定页面跳转事件
         * @method _bindMainDelegate
         * @private
         */
        _bindMainDelegate: function () {
            var _this = this;
            this.cBox.detach('click');
            this.cBox.delegate('click', function() {
                var index = _this.get('index');
                //在跳转页面前的自定义处理
                if (_this.get('beforeGoPage')) {
                    if (!_this.get('beforeGoPage')(index, _this)) {
                        return;
                    }
                }

                if (this.hasClass('page-prev')) {
                    _this._goPage(index - 1 );
                } else if (this.hasClass('page-next')) {
                    _this._goPage(index + 1);
                } else if (this.get('tagName') === 'A' && this.get('parentNode').get('className') === 'page-main') {
                    _this._goPage(parseInt(this.getHTML(), 10));
                }
                if (_this.get('jump') && this.ancestor('.paginator-jump') && this.get('tagName') === 'BUTTON') {
                    _this._jumpPage();
                }
            }, 'a, button');
            var ndPageNo = this.cBox.one("input[name=pageNo]");
            //注册回车跳转,可能没有ndPageNo
            if (ndPageNo) {
                ndPageNo.on("keydown", function (e) {
                    if (e.keyCode === $Macro.key.ENTER) {
                        _this._jumpPage();
                    }
                });
            }
            var ndSetPageSelect = this.cBox.one(".paginator-page-size > select");
            if (ndSetPageSelect) {
                ndSetPageSelect.on("change", function () {
                    _this._setPageSize();
                });
            }
        },
        /**
         * 调转到某页
         * @method _goPage
         * @param {Number} page
         * @private
         */
        _goPage: function(page) {
            this.set('index', page);
            this._renderPage();
            this.callbackHandler(page);
        },
        /**
         * 点击具体分页序号时的回调处理
         * @method callbackHandler
         * @param {Number} page
         */
        callbackHandler: function(page) {
            var params = {'page': page, 'max': this.get('max'), 'pageSize': this.get('pageSize')};
            this.fire('Paginator.trigger', params);

            if(this.get('callback')) {
                this.get('callback')(params);
            } else {
                var handlePage = this.get('handlePage');
                if(handlePage && handlePage.url) {
                    //合并两个handlePage的form和query两个参数, 生成重定向链接
                    var query = "";
                    if(handlePage.query) {
                        query = Y.mt.util.toPostData(handlePage.query);
                    }
                    //Y.use加载模块时, 不会阻止后面语句执行, 所以需要else
                    if(handlePage.formId) {
                        var _this = this;
                        Y.use.apply(Y, ['mt-form', function(Y) {
                            var formData = Y.mt.form.serialize(handlePage.formId);
                            query = query ? [query, formData].join('&') : formData;
                            window.location.href = handlePage.url + '?' + _this.get('pageName') + '=' + page + '&' + query;
                        }]);
                    } else {
                        window.location.href = handlePage.url + '?' + this.get('pageName') + '=' + page + '&' + query;
                    }
                }
            }
        },
        /**
         * 按页码跳转的事件处理逻辑
         * @method _jumpPage
         * @private
         */
        _jumpPage: function () {
            var jumpinputVal = parseInt(this.ndContainer.one('input').get('value'), 10);
            if (this._validatePage(jumpinputVal)) {
                this._goPage(jumpinputVal);
            } else {
                Y.config.win.alert("请输入正确的页码");
            }
        },
        /**
         * 设置每页行数
         * @method _setPageSize
         * @private
         */
        _setPageSize: function () {
            var pageSizeVal = parseInt(this.ndContainer.one('.paginator-page-size select').get('value'), 10),
                totalCount = this.get('totalCount');
            this.set('max', Math.ceil(totalCount / pageSizeVal));
            this.set('pageSize', pageSizeVal);
            this._goPage(1);
        },
        /**
         *  页码验证
         * @method _validatePage
         * @param {Number} val
         * @return {Boolean}
         * @private
         */
        _validatePage: function (val) {
            var max = this.get('max');
            return (Lang.isNumber(val) && val >= 1 && val <= max);
        }
    }, {
        /**
         * 静态配置属性
         * @property ATTRS
         * @type Object
         * @static
         */
        ATTRS: {
            /**
             * 记录只有一条是否自动隐藏
             * @attribute autoHide
             * @type Number
             * @default false
             */
            autoHide: {
                value: false
            },
            /**
             * 共多少条记录
             * @attribute totalCount
             * @type Numbe
             * @default Null
             */
            totalCount: {
                value: null,
                setter: 'setTotalDes'
            },
            /*
             * 皮肤选择
             * @attribute skin
             */
            skin: {
                value: 'cos'
            },
            /**
             * 跨跃步数
             * @attribute step
             * @type Number
             * @default 5
             */
            step: {
                value: 5
            },
            /**
             * 处理页
             * @attribute handlePage
             */
            handlePage: {
                url: {
                    value: null
                },
                query: {
                    value: null
                },
                formId: {
                    value: null
                }
            },
            /**
             * 当前页
             * @attribute index
             * @type Number
             * @default 1
             */
            index: {
                value: 1
            },
            /**
             * 共有多少页
             * @attribute max
             * @type Number
             * @default 10
             */
            max: {
                value: 10
            },
            /**
             * 每页的数量
             * @attribute pageSize
             * @type Number
             * @default 20
             */
            pageSize: {
                value: 20
            },
            /**
             * 是否显示调整每页显示数量
             * @attribute showPageSize
             * @type Boolean
             * @default false
             */
            showPageSize: {
                value: false,
                setter: '_renderShowPageSizeUI'
            },
            /**
             * 跳到具体页
             * @attribute jump
             * @type Boolean
             * @default false
             */
            jump: {
                value: false,
                setter: '_renderJumpUI'
            },
            /**
             * 翻页前自定义处理
             * @attribute beforeGoPage
             * @type Function
             * @default Null
             */
            beforeGoPage: {
                value: null
            },
            /**
             * 回调
             * @attribute callback
             * @type Function
             * @default Null
             */
            callback: {
                value: null
            },
            /**
             * 页面名称
             * @attribute pageName
             * @type String
             * @default 'pageNo'
             */
            pageName: {
                value: 'pageNo'
            }
        },
        /**
         * CSS前缀
         * @property CSS_PREFIX
         * @static
         * @type String
         * @default 'Widget-paginator'
         */
        CSS_PREFIX: 'widget-paginator',
        /**
         * CSS类
         * @property CSS_CLASS
         * @static
         * @type Object
         */
        CSS_CLASS: {
            cssTotalCount: 'paginator-total-count',
            cssContainer: 'paginator-container',
            cssItem: 'paginator-page-items',
            cssPageSize: 'paginator-page-size',
            cssJump: 'paginator-jump',
            cssSkin: 'paginator-cos'
        },
        /**
         * 模板信息
         * @property TEMPLATE
         * @static
         * @type Object
         */
        TEMPLATE: {
            link: '<a class="{classname}" href="javascript:void(0)" title="{title}">{text}</a>',
            pageSize: '<span class="{cssPageSize}"> 每页行数 ' +
                       '<select>' +
                           '<option value="10" {option10}>10</option>' +
                           '<option value="20" {option20}>20</option>' +
                           '<option value="30" {option30}>30</option>' +
                           '<option value="50" {option50}>50</option>' +
                       '</select>' +
                   '</span>',
            jump: '<span class="{cssJump}"> 到第 <input type="text" name="pageNo"> 页 <button class="btn" type="button">跳转</button></span>',
            pageContainer: '<div class="{cssContainer} {cssSkin}">' +
                                '<span class="{cssTotalCount}">共<em>{totalCount}</em>条</span>' +
                                '<span class="{cssItem}"></span>' +
                            '</div>'
        }
    });

    Y.mt.widget.Paginator = Paginator;

}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'widget',
        'node',
        'oop',
        'mt-form'
    ],
    skinnable: true
});

