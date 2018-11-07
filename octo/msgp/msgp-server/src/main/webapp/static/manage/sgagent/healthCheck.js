/**
 * Created by lhmily on 04/13/2016.
 */
M.add('msgp-manage/sgagent/healthCheck', function (Y) {
    Y.namespace('msgp.manage').healthCheck = healthCheck;

    // define variable
    var wrapper = Y.one('#wrap_healthCheck'),
        search_ip = wrapper.one('#healthcheck_ip'),
        search_button = wrapper.one('#searchIP'),
        restart_button = wrapper.one('#restart'),
        tbody = wrapper.one('#health_check_data tbody');

    //
    function healthCheck() {
        tbody = wrapper.one("#health_check_data table tbody");
        search_ip.on('keyup', searchOnkeyup);
        search_button.on('click', function () {
            var ip = Y.Lang.trim(search_ip.get('value'));
            if (!ip) {
                Y.msgp.utils.msgpHeaderTip('info', '请输入IP地址或主机名', 3);
                return;
            }
            setLoading();
            getData();
        });
        restart_button.on('click', function () {
            var ip = Y.Lang.trim(search_ip.get('value'));
            if (!ip) {
                Y.msgp.utils.msgpHeaderTip('info', '请输入IP地址或主机名', 3);
                return;
            }
            restartAgent();
        });
    }

    function searchOnkeyup(e) {
        if (e.keyCode === 13) {
            var ip = Y.Lang.trim(search_ip.get('value'));
            if (!ip) {
                Y.msgp.utils.msgpHeaderTip('info', '请输入IP地址或主机名', 3);
                return;
            }
            setLoading();
            getData();
        }
    }

    function setLoading() {
        var loading = '<tr class="content-overlay"><td colspan="2"><i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span></td></tr>';
        tbody.setHTML(loading);
    }

    function getData() {
        var ip = Y.Lang.trim(search_ip.get('value'));
        var url = '/manage/agent/healthcheck';
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var res = '';
                    if (ret.isSuccess) {
                        var data = ret.data;
                        try {
                            data = Y.JSON.parse(data);
                            var keys = Object.keys(data);
                            keys.sort();
                            keys.forEach(function (item) {
                                var item_data = data[item];
                                if (item == "installed") {
                                    var is_installed = false;
                                    if (item_data != 'sg_agent sg_agent_worker' && item_data != 'sg_agent_worker sg_agent') {
                                        is_installed = true;
                                        item_data += '&nbsp;&nbsp;&nbsp;<button class="btn btn-default" type="button" id="install">安装sggent</button>'
                                    }

                                    if (!is_installed  && (false == data['file_integrity'] || "Fail" == data['getConfig'] || "Fail" == data['getServiceList'])) {
                                        item_data += '&nbsp;&nbsp;&nbsp;<button class="btn btn-default" type="button" id="reinstall">重新安装sggent</button>'
                                    }
                                }
                                res += "<tr><td>" + item + "</td><td>" + item_data + "</td></tr>";
                            });
                            tbody.setHTML(res);
                            bindAgentStall();
                        } catch (err) {
                            var html = '<tr><td colspan="2">获取失败：没有内容</td></tr>';
                            tbody.setHTML(html);
                        }

                    } else {
                        var html = '<tr><td colspan="2">获取失败：没有内容</td></tr>';
                        tbody.setHTML(html);
                    }
                }
            }
        });
    }

    function bindAgentStall(){
        if(tbody.one("#install")){
            tbody.one("#install").on('click', function () {
                var ip = Y.Lang.trim(search_ip.get('value'));
                if (!ip) {
                    Y.msgp.utils.msgpHeaderTip('info', '请输入IP地址或主机名', 3);
                    return;
                }
                installAgent();
            });
        }

        if(tbody.one("#reinstall")){
            tbody.one("#reinstall").on('click', function () {
                var ip = Y.Lang.trim(search_ip.get('value'));
                if (!ip) {
                    Y.msgp.utils.msgpHeaderTip('info', '请输入IP地址或主机名', 3);
                    return;
                }
                reInstallAgent();
            });
        }
    }

    function restartAgent() {
        var ip = Y.Lang.trim(search_ip.get('value'));
        var url = '/manage/agent/restart';
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        //刷新界面
                        search_button.simulate("click")
                    } else {
                        var html = '<tr><td colspan="2">重启失败</td></tr>';
                        tbody.setHTML(html);
                    }
                }
            }
        });
    }

    function installAgent() {
        var ip = Y.Lang.trim(search_ip.get('value'));
        var url = '/manage/agent/install';
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        //刷新界面
                        search_button.simulate("click")
                    } else {
                        var html = '<tr><td colspan="2">安装失败</td></tr>';
                        tbody.setHTML(html);
                    }
                }
            }
        });
    }

    function reInstallAgent() {
        var ip = Y.Lang.trim(search_ip.get('value'));
        var url = '/manage/agent/reinstall';
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        //刷新界面
                        search_button.simulate("click")
                    } else {
                        var html = '<tr><td colspan="2">重启安装</td></tr>';
                        tbody.setHTML(html);
                    }
                }
            }
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
        'msgp-service/commonMap'
    ]
});