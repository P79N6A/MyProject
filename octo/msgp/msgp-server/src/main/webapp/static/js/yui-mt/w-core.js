/**
 * widget核心文件 预计以后会和YUI3的widget相结合，实现一些小的零件
 * test
 * @module w-core
 */
M.add('w-core', function(Y) {

    var $Util = Y.mt.util;
    var isIE =  Y.UA.ie;
    var isIE6 = (isIE === 6);
    var isIE8 = (isIE === 8);
    var docClickHandler = {};
    Y.namespace('mt.widget');

    /**
     * widget util:定义一些widget需要使用的公共方法
     * @class util
     * @namespace mt.widget
     * @static
     */
    Y.mt.widget.util = {
        /**
         * 定义widget的位置
         * @method setPosition
         * @param {Node} nd node object to set position
         * @param {String|Node} target 规则
         * @param {String|Node} container 配合container使用
         * @param {String|Node} doc 加入到的文档中
         * @param {String} positionStyle position样式设置，默认为absolute
         */
        setPosition: function(nd, target, container, doc, positionStyle) {
            var ndWidth,
                ndHeight,
                position = positionStyle || 'absolute';
            doc = doc || Y.one(document.body);
            if (!target || target === "center") {
                //放到中心
                ndWidth = nd.get("offsetWidth");
                ndHeight = nd.get("offsetHeight");
                var left = (doc.get("offsetWidth") - ndWidth)/2;
                var ndTop = (doc.get("winHeight") - ndHeight)/2 + doc.get("docScrollY");
                var top = ndTop <= 0 ?  10 : ndTop;
                this.setXY(nd, [left, top], position);
            } else if (target === "inner"){
                //放到目标的里面
                Y.one(container).appendChild(nd);
                nd.setStyle('position', 'static');
            } else if (Y.Lang.isArray(target)) {
                //设置层的位置
                this.setXY(nd, target, position);
            } else {
                //放到目标的正下方
                var ndTag = Y.one(target);
                this.setXY(nd, [ndTag.getX(), ndTag.getY() + ndTag.get('offsetHeight')], position);
            }
        },
        /**
         * 对YUI中setXY进行封装，使能够设置position（原生的setXY会将position为static时设为relative）
         * @method setXY
         * @param {Node} node 进行定位的元素
         * @param {Array} pos 定位信息：[left, top]
         * @param {String} positionStyle position的样式设置
         */
        setXY: function( node, pos, positionStyle) {
            node.setXY(pos);
            if ( positionStyle ) {
                node.setStyle("position", positionStyle);
            }
        },
        /**
         * 点击其它地方，隐藏当前的widget对象, 除开ndExclude对象外
         * @method hideNodeByClickDocument
         * @param {Node|Selector} ndHide 当前的widget对象节点
         * @param {Node|Selector} ndExclude 例外节点，点击它不执行后续逻辑
         * @param {Function} callback 隐藏后的回调
         */
        hideNodeByClickDocument: function(ndHide, ndExclude, callback) {
            if (!ndHide) {
                return;
            }
            ndHide = Y.one(ndHide);
            var id = ndHide.get('id');
            this.detachHideNodeByClickDocument(ndHide);

            var _this = this;
            docClickHandler[id] = Y.on("click", function(e) {
                if (ndHide.getStyle("display") === 'none' ||
                    _this._isNodeContainsTarget(e.target, ndHide) ||
                    _this._isNodeContainsTarget(e.target, ndExclude)) {
                    return;
                }
                ndHide.hide();
                if (callback) {
                    callback();
                }
            }, document);
        },
        detachHideNodeByClickDocument: function(ndHide) {
            ndHide = Y.one(ndHide);
            var id = ndHide.get('id');
            if (docClickHandler[id]) {
                Y.detach(docClickHandler[id]);
            }
        },
        /**
         * 节点是否包含e.target
         * @method _isNodeContainsTarget
         * @param {String|Node|NodeList} node 检测对象
         */
        _isNodeContainsTarget: function (target, node) {
            if (!(node instanceof Y.NodeList)) {
                node = Y.all(node);
            }
            var included = false;
            node.some(function(item) {
                if (item.contains(target)) {
                    included = true;
                    return true;
                }
            });
            return included;
        },
        /**
         * 检测当前位置(一般用作鼠标点击位置)是否在检测对象范围内
         * @method isPosInRegion
         * @param {Node|NodeList} nl 检测对象
         */
        isPosInRegion: function(pos, nl) {
            if (typeof nl === 'string') {
                nl = Y.all(nl);
            } else if (nl instanceof Y.Node) {
                nl = new Y.NodeList(nl);
            }
            var inRegion = false;
            nl.each(function(item) {
                var region = item.get('region');
                if (pos[0] >= region.left && pos[0] <= region.right && pos[1] >= region.top && pos[1] <= region.bottom) {
                    inRegion = true;
                }
            });
            return inRegion;
        },
        /**
         * 搜索匹配的模式, 正则表达式的计算
         * @method  getRegByMode
         * @param {String} q 查询的key
         * @param {String} mode 匹配的模式, 可以为fuzzy: 模糊模式, start: 开头模式, strict: 严格
         */
        getRegByMode: function(q, mode) {
            var reg;

            mode = mode || "strict";
            //转义字符串中的特殊字符
            q = Y.Escape.regex(q);

            if (mode === 'fuzzy') {
                reg = new RegExp(q);
            } else if (mode === 'start') {
                reg = new RegExp('^' + q);
            } else {
                reg = new RegExp('^' + q + '$');
            }
            return reg;
        },
        /**
         * 生成widget的统一cssName
         * @method getClassName
         * @param {Object | String} classname 名称或者集合
         * @param {String} prefix 名称前缀
         */
        getClassName: function(classname, prefix) {
            if (!prefix) return classname;

            if (Y.Lang.isString(classname)) {
                return prefix + '-' + classname;
            } else {
                for (var p in classname) {
                    if (classname.hasOwnProperty(p)) {
                        classname[p] = prefix + '-' + classname[p];
                    }
                }
            }
            return classname;
        },
        /**
         * 离开视野时, 固定表格头部, 可用于报表统计类表格的展示
         * @method fixedTableHeader
         * @param {String | Node} ndTable 表格对象
         */
        fixedTableHeader: function(ndTable) {
            ndTable = Y.one(ndTable);
            if (!ndTable) return;

            var rows = ndTable.get('rows'),
                firstTr = rows.item(0),
                trWidth = ndTable.getComputedStyle('width');

            ndTable.setStyle('width', trWidth);
            firstTr.setStyle('width', trWidth);

            //设置第一行td的宽度
            firstTr.all('th').each(function(item) {
                var width = item.getComputedStyle("width");
                item.setStyle('width', width);
            });
            //依据第一行td宽度设置第二行td的宽度
            setTr2WidthByTr1(firstTr, getNextTr());
            ndTable.setStyle('word-wrap', 'break-word');

            var ndWin = Y.one(window);
            ndWin.on('scroll', $Util.throttle(handleScroll, 100));
            Y.Global.on('table:fixed', function(e) {
                if(e.ndTable !== ndTable) {
                    resetHead(firstTr);
                }
            });

            //计算ndTable离开视野和进入视野加入不同的样式处理
            function handleScroll() {
                if (ndTable.getY() <= ndTable.get('docScrollY')) {
                    var left = ndTable.getX() - ndTable.get('docScrollX');
                    if (isIE6 || isIE8) {
                        firstTr.setStyles({
                            zIndex: 6,
                            left: left,
                            top: ndTable.get('docScrollY'),
                            position: 'absolute'
                        });
                    } else {
                        firstTr.setStyles({
                            position: 'fixed',
                            top: 0,
                            left: left
                        });
                    }
                    Y.Global.fire('table:fixed', {ndTable: ndTable});
                } else {
                    resetHead(firstTr);
                }
            }
            function resetHead(firstTr) {
                firstTr.setStyles({
                    position: 'static',
                    border: 0
                });
            }
            function getNextTr() {
                var rows = ndTable.get('rows');
                var row = null;
                rows.some(function(item, i) {
                    if (i !== 0 && item.getStyle('display') !== 'none') {
                        row = item;
                        return true;
                    }
                });
                return row;
            }
            function setTr2WidthByTr1(ndTr1, ndTr2) {
                if ( !ndTr1 || ! ndTr2) {
                    return;
                }
                var nlCell2 = ndTr2.get('cells');
                ndTr1.get('cells').each(function(ndCell, i) {
                    var ndCell2 = nlCell2.item(i);
                    ndCell2.setStyle('width', ndCell.getComputedStyle("width"));
                    var paddingLeft = parseInt(ndCell.getComputedStyle('paddingLeft'), 10);
                    var paddingRight = parseInt(ndCell.getComputedStyle('paddingRight'), 10);
                    var paddingNew;
                    if (ndCell2.getStyle('textAlign') === 'right') {
                        paddingNew = paddingLeft + paddingRight - parseInt(ndCell2.getComputedStyle('paddingRight'), 10);
                        ndCell2.setStyle('paddingLeft', paddingNew);
                    } else {
                        paddingNew = paddingLeft + paddingRight - parseInt(ndCell2.getComputedStyle('paddingLeft'), 10);
                        ndCell2.setStyle('paddingRight', paddingNew);
                    }
                });
            }
        }
    };

    Y.namespace('mt.extension');
    /**
     * 作为AutoComplete和LiveSearch的扩展
     * @class List
     * @namespace mt.extension
     * @constructor
     * @param {Object} config
     */
    Y.mt.extension.List = function() {
        this._cache = [];
        this._listHandle = {
            mouse:{
                click:null
            },
            delegate:{
                mouseover:null,
                mouseout:null,
                click:null
            },
            key:{
                up:null,
                down:null,
                enter:null,
                tab:null
            }
        };

        // AOP
        Y.after(this._syncListUI, this, 'syncUI');
        Y.after(this._bindListUI, this, 'bindUI');
        Y.before(this._listDestructor, this, 'destructor');
    };
    Y.mt.extension.List.ATTRS = {
        /**
         * 最大显示数目
         * @property
         * @type Number
         */
        maxShowItem:{
            value:10
        }
    };
    Y.mt.extension.List.prototype = {
        /**
         * 初始化UIList的所有事件
         * @method _bindListUI
         * @private
         */
        _bindListUI:function() {
            this._bindListDelegate();
            this._bindListKey();
            this._bindListMouse();
        },
        /**
         * 销毁定义的事件句柄
         * @method _listDestructor
         * @private
         */
        _listDestructor:function() {
            $Util.detachHandle(this._listHandle);
        },
        /**
         * 绑定鼠标事件
         * @method _bindListMouse
         * @private
         */
        _bindListMouse:function() {
            var widget = this;
            var bBox = this.get('boundingBox');
            if (this._listHandle.mouse['click']) this._listHandle.mouse['click'].detach();
            this._listHandle.mouse.click = Y.one(Y.config.doc).on('click', function(e) {
                var target = e.target;
                var nd = widget._ndListTrigger;
                if (target !== nd && !bBox.contains(target)) {
                    if (widget.get('visible') && widget.get('boundingBox').getStyle("display") !== 'none') {
                        widget._leave();
                    }
                }
            });
        },
        /**
         * 绑定代理事件
         * @method _bindListDelegate
         * @private
         */
        _bindListDelegate:function() {
            var widget = this;
            var ndContent = this.get('contentBox');
            // 鼠标悬浮
            this._listHandle.delegate.mouseover = ndContent.delegate('mouseover', function() {
                ndContent.all('li').removeClass('current');
                this.addClass('current');
            }, 'li');

            // 鼠标移出
            this._listHandle.delegate.mouseout = ndContent.delegate('mouseout', function() {
                this.removeClass('current');
            }, 'li');

            // 点击选择
            this._listHandle.delegate.click = ndContent.delegate('click', function(e) {
                e.stopPropagation();
                widget._select(this);
            }, 'li, input');
        },
        /**
         * 绑定按键事件
         * @method _bindListKey
         * @private
         */
        _bindListKey:function() {
            var widget = this;
            // TODO
            var nd = this._ndListTrigger;
            if (!nd) return;

            // 向下键
            this._listHandle.key.down = nd.on('key', function(e) {
                var nlLi = widget._getList();
                if (nlLi.size() === 0) return;
                if (widget.get('visible')) {
                    e.halt();
                    var ndCurrent = widget._getCurrentItem();
                    if (ndCurrent) {
                        var ndNext = ndCurrent.next('li');
                        if (ndNext) {
                            ndCurrent.removeClass('current');
                            ndNext.addClass('current');
                        } else {
                            ndCurrent.removeClass('current');
                            nlLi.item(0).addClass('current');
                        }
                    } else {
                        nlLi.item(0).addClass('current');
                    }
                }
            }, 'down:40');

            // 向上键
            this._listHandle.key.up = nd.on('key', function(e) {
                var nlLi = widget._getList();
                if (!nlLi) return;
                if (widget.get('visible')) {
                    e.halt();
                    var ndCurrent = widget._getCurrentItem();
                    if (ndCurrent) {
                        var ndPrev = ndCurrent.previous('li');
                        if (ndPrev) {
                            ndCurrent.removeClass('current');
                            ndPrev.addClass('current');
                        } else {
                            ndCurrent.removeClass('current');
                            nlLi.item(nlLi.size() - 1).addClass('current');
                        }
                    }
                }
            }, 'down:38');
            // enter键
            this._listHandle.key.enter = nd.on('key', function(e) {
                e.halt();
                var ndCurrent = widget._getCurrentItem();
                if (ndCurrent) {
                    widget._select(ndCurrent);
                }
            }, 'down:13');
            // tab键
            this._listHandle.key.tab = nd.on('key', function() {
                widget._leave();
            }, 'down:9');
        },
        /**
         * 获得列表
         * @method _getList
         * @private
         */
        _getList:function() {
            return this.get('contentBox').all('li');
        },
        /**
         * 获得当前项
         * @method _getCurrentItem
         * @private
         */
        _getCurrentItem:function() {
            return this.get('contentBox').one('li.current');
        },
        /**
         * 更新列表
         * @method _syncList
         * @private
         */
        _syncList:function(data) {
            var i, item, html = [];
            this._cache = data || [];
            if (!data || data.length === 0) {
                html.push('<p class="none">没有结果</p>');
            } else {
                html.push('<ul>');
                for (i = 0; i < data.length && i < this.get('maxShowItem'); i++) {
                    item = data[i];
                    html.push('<li' + (i % 2 ? ' class="alt"' : '') + '>');
                    html.push(item['text']);
                    html.push('</li>');
                }
                html.push('</ul>');
            }
            return html.join('');
        },
        /**
         * 显示列表
         * @method _showList
         * @private
         */
        _showList:function(html, pos) {
            if (html !== undefined) {
                this.get('contentBox').setHTML(html);
            }
            if (pos) this._setListPos(pos);
            this.show();
        },
        /**
         * 计算列表位置
         * @method _calListPosition
         * @private
         */
        _calListPosition:function(posTarget) {
            var pos = posTarget.getXY();
            pos[1] += posTarget.get('offsetHeight');
            return pos;
        },
        /**
         * 设置列表位置
         * @method _setListPos
         * @private
         */
        _setListPos:function(pos) {
            var position = pos || this._listPos;
            if (!position) {
                throw new Error('请设置结果列表定位信息');
            }
            Y.mt.widget.util.setXY(this.get('boundingBox'), position, "absolute");
        }
    };
},

'1.0.0', { requires: ['mt-base', 'escape', 'node', 'event-custom' ] });

