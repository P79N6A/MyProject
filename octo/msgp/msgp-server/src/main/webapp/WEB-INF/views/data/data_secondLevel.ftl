<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_secondLevel"></div>
<textarea id="text_data_secondLevel" style="display:none">
    <div class="form-inline mb20" style="padding: 0;">
        <table>
            <tr style="vertical-align: bottom;">
                <td>
                <#include "/common/env.ftl" >
                </td>
                <td>
                    <div id="secondLevel_env_select" class="btn-group">
                        <a value="prod" type="button" class="btn <#if (!env?? || env='prod')>btn-primary</#if>"
                           href="javascript:void(0)">prod</a>
                        <a value="stage" type="button" class="btn <#if (env?exists && env='stage')>btn-primary</#if>"
                           href="javascript:void(0)">stage</a>
                        <#if isOffline>
                            <a value="test" type="button" class="btn <#if (env?exists && env='test')>btn-primary</#if>"
                               href="javascript:void(0)">test</a>
                        </#if>
                    </div>
                </td>
                <td style="padding-left: 20px;">
                     <label> 开始：</label>
                </td>
                <td>
                    <input id="start_time" type="text" class="span3" placeholder="查询开始时间" style="width: 160px;">
                </td>
                <td style="padding-left: 20px;">
                     <label> 结束：</label>
                </td>
                <td>
                    <input id="end_time" type="text" class="span3" placeholder="查询结束时间" style="width: 160px;">
                </td>
                <td style="padding-left: 20px;"><label> 主机：</label></td>
                <td colspan="3" style="padding-top: 15px;" >
                    <input type="text" placeholder="选择主机" id="localhost" style="width: 310px;">
                </td>
                <td style="padding-left: 20px;">
                    <div class="btn-group" id="query_btn">
                        <a type="button" class="btn graph btn-primary">查图</a>
                    </div>
                </td>
            </tr>
        </table>
    <hr>
    <div class="secondLevel_kpi_list clearfix" style="text-align: center;"></div>
</textarea>

