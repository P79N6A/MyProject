<div id="hulkPolicy_content">
    <!-- 基于现有组件和样式,构建弹性v2.0页面 -->
    <div>
    <#include "/common/env.ftl" >
        <div id="hulkScalingGroup_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
        <#--<a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>-->
        </div>
        <a style="padding-left:2.0em"></a>
        <#--<button id="refreshHulkPolicy" type="button" class="btn btn-primary" title="刷新列表">-->
            <#--<i class="fa fa-refresh">刷新</i>-->
        <#--</button>-->
        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/59021307" target="_blank">
                HULK弹性伸缩操作手册<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
    <table><tr><td style="width:100%;height:14px;font-size:14px;border:0"></td></tr></table>
    <legend>策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span>
        <button id="addNewScalingPolicy" type="button" class="btn btn-primary" title="添加策略">监控策略</button>
            <button id="addNewPeriodicPolicy" type="button" class="btn" title="添加策略">周期策略</button>
                <button id="addModifySgGroup" type="button" class="btn" title="配置分组">分组</button>
        <input style="display: none" id="logined_user_name_" value="${_currentUser.name}"/>
    </legend>
    <div id="table_supplier_scalingpolicy" class="table-responsive" style="display:block;">
        <span id="scalingpolicy_add" style="color: #00aaee;float: right;font-size: x-small;"><label>+添加</label></span>
        <table class="table table-striped table-hover" id="hulkScalingPolicy_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 8%;text-align: center;">策略编号</th>
                <th style="width: 15%;text-align: center;">分组名称</th>
                <th style="width: 12%;text-align: center;">监控指标</th>
                <th style="width: 25%;text-align: center;">禁止缩容时间</th>
                <th style="width: 8%;text-align: center;">策略状态</th>
                <th style="width: 16%;text-align: center;">操作</th>
                <th style="width: 14%;text-align: center;">策略:启用/停用</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="13"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
            </tr>
            </tbody>
        </table>
    </div>

    <!--周期策略-->
    <#include "detail_hulkPeriodicPolicy.ftl" >

    <!--分组-->
    <#include "detail_hulkTagInfo.ftl" >

    <legend>策略记录<span style="font-size: 10px; margin-left: 10px; color: grey;"></span>
        <button id="btn_show_hulk_scale_records" class="btn btn-primary" title="弹性记录">弹性记录</button>
        <button id="btn_show_hulk_operation_records" class="btn" title="操作记录">操作记录</button>
    </legend>
    <div id="div_hulk_scale_records" class="form-inline mb20" style="padding-left: 0px;">
        <div class="control-group">
            <label> 开始时间：</label><input id="hulk_start_time" type="text" class="span3" placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label><input id="hulk_end_time" type="text" class="span3" placeholder="查询结束时间">
            <label class="ml20">操作类型：</label>
            <select id="hulk_operatorType" name="hulk_operatorType" title="操作类型">
                <option value="监控策略新增">监控策略新增</option>
            </select>
        </div>
        <div id="hulkpolicy_log_wrap">
            <div class="content-overlay">
                <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
            </div>
            <div class="content-body" style="display:none;">
                <table class="table table-striped table-hover">
                    <colgroup>
                        <col width="15%"></col>
                        <col width="15%"></col>
                        <col width="10%"></col>
                        <col width="20%"></col>
                        <col width="40%"></col>
                    </colgroup>
                    <thead>
                    <tr>
                        <th>时间</th>
                        <th>操作人</th>
                        <th>分组名称</th>
                        <th>操作描述</th>
                        <th>详细信息</th>
                    </tr>
                    </thead>
                    <tbody>
                    <td colspan="4" style="text-align: center;">Loading contents...</td>
                    </tbody>
                </table>
                <div id="paghulk_wrapper">
                </div>
            </div>
        </div>
    </div>
    <!--操作记录-->
    <#include "detail_hulkOperation.ftl" >
