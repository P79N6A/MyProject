<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <h3 class="page-header">已删除服务</h3>
    <hr/>
    <#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
    <div class="form-inline mt20 mb20">
        <div class="control-group">
            <label> 开始时间：</label><input id="start_time" type="text" class="span3" placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label><input id="end_time" type="text" class="span3" placeholder="查询结束时间">
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
                    <col width="20%"></col>
                    <col width="40%"></col>
                </colgroup>
                <thead>
                <tr>
                    <th>时间</th>
                    <th>操作人</th>
                    <th>操作描述</th>
                    <th>服务标示</th>
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
    <script>
        document.title="已删除服务";
        var key = '${appkey}';
    </script>
    <script type="text/javascript" src="/static/manage/operationLog.js"></script>
</div>