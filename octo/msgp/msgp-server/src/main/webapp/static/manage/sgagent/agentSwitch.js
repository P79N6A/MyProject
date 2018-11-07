/**
 * Created by lhmily on 06/08/2016.
 */
M.add('msgp-manage/sgagent/agentSwitch', function (Y) {
        Y.namespace('msgp.manage').agentSwitch = agentSwitch;
        var wrapper = Y.one('#wrap_agentSwitch');
        var templateSwitchStr = [
            '<tr class="tr_switch">',
            '<%var nullItemNum=0; Y.Array.each(this.data, function(item, index){ %>',
            '<%if(null==item||""==item){++nullItemNum;}else{%>',
            '<td style="width: 20%;">',
            '<input type="radio" name="switch-radio" style="margin-bottom: 3px;" value="<%= item%>"/> &nbsp;<%= item%>',
            '</td>',
            '<% }}); %>',
            '<% for(var i=0;i < 5 - this.data.length-nullItemNum; ++i){%>',
            '<td></td>',
            '<% } %>',
            '</tr>'
        ].join('');

        var templateStr = [
            '<tr class="tr_machine_node">',
            '<%var nullItemNum=0; Y.Array.each(this.data, function(item, index){ %>',
            '<%if(null==item||""==item){++nullItemNum;}else{%>',
            '<td style="width: 20%;">',
            '<input type="checkbox" id="ip-one-check" style="margin-bottom: 3px;" value="<%= item.ip%>"/> &nbsp;<%= item.name %>(<%= item.ip %>)',
            '</td>',
            '<% }}); %>',
            '<% for(var i=0;i < 5 - this.data.length-nullItemNum; ++i){%>',
            '<td></td>',
            '<% } %>',
            '</tr>'
        ].join('');

        var nodeTable = wrapper.one("#table_supplier");
        var switchTable = wrapper.one("#table_switch");

        var nodeTableTbody = nodeTable.one('tbody');
        var switchTbody = switchTable.one('tbody');
        var logSpan = '<span>[<font color="{color}">{msgType}</font>][{time}]:{log_content}<br/></span>';


        var colspan = 10;
        var currentSupplierAjax, isAbort = false;

        var currentEnv = 3;
        var dialog, isDialogOpen = false;
        var log_content_div = wrapper.one("#log-content");

        function agentSwitch() {
            bind();
            getSwitch();
            wrapper.one('#content_overlay').hide();
            wrapper.one('#supplier_env_select a').simulate('click');
           // Y.msgp.service.setEnvText('supplier_env_select');
        }

        function bind() {
            bindClickCheckBox();
            wrapper.delegate('click', function () {
                this.ancestor('div').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                currentEnv = Number(this.getAttribute('value'));
                getSupplier();
            }, '#supplier_env_select a');

            wrapper.delegate('keyup', function (e) {
                if (e.keyCode === 13) {
                    getSupplier();
                }
            }, '#searchBox');

            wrapper.delegate('click', function () {
                getSupplier();
            }, '#searchBtn');

            wrapper.delegate('click', function () {
                var switchNum = Number(this.getAttribute("value"));
                handleSGAgentOperate(1 === switchNum);
            }, '#sgagent_operate button');
        }

        function handleSGAgentOperate(isOpen) {
            var selected_Ips = nodeTable.all('#ip-one-check:checked'),
                selected_switch = switchTable.one('input:checked');
            if (null == selected_switch) {
                local_error_alert('请选择开关类型');
                return;
            }
            var switchname = selected_switch.getAttribute("value");
            if (selected_Ips.isEmpty()) {
                var curIp = wrapper.one('#searchBox').get('value');
                if (!check_input_valid(curIp)) return;
                doSGAgentOperation(isOpen, switchname, [curIp]);
                return;
            }
            if (selected_Ips.size() > 50) {
                Y.msgp.utils.msgpHeaderTip('warn', "请注意,建议每次选择的主机不超过50台", 3);
            }
            var ips = [];
            selected_Ips.each(function (nd) {
                ips.push(nd.getAttribute("value"));
            });

            doSGAgentOperation(isOpen, switchname, ips);
        }

        function ipsSplit(ips) {
            var ret = []
            var colspan = 20;
            var row_num = ips.length / colspan;

            for (var i = 0; i < row_num; ++i) {
                var sub = ips.slice(colspan * i, colspan * i + colspan);
                ret.push(sub);
            }
            return ret;
        }
        function sleep(n) {
            var start = new Date().getTime();
            while(true)  if(new Date().getTime()-start > n) break;
        }

        function doSGAgentOperation(isOpen, switchname, ips) {
            showDialog("正在处理中...");
            var ipArr = ipsSplit(ips);
            ipArr.forEach(function (currentIps){
                var data = {
                    isOpen: isOpen,
                    switchName: switchname,
                    ips: currentIps
                }
                var url = "/manage/agent/switchsgagent";
                Y.io(url, {
                    method: 'POST',
                    headers : {'Content-Type':"application/json;charset=UTF-8"},
                    data: Y.JSON.stringify(data),
                    on: {
                        success: function (id, o) {
                            if (!isDialogOpen)return;
                            hideDialog();

                            var ret = Y.JSON.parse(o.responseText);
                            if (ret.isSuccess) {
                                var successList = ret.data.successList;
                                var failureList = ret.data.failureList;
                                var successIPs = [];
                                successList.forEach(function (item) {
                                    successIPs.push(item.ip);
                                });
                                if (successList.length > 0)
                                    printLog("INFO", switchname + (isOpen ? "开启成功" : "关闭成功") + ": " + successIPs.join(","));
                                if (failureList.length > 0) {
                                    printLog("ERROR", switchname + (isOpen ? "开启失败" : "关闭失败") + ": ");

                                    failureList.forEach(function (item) {
                                        log_content_div.append("ip:" + item.ip + ",errcode:" + item.errcode + ",msg:" + item.msg + "<br/>");
                                    });
                                }
                            } else {
                                local_error_alert(ret.msg);
                            }
                        },
                        failure: function (id, o) {
                            hideDialog();
                            local_error_alert("系统出错");
                        }
                    }
                });
            });
        }

        function getSwitch() {
            showContent(false, 'loading');
            var url = '/manage/agent/switchname';
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            var colspan = 5;
                            var row_num = data.length / colspan;
                            var html = [];
                            var micro = new Y.Template();
                            for (var i = 0; i < row_num; ++i) {
                                var sub = data.slice(colspan * i, colspan * i + colspan);
                                var trHtml = micro.render(templateSwitchStr, {data: sub});
                                html.push(trHtml);
                            }
                            showContent(false, 'data', html.join(''));
                        } else {
                            emptyOrError(true);
                        }
                    },
                    failure: function () {
                        if (!isAbort)
                            emptyOrError(true);
                    }
                }
            });
        }


        function getSupplier() {
            showContent(true, 'loading');
            var url = '/manage/agent/provider';
            isAbort = false;
            if ('undefined' != typeof(currentSupplierAjax)) {
                isAbort = true;
                currentSupplierAjax.abort();
            }
            currentSupplierAjax = Y.io(url, {
                method: 'get',
                data: {
                    env: currentEnv,
                    keyword: wrapper.one('#searchBox').get('value')
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            if (Y.Lang.isArray(data) && data.length !== 0) {
                                nodeTable.one("#ip-sum").set('text', "共" + data.length + "节点");
                                var colspan = 5;
                                var row_num = data.length / colspan;
                                var html = [];
                                var micro = new Y.Template();
                                for (var i = 0; i < row_num; ++i) {
                                    var sub = data.slice(colspan * i, colspan * i + colspan);
                                    var trHtml = micro.render(templateStr, {data: sub});
                                    html.push(trHtml);
                                }
                                showContent(true, 'data', html.join(''));
                            } else if (data.length === 0) {
                                emptyOrError();
                            }
                        } else {
                            emptyOrError(true);
                        }
                    },
                    failure: function () {
                        if (!isAbort)
                            emptyOrError(true);
                    }
                }
            });
        }

        function emptyOrError(isError) {
            var html = '<tr class="supplier_error"><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
            showContent(true, 'error', html);
        }

        function showContent(isNodeTable, type, data) {
            if (isNodeTable) {
                var supplier_error = nodeTableTbody.one('.supplier_error');
                if (null != supplier_error) {
                    supplier_error.remove();
                }
                nodeTableTbody.one('#content_overlay').hide();
                switch (type) {
                    case 'error':
                        nodeTableTbody.append(data);
                        break;
                    case 'loading':
                        nodeTableTbody.all('.tr_machine_node').remove();
                        nodeTableTbody.one('#content_overlay').show();
                        break;
                    case 'data':
                        nodeTableTbody.append(data);
                        break;
                }
            } else {
                var switch_error = switchTable.one('.switch_error');
                if (null != switch_error) {
                    switch_error.remove();
                }
                switchTbody.one('#switch_content_overlay').hide();
                switch (type) {
                    case 'error':
                        switchTbody.append(data);
                        break;
                    case 'loading':
                        switchTbody.all('.tr_swtich').remove();
                        switchTbody.one('#switch_content_overlay').show();
                        break;
                    case 'data':
                        switchTbody.append(data);
                        break;
                }
            }

        }

        function bindClickCheckBox() {
            //全选
            nodeTable.delegate('click', function () {
                nodeTable.all('#ip-one-check').set("checked", true);
            }, '#ip-all-check');
            nodeTable.delegate('click', function () {
                nodeTable.all('#ip-one-check').each(function (nd) {
                    var isChecked = !this.get('checked');
                    nd.set("checked", isChecked);
                });
            }, '#ip-all-uncheck');
        }

        function showDialog(msg) {
            dialog = dialog ? dialog : new Y.mt.widget.CommonDialog({
                width: 400,
                title: '加载数据',
                content: '<i class="fa fa-spinner fa-spin text-blue mr10"></i>' + msg,
                closeCallback: function () {
                    isOpen = false;
                }
            });
            dialog.show();
            isDialogOpen = true;
        }

        function hideDialog() {
            dialog.close();
        }

        /**
         * 日志打印函数
         * @param msgType
         * @param logMsg
         */
        function printLog(msgType, logMsg) {
            var dataTime = new Date();
            var log_content = [];
            var params = {
                color: ("error" == msgType.toLowerCase().toString()) ? "red" : "blue",
                msgType: msgType.toUpperCase(),
                time: dataTime.toString(),
                log_content: logMsg
            }
            log_content.push(Y.Lang.sub(logSpan, params));
            var template = log_content.join('');
            log_content_div.append(template);
        }

        function local_error_alert(msg) {
            Y.msgp.utils.msgpHeaderTip('error', msg, 3);
        }

        function check_input_valid(ip_value) {
            if (!Y.msgp.utils.checkIP(ip_value)) {
                local_error_alert('请输入合法的\"IP地址\"');
                return false;
            }
            return true;
        }
    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'template',
            'msgp-utils/check',
            'msgp-utils/common',
            'msgp-service/commonMap',
        ]
    }
);