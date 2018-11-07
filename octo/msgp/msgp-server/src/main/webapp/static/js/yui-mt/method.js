/**
 * 验证器模块
 * @module e-validator/rule
 */

M.add('e-validator/method', function(Y) {

    var trim = Y.Lang.trim;

    var Method = {
        /**
         *
         * 限定最大勾选数
         * @method
         * @param ${}
         * @return
         */
        maxChecked: function (value, param) {
            return Y.Lang.isArray(value) && value.length <= param;
        },
        /**
         * 验证邮箱
         * @method email
         * @param {Number} value 节点value值
         */
        email: function(value) {
            /*jshint maxlen:2000 */
            var reg = new RegExp(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
            return reg.test(value);
        },
        /**
         * 验证链接
         * @method url
         * @param {Number} value 节点value值
         */
        url: function(value) {
            /*jshint maxlen:2000 */
            var reg = new RegExp(/^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i);
            return reg.test(value);
        },
        /**
         * 系统时间格式
         * @method date
         * @param {Number} value 节点value值
         */
        date: function(value) {
            return new RegExp(/Invalid|NaN/).test(new Date(value));
        },
        /**
         * 标准时间格式：2011-11-12
         * @method dateISO
         * @param {Number} value 节点value值
         */
        dateISO: function(value) {
            return new RegExp(/^\d{4}-\d{1,2}-\d{1,2}$/).test(value);
        },
        /**
         * 标准时间格式：2011-11-12 11:11:11
         * @method dateTime
         * @param {Number} value 节点value值
         */
        dateTime: function(value) {
            return new RegExp(/^\d{4}-\d{1,2}-\d{1,2} \d{1,2}[\/:]\d{1,2}[\/:]\d{1,2}$/).test(value);
        },
        /**
         * 邮编
         * @method zipCode
         * @param {Number} value 节点value值
         */
        zipCode: function(value) {
            return new RegExp(/^[0-9]{6}$/).test(value);
        },
        /**
         * 电话号码
         * @method telephone
         * @param {Number} value 节点value值
         */
        telephone: function(value) {
            return new RegExp(/^(\d{3,4}-?)?\d{7,9}$/g).test(value);
        },
        /**
         * 各种电话
         */
        phone: function(value) {
            return this.telephone(value) || this.mobile(value) || this.virtualPhone(value);
        },
        /**
         *
         */
        virtualPhone: function(value) {
            new RegExp(/^(\d{3,4}-){2}\d{3,4}$/g).test(value);
        },
        /**
         * 手机号码
         * @method mobile
         * @param {Number} value 节点value值
         */
        mobile: function(value) {
            var reg = new RegExp(/^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/);
            return (value.length === 11 && reg.test(value));
        },
        /**
         *
         */
        minute: function(value) {
            return new RegExp(/^(\d{1,2}):\d{1,2}$/g).test(value);
        },
        /**
         * 全中文
         * @method chinese
         * @param {Number} value 节点value值
         */
        chinese: function(value) {
            return new RegExp(/^[\u4e00-\u9fa5]+$/).test(value);
        },
        /**
         * 全英文
         * @method english
         * @param {Number} value 节点value值
         */
        english: function(value) {
            return new RegExp(/^[a-zA-Z]+$/).test(value);
        },
        /**
         * 中文或英文
         * @method chOrEn
         * @param {Number} value 节点value值
         */
        chOrEn: function(value) {
            return new RegExp(/^([\u4e00-\u9fa5]|[a-zA-Z])+/).test(value);
        },
        /**
         * 非中文
         * @method notChinese
         * @param {Number} value 节点value值
         */
        notChinese: function(value) {
            return !(new RegExp(/^[\u4e00-\u9fa5]+/).test(value));
        },
        /**
         * 验证数字:包含小数
         * @method number
         * @param {Number} value 节点value值
         */
        number: function(value) {
            return new RegExp(/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/).test(value);
        },
        /**
         * 验证正数,包括小数
         */
        positiveNumber: function(value) {
            return new RegExp(/^(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/).test(value);
        },
        /**
         * 验证小数
         */
        decimal: function(value) {
            return new RegExp(/^0(?:\.\d+)?$/).test(value);
        },
        /**
         * 验证全数字,不能有小数点
         * @method integer
         * @param {Number} value 节点value值
         */
        integer: function(value) {
            return new RegExp(/^\d+$/).test(value);
        },
        /**
         * 包括正整数和负整数和0
         */
        allInteger: function(value) {
            return new RegExp(/^-?\d+$/).test(value);
        },
        /**
         * 验证信用卡
         * @method creditCard
         * @param {Number} value 节点value值
         */
        creditCard: function(value) {
            // accept only digits and dashes
            if (/[^0-9\-]+/.test(value))
                return false;
            var nCheck = 0,
                nDigit = 0,
                bEven = false;

            value = value.replace(/\D/g, "");

            for (var n = value.length - 1; n >= 0; n--) {
                var cDigit = value.charAt(n);
                nDigit = parseInt(cDigit, 10);
                if (bEven) {
                    if ((nDigit *= 2) > 9)
                        nDigit -= 9;
                }
                nCheck += nDigit;
                bEven = !bEven;
            }

            return (nCheck % 10) === 0;
        },
        /**
         * 最小长度
         * @method minlength
         * @param {Number} value 节点value值
         * @param {Number} param 判断值
         */
        minlength: function(value, param) {
            return trim(value).length >= param;
        },
        /**
         * 最大长度
         * @method maxlength
         * @param {Number} value 节点value值
         * @param {Number} param 判断值
         */
        maxlength: function(value, param) {
            return trim(value).length <= param;
        },
        /**
         * 最大值
         * @method max
         * @param {Number} value 节点value值
         * @param {Number} param 判断值
         */
        max: function(value, param) {
            return value <= param;
        },
        /**
         * 最小值
         * @method min
         * @param {Number} value 节点value值
         * @param {Number} param 判断值
         */
        min: function(value, param) {
            return value >= param;
        },
        equalTo: function(value, param) {
            return value === param;
        },
        // 自定义匹配
        regexp: function(value, param) {
            return new RegExp(param).test(value);
        }
    };

    Y.Object.each(Method, function (rule, name) {
        Method[name] = function (value) {
            if (value === "") {
                return true;
            } else {
                return rule.apply(Method, arguments);
            }
        };
    });

    /**
     * 验证必填字段
     * @param  node 节点
     */
    Method.required = function(value) {
        return value && value.length > 0;
    };

    Method.messages = {
		required: "必填",
        requiredByVal: "必填",
        remote: "Please fix this field.",
        email: "请输入正确的Email.",
        url: "请输入正确的链接地址.",
        date: "请输入正确的时间(毫秒数).",
        dateTime: "请输入标准时间格式 2011-11-11 11:11:11",
        dateISO: "请输入标准时间格式 2012-06-26 .",
        number: "请输入正确的数字,可以为小数.",
        positiveNumber: "请输入正数，可以为小数.",
        decimal: "请输入正确的小数",
        integer: "请输入正确的数字，只能为整数.",
        allInteger: "请输入正确的数字，可以为正负整数和0.",
        creditCard: "请输入正确的信用卡号.",
        equalTo: "输入不一致.",
        mobile: "请输入正确的手机号码.",
        telephone: "请输入正确的电话号码.",
        phone: "请输入正确的电话号码.",
        minute: "请输入正确的时间",
        zipCode: '请输入正确的邮政编码',
        chinese:'请输入正确的中文',
        english: '请输入正确的英文',
        chOrEn: '请输入中文或者英文',
        notChinese: '不能输入中文,请检查',
        minlength: "请最少输入{value} 个字符.",
        maxlength: "请最多输入{value} 个字符.",
        maxChecked: "最多只能勾选{value}项.",
        max: "输入项的值不超过{value}.",
        min: "输入项的值不小于{value}.",
        regexp: '输入不符合要求'
    };
    Y.namespace('mt.extension').Method = Method;

}, '1.0.0', { requires: [] });

