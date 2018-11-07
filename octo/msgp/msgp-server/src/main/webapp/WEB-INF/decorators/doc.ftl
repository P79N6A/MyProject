<!DOCTYPE html>
<html lang="en">
<head>

<#include "header.inc" >
<script src="/static/js/echarts-plain.js"></script>
<link rel="dns-prefetch" href="//analytics.meituan.com" />
<link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon" />

<link href="https://cs0.meituan.net/cf/twitter-bootstrap/3.2.0/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cs0.meituan.net/msgp/template/css/font-awesome.min.css" rel="stylesheet">

<!-- Endless -->
<link href="https://cs0.meituan.net/msgp/template/css/endless.min.css" rel="stylesheet">
<link href="https://cs0.meituan.net/msgp/template/css/endless-skin.css" rel="stylesheet">

<style>
       #l-hd .logo { background: none; }
</style>

<style>
    aside{
        z-index: 90;
        width: 300px;
        padding: 92px 0 0 0;
    }
    aside .main-menu>ul>li>a {
        font-size: 12px;
    }
    .headline{
        margin-top: 50px;
    }
    #doc-sections section h1{
        margin-top:50px;
    }
    #main-container{
        padding:50px 50px 0px 50px;
        margin-left:300px;
    }
    table,th,td{
        font-size: 12px
    }
    .fuck_menu{
        display:none;
    }
    .search-block{
        position: fixed;
        z-index: 100;
        width: 300px;
        height: 30px;
        padding: 0 10px;
    }
    #apps_select{
        height: 30px
    }
    .main-menu {
        padding-top:30px;
    }
    .submenu{
        margin: 0;
    }
    #apps_select_auto{
        position: fixed !important;
    }
    #l-hd{
        position: fixed;
        z-index: 100;
    }
    .dropdown-menu{
        margin: 0;
    }
</style>

</head>
<body class="theme-cos yui3-skin-sam">
    <div id="doc">
        <div id="l-hd">
            <div class="l-hd-left">
                <div class="l-hd-main">
                    <#--<img src="/static/img/octo.png" />-->
                    <span class="logo">美团点评</span>
                    <span class="logo-sep"></span>
                    <h1><a href="/">服务治理平台</a></h1>
                </div>
                <#include "top_nav.ftl" > 
            </div>
            <div class="user-info">
                <ul>
                    <a href="http://x.sankuai.com/chat/zhangxi@meituan.com" target="_blank"><span class="fa fa-pencil-square-o"></span>意见反馈</a>
                    <li><a id="add_quick_nav" href="javascript:;"><span class="fa fa-plus"></span>添加</a></li>
                    <li class="sep"></li>
                    <li><a class="user-name ml0" href="javascript:;">Hi，${_currentUser.name!"nobody"}</a></li>
                    <#--
                    <li class="sep"></li>
                    -->
                    <li>
                        <a href="/logout" class="logout" title="退出">退出</a>
                    </li>
                </ul>
            </div>
        </div>
        <div id="wrapper">
            ${body}
        </div>
    </div>
    <script src="https://cs0.meituan.net/msgp/template/js/jquery-1.10.2.min.js"></script>
    <script src="https://cs0.meituan.net/cf/twitter-bootstrap/3.2.0/js/bootstrap.min.js"></script>
    <script src='https://cs0.meituan.net/msgp/template/js/uncompressed/holder.js'></script>
    <script src='https://cs0.meituan.net/msgp/template/js/modernizr.min.js'></script>
    <script src='https://cs0.meituan.net/msgp/template/js/jquery.popupoverlay.min.js'></script>
    <script src='https://cs0.meituan.net/msgp/template/js/jquery.slimscroll.min.js'></script>
    <script src='https://cs0.meituan.net/msgp/template/js/pace.min.js'></script>
    <script src='https://cs0.meituan.net/msgp/template/js/jquery.cookie.min.js'></script>
    <script src="https://cs0.meituan.net/msgp/template/js/endless/endless.js"></script>
    <#include "footer.inc" >
    <script>
        M.use('msgp-utils/topLinkEvent', function(Y){
            var key = '${appkey!""}';
            var list = [<#list apps![] as app>'${app}',</#list>];
            Y.msgp.utils.topLinkEvent( key, list);
        });
    </script>
    <script type="text/javascript">
        $('.dropdown').mouseover(function(e) {
            $(this).find('.dropdown-menu').show();
        })
        $('.dropdown').mouseout(function(e) {
            $(this).find('.dropdown-menu').hide();
        })
    </script>
    <script>
        var uid = '${_currentUser.id!0}'
        !function(e,n,a,t){var i=window,r=document,c="_MeiTuanALogObject";if(i[c]=a,!i[a]){var s=function(){return s.q.push(arguments),s};s.q=s.q||[],s.v=n,s.l=+new Date,i[a]=i[a]||s;var u=r.getElementsByTagName("head")[0],d=r.createElement("script");d.defer=d.async=!0;var m=parseInt(+new Date/3e5);d.src=["/",e,"seed",n,m,"index.js"].join("/"),u.appendChild(d)}}("analytics.meituan.com","stable","Analytics");
        Analytics('use', 'data_sdk_octo' , {uid: uid});
        Analytics('set', 'appnm', 'OCTO');
        Analytics('send', 'pv');
    </script>
</body>
</html>
