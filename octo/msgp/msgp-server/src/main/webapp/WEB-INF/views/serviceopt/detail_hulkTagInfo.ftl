<div id="table_supplier_group" class="table-responsive" style="display:none;">
    <span id="create_tag_info" style="color: #00aaee;float: right;">+高级分组</span>
    <span id="fast_create_tag_info" style="color: #00aaee;float: right;">+普通分组&nbsp;&nbsp;</span>
    <a style="float: right;" href="https://123.sankuai.com/km/page/61240348" target="_blank"><i class="fa fa-question-circle">&nbsp;&nbsp;</i></a>
    <table class="table table-striped table-hover" id="hulkUserGroup_table">
        <thead>
        <tr>
            <th hidden>id</th>
            <th style="width: 10%;text-align: center;">分组编号</th>
            <th style="width: 35%;text-align: center;">名称</th>
            <th style="width: 10%;text-align: center;">区域</th>
            <th style="width: 10%;text-align: center;">机房</th>
            <th style="width: 10%;text-align: center;">SET标识</th>
            <th style="width: 10%;text-align: center;">泳道</th>
            <th style="width: 15%;text-align: center;">操作</th>
        </tr>
        </thead>
        <tbody id="tag_info_table_tbody">
        <tr>
            <td colspan="12"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i></td>
        </tr>
        </tbody>
    </table>
</div>

<#--更新分组记录弹出页面-->
<textarea id="text_updateUserSelfGroup_dialog" style="display:none">
     <div id="updateUserSelfGroup_form" class="form-horizontal">
         <div class="control-group" style="width:100%;">
             <table class="table table-borderless table-striped table-hover">
                <tbody>
                    <tr>
                        <th style="width: 40%;text-align: right;">名称</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="updatetag_id" type="hidden"/>
                            <input id="updatetag_tagName_input" type="text" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr>
                        <th style="width: 40%;text-align: right;">环境</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="updatetag_env_input" type="text" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr>
                        <th style="width: 40%;text-align: right;">地域</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="updatetag_region_input" type="text" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr>
                        <th style="width: 40%;text-align: right;">机房</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="updatetag_idc_input" type="text" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr id="updatetag_cell_tr">
                        <th style="width: 40%;text-align: right;">SET标识</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="updatetag_cell_input" type="text" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr id="updatetag_swimlane_tr">
                        <th style="width: 40%;text-align: right;">泳道</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="updatetag_swimlane_input" type="text" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*提示:线上环境无泳道</span>
                        </th>
                    </tr>
                </tbody>
        </table>
         </div>
     </div>
</textarea>

<textarea id="text_user_group_form" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr id="tr" data-info="<%= Y.JSON.stringify(item) %>">
            <td style="width: 45%;text-align:center;"><%=item.tagName%></td>
            <td style="width: 10%;text-align:center;"><%=item.regionName%></td>
            <td style="width: 10%;text-align:center;"><%=item.idcName%></td>
            <td style="width: 10%;text-align:center;"><%=item.cell%></td>
            <td style="width: 10%;text-align:center;"><%=item.swimlane%></td>
            <td id="td_update" style="width: 15%;text-align:center;"><input class="" id="one_click_for_group" name="user_group_checkbox" type="checkbox" value="<%=item.id%>" <%=item.flag%>></td>
        </tr>
        <% }); %>
</textarea>
<textarea id="text_update_user_group_form" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr id="tr" data-info="<%= Y.JSON.stringify(item)%>">
            <td style="width: 45%;text-align:center;"><%=item.tagName%></td>
            <td style="width: 10%;text-align:center;"><%=item.regionName%></td>
            <td style="width: 10%;text-align:center;"><%=item.idcName%></td>
            <td style="width: 10%;text-align:center;"><%=item.cell%></td>
            <td style="width: 10%;text-align:center;"><%=item.swimlane%></td>
            <td id="up_td_update" name="up_td_update" style="width: 15%;text-align:center;"><input id="one_click_for_group_<%=item.id%>" value="<%=item.id%>" name="user_group_td_" type="checkbox" <%=item.flag%>></td>
        </tr>
        <% }); %>
</textarea>
<textarea id="text_modifySgGroup_form" style="display:none">
    <div id="modifySgGroup_form" class="form-horizontal">
    </div>
</textarea>

