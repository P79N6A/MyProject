/**
 * 关于时间日期的一些常用功能
 * @module mt-date
 */
M.add('mt-date', function(Y){

    var isDate = Y.Lang.isDate;
    var isArray = Y.Lang.isArray;

    Y.namespace('mt.date');
    /**
     * 关于时间日期的一些常用功能
     * @class date
     * @namespace mt
     * @static
     */
    Y.mt.date = {
        /**
         * 获取两个时间段的差
         * @method getTimeReduction
         * @param {Date} begin 开始时间
         * @param {Date} end 结束时间
         * @return {Number} 时间的毫秒数
         */
        getTimeReduction: function(begin, end, isYMD) {
            if(!isDate(begin) || !isDate(end)) return null;
            if(isYMD) {
                begin = new Date(begin.getFullYear(), begin.getMonth(),begin.getDate());
                end = new Date(end.getFullYear(), end.getMonth(), end.getDate());
            }
            return end.getTime() - begin.getTime();
        },
        /**
         * 在当前时间是否在一个时间段内
         * @method isInRegions
         * @param {Date} currentDate 当前时间
         * @param {Date} startDate 开始时间
         * @param {Date} endDate 结束时间
         * @param {Boolean} allowRegionOverlap 是否允许边界重叠
         */
        isInRegions: function(currentDate, startDate, endDate, allowRegionOverlap) {
            if(!isDate(currentDate) || !isDate(startDate) || !isDate(endDate)) return false;

            var startReduction = this.getTimeReduction(startDate, currentDate);
            var endReduction = this.getTimeReduction(currentDate, endDate);
            if (allowRegionOverlap) {
                return  startReduction > 0 &&  endReduction > 0;
            } else {
                return  startReduction >= 0 && endReduction >= 0;
            }
        },
        /**
         * 比较两个时间是否相等
         * @method equalTime
         * @param {Date} time1 第一个时间
         * @param {Date} time2 另一个时间
         * @param {Boolean} [part=false] 若part=true, 只比较到年月日
         * @return {Boolean} 两个时间是否相等
         */
        equalTime: function(time1, time2, part) {
            if(part) {
                return (time1.getFullYear() === time2.getFullYear()) &&
                (time1.getMonth() === time2.getMonth()) &&
                (time1.getDate() === time2.getDate());
            } else {
                return time1.getTime() === time1.getTime();
            }
        },
        /**
         * 格式化时间
         * @method formatDate
         * @param { Date } time
         * @param { String } [interval] 年月日数值间的连接符，默认返回格式类似：2012年12月05日
         * @return { String } 格式化后的字符串
         */
        formatDate: function(time, interval) {
            //TODO 支持更多的时间格式
            if(!Y.Lang.isDate(time)) return null;
            var fullYear = time.getFullYear();
            var month = time.getMonth();
            var day = time.getDate();
            var textMonth = month + 1;
            var textDay = day;
            if(textMonth < 10 ){
                textMonth = '0' + textMonth;
            }
            if (day < 10) {
                textDay = '0' + textDay;
            }
            if(interval) {
                return [fullYear, textMonth, textDay].join(interval);
            }
            return fullYear + "年" + textMonth + "月" + textDay + "日";
        },
        /**
         * 格式化日期
         * format="yyyy-MM-dd hh:mm:ss";
         * format="yyyy年MM月dd日hh时mm分ss";
         * @private
         * @method
         * @param param
         * @return return
         */
        formatDateByString: function(date, format) {
            if (!date) {
                return;
            }
            var o = {
                "M+" :  date.getMonth()+1,//month
                "d+" :  date.getDate(),//day
                "h+" :  date.getHours(),//hour
                "m+" :  date.getMinutes(),//minute
                "s+" :  date.getSeconds(),//second
                "q+" :  Math.floor((date.getMonth()+3)/3),
                "S"  :  date.getMilliseconds()//millisecond
            };

            if (/(y+)/.test(format)) {
                format = format.replace(RegExp.$1, (date.getFullYear()+"").substr(4 - RegExp.$1.length));
            }

            for (var k in o) {
                if (new RegExp("("+ k +")").test(format)) {
                    format = format.replace(RegExp.$1, RegExp.$1.length ===1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length));
                }
            }
            return format;
        },
        /**
         * 判断若干个日期区间是否重叠
         * @private
         * @method
         * @param ${Array} periodArray 二维数组，子数组为开始日期、结束日期
         * @param {Boolean} allowRegionOverlap 是否允许边界重叠
         * @return ${return}
         */
        isPeriodsOverlap: function(periodArray, allowRegionOverlap) {
            if (!Y.Lang.isArray(periodArray)) {
                return;
            }
            allowRegionOverlap = allowRegionOverlap === false ? false : true;
            var _this = this;
            //转化日期字符串为Date对象
            Y.Array.each(periodArray, function(period) {
                var startDate = period[0];
                var endDate = period[1];
                if (Y.Lang.isString(startDate)) {
                    startDate = _this.buildDateByFormatedString(startDate);
                }
                if (Y.Lang.isString(endDate)) {
                    endDate = _this.buildDateByFormatedString(endDate);
                }
                period[0] = startDate;
                period[1] = endDate;
            });
            //判断是否重叠
            var isOverlay = false;
            Y.Array.each(periodArray, function(period, index) {
                for (var i = index + 1; i < periodArray.length; i++) {
                    var currentPeriod = periodArray[i];
                    var rs = _this.isTwoPeriodOverlap(period, currentPeriod, allowRegionOverlap);
                    if (rs) {
                        isOverlay = true;
                        return;
                    }
                }
            });
            return isOverlay;
        },
        /**
         * 判断两个区间是否重叠
         * @private
         * @method
         * @param {Boolean} allowRegionOverlap 是否允许边界重叠
         * @return ${return}
         */
        isTwoPeriodOverlap: function(period1, period2, allowRegionOverlap) {
            var rightStartReduceLeftEnd = this.getTimeReduction(period1[1], period2[0]);
            var leftStartReduceRightEnd = this.getTimeReduction(period2[1], period1[0]);
            if (allowRegionOverlap) {
                return !(rightStartReduceLeftEnd >= 0 || leftStartReduceRightEnd >= 0);
            } else {
                return !(rightStartReduceLeftEnd > 0 || leftStartReduceRightEnd > 0);
            }
        },
        /**
         * 比较两个日期
         * yyyy-MM-dd hh:mm:ss
         * @private
         * @method
         * @param ${}
         * @return -1、0、1，分别代表小于、等于、大于
         */
        compareByString: function(dateString1, dateString2) {
            var date1 = this.buildDateByFormatedString(dateString1);
            var date2 = this.buildDateByFormatedString(dateString2);
            return this.compareDate(date1, date2);
        },
        /**
         * 比较两个日期的大小
         * @private
         * @method
         * @param ${}
         * @return ${return}
         */
        compareDate: function(date1, date2) {
            var dateTime1 = date1.getTime();
            var dateTime2 = date2.getTime();
            return dateTime1 === dateTime2 ? 0 : (dateTime1 < dateTime2 ? -1 : 1);
        },
        /**
         * 根据指定格式的日期字符串生成日期对象
         * yyyy-MM-dd hh:mm:ss
         * @private
         * @method
         * @param ${}
         * @return ${return}
         */
        buildDateByFormatedString: function(dateString) {
            if (!Y.Lang.isString(dateString)) {
                return;
            }
            dateString = Y.Lang.trim(dateString);
            var dates = dateString.split(" ")[0];
            var times = dateString.split(" ")[1];
            //获取年月日
            var datesArray = dates.split("-");
            var year = parseInt(datesArray[0], 10);
            var month = parseInt(datesArray[1], 10);
            var day = parseInt(datesArray[2], 10);
            //获取时分秒
            var hour = 0;
            var minute = 0;
            var second = 0;
            if (times) {
                var timesArray = times.split(":");
                hour = parseInt(timesArray[0]||0, 10);
                minute = parseInt(timesArray[1]||0, 10);
                second = parseInt(timesArray[2]||0, 10);
            }
            return new Date(year, month-1, day, hour, minute, second);
        },
        /**
         * 获取某月份的最大天数
         * @method getMaxDaysInMonth
         * @param { Number } year 年份
         * @param { Number } month 月份
         * @return { Number } 该月份的最大天数
         */
        getMaxDaysInMonth: function(year, month) {
            month = parseInt(month, 10);
            var time = year + "/"+ month + "/0";
            var date = new Date(year, month, 0);
            return date.getDate();
        },
        /**
         * 填充时间段到input框中
         * @method fillTimeSection
         * @param { Array } timeSection 时间段，开始与结束时间的数值，如：[2010, 2012]或[01, 12]
         * @param { Node } ndStartTime 要填充开始时间的Node节点
         * @param { Node } ndEndTime 要填充结束时间的Node节点
         */
        fillTimeSection: function(timeSection, ndStartTime, ndEndTime) {
            if(!ndStartTime || !Y.Lang.isArray(timeSection)) return;
            if(ndEndTime) {
                ndStartTime.set('value', timeSection[0]);
                ndEndTime.set('value', timeSection[1]);
            }
        },
        //将所有node字符串变为节点类型
        _replaceOptionString2Node: function (options) {
            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                if (typeof option === 'string') {
                    options[i] = Y.Node.create(option);
                }
            }
        },
        /**
         * 添加年份或者月份选择框的option
         * @method buildDateSelect
         * @param { Selector | Node | HTMLElement } str select节点或其选择器
         * @param { Object } conf 配置的一些参数
         * @param { Array } conf.options=[] 默认选项
         * <ul>
         * <li> from: start time </li>
         * <li> to: end time </li>
         * <li> type:year || month </li>
         * <li> selectVal: 填写选中项 </li>
         * <li> fillZero: 是否需要填充0, 一般对于月份需要进行填充 </li>
         * </ul>
         */
        buildDateSelect: function(str, conf) {
            var ndSelect = Y.one(str);
            if(!ndSelect) return;

            conf = conf || {};
            if(typeof conf.options === 'undefined') {
                conf.options = [];
            } else if (isArray(conf.options)) {
                //将所有node字符串替换为节点类型
                this._replaceOptionString2Node(conf.options);
            }

            var month = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'];
            var options = conf.options;

            if(conf.type === 'year') {
                conf.to = conf.to || new Date().getFullYear();
            } else {
                conf.from = conf.from || 1;
                conf.to = (conf.to <= 12) ? conf.to : 12;
            }

            for(var i = conf.from; i <= conf.to; i++) {
                var optionText = "";
                var optionValue = i;
                var selected = "";
                if(conf.type === 'year') {
                    optionText = i;
                } else {
                    optionText =  month[i-1];
                    //双位值
                    if(conf.fillZero && i < 10) {
                        optionValue = '0' + i;
                    }
                }
                if(conf.selectVal) {
                    if (conf.selectVal.toString() === optionValue.toString()) {
                        selected = " selected='selected'";
                    }
                }
                options.push(Y.Node.create('<option value="' + optionValue + '"' + selected + '>' + optionText + '</option>'));
            }
            ndSelect.setHTML(new Y.NodeList(options).toFrag());
        },
        /**
         * 常用的时间段，可计算诸如“前两个月”、“这星期”、“后5年”...之类的开始、结束日期
         * @method calculateDay
         * @param { String } name 时间段名称
         * @param { String } now 参照日期，默认为当前日期
         * @return { Array } 返回时间段的开始、结束日期的数组
         * 注：未包括诸如设定某一开始日期，计算其3个月后的结束日期的功能，可通过Y.mt.lang.strtotime()实现
         */
        calculateDay: function(name, now) {
            var beginTime, endTime, recentdays;
            now = now || new Date();
            var res = [],
                _this = this;

            var reg = /^([pnt]{1})(\d*)(\w+)/;
            var m = name.match(reg);
            //兼容原有方法中的“recentdays-n”形式
            if(/recentdays/.test(name)) {
                recentdays = name.split('-')[1];
                m = [name, 'p', recentdays, 'day'];
            }
            if(m) {
                //向前或向后推算的单位量
                var num = parseInt(m[2], 10) || 1,
                    pre = m[1];

                switch(m[3].substring(0, 3)) {
                case 'day':
                    res = _this._calcDay(pre, num, now);
                    break;
                case 'wee':
                    res = _this._calcWeek(pre, num, now);
                    break;
                case 'mon':
                    res = _this._calcMonth(pre, num, now);
                    break;
                case 'qua':
                    res = _this._calcQuarter(pre, num, now);
                    break;
                case 'yea':
                    res = _this._calcYear(pre, num, now);
                    break;
                default:
                    if (m[0] === 'today') {
                        beginTime = endTime =  now.getTime();
                    }
                    break;
                }
            }
            if (res.length > 0) {
                beginTime = res[0];
                endTime = res[1];
            }
            if(beginTime > endTime) {
                var temp = beginTime;
                beginTime = endTime;
                endTime = temp;
            }
            endTime = this.formatDate(new Date(endTime), '-');
            beginTime = this.formatDate(new Date(beginTime), '-');
            return [beginTime, endTime];
        },
        _calcDay: function(pre, num, now) {
            var f = 1,
                beginTime, endTime;
            switch(pre) {
            case 't':
                f = 0;
                break;
            case 'p':
                f = -1;
                break;
            default:
                break;
            }
            now.setDate(now.getDate() + num * f);
            beginTime = now.getTime();
            now.setDate(now.getDate() - num * f);
            endTime = now.getTime();
            return [beginTime, endTime];
        },
        _calcWeek: function(pre, num, now) {
            var f = 1, //f、g均为正负号功能
                g = 1,
                n = 1, //用于计算星期一或日
                beginTime, endTime;
            //星期日getDay()为0，应转为7
            var day  = now.getDay() === 0 ? 7 : now.getDay();

            switch(pre) {
            case 't':
                f = 0;
                break;
            case 'p':
                f = -1;
                break;
            case 'n':
                n = 7;
                g = -1;
                break;
            default:
                break;
            }
            now.setDate(now.getDate() + 7 * num * f - day + n);
            beginTime = now.getTime();
            now.setDate(now.getDate() - 7 * (num - 1) * f + g * 6);
            endTime = now.getTime();
            return [beginTime, endTime];
        },
        _calcMonth: function(pre, num, now) {
            var f = 1, //正负号功能
                g = 1, //用于计算某月的第一天或最后一天
                j = 0,
                beginTime, endTime;

            switch(pre) {
            case 't':
                f = 0;
                j = 1;
                break;
            case 'p':
                f = -1;
                break;
            case 'n':
                num = num + 1;
                g = 0;
                j = 2;
                break;
            default:
                break;
            }
            now.setMonth(now.getMonth() + num * f);
            now.setDate(g);
            beginTime = now.getTime();

            now.setMonth(now.getMonth() - num * f + j);
            now.setDate(Number(!g));
            endTime = now.getTime();
            return [beginTime, endTime];
        },
        _calcQuarter: function(pre, num, now) {
            var f = 1, //正负号的功能
                qua = parseInt( now.getMonth() / 3, 10) + 1, //得到第几季
                monthStart = qua * 3 - 2, //得到季的首月份
                monthEnd,
                g = 1, //g用于计算该月的第一天或最后一天
                beginTime, endTime;

            switch(pre) {
            case 't':
                f = -1;
                monthEnd = 3;
                break;
            case 'p':
                f = -1;
                monthStart = monthStart - 3 * num;
                monthEnd = 3 * num;
                break;
            case 'n':
                monthStart = monthStart + 3 * (num + 1);
                g = 0;
                monthEnd = -3 * num + 1;
                break;
            default:
                break;
            }
            now.setMonth(monthStart - 1);
            now.setDate(g);
            beginTime = now.getTime();

            now.setMonth(now.getMonth() + monthEnd);
            now.setDate(Number(!g));
            endTime = now.getTime();
            return [beginTime, endTime];
        },
        _calcYear: function(pre, num, now) {
            var f = 1, //正负号功能
                first0 = 0, //用于设置每年的最后一天或第一天
                first1 = 1,
                last0 = 11,
                last1 = 31,
                beginTime, endTime;

            switch(pre) {
            case 't':
                f = 0;
                break;
            case 'p':
                f = -1;
                break;
            case 'n':
                first0 = last0;
                first1 = last1;
                last0 = 0;
                last1 = 1;
                break;
            default:
                break;
            }
            now.setFullYear(now.getFullYear() + f * num);
            now.setMonth(first0);
            now.setDate(first1);
            beginTime = now.getTime();

            now.setFullYear(now.getFullYear() - f * num + f);
            now.setMonth(last0);
            now.setDate(last1);
            endTime = now.getTime();
            return [beginTime, endTime];
        }
    };

}, '1.0.0', { requires: ['mt-base', 'node'] });