</div>
<#--<更新操作的弹出页面JJ>-->
<textarea id="text_updateScalingPolicy_dialog" style="display:none">
     <div id="updateScalingGroup_dialog" data-info="" class="form-horizontal">
         <legend>更新策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
         <div class="controls">
             <input type="hidden" value="" id="s_update_sp_id">
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_mtype">监控指标：</label>
             <div class="controls">
                 <% Y.Array.each(this.allIndexArray, function( item, index ){ %>
                 <input value="<%=item%>" type="checkbox" name="set_cell_switch" style="vertical-align: top" id="update_<%=item%>_radio" value="<%=item%>">
                 <span id="<%=item%>_name">&nbsp;<%=item%>&nbsp;&nbsp;</span>
                 <%});%>
                 <#--<input value="cpu" type="checkbox" name="set_cell_switch" style="vertical-align: top" id="update_cpu_radio" value="cpu" disabled>-->
                 <#--<span id="cpu_name">&nbsp;CPU&nbsp;&nbsp;</span>-->
                 <#--<input value="mem" type="checkbox" name="set_cell_switch" style="vertical-align: top" id="update_mem_radio" value="mem" disabled>-->
                 <#--<span id="mem_name">&nbsp;MEM&nbsp;&nbsp;</span>-->
                 <#--<input value="load" type="checkbox" name="set_cell_switch" style="vertical-align: top" id="update_load_radio" value="load" disabled>-->
                 <#--<span id="load_name">&nbsp;LOAD&nbsp;&nbsp;</span>-->
                 <#--<input value="mq" type="checkbox" name="set_cell_switch" style="vertical-align: top" id="update_mq_radio" value="mq" disabled>-->
                 <#--<span id="mq_name">&nbsp;MQ&nbsp;&nbsp;</span>-->
                 <#--<input value="db" type="checkbox" name="set_cell_switch" style="vertical-align: top" id="update_db_radio" value="db" disabled>-->
                 <#--<span id="db_name">&nbsp;DB&nbsp;&nbsp;</span>-->

                 <span id="add_monitor_policy_look_value" style="color: #00aaee;font-size: small">&nbsp;&nbsp;&nbsp;查看阈值</span>
             </div>
         </div>

         <#--更新策略配置中缩容时间段-->
         <div class="control-group" id="s_update_sp_noScaleInTime_content"><label class="control-label" for="s_update_sp_noScaleInTime">禁止缩容时间：</label>
             <%if(this.timeflag == "false"){%>
                 <div class="controls">
                     <input type="text" value="" class="stt_update_sp_noScaleInTime" id="stt_update_sp_noScaleInTime" onchange="">
                     &nbsp;&nbsp;-&nbsp;&nbsp;
                     <input type="text" value="" class="edt_update_sp_noScaleInTime" id="edt_update_sp_noScaleInTime" onchange="">
                     <input id="multi_noScaleInTime_update_btn" type="button" value="添加"/>
                     <span class="tips">例如11:00-14:00,间隔不能超过3</span>
                 </div>
             <%}else{%>
                 <% Y.Array.each(this.spData.noScaleinPeriods, function( item, index ){ %>
                     <%if(item.flag == "true"){%>
                     <div class="controls">
                         <input type="text" value="<%= item.startTime %>" class="stt_update_sp_noScaleInTime" id="stt_update_sp_noScaleInTime" onchange="">
                         &nbsp;&nbsp;-&nbsp;&nbsp;
                         <input type="text" value="<%= item.endTime %>" class="edt_update_sp_noScaleInTime" id="edt_update_sp_noScaleInTime" onchange="">
                         <input id="multi_noScaleInTime_update_btn" type="button" value="添加"/>
                         <span class="tips">例如11:00-14:00,间隔不能超过3</span>
                     </div>
                     <%}else{%>
                     <div class="controls" style="margin-top: 3px;">
                         <input type="text" value="<%= item.startTime %>" class="stt_update_sp_noScaleInTime" id="stt_update_sp_noScaleInTime" onchange="">
                         &nbsp;&nbsp;-&nbsp;&nbsp;
                         <input type="text" value="<%= item.endTime %>" class="edt_update_sp_noScaleInTime" id="edt_update_sp_noScaleInTime" onchange="">
                         <input id="delNoScaleInTime" class="delNoScaleInTime_" type="button" value="删除"/>
                     </div>
                     <%}%>
                 <%});%>

             <%}%>
         </div>
         <div class="control-group"><label class="control-label" for="s_jj_isConfigGroup">
             <input id="update_monitor_policy_groupshow_click" type="checkbox" style="vertical-align: top;margin-right: 5px;" checked="checked">配置分组：</label>
             <div id="update_monitor_policy_groupshow2" class="controls" style="display: block;">
                 <table border="1"  style="width: 90%;margin-top: 5px;" id="update_unified_policy_tags_table">
                     <thead>
                     <tr>
                         <th hidden>id</th>
                         <th style="width: 45%;text-align: center;">名称</th>
                         <th style="width: 10%;text-align: center;">区域</th>
                         <th style="width: 10%;text-align: center;">机房</th>
                         <%if(Y.JSON.stringify(this.isOnLineEnv) == "false"){%>
                         <th style="width: 10%;text-align: center;">泳道</th>
                         <%}else{%>
                         <th style="width: 10%;text-align: center;">SET标识</th>
                         <%}%>
                         <th style="width: 15%;text-align: center;">选择</th>
                     </tr>
                     </thead>
                     <tbody>

                         <tr>
                             <td colspan="3"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
                         </tr>
                     </tbody>
                 </table>
             </div>
        </div>
     </div>
