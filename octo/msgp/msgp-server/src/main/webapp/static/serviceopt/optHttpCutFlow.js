M.add('msgp-serviceopt/optHttpCutFlow', function (Y) {
    Y.namespace('msgp.serviceopt').optHttpCutFlow = detailHttpCutFlow;

    var httpCutFlowWrapper = Y.one('#wrap_httpCutFlow'),
        httpCutFlowTableWrapper = httpCutFlowWrapper.one('#httpCutFlow_table'),
        httpCutFlowTableTbody = httpCutFlowTableWrapper.one('tbody');

    var appkey,
        showOverlay,
        showContent,
        httpCutFlowList = [],
        curEnv = 3;

    var addHttpCutFlowDialog;
    
    function detailHttpCutFlow(key, func1, func2) {
        appkey = key;
        showOverlay = func1;
        showContent = func2;
        bindRefresh();
        bindEnvSelect();
        bindHttpEvent();
        getHttpCutFlowData();
    }

    function bindRefresh() {
        httpCutFlowWrapper.delegate('click', function () {
            getHttpCutFlowData();
        }, '#refreshHttpCutFlow');
    }

    function bindEnvSelect() {
        Y.msgp.service.setEnvText('httpCutFlow_env_select');
        httpCutFlowWrapper.delegate('click', function () {
            httpCutFlowWrapper.all('#httpCutFlow_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            curEnv = Number(this.getAttribute("value"));
            getHttpCutFlowData();
        }, "#httpCutFlow_env_select a")
    }

    function bindHttpEvent() {
        httpCutFlowWrapper.delegate('click', function () {
            addHttpCutFlowDialog = addHttpCutFlowDialog != null ? addHttpCutFlowDialog : new Y.mt.widget.CommonDialog({
                id: 'add_httpProviderCutFlow_dialog',
                title: '新增基本截流配置 ',
                width: 1024,
                btn: {
                    pass: doAddHttpCutFlow
                }
            });

            var micro = new Y.Template();
            var template = Y.one('#text_add_httpProviderCutFlow_form').get('value');
            var str = micro.render(template,{});
            addHttpCutFlowDialog.setContent(str);

            var addBody = addHttpCutFlowDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');

            var env = getEnvStr();
            var serverNames = [];
            var methodUrls = [];
            $.ajax({
                type:"get",
                data: {
                    env: env
                },
                url: '/service/httpCutFlow/' + appkey + '/' + env + '/serverNameList/get',
                async: false,
                success:function(ret){
                    serverNames = ret.data;
                }
            });

            if(serverNames.length > 0){
                addBody.one("#s_server_name").set('disabled', false);
                addBody.one('#s_server_name').empty();
                Y.each(serverNames, function (item) {
                    addBody.one('#s_server_name').append('<option value=' + item + '>' + item + '</option>');
                });
            }

            addBody.one('#s_server_name').on('change', function () {
                var serverName = this.get('value');
                addBody.one('#s_method_url').empty();
                $.ajax({
                    type:"get",
                    data: {
                        env: env
                    },
                    url: '/service/httpCutFlow/' + appkey + '/' + env + '/'+ serverName +'/serNameUrl/get',
                    async: false,
                    success:function(ret){
                        methodUrls = ret.data;
                        if(methodUrls.length > 0){
                            addBody.one("#s_method_url").set('disabled', false);
                            Y.each(methodUrls, function (item) {
                                addBody.one('#s_method_url').append('<option value=' + item + '>' + item + '</option>');
                            });
                        }else{
                            addBody.one("#s_method_url").set('disabled', true);
                            addBody.one('#s_method_url').append('<option value='+ '无数据' +  '>' + '无数据' + '</option>');
                        }
                    }
                });
            });
            if(serverNames.length > 0){
                //addBody.one('#s_server_name')._node.click();
                addBody.one("#s_server_name").simulate("change");
            }
            addHttpCutFlowDialog.show();

        }, '#addHttpCutFlow');

        //table中的删除事件
        httpCutFlowTableTbody.delegate('click', function () {
            //如何获取要删除的对象
            var el = this;
            var line = el.ancestor('tr');
            var info = line.getData('info');
            if (Y.Lang.isString(info)) {
                info = Y.JSON.parse(info);
            }
            var postData = {
                serverName: info.serverName,
                methodUrl: info.methodUrl,
                closurePercent: info.closurePercent,
                closureStatus: info.closureStatus
            };

            Y.io('/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/methodUrl/del', {
                method: 'post',
                data: Y.JSON.stringify(postData),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            getHttpCutFlowData();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }
                }
            });
        }, '.delHttpCutFlow');

        //table中的编辑手段
        httpCutFlowTableTbody.delegate('click', function () {
            //如何获取要删除的对象
            var el = this;
            var line = el.ancestor('tr');
            var info = line.getData('info');
            if (Y.Lang.isString(info)) {
                info = Y.JSON.parse(info);
            }

            addHttpCutFlowDialog =  new Y.mt.widget.CommonDialog({
                id: 'edit_httpProviderCutFlow_dialog',
                title: '截流配置修改 ',
                width: 1024,
                drag: function () {
                },
                btn: {
                    pass: function (btn, container) {
                        var postData = {
                            methodUrl: container.one("#s_method_url").get('value'),
                            serverName: container.one("#s_server_name").get('value'),
                            closurePercent: container.one("#s_closure_percent").get('value'),
                            closureStatus: container.one('#s_closure_status').get('value')
                        };
                        var url = '/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/methodUrl/update';
                        Y.io(url, {
                            method: 'post',
                            headers : {'Content-Type':"application/json;charset=UTF-8"},
                            data: Y.JSON.stringify(postData),
                            on: {
                                success: function (id, o) {
                                    var ret = Y.JSON.parse(o.responseText);
                                    if (ret.isSuccess) {
                                        getHttpCutFlowData();
                                    } else {
                                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                                        getHttpCutFlowData();
                                    }
                                },
                                failure: function () {
                                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                                    getHttpCutFlowData();
                                }
                            }
                        });
                    }
                }
            });

            var micro = new Y.Template();
            var template = Y.one('#text_add_httpProviderCutFlow_form').get('value');
            var str = micro.render(template,{});
            addHttpCutFlowDialog.setContent(str);

            var addBody = addHttpCutFlowDialog.getBody();

            addBody.one("#s_server_name" ).set("value", info.serverName);
            addBody.one("#s_server_name" ).set('disabled', true);

            addBody.one("#s_method_url" ).set("value", info.methodUrl);
            addBody.one("#s_method_url" ).set('disabled', true);

            addBody.one("#s_closure_percent" ).set("value", info.closurePercent);

            addBody.one("#s_closure_status" ).set("value", info.closureStatus);

            addHttpCutFlowDialog.show();
        }, '.editHttpCutFlow');

        httpCutFlowTableWrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');

            var line = el.ancestor('tr');
            var data = line.getData('info');
            if (Y.Lang.isString(data)) {
                data = Y.JSON.parse(data);
            }
            data.closureStatus = enabled.toString();

            var url = '/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/methodUrl/update';
            Y.io(url, {
                method: 'post',
                headers : {'Content-Type':"application/json;charset=UTF-8"},
                data: Y.JSON.stringify(data),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var enabledDesc = '';
                        if(enabled == 0){
                            enabledDesc = '启用';
                        }else{
                            enabledDesc = '禁用';
                        }
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', ret.msg || enabledDesc +'成功', 3);
                            getHttpCutFlowData();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || enabledDesc+ '失败', 3);
                            getHttpCutFlowData();
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || enabledDesc+ '失败', 3);
                        getHttpCutFlowData();
                    }
                }
            });
        }, '#http-one-enabled .btn');
    }

    //Http截流新增配置回调
    function doAddHttpCutFlow(btn, container) {
        var url = '/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/methodUrl/add';
        var data = {
            methodUrl: container.one("#s_method_url").get('value'),
            serverName: container.one("#s_server_name").get('value'),
            closurePercent: container.one("#s_closure_percent").get('value'),
            closureStatus: container.one('#s_closure_status').get('value')
        };

        function successCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('success', msg || "添加成功", 3);
            addHttpCutFlowDialog.close();
            getHttpCutFlowData();
        }

        function errorCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('error',msg || '添加失败',3);
        }

        Y.io(url, {
            method: 'post',
            data: Y.JSON.stringify(data),
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    successCallback && successCallback(ret.msg);
                },
                failure: function () {
                    errorCallback && errorCallback();
                }
            }
        });
        return true;
    }

    function fillHttpCutFlowTable(cutFlowData) {
        var micro = new Y.Template();
        var addCoreSavedTbodyTemplate = Y.one('#text-http-cut-flow-table-tbody').get('value');
        var html = micro.render(addCoreSavedTbodyTemplate, {data: cutFlowData});
        httpCutFlowTableWrapper.one("tbody").setHTML(html);
    }

    function getHttpCutFlowData() {
        showContent(httpCutFlowWrapper);
        var url = '/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/methodUrl/get';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        httpCutFlowList = ret.data;
                        fillHttpCutFlowTable(ret.data);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取截流数据失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取截流数据失败', 3);
                }
            }
        });
    }

    function getEnvStr() {
        switch (curEnv) {
            case 3:
                return "prod";
            case 2:
                return "stage";
            case 1:
                return "test";
        }
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit'
        //'msgp-service/commonMap'
    ]
});
