<div class="form-inline mb20" style="vertical-align: middle" >
    <button id="refresh_table" type="button" class="btn btn-primary">
        <i class="fa fa-refresh">刷新列表</i>
    </button>
    <div style="float: right;">
        <span style="font-size: 10px; padding-left: 10px; font-weight: bold;">Detector: </span><span id="scannner_detector_availability" style="font-size: 14px;">100.0000%</span>
    </div>
    <div style="float: right;">
        <span style="font-size: 10px; padding-left: 10px; font-weight: bold;">Updater: </span><span id="scannner_updater_availability" style="font-size: 14px;">100.0000%</span>
    </div>
    <div style="float: right;">
        <span style="font-size: 10px; padding-left: 10px; font-weight: bold;">Master: </span><span id="scannner_master_availability" style="font-size: 14px;">100.0000%</span>
    </div>
    <div style="float: right;">
        <span style="font-size: 16px; padding-left: 10px;">Scannner可用率: </span><span id="scannner_availability" style="font-size: 22px; color: #3fab99; font-weight: bold;">100.0000%</span>
    </div>

</div>
<legend>任务调度自检</legend>
<div id="scanner_job">
    <div class="content-overlay" >
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col ></col>
                <col ></col>
                <col ></col>
                <col ></col>
                <col ></col>
            <#--<col width="30%"></col>-->
            </colgroup>
            <thead>
            <tr>
                <th>部署机房</th>
                <th>任务</th>
                <th>开始时间</th>
                <th>耗时</th>
                <th>其他</th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="4">Loading contents...</td></tr>
            </tbody>
        </table>
    </div>
</div>

<legend>scanner操作</legend>
<div id="scanner_action">
    <div class="content-overlay" >
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col ></col>
                <col ></col>
                <col ></col>
                <col ></col>
            <#--<col width="30%"></col>-->
            </colgroup>
            <thead>
            <tr>
                <th>时间</th>
                <th>操作描述</th>
                <th>服务</th>
                <th>详细信息</th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="4">Loading contents...</td></tr>
            </tbody>
        </table>
        <div id="paginator_action">
        </div>
    </div>
</div>

<legend>异常节点信息</legend>
<div id="scanner_log">
    <div class="content-overlay" >
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="15%" ></col>
                <col width="10%" ></col>
                <col width="15%" ></col>
                <col width="15%" ></col>
                <col width="30%" ></col>
                <col width="15%" ></col>
            <#--<col width="30%"></col>-->
            </colgroup>
            <thead>
            <tr>
                <th>服务</th>
                <th>环境</th>
                <th>提供者</th>
                <th>状态</th>
                <th>详情</th>
                <th>时间</th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="3">Loading contents...</td></tr>
            </tbody>
        </table>
        <div id="paginator_log">
        </div>
    </div>
</div>