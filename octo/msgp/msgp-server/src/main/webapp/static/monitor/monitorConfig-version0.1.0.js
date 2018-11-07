/* jshint indent : false */
M.add('msgp-monitor/monitorConfig', function (Y) {
    Y.namespace('msgp.monitor').monitorConfig = monitorConfig;
    var appkey;
    var twrap = Y.one('#triggers_wrap');
    var tbody = twrap.one('tbody');
    var thresholdInput, durationInput;
    var addTriggerDialog;
    var addCoreTriggerDialog;
    var dialogEdit;
    var dialogDelete;
    var dialogSubscribe;


    function monitorConfig(key) {
        appkey = key;
        initEvent();
        getTriggerItems();
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
        thresholdInput = Y.msgp.utils.check.init(Y.one('#threshold'), {
            type: 'custom',
            customRegExp: /^\d{1,}$/,
            warnMsg: '必须是正整数',
            warnElement: Y.one('#threshold').next('')
        });

        durationInput = Y.msgp.utils.check.init(Y.one('#duration'), {
            type: 'custom',
            customRegExp: /^\d{1,}$/,
            warnMsg: '必须是正整数',
            warnElement: Y.one('#duration').next('')
        });
    }

    function initEvent() {
        var addButton = Y.one('#add_trigger');
        addButton.on('click', addTriggerEvent);
        var addCallButton = Y.one("#add_call_trigger");
        addCallButton.on('click', addTriggerEvent);
        var addCoreButton = Y.one("#add_core_trigger");
        addCoreButton.on('click', addCoreTriggerEvent);
        buildEditEvent();
        buildDeleteEvent();
        buildAppSubScribeEvent();
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

            var dDurationInput = Y.one('#dDuration');
            dDurationInput.set('value', tr.getData('duration'));
            dDuration = Y.msgp.utils.check.init(dDurationInput, {
                type: 'custom',
                customRegExp: /^\d{1,}$/,
                warnMsg: '必须是正整数',
                warnElement: dDurationInput.next()
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
                    var xm = Y.one("#xm").get("checked");
                    var sms = Y.one("#sms").get("checked");
                    var email = Y.one("#email").get("checked");
                    var params = {appkey: appkey, xm: xm, sms: sms, email: email};

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
                                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
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
        var triggerId = tr.getData("id");
        var status = node.getData("status");
        var newStatus = (status == undefined || status == null || status == '' || status == 1) ? 0 : 1;
        var params = {appkey: appkey, triggerId: +triggerId, mode: mode, newStatus: newStatus}
        var url = '/monitor/' + appkey + '/trigger/subscribe';
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
        var desc = [];
        var button = this;
        var side = button.getAttribute("data-side");
        var spans = getSpannames(side);
        var items = getItems();
        var functions = getFunctions();

        var micro = new Y.Template();
        var str = micro.render(addTemplate, {side: side, spans: spans, items: items, functions: functions});
        $.each(items, function (key, item) {
            desc.push(item.desc);
        });
        addTriggerDialog.setContent(str);
        initMultipleSpans(spans, '#triggerSpans');
      
        $('#show-triggerSpans-input').click(function () {
            $('#triggerSpans-input')[0].disabled = false;
        });

        $("#triggerItems").autocomplete({
            source: desc,
            minLength: 0
        });
        addTriggerDialog.show();
        initVerify();
    }

    function addCoreTriggerEvent() {
        addCoreTriggerDialog = addCoreTriggerDialog ? addCoreTriggerDialog : initAddCoreTriggerDialog();
        var desc = [];
        var button = this;
        var side = button.getAttribute("data-side");
        var spans = getSpannames(side);
        var items = getItems();
        var functions = getFunctions();

        var micro = new Y.Template();
        var addCoreTemplate = Y.one('#text-add-core-trigger-form').get('value');
        var str = micro.render(addCoreTemplate, {functions: functions});
        $.each(items, function (key, item) {
            desc.push(item.desc);
        });

        addCoreTriggerDialog.setContent(str);

        $("#coreTriggerItems").autocomplete({
            source: desc,
            minLength: 0
        });
        $("#coreTriggerSpans").autocomplete({
            source: spans,
            minLength: 0,
            select: function( event, ui ){
                var span = ui.item.value;
                var url = '/monitor/' + appkey + '/triggers/span/core';
                $.ajax({
                    type: 'GET',
                    url: url,
                    data: {
                        span: span
                    },
                    success: function (ret) {
                        var data = ret.data;
                        if (ret.isSuccess && data.length > 0) {
                            var clientSpans = data.map(function (obj) {
                                return obj.methodName
                            });
                            console.log(clientSpans);
                            initMultipleSpans(clientSpans, '#coreTriggerClientSpans')
                        }
                    }
                })
            }
        });
        initMultipleSpans([], '#coreTriggerClientSpans');
        bindCoreTriggerDialog(addCoreTriggerDialog);
        addCoreTriggerDialog.show();
    }

    function bindCoreTriggerDialog() {
        var node = addCoreTriggerDialog.getBody();
        node.delegate('click', function(){
            var ifChecked = this.get("checked");
            if(!ifChecked){
                node.one('#triggers-saved-tip').hide();
                node.one('#triggers-saved').hide();
            }else{
                var url = '/monitor/' + appkey + '/triggers';
                Y.io(url, {
                    method: 'get',
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            var data = ret.data;
                            if (ret.isSuccess) {
                                var clientTriggers = data.filter(function (element) {
                                    return element.side == "client";
                                });
                                if (clientTriggers && clientTriggers.length > 0) {
                                    var micro = new Y.Template();
                                    var addCoreSavedTbodyTemplate = Y.one('#text-add-core-trigger-saved-tbody').get('value');
                                    var html = micro.render(addCoreSavedTbodyTemplate, {data: clientTriggers});
                                    node.one("#triggers-saved").setHTML(html);

                                    node.one('#triggers-saved-tip').show();
                                    node.one('#triggers-saved').show();

                                }else{
                                    node.one('#triggers-saved-tip').hide();
                                    node.one('#triggers-saved').hide();
                                }
                            }else{
                                node.one('#triggers-saved-tip').hide();
                                node.one('#triggers-saved').hide();
                            }
                        }
                    }
                });
            }
        }, '#display-triggers-saved');

        node.one('#display-triggers-saved')._node.click();

        node.delegate('click', function(){
            var itemSelect = Y.one('#coreTriggerItems');
            var itemDesc = itemSelect.get('value');
            var side = 'client';

            var selectedClientSpans = [];
            $('#coreTriggerClientSpans option:selected').map(function (a, item) {
                selectedClientSpans.push(item.value);
            });

            var funcSelect = Y.one('#coreTriggerFunctions');
            var items = getItems();
            var duration = node.one('#coreTriggerDuration').get('value');
            var threshold = node.one('#coreTriggerThreshold').get('value');
            var itemName = '';
            var _function = funcSelect.get('value');
            var functionDesc = funcSelect.get('children').getDOMNodes()[funcSelect.getDOMNode().selectedIndex].innerHTML;

            $.each(items, function (key, item) {
                if (item.desc == (itemSelect.get('value'))) {
                    itemName = item.name + '';
                }
            });

            var triggers = [];
            $.each(selectedClientSpans, function (key, item) {
                triggers.push({
                    "side": side,
                    "spanname": item,
                    "item": itemName,
                    "itemDesc": itemDesc,
                    "duration": duration,
                    "function": _function,
                    "functionDesc": functionDesc,
                    "threshold": threshold
                })
            });

            var numberReg = /^\d{1,}$/;
            if(numberReg.test(threshold) && numberReg.test(duration) && triggers.length > 0) {
                var micro = new Y.Template();
                var addCoreSelectedTbodyTemplate = Y.one('#text-add-core-trigger-selected-tbody').get('value');
                var html = micro.render(addCoreSelectedTbodyTemplate, {data: triggers});
                node.one("#triggers-selected").setHTML(html);
            }else{
                alert("参数错误: 请检查监控指标、阈值、持续时间是否为空");
            }
        }, '#add-core-trigger-form-button');

        //绑定表中的删除
        var addCoreSelectedTbody = node.one("#triggers-selected");
        addCoreSelectedTbody.delegate('click', function () {
            var tr = this.ancestor('tr');
            tr.remove();
        }, '.core-trigger-delete');
    }

    //初始化多选框
    function initMultipleSpans(spans, node_id) {
        $(node_id).multiselect({
            selectAllText: "选择全部",
            allSelectedText: "已选择全部即接口",
            nonSelectedText: "未选择接口",
            placeholder: "请选择接口[最多支持40个]",
            buttonWidth: '300px',
            maxHeight: 200,
            filterPlaceholder: '搜索接口',
            enableFiltering: true,
            includeSelectAllOption: true,
            selectAllNumber: true,
            buttonText: function (options, select) {
                var total = $(node_id + ' option').length;
                if (options.length === 0) {
                    return '未选择接口,无法添加监控项';
                }
                else if (options.length < total && options.length > 1) {
                    return '已选择' + options.length + '个接口';
                } else if (options.length == total) {
                    return '已选择全部接口(' + total + '个)';
                } else {
                    var labels = [];
                    options.each(function () {
                        if ($(this).attr('label') !== undefined) {
                            labels.push($(this).attr('label'));
                        }
                        else {
                            labels.push($(this).html());
                        }
                    });
                    return labels.join(', ') + '';
                }
            }
        });
        var options = [];
        $.each(spans, function(i, span) {
            options.push({
                label: span,
                value: span
            })
        });
        $(node_id).multiselect('dataprovider', options);
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

    function initAddCoreTriggerDialog() {
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'add-trigger-dialog',
            title: '添加核心接口监控',
            width: 1000,
            height: 500,
            drag: function () {
            },
            refresh: 1,
            btn: {
                pass: doAddCoreTrigger
            }
        });
        return dialog;
    }

    function refreshLineData() {
        if (!dThreshold.node.getData('status') || !dDuration.node.getData('status')) {
            dThreshold.showMsg();
            dDuration.showMsg();
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
            "threshold": +dThreshold.node.get('value'),
            "duration": +dDuration.node.get('value')
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
        var url = '/monitor/' + appkey + '/trigger/';
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
    
    function doAddCoreTrigger() {
        var itemSelect = Y.one('#coreTriggerItems');
        var itemDesc = itemSelect.get('value');
        var side = 'client';

        var selectedClientSpans = [];
        $('#coreTriggerClientSpans option:selected').map(function (a, item) {
            selectedClientSpans.push(item.value);
        });

        var itemCountConfig = getExistedItemDescCount(appkey, itemDesc, side);
        var existedItemDescCount = Number(itemCountConfig[0]);
        var maxItemCount = Number(itemCountConfig[1]);
        var totalItemDescCount = selectedClientSpans.length + existedItemDescCount;

        if(totalItemDescCount > maxItemCount) {
            var validCount = existedItemDescCount > maxItemCount ? 0 : maxItemCount - existedItemDescCount;
            var message = "单个监控项的配置的接口数不能大于" + maxItemCount + ", 请取消部分非核心接口配置, 该监控项[" + itemDesc +  "]还可配置的接口数为" + validCount;
            Y.msgp.utils.msgpHeaderTip('error', message, 20);
            return;
        }

        var funcSelect = Y.one('#coreTriggerFunctions');
        var items = getItems();
        var duration = +Y.one('#coreTriggerDuration').get('value');
        var threshold = +Y.one('#coreTriggerThreshold').get('value');
        var itemName = '';
        var _function = funcSelect.get('value');
        var functionDesc = funcSelect.get('children').getDOMNodes()[funcSelect.getDOMNode().selectedIndex].innerHTML;

        $.each(items, function (key, item) {
            if (item.desc == (itemSelect.get('value'))) {
                itemName = item.name + '';
            }
        });

        var triggers = [];
        $.each(selectedClientSpans, function (key, item) {
            triggers.push({
                "side": side,
                "spanname": item,
                "item": itemName,
                "itemDesc": itemDesc,
                "duration": duration,
                "function": _function,
                "functionDesc": functionDesc,
                "threshold": threshold
            })
        });

        var url = '/monitor/' + appkey + '/triggers/insert/batch';
        showWaitMsg(tbody);
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(triggers),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '添加成功', 3);
                        getTriggerItems(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '添加失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
                }
            }
        });
    }

    function doAddTrigger() {
        if (!thresholdInput.node.getData('status')) {
            thresholdInput.showMsg();
            return;
        }

        if (!durationInput.node.getData('status')) {
            durationInput.showMsg();
            return;
        }

        var itemSelect = Y.one('#triggerItems');
        var itemDesc = itemSelect.get('value');
        var side = Y.one('#triggerSide').getAttribute("data-side");

        var selectedSpans = [];
        $('#triggerSpans option:selected').map(function (a, item) {
            selectedSpans.push(item.value);
        });
        var inputedSpans = $.trim($("#triggerSpans-input").val());
        if(inputedSpans != ''){
            selectedSpans.push(inputedSpans);
        }
        distinctArray(selectedSpans);

        var itemCountConfig = getExistedItemDescCount(appkey, itemDesc, side);
        var existedItemDescCount = Number(itemCountConfig[0]);
        var maxItemCount = Number(itemCountConfig[1]);
        var totalItemDescCount = selectedSpans.length + existedItemDescCount;

        if(totalItemDescCount > maxItemCount) {
            var validCount = existedItemDescCount > maxItemCount ? 0 : maxItemCount - existedItemDescCount;
            var message = "单个监控项的配置的接口数不能大于" + maxItemCount + ", 请取消部分非核心接口配置, 该监控项[" + itemDesc +  "]还可配置的接口数为" + validCount;
            Y.msgp.utils.msgpHeaderTip('error', message, 20);
            return;
        }

        var url = '/monitor/' + appkey + '/triggers/insert/batch';
        var funcSelect = Y.one('#triggerFunctions');
        var items = getItems();
        var duration = +Y.one('#duration').get('value');
        var threshold = +Y.one('#threshold').get('value');
        var itemName = '';
        var _function = funcSelect.get('value');
        var functionDesc = funcSelect.get('children').getDOMNodes()[funcSelect.getDOMNode().selectedIndex].innerHTML;
        
        $.each(items, function (key, item) {
            if (item.desc == (itemSelect.get('value'))) {
                itemName = item.name + '';
            }
        });
        var triggers = [];
        $.each(selectedSpans, function (key, item) {
            triggers.push({
                "side": side,
                "spanname": item,
                "item": itemName,
                "itemDesc": itemDesc,
                "duration": duration,
                "function": _function,
                "functionDesc": functionDesc,
                "threshold": threshold
            })
        });
        showWaitMsg(tbody);
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(triggers),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '添加成功', 3);
                        getTriggerItems(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '添加失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
                }
            }
        });
    }

    //获取该监控项已存在的数目
    function getExistedItemDescCount(appkey, itemDesc, side) {
        var itemCountConfig = [0, 40];
        $.ajax({
            type:"POST",
            data: {
                'itemDesc': itemDesc,
                'side': side
            },
            url:'/monitor/' + appkey + '/triggers/itemDesc/count',
            async: false,
            success:function(ret){  //function1()
                itemCountConfig =  ret.data
            }
        });
        return itemCountConfig
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

        //过滤静态资源
        var filteredSpans = [];
        Y.each(spans, function (element) {
            if(element.indexOf('/') < 0){
                filteredSpans.push(element);
            }
        });
        return filteredSpans;
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
        var url = '/monitor/' + appkey + '/triggers';
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
        showContent();
    }

    var addTemplate = [
        '<div id="add_trigger_form" class="form-horizontal">',
        '<div class="control-group">',
        '<label id="triggerSide" class="control-label" data-side="<%= data.side %>">',
        '<% if(data.side == "server") { %> 服务接口 <% } else { %> 外部接口 <% } %>：</label>',
        '<div class="controls">',
        '<select id="triggerSpans" class="span3 triggerSpans" multiple="multiple"></select>',
        '</div>',
        '</div>',
        '<div class="control-group" style="margin-bottom: 10px;"><label class="control-label"><a id="show-triggerSpans-input" href="javascript:;" style="padding-left: 20px;">新接口点此输入</a></label>',
        '<div class="controls">',
        '<input id="triggerSpans-input" style="width: 295px;" placeholder="请准确输入选择框中未出现的接口名" disabled="disabled">',
        '</div>',
        '</div>',
        '<div class="control-group" style="padding-top: 10px;"><label class="control-label">监控项：</label>',
        '<div class="controls">',
        '<input id="triggerItems" class="span3 triggerItems" style="width: 295px">',
        '</div>',
        '</div>',
        '<div class="control-group"><label id="triggerSide" class="control-label" data-side="<%= data.side %>">持续时间(分钟)：</label>',
        '<div class="controls">',
        '<input id="duration" type="text" class="span3" value="" placeholder="必须是正整数" style="width: 286px"/>',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">触发条件：</label>',
        '<div class="controls">',
        '<select id="triggerFunctions" class="span3" style="width: 300px">',
        '<% Y.Object.each(this.functions, function(func, index){ %>',
        '<option value="<%= func.name %>"><%= func.desc %></option>',
        '<% }); %>',
        '</select>',
        '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">阈值：</label>',
        '<div class="controls">',
        '<input id="threshold" type="text" class="span3" value="200" placeholder="必须是正整数" style="width: 286px"/>',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
        '</div>'
    ].join('');

    var dialogEditContent = [
        '<div id="dialog_edit_form" class="form-horizontal">',
        '<div class="control-group"><label id="triggerSide" class="control-label">持续时间(分钟)：</label>',
        '<div class="controls">',
        '<input id="dDuration" type="text" class="span3" value="<%= this.duration %>" placeholder="必须是正整数" />',
        '<span class="tips"></span>',
        '</div>',
        '</div>',
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
        '<tr data-id="<%= item.id %>" data-side="<%= item.side %>" data-spanname="<%= item.spanname %>" data-item="<%= item.item %>"  data-threshold="<%= item.threshold %>" data-function="<%= item.function %>" data-itemdesc="<%= item.itemDesc %>" data-duration="<%= item.duration %>" data-functiondesc="<%= item.functionDesc %>">',
        //在这里做side替换而不在后台做替换的原因是：在“编辑”监控项时需要值为"server"或者“client”的side进行ajax请求。
        '<td><% if(item.side == "server") { %> 服务接口 <% } else { %> 外部接口 <% } %></td>',
        '<td><%= item.spanname %></td>',
        '<td><%= item.itemDesc + " " + item.functionDesc %></td>',
        '<td><%= item.duration %></td>',
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

    function showWaitMsg(node) {
        var html = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">后台操作中...</span></div>'+
            '</div>';
        node.append(html);
    }

    function distinctArray(arr){
        var self = arr;
        var _a = arr.concat().sort();
        _a.sort(function(a,b){
            if(a == b){
                var n = self.indexOf(a);
                self.splice(n,1);
            }
        });
        return self;
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'transition',
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip'
    ]
});
M.use('msgp-monitor/monitorConfig', function (Y) {
    Y.msgp.monitor.monitorConfig(key);
});
