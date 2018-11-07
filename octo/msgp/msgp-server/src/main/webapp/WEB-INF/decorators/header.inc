<head>
    <script type="text/javascript">
        //app config init
        var APP_Config = {
            appKey: "msgp",
            jsModPrefix: "msgp",
            yuiVersion: "3.17.2",
            sentryDSN: "",
            gaAccount: "",
            mtaUUID: ""
        };
        var APP_ENV = <#include "env.inc"/>
    </script>
    <!-- js seed -->
    <script src="/static/js/yui-mt/yui-mt.js" type="text/javascript"></script>
    <link href="https://cs0.meituan.net/cosui/1.3.0/css/cosui.min.css" rel="stylesheet"/>
    <link href="/static/css/main.css" rel="stylesheet"/>
    <link href="/static/css/common.css" rel="stylesheet"/>
    ${head}
</head>
