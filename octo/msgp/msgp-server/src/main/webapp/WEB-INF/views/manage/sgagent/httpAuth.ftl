<div id="add_new_item" class="btn-group">
    <button class="btn btn-primary add_auth_btn">
        <i class='fa fa-plus'></i>
        <span>添加一项</span>
    </button>
</div>
<div id="http_auth_board">
    <div class="form-inline mb20">
    </div>
    <table class="table">
        <colgroup>
            <col width="15%"></col>
            <col width="30%"></col>
            <col width="25%"></col>
            <col width="30%"></col>
        </colgroup>
        <thead>
        <tr>
            <th>用户名</th>
            <th>token</th>
            <th>更新时间</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody class="J-config-panel-tbody">
        <tr id="board_loading">
            <td colspan="4">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </td>
        </tr>
        <tr id="board_error_data">
            <td colspan="4"></td>
        </tr>
        </tbody>
    </table>
    <div id="page_navigation_http_auth"></div>
</div>