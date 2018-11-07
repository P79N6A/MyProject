<title>数据分析</title>
<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: dimgray;
    }
    .common-popdialog {
        overflow: scroll;
    }

    .tooltip-inner {
        background: rgba(0,0,0,0.7);
    }

    .ui-autocomplete {
        max-width: 220px;
        max-height: 220px;
        overflow: auto;
    }

</style>
<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<script crossorigin="anonymous" src="//www.dpfile.com/app/owl/static/owl_1.5.13.js"></script>
<script>
    Owl.start({
        project: 'msgp-project',
        pageUrl: 'data-tabNav'
    })
</script>
<#include "/topLinkEvent.ftl" >
    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs" style="margin: 10px 0 0px 0">
            <li><a href="#dashboard">数据总览</a></li>
            <li><a href="#operation">业务指标</a></li>
            <li><a href="#performance">性能指标</a></li>
            <li><a href="#source">来源分析</a></li>
            <li><a href="#destination">去向分析</a></li>
            <li><a href="#host">主机分析</a></li>
            <li><a href="#secondLevel">秒级指标<span class="corner corner-danger">New!</span></a></li>
            <li><a href="#stream">上下游分析</a></li>
            <li><a href="#trace">调用链分析</a></li>
            <li><a href="#tag">标签治理</a></li>
        </ul>

        <div id="content_wrapper">
            <div id="wrap_data_dashboard" class="sheet" style="display:none;">
            <#include "data_dashboard.ftl" >
            </div>
            <div id="wrap_data_operation" class="sheet" style="display:none;">
            <#include "data_operation.ftl" >
            </div>
            <div id="wrap_data_performance" class="sheet" style="display:none;">
            <#include "data_performance.ftl" >
            </div>
            <div id="wrap_data_source" class="sheet" style="display:none;">
            <#include "data_source.ftl" >
            </div>
            <div id="wrap_data_destination" class="sheet" style="display:none;">
            <#include "data_destination.ftl" >
            </div>
            <div id="wrap_data_host" class="sheet" style="display:none;">
            <#include "data_host.ftl" >
            </div>
            <div id="wrap_data_secondLevel" class="sheet" style="display:none;">
            <#include "data_secondLevel.ftl">
            </div>
            <div id="wrap_data_stream" class="sheet" style="display:none;">
            <#include "data_stream.ftl" >
            </div>
            <div id="wrap_data_trace" class="sheet" style="display:none;">
            <#include "data_trace.ftl" >
            </div>
            <div id="wrap_data_tag" class="sheet" style="display:none;">
            <#include "data_tag.ftl" >
            </div>
        </div>
        <script type="text/javascript" src="/static/js/jquery.min.js"></script>
        <script type="text/javascript" src="/static/js/jquery-ui.js"></script>
        <script src="/static/js/tooltip.js"></script>
        <script>
            M.use('msgp-data/dataTabNavigation_1.1', function (Y) {
                var appkey = '${appkey}';
                var mtraceUrl = '${mtraceUrl}';
                Y.msgp.data.dataTabNavigation(appkey, mtraceUrl);
            });
        </script>
    </div>
</div>