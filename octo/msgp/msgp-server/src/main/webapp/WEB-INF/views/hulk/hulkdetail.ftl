<title>弹性伸缩配置</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

    <#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
    <#include "/topLinkEvent.ftl" >

    <#include "sub_nav.ftl"/>
    <!-- TODO: 概要页面、提供者页面、消费者页面切换 -->
<script>
     M.use('msgp-hulk/hulkdetail', function(Y){
        var key = '${appkey}';
        Y.msgp.hulk.hulkdetail( key );
    });
</script>
</div>
