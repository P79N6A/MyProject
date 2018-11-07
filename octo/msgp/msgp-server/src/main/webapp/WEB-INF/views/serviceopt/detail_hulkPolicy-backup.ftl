<div id="hulkPolicy_content">
    <div>
    <#include "/common/env.ftl" >
        <div id="hulkScalingGroup_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
        <#--<a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>-->
        </div>
        <a style="padding-left:2.0em"></a>
        <button id="refreshHulkPolicy" type="button" class="btn btn-primary" title="刷新列表">
            <i class="fa fa-refresh">刷新</i>
        </button>
        <div style="float: right;">
            <a href="http://wiki.sankuai.com/pages/viewpage.action?pageId=982641062" target="_blank">
                HULK弹性伸缩介绍<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
    <table><tr><td style="width:100%;height:14px;font-size:14px;border:0"></td></tr></table>
    <legend>策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span>
        <button id="addNewScalingPolicy" type="button" class="btn" title="添加策略"><i class="fa fa-plus">监控策略</i>
            <button id="addNewPeriodicPolicy" type="button" class="btn" title="添加策略"><i class="fa fa-plus">周期策略</i>
                <button id="addModifySgGroup" type="button" class="btn" title="修改机房最大实例"><i class="fa fa-plus">资源自助修改</i>
                </button><a href="https://wiki.sankuai.com/pages/viewpage.action?pageId=1314767732" target="_blank">&nbsp<i class="fa fa-question-circle"></i></a>
    </legend>
    <div id="table_supplier" class="table-responsive">
        <table class="table table-striped table-hover" id="hulkScalingPolicy_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 10%;text-align: center;">策略类型</th>
                <th style="width: 6%;text-align: center;">扩容阈值</th>
                <th style="width: 6%;text-align: center;">扩容数量</th>
                <th style="width: 6%;text-align: center;">缩容阈值</th>
                <th style="width: 6%;text-align: center;">缩容数量</th>
                <th style="width: 6%;text-align: center;">机房</th>
                <th style="width: 7%;text-align: center;">最小实例数</th>
                <th style="width: 7%;text-align: center;">最大实例数</th>
                <th style="width: 8%;text-align: center;">禁止缩容时间</th>
                <th style="width: 14%;text-align: center;">机器配置(CPU:内存:硬盘)</th>
                <th style="width: 7%;text-align: center;">状态</th>
                <th style="width: 8%;text-align: center;">操作</th>
                <th style="width: 8%;text-align: center;">策略状态</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="13"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="table_supplier" class="table-responsive">
        <table class="table table-striped table-hover" id="hulkPeriodicPolicy_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 8%;text-align: center;">策略类型</th>
                <th style="width: 10%;text-align: center;">日期范围</th>
                <th style="width: 10%;text-align: center;">时间段</th>
                <th style="width: 6%;text-align: center;">扩容数量</th>
                <th style="width: 6%;text-align: center;">机房</th>
                <th style="width: 7%;text-align: center;">最小实例数</th>
                <th style="width: 7%;text-align: center;">最大实例数</th>
                <th style="width: 8%;text-align: center;">冷却时间(秒)</th>
                <th style="width: 14%;text-align: center;">机器配置(CPU:内存:硬盘)</th>
                <th style="width: 7%;text-align: center;">状态</th>
                <th style="width: 8%;text-align: center;">操作</th>
                <th style="width: 8%;text-align: center;">策略状态</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="12"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
            </tr>
            </tbody>
        </table>
    </div>
    <legend>弹性记录<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
    <div class="form-inline mb20" style="padding-left: 0px;">
        <div class="control-group">
            <label> 开始时间：</label><input id="hulk_start_time" type="text" class="span3" placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label><input id="hulk_end_time" type="text" class="span3" placeholder="查询结束时间">
            <label class="ml20">操作类型：</label>
            <select id="hulk_operatorType" name="hulk_operatorType" title="操作类型">
                <option value="全部">全部</option>
            </select>
            <label class="ml20">机房：</label>
            <select id="hulk_idc" name="hulk_idc" title="机房">
                <option value="全部">全部</option>
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
                        <col width="10%"></col>
                        <col width="15%"></col>
                        <col width="60%"></col>
                    </colgroup>
                    <thead>
                    <tr>
                        <th>时间</th>
                        <th>机房</th>
                        <th>操作</th>
                        <th>详细信息</th>
                    </tr>
                    </thead>
                    <tbody>
                    <td colspan="4">Loading contents...</td>
                    </tbody>
                </table>
                <div id="paghulk_wrapper">
                </div>
            </div>
        </div>
    </div>
