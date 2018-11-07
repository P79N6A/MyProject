<div class="form-inline mb20">
    <div class="btn-group">


        <a value="hostInfo" type="button" class="btn btn-default typeBtn" href="javascript:void(0)">主机信息</a>
        <#--<a value="switchEnv" type="button" class="btn btn-default typeBtn" href="javascript:void(0)">环境切换</a>-->
    </div>

    <#--主机诊断-->
    <a style="padding-left:1em;display:none;" class="hostInfo_menu">主机：</a>
    <div class="input-append hostInfo_menu" style="display: none;">
        <input id="hostInfoSearchIP" class="span3" placeholder="IP地址" type="text"/>
        <button class="btn btn-primary" type="button" id="hostInfoSearch">查询</button>
    </div>
    <span class="tips hostInfo_menu" style="display: none;" id="checkHostInfoIPTips"></span>
    <span class="tips hostInfo_menu" style="color:blue; display: none;">针对服务列表拉取、无法获取配置等问题，请输入客户端主机，只支持10开头的ip诊断。</span>

    <#--环境切换-->
    <a style="padding-left:1em;display: none;"  class="switchEnv_menu">新环境：</a>

    <div id="switch_env_select" style="display: none;" class="btn-group switchEnv_menu">
        <a id="env_prod" value="prod" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
        <a id="env_stage" value="stage" type="button" class="btn btn-default"
           href="javascript:void(0)">stage</a>
        <#if isOffline>
            <a id="env_test" value="test" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
        </#if>
    </div>
    <a style="padding-left:1em;display: none;"  class="switchEnv_menu">主机：</a>
    <div class="input-append switchEnv_menu" style="display: none;">
        <input id="switchEnv_ip" class="span3" placeholder="IP地址" type="text"/>
        <button class="btn btn-primary" type="button" id="addApply">申请切换</button>
    </div>
    <span class="tips switchEnv_menu" style="display: none;" id="checkIPTips"></span>

</div>
<div id="hostInfo" class="hostInfo_menu" style="display: none;">
<#include "hostInfo.ftl"/>
</div>
<div id="switchEnv" class="switchEnv_menu" style="display: none;">
<#include "switchEnv.ftl"/>
</div>