</textarea>

<textarea id="text_userSelectedIndex_showDialog" style="display: none;">
    <div id="userSelectedIndex_showDialog">
        <table border="1"  style="width: 100%;margin-top: 5px;border-color: #3c485a;" id="index_data_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 20%;text-align: center;">监控指标</th>
                <th style="width: 70%;text-align: center;">监控指标项阈值</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="3"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
            </tr>
            </tbody>
        </table>
    </div>
</textarea>

<textarea id="text_addNewScalingPolicy_form" style="display:none">
    <div id="addNewScalingPolicy_form" class="form-horizontal">
        <legend>新建统一监控策略<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="control-group"><label class="control-label" for="s_sp_mtype">监控指标：</label>
            <div class="controls">
                <% Y.Array.each(this.allIndexArray, function( item, index ){ %>
                <input value="0" type="checkbox" name="set_ii_switch" style="vertical-align: top" id="<%=item%>_radio">
                <span id="<%=item%>_name">&nbsp;<%=item%>&nbsp;&nbsp;</span>
                <%});%>
                <#--<input value="0" type="checkbox" name="set_ii_switch" style="vertical-align: top" id="cpu_radio" disabled>-->
                <#--<span id="cpu_name">&nbsp;CPU&nbsp;&nbsp;</span>-->
                <#--<input value="0" type="checkbox" name="set_ii_switch" style="vertical-align: top" id="mem_radio" disabled>-->
                <#--<span id="mem_name">&nbsp;MEM&nbsp;&nbsp;</span>-->
                <#--<input value="0" type="checkbox" name="set_ii_switch" style="vertical-align: top" id="load_radio" disabled>-->
                <#--<span id="load_name">&nbsp;LOAD&nbsp;&nbsp;</span>-->
                <#--<input value="0" type="checkbox" name="set_ii_switch" style="vertical-align: top" id="mq_radio" disabled>-->
                <#--<span id="mq_name">&nbsp;MQ&nbsp;&nbsp;</span>-->
                <#--<input value="0" type="checkbox" name="set_ii_switch" style="vertical-align: top" id="db_radio" disabled>-->
                <#--<span id="db_name">&nbsp;DB&nbsp;&nbsp;</span>-->

            </div>
        </div>
        <#--增加禁止缩容字段-->
        <div class="control-group" id="s_sp_noScaleInTime_content"><label class="control-label" for="s_sp_noScaleInTime">禁止缩容时间：</label>
            <div class="controls" id="allNoScaleInTime_0">
                <input type="text" value="" class="s_sp_noScaleInTime_startTime" id="s_sp_noScaleInTime_startTime"
                       onchange="javascript:if(this.value.length>5){this.value='';}">&nbsp;&nbsp;-&nbsp;
                <input type="text" value="" class="s_sp_noScaleInTime_endTime" id="s_sp_noScaleInTime_endTime"
                       onchange="javascript:if(this.value.length>5){this.value='';}">
                <input id="multi_noScaleInTime_add_btn" type="button" value="添加"/>
                <span class="tips">例如11:00-14:00,间隔不能超过3</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sp_isConfigGroup">
            <input id="add_monitor_policy_groupshow_click" type="checkbox" style="vertical-align: top;margin-right: 5px;">配置分组：</label>
            <div id="add_monitor_policy_groupshow" class="controls" style="display: none;">
                <table border="1"  style="width: 90%;margin-top: 5px;border-color: #3c485a;" id="add_unified_policy_tags_table">
                    <thead>
                    <tr>
                        <th hidden>id</th>
                        <th style="width: 45%;text-align: center;">名称</th>
                        <th style="width: 10%;text-align: center;">区域</th>
                        <th style="width: 10%;text-align: center;">机房</th>
                        <% if(Y.JSON.stringify(this.isOnLineEnv) == "false"){ %>
                        <th style="width: 10%;text-align: center;">泳道</th>
                        <%}else{%>
                        <th style="width: 10%;text-align: center;">SET标识</th>
                        <%}%>
                        <th style="width: 15%;text-align: center;">选择</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td colspan="3"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</textarea>
