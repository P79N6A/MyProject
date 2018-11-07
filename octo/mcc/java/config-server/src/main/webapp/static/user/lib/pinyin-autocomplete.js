YUI.add('config-user/lib/pinyin-autocomplete', function(Y) {

    var pinyin = Y.namespace('mt.vendor.Pinyin');

    function PinYinAC(config) {
        if(typeof config.showHeaderTip !== 'undefined') {
            this.showHeaderTip = config.showHeaderTip;
        }
    }

    PinYinAC.ATTRS = {
        resultFilters: { value: pinyinFilter },
        resultFormatter: { value: resultFormatter },
        resultTextLocator: { value: 'name' }
    };

    PinYinAC.prototype.showHeaderTip = false;
    PinYinAC.prototype.pinyinFilter = pinyinFilter;

    PinYinAC.prototype.initializer = function() {
        if(this.showHeaderTip) {
            this.after(this._renderHeaderTip, this, 'renderUI');
        }
        this.after(this.bindUIPY, this, 'bindUI');
    };

    PinYinAC.prototype.bindUIPY = function() {
        this.get('inputNode').on('focus', function() {
            this.sendRequest();
        }, this);
    };

    PinYinAC.prototype._renderHeaderTip = function() {
        this.get('contentBox')
            .prepend('<div class="pinyin-aclist-header-tip">输入 中文|拼音|首拼 搜索</div>');
    };

    PinYinAC.prototype.destructor = function() {
    };

    Y.namespace('mt.config.user').PinYinAC = PinYinAC;

    /**
     * @param query
     * @param results
     */
    function pinyinFilter(query, results) {
        if(query === '') {
            return results;
        }
        if (/^[a-z0-9]+$/i.test(query)) {
            return Y.Array.filter(results, function(result) {
                var login = result.raw.login;
                if (login.indexOf(query) !== -1) {
                    return true;
                }
                return false;
            });
        }
        return Y.Array.filter(results, function(result) {
            var name = result.raw.name;
            if(name.indexOf(query) !== -1) {
                return true;
            }
            var pys = toPinyin(name);
            var s = pys[0];
            var l = pys[1];
            if(l.indexOf(query) !== -1) {
                return true;
            }
            if(s.indexOf(query) !== -1) {
                return true;
            }
            return false;
        });

        function toPinyin(str) {
            var shortPY = '';
            var longPY = '';
            var i = 0;
            var py;
            while(i < str.length) {
                py = pinyin[str.slice(i, i + 1)];
                if(py) {
                    shortPY = shortPY + py[0];
                    longPY = longPY + py;
                }
                i++;
            }
            return [ shortPY, longPY ];
        }
    }

    function resultFormatter(query, results) {
        return Y.Array.map(results, function(result) {
            return result.raw.name + ' - ' + result.raw.login;
        });
    }

}, '', {
    requires: [
        'array-extras',
        'mt-pinyin-db'
    ]
});
