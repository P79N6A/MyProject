<!doctype html>
<html>
<#include "header.inc" >
<body class="theme-cos yui3-skin-sam">
<div id="doc">
    <div id="l-hd">
        <div class="l-hd-left">
            <div class="l-hd-main">
                <span class="logo"></span>
                <h1><a href="/"><img src="/static/img/logo.png"></a></h1>
            </div>
        </div>
        <ul class="user-info">
            <#if __user__??>
                <li>欢迎您，<a class="user-name" href="javascript:void(0);" title="个人中心">${__user__.name!"UNKNOW_USER"}</a></li>
                <li><a href="/logout" title="退出"><i class="fa fa-power-off fa-white"></i> 退出</a></li>
            </#if>

        </ul>
    </div>
    <div id="banner" class="banner" style="display: grid;background-color: #d04437">
        <ul id="banner_body"></ul>
        <div>
            <i class="fa fa-warning" style="color: white; padding-right: 10px; padding-left: 20px"></i>
            <span style="font-weight: bold; color: white; font-size: 15px;">周知: </span>
            <span style="padding-left: 5px; color: white; font-size: 15px;">11月10日后MCC V1版本的管理平台将下线，所有配置将无法查看和修改，请尽快升级到MCC V2版本！</span>
        </div>
    </div>
    <div id="wrapper" class="with-sidebar">
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span12">
                    ${body}
                </div>
            </div>
        </div>
    </div>
    <div id="l-ft">
        &#64; 2014 meituan
    </div>
</div>
<#include "footer.inc" >
</body>
</html>
