<div class="row">
    <div style="overflow:scroll;" class="ten columns">
    <#if log??>
        <h5>
            日志详情:
        </h5>
        <hr/>
        <table class="table table-striped table-hover">
            <tr>
                <td style="width:60px;">应用</td>
                <td>${log.appkey!""}</td>
            </tr>
            <tr>
                <td>时间</td>
                <td> <#if log.logTime??>${log.logTime?datetime}</#if></td>
            </tr>
            <tr>
                <td>级别</td>
                <td>ERROR</td>
            </tr>
            <tr>
                <td>主机名</td>
                <td>${log.host!""}<#if isSetService == "true"> (Set分组: ${log.hostSet})</#if></td>
            </tr>
            <#if log.traceId != "">
                <tr>
                    <td>MTraceID</td>
                    <td>${log.traceId}</td>
                </tr>
            </#if>
            <tr>
                <td>日志位置</td>
                <td>${log.location!""}</td>
            </tr>
            <tr>
                <td>日志内容</td>
                <td><#assign message='${log.message!""}'>${message?html}</td>
            </tr>
            <tr>
                <td>异常堆栈</td>
                <td><#assign exception='${log.exception!""}'>${exception?html}</td>
            </tr>
        </table>
    <#else>
        <h5>
            <div style="color: red;">没有日志, 请到<a href="/log/report "> 异常日志统计 </a>查询</div>
            <font class="label">${uniqueKey!""}</font>
        </h5>
    </#if>
    </div>
</div>
