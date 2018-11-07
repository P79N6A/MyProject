<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<div id ="cut_flow_loading" class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div id ="cut_flow_content" class="content-body" style="display:none;">
    <div class="form-inline mb20">
        <button id="refreshThriftCutFlow" type="button" class="btn btn-primary" title="刷新列表">
            <i class="fa fa-refresh"></i>
        </button>
        <button id="addThriftCutFlow" type="button" class="btn btn-primary" title="新增截流配置">
            <i class="fa fa-plus">新增策略截流</i>
        </button>
        <button id="addCutFlow" type="button" class="btn btn-primary" title="新增截流配置">
            <i class="fa fa-plus">新增一键截流</i>
        </button>
        <#include "../common/env.ftl" >
        <div id="thriftCutFlow_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <#if isOffline>
                <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
            </#if>
        </div>

        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28354805" target="_blank">
                Thrift截流使用说明书<i class="fa fa-question-circle"></i>
            </a>
            <a href="https://123.sankuai.com/km/page/15975100" target="_blank">
                一键截流使用说明书<i class="fa fa-question-circle"></i>
            </a>
        </div>

    </div>
    <div class="form-inline mb20">
        <p><strong>注意：</strong>策略截流为原来的截流方式，根据配置和实时QPS动态生成截流比例；一键截流为新增的截流方式，直接配置服务不同接口的截流比例</p>
        <p></p>
        <strong>mtthrift最新提供基于rhino的限流，如果使用rhino限流,保证服务提供方的mtthrift版本 >= 1.8.5.4</strong>
        <#if isOffline>
            <a id="rhino-url" href="http://rhino.inf.dev.sankuai.com/#/limit/${appkey!''}"
               title="如果要使用rhino限流，保证服务端的mtthrift版本 >= 1.8.5.4！！！">
                &nbsp;&nbsp;Rhino限流平台
                <i class="fa fa-question-circle"></i>
            </a>
        <#else>
            <a id="rhino-url" href="https://rhino.sankuai.com/#/limit/${appkey!''}"
               title="如果要使用rhino限流，保证服务端的mtthrift版本 >= 1.8.5.4！！！">
                &nbsp;&nbsp;Rhino限流平台
                <i class="fa fa-question-circle"></i>
            </a>
        </#if>
        <a href="https://123.sankuai.com/km/page/52109517" target="_blank">
            mtthift统一限流(rhino)使用手册<i class="fa fa-question-circle"></i>
        </a>
        <a href="https://123.sankuai.com/km/page/41184331" target="_blank">
            截流配置迁移指南(Octo->Rhino)<i class="fa fa-question-circle"></i>
        </a>
    </div>
    <div id="table_supplier" class="table-responsive">
        <h3 style="text-align:center">策略截流</h3>
        <table class="table table-striped table-hover" id="thriftCutFlow_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 15%">接口</th>
                <th style="width: 15%">容量(单机:集群)</th>
                <th style="width: 36%">策略详情(消费者:集群配额:降级策略:截流比例)</th>
                <th style="width: 10%">测试模式</th>
                <th style="width: 10%">是否启用</th>
                <th style="width: 14%">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="10">Loading contents...</td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="form-inline mb20">


    </div>
    <div class="form-inline mb20">
    </div>
    <div id="table_supplier" class="table-responsive">
        <h3 style="text-align:center">一键截流</h3>
        <table class="table table-striped table-hover" id="cutFlow_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 15%">接口</th>
                <!--<th style="width: 15%">截流比例</th>-->
                <th style="width: 46%">消费者:截流比例</th>
                <th style="width: 10%">是否启用</th>
                <th style="width: 14%">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="10">Loading contents...</td>
            </tr>
            </tbody>
        </table>
    </div>

    <div id="thrift_cutflow_tips">
        <b>注意事项</b>
        <p>1.如果配置了<strong>all</strong>接口,则不能再配置其他接口</p>
        <p>2.针对某个接口，<strong>一键截流和策略截流不能同时配置</strong></p>
        <br/>
        <b>策略截流</b>
        <p>1.测试模式下启用后，会根据QPS和容量自动生成截流策略，但截流策略不会在服务端生效;</p>
        <p>2.非测试模式下启用后，不仅会根据QPS和容量自动生成截流策略，并且截流策略会在服务端生效;</p>
        <p>3.使用过程中有问题或对配置存在疑惑, 请联系高升(gaosheng)。</p>
        <br/>
        <b>一键截流</b>
        <p>1.截流比例设置范围为0-100，如果将截流比例设置为100，即完全截流功能</p>
        <!--<p>2.针对某个接口，如果没有配置任何消费者(调用端key)，会自动生成key为<strong>others</strong>的选项，截流比例等同于方法设置的截流比例</p>
        <p>3.针对某个接口，如果配置了消费者(调用端key)，调用端key的截流比例优先级<strong>高于方法</strong>的截流比例</p>-->


    </div>
