<!doctype html>
<html>
<head>
    <#include "header.inc" >
    <script src="/static/js/echarts-plain.js"></script>
    <link rel="shortcut icon" href="/static/favicon.ico" type="image/x-icon" />
</head>
<body class="theme-cos">
    <div id="wrapper" style="min-height: 100%">
        ${body}
    </div>
    <div id="l-ft">© Copyright ${.now?string('yyyy')} by meituan</div>
    <#include "footer.inc" >
</body>
</html>
