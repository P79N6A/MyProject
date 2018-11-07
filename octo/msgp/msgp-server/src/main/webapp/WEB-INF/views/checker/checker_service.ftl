<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="content-body">
        <div class="form-inline mb20">

            <a>协议:</a>

            <div id="thrift_http" class="btn-group">
                <a value="1" type="button" id="supplier_type" class="btn btn-primary" href="javascript:void(0)">thrift</a>
                <a value="2" type="button" id="supplier_type" class="btn btn-default" href="javascript:void(0)">http</a>
            </div>
            &nbsp;&nbsp;
            <a>client端IP:</a>

            <div class="input-append">
                <input id="search_ip" class="mb5 span4" type="text"></input>
                <button id="search_button" class="btn btn-primary" type="button">查询</button>
            </div>
            <input id="search_ip_port" class="mb5 span3" type="text"
                   placeholder="过滤IP、端口(选填)，以空格隔开"></input>
            <#--<input id="search_port" class="mb5 span2" type="text" placeholder="过滤端口(选填)"></input>-->

        </div>
        <div id="table_wrapper" class="table-responsive mt20">
            <table class="table table-striped table-hover">
                <colgroup>
                    <col width="5%"></col>
                    <col width="7%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="5%"></col>
                    <col width="15%"></col>
                </colgroup>
                <thead>
                <tr>
                    <th>IP地址</th>
                    <th>版本</th>
                    <th>端口</th>
                    <th>weight</th>
                    <th>fweight</th>
                    <th>状态</th>
                    <th>角色</th>
                    <th>泳道</th>
                    <th>单元</th>
                    <th>环境</th>
                    <th>最后更新时间</th>
                </tr>
                </thead>
                <tbody>
                <tr class="content-overlay">
                    <td colspan="11">
                        <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                    </td>
                </tr>
                </tbody>
            </table>
            <div id="paginator_monitor">
            </div>
        </div>
    </div>
    <div id="paginator_wrapper">
    </div>
</div>