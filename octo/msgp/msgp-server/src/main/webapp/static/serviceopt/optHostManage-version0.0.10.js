/**
 * Created by lhmily on 12/29/2015.
 */
M.add('msgp-serviceopt/optHostManage-version0.0.10', function (Y) {

    Y.namespace('msgp.serviceopt').optHostManage = optHostManage;
    var wrapper = Y.one('#wrap_hostManage');
    var checkSearchIP;
    var env_prod, env_stage, env_test;
    var newEnv = 'prod';
    var defaultPage = {
        page: 1,
        pageSize: 20
    }
    var currentAccessAjax;
    var currentPage = 1;
    var checkIP;
    var errlogLoading, errlogContent;
    var sgagentBtn;
    var data;

    var add_item_template = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-id=<%= item.id%> id="switchEnv_tr">',
        '<td><%= item.ip%>',
        '</td>',
        '<td><%= item.oldEnv%>',
        '</td>',
        '<td><%= item.newEnv%>',
        '</td>',
        '<td><%= Y.mt.date.formatDateByString( new Date(item.applyTime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '</td>',
        '<td class="<%=item.flagClass %>"><%= item.flagDesc%>',
        '</td>',
        '<td><%= (null!=item.comfirmMisid)?item.comfirmMisid:""%>',
        '</td>',
        '<td><%= (null!=item.comfirmTime)?Y.mt.date.formatDateByString( new Date(item.comfirmTime), "yyyy-MM-dd hh:mm:ss" ):"" %></td>',
        '</td>',
        '<td><%if(null==item.comfirmMisid){%>',
        '<a id="deleteItem" href="javascript:void(0);" class="config-panel-delete"><i class="fa fa-trash-o"></i> <span>删除</span> </a>',
        '<% }%>',
        '</td>',
        '<td><%= (null!=item.note)?item.note:""%>',
        '</td>',
        '</tr>',
        '<% }); %>'
    ].join('');


    function optHostManage(key) {
        bind();
        initCheck();

        /***************环境切换*****************/
        env_prod=wrapper.one('#env_prod');
        env_stage=wrapper.one('#env_stage');
        env_test=wrapper.one('#env_test');
        wrapper.one('.typeBtn').simulate('click');

        env_prod.on('click', clickEnv);
        env_stage.on('click', clickEnv);
        env_test.on('click', clickEnv);
        fillTable(defaultPage);
        bindaddApply();
        bindDelete();
        Y.msgp.service.setEnvText('switch_env_select');
        /**************************************/
    }


    function clickEnv() {
        if (this.hasClass("btn-primary"))return;
        wrapper.one('#switch_env_select').all('a').removeClass('btn-primary');
        this.addClass('btn-primary');
        newEnv = this.getAttribute('value');
    }

    function bind() {
        wrapper.one("#detailinfo_div").hide();
        wrapper.one("#detail_div").hide();

        wrapper.delegate('click', function () {
            this.ancestor('div').all('a').removeClass('btn-primary');
            this.addClass('btn-primary');
            showPage(this.getAttribute('value'));
            console.log(this.getAttribute('value'));
        }, '.typeBtn');

        wrapper.delegate('keyup', function (e) {
            if (e.keyCode === 13) {
                wrapper.one('#hostInfoSearch').simulate('click');
            }
        }, '#hostInfoSearchIP');

        wrapper.delegate('keyup', function (e) {
            if (e.keyCode === 13) {
                wrapper.one('#addApply').simulate('click');
            }
        }, '#switchEnv_ip');

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

        checkIP = Y.msgp.utils.check.init(wrapper.one('#switchEnv_ip'), {
            type: 'custom',
            customRegExp: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
            warnMsg: "IP非法",
            warnElement: wrapper.one('#checkIPTips')
        });
    }

    function showPage(page) {
        wrapper.all('.hostInfo_menu').hide();
        wrapper.all('.switchEnv_menu').hide();
        switch (page) {
            case 'hostInfo':
                showHostInfoContent("init");
                wrapper.all('.hostInfo_menu').show();
                break;
            case 'switchEnv':
                wrapper.all('.switchEnv_menu').show();
                break;
        }
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
                wrapper.one("#monitor_res_content").set("text", data.monitorRes);

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
                break;
            case "data":
                if ("一切正常" != data.result){
                    checkinfoContent.setStyle("color", "brown");
                }
                checkinfoContent.set("text", data.result);
                checkinfoContent.show();
                wrapper.one("#detailinfo_div").show();
                detailHide();
                break;
            case "init":
                checkinfoContent.set("text", "无");
                checkinfoContent.setStyle("color", "black");
                checkinfoContent.show();
                wrapper.one("#detailinfo_div").hide();
                wrapper.one("#detail_div").hide();
                break;
        }

    }



    function showContent(flag, data) {
        var loading = wrapper.one('#delay_content');
        var error = wrapper.one('#error_msg');
        wrapper.all('#switchEnv_tr').remove();
        wrapper.one('#paginator_switchEnv').hide();
        loading.hide();
        error.hide();
        switch (flag) {
            case 'loading':
                loading.show();
                break;
            case 'error':
                error.show();
                error.one('span').set('text', data);
                break;
            case 'data':
                break;
        }
    }

    function bindDelete() {
        wrapper.delegate('click', function () {
            var tr = this.ancestor('tr');
            var id = tr.getData('id');
            var url = "/manage/agent/switchEnv/switchEnv";
            showContent('loading');
            Y.io(url, {
                method: 'DELETE',
                data: {
                    id: id
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', ret.data, 3);
                        } else {
                            local_error_alert(ret.msg);
                        }
                        showContent('data');
                        fillTable({
                            page: currentPage,
                            pageSize: 20
                        });
                    },
                    failure: function (id, o) {
                        showContent('error', '服务器异常')
                    }
                }
            });
        }, '#deleteItem');
    }

    function bindaddApply() {
        wrapper.delegate('click', function () {
            if (!checkIP.isValid()) {
                checkIP.showMsg();
                return;
            }
            var ip = Y.Lang.trim(wrapper.one('#switchEnv_ip').get('value'));
            var url = '/manage/agent/switchEnv/applySwitchEnv';
            showContent('loading');
            Y.io(url, {
                method: 'GET',
                data: {
                    ip: ip,
                    newEnv: newEnv
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', ret.data, 5);
                        } else {
                            local_error_alert(ret.msg);
                        }
                        showContent('data');
                        fillTable(defaultPage);
                    },
                    failure: function (id, o) {
                        showContent('error', '服务器异常，请重试');
                    }
                }
            });
        }, '#addApply');
    }

    function fillTable(page) {
        currentPage = page.page;
        var url = '/manage/agent/switchEnv/applyListByUser';
        var tbody = wrapper.one('#table_switchEnv').one('tbody');
        showContent('loading');
        if (typeof(currentAccessAjax) != "undefined") {
            currentAccessAjax.abort();
        }
        currentAccessAjax = Y.io(url, {
            method: 'GET',
            data: {
                pageNo: page.page,
                pageSize: page.pageSize
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var ret_data = ret.data;
                        if (0 == ret_data.length) {
                            showContent('error', '无内容');
                            return;
                        }
                        showContent('data');
                        for (var i = 0, l = ret_data.length; i < l; i++) {
                            var tmp = ret_data[i];
                            tmp.flagDesc = (0 == tmp.flag) ? "未审核" : ((1 == tmp.flag) ? "已审核" : "不通过");
                            switch (tmp.flag) {
                                case 0:
                                    tmp.flagClass = "status-4";
                                    break;
                                case 1:
                                    tmp.flagClass = "status-2";
                                    break;
                                case 2:
                                    tmp.flagClass = "status-0"
                                    break;
                            }
                        }
                        var ret_page = ret.page;
                        var micro = new Y.Template();
                        var html = micro.render(add_item_template, {data: ret_data});
                        tbody.append(html);
                        new Y.mt.widget.Paginator({
                            contentBox: service_pbody,
                            index: ret_page.pageNo,
                            max: ret_page.totalPageCount,
                            totalCount: ret_page.totalCount,
                            pageSize: ret_page.pageSize,
                            callback: fillTable
                        });
                        wrapper.one('#paginator_switchEnv').show();
                    }
                },
                failure: function (id, o) {
                    if ('abort' != o.statusText) {
                        showContent('error', '服务器异常');
                    }
                }
            }
        });
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
        'msgp-service/commonMap'
    ]
});