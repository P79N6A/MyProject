<div class="form-inline mb20" style="padding-left: 0px;">
    <div id="comfirm_select" class="btn-group">
        <a id="uncomfirm" value="0" type="button" class="btn btn-default" href="javascript:void(0)">未审核</a>
        <a id="comfirmed" value="1" type="button" class="btn btn-default" href="javascript:void(0)">已审核</a>
        <a id="rejected" value="2" type="button" class="btn btn-default" href="javascript:void(0)">不通过</a>
    </div>
    <a style="padding-left:1em">主机：</a>

    <div class="input-append">
        <input id="switchEnv_ip" class="span3" placeholder="IP地址" type="text"/>
        <button class="btn btn-primary" type="button" id="searchIP">查询</button>
    </div>
</div>
<div id="table_switchEnv" class="table-responsive">
    <table class="table table-striped table-hover">
        <colgroup>
            <col width="15%"></col>
            <col width="5%"></col>
            <col width="5%"></col>
            <col width="4%"></col>
            <col width="4%"></col>
            <col width="10%"></col>
            <col width="5%"></col>
            <col width="10%"></col>
            <col width="14%"></col>
            <col width="20%"></col>
        </colgroup>
        <thead>
        <tr>
            <th>IP</th>
            <th>cluster</th>
            <th>申请人</th>
            <th>旧环境</th>
            <th>新环境</th>
            <th>申请时间</th>
            <th>审核人</th>
            <th>审核时间</th>
            <th>操作</th>
            <th>备注</th>
        </tr>
        </thead>
        <tbody>
        <tr id="content_loading" class="delay_content">
            <td colspan="10">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </td>
        </tr>
        <tr id="content_msg">
            <td colspan="10">
                <span id="content_msg_td"></span>
            </td>
        </tr>
        </tbody>
    </table>
    <div id="paginator_switchEnv">
    </div>
</div>
