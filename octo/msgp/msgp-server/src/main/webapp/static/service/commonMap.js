M.add('msgp-service/commonMap', function (Y) {
    Y.namespace('msgp.service').commonMap = commonMap;
    Y.namespace('msgp.service').setEnvText = setEnvTextByType;
    var obj = {};
    var LSKEY = 'msgp-commonmap';
    var online;

    function commonMap(callback) {
        var data = getLocalStorage();
        if (data) {
            Y.Lang.isFunction(callback) && callback(data);
            return;
        }
        var stack = new Y.Parallel();
        getStatus(stack);
        getEnv(stack);
        getRole(stack);
        stack.done(function () {
            setLocalStorage(obj);
            if (Y.Lang.isFunction(callback)) {
                callback(obj);
            }
        });
    }

    function getStatus(stack) {
        var url = '/common/status';
        Y.io(url, {
            method: 'get',
            on: {
                success: stack.add(function (id, o) {
                    var res = Y.JSON.parse(o.responseText);
                    if (res.isSuccess) {
                        obj.status = res.data;
                    }
                }),
                failure: function () {

                }
            }
        });
    }

    function getEnv(stack) {
        var url = '/common/env';
        Y.io(url, {
            method: 'get',
            on: {
                success: stack.add(function (id, o) {
                    var res = Y.JSON.parse(o.responseText);
                    if (res.isSuccess) {
                        obj.env = res.data;
                    }
                }),
                failure: function () {

                }
            }
        });
    }

    function getRole(stack) {
        var url = '/common/role';
        Y.io(url, {
            method: 'get',
            on: {
                success: stack.add(function (id, o) {
                    var res = Y.JSON.parse(o.responseText);
                    if (res.isSuccess) {
                        obj.role = res.data;
                    }
                }),
                failure: function () {

                }
            }
        });
    }

    function getLocalStorage() {
        var tmp = localStorage.getItem(LSKEY);
        if (!tmp) return false;
        var data = Y.JSON.parse(tmp);
        if ((+new Date()) - data.timestamp < 1 * 3600 * 1000) {
            return data.commonMap;
        } else {
            localStorage.removeItem(LSKEY);
            return false;
        }
    }

    function setLocalStorage(obj) {
        var data = {
            timestamp: +new Date(),
            commonMap: obj
        };
        localStorage.setItem(LSKEY, Y.JSON.stringify(data));
    }

    function setEnvTextByType(type) {
        var url = '/common/online';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var res = Y.JSON.parse(o.responseText);
                    if (res.isSuccess) {
                        if (res.data) {
                            Y.all('#' + type + ' a').item(0).set('text', 'prod');
                            Y.all('#' + type + ' a').item(1).set('text', 'staging');
                            Y.all('#' + type + ' a').item(2).set('text', 'test');
                            Y.msgp.utils.addTooltipWithContent("#" + type + " [value='test']", '线上TEST环境即将下线');
                        } else {
                            Y.all('#' + type + ' a').item(0).set('text', 'dev');
                            Y.all('#' + type + ' a').item(1).set('text', 'ppe');
                        //    Y.all('#' + type + ' a').item(2).set('text', 'test');
                            // value是字符串btn组
                            Y.msgp.utils.addTooltipWithContent("#" + type + " [value='prod']", '对应上海DEV(ALPHA)环境');
                            Y.msgp.utils.addTooltipWithContent("#" + type + " [value='stage']", '对应上海PPE环境');
                        //    Y.msgp.utils.addTooltipWithContent("#" + type + " [value='test']", '对应上海QA(BETA)环境');
                            // value是数字btn组
                            Y.msgp.utils.addTooltipWithContent("#" + type + " [value='3']", '对应上海DEV(ALPHA)环境');
                            Y.msgp.utils.addTooltipWithContent("#" + type + " [value='2']", '对应上海PPE环境');
                        //    Y.msgp.utils.addTooltipWithContent("#" + type + " [value='1']", '对应上海QA(BETA)环境');

                        }
                    }
                }
            }
        });
    };

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'parallel'
    ]
});
