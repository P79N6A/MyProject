M.add('msgp-oauth2/authorize', function (Y) {

    Y.namespace('msgp.oauth2').authorize = authorize;

    var response_type,
        client_id,
        redirect_uri,
        bind_button = Y.one('#bind_button'),
        autoCompleteWidget;

    function authorize(a, b, c, list) {
        response_type = a;
        client_id = b;
        redirect_uri = c;

        var obj = [];
        for (var i = 0, l = list.length; i < l; i++) {
            obj.push({id: i, name: list[i]});
        }

        if (obj.length && Y.one('#apps_select') != null) {
            AutoCompleteList(obj);
        }

        bindEvent();
    }

    function AutoCompleteList(obj) {
        autoCompleteWidget = new Y.mt.widget.AutoCompleteList({
            id: "apps_select_auto",
            node: Y.one("#apps_select"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            more: "",
            showCheckbox: true,
            showCheckedInList: true
        });
        Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "400px");
        Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("overflow", "auto");
        Y.one("#apps_select_auto").one(".widget-autocomplete-tip").setHTML("输入服务名搜索");
        var node1 = Y.one("#apps_select_auto").one(".widget-autocomplete-list");
        Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").append(node1);
    }

    function bindEvent() {
        if (bind_button) {
            bind_button.on('click', doCallback);
        }
    }

    function doCallback() {
        var obj = checkDialogUl();
        if (obj) {
            getOutline(obj);
        }
    }

    function checkDialogUl() {
        var obj = new Array();
        var appkeyChecked = autoCompleteWidget.getCheckedNodes();
        console.log(appkeyChecked);
        if (appkeyChecked.length === 0) {
            Y.msgp.utils.msgpHeaderTip('error', '请选择至少一个appkey！', 3);
            return false;
        }
        for(var i = 0; i < appkeyChecked.length; i++) {
            console.log(appkeyChecked[i].name);
            obj.push(appkeyChecked[i].name);
        }
        return obj;
    }

    function getOutline(scope) {
        var data = Y.JSON.stringify({
            response_type: response_type,
            client_id: client_id,
            redirect_uri: redirect_uri,
            scope: scope
        });
        var url = '/oauth2/token';
        Y.io(url, {
            method: 'post',
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '授权成功', 3);
                        window.location.href = ret.data;
                    } else if (ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '授权失败，请稍后重试！', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '授权失败', 3);
                }
            }
        });
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'msgp-utils/msgpHeaderTip'
        , 'w-autocomplete'

    ]
});
M.use('msgp-oauth2/authorize', function (Y) {
    Y.msgp.oauth2.authorize(response_type, client_id, redirect_uri, list);
});