</div>
<textarea id="text_updateScalingGroup_dialog" style="display:none">
     <div id="updateScalingGroup_dialog" data-info="<%= Y.JSON.stringify(this.sgAndSpData)%>" class="form-horizontal">
         <legend>更新策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
         <div class="controls">
             <input type="hidden" value="<%= this.sgAndSpData.policyRow.id %>" id="s_update_sp_id">
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_mtype">监控策略类型：</label>
             <div class="controls">
                 <input type="text" id="s_update_sp_mtype" readonly="readonly"
                        value="<%= (this.sgAndSpData.policyRow.monitor.mType == 2?" 平均单机QPS":(this.sgAndSpData.policyRow.monitor.mType == 1?"平均内存利用率":"平均CPU利用率")) %>"/>
                 <span class="tips">不允许修改</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_value">扩容阈值：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.policyRow.monitor.value %>" onkeyup="cky(this)" id="s_update_sp_value"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000){this.value=0;}">
                 <span class="tips">建议：扩容阈值设成单机QPS值<font color="red"><%=this.peakQPS%></font>的60%：<font color="red"><%=this.initValue%></font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_esgNum">扩容数量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.policyRow.esgNum %>" onkeyup="cky(this)" id="s_update_sp_esgNum"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">监控达到扩容阈值时，单次扩容的数量。注：扩容数量不能超过30，如需要请联系hulk</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_monitorValueLower">缩容阈值：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.policyRow.monitor.monitorValueLower %>" onkeyup="cky(this)"
                        id="s_update_sp_monitorValueLower"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000){this.value=0;}">
                 <span class="tips">建议：缩容阈值设成单机QPS值<font color="red"><%=this.peakQPS%></font>的30%：<font color="red"><%=this.initLowerValue%></font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_esgNumScaleIn">缩容数量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.policyRow.esgNumScaleIn %>" onkeyup="cky(this)"
                        id="s_update_sp_esgNumScaleIn"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">监控达到缩容阈值时，单次缩容的数量；缩容数量可稍微调小，少量多次</span>
             </div>
         </div>
         <#--jj更新策略配置中缩容时间段-->
         <div class="control-group"><label class="control-label" for="s_update_sp_noScaleInTime">禁止缩容时间：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.policyRow.monitor.startTime %>-<%= this.sgAndSpData.policyRow.monitor.endTime %>" id="s_update_sp_noScaleInTime"
                        onchange="javascript:if(this.value.length>11){this.value='<%= this.sgAndSpData.policyRow.monitor.startTime %>-<%= this.sgAndSpData.policyRow.monitor.endTime %>';}">
                 <span class="tips">禁止缩容时间段,例如11:00-14:00,间隔不能超过3</span>
             </div>
         </div>
         <legend>更新机房配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
         <div class="control-group"><label class="control-label" for="s_update_sp_idcs">机房：</label>
             <div class="controls">
                 <input type="text" id="s_update_sp_idcs" readonly="readonly"
                        value="<%= this.sgAndSpData.policyRow.idcs %>"/>
                 <span class="tips">监控策略适用的机房，监控值会取选中机房的平均值；扩缩容操作会分别在选中机房执行</span>
             </div>
         </div>
         <div class="controls">
             <input type="hidden" value="<%= this.sgAndSpData.groupRow.id %>" id="s_id">
         </div>
         <div class="control-group"><label class="control-label">最小实例数：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.groupRow.minimumInstance %>" onkeyup="cky(this)"
                        id="s_sg_minimumInstance"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">限定机房保持的最小容器个数；<font color="red">建议最小实例数不要为0</font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">最大实例数：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.groupRow.maximumInstance %>" onkeyup="cky(this)"
                        id="s_sg_maximumInstance"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">限定机房最大可扩容的容器个数。<font color="red">注：最大实例数不能超过50，如需要请联系hulk</font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">冷却时间(秒)：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.groupRow.cooldown %>" onkeyup="cky(this)" id="s_sg_cooldown"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">两次扩缩容操作的最小时间间隔，预防伸缩操作过频繁</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_test_status">CPU配置：</label>
             <div class="controls">
                 <input type="text" id="s_container_cpu" value="<%= this.sgAndSpData.groupRow.cpu %>核"/>
                 <span class="tips">容器CPU配置8核以下可自助调整，大于8核时只允许向下调整</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_test_status">内存配置：</label>
             <div class="controls">
                 <input type="text" id="s_container_mem" value="<%= this.sgAndSpData.groupRow.mem/1024 %>G"/>
                 <span class="tips">容器内存配置12G以下可自助调整，大于12G时只允许向下调整</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_test_status">硬盘配置：</label>
             <div class="controls">
                 <input type="text" id="s_container_disk" value="<%= this.sgAndSpData.groupRow.hd %>G"/>
                 <span class="tips"></span>
             </div>
         </div>
     </div>
