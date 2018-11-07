/* jshint indent : false */
M.add('msgp-utils/topLinkEvent', function (Y) {
    Y.namespace('msgp.utils').topLinkEvent = topLinkEvent;
    var appkey = '';
    var quickNavUl = Y.one('#quick_nav .dropdown-menu');
    var EMPTYLIST = '<li><a href="javascript:;">暂无快捷导航</a></li>';
    var LISTTEMPLATE = [
        '<% Y.Array.each(this.data, function(item,index){ %>',
        '<li>',
        '<a data-id="<%= item.id %>" href="<%= item.url %>"><%= item.title %> <%= item.appkey==""? "":"- "+item.appkey %> ',
        '<i class="fa fa-times" title="从快捷导航删除"></i>',
        '</a>',
        '</li>',
        '<% }); %>'
    ].join('');

    function topLinkEvent(key, list) {
        var obj = [];
        for (var i = 0, l = list.length; i < l; i++) {
            obj.push({id: i, name: list[i]});
        }
        if (obj.length && Y.one('#apps_select') != null) {
            AutoCompleteList(obj);
        }
        appkey = key;
        //Y.one('#add_quick_nav').on('click', function () {
        //    doAddQuickNav();
        //});

        Y.one('#menus').delegate('click', function () {
            var url = this.getAttribute('data-url');
            if (url) {
                url = Y.Lang.sub(url, {appkey: appkey || this.getAttribute('data-appkey')});
            }
            window.location.href = url;
        }, '.menu-list');
    }

    function showTipContentDetail(content) {
        var tipsDialog = tipsDialog ? tipsDialog : initTips();
        tipsDialog.setContent(content);
        tipsDialog.show();
    }

    function showTipContent() {
        var micro = new Y.Template();
        var message = micro.render(tipcontent);
        showTipContentDetail(message);
    }

    function initTips() {
        var tipsDialog = new Y.mt.widget.CommonDialog({
            id: 'tips_dialog',
            title: '基础架构组件服务协议',
            width: 1000,
            height: 600,
            overflow: scroll
        });
        return tipsDialog;
    }

    function AutoCompleteList(obj) {
        new Y.mt.widget.AutoCompleteList({
            id: "apps_select_auto",
            node: Y.one("#apps_select"),
            listParam: 'name',
            objList: obj,
            showMax: obj.length,
            matchMode: 'fuzzy',
            forbidWrite: false,
            more: "",
            tabSelect: true,
            callback: function (data) {
                var akey = data.name;
                location.search = 'appkey=' + akey;
            }
        });
        Y.one("#apps_select_auto").one(".widget-autocomplete-complete-list").setStyle("height", "400px");
        Y.one("#apps_select_auto").one(".widget-autocomplete-tip").setHTML("输入服务名搜索或向下滚动选择");
        Y.one("#apps_select_auto").one(".widget-autocomplete-menu-operator").remove();
    }

    function refreshSearch(newKey) {
        var search = location.search.slice(1);
        var arr = search.split('&');
        for (var i = 0, l = arr.length; i < l; i++) {
            if (arr[i].indexOf('appkey=') === 0) {
                arr[i] = 'appkey=' + newKey;
                break;
            } else if (i === l - 1) {
                arr.push('appkey=' + newKey);
            }
        }
        return arr.join('&');
    }


    function refreshQuickNav(arr) {
        if (arr.length === 0) {
            quickNavUl.setHTML(EMPTYLIST);
            return;
        }
        var micro = new Y.Template();
        var html = micro.render(LISTTEMPLATE, {data: arr});
        quickNavUl.setHTML(html);
    }


    var tipcontent =
        '<style rel="stylesheet">' +
        '.common-popdialog .btn.btn-primary { background-color: #3fab99; }' +
        '.common-popdialog .head h3 { background-color: #3fab99; }' +
        '.common-popdialog { overflow: scroll; }' +
        '</style>' +
        '<div class="ant-modal-content"><div class="ant-modal-header"><div class="ant-modal-title" id="rcDialogTitle0"></div></div><div class="ant-modal-body"><div class="I5UejvPGxMyNS70GDNGH0"><h1>一，服务定义</h1>' +
        '<br>' +
        '<p>本协议条款中“服务”指：乙方（基础架构部）向甲方（用户）提供各基础架构组件的相关服务。包括但不限于以下服务：</p>' +
        '<br>' +
        '<p>1.1. 服务治理：</p>' +
        '<ul>' +
        '    <li><p>Octo服务治理体系（MSGP/Mtthrift/Cthrift/MCC/Oceanus/Pigeon/Lion）及相关的技术支持服务</p></li>' +
        '</ul>' +
        '<p>1.2. KV存储：</p>' +
        '<ul>' +
        '    <li><p>Cellar及相关的技术支持服务</p></li>' +
        '    <li><p>Squirrel及相关的技术支持服务</p></li>' +
        '</ul>' +
        '<p>1.3. 弹性调度：</p>' +
        '<ul><li><p>Hulk及相关的技术支持服务</p></li></ul>' +
        '<p>1.4. 消息中间件：</p>' +
        '<ul><li><p>Mafka及相关的技术支持服务</p></li></ul>' +
        '<p>1.5. 监控：</p>' +
        '<ul><li><p>CAT及相关的技术支持服务</p></li></ul>' +
        '<p>1.6. 其他基础组件：</p>' +
        '<ul>' +
        '    <li><p>数据库访问框架Zebra及相关的技术支持服务</p></li>' +
        '    <li><p>数据同步组件Databus及相关的技术支持服务</p></li>' +
        '    <li><p>秘钥管理系统KMS及相关的技术支持服务</p></li>' +
        '    <li><p>分布式ID生成系统Leaf及相关的技术支持服务</p></li>' +
        '    <li><p>分布式任务调度系统Crane及相关的技术支持服务</p></li>' +
        '    <li><p>图片服务Venus及相关的技术支持服务</p></li>' +
        '    <li><p>稳定性保障组件Rhino及相关的技术支持服务</p></li>' +
        '</ul>' +
        '<br>' +
        '<br>' +
        '<p>以上服务各组件的SLA定义：<a href="https://123.sankuai.com/km/page/28125608" target="_blank">基础架构组件SLA</a></p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>二，服务收费</h1>' +
        '<p>2.1. 采用后付费方式，按月结算。合同另有规定的，以合同规定为准。</p>' +
        '<br>' +
        '<p>2.2. 乙方每月出具账单，甲方确认后付费。费用金额以双方每月确认的账单金额为准。合同另有规定的，以合同规定为准。</p>' +
        '<br>' +
        '<p>2.3. 对月账单金额有异议时，双方友好协商解决。提出异议方有举证责任。</p>' +
        '<br>' +
        '<p>2.4. 乙方保留在甲方未按照约定支付全部费用之前不继续向甲方提供服务和/或技术支持，或者终止服务和/或技术支持的权利。同时，乙方保留对后付费服务中的欠费行为追究法律责任的权利。</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>三，服务赔偿条款</h1>' +
        '<br>' +
        '<p>3.1. 由于乙方原因引起系统中断或故障，导致用户服务不可用的，赔付标准为乙方按照服务不可用时间的100倍赔偿，即赔付金额=发生故障的资源故障前24小时平均每分钟的业务营收费用×不可用分钟数×100倍。不可用分钟=故障解决时间-故障开始时间（时间单位换算到分钟，精确到2位小数）。</p>' +
        '<br>' +
        '<p>3.2. 甲方未按照乙方对服务的约定使用服务或者使用方式具有明显过错，导致甲方业务受损时，乙方不承担赔偿责任。</p>' +
        '<br>' +
        '<p>3.3. 在任何情况下，乙方承担的赔偿责任总额不超过向甲方收取的当月服务费用总额。</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>四，乙方权利和义务</h1>' +
        '<br>' +
        '<p>4.1. 乙方应确保所提供的服务符合其ＳＬＡ约定，并且不断提高所提供服务的稳定性、性能和降低服务成本；</p>' +
        '<br>' +
        '<p>4.2. 乙方应按照服务SLA约定保障甲方的数据安全；</p>' +
        '<br>' +
        '<p>4.3. 乙方应有相应机制，及时听取甲方的使用反馈并进行服务改进，包括但不限于支持更多的使用方式，提高易用性，提高运维支持效率等；</p>' +
        '<br>' +
        '<p>4.4. 乙方有权对甲方的使用情况进行监控和分析，并对甲方的使用方式提出建议；</p>' +
        '<br>' +
        '<p>4.5. 乙方有权在根据自身版本迭代计划对所提供服务进行版本升级或版本回滚或停机维护。维护期间所产生的服务中断不属于乙方违约，不计入赔偿；</p>' +
        '<br>' +
        '<p>4.6. 乙方有权在所提供的服务由于甲方的不当使用而导致服务不稳定或不可用时，暂时停止甲方的接入或对甲方的接入进行限流；</p>' +
        '<br>' +
        '<p>4.7. 4.4,4.5,4.6 条款所涉及的操作，乙方有义务及时通知甲方。</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>五，甲方权利和义务</h1>' +
        '<br>' +
        '<p>5.1. 甲方使用乙方的服务时，应符合国家法律法规，社会公序良知；甲方不得使用乙方服务来非法损害第三方合法利益；</p>' +
        '<br>' +
        '<p>5.2. 甲方使用乙方的服务时，应遵从乙方对具体服务的使用规范。甲方不同意乙方使用规范时，应提前进行磋商。在双方一致同意修改规范前，应遵从原规范；</p>' +
        '<br>' +
        '<p>5.2. 甲方有权向乙方反馈服务改进意见，乙方应对甲方的意见出具书面答复；</p>' +
        '<br>' +
        '<p>5.3. 甲方有权对乙方的月账单提出异议，双方对账单进行共同审核；</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>六，保密条款</h1>' +
        '<br>' +
        '<p>6.1. 甲方同意乙方对所提供的服务，包括但不限于服务代码、服务相关资料文档、服务设计思路和解决方案、服务数据等拥有知识产权并承诺未经乙方允许不得向第三方泄露；</p>' +
        '<br>' +
        '<p>6.2. 乙方同意甲方在乙方服务所产生的数据等拥有知识产权，并承诺未经甲方允许不得向第三方泄露；</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>七，数据</h1>' +
        '<br>' +
        '<p>7.1. 双方同意，乙方所提供服务所产生的数据分为两类，用户数据和服务运行数据。用户数据的所有权属于甲方，服务运行数据所有权属于乙方；</p>' +
        '<br>' +
        '<p>7.2. 甲乙双方对各自所有权的数据具有完全处置权，对另一方所拥有的数据无权处置，除非得到对方的明确书面授权；</p>' +
        '<br>' +
        '<p>7.3. 对数据的处置授权，应包括所授权处置的数据范围，和所授权的处置方式。被授权方必须严格按照所授权的数据范围和处置方式进行处置，包括但不限于数据备份、历史数据清理、数据压缩存储、数据冗余、数据二次加工、数据报表、数据下载等。</p>' +
        '<br>' +
        '<p>7.4. 对数据的所有权或数据的处置方式存在异议的，由双方友好协商处理。</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>八，不可抗力</h1>' +
        '<br>' +
        '<p>8.1. 因不可抗力或者其他意外事件，使得本服务条款的履行不可能、不必要或者无意义的，遭受不可抗力、意外事件的一方不承担责任。</p>' +
        '<br>' +
        '<p>8.2. 不可抗力、意外事件是指不能预见、不能克服并不能避免且对一方或双方当事人造成重大影响的客观事件，包括但不限于：自然灾害如洪水、地震、瘟疫流行等以及社会事件如战争、动乱、政府行为、电信主干线路中断、黑客、第三方网路堵塞、电信部门技术调整和政府管制等。</p>' +
        '<br>' +
        '<br>' +
        '<br>' +
        '<h1>九. 附则</h1>' +
        '<br>' +
        '<a href="https://123.sankuai.com/km/page/28125608" target="_blank">基础架构组件SLA</a>' +
        '<br>' +
        '<p>9.1. 本协议附件服务说明是本协议不可分割的一部分，与本协议具有同等法律效力。如果有任何前述约定与本协议有不一致之处，以本协议附件为准。</p>' +
        '<br></div><div><label class="checkbox inline"><span class="ant-checkbox"><input id="tipsCheck" type="checkbox" class="ant-checkbox-input" value="on" onclick="tipsClick()"><span class="ant-checkbox-inner"></span></span><span><!-- react-text: 16 -->请仔细阅读您所使用的架构组件服务协议条款(<!-- /react-text --><span style="color: red;">勾选本项代表您同意本协议</span><!-- react-text: 18 -->)<!-- /react-text --></span></label><div><p style="margin-top: 10px;"><span>协议Wiki链接:</span><a href="https://123.sankuai.com/km/page/28125601" target="_blank">基础架构组件服务协议</a></p></div></div></div><div align="center" class="ant-modal-footer"><div><!-- /react-text --><button id="confirmBtn" disabled type="button" onclick="tipsack()" class="btn btn-primary"><span>确 认</span></button></div></div></div>';
    //<button type="button" class="btn btn-warring"><span>放 弃</span></button><!-- react-text: 27 -->&nbsp;&nbsp;
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete'
    ]
});
