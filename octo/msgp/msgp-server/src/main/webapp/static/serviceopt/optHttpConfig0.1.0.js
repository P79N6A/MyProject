/* jshint indent : false */
M.add('msgp-serviceopt/optHttpConfig0.1.0', function(Y){
    Y.namespace('msgp.serviceopt').optHttpConfig = detailHttpConfig;

    var appkey, showOverlay, showContent;

    var wrapper = Y.one('#wrap_httpConfig');
    var healthCheck_wrap = wrapper.one('#httpConfig_healthCheck_wrap');
    var slowStart_wrap =wrapper.one('#httpConfig_slowStart_wrap')
    var domain_wrap = wrapper.one('#httpConfig_domain_wrap');
    var loadBalance_wrap = wrapper.one('#httpConfig_loadBalance_wrap');
    var httpMethodUrlTableWrap = wrapper.one('#httpMethod-list-table');
    var httpMethodUrlTableWrapTbody = httpMethodUrlTableWrap.one('tbody');
    pbody = wrapper.one('#paginator_methodUrl');

    //健康检查配置相关
    //var radioDefaultTCP = document.getElementById('radio_default_tcp');
    //var radioDefaultHTTP = document.getElementById('radio_default_http');
    var radioDefaultUniform = document.getElementById('radio_default_uniform');
    var radioCustomized = document.getElementById('radio_customized');
    var valueToRadioMapHealthCheck = {
        //"defaultTCP" : radioDefaultTCP,
        //"defaultHTTP" : radioDefaultHTTP,
        "defaultUniform" : radioDefaultUniform,
        "customized" : radioCustomized
    };

    var healthCheck_TCP_Dialog;
    var healthCheck_HTTP_Dialog;
    var healthCheck_Custom_Dialog;

    /*var textCustomizedRise = document.getElementById("text_customized_rise");
    var textCustomizedFall = document.getElementById("text_customized_fall");
    var textCustomizedInterval = document.getElementById("text_customized_interval");
    var textCustomizedTimeout = document.getElementById("text_customized_timeout");*/
    var textDefaultUniform = document.getElementById("text_default_http_send");
    var textCustomizedHttpSend = document.getElementById("text_customized_http_send");

    /*var checkboxExpectAlive2xx = document.getElementById("checkbox_expect_alive_2xx");
    var checkboxExpectAlive3xx = document.getElementById("checkbox_expect_alive_3xx");
    var checkboxExpectAlive4xx = document.getElementById("checkbox_expect_alive_4xx");
    var checkboxExpectAlive5xx = document.getElementById("checkbox_expect_alive_5xx");
    var expectAliveValueToCheckboxMap = {
        "http_2xx" : checkboxExpectAlive2xx,
        "http_3xx" : checkboxExpectAlive3xx,
        "http_4xx" : checkboxExpectAlive4xx,
        "http_5xx" : checkboxExpectAlive5xx
    };*/

    //负载均衡配置相关
    var balance_consistent_hash_wrap = wrapper.one('#balance_consistent_hash_wrap');
    var radioBalanceWRR = document.getElementById('radio_balance_wrr');
    var radioBalanceIpHash = document.getElementById('radio_balance_ip_hash');
    var radioBalanceLeastConn = document.getElementById('radio_balance_least_conn');
    var radioBalanceIDCOptimize = document.getElementById('radio_balance_idc_optimize');
    var radioBalanceReadWriteOptimize = document.getElementById('radio_balance_read_write_optimize');
    var radioBalanceCenterOptimize = document.getElementById('radio_balance_center_optimize');
    var radioBalanceSessionSticky = document.getElementById('radio_balance_session_sticky');
    var radioBalanceConsistentHash = document.getElementById('radio_balance_consistent_hash');
    var valueToRadioMapLoadBalance = {
        "WRR" : radioBalanceWRR,
        "ip_hash" : radioBalanceIpHash,
        "least_conn" : radioBalanceLeastConn,
        "idc_optimize" : radioBalanceIDCOptimize,
        "read_write_optimize" : radioBalanceReadWriteOptimize,
        "center_optimize": radioBalanceCenterOptimize,
        "session_sticky" : radioBalanceSessionSticky,
        "consistent_hash" : radioBalanceConsistentHash
    };
    var textBalanceSessionSticky = document.getElementById("text_balance_session_sticky");
    var textBalanceConsistentHash = document.getElementById("text_balance_consistent_hash");

    var balance_WRR_Dialog;
    var balance_IDCOptimize_Dialog;
    var balance_ReadWriteOptimize_Dialog;
    var balance_CenterOptimize_Dialog;
    var balance_IpHash_Dialog;
    var balance_LeastConn_Dialog;
    var balance_SessionSticky_Dialog;
    var balance_ConsistentHash_Dialog;

    var httpMethodListTableWrapper;
    var addHttpMethodUrlDialog;
    var httpMethodUrlList = [];

    //域名映射配置相关
    //var textDomainName = document.getElementById("text_domain_name");
    //var textDomainLocation = document.getElementById("text_domain_location");

    //慢启动映射配置相关
    var slow_Start_Dialog;
    var radioSlowstartTime = document.getElementById('radio_slowstart_time');
    var textSlowstartTime = document.getElementById("text_slowstart_time");

    function detailHttpConfig( key, f1, f2 ){
        appkey = key;
        showOverlay = f1;
        showContent = f2;
        bindSelect();
        bindInputValidCheck();
        bindHealthCheckDescription();
        bindLoadBalanceDescription();
        bindSlowStartDescription();
        bindHttpAddEvent();
        wrapper.one('#httpConfig_healthCheck_page')._node.click();
        Y.msgp.service.setEnvText('httpConfig_env_select');

        httpMethodListTableWrapper = Y.one('#httpMethod-list-table');
    }

    function bindHealthCheckDescription() {
        wrapper.delegate('click', function () {
            healthCheck_TCP_Dialog = healthCheck_TCP_Dialog ? healthCheck_TCP_Dialog : new Y.mt.widget.CommonDialog({
                id: 'healthCheck_TCP_Desc',
                title: '统一规范的http检查url',
                width: 640
            });
            var descContent = '推荐使用统一的url进行http健康检查;<br><br>' +
                '检测周期是8s,一个周期内会检测3次；<br>' +
                '后续检测周期会缩短到3-5s';
            healthCheck_TCP_Dialog.setContent(descContent);
            healthCheck_TCP_Dialog.show();
        }, '#healthCheck_TCP_Desc');

        wrapper.delegate('click', function () {
            healthCheck_HTTP_Dialog = healthCheck_HTTP_Dialog ? healthCheck_HTTP_Dialog : new Y.mt.widget.CommonDialog({
                id: 'healthCheck_HTTP_Desc',
                title: 'HTTP层默认检查方案',
                width: 640
            });
            var descContent = 'check interval=3000 rise=2 fall=3 timeout=1500 type=http;<br>' +
                'check_http_send  "GET / HTTP/1.0 \\r\\n\\r\\n" ;<br>' +
                'check_http_expect_alive http_2xx http_3xx ;<br><br>' +
                '每3000ms发起一个http请求，返回状态是2xx或3xx时认为后端服务正常，超时时间为1500ms；<br>' +
                '如果连续失败fall=3次服务器被认为是down；如果连续成功rise=2次服务器被认为是up；<br>';
            healthCheck_HTTP_Dialog.setContent(descContent);
            healthCheck_HTTP_Dialog.show();
        }, '#healthCheck_HTTP_Desc');

        wrapper.delegate('click', function () {
            healthCheck_Custom_Dialog = healthCheck_Custom_Dialog ? healthCheck_Custom_Dialog : new Y.mt.widget.CommonDialog({
                id: 'healthCheck_Custom_Desc',
                title: '自定义的健康检查url',
                width: 640
            });
            var descContent = '不推荐使用自定义接口，后续会全部下掉，使用统一的接口，希望尽快调整';
            healthCheck_Custom_Dialog.setContent(descContent);
            healthCheck_Custom_Dialog.show();
        }, '#healthCheck_Custom_Desc');
    }

    function bindLoadBalanceDescription() {
        wrapper.delegate('click', function () {
            balance_WRR_Dialog = balance_WRR_Dialog ? balance_WRR_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_wrr',
                title: '加权轮询',
                width: 640
            });
            var descContent = '【默认策略】<br>按server节点权重来轮询分配流量';
            balance_WRR_Dialog.setContent(descContent);
            balance_WRR_Dialog.show();
        }, '#desc_balance_wrr');

        wrapper.delegate('click', function () {
            balance_IDCOptimize_Dialog = balance_IDCOptimize_Dialog ? balance_IDCOptimize_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_idc_optimize',
                title: 'IDC优化',
                width: 640
            });
            var descContent = '【推荐策略】<br>后端server节点的访问优先级顺序为：本IDC > 同城其他IDC > 异地IDC' +
                '<br>即只有在本IDC无可用服务节点时才会进行跨机房访问，在本城无可用服务节点时才会进行跨城访问';
            balance_IDCOptimize_Dialog.setContent(descContent);
            balance_IDCOptimize_Dialog.show();
        }, '#desc_balance_idc_optimize');

        wrapper.delegate('click', function () {
            balance_ReadWriteOptimize_Dialog = balance_ReadWriteOptimize_Dialog ? balance_ReadWriteOptimize_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_read_write_optimize',
                title: '读写分离优化',
                width: 640
            });
            var descContent = '【策略须知】<br>该功能目前仅针对上海到综业务开放，开启后，会针对当前appkey新增同中心、地域（北京、上海）的分组upstream，用于接口粒度开启读写优化（读逻辑走同中心优先策略，写逻辑走上海区域策略）';
            balance_ReadWriteOptimize_Dialog.setContent(descContent);
            balance_ReadWriteOptimize_Dialog.show();
        }, '#desc_balance_read_write_optimize');

        wrapper.delegate('click', function () {
            balance_CenterOptimize_Dialog = balance_CenterOptimize_Dialog ? balance_CenterOptimize_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_center_optimize',
                title: '同中心优化',
                width: 640
            });
            var descContent = '【推荐策略】<br>后端server节点的访问优先级顺序为：同中心优先 > 同城 > 异地' +
                '<br>即只有在同中心无可用服务节点时才会进行跨机房访问，在本城无可用服务节点时才会进行跨城访问';
            balance_CenterOptimize_Dialog.setContent(descContent);
            balance_CenterOptimize_Dialog.show();
        }, '#desc_balance_center_optimize');

        wrapper.delegate('click', function () {
            balance_IpHash_Dialog = balance_IpHash_Dialog ? balance_IpHash_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_ip_hash',
                title: 'IP哈希',
                width: 640
            });
            var descContent = '以client端ip作为hash key来向后端server节点打流量，确保相同client端的请求映射到同一个server节点上<br>' +
                '（注意在服务节点列表发生变化时会rehash）';
            balance_IpHash_Dialog.setContent(descContent);
            balance_IpHash_Dialog.show();
        }, '#desc_balance_ip_hash');

        wrapper.delegate('click', function () {
            balance_LeastConn_Dialog = balance_LeastConn_Dialog ? balance_LeastConn_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_least_conn',
                title: '最少连接数优先',
                width: 640
            });
            var descContent = '优先使用当前活跃链接数最少的server节点';
            balance_LeastConn_Dialog.setContent(descContent);
            balance_LeastConn_Dialog.show();
        }, '#desc_balance_least_conn');

        wrapper.delegate('click', function () {
            balance_SessionSticky_Dialog = balance_SessionSticky_Dialog ? balance_SessionSticky_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_session_sticky',
                title: '会话保持',
                width: 640
            });
            var descContent = '通过cookie保持会话，保证同一client端的请求映射到同一后端server<br>' +
                '参数及含义参考：http://tengine.taobao.org/document/http_upstream_session_sticky.html';
            balance_SessionSticky_Dialog.setContent(descContent);
            balance_SessionSticky_Dialog.show();
        }, '#desc_balance_session_sticky');

        wrapper.delegate('click', function () {
            balance_ConsistentHash_Dialog = balance_ConsistentHash_Dialog ? balance_ConsistentHash_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_balance_consistent_hash',
                title: '一致性哈希',
                width: 640
            });
            var descContent = '使用客户端信息(如：$ip, $uri, $args等变量)作为参数，使用一致性哈希算法将client端请求映射到后端server节点上<br>' +
                '参数及含义参考：http://tengine.taobao.org/document_cn/http_upstream_consistent_hash_cn.html';
            balance_ConsistentHash_Dialog.setContent(descContent);
            balance_ConsistentHash_Dialog.show();
        }, '#desc_balance_consistent_hash');

    }

    function bindSlowStartDescription(){

        wrapper.delegate('click', function () {
            slow_Start_Dialog = slow_Start_Dialog ? slow_Start_Dialog : new Y.mt.widget.CommonDialog({
                id: 'desc_slowstart_hash',
                title: '慢启动',
                width: 640
            });
            var descContent = '【策略介绍】<br>设置一台不健康的主机变成健康主机，或者当一台主机在被认为不可用变成可用时，将其权重由零恢复到权重值的时间。<br>【注意事项】<br>慢启动策略与负载均衡策略的ip_hash，session_sticky，consistent_hash不兼容。';
            slow_Start_Dialog.setContent(descContent);
            slow_Start_Dialog.show();
        }, '#desc_slowstart_hash');

    }

    function bindSelect(){
        // 环境选择
        wrapper.delegate('click', function () {
            Y.all('#httpConfig_env_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            reloadHttpConfigPage();
        }, "#httpConfig_env_select a");

        // 配置项选择
        wrapper.delegate('click', function() {
            Y.all('#httpConfig_configType_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            reloadHttpConfigPage();
        }, "#httpConfig_configType_select a");

        // 启用 停用Radio
        wrapper.delegate('click', function() {
            wrapper.one('#healthCheck_switch_on').set("checked", true);
            wrapper.one('#healthCheck_switch_off').set("checked", false);
        }, '#healthCheck_switch_on');
        wrapper.delegate('click', function() {
            wrapper.one('#healthCheck_switch_on').set("checked", false);
            wrapper.one('#healthCheck_switch_off').set("checked", true);
        }, '#healthCheck_switch_off');

        //启动 停用slowstart
        wrapper.delegate('click', function() {
            wrapper.one('#slowStart_switch_on').set("checked", true);
            wrapper.one('#slowStart_switch_off').set("checked", false);
        }, '#slowStart_switch_on');
        wrapper.delegate('click', function() {
            wrapper.one('#slowStart_switch_on').set("checked", false);
            wrapper.one('#slowStart_switch_off').set("checked", true);
        }, '#slowStart_switch_off');

        //健康检查保存按钮
        Y.one("#healthCheck_btn_save").on('click', saveHealthCheckConfig);

        //负载均衡保存按钮
        Y.one("#loadBalance_btn_save").on('click', saveLoadBalanceConfig);

        //域名保存按钮
        //Y.one("#domain_btn_save").on('click', saveDomainConfig);

        //慢启动保存按钮
        Y.one("#slowStart_btn_save").on('click', saveSlowStartConfig);

    }

    function bindInputValidCheck() {

    }

    function reloadHttpConfigPage() {
        showOverlay( wrapper );
        var page = getSelectedConfigType();

        switch (Number(page)) {
            case 0: //健康检查
                healthCheck_wrap.show();
                loadBalance_wrap.hide();
                domain_wrap.hide();
                slowStart_wrap.hide();
                getHealthCheckConfig();
                break;
            case 1: //负载均衡
                healthCheck_wrap.hide();
                loadBalance_wrap.show();
                domain_wrap.hide();
                slowStart_wrap.hide();
                getLoadBalanceConfig();
                break;
            case 2: //域名映射
                healthCheck_wrap.hide();
                loadBalance_wrap.hide();
                domain_wrap.show();
                slowStart_wrap.hide();
                showContent(wrapper);
                getHttpMethodData();
                //getDomainConfig();
                break;
            case 3: //慢启动
                healthCheck_wrap.hide();
                loadBalance_wrap.hide();
                domain_wrap.hide();
                slowStart_wrap.show();
                getSlowStartConfig();
                break;
        }
    }

    function getSelectedEnv() {
        return Y.one('#httpConfig_env_select a.btn-primary').getAttribute('value');
    }

    function getSelectedConfigType() {
        return Y.one('#httpConfig_configType_select a.btn-primary').getAttribute('value')
    }


    function patchHealthCheckData(){
        var obj = {
            appkey: appkey,
            is_health_check: parseInt(getHealthCheckSwitchValue()),
            health_check_type: "defaultUniform",
            health_check: "",
            centra_check_type: "tcp",
            centra_http_send: ""
        };

        if(1 == obj.is_health_check) {
            obj.centra_check_type = "http";
            if(document.getElementById("radio_default_uniform").checked){
                obj.centra_http_send = textDefaultUniform.value;
            }
            if(document.getElementById("radio_customized").checked){
                obj.health_check_type = "customized";
                obj.centra_http_send = textCustomizedHttpSend.value;
            }
        }

        if (obj.centra_http_send.charAt(0)!="/"){
            obj.centra_http_send = "/" + obj.centra_http_send
        }
        console.log(obj.centra_http_send)
        return obj;
    }

    function fillHealthCheckConfig( data ){
        setHealthCheckSwitch(data.is_health_check);
        if(data.health_check_type == 'customized') {
            textCustomizedHttpSend.value = data.centra_http_send;

        }

        setHealthCheckTypeRadio(data.health_check_type);
        showContent( wrapper );
    }

    function saveHealthCheckConfig(){
        var env = getSelectedEnv();
        if (document.getElementById("radio_customized").checked && textCustomizedHttpSend.value=='') {
            Y.msgp.utils.msgpHeaderTip('error', '若选择自定义健康检查，check_http_send不能为空', 3);
            return
        }
        if(0 == parseInt(getHealthCheckSwitchValue())) {
            var data = patchHealthCheckData();
            var url = '/service/' + appkey + '/healthCheckConfig';
            Y.io(url, {
                method : 'put',
                headers : {'Content-Type':"application/json;charset=UTF-8"},
                data : {
                    env: env,
                    data: Y.JSON.stringify( data )
                },
                on : {
                    success : function (id, o) {
                        var ret = Y.JSON.parse( o.responseText );
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', '保存成功！', 3);
                            reloadHttpConfigPage();
                        }else if(ret.msg) {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                        }else{
                            Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                        }
                    },
                    failure : function() {
                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                    }
                }
            });
        } else {
            var checkUrl = '/service/' + appkey + '/healthUrlCheck';
            var urlValue;
            if(document.getElementById("radio_default_uniform").checked) {
                urlValue = textDefaultUniform.value;
            } else {
                urlValue = textCustomizedHttpSend.value;
            }

            if (urlValue.charAt(0)!="/"){
                urlValue = "/" + urlValue
            }

            Y.io( checkUrl, {
                method : 'get',
                data : {
                    env : env,
                    healthUrl : urlValue
                },
                on : {
                    success : function(id, o){
                        if(o.responseText == "success") {
                            var data = patchHealthCheckData();
                            var url = '/service/' + appkey + '/healthCheckConfig';
                            Y.io(url, {
                                method : 'put',
                                headers : {'Content-Type':"application/json;charset=UTF-8"},
                                data : {
                                    env: env,
                                    data: Y.JSON.stringify( data )
                                },
                                on : {
                                    success : function (id, o) {
                                        var ret = Y.JSON.parse( o.responseText );
                                        if (ret.isSuccess) {
                                            Y.msgp.utils.msgpHeaderTip('info', '保存成功！', 3);
                                            reloadHttpConfigPage();
                                        }else if(ret.msg) {
                                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                                        }else{
                                            Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                                        }
                                    },
                                    failure : function() {
                                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                                    }
                                }
                            });
                        } else if(o.responseText == "emptyFailed") {
                            Y.msgp.utils.msgpHeaderTip('error', '可用服务列表为空，请确认', 3);
                        } else if(o.responseText == "redirect") {
                            Y.msgp.utils.msgpHeaderTip('error', '接口请去掉sso配置', 3);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '接口url检测失败', 3);
                        }
                    },
                    failure : function(){
                        Y.msgp.utils.msgpHeaderTip('error', '接口url检测失败', 3);
                    }
                }
            });
        }
    }

    function getHealthCheckConfig(){
        var url = '/service/' + appkey + '/healthCheckConfig';
        var env = getSelectedEnv();
        Y.io( url, {
            method : 'get',
            data : {
                env : env
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        fillHealthCheckConfig( ret.data );
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                }
            }
        });
    }

    function setHealthCheckSwitch(is_enabled) {
        if (1== Number(is_enabled)) {
            wrapper.one('#healthCheck_switch_on')._node.click();
        } else {
            wrapper.one('#healthCheck_switch_off')._node.click();
        }
    }

    function   getHealthCheckSwitchValue(){
        if (document.getElementById('healthCheck_switch_on').checked) {
            return Number( wrapper.one('#healthCheck_switch_on').get('value'));
        } else {
            return Number( wrapper.one('#healthCheck_switch_off').get('value'));
        }
    }

    function setSlowStartSwitch(is_enabled) {
        if (1== Number(is_enabled)) {
            wrapper.one('#slowStart_switch_on')._node.click();
        } else {
            wrapper.one('#slowStart_switch_off')._node.click();
        }
    }

    function getSlowStartSwitchValue(){
        if (document.getElementById('slowStart_switch_on').checked) {
            return Number( wrapper.one('#slowStart_switch_on').get('value'));
        } else {
            return Number( wrapper.one('#slowStart_switch_off').get('value'));
        }
    }

    //健康检查方案Radio
    function setHealthCheckTypeRadio(health_check_type){
        for(var key in valueToRadioMapHealthCheck) {
            valueToRadioMapHealthCheck[key].checked = false;
        }
        var radio = valueToRadioMapHealthCheck[health_check_type];
        if(radio) {
            radio.checked = true;
        }
    }


    function patchLoadBalanceData(){
        var obj = {
            appkey: appkey,
            load_balance_type: wrapper.one('#loadBalance_strategy input:checked').get('value'),
            load_balance_value: ""
        };
        if(document.getElementById("radio_balance_session_sticky").checked){
            obj.load_balance_value = getLoadBalanceSessionStickyContent();
        } else if (document.getElementById("radio_balance_consistent_hash").checked) {
            obj.load_balance_value = getLoadBalanceConsistentHashContent();
        }
        return obj;
    }

    function saveLoadBalanceConfig() {
        if (document.getElementById("radio_balance_consistent_hash").checked) {
            var url = '/hlb/group/list?appkey=' + appkey;
            Y.io(url, {
                method: 'get',
                async: false,
                data: {
                    env: getEnvStr(),
                    pageNo: 1,
                    pageSize: 10000
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        var arr = ret.data;
                        var weightnum = 0
                        for (var i = 0, l = arr.length; i < l; i++) {
                            var tmp;
                            tmp = arr[i]['weight'];
                            if (tmp != null && tmp.length != 0) {
                                weightnum += Number(tmp)
                            }

                        }
                        if (weightnum < 300) {
                            var data = patchLoadBalanceData();
                            var env = getSelectedEnv();
                            var url = '/service/' + appkey + '/loadBalanceConfig';
                            Y.io(url, {
                                method: 'put',
                                headers: {'Content-Type': "application/json;charset=UTF-8"},
                                data: {
                                    env: env,
                                    data: Y.JSON.stringify(data)
                                },
                                on: {
                                    success: function (id, o) {
                                        var ret = Y.JSON.parse(o.responseText);
                                        if (ret.isSuccess) {
                                            Y.msgp.utils.msgpHeaderTip('info', '保存成功！', 3);
                                            reloadHttpConfigPage();
                                        } else if (ret.msg) {
                                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                                        } else {
                                            Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                                        }
                                    },
                                    failure: function () {
                                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                                    }
                                }
                            });
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '节点权重总和大于300,无法操作', 3);
                        }
                    }
                }
            });
        }else{
            var data = patchLoadBalanceData();
            var env = getSelectedEnv();
            var url = '/service/' + appkey + '/loadBalanceConfig';
            Y.io(url, {
                method: 'put',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data: {
                    env: env,
                    data: Y.JSON.stringify(data)
                },
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('info', '保存成功！', 3);
                            reloadHttpConfigPage();
                        } else if (ret.msg) {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                    }
                }
            });
        }

    }

    function getLoadBalanceConfig(){
        var url = '/service/' + appkey + '/loadBalanceConfig';
        var env = getSelectedEnv();
        Y.io( url, {
            method : 'get',
            data : {
                env : env,
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        fillLoadBalanceConfig( ret.data );
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                }
            }
        });
    }

    function fillLoadBalanceConfig( data ){
        if(data.load_balance_type == 'session_sticky') {
            setLoadBalanceSessionStickyContent(data.load_balance_value);
        } else if (data.load_balance_type == 'consistent_hash') {
            setLoadBalanceConsistentHashContent(data.load_balance_value);
        }
        setLoadBalanceTypeRadio(data.load_balance_type);
        showContent( wrapper );
    }

    //负载均衡方案Radio
    function setLoadBalanceTypeRadio( load_balance_type){
        for(var key in valueToRadioMapLoadBalance) {
            valueToRadioMapLoadBalance[key].checked = false;
        }
        var radio = valueToRadioMapLoadBalance[load_balance_type];
        if(radio) {
            radio.checked = true;
        }
    }

    function getLoadBalanceSessionStickyContent() {
        return textBalanceSessionSticky.value;
    }

    function setLoadBalanceSessionStickyContent(content){
        textBalanceSessionSticky.value = content;
    }

    function getLoadBalanceConsistentHashContent() {
        return textBalanceConsistentHash.value;
    }

    function setLoadBalanceConsistentHashContent(content) {
        textBalanceConsistentHash.value = content;
    }

    function fillSlowStartConfig( data ){
        setSlowStartSwitch(data.is_slow_start);
        var slow_strat_value = data.slow_start_value
        slow_strat_value = slow_strat_value.substring(0,slow_strat_value.length-1)
        setSlowStartTimeContent(slow_strat_value)
        if(radioSlowstartTime){
            radioSlowstartTime.checked = true;
        }
        showContent(wrapper);
    }

    function getLoadBalanceType(){
        var url = '/service/' + appkey + '/loadBalanceConfig';
        var env = getSelectedEnv();
        var load_balance_type =null
        Y.io( url, {
            method : 'get',
            sync:true,
            data : {
                env : env,
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    console.dir(ret);
                    if( ret.isSuccess ){
                        load_balance_type=ret.data.load_balance_type;
                    }

                },
            }
        });
        return load_balance_type;

    }

    function saveSlowStartConfig(){
        var data = patchSlowStartData();
        if (!data) {
            Y.msgp.utils.msgpHeaderTip('error', "请输入一个数字", 3);
            return;
        }
        var load_balance_type = getLoadBalanceType();
        if(data.is_slow_start == 1){
            if(load_balance_type == 'session_sticky' || load_balance_type == 'consistent_hash'|| load_balance_type == 'ip_hash') {
                Y.msgp.utils.msgpHeaderTip('error', "慢启动与所选择的负载均衡策略不兼容", 3);
                return;
            }
        }
        var env = getSelectedEnv();
        var url = '/service/' + appkey + '/slowStartConfig';
        console.log('save tab slowStartConfig for ' + appkey + ' ' + env);
        Y.io(url, {
            method : 'put',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data : {
                env: env,
                data: Y.JSON.stringify( data )
            },
            on : {
                success : function (id, o) {
                    var ret = Y.JSON.parse( o.responseText );
                    if (ret.isSuccess) {
//                        window.location.href = '/service';
                        Y.msgp.utils.msgpHeaderTip('info', '保存成功！', 3);
                        reloadHttpConfigPage();
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                    }
                },
                failure : function() {
                    Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                }
            }
        });
    }

    function getSlowStartConfig(){
        var url = '/service/' + appkey + '/slowStartConfig';
        var env = getSelectedEnv();
        Y.io( url, {
            method : 'get',
            data : {
                env : env
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    console.dir(ret);
                    if( ret.isSuccess ){
                        fillSlowStartConfig( ret.data );
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                }
            }
        });
    }

    function patchSlowStartData(){
        var obj = {
            appkey: appkey,
            is_slow_start: parseInt(getSlowStartSwitchValue()),
            slow_start_value: ""
        };

        if(document.getElementById("radio_slowstart_time").checked){
            var num = Number(getSlowStartTimeContent());
            if (isNaN(num))
            {
                return false;
            }
            var slow_strat_value = getSlowStartTimeContent() + "s";
            obj.slow_start_value = slow_strat_value;
        }
        return obj;
    }

    function getSlowStartTimeContent() {
        return textSlowstartTime.value;
    }

    function setSlowStartTimeContent(content){
        textSlowstartTime.value = content;
    }

    function getEnvStr() {
        switch (getSelectedEnv()) {
            case '3':
                return "prod";
            case '2':
                return "stage";
            case '1':
                return "test";
        }
    }

    function setLoading() {
        colspan = 3;
        var html = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>';
        httpMethodUrlTableWrapTbody.setHTML(html);
        pbody.empty();
        showContent(wrapper);
    }

    function getRoutes() {
        setLoading();
        getHttpMethodData();
    }
    function fillHttpMethodUrlTable(methodData) {
        var micro = new Y.Template();
        var addCoreSavedTbodyTemplate = Y.one('#text-http-method-table-tbody').get('value');
        var html = micro.render(addCoreSavedTbodyTemplate, {data: methodData});
        httpMethodUrlTableWrap.one("tbody").setHTML(html);
    }
    function getHttpMethodData() {
        var url = '/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/apkUrl/get';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        httpMethodUrlList = ret.data;
                        fillHttpMethodUrlTable(ret.data);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取接口数据失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取接口数据失败', 3);
                }
            }
        });
    }
    function doAddHttpMethodUrl(btn, container) {
        var url = '/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/apkUrl/add';
        var data = {
            methodUrl: container.one("#s_method_url").get('value'),
            serverName: container.one("#s_server_name").get('value'),
        };

        function successCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('success', msg || "添加成功", 3);
            addHttpMethodUrlDialog.close();
            getRoutes();
        }

        function errorCallback(msg) {
            Y.msgp.utils.msgpHeaderTip('error',msg || '添加失败',3);
        }

        Y.io(url, {
            method: 'post',
            data: Y.JSON.stringify(data),
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    successCallback && successCallback(ret.msg);
                },
                failure: function () {
                    errorCallback && errorCallback();
                }
            }
        });
        return true;
    }

    function bindHttpAddEvent() {
        wrapper.delegate('click', function () {
            addHttpMethodUrlDialog = addHttpMethodUrlDialog != null ? addHttpMethodUrlDialog : new Y.mt.widget.CommonDialog({
                id: 'add_httpMethodUrl_dialog',
                title: '新增接口 ',
                width: 1024,
                btn: {
                    pass: doAddHttpMethodUrl
                }
            });

            var micro = new Y.Template();
            var template = Y.one('#text_add_httpMethodUrl_form').get('value');
            var str = micro.render(template, {});
            addHttpMethodUrlDialog.setContent(str);

            var addBody = addHttpMethodUrlDialog.getBody();
            addBody.detachAll('click');
            addBody.detachAll('change');

            addHttpMethodUrlDialog.show();

        }, '#http-methodUrl-list-add');

        httpMethodUrlTableWrapTbody.delegate('click', function () {
            var el = this;
            var line = el.ancestor('tr');
            var info = line.getData('info');
            if (Y.Lang.isString(info)) {
                info = Y.JSON.parse(info);
            }
            var postData = {
                serverName: info.serverName,
                methodUrl: info.methodUrl
            };

            Y.io('/service/httpCutFlow/' + appkey + '/' + getEnvStr() + '/apkUrl/del', {
                method: 'post',
                data: Y.JSON.stringify(postData),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                            getHttpMethodData();
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
                    }
                }
            });
        }, '.delHttpMethodUrl');
    }

    /*function patchDomainData(){
        var obj = {
            appkey: appkey,
            domain_name: getDomainName(),
            domain_location: getDomainLocation(),
        };
        return obj;
    }*/

    /*function fillDomainConfig( data ){
        setDomainName(data.domain_name);
        setDomainLocation(data.domain_location);
        showContent( wrapper );
    }*/

    /*function saveDomainConfig(){
        var data = patchDomainData();
        var env = getSelectedEnv();
        var url = '/service/' + appkey + '/domainConfig';
        console.log('save tab domainConfig for ' + appkey + ' ' + env);
        Y.io(url, {
            method : 'put',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data : {
                env: env,
                data: Y.JSON.stringify( data )
            },
            on : {
                success : function (id, o) {
                    var ret = Y.JSON.parse( o.responseText );
                    if (ret.isSuccess) {
//                        window.location.href = '/service';
                        Y.msgp.utils.msgpHeaderTip('info', '保存成功！', 3);
                        reloadHttpConfigPage();
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                }
            }
        });
    }*/

    /*function getDomainConfig(){
        var url = '/service/' + appkey + '/domainConfig';
        var env = getSelectedEnv();
        console.log('get tab domainConfig for ' + appkey + ' ' + env);
        Y.io( url, {
            method : 'get',
            data : {
                env : env,
            },
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    console.dir(ret);
                    if( ret.isSuccess ){
                        fillDomainConfig( ret.data );
                    }else if(ret.msg) {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }else{
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                    }
                },
                failure : function(){
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败，请稍后重试！', 3);
                }
            }
        });
    }*/

    /*function getDomainName(){
        return textDomainName.value;
    }*/

    /*function getDomainLocation(){
        return textDomainLocation.value;
    }*/

    /*function setDomainName(domainName) {
        textDomainName.value = domainName;
    }*/

    /*function setDomainLocation(domainLocation){
        textDomainLocation.value = domainLocation;
    }*/

}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap'
    ]
});
