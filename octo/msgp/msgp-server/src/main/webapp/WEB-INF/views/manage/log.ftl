<title>异常日志</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <h3 class="page-header">异常日志</h3>
    <hr/>
    <#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
    <#include "/topLinkEvent.ftl" >

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
                    <col width="60%"></col>
                </colgroup>
                <thead>
                <tr>
                    <th>时间</th>
                    <th>级别</th>
                    <th>类型</th>
                    <th>内容</th>
                </tr>
                </thead>
                <tbody>
                <td colspan="3">Loading contents...</td>
                </tbody>
            </table>
            <div id="paginator_monitor">
            </div>
        </div>
    </div>
    <div id="paginator_wrapper">
    </div>
    <script>
        M.use('msgp-manage/octoLogList', function (Y) {
            document.title="异常日志";
            var key = '${appkey}';
            Y.msgp.manage.octoLogList(key);
        });
    </script>
</div>