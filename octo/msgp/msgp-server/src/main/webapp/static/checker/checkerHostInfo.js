M.add('msgp-checker/checkerHostInfo', function (Y) {
    Y.namespace('msgp.checker').checkerHostInfo = HostInfo;


    var wrapper = Y.one('#wrap_checkerHostInfo');
    var checkSearchIP;

    var checkIP;
    var errlogLoading, errlogContent;
    var sgagentBtn;
    var data;
    var urlHostname;
    var hostnameUrl = document.location.href;

    String.prototype.startWith = function(str){
        if(str==null||str==""||this.length==0||str.length>this.length)
            return false;
        if(this.substr(0,str.length)==str)
            return true;
        else
            return false;
        return true;
    };

    if (hostnameUrl.indexOf("hostname") != -1) {
        urlHostname = hostnameUrl.split("hostname=")[1].split("#")[0];
        hostnameToIp(urlHostname);
    }

    function HostInfo(key, func1, func2) {
        bind();
        initCheck();
        if (typeof(urlHostname) !== "undefined") {
            if(checkIP.startWith("10.")){
                Y.one('#hostInfoSearch').simulate('click');
            }
        }else{
            showHostInfoContent("init");
        }
        wrapper.all('.hostInfo_menu').show();
    }

    function hostnameToIp(hostname) {
        var url = '/serverOpt/host/hostname2ip';
        var data = {
            hostname: hostname
        };
        Y.io(url, {
            method: 'get',
            sync: true,
            data: data,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        checkIP = ret.data;
                        if(checkIP.startWith("10.")){
                            wrapper.one('#hostInfoSearchIP').set("value", ret.data);
                        }else{
                            wrapper.one('#hostInfoSearchIP').set("value", hostname);
                            var checkinfoLoading = wrapper.one("#checkinfo_loading");
                            var checkinfoContent = wrapper.one("#checkinfo_content");
                            checkinfoLoading.hide();
                            checkinfoContent.setStyle("color", "red");
                            checkinfoContent.set("text", checkIP);
                            checkinfoContent.show();
                        }
                    } else {
                        showHostInfoContent('init');
                        local_error_alert(ret.msg);
                    }
                },
                failure: function (id, o) {
                    local_error_alert("服务器异常");
                }
            }
        });
    }

    function bind() {
        wrapper.one("#detailinfo_div").hide();
        wrapper.one("#detail_div").hide();

        wrapper.delegate('keyup', function (e) {
            if (e.keyCode === 13) {
                wrapper.one('#hostInfoSearch').simulate('click');
            }
        }, '#hostInfoSearchIP');

        wrapper.delegate('click', function () {
            if (!checkSearchIP.isValid()) {
                checkSearchIP.showMsg();
                return;
            }
            showHostInfoContent("loading");
            var url = "/serverOpt/host/hostInfo";
            var ip = Y.Lang.trim(wrapper.one('#hostInfoSearchIP').get('value'));
            Y.io(url, {
                method: 'GET',
                data: {
                    ip: ip
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            showHostInfoContent("data", ret.data);
                            data = ret.data;
                        } else {
                            showHostInfoContent('init');
                            local_error_alert(ret.msg);
                        }

                    },
                    failure: function (id, o) {
                        local_error_alert("服务器异常");
                    }
                }
            });

        }, '#hostInfoSearch');

        wrapper.delegate('click', function () {
            if ("" != data.errlog) {
                showDetailInfoContent('err', data);
            } else {
                showDetailInfoContent('data', data);
            }
        }, '#detailinfo');

    }

    function initCheck() {
        checkSearchIP = Y.msgp.utils.check.init(wrapper.one('#hostInfoSearchIP'), {
            type: 'custom',
            customRegExp: /^\s*(10)\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\s*$/,
            warnMsg: "IP非法",
            spaceOk:true,
            warnElement: wrapper.one('#checkHostInfoIPTips')
        });
    }



    function showDetailInfoContent(type, data) {
        detailHide();

        switch (type) {
            case "data":
                wrapper.one("#puppet_res_content").set("text", data.puppetRes);
                wrapper.one("#os_start_time_content").set("text", data.osStartTime);
                wrapper.one("#file_res_content").set("text", data.fileRes);
                wrapper.one("#sg_agent_installed_content").set("text", data.sgagentInstalled);
                wrapper.one("#os_version_content").set("text", data.osVersion);
                wrapper.one("#hostname_content").set("text", data.hostName);
                wrapper.one("#idc_content").set("text", data.idc);
                wrapper.one("#env_content").set("text", data.env);
                wrapper.one("#sg_agent_log_content").set("text", data.sgagentLog);
                wrapper.one("#rpc_res_content").set("text", data.rpcRes);
                wrapper.one("#cplugin_running_res_content").set("text", data.cpluginRunningRes);
                wrapper.one("#sgagent_running_res_content").set("text", data.sgagentRunningRes);
                wrapper.one("#sys_resource_res_content").set("text", data.sysResourceRes);
                wrapper.one("#ipvlan_res_content").set("text", data.ipVlanRes);
                $("#monitor_res_content").attr('href', data.monitorRes);
                $("#monitor_res_content").text(data.monitorRes)

                errMsgShowColor(data.errList);

                wrapper.one("#detail_div").show();
                $(".detail-content").show();
                $(".detail-label").show();
                break;
            case "err":
                wrapper.one("#hostname_content").set("text", data.hostName);
                wrapper.one("#env_content").set("text", data.env);
                wrapper.one("#idc_content").set("text", data.idc);
                errlogContent.set("text", data.errlog);
                wrapper.one("#detail_div").show();
                $(".common-content").show();
                wrapper.one("#errlog_div").show();
                errlogContent.show();
                $(".detail-label").hide();
                break;
        }
    }

    function detailHide() {
        errlogLoading = wrapper.one("#errlog_loading");
        errlogContent = wrapper.one("#errlog_content");

        sgagentBtn = wrapper.one("#install_sggent");
        sgagentBtn.hide();

        $(".detail-content").hide();
        $('.detail-loading').hide();

        wrapper.one("#errlog_div").hide();
        errlogLoading.hide();
        errlogContent.hide();
    }

    function showHostInfoContent(type, data) {
        var checkinfoLoading = wrapper.one("#checkinfo_loading");
        var checkinfoContent = wrapper.one("#checkinfo_content");
        checkinfoLoading.hide();
        checkinfoContent.hide();

        switch (type) {
            case "loading":
                checkinfoLoading.show();
                wrapper.one("#detailinfo_div").hide();
                wrapper.one("#detail_div").hide();
                initErrMsgColor();
                break;
            case "data":
                if ("一切正常" != data.result){
                    checkinfoContent.setStyle("color", "red");
                }
                checkinfoContent.set("text", data.result);
                checkinfoContent.show();
                wrapper.one("#detailinfo_div").show();
                detailHide();
                break;
            case "init":
                checkinfoContent.set("text", "无");
                checkinfoContent.setStyle("color", "black");
                wrapper.one("#puppet_res_content").setStyle("color", "black");
                wrapper.one("#file_res_content").setStyle("color", "black");
                wrapper.one("#sg_agent_installed_content").setStyle("color", "black");
                wrapper.one("#env_content").setStyle("color", "black");
                wrapper.one("#rpc_res_content").setStyle("color", "black");
                checkinfoContent.show();
                wrapper.one("#detailinfo_div").hide();
                wrapper.one("#detail_div").hide();
                break;
        }

    }

    function errMsgShowColor(errList) {
        errList.forEach(function (item) {
            wrapper.one("#" + item + "_content").setStyle("color", "red");
        });
    }

    function initErrMsgColor() {
        wrapper.one("#checkinfo_content").setStyle("color", "black");
        wrapper.one("#puppet_res_content").setStyle("color", "black");
        wrapper.one("#file_res_content").setStyle("color", "black");
        wrapper.one("#sg_agent_installed_content").setStyle("color", "black");
        wrapper.one("#env_content").setStyle("color", "black");
        wrapper.one("#rpc_res_content").setStyle("color", "black");
        wrapper.one("#sys_resource_res_content").setStyle("color", "black");
        wrapper.one("#ipvlan_res_content").setStyle("color", "black");
    }
    function local_error_alert(msg) {
        Y.msgp.utils.msgpHeaderTip('error', msg, 10);
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip',
        'msgp-service/commonMap',
        'node-event-simulate'
    ]
})

;