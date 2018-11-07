<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<link rel="stylesheet" type="text/css" href="/static/css/bootstrap-multiselect.css"/>
<script crossorigin="anonymous" src="//www.dpfile.com/app/owl/static/owl_1.5.13.js"></script>
<script src="/static/js/jquery-2.2.3.min.js"></script>
<script>
    Owl.start({
        project: 'msgp-project',
        pageUrl: 'personal'
    })
</script>
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

    .multiselect-all label {
        font-weight: bold;
    }
    .multiselect-search {
        margin-left: 10px;
    }

    .input-group-btn {
        display: none;
    }

    .multiselect-container li{
        text-align: left;
    }

    .dropdown-menu>li>a:hover, .dropdown-menu>li>a:focus, .dropdown-submenu:hover>a, .dropdown-submenu:focus>a{
        background-color: #3fab99;
    }

    .dropdown-menu>.active>a, .dropdown-menu>.active>a:hover, .dropdown-menu>.active>a:focus {
        background-color: #3fab99;
    }


    .form-inline {
        padding: 0;
    }

    div.tooltip-inner {
        text-align: left;
        max-width: 100%;
        width:auto;
        background-color: #505050;
        font-size: 14px;
    }


</style>
<title>订阅中心</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="page-header">
        <div class="input-append">
            <input id="search_input" class="span6" type="text" placeholder="appkey、负责人、标签，为空表示查询全部"/>
            <button id="search_button" class="btn btn-primary" type="button">查询服务</button>
        </div>
        <script type="text/javascript" src="/static/dashboard/bindSearch.js"></script>
        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28178492" target="_blank">
                服务如何接入OCTO<i class="fa fa-question-circle"></i>
            </a>
            <br>
            <a href="https://123.sankuai.com/km/page/28125601" target="_blank">
                基础服务架构协议<i class="fa fa-question-circle"></i>
            </a>
        </div>

    </div>
    <div class="tab-box">
        <ul class="nav nav-tabs widget-edit-tab">
            <li><a href="/personal">个人主页</a></li>
            <li class="current"><a href="/subscribe">订阅中心</a></li>
            <li><a href="/error">报错大盘</a></li>
            <li><a href="/database">数据库大盘</a></li>
            <li><a href="/tair">缓存大盘</a></li>
        </ul>
    </div>
</div>

<div id="subs_wrap">
    <div class="content-body">
        <div class="form-inline mb20" style="padding:10px 0px 0px">
            <div id="all-enabled" class="btn-group btn-enabled">
                <button data-enabled="1" class="btn btn-small btn-alive" title="订阅全部">订阅全部</button>
                <button data-enabled="0" class="btn btn-small btn-dead" title="取消订阅全部">取消订阅全部</button>
            </div>
        </div>

        <table id="subs_container" class="table table-striped table-hover" id="falcon_alarm">
            <colgroup>
                <col width="5%">
                <col width="30%">
                <col width="15%">
                <col width="20%">
                <col width="20%">
            </colgroup>
            <thead>
            <tr>
                <th><input id="all-check" type="checkbox">
                </th>
                <th>服务</th>
                <th>负责人</th>
                <th>报表订阅</th>
                <th>节点报警订阅</th>
                <th>性能报警订阅</th>
            </tr>
            </thead>
            <tbody id="subs_content_body">
            <tr id="content_overlay" class="content-overlay">
                <td colspan="6" style="text-align: center;">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>
        <div id="paginator_subs"></div>
    </div>
</div>

<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="/static/js/bootstrap-multiselect.js"></script>

<script>
    M.use('msgp-dashboard/subs', function (Y) {
        Y.msgp.dashboard.subs();
    });
</script>
