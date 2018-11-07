<title>配置报警</title>

<h3 class="page-header">
    选择指标：<input class="mb5 span6" type="text" id='business_kpi_select' value='${name!""}' autocomplete="off"/>
    <input type="hidden" class="f-text" id='business_kpi_select_hidden' value='${screenId!""}'>
</h3>
<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=">性能配置报警</a></li>
        <li><a href="/monitor/log?appkey=">性能报警记录</a></li>
        <li><a href="/log/report?appkey=">异常日志统计</a></li>
        <li><a href="/log/trend?appkey=">异常日志趋势</a></li>
        <li><a href="/log/configuration/list?appkey=">异常监控配置</a></li>
        <li><a href="/log/filter/list?appkey=">异常过滤器配置</a></li>
        <li class="current"><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>
<div class="form-inline mt20 mb20">
    <button id="add_trigger" data-side="server" class="btn btn-primary ml20">添加监控</button>

</div>

<div id="triggers_wrap">
    <div class="content-overlay">
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="20%"></col>
                <col width="20%"></col>
                <col width="20%"></col>
                <col width="40%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>持续时间(分钟)</th>
                <th>规则类型</th>
                <th>阈值</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="4">Loading contents...</td>
            </tr>
            </tbody>
        </table>
        <div id="paginator_monitor">
        </div>
    </div>
</div>
<script>
    document.title = "配置报警";
    var key = '${screenId!""}';
</script>
<script type="text/javascript" src="/static/monitor/businessConfig.js"></script>