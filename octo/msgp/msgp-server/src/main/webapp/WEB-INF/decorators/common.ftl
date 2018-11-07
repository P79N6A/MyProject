<!DOCTYPE html>
<html lang="zh">
<head>
<#include "header.inc" >
    <link rel="dns-prefetch" href="//analytics.meituan.com"/>
    <link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src="/static/js/jquery.min.js"></script>
    <script src="/static/js/echarts.min.js"></script>
    <style>
        #l-hd .logo {
            background: none;
        }
        #doc, #wrapper, #l-ft{
            background: url("/api-wm/image/visible") 0 0 repeat;
            background-size: 300px 250px;
        }
    </style>

</head>
<body class="theme-cos yui3-skin-sam">
<div id="doc">
    <div id="l-hd">
        <div class="l-hd-left">
            <div class="l-hd-main">
            <#--<img src="/static/img/octo.png" />-->
                <h1 class="logo"><a href="/">服务治理平台</a></h1>
            <#--<span class="logo">新美大</span>-->
            <#--<span class="logo-sep"></span>-->
            <#--<h1><a href="/">OCTO</a></h1>-->
            </div>
        <#include "top_nav.ftl" >
        </div>
        <div class="user-info">
            <ul>
            <#--<li><a id="add_quick_nav" href="javascript:;"><span class="fa fa-plus"></span>添加</a></li>-->
            <#--<li class="sep"></li>-->
                <li><a class="user-name ml0" href="javascript:;">Hi，${_currentUser.name!"nobody"}</a></li>
            <#--<li>-->
            <#--<a href="/logout" class="logout" title="退出">退出</a>-->
            <#--</li>-->
            </ul>
        </div>
        <div class="fb-man">
            <a style="padding: 8px; border: 1px solid; border-color: #ccc;" class="onlineState"></a>
        </div>
    </div>
    <div id="banner" class="banner" style="display: none">
        <ul id="banner_body"></ul>
    </div>
    <div id="wrapper">
    ${body}
    </div>
</div>
<div id="l-ft"> Copyright © ${.now?string('yyyy')} <a target="_blank" href="https://123.sankuai.com/km/page/28094974">基础架构团队</a>
    反馈:${customerServices?default('OCTO技术支持(infocto)')}</div>
<#include "footer.inc" >
<script>
    M.use('msgp-utils/topLinkEvent', function (Y) {
        var key = '${appkey!""}';
        var list = [<#list apps![] as app>'${app}',</#list>];
        Y.msgp.utils.topLinkEvent(key, list);
    });
</script>
<script>
    var uid = '${_currentUser.id!0}';
    !function (e, n, a, t) {
        var i = window, r = document, c = "_MeiTuanALogObject";
        if (i[c] = a, !i[a]) {
            var s = function () {
                return s.q.push(arguments), s
            };
            s.q = s.q || [], s.v = n, s.l = +new Date, i[a] = i[a] || s;
            var u = r.getElementsByTagName("head")[0], d = r.createElement("script");
            d.defer = d.async = !0;
            var m = parseInt(+new Date / 3e5);
            d.src = ["/", e, "seed", n, m, "index.js"].join("/"), u.appendChild(d)
        }
    }("analytics.meituan.com", "stable", "Analytics");
    Analytics('use', 'data_sdk_octo', {uid: uid});
    Analytics('set', 'appnm', 'OCTO');
    Analytics('send', 'pv');
</script>
<script>
    // Get the station about online or offline.
    var isOffline = '${isOffline?string('true','false')}';

    $(document).ready(function () {
        if (isOffline == "true") { // It means host is "octo.sankuai.com" or "octo.st.sankuai.com".
            $(".onlineState").html("切换线上");
        }
        else { // It means host is "localhost" or "octo.test.sankuai.com" or "octo.test.sankuai.info".
            $(".onlineState").html("切换线下");
        }
    });
    $(".onlineState").click(function () {
        if (isOffline == "true") {
            $(location).attr('href', 'http://octo.sankuai.com/');
        }
        else {
            $(location).attr('href', 'http://octo.test.sankuai.com/');
        }
    });

    $("#banner").click(function () {
        this.style.display = 'none';
    });

    $.ajax({
        type: "GET",
        contentType: "application/json",
        url: "/common/banner/valid_message",
        dataType: 'json',
        success: function (result) {
            var messages = result.data;
            if (messages.length == 0) {
                $('#banner')[0].style.display = 'none';
            } else {
                $('#banner')[0].style.display = 'block';
                messages.forEach(function (item) {
                    var html = '';
                    if (item.messageType == 2) {
                        $('#banner').css("background-color", "#d04437");
                        html = '<li><i class="fa fa-warning" style="color: white; padding-right: 10px"></i><span style="font-weight: bold; color: white; font-size: 15px;">周知: </span><span style="padding-left: 5px; color: white; font-size: 15px;">' + item.messageBody + '</span></li>'
                    } else {
                        $('#banner').css("background-color", "#2E8B57");
                        html = '<li><i class="fa fa-info" style="color: white; padding-right: 10px"></i><span style="font-weight: bold; color: white; font-size: 15px;">提醒: </span><span style="padding-left: 5px; color: white; font-size: 15px;">' + item.messageBody + '</span></li>'
                    }
                    $('#banner_body').append(html);
                });
                scroll();
            }
        },
        error: function () {
            $('#banner').css("background-color", "#565958");
            $('#banner')[0].style.display = 'none';
        }
    });

    function scroll() {
        var banner = $("#banner");
        var bannerUl = banner.find('ul');
        var bannerLi = banner.find('li');
        var liLen = banner.find('li').length;
        var initHeight = bannerLi.first().outerHeight(true);

        var scrollTimer;
        banner.hover(function () {
                    clearInterval(scrollTimer);
                },
                function () {
                    scrollTimer = setInterval(function () {
                        scrollMessage(banner);
                    }, 5000);
                }).trigger("mouseleave");

        function scrollMessage(obj) {
            if (liLen <= 1) return;
            var self = arguments.callee;
            var bannerLiFirst = banner.find('li').first();
            bannerLiFirst.animate({
                        "marginTop": -initHeight
                    },
                    500,
                    function () {
                        clearInterval(scrollTimer);
                        bannerLiFirst.appendTo(bannerUl).css({marginTop: 0});
                        scrollTimer = setTimeout(self, 5000);
                    })
        }
    }

    function tipsClick() {
        var btn = $('#tipsCheck')[0],
                flag = btn.checked;
        if (flag) {
            $('#confirmBtn')[0].removeAttribute("disabled");
        } else {
            $('#confirmBtn')[0].setAttribute("disabled", "");
        }
    }

    function tipsack() {
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: "/personal/tipack",
            dataType: 'json',
            success: function (result) {
                $("#tips_dialog")[0].setAttribute("style", "display:none");
                $("#overlay-mask")[0].setAttribute("style", "display:none");
            },
            error: function () {
                $("#tips_dialog")[0].setAttribute("style", "display:none");
                $("#overlay-mask")[0].setAttribute("style", "display:none");
            }
        });
    }
</script>
</body>
</html>