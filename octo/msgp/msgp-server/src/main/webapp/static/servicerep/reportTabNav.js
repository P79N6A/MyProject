M.add('msgp-servicerep/reportTabNav', function (Y) {
    Y.namespace('msgp.servicerep').reportTabNav = reportTabNav;

    var _owt, _day;
    //斑马配送、电影、酒店数据、团购web端、支付、美食、外卖、酒店
    var _owtName = {
        web: '到店餐饮BG_团购业务线',
        mobile: '到店餐饮BG_移动端',
        meishi: '到店餐饮BG_美食业务线',
        srq: '到店餐饮BG_智能餐厅',

        sjst: '餐饮生态平台BG_商家生态',

        waimai: '外卖配送BG_外卖业务线',
        banma: '外卖配送BG_配送业务线',
        shangchao: '外卖配送BG_商超业务线',
        sc: '外卖配送BG_商超业务线',//废弃
        wmarch: '外卖配送BG_外卖基础架构',
        xianguo: '外卖配送BG_鲜果',
        retail: '外卖配送BG_零售研发',

        movie: '猫眼电影BG_电影业务线',

        hotel: '酒店旅游BG_酒店业务线',
        trip: '酒店旅游BG_门票业务线',
        tower: '酒店旅游BG_目的地业务线',
        flight: '酒店旅游BG_机票业务线',
        ia: '酒店旅游BG_智能住宿业务线',
        hotel_train: '酒店旅游BG_火车票业务线',
        train: '酒店旅游BG_火车票业务线',
        travel: '酒店旅游BG_酒旅平台',
        hbdata: '酒店旅游BG_酒店数据',
        tdc: '酒店旅游BG_TDC', //废弃
        icb: '酒店旅游BG_创新业务',
        oversea: '酒店旅游BG_酒店海外',

        xm: '企业平台BG_大象业务线',
        mit: '企业平台BG_技术学院',
        it: '企业平台BG_企业平台',

        pay: '金融服务BG_支付业务线',
        fd: '金融服务BG_金融发展',
        zc: '金融服务BG_招财猫',

        adp: '广告BG_广告平台',

        bp: '平台BG_商户挖据',
        giant: '平台BG_流量',
        fe: '平台BG_平台终端',
        dataapp: '平台BG_数据应用',
        wpt: '平台BG_服务平台',

        rc: '技术工程BG_风控',
        inf: '技术工程BG_基础架构',
        mx: '技术工程BG_基础终端',
        dba: '技术工程BG_DBA',
        tair: '技术工程BG_TAIR',
        cloudprivate: '技术工程BG_私有云',
        cloudpublic: '技术工程BG_公有云',
        sec: '技术工程BG_信息安全',
        cloudoffice: '技术工程BG_办公云',
        cloud: '技术工程BG_云计算',
        sre: '技术工程BG_服务运维',
        ep: '技术工程BG_工程质量',
        ee: '技术工程BG_工程效率',
        network: '技术工程BG_network',//废弃
        data: '技术工程BG_数据组',
        gct: '到店综合BG_综合品类'

    }
    var nameOwt = {};
    var tab;
    var startInput = Y.one('#start_time');
    var validHash = ["availability","topqps", "topqpstp", "toptp", "client", "server", "error", "qpspeak", "idc"];
    var validHashDesc = ["QPS最高服务", "服务QPS最高的TP90", "日均性能数据", "依赖最多服务", "被依赖最多服务", "服务错误日志", "服务QPS峰值均值", "机房流量分布"];

    var map = {
        availability : 'availability',
        topqps: 'topqps',
        topqpstp: 'topqpstp',
        toptp: 'toptp',
        client: 'client',
        server: 'server',
        error: 'error',
        qpspeak: 'qpspeak',
        idc: 'idc'
    };

    function reportTabNav(owtList, owt, day) {
        //initOwt(owtList);
        initDatePicker();
        initNameOwt();

        _owt = owt;

        _day = day;
        initPageHashAndTab();
        Y.msgp.utils.hashchange(hashChangeCallback);
        Y.one('#query_btn').on('click', function () {
            var hash = location.hash.slice(1);
            _day = startInput.get("value");
            var owtVal = _owt
            if (nameOwt[_owt]) {
                owtVal = nameOwt[_owt];
            }
            Y.msgp.servicerep[map[hash]](owtVal, _day);
            showWeekTips()
        });
        bindDescription();
        showWeekTips();
        var owts = [];
        Y.Array.each(owtList, function (item, index) {
            var name = _owtName[item] ? _owtName[item] : item
            owts.push({id: index, name: name});
        });
        var defaultName = _owtName[owt] ? _owtName[owt] : owt;
        Y.one('#owtSelect').set("value", defaultName);
        autoCompleteList(owts.sort())
    }

    function initNameOwt() {
        for (var prop in _owtName) {
            var value = _owtName[prop];
            nameOwt[value] = prop;
        }
    }

    function initDatePicker() {
        sdate = new Y.mt.widget.Datepicker({
            node: startInput,
            showSetTime: false
        });
        //startInput.set('value', Y.mt.date.formatDateByString(_day, 'yyyy-MM-dd'));
    }

    function showWeekTips() {
        var days = getWeek(_day)
        Y.one('#week_tips').setHTML("<b>报告周期:</b> " + days[0] + " 至 " + days[days.length - 1])
    }

    function initOwt(list) {
        list = ['waimai', 'banma', 'movie', 'hotel', 'pay', 'meishi', 'web', 'hbdata'];
        var obj = [];
        for (var i = 0, l = list.length; i < l; i++) {
            var nameVal = _owtName[list[i]]
            if (nameVal) {
                obj.push({id: i, name: nameVal});
            }
        }
        if (obj.length && Y.one('#owts_select') != null) {
            AutoCompleteOwtList(obj);
        }
    }

    function initPageHashAndTab() {
        //init hash
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#availability';
        }

        //init tab
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
    }

    function hashChangeCallback(hash) {
        switchToTab(hash);
        //set the tab current when the change is caused by back or forward
        //but this will be excess twice when user click the tab trigger
        tab.select({
            trigger: Y.one('a[href="#' + hash + '"]').ancestor('li'),
            sheet: Y.one('#wrap_' + hash)
        });

        setDocumentTitle(hash);
    }

    function switchToTab(hash) {
        if (Y.Array.indexOf(validHash, hash) === -1) {
            throw new Error('Invalid hash value');
        }
        if (map[hash]) {
            var owt = getOwt();
            Y.msgp.servicerep[map[hash]](owt, _day, showOverlay, showContent);
        } else {
            throw new Error('Invalid hash value');
        }

        function showOverlay(wrapper) {
            wrapper.one('.content-body').hide();
            wrapper.one('.content-overlay').show();
        }

        function showContent(wrapper) {
            wrapper.one('.content-overlay').hide();
            wrapper.one('.content-body').show();
        }
    }

    function setDocumentTitle(hash) {
        document.title = '服务治理周报 - ' + validHashDesc[Y.Array.indexOf(validHash, hash)];
    }

    function AutoCompleteOwtList(obj) {
        new Y.mt.widget.AutoCompleteList({
            id: "owts_select_auto",
            node: Y.one("#owts_select"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            more: "",
            callback: function (data) {
                var owt = data.name;
                console.log(owt);
                _owt = owt;
            }
        });
        Y.one("#owts_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "400px");
        Y.one("#owts_select_auto").one(".widget-autocomplete-tip").setHTML("输入部门名搜索或向下滚动选择");
        Y.one("#owts_select_auto").one(".widget-autocomplete-menu-operator").remove();
    }

    function getWeek(day) {
        var url = '/repservice/week';
        var request = Y.io(url, {
            method: 'get',
            sync: true,
            data: {day: day}

        });
        var days = Array();
        var ret = Y.JSON.parse(request.responseText);
        if (ret.isSuccess) {
            var data = ret.data;
            if (Y.Lang.isArray(data) && data.length !== 0) {
                days = data;
            }
        }
        return days;
    }

    function getOwt() {
        var owtVal = _owt
        if (nameOwt[_owt]) {
            owtVal = nameOwt[_owt];
        }
        return owtVal;
    }

    var avg_qps_desc_Dialog, max_qps_desc_Dialog, min_qps_desc_Dialog, avg_host_qps_desc_Dialog, max_host_desc_Dialog;

    function bindDescription() {
        var qpspeakWrap = Y.one("#qpspeak_wrap");
        qpspeakWrap.delegate('click', function () {
            avg_qps_desc_Dialog = avg_qps_desc_Dialog ? avg_qps_desc_Dialog : new Y.mt.widget.CommonDialog({
                id: 'avg_qps_desc',
                title: '服务QPS均值',
                width: 320
            });
            var descContent = '报告周期内 请求数 / 时间(秒)';
            avg_qps_desc_Dialog.setContent(descContent);
            avg_qps_desc_Dialog.show();
        }, '#avg_qps_desc');

        qpspeakWrap.delegate('click', function () {
            max_qps_desc_Dialog = max_qps_desc_Dialog ? max_qps_desc_Dialog : new Y.mt.widget.CommonDialog({
                id: 'max_qps_desc',
                title: '高峰期服务QPS',
                width: 320
            });
            var descContent = '报告周期内 某一分钟的最大平均QPS';
            max_qps_desc_Dialog.setContent(descContent);
            max_qps_desc_Dialog.show();
        }, '#max_qps_desc');

        qpspeakWrap.delegate('click', function () {
            min_qps_desc_Dialog = min_qps_desc_Dialog ? min_qps_desc_Dialog : new Y.mt.widget.CommonDialog({
                id: 'min_qps_desc',
                title: '低峰期服务QPS',
                width: 320
            });
            var descContent = '报告周期内 某一分钟的最小平均QPS';
            min_qps_desc_Dialog.setContent(descContent);
            min_qps_desc_Dialog.show();
        }, '#min_qps_desc');

        qpspeakWrap.delegate('click', function () {
            avg_host_qps_desc_Dialog = avg_host_qps_desc_Dialog ? avg_host_qps_desc_Dialog : new Y.mt.widget.CommonDialog({
                id: 'avg_host_qps_desc',
                title: '单节点QPS均值',
                width: 320
            });
            var descContent = '报告周期内 请求数 / 时间 / 机器节点数';
            avg_qps_desc_Dialog.setContent(descContent);
            avg_qps_desc_Dialog.show();
        }, '#avg_host_qps_desc');

        qpspeakWrap.delegate('click', function () {
            max_host_desc_Dialog = max_host_desc_Dialog ? max_host_desc_Dialog : new Y.mt.widget.CommonDialog({
                id: 'max_host_desc',
                title: '高峰期单节点QPS均值',
                width: 320
            });
            var descContent = '高峰期服务QPS / 机器节点数';
            max_host_desc_Dialog.setContent(descContent);
            max_host_desc_Dialog.show();
        }, '#max_host_desc');
    }

    function autoCompleteList(owts) {
        new Y.mt.widget.AutoCompleteList({
            id: "owtSelect_auto",
            node: Y.one("#owtSelect"),
            listParam: 'name',
            objList: owts,
            showMax: owts.length,
            matchMode: 'fuzzy',
            more: "",
            callback: function (data) {
                var owt = data.name;
                owt = nameOwt[owt] ? nameOwt[owt] : owt;
                console.log(owt);
                _owt = owt;
            }
        });
        Y.one("#owtSelect_auto").one(".widget-autocomplete-complete-list").setStyle("height", "200px");
        Y.one("#owtSelect_auto").one(".widget-autocomplete-tip").setHTML("输入业务线名搜索或向下滚动选择");
        Y.one("#owtSelect_auto").one(".widget-autocomplete-menu-operator").remove();
    }


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'mt-date',
        'w-date',
        'w-autocomplete',
        'msgp-utils/hashchange',
        'msgp-servicerep/availability',
        'msgp-servicerep/qpspeak',
        'msgp-servicerep/client',
        'msgp-servicerep/error',
        'msgp-servicerep/idc',
        'msgp-servicerep/server',
        'msgp-servicerep/topqps',
        'msgp-servicerep/topqpstp',
        'msgp-servicerep/toptp'

    ]
});
M.use('msgp-servicerep/reportTabNav', function (Y) {
    Y.msgp.servicerep.reportTabNav(owtList,owt,date);
});
