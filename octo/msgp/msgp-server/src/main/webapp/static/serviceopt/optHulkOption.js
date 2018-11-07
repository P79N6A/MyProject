M.add('msgp-serviceopt/optHulkOption', function (Y) {
    Y.namespace('msgp.serviceopt').optHulkOption = optHulkOptioan;
    var inited = false;

    var appkey,
        showOverlay,
        showContent;

    var hulkOptionWrapper = Y.one('#wrap_hulkOption');
    var hulkOrManu = hulkOptionWrapper.one('#hulk_or_manu');
    var hulkscale = hulkOptionWrapper.one('#wrap_hulkscale');
    var manuscale = hulkOptionWrapper.one('#wrap_manuscale');

    function optHulkOptioan(key, func1, func2) {
        appkey = key;
        showOverlay = func1;
        showContent = func2;
        init(appkey);
        showContent(hulkOptionWrapper);
        $("#wrap_manuscale").css('display', 'none');
        hulkOptionWrapper.all('#hulk_or_manu a').removeClass('btn-default');
        $("#wrap_manuscale").hide();
        $("#wrap_hulkscale").css('display', 'block');
        function showContent() {
            Y.one('#hulkPolicy_content').one('.content-overlay').hide();
            Y.one('#hulkPolicy_content').one('.content-body').show();
        }
        function showOverlay() {
            Y.one('#hulkPolicy_content').one('.content-body').hide();
            Y.one('#hulkPolicy_content').one('.content-overlay').show();
        }
        var appkey = key;
        Y.msgp.serviceopt.optHulkPolicy(appkey, showOverlay, showContent);
    }

    function init(key) {
        hulkOptionWrapper.delegate('click', function () {
            if (this.getAttribute("value") == 1) {
                hulkOptionWrapper.all('#hulk_or_manu a').removeClass('btn-primary');
                this.addClass("btn-primary");
                $("#wrap_hulkscale").css('display', 'none');
                $("#wrap_manuscale").css('display', 'block');
                function showContent() {
                    Y.one('#manuScale_content').one('.content-overlay').hide();
                    Y.one('#manuScale_content').one('.content-body').show();
                }
                function showOverlay() {
                    Y.one('#manuScale_content').one('.content-body').hide();
                    Y.one('#manuScale_content').one('.content-overlay').show();
                }
                var appkey = key;
                Y.msgp.serviceopt.optManuScaleOut(appkey, showOverlay, showContent);

            }
            if (this.getAttribute("value") == 0) {
                hulkOptionWrapper.all('#hulk_or_manu a').removeClass('btn-primary');
                this.addClass("btn-primary");
                $("#wrap_manuscale").css('display', 'none');
                $("#wrap_hulkscale").css('display', 'block');
                function showContent() {
                    Y.one('#hulkPolicy_content').one('.content-overlay').hide();
                    Y.one('#hulkPolicy_content').one('.content-body').show();
                }
                function showOverlay() {
                    Y.one('#hulkPolicy_content').one('.content-body').hide();
                    Y.one('#hulkPolicy_content').one('.content-overlay').show();
                }
                var appkey = key;
                Y.msgp.serviceopt.optHulkPolicy(appkey, showOverlay, showContent);

            }
        }, "#hulk_or_manu a")
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-serviceopt/optHulkPolicy-version0.0.3',
        'msgp-serviceopt/optManuScaleOut'
    ]
});
