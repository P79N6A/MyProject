<input id="cur_update_periodic_policy" type="hidden" value=""/>

<div id="table_supplier_periodpolicy" class="table-responsive" style="display:none;">
    <span id="add_periodic_policy" style="color: #00aaee;float: right;"><label>+添加</label></span>
    <table class="table table-striped table-hover" id="periodic_policy_table">
        <thead>
        <tr>
            <th style="width: 8%;text-align: center;">策略编号</th>
            <th style="width: 10%;text-align: center;">分组</th>
            <th style="width: 10%;text-align: center;">日常策略</th>
            <th style="width: 10%;text-align: center;">活动策略</th>
            <th style="width: 7%;text-align: center;">状态</th>
            <th style="width: 8%;text-align: center;">操作</th>
            <th style="width: 8%;text-align: center;">策略状态</th>
        </tr>
        </thead>
        <tbody id="periodic_policy_table_tbody">

        </tbody>
    </table>
</div>

<textarea id="text_periodic_policy_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td style="text-align: center;"><%= index+1 %></td>
            <%if(item.tags == ""){%>
            <td style="text-align: center;"><span style="color: red;">未绑分组,策略无效</span><br/></td>
            <%}else{%>
            <td style="text-align: center;"><% Y.Array.each(item.tagNameArr, function( tagName, tagIndex ){ %>  <span><%= tagName %></span><br/>  <% }); %></td>
            <%}%>
            <td style="text-align: center;">
                <a id="updatePoliciesInWeek_<%= item.index %>" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>日常策略</span> </a>
            </td>
            <td style="text-align: center;">
                <a id="updatePoliciesBtwDatetime_<%= item.index %>" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>活动策略</span> </a>
            </td>
            <td style="text-align: center;"> <%= item.state==1? "运行中":"停止" %></td>
            <td style="text-align: center;">
                <a id="updatePeriodicPolicyTags_<%= item.index %>" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>配置分组</span> </a>
                <a id="deletePeriodicPolicy_<%= item.index %>" href="javascript:void(0);" title="删除操作不可恢复，请谨慎！" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-recycle"></i> <span>删除</span> </a>
            </td>
            <td style="text-align: center;">
                <div class="btn-group btn-enabled">
                    <button id="updatePeriodicPolicyState_<%= item.index %>" data-enabled="1" class="btn btn-mini btn-dead <%= item.state==1?" active":"" %>">启用</button>
                    <button id="updatePeriodicPolicyState_<%= item.index %>" data-enabled="0" class="btn btn-mini btn-alive <%= item.state==0?" active":"" %>">停用</button>
                </div>
            </td>
        </tr>
    <% }); %>
</textarea>

