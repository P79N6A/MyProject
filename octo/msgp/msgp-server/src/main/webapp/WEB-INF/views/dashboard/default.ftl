<title>DashBoard - MSGP</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="dashboard-logo">
        <div class="input-append">
            <input id="search_input" class="span6" type="text" placeholder="appkey、负责人、标签，为空表示查询全部" />
            <button id="search_button" class="btn btn-primary" type="button">查询服务</button>
        </div>
        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28178492" target="_blank">
                服务如何接入OCTO<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
    <div class="tab-box" style="padding-top:25px;padding-left: 45px;">
        <ul id="tab_trigger" class="nav nav-tabs">
            <li><a href="#dashboard">OCTO大盘</a></li>
            <li><a href="#status">按状态分布</a></li>
            <li><a href="#idc">按机房分布</a></li>

        </ul>
        <div id="dashboard_wrapper">
            <div id="wrap_dashboard" class="sheet" style="display:none;">
            <#include "dashboard.ftl" >
            </div>
            <div id="wrap_status" class="sheet" style="display:none;">
            <#include "dashboard_status.ftl" >
            </div>
            <div id="wrap_idc" class="sheet" style="display:none;">
            <#include "dashboard_idc.ftl" >
            </div>
        </div>
    </div>
</div>
    <script type="text/javascript" src="/static/dashboard/dashboard-tab.js"></script>
</div>
