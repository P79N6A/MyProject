M.add('msgp-oauth2/registry', function (Y) {

    Y.namespace("msgp.oauth2").registry = registry;

    var ndForm = Y.one('#registryForm');
    var appkey;
    var response_type,
        client_id,
        redirect_uri;

    function registry(a, b, c) {
        response_type = a;
        client_id = b;
        redirect_uri = c;
        initFormCheck()
        initAction()
    }

    function initAction() {
        Y.one("#submit-btn").on('click', doReg);
    }

    function initFormCheck() {
        appkey = Y.msgp.utils.check.init(Y.one('#appkey'), {
            type: 'string',
            warnElement: Y.one('#appkey').next(),
            blurback: function () {
                checkAppkey(Y.one('#appkey').get('value'));
            }
        });
    }

    function checkAppkey(appkey) {
        var url = '/service/' + appkey + '/exist';
        Y.mt.io.get(url, {}, function (res) {
            if (!res.isSuccess || res.data) {
                Y.one('#appkey').setData('status', false);
                Y.one('#appkey').next().setHTML('appkey已存在！').setStyle('color', '#f00');
            }
        });
    }

    function doReg() {
        var data = patchData();
        var url = '/service/registry';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        auth()
                    } else if (ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '注册失败', 3);
                }
            }
        });
    }

    function auth() {
        var data = Y.JSON.stringify({
            response_type: response_type,
            client_id: client_id,
            redirect_uri: redirect_uri,
            scope: appkey
        });
        var url = '/oauth2/token';
        Y.io(url, {
            method: 'post',
            data: data,
            on : {
                success : function (id, o) {
                    var ret = Y.JSON.parse( o.responseText );
                    if (ret.isSuccess) {
                        //window.location.href = '/service';
                        closeWindow()
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '授权失败，请稍后重试！', 3);
                    }
                },
                failure : function(){
                        Y.msgp.utils.msgpHeaderTip('error', '授权失败' ,3);
                }
            }
        });
    }

    function closeWindow() {
        var browserName = navigator.appName;
        if (browserName == "Microsoft Internet Explorer") {
            var ie7 = (document.all && !window.opera && window.XMLHttpRequest) ? true : false;
            if (ie7) {
                window.open('', '_parent', '');
                window.close();
            }
            else {
                this.focus();
                self.opener = this;
                self.close();
            }
        } else {
            try {
                this.focus();
                self.opener = this;
                self.close();
            } catch (e) {
            }
            try {
                window.open('', '_self', '');
                window.close();
            } catch (e) {
            }
        }
    }

    function patchData() {
        appkey = ndForm.one('#appkey').get('value');
        var obj = {
            name: '',
            appkey: ndForm.one('#appkey').get('value'),
            owners: [],
            intro: ndForm.one('#intro').get('value'),
            tags: '',
            category: ndForm.one('#category_wrap input:checked').get('value'),
            business: +ndForm.one('#business_name').get('value'),
            group: ndForm.one('#business_group').get('value'),
            level: +ndForm.one('#level_wrap input:checked').get('value')
        };
        return obj;
    }
}, '1.0.0', {
    requires: [
        "mt-base",
        "mt-form",
        "mt-io",
        "node-event-simulate",
        "w-autocomplete",
        "msgp-utils/check",
        "msgp-utils/msgpHeaderTip"
    ]
});
M.use("msgp-oauth2/registry", function (Y) {
    Y.msgp.oauth2.registry(response_type, client_id, redirect_uri);
});