<textarea id="text_periodic_policy_in_week_template" style="display:none">
    <div class="table-responsive">
        <span  style="color: #00aaee;float: left;">日常策略中时间格式HH:mm</span>
          <span id="add_period_policy_in_week" style="color: #00aaee;float: right;"><label>+添加</label></span>
    </div>
    <table class="table table-striped table-hover" id="periodic_policy_in_week_table">
            <thead>
            <tr>
                <th style="width: 8%;text-align: center;">日常策略</th>
                <th style="width: 10%;text-align: center;">开始日期</th>
                <th style="width: 10%;text-align: center;">结束日期</th>
                <th style="width: 20%;text-align: center;">开始时间</th>
                <th style="width: 20%;text-align: center;">结束时间</th>
                <th style="width: 8%;text-align: center;">扩容步长</th>
                <th style="width: 20%;text-align: center;">操作</th>
            </tr>
            </thead>
            <tbody id="periodic_policy_in_week_table_tbody">
                <% Y.Array.each(this.data, function( item, index ){ %>
                    <tr data-info="<%= Y.JSON.stringify(item) %>">
                        <td style="text-align: center;">日常策略</td>
                        <td style="text-align: center;">
                            <select disabled style="width: 80%;background-color : #d1d1d1;">
                                <option value="0" <%= item.startDay==0? "selected":"" %>>周一</option>
                                <option value="1" <%= item.startDay==1? "selected":"" %>>周二</option>
                                <option value="2" <%= item.startDay==2? "selected":"" %>>周三</option>
                                <option value="3" <%= item.startDay==3? "selected":"" %>>周四</option>
                                <option value="4" <%= item.startDay==4? "selected":"" %>>周五</option>
                                <option value="5" <%= item.startDay==5? "selected":"" %>>周六</option>
                                <option value="6" <%= item.startDay==6? "selected":"" %>>周日</option>
                            </select>
                            <#--<input disabled style="width: 50%;" type="text" value="<%= item.startDay %>" onkeyup="checkInWeekDay(this)"/>-->
                        </td>
                        <td style="text-align: center;">
                             <select disabled style="width: 80%;background-color : #d1d1d1;">
                                 <option value="0" <%= item.endDay==0? "selected":"" %>>周一</option>
                                 <option value="1" <%= item.endDay==1? "selected":"" %>>周二</option>
                                 <option value="2" <%= item.endDay==2? "selected":"" %>>周三</option>
                                 <option value="3" <%= item.endDay==3? "selected":"" %>>周四</option>
                                 <option value="4" <%= item.endDay==4? "selected":"" %>>周五</option>
                                 <option value="5" <%= item.endDay==5? "selected":"" %>>周六</option>
                                 <option value="6" <%= item.endDay==6? "selected":"" %>>周日</option>
                            </select>
                            <#--<input disabled style="width: 50%;background-color : #d1d1d1;" type="text" value="<%= item.endDay %>" onkeyup="checkInWeekDay(this)"/>-->
                        </td>
                        <td style="text-align: center;"><input disabled style="width: 80%;background-color : #d1d1d1;" type="text" value="<%= item.startTime %>"/></td>
                        <td style="text-align: center;"><input disabled style="width: 80%;background-color : #d1d1d1;" type="text" value="<%= item.endTime %>"/></td>
                        <td style="text-align: center;"><input disabled style="width: 50%;background-color : #d1d1d1;" type="text" value="<%= item.scaleStep %>" onkeyup="checkScaleStep(this)"/></td>
                        <td style="text-align: center;">
                            <div class="btn-group btn-enabled">
                                <button data-action="editPolicyInWeek" class="btn btn-mini btn-alive">编辑</button>&nbsp;
                                <button data-action="removePolicyInWeek" class="btn btn-mini btn-alive">删除</button>
                            </div>
                        </td>
                    </tr>
                <% }); %>
            </tbody>
    </table>
</textarea>

<textarea id="text_periodic_policy_in_week_add_one_template" style="display: none">
    <tr>
        <td style="text-align: center;">日常策略</td>
        <td style="text-align: center;">
            <select style="width: 80%">
                <option value="0" selected>周一</option>
                <option value="1">周二</option>
                <option value="2">周三</option>
                <option value="3">周四</option>
                <option value="4">周五</option>
                <option value="5">周六</option>
                <option value="6">周日</option>
            </select>
        </td>
        <td style="text-align: center;">
            <select style="width: 80%">
                <option value="0" selected>周一</option>
                <option value="1">周二</option>
                <option value="2">周三</option>
                <option value="3">周四</option>
                <option value="4">周五</option>
                <option value="5">周六</option>
                <option value="6">周日</option>
            </select>
        </td>
        <td style="text-align: center;"><input style="width: 80%" type="text" value=""/></td>
        <td style="text-align: center;"><input style="width: 80%" type="text" value=""/></td>
        <td style="text-align: center;"><input style="width: 50%" type="text" value="" onkeyup="checkScaleStep(this)"/></td>
        <td style="text-align: center;">
            <div class="btn-group btn-enabled">
                <button data-action="editPolicyInWeek" class="btn btn-mini btn-alive">编辑</button>&nbsp;
                <button data-action="removePolicyInWeek" class="btn btn-mini btn-alive">删除</button>
            </div>
        </td>
    </tr>
</textarea>

