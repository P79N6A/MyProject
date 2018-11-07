/* jshint indent : false */
M.add('msgp-monitor/providerConfig-version0.0.1', function (Y) {
    Y.namespace('msgp.monitor').providerConfig = providerConfig;
    var _appkey, _items;
    var twrap = Y.one('#triggers_wrap');
    var tbody = twrap.one('tbody');
    var thresholdInput, durationInput;
    var addTriggerDialog, dialogEdit, dialogDelete, dialogSubscribe, dialogTriggerSwitch;

    function providerConfig(key) {
        _appkey = key;
        initEvent();
        getTriggerItems();
    }


    function initEvent() {
        var addButton = Y.one('#add_trigger');
        addButton.on('click', addTriggerEvent);
        buildEditEvent();
        buildAppSubScribeEvent();
        buildDeleteEvent();
        buildTriggerSubscribeEvent();
        //线下节点启用的开关
        buildOfflineProviderMonitorTrigger();
    }

    function getTriggerItems() {
        showOverlay();
        var url = '/monitor/' + _appkey + '/provider/triggers';
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


    function buildOfflineProviderMonitorTrigger() {
        Y.one('#provider_trigger').on('click', MonitorTriggerSwitch);
    }


    function buildAppSubScribeEvent() {
        Y.one('#subscribe_all_alarm').on('click', subs);
    }

    function MonitorTriggerSwitch() {
        var triggerSwitch = document.getElementById("provider_trigger").getAttribute("value");
        var content = triggerSwitch == "on" ? "确定开启"+_appkey+"的线下节点监控？" : "确定关闭"+_appkey+"的线下节点监控？";
        dialogTriggerSwitch = dialogTriggerSwitch ? dialogTriggerSwitch : new Y.mt.widget.CommonDialog({
            title: '线下节点监控开关',
            width: 400,
            content: content,
            btn: {
                pass: function () {
                    var url = '/monitor/provider/trigger/switch?appkey='+_appkey+"&triggerSwitch="+triggerSwitch;
                    Y.io(url,{
                        method: 'GET',
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                if (ret.isSuccess) {
                                    Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                                    location.reload();
                                }
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '操作失败', 3);
                            }
                        }
                    });
                }
            }
        });
        dialogTriggerSwitch.show();
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
                    var params = {appkey: _appkey, xm: xm, sms: sms, email: email}
                    console.log(params)
                    var url = '/monitor/' + _appkey + '/provider/trigger/subscribe/batch';
                    Y.io(url, {
                        method: 'post',
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
        var triggerId = tr.getData("id")
        var status = node.getData("status");
        var newStatus = (status == undefined || status == null || status == '' || status == 1) ? 0 : 1;
        var params = {appkey: _appkey, triggerId: +triggerId, mode: mode, newStatus: newStatus}
        console.log(params)
        var url = '/monitor/' + _appkey + '/provider/trigger/subscribe';
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
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
        var desc = [];
        _items = getItems();

        var micro = new Y.Template();
        var str = micro.render(addTemplate, {items: _items});
        $.each(_items, function (key, item) {
            desc.push(item.desc);
        });
        addTriggerDialog.setContent(str);
        addTriggerDialog.show();
        $(".triggerItems").autocomplete({
            source: desc,
            minLength: 0
        });
        initVerify();
    }

    function initAddTriggerDialog() {
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'add_trigger_dialog',
            title: '增加监控项',
            width: 640,
            drag: function () {
            },
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
            "item": tr.getData('item'),
            "itemDesc": tr.getData('itemdesc'),
            "function": tr.getData('function'),
            "functionDesc": tr.getData('functiondesc'),
            "threshold": +dThreshold.node.get('value')
        };
        var url = '/monitor/' + _appkey + '/provider/trigger';
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
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


    function doAddTrigger() {
        if (!thresholdInput.node.getData('status')) {
            thresholdInput.showMsg();
            return;
        }
        var url = '/monitor/' + _appkey + '/provider/trigger';
        var itemSelect = Y.one('#triggerItems');
        var funcSelect = Y.one('#triggerFunctions');
        var itemName = "";
        $.each(_items, function (key, item) {
            if (item.desc == (itemSelect.get('value'))) {
                itemName = item.name + '';
            }
        });
        var data = {
            "item": itemName,
            "itemDesc": itemSelect.get('value'),
            "function": funcSelect.get('value'),
            "functionDesc": funcSelect.get('children').getDOMNodes()[funcSelect.getDOMNode().selectedIndex].innerHTML,
            "threshold": +Y.one('#threshold').get('value')
        };
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
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


    function getItems() {
        var url = '/monitor/' + _appkey + '/provider/trigger/items';
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
        var url = '/monitor/' + _appkey + '/trigger/functions';
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


    function fillTriggerItems(arr) {
        var micro = new Y.Template();
        var html = micro.render(triggerTemplate, {data: arr});
        tbody.setHTML(html);
        showContent();
    }

    var addTemplate = [
        '<div id="add_trigger_form" class="form-horizontal">',
        '<div class="control-group"><label class="control-label">监控项：</label>',
        '<div class="controls">',
        '<input id="triggerItems" class="span3 triggerItems">',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">触发条件：</label>',
        '<div class="controls">',
        '<select id="triggerFunctions" class="span3"><option value="<">小于</option><option value=">">大于</option></select>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">阈值：</label>',
        '<div class="controls">',
        '<input id="threshold" type="text" class="span3" value="10" placeholder="必须是正整数" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    var dialogEditContent = [
        '<div id="dialog_edit_form" class="form-horizontal">',
        '<div class="control-group mb0"><label class="control-label">阈值：</label>',
        '<div class="controls">',
        '<input id="dThreshold" type="text" value="<%= this.threshold %>" placeholder="必须是正整数" />',
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
        '<tr data-id="<%= item.id %>" data-item="<%= item.item %>"  data-threshold="<%= item.threshold %>" data-function="<%= item.function %>" data-itemdesc="<%= item.itemDesc %>"   data-functiondesc="<%= item.functionDesc %>">',
        '<td><%= item.itemDesc + " " + item.functionDesc %></td>',
        '<td><%= item.threshold %></td>',
        '<td><a class="trigger-edit" href="javascript:;">编辑阈值</a>',
        '<a class="trigger-delete ml20" href="javascript:;">删除告警项</a>',
        '<a class="trigger-subscribe-xm ml20" data-status="<%= item.xm %>" href="javascript:;"><% if(item.xm == 0) { %> 取消大象订阅 <% } else { %> 大象订阅 <% } %></a>',
        '<a class="trigger-subscribe-sms ml20" data-status="<%= item.sms %>"  href="javascript:;"><% if(item.sms == 0) { %> 取消短信订阅 <% } else { %> 短信订阅 <% } %></a>',
        '<a class="trigger-subscribe-email ml20" data-status="<%= item.email %>" href="javascript:;"><% if(item.email == 0) { %> 取消邮件订阅 <% } else { %> 邮件订阅 <% } %></a>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');


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
        thresholdInput = Y.msgp.utils.check.init(Y.one('#threshold'), {
            type: 'custom',
            customRegExp: /^\d{1,}$/,
            warnMsg: '必须是正整数',
            warnElement: Y.one('#threshold').next('')
        });
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

    function deleteLineData() {
        var tr = dialogDelete.tr;
        var url = '/monitor/' + _appkey + '/provider/trigger/';
        Y.io(url, {
            method: 'delete',
            data: {
                "id": tr.getData('id')
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        //tr.remove( true );
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

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'transition',
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete'
    ]
});
M.use('msgp-monitor/providerConfig-version0.0.1', function (Y) {
    Y.msgp.monitor.providerConfig(key);
});
