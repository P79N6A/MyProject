M.add('msgp-checker/thriftCheck', function (Y) {
    Y.namespace('msgp.checker').thriftCheck = initThriftCheck;
    var appkey;
    var thriftCheckWrap = Y.one('.thriftCheck');
    var httpCheckWrap = Y.one('.httpCheck');
    var httpInvokeWrap = Y.one('.httpInvoke');

    var httpCheckHost;
    var httpCheckPort;
    var httpInvokePort;
    var nodesVersion;

    // http请求url
    var httpCheckUrl = "/thriftHttpSvc/httpCheck";
    var httpInvokeUrl = "/thriftHttpSvc/httpInvoke";

    var getServerNodesUrl = "/thriftHttpSvc/serverNode/get";
    var getServiceMethodsUrl = "/thriftHttpSvc/serviceMethods/get";

    var loadHtml = "<i class='fa fa-spinner fa-spin' style='font-size: 15px;'></i>";

    function initThriftCheck(key) {
        appkey = key;
        bindCheckRole();
        bindCheckSumit();
        bindPortUpdate();
        initParamCheck();

        bindHttpInvoke();

        // 保证事件绑定顺序，放最后
        bindCheckTab();
    }

    function bindCheckTab() {
        $(".thriftCheck .httpCheckBtn").on("click", function () {
            $(".thriftCheck .httpCheckBtn").addClass('btn-primary');
            $(".thriftCheck .httpInvokeBtn").removeClass('btn-primary');
            $(".httpCheck").show();
            $(".httpInvoke").hide();
            $(".operWarning").hide()
        });

        $(".thriftCheck .httpInvokeBtn").on("click", function () {
            $(".thriftCheck .httpCheckBtn").removeClass('btn-primary');
            $(".thriftCheck .httpInvokeBtn").addClass('btn-primary');
            $(".httpCheck").hide();
            $(".httpInvoke").show();
            $(".operWarning").show()
            initHttpInvokeModule();
        });
    }


    function bindHttpInvoke() {
        $("#httpInvokeEnv a").on("click", function () {
            $("#httpInvokeEnv a").removeClass('btn-primary');
            $(this).addClass('btn-primary');
            reloadServerNode();
        });
        Y.msgp.service.setEnvText('httpInvokeEnv');
        $(".httpInvoke #serverNodes").on("blur", function () {
            var nodeName = $(".httpInvoke #serverNodes").val();
            if (!nodeName || !nodesVersion[nodeName]) {
                $(".httpInvoke .serverNodeTip").html("");
            }
        });
        $(".httpInvoke .queryMethod").on("click", function () {
            loadServiceMethod();
        });

        $(".thriftCheck").on("click", "li label", function () {
            if ($(this).next()[0].checked) {
                $(this).css("background", "url(/static/img/toggle.png) no-repeat 0px 1px");
            } else {
                $(this).css("background", "url(/static/img/toggled.png) no-repeat 0px 1px");
            }
        });

        $(".thriftCheck").on("click", "li span", function () {
            $(".thriftCheck li span").css("font-weight", "500");
            $(".thriftCheck li span").css("color", "#000000");
            $(".thriftCheck li span").css("text-decoration", "none");

            $(this).css("font-weight", "bold");
            $(this).css("color", "#3fab99");
            $(this).css("text-decoration", "underline");

            var serviceName = $(this).attr("svcName");
            var methodName = $(this).text();
            $(".httpInvoke .methodInvoke .invokeParam .methodSign").val(methodName);
            $(".httpInvoke .methodInvoke .invokeParam .methodSign").attr("serviceName", serviceName);

            var paramNum = $(this).attr("paramNum");
            if (!paramNum || paramNum == 0) {
                $(".methodResult .params").html("");
                return;
            }
            var paramsDivHtml = "";
            for (var i = 1; i <= paramNum; i++) {
                var paramHtml = "<div class='control-group' style='margin-bottom: 10px'>" +
                    "<label class='control-label' style='width: 95px; padding-top: 0px;'>paramTitle</label>" +
                "<textarea class='invokeMethodParam' placeholder='paramDesc' style='height: 20px;'></textarea></div>";
                if (i == 1) {
                    paramHtml = paramHtml.replace(/paramTitle/, "输入参数：");
                } else {
                    paramHtml = paramHtml.replace(/paramTitle/, "");
                }
                paramHtml = paramHtml.replace(/paramDesc/, "参数" + i + " : Object用JacksonUtil.serialize()生成");
                paramsDivHtml += paramHtml;
            }
            $(".methodResult .params").html(paramsDivHtml);
        });

        $(".thriftCheck .httpInvoke").on("click", ".invokeBtn", function () {
            $(".thriftCheck .invokeBtnTip").html("");
            var methodSign = $(".httpInvoke .methodInvoke .invokeParam .methodSign").val();

            var invokeParams = [];
            var i = 0;
            $(".httpInvoke .methodInvoke .invokeParam .invokeMethodParam").each(function () {
                var param = $(this).val();
                invokeParams.push(JSON.stringify($(this).val()));
            });
            if (!methodSign) {
                $(".thriftCheck .invokeBtnTip").html("调用方法不能为空");
                return;
            }
            var serviceName = $(".httpInvoke .methodInvoke .invokeParam .methodSign").attr("serviceName");
            if (!serviceName) {
                $(".thriftCheck .invokeBtnTip").html("未获取到ServiceName请联系管理员");
                return;
            }

            doHttpInvoke(serviceName, methodSign, invokeParams);
        });
    }

    function bindCheckRole() {
        $(".checkRole [name='server']").on("click", function () {
            $(".checkRole [name='server']")[0].checked = true;
            $(".checkRole [name='client']")[0].checked = false;
            $(".serverCheckType").show();
            $(".clientCheckType").hide();
        });
        $(".checkRole [name='client']").on("click", function () {
            $(".checkRole [name='server']")[0].checked = false;
            $(".checkRole [name='client']")[0].checked = true;
            $(".serverCheckType").hide();
            $(".clientCheckType").show();
        });
        $(".checkRole [name='server']").click();
    }

    function bindCheckSumit() {
        $(".httpCheck .submit").on("click", function () {
            $(".httpCheck .submitTip").html("");
            if (!verifyHttpCheckInputInfo()) {
                return;
            }

            var host = $(".httpCheck .host").val();
            var port = $(".httpCheck .httpPort").val();
            var checkRole = "server";
            if ($(".checkRole [name='client']")[0].checked) {
                checkRole = "client";
            }
            var checkType;
            if (checkRole == "server") {
                if ($(".httpCheck .serverCheckType").is(":hidden")) {
                    $(".httpCheck .submitTip").html("自检端和自检信息不匹配，请联系管理员");
                    $(".httpCheck .submitTip").css('color', '#f00');
                    return;
                }
                checkType = $(".httpCheck .serverCheckType").val();
            } else {
                if ($(".httpCheck .clientCheckType").is(":hidden")) {
                    $(".httpCheck .submitTip").html("自检端和自检信息不匹配，请联系管理员");
                    $(".httpCheck .submitTip").css('color', '#f00');
                    return;
                }
                checkType = $(".httpCheck .clientCheckType").val();
            }

            $(".checkResult").show();
            $(".checkResult .resultContent").hide();
            $(".resultLoad").show();
            $.ajax({
                url: httpCheckUrl,
                type: 'post',
                data: {appkey: appkey, host: host, port: port, role: checkRole, checkType: checkType},
                success: function (ret) {
                    $(".resultLoad").hide();
                    $(".checkResult .resultContent").html("");
                    $(".checkResult .resultContent").show();
                    if (ret.isSuccess) {
                        $(".checkResult .resultContent").css('color', 'black');
                        try {
                            var dataStr = ret.data.replace(/\"taskId\":\b\d+\b/g, replaceOverflowId);
                            var jsonData = JSON.parse(dataStr);
                            $(".checkResult .resultContent").html(JSON.stringify(jsonData, null, " "));
                        } catch (err) {
                            $(".checkResult .resultContent").html(showText(ret.data));
                        }
                    } else {
                        $(".checkResult .resultContent").css('color', '#f00');
                        $(".checkResult .resultContent").html(showText("请求失败:\n" + ret.msg));
                    }
                },
                error: function (ret) {
                    $(".resultLoad").hide();
                    Y.msgp.utils.msgpHeaderTip('error', '请求失败, code=' + ret.status, 3);
                }
            });

        });
    }

    function bindPortUpdate() {
        $(".httpCheck .changePort").on("click", function () {
            $(".httpCheck .httpPort").attr("readonly", false);
            $(".httpCheck .httpPort").trigger("focus");
        });
        $(".httpCheck .httpPort").on("blur", function () {
            $(".httpCheck .httpPort").attr("readonly", true);
        });

        $(".httpInvoke .invokeChangePort").on("click", function () {
            $(".httpInvoke .httpInvokePort").attr("readonly", false);
            $(".httpInvoke .httpInvokePort").trigger("focus");
        });
        $(".httpInvoke .httpInvokePort").on("blur", function () {
            $(".httpInvoke .httpInvokePort").attr("readonly", true);
        });
    }

    function initParamCheck() {
        httpCheckHost = Y.msgp.utils.check.init(httpCheckWrap.one(".host"), {
            chineseOk: false,
            spaceOk: true,
            emptyOk: false,
            warnElement: httpCheckWrap.one('#checkHostTip')
        });

        httpCheckPort = Y.msgp.utils.check.init(httpCheckWrap.one(".httpPort"), {
            type: 'int',
            warnMsg: '必须为数字',
            warnElement: httpCheckWrap.one('#checkPortTip')
        });

        httpInvokePort = Y.msgp.utils.check.init(httpInvokeWrap.one(".httpInvokePort"), {
            type: 'int',
            warnMsg: '必须为数字',
            warnElement: httpInvokeWrap.one('#invokePortTip')
        });
    }

    function verifyHttpCheckInputInfo() {
        if (!httpCheckHost.isValid()) {
            httpCheckHost.showMsg();
            return false;
        }
        if (!httpCheckPort.isValid()) {
            httpCheckPort.showMsg();
            return false;
        }
        return true;
    }

    function verifyHttpInvokeInputInfo() {
        if (!httpInvokePort.isValid()) {
            httpInvokePort.showMsg();
            return false;
        }
        if (!$(".httpInvoke #serverNodes").val()) {
            $(".httpInvoke .serverNodeTip").css('color', '#f00');
            $(".httpInvoke .serverNodeTip").html("不能为空");
        }
        return true;
    }

    function showText(message) {
        var showMsg = message.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/'/g, "&#39;").replace(/"/g, "&quot;").replace(/\n/g, "<br/>");
        return showMsg;
    }

    function initHttpInvokeModule() {
        $("#httpInvokeEnv .httpInvoke_prod").click();
        $(".httpInvoke .methodResult").hide();
    }

    function reloadServerNode() {
        var env = $("#httpInvokeEnv a.btn-primary").attr("value");
        $(".httpInvoke .serverNodeTip").html("");
        $("#serverNodes").val("");
        $.ajax({
            url: getServerNodesUrl,
            type: 'get',
            data: {appkey: appkey, env: env},
            success: function (ret) {
                if (ret.isSuccess) {
                    nodesVersion = ret.data;
                    var serverNodes = [];
                    var i = 0;
                    for (var nodeName in nodesVersion) {
                        serverNodes[i++] = nodeName;
                    }
                    if (serverNodes.length == 0) {
                        $(".httpInvoke .serverNodeTip").html("没有查询到1.8.5.4(包含)以上版本节点")
                    }
                    $("#serverNodes").autocomplete({
                        source: serverNodes,
                        minLength: 0,
                        focus: function (event, ui) {
                            var nodeName = ui.item.value;
                            if (nodeName && nodesVersion[nodeName]) {
                                $(".httpInvoke .serverNodeTip").css('color', 'black');
                                $(".httpInvoke .serverNodeTip").html(" 版本: " + nodesVersion[nodeName]);
                            }
                        }
                    });

                } else {
                    $(".httpInvoke .serverNodeTip").css('color', '#f00');
                    $(".httpInvoke .serverNodeTip").html("请求失败:" + ret.msg);
                }
            },
            error: function (ret) {
                Y.msgp.utils.msgpHeaderTip('error', '请求失败, code=' + ret.status, 3);
            }
        });
    }

    function loadServiceMethod() {
        if (!verifyHttpInvokeInputInfo()) {
            return;
        }
        var host = $(".httpInvoke #serverNodes").val();
        var port = $(".httpInvoke .httpInvokePort").val();
        initMethodQuery();
        $.ajax({
            url: getServiceMethodsUrl,
            type: 'get',
            data: {appkey: appkey, host: host, port: port},
            success: function (ret) {
                $(".httpInvoke .methodLoad").hide();
                if (ret.isSuccess) {
                    showMethodQueryResult(ret.data);
                    $(".httpInvoke .methodInvoke").show();
                } else {
                    $(".httpInvoke .methodQueryError").show();
                    $(".httpInvoke .methodQueryError pre").html(showText("请求失败:" + ret.msg));
                }
            },
            error: function (ret) {
                $(".httpInvoke .methodLoad").hide();
                Y.msgp.utils.msgpHeaderTip('error', '请求失败, code=' + ret.status, 3);
            }
        });
    }

    function doHttpInvoke(serviceName, methodSign, invokeParams) {
        var host = $(".httpInvoke #serverNodes").val();
        var port = $(".httpInvoke .httpInvokePort").val();

        $(".httpInvoke .methodInvoke .invokeResult  .invokeResultContent").css('color', 'black');
        $(".httpInvoke .methodInvoke .invokeResult .invokeResultContent").html(loadHtml);
        $(".httpInvoke .methodInvoke .invokeResult").show();
        var invokeApiParam = {
            appkey: appkey,
            host: host,
            port: port,
            serviceName: serviceName,
            method: methodSign,
            params: "[" + invokeParams + "]"
        };
        $.ajax({
            url: httpInvokeUrl,
            type: 'POST',
            // contentType: "application/json",
            // dataType: "json",
            data: invokeApiParam,
            success: function (ret) {
                if (ret.isSuccess) {
                    $(".httpInvoke .methodInvoke .invokeResult  .invokeResultContent").css('color', 'black');
                    $(".httpInvoke .methodInvoke .invokeResult  .invokeResultContent").html(JSON.stringify(JSON.parse(ret.data), null, " "));
                } else {
                    $(".httpInvoke .methodInvoke .invokeResult  .invokeResultContent").css('color', '#f00');
                    $(".httpInvoke .methodInvoke .invokeResult  .invokeResultContent").html(showText("请求失败:\n" + ret.msg));
                }
            },
            error: function (ret) {
                $(".httpInvoke .methodInvoke .invokeResult .invokeResultContent").html("请求失败");
                Y.msgp.utils.msgpHeaderTip('error', '请求失败, code=' + ret.status, 3);
            }
        });
    }

    function showMethodQueryResult(methodQueryResult) {
        var showHtml = "<ul>serviceInfo</ul>";
        var servicesInfoHtml = "";
        for (var serviceName in methodQueryResult) {
            var serviceInfoHtml = "<li><label for='serviceName'>serviceName</label><input id='serviceName' type='checkbox'/><ol>methodsInfo</ol></li>";
            var methods = methodQueryResult[serviceName];

            var methodsInfoHtml = ""
            for (var methodSign in methods) {
                var methodInfoHtml = "<li><span svcName='serviceName' paramNum='paramNumber'>methodName</span></li>";
                var paramNum = methods[methodSign];

                methodInfoHtml = methodInfoHtml.replace(/serviceName/g, serviceName);
                methodInfoHtml = methodInfoHtml.replace(/methodName/g, methodSign);
                methodInfoHtml = methodInfoHtml.replace(/paramNumber/g, paramNum);
                methodsInfoHtml += methodInfoHtml;
            }
            serviceInfoHtml = serviceInfoHtml.replace(/serviceName/g, serviceName);
            serviceInfoHtml = serviceInfoHtml.replace(/methodsInfo/g, methodsInfoHtml);
            servicesInfoHtml += serviceInfoHtml;
        }
        showHtml = showHtml.replace(/serviceInfo/g, servicesInfoHtml);

        $(".httpInvoke .methodInvoke .queryResult").html(showHtml);
        $(".httpInvoke .methodInvoke .invokeParam .methodSign").val("");
        $(".httpInvoke .methodInvoke .invokeParam .params").html("");
        $(".httpInvoke .methodInvoke .invokeParam .invokeMethodParam").val("");
        $(".httpInvoke .methodInvoke .invokeParam .invokeBtnTip").html();
        $(".httpInvoke .methodInvoke .invokeParam").show();
        $(".httpInvoke .methodInvoke .invokeResult").hide();

        $(".httpInvoke .queryResult li span").each(function () {
            $(this).tooltip({
                title: $(this).text(),
                delay: {
                    hide: 100
                },
            });
        });
    }

    function initMethodQuery() {
        $(".httpInvoke .methodQueryError").hide();
        $(".httpInvoke .methodInvoke .queryResult").html("");
        $(".httpInvoke .methodInvoke").hide();

        $(".httpInvoke .methodLoad").show();
        $(".httpInvoke .methodResult").show();
    }

    function replaceOverflowId(text) {
        return text.replace(/\b\d+\b/g, "\"$&\"")
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'msgp-utils/check',
        'msgp-utils/msgpHeaderTip',
        'msgp-service/commonMap'
    ]
});