<textarea id="text_periodic_policy_btw_datetime_template" style="display:none">
        <div class="table-responsive" data-info="">
            <span  style="color: #00aaee;float: left;">活动策略中日期格式MM-dd HH:mm</span>
          <span id="add_period_policy_btw_datetime" style="color: #00aaee;float: right;"><label>+添加</label></span>
        </div>
        <table class="table table-striped table-hover" id="periodic_policy_btw_datetime_table">
            <thead>
            <tr>
                <th style="width: 8%;text-align: center;">活动策略</th>
                <th style="width: 20%;text-align: center;">开始日期</th>
                <th style="width: 20%;text-align: center;">结束日期</th>
                <th style="width: 8%;text-align: center;">扩容步长</th>
                <th style="width: 20%;text-align: center;">操作</th>
            </tr>
            </thead>
            <tbody id="periodic_policy_btw_datetime_table_tbody">
                <% Y.Array.each(this.data, function( item, index ){ %>
                <tr data-info="<%= Y.JSON.stringify(item) %>">
                    <td hidden> <%= item.id %></td>
                    <td style="text-align: center;">活动策略</td>
                    <td style="text-align: center;"><input id="policy_btw_datetime_start_datetime" disabled style="background-color : #d1d1d1;" type="text" value="<%= item.startDatetime %>"/></td>
                    <td style="text-align: center;"><input id="policy_btw_datetime_end_datetime" disabled style="background-color : #d1d1d1;" type="text" value="<%= item.endDatetime %>"/></td>
                    <td style="text-align: center;"><input disabled type="text" style="background-color : #d1d1d1;" value="<%= item.scaleStep %>" onkeyup="checkScaleStep(this)"/></td>
                    <td style="text-align: center;">
                        <div class="btn-group btn-enabled">
                             <button data-action="editPolicyBtwDatetime" class="btn btn-mini btn-alive">编辑</button>
                             <button data-action="removePolicyBtwDatetime" class="btn btn-mini btn-alive">删除</button>
                        </div>
                    </td>
                </tr>
                <% }); %>
            </tbody>
        </table>
</textarea>

<textarea id="text_periodic_policy_btw_datetime_add_one_template" style="display: none">
    <tr>
        <td style="text-align: center;">活动策略</td>
        <td style="text-align: center;"><input id="policy_btw_datetime_start_datetime" type="text" value=""/></td>
        <td style="text-align: center;"><input id="policy_btw_datetime_end_datetime" type="text" value=""/></td>
        <td style="text-align: center;"><input type="text" value="" onkeyup="checkScaleStep(this)"/></td>
        <td style="text-align: center;">
            <div class="btn-group btn-enabled">
                <button data-action="editPolicyBtwDatetime" class="btn btn-mini btn-alive">编辑</button>&nbsp;
                <button data-action="removePolicyBtwDatetime" class="btn btn-mini btn-alive">删除</button>
            </div>
        </td>
    </tr>
</textarea>

<textarea id="text_periodic_policy_tags_template" style="display: none">
    <div class="table-responsive" data-info="">
            <span  style="color: #00aaee;float: left;">如果某分组已被其他周期策略绑定，则不能绑定该分组</span>
    </div>
    <div class="control-group">
            <table class="table table-striped table-hover" id="update_periodic_policy_tags_table">
                  <thead>
                        <tr>
                            <th style="width: 10%;text-align: center;">名称</th>
                            <th style="width: 10%;text-align: center;">区域</th>
                            <th style="width: 20%;text-align: center;">机房</th>
                            <%if(Y.JSON.stringify(isOnline) == "true"){%>
                            <th style="width: 20%;text-align: center;">SET标识</th>
                            <%}else{%>
                            <th style="width: 8%;text-align: center;">泳道</th>
                            <%}%>
                            <th style="width: 20%;text-align: center;">绑定/解绑</th>
                        </tr>
                  </thead>
                  <tbody id="update_periodic_policy_tags_table_tbody">
                     <% Y.Array.each(this.data, function( item, index ){ %>
                        <tr data-info="<%= Y.JSON.stringify(item) %>">
                            <td style="text-align: center;"><%= item.tagName %></td>
                            <td style="text-align: center;"><%= item.region %></td>
                            <td style="text-align: center;"><%= item.idc %></td>
                            <%if(item.isOnline == "true"){%>
                            <td style="text-align: center;"><%= item.cell %></td>
                            <%}else{%>
                            <td style="text-align: center;"><%= item.swimlane %></td>
                            <%}%>
                            <td style="text-align: center;">
                               <input class="tag_periodic_in_" <%= item.binded==true? "disabled":"" %> type="checkbox" data-info="<%= item.id %>" name="periodicPolicyTags" <%= item.related==true? "checked":"" %>/>
                            </td>
                        </tr>
                    <% }); %>
                  </tbody>
             </table>
        </div>
</textarea>

