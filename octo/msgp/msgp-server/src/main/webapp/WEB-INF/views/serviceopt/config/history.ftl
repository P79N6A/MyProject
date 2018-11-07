<title>操作记录</title>
<div class="panel-body">
    <div class="form-inline mb20">
        <div id="history_menu" class="control-group">
            <#include "/common/env.ftl" >
            <div id="history_env" class="btn-group">
                <a value="prod" type="button" class="btn btn-default"
                   href="javascript:void(0)">prod</a>
                <a value="stage" type="button" class="btn btn-default"
                   href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="test" type="button" class="btn btn-default"
                       href="javascript:void(0)">test</a>
                </#if>
            </div>
            <label>开始时间：</label>
            <input id="log_start_time" type="text" class="span3" placeholder="查询开始时间" value="" style="width: auto;">
            <label>结束时间：</label>
            <input id="log_end_time" type="text" class="span3" placeholder="查询结束时间" value="" style="width: auto;">

            <label>key值：</label>
            <input id="log_key" type="text" class="span3" placeholder="key值检索" value="" style="width: auto;">
            <label>value值：</label>
            <input id="log_value" type="text" class="span3" placeholder="value值检索" value="" style="width: auto;">
            <button class="btn btn-primary" type="button" id="historySearchButton">查询</button>

            <button id="history_return" class="btn btn-primary" type="button">
                <i class='fa fa-step-backward'></i>
                返回
            </button>
        </div>
    </div>


    <div id="history_data_div" class="table-responsive" style="margin-left:30px">
        <table class="table  table-striped table-hover table-condensed table-page">
            <colgroup>
                <col width="8%"></col>
                <col width="4%"></col>
                <col width="6%"></col>
                <col width="15%"></col>
                <col width="10%"></col>
                <col width="40%"></col>
                <col width="17%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>时间</th>
                <th>操作</th>
                <th>操作人</th>
                <th>路径</th>
                <th>Key</th>
                <th>Value</th>
                <th>Comment</th>
            </tr>
            </thead>
            <tbody class="J-config-panel-tbody">
            </tbody>
        </table>
        <div id="page_navigation_history"></div>
    </div>
</div>








