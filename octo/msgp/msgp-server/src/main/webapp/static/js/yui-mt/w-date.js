/*jshint white:true,unused:true*/
/**
 * 包含日期相关的一些控件
 * @module w-date
 */
M.add('w-date', function (Y) {
    var isNumber = Y.Lang.isNumber;
    var trim = Y.Lang.trim;

    var $Macro = Y.mt.macro;
    var DatepickerBase = Y.mt.widget.DatepickerBase;
    var WidgetUtil = Y.mt.widget.util;
    var $Lang = Y.mt.lang;

    Y.namespace('mt.widget');

     /**
      * 一些和input交互的功能
      * @class Datepicker
      * @namespace mt.widget
      * @constructor
      */
    var Datepicker = Y.Base.create('datepicker', DatepickerBase, [], {
        /**
         * widget初始化入口
         * @method initializer
         */
        initializer: function () {
            this.input = Y.one(this.get('node'));
            this.currentDate = this.get("date") || new Date();
            if (!this.input) return;
            if (!this.input.get('id')) this.input.set('id', Y.guid());
            //render之前注册
            this.on('DatepickerBase:selectDay', this.selectDay);
            this.on('DatepickerBase:refresh', this.refresh);
            this.on('DatepickerBase:rendered', this._renderFooterUI);

            if (this.get('autoRender')) {
                this.render();
            }
        },
        /**
         * UI事件绑定相关, 包括focus、blur、keydown等键盘操作事件
         * @method bindUI
         */
        bindUI: function () {
            var instance = this;
            //触发控件初始化数据并展示
            this.input.on('focus', function () {
                if (instance.currentDate < instance.get('mindate')) {
                    instance.currentDate = new Date(instance.get('mindate').getTime());
                } else if (instance.currentDate > instance.get('maxdate')) {
                    instance.currentDate = new Date(instance.get('maxdate').getTime());
                } else {
                    var inputValue = trim(this.get('value'));
                    instance.currentDate = inputValue ? instance._getDate(inputValue) : instance.get("date");
                }
                instance.inputNodeDate = Y.clone(instance.currentDate);
                instance.show();
            });
            //点击除了input外的其它地方, overlay消失
            WidgetUtil.hideNodeByClickDocument(this.get('boundingBox'), this.input, function () {
                Y.Global.fire('Datepicker.hide');
            });

            //键盘操作支持
            this.input.on('keydown', function (e) {

                var container = instance.container, preTr, nextTr, toSelect;
                var preSelected = container.one('.' + DatepickerBase.CSS_NAME['selected']) || container.one('.' + DatepickerBase.CSS_NAME["selectable"]);
                var currentIndex = preSelected.get('cellIndex');

                if (!preSelected) return;
                switch (e.keyCode) {
                case $Macro.key.TAB:
                    instance.hide();
                    break;
                case $Macro.key.DOWN:
                    e.halt();
                    if (e.shiftKey) {
                        instance.previousYear();
                    } else {
                        nextTr = preSelected.ancestor('tr').next('tr') || container.one('tr');
                        toSelect = nextTr.get('cells').item(currentIndex);
                        instance._selectDate(preSelected, toSelect);
                    }
                    break;
                case $Macro.key.UP:
                    e.halt();
                    if (e.shiftKey) {
                        instance.nextYear();
                    } else {
                        preTr = preSelected.ancestor('tr').previous('tr') || container.one('tr:last-child');
                        toSelect = preTr.get('cells').item(currentIndex);
                        instance._selectDate(preSelected, toSelect);
                    }
                    break;
                case $Macro.key.LEFT:
                    e.halt();
                    if (e.shiftKey) {
                        instance.previousMonth();
                    } else {
                        toSelect = preSelected.previous('td');
                        instance._selectDate(preSelected, toSelect);
                    }
                    break;
                case $Macro.key.RIGHT:
                    e.halt();
                    if (e.shiftKey) {
                        instance.nextMonth();
                    } else {
                        toSelect = preSelected.next('td');
                        instance._selectDate(preSelected, toSelect);
                    }
                    break;
                case $Macro.key.ENTER:
                    e.halt();
                    instance.selectDay();
                    break;
                default:
                    break;
                }
            });

        },
        /**
         * 判断按下的方向键指向的日期是否可选，可选则选上
         * @method _selectDate
         * @param {Node} preSelected 之前选中的td
         * @param {Node} toSelect 将要选中的td
         * @private
         */
        _selectDate: function (preSelected, toSelect) {
            var selected = DatepickerBase.CSS_NAME['selected'];
            if (toSelect && toSelect.hasClass(DatepickerBase.CSS_NAME['selectable'])) {
                preSelected.removeClass(selected);
                toSelect.addClass(selected);
            }
        },
        /**
         * 获取日期
         * @method _getDate
         * @private
         * @return {Date|null}
         */
        _getDate: function (inputValue) {
            //0和""的优先级低于this.get('date');
            var date, time;
            if (this._isValidDate(inputValue)) {
                var tmp = inputValue.split(/\s/ig);
                date = tmp[0].split('-');
                if (this.get("showSetTime") && tmp[1]) {
                    time = tmp[1].split(':');
                    date = new Date(date[0], date[1] - 1, date[2] || 1, time[0], time[1], time[2]);
                } else if (this.get("onlyShowYearMonth")) {
                    date = new Date(date[0], date[1] - 1, 1);
                } else {
                    date = new Date(date[0], date[1] - 1, date[2] || 1);
                }
                if (inputValue === "" || inputValue === "0") {
                    //允许填空
                    date = new Date();
                }
            } else {
                date = new Date();
            }
            return date;
        },
        /**
         * 同步关联的输入框中的值
         * @method setInputValue
         */
        setInputValue: function () {
            var date = this.currentDate;
            var inputValue = this.currentDate;
            if (this.get("onlyShowYearMonth")) {
                inputValue = $Lang.date('Y-m', date);
            } else if (this.get("showSetTime")) {
                inputValue = $Lang.date('Y-m-d H:i:s', date);
            } else {
                inputValue = $Lang.date('Y-m-d', date);
                if (this.get('appendWeekday')) {
                    inputValue += ' ' + $Lang.getReadableWeek(date);
                }
            }
            this.input.set('value', inputValue);
            this.input.oldValue = inputValue;
            //Global用于在页面初始化的情况的处理
            Y.Global.fire('Datepicker.select', {id: this.input.get('id'), date: date, inputValue: inputValue, input: this.input});
            this.fire('Datepicker.select', {id: this.input.get('id'), date: date, inputValue: inputValue, input: this.input});
            this.input.fire('widgetvaluechange');
        },
        /**
         * 刷新
         * @method refresh
         */
        refresh: function () {
            this._removeTip();
            if (this.get('syncInputValue')) {
                this.inputNodeDate = Y.clone(this.currentDate);
            }
            this.get('contentBox').setHTML(this._generateCalHTML());
            if (this.get("showSetTime")) {
                this._renderFooterUI();
            }
            if (this.get('syncInputValue')) {
                this.setInputValue();
            }
        },
        /**
         * 选择日期并触发选择日期事件
         * @method selectDay
         * @param {Node} targetTd 选中的td
         */
        selectDay: function (target, noHide) {
            if (!target || !target.targetTd) {
                target = this.container.one('.' + DatepickerBase.CSS_NAME["selected"]);
            } else {
                target = target.targetTd;
            }
            var day = parseInt(target.getAttribute('data-day'), 10);
            var year = parseInt(this.container.one('.' + DatepickerBase.CSS_NAME["changeYear"]).get('value'), 10);
            var month = parseInt(this.container.one('.' + DatepickerBase.CSS_NAME["changeMonth"]).get('value'), 10);
            var h = 0, m = 0, s = 0;

            if (this.get("showSetTime")) {
                var timeInput = this.getTimeInput(true);
                h = timeInput.hours;
                m = timeInput.minute;
                s = timeInput.second;
            }

            this.currentDate = new Date(year, month - 1, day, h, m, s);
            this.setInputValue();
            if (!noHide) this.hide();
        },
        /**
         * 获得事件输入框
         * @method getTimeInput
         * @param {Boolean} value 获得input框的值的标识
         * @return Object
         */
        getTimeInput: function (value) {
            var timeInput = {
                hours: this.container.one('.' + Datepicker.CSS_NAME['hoursInput']),
                minute: this.container.one('.' + Datepicker.CSS_NAME["minuteInput"]),
                second: this.container.one('.' + Datepicker.CSS_NAME["secondInput"])
            };
            if (value) {
                for (var p in timeInput) {
                    if (timeInput.hasOwnProperty(p)) {
                        timeInput[p] = parseInt(timeInput[p].get("value"), 10);
                    }
                }
            }
            return timeInput;
        },
        /**
         * 判断时期是否是2012-12-12这种类型
         * @method _isValidShortDate
         * @param {String} str 需要验证的字符串
         * @return Boolean
         * @private
         */
        _isValidShortDate: function (str) {
            return this._isValidDate(str, true);
        },
        /**
         * 判断日期合法性
         * @method _isValidDate
         * @private
         * @param {String} str 需要验证的字符串
         * @param {Boolean} isShortDate 若需要验证短日期（如：1970-12-24），则设为为true，否则验证的是长日期（如：1989-12-24 00:00:00）
         * @return Boolean
         */
        _isValidDate: function (str, isShortDate) {
            if (str === "" || str === "0") return true;
            if (!str) return false;
            str = trim(str.replace(/(^|\s)(星期|周)\S(\s|$)/, ' '));
            var regex1 = /^[12]\d{3}-[01]?\d{1}-[0-3]?\d\s[0-2]?\d:[0-5]?\d:[0-5]?\d$/ig,
                regex2 = /^[12]\d{3}-[01]?\d{1}(-[0-3]?\d)?$/ig,
                regex3 = /^[12]\d{3}-[01]?\d{1}-[0-3]?\d$/ig;

            if (isShortDate) {
                return regex3.test(str);
            } else {
                return regex1.test(str) || regex2.test(str) || regex3.test(str);
            }
        },
        /**
         * 初始化foot，现有时间输入栏
         * @method _renderFooterUI
         * @private
         */
        _renderFooterUI: function () {
            if (!this.get('showSetTime')) return;
            var date = this.currentDate;
            //判断是否有Date参数，有则使用提供的，否则使用当前时间
            var footer, dateHMS = {
                hours: date.getHours(),
                second: date.getSeconds(),
                minute: date.getMinutes()
            };
            if (this.container.one('.' + Datepicker.CSS_NAME['calFoot'])) {
                var timeInput = this.getTimeInput();
                for (var p in dateHMS) {
                    if (dateHMS.hasOwnProperty(p)) {
                        timeInput[p].set('value', dateHMS[p]);
                    }
                }
            }  else {
                Y.mix(dateHMS, Datepicker.CSS_NAME);
                footer = Y.Lang.sub(Datepicker.TEMPLATES['footer'], dateHMS);
                this.get('contentBox').append(footer);
                this._bindFooterUI();
            }
        },
        /**
         * 对footer的时间input进行时间绑定，用于验证和选择
         * @method _bindFooterUI
         * @private
         */
        _bindFooterUI: function () {
            var instance = this;
            //点击箭头,对时间设置栏进行展开收缩
            this.container.delegate('click', function () {
                var timeSet = this.next();
                if (timeSet.getStyle("display") === "none") {
                    this.setHTML("&#9660");
                    timeSet.setStyle("display", "block");
                } else {
                    this.setHTML("&#9650");
                    timeSet.hide();
                }
            }, '.' + Datepicker.CSS_NAME["downArrow"]);

            //对时间输入进行验证
            //考虑支持键盘
            this.container.one('.' + Datepicker.CSS_NAME['calFoot']).delegate("blur", function () {
                var maxInput;

                if (this.get("className") === Datepicker.CSS_NAME["hoursInput"]) {
                    maxInput = 23;//小时最大23
                } else {
                    maxInput = 59;//分钟 秒最大59
                }
                var newValue = parseInt(this.get("value"), 10);
                instance._removeTip();
                if (!isNumber(newValue) || newValue > maxInput || newValue < 0) {
                    this.set("value", this.oldValue);
                    instance._displayTip("取值范围为：0～" + maxInput);
                    return;
                }
                instance.selectDay(null, true);
            }, 'input[type="text"]');

            this.container.delegate("focus", function () {
                this.oldValue = this.get("value");
            }, 'input[type="text"]');
        },
        /**
         * 设置时间做为当前时间
         * @method _setDate
         * @private
         */
        _setDate: function (val) {
            this.currentDate = this._getDate(val);
            return this.currentDate;
        },
        /**
         * 设置只显示年月
         * @method _onlyShowYearMonth
         */
        _onlyShowYearMonth: function (val) {
            if (val) {
                var bodyContent = this.get('contentBox');
                bodyContent.addClass(Datepicker.CSS_NAME['onlyShowYearMonth']);
            }
            return val;
        }
    }, {
        CSS_PREFIX: 'widget-datepicker',
        CSS_NAME: {
            onlyShowYearMonth: 'mt-datepicker-only-year-month',
            hoursInput: 'time-input-h',
            minuteInput: 'time-input-m',
            secondInput: 'time-input-s',
            downArrow: 'widget-datepicker-down-arrow',
            calFoot: 'widget-datepicker-foot'
        },
        TEMPLATES: {
            footer: '<div class="{calFoot}">' +
                        '<div class="{downArrow}">&#9660</div>' +
                        '<div class="widget-datepicker-time-set">' +
                            '<span><input type="text" class="{hoursInput}" maxlength="2" value="{hours}"/> 时</span>' +
                            '<span><input type="text" class="{minuteInput}" maxlength="2" value="{minute}"/> 分</span>' +
                            '<span><input type="text" class="{secondInput}" maxlength="2" value="{second}"/> 秒</span>' +
                        '</div>' +
                    '</div>'
        },
        ATTRS: {
            /**
             * input target
             * @attribute node
             * @type Node
             */
            node: {
                value: null
            },
            /**
             * 增加位置显示
             * @attribute xy
             * @type Object
             */
            xy: {
                value: null
            },
            /**
             * 增加显示时间
             * @attribute showSetTime
             * @type Boolean
             */
            showSetTime: {
                value: false,
                setter: '_showSetTime'
            },
            /**
             * 增加显示周几
             * @attribute appendWeekday
             * @type Boolean
             */
            appendWeekday: {
                value: false
            },
            /**
             * 是否只显示年月
             * @attribute onlyShowYearMonth
             * @type Boolean
             */
            onlyShowYearMonth: {
                value: false,
                setter: '_onlyShowYearMonth'
            },
            /**
             * 是否自动渲染
             * @attribute autoRender
             * @type Boolean
             */
            autoRender: {
                value: true
            },
            /**
             * 用于0或者空的情况下过滤使用
             * @attribute syncInputValue
             * @type Boolean
             */
            syncInputValue: {
                value: false
            },
            /**
             * 手动初始化控件的初始值
             * @attribute date
             * @type Date | String
             */
            date: {
                value: null,
                setter: '_setDate'
            }
        }
    });

    Y.mt.widget.Datepicker = Datepicker;

}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'widget',
        'mt-date',
        'w-date-base',
        'w-core',
        'node',
        'oop'
    ],
    skinnable: true
});
 