<!--新增-->
<textarea id="text_add_periodic_policy_form" style="display:none">
    <div id="add_periodic_policy_form" class="form-horizontal">
        <legend>新建日常策略<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="table-responsive">
            <span  style="color: #00aaee;float: left;">日常策略中时间格式HH:mm</span>
            <span id="new_add_period_policy_in_week" style="color: #00aaee;float: right;"><label>+添加</label></span>
        </div>
        <table class="table table-striped table-hover" id="periodic_policy_in_week_table">
                <thead>
                <tr>
                    <th style="width: 8%;text-align: center;">日常策略</th>
                    <th style="width: 10%;text-align: center;">开始日期</th>
                    <th style="width: 10%;text-align: center;">结束日期</th>
                    <th style="width: 20%;text-align: center;">开始时间</th>
                    <th style="width: 20%;text-align: center;">结束时间</th>
                    <th style="width: 8%;text-align: center;">扩容步长</th>
                    <th style="width: 20%;text-align: center;">操作</th>
                </tr>
                </thead>
                <tbody id="new_periodic_policy_in_week_table_tbody">

                </tbody>
        </table>
        <legend>新建活动策略<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="table-responsive" data-info="">
            <span  style="color: #00aaee;float: left;">活动策略中日期格式MM-dd HH:mm</span>
          <span id="new_add_period_policy_btw_datetime" style="color: #00aaee;float: right;"><label>+添加</label></span>
        </div>
        <table class="table table-striped table-hover" id="periodic_policy_btw_datetime_table">
            <thead>
            <tr>
                <th style="width: 8%;text-align: center;">活动策略</th>
                <th style="width: 20%;text-align: center;">开始日期</th>
                <th style="width: 20%;text-align: center;">结束日期</th>
                <th style="width: 8%;text-align: center;">扩容步长</th>
                <th style="width: 20%;text-align: center;">操作</th>
            </tr>
            </thead>
            <tbody id="new_periodic_policy_btw_datetime_table_tbody">

            </tbody>
        </table>
        <legend>配置分组<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="table-responsive" data-info="">
            <span  style="color: #00aaee;float: left;">如果某分组已被其他周期策略绑定，则不能绑定该分组</span>
        </div>
        <div class="control-group">
            <table class="table table-striped table-hover" id="update_periodic_policy_tags_table">
                  <thead>
                        <tr>
                            <th style="width: 10%;text-align: center;">名称</th>
                            <th style="width: 10%;text-align: center;">区域</th>
                            <th style="width: 20%;text-align: center;">机房</th>
                            <%if(Y.JSON.stringify(isOnline) == "true"){%>
                            <th style="width: 20%;text-align: center;">SET标识</th>
                            <%}else{%>
                            <th style="width: 8%;text-align: center;">泳道</th>
                            <%}%>
                            <th style="width: 20%;text-align: center;">绑定/解绑</th>
                        </tr>
                  </thead>
                  <tbody id="new_update_periodic_policy_tags_table_tbody">
                     <% Y.Array.each(this.tagList, function( item, index ){ %>
                        <tr data-info="<%= Y.JSON.stringify(item) %>">
                            <td style="text-align: center;"><%= item.tagName %></td>
                            <td style="text-align: center;"><%= item.region %></td>
                            <td style="text-align: center;"><%= item.idc %></td>
                            <%if(item.isOnline == "true"){%>
                            <td style="text-align: center;"><%= item.cell %></td>
                            <%}else{%>
                            <td style="text-align: center;"><%= item.swimlane %></td>
                            <%}%>
                            <td style="text-align: center;">
                               <input class="tag_periodic_in_" <%= item.binded==true? "disabled":"" %> type="checkbox" data-info="<%= item.id %>" name="periodicPolicyTags" <%= item.related==true? "checked":"" %>/>
                            </td>
                        </tr>
                    <% }); %>
                  </tbody>
             </table>
        </div>
    </div>
</textarea>

<script type="text/javascript">
    M.use('msgp-serviceopt/optHulkPeriodicPolicy', function (Y) {
        var appkey = '${appkey}';
        var envNumber = Y.one('#hulkScalingGroup_env_select a.btn-primary').getAttribute('value');

    });

    function checkScaleStep(obj) {
        var t = obj.value.replace(/[^\d]/g, '');
        if (obj.value != t) obj.value = t;
        if (obj.value < 0) obj.value = 1;
        if (obj.value > 20) obj.value = 20;
    }
</script>