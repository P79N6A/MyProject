/*jshint white:true*/
/*global M:true*/

/**
 * 二次开发基础模块
 * @module mt-base
 */
M.add('mt-base', function (Y) {
    var $N = Y.Node;
    var isStr = Y.Lang.isString;
    var isArray = Y.Lang.isArray;
    var isObject = Y.Lang.isObject;
    var isFunction = Y.Lang.isFunction;
    var trim = Y.Lang.trim;

    Y.namespace('mt.lang');
    /**
     * 语言层面的扩展
     * @class lang
     * @namespace mt
     * @static
     */
    Y.mt.lang = {
        /**
         * 时间格式化
         * [A JavaScript equivalent of PHP’s date](http://phpjs.org/functions/date:380)
         * [详细文档](http://php.net/manual/en/function.date.php)
         * @method date
         * @static
         * @param {String} format 时间格式，如Y-m-d H:i:s
         * @param {Date|String|Integer} [timestamp] 时间戳 注意：如果是整数，时间单位为秒
         * @example
         *      var time = Y.mt.lang.date('Y-m-d H:i');
         */
        date: function (format, timestamp) {
            var that = this,
                jsdate, f, formatChr = /\\?([a-z])/gi, formatChrCb,
                // Keep this here (works, but for code commented-out
                // below for file size reasons)
                //, tal= [],
                _pad = function (n, c) {
                    if ((n = n + "").length < c) {
                        return new Array((++c) - n.length).join("0") + n;
                    } else {
                        return n;
                    }
                },
                txtWords = ["Sun", "Mon", "Tues", "Wednes", "Thurs", "Fri", "Satur",
                "January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"],
                txtOrdin = {
                    1: "st",
                    2: "nd",
                    3: "rd",
                    21: "st",
                    22: "nd",
                    23: "rd",
                    31: "st"
                };
            formatChrCb = function (t, s) {
                return f[t] ? f[t]() : s;
            };
            f = {
            // Day
                // 返回月中的Date | Day of month w/leading 0; 01..31
                d: function () {
                    return _pad(f.j(), 2);
                },
                // Shorthand day name; Mon...Sun
                D: function () {
                    return f.l().slice(0, 3);
                },
                // Day of month; 1..31
                j: function () {
                    return jsdate.getDate();
                },
                // Full day name; Monday...Sunday
                l: function () {
                    return txtWords[f.w()] + 'day';
                },
                // ISO-8601 day of week; 1[Mon]..7[Sun]
                N: function () {
                    return f.w() || 7;
                },
                // Ordinal suffix for day of month; st, nd, rd, th
                S: function () {
                    return txtOrdin[f.j()] || 'th';
                },
                // Day of week; 0[Sun]..6[Sat]
                w: function () { //
                    return jsdate.getDay();
                },
                // Day of year; 0..365
                z: function () { //
                    var a = new Date(f.Y(), f.n() - 1, f.j()),
                        b = new Date(f.Y(), 0, 1);
                    return Math.round((a - b) / 864e5) + 1;
                },
                // ISO-8601 week number
                W: function () {
                    var a = new Date(f.Y(), f.n() - 1, f.j() - f.N() + 3),
                        b = new Date(a.getFullYear(), 0, 4);
                    return 1 + Math.round((a - b) / 864e5 / 7);
                },

            // Month
                // Full month name; January...December
                F: function () {
                    return txtWords[6 + f.n()];
                },
                // Month w/leading 0; 01...12
                m: function () {
                    return _pad(f.n(), 2);
                },
                // Shorthand month name; Jan...Dec
                M: function () {
                    return f.F().slice(0, 3);
                },
                // Month; 1...12
                n: function () {
                    return jsdate.getMonth() + 1;
                },
                // Days in month; 28...31
                t: function () {
                    return (new Date(f.Y(), f.n(), 0)).getDate();
                },

            // Year
                // Is leap year?; 0 or 1
                L: function () {
                    return (new Date(f.Y(), 1, 29).getMonth()) === 1 ? 1 : 0;
                },
                // ISO-8601 year
                o: function () {
                    var n = f.n(), W = f.W(), Y = f.Y();
                    return Y + (n === 12 && W < 9 ? -1 : n === 1 && W > 9);
                },
                // Full year; e.g. 1980...2010
                Y: function () {
                    return jsdate.getFullYear();
                },
                // Last two digits of year; 00...99
                y: function () {
                    return (f.Y() + "").slice(-2);
                },

            // Time
                // am or pm
                a: function () {
                    return jsdate.getHours() > 11 ? "pm" : "am";
                },
                // AM or PM
                A: function () {
                    return f.a().toUpperCase();
                },
                // Swatch Internet time; 000..999
                B: function () {
                    var H = jsdate.getUTCHours() * 36e2, // Hours
                        i = jsdate.getUTCMinutes() * 60, // Minutes
                        s = jsdate.getUTCSeconds(); // Seconds
                    return _pad(Math.floor((H + i + s + 36e2) / 86.4) % 1e3, 3);
                },
                // 12-Hours; 1..12
                g: function () {
                    return f.G() % 12 || 12;
                },
                // 24-Hours; 0..23
                G: function () {
                    return jsdate.getHours();
                },
                // 12-Hours w/leading 0; 01..12
                h: function () {
                    return _pad(f.g(), 2);
                },
                // 24-Hours w/leading 0; 00..23
                H: function () {
                    return _pad(f.G(), 2);
                },
                // Minutes w/leading 0; 00..59
                i: function () {
                    return _pad(jsdate.getMinutes(), 2);
                },
                // Seconds w/leading 0; 00..59
                s: function () {
                    return _pad(jsdate.getSeconds(), 2);
                },
                // Microseconds; 000000-999000
                u: function () {
                    return _pad(jsdate.getMilliseconds() * 1000, 6);
                },

            // Timezone
                // Timezone identifier; e.g. Atlantic/Azores, ...
                e: function () {
                    throw 'Not supported (see source code of date() for timezone on how to add support)';
                },
                // DST observed?; 0 or 1
                // Compares Jan 1 minus Jan 1 UTC to Jul 1 minus Jul 1 UTC.
                // If they are not equal, then DST is observed.
                I: function () {
                    var a = new Date(f.Y(), 0), // Jan 1
                        c = Date.UTC(f.Y(), 0), // Jan 1 UTC
                        b = new Date(f.Y(), 6), // Jul 1
                        d = Date.UTC(f.Y(), 6); // Jul 1 UTC
                    return 0 + ((a - c) !== (b - d));
                },
                // Difference to GMT in hour format; e.g. +0200
                O: function () {
                    var a = jsdate.getTimezoneOffset();
                    return (a > 0 ? "-" : "+") + _pad(Math.abs(a / 60 * 100), 4);
                },
                // Difference to GMT w/colon; e.g. +02:00
                P: function () {
                    var O = f.O();
                    return (O.substr(0, 3) + ":" + O.substr(3, 2));
                },
                // Timezone abbreviation; e.g. EST, MDT, ...
                T: function () {
                    return 'UTC';
                },
                // Timezone offset in seconds (-43200...50400)
                Z: function () {
                    return -jsdate.getTimezoneOffset() * 60;
                },

            // Full Date/Time
                // ISO-8601 date.
                c: function () {
                    return 'Y-m-d\\Th:i:sP'.replace(formatChr, formatChrCb);
                },
                // RFC 2822
                r: function () {
                    return 'D, d M Y H:i:s O'.replace(formatChr, formatChrCb);
                },
                // Seconds since UNIX epoch
                U: function () {
                    return parseInt(jsdate.getTime() / 1000, 10);
                }
            };
            /**
             * 格式化日期
             * @method date
             * @param {String} format 指定格式
             * @param {Number} [timestamp] 日期
             * @return {String|Number} 指定格式的日期
             */
            this.date = function (format, timestamp) {
                that = this;
                // 修改双重三元表达式为分支判断以通过jslint检查
                if (typeof timestamp === 'undefined') {
                    jsdate = new Date();
                } else {
                    jsdate = (timestamp instanceof Date) ? new Date(timestamp) : new Date(timestamp * 1000);
                }
                return format.replace(formatChr, formatChrCb);
            };
            return this.date(format, timestamp);
        },
        /**
         * 由字符串生成UNIX时间戳
         * [A JavaScript equivalent of PHP’s strtotime](http://phpjs.org/functions/strtotime:554)
         * [详细文档](http://php.net/manual/en/function.strtotime.php)
         * @method strtotime
         * @static
         * @param {String} str 时间字符串
         * @param {Integer} now 计算相对时间时使用的时间戳，默认为当前时间，注意：时间单位为秒
         * @return {Number} 时间戳，单位s
         */
        strtotime: function (str, now) {
            /*jshint maxcomplexity:30*/
            var i, match, s, strTmp = '', parse = '';

            strTmp = trim(str + '');
            strTmp = strTmp.replace(/\s{2,}|^\s|\s$/g, ' '); // unecessary spaces
            strTmp = strTmp.replace(/[\t\r\n]/g, ''); // unecessary chars

            if (strTmp === 'now') {
                return (new Date()).getTime() / 1000; // Return seconds, not milli-seconds
            } else if (!isNaN(parse = Date.parse(strTmp))) {
                return (parse / 1000);
            } else if (now) {
                now = new Date(now * 1000); // Accept PHP-style seconds
            } else {
                now = new Date();
            }

            strTmp = strTmp.toLowerCase();

            var __is =
            {
                day:
                {
                    'sun': 0,
                    'mon': 1,
                    'tue': 2,
                    'wed': 3,
                    'thu': 4,
                    'fri': 5,
                    'sat': 6
                },
                mon:
                {
                    'jan': 0,
                    'feb': 1,
                    'mar': 2,
                    'apr': 3,
                    'may': 4,
                    'jun': 5,
                    'jul': 6,
                    'aug': 7,
                    'sep': 8,
                    'oct': 9,
                    'nov': 10,
                    'dec': 11
                }
            };

            var process = function (m) {
                var ago = (m[2] && m[2] === 'ago');
                var num = (num = m[0] === 'last' ? -1 : 1) * (ago ? -1 : 1);

                switch (m[0]) {
                case 'last':
                case 'next':
                    switch (m[1].substring(0, 3)) {
                    case 'yea':
                        now.setFullYear(now.getFullYear() + num);
                        break;
                    case 'mon':
                        now.setMonth(now.getMonth() + num);
                        break;
                    case 'wee':
                        now.setDate(now.getDate() + (num * 7));
                        break;
                    case 'day':
                        now.setDate(now.getDate() + num);
                        break;
                    case 'hou':
                        now.setHours(now.getHours() + num);
                        break;
                    case 'min':
                        now.setMinutes(now.getMinutes() + num);
                        break;
                    case 'sec':
                        now.setSeconds(now.getSeconds() + num);
                        break;
                    default:
                        var day;
                        if (typeof (day = __is.day[m[1].substring(0, 3)]) !== 'undefined') {
                            var diff = day - now.getDay();
                            if (diff === 0) {
                                diff = 7 * num;
                            } else if (diff > 0) {
                                if (m[0] === 'last') diff -= 7;
                            } else {
                                if (m[0] === 'next') diff += 7;
                            }
                            now.setDate(now.getDate() + diff);
                        }
                        break;
                    }
                    break;

                default:
                    if (/\d+/.test(m[0])) {
                        num *= parseInt(m[0], 10);

                        switch (m[1].substring(0, 3)) {
                        case 'yea':
                            now.setFullYear(now.getFullYear() + num);
                            break;
                        case 'mon':
                            now.setMonth(now.getMonth() + num);
                            break;
                        case 'wee':
                            now.setDate(now.getDate() + (num * 7));
                            break;
                        case 'day':
                            now.setDate(now.getDate() + num);
                            break;
                        case 'hou':
                            now.setHours(now.getHours() + num);
                            break;
                        case 'min':
                            now.setMinutes(now.getMinutes() + num);
                            break;
                        case 'sec':
                            now.setSeconds(now.getSeconds() + num);
                            break;
                        default:
                            now.setSeconds(now.getSeconds() + num);
                            break;
                        }
                    } else {
                        return false;
                    }
                    break;
                }
                return true;
            };

            // match = strTmp.match(/^(\d{2,4}-\d{2}-\d{2})(?:\s(\d{1,2}:\d{2}(:\d{2})?)?(?:\.(\d+))?)?$/);
            // 修复无前缀0的错误，例如2010-9-1可以识别为2010-09-01
            match = strTmp.match(/^(\d{2,4}-\d{1,2}-\d{1,2})(?:\s(\d{1,2}:\d{1,2}(:\d{1,2})?)?(?:\.(\d+))?)?$/);

            if (match !== null) {
                if (!match[2]) {
                    match[2] = '00:00:00';
                } else if (!match[3]) {
                    match[2] += ':00';
                }

                s = match[1].split(/-/g);

                for (i in __is.mon) {
                    if (__is.mon[i] === s[1] - 1) {
                        s[1] = i;
                    }
                }
                s[0] = parseInt(s[0], 10);

                s[0] = (s[0] >= 0 && s[0] <= 69) ? '20' + (s[0] < 10 ? '0' + s[0] : s[0] + '') : (s[0] >= 70 && s[0] <= 99) ? '19' + s[0] : s[0] + '';
                return parseInt(Y.mt.lang.strtotime(s[2] + ' ' + s[1] + ' ' + s[0] + ' ' + match[2]) + (match[4] ? match[4] / 1000 : ''), 10);
            }

            var regex = '([+-]?\\d+\\s' +
                '(years?|months?|weeks?|days?|hours?|min|minutes?|sec|seconds?' +
                '|sun\\.?|sunday|mon\\.?|monday|tue\\.?|tuesday|wed\\.?|wednesday' +
                '|thu\\.?|thursday|fri\\.?|friday|sat\\.?|saturday)' +
                '|(last|next)\\s' +
                '(years?|months?|weeks?|days?|hours?|min|minutes?|sec|seconds?' +
                '|sun\\.?|sunday|mon\\.?|monday|tue\\.?|tuesday|wed\\.?|wednesday' +
                '|thu\\.?|thursday|fri\\.?|friday|sat\\.?|saturday))' +
                '(\\sago)?';

            match = strTmp.match(new RegExp(regex, 'gi')); // Brett: seems should be case insensitive per docs, so added 'i'
            if (match === null) {
                return false;
            }

            for (i = 0; i < match.length; i++) {
                if (!process(match[i].split(' '))) {
                    return false;
                }
            }

            return (now.getTime() / 1000);
        },
        /**
         * 获取日期对应周几，返回周一
         * @method getReadableWeek
         * @param {Date|String|Integer} 时间戳
         * @return {String} '周一','周日'
         */
        getReadableWeek: function (timestamp) {
            var weekDay = Y.mt.lang.date('w', timestamp),
                weekPattern = ['日', '一', '二', '三', '四', '五', '六'],
                slug = weekPattern[weekDay];
            return '周' + slug;
        },
        /**
         * 计算字符串长度，中文算两字符
         * @method getLength
         * @static
         * @param {String} str 字符串
         * @return {Number} 字符串长度
         */
        getLength: function (str) {
            if (!str || !isStr(str)) return 0;
            for (var i = 0, count = 0, len = str.length ; i < len; i++) {
                count = str.charCodeAt(i) > 255 ? count + 2 : count + 1;
            }
            return count;
        },
        /**
         * 截断字符串，中文算两字符
         * @method truncateStr
         * @static
         * @param {String} str 字符串
         * @param {Integer} len 长度
         * @param {String} [suffix=''] 截断后缀
         * @return {String} 截断后的字符串
         */
        truncateStr: function (str, len, suffix) {
            var i,
                realLen = 0,
                needSuffix = true,
                charCode,
                result = str;

            suffix = suffix || '';


            // 计算需要显示的实际长度
            for (i = 0; realLen <= len; i++) {
                charCode = str.charCodeAt(i);
                // 字符串长度小于限制长度 则不需要后缀
                if (isNaN(charCode)) {
                    needSuffix = false;
                    break;
                } else if (charCode > 255) {
                    // ASCII 大于255 的为中文字符,中文算两个字符
                    realLen += 2;
                } else {
                    realLen += 1;
                }
            }

            // 如果最后一次相加超出了len则 i-1
            if (realLen > len) {
                i--;
            }

            if (needSuffix) {
                result = str.substring(0, i) + suffix;
            }
            return result;
        },
        /**
         * 字符半角转全角
         * 全角空格为12288，半角空格为32
         * 其他字符全角(65281-65374)减去半角(33-126)等于65248
         * @method toFull
         * @static
         * @param {String} str 字符串
         * @return {String} 转换为全角的字符串
         */
        toFull: function (str) {
            var ret = '';
            for (var i = 0, len = str.length; i < len; i++) {
                var charCode = str.charCodeAt(i);
                if (charCode === 32) {
                    ret += String.fromCharCode(12288);
                } else if (charCode < 127) {
                    ret += String.fromCharCode(charCode + 65248);
                } else {
                    ret += str[i];
                }
            }
            return ret;
        },
        /**
         * 字符全角转半角
         * 全角空格为12288，半角空格为32
         * 其他字符全角(65281-65374)减去半角(33-126)等于65248
         * @method toHalf
         * @static
         * @param {String} str 字符串
         * @return {String} 转换为半角的字符串
         */
        toHalf: function (str) {
            var ret = '';
            for (var i = 0, len = str.length; i < len; i++) {
                var charCode = str.charCodeAt(i);
                if (charCode === 12288) {
                    ret += String.fromCharCode(32);
                } else if (charCode > 65280 && charCode < 65375) {
                    ret += String.fromCharCode(charCode - 65248);
                } else {
                    ret += String.fromCharCode(charCode);
                }
            }
            return ret;
        },
        /**
         * 从字符串转换成数组，相同键值将覆盖
         * @method parseStr
         * @static
         * @param {String} str 要转换的字符窜 token=abcdef&action=msg.del
         * @param {Boolean} [isCover=true] 如有相同键是否后者覆盖前者, 默认覆盖
         * @param {String} [joinKey='&'] join时候的字符串
         * @param {String} multipleJoin  如果有多个key相同，将其按照给定的key进行join
         * @return {Object} 转换得到的对象
         */
        parseStr: function (str, isCover, joinKey, multipleJoin, decode) {
            if (!str || !isStr(str)) return null;

            if (typeof isCover === 'undefined') isCover = true;
            if (typeof decode === 'undefined') decode = true;
            joinKey = joinKey || '&';

            var arr = str.split(joinKey),
                ret = {},
                i, len,
                pair, k, v;
            for (i = 0, len = arr.length; i < len; i++) {
                pair = arr[i].split('=');
                //pair[1]中含有"="的情况
                if (pair.length > 2) {
                    pair[1] = pair.slice(1).join('=');
                }
                k = decode ? decodeURIComponent(pair[0]) : pair[0];
                v = decode ? decodeURIComponent(pair[1]) : pair[1];
                if (isCover) {
                    ret[k] = v;
                } else {
                    if (ret.hasOwnProperty(k)) {
                        if (!isArray(ret[k])) ret[k] = [ret[k]];
                        ret[k].push(v);
                        if (multipleJoin) ret[k] = ret[k].join(multipleJoin);
                    } else {
                        ret[k] = v;
                    }
                }
            }
            return ret;
        },
        /**
         * 填补数字的空白
         * @method pad
         * @static
         * @param {Number} num 需要填补空白的数字
         * @param {Number} count 需要凑齐的位数
         * @param {String} [letter='0'] 填补的字母
         */
        pad: function (num, count, letter) {
            letter = letter || '0';
            var len = num.toString().length;
            while (len < count) {
                num = letter + num;
                len++;
            }
            return num;
        },
        /**
         * 解析服务器返回的UTC时间
         * @method parseDate
         * @static
         * @param {String} strTime 格式如Wed Jun 13 10:11:18 +0000 2007
         * @return {Number} 时间戳
         */
        parseDate: function (strTime) {
            var tmp = strTime.split(' ');
            var stdFormat = tmp[1] + " " + tmp[2] + ", " + tmp[5] + " " + tmp[3];
            return Date.parse(stdFormat) - (new Date()).getTimezoneOffset() * 60 * 1000;
        },
        /**
         * 获取服务器相对时间
         * @method getRelativeTime
         * @static
         * @param {String|Date} strTime 格式如Wed Jun 13 10:11:18 +0000 2007 或者Date对象
         * @return {String} 服务器相对时间
         */
        getRelativeTime: function (strTime) {
            var time = isStr(strTime) ? this.parseDate(strTime).valueOf() : strTime;
            var now = new Date();
            var seconds = parseInt((now.getTime() - time) / 1000, 10);
            if (seconds < 1) {
                return '1 秒前';
            } else if (seconds < 60) {
                return seconds + ' 秒前';
            } else if (seconds < 60 * 60) {
                return parseInt(seconds / 60, 10) + ' 分钟前';
            } else if (seconds < 60 * 60 * 24) {
                return '约 ' + parseInt(seconds / 3600, 10) + ' 小时前';
            } else {
                return this.getFullTime(strTime);
            }
        },
        /**
         * 获取服务器完整时间
         * @method getFullTime
         * @static
         * @param {String|Date} strTime 格式如Wed Jun 13 10:11:18 +0000 2007 或者Date对象
         * @return {String} 服务器完整时间，如2008-11-03
         */
        getFullTime: function (strTime) {
            var d = new Date(isStr(strTime) ? this.parseDate(strTime) : strTime);
            var year = d.getFullYear();
            var month = this.pad(d.getMonth() + 1, 2);
            var date = this.pad(d.getDate(), 2);
            var hour = this.pad(d.getHours(), 2);
            var minute = this.pad(d.getMinutes(), 2);
            return year + '-' + month + '-' + date + ' ' + hour + ':' + minute;
        },
        /**
         * 检查needle是否在数组中
         * [A JavaScript equivalent of PHP’s inArray](http://phpjs.org/functions/in_array:432)
         * @method inArray
         * @static
         * @param {Mixed} needle 需要查找的项
         * @param {Array} haystack 数组
         * @param {Boolean} argStrict 是否是严格匹配
         * @return {Boolean} 是否在数组中
         */
        inArray: function (needle, haystack, argStrict) {
            /*jshint eqeqeq:false*/
            var key = '', strict = !!argStrict;
            if (strict) {
                for (key in haystack) {
                    if (haystack[key] === needle) {
                        return true;
                    }
                }
            } else {
                for (key in haystack) {
                    if (haystack[key] == needle) {
                        return true;
                    }
                }
            }
            return false;
        },
        /**
         * 字符串第一个字符大写
         * @method ucfirst
         * @static
         * @param {String} str 字符串
         * @return {String} 结果字符串
         */
        ucfirst: function (str) {
            str += '';
            return str.charAt(0).toUpperCase() + str.substr(1);
        },
        /**
         * 将array-like对象转换为real array
         * @method toArray
         * @static
         * @param {Object} o array-like object
         * @return {Array} 数组
         */
        toArray: function (o) {
            var arr = null;
            try {
                // 非ie浏览器
                arr = Array.prototype.slice.call(o, 0);
            } catch (ex) {
                arr = [];
                for (var i = 0, len = o.length; i < len; ++i) {
                    arr.push(o[i]);
                }
            }
            return arr;
        },
        /**
         * 获得[min, max]区间的值
         * [参考文档](http://code.google.com/closure/library/docs/introduction.html#names)
         * @method clamp
         * @static
         * @param {Number} value 参考值
         * @param {Number} min 最小值
         * @param {Number} max 最大值
         * @return {Number} 结果值
         * @example
         *      Y.mt.lang.clamp(5, 2, 3); // output 3
         *      Y.mt.lang.clamp(5, 2, 9); // output 5
         */
        clamp: function (value, min, max) {
            return Math.min(max, Math.max(min, value));
        },
        /**
         * 对象是否存在
         * @method isExist
         * @static
         * @param {Object} o 待检查对象
         * @return {Boolean} 对象是否存在
         */
        isExist: function (o) {
            return o !== null && typeof o !== 'undefined';
        },
        /**
         * 判断是否为数值
         * @method isNumeric
         * @static
         * @param {*} o 待检查对象
         * @return {Boolean}
         */
        isNumeric: function (o) {
            return this.isExist(o) && (/\d/).test(o) && !isNaN(o);
        },
        /*
         * 替换html实体字符
         */
        htmlEscape: function (text) {
            return text.replace(/[<>"&]/g/*" to fool the syntax highlight*/, function (match) {
                switch (match) {
                case '<':
                    return "&lt;";
                case '>':
                    return "&gt;";
                case '&':
                    return "&amp;";
                case '"':
                    return "&quot;";
                }
            });
        }
    };

    Y.namespace('mt.util');
    /**
     * 工具函数
     * @class util
     * @namespace mt
     * @static
     */
    Y.mt.util = {
        /**
         * 获取指定对象上属性值，支持多级属性名称解析。
         * @method getProperty
         * @param {Object} o 属性宿主
         * @param {String} prop 属性名称
         * @param {Boolean} [notDeep=false] 为false时将开启多级属性解析。
         * @return {Mixed} 返回的属性值。undefined表示属性不存在
         */
        getProperty: function (o, prop, notDeep) {
            var keys,
                k;
            if (!o || !prop) {
                return undefined;
            }
            if (notDeep || prop.indexOf('.') === -1) {
                return o[prop];
            }
            keys = prop.split('.');
            while (keys.length) {
                k = keys.shift();
                if (Y.Lang.isUndefined(o[k]) || Y.Lang.isNull(o[k])) {
                    return o[k];
                } else {
                    o = o[k];
                }
            }
            return o;
        },
        /**
         * 从指定对象上获取多个属性值
         * @method getProperties
         * @param {Object} o 属性宿主
         * @param {Array} props 指定的属性名称
         * @return {Object} 返回的属性集合。注意：所有指定属性在hasOwnProperty时都会为true，但若该属性不存在于宿主上，其值为undefined
         *
         */
        getProperties: function (o, props) {
            var ret = {},
                _ = Y.mt.util;
            if (!o) {
                return null;
            }
            Y.Array.each(props, function (p) {
                var v = _.getProperty(o, p);
                if (!Y.Lang.isUndefined(v)) {
                    ret[p] = v;
                }
            });
            return ret;
        },
        /**
         * 检查浏览器是否支持placeholder
         * @method isSupportPlaceholder
         * @static
         * @return {Boolean} 是否支持placeholder
         */
        isSupportPlaceholder: function () {
            var elInput = document.createElement('input');
            return 'placeholder' in elInput;
        },
        /**
         * 解析Ajax返回对象
         * @method getEvalRes
         * @static
         * @param {Object} o
         * @param {String} customMsg 自定义错误字段的信息
         * @return {Object} JSON对象
         */
        getEvalRes: function (o, customMsg) {
            if (!o || !o.responseText) return o;
            var res = this.decodeJSON(o.responseText);
            return res || {'status': 0, 'msg': customMsg || '网络有问题，请稍后重试。'};
        },
        /**
         * 解析JSON字符串成对象
         * @method decodeJSON
         * @static
         * @param {String} str JSON字符串
         * @return {Object} JSON对象
         */
        decodeJSON: function (str) {
            /*jshint evil:true*/
            try {
                return eval('(' + str + ')');
            } catch (ex) {
                return null;
            }
        },
        /**
         * 解析节点属性保存的JSON
         * @method decodeAttr
         * @static
         * @param {Selector|HTMLElement|Node} nd
         * @param {String} attr 除去'data-'前缀的属性名。保存json数据的属性需以data-为前缀
         * @return {Object} JSON对象
         */
        decodeAttr: function (nd, attr) {
            var str = Y.mt.util.data(nd, attr);
            if (!str) return null;
            return Y.mt.util.decodeJSON(str);
        },
        /**
         * 通过ctrl enter来操作
         * @method ctrlEnter
         * @static
         * @param {Selector|HTMLElement|Node} nd
         * @param {Function} callback ctrlEnter后需要执行的函数
         */
        ctrlEnter: function (nd, callback) {
            nd = Y.one(nd);
            if (!nd) return;
            nd.on('key', function (e) {
                e.halt();
                callback();
            }, 'down:13+ctrl');
        },
        /**
         * 转换postData
         * @method toPostData
         * @static
         * @param {Object} o 要转换的对象 如{token:'abcdef', action:'msg.del'}
         * @param {Boolean} [isEncode=true] 是否需要encode
         * @return {String} POST的数据字符串
         */
        toPostData: function (o, isEncode) {
            if (!Y.Lang.isObject(o)) return o;
            if (typeof isEncode === 'undefined') isEncode = true;

            var arr = [],
                util = this;
            Y.Object.each(o, function (value, key) {
                var param;
                if (isArray(value)) {
                    Y.Array.each(value, function (item) {
                        param = util.joinKeyValue(key, item, isEncode);
                        arr.push(param);
                    });
                } else {
                    param = util.joinKeyValue(key, value, isEncode);
                    arr.push(param);
                }
            });
            return arr.join('&');
        },
        /**
         * 根据key,value生成键值对
         * @method joinKeyValue
         * @static
         * @param {String} key 键
         * @param {String} value 值
         * @param {Boolean} [isEncode] 是否编码
         * @return {String} key-value对组成的字符串
         */
        joinKeyValue: function (key, value, isEncode) {
            if (isEncode) {
                return this.encodeText(key) + "=" + this.encodeText(value);
            } else {
                return key + "=" + value;
            }
        },
        /**
         * 对字符编码
         * @method encodeText
         * @static
         * @param {String} text
         * @return {String} 编码后的字符串
         */
        encodeText: function (text) {
            return text ? encodeURIComponent(text) : text;
        },
        /**
         * 预加载图片
         * @method loadImage
         * @static
         * @param {String} src 图片地址
         */
        loadImage: function (src) {
            var img = new Image();
            img.src = src;
        },
        /**
         * 获取或设置节点属性(获取节点属性data-key的value，建议用Node.getData('key')代替)
         * @method data
         * @static
         * @param {Selector|HTMLElement|Node} nd 节点
         * @param {String} key 键
         * @param {String} value 值
         * @return {String|Boolean} 节点属性|是否设置成功
         */
        data: function (nd, key, value) {
            nd = Y.one(nd);
            if (!nd || !isStr(key)) return false;
            var attr = 'data-' + key;
            if (value === undefined) {
                return nd.getAttribute(attr);
            } else {
                if (!isStr(value)) {
                    value = Y.JSON.stringify(value);
                }
                nd.setAttribute(attr, value.replace(/'/g/*'*/, '&#039;'));
                return true;
            }
        },
        /**
         * 获取或设置表单项
         * @method field
         * @static
         * @param {Selector|HTMLElement|Node} ndForm form节点
         * @param {String} name 表单项名称
         * @param {String} value 值
         * @return {Node|Boolean} 表单项值|是否设置成功
         */
        field: function (ndForm, name, value) {
            ndForm = Y.one(ndForm);
            if (!ndForm || ndForm.get('tagName').toLowerCase() !== 'form') return false;
            var elForm = $N.getDOMNode(ndForm),
                ndField;
            if (value === undefined) {
                return Y.one(elForm.elements[name]);
            } else {
                // TODO 支持radio等同名元素数组
                ndField = Y.one(elForm.elements[name]);
                if (!ndField) return false;
                ndField.set('value', value);
                return true;
            }
        },
        /**
         * 切换显示隐藏区块
         * @method toggle
         * @static
         * @param {HTMLElement|Selector|Node} nd 区块元素
         * @param {Boolean} [flag] true为显示/false为隐藏
         */
        toggle: function (nd, flag) {
            nd = Y.one(nd);
            if (!nd) return;
            var show = flag === undefined ? nd.getStyle('display') === 'none' : flag;
            nd.setStyle('display', show ? 'block' : 'none'); // NOTE 此处不能将'block'换为''
        },
        /**
         * 添加onload监听器
         * @method addLoadListener
         * @static
         * @param {Function} handler 响应函数
         */
        addLoadListener: function (handler) {
            var oldListener = window.onload;
            if (!isFunction(oldListener)) {
                window.onload = handler;
            } else {
                window.onload = function () {
                    oldListener();
                    handler();
                };
            }
        },
        /**
         * 添加onbeforeunload监听器
         * @method addUnloadListener
         * @static
         * @param {Function} handler 响应函数
         */
        addUnloadListener: function (handler) {
            var oldListener = window.onbeforeunload;
            if (!isFunction(oldListener)) {
                window.onbeforeunload = handler;
            } else {
                window.onbeforeunload = function () {
                    var oldMsg, msg;
                    oldMsg = oldListener();
                    msg = handler();
                    return msg || oldMsg;
                };
            }
        },
        /**
         * 控制事件回调函数响应频率，例如scroll、resize
         * @method throttle
         * @static
         * @param {Function} handler
         * @param {Number} [time] 单位为ms
         * @return {Function} 添加频率控制的函数
         */
        throttle: function (handler, time) {
            var timer = null;
            time = time || 0;

            return function () {
                if (timer) return;
                timer = window.setTimeout(function () {
                    handler();
                    timer = null;
                }, time);
            };
        },
        /**
         * 传入一个函数func，和一个数字ms，返回一个f.
         * f函数调用时，将在ms毫秒后执行func.
         * 如果在ms内调用再次调用了f，则又将当前时刻为基准，
         * 在ms后执行func.
         *
         * @param {Function} func
         * @param {Number} ms
         * @returns {Function}
         */
        delay: function (func, ms) {
            var handler = null;
            return function () {
                if (handler) {
                    clearTimeout(handler);
                    handler = null;
                }
                var args = [].slice.call(arguments);
                handler = setTimeout(function () {
                    func.apply(null, args);
                }, ms);
            };
        },
        /**
         * 移除对象中的事件句柄
         * @method detachHandle
         * @static
         * @param {Object} o 保存事件句柄对象
         */
        detachHandle: function (o) {
            if (!o) return;
            var p;
            for (p in o) {
                if (o.hasOwnProperty(p) && o[p]) {
                    if (isFunction(o[p].detach)) {
                        o[p].detach();
                    } else {
                        Y.mt.util.detachHandle(o[p]);
                    }
                }
            }
        },
        /**
         * TODO 改进
         * javascript object to string
         * @method objToString
         * @static
         * @param {Object} o 对象
         * @param {String} level 递归
         * @return {String} JSON字符串
         */
        objToString: function (o, level) {
            if (level && level === "deep") return Y.JSON.stringify(o);

            var strArr = [],
                key = '',
                value = '',
                keys = Y.Object.keys(o);

            strArr.push('{');
            for (var i = 0; i < keys.length; i++) {
                key = keys[i];
                value = o[key];

                strArr.push(key + ':');
                // boolean、对象、string和number
                if (value) {
                    value = isObject(value) ? null : value.toString();
                }
                strArr.push("'" + value + "'");
                if (i !== keys.length - 1) {
                    strArr.push(',');
                }
            }

            strArr.push('}');
            return strArr.join('');
        },
        /**
         * 获取对象的keys 而非function的key
         * @deprecated 请使用Y.Object.keys
         * @method getKeys
         * @static
         * @param {Object} o 对象
         * @return {Array} key数组
         */
        getKeys: function (o) {
            var res = [];
            for (var p in o) {
                if (typeof o === "object" && typeof o !== "function" && o.hasOwnProperty(p)) {
                    res.push(p);
                }
            }
            return res;
        },
        /**
         * 重排。TIP：能够通过改善CSS解决reflow bug的，不要使用此方式
         * [force reflow in ie7](http://stackoverflow.com/questions/1702399/how-can-i-force-reflow-after-dhtml-change-in-ie7)
         * @method reflow
         * @static
         * @param {Selector|HTMLEelement|Node} node
         */
        reflow: function (node) {
            node = Y.one(node);
            var el = node ? node.getDOMNode() : document.body;
            el.className = el.className;
        },
        /**
         * Cookie
         * @class Cookie
         * @namespace mt.util
         * @static
         */
        Cookie: {
            /**
             * 获取过期时间
             * @method getExpiresDate
             * @static
             * @param {Number} days 天数
             * @param {Number} hours 小时数
             * @param {Number} minutes 分钟数
             * @return {String} 过期时间字符串
             */
            getExpiresDate: function (days, hours, minutes) {
                var ExpiresDate = new Date();
                //hours should be minutes
                if (typeof days === "number" && typeof hours === "number" && typeof minutes === "number") {
                    ExpiresDate.setDate(ExpiresDate.getDate() + parseInt(days, 10));
                    ExpiresDate.setHours(ExpiresDate.getHours() + parseInt(hours, 10));
                    ExpiresDate.setMinutes(ExpiresDate.getMinutes() + parseInt(minutes, 10));
                    return ExpiresDate.toGMTString();
                }
            },
            /**
             * 获取offset起的一对键值
             * @method _getValue
             * @private
             * @static
             * @param {Number} offset
             * @return {String} 一对键值组成的字符串
             */
            _getValue: function (offset) {
                var cookie = document.cookie,
                    endstr = cookie.indexOf(";", offset);
                if (endstr === -1) {
                    endstr = cookie.length;
                }
                return decodeURIComponent(cookie.substring(offset, endstr));
            },
            /**
             * 获取cookie值
             * @method get
             * @static
             * @param {String} name
             * @return {String} cookie值
             */
            get: function (name) {
                var cookie = document.cookie,
                    arg = name + "=",
                    alen = arg.length,
                    clen = cookie.length,
                    i = 0;
                while (i < clen) {
                    var j = i + alen;
                    if (cookie.substring(i, j) === arg) {
                        return this._getValue(j);
                    }
                    i = cookie.indexOf(" ", i) + 1;
                    if (i === 0) break;
                }
                return "";
            },
            /**
             * 设置cookie值
             * @method set
             * @static
             * @param {String} name
             * @param {String|Number} value
             * @param {String} expires
             * @param {String} path
             * @param {String} domain
             * @param {String} secure
             */
            set: function (name, value, expires, path, domain, secure) {
                document.cookie = name + "=" + encodeURIComponent(value) +
                    ((expires) ? "; expires=" + expires : "") +
                    ((path) ? "; path=" + path : "") +
                    ((domain) ? "; domain=" + domain : "") +
                    ((secure) ? "; secure" : "");
            },
            /**
             * 删除cookie值
             * @method remove
             * @static
             * @param {String} name
             * @param {String} path
             * @param {String} domain
             */
            remove: function (name, path, domain) {
                if (this.get(name)) {
                    document.cookie = name + "=" +
                        ((path) ? "; path=" + path : "") +
                        ((domain) ? "; domain=" + domain : "") +
                        "; expires=Thu, 01-Jan-70 00:00:01 GMT";
                }
            },
            /**
             * 清除mt cookie值
             * TODO 此函数有一些问题，清除cookie需要指定path和domain，但是path和domain无法从document.cookie中取得
             * @method clear
             * @static
             */
            clear: function () {
                var cookies = document.cookie.split(';');
                for (var i = 0; i < cookies.length; i++) {
                    this.remove(trim(cookies[i].split('=')[0]));
                    this.remove(trim(cookies[i].split('=')[0]));
                }
            }
        }
    };

    Y.namespace('mt.Tracking');
    /**
     * 页面追踪
     * @class Tracking
     * @namespace mt
     * @static
     */
    Y.mt.Tracking = {
        /**
         * 初始化事件自动跟踪
         * 注意：如果是监听链接，注意不要使用stopEvent来停止默认事件，否则ga无法完成统计。
         *       可以使用preventDefault或者javascript:void(0)。
         * 1.自定义跟踪GA事件，标签需添加gaevent属性, 如<dt gaevent="Help|Open|/help/Newbie/PayToday">...</dt>
         * 2.跟踪外链区分来源，a标签需添加galabel属性，如<a galabel="default" herf="http://kaixin001.com/...">...</a>
         * @method init
         * @static
         */
        init: function () {
            var GA_EVENT = 'gaevent', // 自定义trackEvent属性
                GA_LABEL = 'galabel', // 通用trackEvent外链属性
                _gaq = window._gaq,
                ndDoc = Y.one(document.body);

            ndDoc.delegate('click', function () {
                var node = this;

                Y.mt.Tracking.trackEventByNode(node);
                trackGACustomEvent(node);

                if (node.test('a')) {
                    trackOutLink(node);
                }
            }, '[gaevent], [data-mtevent], a');

            /**
             * 处理自定义GA事件
             * @method trackGACustomEvent
             * @param { Node } nd DOM节点
             */
            function trackGACustomEvent(nd) {
                var attr = nd.getAttribute(GA_EVENT);
                if (!attr) return;
                var attrs = attr.split('|'),
                    gaCategory = attrs[0],
                    gaEvent = attrs[1],
                    gaLabel = attrs[2],
                    gaValue = attrs[3];

                if (!gaLabel && nd.get('href')) {
                    gaLabel = nd.get('href').replace('http://', '');
                }
                if (typeof _gaq !== 'undefined' && _gaq) {
                    _gaq.push(['_trackEvent', gaCategory, gaEvent, gaLabel, gaValue]);
                }
            }

            /**
             * 处理外链
             * @method trackOutLink
             * @param { Node } nd Node节点
             */
            function trackOutLink(nd) {
                var link = nd.get('href'),
                    reg = /^(http:\/\/|mailto:)/,
                    index,
                    label;

                if (link && reg.test(link)) {
                    link = link.replace('http://', '');
                    index = link.indexOf('?');
                    label = nd.getAttribute(GA_LABEL);
                    if (index > 0) {
                        link = link.substring(0, index);
                    }

                    if (/\.(meituan|mt|sankuai)\.com/.test(link)) {
                        // 跳过美团内部链接
                        return;
                    }

                    if (label) {
                        link += '#label=' + label;
                    }

                    if (_gaq) {
                        _gaq.push(['_trackEvent', 'OutLink', 'Click', link]);
                    }
                }
            }
        },
        /**
         * 追踪pv
         * @method trackPageview
         * @static
         * @param {String} url
         * @param {Object} extra
         */
        trackPageview: function () {
            M.trackPageview.apply(Y, arguments);
        },
        /**
         * 追踪事件
         * @method trackEvent
         * @static
         * @param {String} category
         * @param {String} action
         * @param {String} label
         * @param {Object} extra
         */
        trackEvent: function () {
            M.trackEvent.apply(Y, arguments);
        },
        /**
         * 追踪节点事件
         * @method trackEventByNode
         * @static
         * @param {Node} nd
         */
        trackEventByNode: function (nd) {
            var MT_EVENT = 'mtevent',
                data = Y.mt.util.decodeAttr(nd, MT_EVENT),
                category, action, label;
            if (!data) return;
            category = data.ca;
            action = data.ac;
            label = data.la;
            delete data.ca;
            delete data.ac;
            delete data.la;

            this.trackEvent(category, action, label, data);
        }
    };

    /**
     * 宏
     * @class macro
     * @namespace mt
     * @static
     */
    Y.mt.macro = {
        /**
         * key map
         * @property key
         * @static
         * @type {Object}
         */
        key: {
            BACKSPACE   : 8,
            TAB         : 9,
            ENTER       : 13,
            PAGEUP      : 33,
            PAGEDOWN    : 34,
            ESC         : 27,
            LEFT        : 37,
            UP          : 38,
            RIGHT       : 39,
            DOWN        : 40
        }
    };

    /**
     * 使用Y.mt.require而不是使用namespace.
     * 因为module名已经限定了模块的名字，没有必要另起namespace的炉灶。
     * 如果模块没有加载, Y.mt.require将抛出友好的错误提示。
     *
     * @param module
     * @returns {*}
     */
    Y.mt.require = function (module) {
        var exports = Y.Env._exported;
        if (!Object.prototype.hasOwnProperty.call(exports, module)) {
            throw new Error(
                    'Please `Y.use` the module [' + module + '] before `Y.mt.require` it.' +
                    'Otherwise you must forgot add `es:true` when defined the module.'
            );
        }
        return exports[module];
    };

    Y.namespace('mt.widget');
    /**
     * 详见: https://123.sankuai.com/km/page/28118429
     * @class widget
     * @namespace mt
     * @description 
     * @static
     */
    Y.mix(Y.mt.widget, {
        /**
         * 预加载
         * @method preload
         * @static
         */
        preload: function (container) {
            //有关widget的初始化记录
            M.Widget = {
                preload: {
                    modules: [],
                    instances: {}
                },
                lazyload: {
                    modules: [],
                    instances: {}
                },
                map: this.getWidgetMap(),
                instances: {}
            };

            var widgetMap = M.Widget.map,
                preload = M.Widget.preload,
                lazyload = M.Widget.lazyload,
                decode = Y.mt.util.decodeJSON;

            var nodeList;
            var widgetSelector = '[data-widget], [data-uix]';
            if (container) {
                var ndContainer = Y.one(container);
                if (ndContainer) {
                    nodeList = ndContainer.all(widgetSelector);
                    if (ndContainer.hasAttribute('data-widget') || ndContainer.hasAttribute('data-uix')) {
                        nodeList.push(ndContainer);
                    }
                }
            } else {
                nodeList = Y.all(widgetSelector);
            }

            if (!nodeList || !nodeList.size()) {
                return;
            }

            nodeList.each(function (ndWidget) {
                var widgetName = ndWidget.getAttribute('data-widget') || ndWidget.getAttribute('data-uix'),
                    widgetLowerName = widgetName.toLowerCase(),
                    indexOf = Y.Array.indexOf;

                // 将属性写在DomNode上，防止不同实例的YUI生成不同实例的ndWidget
                // 不写在attribute上的原因是，节点可能会被复制。
                if (ndWidget.getDOMNode().hasWidgetInited) return;
                ndWidget.getDOMNode().hasWidgetInited = true;

                if (widgetName && (widgetMap[widgetLowerName] || widgetMap[widgetName])) {
                    var params = decode(ndWidget.getAttribute('data-params')) || ndWidget.getData('data-params') || {},
                        isUIX = ndWidget.hasAttribute('data-uix'),
                        isLazyload = !!params.lazyload;

                    if (isLazyload) {
                        if (!lazyload.instances[widgetName]) {
                            lazyload.instances[widgetName] = [];
                        }
                        lazyload.instances[widgetName].push({node: ndWidget, params: params, isUIX: isUIX});
                        if (indexOf(lazyload.modules, widgetMap[widgetLowerName]) === -1) {
                            lazyload.modules.push(widgetMap[widgetLowerName]);
                        }
                    } else {
                        if (!preload.instances[widgetName]) {
                            preload.instances[widgetName] = [];
                        }
                        preload.instances[widgetName].push({node: ndWidget, params: params, isUIX: isUIX});
                        if (indexOf(preload.modules, widgetMap[widgetLowerName]) === -1) {
                            preload.modules.push(widgetMap[widgetLowerName]);
                        }
                    }
                }
            });

        },
        /**
         * 初始化
         * @method init
         * @static
         * @event Widget.init.complete
         */
        init: function (container) {
            this.preload(container);

            var _this = this,
                ucfirst = Y.mt.lang.ucfirst,
                preload = M.Widget.preload,
                lazyload = M.Widget.lazyload;

            if (preload.modules.length && !Y.Object.isEmpty(preload.instances)) {
                Y.use(preload.modules, function (Y) {
                    Y.Object.each(preload.instances, callback);
                    Y.Global.fire("Widget.init.complete", M.Widget);
                });
            }

            if (lazyload.modules.length && !Y.Object.isEmpty(lazyload.instances)) {
                if (document.readyState === 'complete') {
                    Y.use(lazyload.modules, function (Y) {
                        Y.Object.each(lazyload.instances, callback);
                    });
                } else {
                    var onload = window.onload;
                    window.onload = function () {
                        if (onload) {
                            onload();
                        }
                        Y.use(lazyload.modules, function (Y) {
                            Y.Object.each(lazyload.instances, callback);
                        });
                    };
                }
            }

            function callback(instances, key) {
                var widgetName = ucfirst(key);
                Y.Array.each(instances, function (instance) {
                    var widget = instance.isUIX ? Y.mt.uix[widgetName] : Y.mt.widget[widgetName];
                    if (widget) {
                        _this.widgetInit(widget, instance.node);
                    }
                });
            }
        },
        /**
         * 实例化widget
         * @method widgetInit
         * @static
         * @param {Class} Widget 控件构造器
         * @param {Node} nd 节点
         */
        widgetInit: function (Widget, node) {
            var decode = Y.mt.util.decodeJSON,
                isNewWidget = Widget.superclass && Widget.superclass.constructor && Widget.superclass.constructor.NAME,
                params = decode(node.getAttribute('data-params')) || node.getData('data-params') || {},
                isUIX = node.hasAttribute('data-uix'),
                widgetInstance;

            if (isUIX) {
                widgetInstance = new Widget(node, params);
            } else if (isNewWidget) {
                //yui3类的Widget初始化
                params.node = node;
                widgetInstance = new Widget(params);
                widgetInstance.render();
            } else {
                //一般的Widget初始化
                widgetInstance = new Widget(node, params);
            }
            this.saveWidget(node, widgetInstance);
        },
        /**
         * 保存widget
         * @method saveWidget
         * @static
         * @param {Selector|HTMLElement|Node} nd
         * @param {Object} widget
         */
        saveWidget: function (nd, widget) {
            nd = Y.one(nd);
            if (!nd) return;
            if (!nd.get('id')) nd.set('id', Y.guid());
            M.Widget.instances[nd.get('id')] = widget;
        },
        /**
         * 取回widget
         * @method fetchWidget
         * @static
         * @param {Selector|HTMLElement|Node} nd
         * @return {Object} widget实例
         */
        fetchWidget: function (nd) {
            var ndId;
            nd = Y.one(nd);
            if (nd) ndId = nd.get('id');

            return ndId ? M.Widget.instances[ndId] : null;
        },
        /**
         * 可被初始化的widget序列
         * @method getWidgetMap
         * @static
         * @return {Object} widget map
         */
        getWidgetMap: function () {
            return {
                buttontopost: "w-base",
                cascade: "w-select-cascade",
                livesearch: "w-livesearch",
                flashcopy: "w-flashcopy",
                commondialog: "w-base",
                headertip: "w-base",
                autocompletelist: "w-autocomplete",
                commontable: "w-table",
                sorttable: "w-table",
                ajaxtable: "w-table",
                paginator: "w-paginator",
                datepicker: "w-date",
                colorpicker: "w-colorpicker",
                timepicker: "w-date",
                validate: "w-validate",
                rating: "w-rating",
                dialog: "w-dialog",
                linkdialog: "w-dialog",
                bubbletip: "w-bubbletip",
                slider: "w-slider",
                verticaltab: "w-tab",
                tab: "w-tab",
                tabindex: "w-tab",
                picgroup: "w-image",
                picphotodialog: "w-image-dialog",
                realtimeeditor: "w-realtime-editor",
                hovermark: "w-hovermark",
                guide: "w-guide",
                imagecopper: 'w-image-copper',
                collapse: "uix-collapse",
                tooltip: "uix-tooltip",
                dropdown: "uix-dropdown",
                sticky: "uix-sticky",
                tabview: "uix-tabview",
                scrollspy: 'uix-scrollspy',
                smoothscroll: "uix-smoothscroll",
                mapview: "w-mapview"
            };
        }
    });
},
// version
'1.0.0',
// dependency
{
    requires: [
        'node',
        'event',
        'json'
    ]
});
 
