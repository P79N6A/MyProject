<style>
    .common-popdialog .btn.btn-primary:hover, .common-popdialog .btn.btn-primary:focus {
        background-color: #3fab99;
        color: #fff;
</style>
<div class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="content-body" style="display:none;">
    <div class="form-inline mb20">
        <button id="add_route" type="button" class="btn btn-primary" title="创建自定义分组">
            <i class="fa fa-plus">新增</i>
        </button>
        <button id="add_route_http" type="button" class="btn btn-primary" title="创建自定义分组">
            <i class="fa fa-plus">新增</i>
        </button>
        <a>类型：</a>

        <div id="routes_thrift_http" class="btn-group">
            <a value="1" type="button" id="supplier_type" class="btn <#if (!type?? || type='1')>btn-primary</#if>"
               href="javascript:void(0)">thrift</a>
            <a value="2" type="button" id="supplier_type"
               class="btn btn-default <#if (type?exists && type='2')>btn-primary</#if>"
               href="javascript:void(0)">http</a>
        </div>
    <#include "/common/env.ftl" >
        <div id="routes_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <#if isOffline>
                <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
            </#if>
        </div>
    </div>
    <div id="table_routes" class="table-responsive">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="13%"></col>
                <col width="12%"></col>
                <col width="11%"></col>
                <col width="11%"></col>
                <col width="13%"></col>
                <col width="13%"></col>
                <col width="13%"></col>
                <col width="17%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>分组名称</th>
                <th>环境</th>
                <th>类型</th>
                <th>优先级</th>
                <th>创建时间</th>
                <th>更新时间</th>
                <th>状态</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="8">Loading contents...</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="paginator_routes">
    </div>
    <div id="group_detail_statement">
        <hr/>
        <p><b>非强制：</b>非强制状态下启用分组，会优先访问分组指定服务节点，只有当指定服务节点异常时，才会访问其他服务节点。</p>

        <p><b>强制：</b>强制状态下启用分组，会强制只访问分组指定的服务节点。</p>

        <p><b>同机房优先：</b>无需配置，自动生成同机房访问策略。非强制状态下会优先访问本机房服务节点；强制状态下只访问本机房服务节点。</p>

        <p><b>同中心优先：</b>无需配置，自动生成同中心访问策略。非强制状态下会优先访问同中心服务节点；强制状态下只访问同中心服务节点。多中心的划分详情见:<a href="https://123.sankuai.com/km/page/14751216" target="_blank">多中心<i class="fa fa-question-circle"></i>
        </a></p>

        <p><b>自定义分组：</b>可创建多个自定义分组，优先级由高到低生效。非强制状态下，特定来源IP优先访问分组指定服务节点；强制状态下只访问指定服务节点。</p>
        <p><b>服务端排他分组：</b>分组内的服务节点只允许分组内的调用方节点访问，其他非组内指定节点无法访问</p>
    </div>
    <div id="group_http_statement">
        <hr/>
        <p style="color: #ff0000"><b>http分组周知：</b>http一期分组不支持变更同步，请及时更新新的分组，如http流量有使用老分组，请联系sre在oceanus更新新的分组名称。新分组落地配置格式为：appkey__GROUP__分组名(注意是双下滑线)</p>
        <p style="color: #ff0000">若在使用过程还有其他问题，请咨询姚剑鹏或者周峰</p>

    </div>

    <textarea id="text_addrouter_dialog" style="display:none">
        <% if(this.isAdd){ %>
        <div class="control-group" style="margin-bottom: 30px;">
            <div id="routes_ip_idc" class="btn-group" style="float: left;">
             <a value="0" type="button" id="category_type" class="btn btn-primary"
                href="javascript:void(0)">ip分组</a>
             <a value="2" type="button" id="category_type"
                class="btn btn-default"
                href="javascript:void(0)">idc分组</a>
            </div>
            <div style="padding-top: 10px;">
                 <label style="padding-left: 8px; float: left;" >显示使用说明：</label>
                <input id="show-instructions" type="checkbox" style="margin-bottom: 6px; float: left;"/>
            </div>
        </div>

        <%}%>
        <div id="add_supplier_form" class="form-horizontal" style="padding: 20px 0 0 0">
            <table width="100%">
                <tr style="display: none;" id="route-instructions">
                    <td colspan="4" style="font-size: 12px; line-height: 12px; padding: 10px 0 30px 30px;">
                        <p><b>优先级: </b>数字越大优先级越高;</p>
                        <p><b>调用方: </b>选定IP、服务，必须配置其中一项，可同时配置，符合该条件的将优先访问右侧服务节点;</p>
                        <p><b>服务和IP地址: </b>从负责的服务列表中选定一项，自动获取到所选中服务的IP地址列表，从IP地址列表中选中的IP列入到调用方IP地址区域中;</p>
                        <p><b>服务节点: </b>从服务节点列表中选定某几项，承载来自于左侧调用方的请求，必须配置。</p>
                        <p style="color: red;">若在使用过程中还有其他问题, 请咨询OCTO技术支持(infocto)</p>

                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <div class="form-inline mb20">
                            <label>分组名称：</label>
                            <input id="name_input" type="text" value="<%= this.name %>" placeholder="分组名称，必填"/><span class="tips"></span>
                        </div>
                    </td>
                    <td colspan="2">
                         <div class="form-inline mb20" id="exclude" >
                            <label>分组特性：</label>
                            <label class="checkbox mb0">
                                <input id="exclude-group" value="0" type="checkbox" <%
                                if(this.pre_category=="4"){ %> checked <%} else {%> ""<% }%>/>
                                <span>服务端排他分组</span><i class="fa fa-question-circle" id="exclude-group-desc"></i>
                            </label>
                         <#--<input id="exclude-group" value="0" type="radio" >&nbsp;普通分组-->
                         <#--&nbsp;&nbsp;<input id="exclude-group" value="4" type="radio"-->
                         <#--style="margin-bottom: 5px;">&nbsp;排他性分组-->
                         </div>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <div class="form-inline mb20">
                            <label >优先级别：</label>
                            <input id="priority_input" type="text" value="<%= this.priority%>" placeholder="优先级，必填"/><span class="tips"></span>
                        </div>
                    </td>
                    <td colspan="2">
                        <label id="cell-feature">分组特性：</label>
                        <div class="form-inline mb20" id="force" >
                            <label class="checkbox mb0" style="padding-left: 75px">
                                <input id="force-change" type="checkbox"  name="forceChange"
                                       data-force="route_limit:1" <%
                                if(this.reserved=="route_limit:1"){ %> checked <%} else {%> ""<% }%>>
                                <span>强制</span><i class="fa fa-question-circle" id="force-change-desc"></i>
                            </label>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">
                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:15px 0 15px 0;"
                            width="100%; " color=#987cb9 SIZE=1>
                    </td>
                </tr>
                <tr>
                    <td colspan="4">
                         <!--设置调用方-->
                         <div class="control-group mb0" id="div_consumer_provider">
                            #div_consumer_provider#
                        </div>
                    </td>
                </tr>
               <#-- <tr>
                    <td colspan="4" style="font-size: 12px; line-height: 12px; padding: 30px 0 10px 0">
                        <p><b>说明: </b></p>
                        <p>优先级: 数字越大优先级越高;</p>
                        <p>调用方: 选定IP、服务，必须配置其中一项，可同时配置，符合该条件的将优先访问右侧服务节点;</p>
                        <p>服务和IP地址: 从负责的服务列表中选定一项，自动获取到所选中服务的IP地址列表，从IP地址列表中选中的IP列入到调用方IP地址区域中。</p>
                        <p>服务节点: 从服务节点列表中选定某几项，承载来自于左侧调用方的请求，必须配置。</p>
                    </td>
                </tr>-->

            </table>
        </div>
</textarea>
<textarea id="text_consumer_provider_0" style="display:none">
        <div class="block-left-45">
            <h4 class="text-center mt0 mb10">调用方</h4>
            <div style="height: 400px;">
                 <div style="margin-top:5px;">
                    <div class="form-inline mb20">
                        <label>服务名：</label>
                        <input style="width:300px;font-size:12px;" type="text" id="optRoutes_apps_select" autocomplete="off"></div>
                </div>

                <div class="form-inline mb20">
                    <label>IP地址：</label>
                    <input style="width:300px;font-size:12px;" type="text" id="optRoutes_ip_select" autocomplete="off">
                </div>

                 &lt;textarea id="add_ip_textarea" placeholder="调用方IP地址 ..."
                style="width:360px;overflow: auto; border-radius:0px;height: 100px; margin-left: 30px;">&lt;/textarea&gt;
                <button class="btn btn-primary" type="button" id="add_IPs_manual"
                         style="width:280px;border-radius: 5px;border-top: 0;height: 30px; margin: 20px 70px 20px 70px;">添加</button>
                <div id="consumer_ips_ul" class="box dialog-ul-wrapper-inner ips-ul-wrapper"
                     style="width:372px; overflow: auto;height: 100px;margin-bottom: 0;margin-left: 30px;"></div>
            </div>
            <span class="tips" id="manual_add_ip_tips"></span>
        </div>

        <div class="block-middle-10">
            <p class="text-center mb5" style="font-size:24px;font-weight:bold;margin-top:150px; color: #3fab99">
                -&gt;
            </p>
        </div>
        <div class="block-right-45">
            <h4 class="text-center mt0 mb10">服务节点</h4>
            <input type="text" style="width: 95%;margin-bottom: 0;" id="route_provider_search_input" placeholder="ip 或者 主机名 或者 端口，以空格分开"/>
            <div id="provider_ips_ul" class="box dialog-ul-wrapper provider-ul-wrapper" style="overflow: auto;height: 380px;margin-bottom: 0;">
                <span id="ip-all-check-node" style="cursor:pointer;color: #3fab99;">全选/</span>
                <span id="ip-all-uncheck-node" style="cursor:pointer;color: #3fab99;">反选</span>
            </div>
        </div>
</textarea>
 <textarea id="text_consumer_provider_2" style="display:none">
        <div class="block-left-45">
            <h4 class="text-center mt0 mb10">调用方</h4>
            <div style="height: 400px;">
                <div class="form-inline mb20">
                    <label>服务名：</label>
                    <input style="width:300px;font-size:12px;" type="text" id="optRoutes_idc_apps_select" autocomplete="off">
                </div>
                <div id="consumer_idc_ul" class="box dialog-ul-wrapper-inner ips-ul-wrapper" style="overflow: auto;height: 260px;margin-bottom: 0; width: 372px;  margin-left: 30px;">
                    <span id="consumer-idc-all-check-node" style="cursor:pointer;color: #3fab99;">全选/</span>
                    <span id="consumer-idc-all-uncheck-node" style="cursor:pointer;color: #3fab99;">反选</span>
                </div>
            </div>
        </div>

        <div class="block-middle-10">
            <p class="text-center mb5 text-primary" style="font-size:24px;font-weight:bold;margin-top:150px;">
                -&gt;
            </p>
        </div>

        <div class="block-right-45">
            <h4 class="text-center mt0 mb10">服务节点</h4>
            <div id="provider_idc_ul" class="box dialog-ul-wrapper provider-ul-wrapper" style="overflow: auto;height: 310px;margin-bottom: 0px;">
                <span id="provider-idc-all-check-node" style="cursor:pointer;color: #3fab99;">全选/</span>
                <span id="provider-idc-all-uncheck-node" style="cursor:pointer;color: #3fab99;">反选</span>
            </div>
        </div>
</textarea>
    <textarea id="text_consumer_provider_4" style="display:none">
        <div class="block-left-45">
            <h4 class="text-center mt0 mb10">调用方</h4>
            <div style="height: 400px;">
                 <div style="margin-top:5px;">
                    <div class="form-inline mb20">
                        <label>服务名：</label>
                        <input style="width:300px;font-size:12px;" type="text" id="optRoutes_apps_select" autocomplete="off"></div>
                </div>

                <div class="form-inline mb20">
                    <label>IP地址：</label>
                    <input style="width:300px;font-size:12px;" type="text" id="optRoutes_ip_select" autocomplete="off">
                </div>

                 &lt;textarea id="add_ip_textarea" placeholder="调用方IP地址 ..."
                style="width:360px;overflow: auto; border-radius:0px;height: 100px; margin-left: 30px;">&lt;/textarea&gt;
                <button class="btn btn-primary" type="button" id="add_IPs_manual"
                        style="width:280px;border-radius: 5px;border-top: 0;height: 30px; margin: 20px 70px 20px 70px;">添加</button>
                <div id="consumer_ips_ul" class="box dialog-ul-wrapper-inner ips-ul-wrapper"
                     style="width:372px; overflow: auto;height: 100px;margin-bottom: 0;margin-left: 30px;"></div>
            </div>
            <span class="tips" id="manual_add_ip_tips"></span>
        </div>

        <div class="block-middle-10">
            <p class="text-center mb5" style="font-size:24px;font-weight:bold;margin-top:150px; color: #3fab99">
                -&gt;
            </p>
        </div>
        <div class="block-right-45">
            <h4 class="text-center mt0 mb10">服务节点</h4>
            <input type="text" style="width: 95%;margin-bottom: 0;" id="route_provider_search_input" placeholder="ip 或者 主机名 或者 端口，以空格分开"/>
            <div id="provider_ips_ul" class="box dialog-ul-wrapper provider-ul-wrapper" style="overflow: auto;height: 380px;margin-bottom: 0;">
                <span id="ip-all-check-node" style="cursor:pointer;color: #3fab99;">全选/</span>
                <span id="ip-all-uncheck-node" style="cursor:pointer;color: #3fab99;">反选</span>
            </div>
        </div>
</textarea>
    <textarea id="text_routers" style="display:none"><% Y.Array.each(this.data, function(item,index){ %>
       <tr data-info="<%= Y.JSON.stringify(item) %>" data-envdesc="<%= item.envDesc %>" data-id="<%=item.id%>">
           <td>
               <% if(item.category===1 || item.category === 3){ %>
               <span class="no-change" title="默认分组不能修改"><%= item.name %></span>
               <% }else{ %>
               <span class="change-name" title="点击修改"><%= item.name %></span>
               <% } %>
           </td>
           <td><%= item.envDesc %></td>
           <td><%= item.categoryDesc %></td>
           <td>
               <% if(item.category===1 || item.category === 3 ){ %>
               <span class="no-change" title="默认分组不能修改"><%= item.priority %></span>
               <% }else{ %>
               <span class="change-priority" title="点击修改"><%= item.priority %></span>
               <% } %>
           </td>
           <td><%= Y.mt.date.formatDateByString( new Date(item.createTime), "yyyy-MM-dd hh:mm:ss" ) %></td>
           <td><%= Y.mt.date.formatDateByString( new Date(item.updateTime), "yyyy-MM-dd hh:mm:ss" ) %></td>
           <td>
               <div class="btn-group btn-enabled <%= item.category===1?"is-default":"" %> <%= item.category===3 ?"is-multicenter":"" %>">
               <button data-status="1"  class="btn btn-mini btn-alive <%= item.status==1?"active":"" %>">启用</button>
               <button data-status="0"  class="btn btn-mini btn-dead <%= item.status==0?"active":"" %>">禁用</button></div>
            </td>
            <td>
                <% if(item.category ===1 || item.category === 3){ %>
                <div class="btn-group btn-forced <%= item.category===1?"is-default":"" %> <%= item.category===3 ?"is-multicenter":"" %>">
                <button data-force="route_limit:0" class="btn btn-mini btn-normal <%= (item.reserved=="route_limit:0" || item.reserved=="")?"active":"" %>">非强制</button>
                <button data-force="route_limit:1" class="btn btn-mini btn-force <%= item.reserved=="route_limit:1"?"active":"" %>">强制</button>
                </div>
                <% }else{ %>
                <a class="do-edit config-panel-read" data-category="<%=item.category %>"  href="javascript:;"  ><i class="fa fa-edit"></i>编辑</a>
                <a class="do-delete config-panel-delete"   href="javascript:;">&nbsp;&nbsp;&nbsp;<i class="fa fa-trash-o"></i> 删除</a>
                <% } %>
            </td>
        </tr>
<% }); %>
</textarea>


<textarea id="text_dialog_ul" style="display:none"><ul class="unstyled dialog-ul">
        <% Y.Array.each(this.data, function(item,index){ %>
        <li class="">
            <label class="checkbox mb0">
                <input value="<%=item%>"  type="checkbox"/>
                <span><%= item %></span>
            </label>
        </li>
        <% }); %>
    </ul></textarea>

<textarea id="text_route_thrift_provider_ul" style="display:none"><ul class="unstyled dialog-ul">
        <% Y.Array.each(this.data, function(item,index){ %>
        <li class="">
            <label class="checkbox mb0">
                <input value="<%=item.ip%>"  type="checkbox"  <%if(item.isExist){%>checked<%}%>/>
                <span><%= item.showText %></span>
            </label>
        </li>
        <% }); %>
    </ul></textarea>
<textarea id="text_consumer_dialog_ul" style="display:none"><ul class="unstyled dialog-ul">
    <% Y.Array.each(this.data, function(item,index){ %>
    <li class="">
        <label class="checkbox mb0">
            <input value="<%=item%>"  type="checkbox" checked/>
            <span><%= item %></span>
        </label>
    </li>
    <% }); %>
</ul></textarea>
<textarea id="text_provider_dialog_ul" style="display:none"><ul class="unstyled dialog-ul">
    <% Y.Array.each(this.data, function(item,index){ %>
    <li class="">
        <label class="checkbox mb0">
            <input value="<%=item.ip%>"  type="checkbox" <%if(item.isExist){%>checked<%}%>/>
            <span><%= item.ip %></span>
        </label>
    </li>
    <% }); %>
</ul></textarea>


<textarea id="text_idc_dialog_ul" style="display:none"><ul class="unstyled dialog-ul">
    <% Y.Array.each(this.data, function(item,index){ %>
    <li class="">
        <label class="checkbox mb0">
            <input value="<%=item.ipprefix%>"  data-idc="<%= item.idc%>" type="checkbox" <%if(item.isExist){%>checked<%}%>/>
            <span><%= item.idc%>:<%=item.ipprefix %></span>
        </label>
    </li>
    <% }); %>
</ul></textarea>

</div>

