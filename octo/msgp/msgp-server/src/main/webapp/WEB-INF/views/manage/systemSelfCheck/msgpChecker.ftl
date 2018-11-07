<div class="form-inline mb20">
    <button id="refresh_supplier" type="button" class="btn btn-primary">
        <i class="fa fa-refresh">刷新列表</i>
    </button>
</div>
<legend>任务调度自检</legend>
<div id="msgp_schedule">
    <div class="content-overlay" >
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">

        <#--<div class="form-inline mt20 mb20">-->
            <#--<legend>msgp自检</legend>-->
        <#--</div>-->
        <table class="table table-striped table-hover">
            <colgroup>
                <col ></col>
                <col ></col>
                <#--<col width="30%"></col>-->
            </colgroup>
            <thead>
            <tr>
                <th>描述</th>
                <th>详情</th>
                <#--<th>其他</th>-->
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="3">Loading contents...</td></tr>
            </tbody>
        </table>
    </div>
</div>

<legend>路由分组自检</legend>
<div id="msgp_route">
    <div class="content-overlay" >
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col ></col>
                <col ></col>
                <#--<col width="30%"></col>-->
            </colgroup>
            <thead>
            <tr>
                <th>服务</th>
                <th>详情</th>
                <#--<th>其他</th>-->
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="3">Loading contents...</td></tr>
            </tbody>
        </table>
    </div>
</div>