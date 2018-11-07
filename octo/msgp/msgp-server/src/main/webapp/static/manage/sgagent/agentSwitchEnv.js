/**
 * Created by lhmily on 08/24/2015.
 */
M.add('msgp-manage/sgagent/agentSwitchEnv', function (Y) {
        Y.namespace('msgp.manage').agentSwitchEnv = agentSwitchEnv;
        var wrapper = Y.one('#wrap_agentSwitchEnv');
        var uncomfirm = wrapper.one('#uncomfirm');
        var comfirmed = wrapper.one('#comfirmed');
        var rejected = wrapper.one('#rejected');
        var pbody = wrapper.one('#paginator_switchEnv');
        var rejectDialog, checkNote;
        var defaultPage = {
            page: 1,
            pageSize: 20
        }
        var currentPage = 1;
        var flag = 0;
        var tbody = wrapper.one('#table_switchEnv').one('tbody');
        var searchIP = '';
        var currentAccessAjax;
        var dialogContent = [
            '<textarea placeholder="填写拒绝该主机环境切换的理由 ..." style="width:80%;overflow: auto; height: 240px;"></textarea>',
            '<sapn class="tips" id="textareaTips"></sapn>'
        ].join('');


        var trTemplate = [
            '<% Y.Array.each(this.data, function(item, index){ %>',
            '<tr data-id=<%= item.id%>  id="ret-tr">',
            '<td><%= item.hostName%>(<%= item.ip%>)',
            '</td>',
            '<td><%= item.cluster%>',
            '</td>',
            '<td><%= item.applyMisid%>',
            '</td>',
            '<td><%= item.oldEnv%>',
            '</td>',
            '<td><%= item.newEnv%>',
            '</td>',
            '<td><%= Y.mt.date.formatDateByString( new Date(item.applyTime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
            '</td>',
            '<td><%= (null!=item.comfirmMisid)?item.comfirmMisid:""%>',
            '</td>',
            '<td><%= (null!=item.comfirmTime)?Y.mt.date.formatDateByString( new Date(item.comfirmTime), "yyyy-MM-dd hh:mm:ss" ):"" %></td>',
            '</td>',
            '<td> <% switch(item.flag){%>',
            '<% case 0:%>',
            '      <a id="resetAgent" href="javascript:void(0);" class="panel-comfirm"><i class="fa fa-check"></i> <span>重置agent</span> </a>',
            '      <a id="comfirmItem" href="javascript:void(0);" class="panel-comfirm">&nbsp;&nbsp;<i class="fa fa-check"></i> <span>调整zk</span> </a>',
            '      <a id="rejectItem" href="javascript:void(0);" class="panel-delete">&nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span>拒绝</span> </a>',
            '<%  break;%>',
            '<%}%></td>',
            '<td><%= (null!=item.note)?item.note:""%>',
            '</td>',
            '</tr>',
            '<% }); %>'
        ].join('');

        function agentSwitchEnv() {
            uncomfirm.on('click', clickComfirmSelect);
            comfirmed.on('click', clickComfirmSelect);
            rejected.on('click', clickComfirmSelect);
            uncomfirm._node.click();
            bindSearchIP();
            bindResetAgent();
            bindConfirm();
            bindReject();
        }

        function bindResetAgent() {
            wrapper.delegate('click', function () {
                var tr = this.ancestor('tr');
                var id = tr.getData('id');
                var url = '/manage/agent/switchEnv/resetAgent';
                var note = "";
                Y.io(url, {
                    method: 'GET',
                    data: {
                        id: id
                    },
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                Y.msgp.utils.msgpHeaderTip('info', ret.data, 5);
                                fillTable({
                                    page: currentPage,
                                    pageSize: 20
                                });
                            } else {
                                local_error_alert(ret.msg);
                            }
                        },
                        failure: function (id, o) {
                            local_error_alert("服务器异常");
                        }
                    }
                });
            }, '#resetAgent');
        }

        function doReject(x, y) {

            if (!checkNote.isValid()) {
                checkNote.showMsg();
                return true;
            }
            var body = rejectDialog.getBody();
            var id = y.get('id');
            var url = '/manage/agent/switchEnv/rejectSwitchEnv';
            var note = body.one('textarea').get('value');
            Y.io(url, {
                method: 'GET',
                data: {
                    id: id,
                    note: note
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', ret.data, 5);
                            fillTable({
                                page: currentPage,
                                pageSize: 20
                            });
                        } else {
                            local_error_alert(ret.msg);
                        }
                    },
                    failure: function (id, o) {
                        local_error_alert("服务器异常");
                    }
                }
            });
        }

        function bindReject() {
            wrapper.delegate('click', function () {
                var tr = this.ancestor('tr');
                var id = tr.getData('id');
                rejectDialog = rejectDialog ? rejectDialog : new Y.mt.widget.CommonDialog({
                    id: id,
                    title: '填写拒绝理由',
                    width: 640,
                    btn: {
                        pass: doReject,
                        passName: "拒绝"
                    }
                });
                Y.msgp.service.commonMap(showDialogAfter);
                function showDialogAfter(obj) {
                    var micro = new Y.Template();
                    var str = micro.render(dialogContent, {data: ''});
                    rejectDialog.setContent(str);
                    checkNote = Y.msgp.utils.check.init(rejectDialog.getBody().one('textarea'), {
                        type: 'string',
                        chineseOk: true,
                        spaceOk: true,
                        warnElement: rejectDialog.getBody().one('#textareaTips')
                    });
                    rejectDialog.show();
                }
            }, '#rejectItem');
        }

        function bindConfirm() {
            wrapper.delegate('click', function () {
                var tr = this.ancestor('tr');
                var id = tr.getData('id');
                var url = '/manage/agent/switchEnv/comfirmSwitchEnv';
                Y.io(url, {
                    method: 'GET',
                    data: {
                        id: id
                    },
                    on: {
                        success: function (id, o) {
                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                Y.msgp.utils.msgpHeaderTip('info', ret.data, 5);
                                fillTable({
                                    page: currentPage,
                                    pageSize: 20
                                });
                            } else {
                                local_error_alert(ret.msg);
                            }
                        },
                        failure: function (id, o) {
                            local_error_alert("审核失败");
                        }
                    }
                });
            }, '#comfirmItem');
        }

        function clickComfirmSelect() {
            if (this.hasClass("btn-primary"))return;
            wrapper.one('#comfirm_select').all('a').removeClass('btn-primary');
            this.addClass('btn-primary');
            flag = this.getAttribute('value');
            fillTable(defaultPage);
        }

        function bindSearchIP() {
            wrapper.delegate('click', function () {
                searchIP = this.ancestor('div').one('#switchEnv_ip').get('value');
                fillTable(defaultPage);
            }, '#searchIP');
        }

        function fillTable(page) {
            show('loading', '');
            currentPage = page.page;
            var url = '/manage/agent/switchEnv/switchEnvList';
            if (typeof(currentAccessAjax) != "undefined") {
                currentAccessAjax.abort();
            }
            currentAccessAjax = Y.io(url, {
                method: 'GET',
                data: {
                    pageNo: page.page,
                    pageSize: page.pageSize,
                    flag: flag,
                    searchIP: searchIP
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var ret_page = ret.page;
                            if (ret.data.length <= 0) {
                                show('error', '无内容');
                                return;
                            }
                            show('data', ret.data);
                            new Y.mt.widget.Paginator({
                                contentBox: pbody,
                                index: ret_page.pageNo,
                                max: ret_page.totalPageCount,
                                totalCount: ret_page.totalCount,
                                pageSize: ret_page.pageSize,
                                callback: fillTable
                            });
                            pbody.removeClass('displayNone');
                        } else {
                            show('error', ret.msg);
                        }
                    },
                    failure: function (id, o) {
                        if ('abort' != o.statusText) {
                            show('error', '服务器异常');
                        }
                    }
                }
            });
        }

        function show(flag, data) {
            switch (flag) {
                case 'data':
                    var micro = new Y.Template();
                    var html = micro.render(trTemplate, {data: data});
                    tbody.append(html);
                    tbody.one('#content_loading').addClass('displayNone');
                    tbody.one('#content_msg').addClass('displayNone');
                    break;
                case 'error':
                    tbody.one('#content_msg_td')._node.innerText = data;
                    tbody.all('#ret-tr').remove();
                    tbody.one('#content_loading').addClass('displayNone');
                    tbody.one('#content_msg').removeClass('displayNone');
                    pbody.addClass('displayNone');
                    break;
                case 'loading':
                default:
                    tbody.all('#ret-tr').remove();
                    tbody.one('#content_loading').removeClass('displayNone');
                    tbody.one('#content_msg').addClass('displayNone');
                    pbody.addClass('displayNone');
                    break;
            }
        }

        function local_error_alert(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg, 1000);
        }
    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'template',
            'msgp-utils/check',
            'msgp-service/commonMap'
        ]
    }
)
;