</div>

<textarea id="text_consumer_dialog" style="display:none">
     <div id="consumer_dialog" class="form-horizontal">
         <div class="controls">
             <input type="hidden" value="<%= this.parseData.id %>" id="s_id" >
         </div>
         <div class="control-group"><label class="control-label">接口：</label>
             <div class="controls">
                 <input type="text" readonly="readonly" id="s_method" value="<%= this.parseData.method %>">
             </div>
         </div>
         <div class="control-group"><label class="control-label">单机QPS容量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.parseData.hostQpsCapacity %>" onkeyup="cky(this)" id="s_host_qps_capacity" >
             </div>
         </div>
         <div class="control-group"><label class="control-label">集群QPS容量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.parseData.clusterQpsCapacity %>" onkeyup="cky(this)" id="s_cluster_qps_capacity" >
             </div>
         </div>
         <div class="controls">
             <input type="hidden" value="<%= this.parseData.testStatus %>" id="s_test_status" >
         </div>
         <div class="controls">
             <input type="hidden" value="<%= this.parseData.degradeStatus %>" id="s_degrade_status" >
         </div>
         <table id="consumer_table" class="table table-striped table-hover">
             <thead id="<%= this.quotaId %>">
             <th>客户端appkey</th>
             <th>集群配额</th>
             <th>降级策略</th>
             <th>删除</th>
             </thead>
             <tbody>
             <% Y.Array.each(this.data, function( item, index ){ %>
                 <tr id="consumer_table_tr_<%= index %>" data-info="<%= Y.JSON.stringify(item) %>">
                 <td id="appkey" value="<%= item.consumerAppkey %>"><%= item.consumerAppkey %></td>
                 <td>
                     <input id="cluster_quota" type="text" value="<%= item.clusterQuota %>" onkeyup="cky(this)"
                            onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000000){this.value=0;}"
                            placeholder="0-100000000之间整数，默认为0">
                 </td>
                 <td id="strategy" value = "<%=item.strategy %>"><% if(item.strategy == 0){ %> Drop <% } else{ %> Customize <% } %></td>
                 <td><input id="delConsumer" type="button" class="btn btn-mini btn-danger" value="-"></td>
             </tr>
             <% }); %>
             </tbody>
         </table>
         <button id="addConsumerRatio" class="btn btn-success J-config-panel-add">添加</button>
     </div>
</textarea>