</textarea>
<textarea id="text_updatePeriodicScalingGroup_dialog" style="display:none">
     <div id="updatePeriodicGroup_dialog" data-info="<%= Y.JSON.stringify(this.sgAndSpData)%>" class="form-horizontal">
         <legend>更新策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
         <div class="controls">
             <input type="hidden" value="<%= this.sgAndSpData.policyRow.id %>" id="s_update_sp_id">
         </div>
         <div class="control-group"><label class="control-label" for="periodic_update_sp_mtype">策略类型：</label>
             <div class="controls">
                 <input type="text" id="periodic_update_sp_mtype" readonly="readonly"
                        value="周期策略"/>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="periodic_update_date_value">日期范围：</label>
             <div class="controls">
                 <input type="text" id="periodic_policy_start_date" value="<%= this.sgAndSpData.policyRow.schedule.startDay %>"/>~
                 <input type="text" id="periodic_policy_end_date" value="<%= this.sgAndSpData.policyRow.schedule.endDay %>"/>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="periodic_policy_time_value">时间段：</label>
             <div class="controls">
                 <input type="text" id="periodic_policy_start_time" value="<%= this.sgAndSpData.policyRow.schedule.startTime %>"/>~
                 <input type="text" id="periodic_policy_end_time" value="<%= this.sgAndSpData.policyRow.schedule.endTime %>"/>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="periodic_update_sp_esgNumScaleIn">扩容数量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.policyRow.esgNumScaleIn %>" onkeyup="cky(this)"
                        id="periodic_update_sp_esgNum"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
             </div>
         </div>
         <legend>更新机房配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
         <div class="control-group"><label class="control-label" for="periodic_update_sp_idcs">机房：</label>
             <div class="controls">
                 <input type="text" id="periodic_update_sp_idcs" readonly="readonly"
                        value="<%= this.sgAndSpData.policyRow.idcs %>"/>
                 <span class="tips">监控策略适用的机房，监控值会取选中机房的平均值；扩缩容操作会分别在选中机房执行</span>
             </div>
         </div>
         <div class="controls">
             <input type="hidden" value="<%= this.sgAndSpData.groupRow.id %>" id="s_id">
         </div>
         <div class="control-group"><label class="control-label">最小实例数：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.groupRow.minimumInstance %>" onkeyup="cky(this)"
                        id="periodic_sg_minimumInstance"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">限定机房保持的最小容器个数；<font color="red">建议最小实例数不要为0</font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">最大实例数：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.groupRow.maximumInstance %>" onkeyup="cky(this)"
                        id="periodic_sg_maximumInstance"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">限定机房最大可扩容的容器个数。注：最大实例数不能超过50，如需要请联系hulk</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">冷却时间(秒)：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSpData.groupRow.cooldown %>" onkeyup="cky(this)" id="periodic_sg_cooldown"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">两次扩缩容操作的最小时间间隔，预防伸缩操作过频繁</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="periodic_test_status">CPU配置：</label>
             <div class="controls">
                 <input type="text" id="periodic_container_cpu" value="<%= this.sgAndSpData.groupRow.cpu %>核"/>
                 <span class="tips">容器CPU配置8核以下可自助调整，大于8核时只允许向下调整</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="periodic_test_status">内存配置：</label>
             <div class="controls">
                 <input type="text" id="periodic_container_mem" value="<%= this.sgAndSpData.groupRow.mem/1024 %>G"/>
                 <span class="tips">容器内存配置12G以下可自助调整，大于12G时只允许向下调整</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="periodic_test_status">硬盘配置：</label>
             <div class="controls">
                 <input type="text" id="periodic_container_disk" value="<%= this.sgAndSpData.groupRow.hd %>G"/>
                 <span class="tips"></span>
             </div>
         </div>
     </div>
