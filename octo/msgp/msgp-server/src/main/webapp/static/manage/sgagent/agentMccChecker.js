/**
 * Created by lhmily on 04/13/2016.
 */
M.add('msgp-manage/sgagent/agentMccChecker', function (Y) {
    Y.namespace('msgp.manage').agentMccChecker = agentMccChecker;

    // define variable
    var wrapper = Y.one('#wrap_agentMccChecker');
    var search_ip = wrapper.one('#search_ip');
    var search_button = wrapper.one('#search_button');
    var search_file_ip = wrapper.one('#search_file_ip');
    var search_file_name = wrapper.one('#search_file_name');
    var search_file_button = wrapper.one('#search_file_button');
    var content_overlay = wrapper.one('.content-overlay');
    var dynamic_data = wrapper.one('#dynamic_data');
    var file_data = wrapper.one('#file_data');
    var dynamic_input = wrapper.one('#dynamic_input');
    var file_input = wrapper.one('#file_input');
    var appkey;

    //
    function agentMccChecker(app) {
        appkey = app;
        search_ip.on('keyup', searchOnkeyup);
        search_button.on('click', function () {
            getDynamicData();
        });
        bind();
        search_file_ip.on('keyup', searchFileOnkeyup);
        search_file_name.on('keyup', searchFileOnkeyup);
        search_file_button.on('click', function () {
            getFileData();
        });
        wrapper.one('#dynamic_or_file a').simulate('click');
    }


    function bind() {
        wrapper.delegate('click', function () {
            var type = Number(this.getAttribute("value"));
            this.ancestor('div').all('a').removeClass('btn-primary');
            dynamic_input.hide();
            file_input.hide();
            switch (type) {
                case 1:
                    this.addClass('btn-primary');
                    dynamic_input.show();
                    showContent(true, false);
                    break;
                case 2:
                    this.addClass('btn-primary');
                    file_input.show();
                    showContent(true, true);
                    break;
            }
        }, '#dynamic_or_file a');
    }

    function searchOnkeyup(e) {
        if (e.keyCode === 13) {
            getDynamicData();
        }
    }

    function searchFileOnkeyup(e) {
        if (e.keyCode === 13) {
            getFileData();
        }
    }

    function getFileData() {
        var ip = Y.Lang.trim(search_file_ip.get('value'));
        var filename = Y.Lang.trim(search_file_name.get('value'));
        if (!check_input_valid(appkey, ip)) return;
        if (filename == '') {
            local_error_alert('\"文件名\"不能为空');
            return false;
        }

        var url = '/manage/agent/' + appkey + '/mcc/filedata';
        showContent(false);
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip,
                filename: filename
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.isSuccess ? ret.data : ret.msg;
                    file_data.one('#file_data_value').set('value',data);

                    showContent(true, true);
                },
                failure: function () {
                    local_error_alert("服务器异常");
                }
            }
        });
    }

    function getDynamicData() {
        var ip = Y.Lang.trim(search_ip.get('value'));
        if (!check_input_valid(appkey, ip)) return;

        var url = '/manage/agent/' + appkey + '/mcc/dynamicdata';
        showContent(false);
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip,
                path:"/",
                swimlane: ""
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.isSuccess ? ret.data : ret.msg;
                    dynamic_data.one('#dynamic_data_value').set('value',data);
                    showContent(true, false);
                },
                failure: function () {
                    local_error_alert("服务器异常");
                }
            }
        });
    }

    function showContent(flag, isFile) {
        content_overlay.hide();
        dynamic_data.hide();
        file_data.hide();
        if (flag) {
            if (isFile) {
                file_data.show();
            } else {
                dynamic_data.show();
            }
        } else {
            content_overlay.show();
        }
    }

    function check_input_valid(appkey_value, ip_value) {
        if (appkey_value == '') {
            local_error_alert('\"Appkey\"不能为空');
            return false;
        }

        if (ip_value == '') {
            local_error_alert('\"sg_agent IP地址\"不能为空');
            return false;
        } else if (!Y.msgp.utils.checkIP(ip_value)) {
            local_error_alert('请输入合法的\"IP地址\"');
            return false;
        }

        if (ip_value == '' && appkey_value == '') {
            local_error_alert('\"Appkey\"和\"IP地址\"不能为空');
            return false;
        }
        return true;
    }

    function local_error_alert(msg) {
        Y.msgp.utils.msgpHeaderTip('error', msg, 3);
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'w-date',
        'mt-date',
        'transition',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/checkIP',
        'msgp-service/commonMap'
    ]
});