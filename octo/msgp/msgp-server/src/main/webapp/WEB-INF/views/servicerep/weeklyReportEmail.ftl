<style>
    body {
        background: #f9f7f6;
        color: #404d5b;
        line-height: 1.42857143;
    }

    .table {
        background-color: transparent;
        margin-bottom: 20px;
        width: 100%;
    }

    .table > thead > tr > th {
        vertical-align: bottom;
        white-space: nowrap;
        font-weight: bold;
        font-size: 15px;
        background-color: #0D826E;
        color: white;
        text-align: -internal-center;
        border-bottom: 2px solid #ddd;
    }

    .table > tbody > tr > td, .table > tbody > tr > th, .table > tfoot > tr > td, .table > tfoot > tr > th, .table > thead > tr > td, .table > thead > tr > th {
        padding: 5px 5px 5px 6px;
        vertical-align: top;
        white-space: nowrap;
        font-size: 14px;
        border: 2px solid white;
        border-top: 1px solid #ddd;
    }

    .appkey {
        background-color: #f6f7fa;
    }

    .table a:link {
        color: #000000
    }

    /* 未访问的链接 */
    .table a:visited {
        color: #000000
    }

    /* 已访问的链接 */
    .table a:hover {
        color: #4fbba9
    }

    /* 鼠标移动到链接上 */
    .table a:active {
        color: #4fbba9
    }

    /* 选定的链接 */


</style>
<div id="daily_report_title" style="padding: 10px 0 5px 10px;">
    <h5>报告时间 ${start} 至 ${end} &nbsp;<span style="font-weight: bold; color: black;"></h5>
</div>
<div id="weekly_report_wrapper">
    <div style="overflow: auto;">
    <table class="table">
        <thead>
        <tr>
            <th>Appkey</th>
            <th>时间</th>
            <th>服务可用率</th>
            <th>调用量</th>
            <th>QPS</th>
            <th>TP50(ms)</th>
            <th>TP90(ms)</th>
            <th>TP999(ms)</th>
        </tr>
        </thead>
    <tbody>
    <#assign days = days ! ""/>
    <#list weeklyList![] as weekData>
        <#if weekData_index%2==0>
        <tr style="background-color:#f6f7fa;">
        <#else>
        <tr style="background-color: #FFFFFF;">
        </#if>

        <td style="border: 2px solid white; font-size: 14px;">${weekData.title}</td>
        <td style="text-align: center;border: 2px solid white;">
            <table width="100%">
                <#list days![] as day>
                    <tr>
                        <td style="text-align: center; border: none;">${day}</td>
                    </tr>
                </#list>
            </table>
        </td>

        <td style="text-align: center;border: 2px solid white;">
            <table width="100%">
                <#assign datas = weekData.series.successRatio[0].data ! ""/>
                <#assign counts = weekData.series.count[0].data ! ""/>
                <#list datas![] as data>
                    <#if (counts[data_index] > 0) >
                        <#if data/100 < 99>
                            <tr>
                                <td style="text-align: center;border: none; color: red;">${data/100 }%</td>
                            </tr>
                        <#else>
                            <tr>
                                <td style="text-align: center;border: none;">${data/100 }%</td>
                            </tr>
                        </#if>
                    <#else>
                        <tr>
                            <td style="text-align: center;border: none; ">NaN</td>
                        </tr>
                    </#if>
                </#list>
            </table>
        </td>

        <td style="text-align: center;border: 2px solid white;">
            <table width="100%">
                <#assign datas = weekData.series.count[0].data ! ""/>
                <#list datas![] as data>
                    <tr>
                        <td style="text-align: center;border: none;">${data }</td>
                    </tr>
                </#list>
            </table>
        </td>

        <td style="text-align: center;border: 2px solid white;">
            <table width="100%">
                <#assign datas = weekData.series.qps[0].data ! ""/>
                <#list datas![] as data>
                    <tr>
                        <td style="text-align: center;border: none;">${data }</td>
                    </tr>
                </#list>
            </table>
        </td>

        <td style="text-align: center;border: 2px solid white;">
            <table width="100%">
                <#assign datas = weekData.series.tp50[0].data ! ""/>
                <#list datas![] as data>
                    <tr>
                        <td style="text-align: center;border: none;">${data }</td>
                    </tr>
                </#list>
            </table>
        </td>

        <td style="border: 2px solid white;">
            <table width="100%">
                <#assign datas = weekData.series.tp90[0].data ! ""/>
                <#list datas![] as data>
                    <tr>
                        <td style="text-align: center;border: none;">${data }</td>
                    </tr>
                </#list>
            </table>
        </td>

        <td style="border: 2px solid white;">
            <table width="100%">
                <#assign datas = weekData.series.tp999[0].data ! ""/>
                <#list datas![] as data>
                    <tr>
                        <td style="text-align: center;border: none;">${data }</td>
                    </tr>
                </#list>
            </table>
        </td>
    </tr>
    </#list>
    </table>
</div>

<div style="padding:20px 0;font-size: 12px; font-weight: 100; line-height: 24px; ">
    使用说明：<br/>
    服务治理周报汇聚了服务在过去一周的各项关键指标, 若当天调用量未0, 则可用率被表示为NaN<br/>
    若需要获取更多的指标信息, 请关注每天的服务治理日报，或访问<a href="https://octo.sankuai.com">OCTO服务治理平台</a><br/>
    意见反馈：OCTO技术支持(infocto)
</div>