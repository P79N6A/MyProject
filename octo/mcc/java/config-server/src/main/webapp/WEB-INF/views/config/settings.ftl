<div>
    当前节点版本:${structVersion}<br>
<#switch structVersion>
	<#case "2">
        可否使用第三层自定义节点:
		<#if canUseThirdLevel="true">
            <a class="btn btn-primary" href="/config/spaces/${spaceName}/settings/set/third_level/false">可以</a>
		<#else>
            <a class="btn btn-primary" href="/config/spaces/${spaceName}/settings/set/third_level/true">不可以</a>
		</#if>
		<#break>
	<#default>
        不支持空间配置设置
</#switch>
</div>