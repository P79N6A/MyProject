<title>服务统计报告</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="clearfix">
        <label style="padding-left: 10px;float: right">
            <a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28326917" target="_blank" id="count_guide">服务价值统计使用手册
                <i class="fa fa-question-circle"></i></a>
        </label>
        <h3 class="page-header" id="page_header" style="float: left; height: 20px; line-height: 20px; border: none; margin-bottom: 0;">服务价值统计</h3>
    </div>

    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs" style="margin: 10px 0 20px 0">
            <li><a href="#day_count">总流量日趋势</a></li>
            <li><a href="#business_count">部门访问日趋势</a></li>
            <li><a href="#module_count">模块使用日趋势</a></li>
            <li><a href="#coverage_count">部门覆盖度统计</a></li>
            <li><a href="#integrated_count">统计信息综合检索</a></li>

        </ul>
        <div id="content_wrapper">

            <div id="wrap_day_count" class="sheet" style="display:none;">
            <#include "daycount.ftl" >
            </div>
            <div id="wrap_business_count" class="sheet" style="display:none;">
            <#include "businesscount.ftl" >
            </div>
            <div id="wrap_module_count" class="sheet" style="display:none;">
            <#include "modulecount.ftl" >
            </div>
            <div id="wrap_coverage_count" class="sheet" style="display:none;">
            <#include "coveragecount.ftl" >
            </div>
            <div id="wrap_integrated_count" class="sheet" style="display:none;">
            <#include "integratedcount.ftl" >
            </div>

        </div>
        <script>
            var report_style = "${style}";
            var business = "${business!''}";
        </script>
       <#-- <script src="https://cs0.meituan.net/node_modules/mtfe_cos_track/raven/raven-all-min.js"></script>
        <script>
            Raven.config('http://c496cd1a76bd472d9f4330699cc58a7f@sentry7.meituan.com/696').install();
        </script>-->
        <script type="text/javascript" src="/static/worth/reportTabNavigation.js"></script>
    </div>
</div>
</div>