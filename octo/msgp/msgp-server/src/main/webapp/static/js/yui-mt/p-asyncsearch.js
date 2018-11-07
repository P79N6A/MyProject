/**
 * @module w-autocomplete/p-asyncsearch
 */
M.add('w-autocomplete/p-asyncsearch', function(Y) {

    Y.namespace('mt.plugin.autocomplete');

    /**
     * @class AyncSearch
     * @namespace mt.plugin.autocomplete
     * @constructor
     * @extends Plugin.Base
     */
    var AsyncSearch = Y.Base.create('autocomplete-ayncsearch', Y.Plugin.Base, [], {
        initializer: function() {
            var AutoCompleteList = Y.mt.widget.AutoCompleteList;
            var host = this.get('host');
            var ayncsearch = this.get('ayncsearch');
            this.set('ayncsearch', ayncsearch);
            host.set('noDataTips', '');

            //显示之前处理
            this.doAfter(AutoCompleteList.EVENT_NAME.beforeShow, this._beforeShow);
            this.doAfter(AutoCompleteList.EVENT_NAME.beforeUpdateData, this._displayList);
            this.doAfter(AutoCompleteList.EVENT_NAME.beforeShowResultBox, this._changeLoadingStatus);
        },
        /**
         * 改变loading的状态, 防止输入抖动
         * @method _changeLoadingStatus
         * @private
         */
        _changeLoadingStatus: function() {
            var host = this.get('host');
            //去掉loading效果
            if(!this.get('keepLoading')) {
                host.isLoading = false;
            }
        },
        /**
         * 显示之前的处理
         * @method _beforeShow
         * @private
         */
        _beforeShow: function(e) {
            var host = this.get('host');
            var ayncsearch = this.get('ayncsearch');
            e.show = false;
            // 若数据为空，先异步获取数据, 否则显示无数据
            host.updateData({ action: host.get('action') }, true);
        },
        /**
         * 显示搜索结果
         * @method _displayList
         * @private
         */
        _displayList: function() {
            var host = this.get('host');
            host.displayList();
        }
    }, {
        NS: 'plugin-autocomplete-ayncsearch',
        ATTRS: {
            /**
             * 搜索的地址
             * @attribute ayncsearch
             * @type { Boolean|String }
             */
            ayncsearch: {
                value: null
            },
            keepLoading: {
                value: false
            }
        }
    });

    Y.mt.plugin.autocomplete.AsyncSearch = AsyncSearch;


}, '1.0.0', {
    requires: [
        'mt-base',
        'base-build',
        'plugin',
        'w-autocomplete'
    ]
});
 
