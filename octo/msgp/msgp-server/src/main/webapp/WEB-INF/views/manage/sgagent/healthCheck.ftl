<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="form-inline mb20">
        <a>IP：</a>
        <div class="input-append">
            <input id="healthcheck_ip" class="span3" placeholder="IP地址或主机名" type="text">
            <button class="btn btn-primary" type="button" id="searchIP">查询</button>
            <button class="btn btn-default" type="button" id="restart">重启</button>
        </div>
    </div>

    <div class="content-body">

        <div id="health_check_data" style="padding-left: 30px;">
            <table class="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>Key</th>
                        <th>Value</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>

</div>