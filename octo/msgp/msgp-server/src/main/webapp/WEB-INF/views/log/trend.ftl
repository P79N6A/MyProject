<script type="text/javascript" src="/static/js/jquery.min.js"></script>
<script src="https://cs0.meituan.net/fe.gallery/highcharts/3.0.1/highcharts.js"></script>
<script src="/static/hightcharts/modules/exporting.js"></script>

<#include "/topLinkEvent.ftl" >

<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=${appkey}">性能配置报警</a></li>
        <li><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
        <li class="current"><a href="/log/trend?appkey=${appkey}">异常日志趋势</a></li>
        <li><a href="/log/configuration/list?appkey=${appkey}">异常监控配置</a></li>
        <li><a href="/log/filter/list?appkey=${appkey}">异常过滤器配置</a></li>
        <li><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>

<div class="label-pool box box-noborder">
</div>

<div class="box">
    <div class="box-header" style="margin-bottom: 10px">
        <form id="logReportOfAllAppForm" method="get" action="/log/trend" class="form-search">
            <input id="appkey" name="appkey" type="hidden" value="${appkey!''}"/>
            <label>开始时间</label>
            <input id="startTime" name="startTime" type="text" data-widget="datepicker" class="input-small"
                   value="${startTime!''}"/>
            <label>结束时间</label>
            <input id="stopTime" name="stopTime" type="text" data-widget="datepicker" class="input-small"
                   value="${stopTime!''}"/>
            <label>统计粒度：</label>
        <#if ! periodType?? || periodType == 1>
            <label class="radio"><input id="period-day" name="periodType" type="radio" value="1"
                                        checked="checked"/>天</label>
            <label class="radio"><input id="period-month" name="periodType" type="radio" value="2"/>月</label>
        <#else>
            <label class="radio"><input id="period-day" name="periodType" type="radio" value="1"/>天</label>
            <label class="radio"><input id="period-month" name="periodType" type="radio" value="2" checked="checked"/>月</label>
        </#if>
            <input id="logReportBtn" type="submit" class="btn btn-primary" value="查询"/>
        </form>
    </div>
    <div class="box-content">
        <div id="logChat" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#logChat").highcharts({
            title: {
                text: "异常日志统计",
                x: -20
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150,
                dateTimeLabelFormats: {
                    second: '%H:%M:%S',
                    minute: '%m-%d %H:%M',
                    hour: '%Y-%m-%d %H:%M',
                    day: '%Y-%m-%d',
                    month: '%Y-%m-%d',
                    year: '%Y'
                }
            },
            yAxis: {
                title: {
                    text: '异常日志数量'
                },
                plotLines: [
                    {
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }
                ]
            },
            tooltip: {
                formatter: function () {
                    return Highcharts.dateFormat('%Y-%m-%d', this.x) + '<br/>' + this.series.name + ':' + this.y;
                }
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                borderWidth: 0
            },
            series: [
            <#if logMap?? && logMap?keys?size &gt; 0>
                {
                    showInLegend: true,
                    name: '${appkey!""}',
                    data: [
                        <#list logMap?keys?sort as time>
                            [Date.parse('${time} GMT+0000'), ${logMap[time]}],
                        </#list>
                    ]
                }
            </#if>
            ]
        });
    });
</script>
