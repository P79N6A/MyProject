<title>服务上下游</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_stream"></div>
<textarea id="text_data_stream" style="display:none">
    <div class="form-inline mb20">
        <div class="control-group">
            <label style="padding-left:1em"> 开始时间：</label>
            <input id="start_time" type="text" class="span3" value="${start}"  placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label>
            <input id="end_time" type="text" class="span3" value="${end}" placeholder="查询结束时间">
            <button id="query_btn" class="btn btn-primary ml20">查询</button>
            <a href="https://123.sankuai.com/km/page/28355029" target="_blank" style="float: right;">上下游信息使用说明<i
                    class="fa fa-question-circle"></i></a>
    </div>
    <hr>
    <div class="content-body">
        <table class="table table-striped table-hover" id="depend_app">
            <colgroup>
                <col width="20%"></col>
                <col width="15%"></col>
                <col width="10%"></col>
                <col width="10%"></col>
                <col width="10%"></col>
                <col width="10%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>服务</th>
                <th>调用量</th>
                <th>tp90</th>
                <th>falcon报警</th>
                <th>octo性能报警</th>
                <th>errorLog日志</th>
            </tr>
            </thead>
            <tbody id="tbody_app"></tbody>
        </table>

        <table class="table table-striped">
            <tr>
                <td>
                    <table class="table table-hover" id="depend_server">
                        <caption>上游服务</caption>
                        <thead>
                        <tr>
                            <th>服务<i class="fa fa-sort"/></th>
                            <th>调用量<i class="fa fa-sort"/></th>
                            <th>tp90<i class="fa fa-sort"/></th>
                            <th>falcon报警<i class="fa fa-sort"/></th>
                            <th>octo性能报警<i class="fa fa-sort"/></th>
                            <th>errorLog日志<i class="fa fa-sort"/></th>
                        </tr>
                        </thead>
                        <tbody id="tbody_server">

                        </tbody>
                    </table>
                </td>

                <td>
                    <table class="table table-hover" id="depend_client">
                        <caption>下游服务</caption>
                        <thead>
                        <tr>
                            <th>服务<i class="fa fa-sort"/></th>
                            <th>调用量<i class="fa fa-sort"/></th>
                            <th>tp90<i class="fa fa-sort"/></th>
                            <th>falcon报警<i class="fa fa-sort"/></th>
                            <th>octo性能报警<i class="fa fa-sort"/></th>
                            <th>errorLog日志<i class="fa fa-sort"/></th>
                        </tr>
                        </thead>
                        <tbody id="tbody_client"></tbody>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</textarea>
