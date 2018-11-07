/**
 * Created by yves on 17/7/11.
 */
/* jshint indent : false */
M.add('msgp-serviceopt/optXmdlog', function (Y) {
    Y.namespace('msgp.serviceopt').optXmdlog = optXmdlog;

    var xmdlogWrapper = Y.one('#wrap_xmdlog');
    var appkey,
        showOverlay,
        showContent;

    function optXmdlog(key, func1, func2) {
        appkey = key;
        showOverlay = func1;
        showContent = func2;
        
        document.title = "日志级别调整";
        //新窗口打开会被拦截
        document.getElementById("xmdlog_link").click();
        showContent(xmdlogWrapper);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'msgp-utils/common'
    ]
});
    