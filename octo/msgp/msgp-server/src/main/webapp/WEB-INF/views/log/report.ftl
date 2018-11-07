<#import "../util/paginator.ftl" as paginator>
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<style>
    .ui-autocomplete {
        max-width: 220px;
        max-height: 220px;
        overflow: auto;
    }

    .control-group .down-more {
        position: absolute;
        top: 6px;
        right: 0;
        width: 40px;
        height: 18px;
        line-height: 18px;
        border: 1px solid #ccc;
        border-radius: 2px;
        text-align: center;
        font-size: 12px;
        cursor: pointer;
        visibility: hidden;
    }

    .control-group .down-more:hover {
        color: #4bb1cf;
    }

    .control-group ul li {
        list-style: none;
        float: left;
        margin: 5px 0;
        height: 40px;
        width: 240px;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        padding: 0 15px;
        border-right: 1px solid #ccc;
        cursor: pointer;
        word-break: break-all;
    }

    .control-group ul li:last-child,
    .control-group ul li:nth-child(4n) {
        border-right: none;
    }

    .control-group ul li:hover {
        background: #f9f9f9;
    }

    .control-group ul li.active {
        color: #3fab99;
        background: #f9f9f9;
    }

    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: #3fab99;
    }

    .common-popdialog .btn.btn-primary:hover, .common-popdialog .btn.btn-primary:focus {
        background-color: #30a28f;
        color: #fff;
    }
</style>
<script type="text/javascript" src="/static/js/jquery.min.js"></script>
<script src="https://cs0.meituan.net/fe.gallery/highcharts/3.0.1/highcharts.js"></script>
<script src="/static/hightcharts/modules/exporting.js"></script>

<#include "/topLinkEvent.ftl" >

<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=${appkey}">性能配置报警</a></li>
        <li><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li class="current"><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
        <li><a href="/log/trend?appkey=${appkey}">异常日志趋势</a></li>
        <li><a href="/log/configuration/list?appkey=${appkey}">异常监控配置</a></li>
        <li><a href="/log/filter/list?appkey=${appkey}">异常过滤器配置</a></li>
        <li><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>

<div class="form-inline mt20 mb20">
    <div class="control-group">
        <div style="width: 100px;float: left; margin-bottom: 16px; padding-left: 10px; line-height: 34px; height: 34px; background: #eee;">
            <strong>时间范围</strong></div>
        <div style="margin-left: 125px; height: 34px; line-height: 34px; margin-bottom: 16px;">
            <div id="datePicker" style=" float: left;">
                <label style="padding-left:1em;"> 开始：</label>
                <input id="start_time" type="text" style="color: #858585; width: 135px;" placeholder="查询开始时间"
                       value="${start}">
                <label class="ml20"> 结束：</label>
                <input id="end_time" type="text" style="color: #858585; width: 135px;" placeholder="查询结束时间"
                       value="${end}">
            </div>
            <div id="timeline" style=" float: left;">
                <label style="padding-left:1em"> 快速选择：</label>
                [<a style="cursor:pointer;" data-value="today"> 今天 </a>]
                [<a style="cursor:pointer;" data-value="one_hour_before"> 前1小时 </a>]
                [<a style="cursor:pointer;" data-value="six_hour_before"> 前6小时 </a>]
                [<a style="cursor:pointer;" data-value="twenty_four_hour_before"> 前24小时 </a>]
                [<a style="cursor:pointer;" data-value="yesterday"> 昨天 </a>]
                <span> :: </span>
                [<a style="cursor:pointer;" data-value="minus_one_day"> 减1天 </a>]
                [<a style="cursor:pointer;" data-value="plus_one_day"> 加1天 </a>]
            </div>

            <div style="float: right;">
                <a href="https://123.sankuai.com/km/page/28326385" target="_blank">
                    异常日志接入<i class="fa fa-question-circle"></i>
                </a>
            </div>
        </div>
        <div id="sets" class="clearfix" style="position: relative; display: none;">
            <div style="width: 100px;float: left; padding-left: 10px; margin: 5px 0; line-height: 40px; height: 40px; background: #eee;">
                <strong>Set分组</strong></div>
            <div class="list" style="margin-left: 100px; padding-right: 80px;">
                <ul class="clearfix" style="height: 50px; overflow: hidden;">
                </ul>
            </div>
            <div class="down-more"><span>更多 </span><i class="fa fa-angle-down"></i></div>
        </div>
        <div id="hosts" class="clearfix" style="position: relative;">
            <div style="width: 100px;float: left; padding-left: 10px; margin: 5px 0; line-height: 40px; height: 40px; background: #eee;">
                <strong>主机</strong></div>
            <div class="list" style="margin-left: 100px; padding-right: 80px;">
                <ul class="clearfix" style="height: 50px; overflow: hidden;">

                </ul>
            </div>
            <div class="down-more"><span>更多 </span><i class="fa fa-angle-down"></i></div>
        </div>
        <div id="filter" class="clearfix" style="position: relative;">
            <div style="width: 100px;float: left; padding-left: 10px; margin: 5px 0; line-height: 40px; height: 40px; background: #eee;">
                <strong>异常类型</strong></div>
            <div class="list" style="margin-left: 100px; padding-right: 80px;">
                <ul class="clearfix" style="height: 50px; overflow: hidden;">
                </ul>
            </div>
            <div class="down-more"><span>更多 </span><i class="fa fa-angle-down"></i></div>
        </div>
        <div id="message" style="margin: 5px 0 16px 0;">
            <div style="width: 100px;float: left; padding-left: 10px; line-height: 34px; height: 34px; background: #eee;">
                <strong>异常信息检索</strong></div>
            <div style="margin-left: 125px; height: 34px; line-height: 34px;">
                <input id="exception" type="text" style="width: 250px" placeholder="请输入部分日志信息"
                       value="${message?default('')}">

                <button id="query_btn" class="btn btn-primary ml20">查询</button>
                <span style="padding-left: 20px">查不到日志? <a style="padding-left: 5px;"
                                                           href="https://123.sankuai.com/km/page/28354543"
                                                           target="_blank">
                <i class="fa fa-question-circle"></i>点我检查一下</a></span>
            </div>
        </div>
    </div>
