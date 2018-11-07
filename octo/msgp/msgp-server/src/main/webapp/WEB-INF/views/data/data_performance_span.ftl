<#--此视图显示接口的分钟/小时粒度的变化趋势-->
<title>性能指标-接口详情</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
<#include "/topLinkEvent.ftl" >
    <div class="tab-box">
        <ul class="nav nav-tabs widget-edit-tab">
            <li><a href="/data/tabNav?appkey=${appkey}#dashboard">Dashboard</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#operation">业务指标</a></li>
            <li class="current"><a href="/data/tabNav?appkey=${appkey}#performance">性能指标</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#source">来源分析</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#destination">去向分析</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#host">主机分析</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#secondLevel">秒级指标</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#stream">上下游分析</a></li>
            <li><a target="_blank" href="${mtraceUrl}?appkey=${appkey}">调用链分析</a></li>
            <li><a href="/data/tabNav?appkey=${appkey}#tag">标签治理</a></li>
        </ul>
    </div>

    <div class="form-inline mt20 mb20">
        <div class="control-group">
        <#include "/common/env.ftl" >
            <div id="supplier_env_select" class="btn-group">
                <a value="prod" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
                <a value="stage" type="button" class="btn" href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="test" type="button" class="btn" href="javascript:void(0)">test</a>
                </#if>
            </div>
            <label style="padding-left:1em">日期：</label>

            <input id="day" type="text" class="span2" placeholder="日期" value="${day!''}"
                   <#if unit?exists && unit=="Minute">style="display: none"</#if>>
            <input id="hour" type="text" class="span2" placeholder="日期" value="${hour!''}"
                   <#if unit?exists && unit=="Hour">style="display: none"</#if>>
            <label style="padding-left:1em">时间粒度：</label>
            <input id="unit_switch_hour" name="unit_switch" value="Hour" type="radio"
                   <#if unit?exists && unit=="Hour">checked</#if>  style="vertical-align: top"/>
            <span>&nbsp;小时&nbsp;&nbsp;</span>
            <input id="unit_switch_minute" name="unit_switch" value="Minute" type="radio"
                   <#if unit?exists &&unit=="Minute">checked</#if> style="vertical-align: top"/>
            <span>&nbsp;分钟&nbsp;&nbsp;</span>

            <div class="btn-group" id="day_week_btn">
                <a href="javascript:void(0)" class="btn btn-default" id="day_query_btn" type="button" value="1">环比</a>
                <a href="javascript:void(0)" class="btn btn-default" id="week_query_btn" type="button" value="2">同比</a>
            </div>

        </div>
        <div class="control-group" style="padding-top: 10px;">
            <label class="ml10" style="padding-left: 16px;"> 接口：</label>
            <input class="mb5 span5" style="width:350px" type="text"
                   id='spanSelect' <#assign x = spanname!"*" list = spannameList![]>
                   value='${list?seq_contains(x)?string(x, "*")}' <#--判断spanname是否在列表中，前端切换服务时接口选择正确--> />
        </div>
    </div>
    <hr>
    <div id="charts_outer" class="clearfix">
    </div>

    <script>
        M.use('msgp-data/data_performance_span', function (Y) {
            var appkey = '${appkey}';
            var type = '${type}';
            var unit = '${unit}';
            var list = [<#list spannameList![] as spanname>'${spanname}',</#list>];
            Y.msgp.data.data_performance_span(appkey, list, type, unit);
        });
    </script>
</div>