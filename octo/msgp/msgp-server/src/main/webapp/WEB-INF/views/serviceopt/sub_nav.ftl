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
        pageUrl: 'serverOpt-config'
    })
</script>
<div class="tab-box">
    <ul id="tab_trigger" class="nav nav-tabs">
        <li><a href="#config">配置管理</a></li>
        <li><a href="#routes">服务分组</a></li>
        <li><a href="#thriftCutFlow">Thrift截流</a></li>
<#--
        <li><a href="#cutFlow">一键截流</a></li>
-->
    <#--
            <li><a href="#httpCutFlow">HTTP截流<span class="corner corner-danger">New!</span></a></li>
    -->
        <li><a href="#httpConfig">HTTP设置</a></li>
        <li><a href="#accessCtrl" id="access_ctrl_title">访问控制</a></li>
        <li><a href="#appkeyAuth">服务鉴权<span class="corner corner-danger">New!</span></a></li>
    <#--<li><a href="#hulkPolicy" >弹性伸缩<span class="corner corner-danger">New!</span></a></li>-->
    <#--<li><a href="#manuScaleOut" >一键扩容<span class="corner corner-danger">New!</span></a></li>-->
        <li><a href="#hulkOption">弹性伸缩<span class="corner corner-danger">New!</span></a></li>
    <#--<li><a href="#hostManage">主机诊断</a></li>&lt;#&ndash;主机管理&ndash;&gt;-->
        <li><a href="#xmdlog">日志级别调整</a></li>
        <li><a href="#syslog">操作记录</a></li>

    <#--<li><a href="#quota">配额</a></li>-->
    </ul>
    <div id="content_wrapper">
        <div id="wrap_config" class="sheet" style="display:none;">
        <#include "detail_config.ftl" >
        </div>
        <div id="wrap_routes" class="sheet" style="display:none;">
        <#include "detail_routes.ftl" >
        </div>
        <div id="wrap_thriftCutFlow" class="sheet" style="display:none;">
        <#include "detail_thriftCutFlow.ftl" >
        </div>
    <#-- <div id="wrap_httpCutFlow" class="sheet" style="display:none;">
     <#include "detail_httpCutFlow.ftl" >
     </div>-->
        <div id="wrap_httpConfig" class="sheet" style="display:none;">
        <#include "detail_httpConfig.ftl" >
        </div>
        <div id="wrap_accessCtrl" class="sheet" style="display:none;">
        <#include "access/index.ftl" >
        </div>
        <div id="wrap_appkeyAuth" class="sheet" style="display:none;">
        <#include "appkey_auth.ftl" >
        </div>
    <#--<div id="wrap_hulkPolicy" class="sheet" style="display:none;">-->
    <#--<#include "detail_hulkPolicy.ftl" >-->
    <#--</div>-->
    <#--<div id="wrap_manuScaleOut" class="sheet" style="display:none;">-->
    <#--<#include "detail_manuScaleOut.ftl" >-->
    <#--</div>-->
        <div id="wrap_hulkOption" class="sheet" style="display:none;">
        <#include "detail_hulkOption.ftl" >
        </div>
    <#--<div id="wrap_hostManage" class="sheet" style="display:none;">-->
    <#--<#include "host/hostManage.ftl" >-->
    <#--</div>-->
        <div id="wrap_xmdlog" class="sheet" style="display:none;">
        <#include "detail_xmdlog.ftl" >
        </div>
        <div id="wrap_syslog" class="sheet" style="display:none;">
        <#include "detail_syslog.ftl" >
        </div>
    </div>
</div>