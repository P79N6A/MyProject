// M.add('msgp-hulk/scalingGroup', function (Y) {
//     Y.namespace('msgp.hulk').scalingGroup = scalingGroup;
//     var inited = false;
//     var addScalingGroupDialog;
//     var setScheduleScalingPolicyDialog;
//     var setMonitorScalingPolicyDialog;
//     var setScalingGroupDialog;
//     var appkey,
//         showOverlay,
//         showContent,
//         wrapper = Y.one('#scalingGropuWrapper');
//         tbody = wrapper.one('tbody'),
//         pbody = wrapper.one('#paginator_scalingGroup');
//
//     var everPaged = false,
//         totalPage,
//         totalCount;
//     var launchConfigDialog;
//     var instancetypePerTr;
//
//     var colspan = 15;
//
//     var templateStr = [
//         '<% Y.Array.each(this.data, function( item, index ){ %>',
//         '<tr data-info="<%= Y.JSON.stringify(item) %>">',
//         '<td><input id="setScalingGroup" type="button" class="btn btn-mini btn-info" value="伸缩组">',
//         '<input id="setScheduleScalingPolicy" type="button" class="btn btn-mini btn-primary" value="周期伸缩策略">',
//         '<input id="setMonitorScalingPolicy" type="button" class="btn btn-mini btn-primary" value="监控伸缩策略">',
//         '<input id="delScalingGroup" type="button" class="btn btn-mini btn-danger" value="删除">',
//         '</td>',
//         '<td><%= item.id %></td>',
//         '<td><%= item.name %></td>',
//         '<td><%= item.maximumInstance %></td>',
//         '<td><%= item.minimumInstance %></td>',
//         '<td><%= item.desireInstance %></td>',
//         '<td id=insType value="<%=item.instancetype%>"><%if(item.instancetype==0){%>VM实例<%}else{%>Container实例<%}%></td>',
//         '<td><%= item.cooldown%></td>',
//         '<td><%= item.idc %></td>',
//         '<td><a class="launchConfig-detail" href="javascript:;">LaunchConfig</a></td>',
//         '</tr>',
//         '<% }); %>'
//     ].join('');
//
//     var vmLaunchConfigStr=[
//         '<div id="vmLaunchConfig_form" class="form-horizontal" data-info=<%= this.data%>>',
//         '<div class="control-group"><label class="control-label">cpu：</label>',
//         '<div class="controls">',
//         '<input id="s_cpu" type="text"  value="<%=this.data.cpu%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">内存：</label>',
//         '<div class="controls">',
//         '<input id="s_mem" type="text"  value="<%=this.data.mem%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">硬盘：</label>',
//         '<div class="controls">',
//         '<input id="s_hd" type="text"  value="<%=this.data.hd%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '</div>'
//     ].join('');
//
//     var containerLaunchConfigStr=[
//         '<div id="containerLaunchConfig_form" class="form-horizontal" data-info=<%= this.data%>>',
//         '<div class="control-group"><label class="control-label">cpu：</label>',
//         '<div class="controls">',
//         '<input id="s_cpu" type="text"  value="<%=this.data.cpu%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">内存：</label>',
//         '<div class="controls">',
//         '<input id="s_mem" type="text"  value="<%=this.data.mem%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">imageName：</label>',
//         '<div class="controls">',
//         '<input id="s_imageName" type="text"  value="<%=this.data.mem%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">volumes：</label>',
//         '<div class="controls">',
//         '<input id="s_volumes" type="text"  value="<%=this.data.volumes%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">command：</label>',
//         '<div class="controls">',
//         '<input id="s_command" type="text"  value="<%=this.data.command%>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">failurePolicy：</label>',
//         '<div class="controls">',
//         '<input id= "s_failurePolicy" type="text" value=<% if(this.data.failurePolicy == 0){ %> KILL <% } else if(this.data.failurePolicy == 1){ %> RESTART <% } else {%> IGNORE <%}%> </td>' ,
//         //'<select id="s_failurePolicy">',
//         //'<option value="0" <%= (0==this.data.failurePolicy)? "selected":""%> >KILL</option>',
//         //'<option value="1" <%= (1==this.data.failurePolicy)?"selected":""%> >RESTART</option>',
//         //'<option value="1" <%= (2==this.data.failurePolicy)?"selected":""%> >IGNORE</option>',
//         //'</select>',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '</div>',
//     ].join('');
//
//     //更新伸缩组-VM实例
//     var updateVMScalingGroupStr = [
//         '<div id="ScalingGroup_form" class="form-horizontal" data-info=<%= this.data%>>',//Y.JSON.stringify(this.data)
//         '<div class="control-group"><label class="control-label">ID：</label>',
//         '<div class="controls">',
//         '<input id="s_id" type="text" value="<%= this.data.id %>" style="background-color:#F3EEEE;" readOnly="true"/>',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">伸缩组名：</label>',
//         '<div class="controls">',
//         '<input id="s_name" type="text" value="<%= this.data.name %>"  />',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">最大实例数：</label>',
//         '<div class="controls">',
//         '<input id="s_maximumInstance" type="text" value="<%= this.data.maximumInstance%>"  />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">最小实例数：</label>',
//         '<div class="controls">',
//         '<input id="s_minimumInstance" type="text" value="<%= this.data.minimumInstance%>"  />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">期望实例数：</label>',
//         '<div class="controls">',
//         '<input id="s_desireInstance" type="text" value="<%= this.data.desireInstance%>" placeholder="正整数" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">实例类型：</label>',
//         '<div class="controls">',
//         //'<select id="s_instancetype">',
//         //'<option value="0" selected readOnly="true" >VM实例</option>',
//         ////'<option value="1" <%= (1==this.data.type)? "selected":""%> readOnly="true" >Container实例</option>',
//         //'</select>',
//         '<input id="s_instancetype" type="text" value="VM实例" style="background-color:#F3EEEE;" readOnly="true"/>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">冷却时间（秒）：</label>',
//         '<div class="controls">',
//         '<input id="s_cooldown" type="text"  value="<%= this.data.cooldown%>" placeholder="正整数" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">idc：</label>',
//         '<div class="controls">',
//         '<input id="s_idc" type="text"  value="<%=this.data.idc %>" placeholder="机房" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">cpu：</label>',
//         '<div class="controls">',
//         '<input id="s_cpu" type="text"  value="<%=this.data.cpu %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">内存：</label>',
//         '<div class="controls">',
//         '<input id="s_mem" type="text"  value="<%=this.data.mem %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">硬盘：</label>',
//         '<div class="controls">',
//         '<input id="s_hd" type="text"  value="<%=this.data.hd %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '</div>'
//     ].join('');
//
//     //更新伸缩组-Contaner实例
//     var updateContanerScalingGroupStr = [
//         '<div id="ScalingGroup_form" class="form-horizontal" data-info=<%= this.data%>>',//Y.JSON.stringify(this.data)
//         '<div class="control-group"><label class="control-label">ID：</label>',
//         '<div class="controls">',
//         '<input id="s_id" type="text" value="<%= this.data.id %>" style="background-color:#F3EEEE;" readOnly="true"/>',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">伸缩组名：</label>',
//         '<div class="controls">',
//         '<input id="s_name" type="text" value="<%= this.data.name %>"  />',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">最大实例数：</label>',
//         '<div class="controls">',
//         '<input id="s_maximumInstance" type="text" value="<%= this.data.maximumInstance%>"  />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">最小实例数：</label>',
//         '<div class="controls">',
//         '<input id="s_minimumInstance" type="text" value="<%= this.data.minimumInstance%>"  />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">期望实例数：</label>',
//         '<div class="controls">',
//         '<input id="s_desireInstance" type="text" value="<%= this.data.desireInstance%>" placeholder="正整数" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">实例类型：</label>',
//         '<div class="controls">',
//         //'<select id="s_type">',
//         //'<option value="0" selected readOnly="true" >Container实例</option>',
//         ////'<option value="1" <%= (1==this.data.type)? "selected":""%> readOnly="true" >Container实例</option>',
//         //'</select>',
//         '<input id="s_instancetype" type="text" value="Container实例" style="background-color:#F3EEEE;" readOnly="true"/>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">冷却时间（秒）：</label>',
//         '<div class="controls">',
//         '<input id="s_cooldown" type="text"  value="<%= this.data.cooldown%>" placeholder="正整数" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">idc：</label>',
//         '<div class="controls">',
//         '<input id="s_idc" type="text"  value="<%=this.data.idc %>" placeholder="机房" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">cpu：</label>',
//         '<div class="controls">',
//         '<input id="s_cpu" type="text"  value="<%=this.data.cpu %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">内存：</label>',
//         '<div class="controls">',
//         '<input id="s_mem" type="text"  value="<%=this.data.mem %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">imageName：</label>',
//         '<div class="controls">',
//         '<input id="s_imageName" type="text"  value="<%=this.data.imageName %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">volumes：</label>',
//         '<div class="controls">',
//         '<input id="s_volumes" type="text"  value="<%=this.data.volumes %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">command：</label>',
//         '<div class="controls">',
//         '<input id="s_command" type="text"  value="<%=this.data.command %>" placeholder="" />',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '<div class="control-group"><label class="control-label">failurePolicy：</label>',
//         '<div class="controls">',
//         //'<input id="s_failurePolicy" type="text"  value="<%=this.data.failurePolicy %>" placeholder="" />',
//         '<select id="s_failurePolicy">',
//         '<option value="0"  <%= (0==this.data.failurePolicy)? "selected":""%> >KILL</option>',
//         '<option value="1" <%= (1==this.data.failurePolicy)? "selected":""%>  >RESTART</option>',
//         '<option value="2" <%= (2==this.data.failurePolicy)? "selected":""%>  >IGNORE</option>',
//         '</select>',
//         '<span class="tips"></span>',
//         '</div>',
//         '</div>',
//         '</div>'
//     ].join('');
//
//     //新增弹性伸缩组
//     var dialogContentStr = [
//         '<div id="SG_LanchConfig_form" class="form-horizontal" style="Height:350px">',
//         '<div id="ScalingGroup_form" class="form-horizontal" style="width:150px; float:left;">',
//             '<div class="control-group style="Height:30px"><label class="control-label">伸缩组名：</label>',
//             '<div class="controls">',
//             '<input id="add_name" type="text" value="" placeholder="字符串" />',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">最大实例数：</label>',
//             '<div class="controls">',
//             '<input id="add_maximumInstance" type="text" value="" placeholder="正整数" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">最小实例数：</label>',
//             '<div class="controls">',
//             '<input id="add_minimumInstance" type="text" value="" placeholder="正整数" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">期望实例数：</label>',
//             '<div class="controls">',
//             '<input id="add_desireInstance" type="text" value="" placeholder="正整数" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">实例类型：</label>',
//             '<div class="controls">',
//             '<select id="add_type">',
//             '<option value="0" selected="selected">VM实例</option>',
//             '<option value="1">Container实例</option>',
//             '</select>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">冷却时间(秒)：</label>',
//             '<div class="controls">',
//             '<input id="add_cooldown" type="text"  value="" placeholder="正整数" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">idc：</label>',
//             '<div class="controls">',
//             '<input id="add_idc" type="text"  value="" placeholder="机房" />',
//             '</div>',
//             '</div>',
//         '</div>',
//
//         '<div id="LanchConfig_form" class="form-horizontal" style="margin-left:350px;">',
//             '<div class="control-group" style="Height:30px"><label class="control-label">cpu：</label>',
//             '<div class="controls">',
//             '<input id="add_cpu" type="text"  value="" placeholder="" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">内存：</label>',
//             '<div class="controls">',
//             '<input id="add_mem" type="text"  value="" placeholder="" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">硬盘：</label>',
//             '<div class="controls">',
//             '<input id="add_hd" type="text"  value="" placeholder="" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">imageName：</label>',
//             '<div class="controls">',
//             '<input id="add_imageName" type="text"  value="" placeholder="" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">volumes：</label>',
//             '<div class="controls">',
//             '<input id="add_volumes" type="text"  value="" placeholder="" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">command：</label>',
//             '<div class="controls">',
//             '<input id="add_command" type="text"  value="" placeholder="" />',
//             '<span class="tips"></span>',
//             '</div>',
//             '</div>',
//             '<div class="control-group" style="Height:30px"><label class="control-label">failurePolicy：</label>',
//             '<div class="controls">',
//             '<select id="add_failurePolicy">',
//             '<span class="tips"></span>',
//             '<option value="0" selected >KILL</option>',
//             '<option value="1" >RESTART</option>',
//             '<option value="2" >IGNORE</option>',
//             '</select>',
//             '</div>',
//         '</div>'
//     ].join('');
//
//    //设置周期伸缩策略对话框
//     var dialogScheduleScalingPolicyStr = [
//         '<div id="ScheduleScalingPolicy_dialog"  class="table-responsive">',
//         //'<div id="scalingGroup_id"><%= this.scalingGroupId %></div>',
//         '<p><b>周期伸缩：</b>在指定时间点增加或减少指定数量的服务实例。周期性策略的优先级最高</p>',
//         //'<p><b>伸缩策略2：</b>监控伸缩：Hulk根据服务的CPU、内存、网络IO等实时状态判断服务是否需要扩容或缩容。监控触发的优先级较低</p>',
//         '<br>',
//         '<table id="ScheduleScalingPolicy_table" class="table table-striped table-hover">',
//         '<thead id="<%= this.scalingGroupId %>">',
//         '<th >起始时间(天/周)</th>',
//         '<th >结束时间(天/周)</th>',
//         '<th >起始时间(时/天)</th>',
//         '<th >结束时间(时/天)</th>',
//         '<th >伸缩实例数(个)</th>',
//         //'<th >策略状态 </th>',
//         '<th>操作</th>',
//         '</thead>',
//         '<% Y.Array.each(this.data, function( item, index ){ %>',
//         '<tr id="ScheduleScalingPolicy_table_tr_<%= index %>" data-info="<%= Y.JSON.stringify(item) %>">',
//         '<td id="startDay"><%=item.startDay %></td>',
//         '<td id="endDay"><%=item.endDay %></td>',
//         '<td id="startTime"><%=item.startTime %></td>',
//         '<td id="endTime"><%= item.endTime%></td>',
//         '<td id="esgNum"><%= item.esgNum%></td>',
//         //'<td id="state"><%if(item.state==0){%>启用<%}else{%>禁用<%}%></td>',
//         '<td><input id="delScheduleScalingPolicy" type="button" class="btn btn-mini btn-danger" value="删除"></td>',
//         '</tr>',
//         '<% }); %>',
//         '</table>',
//         '<button id="addScheduleScalingPolicy" class="btn btn-success J-config-panel-add">添加</button>',
//         '</div>'
//     ].join('');
//
//     //设定监控伸缩策略对话框
//     var dialogMonitorScalingPolicyStr = [
//         '<div id="MonitorScalingPolicy_dialog" value="<%= this.scalingGroupId %>"  class="table-responsive">',
//         //'<div id="<%= this.scalingGroupId %>"  class="table-responsive">',
//             '<div class="form-inline mt10 mb20"  style="padding-left: 00px;">选择接口：',
//             '<input id="service_method" type="text" class="span3" placeholder="appkey method" />',
//             '<span class="tips" style="color: rgb(255, 0, 0);padding-left: 20px;">Tips:仅在监控项选择TP时有效，细化到服务的接口级别</span>',
//             '</div>',
//         '<div class="form-inline mt10 mb20"  style="padding-left: 00px;">',
//         '<div class="control-group">',
//             '<label >决策：</label>',
//                 '<select id="esPolicy" style="width:8%">',
//                     '<option value="0">扩容</option>',
//                     '<option value="1">缩容</option>',
//                 '</select>' ,
//             '<label class="ml20">监控项：</label>',
//                '<select id="triggerItems" style="width:8%">',
//                  '<option value="cpu">cpu</option>',
//                  '<option value="mem">mem</option>',
//                  '<option value="qps">qps</option>',
//                  '<option value="TP50">TP50</option>',
//                  '<option value="TP90">TP90</option>',
//                  '<option value="TP95">TP95</option>',
//                  '<option value="TP99">TP99</option>',
//                '</select>',
//             '<label class="ml20">触发条件：</label>',
//                '<select id="triggerFunctions" style="width:8%">',
//                  '<option value="0">大于</option>',
//                  '<option value="1">小于</option>',
//                '</select>',
//             '<label class="ml20">阈值：</label>',
//                '<input id="threshold" type="text" style="width:8%" placeholder="必须是正整数" />',
//
//             '<label class="ml20">伸缩梯度：</label>',
//             '<input id="gradient" type="text" style="width:8%" placeholder="必须是正整数" />',
//
//             '<button id="add_trigger" class="btn btn-primary ml20">添加</button>',
//
//         '</div>',
//         '</div>',
//
//         '<table id="MonitorScalingPolicy_table" class="table table-striped table-hover">',
//         '<thead id="<%= this.scalingGroupId %>">' ,
//         '<tr>',
//         '<th>决策</th>',
//         '<th>监控项</th>',
//         '<th>触发条件</th>',
//         '<th>阈值</th>',
//         '<th>伸缩梯度</th>',
//         //'<th>策略状态</th>',
//         '<th>操作</th>',
//         '</tr>',
//         '</thead>',
//         '<tbody>',
//         '<% Y.Array.each(this.data, function( item, index ){ %>',
//         '<tr id="MonitorScalingPolicy_table_tr_<%= index %>" data-info="<%= Y.JSON.stringify(item) %>">',
//         '<td id="esPolicy"><%if(item.esPolicy==0){%>扩容<%}else{%>缩容<%}%></td>',
//         '<td id="triggerItems"><%=item.triggerItems %></td>',
//         '<td id="triggerFunctions"><%if(item.triggerFunction==0){%>大于<%}else{%>小于<%}%></td>',
//         '<td id="threshold"><%= item.threshold %></td>',
//         '<td id="gradient"><%= item.esgNum %></td>',
//         //'<td id="state"><%if(item.state==0){%>启用<%}else{%>禁用<%}%></td>',
//         '<td><input id="delMonitorScalingPolicy" type="button" class="btn btn-mini btn-danger" value="删除"></td>',
//         //'<td><a class="trigger-edit" href="javascript:;">编辑</a><a class="trigger-delete ml20" href="javascript:;">删除</a></td>',
//         '</tr>',
//         '<% }); %>',
//         '</tbody>',
//         '</table>',
//
//         '</div>'
//     ].join('');
//
//     function scalingGroup(key, func1, func2) {
//         if (!inited) {
//             appkey = key;
//             showOverlay = func1;
//             showContent = func2;
//
//             bindAddScalingGroupQuota();
//             bindSetScalingGroup();
//             bindDelScalingGroup();
//             bindLaunchConfig();
//             bindsetScheduleScalingPolicy();
//             bindsetMonitorScalingPolicy();
//             inited = true;
//         }
//         getScalingGroupQuota(1);
//     }
//
//     function bindAddScalingGroupQuota() {
//         wrapper.delegate('click', function () {
//             addScalingGroupDialog = addScalingGroupDialog ? addScalingGroupDialog : new Y.mt.widget.CommonDialog({
//                 id: 'ScalingGroup_dialog',
//                 title: '新增伸缩组',
//                 width: 1000,
//                 btn: {
//                     pass: doAddScalingGroupQuota
//                 }
//             });
//             Y.msgp.service.commonMap(showDialogAfter);
//             function showDialogAfter(obj) {
//                 var micro = new Y.Template();
//                 var str = micro.render(dialogContentStr);
//                 addScalingGroupDialog.setContent(str);
//                 addScalingGroupDialog.show();
//                 //initAddScalingGroupDialog();
//                // bindAddType();
//             }
//         }, '#add_ScalingGroup');
//     }
//
//     //function bindAddType(){
//     //    console.log("bindAddType is called")
//     //    Y.delegate('change',function(){
//     //        console.log("alert is called")
//     //        alert('YES');
//     //    },'#add_type')
//     //}
//
//     function bindLaunchConfig(){
//         tbody.delegate('click', function(){
//             var tr = this.ancestor('tr');
//             var info=Y.JSON.parse(tr.getData('info'));
//             instancetypePerTr=info.instancetype;
//             launchConfigDialog = launchConfigDialog ? launchConfigDialog : new Y.mt.widget.CommonDialog({
//                 title : '伸缩实例详情',
//                 width : 550
//                 //btn : {
//                 //    pass : refreshLineData
//                 //}
//             });
//             Y.msgp.service.commonMap(showDialogAfter);
//             function showDialogAfter(obj) {
//                 var micro = new Y.Template();
//                 var str ;
//                 if(instancetypePerTr==0)str= micro.render(vmLaunchConfigStr,{data:info});
//                 else str=micro.render(containerLaunchConfigStr,{data:info});
//                 launchConfigDialog.setContent(str);
//                 launchConfigDialog.show();
//             }
//         }, '.launchConfig-detail');
//     }
//
//     function bindSetScalingGroup() {
//         var Id;
//         setScalingGroupDialog = setScalingGroupDialog ? setScalingGroupDialog : new Y.mt.widget.CommonDialog({
//             id: 'set_ScalingGroup_dialog',
//             title: '设置伸缩组',
//             width: 640,
//             btn: {
//                 pass: doUpdateScalingGroupQuota
//             }
//         });
//
//         wrapper.delegate('click', function () {
//             var el = this;
//             var line = el.ancestor('tr');
//             var data = Y.JSON.parse(line.getData('info'));
//             set_ScalingGroup_id =  data.id;
//             instancetypePerTr=data.instancetype;
//             //set_ScalingGroup_InstanceType=data.type;
//             Y.msgp.service.commonMap(showDialogAfter);
//             function showDialogAfter(obj) {
//                 var micro = new Y.Template();
//                 var str ;
//                 if(instancetypePerTr==0)str= micro.render(updateVMScalingGroupStr,{data:data});
//                 else str=micro.render(updateContanerScalingGroupStr,{data:data});
//                 console.log("setScalingGroupDialog",str);
//                 setScalingGroupDialog.setContent(str);
//                 setScalingGroupDialog.show();
//             }
//
//         }, '#setScalingGroup');
//     };
//
//     function bindDelScalingGroup() {
//         wrapper.delegate('click', function () {
//             var el = this;
//             var line = el.ancestor('tr');
//             var scalingGroupId = getScalingGroupIdFromLine(line);
//
//             Y.io('/hulk/' + scalingGroupId + '/scalingGroup/del', {
//                 method: 'post',
//                 on: {
//                     success: function (id, o) {
//                         var ret = Y.JSON.parse(o.responseText);
//                         if (ret.isSuccess) {
//                             Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
//                             getScalingGroupQuota(1);
//                         } else {
//                             Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                         }
//                     },
//                     failure: function () {
//                         Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                     }
//                 }
//             });
//         }, '#delScalingGroup');
//     }
//
//     function getScalingGroupIdFromLine(line) {
//         var data = line.getData('info');
//         return Y.JSON.parse(data).id;
//     }
//
//     function getScalingGroupQuota(pageNo) {
//         //var env = Y.one('#ScalingGroup_env_select a.btn-primary').getAttribute('value');
//         showContent(wrapper);
//         var url = '/hulk/' + appkey + '/scalingGroup/get';
//
//         Y.io(url, {
//             method: 'get',
//             data: {
//                 env:3
//                 //env: env
//             },
//             on: {
//                 success: function (id, o) {
//                     var ret = Y.JSON.parse(o.responseText);
//                     if (ret.isSuccess) {
//                         var data = ret.data;
//                         var pobj = ret.page;
//
//                         if (Y.Lang.isArray(data) && data.length !== 0) {
//                             fillQuota(data);
//                             if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
//                                 refreshPaginator(pbody, pobj);
//                             }
//                         } else if (data.length === 0) {
//                             emptyOrError();
//                         }
//                         everPaged = true;
//                         totalPage = pobj.totalPageCount;
//                         totalCount = pobj.totalCount;
//                     } else {
//                         emptyOrError(true);
//                     }
//                 },
//                 failure: function () {
//                     emptyOrError(true);
//                 }
//             }
//         });
//     }
//
//     function refreshPaginator( pbody, pobj ){
//         new Y.mt.widget.Paginator({
//             contentBox: pbody,
//             index: pobj.pageNo || 1,
//             max: pobj.totalPageCount || 1,
//             pageSize: pobj.pageSize,
//             totalCount: pobj.totalCount,
//             callback : changePage
//         });
//     }
//
//     function fillQuota(arr) {
//         Y.msgp.service.commonMap(fillQuotaAfter);
//         function fillQuotaAfter(obj) {
//             for(var i=0,l=arr.length; i<l; i++){
//                 var tmp = arr[i];
//                 tmp.envDesc = obj.env[ tmp.env ] || tmp.env;
//             }
//             console.log("arr",arr);
//             var html = wrapData( arr );
//             tbody.setHTML(html);
//             showContent(wrapper);
//         }
//     }
//
//     function wrapData( arr ){
//         var micro = new Y.Template();
//         var str = micro.render( templateStr, {data : arr} );
//         return str;
//     }
//
//     function emptyOrError(isError) {
//         var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容') + '<a href="javascript:;" class="get-again">重新获取</a></td></tr>';
//         tbody.setHTML(html);
//         pbody.empty();
//         showContent(wrapper);
//     }
//
//     function doUpdateScalingGroupQuota() {
//         var url =  '/hulk/'+appkey + '/scalingGroup/add';
//         //var scalingGroupid=getScalingGroupIdFromLine();
//         //var envTmp=Y.one('#s_env').get('value');
//         //var envInt;
//         //if(envTmp=="prod"){
//         //    envInt=0;
//         //}else if(envTmp=="stage") {
//         //    envInt=1;
//         //}else{
//         //    envInt=2;
//         //}
//         var data;
//         if(instancetypePerTr==0){
//              data = {
//                 id: set_ScalingGroup_id,
//                 appkey: appkey,
//                 env:3,
//                 name: Y.one("#s_name").get('value'),
//                 idc: Y.one("#s_idc").get('value'),
//                 maximumInstance: +Y.one("#s_maximumInstance").get('value'),
//                 minimumInstance: +Y.one('#s_minimumInstance').get('value'),
//                 desireInstance: +Y.one('#s_desireInstance').get('value'),
//                 instancetype: 0,
//                 cooldown: +Y.one("#s_cooldown").get('value'),
//                 cpu:+Y.one("#s_cpu").get('value'),
//                 mem:+Y.one("#s_mem").get('value'),
//                 hd:+Y.one("#s_hd").get('value'),
//                 imageName:"imageNameUseless",
//                 volumes:"volumesUseless",
//                 command:"commandUseless",
//                 failurePolicy:-1,
//                 ctime: ~~(new Date() / 1000),
//                 utime: ~~(new Date() / 1000)
//             };
//         }else{
//              data = {
//                 id: set_ScalingGroup_id,
//                 appkey: appkey,
//                 env:3,
//                 name: Y.one("#s_name").get('value'),
//                 idc: Y.one("#s_idc").get('value'),
//                 maximumInstance: +Y.one("#s_maximumInstance").get('value'),
//                 minimumInstance: +Y.one('#s_minimumInstance').get('value'),
//                 desireInstance: +Y.one('#s_desireInstance').get('value'),
//                 instancetype: 1,
//                 cooldown: +Y.one("#s_cooldown").get('value'),
//                 cpu:+Y.one("#s_cpu").get('value'),
//                 mem:+Y.one("#s_mem").get('value'),
//                 hd:-1,
//                 imageName: Y.one("#s_imageName").get('value'),
//                 volumes:Y.one("#s_volumes").get('value'),
//                 command:Y.one("#s_command").get('value'),
//                 failurePolicy:parseInt(Y.one("#s_failurePolicy").get('value')),
//                 ctime: ~~(new Date() / 1000),
//                 utime: ~~(new Date() / 1000)
//             };
//         }
//         Y.io(url, {
//             method: 'post',
//             headers : {'Content-Type':"application/json;charset=UTF-8"},
//             data: Y.JSON.stringify(data),
//             on: {
//                 success: function (id, o) {
//                     var ret = Y.JSON.parse(o.responseText);
//                     if (ret.isSuccess) {
//                         Y.msgp.utils.msgpHeaderTip('success', '修改成功', 3);
//                         getScalingGroupQuota(1);
//                         setScalingGroupDialog.close();
//                         //清空搜索框
//                     } else {
//                         Y.msgp.utils.msgpHeaderTip('error', ret.msg || '修改失败', 3);
//                         getScalingGroupQuota(1);
//                     }
//                 },
//                 failure: function () {
//                     Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
//                     getScalingGroupQuota(1);
//                 }
//             }
//         });
//         return true;
//     }
//
//     function doAddScalingGroupQuota() {
//         var url = '/hulk/'+appkey + '/scalingGroup/add';
//         var data = {
//             id: 0,
//             appkey: appkey,
//             env:3,
//             name: Y.one("#add_name").get('value'),
//             idc: Y.one("#add_idc").get('value'),
//             maximumInstance: +Y.one("#add_maximumInstance").get('value'),
//             minimumInstance: +Y.one('#add_minimumInstance').get('value'),
//             desireInstance: +Y.one('#add_desireInstance').get('value'),
//             instancetype: +Y.one('#add_type').get('value'),
//             cooldown: +Y.one("#add_cooldown").get('value'),
//             cpu:+Y.one("#add_cpu").get('value'),
//             mem:+Y.one("#add_mem").get('value'),
//             hd:+Y.one("#add_hd").get('value')||-1,
//             imageName: Y.one("#add_imageName").get('value')||"imageNameNil",
//             volumes:Y.one("#add_volumes").get('value')||"volumesNil",
//             command:Y.one("#add_command").get('value')||"commandNil",
//             failurePolicy:parseInt(Y.one("#add_failurePolicy").get('value'))||-1,
//             ctime: ~~(new Date() / 1000),
//             utime: ~~(new Date() / 1000)
//         };
//         console.log("Y.JSON.stringify(data)",Y.JSON.stringify(data));
//         Y.io(url, {
//             method: 'post',
//             headers : {'Content-Type':"application/json;charset=UTF-8"},
//             data: Y.JSON.stringify(data),
//             on: {
//                 success: function (id, o) {
//
//                     var ret = Y.JSON.parse(o.responseText);
//                     console.log(ret);
//                     if (ret.isSuccess) {
//                         Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
//                         addScalingGroupDialog.close();
//                         //清空搜索框
//                         getScalingGroupQuota(1);
//                     } else {
//                         Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 3);
//                     }
//                 },
//                 failure: function () {
//                     Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
//                 }
//             }
//         });
//         return true;
//     }
//
//     function bindsetScheduleScalingPolicy() {
//         setScheduleScalingPolicyDialog = setScheduleScalingPolicyDialog ? setScheduleScalingPolicyDialog : new Y.mt.widget.CommonDialog({
//             id: 'add_ScheduleScalingPolicy_dialog',
//             title: '设置周期伸缩策略',
//             width: '1024',
//             btn: {
//                 pass: doAddScheduleScalingPolicy
//             }
//         });
//
//         bindScheduleScalingPolicyButtons(setScheduleScalingPolicyDialog);
//
//         wrapper.delegate('click', function () {
//             var el = this;
//             var line = el.ancestor('tr');
//             var scalingGroupId = getScalingGroupIdFromLine(line);
//
//             Y.io('/hulk/' + scalingGroupId + '/ScheduleScalingpolicy/get', {
//                 method: 'get',
//                 on: {
//                     success: function (id, o) {
//                         var ret = Y.JSON.parse(o.responseText);
//
//                         if (ret.isSuccess) {
//                             Y.msgp.service.commonMap(showDialogAfter);
//                             function showDialogAfter(obj) {
//                                 var micro = new Y.Template();
//                                 var str = micro.render(dialogScheduleScalingPolicyStr, {data: ret.data,scalingGroupId: scalingGroupId});
//
//                                 setScheduleScalingPolicyDialog.setContent(str);
//                                 setScheduleScalingPolicyDialog.show();
//                                 //initAddConsumerDialog();
//                             }
//                         } else {
//                             Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                         }
//                         getScalingGroupQuota(1);
//                     },
//                     failure: function () {
//                         Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                         getScalingGroupQuota(1);
//                     }
//                 }
//             });
//         }, '#setScheduleScalingPolicy');
//     }
//
//     function bindScheduleScalingPolicyButtons() {
//         var dbody = setScheduleScalingPolicyDialog.getBody();
//         dbody.delegate('click', function () {
//             var table = document.getElementById("ScheduleScalingPolicy_table");
//             var rowid = table.rows.length - 1;
//             var tr = document.createElement("tr");
//             tr.id = "ScheduleScalingPolicy_table_tr_" + rowid;
//             var tbody = table.tBodies[0];
//
//             var td0 = document.createElement("td");
//             //td0.innerHTML = '<input id="startDay" type="text"  value=" " name="unit" style="width:120px"/>';
//             td0.innerHTML = "<select id='startDay'>"+
//                 "<option value='1'>星期一</option>"+
//                 "<option value='2'>星期二</option>"+
//                 "<option value='3'>星期三</option>"+
//                 "<option value='4'>星期四</option>"+
//                 "<option value='5'>星期五</option>"+
//                 "<option value='6'>星期六</option>"+
//                 "<option value='7'>星期天</option>"+
//                 "</select>";
//             tr.appendChild(td0);
//
//             var td1 = document.createElement("td");
//             //td1.innerHTML = '<input id="endDay" type="text"  value=" " name="unit" style="width:120px" />';
//             td1.innerHTML = "<select id='endDay'>"+
//                 "<option value='1'>星期一</option>"+
//                 "<option value='2'>星期二</option>"+
//                 "<option value='3'>星期三</option>"+
//                 "<option value='4'>星期四</option>"+
//                 "<option value='5'>星期五</option>"+
//                 "<option value='6'>星期六</option>"+
//                 "<option value='7'>星期天</option>"+
//                 "</select>";
//             tr.appendChild(td1);
//
//             var td2 = document.createElement("td");
//             td2.innerHTML = '<input id="startTime" type="text"  value=" " name="unit" style="width:120px"/>';
//             tr.appendChild(td2);
//
//             var td3 = document.createElement("td");
//             td3.innerHTML = '<input id="endTime" type="text"  value=" " name="unit" style="width:120px" />';
//             tr.appendChild(td3);
//
//             var td4 = document.createElement("td");
//             td4.innerHTML = '<input id="esgNum" type="text"  value=" " name="unit" style="width:120px"/>';
//             tr.appendChild(td4);
//
//             //var td5 = document.createElement("td");
//             //td5.innerHTML = "<select id='state' style='width:100px'>"+"<option value='0'>启用</option>"+"<option value='1'>禁用</option>"+"</select>";
//             //tr.appendChild(td5);
//
//             var td6 = document.createElement("td");
//             td6.innerHTML = "<input id='delScheduleScalingPolicy' type=\"button\" class=\"btn btn-mini btn-danger\" value=\"删除\" style='width:54px'/>";
//             tr.appendChild(td6);
//
//             if (tbody == null) table.appendChild(tr);
//             else tbody.appendChild(tr);
//         }, '#addScheduleScalingPolicy');
//
//         dbody.delegate('click', function () {
//             var el = this;
//             var line = el.ancestor('tr');
//             var trId=line._stateProxy.id;
//             var table = document.getElementById("ScheduleScalingPolicy_table");
//             var tbody = table.tBodies[0];
//             var tr = document.getElementById(trId);
//             tr.remove();
//             if(line.getData('info')!=null) {
//                 var policyId = Y.JSON.parse(line.getData('info')).id;
//                 var url = '/hulk/' + policyId + '/policy/del';
//
//                 Y.io(url, {
//                     method: 'post',
//                     data: {},
//                     on: {
//                         success: function (id, o) {
//                             var ret = Y.JSON.parse(o.responseText);
//                             if (ret.isSuccess) {
//                                 Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
//                                 //清空搜索框
//                             } else {
//                                 Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
//                             }
//                         },
//                         failure: function () {
//                             Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
//                         }
//                     }
//                 });
//             }
//         }, '#delScheduleScalingPolicy');
//     }
//
//     function doAddScheduleScalingPolicy() {
//         //appkey 是无用的，可删
//         var url = '/hulk/' + appkey + '/AddScheduleScalingPolicy';
//         var tabledata = Y.one("#ScheduleScalingPolicy_table");
//         var listrow = tabledata.all("tr");
//         var scalingGroupId = parseInt(tabledata.one("thead").get('id'));
//         var jsonData = {};
//         jsonData.policyarr = [];
//
//         listrow.each(function (item, index) {
//             if (index != 0) {
//                 var row = {};
//                 row.startDay = parseInt(item.one("#startDay").get('value')).toString();
//                 row.endDay = parseInt(item.one("#endDay").get('value')).toString();
//                 row.startTime =parseInt(item.one("#startTime").get('value')).toString();
//                 row.endTime = parseInt(item.one("#endTime").get('value')).toString();
//                 row.esgNum = parseInt(item.one("#esgNum").get('value'));
//                 row.state = 0;//parseInt(item.one("#state").get('value'));
//                 row.scalingGroupId = scalingGroupId;
//                 row.id = 0;
//                 jsonData.policyarr.push(row);
//             }
//         });
//
//         if (jsonData.policyarr.length == 0) {
//             setScheduleScalingPolicyDialog.close();
//             getScalingGroupQuota(1);
//             return true;
//         }
//
//         Y.io(url, {
//             method: 'post',
//             headers : {'Content-Type':"application/json;charset=UTF-8"},
//             data: Y.JSON.stringify(jsonData),
//             on: {
//                 success: function (id, o) {
//                     var ret = Y.JSON.parse(o.responseText);
//                     if (ret.isSuccess) {
//                         Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
//                         setScheduleScalingPolicyDialog.close();
//                         //清空搜索框
//                         getScalingGroupQuota(1);
//                     } else {
//                         Y.msgp.utils.msgpHeaderTip('error', ret.msg || '增加失败', 3);
//                         getScalingGroupQuota(1);
//                     }
//                 },
//                 failure: function () {
//                     Y.msgp.utils.msgpHeaderTip('error', '增加失败', 3);
//                     getScalingGroupQuota(1);
//                 }
//             }
//         });
//         return true;
//     }
//
//     //监控伸缩
//     function bindsetMonitorScalingPolicy() {
//         setMonitorScalingPolicyDialog = setMonitorScalingPolicyDialog ? setMonitorScalingPolicyDialog : new Y.mt.widget.CommonDialog({
//             id: 'add_MonitorScalingPolicy_dialog',
//             title: '设置监控伸缩策略',
//             width: '1024'
//         });
//
//         wrapper.delegate('click', function () {
//         var el = this;
//         var line = el.ancestor('tr');
//         var scalingGroupId = getScalingGroupIdFromLine(line);
//         console.log("bindsetMonitorScalingPolicy  scalingGroupId",scalingGroupId);
//
//             Y.io('/hulk/' + scalingGroupId + '/MonitorScalingPolicy/get', {
//                 method: 'get',
//                 on: {
//                     success: function (id, o) {
//                         var ret = Y.JSON.parse(o.responseText);
//                         if (ret.isSuccess) {
//                             Y.msgp.service.commonMap(showDialogAfter);
//                             function showDialogAfter(obj) {
//                                 var micro = new Y.Template();
//                                 var str = micro.render(dialogMonitorScalingPolicyStr, {data: ret.data,scalingGroupId:scalingGroupId});
//                                 setMonitorScalingPolicyDialog.setContent(str);
//                                 setMonitorScalingPolicyDialog.show();
//                             }
//                         } else {
//                             Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                         }
//                         getScalingGroupQuota(1);
//                     },
//                     failure: function () {
//                         Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                         getScalingGroupQuota(1);
//                     }
//                 }
//             });
//         }, '#setMonitorScalingPolicy');
//
//         bindAddMonitorScalingPolicyButtons();
//     }
//
//     function bindAddMonitorScalingPolicyButtons()
//     {
//         var dbody = setMonitorScalingPolicyDialog.getBody();
//         dbody.delegate('click', function () {
//                var el = this;
//                if( el.getData('clicked') ) return;
//                var scalingGroupId=document.getElementById("MonitorScalingPolicy_dialog").getAttribute('value');
//                 var addButton = Y.one('#add_trigger');
//                 addButton.setData('clicked', true);
//                 var itemSelect = Y.one('#triggerItems');
//                 var funcSelect = Y.one('#triggerFunctions');
//
//                 var url='/hulk/'+appkey+'/MonitorScalingPolicy/add';
//                 var data = {
//                     "id" : 0,
//                     "scalingGroupId":parseInt(scalingGroupId),
//                     "esPolicy":parseInt(Y.one('#esPolicy').get('value')),
//                     "triggerItems" : itemSelect.get('value'),
//                     "triggerFunction" : +Y.one('#triggerFunctions').get('value'),//> <
//                     "threshold" : +Y.one('#threshold').get('value'),
//                     "spanname" : Y.one('#MonitorScalingPolicy_dialog').one('#service_method').get('value'),
//                     "state":0,
//                     "esgNum" : +Y.one('#gradient').get('value')
//                 };
//
//                 Y.io(url, {
//                     method : 'post',
//                     data : Y.JSON.stringify( data ),
//                     on : {
//                         success : function(id, o){
//                             addButton.setData('clicked', false);
//                             var ret = Y.JSON.parse( o.responseText );
//                             if( ret.isSuccess ){
//                                 Y.msgp.utils.msgpHeaderTip('success', '添加成功' ,3);
//                                 Y.one('#threshold').set('value', '').setData('status', false);
//                                 getTriggerItems( 1 );
//                             }else{
//                                 Y.msgp.utils.msgpHeaderTip('error', '添加失败' || ret.msg ,3);
//                             }
//                         },
//                         failure : function(){
//                             addButton.setData('clicked', false);
//                             Y.msgp.utils.msgpHeaderTip('error', '添加失败' ,3);
//                         }
//                     }
//                 });
//         },'#add_trigger');
//
//         dbody.delegate('click', function () {
//             var el = this;
//             var line = el.ancestor('tr');
//             var trId=line._stateProxy.id;
//             var table = document.getElementById("MonitorScalingPolicy_table");
//             var tr = document.getElementById(trId);
//             var policyId = Y.JSON.parse(line.getData('info')).id;
//             var url = '/hulk/' + policyId + '/policy/del';
//             tr.remove();
//
//             Y.io(url, {
//                 method: 'post',
//                 data: {},
//                 on: {
//                     success: function (id, o) {
//                         var ret = Y.JSON.parse(o.responseText);
//                         if (ret.isSuccess) {
//                             Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
//                             //清空搜索框
//                         } else {
//                             Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 3);
//                         }
//                     },
//                     failure: function () {
//                         Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
//                     }
//                 }
//             });
//         }, '#delMonitorScalingPolicy');
//     }
//
//     function getTriggerItems(pagNo){
//         var el = this;
//         var scalingGroupId=document.getElementById("MonitorScalingPolicy_dialog").getAttribute('value');
//
//         Y.io('/hulk/' + scalingGroupId + '/MonitorScalingPolicy/get', {
//             method: 'get',
//             on: {
//                 success: function (id, o) {
//                     var ret = Y.JSON.parse(o.responseText);
//
//                     if (ret.isSuccess) {
//                         Y.msgp.service.commonMap(showDialogAfter);
//                         function showDialogAfter(obj) {
//                             var micro = new Y.Template();
//                             var str = micro.render(dialogMonitorScalingPolicyStr, {data: ret.data,scalingGroupId: scalingGroupId});
//
//                             setMonitorScalingPolicyDialog.setContent(str);
//                             setMonitorScalingPolicyDialog.show();
//                             //initAddConsumerDialog();
//                         }
//                     } else {
//                         Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                     }
//
//                 },
//                 failure: function () {
//                     Y.msgp.utils.msgpHeaderTip('error', '获取失败', 3);
//                 }
//             }
//         });
//     }
// }, '0.0.1', {
//     requires: [
//         'mt-base',
//         'mt-io',
//         'mt-date',
//         'w-base',
//         'w-paginator',
//         'template',
//         'msgp-utils/msgpHeaderTip',
//         'msgp-utils/check',
//         'msgp-utils/localEdit',
//         'msgp-service/commonMap'
//     ]
// });
