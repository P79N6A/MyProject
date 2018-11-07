<div class="content-body">
    <div class="form-inline mb20">
        <div class="control-group">
            <label> 开始时间：</label><input id="start_time" type="text" class="span3" placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label><input id="end_time" type="text" class="span3" placeholder="查询结束时间">
            <label class="ml20">操作类型：</label>
            <select id="entityType" name="entityType" title="操作类型">
                <option value="选择全部">选择全部</option>
            </select>
            <label class="ml20">操作人员：</label>
            <select id="operator" name="entityType" title="操作人员">
                <option value="选择全部">选择全部</option>
            </select>
        </div>
    </div>
    <div id="log_wrap">
        <div class="content-overlay">
            <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
        </div>
        <div class="content-body" style="display:none;">
            <table class="table table-striped table-hover">
                <colgroup>
                    <col width="15%"></col>
                    <col width="10%"></col>
                    <col width="15%"></col>
                    <col width="60%"></col>
                </colgroup>
                <thead>
                <tr>
                    <th>时间</th>
                    <th>操作人</th>
                    <th>操作描述</th>
                    <th>详细信息</th>
                </tr>
                </thead>
                <tbody>
                <td colspan="4">Loading contents...</td>
                </tbody>
            </table>
            <div id="paginator_monitor">
            </div>
        </div>
    </div>
    <div id="paginator_wrapper">
    </div>
</div>