<!doctype html>
<html>
<head>
    <#assign cos_yuiVersion = '3.13.0' >

    <#-- 静态资源加载时的前缀, group等信息默认都和该参数有关, 配置完后，JS模块默认已"ct-"开头, group默认使用"mtct", 会影响文件加载的路径 -->
    <#assign cos_siteKey = 'ct'/>

    <#-- 请修改为本系统的GA账号, 如无，请找相关人申请 -->
    <#assign cos_gaAccount = 'UA-28174807-3'/>
    <#assign cos_useUI = true />
    <#include "header.core.inc" >
</head>
<body class="theme-cos">
<div id="doc">
    <div id="bd" class="clearfix">
        <div id="l-hd">
            <div class="l-hd-left">
                <div class="l-hd-main">
                    <span class="logo"></span>
                    <h1><a href="#">xxx系统</a></h1>
                </div>
                <div class="navbar navbar-inverse">
                    <div class="navbar-inner">
                        <ul class="nav">
                            <li class="active"><a href="./">首页</a></li>
                            <li class="dropdown">
                                <a href="#" class="dropdown-toggle" data-hover="dropdown">通用样式库 <b class="caret"></b></a>
                                <ul class="dropdown-menu">
                                    <li><a href="#">menu1</a></li>
                                </ul>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <ul class="user-info">
                <li>欢迎您，
                    <a href="http://www.meituan.com/acl/user/myprofile">xxx</a>
                </li>
            </ul>
        </div>
        <div class="wrapper" class="with-sidebar-icon">
            <div id="main-nav" class="main-nav-fixed">
                <div class="navigation">
                    <div class="toggle-view">
                        <i class="fa fa-caret-left"></i>
                    </div>
                    <ul class="nav">
                        <li class="nav-item-typography active">
                            <a href="#">
                                <i class="fa fa-font"></i>
                                <span>typography</span>
                            </a>
                        </li>

                        <li class="nav-item-button ">
                            <a href="#">
                                <i class="fa fa-star"></i>
                                <span>button</span>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
        <div id="l-bd">
            <div class="container-fluid">
                ${body}
            </div>
        </div>
        <div id="l-ft">
            <p>
            <span class="ft-time">页面响应时间：${_timeCost!""}ms</span>
            ©2014 meituan
            <span class="ft-time">服务器：${_hostname!""}</span>
            </p>
        </div>
    </div>
</div>
<#include "footer.core.inc" >
</body>
</html>
