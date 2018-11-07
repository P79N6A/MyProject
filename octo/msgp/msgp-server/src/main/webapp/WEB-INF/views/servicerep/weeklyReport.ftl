<style>
    body {
        background: #f9f7f6;
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
        /*text-align: -internal-center;
        border-bottom: 2px solid #ddd;*/
    }

    .table > tbody > tr > td, .table > tbody > tr > th, .table > tfoot > tr > td, .table > tfoot > tr > th, .table > thead > tr > td, .table > thead > tr > th {
        padding: 5px 5px 5px 6px;
        white-space: nowrap;
        font-size: 14px;
        vertical-align: middle;
        /*        border: 2px solid white;
                border-top: 1px solid #ddd;*/
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
<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<div class="form-inline mt20 mb20" >
    <div class="control-group">
        <label>查询时间：</label>

        <input id="start_time" type="text" placeholder="时间" value='${day!""}'>
        <label style="margin-left: 5px"> 服务(Appkey)：</label>
        <input type="text" placeholder="选择服务" value="all"  id="appkeys">
        <div class="btn-group" style="padding-left: 20px;">
            <a type="button" id="btn_table" class="btn <#if (echart?exists && echart=false)>btn-primary</#if>">查表</a>
            <a type="button" id="btn_echart"
               class="btn <#if (echart?exists && echart=true)>btn-primary</#if>">查图</a>
        </div>
        <label style="padding-left:1em" id="weekly_tips"></label>

        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28467108" target="_blank">
                服务周报使用说明<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
</div>


<div style="width:100%;overflow:auto; padding: 20px 0 20px 0;" id="showGraph">

</div>
<textarea id="text_echart" style="display:none">
    <div style="color: #999;">注意: 若当天调用量为0, 在表格数据中可用率被表示为NaN, 在趋势图中, 由于图表限制, 被表示为0%</div>
    <div id="week_<%=data.appkey%>">
        <table>
            <tr>
                <td colspan="5" style="padding: 20px 0 10px 0">
                    <div>
                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;"
                            width="100%; " color=#987cb9 SIZE=1>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="5">服务名: <a title="服务概要"
                                        href="/service/detail?appkey=<%=data.appkey%>#outline"
                                        target="_blank"><%=data.appkey%></a>
                </td>
            </tr>
            <tr>
                <td>
                    <div class="charts-wrapper-out">
                        <div id="screen_successRatio" class="charts-wrapper"
                             style="width: 300px;height: 300px;border: 0"></div>
                    </div>
                </td>
                 <#--<td>
                    <div class="charts-wrapper-out">
                        <div id="screen_count" class="charts-wrapper"
                             style="width: 300px;height: 300px;border: 0"></div>
                    </div>
                </td>-->
                <td>
                    <div class="charts-wrapper-out">
                        <div id="screen_qps" class="charts-wrapper"
                             style="width: 300px;height: 300px;border: 0"></div>
                    </div>
                </td>
                <td>
                    <div class="charts-wrapper-out">
                        <div id="screen_tp5090" class="charts-wrapper"
                             style="width: 300px;height: 300px;border: 0"></div>
                    </div>
                </td>
                <td>
                    <div class="charts-wrapper-out">
                        <div id="screen_tp999" class="charts-wrapper"
                             style="width: 300px;height: 300px;border: 0"></div>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</textarea>

<textarea id="text_table" style="display:none">
    <table class="table" style="width: 100%;">
        <tr>
            <td style="text-align: center; width: 20%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E; padding-left: 20px;">
                Appkey
            </td>
            <td style="text-align: center;width:10%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                时间
            </td>
            <td style="text-align: center;width: 12%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                可用率
            </td>
             <td style="text-align: center;width: 14%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                调用量
            </td>
            <td style="text-align: center;width: 10%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                QPS
            </td>
            <td style="text-align: center;width: 13%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                TP50(毫秒)
            </td>
            <td style="text-align: center;width: 13%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                TP90(毫秒)
            </td>
            <td style="text-align: center;width: 14%; color:white; font-size: 14px; border: 2px solid white; font-weight: bold;background-color: #0D826E;">
                TP999(毫秒)
            </td>
        </tr>
        <% var days = this.days %>
        <% Y.Array.each(this.data, function( weekData, index ){ %>
        <% if(index % 2 == 0) { %>
        <tr style="background-color:#f6f7fa;">
            <% }else{ %>
        <tr style="background-color: #FFFFFF;">
            <% } %>
            <td style="border: 2px solid white; font-size: 14px; padding-left: 20px;"><%=weekData.title %></td>

            <td style="text-align: center;border: 2px solid white;">
                <table width="100%">
                    <% Y.Array.each(days, function( day, index ){ %>
                    <tr>
                        <td style="text-align: center; border: none;"><%=day %></td>
                    </tr>
                    <% }); %>
                </table>
            </td>

            <td style="text-align: center;border: 2px solid white;">
                <table width="100%">
                    <% var datas = weekData.series.successRatio[0].data %>
                    <% var counts = weekData.series.count[0].data %>
                    <% Y.Array.each(datas, function( data, index ){ %>
                        <% if(counts[index] > 0) {%>
                            <% if((data/100) < 99){ %>
                            <tr>
                                <td style="text-align: center;border: none; color: red;"><%= (data).toFixed(2)/100 %>%</td>
                            </tr>
                            <% }else{ %>
                            <tr>
                                <td style="text-align: center;border: none;"><%= (data).toFixed(2)/100 %>%</td>
                            </tr>
                            <% } %>
                        <% }else{ %>
                            <tr>
                                <td style="text-align: center;border: none;">NaN</td>
                            </tr>
                        <% } %>
                    <% }); %>
                </table>
            </td>

            <td style="text-align: center;border: 2px solid white;">
                <table width="100%">
                    <% var datas = weekData.series.count[0].data %>
                    <% Y.Array.each(datas, function( data, index ){ %>
                    <tr>
                        <td style="text-align: center;border: none;"><%=data %></td>
                    </tr>
                    <% }); %>
                </table>
            </td>

            <td style="text-align: center;border: 2px solid white;">
                <table width="100%">
                    <% var datas = weekData.series.qps[0].data %>
                    <% Y.Array.each(datas, function( data, index ){ %>
                    <tr>
                        <td style="text-align: center;border: none;"><%=data %></td>
                    </tr>
                    <% }); %>
                </table>
            </td>

            <td style="text-align: center;border: 2px solid white;">
                <table width="100%">
                    <% var datas = weekData.series.tp50[0].data %>
                    <% Y.Array.each(datas, function( data, index ){ %>
                    <tr>
                        <td style="text-align: center;border: none;"><%=data %></td>
                    </tr>
                    <% }); %>
                </table>
            </td>

            <td style="border: 2px solid white;">
                <table width="100%">
                    <% var datas = weekData.series.tp90[0].data %>
                    <% Y.Array.each(datas, function( data, index ){ %>
                    <tr>
                        <td style="text-align: center;border: none;"><%=data %></td>
                    </tr>
                    <% }); %>
                </table>
            </td>

            <td style="border: 2px solid white;">
                <table width="100%">
                    <% var datas = weekData.series.tp999[0].data %>
                    <% Y.Array.each(datas, function( data, index ){ %>
                    <tr>
                        <td style="text-align: center;border: none;"><%=data %></td>
                    </tr>
                    <% }); %>
                </table>
            </td>
        </tr>
        <% }); %>
    </table>
</textarea>
<HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;"
    width="100%; " color=#987cb9 SIZE=1>
<div style="padding:20px 0;font-size: 12px; font-weight: 100; line-height: 24px; ">
    使用说明：<br/>
    服务治理周报汇聚了服务在过去一周的各项关键指标, 若当天调用量未0, 则可用率被表示为NaN <br/>
    若需要获取更多的指标信息, 请关注每天的服务治理日报，或访问<a href="/">OCTO服务治理平台</a><br/>
    意见反馈：OCTO技术支持(infocto)
</div>

<script type="text/javascript" src="/static/js/jquery.min.js"></script>
<script type="text/javascript" src="/static/js/jquery-ui.js"></script>
<script>
    M.use('msgp-servicerep/weeklyReport', function (Y) {
        var day = '${day!""}';
        var username = '${username!""}';
        var appkeys = [<#list appkeyList![] as appkey>'${appkey}',</#list>];
        Y.msgp.servicerep.weeklyReport(day, username, appkeys);
    });
</script>