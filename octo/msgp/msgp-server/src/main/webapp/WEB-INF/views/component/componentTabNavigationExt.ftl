<title>服务组件治理</title>

<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="clearfix">
        <label style="padding-left: 10px;float: right">
            <a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28354563" target="_blank" id="count_guide">服务组件治理常见问题
                <i class="fa fa-question-circle"></i> </a>
            <a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28327803" target="_blank" id="count_guide"> 服务组件治理使用说明</a>
        </label>
        <h3 class="page-header" id="page_header" style="float: left; height: 20px; line-height: 20px; border: none; margin-bottom: 0;">服务组件治理</h3>
    </div>
    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs" style="margin: 10px 0 20px 0">
            <li><a href="#cmpt_version">组件版本分布</a></li>
            <li><a href="#cmpt_coverage">组件覆盖清单</a></li>
            <li><a href="#cmpt_query">组件综合检索</a></li>
            <li><a href="#cmpt_stack">事业群技术栈</a></li>
            <li><a href="#cmpt_app">线上应用分布</a></li>
            <li><a href="#cmpt_user">组件自助处理</a></li>
        </ul>

        <div id="content_wrapper">
            <div id="wrap_cmpt_version" class="sheet" style="display:none;">
            <#include "cmpt_version.ftl" >
            </div>
            <div id="wrap_cmpt_coverage" class="sheet" style="display:none;">
            <#include "cmpt_coverage.ftl" >
            </div>
            <div id="wrap_cmpt_query" class="sheet" style="display:none;">
            <#include "cmpt_query.ftl" >
            </div>
            <div id="wrap_cmpt_stack" class="sheet" style="display:none;">
            <#include "cmpt_stack.ftl" >
            </div>
            <div id="wrap_cmpt_app" class="sheet" style="display:none;">
            <#include "cmpt_app.ftl" >
            </div>
            <div id="wrap_cmpt_user" class="sheet" style="display:none;">
            <#include "cmpt_user.ftl" >
            </div>
        </div>
        <script type="text/javascript" src="/static/js/jquery.min.js"></script>
        <script type="text/javascript" src="/static/js/jquery-ui.js"></script>
        <script src="/static/js/tooltip.js"></script>
        <script>
            M.use('msgp-component/componentTabNavigationExt', function (Y) {
                var tab_style = "${style}";
                var groupId = "${groupId}";
                var artifactId = "${artifactId}";
                Y.msgp.component.componentTabNavigationExt(tab_style, groupId, artifactId);
            });
        </script>
    </div>
</div>
