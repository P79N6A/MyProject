<title>sg_agent主机</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="form-inline mt20 mb20">
        <legend>${version}</legend>
    </div>
    <div id="body_wrap">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="25%"></col>
                <col width="25%"></col>
                <col width="25%"></col>
                <col width="25%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>节点</th>
                <th>环境</th>
                <th>角色</th>
                <th>节点状态</th>
            </tr>
            </thead>
            <tbody>
            <tr><td colspan="4">Loading contents...</td></tr>
            </tbody>
        </table>
    </div>
    <script>
        M.use('msgp-manage/providerByVersion', function (Y) {
            var envId  = '${envId}';
            var version = '${version}';
            var thrifttype = '${thrifttype}';
            Y.msgp.manage.providerByVersion(version, envId, thrifttype);
        });
    </script>
</div>