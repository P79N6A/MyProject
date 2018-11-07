/**
 * 表单验证组件, 负责rule的parse以及管理
 * @module w-validate-base
 */
M.add('e-validator/rules', function(Y) {

    var trim = Y.Lang.trim;
    var isArray = Y.Lang.isArray;
    var isObject = Y.Lang.isObject;

    Y.namespace('mt.extension');

    /**
     * 规则管理
     * @class Rules
     * @namespace mt.extension
     * @constructor
     */
    var Rules = function(container, rules, parseDomRules, parseAttrRules) {
        //cache池，均统一为name当作key
        this.rulesPool = {};
        this.tipsPool = {};
        this.container = container;

        //初始化的时候是否解析页面的data-rules
        if(parseDomRules !== false) {
            this.parsePageRules();
        }
        //是否解析传入的rules
        if(rules && parseAttrRules !== false) {
            this.parseRules(rules);
        }
    };

    Rules.prototype = {
        /**
         * 获得转换后的rule string
         * @method getRulesString
         * name
         */
        getRulesStr: function(name) {
            var rules = this.rulesPool[name];
            if(!rules) return '';
            var res = {};
            Y.Array.each(rules, function(rule) {
                res[rule.rule] = rule;
            });
            return Y.JSON.stringify(res);
        },
        getTips: function(name) {
            return this.tipsPool[name] || '';
        },
        /**
         * 获得转换后的rule obj
         * @method getRulesString
         * name
         */
        getRules: function(name) {
            if(!name) return this.rulesPool;
            return this.rulesPool[name];
        },
        getNode: function(selector) {
            if (selector instanceof Y.Node || selector instanceof Y.NodeList) {
                return selector;
            }
            return this.container.one(selector) || this.container.one('[name="' + selector + '"]');
        },
        /**
         * 解析container下的所有的规则
         * @method addAllRules
         */
        parsePageRules: function(container) {
            container = container || this.container;
            var nlNode = container.all('*[data-rules]');
            var _this = this;

            if (nlNode) {
                nlNode.each(function(node) {
                    _this.addRulesByNode(node);
                });
            }
        },
        /**
         * 根据元素上定义的data-rules, 加入到rulesPool里面管理
         * 会替换掉已有的name的rules
         * @method addRulesByNode
         * @param {Node} node
         */
        addRulesByNode: function(node) {
            var tips = trim(node.getAttribute('data-tips'));
            var rules = trim(node.getAttribute('data-rules'));
            var name = node.getAttribute('name');
            var rulesArr;

            if(!name) return;
            rulesArr = this._parseStrRules(rules);
            this.rulesPool[name] = rulesArr;
            this.tipsPool[name] = tips;
        },
        /**
         * 在已有的rule上加入更多规则
         * 会进行简单的parse
         * @method addRuleByName
         */
        addRuleByName: function(name, rules, tips) {
            var originRules = this.rulesPool[name] || [];
            rules = this._parseSingleRules(rules.rule, rules);
            this.rulesPool[name] = originRules.concat(rules);
            this.tipsPool[name] = tips;
        },
        /**
         * 在已有的rule上加入更多规则
         * @method addRuleBySeletor
         */
        addRuleBySeletor: function(selector, rules, tips) {
            var node = this.getNode(selector);
            if(!node) return;
            var name = node.getAttribute('name');
            if(!name) return;
            this.addRuleByName(name, rules, tips);
        },
        /**
         * 添加多条规则
         * @method addRules
         */
        addRules: function(name, rules, tips) {
            if(isObject(rules)) {
                this.parseRules(rules);
            } else {
                this._addStrRules(name, rules, tips);
            }
        },
        /**
         * 添加rules
         * @method _addRules
         */
        _addStrRules: function(name, rules, tips) {
            var rulesArr = this._parseStrRules(rules);
            this.rulesPool[name] = rulesArr;
            this.tipsPool[name] = tips;
        },
        /**
         * 解析自定制的rules
         * @method parseRules
         */
        parseRules: function(rules) {
            if(!rules) return;
            var _this = this;

            Y.Array.each(rules, function(rule) {
                _this._parseObjRules(rule);
            });
        },
        /**
         * 转化对象形式的Rules
         * @method _parseObjRules
         * @param {Object} rules
         * @return {Object}
            rules links:
            {node: '.chinese-name', rule: 'chinese'},
            {node: '.english-name', rule: 'english', msg:'请填写英文字母aaa'},
            {node: ['.chinese-name', '.english-name', '#hometown', '#inputEmail', 'sex', 'interest'], rule: 'required', msg:['中文不允许为空','英文不允许为空','家乡不允许为空','邮箱不允许为空']},
            {node: ['.chinese-name', '.english-name'], rule: 'minlength', value:[3,4,], msg:['至少输入3个中文','至少输入4个英文']},
            {'#inputEmail':'email'}

            change to:
            {node: node, rule: 'xx', msg: 'xxx', value: value}
        */
        _parseObjRules: function(rule) {
            var nds = rule.node,
                value = rule.value,
                msg = rule.msg,
                _this = this;

            var getInfo = function(o, index) {
                return isArray(o) ? o[index] : '';
            };
            //{'#inputEmail':'email'}
            if(!nds) {
                Y.Object.each(rule, function(ruleName, selector) {
                    _this.addRuleBySeletor(selector, {rule: ruleName});
                });
            }
            //数组拉平
            if(isArray(nds)) {
                //{node: ['.chinese-name', '.english-name', '#hometown', '#inputEmail', 'sex', 'interest'], rule: 'required'}
                Y.Array.each(nds, function(nd, index) {
                    _this.addRuleBySeletor(nd, {
                        msg: getInfo(msg, index),
                        value: getInfo(value, index),
                        rule: rule.rule
                    });
                });
            } else {
                //{node: '.chinese-name', rule: 'chinese'}
                this.addRuleBySeletor(nds, rule);
            }
        },
        /**
         * 转化string形式的Rules
         * @method _parseStrRules
         * @param {String} rules
         * @return {Object}
            rules likes:
            {"chinese":{"value":true, "msg":"请输入中文"}, "required":{"value":true, "msg":"必填"}, "minlength":3, "maxlength":5}
            {"chinese": true, 'required': true}
            'chinese', 'required'

            change to:
            [{rule: rule,  value: value, msg: ''}, {}]
         */
        _parseStrRules: function(rule) {
            var jsonReg = /^[\{\[].*[\}\]]$/;
            var res = {};
            var rulesArr = [];
            var arr;
            var _this = this;

            if(jsonReg.test(rule)) {
                try {
                    //兼容非规范JSON串
                    res = Y.mt.util.decodeJSON(rule);
                } catch(e) {
                    res = rule;
                }
            } else {
                arr = rule.split(',');
                Y.Array.each(arr, function(item) {
                    if(!item) return;
                    res[trim(item)] = true;
                });
            }
            Y.Object.each(res, function(ruleValue, ruleName) {
                rulesArr.push(_this._parseSingleRules(ruleName, ruleValue));
            });
            return rulesArr;
        },
        /**
          * ruleValue为简单的类型，转换为标准的结构
          * @method _parseSingleRules
            ruleValue like
            {value: xx, msg: 'xx'} || true || 3
            to
            {rule: ruleName, value: String ruleValue ? ruleValue : ruleValue.value, msg: ruleValue.msg || "")}
          */
        _parseSingleRules: function(ruleName, ruleValue) {
            var res = {value: true, msg: ''};

            if(isObject(ruleValue)) {
                Y.mix(res, ruleValue, true);
            } else {
                res.value = ruleValue;
            }
            res.rule = ruleName;
            return res;
        },
        /**
         * 清空所有的rules
         * @method clearRules
         */
        clearRules: function() {
            this.rulesPool = {};
            this.tipsPool = {};
        }
    };
    Y.mt.extension.Rules = Rules;

},'1.0.0', { requires: []});
