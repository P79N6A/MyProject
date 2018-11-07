M.add('msgp-worth/reportTabNavigation', function (Y) {
    Y.namespace('msgp.worth').reportTabNavigation = reportTabNavigation;
    var _report_style = "day_count";
    var tab;
    var validHash = ['day_count', 'business_count', 'module_count', 'coverage_count', 'integrated_count'];
    var preHash;
    var _business;


    var map = {
        day_count: 'daycount',
        business_count: 'businesscount',
        module_count: 'modulecount',
        coverage_count: 'coveragecount',
        integrated_count: 'integratedcount'
    }

    function reportTabNavigation(report_style, business) {
        _business = business;
        _report_style = report_style;
        initPageHashAndTab();
        Y.msgp.utils.hashchange(hashChangeCallback);
    }

    function initPageHashAndTab() {
        //init hash
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#' + _report_style;
        }
        //init tab
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
        Y.all('#tab_trigger li').on('click', function () {
            Y.one('#wrap_day_count').show();
        });
    }

    function hashChangeCallback(hash) {
        if (preHash == hash) {
            return;
        }
        switchToTab(hash);
        tab.select({
            trigger: Y.one('a[href="#' + hash + '"]').ancestor('li'),
            sheet: Y.one('#wrap_' + hash)
        });
        preHash = hash;
        //隐藏当前hash,显示第一个hash
        Y.one('#wrap_' + hash).hide();
        Y.one('#wrap_day_count').show();
    }

    function switchToTab(hash) {
        _report_style = hash;
        var hashIndex = Y.Array.indexOf(validHash, hash);
        if (hashIndex === -1) {
            throw new Error('Invalid hash value');
        }
        if (map[hash]) {
            var mapvalue = map[hash];
            var div_report = Y.one('#div_daycount');
            var text_report = Y.one('#text_' + mapvalue).get("value");
            div_report.setHTML(text_report);
            Y.msgp.worth[mapvalue](_report_style, _business);
            //隐藏不需要的搜索条件
            var hiddenClass = "span_" + hash.split("_")[0];
            Y.all("." + hiddenClass).hide();
            Y.all('span#span_' + hash).show();
        } else {
            throw new Error('Invalid hash value');
        }

    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'msgp-utils/hashchange',
        'msgp-worth/modulecount',
        'msgp-worth/businesscount',
        'msgp-worth/daycount',
        'msgp-worth/integratedcount',
        'msgp-worth/coveragecount'
    ]
});
M.use('msgp-worth/reportTabNavigation', function (Y) {
    Y.msgp.worth.reportTabNavigation(report_style, business);
});
