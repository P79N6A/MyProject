<link type="text/css" rel="stylesheet" href="/static/css/config.css" media="all">
<div class="content-body">
    <div class="form-inline mb20">
        <a>环境:</a>
        <div id="shutdown_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-default" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
        </div>
    </div>
    <div class="form-inline mb20">
    </div>
    <div id="table_supplier" class="table-responsive">
        <table class="table" style="margin-bottom: 0px;">
            <thead>
            <tr>
                <th><span id="ip-all-check" style="cursor:pointer;color: #3fab99;">全选</span>/<span id="ip-all-uncheck" style="cursor:pointer;color: #3fab99;">反选</span>
                    &nbsp;
                    <div class="input-append" style="margin-bottom: 0px;">
                        <input id="searchBox" class="span3" placeholder="主机名 或 IP地址 ..." type="text"
                               style="width:266px"/>
                        <button class="btn btn-primary" type="button" id="searchBtn">过滤</button>
                    </div>
                    <a style="padding-left:1.5em">操作:</a>
                    <div id="sgagent_operate" class="btn-group">
                        <button value="1" class="btn btn-default">重启前端</button>
                        <button value="0" class="btn btn-default">重启worker</button>
                    </div>
                    <span id="ip-sum" style="float:right;"></span>
                </th>
            </tr>
            </thead>
        </table>
        <div style="max-height: 200px;overflow: auto;">
            <table class="table table-striped table-hover">
                <tbody>
                <tr id="content_overlay">
                    <td colspan="3">
                        <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div id="log" class="config-file">
        <h3>操作日志</h3>
        <div id="log-content" class="config-file log-content" style="overflow: auto;">
        </div>
    </div>
</div>
