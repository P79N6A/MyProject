M.add('msgp-config/rest-version0.0.2', function (Y) {

    /**
     * 获取 spaces list
     * get('/spaces', {}, successCallback, failureCallback);
     *
     * 新增 space
     * post('/spaces', {
     *   spaceNmae: spaceName
     * }, successCallback, failureCallback);
     *
     * 删除 space
     * delete('/spaces/:spaceName', {}, successCallback, failureCallback);
     *
     * 获取 node
     * get('/node/:nodeName', {}, successCallback, failureCallback);
     *
     * 新增 node
     * post('/node', {
     *   nodeName: nodeName
     * }, successCallback, failureCallback);
     *
     * 修改 node
     * put('/node/:nodeName', {
     *   nodeData: {
     *     key: value
     *   }
     * }, successCallback, failureCallback);
     *
     * 删除 node
     * delete('/node/:nodeName', {}, successCallback, failureCallback);
     *
     */
    var rest = {
        PREFIX: '',
        formatUrl: function(url) {
            var prefix = this.PREFIX;
            if (prefix.charAt(prefix.length - 1) !== '/') {
                prefix += '/';
            }
            if (url.charAt(0) === '/') {
                url = url.slice(1);
            }
            return prefix + url;
        },
        create: function create(prefix) {
            var newRest = Y.Object(this);
            newRest.PREFIX = prefix;
            return newRest;
        },

        newGet: function(url, params, successCallback, failureCallback) {
            var req = get(url, params, successCallback, failureCallback);
            return req;
        },

        get: function(url, params, successCallback, failureCallback) {
            if (typeof params === 'function') {
                failureCallback = successCallback;
                successCallback = params;
                params = {};
            }
            if (!failureCallback) {
                failureCallback = function(msg) {
                    Y.msgp.config.popup.alert(msg);
                };
            }
            var urlSplit = url.split('/');
            var spaceName, nodeName;
            var req;
            if (urlSplit[1] === 'spaces') {
                req = get('/config/spaces/list', params, successCallback, failureCallback);
            } else if (urlSplit[1] === 'node') {
                nodeName = urlSplit[2];
                spaceName = getSpaceName(nodeName);
                req = get('config/space/node/get', {
                    appkey:spaceName,nodeName: nodeName
                }, successCallback, failureCallback);
            } else {
                url = this.formatUrl(url);
                req = get(url, params, successCallback, failureCallback);
            }
            return req;
        },
        newPost: function(url, params, successCallback, failureCallback) {
            req = post(url, params, successCallback, failureCallback);
            return req;
        },
        post: function(url, params, successCallback, failureCallback) {
            if (typeof params === 'function') {
                failureCallback = successCallback;
                successCallback = params;
                params = {};
            }
            if (!failureCallback) {
                failureCallback = function(msg) {
                    Y.msgp.config.popup.alert(msg);
                };
            }
            var urlSplit = url.split('/');
            var spaceName;
            var nodeName;
            var req;
            if (urlSplit[1] === 'spaces') {
                params = {
                    name: params.spaceName
                };
                req = post('/config/spaces/add', params, successCallback, failureCallback);
            } else if (urlSplit[1] === 'node') {
                nodeName = params.nodeName;
                spaceName = getSpaceName(nodeName);
                req = post('/config/space/' + spaceName + '/node/add', {
                    nodeName: nodeName
                }, successCallback, failureCallback);
            } else {
                url = this.formatUrl(url);
                req = post(url, params, successCallback, failureCallback);
            }
            return req;
        },
        newPut: function(url, params, successCallback, failureCallback) {
            var req = post(url, params, successCallback, failureCallback)
            return req;
        },
        newDelete: function(url, params, successCallback, failureCallback) {
            var req = post(url, params, successCallback, failureCallback);
            return req;
        },
        delete: function(url, params, successCallback, failureCallback) {
            if (typeof params === 'function') {
                failureCallback = successCallback;
                successCallback = params;
                params = {};
            }
            if (!failureCallback) {
                failureCallback = function(msg) {
                    Y.msgp.config.popup.alert(msg);
                };
            }
            var urlSplit = url.split('/');
            var nodeName;
            var spaceName;
            var req;
            if (urlSplit[1] === 'spaces') {
                req = post('/config/spaces/delete', {
                    name: urlSplit[2]
                }, successCallback, failureCallback);
            } else if (urlSplit[1] === 'node') {
                nodeName = urlSplit[2];
                spaceName = getSpaceName(nodeName);
                req = post('/config/space/' + spaceName + '/node/delete', {
                    nodeName: nodeName
                }, successCallback, failureCallback);
            } else {
                url = this.formatUrl(url);
                req = post(url, params, successCallback, failureCallback);
            }
            return req;
        }
    };

    function getSpaceName(nodeName) {
        var firstDot = nodeName.indexOf('.');
        var spaceName;
        if (firstDot === -1) {
            spaceName = nodeName;
        } else {
            spaceName = nodeName.slice(0, firstDot);
        }
        return spaceName;
    }

    function get(url, params, successCallback, failureCallback) {
        return io('GET', url,{}, params, successCallback, failureCallback);
    }

    function post(url, params, successCallback, failureCallback) {
        return io('POST', url,{'Content-Type':"application/json;charset=UTF-8"}, params, successCallback, failureCallback);
    }

    function io(method, url,headers, params, successCallback, failureCallback) {
        var config = {
            method: method,
            headers : headers,
            data: params,
            on: {
                success: function(code, req) {
                    var response = Y.JSON.parse(req.response);
                    if (response.status === 200 || response.status === 'success') {
                        successCallback(response.data);
                    } else {
                        failureCallback(response.msg || '未知错误');
                    }
                },
                failure: function(code, req) {
                    if (req.status === 403) {
                        failureCallback('没有权限');
                    } else {
                        failureCallback('服务器出错了');
                    }
                }
            }
        };
        return Y.io(url, config);
    }

    Y.namespace('msgp.config').rest = rest;
    //config.rest = rest;

}, '', {
    requires: [ 'io', 'json']
});