</textarea>
<textarea id="text_addScalingGroup_dialog" style="display:none">
     <div id="addScalingGroup_dialog" data-info="<%= Y.JSON.stringify(this.sgAndSgData)%>" class="form-horizontal">
         <div class="controls">
             <input type="hidden" value="<%= this.sgAndSgData.policyRow.id %>" id="s_update_sp_id">
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_mtype">策略类型：</label>
             <div class="controls">
                 <input type="text" id="s_update_sp_mtype" readonly="readonly"
                        value="<%= (this.sgAndSgData.policyRow.monitor.mType == 2?"平均单机QPS":(this.sgAndSgData.policyRow.monitor.mType == 1?"平均内存利用率":"平均CPU利用率")) %>"/>
                 <span class="tips">监控类型,更新时不允许修改</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_value">扩容阈值：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.policyRow.monitor.value %>" onkeyup="cky(this)" id="s_update_sp_value"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000){this.value=0;}">
                 <span class="tips">QPS阈值可设成近7天单机平均QPS峰值<font color="red"><%=this.peakQPS%></font>的60%:<font color="red"><%=this.initValue%></font>;CPU、内存阈值设成<font color="red">60(%)</font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_esgNum">扩容数量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.policyRow.esgNum %>" onkeyup="cky(this)" id="s_update_sp_esgNum"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">监控达到扩容预设值时，单次扩容的数量。注：扩容数量不能超过30，如需要请联系hulk</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_monitorValueLower">缩容阈值：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.policyRow.monitor.monitorValueLower %>" onkeyup="cky(this)"
                        id="s_update_sp_monitorValueLower"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000){this.value=0;}">
                 <span class="tips">QPS阈值可设成近7天单机平均QPS峰值<font color="red"><%=this.peakQPS%></font>的30%:<font color="red"><%=this.initLowerValue%></font>;CPU、内存阈值设成<font color="red">30(%)</font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_update_sp_esgNumScaleIn">缩容数量：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.policyRow.esgNumScaleIn %>" onkeyup="cky(this)"
                        id="s_update_sp_esgNumScaleIn"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">监控达到缩容预设值时,单次缩容的数量;缩容数量可稍微调小,少量多次</span>
             </div>
         </div>
         <div class="control-group">
             <label class="control-label" for="s_add_sp_idcs" style="margin-right: 20px">适用机房：</label>
             <select id="s_add_sp_idcs" name="s_add_sp_idcs" title="请选择机房" multiple="multiple">
             </select>
             <span class="tips">监控策略适用的机房,监控值会取选中机房的平均值;扩缩容操作会分别在选中机房执行</span>
         </div>
         <div class="controls">
             <input type="hidden" value="<%= this.sgAndSgData.groupRow.id %>" id="s_id">
         </div>
         <div class="control-group"><label class="control-label">机房：</label>
             <div class="controls">
                 <input type="text" readonly="readonly" id="s_idcList" value="<%=this.sgAndSgData.groupRow.idc %>">
                 <span class="tips"></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">最小实例数：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.groupRow.minimumInstance %>" onkeyup="cky(this)"
                        id="s_sg_minimumInstance"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">限定机房保持的最小容器个数;<font color="red">建议最小实例数不要为0</font></span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">最大实例数：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.groupRow.maximumInstance %>" onkeyup="cky(this)"
                        id="s_sg_maximumInstance"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">限定机房最大可扩容的容器个数。注：最大实例数不能超过50，如需要请联系hulk</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label">冷却时间(秒)：</label>
             <div class="controls">
                 <input type="text" value="<%= this.sgAndSgData.groupRow.cooldown %>" onkeyup="cky(this)" id="s_sg_cooldown"
                        onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                 <span class="tips">两次扩缩容操作的最小时间间隔,预防伸缩操作过频繁</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_test_status">CPU配置(核)：</label>
             <div class="controls">
                 <input type="text" id="s_container_cpu" value="<%= this.sgAndSgData.groupRow.cpu %>核"/>
                 <span class="tips">容器CPU配置>8核时需要审核,建议申请8核以下配置</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_test_status">内存配置(G)：</label>
             <div class="controls">
                 <input type="text" id="s_container_mem" value="<%= this.sgAndSgData.groupRow.mem/1024 %>G"/>
                 <span class="tips">容器内存配置>12G时需要审核,建议申请12G以下配置</span>
             </div>
         </div>
         <div class="control-group"><label class="control-label" for="s_test_status">硬盘配置(G)：</label>
             <div class="controls">
                 <input type="text" id="s_container_disk" value="<%= this.sgAndSgData.groupRow.hd %>G"/>
                 <span class="tips"></span>
             </div>
         </div>
     </div>
