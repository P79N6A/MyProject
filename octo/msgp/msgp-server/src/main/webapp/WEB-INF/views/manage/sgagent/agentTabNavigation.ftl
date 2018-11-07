<title>系统自检</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

   <#include "/topLinkEvent.ftl" >

    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs">
            <li><a href="#availabilityData">可用率数据</a></li>
            <li><a href="#agentChecker">SGAgent巡检</a></li>
            <li><a href="#agentSwitch">开关</a></li>
            <li><a href="#httpAuth">http接口鉴权</a></li>
        </ul>
        <div id="content_wrapper">
            <div id="wrap_availabilityData" class="sheet" style="display:none;">
            <#include "availabilityData.ftl" >
            </div>
            <div id="wrap_agentChecker" class="sheet" style="display:none;">
            <#include "agentChecker.ftl" >
            </div>
            <div id="wrap_agentSwitch" class="sheet" style="display:none;">
            <#include "agentSwitch.ftl" >
            </div>
            <div id="wrap_httpAuth" class="sheet" style="display:none;">
            <#include "httpAuth.ftl" >
            </div>
        </div>
        <script>
            var key = '${appkey}';
        </script>
        <script type="text/javascript" src="/static/manage/sgagent/agentTabNavigation-version0.0.1.js"></script>
    </div>
</div>
</div>