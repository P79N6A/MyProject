<title>服务详情</title>
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<link type="text/css" rel="stylesheet" href="/static/css/select2.css"/>
<style>
    .ui-autocomplete {
        max-width: 220px;
        max-height: 220px;
        overflow: auto;
    }
</style>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#--
<div class="sub-title">
    <h1>服务治理</h1>
</div>
-->
<#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
<#include "/topLinkEvent.ftl" >

<#include "sub_nav.ftl"/>
<input id = "isOffline" value="${isOffline?string('true','false')}" style="display: none"/>
    <!-- TODO: 概要页面、提供者页面、消费者页面切换 -->
<#--<script-->
<#--src="https://jc.meituan.net/combo/?f=fecore/w-tab/w-tab-min.v42e1f75b.js;msgp/utils/hashchange-min.v3e3ca5fd.js;msgp/servicedetail/detailTab-min.v6cda55a6.js;msgp/service/commonMap-min.vddd39e40.js"-->

<#--type="text/javascript"></script>-->
<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script src="/static/js/tooltip.js"></script>
<script type="text/javascript" src="/static/js/select2.min.js"></script>
<script>
    var key = '${appkey}';
    M.use('msgp-servicedetail/detail', function(Y){
        Y.msgp.servicedetail.detail(key);
    });
</script>
</div>
