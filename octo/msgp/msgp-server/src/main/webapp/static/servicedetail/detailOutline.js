/* jshint indent : false */
M.add('msgp-servicedetail/detailOutline', function (Y) {
    Y.namespace('msgp.servicedetail').detailOutline = detailOutline;
    var inited = false;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_outline'),
        cBody = wrapper.one('.content-body'),
        processingDialog,
        isDialogOpen = false;

    var outlineTemplate = Y.one('#text_detail_outline').get('value');


    var noappkeyTemplate = [
        '<div class="control-group"><label class="control-label">唯一标识：</label>',
        '<div class="controls">',
        '<span id="outline_appkey" class="outline-content"><%= this.data.appkey %> 不存在</span>',
        '</div>',
        '</div>',
    ].join('');

    function detailOutline(key, f1, f2) {
        if (!inited) {
            appkey = key;
            showOverlay = f1;
            showContent = f2;
            inited = true;
        }
        getOutline();
        Y.one("#content_wrapper").delegate('change', checkCell, "input[name='set_cell_switch']");
    }

    function checkCell() {
        var cellStatus = $('input[name="set_cell_switch"]:checked').val(),
            otherCellStatus = 1 - cellStatus;
        var cellSwitchArr = ['关闭', '开启'];
        var cellSwitchTrigger = new Y.mt.widget.CommonDialog({
            id: 'switch_cell',
            title: '是否' + cellSwitchArr[cellStatus] + 'set',
            content: '确认' + cellSwitchArr[cellStatus] + appkey + '的set开关?',
            width: 300,
            btn: {
                pass: initCell,
                unpass: function () {
                    $('input[name="set_cell_switch"]:checked').removeAttr('checked');
                    $('input[name="set_cell_switch"][value="' + otherCellStatus + '"]')[0].checked = true;
                }
            }
        });
        cellSwitchTrigger.show();

    }

    function initCell() {
        var cellStatus = $('input[name="set_cell_switch"]:checked').val();
        var url = '/service/cellSwitch';
        var data = 'appkey=' + appkey + '&switch=' + cellStatus;
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', ret.data, 3)
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.data, 3);
                        var otherRadioVal = cellStatus === '1' ? '0' : '1';
                        $('input[name="set_cell_switch"]:checked').removeAttr('checked');
                        $(':radio[name="set_cell_switch"][value="' + otherRadioVal + '"]')[0].checked = true;
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                }
            }
        });
    }

    function getOutline() {
        showOverlay(wrapper);
        var url = '/service/' + appkey + '/desc';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillOutline(ret.data);
                        fillOutlineDeleteOwner(ret.data);
                        fillOutlineDeleteObserver(ret.data);
                    } else {

                    }
                },
                failure: function () {

                }
            }
        });
    }

    function fillOutline(obj) {
        obj.ownersStr = getOwners(obj.owners);
        obj.observersStr = getOwners(obj.observers);
        var micro = new Y.Template();
        var templateContent = (0 === obj.createTime) ? noappkeyTemplate : outlineTemplate;
        var html = micro.render(templateContent, {data: obj});
        cBody.setHTML(html);
        wrapper.one('.overlay-mask-process').hide();
        showContent(wrapper);
        cBody.delegate('click', function clickFun(parent) {
            return function () {
                var self = this;
                clickDelete(self);
            }
        }(this), '#delete_outline_service');
    }

    function fillOutlineDeleteOwner(obj) {
        obj.ownersStr = getOwners(obj.owners);
        obj.observersStr = getOwners(obj.observers);
        var micro = new Y.Template();
        var templateContent = (0 === obj.createTime) ? noappkeyTemplate : outlineTemplate;
        var html = micro.render(templateContent, {data: obj});
        cBody.setHTML(html);
        wrapper.one('.overlay-mask-process').hide();
        showContent(wrapper);
        cBody.delegate('click', function clickFun(parent) {
            return function () {
                var self = this;
                deleteOwnerSelf(self)
            }
        }(this), '#delete_outline_service_owner');
    }

    function deleteOwnerSelf(self) {
        var appkey = self.getData("appkey");
        var delOwnerSelfDialog = new Y.mt.widget.CommonDialog({
            id: 'del_owner_dialog',
            title: '取消负责人',
            content: '确认取消' + appkey + '自己的负责人权限?',
            width: 300,
            btn: {
                pass: doDelowner
            }
        });
        delOwnerSelfDialog.show();

    }

    function doDelowner() {
        showDialog('正在处理中...');
        var url = '/service/deleteSelf';
        var data = 'appkey=' + appkey + '&type=owner'
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    if (!isDialogOpen)
                        return;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        hideDialog();
                        Y.config.win.location = '/service/detail?appkey=' + appkey + '#outline';
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                        hideDialog();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    hideDialog();
                }
            }
        });
    }

    function fillOutlineDeleteObserver(obj) {
        obj.ownersStr = getOwners(obj.owners);
        obj.observersStr = getOwners(obj.observers);
        var micro = new Y.Template();
        var templateContent = (0 === obj.createTime) ? noappkeyTemplate : outlineTemplate;
        var html = micro.render(templateContent, {data: obj});
        cBody.setHTML(html);
        wrapper.one('.overlay-mask-process').hide();
        showContent(wrapper);
        cBody.delegate('click', function clickFun(parent) {
            return function () {
                var self = this;
                deleteObserverSelf(self);
            }
        }(this), '#delete_outline_service_observer');
    }

    function deleteObserverSelf(self) {
        var appkey = self.getData("appkey");
        var delOwnerSelfDialog = new Y.mt.widget.CommonDialog({
            id: 'del_owner_dialog',
            title: '取消关注人',
            content: '确认取消' + appkey + '自己的关注人权限?',
            width: 300,
            btn: {
                pass: doDelobserver
            }
        });
        delOwnerSelfDialog.show();

    }

    function doDelobserver() {
        showDialog('正在处理中...');
        var url = '/service/deleteSelf';
        var data = 'appkey=' + appkey + '&type=observer'
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: data,
            on: {
                success: function (id, o) {
                    if (!isDialogOpen)
                        return;
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        hideDialog();
                        Y.config.win.location = '/service/detail?appkey=' + appkey + '#outline';
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                        hideDialog();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    hideDialog();
                }
            }
        });
    }

    function getOwners(arr) {
        var tmp = [];
        if (null == arr) {
            return '';
        }
        for (var i = 0, l = arr.length; i < l; i++) {
            tmp.push(arr[i].name + "(" + arr[i].login + ")");
        }
        return tmp.join(',');
    }

    function clickDelete(self) {
        var appkey = self.getData("appkey");
        var delServiceDialog = new Y.mt.widget.CommonDialog({
            id: 'del_service_dialog',
            title: '删除服务',
            content: '确认删除服务' + appkey + '?',
            width: 300,
            btn: {
                pass: doDelService
            }
        });
        delServiceDialog.show();

        function doDelService() {
            showDialog('正在处理中...');
            var url = '/service/delete?appkey=' + appkey + '&force=true';
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        if (!isDialogOpen)
                            return;
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            hideDialog();
                            Y.config.win.location = '/business';
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
                            hideDialog();
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                        hideDialog();
                    }
                }
            });
        }
    }

    function showDialog(msg) {
        processingDialog = processingDialog ? processingDialog : new Y.mt.widget.CommonDialog({
            id: 'del_service_processing_dialog',
            width: 400,
            title: '处理中',
            content: '<i class="fa fa-spinner fa-spin text-blue mr10"></i>' + msg
        });
        processingDialog.show();
        wrapper.one('.overlay-mask-process').show();
        isDialogOpen = true;
    }

    function hideDialog() {
        processingDialog.close();
        wrapper.one('.overlay-mask-process').hide();
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'msgp-utils/msgpHeaderTip'
    ]
});