<#--<添加用户所选择的监控指标的阈值页面>-->
<textarea id="text_userSelectedIndex_form" style="display:none">
    <div id="userSelectedIndex_form" class="form-horizontal">
        <table border="1"  style="width: 100%;margin-top: 5px;border-color: #3c485a;">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 10%;text-align: center;">监控指标</th>
                <th style="width: 60%;text-align: center;">监控指标阈值(最小值/最大值)</th>
            </tr>
            </thead>
            <tbody>
            <i style="font-size: 0.6em;color: red;margin-left: 5px;">CPU MEM的上下阈值为20% - 80%</i>
                <% Y.Array.each(this.data, function( item, index ){ %>
                <tr id="tr" data-info="<%= Y.JSON.stringify(item) %>">
                    <td style="width: 20%;text-align:center;" id="nothingIdc_<%=item.index%>"><%=item.index%></td>
                    <%if (item.index == "cpu" || item.index == "mem"){%>
                    <td style="width: 70%;text-align:center;">
                        <input name="<%=item.index%>_low" id="indexValue_low" style="width: 30%;text-align:center;" value="<%= item.low%>" onkeyup="cky(this)" onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<20 || this.value > 80){this.value=<%=item.low%>;}"/>%
                        <input name="<%=item.index%>_high" id="indexValue_high" style="width: 30%;text-align:center;" value="<%= item.high%>" onkeyup="cky(this)" onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<20 || this.value > 80){this.value=<%=item.high%>;}"/>%
                    </td>
                    <%}else{%>
                    <td style="width: 70%;text-align:center;">
                        <input name="<%=item.index%>_low" id="indexValue_low" style="width: 30%;text-align:center;" value="<%= item.low%>" onkeyup="cky(this)" onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0 || isNaN(this.value)){this.value=<%=item.low%>;}"/>&nbsp;&nbsp;&nbsp;
                        <input name="<%=item.index%>_high" id="indexValue_high" style="width: 30%;text-align:center;" value="<%= item.high%>" onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(isNaN(this.value)){this.value=<%=item.high%>;}"/>&nbsp;&nbsp;&nbsp;&nbsp;
                    </td>
                    <%}%>
                </tr>
                <% }); %>
            </tbody>
        </table>
        <div id="user_select_quota_show_div" style="width: 100%;margin-top: 15px;text-align: center">
            <button class="btn btn-default btn-primary" style="margin-right: 15px;" id="add_btn_index">确定</button>
            <button class="btn btn-default commonDialog-btn-unpass" style="margin-right: 15px;" id="cancle_btn_index">取消</button>
        </div>
    </div>

