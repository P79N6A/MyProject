M.add('msgp-manage/sgagent/httpAuth', function (Y) {
    Y.namespace('msgp.manage').httpAuth = httpAuth;

    var appkey;
    var wrapper = Y.one('#wrap_httpAuth');
    var addGroupDialog, editAuthDialog, deleteAuthDialog, detailDialog;
    var dialogBody;

    var dialogTemplate = [
        '<div id="add_auth_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label"><b>用户名：</b></label>',
                '<div class="controls">',
                     '<input id="username_input" type="text" class="span5" value="<%= this.name %>" placeholder="用户名，必填" />',
                '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label"><b>token：</b></label>',
                '<div class="controls">',
                    '<input id="token_input" type="text" class="span5" value="<%= this.name %>" readonly/>',
                    '<button id="make_mnsapi_token" class="btn btn-primary" style="margin-left: 5px;">生成密文</button>',
                '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label"><b>owt_pattern：</b></label>',
                '<div class="controls">',
                    '<textarea id = "owt_pattern_input" style="overflow: scroll;width:365px;height: 100px;"></textarea>',
                '</div>',
            '</div>',
            '<div class="control-group"><label class="control-label"><b>appkey_pattern：</b></label>',
                '<div class="controls">',
                    '<textarea id = "appkey_pattern_input" style="overflow: scroll;width:365px;height: 100px;"></textarea>',
                '</div>',
            '</div>',
        '</div>'
    ].join('');

    var http_auth_tr_template = [
        '<% Y.Array.each(this.data,function(item){ %>',
        '<tr class="http_auth_tr" data-id="<%= item.id%>" data-username="<%= item.username%>">',
        '<td><%= item.username%></td>',
        '<td><%= item.token %></td>',
        '<td><%= item.updateTime %></td>',
        '<td>',
        '<a id="view_http_auth" href="javascript:void(0);" class="http-auth-view"> &nbsp;&nbsp;<i class="fa fa-eye"></i> <span>查看</span> </a>',
        '<a id="edit_http_auth" href="javascript:void(0);" class="http-auth-edit"> &nbsp;&nbsp;<i class="fa fa-edit"></i> <span>编辑</span> </a>',
        '<a id="delete_http_auth" href="javascript:void(0);" class="http-auth-delete"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span >删除</span> </a>',
        '</td>',
        '</tr>',
        '<%});%>'
    ].join('');

    function httpAuth(app) {
        bind();
        fillHttpAuthTable('loading');
        loadHttpAuths(1);
    }

    function bind() {
        wrapper.delegate('click', function () {
            responeAddAuth(this);
        }, '.add_auth_btn');
        wrapper.delegate('click', function () {
            editAuth(this);
        }, '#edit_http_auth');
        wrapper.delegate('click', function () {
            deleteAuth(this);
        }, '#delete_http_auth');
        wrapper.delegate('click', function () {
            viewDetail(this);
        }, '#view_http_auth');
    }

    function makeConfigToken() {
        var appkey = "com.sankuai.inf.octo.mnsapi";
        var tokenVal = dialogBody.one('#username_input').get('value');
        if ('' == tokenVal) {
            Y.msgp.utils.msgpHeaderTip('error', "请填写用户名，不能为空", 3);
        } else {
            var url = '/serverOpt/' + appkey + '/config/token';
            Y.io(url, {
                method: 'get',
                data: {
                    token: tokenVal
                },
                on: {
                    success: function (id, o) {
                        var res = Y.JSON.parse(o.responseText);
                        if (res.success && null != res.data) {
                            dialogBody.one('#token_input').set('value', res.data);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', "生成密文失败，请重试", 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                    }
                }
            });
        }
    }

    function responeAddAuth(self) {
        addGroupDialog = addGroupDialog ? addGroupDialog : new Y.mt.widget.CommonDialog({
            id: 'add_auth_dialog',
            title: '增加鉴权项',
            width: 800,
            btn: {
                pass: doAddGroup,
                passName: "保存"
            }
        });
        var micro = new Y.Template();
        var html = micro.render(dialogTemplate);
        addGroupDialog.setContent(html);
        addGroupDialog.show();
        dialogBody = addGroupDialog.getBody();
        dialogBody.delegate('click', function () {
            makeConfigToken();
        }, '#make_mnsapi_token');
    }

    function doAddGroup() {
        var usernameVal = Y.Lang.trim(dialogBody.one('#username_input').get('value'));
        if (checkValidUsername(usernameVal)) {
            return true;
        }
        var tokenVal = Y.Lang.trim(dialogBody.one('#token_input').get('value'));
        if ('' == tokenVal) {
            Y.msgp.utils.msgpHeaderTip('error', "token为空，请生成token！", 3);
            return true;
        }
        var owtPatternVal = dialogBody.one('#owt_pattern_input').get('value');
        var appkeyPatternVal = dialogBody.one('#appkey_pattern_input').get('value');
        if ('' == owtPatternVal && '' == appkeyPatternVal) {
            Y.msgp.utils.msgpHeaderTip('error', "鉴权规则为空，请填写！", 3);
            return true;
        }
        var data = {
            username: usernameVal,
            token: tokenVal,
            owt_pattern: owtPatternVal,
            appkey_pattern: appkeyPatternVal
        };
        saveAuth(data);
    }

    function checkValidUsername(usernameVal) {
        if ('' == usernameVal) {
            Y.msgp.utils.msgpHeaderTip('error', "用户名为空，请填写！", 3);
            return true;
        } else {
            var usernameList = getUsernames();
            if (usernameList.checkItemIsExist(usernameVal)) {
                Y.msgp.utils.msgpHeaderTip('error', "用户名已存在！", 3);
                return true;
            }
        }
        return false;
    }

    Array.prototype.checkItemIsExist = function (x) {
        for (var i = 0; i < this.length; ++i) {
            if (this[i] === x) {
                return true;
            }
        }
        return false;
    }

    function getUsernames() {
        var usernames = [];
        $.ajax({
            type:"get",
            data: null,
            url: "/manage/agent/httpAuth/usernames",
            async: false,
            success:function(ret){
                if (ret.isSuccess && ret.data) {
                    usernames = ret.data;
                    return ret.data;
                }
            },
            failure: function(ret){
                Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
            }
        });
        return usernames;
    }


    function saveAuth(data) {
        var url = '/manage/agent/addAuthItem';
        myIO(url, "POST", data, suc, fail);
        function suc(ret) {
            if (ret.isSuccess) {
                Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                loadHttpAuths(1);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
            }
        }

        function fail() {
            Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
        }
    }

    function myIO(url, method, data, suc, fail) {
        if ('POST' == method) data = Y.JSON.stringify(data);
        Y.io(url, {
            method: method,
            data: data,
            on: {
                success: function (id, o) {
                    suc(Y.JSON.parse(o.responseText));
                },
                failure: function () {
                    fail();
                }
            }
        });
    }

    function fillHttpAuthTable(type, data) {
        var loading = wrapper.one('#board_loading');
        var error_tr = wrapper.one('#board_error_data');
        var tbody = wrapper.one('#http_auth_board').one('tbody');
        var navigation = wrapper.one('#page_navigation_http_auth');
        tbody.all('.http_auth_tr').remove();
        error_tr.hide();
        loading.hide();
        navigation.hide();
        switch (type) {
            case 'error':
                error_tr.show();
                error_tr.one('td').set('text', data);
                break;
            case 'loading':
                loading.show();
                break;
            case 'data':
                if (data.length <= 0) {
                    fillHttpAuthTable('error', '无内容');
                    return;
                }
                var micro = new Y.Template();
                var html = micro.render(http_auth_tr_template, {
                    data: data
                });
                tbody.append(html);
                navigation.show();
                break;
        }
    }

    function loadHttpAuths(pageNo) {
        var url = '/manage/agent/httpAuths';
        var data = {
            pageNo: pageNo
        };
        myIO(url, 'GET', data, suc, fail);
        function suc(ret) {
            if (ret.isSuccess) {
                fillHttpAuthTable('data', ret.data);
                refreshPageNavigation(ret.page);
            } else {
                fillHttpAuthTable('error', ret.msg || '内部异常');
            }
        }

        function fail() {
            fillHttpAuthTable('error', "内部异常");
        }
    }

    function refreshPageNavigation(ret_page) {
        new Y.mt.widget.Paginator({
            contentBox: wrapper.one('#page_navigation_http_auth'),
            index: ret_page.pageNo,
            max: ret_page.totalPageCount,
            pageSize: ret_page.pageSize,
            totalCount: ret_page.totalCount,
            callback: httpAuthChangePage
        });
        function httpAuthChangePage(arg) {
            loadHttpAuths(arg.page)
        }
    }

    function editAuth(self) {
        var tr = self.ancestor('tr');
        var authID = tr.getData('id');
        var url = '/manage/agent/httpAuth';
        var data = {
            authID: authID
        };
        myIO(url, "GET", data, suc, fail);
        function suc(ret) {
            if (ret.isSuccess) {
                var data = ret.data;
                editAuthDialog = editAuthDialog ? editAuthDialog : new Y.mt.widget.CommonDialog({
                    id: 'edit_auth_dialog',
                    title: '编辑权限',
                    width: 800,
                    btn: {
                        pass: doEditAuth,
                        passName: "保存"
                    }
                });

                var micro = new Y.Template();
                var html = micro.render(dialogTemplate);
                editAuthDialog.setContent(html);
                editAuthDialog.show();
                var dialogBody = editAuthDialog.getBody();
                dialogBody.one('#username_input').set('value', data.username);
                dialogBody.one('#username_input').setAttribute('disabled');
                dialogBody.one('#token_input').set('value', data.token);
                dialogBody.one('#token_input').setAttribute('disabled');
                dialogBody.one('#make_mnsapi_token').hide();
                dialogBody.one('#owt_pattern_input').set('value', data.owt_pattern);
                dialogBody.one('#appkey_pattern_input').set('value', data.appkey_pattern);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
            }
        }

        function fail() {
            Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
        }
    }

    function doEditAuth() {
        var dialogBody = editAuthDialog.getBody();
        var usernameVal = dialogBody.one('#username_input').get('value');
        var tokenVal = dialogBody.one('#token_input').get('value');
        var owtPatternVal = dialogBody.one('#owt_pattern_input').get('value');
        var appkeyPatternVal = dialogBody.one('#appkey_pattern_input').get('value');
        if ('' == owtPatternVal && '' == appkeyPatternVal) {
            Y.msgp.utils.msgpHeaderTip('error', "鉴权规则为空，请填写！", 3);
            return true;
        }
        var data = {
            username: usernameVal,
            token: tokenVal,
            owt_pattern: owtPatternVal,
            appkey_pattern: appkeyPatternVal
        };
        saveAuth(data);
    }

    function deleteAuth(self) {
        var tr = self.ancestor('tr');
        var authID = tr.getData('id');
        var username = tr.getData("username");
        deleteAuthDialog = deleteAuthDialog ? deleteAuthDialog : new Y.mt.widget.CommonDialog({
            id: 'delete_auth_dialog',
            title: '删除权限',
            width: 300,
            btn: {
                pass: doDeleteAuth,
                passName: "保存"
            }
        });

        deleteAuthDialog.setContent("确定删除用户名为" + username + "的权限吗？");
        deleteAuthDialog.text = authID;
        deleteAuthDialog.show();
    }

    function doDeleteAuth() {
        var authID = deleteAuthDialog.text;
        var data = {
            authID: authID
        };
        var url = '/manage/agent/httpAuth';
        myIO(url, "DELETE", data, suc, fail);
        function suc(ret) {
            if (ret.isSuccess) {
                Y.msgp.utils.msgpHeaderTip('success', "删除成功", 3);
                loadHttpAuths(1);
            } else {
                Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
            }
        }

        function fail() {
            Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
        }
    }

    function viewDetail(self) {
        var tr = self.ancestor('tr');
        var authID = tr.getData('id');
        var url = '/manage/agent/httpAuth';
        var data = {
            authID: authID
        };
        myIO(url, "GET", data, suc, fail);
        function suc(ret) {
            if (ret.isSuccess) {
                var data = ret.data;
                detailDialog = detailDialog ? detailDialog : new Y.mt.widget.CommonDialog({
                    id: 'view_auth_dialog',
                    title: '查看权限',
                    width: 800,
                });

                var micro = new Y.Template();
                var html = micro.render(dialogTemplate);
                detailDialog.setContent(html);
                detailDialog.show();
                var dialogBody = detailDialog .getBody();
                dialogBody.one('#username_input').set('value', data.username);
                dialogBody.one('#username_input').setAttribute('disabled');
                dialogBody.one('#token_input').set('value', data.token);
                dialogBody.one('#token_input').setAttribute('disabled');
                dialogBody.one('#make_mnsapi_token').hide();
                dialogBody.one('#owt_pattern_input').set('value', data.owt_pattern);
                dialogBody.one('#owt_pattern_input').setAttribute('disabled');
                dialogBody.one('#appkey_pattern_input').set('value', data.appkey_pattern);
                dialogBody.one('#appkey_pattern_input').setAttribute('disabled');
            } else {
                Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
            }
        }

        function fail() {
            Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
        }
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'w-base',
        'transition',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/checkIP',
        'msgp-service/commonMap'
    ]
});