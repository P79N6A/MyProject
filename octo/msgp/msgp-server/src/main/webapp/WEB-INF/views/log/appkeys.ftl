<div class="label-pool box box-noborder">
<#if apps??>
    <div class="box-header">app列表</div>
    <div class="box-content">
        <form id="logAddAppkeyForm" class="form-search bd-top-box" method="post" action="/log/appkeys">
            <label>appkey：</label>
            <input type="text" class="input-small" name="appkey"/>
            <input type="submit" class="btn" value="添加"/>
            <a class="pull-right btn" href="/log/report?appkey=${appkey!''}">日志统计</a>
        </form>
        <hr class="hr-normal">
        <#list apps as key>
            <span class="label label-primary">${key}</span>
        </#list>
    </div>
</#if>
</div>