</textarea>
<#--<显示用户已配置的分组>-->
<textarea id="idc_info" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr id="tr" data-info="<%= Y.JSON.stringify(item) %>">
            <td id="hidden_id" hidden><input class="label" id="somethingIdc_<%=item.id%>" value="<%=item.idc%>"/></td>
            <td style="width: 15%;text-align:center;" id="nothingIdc_<%=item.id%>"><%=item.idc%></td>
            <td style="width: 70%;text-align:center;"><input id="inputMaxmumInstance_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.maximumInstance%>"/></td>
            <td id="td_update" style="width: 15%;text-align:center;"><button class="btn btn-primary" id="updateBtn_<%=item.id%>">更新</button><button class="btn btn-default" id="updateBtn_<%=item.id%>">更新</button></td>
        </tr>
        <% }); %>
</textarea>

<#--<这里进行监控策略的显示>-->
<textarea id="text_spandsp_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td hidden> <%= item.id %></td>
            <td style="text-align:center;"> <%= item.policy_id_fake %></td>
            <%if(item.tagNameArr == "null"){%>
            <td style="text-align: center;"><span style="color: red;">未绑分组,策略无效</span><br/></td>
            <%}else{%>
            <td style="text-align: center;"><% Y.Array.each(item.tagNameArr, function( tagName, tagIndex ){ %>  <span><%= tagName %></span><br/>  <% }); %></td>
            <%}%>
            <td style="text-align:center;"> <%= item.quota %></td>
            <td style="text-align:center;"> <%= item.noScaleInTimeAfter %></td>
            <td style="text-align:center;"> <%= item.state==1? "运行中":"停止" %></td>
            <td style="text-align:center;">
                <a id="updateScalingPolicy" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>编辑</span> </a>
                <a id="removeScalingPolicy" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-recycle"></i> <span>删除</span> </a>
            </td>
            <#--<td style="text-align:center;"><button id="delete_unipolicy_<%=item.id%>" data-enabled="1" class="btn btn-mini btn-alive " disabled>删除</button></td>-->
            <td style="text-align:center;">
                <div id="hulk-policy-one-enabled" class="btn-group btn-enabled">
                    <#--<%= item.policyRow.state==0?" active":"" %>-->
                    <button id="open_<%=item.id%>" data-enabled="1" class="btn btn-mini btn-alive <%= item.state==1?" active":"" %>">启用</button>
                    <button id="close_<%=item.id%>" data-enabled="0" class="btn btn-mini btn-dead <%= item.state==0?" active":"" %>">停用</button>
                </div>

            </td>
        </tr>
        <% }); %>
</textarea>

<textarea id="text_update_unified_policy_tag" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr id="tr" data-info="<%= Y.JSON.stringify(item)%>">
            <td style="width: 45%;text-align:center;"><%=item.tagName%></td>
            <td style="width: 10%;text-align:center;"><%=item.regionName%></td>
            <td style="width: 10%;text-align:center;"><%=item.idcName%></td>
            <%if(item.isOnLineEnv == "false"){%>
            <td style="width: 10%;text-align:center;"><%=item.swimlane%></td>
            <%}else{%>
            <td style="width: 10%;text-align:center;"><%=item.cell%></td>
            <%}%>
            <td id="up_td_update_<%=item.id%>" name="up_td_update" style="width: 15%;text-align:center;">
                <input class="one_click_for_group_" id="one_click_for_group_<%=item.id%>" <%= item.binded==true? "disabled":"" %> type="checkbox" data-info="<%= item.id %>" name="unifiedPolicyTags" <%= item.related==true? "checked":"" %>/>
            </td>
        </tr>
        <% }); %>
</textarea>