<head>
    <title>${title!'美团配置中心'}</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport"/>
    <link rel="shortcut icon" href="http://www.meituan.com/favicon.ico" type="image/x-icon"/>
    <link rel="Bookmark" href="http://www.meituan.com/favicon.ico" type="image/x-icon"/>
    <script
        src="../../static/js/underscore-min.js"
        type="text/javascript"></script>
    <script type="text/javascript">
        //app config init
        var APP_Config = {
            appKey: "mtconfig",
            jsModPrefix: "config",
            yuiVersion: "3.17.2",
            sentryDSN: "",
            gaAccount: "",
            mtaUUID: ""
        };
        var APP_ENV = <#include "env.inc"/>
    </script>
    <!-- js seed -->
    <script
        src="http://jc.meituan.net/combo/?f=/yui/3.17.2/build/yui/yui-min.js;slim-config/1.2.43/slim-config.js;yuipagecore/1.0.14/header.js"
        type="text/javascript"></script>
    <link href="http://cs0.meituan.net/cosui/1.3.0/css/cosui.min.css" rel="stylesheet"/>
    <link href="/static/css/config.css" type="text/css" rel="stylesheet">
    <script type="text/javascript">
        if (APP_Config.env !== "production") {
            M.applyConfig({
                base: "http://fe.yuibase.sankuai.com/",
                yuiVersion: "3.17.2",
                noVersion: true,
                root: "yui/3.17.2/build/"
            });
        }
        ;
        M.addGroups({
            knockout: {
                base: "http://cs0.meituan.net/",
                root: 'vendor/',
                prefixs: 'mvvm'
            }
        });
    </script>

    ${head}
</head>
