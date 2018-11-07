<title>操作记录</title>
<div class="panel-body">
    <div class="form-inline mb20">
        <div id="filelog_menu" class="control-group">
        <#include "/common/env.ftl" >
            <div id="filelog_env" class="btn-group">
                <a value="prod" type="button" class="btn btn-default"
                   href="javascript:void(0)">prod</a>
                <a value="stage" type="button" class="btn btn-default"
                   href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="test" type="button" class="btn btn-default"
                       href="javascript:void(0)">test</a>
                </#if>
            </div>
            <label class="ml20">开始时间：</label>
            <input id="file_log_start_time" type="text" class="span3" placeholder="查询开始时间" value="" style="width: auto;">
            &nbsp;&nbsp;
            <label>结束时间：</label>
            <input id="file_log_end_time" type="text" class="span3" placeholder="查询结束时间" value="" style="width: auto;">
            &nbsp;&nbsp;
            <button id="filelog_return" class="btn btn-primary" type="button">
                <i class='fa fa-step-backward'></i>
                返回
            </button>
        </div>
    </div>


    <div id="filelog_data_div" class="table-responsive" style="margin-left:30px">
        <table class="table  table-striped table-hover table-condensed table-page">
            <colgroup>
                <col width="12%"></col>
                <col width="8%"></col>
                <col width="6%"></col>
                <col width="8%"></col>
                <col width="8%"></col>
                <col width="58%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>时间</th>
                <th>操作</th>
                <th>操作人</th>
                <th>分组</th>
                <th>文件</th>
                <th>详情</th>
            </tr>
            </thead>
            <tbody class="J-config-panel-tbody">
            </tbody>
        </table>
        <div id="page_navigation_filelog"></div>
    </div>
</div>








