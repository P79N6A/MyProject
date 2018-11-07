<title>报警记录</title>
<#include "/topLinkEvent.ftl" >
<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=${appkey}">性能配置报警</a></li>
        <li class="current"><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
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
        <label> 开始时间：</label><input id="start_time" type="text" class="span3" placeholder="查询开始时间">
        <label class="ml20"> 结束时间：</label><input id="end_time" type="text" class="span3" placeholder="查询结束时间">
        <button id="get_trigger_event" class="btn btn-primary ml20">查询</button>
    </div>
</div>

<div id="log_wrap">
    <hr/>
    <div class="content-overlay">
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="10%"></col>
                <col width="15%"></col>
                <col width="10%"></col>
                <col width="20%"></col>
                <col width="15%"></col>
                <col width="30%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>报警时间</th>
                <th>是否确认</th>
                <th>服务与调用服务</th>
                <th>接口</th>
                <th>监控项</th>
                <th>信息</th>
            </tr>
            </thead>
            <tbody>
            <td colspan="4">Loading contents...</td>
            </tbody>
        </table>
        <div id="paginator_monitor">
        </div>
    </div>
</div>
<div id="paginator_wrapper">
</div>

<script>
    document.title = "报警记录";
    var key = '${appkey}';
</script>
<script type="text/javascript" src="/static/monitor/monitorLog.js"></script>
