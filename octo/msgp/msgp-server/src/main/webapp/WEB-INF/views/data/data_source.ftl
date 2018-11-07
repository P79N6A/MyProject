<title xmlns="http://www.w3.org/1999/html">来源分析</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_source"></div>
<textarea id="text_data_source" style="display:none">
    <div class="form-inline mb20" style="padding: 0;">
        <div class="control-group" style="display: inline-block; white-space: nowrap;">
        <#include "/common/env.ftl" >
            <div id="source_env_select" class="btn-group">
                <a value="prod" type="button" class="btn <#if (!env?? || env='prod')>btn-primary</#if>" href="javascript:void(0)">prod</a>
                <a value="stage" type="button" class="btn <#if (env?exists && env='stage')>btn-primary</#if>" href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="test" type="button" class="btn <#if (env?exists && env='test')>btn-primary</#if>" href="javascript:void(0)">test</a>
                </#if>
            </div>
            <label style="margin-left: 5px"> 开始：</label><input id="start_time" type="text" class="span3" value="${start}" placeholder="查询开始时间"  style="width: 160px;">
            <label style="margin-left: 5px"> 结束：</label><input id="end_time" type="text" class="span3"  value="${end}" placeholder="查询结束时间"  style="width: 160px;">
            <label style="margin-left: 5px"> 服务：</label><input type="text" placeholder="选择服务" value="${remoteApp!'*'}"  id="remoteApp">
            <label style="margin-left: 5px"> 接口：</label><input type="text" placeholder="选择接口" value="${spanname!'all'}" id="spanname">
        </div>
        <div class="control-group" style="display: inline-block; margin-left: 5px;">
            <div class="btn-group" id="query_btn">
                <a type="button" class="btn chart <#if (!merge?? || merge= true)>btn-primary</#if>">查表</a>
                <a type="button" class="btn graph <#if (merge?exists && merge=false)>btn-primary</#if>">查图</a>
            </div>
            <div id="compare_wrap" class="btn-group hidden ml20">
                <button id="compare_button" class="btn btn-primary">显示对比</button>
            </div>
        </div>
    </div>
    <hr>
    <div class="source_kpi_list clearfix" style="text-align: center;"></div>
</textarea>
