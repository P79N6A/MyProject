<#--
 - 此文件由 mt-fe 模块维护，修改请绕行 mt-fe
 -->

<#assign IS_ONLINE_FILE  = true >

<#assign
    cos_domainHost = 'meituan.net'
    cos_comboBase = 'http://jc.${cos_domainHost}/combo/?f='
    cos_baseUri = 'http://cs0.${cos_domainHost}/${cos_pkgName}/'
    cos_debug = false
    cos_min = '-min'
>
<#if !cos_yuiVersion??  ><#assign cos_yuiVersion    = '3.10.3' ></#if>

<#if RequestParameters?? && RequestParameters.cos_debug??>
    <#assign cos_debug = RequestParameters.cos_debug != 'false' >
</#if>
<#if RequestParameters?? && RequestParameters.cos_baseUri??>
    <#assign cos_baseUri = RequestParameters.cos_baseUri >
</#if>
<#if cos_debug>
    <#assign cos_min = '' >
</#if>

<#if cos_useUI ?? && cos_useUI == true>
<link href="${cos_baseUri}node_modules/mtfe_cos-ui/cos-ui${cos_min}.v{UI_VERSION}.css" rel="stylesheet">
</#if>

<script>
if (typeof YUI_config === 'undefined') {
    YUI_config = {};
}
YUI_config.yuiVersion = '${cos_yuiVersion}';
YUI_config.baseUri = '${cos_baseUri}';
YUI_config.comboBase = '${cos_comboBase}';
YUI_config.debug = ${cos_debug?string('true', 'false')};
YUI_config.combine = true;
<#--
  - TODO YUI_config.site 可以废除？
  -->
YUI_config.site = '${cos_siteKey}'.replace('mt', '');
YUI_config.cosPackage = {
    name: '${cos_pkgName}'
};
<#if cos_YUI_config??>
<@cos_YUI_config isOnline=IS_ONLINE_FILE />
</#if>
</script>
<#if cos_sentry_dsn??>
<script>
YUI_config['track-raven'] = { dsn: '${cos_sentry_dsn}' };
</script>
<script src="${cos_baseUri}node_modules/mtfe_cos_track/raven/raven-all${cos_min}.js"></script>
</#if>

<script src="${cos_baseUri?replace(cos_pkgName + '/', '')}yui/${cos_yuiVersion}/build/yui/yui${cos_min}.js"></script>

<#--
  - 前端小流量上线, 非小流量指定yui-config的版本号支持
  -->
<#if ! cos_isSmallFlow?? || ! cos_yuiConfigVersion?? || cos_isSmallFlow == true >
<script src="${cos_baseUri}yui-config-{SITE_GROUP_KEY}${cos_min}.v{YUI_CONFIG_VERSION}.js"></script>
<#else>
<script src="${cos_baseUri}yui-config-{SITE_GROUP_KEY}${cos_min}.v${cos_yuiConfigVersion}.js"></script>
</#if>

