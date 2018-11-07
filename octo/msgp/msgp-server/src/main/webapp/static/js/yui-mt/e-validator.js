/**
 * 验证器模块
 * @module e-validator
 */

M.add('e-validator', function(Y) {
    var form = Y.mt.form;
    var Method = Y.mt.extension.Method;
    var Rules = Y.mt.extension.Rules;
    /**
     * 验证器
     * @class Validator
     */
    function Validator() {
        Validator.superclass.constructor.apply(this, arguments);
    }

    Validator.NAME = 'e-validator';
    Validator.ATTRS = {
        rules: {
            value: null
        },
        /**
         * 排除列表，包含其中的元素都不验证
         * @type Array
         */
        excludeArr: {
            value: []
        },
        /**
         * 排除列表，包含其中的元素都不验证
         * @type Array
         */
        excludeSelector: {
            value: ''
        },
        /**
         * 最外层容器
         * @type Node
         */
        container: {
            value: Y.one('body')
        },
        /**
         * @attribute parseDomRules
         * 是否初始化的时候解析data-rules
         */
        parseDomRules: {
            value: true
        },
        /**
         * @attribute parseAttrRules
         * 是否解析rules
         */
        parseAttrRules: {
            value: true
        },
        /**
         * 在验证或者保存的时候重新获得和解析规则
         */
        reParseRules: {
            value: false
        }
    };
    var proto = {
        /**
         * 初始化
         * @method initializer
         */
        initializer: function() {
            var container = this.get('form') || this.get('container');
            this.errorList = [];
            this.rulesManager = new Rules(
                container,
                this.get('rules'),
                this.get('parseDomRules'),
                this.get('parseAttrRules')
            );
        },
        /**
         * 析构
         * @method destructor
         */
        destructor: function() {

        },
        validate: function(container) {
            var matchNode, type;
            var _this = this;
            var res;
            container = container || this.get('container');
            //重新获取rules
            if (_this.get('reParseRules')) {
                _this.rulesManager.clearRules();
                _this.rulesManager.parsePageRules(container);
            }
            var rules = this.rulesManager.getRules();

            this.errorList = [];
            Y.Object.each(rules, function(rule, name) {
                matchNode = container.all('[name="'+ name +'"]');
                if(matchNode.size() < 1) return;

                type = matchNode.item(0).get('type');
                if(type === 'checkbox' || type === 'radio') {
                    _this.validateByNode(matchNode.item(0), rule);
                } else {
                    //相同name的rule相同
                    matchNode.each(function(item) {
                        _this.validateByNode(item, rule);
                    });
                }
            });
            if(this.errorList.length > 0) {
                res = {result: false, errorArr: this.errorList};
            } else {
                res = {result: true};
            }
            this.fire('VALIDATOR:COMPLETE', res);

            if (this.get('callback')) {
                this.get('callback')(res);
            }
            return res;
        },
        /**
         * 验证某一个node节点验证
         * @method validateByNode
         */
        validateByNode: function(node, rules) {
            var name, value, errorFind, res;
            var rulesManager = this.rulesManager;

            if (this.isExcluded(node)) return false;

            name = node.get('name');
            if(!name) return false;

            //重新解析
            if(!rules) {
                if(this.get('reParseRules')) {
                    rulesManager.clearRules();
                    rulesManager.addRulesByNode(node);
                }
                rules = rulesManager.getRules(name);
            }
            if(!rules) return false;

            value = this.getNodeValue(node);
            errorFind = this.validateByValue(value, rules);

            if(errorFind) {
                res = {
                    node: node,
                    name: name,
                    value: value,
                    msg: errorFind.msg || Y.Lang.sub(Method.messages[errorFind.rule], errorFind),
                    rule: errorFind
                };
                this.errorList.push(res);
                return res;
            }
            return false;
        },
        /**
         * 逐一验证
         * @method validateByValue
         */
        validateByValue: function(value, ruleArr) {
            var errorFind = false;
            Y.Object.some(ruleArr, function(rule) {
                var ruleName = rule.rule;
                var ruleFn =  Method[ruleName];
                if (rule.fn && !rule.fn()) {
                    return false;
                }
                if(!ruleFn) {
                    Y.log('配置的规则错误, 目前不支持' + ruleName);
                    return false;
                }

                //Rules预先定义好的规则
                if (!ruleFn(value, rule.value)) {
                    errorFind = rule;
                    return true;
                }
            });
            return errorFind;
        },
        getNodeValue: function(node) {

            var type = node.get('type'),
                name = node.get('name'),
                container = this.get('container');

            if(type === 'checkbox' || type === 'radio') {
                return form.field(container, name);
            } else {
                return node.get('value');
            }
        },
        /**
         * 查找该元素是否在被忽略的父容器中
         * @method isExcluded
         * @param {Node} node 节点
         */
        isExcluded: function(node) {
            var excluded = false;
            var excludeArr = this.get('excludeArr');
            var excludeSelector = this.get('excludeSelector');

            if (excludeSelector && node.test(excludeSelector)) {
                return false;
            }

            Y.Array.each(excludeArr, function(value) {
                if (node.ancestor(value)) {
                    excluded = true;
                }
            });
            return excluded;
        }
    };

    Y.extend(Validator, Y.Base, proto);
    Y.namespace('mt.extension');
    Y.mt.extension.Validator = Validator;

}, '1.0.0', { requires: ["mt-base", "base", 'node', 'mt-form', 'e-validator/method', 'e-validator/rules'] });

