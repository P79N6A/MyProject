M.add('msgp-data/data_trace', function (Y) {
    Y.namespace('msgp.data').data_trace = data_trace;
    function data_trace(_appkey) {
        appkey = _appkey;
        document.title = "上下游分析";
        //新窗口打开会被拦截
        document.getElementById("mtrace_link").click();
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'template',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
