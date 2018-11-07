<!DOCTYPE html>
<html lang="en">

<head>
<link href="https://cs0.meituan.net/cf/twitter-bootstrap/3.2.0/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cs0.meituan.net/msgp/template/css/font-awesome.min.css" rel="stylesheet">

<!-- Endless -->
<link href="https://cs0.meituan.net/msgp/template/css/endless.min.css" rel="stylesheet">
<link href="https://cs0.meituan.net/msgp/template/css/endless-skin.css" rel="stylesheet">

<style>
    aside{
        z-index: 90;
        width:300px;
        padding:50px 0 0 0;
    }
    aside .main-menu>ul>li>a {
        font-size:12px;
    }
    .headline{
    	margin-top:30px;
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
    	width: 280px;
    	padding: 0 10px;
    }
    .main-menu {
    	padding-top:30px;
    }
    #apps_select_auto{
    	position: fixed !important;
    }
    </style>

<script type="text/javascript">
//app config init
var APP_Config = {
    appKey: "msgp",
    jsModPrefix: "msgp",
    yuiVersion: "3.17.2",
    sentryDSN: "",
    gaAccount: "",
    mtaUUID:  ""
};
var APP_ENV = <#include "env.inc"/>
</script>
<!-- js seed -->
<script src="https://jc.meituan.net/combo/?f=/yui/3.17.2/build/yui/yui-min.js;slim-config/1.1.4/slim-config-min.js;yuipagecore/1.0.10/header.js" type="text/javascript"></script>
${head}
</head>

<body class="overflow-hidden">
	<div id="wrapper" class="preload">
		<div id="top-nav" class="skin-1 fixed">
			<div class="brand">
				<span>美团点评</span>
				<span class="text-toggle"> 服务文档</span>
			</div><!-- /brand -->
			<a class="fuck_menu" id="add_quick_nav"></a>
			<a class="fuck_menu" id="menus"></a>
			<a class="fuck_menu" id="quick_nav"></a>
			<a class="fuck_menu" id="add_quick_nav"></a>

			<ul class="nav-notification clearfix">
				<li class="profile dropdown">
					<a class="dropdown-toggle" data-toggle="dropdown" href="#">
						<strong>Hi,${_currentUser.name!"nobody"}</strong>
						<span><i class="fa fa-chevron-down"></i></span>
					</a>
					<ul class="dropdown-menu">
						<li><a tabindex="-1" class="main-link logoutConfirm_open" href="/logout"><i class="fa fa-lock fa-lg"></i> 退出登录</a></li>
					</ul>
				</li>
			</ul>
		</div><!-- /top-nav-->
		${body}
	</div><!-- /wrapper -->

	

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
	<script>
	    var uid = '${_currentUser.id!0}'
	    !function(e,n,a,t){var i=window,r=document,c="_MeiTuanALogObject";if(i[c]=a,!i[a]){var s=function(){return s.q.push(arguments),s};s.q=s.q||[],s.v=n,s.l=+new Date,i[a]=i[a]||s;var u=r.getElementsByTagName("head")[0],d=r.createElement("script");d.defer=d.async=!0;var m=parseInt(+new Date/3e5);d.src=["/",e,"seed",n,m,"index.js"].join("/"),u.appendChild(d)}}("analytics.meituan.com","stable","Analytics");
	    Analytics('use', 'data_sdk_octo' , {uid: uid});
	    Analytics('set', 'appnm', 'OCTO');
	    Analytics('send', 'pv');
	</script>
</body>
</html>
