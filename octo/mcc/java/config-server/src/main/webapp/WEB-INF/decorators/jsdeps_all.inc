<#--
 - 此文件由 mt-fe 模块维护，修改请绕行 mt-fe
 -
 - 请注意, 该文件只在线下生效，所有配置对线上不起作用
 - 要使用自己的开发环境, 请在header.inc文件中配置cos_baseUri即可, 避免修改此文件，导致重启被覆盖
 -->
<#if !cos_pkgName??>
    <script>
    alert('请定义 cos_pkgName, 与系统静态资源(static)中 package.json 中 name 命名一致');
    </script>
</#if>

<#if !cos_yuiVersion??  ><#assign cos_yuiVersion = '3.13.0' ></#if>
<#if !cos_static??      ><#assign cos_static = "static" ></#if>
<#if !cos_baseUri??     >
    <#assign cos_baseUri = 'http://develop.cs0.meituan.net/${cos_pkgName}/' ></#if>
<#if !cos_comboBase??   ><#assign cos_comboBase = 'http://develop.jc.meituan.net/combo/?f=' ></#if>

<#if cos_useUI?? && cos_useUI == true>
<link href="${cos_baseUri}node_modules/mtfe_cos-ui/cos-ui.css" rel="stylesheet">
</#if>

<script>
if (typeof YUI_config === 'undefined') {
    YUI_config = {};
}
YUI_config.yuiVersion = '${cos_yuiVersion}';
YUI_config.baseUri = '${cos_baseUri}';
YUI_config.comboBase = '${cos_comboBase}';
YUI_config.combine = false;
YUI_config.debug = true;
<#--
  - TODO YUI_config.site 可以废除？
  -->
YUI_config.site = '${cos_siteKey}';
YUI_config.cosPackage = {
    name: '${cos_pkgName}'
};
<#if cos_YUI_config??>
<@cos_YUI_config isOnline=false/>
</#if>
</script>

<#--
  - 使用 replace 替换 cos_pkgName
  - 本地开发中如果遇到 yui 也在 baseUri 中，则也同样可以支持
  - 因为本地环境是  http://localhost:port/static/
  -->
<script src="${cos_baseUri?replace(cos_pkgName + '/', '')}yui/${cos_yuiVersion}/build/yui/yui.js"></script>
<script src="${cos_baseUri}node_modules/mtfe_yui-config/yui-config.js"></script>

