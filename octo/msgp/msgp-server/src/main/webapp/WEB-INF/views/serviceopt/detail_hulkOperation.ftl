 <div id="div_hulk_operation_records" class="form-inline mb20 table-responsive" style="display:none;">
     <div class="control-group">
         <label> 开始时间：</label><input id="hulk_start_datetime" type="text" class="span3" placeholder="查询开始时间">
         <label class="ml20"> 结束时间：</label><input id="hulk_end_datetime" type="text" class="span3" placeholder="查询结束时间">
         <label class="ml20">操作对象：</label>
         <select id="hulk_operation_entity_type_select" name="actionType" title="操作对象">
             <option value="unified_policy" selected>监控策略</option>
             <option value="periodic_policy">周期策略</option>
             <option value="tag_info">分组</option>
         </select>
         <label class="ml20">操作类型：</label>
         <select id="hulk_operation_action_type_select" name="actionType" title="操作类型">
             <option value="" selected>所有</option>
             <option value="1">新增监控策略</option>
             <option value="2">修改监控策略</option>
             <option value="3">启用监控策略</option>
             <option value="4">停用监控策略</option>
             <option value="5">删除监控策略</option>
         </select>
     </div>
     <div id="hulk_operation_action_div">
         <table class="table table-striped table-hover">
             <thead>
             <tr>
                 <th style="width:20%;text-align:center">时间</th>
                 <th style="width:20%;text-align:center">操作人</th>
                 <th style="width:20%;text-align:center">操作类型</th>
                 <th style="width:20%;text-align:center">操作对象id</th>
                 <th style="width:20%;text-align:center">详细信息</th>
             </tr>
             </thead>
             <tbody id="hulk_operation_action_tbody">
             <tr>
                 <td colspan="5" style="text-align: center;">内容加载中...</td>
             </tr>
             </tbody>
         </table>
         <div id="hulk_operation_action_page_wrapper">
         </div>
     </div>

     <textarea id="text_hulk_operation_action" style="display:none">
        <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td style="width: 20%;text-align:center;"><%=item.operationTime%></td>
            <td style="width: 20%;text-align:center;"><%=item.operatorName%></td>
            <td style="width: 20%;text-align:center;"><%=item.actionTypeName%></td>
            <td style="width: 20%;text-align:center;"><%=item.entityId%></td>
            <td style="width: 20%;text-align:center;">
                <a id="operation_action_details_<%=item.index%>" href="javascript:void(0)">详情</a>
            </td>
        </tr>
        <% }); %>
     </textarea>

     <textarea id="text_hulk_operation_action_type" style="display:none">
         <option value="" selected>所有</option>
        <% Y.Array.each(this.data, function( item, index ){ %>
         <option value="<%=item.value%>"><%=item.name%></option>
        <% }); %>
     </textarea>
 </div>