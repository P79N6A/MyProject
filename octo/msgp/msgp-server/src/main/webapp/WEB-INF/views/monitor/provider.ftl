<title>服务提供者报警配置</title>
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<#include "/topLinkEvent.ftl" >
<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=${appkey}">性能报警配置</a></li>
        <li><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
        <li><a href="/log/trend?appkey=${appkey}">异常日志趋势</a></li>
        <li><a href="/log/configuration/list?appkey=${appkey}">异常监控配置</a></li>
        <li><a href="/log/filter/list?appkey=${appkey}">异常过滤器配置</a></li>
        <li><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li class="current"><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>
<div class="form-inline mt20 mb20">
    <button id="add_trigger" class="btn btn-primary ml20">添加监控</button>
    <button id="subscribe_all_alarm" class="btn btn-primary ml20">批量修改订阅</button>
    <#if isOffline && !isStartProviderMonitor>
        <button id="provider_trigger" class="btn btn-primary ml20" value="on">启用线下节点监控</button>
    <#elseif isOffline && isStartProviderMonitor>
        <button id="provider_trigger" class="btn btn-primary ml20" value="off">关闭线下节点监控</button>
    <#else>
        <button id="provider_trigger" class="btn btn-primary ml20" style="display: none"></button>
    </#if>
    <div style="float: right;margin-bottom: 0px">
        <a href="https://123.sankuai.com/km/page/28326952" target="_blank">服务提供者报警使用说明<i
                class="fa fa-question-circle"></i></a>
    </div>
</div>

<div id="triggers_wrap">
    <div class="content-overlay">
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="30%"></col>
                <col width="30%"></col>
                <col width="40%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>监控项</th>
                <th>阈值</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="3">Loading contents...</td>
            </tr>
            </tbody>
        </table>
        <div id="paginator_monitor">
        </div>
    </div>
</div>
</div>
<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script>
    document.title = "服务提供者报警配置";
    var key = '${appkey}';
</script>
<script type="text/javascript" src="/static/monitor/providerConfig-version0.0.1.js"></script>
