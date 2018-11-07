/**
 * 该功能是基于YUI3的IO基础上, 为了更好的减少关注点，进行的一些封装
 * @module mt-io
 */
M.add('mt-io', function(Y) {

    Y.namespace('mt.io');

    var isArray = Y.Lang.isArray;
    /**
     * Y.mt.io类
     * @class io
     * @namespace mt
     */
    Y.mt.io = {
        /**
         * get请求, 参数意义同post
         * @method get
         * @param {Object} param 参数解释同post
         */
        get: function(url, params, onSuccess, onFailure, onOthers) {
            this._io(url, "GET", params, onSuccess, onFailure, onOthers);
        },
        /**
         * post请求
         * @method post
         * @param {String} url 发送请求的url地址
         * @param {Object} params 传递的参数, 格式如：
            <pre><code>
            { 
                postForm:{ 
                    id:formID, 
                    useDisabled: false //是否也序列化disabled的表单项值
                }, 
                param1:val1, 
                param2:val2
            }
            </code></pre>
         * @param {Function} onSuccess 返回数据中statusCode为0或者不存在时执行, 回调函数中的两个参数分别为data, msg
         * @param {Function} onFailure 请求出错或者返回数据中statusCode为真时执行
         * @param {Function} onOthers 如:{ start: funcStart, complete: funcComp, end: funcEnd }，分别为在start、complete、end状态时执行的自定义方法
         */
        post: function(url, params, onSuccess, onFailure, onOthers) {
            this._io(url, "POST", params, onSuccess, onFailure, onOthers);
        },
        /**
         * ajax请求
         * @method _io
         * @param {String} url 发送请求的url地址
         * @param {String} method 请求的类型post或者是get
         * @param {Object} params 传递的参数
         * @param {Function} onSuccess 成功时的回调函数
         * @param {Function} onFailure 失败时的回调函数
         * @param {Function} onOthers 完成时调用的函数
         * @private
         */
        _io: function(url, method, params, onSuccess, onFailure, onOthers) {
            var _this = this;
            var postConfig = {
                method: method,
                on: {
                    start: onOthers && onOthers.start,
                    complete: onOthers && onOthers.complete,
                    success: function(id, o) {
                        var resp = o.responseText;
                        _this._dealResponse(resp, onSuccess, onFailure);
                    },
                    failure: function(id, o) {
                        if(onFailure) {
                            onFailure(o.responseText);
                        }
                    },
                    end: onOthers && onOthers.end
                }
            };
            if(params) {
                var postForm = params.postForm;
                if(postForm && postForm['id'] ) {
                    postConfig.form = {id: postForm['id']};
                    if (postForm['useDisabled']) {
                        postConfig.form.useDisabled = postForm['useDisabled'];
                    }
                    delete params.postForm;
                }
                if(!Y.Object.isEmpty(params)) {
                    var urlData = Y.mt.util.toPostData(params);
                    if(method === "POST") {
                        postConfig.data = urlData;
                    } else {
                        url += "?" + urlData;
                    }
                }
            }
            Y.io(url, postConfig);
        },
        /**
         * 处理ajax请求返回的数据
         * @method _dealResponse
         * @param {String} resp 返回的字符串数据
         * @param {Function} onSuccess 成功时的回调函数
         * @param {Function} onFailure 失败时的回调函数
         * @private
         */
        _dealResponse:function(resp, onSuccess, onFailure) {
            var res;
            if(resp && resp !== "") {
                try {
                    res = Y.JSON.parse(resp);
                    //成功调用成功处理函数, 失败调用失败处理函数
                    //statusCode:0为成功
                    if(Y.mt.lang.isExist(res.statusCode) && res.statusCode === 0) {
                        var msg = res.msg || '';
                        onSuccess(res.data, msg);
                    } else if (res.statusCode) {
                        if(onFailure) {
                            onFailure(res.msg);
                        } else {
                            if(!Y.Object.isEmpty(res.msg)) {
                                for(var p in res.msg){
                                    if (res.msg.hasOwnProperty(p)) {
                                        Y.config.win.alert(res['msg'].p);
                                    }
                                }
                            }
                        }
                    } else {
                        onSuccess(res);
                    }
                } catch(e) {
                    Y.log(e.message, 'error');
                    if (e.stack) {
                        Y.log(e.stack, 'error');
                    }
                    if(onFailure) {
                        onFailure(resp);
                    }
                    throw e;
                }
            } else {
                res = {};
                onSuccess(res);
            }
        },
        /**
         *  给某个node节点添加loading效果
         * @method loading
         * @param {Selector|HTMLElement|Node} node 目标节点
         * @param {String} msg 添加loading效果的同时显示的文字信息
         * @param {Boolean} clearContent 是否需要清空原node节点的字节点
         * @param {Array} pos loading效果的位置信息, pos可为XY坐标数组，也可为Boolean，若为true，在节点选择器的所有子节点之后加入loading效果节点，false则在之前
         */
        loading: function(node, msg, clearContent, pos) {
            var nd = Y.one(node);
            if (!nd) return;
            if (clearContent) nd.setHTML('');

            var showMsg = msg || "";
            var ndLoad = Y.Node.create('<label class="loading">' + showMsg + '</label>');
            if (pos && isArray(pos)) {
                ndLoad.setXY(pos);
                if (nd.one('.loading')) nd.one('.loading').remove();
                nd.append(ndLoad);
                return;
            }
            if (pos) {
                nd.append(ndLoad);
            } else {
                nd.prepend(ndLoad);
            }
        },
        /**
         *  清除某个node节点的loading效果
         * @method clearLoading
         * @param {Selector|HTMLElement|Node} node 目标节点
         */
        clearLoading: function(node) {
            var container = Y.one(node);
            if (!container) return;
            var loading = container.one('.loading');
            if (loading) loading.hide();
        }
    };

}, '1.0.0', {
    requires: [
        'mt-base',
        'io-base',
        'io-form',
        'json',
        'node'
    ]
});
 
