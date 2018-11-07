<title>服务列表</title>
<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: #3fab99;
    }
    .common-popdialog {
        overflow: scroll;
    }
</style>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">

    <div id="searchForm" class="form-horizontal">
        <div class="input-append">
            <input id="searchBox" class="span6" placeholder="appkey、负责人、标签，为空表示查询全部" type="text"  value="${keyword!""}" />
            <button class="btn btn-primary" type="button" id="searchBtn">&nbsp;&nbsp;查询服务</button>
        </div>
        <div class="tab-box" style="margin-top:20px;">
            <ul class="nav nav-tabs">
                <li><a class="classify_btn" value="3" href="javascript:void(0)">负责的服务</a></li>
                <li><a class="classify_btn" value="1" href="javascript:void(0)">关注的服务</a></li>
                <li><a class="classify_btn" value="2" href="javascript:void(0)">业务线服务</a></li>
                <li><a class="classify_btn" value="0" href="javascript:void(0)">事业群服务</a></li>
                <li><a class="classify_btn" value="4" href="javascript:void(0)">全部服务</a></li>
            </ul>
        </div>
    </div>
    <div id="table_wrapper" class="table-responsive mt20">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="15%"></col>
                <col width="10%"></col>
                <col width="20%"></col>
                <col width="15%"></col>
                <col width="10%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <!-- 唯一标识，可选tag：业务线、发布系统key、主机名前缀、其他key -->
                <th>唯一标识</th>
                <th>所属业务</th>
                <th>负责人</th>
                <th>强制验证主机列表</th>
                <th>描述信息</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <!-- 示例 -->
            <tr>
                <td colspan="6"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="paginator_wrapper">
    </div>
    <script type="text/javascript" src="/static/servicedetail/list-version0.1.1.js"></script>
</div>
