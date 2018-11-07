<div class="box box-noborder">
    <div class="box-header">
        <form id="logFeedForm" action="/log/feed" method="get" class="form-search pull-left">
            Appkey
            <select id="appkey" name="appkey" style="width: 100px; height: 27px; display: inline;"
                    onchange="document.getElementById('logFeedForm').submit();">
            <#if apps??>
                <#list apps as __appkey>
                    <option value="${__appkey}" <#if appkey?? && __appkey == appkey>selected="selected"</#if>>${__appkey}</option>
                </#list>
            <#else>
                <#assign __appkeyList = ["mtcrm", "mtupm", "mtsg", "mtsso", "mtct"]>
                <#list __appkeyList as __appkey>
                    <option value="${__appkey}" <#if appkey?? && __appkey == appkey>selected="selected"</#if>>${__appkey}</option>
                </#list>
            </#if>
            </select>
        </form>
        <input id="logFeedClearBtn" type="button" value="清空" class="pull-left btn" style="margin-left:10px;" />
        <input id="logFeedControlBtn" type="button" data-start="开始" data-pause="暂停" value="暂停" class="btn btn-primary pull-right"/>
    </div>
    <div class="box-content">
        <input id="currentId" type="hidden" value="${currentId!0}"/>
        <ul id="logList" class="unstyled">
            <li id="topLog" style="display: none">

            </li>
        </ul>
    </div>
</div>
<script>
    M.use("msgp-log", function(Y) {
        Y.msgp.Log.feed();
    });
</script>