</div>
<hr/>
<div class="row-fluid">
    <div class="box">
        <div class="box-content" style="display: flex; position: relative;">
            <div id="hourReports" style="min-width: 310px; height: 400px; display: inline-block; flex: 1"></div>
            <div style="max-height: 400px; max-width: 600px; overflow: scroll;">
                <div id="hostReports" style="height: 400px; display: none; width: 0;"></div>
            </div>
        </div>
    </div>
    <div class="text-shalow" style="font-size:13px; position: relative; min-height: 100px;">
        <ul id="log-list" class="unstyled">

        </ul>
        <div id="paginator_supplier"></div>
    </div>
</div>
<div id="errorLogSwitchDialog" class="form-horizontal" style="display: none;">
    <input id="statusVal" type="hidden"/>
    <div class="form-inline">
        <div class="statusDiv control-group">
            <label> 状态：</label>
            <span>未知（无法判断当前日志服务状态，请根据实际情况启动或停止）</span>
        </div>
        <div class="startDiv control-group">
            <button class="startErrorLog btn btn-primary" style="background-color: #33a34e">启动</button>
            <span style="font-size: 10px; color: gray">
            说明：资源有限将不自动启动日志服务，启动代表启动日志中心的Parser服务(5min生效)，若启动后没有日志请在kafka.data.sankuai.com确认是否有error.{appkey}
        </span></div>
        <div class="stopDiv control-group">
            <button class="stopErrorLog btn btn-primary" style="background-color: #f35959">停止</button>
            <span style="font-size: 10px; color: gray">
            说明：Octo将不再统计服务异常日志或发出报警，若需停止日志中心解析服务请联系日志中心。(停止Octo异常日志服务对业务服务无任何影响)
        </span></div>
    </div>
</div>
<#--<@paginator.initPaginator "paginator" "logReportForm" />-->
<script type="text/javascript" src="/static/js/jquery-ui.min.js"></script>
<script src="/static/js/tooltip.js"></script>
<script type="text/javascript">
    M.use("msgp-log", function (Y) {
        document.title = '异常日志统计';
        var host = '${host?default('All')}';
        var hostSet = '${hostSet?default('All')}';
        var exceptionName = '${exceptionName?default('')}';
        var filterId = '${filterId?default('')}';
        var isSetService = '${isSetService?default(false)}';
        Y.msgp.Log.report(hostSet, host, exceptionName, filterId, isSetService);
    });
</script>