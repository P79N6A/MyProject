<title>系统自检</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <h3 class="page-header">系统自检</h3>
    <#--<hr/>-->
    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs">
            <li><a href="#msgp">MSGP</a></li>
            <li><a href="#sgAgent">SG_AGENT</a></li>
            <li><a href="#mcc">MCC</a></li>
            <li><a href="#thrift">THRIFT</a></li>
            <li><a href="#scanner">SCANNER</a></li>
            <li><a href="#daily">每日数据</a></li>
            <li><a href="#banner">通知管理</a></li>
        </ul>
        <div id="content_wrapper">
            <div id="wrap_msgp" class="sheet" style="display:none;">
                <#include 'msgpChecker.ftl' >
            </div>
            <div id="wrap_sgAgent" class="sheet" style="display:none;">
                <#include 'sgAgentChecker.ftl' >
            </div>
            <div id="wrap_mcc" class="sheet" style="display:none;">
                <#include 'mccChecker.ftl'>
            </div>
            <div id="wrap_thrift" class="sheet" style="display:none;">
                <#include 'thriftChecker.ftl' >
            </div>
            <div id="wrap_scanner" class="sheet" style="display:none;">
                <#include  'scannerChecker.ftl' >
            </div>
            <div id="wrap_daily" class="sheet" style="display:none;">
            <#include  'daily.ftl' >
            </div>
            <div id="wrap_banner" class="sheet" style="display:none;">
            <#include 'banner.ftl' >
            </div>
        </div>
    </div>
    <script>
        M.use('msgp-manage/octoSelfCheck', function (Y) {
            var key = 'sss';
            Y.msgp.manage.octoSelfCheck(key);
        });
    </script>
</div>