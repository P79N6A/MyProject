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

    .table > tbody > tr div.table-expandable-arrow {
        background: transparent url(/static/css/images/arrows.png) no-repeat scroll 0 -16px;
        width: 16px;
        height: 16px;
        display: block;
    }

    .table > tbody > tr div.table-expandable-arrow.up {
        background-position: 0 0;
    }

    .spanname {
        float: none;
        background-color: #F3ECCB;
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

<#if (appDailyList?size > 0) >
<div style="display: block;width: 100%;padding: 0;margin-bottom: 20px;font-size: 21px;line-height: 40px;color: #333;border: 0;border-bottom: 1px solid #e5e5e5;">OCTO服务质量报表<span style="font-size: 12px; padding-left: 12px; color: red">注意 :在一些邮件客户端中, 报表内容可能需要点击才能全部显示。邮件向右滑动还有更多内容 →_→</span></div>

<div id="daily_report_title" style="padding: 0 0 20px 10px;">
    <h5 style="font-size: 14px;">报告时间 ${start} 至 ${end}</h5>
    <span>更详细的<strong style="color: red;">接口报告信息</strong>请前往<a href="https://octo.sankuai.com/repservice/daily">OCTO服务治理平台</a>查看</span>
    <span>意见反馈：OCTO技术支持(infocto)</span>
</div>
<div id="daily_report_wrapper">
    <div style="overflow: auto;">
        <table class="table">
            <thead>
            <tr>
                <th>Appkey</th>
                <th>服务状态</th>
                <th>服务可用率</th>
                <th>QPS/同比/环比</th>
                <th>异常日志数/同比/环比</th>
                <th>TP90/同比/环比</th>
                <th>机房流量</th>
            </tr>
            </thead>
            <tbody>
            <#if (appDailyList?size == 0) >
            <tr style="background-color:#f6f7fa;">
                <td colspan="13" style="text-align: center;">无有效日报数据, 请点击<a
                        href="https://123.sankuai.com/km/page/28354574"
                        target="_Blank" style="color: #3fab99;">业务治理问题自查</a>快速解决
                </td>
            </tr>
            <#else>
                <#list appDailyList as appDailyData>
                    <#assign owt = appDailyData.owt ! ""/>
                    <#assign appkey = appDailyData.appkey />
                    <#assign mainData = appDailyData.mainData />
                    <#if appDailyData_index%2==0>
                    <tr class="appkey" style="background-color:#f6f7fa;">
                    <#else>
                    <tr class="appkey" style="background-color: #FFFFFF;">
                    </#if>
                        <td><a title="点我有惊喜" href="${hostUrl}/service/detail?appkey=${appkey}#outline"
                               target="_blank">${appkey}</a></td>
                        <td><a title="点我有惊喜" href="${hostUrl}/data/tabNav?appkey=${appkey}&type=performance#performance"
                               target="_blank">
                            <#assign m_status = mainData.status=true/>
                            ${m_status ? string("<span style='color:green'>GOOD</span>", "<span style='color:red'>WARN</span>")}</a>
                        </td>
                        <td><a title="点我有惊喜" href="${hostUrl}/data/tabNav?appkey=${appkey}&type=performance#performance"
                               target="_blank">
                            <#if (mainData.successRatio < 99) >
                                <span style='color:red'>${mainData.successRatio?string('#.####')}%</span>
                            <#else>
                                <span>${mainData.successRatio?string('#.####')}%</span>
                            </#if>
                        </a></td>
                        <td>
                            <a title="点我有惊喜" href="${hostUrl}/data/tabNav?appkey=${appkey}&type=performance#performance"
                               target="_blank">
                            ${mainData.qps.value?string("0")}
                                &nbsp;/
                                <#if (mainData.qps.woW > 20 && mainData.qps.value > 1500) >
                                    <span style='color:red'>${mainData.qps.woW?string("0")}%</span>
                                <#elseif (mainData.qps.woW == 0.142857)>
                                    --
                                <#else>
                                ${mainData.qps.woW?string("0")}%
                                </#if>
                                &nbsp;/
                                <#if (mainData.qps.doD > 20 && mainData.qps.value > 1500) >
                                    <span style='color:red'>${mainData.qps.doD?string("0")}%</span>
                                <#elseif (mainData.qps.doD == 0.142857)>
                                    --
                                <#else>
                                ${mainData.qps.doD?string("0")}%
                                </#if>
                            </a>
                        </td>
                        <td>
                            <a title="点我有惊喜" href="${hostUrl}/log/report?appkey=${appkey}" target="_blank">
                            ${mainData.errorCount.value}&nbsp;/
                                <#if (mainData.errorCount.woW > 20 && mainData.errorCount.value > 5) >
                                    <span style='color:red'>${mainData.errorCount.woW?string("0.##")}%</span>
                                <#else>
                                ${mainData.errorCount.woW?string("0.##")}%
                                </#if>
                                &nbsp;/
                                <#if (mainData.errorCount.doD > 20 && mainData.errorCount.value > 5) >
                                    <span style='color:red'>${mainData.errorCount.doD?string("0.##")}%</span>
                                <#else>
                                ${mainData.errorCount.doD?string("0.##")}%
                                </#if>
                            </a>
                        </td>
                        <td>
                            <a title="点我有惊喜" href="${hostUrl}/data/tabNav?appkey=${appkey}&type=performance#performance"
                               target="_blank">
                            ${mainData.tp90.value}&nbsp;/
                                <#if (mainData.tp90.woW > 20 && mainData.tp90.value > 200) >
                                    <span style='color:red'>${mainData.tp90.woW?string("0.##")}%</span>
                                <#elseif (mainData.tp90.woW == 0.142857)>
                                    --
                                <#else>
                                ${mainData.tp90.woW?string("0.##")}%
                                </#if>
                                &nbsp;/
                                <#if (mainData.tp90.doD > 20 && mainData.tp90.value > 200) >
                                    <span style='color:red'>${mainData.tp90.doD?string("0.##")}%</span>
                                <#elseif (mainData.tp90.doD == 0.142857)>
                                    --
                                <#else>
                                ${mainData.tp90.doD?string("0.##")}%
                                </#if>
                            </a>
                        </td>
                        <td>
                            <#assign m_isLoadBalance = mainData.isLoadBalance=0 />
                            <a title="点我有惊喜" href="${hostUrl}/repservice?owt=${owt!""}#idc" target="_blank">
                            ${m_isLoadBalance ? string("均衡", "不均衡")}</a>
                        </td>
                    </tr>
                </#list>
            </#if>
            </tbody>
        </table>
    </div>
</div>
<div style="padding:20px 0;font-size: 12px; font-weight: 300; line-height: 24px; ">
    报表说明：<br/>
    服务治理日报展示所负责、关注的服务，未产生调用量的服务不会展示在这里, 订阅管理请点击: <a href="${hostUrl}/service?type=3&business=-1&pageNo=1&pageSize=20">我的服务</a><br/>
    <strong>服务状态：</strong>WARN 表示服务需要关注，关键指标同比、环比变化较大，GOOD表示服务运行良好,关键指标同比、环比变化不大<br/>
    <strong>服务可用率：</strong>服务可用率的定义及统计方法请参考WIKI<a href="https://123.sankuai.com/km/page/28327894">服务可用率使用指南</a><br/>
    <strong>机房流量：</strong>机房节点的分布比与机房流量分布比例如果成正比，流量即为均衡，否则不均衡<br/>
    <strong>同比：</strong>和上周同一天相比，差值20%且数值符合一定条件标红(qps、异常日志、tp90)<br/>
    <strong>环比：</strong>和昨天相比,差值20%且数值符合一定条件标红(qps、异常日志、tp90)<br/>
    更多信息，请访问<a href="${hostUrl}">OCTO服务治理平台</a><br/>
    意见反馈：OCTO技术支持(infocto)
</div>
</#if>

<#if (nonstandardAppkeyList?size > 0) >
<div id="abnormal_appkey_report" style="display: block;width: 100%;padding: 0;margin-bottom: 20px;font-size: 21px;line-height: 40px;color: #333;border: 0;border-bottom: 1px solid #e5e5e5;">异常服务统计</div>

<table style="background-color: transparent;margin-bottom: 20px;width: 100%;">
    <thead>
    <tr>
        <th style="vertical-align: bottom;
                white-space: nowrap;
                font-weight: bold;
                font-size: 15px;
                background-color: #0D826E;
                color: white;
                text-align: -internal-center;
                border-bottom: 2px solid #ddd;
                padding: 5px 0 5px 0;" colspan="3">异常服务列表</th>
    </tr>
    </thead>
    <#list nonstandardAppkeyList as groupedAppkey>
        <#if groupedAppkey_index%2==0>
        <tr style="background-color:#f6f7fa;">
        <#else>
        <tr style="background-color: #FFFFFF;">
        </#if>
        <#list groupedAppkey as appkeyItem>
            <td style="padding: 5px 5px 5px 6px;
                    vertical-align: top;
                    white-space: nowrap;
                    font-size: 14px;
                    border: 2px solid white;
                    border-top: 1px solid #ddd;">
                <a title="点我有惊喜" href="${hostUrl}/service/detail?appkey=${appkeyItem.appkey}#outline" target="_blank">${appkeyItem.appkey}</a><span style="color: grey; padding-left: 5px;">(异常类型: ${appkeyItem.abnormityDescption})</span>
            </td>
        </#list>
    </tr>
    </#list>
</table>
<div style="font-size: 12px; font-weight: 300; line-height: 24px; padding-bottom: 20px;">
    <div style="padding: 0 0 10px 0;">
        <span>异常类型及风险:</span>
    </div>
    <div>
        <table class="instruction" style="border: 1px  #ddd; width: 100%; font-size: 12px;">
            <thead>
            <tr>
                <th style=" border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;border-top: 1px  #ccc;border-bottom: 1px  #ccc;background-color: #f4f5f6;line-height: 20px;color: #999;vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">异常类型</th>
                <th style=" border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;border-top: 1px  #ccc;border-bottom: 1px  #ccc;background-color: #f4f5f6;line-height: 20px;color: #999;vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">异常原因</th>
                <th style=" border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;border-top: 1px  #ccc;border-bottom: 1px  #ccc;background-color: #f4f5f6;line-height: 20px;color: #999;vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">风险点</th>
                <th style=" border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;border-top: 1px  #ccc;border-bottom: 1px  #ccc;background-color: #f4f5f6;line-height: 20px;color: #999;vertical-align: middle; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">解决方案</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">1</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">OCTO环境和服务树环境不一致	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">线上流量打到测试环境，或测试环境流量打到线上	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">修正主机的注册环境</td>
            </tr>
            <tr>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">2</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">appkey绑定的服务树和主机所在服务树不一致	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">未来上线服务注册验证后，非appkey绑定服务单元的主机无法注册到octo	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">确保一个appkey只绑定在一个服务单元，调整服务单元下的主机</td>
            </tr>
            <tr>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">3</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">appkey主机数/服务树主机数<100%	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">服务树服务单元下的主机没有全部投入使用，有可能申请多了造成资源浪费	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">检查服务单元下主机的使用情况，下线不用的主机</td>
            </tr>
            <tr>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">4</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">appkey主机数>服务树主机数之和	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">有已经下线的主机还注册在octo上，未来如果新创建的主机复用了这个ip，可能导致流量误分发	</td>
                <td style="border-color: #ddd;border-left: 1px  #ddd;padding: 5px 5px;line-height: 25px;background-color: #fff;border-bottom: 1px  #ddd;word-wrap: break-word;word-break: break-all;color: #666;vertical-align: middle;">从OCTO上删除已经下线的主机</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div style="padding: 10px 0 10px 0">
        <span>
            详细数据请参考：<a href="http://dom.dp/bj/appkey_stree"  target="_blank">AppKey与服务树异常报表</a><br/>
            更多处理及说明信息,请参考: <a href="https://123.sankuai.com/km/page/14781087" target="_blank">Appkey与服务树异常报表说明及处理方法</a><br/>
            若有疑问请咨询：业务SRE负责人或OCTO技术支持(infocto)
        </span>
    </div>
</div>
</#if>