</textarea>
<textarea id="text_addNewScalingPolicy_form" style="display:none">
    <div id="addNewScalingPolicy_form" class="form-horizontal">
        <legend>新建策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="control-group"><label class="control-label" for="s_sp_mtype">监控策略类型：</label>
            <div class="controls">
                <input type="text" readonly="readonly" id="s_sp_mtype" value="<%= this.initMtype %>"/>
                <span class="tips">目前只支持平均单机QPS</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sp_value">扩容阈值：</label>
            <div class="controls">
                <input type="text" value="<%= this.initValue %>" onkeyup="cky(this)" id="s_sp_value"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000){this.value=0;}">
                <span class="tips">建议：扩容阈值可设成单机平均QPS值<font color="red"><%=this.peakQPS%></font>的60%：<font color="red"><%=this.initValue%></font></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sp_esgNum">扩容数量：</label>
            <div class="controls">
                <input type="text" value="2" onkeyup="cky(this)" id="s_sp_esgNum"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">监控达到扩容阈值时，单次扩容的数量。注：扩容数量不能超过30，如需要请联系hulk</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sp_monitorValueLower">缩容阈值：</label>
            <div class="controls">
                <input type="text" value="<%= this.initLowerValue %>" onkeyup="cky(this)" id="s_sp_monitorValueLower"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>100000){this.value=0;}">
                <span class="tips">建议：缩容阈值可设成单机平均QPS值<font color="red"><%=this.peakQPS%></font>的30%：<font color="red"><%=this.initLowerValue%></font></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sp_esgNumScaleIn">缩容数量：</label>
            <div class="controls">
                <input type="text" value="2" onkeyup="cky(this)" id="s_sp_esgNumScaleIn"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">监控达到缩容阈值时，单次缩容的数量；缩容数量可稍微调小，少量多次</span>
            </div>
        </div>
        <#--增加禁止缩容字段jj-->
        <div class="control-group"><label class="control-label" for="s_sp_noScaleInTime">禁止缩容时间：</label>
            <div class="controls">
                <input type="text" value="00:00-00:00" id="s_sp_noScaleInTime"
                       onchange="javascript:if(this.value.length>11){this.value='00:00-00:00';}">
                <span class="tips">禁止缩容时间段,例如11:00-14:00,间隔不能超过3</span>
            </div>
        </div>
        <legend>新建机房配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="control-group"><label class="control-label" for="s_sp_idcs" style="margin-right: 20px">机房：</label>
            <input type="text" id="s_sp_idcs" value="请选择机房"/>
        <#--<select id="s_sp_idcs" name="s_sp_idcs" title="请选择机房" multiple="multiple">-->
        <#--</select>-->
            <span class="tips">&nbsp监控策略适用的机房，监控值会取选中机房的平均值；扩缩容操作会分别在选中机房执行</span>
        </div>
        <div class="control-group"><label class="control-label" for="s_sg_minimumInstance">最小实例数：</label>
            <div class="controls">
                <input type="text" value="1" onkeyup="cky(this)"
                       id="s_sg_minimumInstance"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">限定机房保持的最小容器个数；<font color="red">建议最小实例数不要为0</font></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sg_maximumInstance">最大实例数：</label>
            <div class="controls">
                <input type="text" value="20" onkeyup="cky(this)"
                       id="s_sg_maximumInstance"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">限定机房最大可扩容的容器个数。注：最大实例数不能超过50，如需要请联系hulk</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_sg_cooldown">冷却时间(秒)：</label>
            <div class="controls">
                <input type="text" value="120" onkeyup="cky(this)" id="s_sg_cooldown"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">两次扩缩容操作的最小时间间隔，预防伸缩操作过频繁</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_container_cpu">CPU配置：</label>
            <div class="controls">
                <input type="text" id="s_container_cpu" value="1核"/>
                <span class="tips">容器CPU配置8核以下可自助调整，大于8核时只允许向下调整</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_container_mem">内存配置：</label>
            <div class="controls">
                <input type="text" id="s_container_mem" value="1G"/>
                <span class="tips">容器内存配置12G以下可自助调整，大于12G时只允许向下调整</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_container_disk">硬盘配置：</label>
            <div class="controls">
                <input type="text" id="s_container_disk" value="50G"/>
                <span class="tips"></span>
            </div>
        </div>
    </div>