<#--新增分组-->
<textarea id="text_add_tag_info" style="display:none">
    <div class="form-horizontal">
        <div class="control-group" style="width:100%;">
            <table class="table table-borderless table-striped table-hover">
                <tbody>
                    <tr>
                        <th style="width: 40%;text-align: right;">名称</th>
                        <th style="width: 60%;text-align: left;"><input id="tag_tagName" type="text" value="" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr>
                        <th style="width: 40%;text-align: right;">环境</th>
                        <th style="width: 60%;text-align: left;">
                            <input type="text" id="tag_env_input" style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr>
                        <th style="width: 40%;text-align: right;">区域</th>
                        <th style="width: 60%;text-align: left;">
                            <select id="tag_region_select" style="width: 200px;">
                                <option value="bj" selected>北京侧</option>
                                <option value="sh">上海侧</option>
                            </select>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr>
                        <th style="width: 40%;text-align: right;">机房</th>
                        <th style="width: 60%;text-align: left;">
                            <select id="tag_idc_select" style="width: 200px;">
                                <option value="" selected="selected">请选择机房</option>
                                <% Y.Array.each(this.data.idcList, function( item, index ){ %>
                                <option value="<%= item.idc %>"><%= item.name %></option>
                                <% }); %>
                            </select>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr id="tag_cell_tr">
                        <th style="width: 40%;text-align: right;">
                            <a href="https://123.sankuai.com/km/page/60968019" target="_blank">&nbsp;<i class="fa fa-question-circle"></i>&nbsp;&nbsp;</a>
                            <input id="tag_cell_checkbox" type="checkbox" style="margin-bottom: 6px;margin-right: 2px;"/>
                            <span>SET标识</span>
                        </th>
                        <th style="width: 60%;text-align: left;display:none" id="tag_cell_th">
                            <select id="tag_cell_select" style="width: 200px;">
                                <option value="" selected="selected">选择SET标识</option>
                                <% Y.Array.each(this.data.cellList, function( item, index ){ %>
                                <option value="<%= item.name %>"><%= item.name %></option>
                                <% }); %>
                            </select>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*没有则不用填写</span>
                        </th>
                    </tr>
                    <tr id="tag_swimlane_tr">
                        <th style="width: 40%;text-align: right;">泳道</th>
                        <th style="width: 60%;text-align: left;">
                            <input type="text" id="tag_swimlane_input" value=""  style="width: 185px;"/>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*线上环境无泳道</span>
                        </th>
                    </tr>
                </tbody>
        </table>
            <div style="width: 100%;margin-top: 15px;">
                <button id="btn_cancel_group" class="btn btn-default commonDialog-btn-unpass" style="float: right;margin-right: 15px;">取消</button>
                <button id="btn_add_group" class="btn btn-default btn-primary" style="float: right;margin-right: 15px;">添加</button>
            </div>
        </div>
    </div>
</textarea>

<#--快速新增分组-->
<textarea id="text_fast_add_tag_info" style="display:none">
    <div class="form-horizontal">
        <div class="control-group" style="width:100%;">
            <table class="table table-borderless table-striped table-hover">
                <tbody>
                    <tr id="fast_tag_global_tr">
                        <th style="width: 40%;text-align: right;">全局模式</th>
                        <th style="width: 60%;text-align: left;">
                            <input id="fast_global_true_radio" type="radio" name="fast_global" for="fast_global_true" value="true"/>
                            <span id="fast_global_true">是</span>
                            <input id="fast_global_false_radio" type="radio" name="fast_global" for="fast_global_false" value="false" checked/>
                            <span id="fast_global_false">否</span>
                            <span class="tips" style="color: #00aaee;font-size: x-small;">*</span>
                        </th>
                    </tr>
                    <tr id="fast_tag_region_tr">
                        <th style="width: 40%;text-align: right;">区域</th>
                        <th style="width: 60%;text-align: left;">
                            <select id="fast_tag_region_select" style="width: 200px;">
                                <option value="" selected>全部</option>
                                <option value="bj">北京侧</option>
                                <option value="sh">上海侧</option>
                            </select>
                            <span class="tips" style="color: #00aaee;font-size: x-small;"></span>
                        </th>
                    </tr>
                    <tr id="fast_tag_idc_tr">
                        <th style="width: 40%;text-align: right;">机房</th>
                        <th style="width: 60%;text-align: left;" id="fast_tag_idc_th">
                                <% Y.Array.each(this.data, function( item, index ){ %>
                                <input type="checkbox" name="fast_tag_idc" value="<%= item.idc %>" for="fast_<%= item.idc %>"/>
                                <span id="fast_<%= item.idc %>"><%= item.name %></span>
                                <% }); %>
                        </th>
                    </tr>
                </tbody>
        </table>
            <div style="width: 100%;margin-top: 15px;">
                <button id="btn_fast_cancel_group" class="btn btn-default commonDialog-btn-unpass" style="float: right;margin-right: 15px;">取消</button>
                <button id="btn_fast_add_group" class="btn btn-default btn-primary" style="float: right;margin-right: 15px;">添加</button>
            </div>
        </div>
    </div>
</textarea>

<textarea id="tag_info_idc_list" style="display:none">
    <option value="" selected="selected">请选择机房</option>
    <% Y.Array.each(this.data, function( item, index ){ %>
          <option value="<%= item.idc %>"><%= item.name %></option>
    <% }); %>
</textarea>

<textarea id="fast_tag_info_idc_list" style="display:none">
   <% Y.Array.each(this.data, function( item, index ){ %>
       <input type="checkbox" name="fast_tag_idc" value="<%= item.idc %>" for="fast_<%= item.idc %>"/>
       <span id="fast_<%= item.idc %>"><%= item.name %></span>
    <% }); %>
</textarea>

<#--<当前策略,当前环境,用户已进行的分组>-->
<textarea id="text_spandUsergp_template" style="display:none">
    <% Y.Array.each(this.data, function( item, index ){ %>
        <tr data-info="<%= Y.JSON.stringify(item) %>">
            <td hidden> <%= item.id %></td>
            <td style="text-align: center;"> <%= item.tag_id_fake %></td>
            <td style="text-align: center;"> <%= item.tagName %></td>
            <td style="text-align: center;"> <%= item.regionName %></td>
            <td style="text-align: center;"> <%= item.idcName %></td>
            <td style="text-align: center;"> <%= item.cell %></td>
            <td style="text-align: center;"> <%= item.swimlane %></td>
            <td style="text-align: center;">
                <a id="updateUserSelfGroup_<%= item.index %>" href="javascript:void(0);" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-edit"></i> <span>编辑</span> </a>
                <a id="removeTagInfo_<%= item.index %>" href="javascript:void(0);" title="删除操作不可恢复，请谨慎！" class="config-panel-edit"> &nbsp;&nbsp;<i
                        class="fa fa-recycle"></i> <span>删除</span> </a>
            </td>
        </tr>
        <% }); %>
</textarea>