<textarea id="text_add_providerCutFlow_form" style="display:none">
    <div id="add_providerCutFlow_form" class="form-horizontal">
        <div class="control-group"><label class="control-label" for="s_method">接口：</label>
            <div class="controls">
                <input type="text" id="s_method" />
                <span class="tips" >对Provider的某一方法进行配置</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_host_qps_capacity">单机QPS容量：</label>
            <div class="controls">
                <input type="text" value="0" onkeyup="cky(this)" id="s_host_qps_capacity" >
                <span class="tips" >集群单机容量上限，保证单台Server不被打垮</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_cluster_qps_capacity">集群QPS容量：</label>
            <div class="controls">
                <input type="text" value="0" onkeyup="cky(this)" id="s_cluster_qps_capacity" >
                <span class="tips" >集群整体容量上限，水平扩展也不能支持更大的QPS，防止集群水平扩缩容时影响下游服务或者DB</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_test_status">测试模式：</label>
            <div class="controls">
                <select id="s_test_status">
                    <option value="0" selected="selected" >是</option>
                    <option value="1">否</option>
                </select>
                <span class="tips" >测试模式时只生成截流策略，并不在Server端生效；非测试模式时会自动截流</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_degrade_status">是否启用：</label>
            <div class="controls">
                <select id="s_degrade_status">
                    <option value="0">启用</option>
                    <option value="1" selected="selected">停用</option>
                </select>
                <span class="tips" >启动时会自动生成截流策略并生效；停用时会删除已生成策略并不再生成截流策略</span>
            </div>
        </div>
    </div>
        <div id="consumer_dialog" class="table-responsive">
            <br>
            <p><b>集群配额：</b>客户端appkey访问该接口的集群流量上限，超出该限额就会被截流</p>
            <table id="consumer_table_add" class="table table-striped table-hover">
                <thead>
                <th>客户端appkey</th>
                <th>集群配额</th>
                <th>降级策略</th>
                <th>删除</th>
                </thead>
                <tbody>
                </tbody>
            </table>
            <button id="addConsumerRatio" class="btn btn-success J-config-panel-add">添加</button>
        </div>
</textarea>
<textarea id="text_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td hidden><%= item.id %></td>
            <td><%= item.method %></td>
            <td><%= item.hostQpsCapacity %> : <%= item.clusterQpsCapacity %></td>
            <td>
                <table>
                    <% Y.Array.each(item.consumers, function(consumer, index){%>
                    <tr><td style="border: none;"><% var str =consumer.config;var s0 = str.split(";")[0];var s1 = str.split(";")[1];var s2 = str.split(";")[2]; var s3 = str.split(";")[3] %>
                        <% var qpsRatio = consumer.qpsRatio; %>
                        <%= s0 + " : " + s2 + " : " + ((0 == Number(s3)) ? "Drop" : "Customise") + " : " + qpsRatio%>
                        <% var link = consumer.cutFlowLink; if(link != null) {%>
                            <a href="<%= consumer.cutFlowLink %>" target="_blank">数据详情</a>
                        <%}%>
                    </td></tr><% });%>
                </table>
            </td>
            <td>
                <div id="thrift-test-enabled" class="btn-group btn-enabled">
                    <button data-enabled="0" class="btn btn-mini btn-alive <%= (item.testStatus==0 || !item.testStatus)?" active":"" %>">是</button>
                    <button data-enabled="1" class="btn btn-mini btn-dead <%= item.testStatus==1?" active":"" %>">否</button>
                </div>
            </td>
            <td>
                <div id="thrift-one-enabled" class="btn-group btn-enabled">
                    <button data-enabled="0" class="btn btn-mini btn-alive <%= (item.degradeStatus==0 || !item.degradeStatus)?" active":"" %>">启用</button>
                    <button data-enabled="1" class="btn btn-mini btn-dead <%= item.degradeStatus==1?" active":"" %>">停用</button>
                </div>
            </td>
            <td>
                <a id="setConsumerRatio" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>编辑</span> </a>
                <a id="delProviderCutFlow" href="javascript:void(0);" class="config-panel-delete"> &nbsp;&nbsp;<i
                        class="fa fa-trash-o"></i> <span>删除</span> </a>
            </td>
        </tr>
        <% }); %>
</textarea>
<textarea id="text_consumer_table" style="display:none">
    <td>
        <select id="appkey">
            <% Y.Array.each(this.data, function( item, index ){ %>
            <option value="<%= item %>"> <%= item %></option>
            <% }); %>
        </select>
    </td>
    <td><input id="cluster_quota" type="text" value="0" onkeyup="cky(this)"
               onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000000){this.value=0;}"
               placeholder="0-100000000之间整数，默认为0"> </td>
    <td>
        <select id="strategy"><option value="0">Drop</option><option value="1">Customize</option></select></td>
    <td><input id="delConsumer" type="button" class="btn btn-mini btn-danger" value="-"></td>
