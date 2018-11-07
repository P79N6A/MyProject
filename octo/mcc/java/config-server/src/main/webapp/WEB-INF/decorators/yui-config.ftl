<#macro cos_YUI_config isOnline=false>
    <#if isOnline>
    <#local
        devStaticPath = "'"
        staticPath = "'" 
    >
    <#else>
    <#local
        devStaticPath = "'${cos_baseUri}"
        staticPath = "'http://' + location.host + '/static/"
    >
    </#if>
YUI_config.cosGroups = [
    {
        name: 'cos.core',
        prefix: 'cos-',
        path: ${devStaticPath}node_modules/mtfe_cos-core/'
    },
    {
        name: 'fecore',
        prefix: [ 'mt-', 'w-', 'p-', 'e-', 'uix-' ],
        path: ${devStaticPath}node_modules/mtfe_fe.core/'
    },
    {
        name: 'cos.ui',
        prefix: 'ui-',
        path: ${devStaticPath}node_modules/mtfe_cos-ui/'
    },
    {
        name: 'track',
        prefix: 'track-',
        path: ${devStaticPath}node_modules/mtfe_cos_track/'
    },
    {
        name: 'mtconfig',
        prefix: 'config-',
        path: ${staticPath}'
    }
];
</#macro>