</textarea>
<textarea id="text_modifySgGroup_form" style="display:none">
    <div id="modifySgGroup_form" class="form-horizontal">
    </div>
</textarea>
<textarea id="text_addNewPeriodicPolicy_form" style="display:none">
    <div id="addNewPeriodicPolicy_form" class="form-horizontal">
        <legend>新建策略配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="control-group"><label class="control-label" for="periodic_policy_date_value">日期范围：</label>
            <div class="controls">
                <input type="text" id="periodic_policy_start_date" value="星期一"/>～<input type="text" id="periodic_policy_end_date" value="星期日"/>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="periodic_policy_time_value">时间段（时）</时间段>：</label>
            <div class="controls">
                <input type="text" id="periodic_policy_start_time" value="0:00"/>～<input type="text" id="periodic_policy_end_time" value="0:00"/> <span class="tips">&nbspxx:xx格式</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="periodic_policy_scaleNum_value">扩容数量：</label>
            <div class="controls">
                <input type="text" value="2" onkeyup="cky(this)" id="periodic_policy_scaleNum_value" onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
            </div>
        </div>
        <legend>新建机房配置<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
        <div class="control-group"><label class="control-label" for="periodic_sp_idcs" style="margin-right: 20px">机房：</label>
            <input type="text" id="periodic_sp_idcs" value="请选择机房"/>
            <span class="tips">&nbsp监控策略适用的机房，监控值会取选中机房的平均值；扩缩容操作会分别在选中机房执行</span>
        </div>
        <div class="control-group"><label class="control-label" for="periodic_sg_minimumInstance">最小实例数：</label>
            <div class="controls">
                <input type="text" value="1" onkeyup="cky(this)"
                       id="periodic_sg_minimumInstance"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">限定机房保持的最小容器个数；<font color="red">建议最小实例数不要为0</font></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="periodic_sg_maximumInstance">最大实例数：</label>
            <div class="controls">
                <input type="text" value="20" onkeyup="cky(this)"
                       id="periodic_sg_maximumInstance"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">限定机房最大可扩容的容器个数。注：最大实例数不能超过50，如需要请联系hulk</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="periodic_sg_cooldown">冷却时间(秒)：</label>
            <div class="controls">
                <input type="text" value="120" onkeyup="cky(this)" id="periodic_sg_cooldown"
                       onchange="javascript:this.value=this.value.replace(/[^\d]/g,'');if(this.value<0){this.value=0;}if(this.value>5000){this.value=0;}">
                <span class="tips">两次扩缩容操作的最小时间间隔，预防伸缩操作过频繁</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_container_cpu">CPU配置：</label>
            <div class="controls">
                <input type="text" id="periodic_container_cpu" value="1核"/>
                <span class="tips">容器CPU配置8核以下可自助调整，大于8核时只允许向下调整</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_container_mem">内存配置：</label>
            <div class="controls">
                <input type="text" id="periodic_container_mem" value="1G"/>
                <span class="tips">容器内存配置12G以下可自助调整，大于12G时只允许向下调整</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_container_disk">硬盘配置：</label>
            <div class="controls">
                <input type="text" id="periodic_container_disk" value="50G"/>
                <span class="tips"></span>
            </div>
        </div>
    </div>