</textarea>
<textarea id="text-http-cut-flow-table-tbody" style="display:none">
<% if(this.data.length == 0){ %>
    <tr>
        <td colspan="5" style="text-align: center;">无截流数据，请添加</td>
    </tr>
<% } else{ %>
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-info="<%= Y.JSON.stringify(item) %>">
        <td style="width: 25%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.methodUrl %></td>
        <td style="width: 25%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.serverName %></td>
        <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.closurePercent %></td>
        <!--td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.closureStatus %></td-->
        <td>
            <div id="http-one-enabled" class="btn-group btn-enabled">
                <button data-enabled="0"
                        class="btn btn-mini btn-alive <%= (item.closureStatus==0 || !item.closureStatus)?" active":"" %>">启用</button>
                <button data-enabled="1" class="btn btn-mini btn-dead <%= item.closureStatus==1?"
                        active":"" %>">停用</button>
            </div>
        </td>
        <!--td style="width: 10%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= 1 %></td-->
        <td>
            <a id="editHttpCutFlow" href="javascript:void(0);" class="config-panel-edit editHttpCutFlow"> &nbsp;&nbsp;<i
                    class="fa fa-edit"></i> <span>编辑</span> </a>
            <a id="delHttpCutFlow" href="javascript:void(0);" class="config-panel-delete delHttpCutFlow"> &nbsp;&nbsp;<i
                    class="fa fa-trash-o"></i> <span>删除</span> </a>
        </td>
    </tr>
<% }); %>
<% } %>
</textarea>

<textarea id="update_client_textarea" style="display:none">
     <div id="update_client_dialog" class="form-horizontal">
         <div class="controls">
             <input type="hidden" value="<%= this.parseData.id %>" id="os_id">
         </div>
         <div class="control-group"><label class="control-label">接口：</label>
             <div class="controls">
                 <input type="text" readonly="readonly" id="os_method" value="<%= this.parseData.method %>">
             </div>
         </div>
         <div class="control-group" style="display:none"><label class="control-label">截流比例(0 - 100)：</label>
             <div class="controls">
                 <input type="hidden" value="0" id="os_ratio"
                        onblur="rangeCheck(this)">
             </div>
         </div>
         <div class="controls">
             <input type="hidden" value="<%= this.parseData.degradeStatus %>" id="os_degrade_status">
         </div>
         <table id="update_client_table" class="table table-striped table-hover">
             <thead id="<%= this.quotaId %>">
             <th>客户端appkey</th>
             <th>截流比例</th>
             <th>删除</th>
             </thead>
             <tbody>
             <% Y.Array.each(this.data, function( item, index ){ %>
             <% if("others" != item.consumerAppkey){%>
                 <tr id="client_table_tr_<%= index %>" data-info="<%= Y.JSON.stringify(item) %>">
                 <td id="appkey" value="<%= item.consumerAppkey %>"><%= item.consumerAppkey %></td>
                 <td>
                     <input id="host_quota" type="text" value="<%= item.qpsRatio * 100 %>" onblur="rangeCheck(this)"
                            placeholder="0-100之间整数，默认为100">
                 </td>
                 <td><input id="delete_client" type="button" class="btn btn-mini btn-danger" value="-"></td>
                 </tr>
             <% }else{%>
                 <tr hidden id="client_table_tr_<%= index %>" data-info="<%= Y.JSON.stringify(item) %>">
                     <td hidden id="appkey" value="<%= item.consumerAppkey %>"><%= item.consumerAppkey %></td>
                     <td hidden>
                        <input id="host_quota" type="text" value="<%= item.qpsRatio * 100 %>" onblur="rangeCheck(this)"
                               placeholder="1-100之间整数，默认为100">
                     </td>
                 </tr>
             <% }}); %>
             </tbody>
         </table>
         <button id="update_client_ratio" class="btn btn-success J-config-panel-add">添加</button>
     </div>
