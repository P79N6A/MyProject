<div class="form-inline mb20">

<#--主机诊断-->
    <a style="padding-left:1em;" class="hostInfo_menu">IP：</a>
    <div class="input-append hostInfo_menu">
        <input id="hostInfoSearchIP" class="span3" type="text"/>
        <button class="btn btn-primary" type="button" id="hostInfoSearch">查询</button>
    </div>
    <span class="tips hostInfo_menu" id="checkHostInfoIPTips"></span>
    <span class="tips hostInfo_menu" style="color:blue;">针对服务列表拉取、无法获取配置等问题，请输入客户端主机，不支持云主机，且只支持10开头的ip诊断。</span>

</div>
<div id="hostInfo" class="hostInfo_menu" style="display: none;">
<#include "hostInfo.ftl"/>
</div>