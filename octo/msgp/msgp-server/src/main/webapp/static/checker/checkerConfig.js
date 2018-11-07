M.add('msgp-checker/checkerConfig', function (Y) {
    Y.namespace('msgp.checker').checkerConfig = Config;

    var wrapper = Y.one('#wrap_checkerConfig');
    var search_ip = wrapper.one('#search_ip');
    var search_path = wrapper.one('#search_path');
    var search_swimlane = wrapper.one('#search_swimlane');
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

    var time = 60 * 1000; // 1分钟
    var stopTimes = 60; // 限制点击的次数
    var timeout_dynamic = undefined, count_dynamic = 0;
    var timeout_file = undefined, count_file = 0;

    function Config(key, func1, func2) {
        appkey = key;
        search_ip.on('keyup', searchOnkeyup);
        search_path.on('keyup', searchOnkeyup);
        search_swimlane.on('keyup', searchOnkeyup);
        search_button.on('click', function () {
            if (timeout_dynamic === undefined) {
                timeout_dynamic = setTimeout(function() {
                    clearTimeout(timeout_dynamic);
                    timeout_dynamic = undefined;
                    count_dynamic = 0;
                    search_button.removeAttribute('disabled')
                }, time);
            }
            getDynamicData();
            if (++count_dynamic === stopTimes) {
                search_button.setAttribute('disabled','disabled');
            }
        });

        bind();
        search_file_ip.on('keyup', searchFileOnkeyup);
        search_file_name.on('keyup', searchFileOnkeyup);
        search_file_button.on('click', function () {
            if (timeout_file === undefined) {
                timeout_file = setTimeout(function() {
                    clearTimeout(timeout_file);
                    timeout_file = undefined;
                    count_file = 0;
                    search_file_button.removeAttribute('disabled')
                }, time);
            }
            getFileData();
            if (++count_file === stopTimes) {
                search_file_button.setAttribute('disabled','disabled');
            }
        });
        wrapper.one('#dynamic_or_file a')._node.click();
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
        Y.msgp.utils.addTooltipWithContent("#search-path-desc", '使用配置分组，请填写代码里mcc初始化设置的path，否则不用填写。');
        Y.msgp.utils.addTooltipWithContent("#search-swimlane-desc", '使用泳道，请填写机器的泳道标识，否则不用填写。');
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
        var path = Y.Lang.trim(search_path.get('value'));
        var swimlane = Y.Lang.trim(search_swimlane.get('value'));
        path = ('' == path || undefined == path)? "/" : "/" + path.replace(/\./g, "/");
        if (!check_input_valid(appkey, ip)) return;

        var url = '/manage/agent/' + appkey + '/mcc/dynamicdata';
        showContent(false);
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip,
                path: path,
                swimlane: swimlane,
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var pre = dynamic_data.one('#dynamic_data_value')._node;
                    if (undefined != ret.msg) {
                        pre.innerHTML = syntaxHighlight("");
                        local_error_alert(ret.msg);
                    } else {
                        pre.innerHTML = syntaxHighlight(ret);
                    }

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

    function syntaxHighlight(json) {
        if (typeof json != 'string') {
            json = JSON.stringify(json, undefined, 2);
        }
        json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
            var cls = 'number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'key';
                } else {
                    cls = 'string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'boolean';
            } else if (/null/.test(match)) {
                cls = 'null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
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
        'msgp-service/commonMap',
        'msgp-utils/common'
    ]
})

;
