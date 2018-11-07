<#--
 - 此文件由 mt-fe 模块维护，修改请绕行 mt-fe
 -->

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>${title}</title>

<script>
// pageload加载参数初始化, 页面最开始是设置
M = window.M || {};
M.gaAccount = '${cos_gaAccount!""}';

// http://performance.sankuai.com

<#if cos_mtaUUID??>
(function(a,b){function d(){var a=document.createElement("script");a.async=!0,a.src="//s0.meituan.net/vendor/js/mta.min.js";var b=document.getElementsByTagName("script")[0];b.parentNode.insertBefore(a,b)}if(a.MeituanAnalyticsObject=b,a[b]=a[b]||function(){(a[b].q=a[b].q||[]).push(arguments)},"complete"===document.readyState)d();else{var c=window.onload;window.onload=function(){d(),c&&c()}}})(window,"mta");
mta("create","${cos_mtaUUID}");
mta("send","page");
</#if>

if (typeof YUI_config === 'undefined') {
    YUI_config = {};
}
YUI_config['mt-beacon'] = {
    domainHost: 'sankuai.com',
    cdnDomainHost: 'meituan.net',
    data: {}
};
YUI_config['mt-log'] = {
    pageview: {
        data: {
            <#-- start -->
            st: (new Date()).getTime()
        }
    }
};
<#if __reqStart??>
YUI_config['mt-log'].__reqStart = ${__reqStart?c};
</#if>

// 根据cookie记录折叠/展开sidebar
(function() {
    var cookieMatch = document.cookie.match(/cos-ui-sidebar=([^;]*)/),
        cookieStatus = cookieMatch && cookieMatch[1],
        html;
    if (cookieStatus) {
        html = document.documentElement;
        html.className += ' main-nav-' + cookieStatus;
    }
}());
</script>

<#-- js方面的环境初始化 -->
<#include "jsdeps_all.inc" >

${head}
