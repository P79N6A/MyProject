/**
 * @module w-select-cascade
 */
M.add('w-select-cascade', function (Y) {

    var $IO = Y.io;
    var $N = Y.Node;
    var L = Y.Lang;
    var isNumber = L.isNumber;
    var isArray = L.isArray;
    var isObject = L.isObject;
    var $Util = Y.mt.util;


    Y.namespace('mt.widget');

    /**
     * 级联菜单
     * <p>params格式:{ type:'static'|'dynamic', data:obj, action:array, noChildrenText:string, prefix:array, defaultValue:array }</p>
     * <p>type = 'static':静态调用，一次性传入所有数据；type = 'dynamic':动态调用，select change时向特定action请求数据并更新下一级，require:action</p>
     * <p>data：option数据</p>
     * <p>format: 数据格式。default：要求属性名称为各个option的父级value。
     * 每项option的数据格式为[value, text]。例如：{0:[[1, 'meituan'], ...], 1:[...], ...}; 'hierarchy'：{province0:{city0:[distinct0, distinct1]}, ... }。
     * id对应的text另为一个数组; 'level': {0:{title:beijing, data:{1:dongcheng, 2:xicheng}}} TODO 增加对format的自动辨别</p>
     * <p>action：请求action，若需用到前面第i级select的value，可用'{i}'替代</p>
     * <p>noChildrenText：无选项时显示内容</p>
     * <p>nullValue：空选项value</p>
     * <p>prefix：为了防止不同级别select中option value出现重复情况，引入的value前缀</p>
     * <p>defaultValue：初始化</p>
     * <p>例子:<div data-widget="cascade" data-params="{ type:'static', data:{...}, defaultValue:[1,2,3] }">
     *              <select name="province"><option value="-1" checked="checked">-----</option></select>
     *              <select name="city"><option value="-1" checked="checked">-----</option></select>
     *              <select name="distinct"><option value="-1" checked="checked">-----</option></select>
     *          </div>
     * </p>
     * @class Cascade
     * @namespace mt.widget
     * @param {Selector|HTMLElement|Node} nd
     * @param {Object} params 传递的参数
     */
    Y.mt.widget.Cascade = function(nd, params) {
        if (!nd) return;
        var nlSelect = nd.all('select');
        if (!nlSelect.size()) return;

        // 初始化参数
        var config = {
            type: 'static',
            noChildrenText: '无子类',
            prefix: '',
            format: 'default'
        };
        Y.mix(params, config);
        // 默认空值
        var NULL_VALUE = params.nullValue || '-1';
        // 是否存在默认选项
        var hasDefaultOption = checkDefaultOption();
        // 默认值
        var defaultValues = rememberDefault();
        if (!nd.get('id')) {
            nd.set('id', Y.guid());
        }

        markSelect();
        if (params.defaultValue) {
            init();
        }
        if (params.type === 'dynamic') {
            if (!params.action) {
                // 支持在select上声明data-action
                params.action = [];
                nlSelect.each(function(select) {
                    var action = $Util.data(select, 'action') || '';
                    params.action.push(action);
                });
            }
        }
        register();

        /**
         * 检测是否含有默认选项
         * @method checkDefaultOption
         */
        function checkDefaultOption(){
            var len = nlSelect.size();
            var lastOption = nlSelect.item(len - 1).get('options').item(0);
            if (lastOption && lastOption.get('value') === NULL_VALUE) {
                // 默认显示选项的判断依据为 第一个option 且 value为-1
                return true;
            }
            return false;
        }

        /**
         * 记录默认选项
         * @method rememberDefault
         */
        function rememberDefault(){
            var i, len = nlSelect.size(), r = [];
            for (i = 0; i < len; ++i) {
                if (nlSelect.item(i).get('selectedIndex') < 0) break;
                r.push(nlSelect.item(i).get('value'));
            }
            return r;
        }

        /**
         * 为select添加index标记
         * @method markSelect
         */
        function markSelect(){
            nlSelect.each(function(select, i) {
                select.setData('index', i);
                if (!select.get('id')) {
                    select.set('id', Y.guid());
                }
            });
        }

        /**
         * 初始化选中项，WARN:js操作select不会触发change事件
         * @method init
         * @param {Array} args 选中项数值
         */
        function init(args){
            args = args || params.defaultValue;
            setByValues(args);
        }

        /**
         * 监听事件，并做相应处理
         * @method register
         */
        function register(){
            var idx = null, transData = null, vals;
            // 监听select的change事件
            nlSelect.each(function(select) {
                select.on('change', function(e) {
                    update(this);
                });
            });

            // 监听reset事件, 注：暂不支持dynamic类型select项重置
            Y.Global.on('Cascade.reset', function(args) {
                if (nd.get('id') === args.id) {
                    resetCascade();
                }
            });

            // 监听set事件，可供其他方法或模块改变级联菜单某项,args = { element:htmlnode, setting:array }
            Y.Global.on('Cascade.set', function(args){
                if (nd.get('id') === args.id) {
                    setByValues(args.setting);
                }
            });
        }
        function resetCascade() {
            var vals = params.defaultValue || defaultValues;
            setByValues(vals);
        }
        /**
         * 根据参数设置级联菜单
         * @method setByValues
         * @param  {Array} values
         */
        function setByValues(values){
            for (var i = 0, len = nlSelect.size(); i < len; ++i) {
                if (typeof values[i] === 'undefined') return;
                update(nlSelect.item(i), values[i]);
            }
        }

        /**
         * 更新select文本框
         * @method update
         * @param {Node} select
         * @param {Number} value
         */
        function update(select, value){
            var i, len = nlSelect.size();
            var idx = select.getData('index');
            if (typeof value !== 'undefined') {
                // js设置选中项
                selectByValue(select, value);
            }
            if (idx < len - 1) {
                if (params.type === 'dynamic' && params.action[idx + 1]) {
                    // 异步获取数据
                    asyncReq(idx + 1);
                    for (i = idx + 2; i < len; ++i) {
                        clearSelect(nlSelect.item(i));
                    }
                } else {
                    // 处理不需异步获取数据的select
                    var ndNext = nlSelect.item(idx + 1);
                    var optionData = formatData(idx);
                    fillSelect(ndNext, optionData);
                    if (optionData.length === 1) {
                        // 若下一级只有一项，则选中
                        update(ndNext, optionData[0][0]);
                    } else {
                        for (i = idx + 2; i < len; ++i) {
                            clearSelect(nlSelect.item(i));
                        }
                    }
                }
            } else if (idx === (len - 1)) {
                Y.Global.fire('Cascade.noChildren', { id:nd.get('id'), index:len });
            }
            Y.Global.fire('Cascade.change', { id:nd.get('id'), index:idx });
        }

        /**
         * 默认情况处理
         * @method defaultSituation
         * @param {Number} index 索引值
         * @return {Array} data 目标数据数组
         */
        function defaultSituation(index) {

            // example: { 0:[[1, 'province1'], [2, 'province2']], p1:[[1, 'city1']], p2:[[2, 'city2'], [3, 'city3']], c1:[[1, 'district1'], [2, 'district2']] },
            // { 0:[[1, 'title1'], [2, 'title2']], 1:[[3, 'title3']], 2:[[4, 'title4'], [5, 'title5']] },
            // [[[1, 'name1'], [2, 'name2']], [[3, 'name3']], [[4, 'name4'], [5, 'name5']]]
            var prefix = params.prefix[index + 1] || params.prefix;
            var val = nlSelect.item(index).get('value'),
                data = [];

            if (!isArray(params.data)) {
                data = params.data[prefix + val] || [];
            } else {
                data = params.data[parseInt(val, 10)] || [];
            }
            return data;
        }
        /**
         * 层级情况处理
         * @method hierarchySituation
         * @param {Number} index 索引值
         * @return {Array} data 目标数据数组
         */
        function hierarchySituation(index) {
            // example: { link:{ 0:[ 1, 2 ], def:{ 0:'北京', 1:'海淀', 2:'朝阳' } } }
            var i, prop, v, text, len, idList = [];
            var data = [];
            var valueObj = params.data['link'];
            for (i = 0; i <= index; ++i) {
                v = nlSelect.item(i).get('value');
                valueObj = valueObj[v];
                if (!valueObj) break;
            }
            if (isArray(valueObj)) {
                // 最末级为数组
                idList = valueObj;
            } else if (valueObj) {
                // 对象
                for (prop in valueObj) {
                    if (valueObj.hasOwnProperty(prop)) {
                        idList.push(prop);
                    }
                }
            }
            for (i = 0, len = idList.length; i < len; ++i) {
                text = params.data['def'][idList[i]];
                data.push([idList[i], text]);
            }
            return data;
        }
        /**
         * 层级情况处理
         * @method levelSituation
         * @param {Number} index 索引值
         * @return {Array} data 目标数据数组
         */
        function levelSituation(index) {
            var i, prop, v, text, dt,  data = [];
            // example: { 0:{ title:'北京', data: { 1:'海淀', 2:'朝阳' } } }
            dt = params.data;
            for (i = 0; i <= index; ++i) {
                v = nlSelect.item(i).get('value');
                dt = dt[v] && dt[v]['data'];
                if (!dt) break;
            }
            if (dt) {
                if (dt['data']){
                    dt = dt['data'];
                }
                for (prop in dt) {
                    if (dt.hasOwnProperty(prop)) {
                        text = dt[prop]['data'] ? dt[prop]['title'] : dt[prop];
                        data.push([prop, text]);
                    }
                }
            }
            return data;
        }
        /**
         * 独一无二的情况
         * @method uniqueSituation
         * @param {Number} index 索引值
         * @return {Array} data 目标数据数组
         */
        function uniqueSituation(index) {
            var i, v, dt, data = [];
            // example: [ [ { value:1, text:'北京' }, { value:2, text:'上海' } ], { 1:[ { value:3, text:'海淀' }, { value:4, text:'朝阳' } ] } ]
            dt = params.data[index + 1];
            v = nlSelect.item(index).get('value');
            if (v === NULL_VALUE) {
                // 选中空值，则清除所有下级select
                data = [];
            } else {
                dt = dt[v] || dt;
                for (i = 0; i < dt.length; ++i) {
                    data.push([dt[i]['value'], dt[i]['text']]);
                }
            }
            return data;
        }
        /**
         * 格式化数据
         * @method formatData
         * @param {Number} index 索引
         * @param {Array|Object} arr
         */
        // TODO 优化本方法
        function formatData(index, arr) {
            var i, len, prop, v, text, dt, tmp, data = [];
            if (isNumber(index)) {
                switch (params.format) {
                case 'default':
                    data = defaultSituation(index);
                    break;
                case 'hierarchy':
                    data = hierarchySituation(index);
                    break;
                case 'level':
                    data = levelSituation(index);
                    break;
                case 'unique':
                    data = uniqueSituation(index);
                    break;
                default:
                    break;
                }
            } else if (arr) {
                if (isArray(arr)) {
                    for (i = 0; i < arr.length; ++i) {
                        data.push([arr[i]['value'], arr[i]['text']]);
                    }
                } else if (isObject(arr)) {
                    for (i in arr) {
                        if (arr.hasOwnProperty(i)) {
                            data.push([i, arr[i]]);
                        }
                    }
                }
            }

            if (!isArray(data) && isObject(data)) {
                tmp = [];
                Y.Object.each(data, function(v, p) {
                    tmp.push([p, v]);
                });
                data = tmp;
            }

            return data;
        }

        /**
         * 异步请求
         * @method asyncRequest
         * @param {Number} index 索引
         */
        function asyncReq(index){
            var ndCurSel = nlSelect.item(index);
            var res, asyncAct = getAsyncActionUrl(index);
            showLoadingStatus(ndCurSel);
            var async = $IO(asyncAct, {
                method: 'GET',
                on: {
                    success:function(id, o) {
                        res = $Util.getEvalRes(o);
                        if (res.status) {
                            ndCurSel.set('disabled', false);
                            fillSelect(ndCurSel, formatData(null, res.data));
                            Y.Global.fire('Cascade.io.success', { data: res });
                        } else {
                            alert(res.msg);
                        }
                    },
                    failure:function(id, o) {
                        Y.Global.fire('Cascade.io.failure', { data: o });
                    }
                }
            });
        }

        /**
         * 替换url串中的'{level}'（level为select层级，从0开始）为对应select的value
         * @method getAsyncActionUrl
         * @param {Number} index 索引
         */
        function getAsyncActionUrl(index){
            var i, regex, selectvalue;
            var actionurl = params.action[index];
            for (i = 0; i < index; ++i) {
                regex = new RegExp("\\{" + i + "\\}");
                if (regex.test(actionurl)) {
                    selectvalue = nlSelect.item(i).get('value');
                    actionurl = actionurl.replace(regex, selectvalue);
                }
            }
            return actionurl;
        }

        /**
         * 数据填充select框
         * @method fillSelect
         * @param {Selector|HTMLElement|Node} ndSelect 目标select框
         * @param {Array} data 填充数据
         * @param {Number} selectedValue 选中项
         */
        function fillSelect(ndSelect, data, selectedValue) {
            var i, len = data.length, transdata;
            var idx = ndSelect.getData('index');
            selectedValue = selectedValue || NULL_VALUE;
            clearSelect(ndSelect);

            if (!data || (isArray(data) && data.length === 0)) {
                // data为空
                if (!ndSelect.get('options') || !ndSelect.get('options').size()) {
                    $N.getDOMNode(ndSelect).options.add(new Option(params.noChildrenText, NULL_VALUE));
                    ndSelect.set('selectedIndex', 0);
                }

                // 发送空值消息
                Y.Global.fire('Cascade.noChildren', { id:nd.get('id'), index:idx });
                return false;
            }
            for (i = 0; i < len; ++i) {
                // TODO 寻找更优写法 select.append(new Option(data[i][1], data[i][0])); not work in IE6
                $N.getDOMNode(ndSelect).options.add(new Option(data[i][1], data[i][0]));
                if (data[i][0].toString() === selectedValue.toString()) {
                    // 此处不宜用===，因为selectedValue在多数情况下为number类型，而option.value则为string类型
                    ndSelect.set('selectedIndex', ndSelect.get('options').size() - 1);
                }
            }
            return true;
        }

        /**
         * 清空select
         * @method clearSelect
         * @param {Selector|HTMLElement|Node}  ndSelect 目标select框
         */
        function clearSelect(ndSelect){
            var options = ndSelect.get('options');
            if (hasDefaultOption) {
                options.shift();
            }
            options.remove();
            return true;
        }

        /**
         * 显示异步请求加载状态
         * @method showLoadingStatus
         * @param {Selector|HTMLElement|Node} ndSelect  目标元素
         */
        function showLoadingStatus(ndSelect) {
            var options = $N.getDOMNode(ndSelect).options;
            ndSelect.set('disabled', true);
            options.add(new Option('loading...', NULL_VALUE));
            ndSelect.set('selectedIndex', options.length - 1);
        }

        /**
         * 根据value为select赋选中项
         * @method selectByValue
         * @param {Selector|HTMLElement|Node} ndSelect 目标元素
         * @param {Number} value 目标数值
         */
        function selectByValue(ndSelect, value){
            var i, len = ndSelect.get('options').size(), option;
            for (i = 0; i < len; ++i) {
                option = ndSelect.get('options').item(i);
                if (option.get('value').toString() ===  value.toString()) {
                    // 此处判断应用==，防止value为number时不匹配
                    ndSelect.set('selectedIndex', i);
                    return true;
                }
            }
            // 处理意外情况
            ndSelect.set('selectedIndex', 0);
            return false;
        }
    };

}, '1.0.0', {
    requires: [
        'mt-base',
        'io-base',
        'node'
    ]
});
 
