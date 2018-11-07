M.add('msgp-serviceopt/optThriftCutFlow-version0.1.0', function (Y) {
    Y.namespace('msgp.serviceopt').optThriftCutFlow = detailThriftCutFlow;
    var inited = false;
    var thriftCutFlowWrapper = Y.one('#wrap_thriftCutFlow'),
        thriftCutFlowTableWrapper = thriftCutFlowWrapper.one('#thriftCutFlow_table'),
        cutFlowTableWrapper = thriftCutFlowWrapper.one('#cutFlow_table'),
        thriftCutFlowTableTbody = thriftCutFlowTableWrapper.one('tbody'),
        cutFlowTableTbody = cutFlowTableWrapper.one('tbody');

    var appkey,
        showOverlay,
        showContent,
        _spannameList = [],
        consumerAppkeyList,
        curEnv = 3;

    var addThriftCutFlowDialog,
        setConsumerRatioDialog,
        warningDialog,
        addCutFlowDialog,
        setClientRatioDialog;

    var colspan = 10;

    var consumerQuotaConfigList = [];

    function detailThriftCutFlow(key, func1, func2) {
        if (!inited) {
            inited = true;
            appkey = key;
            showOverlay = func1;
            showContent = func2;
            bindRefresh();
            bindEnvSelect();
            bindThriftEvent();
        }
        //获取数据并显示数据
        getThriftCutFlowQuota()
        getCutFlowQuota()
    }

    function bindRefresh() {
        thriftCutFlowWrapper.delegate('click', function () {
            getThriftCutFlowQuota();
            getCutFlowQuota();
        }, '#refreshThriftCutFlow');
    }

    function bindEnvSelect() {
        Y.msgp.service.setEnvText('thriftCutFlow_env_select');
        thriftCutFlowWrapper.delegate('click', function () {
            thriftCutFlowWrapper.all('#thriftCutFlow_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            curEnv = Number(this.getAttribute("value"));
            getCutFlowQuota();
            getThriftCutFlowQuota();
            updateRhinoUrl();
        }, "#thriftCutFlow_env_select a")
    }

    function updateRhinoUrl() {
        var rhino_url = document.getElementById("rhino-url");
        var app_select = document.getElementById("apps_select").value;
        var online = $(".onlineState").html();
        if (online == "切换线上") {
            switch (curEnv) {
                case 3:
                    rhino_url.href = "http://rhino.inf.dev.sankuai.com/#/limit/" + app_select;
                    break;
                case 2:
                    rhino_url.href = "http://rhino.inf.ppe.sankuai.com/#/limit/" + app_select;
                    break;
                case 1:
                    rhino_url.href = "http://rhino.inf.test.sankuai.com/#/limit/" + app_select;
                    break;
            }
        } else {
            switch (curEnv) {
                case 3:
                    rhino_url.href = "https://rhino.sankuai.com/#/limit/" + app_select;
                    break;
                case 2:
                    rhino_url.href = "http://rhino.inf.st.sankuai.com/#/limit/" + app_select;
                    break;
            }
        }
    }

    $(".onlineState").click(function () {
        updateRhinoUrl();
    });


    function bindThriftEvent() {
        //获取服务方法名,成功后绑定添加策略
        var url = '/service/quota/' + appkey + '/spannames';
        var env = getEnvStr();
        $.ajax({
            type: "get",
            data: {
                env: env
            },
            url: url,
            async: false,
            success: function (ret) {  //function1()
                _spannameList = ret.data;
            }
        });
        //绑定增加按钮响应事件
        thriftCutFlowWrapper.delegate('click', function () {
            addThriftCutFlowDialog = addThriftCutFlowDialog ? addThriftCutFlowDialog : new Y.mt.widget.CommonDialog({
                id: 'add_providerCutFlow_dialog',
                title: '新增基本截流配置 ',
                width: 1024,
                btn: {
                    pass: doAddThriftCutFlow
                }
            });

            var micro = new Y.Template();
            var template = Y.one('#text_add_providerCutFlow_form').get('value');
            var str = micro.render(template, {});
            addThriftCutFlowDialog.setContent(str);
            addThriftCutFlowDialog.show();

            $("#s_method").autocomplete({
                source: _spannameList,
                minLength: 0
            });

            var addBody = addThriftCutFlowDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#s_method');

            addBody.delegate('click', function () {
                var spanname = Y.one("#s_method").get('value') || "all";
                getConsumerAppkeyList(spanname, function () {
                    var table = document.getElementById("consumer_table_add");
                    addTR(table, '#text_consumer_table', false);
                });
            }, '#addConsumerRatio');

            addBody.delegate('click', function () {
                this.ancestor('tr').remove();
            }, '#delConsumer');
        }, '#addThriftCutFlow');

        //绑定增加按钮响应事件
        thriftCutFlowWrapper.delegate('click', function () {
            addCutFlowDialog = addCutFlowDialog ? addCutFlowDialog : new Y.mt.widget.CommonDialog({
                id: 'add_cutFlow_dialog',
                title: '新增一键截流配置 ',
                width: 1024,
                btn: {
                    pass: doAddCutFlow
                }
            });

            var micro = new Y.Template();
            var template = Y.one('#add_cutFlow_form').get('value');
            var str = micro.render(template, {});
            addCutFlowDialog.setContent(str);
            addCutFlowDialog.show();

            $("#os_method").autocomplete({
                source: _spannameList,
                minLength: 0
            });

            var addBody = addCutFlowDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');
            addBody.delegate('change', function () {
                addBody.one('tbody').all('tr').remove();
            }, '#os_method');

            addBody.delegate('click', function () {
                var spanname = Y.one("#os_method").get('value') || "all";
                getConsumerAppkeyList(spanname, function () {
                    var table = document.getElementById("add_client_table");
                    addTR(table, '#client_table', true);
                });
            }, '#update_client_ratio');

            addBody.delegate('click', function () {
                this.ancestor('tr').remove();
            }, '#delete_client');
        }, '#addCutFlow');

        //删除
        thriftCutFlowWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            var quotaId = Y.JSON.parse(data).id;

            Y.io('/service/cutFlow/' + appkey + '/provider/del', {
                method: 'post',
                data: {
                    id: quotaId,
                    quotaType: 'simple'
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            getCutFlowQuota();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }
                }
            });
        }, '#deleteCutFlow');

        //删除
        thriftCutFlowWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            var quotaId = Y.JSON.parse(data).id;

            Y.io('/service/cutFlow/' + appkey + '/provider/del', {
                method: 'post',
                data: {
                    id: quotaId,
                    quotaType: 'strategy'
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            getThriftCutFlowQuota();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }
                }
            });
        }, '#delProviderCutFlow');

        //禁用启用
        cutFlowTableWrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');
            doChangedEnabled(el, enabled);
        }, '#open-enabled .btn');

        //禁用启用
        thriftCutFlowTableWrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');
            doThriftChangedEnabled(el, enabled);
        }, '#thrift-one-enabled .btn');

        //测试模式
        thriftCutFlowTableWrapper.delegate('click', function () {
            var el = this;
            if (el.hasClass('active')) return;
            var enabled = +el.getData('enabled');
            doChangedTestStatus(el, enabled);
        }, '#thrift-test-enabled .btn');


        thriftCutFlowWrapper.delegate('click', function () {
            getThriftCutFlowQuota();
            getCutFlowQuota();
        }, '#refresh_providerCutFlow');

        bindSetConsumerRatio();
    }

    //Thrif截流回调
    function doAddThriftCutFlow(btn, container) {
        var url = '/service/cutFlow/' + appkey + '/provider/add';
        var data = {
            appkey: appkey,
            id: 0,
            name: "",
            method: container.one("#s_method").get('value').replace(/\s*$/g,"") || "all",
            alarmStatus: 1,
            degradeStatus: +container.one('#s_degrade_status').get('value'),
            degradeend: 0,//+Y.one('#s_degradeend').get('value'),
            env: curEnv,
            qpsCapacity: 0,
            watchPeriod: 0,
            ctime: ~~(new Date() / 1000),
            utime: ~~(new Date() / 1000),
            hostQpsCapacity: parseInt(container.one("#s_host_qps_capacity").get('value')),
            clusterQpsCapacity: parseInt(container.one("#s_cluster_qps_capacity").get('value')),
            testStatus: +container.one('#s_test_status').get('value')
        };
        var tabledataCheck = container.one("#consumer_table_add");
        var listrowCheck = tabledataCheck.one('tbody').all("tr");
        var isC = isConflict(genThriftJsonData(listrowCheck));
        if (isC) {
            Y.msgp.utils.msgpHeaderTip('error', "消费者appkey不能重复配置", 3);
            return true;
        }

        var tabledata = container.one("#consumer_table_add");
        var listrow = tabledata.one('tbody').all("tr");
        var consumers = genThriftJsonData(listrow, 0);

        if (!containsOthers(consumers)) {
            othersWarning();
            return true;
        }

        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var res = addConsumer(listrow, ret.data);
                        if (res) {
                            addThriftCutFlowDialog.close();
                        }
                        getThriftCutFlowQuota();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                }
            }
        });
        return true;
    }

    //一键截流回调  先添加接口端的截流配置，成功后添加针对每个客户端的截流配置
    function doAddCutFlow(btn, container) {
        var url = '/service/cutFlow/' + appkey + '/provider/add';
        var data = {
            appkey: appkey,
            id: 0,
            name: "sp",
            method: container.one("#os_method").get('value').replace(/\s*$/g,""),
            alarmStatus: 1,
            degradeStatus: +container.one('#os_open').get('value'),
            degradeend: 0,
            env: curEnv,
            qpsCapacity: 0,
            watchPeriod: 0,
            ctime: ~~(new Date() / 1000),
            utime: ~~(new Date() / 1000),
            hostQpsCapacity: parseInt(container.one("#os_ratio").get('value')),
            clusterQpsCapacity: 0,
            testStatus: 0,
            ackStatus: 1
        };
        var tabledataCheck = container.one("#add_client_table");
        var listrowCheck = tabledataCheck.one('tbody').all("tr");
        var isC = isConflict(genJsonData(listrowCheck));
        if (isC) {
            Y.msgp.utils.msgpHeaderTip('error', "消费者appkey不能重复配置", 3);
            return true;
        }

        var tabledata = container.one("#add_client_table");
        var listrow = tabledata.one('tbody').all("tr");

        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var res = addClient(listrow, ret.data, data.hostQpsCapacity, data.degradeStatus);
                        if (res) {
                            addCutFlowDialog.close();
                        }
                        getCutFlowQuota();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 10);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                }
            }
        });
        return true;
    }

    function doThriftChangedEnabled(el, enabledd) {
        var url = '/service/cutFlow/' + appkey + '/provider/update';
        var line = el.ancestor('tr');
        var data = line.getData('info');
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        data.degradeStatus = enabledd;

        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getThriftCutFlowQuota();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        getThriftCutFlowQuota();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    getThriftCutFlowQuota();
                }
            }
        });
        return true;
    }

    // 截流的启用和禁用
    function doChangedEnabled(el, enabledd) {
        var url = '/service/cutFlow/' + appkey + '/provider/updateStatus/' + (enabledd == 0 ? "open" : "close");
        var line = el.ancestor('tr');
        var data = line.getData('info');  //设计一下结构
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        data.degradeStatus = enabledd;
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getCutFlowQuota();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        getCutFlowQuota();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    getCutFlowQuota();
                }
            }
        });
        return true;
    }

    function doChangedTestStatus(el, enabledd) {
        var url = '/service/cutFlow/' + appkey + '/provider/update';
        var line = el.ancestor('tr');
        var data = line.getData('info');
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        data.testStatus = enabledd;

        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getThriftCutFlowQuota();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        getThriftCutFlowQuota();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    getThriftCutFlowQuota();
                }
            }
        });
        return true;
    }

    //设置拦截策略
    function bindSetConsumerRatio() {
        setConsumerRatioDialog = setConsumerRatioDialog ? setConsumerRatioDialog : new Y.mt.widget.CommonDialog({
            id: 'add_consumerRatio_dialog',
            title: '设置消费者截流比例',
            width: 1024,
            btn: {
                pass: doAddConsumerRatio
            }
        });

        setClientRatioDialog = setClientRatioDialog ? setClientRatioDialog : new Y.mt.widget.CommonDialog({
            id: 'add_client_ratio_dialog',
            title: '设置消费者截流比例',
            width: 1024,
            btn: {
                pass: doAddClientRatio
            }
        });

        bindConsumerButtons();

        thriftCutFlowWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            data = Y.JSON.parse(data);

            var quotaId = data.id;
            var spanname = data.method;

            getConsumerAppkeyList(spanname);

            Y.io('/service/cutFlow/' + appkey + '/consumer/get', {
                method: 'get',
                data: {id: quotaId},
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var micro = new Y.Template();
                            var template = Y.one('#text_consumer_dialog').get('value');
                            var str = micro.render(template, {data: ret.data, quotaId: quotaId, parseData: data});
                            setConsumerRatioDialog.setContent(str);
                            setConsumerRatioDialog.show();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        getThriftCutFlowQuota();
                    }
                }
            });
        }, '#setConsumerRatio');

        thriftCutFlowWrapper.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var data = line.getData('info');
            data = Y.JSON.parse(data);

            var quotaId = data.id;
            var spanname = data.method;

            getConsumerAppkeyList(spanname);
            var micro = new Y.Template();
            var template = Y.one('#update_client_textarea').get('value');
            var str = micro.render(template, {
                data: consumerQuotaConfigList[quotaId + "_"].consumers,
                quotaId: quotaId,
                parseData: data
            });
            setClientRatioDialog.setContent(str);
            setClientRatioDialog.show();

        }, '#setClientRatio');
    }

    function doAddClientRatio(btn, container) {
        var url = '/service/cutFlow/' + appkey + '/provider/updateSimple';
        var data = {
            appkey: appkey,
            id: parseInt(container.one("#os_id").get('value')),
            name: "sp",
            method: container.one("#os_method").get('value'),
            alarmStatus: 1,
            degradeStatus: +container.one('#os_degrade_status').get('value'),
            degradeend: 0,
            env: curEnv,
            qpsCapacity: 0,
            watchPeriod: 0,
            ctime: ~~(new Date() / 1000),
            utime: ~~(new Date() / 1000),
            hostQpsCapacity: parseInt(container.one("#os_ratio").get('value')),
            clusterQpsCapacity: 0,
            testStatus: 0,
            consumers: []
        };

        var table = setClientRatioDialog.getBody().one("#update_client_table");
        var tbody = table.one('tbody');
        var listRow = tbody.all("tr");

        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var appQuotaId = parseInt(table.one("thead").get('id'));
                        var res = addClient(listRow, appQuotaId, data.hostQpsCapacity, data.degradeStatus);
                        if (res) {
                            setClientRatioDialog.close();
                            getCutFlowQuota();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        getCutFlowQuota();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    getCutFlowQuota();
                }
            }
        });
        return true;
    }

    function delThirftTR(el) {
        var line = el.ancestor('tr');
        var table = document.getElementById("consumer_table");
        var tbody = table.tBodies[0];
        var tr = document.getElementById(line._stateProxy.id);
        tr.remove();
        if (line.getData('info') != null) {
            var consumerId = Y.JSON.parse(line.getData('info')).id;
            var url = '/service/cutFlow/' + appkey + '/consumer/del';

            Y.io(url, {
                method: 'post',
                data: {id: consumerId},
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    }
                }
            });
        }
    }

    function delTR(el) {
        var line = el.ancestor('tr');
        var tr = document.getElementById(line._stateProxy.id);
        tr.remove();
        var quotaId = setClientRatioDialog.getBody().one("#os_id").get('value');
        var degradeStatus = setClientRatioDialog.getBody().one("#os_degrade_status").get('value');

        if (line.getData('info') != null) {
            var consumerAppkey = Y.JSON.parse(line.getData('info')).consumerAppkey;
            var url = '/service/cutFlow/' + appkey + '/consumer/delSimple';
            Y.io(url, {
                method: 'post',
                data: {
                    consumerAppkey: consumerAppkey,
                    quotaId: quotaId,
                    degradeStatus: degradeStatus
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    }
                }
            });
        }
    }

    function bindConsumerButtons() {
        var dbody = setConsumerRatioDialog.getBody();

        dbody.delegate('click', function () {
            var table = document.getElementById("consumer_table");
            addTR(table, '#text_consumer_table', false);
        }, '#addConsumerRatio');

        dbody.delegate('click', function () {
            delThirftTR(this);
        }, '#delConsumer');

        dbody = setClientRatioDialog.getBody();

        dbody.delegate('click', function () {
            var table = document.getElementById("update_client_table");
            addTR(table, '#client_table', true);
        }, '#update_client_ratio');

        dbody.delegate('click', function () {
            delTR(this);
        }, '#delete_client');
    }

    /**
     * 添加规则：
     *  1：如果选择all了，就不能再选择别的了
     *  2：如果选择了其他不能选别的了
     *  3: 不能重复添加
     * 校验规则：
     *  获取之前填写的appkey
     */
    function addTR(table, table_id, flag) {
        //获取之前填写的客户appkey
        var consumerData = canAddConsumer(consumerAppkeyList, table.id, flag);
        if (!consumerData.canAdd) {
            Y.msgp.utils.msgpHeaderTip('error', "消费者appkey不能all与其他appkey同时存在", 3);
            return;
        }
        var rowid = table.rows.length - 1;
        var tbody = table.tBodies[0];
        var tr = document.createElement("tr");
        tr.id = "consumer_table_tr_" + rowid;
        var micro = new Y.Template();
        var template = Y.one(table_id).get('value');
        var str = micro.render(template, {data: consumerData.appkeys});
        tr.innerHTML = str;
        if (tbody == null) {
            table.appendChild(tr);
        }
        else tbody.appendChild(tr);
    }

    /**
     * 是否可以添加appkey
     *  1：包含all时候不能添加了
     *  2: 删除appkeyList里的元素防止重复添加
     */
    function canAddConsumer(appkeyData, tableId, flag) {
        var appkeyList = [];
        for (var i = 0, len = appkeyData.length; i < len; i++) {
            appkeyList.push(appkeyData[i]);
        }
        var table_dialog = Y.one('#' + tableId);
        var appkey_list = table_dialog.all("#appkey");
        var appkeys = [];
        appkey_list.each(function (item) {
            var appkey = item.get('value') || item.getAttribute("value");
            appkeys.push(appkey)
        });
        if (appkeys.length > 0) {
            appkeyList.remove("all")
        }
        appkeys.forEach(function (item) {
            appkeyList.remove(item)
        });

        if (flag && appkeyList.indexOf("others") > -1)
            appkeyList.remove("others");

        //包含all时候不能添加了
        if (appkeys.indexOf("all") > -1) {
            return {canAdd: false, appkeys: appkeyList}
        } else {
            return {canAdd: true, appkeys: appkeyList}
        }
    }

    function isConflict(consumers) {
        var curConsumerAppkeys = [];
        consumers.forEach(function (item) {
            curConsumerAppkeys.push(item.consumerAppkey);
        });
        curConsumerAppkeys.distinct();
        return curConsumerAppkeys.length != consumers.length;
    }

    function containsOthers(consumers) {
        var ret = false;
        consumers.forEach(function (item) {
            console.log(item.consumerAppkey);
            if (item.consumerAppkey == "others") {
                ret = true;
            }
        });
        return ret;
    }

    function othersWarning() {
        warningDialog = warningDialog ? warningDialog : new Y.mt.widget.CommonDialog({
            id: 'warning_dialog',
            title: 'others配额提醒',
            width: 400,
            btn: {}
        });
        warningDialog.setContent("<p>截流策略中必须配置客户端appkey others！" +
            "<p>若没有配置单个appkey，others配额可以略大于集群QPS容量；" +
            "<p>若配置了其他appkey，others可以略大于剩余的配额；" +
            "<p>当然others配额也可以较小。");
        warningDialog.show();
    }

    function genThriftJsonData(listrow, appQuotaId) {
        var consumers = [];
        listrow.each(function (item) {
            var row = {};
            row.consumerAppkey = item.one("#appkey").get('value') || item.one("#appkey").get('text');
            row.hostQuota = 0;
            row.clusterQuota = parseInt(item.one("#cluster_quota").get('value') || item.one("#cluster_quota").get('text'));
            row.strategy = parseInt(item.one("#strategy").get('value') || item.one("#strategy").getAttribute('value'));
            row.appQuotaId = appQuotaId;
            row.id = 0;
            if (row.consumerAppkey != "") {
                consumers.push(row);
            }
        });
        return consumers;
    }

    function genJsonData(listrow, appQuotaId) {
        var consumers = [];
        listrow.each(function (item) {
            var row = {};
            row.consumerAppkey = item.one("#appkey").get('value') || item.one("#appkey").get('text');
            row.hostQuota = parseInt(item.one("#host_quota").get('value') || item.one("#host_quota").get('text'));
            row.clusterQuota = 0;
            row.strategy = 0;
            row.appQuotaId = appQuotaId;
            row.id = 0;
            if (row.consumerAppkey != "") {
                consumers.push(row);
            }
        });
        return consumers;
    }

    function genConsumerQuotaJsonData(listrow, appQuotaId, capacity) {
        var consumers = [];
        var consumerKeys = [];
        listrow.each(function (item) {
            var row = {};
            row.consumerAppkey = item.one("#appkey").get('value') || item.one("#appkey").get('text');
            if (row.consumerAppkey == 'others' && typeof(capacity) != "undefined")
                row.hostQuota = capacity;
            else
                row.hostQuota = parseInt(item.one("#host_quota").get('value') || item.one("#host_quota").get('text'));
            row.clusterQuota = 0;
            row.strategy = 0;
            row.appQuotaId = appQuotaId;
            row.id = 0;
            if (row.consumerAppkey != "") {
                consumers.push(row);
                consumerKeys.push(row.consumerAppkey);
            }
        });

        if (!consumerKeys.contains('others')) {
            consumers.push({
                consumerAppkey: 'others',
                id: 0,
                appQuotaId: appQuotaId,
                hostQuota: capacity,
                clusterQuota: 0,
                strategy: 0
            })
        }
        return consumers;
    }

    function addConsumer(listrow, appQuotaId) {
        var url = '/service/cutFlow/' + appkey + '/consumer/add';
        var consumers = genThriftJsonData(listrow, appQuotaId);
        if (isConflict(consumers)) {
            Y.msgp.utils.msgpHeaderTip('error', "消费者appkey不能重复配置", 3);
            return true;
        }

        if (consumers.length == 0) {
            setConsumerRatioDialog.close();
            getThriftCutFlowQuota();
            return true;
        }

        var res = false;
        Y.io(url, {
            method: 'post',
            sync: true,
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(consumers),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
                        res = true;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
                }
            }
        });
        return res;
    }

    function addClient(listrow, appQuotaId, capacity, degradeStatus) {
        var url = '/service/cutFlow/' + appkey + '/consumer/addSimple/' + (degradeStatus == 0 ? true : false);
        var consumers = genConsumerQuotaJsonData(listrow, appQuotaId, capacity);
        if (isConflict(consumers)) {
            Y.msgp.utils.msgpHeaderTip('error', "消费者appkey不能重复配置", 3);
            return true;
        }

        if (consumers.length == 0) {
            setClientRatioDialog.close();
            getCutFlowQuota();
            return true;
        }

        var res = false;
        Y.io(url, {
            method: 'post',
            sync: true,
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(consumers),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '操作成功', 3);
                        res = true;
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '操作失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '操作失败', 3);
                }
            }
        });
        return res;
    }

    function doAddConsumerRatio(btn, container) {
        var url = '/service/cutFlow/' + appkey + '/provider/update';
        var data = {
            appkey: appkey,
            id: parseInt(container.one("#s_id").get('value')),
            name: "",
            method: container.one("#s_method").get('value'),
            alarmStatus: 1,
            degradeStatus: +container.one('#s_degrade_status').get('value'),
            degradeend: 0,
            env: curEnv,
            qpsCapacity: 0,
            watchPeriod: 0,
            ctime: ~~(new Date() / 1000),
            utime: ~~(new Date() / 1000),
            hostQpsCapacity: parseInt(container.one("#s_host_qps_capacity").get('value')),
            clusterQpsCapacity: parseInt(container.one("#s_cluster_qps_capacity").get('value')),
            testStatus: +Y.one('#s_test_status').get('value')
        };

        var tabledata = setConsumerRatioDialog.getBody().one("#consumer_table");
        var tbodyData = tabledata.one('tbody');
        var listrow = tbodyData.all("tr");
        var consumers = genThriftJsonData(listrow, 0);

        if (!containsOthers(consumers)) {
            othersWarning();
            return true;
        }

        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var appQuotaId = parseInt(tabledata.one("thead").get('id'));
                        var res = addConsumer(listrow, appQuotaId);
                        if (res) {
                            setConsumerRatioDialog.close();
                            getThriftCutFlowQuota();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
                        getThriftCutFlowQuota();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    getThriftCutFlowQuota();
                }
            }
        });
        return true;
    }

    function getThriftCutFlowQuota() {
        showContent(thriftCutFlowWrapper);
        var env = Y.one('#thriftCutFlow_env_select a.btn-primary').getAttribute('value');
        var url = '/service/cutFlow/' + appkey + '/provider/get';
        Y.io(url, {
            method: 'get',
            data: {
                env: env,
                name: ''
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillThriftCutFlowTable(data);
                        } else if (data.length === 0) {
                            emptyOrError(false, thriftCutFlowTableTbody);
                        }
                    } else {
                        emptyOrError(true, thriftCutFlowTableTbody);
                    }
                },
                failure: function () {
                    emptyOrError(true, thriftCutFlowTableTbody);
                }
            }
        });
    }

    function getCutFlowQuota() {
        showContent(thriftCutFlowWrapper);
        var env = Y.one('#thriftCutFlow_env_select a.btn-primary').getAttribute('value');
        var url = '/service/cutFlow/' + appkey + '/provider/getSimple';
        Y.io(url, {
            method: 'get',
            data: {
                env: env,
                name: "sp"
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            var micro = new Y.Template();
                            var template = Y.one('#template').get('value');
                            var str = micro.render(template, {data: data});
                            cutFlowTableTbody.setHTML(str);
                            showContent(thriftCutFlowWrapper);
                            fillConsumerQuotaList(data);
                        } else if (data.length === 0) {
                            emptyOrError(false, cutFlowTableTbody);
                        }
                    } else {
                        emptyOrError(true, cutFlowTableTbody);
                    }
                },
                failure: function () {
                    emptyOrError(true, cutFlowTableTbody);
                }
            }
        });
    }

    function fillThriftCutFlowTable(arr) {
        var micro = new Y.Template();
        var template = Y.one('#text_template').get('value');
        var str = micro.render(template, {data: arr});
        thriftCutFlowTableTbody.setHTML(str);
        showContent(thriftCutFlowWrapper);
    }

    function getConsumerAppkeyList(var1, callback) {
        var url = '/service/quota/' + appkey + '/consumerAppkeys';
        var env = getEnvStr();
        Y.io(url, {
            method: 'get',
            data: {
                spanname: var1,
                env: env
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    consumerAppkeyList = ret.data;
                    if (typeof(callback) == "function")
                        callback();
                },
                failure: function () {
                    console.error("Get consumerAppkeyList error! ")
                }
            }
        });
    }

    function fillConsumerQuotaList(data) {
        consumerQuotaConfigList = [];
        for (var index = 0; index < data.length; index++)
            consumerQuotaConfigList[data[index].id + "_"] = data[index];
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

    function emptyOrError(isError, body) {
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        body.setHTML(html);
        showContent(thriftCutFlowWrapper);
    }

    Array.prototype.remove = function (val) {
        var index = this.indexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };

    Array.prototype.distinct = function () {
        if (this.length <= 0) return;
        this.sort();
        var re = [this[0]];
        for (var i = 1; i < this.length; i++) {
            if (this[i] !== re[re.length - 1]) {
                re.push(this[i]);
            }
        }
        this.length = re.length;
        for (var i = 0; i < re.length; i++) {
            this[i] = re[i];
        }
    }

    Array.prototype.contains = function (obj) {
        var i = this.length;
        while (i--) {
            if (this[i] === obj) {
                return true;
            }
        }
        return false;
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
