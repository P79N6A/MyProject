<title>配置报警</title>
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<link rel="stylesheet" type="text/css" href="/static/css/bootstrap-multiselect.css"/>
<script crossorigin="anonymous" src="//www.dpfile.com/app/owl/static/owl_1.5.13.js"></script>
<script>
    Owl.start({
        project: 'msgp-project',
        pageUrl: 'monitor-config'
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

    .dropdown-menu > li > a:hover, .dropdown-menu > li > a:focus, .dropdown-submenu:hover > a, .dropdown-submenu:focus > a {
        background-color: #3fab99;
    }

    .dropdown-menu > .active > a, .dropdown-menu > .active > a:hover, .dropdown-menu > .active > a:focus {
        background-color: #3fab99;
    }

</style>
<#include "/topLinkEvent.ftl" >
<div class="tab-box">
    <ul class="nav nav-tabs widget-edit-tab">
        <li class="current"><a href="/monitor/config?appkey=${appkey}">性能指标监控</a></li>
        <li><a href="/monitor/log?appkey=${appkey}">性能报警记录</a></li>
        <li><a href="/log/report?appkey=${appkey}">异常日志统计</a></li>
        <li><a href="/log/trend?appkey=${appkey}">异常日志趋势</a></li>
        <li><a href="/log/configuration/list?appkey=${appkey}">异常监控配置</a></li>
        <li><a href="/log/filter/list?appkey=${appkey}">异常过滤器配置</a></li>
        <li><a href="/monitor/business?screenId=${screenId!""}">业务监控配置</a></li>
        <li><a href="/monitor/business/dash/config?owt=${owt!""}">业务大盘配置</a></li>
        <li><a href="/monitor/provider/config?appkey=${appkey!""}">服务节点报警配置</a></li>
    </ul>
</div>
<div class="form-inline mt20 mb20">
    <button id="add_core_trigger" data-side="server" class="btn btn-primary ml20"><i class="fa fa-plus"> 添加核心接口监控</i>
    </button>
    <button id="add_trigger" data-side="server" class="btn btn-primary ml20"><i class="fa fa-plus"> 添加服务接口监控</i>
    </button>
    <button id="add_call_trigger" data-side="client" class="btn btn-primary ml20"><i class="fa fa-plus"> 添加外部接口监控</i>
    </button>
    <button id="subscribe_all_alarm" class="btn btn-primary ml20"><i class="fa fa-edit"> 批量修改订阅</i></button>

    <div style="float: right;margin-bottom: 0px">
        <a href="https://123.sankuai.com/km/page/28327856" target="_blank">服务性能报警使用说明<i
                class="fa fa-question-circle"></i></a>
    </div>
</div>

<div id="triggers_wrap">
    <div class="content-overlay">
        <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
    </div>
    <div class="content-body" style="display:none;">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="10%"></col>
                <col width="25%"></col>
                <col width="15%"></col>
                <col width="10%"></col>
                <col width="5%"></col>
                <col width="35%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>接口类型</th>
                <th>接口</th>
                <th>监控项</th>
                <th>持续时间(分钟)</th>
                <th>阈值</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="3">Loading contents...</td>
            </tr>
            </tbody>
        </table>
        <div id="paginator_monitor">
        </div>
    </div>
</div>
<textarea id="text-add-core-trigger-form" style="display:none">
    <div id="add-core-trigger-form">
        <div class="form-inline mt20 mb20">
            <span style="font-size: 16px; font-weight: bold;">核心接口定义: 服务自身对外提供的接口里依赖的其他服务的接口, 其实质还是外部接口。</span>
            <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin: 10px 0 0 0;"
                width="100%; " color=#987cb9 SIZE=1>
        </div>
        <div class="form-inline mt20 mb20">
            <div class="control-group">
                <label id="coreTriggerSide" data-side="<%= data.side %>">服务接口：</label>
                <input id="coreTriggerSpans" class="span3 coreTriggerSpans" style="width: 294px">
                <label id="tips"
                       style="padding-left: 20px; color: #CD5F15">使用提示: 首选选择一个服务接口, 然后选择外部接口(自动根据调用链分析得到)</label>
            </div>
        </div>
        <div class="form-inline mt20 mb20">
            <div class="control-group">
                 <label>外部接口：</label>
                <select id="coreTriggerClientSpans" class="span3 triggerSpans" multiple="multiple"></select>
            </div>

            <div class="control-group mt20">
                <label>监控指标：</label>
                <input id="coreTriggerItems" class="span3 coreTriggerItems" style="width: 100px">

                <label style="padding-left: 20px;">持续时间(分钟)：</label>
                <input id="coreTriggerDuration" type="text" class="span3" value="" placeholder="正整数"
                       style="width: 40px"/>

                <label style="padding-left: 20px; ">触发条件：</label>
                <select id="coreTriggerFunctions" class="span3" style="width: 60px">
                    <% Y.Object.each(this.functions, function(func, index){ %>
                    <option value="<%= func.name %>"><%= func.desc %></option>
                    <% }); %>
                </select>

                <label style="padding-left: 20px;">阈值：</label>
                <input id="coreTriggerThreshold" type="text" class="span3" value="200" placeholder="正整数"
                       style="width: 40px"/>
            </div>
        </div>
        <div class="form-inline mt20 mb20">
            <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;"
                width="100%; " color=#987cb9 SIZE=1>
        </div>
        <div class="form-inline mt20 mb20">
            <div class="control-group">
                <button class="btn btn-small" id="add-core-trigger-form-button"><i
                        class="fa fa-plus"></i>添加上述所选</button>
                <label style="padding-left: 10px;">显示已有监控项：</label>
                <input id="display-triggers-saved" type="checkbox" style="margin-bottom: 6px;"/>
                <span style="font-size: 12px; color: grey; float: right;">提示: 后添加的监控项会覆盖之前的同名(接口类型和名称一一对应)监控项</span>

            </div>
            <table style="width: 100%;" class="table table-striped" id="add_core_trigger_form_list">
                <thead>
                    <tr>
                        <th style="width: 10%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap ">接口类型</th>
                        <th style="width: 45%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">接口名称</th>
                        <th style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">监控指标</th>
                        <th style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">持续时间</th>
                        <th style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">阈值</th>
                        <th style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">操作</th>
                    </tr>
                </thead>
                <tbody id="triggers-selected">
                    <tr>
                        <td colspan="6" style="text-align: center;">未添加监控项</td>
                    </tr>
                </tbody>
                 <tbody id="triggers-saved-tip" style="display: none;">
                    <tr>
                        <td colspan="6" style="text-align: center; background: transparent;">以下为已设置的监控项</td>
                    </tr>
                </tbody>
                <tbody id="triggers-saved">
                </tbody>
            </table>
        </div>
    </div>
</textarea>

<textarea id="text-add-core-trigger-selected-tbody" style="display:none">
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-id="<%= item.id %>" data-side="<%= item.side %>" data-spanname="<%= item.spanname %>"
        data-item="<%= item.item %>" data-threshold="<%= item.threshold %>" data-function="<%= item.function %>"
        data-itemdesc="<%= item.itemDesc %>" data-duration="<%= item.duration %>"
        data-functiondesc="<%= item.functionDesc %>">
        <td style="width: 10%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><% if(item.side == "server") { %> 服务接口 <% } else { %> 外部接口 <% } %></td>
        <td style="width: 45%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.spanname %></td>
        <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.itemDesc + " " + item.functionDesc %></td>
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.duration %></td>
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.threshold %></td>
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><a
                class="core-trigger-delete" href="javascript:;">移除</a></td>
    </tr>
<% }); %>
</textarea>

<textarea id="text-add-core-trigger-saved-tbody" style="display:none">
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-id="<%= item.id %>" data-side="<%= item.side %>" data-spanname="<%= item.spanname %>"
        data-item="<%= item.item %>" data-threshold="<%= item.threshold %>" data-function="<%= item.function %>"
        data-itemdesc="<%= item.itemDesc %>" data-duration="<%= item.duration %>"
        data-functiondesc="<%= item.functionDesc %>">
        <td style="width: 10%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><% if(item.side == "server") { %> 服务接口 <% } else { %> 外部接口 <% } %></td>
        <td style="width: 45%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.spanname %></td>
        <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.itemDesc + " " + item.functionDesc %></td>
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.duration %></td>
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.threshold %></td>
        <td style="width: 8%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap">--</td>
    </tr>
<% }); %>
</textarea>

<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="/static/js/bootstrap-multiselect.js"></script>
<script>
    document.title = "配置报警";
    var key = '${appkey}';
</script>
<script type="text/javascript" src="/static/monitor/monitorConfig-version0.1.0.js"></script>
