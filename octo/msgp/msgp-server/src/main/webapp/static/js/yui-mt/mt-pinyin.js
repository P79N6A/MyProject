/*jshint indent:false, white:false, maxlen:1000000*/

/**
 * 提供将汉字转化为拼音的功能
 * @module mt-pinyin
 */
M.add("mt-pinyin",function(Y){
    var trim = Y.Lang.trim,
        isStr = Y.Lang.isString,
        db = Y.mt.vendor.Pinyin;

    Y.namespace("mt.pinyin");

    Y.mt.pinyin = {
        /**
         * 将中文字符转换为拼音
         * @method toPinyin
         * @param String str 需要转换的字符串
         */
        toPinyin: function(str) {
            var shortPin = '';
            var longPin = '';
            var i, c;
            for (i = 0; i < str.length; i++) {
                c = str.charAt(i);
                if (/^\w$/.test(c)) {   //数字、字母
                    shortPin += c;
                    longPin += c;
                } else if (db[c]) {     //汉字
                    shortPin += db[c].charAt(0);
                    longPin += db[c];
                }
            }
            return shortPin + ','+ longPin;
        },
        /**
         * 获取参数中汉字全拼和首拼
         * @method objListToPinyin
         * @param {Object|Array} objList 数组对象，或者objList对象转拼音
         * @param {String} param objList每组数据中要转换的具体字段
         */
        objListToPinyin: function(objList, param) {
            var p, pro, item, text, py;
            for (p in objList) {
                if (objList.hasOwnProperty(p)) {
                    item = objList[p];
                    if (item && typeof item === 'object') {
                        for (pro in item ) {
                            //保存当前对象拼音
                            if ( pro === param ) {
                                text = trim(item[param]);
                                py = this.toPinyin(text).split(',');
                                item.PY_SHORT = py[0];
                                item.PY_ALL = py[1];
                            } else if ( typeof item[pro] === 'object' ) {
                                this.objListToPinyin(item[pro], param);
                            }
                        }
                    } else if (isStr(item)) {
                        text = trim(item);
                        py = this.toPinyin(text).split(',');
                        objList[p] = {
                            id:p,
                            PY_SHORT:py[0],
                            PY_ALL:py[1]
                        };
                        objList[p][param] = text;
                    }
                }
            }
        }
    };

}, '1.0.0', { requires: [ 'mt-pinyin-db' ] });
