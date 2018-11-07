<title>异常监控</title>
<#include "/topLinkEvent.ftl" >
<script type="text/javascript" src="/static/js/jquery.min.js"></script>
<script type="text/javascript" src="/static/js/jquery-sortable.js"></script>
<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li><a href="/monitor/config?appkey=${appkey}">性能配置报警</a></li>
        <li><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
        <li><a href="/log/trend?appkey=${appkey}">异常日志趋势</a></li>
        <li><a href="/log/configuration/list?appkey=${appkey}">异常监控配置</a></li>
        <li class="current"><a href="/log/filter/list?appkey=${appkey}">异常过滤器配置</a></li>
        <li><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>
<div class="label-pool box box-noborder">
    <input id="appkey" type="hidden" value="${appkey!''}"/>
</div>
<div class="box box-noborder">
    <div class="box-header">
        过滤器列表
        <#--<a class="pull-right btn" href="/log/report?appkey=${appkey!''}">日志统计</a>-->
    </div>
    <div class="box-content" style="background: url(/api-wm/image/visible) 0 0 repeat;background-size: 300px 250px;">
        <a id="addFilter" href="javascript:void(0);" class="btn btn-primary">添加过滤器</a>
        <a id="editFilter" href="javascript:void(0);" class="btn btn-success">编辑过滤器</a>
        <a id="deleteFilter" href="javascript:void(0);" class="btn">删除过滤器</a>
        <div class="row-fluid">
            <div class="span5 box box-noborder">
                <div class="box-header" style="font-size:13px;">活动过滤器</div>
                <div id="enabled-filter-list" class="box-content"  style="height: 400px; overflow-y:scroll;style="background: url(/api-wm/image/visible) 0 0 repeat;background-size: 300px 250px;">

                </div>
            </div>
            <div class="span2 box box-noborder">
                <div class="box-header" style="font-size:13px;">操作</div>
                <div class="box-content" style="height: 400px;background: url(/api-wm/image/visible) 0 0 repeat;background-size: 300px 250px;">
                    <div style="text-align: center;">
                        <br/><br/>
                        <button id="enableFilter" href="javascript:void(0);" disabled="disabled">&larr;启用</button><br/><br/>
                        <button id="disableFilter" href="javascript:void(0);"  disabled="disabled">&rarr;废弃</button><br/><br/>
                        <button id="upFilter" href="javascript:void(0);" disabled="disabled">&nbsp;&uarr;&nbsp;上移</button><br/><br/>
                        <button id="downFilter" href="javascript:void(0);" disabled="disabled">&nbsp;&darr;&nbsp;下移</button>
                    </div>
                </div>
            </div>

            <div class="span5 box box-noborder">
                <div class="box-header" style="font-size:13px;">可用过滤器</div>
                <div id="disabled-filter-list" class="box-content" style="height: 400px;overflow-y:scroll; background: url(/api-wm/image/visible) 0 0 repeat;background-size: 300px 250px;">

                </div>
            </div>
        </div>
    </div>
</div>

<div id="filterContentDialog" class="form-horizontal" style="display: none;">
    <input id="edit-id" type="hidden"/>
    <label class="checkbox"><input id="enabled" type="checkbox" checked="checked"/>启用</label>
    <label class="checkbox"><input id="terminate" type="checkbox" checked="checked"/>停止处理其他过滤器</label>
    <label class="checkbox"><input class="fireAlarm" type="checkbox" checked="checked"/>匹配的日志参与报警</label>
    <div class="form-group alarmCond" style="margin-left: 20px;">
        <label class="" style="display: inline-block;">参与报警条件:&nbsp;</label>
        <select class="logCountMin" style="width: 60px; margin-top:10px; margin-bottom:10px;">
            <option value=1>1</option>
            <option value=3>3</option>
            <option value=5>5</option>
        </select> 分钟
        <input class="logCount" type="number" min="1" value="1" style="width: 50px;"> 条
    </div>
    过滤器名称：<input id="name" type="text"/><br/>
    如果符合如下条件中的
    <select id="ruleCondition" style="width: 60px; margin-top:10px; margin-bottom:10px;">
        <option value="0" selected="selected">任何</option>
        <option value="1">所有</option>
    </select>
    条件：<br/>
    <div id="rules" style="height: 200px; overflow: scroll;border:1px solid #ccc; padding:8px;margin-bottom:8px;" class="panel rule-list">
    </div>
    <input id="save" type="button" class="btn btn-primary" value="保存"/>
</div>
<script>
    M.use("msgp-log", function(Y) {
        document.title = "异常监控";
        Y.msgp.Log.filterList();
    });
</script>
<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }
    .common-popdialog .head h3 {
        background-color: #3fab99;
    }
    .common-popdialog .btn.btn-primary:hover, .common-popdialog .btn.btn-primary:focus {
        background-color: #30a28f;
        color: #fff;
    }
</style>
