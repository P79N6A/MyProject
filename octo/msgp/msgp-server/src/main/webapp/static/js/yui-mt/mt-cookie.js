/*jshint white:true, unused:true*/
/**
 * 操作Cookie
 * @module mt-cookie
 */
M.add('mt-cookie', function (Y) {
    var trim = Y.Lang.trim;
    Y.mt.cookie = {
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
            if (typeof days === "number" && typeof hours === "number" && typeof hours === "number") {
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
            if (Y.Lang.trim(name)) {
                document.cookie = name + "=" + encodeURIComponent(value) +
                    ((expires) ? "; expires=" + expires : "") +
                    ((path) ? "; path=" + path : "") +
                    ((domain) ? "; domain=" + domain : "") +
                    ((secure) ? "; secure" : "");
            }
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
                this.remove(trim(cookies[i].split('=')[0]), '/', 'mt.com');
                this.remove(trim(cookies[i].split('=')[0]), '/', 'kxapp.mt.com');
            }
        }
    };
},
'1.0.0',
{
    requires: ['mt-base']
});
 
