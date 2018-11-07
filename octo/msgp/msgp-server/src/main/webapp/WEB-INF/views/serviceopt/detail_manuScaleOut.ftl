<div id="manuScale_content">
    <div>
    <#include "/common/env.ftl" >
        <div id="manScale_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
        <#--<a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>-->
        </div>
        <a style="padding-left:2.0em"></a>
    <#--<button id="refreshmannuscale" type="button" class="btn btn-primary" title="刷新列表">-->
    <#--<i class="fa fa-refresh">刷新</i>-->
    <#--</button>-->
    </div>
    <table><tr><td style="width:100%;height:14px;font-size:14px;border:0"></td></tr></table>
    <legend>实例列表<span  style="font-size: 10px; margin-left: 10px; color: grey;"></span>
        <button id="scale_out" type="button" class="btn" title="一键扩容"><i class="fa fa-plus"> 扩容</i></span></button>
        <button id="scale_delete" style="padding-left: 20px;" type="button" class="btn" title="删除"><i class="fa fa-trash-o"> 缩容</i></button>
        <a href="https://123.sankuai.com/km/page/59694646" target="_blank">&nbsp<i class="fa fa-question-circle"></i></a>
        <span style="color: red;font-size: 0.8em;">!!!上海侧服务使用一键扩容,可能存在问题</span>
    </legend>
    <div id="table_supplier" class="table-responsive">
        <table class="table table-striped table-hover" id="ScalingGroupAndRunningSet_table">
            <thead>
            <tr>
                <th hidden>id</th>
                <th style="width: 5%;"><input id="all-check" type="checkbox"></th>
                <th style="width: 20%;text-align: left;">主机名</th>
                <th style="width: 20%;text-align: left;">IP</th>
                <th style="width: 15%;text-align: left;">来源</th>
                <th style="width: 10%;text-align: left;">SET</th>
                <th style="width: 10%;text-align: left;">HULK类型</th>
                <th style="width: 20%;text-align: left;">创建时间</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="4"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
            </tr>
            </tbody>
        </table>
    </div>
    <legend>历史事件<span style="font-size: 10px; margin-left: 10px; color: grey;"></span></legend>
    <div class="form-inline mb20" style="padding-left: 0px;">
        <div class="control-group">
            <label> 开始时间：</label><input id="manuscale_start_time" type="text" class="span3" placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label><input id="manuscale_end_time" type="text" class="span3" placeholder="查询结束时间">
            <label class="ml20">操作类型：</label>
            <select id="manu_operatorType" name="manu_operatorType" title="操作类型">
                <option value="选择全部">选择全部</option>
            </select>
        </div>
        <div id="manuscale_log_wrap">
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
                        <th>操作人员</th>
                        <th>操作描述</th>
                        <th>详细信息</th>
                    </tr>
                    </thead>
                    <tbody>
                    <td colspan="4">Loading contents...</td>
                    </tbody>
                </table>
                <div id="manuScale_wrapper">
                </div>
            </div>
        </div>
    </div>
</div>
<textarea id="text_scaleout_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>" >
            <td hidden> <%= item.sgRowId %></td>
            <td><input id="one-checkbox" type="checkbox"></td>
            <td> <%= item.setName%></td>
            <td> <%= item.ip %></td>
            <td> <%= item.origin %></td>
            <% if(item.set == null || item.set == "") {%>
            <td>NULL</td>
            <%}else{%>
            <td><%=item.set%></td>
            <%}%>
            <td><%=item.setType%>.0</td>
        <#--<% if(item.nameId < 10) {%>-->
        <#--<td> <%= item.namePrefix%>0<%=item.nameId%></td>-->
        <#--<%}else{%>-->
        <#--<td> <%= item.namePrefix%><%=item.nameId%></td>-->
        <#--<%}%>-->
            <td> <%= item.time %></td>
        </tr>
        <% }); %>
</textarea>
<script type="text/javascript">
    M.use('msgp-serviceopt/optManuScaleOut', function (Y) {
        var appkey = '${appkey}';
    });
    function cky(obj) {
        var t = obj.value.replace(/[^\d]/g, '');
        if (obj.value != t) obj.value = t;
        if (obj.value < 0) obj.value = 0;
        if (obj.value > 100000000) obj.value = 0;
    }
</script>