<#--
 - 此文件由 mt-fe 模块维护，修改请绕行 mt-fe
 -->

<#assign
    isOnline = IS_ONLINE_FILE?? &&  IS_ONLINE_FILE
    needBrowserTip = !showHint??
/>

<script type="text/javascript">

<#if isOnline>
if (M.gaAccount) {
    var loadSeed = loadGA;
    <#if cos_siteKey?? && cos_siteKey == 'crm'>
        loadSeed = loadUA;
        YUI_config['track-ua'] = {
            isUA: true
        };
    <#else>
        YUI_config['track-ua'] = {
            isGA: true
        };
    </#if>
    if (window.addEventListener) {
        window.addEventListener('load', loadSeed, false);
    } else if (window.attachEvent) {
        window.attachEvent('onload', loadSeed);
    } else {
        loadSeed();
    }
}

function loadGA() {
    // ga参数初始化
    window._gaq = [
        [ '_setAccount', M.gaAccount ],
        [ '_trackPageview' ]
    ];
    var ga = document.createElement('script');
    ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('body')[0];
    s.appendChild(ga, s);
}
function loadUA() {
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', M.gaAccount, 'auto');
    ga('send', 'pageview');
}

<#else>

// 浏览器检测升级提醒
M.addCondition("cos-browser-upgrade-tip", "cos.core", {
    "test": function(Y) {
        if (Y.UA.android ||
            Y.UA.iphone ||
            Y.UA.ios ||
            (Y.UA.chrome && parseInt(Y.UA.chrome, 10) > 20)
        ) {
            return false;
        } else {
            return true;
        }
    },
    "trigger": "cos-pageload"
});
YUI_config.groups['cos.core'].modules['cos-browser-upgrade-tip'].skinnable = true;

</#if>

<#if isOnline>

(function() {
    var logConf = YUI_config['mt-log'];
    var ldata = logConf.pageview.data;

    var start = ldata.st;
    var reqtime;
    if (logConf.__reqStart) {
        reqtime = logConf.__reqStart;
    } else {
        reqtime = start;
    }
    ldata.rt = reqtime;
    ldata.dt = (new Date()).getTime();

    var bdata = YUI_config['mt-beacon'].data;
    // 用来校正浏览器时间
    bdata.time = ldata.dt + (ldata.rt - start);

    logConf.error = {
        disable: !YUI_config.throwFail
    };
})();

</#if>

YUI().use('cos-pageload', function(Y) {

    try {
        // onload/domready时的处理, 如记录时间和关闭mask等操作
        Y.mt.cos.PageLoad.init();

        <#if needBrowserTip>
        // 浏览器检测
        if (Y.mt.cos && Y.mt.cos.BrowserUpgradeTip) {
            YUI.Env.cosBrowserUpgradeTip = new Y.mt.cos.BrowserUpgradeTip();
        }
        </#if>
    } catch(err) {
        Y.error('cos.Pageload.init 发生错误', err, 'cos-pageload');
        return;
    }

    Y.use('ui-main', function(Y) {
        // cos.ui初始化
        Y.mt.ui.Main.init();
    });

    <#if cos_siteKey??>
        Y.use("mt-base", "track-ua", function(Y) {
            // 控件初始化
            Y.mt.widget.init();

            Y.track.ua();
        });
    </#if>

    <#if isOnline>
    Y.use('mt-log', function(Y) {
        Y.mt.log.pageview();
    });
    </#if>
});
</script>
