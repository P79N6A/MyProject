<title>访问受限</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <h3 class="page-header">访问受限</h3>
    <hr/>
    <h3>
    <#if appkey??>
        您对此服务(${appkey})没有访问权限，申请权限请联系
        <#list owners as owner> ${owner.name}(${owner.login}) </#list>
        。
    <#else>
        您对此视图服务(${owt})没有访问权限，申请权限请联系
        <#list owners as owner> ${owner}</#list>
        。
    </#if>
    </h3>

</div>