<title>服务治理报告</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<div class="form-inline mt20 mb20" style="padding: 0px 0px 0px">
    <div class="control-group">
        <label style="padding-left:1em">选择业务线：</label>
        <input class="mb5 span3" type="text" id='owtSelect' value='' autocomplete="off"/>
        <label>报告时间：</label>
            <input id="start_time" type="text" class="span3" placeholder="时间" value='${date!""}'>
        <button id="query_btn" class="btn btn-primary ml20">查询</button>
            <label style="padding-left:1em" id="week_tips"></label>
            <div style="float: right;">
                <a href="https://123.sankuai.com/km/page/28465635" target="_blank">
                    服务治理报告使用说明<i class="fa fa-question-circle"></i>
                </a>
            </div>
    </div>

</div>


    <div class="tab-box">
        <ul id="tab_trigger" class="nav nav-tabs">
            <li><a href="#availability">服务可用率</a></li>
            <li><a href="#topqps">服务QPS</a></li>
            <li><a href="#toptp">日均性能数据</a></li>
            <li><a href="#client">依赖服务</a></li>
            <li><a href="#server">被依赖服务</a></li>
            <li><a href="#error">服务错误日志</a></li>
            <li><a href="#qpspeak">服务QPS峰值均值</a></li>
            <li><a href="#idc">机房流量分布</a></li>
        </ul>
        <div id="content_wrapper">
            <div id="wrap_availability" class="sheet" style="display:none;">
            <#include "availability.ftl" >
            </div>
            <div id="wrap_topqps" class="sheet" style="display:none;">
            <#include "topqps.ftl" >
            </div>
            <div id="wrap_toptp" class="sheet" style="display:none;">
            <#include "toptp.ftl">
            </div>
            <div id="wrap_client" class="sheet" style="display:none;">
            <#include "client.ftl" >
            </div>
            <div id="wrap_server" class="sheet" style="display:none;">
            <#include "server.ftl" >
            </div>
            <div id="wrap_error" class="sheet" style="display:none;">
            <#include "error.ftl" >
            </div>
            <div id="wrap_qpspeak" class="sheet" style="display:none;">
            <#include "qpspeak.ftl" >
            </div>
            <div id="wrap_idc" class="sheet" style="display:none;">
            <#include "idc.ftl" >
            </div>
        </div>
        <script>
            var owtList = [<#list owtList![] as owt>'${owt}',</#list>];
            var owt = '${owt!""}';
            var date = '${date!""}';
        </script>
        <script type="text/javascript" src="/static/servicerep/reportTabNav.js"></script>
    </div>
</div>
</div>