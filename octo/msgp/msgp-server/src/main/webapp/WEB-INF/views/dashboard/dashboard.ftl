<div class="dashboard-wrap">
    <div class="content-overlay ml20 mt20">
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <fieldset>
            <legend>接入服务总数：<span id="serviceCount" style="margin-right: 10px" class="text-warning">0</span>
                近两周日均请求量：<span id="requestCountToday" style="margin-right: 10px" class="text-warning">0</span>
                系统可用率：<span style="margin-right: 10px" class="text-warning">99.9991%</span>
            </legend>
            <ul class="clearfix">
                <li>
                    <div id="serviceByBusiness" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="serviceGBLocation" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="serviceGBType" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="requestCountBar" class="dashboard-inner"></div>
                </li>
            </ul class="clearfix">
        </fieldset>
        <fieldset class="mt20">
            <legend>服务节点总数：<span id="instanceCount" class="text-warning">0</span></legend>
            <ul class="clearfix">
                <li>
                    <div id="instanceByStatus" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="instanceByDC" class="dashboard-inner"></div>
                </li>
            </ul>
        </fieldset>
    </div>
</div>