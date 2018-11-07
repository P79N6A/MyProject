<style>
    .common-popdialog .head h3 {
        background-color: #3fab99;
    }

    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

</style>
<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script crossorigin="anonymous" src="//www.dpfile.com/app/owl/static/owl_1.5.13.js"></script>
<script>
    Owl.start({
        project: 'msgp-project',
        pageUrl: 'checker-checkerHostInfo'
    })
</script>
<div class="tab-box">
    <ul id="tab_trigger" class="nav nav-tabs">
        <li><a href="#checkerHostInfo">主机信息</a></li>
        <li><a href="#checkerService">服务列表自检</a></li>
        <li><a href="#checkerConfig">配置获取自检</a></li>
        <li><a href="#thriftCheck">Mtthrift自检</a></li>
    </ul>
    <div id="content_wrapper">
        <div id="wrap_checkerHostInfo" class="sheet" style="display:none;">
        <#include "checker_hostInfo.ftl" >
        </div>
        <div id="wrap_checkerService" class="sheet" style="display:none;">
        <#include "checker_service.ftl" >
        </div>
        <div id="wrap_checkerConfig" class="sheet" style="display:none;">
        <#include "checker_config.ftl" >
        </div>
        <div id="wrap_thriftCheck" class="sheet" style="display:none;">
        <#include "thriftCheck.ftl" >
        </div>
    </div>
</div>