</textarea>
<textarea id="add_cutFlow_form" style="display:none">
    <div id="cutFlow_form" class="form-horizontal">
        <div class="control-group"><label class="control-label" for="os_method">接口：</label>
            <div class="controls">
                <input type="text" id="os_method"/>
                <span class="tips">选择方法名</span>
            </div>
        </div>
        <div class="control-group" style="display:none"><label class="control-label" for="os_ratio">截流比例：</label>
            <div class="controls">
                <input type="hidden" value="0" id="os_ratio"
                       onblur="rangeCheck(this)">
                <span class="tips">截流比例的设置范围为0-100</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="os_open">是否启用：</label>
            <div class="controls">
                <select id="os_open">
                    <option value="0">启用</option>
                    <option value="1" selected="selected">停用</option>
                </select>
            </div>
        </div>
    </div>
    <div id="one_consumer_dialog" class="table-responsive">
        <br>
        <!--<p><b>截流比例：</b>客户端appkey对应的截流比例优先级高于接口设置的截流比例</p>-->
        <table id="add_client_table" class="table table-striped table-hover">
            <thead>
                <th style="width: 40%">客户端appkey</th>
                <th style="width: 30%">截流比例</th>
                <th style="width: 30%">删除</th>
            </thead>
            <tbody>
            </tbody>
        </table>
        <button id="update_client_ratio" class="btn btn-success J-config-panel-add">添加</button>
    </div>
</textarea>
<textarea id="template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td hidden><%= item.id %></td>
            <td><%= item.method %></td>
            <td hidden><%= item.hostQpsCapacity %></td>
            <td>
                <table>
                    <% Y.Array.each(item.consumers, function(consumer, index){%>
                    <% if("others" != consumer.consumerAppkey){ %>
                    <tr><td style="border: none;">
                        <%=  consumer.consumerAppkey + " : " + consumer.qpsRatio * 100 %>
                    </td></tr><% }});%>
                </table>
            </td>
            <td>
                <div id="open-enabled" class="btn-group btn-enabled">
                   <div id="thrift-one-enabled" class="btn-group btn-enabled">
                       <button data-enabled="0" class="btn btn-mini btn-alive <%= (item.degradeStatus==0 || !item.degradeStatus)?" active":"" %>">启用</button>
                       <button data-enabled="1" class="btn btn-mini btn-dead <%= item.degradeStatus==1?" active":"" %>">停用</button>
                   </div>
                </div>
            </td>
            <td>
                <a id="setClientRatio" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>编辑</span> </a>
                <a id="deleteCutFlow" href="javascript:void(0);" class="config-panel-delete"> &nbsp;&nbsp;<i
                        class="fa fa-trash-o"></i> <span>删除</span> </a>
            </td>
        </tr>
        <% }); %>
</textarea>
<textarea id="client_table" style="display:none">
    <td>
        <select id="appkey">
            <% Y.Array.each(this.data, function( item, index ){ %>
            <option value="<%= item %>"> <%= item %></option>
            <% }); %>
        </select>
    </td>
    <td><input id="host_quota" type="text" value="100" placeholder="0-100之间整数，默认为0" onblur="rangeCheck(this)"> </td>
    <td><input id="delete_client" type="button" class="btn btn-mini btn-danger" value="-"></td>
</textarea>

<script type="text/javascript">
    function cky(obj) {
        var t = obj.value.replace(/[^\d]/g, '');
        if (obj.value != t) obj.value = t;
        if (obj.value < 0) obj.value = 0;
        if (obj.value > 100000000) obj.value = 0;
    }

    function rangeCheck(obj) {
        obj.value = obj.value.replace(/\D/g, '');
        if (obj.value > 100)
            obj.value = 100;
        if (obj.value < 0)
            obj.value = 0;
    }
</script>