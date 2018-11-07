<title>组件依赖管理</title>

<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="clearfix">
        <label style="padding-left: 10px;float: right">
            <a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28326939" target="_blank" id="count_guide">组件依赖管理使用说明
                <i class="fa fa-question-circle"></i></a>
        </label>
            <h3 class="page-header" id="page_header" style="float: left; height: 20px; line-height: 20px; border: none; margin-bottom: 0;">组件依赖管理</h3>
    </div>
    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs" style="margin: 10px 0 20px 0">
            <li><a href="#cmpt_trend">组件使用趋势</a></li>
            <li><a href="#cmpt_message">组件依赖提醒</a></li>
            <li><a href="#cmpt_config">组件版本控制</a></li>
        </ul>

        <div id="content_wrapper">
            <div id="wrap_cmpt_trend" class="sheet" style="display:none;">
            <#include "cmpt_trend.ftl" >
            </div>
            <div id="wrap_cmpt_message" class="sheet" style="display:none;">
            <#include "cmpt_message.ftl" >
            </div>
            <div id="wrap_cmpt_config" class="sheet" style="display:none;">
            <#include "cmpt_config.ftl" >
            </div>
        </div>
        <script type="text/javascript" src="/static/js/jquery.min.js"></script>
        <script type="text/javascript" src="/static/js/jquery-ui.js"></script>
        <script src="/static/js/tooltip.js"></script>
        <script>
            M.use('msgp-component/componentTabNavigation', function (Y) {
                var tab_style = "${style}";
                var groupId = "${groupId}";
                var artifactId = "${artifactId}";
                Y.msgp.component.componentTabNavigation(tab_style, groupId, artifactId);
            });
        </script>
    </div>
</div>
