<div class="form-inline mb20">
    <button id="sg_version_detail" class="btn btn-primary" type="button">
        <i class='fa fa-step-backward'></i>
        返回
    </button>
    &nbsp;&nbsp;
    <a href="javascript:void(0)">版本:</a>
    <span id="detail_version"></span>
    &nbsp;&nbsp;
    <a href="javascript:void(0)">节点数:</a>
    <span id="machine_number"></span>


</div>
<table class="table table-striped table-hover">
    <colgroup>
        <col width="10%"></col>
        <col width="8%"></col>
        <col width="5%"></col>
        <col width="5%"></col>
        <col width="10%"></col>
        <col width="10%"></col>
    </colgroup>
    <thead>
    <tr>
        <th>主机名</th>
        <th>IP</th>
        <th>端口</th>
        <th>权重</th>
        <th>状态</th>
        <th>更新时间</th>
    </tr>
    </thead>
    <tbody>
    <tr id="content_overlay">
        <td colspan="6">
            <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
        </td>
    </tr>
    <tr id="content_error">
        <td colspan="6">
           <span class="ml20">服务器错误</span>
        </td>
    </tr>
    </tbody>
</table>







