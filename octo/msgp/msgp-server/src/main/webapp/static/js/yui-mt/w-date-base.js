/*jshint white:true,unused:true*/
/**
 * 包含日期相关的一些控件
 * @module w-date-base
 */
M.add('w-date-base', function (Y) {
    var isNumber = Y.Lang.isNumber;
    var trim = Y.Lang.trim;

    var $Macro = Y.mt.macro;
    var $Date = Y.mt.date;

    Y.namespace('mt.widget');

    /**
     * 日历控件基类
     * @class DatepickerBase
     * @namespace mt.widget
     * @constructor
     * @extends Widget
     */
    var DatepickerBase = Y.Base.create('DatepickerBase', Y.Widget, [], {
        initializer: function () {
            this.ndDoc = this.get('ndDoc');
            this.node = this.get("node");
            this.inputNodeDate = new Date();
            this.container = this.get('boundingBox');
            this.container.hide();
        },
        renderUI: function () {
            this.renderBaseUI();
            this.bindBaseUI();
        },
        /**
         * UI初始化
         * @method bindUI
         */
        renderBaseUI: function () {
            // 日历容器
            this.get('contentBox').setHTML(this._generateCalHTML());
            this.ndDoc.prepend(this.container);
            this.fire('DatepickerBase:rendered');
        },
        /**
         * 基础时间初始化
         * @method bindBaseUI
         */
        bindBaseUI: function () {
            var _this = this;
            var cssName = DatepickerBase.CSS_NAME;
            var selectable = cssName['selectable'];
            var calDayHover = cssName['calDayHover'];
            var preMonth = cssName['preMonth'];
            var preYear = cssName['preYear'];
            var nextMonth = cssName['nextMonth'];
            var nextYear = cssName['nextYear'];
                //是否同步显示值
            this._initChangeYearInputEvent();

            this._initChangeMonthInputEvent();

            // 鼠标悬浮效果
            this.container.delegate('mouseover', function () {
                this.addClass(calDayHover);
            }, '.' + selectable);

            this.container.delegate('mouseout', function () {
                this.removeClass(calDayHover);
            }, '.' + selectable);


            this.container.delegate('click', function (e) {
                var target = e.target;

                if (target.test('.' + selectable)) {
                    _this.fire('DatepickerBase:selectDay', { targetTd: target});
                } else if (target.test('.' + preMonth)) {
                    // 选择上一月
                    e.halt();
                    _this.previousMonth();
                } else if (target.test('.' + nextMonth)) {
                    // 选择下一月
                    e.halt();
                    _this.nextMonth();
                } else if (target.test('.' + nextYear)) {
                    //选择下一年
                    e.halt();
                    _this.nextYear();
                } else if (target.test('.' + preYear)) {
                    //选择上一年
                    e.halt();
                    _this.previousYear();
                }
            }, '.' + selectable + ', a');

        },
        /**
         * 显示
         * @method show
         */
        show: function () {
            var bBox = this.get('boundingBox');
            var nd = this.node;
            //TODO setXY bug check
            bBox.setStyles({'left': nd.getX(), 'top': nd.getY() + nd.get('offsetHeight') + 'px'});
            this.container.show();
            this.fire("DatepickerBase:refresh");
        },
        /**
         * 隐藏
         * @method hide
         */
        hide: function () {
            this.get('boundingBox').hide();
        },
        /**
         * 选择上一月
         * @method previousMonth
         */
        previousMonth: function () {
            this.currentDate.setDate(1);
            this.currentDate.setMonth(this.currentDate.getMonth() - 1);
            this.fire("DatepickerBase:refresh");
        },
        /**
         * 选择下一月
         * @method nextMonth
         */
        nextMonth: function () {
            // 超过11后会自加年份
            this.currentDate.setDate(1);
            this.currentDate.setMonth(this.currentDate.getMonth() + 1);
            this.fire("DatepickerBase:refresh");
        },
        /**
         * 选择上一年
         * @method previousYear
         */
        previousYear: function () {
            this.currentDate.setFullYear(this.currentDate.getFullYear() - 1);
            this.fire("DatepickerBase:refresh");
        },
        /**
         * 选择下一年
         * @method nextYear
         */
        nextYear: function () {
            this.currentDate.setFullYear(this.currentDate.getFullYear() + 1);
            this.fire("DatepickerBase:refresh");
        },
        /**
         * 初始化年份输入框的事件
         * @method _initChangeYearInputEvent
         * @private
         */
        _initChangeYearInputEvent: function () {
            var _this = this;
            var changeYear = DatepickerBase.CSS_NAME['changeYear'];
            this.container.delegate('click', function () {
                //获得旧值，以在用户输入错误数据时恢复
                this.oldValue = this.get("value");
            }, '.' + changeYear);
            //键盘事件
            this.container.delegate('keydown', function (e) {
                if (isNumber(parseInt(this.get('value'), 10))) {
                    switch (e.keyCode) {
                    case $Macro.key.ENTER:
                        _this._confirmChangeYear(true);
                        break;
                    case $Macro.key.UP:
                        this.set('value', parseInt(this.get('value'), 10) + 1);
                        break;
                    case $Macro.key.DOWN:
                        this.set('value', parseInt(this.get('value'), 10) - 1);
                        break;
                    default:
                        break;
                    }
                }
            }, '.' + changeYear);

            this.container.delegate('blur', function () {
                _this._confirmChangeYear();
            }, '.' + changeYear);
        },
        /**
         * 初始化月份输入框的事件
         * @method _initChangeMonthInputEvent
         * @private
         */
        _initChangeMonthInputEvent: function () {
            var _this = this;
            var changeMonth = DatepickerBase.CSS_NAME['changeMonth'];
            this.container.delegate('click', function () {
                //获得旧值，以在用户输入错误数据时恢复
                this.oldValue = this.get("value");
            }, '.' + changeMonth);

            //键盘事件
            this.container.delegate('keydown', function (e) {
                if (isNumber(parseInt(this.get('value'), 10))) {
                    switch (e.keyCode) {
                    case $Macro.key.ENTER:
                        _this._confirmChangeMonth(true);
                        break;
                    case $Macro.key.UP:
                        this.set('value', parseInt(this.get('value'), 10) + 1);
                        break;
                    case $Macro.key.DOWN:
                        this.set('value', parseInt(this.get('value'), 10) - 1);
                        break;
                    default:
                        break;
                    }
                }
            }, '.' + changeMonth);

            this.container.delegate('blur', function () {
                _this._confirmChangeMonth();
            }, '.' + changeMonth);
        },
        /**
         * 改变日历年份，先检查输入，如果正确则刷新日历，否则恢复原值并显示提示
         * @method _confirmChangeYear
         * @param {Boolean} selectDay 是否选中日期
         * @private
         */
        _confirmChangeYear: function (selectDay) {
            var input = this.container.one('.' + DatepickerBase.CSS_NAME["changeYear"]);
            var value = input.get("value");
            //合法范围：1000～2999
            var regex = /^[1-2][0-9]{3}$/ig;

            this._removeTip();
            if (value === input.oldValue) return;

            if (regex.test(value)) {
                this.currentDate.setFullYear(value);
                if (selectDay === true) {
                    this.fire('DatepickerBase:selectDay');
                } else {
                    this.fire("DatepickerBase:refresh");
                }
            } else {
                if (input.oldValue) {
                    input.set("value", input.oldValue);
                }
                this._displayTip("年份范围：1000～2999");
            }
        },
        /**
         * 改变日历月份，先检查输入，如果正确则刷新日历，否则恢复原值并显示提示
         * @method _confirmChangeMonth
         * @param {Boolean} selectDay 是否选中日期
         * @private
         */
        _confirmChangeMonth: function (selectDay) {
            var input = this.container.one('.' + DatepickerBase.CSS_NAME["changeMonth"]);
            var value = input.get("value");
            //合法范围：01～12
            var regex = /^(0?[1-9]|1[0-2])$/ig;

            this._removeTip();
            if (value === input.oldValue) return;

            if (regex.test(value)) {
                this.currentDate.setDate(1);
                this.currentDate.setMonth(value - 1);
                if (selectDay === true) {
                    this.fire('DatepickerBase:selectDay');
                } else {
                    this.fire("DatepickerBase:refresh");
                }
            } else {
                if (input.oldValue) {
                    input.set("value", input.oldValue);
                }
                this._displayTip("月份范围：1～12");
            }
        },
        /**
         * 显示提示信息
         * @method _displayTip
         * @private
         * @param {String} msg 显示的提示信息
         */
        _displayTip: function (msg) {
            this._removeTip();
            this.get('contentBox').prepend(Y.Lang.sub(DatepickerBase.TEMPLATES['tips'], {
                msg: msg,
                cssTips: DatepickerBase.CSS_NAME['cssTips']
            }));
        },
        /**
         * 移除提示信息
         * @method _removeTip
         */
        _removeTip: function () {
            var tip = this.get('contentBox').one('.' + DatepickerBase.CSS_NAME['cssTips']);
            if (tip) {
                tip.remove();
            }
        },
        /**
         * 生成日历html代码
         * @method _generateCalHTML
         * @return String
         */
        _generateCalHTML: function () {
            var y = this.currentDate.getFullYear();
            var m = this.currentDate.getMonth();

            var buildParams = {
                preYearTitle: (y - 1) + '年' + (m + 1) + '月',
                nextYearTitle: (y + 1) + '年' + (m + 1) + '月',
                preMonthTitle: m === 0 ? ((y - 1) + '年12月') : (y + '年' + m + '月'),
                nextMonthTitle: m === 11 ? ((y + 1) + '年1月') : (y + '年' + (m + 2) + '月'),
                month: m + 1,
                year: y,
                content: this._getDayList(),
                header: this._getHeaderList()
            };

            Y.mix(buildParams, DatepickerBase.CSS_NAME);
            return Y.Lang.sub(DatepickerBase.TEMPLATES['container'], buildParams);
        },
        /**
         * 生成日历列表中日期项
         * @method _getDayList
         * @return String
         * @private
         */
        _getDayList: function () {
            var i, dayClass;
            var dayList = ['<tr>'];
            var y = this.currentDate.getFullYear();
            var m = this.currentDate.getMonth();
            var firstDay = new Date(y, m, 1);
            //获得当前月的第一天是星期几
            var weekday = firstDay.getDay();
            var preMonth = m === 0 ? 11 : (m - 1);
            var prevMonthDayNum = $Date.getMaxDaysInMonth(y, preMonth + 1);
            var curMonthDayNum = $Date.getMaxDaysInMonth(y, m + 1);
            var today = new Date();
            var curDate = firstDay;

            var isStartOnSunday = this.get('startOnSunday'),
                prevMonthShowCount,     // 需要输出上月的最后几天
                nextMonthShowCount,     // 需要输出下月的头几天
                curMonthShowCount = curMonthDayNum;

            // 计算上个月应该展示几天
            if (isStartOnSunday) {
                prevMonthShowCount = weekday;
            } else {
                prevMonthShowCount = weekday ? weekday - 1 : 6;
            }
            // 计算下个月应该展示几天
            nextMonthShowCount = (7 - (prevMonthShowCount + curMonthDayNum) % 7) % 7;

            // 输出上个月日期
            for (i = 1; i <= prevMonthShowCount; ++i) {
                dayList.push(Y.Lang.sub(DatepickerBase.TEMPLATES['cellItem'], {
                    dayClass: DatepickerBase.CSS_NAME["dayPreMonth"],
                    index: prevMonthDayNum - prevMonthShowCount + i
                }));
            }

            // 输出当前月日期
            for (i = 1; i <= curMonthShowCount; ++i) {
                curDate.setDate(i);
                dayClass = DatepickerBase.CSS_NAME["dayCurMonth"];
                // 判断是否可选
                if ($Date.isInRegions(curDate, this.get("mindate"), this.get("maxdate"))) {
                    dayClass += ' ' + DatepickerBase.CSS_NAME['selectable'];
                    if (curDate.getDay() === 0 || curDate.getDay() === 6) {
                        dayClass += ' ' + DatepickerBase.CSS_NAME['weekend'];
                    }
                }
                // 判断是否为input输入框中的日期值
                if ($Date.getTimeReduction(curDate, this.inputNodeDate, true) === 0) {
                    dayClass += ' ' + DatepickerBase.CSS_NAME['selected'];
                }
                // 判断是否为今天
                if ($Date.getTimeReduction(curDate, today, true) === 0) {
                    dayClass += ' ' + DatepickerBase.CSS_NAME['today'];
                }
                dayList.push(Y.Lang.sub(DatepickerBase.TEMPLATES['cellItem'], {
                    dayClass: dayClass,
                    index: i
                }));
                if ((i + prevMonthShowCount) % 7 === 0 && i < curMonthDayNum) {
                    dayList.push('</tr><tr>');
                }
            }
            // 输出下个月日期
            for (i = 1; i <= nextMonthShowCount; ++i) {
                dayList.push(Y.Lang.sub(DatepickerBase.TEMPLATES['cellItem'], {
                    dayClass: DatepickerBase.CSS_NAME['dayNextMonth'],
                    index: i
                }));
            }
            dayList.push('</tr>');

            return dayList.join('');
        },
        /**
         * 生成日历头部
         * @method _getHeaderList
         * @return String
         * @private
         */
        _getHeaderList: function () {
            var isStartOnSunday = this.get('startOnSunday'),
                weekdays = [
                    '<th>一</th>',
                    '<th>二</th>',
                    '<th>三</th>',
                    '<th>四</th>',
                    '<th>五</th>',
                    '<th class="widget-datepicker-saturday">六</th>',
                    '<th class="widget-datepicker-sunday">日</th>'
                ];

            if (isStartOnSunday) {
                weekdays.unshift(weekdays.pop());
            }

            return weekdays.join('');
        },
        /**
         * 初始化mindate参数
         * @method setMindate
         * @param {String} mindate 最小的可选择的时间，如2012-12-12
         */
        setMindate: function (mindate) {
            if (mindate && this._isValidShortDate(trim(mindate))) {
                var date = mindate.split('-');
                mindate = new Date(date[0], date[1] - 1, date[2]);
            } else {
                mindate = new Date(2010, 3, 4);
            }
            return mindate;
        },
        /**
         * 初始化maxdate参数
         * @method setMaxdate
         * @param {String} maxdate 最大可选择的时间, 如2012-12-12
         */
        setMaxdate: function (maxdate) {
            if (maxdate && this._isValidShortDate(trim(maxdate))) {
                var date = maxdate.split('-');
                maxdate = new Date(date[0], date[1] - 1, date[2]);
            } else {
                maxdate = new Date(2099, 12, 31);
            }
            return maxdate;
        }
    }, {

        CSS_NAME: {
            calDayHover: 'cal-day-hover',
            selected: 'widget-datepicker-selected',
            selectable: 'widget-datepicker-selectable',
            changeYear: 'widget-datepicker-change-year',
            changeMonth: 'widget-datepicker-change-month',
            preMonth: 'cal-nav-pre-m',
            nextMonth: 'cal-nav-next-m',
            preYear: 'cal-nav-pre-y',
            nextYear: 'cal-nav-next-y',
            dayCurMonth: 'cal-day-cur-month',
            dayPreMonth: 'cal-day-pre-month',
            dayNextMonth: 'cal-day-next-month',
            today: 'widget-datepicker-today',
            cssTips: 'widget-datepicker-tip',
            weekend: 'widget-datepicker-weekend'
        },
        TEMPLATES: {
            tips: '<div class="{cssTips}">{msg}</div>',
            container: '<table class="widget-datepicker-table" cellspacing="0" cellpadding="0" border="0">' +
                            '<thead>' +
                                '<tr class="cal-header">' +
                                    '<th colspan="7">' +
                                        '<div class="cal-header-wrapper">' +
                                            '<a class="{preYear}" href="javascript:void(0)" title="{preYearTitle}">&lt;&lt;</a>' +
                                            '<a class="{preMonth}" href="javascript:void(0)" title="{preMonthTitle}">&lt;</a>' +
                                            '<input type="text" class="{changeYear}" maxlength="4" name="current-year" value="{year}" />年' +
                                            '<input type="text" class="{changeMonth}" maxlength="2" name="current-month" value="{month}" />月' +
                                            '<a class="{nextMonth}" href="javascript:void(0)" title="{nextMonthTitle}">&gt;</a>' +
                                            '<a class="{nextYear}" href="javascript:void(0)" title="{nextYearTitle}">&gt;&gt;</a>' +
                                        '</div>' +
                                    '</th>' +
                                '</tr>' +
                                '<tr class="widget-datepicker-weekday">{header}</tr>' +
                            '</thead>' +
                            '<tbody>{content}</tbody>' +
                        '</table>',
            cellItem: '<td class="{dayClass}" data-day="{index}">{index}</td>'
        },
        CSS_PREFIX: 'datepicker-base',
        ATTRS: {
            /**
             * 每周是否从周日开始
             * @attribute startOnSunday
             * @type Boolean
             */
            startOnSunday: {
                value: false,
                writeOnce: true
            },
            /**
             * 最小可选择的时间
             * @attribute mindate
             * @type String
             */
            mindate: {
                value: null,
                setter: 'setMindate'
            },
            /**
             * 最大可选择的时间
             * @attribute maxdate
             * @type String
             */
            maxdate: {
                value: null,
                setter: 'setMaxdate'
            },
            /**
             * 最大可选择的时间
             * @attribute align
             * @type Object
             * @default { points : ['tl', 'bl']}
             */
            /**
             * 当前文档
             * @attribute ndDoc
             * @type Node
             * @default document.body
             */
            ndDoc: {
                value: Y.one(document.body)
            },
            /**
             * 关联的node对象
             * @attribute node
             * @type Node
             */
            node: {
                value: null
            }
        }
    });

    Y.mt.widget.DatepickerBase = DatepickerBase;

}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'widget',
        'mt-date',
        'node'
    ]
});
 
