<#--<title>用户自检</title>-->
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
<#include "/topLinkEvent.ftl" >

<#include "checker_nav.ftl"/>
    <!-- TODO: 概要页面、提供者页面、消费者页面切换 -->
<#--<script-->
<#--src="https://jc.meituan.net/combo/?f=fecore/w-tab/w-tab-min.v42e1f75b.js;msgp/utils/hashchange-min.v3e3ca5fd.js;msgp/serviceopt/optTab-min.v5735df18.js;msgp/service/commonMap-min.vddd39e40.js"-->
<#--type="text/javascript"></script>-->

    <script src="/static/js/tooltip.js"></script>
    <script>
        M.use('msgp-checker/userCheck', function(Y){
            var key = '${appkey}';
            Y.msgp.checker.userCheck( key );
        });
    </script>

</div>