</textarea>
<#--添加页面修改sjj-->
<textarea id="text_addModifySgGroup_form" style="display:none">
    <div id="addModifySgGroup_form" class="form-horizontal">
        <div class="control-group" style="width:100%;"><label class="control-label" for="periodic_policy_scaleNum_value">服务名称：</label>
            <input style="width:70%;" id="appkey" class="periodic_sp_idcs" name="appkey" value="<%=this.appkey%>" readonly="readonly"/>
        </div>
        <legend>所有机房<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
    <#--加一个table-->
        <div id="div_sg" class="sjjzs table-responsive">
            <table class="table table-striped table-hover" id="modifySgGroup_table">
                <thead>
                <tr>
                    <th hidden>id</th>
                    <th style="width: 6%;text-align:center;">机房名称</th>
                    <th style="width: 10%;text-align:center;">实例最大上限</th>
                    <th style="width: 10%;text-align:center;">CPU最大上限</th>
                    <th style="width: 10%;text-align:center;">内存最大上限</th>
                    <th style="width: 10%;text-align:center;">硬盘最大上限</th>
                    <th style="width: 6%;text-align:center;">操作</th>
                </tr>
                </thead>
                <tbody id="tbody_sg">
                <tr>
                    <td colspan="13"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
                </tr>
                </tbody>
                <tbody id="tbody_sg_not">
                <tr>
                    <td colspan="13"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
                </tr>
                </tbody>
            </table>
        </div>
</textarea>
<textarea id="idc_info" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr id="tr" data-info="<%= Y.JSON.stringify(item) %>">
            <td id="hidden_id" hidden><input class="label" id="somethingIdc_<%=item.id%>" value="<%=item.idc%>"/></td>
            <td style="width: 6%;text-align:center;" id="nothingIdc_<%=item.id%>"><%=item.idc%></td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumInstance_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.maximumInstance%>"/></td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumCpu_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.cpu%>"/>核</td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumMem_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.mem/1024%>"/>G</td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumHd_<%=item.id%>" style="width: 30px;text-align:center;" value="<%= item.hd%>"/>G</td>
            <td id="td_update" style="width: 6%;text-align:center;"><button class="btn btn-primary" id="updateBtn_<%=item.id%>">更新</button></td>
        </tr>
        <% }); %>
</textarea>
<textarea id="idc_info_not" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr id="tr" data-info="<%= Y.JSON.stringify(item) %>">
            <td id="hidden_id" hidden><input class="label" id="somethingIdc_<%=item.id%>" value="<%=item.idc%>"/></td>
            <td style="width: 6%;text-align:center;color: red;" id="nothingIdc_<%=item.id%>"><%=item.idc%></td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumInstance_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.maximumInstance%>" readonly="readonly"/></td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumCpu_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.cpu%>" readonly="readonly"/>核</td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumMem_<%=item.id%>" style="width: 20px;text-align:center;" value="<%= item.mem/1024%>" readonly="readonly"/>G</td>
            <td style="width: 6%;text-align:center;"><input id="inputMaxmumHd_<%=item.id%>" style="width: 30px;text-align:center;" value="<%= item.hd%>" readonly="readonly"/>G</td>
            <td id="td_create" style="width: 6%;text-align:center;"><button class="btn btn-primary" id="updateBtn_<%=item.id%>">创建</button></td>
        </tr>
        <% }); %>
</textarea>

