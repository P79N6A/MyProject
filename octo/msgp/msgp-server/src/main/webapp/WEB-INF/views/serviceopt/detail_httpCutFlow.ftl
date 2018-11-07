<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<div id ="cut_flow_loading" class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div id ="cut_flow_content" class="content-body" style="display:none;">
    <div class="form-inline mb20">
        <button id="refreshHttpCutFlow" type="button" class="btn btn-primary" title="刷新列表">
            <i class="fa fa-refresh"></i>
        </button>
        <button id="addHttpCutFlow" type="button" class="btn btn-primary" title="新增截流配置">
            <i class="fa fa-plus">新增</i>
        </button>
    <#include "/common/env.ftl" >
        <div id="httpCutFlow_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
        </div>

        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28354886" target="_blank">
                HTTP截流使用说明书<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
    <div class="form-inline mb20">
    </div>

    <div id="table_supplier" class="table-responsive">
        <table class="table table-striped table-hover" id="httpCutFlow_table">
            <colgroup>
                <col width="25%"></col>
                <col width="25%"></col>
                <col width="10%"></col>
                <col width="15%"></col>
                <col width="25%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>接口URL</th>
                <th>所属域名</th>
                <th>截流比例</th>
                <th>是否启用</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
        <div id="paginator_cutFlow">
    </div>
    <hr />
    <div id="http_cutflow_tips">
        <b>注意事项</b>
        <p/>
        <p>1.截流策略启用后, 会立即在服务端生效, 如发现服务恢复后请及时停用或删除策略;</p>
        <p>2.使用过程中有问题或对配置存在疑惑, 请联系周峰(zhoufeng04)。</p>
    </div>

</div>

<textarea id="text_add_httpProviderCutFlow_form" style="display:none">
    <div id="add_httpProviderCutFlow_form" class="form-horizontal">
        <div class="control-group"><label class="control-label" for="s_server_name">服务域名：</label>
            <div class="controls">
                <select id="s_server_name" name="s_server_name" title="s_server_name" style="width: 220px;" disabled="disabled">
                    <option value="无数据">无数据</option>
                </select>
                <span class="tips" >当前接口所属的域名</span><span style="padding-left: 20px; color: #CD5F15">需要添加域名接口映射信息? 点击前往<a href= "${hostUrl}/serverOpt/operation?appkey=${appkey}#httpConfig" target="_blank">HTTP设置-域名映射</a></span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_method_url">接口URL：</label>
            <div class="controls">
                <select id="s_method_url" name="s_method_url" title="s_method_url" style="width: 220px;" disabled="disabled">
                    <option value="无数据">无数据</option>
                </select>
                <span class="tips" >需要截流的接口url</span>
            </div>
        </div>

        <div class="control-group"><label class="control-label" for="s_closure_percent">截流百分比：</label>
            <div class="controls">
                <input type="text" value="0" onkeyup="cky(this)" id="s_closure_percent" >
                <span class="tips" >范围【0~100】其中100表示接口流量全部drop，0表示不做任何截流操作</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_closure_status">是否启用：</label>
            <div class="controls">
                <select id="s_closure_status">
                    <option value="0">启用</option>
                    <option value="1" selected="selected">停用</option>
                </select>
                <span class="tips" >启用时会按照上面配置的比例实际生效，停用时不进行实际的截流操作</span>
            </div>
        </div>
    </div>
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
                <button data-enabled="0" class="btn btn-mini btn-alive <%= (item.closureStatus==0 || !item.closureStatus)?"active":"" %>">启用</button>
                <button data-enabled="1" class="btn btn-mini btn-dead <%= item.closureStatus==1?"active":"" %>">停用</button>
            </div>
        </td>
        <!--td style="width: 10%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= 1 %></td-->
        <td>
            <a id="editHttpCutFlow" href="javascript:void(0);"  class="config-panel-edit editHttpCutFlow"> &nbsp;&nbsp;<i class="fa fa-edit"></i> <span>编辑</span> </a>
            <a id="delHttpCutFlow" href="javascript:void(0);" class="config-panel-delete delHttpCutFlow"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span >删除</span> </a>
        </td>
    </tr>
<% }); %>
<% } %>
</textarea>
<script type="text/javascript">
    function cky(obj) {
        var t = obj.value.replace(/[^\d]/g,'');
        if(obj.value!=t) obj.value=t;
        if(obj.value < 0) obj.value=0;
        if(obj.value > 100000000) obj.value=0;
    }
</script>