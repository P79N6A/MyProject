/**
 * @module w-autocomplete/p-cascade
 */
M.add('w-autocomplete/p-cascade', function(Y) {

    Y.namespace('mt.plugin.autocomplete');
    var trim = Y.Lang.trim;
    var $Macro = Y.mt.macro;
    var isArray = Y.Lang.isArray;
    /**
     * @class Cascade
     * @namespace mt.plugin.autocomplete
     * @constructor
     * @extends Plugin.Base
     */
    var Cascade = Y.Base.create('autocomplete-cascade', Y.Plugin.Base, [], {
        initializer: function() {
            var AutoCompleteList = Y.mt.widget.AutoCompleteList;
            this.get('host').insIndex = 0;
            this.autoIns = [this.get("host")];
            this._initIns();

            //在选中后执行cascade
            this.doAfter(AutoCompleteList.EVENT_NAME.select, this.cascadeNextIns);
        },
        /**
         * 初始化被级联的所有autocomplete实例
         * @method _initInstance
         * @private
         */
        _initIns: function() {
            var nds = this.get('nds');
            var AutoCompleteList = Y.mt.widget.AutoCompleteList;
            var host = this.get('host');
            var val = host.getValue();
            var _this = this;
            var params, ins;

            if(!nds || !isArray(nds)) return;
            //初始化每个autocomplete, 并保存到this.autoIns
            Y.Array.each(nds, function(item, index) {
                if(index === 0) return;

                params = _this.getUpdataParams(item, index);
                Y.Global.fire('AutoCompleteList:beforeInitIns', { params: params, index: index, nd: item });
                ins = new AutoCompleteList(params);
                ins.on(AutoCompleteList.EVENT_NAME.select, _this.cascadeNextIns, _this);
                ins.insIndex = index;
                _this.autoIns.push(ins);

                Y.Global.fire('AutoCompleteList:afterInitIns', { ins: _this.autoIns, index: index, nd: item });
            });
        },
        /**
         * @method _getQueryString
         * @private
         */
        _getQueryString: function(index) {
            var ins = null, vals = [], val;
            for(var i = 0; i < index; i++) {
                ins = this.autoIns[i];
                val = ins.getValue();
                vals.push(val.hiddenInput.getAttribute('name') + '=' + val.id);
            }
            return vals.join('&');
        },
        /**
         * 获得当前auto的数据
         * @method getObjList
         * @params {Number} index 实例所在的序号
         */
        getObjList: function(index) {
            var host = this.get('host');
            var previousIns = this.autoIns[index-1];
            var val = previousIns.getValue();
            var itemId, objList;

            if(!val) return {};

            objList = previousIns.get('objList');

            if(!isArray(objList)) return {};
            for( var i = 0; i < objList.length; i++) {
                var item = objList[i];
                if(item[host.get('listParam')].toString() === val.name.toString() && (item.hasOwnProperty("id") && item.id.toString() === val.id.toString())) {
                    return objList[i].cascade || {};
                }
            }
            return {};
        },
        /**
         * 获得渲染级联box的配置数据
         * @method getUpdataParams
         * @param {Node} item
         * @param {Number} index
         */
        getUpdataParams: function(item, index) {
            var params = { node: item };
            var actions = this.get('actions');
            var insItem = this.autoIns[index];

            if(actions && actions[index]) {
                //通过url获取数据
                params.dataUrl = actions[index] + '?' + this._getQueryString(index);
                params.action = params.dataUrl;
            } else {
                //直接获得初始化数据
                params.objList = this.getObjList(index);
                params.data = params.objList;
                if(insItem) insItem.set('objList', params.objList);
            }
            var callbacks = this.get("callback");
            if (callbacks) {
                params.callback = callbacks[index];
            }
            return params;
        },
        /**
         * 与下几个autocomplete实例级联
         * @method cascadeNextIns
         */
        cascadeNextIns: function(data) {
            var currentIns = data.ins;
            var index = currentIns.insIndex;
            var host = this.get('host');
            var insItem = null;
            for(var i = (index + 1); i < this.autoIns.length; i++) {
                //更新数据
                insItem = this.autoIns[i];
                insItem.updateData(this.getUpdataParams(null, i), this.get('keepOldValue'));

                //如果没有设置empty, 选中第一个数据
                if(this.get('selectFirstItem') === true)  {
                    this.selectFirstItem(insItem);
                }
            }
        },
        /**
         * 选中数据项中的第一个初始化input
         * @method selectFirstItem
         */
        selectFirstItem: function(currentIns) {
            var checkObj = this.getFirstObj(currentIns.get('objList'));
            currentIns.setValue(checkObj);
        },
        /**
         * 获取cascadeData中第一条数据
         * @method getFirstObj
         */
        getFirstObj: function(cascadeData) {
            for (var p in cascadeData) {
                if (cascadeData.hasOwnProperty(p)) {
                    return cascadeData[p];
                }
            }
            return {};
        }
    }, {
        NS: 'plugin-autocomplete-cascade',
        ATTRS: {
            /**
             * cascade的参数
             * @attribute cascade
             * @type Object
             */
            nds: {
                value: null
            },
            /**
             * 异步访问时的地址数组
             * @attribute actions
             * @type { Array }
             */
            actions: {
                value: null
            },
            /**
             * 异步访问时的地址数组
             * @attribute actions
             * @type { Array }
             */
            callback: {
                value: []
            },
            /**
             * 是否清空被级联对象后面的值
             * @attribute selectFirstItem
             * @type { Boolean| String }
             */
            selectFirstItem: {
                value: false
            },
            /**
             * 是否保持input中之前的value不变
             * @attribute keepOldValue
             * @type { Boolean }
             */
            keepOldValue: {
                value: true
            }
        }
    });

    Y.mt.plugin.autocomplete.Cascade = Cascade;


}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'w-autocomplete'
    ]
});
 