<textarea id="text_spandsp_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td hidden> <%= item.policyRow.id %></td>
            <td style="text-align:center;"> <%= (item.policyRow.monitor.mType == 2?"平均单机QPS":(item.policyRow.monitor.mType == 1?"平均内存利用率":"平均CPU利用率")) %></td>
            <td style="text-align:center;"> <%= item.policyRow.monitor.value %></td>
            <td style="text-align:center;"><%= item.policyRow.esgNum %></td>
            <td style="text-align:center;"> <%= item.policyRow.monitor.monitorValueLower %></td>
            <td style="text-align:center;"> <%= item.policyRow.esgNumScaleIn %></td>
            <td style="text-align:center;"> <%= item.groupRow.idc %></td>
            <td style="text-align:center;"> <%= item.groupRow.minimumInstance %></td>
            <td style="text-align:center;"> <%= item.groupRow.maximumInstance %></td>
            <td style="text-align:center;"> <%= item.policyRow.monitor.startTime %> - <%= item.policyRow.monitor.endTime %></td>
            <td style="text-align:center;"> <%= item.groupRow.cpu %>核:<%= item.groupRow.mem/1024 %>G:<%= item.groupRow.hd %>G</td>
            <td style="text-align:center;"> <%= item.policyRow.state==0? "运行中":"停止" %></td>
            <td style="text-align:center;">
                <a id="updateScalingGroup" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>编辑</span> </a>
            </td>
            <td>
                <% if( item.policyRow.fakeFlag == 0 ){ %>
                <div id="hulk-policy-one-enabled" class="btn-group btn-enabled">
                    <button data-enabled="0" class="btn btn-mini btn-alive <%= item.policyRow.state==0?" active":"" %>">启用</button>
                    <button data-enabled="1" class="btn btn-mini btn-dead <%= item.policyRow.state==1?" active":"" %>">停用</button>
                </div>
                <% } else { %>
                未创建
                <% } %>
            </td>
        </tr>
        <% }); %>
</textarea>
<textarea id="text_spandPeriodicsp_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td hidden> <%= item.policyRow.id %></td>
            <td style="text-align: center;">周期策略</td>
            <td style="text-align: center;"> <%= item.policyRow.schedule.startDay %>~<%= item.policyRow.schedule.endDay %></td>
            <td style="text-align: center;"> <%= item.policyRow.schedule.startTime %>~<%= item.policyRow.schedule.endTime %></td>
            <td style="text-align: center;"><%= item.policyRow.esgNum %></td>
            <td style="text-align: center;"> <%= item.groupRow.idc %></td>
            <td style="text-align: center;"> <%= item.groupRow.minimumInstance %></td>
            <td style="text-align: center;"> <%= item.groupRow.maximumInstance %></td>
            <td style="text-align:center;"> <%= item.groupRow.cooldown%></td>
            <td style="text-align: center;"> <%= item.groupRow.cpu %>核:<%= item.groupRow.mem/1024 %>G:<%= item.groupRow.hd %>G</td>
            <td style="text-align: center;"> <%= item.policyRow.state==0? "运行中":"停止" %></td>
            <td style="text-align: center;">
                <a id="updatePeriodicScalingGroup" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>编辑</span> </a>
            </td>
            <td>
                <% if( item.policyRow.fakeFlag == 0 ){ %>
                <div id="hulk-periodic-policy-one-enabled" class="btn-group btn-enabled">
                    <button data-enabled="0" class="btn btn-mini btn-alive <%= item.policyRow.state==0?" active":"" %>">启用</button>
                    <button data-enabled="1" class="btn btn-mini btn-dead <%= item.policyRow.state==1?" active":"" %>">停用</button>
                </div>
                <% } else { %>
                未创建
                <% } %>
            </td>
        </tr>
        <% }); %>
</textarea>
<script type="text/javascript">
    M.use('msgp-serviceopt/optHulkPolicy-version0.0.3', function (Y) {
        var appkey = '${appkey}';
    });
    function cky(obj) {
        var t = obj.value.replace(/[^\d]/g, '');
        if (obj.value != t) obj.value = t;
        if (obj.value < 0) obj.value = 0;
        if (obj.value > 100000000) obj.value = 0;
    }
</script>