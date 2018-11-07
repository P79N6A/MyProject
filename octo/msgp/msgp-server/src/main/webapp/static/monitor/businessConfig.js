/* jshint indent : false */
M.add('msgp-monitor/businessConfig', function (Y) {
    Y.namespace('msgp.monitor').businessConfig = businessConfig;
    var screenId;
    var twrap = Y.one('#triggers_wrap');
    var tbody = twrap.one('tbody');
    var addTriggerDialog;
    var dialogEdit;
    var dialogDelete;
    var dialogSubscribe;

    function businessConfig(key) {
        screenId = key;
        topLinkEvent();
        initEvent();
        getTriggerItems();
    }

    function topLinkEvent() {
        var url = '/monitor/business/triggers';
        Y.io(url, {
            method: 'get',
            sync: true,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var obj = [];
                        for (var i = 0, l = data.length; i < l; i++) {
                            obj.push({id: data[i].id, name: data[i].title + "(" + data[i].metric + ")"});
                        }
                        if (obj.length && Y.one('#business_kpi_select') != null) {
                            AutoCompleteList(obj);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                }
            }
        });
    }

    function AutoCompleteList(obj) {
        metricAutoComplete = new Y.mt.widget.AutoCompleteList({
            id: "business_kpi_select_auto",
            node: Y.one("#business_kpi_select"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            more: "",
            callback: function (data) {
                var akey = data.id;
                location.search = refreshSearch(akey);
            }
        });
        Y.one("#business_kpi_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "400px");
        Y.one("#business_kpi_select_auto").one(".widget-autocomplete-tip").setHTML("输入服务名搜索或向下滚动选择");
        Y.one("#business_kpi_select_auto").one(".widget-autocomplete-menu-operator").remove();
    }

    function refreshSearch(newKey) {
        var search = location.search.slice(1);
        var arr = search.split('&');
        for (var i = 0, l = arr.length; i < l; i++) {
            if (arr[i].indexOf('screenId=') === 0) {
                arr[i] = 'screenId=' + newKey;
                break;
            } else if (i === l - 1) {
                arr.push('screenId=' + newKey);
            }
        }
        return arr.join('&');
    }

    function showOverlay() {
        twrap.one('.content-body').hide();
        twrap.one('.content-overlay').show();
    }

    function showContent() {
        twrap.one('.content-overlay').hide();
        twrap.one('.content-body').show();
    }

    function emptyOrError(isError) {
        var colspan = 6;
        var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
        tbody.setHTML(html);
        showContent();
    }

    function initVerify() {
        durationInput = Y.msgp.utils.check.init(Y.one('#duration'), {
            type: 'custom',
            customRegExp: /^\d{1,}$/,
            warnMsg: '必须是正整数',
            warnElement: Y.one('#duration').next('')
        });

        thresholdInput = Y.msgp.utils.check.init(Y.one('#threshold'), {
            type: 'custom',
            customRegExp: /^\d{1,}$/,
            warnMsg: '必须是正整数',
            warnElement: Y.one('#threshold').next('')
        });
    }

    function initEvent() {
        var addButton = Y.one('#add_trigger');
        addButton.on('click', addTriggerEvent);
        //buildEditEvent();
        buildDeleteEvent();
        //buildAppSubScribeEvent();
        buildTriggerSubscribeEvent();
    }

    function buildEditEvent() {
        tbody.delegate('click', function () {
            var tr = this.ancestor('tr');
            dialogEdit = dialogEdit ? dialogEdit : new Y.mt.widget.CommonDialog({
                title: '编辑阈值',
                width: 550,
                btn: {
                    pass: refreshLineData
                }
            });
            dialogEdit.setContent(dialogEditContent);
            //把要修改的tr绑定到dialog上
            dialogEdit.tr = tr;
            dialogEdit.show();
            var dInput = Y.one('#dThreshold');
            dInput.set('value', tr.getData('threshold'));
            dThreshold = Y.msgp.utils.check.init(dInput, {
                type: 'custom',
                customRegExp: /^\d{1,}$/,
                warnMsg: '必须是正整数',
                warnElement: dInput.next()
            });
        }, '.trigger-edit');
    }

    function buildDeleteEvent() {
        tbody.delegate('click', function () {
            var tr = this.ancestor('tr');
            dialogDelete = dialogDelete ? dialogDelete : new Y.mt.widget.CommonDialog({
                title: '删除监控项',
                width: 400,
                content: '你确定要删除这条监控项吗？',
                btn: {
                    pass: deleteLineData
                }
            });
            //把要删除的tr绑定到dialog上
            dialogDelete.tr = tr;
            dialogDelete.show();
        }, '.trigger-delete');
    }

    function buildAppSubScribeEvent() {
        Y.one('#subscribe_all_alarm').on('click', subs);
    }

    function subs() {
        dialogSubscribe = dialogSubscribe ? dialogSubscribe : new Y.mt.widget.CommonDialog({
            title: '批量修改订阅',
            width: 400,
            drag: function () {
            },
            btn: {
                pass: function () {
                    var xm = Y.one("#xm").get("checked")
                    var sms = Y.one("#sms").get("checked")
                    var email = Y.one("#email").get("checked")
                    var params = {appkey: appkey, xm: xm, sms: sms, email: email}
                    console.log(params);
                    var url = '/monitor/' + appkey + '/trigger/subscribe/batch';
                    Y.io(url, {
                        method: 'post',
                        headers : {'Content-Type':"application/json;charset=UTF-8"},
                        data: Y.JSON.stringify(params),
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (ret.isSuccess) {
                                    Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                                    getTriggerItems(1);
                                } else {
                                    Y.msgp.utils.msgpHeaderTip('error', '修改失败' || ret.msg, 3);
                                }
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                            }
                        }
                    });
                }
            }
        });
        var micro = new Y.Template();
        var str = micro.render(dialogSubscribeContent);
        dialogSubscribe.setContent(str);
        dialogSubscribe.show();
    }

    function buildTriggerSubscribeEvent() {
        tbody.delegate('click', function () {
            var tr = this.ancestor('tr');
            doSubscribe(this, "xm");
        }, '.trigger-subscribe-xm');
        tbody.delegate('click', function () {
            var tr = this.ancestor('tr');
            doSubscribe(this, "sms");
        }, '.trigger-subscribe-sms');
        tbody.delegate('click', function () {
            var tr = this.ancestor('tr');
            doSubscribe(this, "email");
        }, '.trigger-subscribe-email');
    }

    function doSubscribe(node, mode) {
        var tr = node.ancestor('tr');
        var businessMonitorId = tr.getData("id");
        var status = node.getData("status");
        console.log(status);
        var newStatus = (status == undefined || status == null || status == '' || status == 1) ? 0 : 1;
        var params = {businessMonitorId: +businessMonitorId, mode: mode, newStatus: newStatus};
        console.log(params);
        var url = '/monitor/business/subscribe';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(params),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getTriggerItems(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                }
            }
        });
    }

    function addTriggerEvent() {
        addTriggerDialog = addTriggerDialog ? addTriggerDialog : initAddTriggerDialog();
        var micro = new Y.Template();
        var str = micro.render(addTemplate);
        addTriggerDialog.setContent(str);
        addTriggerDialog.show();
        initVerify();
    }

    function initAddTriggerDialog() {
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'add_trigger_dialog',
            title: '增加监控项',
            width: 640,
            refresh: 1,
            btn: {
                pass: doAddTrigger
            }
        });
        return dialog;
    }

    function refreshLineData() {
        if (!dThreshold.node.getData('status')) {
            dThreshold.showMsg();
            return true;
        }
        var tr = dialogEdit.tr;
        var data = {
            "side": tr.getData('side'),
            "spanname": tr.getData('spanname'),
            "item": tr.getData('item'),
            "itemDesc": tr.getData('itemdesc'),
            "function": tr.getData('function'),
            "functionDesc": tr.getData('functiondesc'),
            "threshold": +dThreshold.node.get('value')
        };
        var url = '/monitor/' + appkey + '/trigger';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getTriggerItems(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                }
            }
        });
    }

    function deleteLineData() {
        var tr = dialogDelete.tr;
        var url = '/monitor/business/config';
        Y.io(url, {
            method: 'delete',
            data: {
                "id": tr.getData('id')
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        tr.transition({
                            opacity: 0,
                            duration: 0.6
                        }, function () {
                            tr.remove();
                        });
                        delete dialogDelete.tr;
                        Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                }
            }
        });
    }

    function doAddTrigger() {
        if (!durationInput.node.getData('status') || !thresholdInput.node.getData('status')) {
            durationInput.showMsg();
            thresholdInput.showMsg();
            return true;
        }

        var url = '/monitor/business/config';
        var side = Y.one('#triggerSide').getAttribute("data-side");

        var itemSelect = Y.one('#triggerItems');
        var strategy = +itemSelect.get('value');
        var desc = itemSelect.get('options').item(itemSelect.get('selectedIndex')).get('text');
        var threshold = +Y.one('#threshold').get('value');
        var duration = +Y.one('#duration').get('value');

        var screen = metricAutoComplete.getValue();
        var screenId = screen.id;


        var data = {
            "screenId": screenId,
            "strategy": strategy,
            "desc": desc,
            "threshold": threshold,
            "duration": duration
        };

        Y.io(url, {
            method: 'post',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '添加成功', 3);
                        getTriggerItems(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '添加失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
                }
            }
        });
    }

    function doChangeTrigger(node, oldValue, newValue) {
        var url = '/monitor/business/config';
        var line = node.ancestor('tr');
        var screenId = line.getAttribute("data-screenid");
        var strategy = line.getAttribute("data-strategy");
        var desc = line.one(".desc").get("text");
        var threshold = line.one(".threshold").get("text");
        var duration = line.one(".duration").get("text");

        var data = {
            "screenId": screenId,
            "strategy": strategy,
            "desc": desc,
            "threshold": threshold,
            "duration": duration
        };

        Y.io(url, {
            method: 'post',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
                        getTriggerItems(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '修改失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                }
            }
        });
    }

    function getSpannames(side) {
        var url = '/monitor/' + appkey + '/spannames';
        var spans = ['all'];
        Y.io(url, {
            sync: true,
            method: 'get',
            data: {
                side: side
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            spans = data;
                        }
                    }
                }
            }
        });
        return spans;
    }

    function getItems() {
        var url = '/monitor/' + appkey + '/trigger/items';
        var items = [];
        Y.io(url, {
            sync: true,
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            items = data;
                        }
                    }
                }
            }
        });
        return items;
    }

    function getFunctions() {
        var url = '/monitor/' + appkey + '/trigger/functions';
        var functions = [];
        Y.io(url, {
            sync: true,
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            functions = data;
                        }
                    }
                }
            }
        });
        return functions;
    }

    function getTriggerItems() {
        showOverlay();

        var screen = metricAutoComplete.getValue();
        var screenId = screen.id;
        var url = '/monitor/business/config?screenId=' + screenId;
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            fillTriggerItems(data);
                        } else {
                            emptyOrError(false);
                        }
                    } else {
                        emptyOrError(true);
                    }
                },
                failure: function () {
                    emptyOrError(true);
                }
            }
        });
    }

    function fillTriggerItems(arr) {
        var micro = new Y.Template();
        var html = micro.render(triggerTemplate, {data: arr});
        tbody.setHTML(html);
        initLocalEditWeight();
        showContent();
    }




    function initLocalEditWeight() {
        var reg = /^\d{1,}$/g;
        var msg = '必须是正整数';
        Y.msgp.utils.localEdit('.change-weight', doChangeTrigger, reg, msg);
    }

    var addTemplate = [
        '<div id="add_trigger_form" class="form-horizontal">',
        '<div class="control-group">',
        '<label id="triggerSide" class="control-label" data-side="<%= data.side %>">持续时间(分钟)：</label>',
        '<div class="controls">',
        '<input id="duration" type="text" class="span3" value="" placeholder="必须是正整数" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">规则类型：</label>',
        '<div class="controls">',
        '<select id="triggerItems" class="span3">',
        '<option value="0" selected>下降值(比基线)</option>',
        '<option value="1">下降百分比(比基线)</option>',
        '<option value="2">上升值(比基线)</option>',
        '<option value="3">上升百分比(比基线)</option>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">阈值：</label>',
        '<div class="controls">',
        '<input id="threshold" type="text" class="span3" value="" placeholder="必须是正整数" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    var dialogEditContent = [
        '<div id="dialog_edit_form" class="form-horizontal">',
        '<div class="control-group mb0"><label class="control-label">阈值：</label>',
        '<div class="controls">',
        '<input id="dThreshold" type="text" value="" placeholder="必须是正整数" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>'
    ].join('');

    var dialogSubscribeContent = [
        '<div class="control-group">',
        '<label class="control-lable">勾选告警渠道，不勾选即为取消报警:</label>',
        '<div id="dialog_subscribe_form" class="controls">',
        '<label class="checkbox inline"><input id="xm" type="checkbox">大象</label>',
        '<label class="checkbox inline"><input id="sms" type="checkbox">短信</label>',
        '<label class="checkbox inline"><input id="email" type="checkbox">邮件</label>',
        '</div>',
        '</div>'
    ].join('');

    var triggerTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<% var businessMonitor=item.businessMonitor %>',
        '<tr data-id="<%= businessMonitor.id %>" ',
        'data-screenId="<%= businessMonitor.screenId %>" ',
        'data-strategy="<%= businessMonitor.strategy %>"',
        'data-threshold="<%= businessMonitor.threshold %>"',
        'data-duration="<%= businessMonitor.duration %>"',
        'data-xm="<%= item.subscribe.xm %>"',
        'data-sms="<%= item.subscribe.sms %>"',
        'data-email="<%= item.subscribe.email %>">',
        '<td><span class="change-weight duration" title="点击修改"><%= businessMonitor.duration %></span></td>',
        '<td class="desc"><%= businessMonitor.desc%></td>',
        '<td><span class="change-weight threshold" title="点击修改"><%= businessMonitor.threshold %></span></td>',
        '<td><a class="trigger-delete" href="javascript:;">删除</a>',
        '<a class="trigger-subscribe-xm ml20" data-status="<%= item.subscribe.xm %>" href="javascript:;"><% if(item.subscribe.xm == 0) { %> 取消大象订阅 <% } else { %> 大象订阅 <% } %></a>',
        '<a class="trigger-subscribe-sms ml20" data-status="<%= item.subscribe.sms %>"  href="javascript:;"><% if(item.subscribe.sms == 0) { %> 取消短信订阅 <% } else { %> 短信订阅 <% } %></a>',
        '<a class="trigger-subscribe-email ml20" data-status="<%= item.subscribe.email %>" href="javascript:;"><% if(item.subscribe.email == 0) { %> 取消邮件订阅 <% } else { %> 邮件订阅 <% } %></a>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'transition',
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete',
        'msgp-utils/localEdit'
    ]
});
M.use('msgp-monitor/businessConfig', function (Y) {
    Y.msgp.monitor.businessConfig(key);
});
