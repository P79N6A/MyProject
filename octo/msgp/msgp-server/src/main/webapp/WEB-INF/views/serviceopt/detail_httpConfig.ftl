<div class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="content-body">
    <div class="form-inline mb20">
        <#include "/common/env.ftl" >
        <div id="httpConfig_env_select" class="btn-group">
            <a value="3" id="httpConfig_prod"  type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" id="httpConfig_stage" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <#if isOffline>
                <a value="1" id="httpConfig_test"  type="button" class="btn btn-default" href="javascript:void(0)">test</a>
            </#if>
        </div>

        <a style="padding-left:1em">配置项：</a>
        <div id="httpConfig_configType_select" class="btn-group">
            <a value="0" id="httpConfig_healthCheck_page" type="button" class="btn btn-primary btn-http-config-page"
               href="javascript:void(0)">健康检查</a>
            <a value="1" id="httpConfig_loadBalance_page" type="button" class="btn btn-default btn-http-config-page"
               href="javascript:void(0)">负载均衡</a>
            <a value="2" id="httpConfig_domain_page" type="button" class="btn btn-default btn-http-config-page"
               href="javascript:void(0)">域名映射</a>
            <a value="3" id="httpConfig_slowStart_page" type="button" class="btn btn-default btn-http-config-page"
               href="javascript:void(0)">慢启动</a>

        </div>
    </div>

    <div id="httpConfig_healthCheck_wrap">
        <div class="form-horizontal">
            <div class="control-group" style="margin-top: 30px;  margin-bottom: 20px; ">
                <label class="control-label" style="width: 125px; padding-top: 0px;">状态：</label>
                <div id="healthCheck_switch_content" class="controls" style="margin-left: 125px">
                    <input id="healthCheck_switch_on" value="1" type="radio" checked  style="vertical-align: top"/>
                    <span >&nbsp;开启&nbsp;&nbsp;</span>
                    <input id="healthCheck_switch_off" value="0" type="radio" style="vertical-align: top"/>
                    <span >&nbsp;关闭&nbsp;&nbsp;</span>
                </div>
            </div>
            <div class="control-group" style="margin-bottom: 20px;">
                <label class="control-label" style="width: 125px; padding-top: 0px;">健康检查URL：</label>
                <div id="healthCheck_strategy" class="controls" style="margin-left: 125px;">
                    <input id="radio_default_uniform" type="radio" name="options" checked="checked" value="defaultUniform" style="margin-bottom: 25px; vertical-align: top">
                    &nbsp;统一规范:&nbsp;&nbsp;
                    "GET&nbsp;
                    <input id="text_default_http_send" class="span10" type="text" value="/monitor/alive"   readonly="true" style="margin-bottom: 5px; vertical-align: top; width:180px"/>
                    &nbsp;HTTP/1.0 \r\n\r\n" ;
                    <a id="healthCheck_TCP_Desc" class="fa fa-question-circle"></a><br>


                    <input id="radio_customized" type="radio" name="options" value="customized" style="margin-bottom: 25px; vertical-align: top">
                    &nbsp;自定义:&nbsp;&nbsp;
                    "GET&nbsp;
                    <input id="text_customized_http_send" class="span10" type="text"   style="margin-bottom: 5px; vertical-align: top; width:180px"/>
                    &nbsp;HTTP/1.0 \r\n\r\n" ;
                    <a id="healthCheck_Custom_Desc" class="fa fa-question-circle"></a><br>

                </div>
            </div>

            <div class="control-group">
                <label class="control-label" style="width: 125px;"></label>
                <div class="controls" style="margin-left: 125px;">
                    <button id="healthCheck_btn_save" class="btn btn-primary">
                        <i class='fa fa-save'></i>
                        保存
                    </button>
                    <span style="color: red;">编辑后请保存, 状态为"启用"才生效</span>
                    <hr/>
                    <p>健康检查判定条件：</p>
                    <ol>
                        <li>向指定的ip、端口、url发送HTTP GET请求，根据响应时间和http状态码判断服务是否健康，不关注响应内容。</li>
                        <li>一个检测周期（5~8s）内连续两次判断为非正常则将节点状态置为“未启动”。</li>
                        <li>超时时间：500ms</li>
                        <li>正常状态码范围：200~399</li>
                        <li>异常情况如端口拒绝、连接超时一律判断为非正常。</li>
                        <li>详细参考：<a href="https://123.sankuai.com/km/page/43323220">OCTO HTTP服务自定义健康检查介绍</a></li>
                    </ol>
                </div>
            </div>
        </div>
    </div>

    <div id="httpConfig_loadBalance_wrap">
        <div class="form-horizontal">
            <div class="control-group" style="margin-top: 30px; margin-bottom: 20px;">
                <label class="control-label" style="width: 125px; padding-top: 0px;">负载均衡方案：</label>
                <div id="loadBalance_strategy" class="controls" style="margin-left: 125px;">
                    <div style="height:40px">
                        <input id="radio_balance_wrr" type="radio" name="options" value="WRR"  checked="checked" style="margin-bottom: 6px">
                        &nbsp;Weight Round Robin&nbsp;&nbsp;
                        <a id="desc_balance_wrr" class="fa fa-question-circle"></a><br>
                    </div>

                    <div id="balance_idc_optimize_wrap" style="height:40px">
                        <input id="radio_balance_idc_optimize" type="radio" name="options" value="idc_optimize" style="margin-bottom: 6px">
                        &nbsp;IDC优化&nbsp;&nbsp;
                        <a id="desc_balance_idc_optimize" class="fa fa-question-circle"></a><br><br>
                    </div>

                    <div id="balance_read_write_optimize_wrap" style="height:40px">
                        <input id="radio_balance_read_write_optimize" type="radio" name="options" value="read_write_optimize" style="margin-bottom: 6px">
                        &nbsp;读写分离优化&nbsp;&nbsp;
                        <a id="desc_balance_read_write_optimize" class="fa fa-question-circle"></a><br><br>
                    </div>

                    <div id="balance_center_optimize_wrap" style="height:40px">
                        <input id="radio_balance_center_optimize" type="radio" name="options" value="center_optimize" style="margin-bottom: 6px">
                        &nbsp;同中心优化&nbsp;&nbsp;
                        <a id="desc_balance_center_optimize" class="fa fa-question-circle"></a><br><br>
                    </div>

                    <div style="height:40px">
                        <input id="radio_balance_ip_hash" type="radio" name="options" value="ip_hash" style="margin-bottom: 6px">
                        &nbsp;ip_hash&nbsp;&nbsp;
                        <a id="desc_balance_ip_hash" class="fa fa-question-circle"></a><br>
                    </div>

                    <div style="height:40px">
                        <input id="radio_balance_least_conn" type="radio" name="options" value="least_conn" style="margin-bottom: 6px">
                        <span style="vertical-align: middle">&nbsp;least_conn&nbsp;&nbsp;</span>
                        <a id="desc_balance_least_conn" class="fa fa-question-circle"></a><br>
                    </div>

                    <div style="height:40px">
                        <input id="radio_balance_session_sticky" type="radio" name="options" value="session_sticky" style="margin-bottom: 6px">
                        &nbsp;session_sticky&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <input id="text_balance_session_sticky" class="span10" type="text" placeholder="如：cookie=FWS fallback=on path=/"  style="width:300px"/>
                        &nbsp;&nbsp;
                        <a id="desc_balance_session_sticky" class="fa fa-question-circle"></a><br>
                    </div>

                    <div id="balance_consistent_hash_wrap" style="height:40px">
                        <input id="radio_balance_consistent_hash" type="radio" name="options" value="consistent_hash" style="margin-bottom: 6px">
                        &nbsp;consistent_hash&nbsp;&nbsp;
                        <input id="text_balance_consistent_hash" class="span10" type="text"  placeholder="如：$request_uri(此项较敏感，请联系SRE操作)" style="width:300px"/>
                        &nbsp;&nbsp;
                        <a id="desc_balance_consistent_hash" class="fa fa-question-circle"></a><br><br>
                    </div>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" style="width: 125px;"></label>
                <div class="controls" style="margin-left: 125px;">
                    <button id="loadBalance_btn_save" class="btn btn-primary">
                        <i class='fa fa-save'></i>
                        保存
                    </button>
                    <span style="color: red;">编辑后请保存</span>
                </div>
            </div>
        </div>
    </div>

    <div id="httpConfig_domain_wrap">
        <!--div class="form-inline" style="padding-left: 0;">
            <div class="control-group"  white-space: nowrap;">
                <button class="btn btn-small" id="http-methodUrl-list-add" title="新增接口"><i class="fa fa-plus"></i>添加</button>
            </div>
        </div-->
        <button id="http-methodUrl-list-add" type="button" class="btn btn-primary" title="新增接口"">
            <i class="fa fa-plus">添加</i>
        </button>
        <table class="table table-striped table-hover" id="httpMethod-list-table" style="margin-top: 20px;">
            <colgroup>
                <col width="40%"></col>
                <col width="40%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>接口URL</th>
                <th>所属域名</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>

    <div id="httpConfig_slowStart_wrap">
        <div class="form-horizontal">
            <div class="control-group" style="margin-top: 30px;  margin-bottom: 20px; ">
                <label class="control-label" style="width: 125px; padding-top: 0px;">状态：</label>
                <div id="slowStart_switch_content" class="controls" style="margin-left: 125px">
                    <input id="slowStart_switch_on" value="1" type="radio" checked  style="vertical-align: top"/>
                    <span >&nbsp;开启&nbsp;&nbsp;</span>
                    <input id="slowStart_switch_off" value="0" type="radio" style="vertical-align: top"/>
                    <span >&nbsp;关闭&nbsp;&nbsp;</span>
                    &nbsp;&nbsp;&nbsp;&nbsp;此选项较敏感，使用前请咨询姚剑鹏。
                </div>
            </div>

            <div class="control-group" style="margin-bottom: 20px;">
                <label class="control-label" style="width: 125px; padding-top: 0px;">慢启动规则：</label>
                <div id="slowstart_time" class="controls" style="margin-left: 125px;">
                    <input id="radio_slowstart_time" type="radio" name="options" checked="checked" value="slowstarttime" style="margin-bottom: 25px; vertical-align: top">
                    &nbsp;自定义时间:&nbsp;&nbsp;
                    &nbsp;
                    <input id="text_slowstart_time" class="span10" type="text"   placeholder="如：10 " style="margin-bottom: 5px; vertical-align: top; width:180px"/>
                    &nbsp;单位：s（秒)；
                    <!--a id="desc_slowstart_time" class="fa fa-question-circle"></a><br-->
                    <a id="desc_slowstart_hash" class="fa fa-question-circle"></a><br><br>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" style="width: 125px;"></label>
                <div class="controls" style="margin-left: 125px;">
                    <button id="slowStart_btn_save" class="btn btn-primary">
                        <i class='fa fa-save'></i>
                        保存
                    </button>
                    <span style="color: red;">编辑后请保存, 状态为"开启"才生效</span>
                </div>
            </div>
        </div>
    </div>

    <div id="paginator_methodUrl">

    </div>
        <!--div class="form-horizontal" style="margin-top:30px">
            <div class="control-group">
                <label class="control-label" style="width: 125px">域名：</label>
                <div class="controls" style="margin-left: 125px;">
                    <input id="text_domain_name" class="span10" type="text"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px">Location：</label>
                <div class="controls" style="margin-left: 125px; margin-bottom: 10px;">
                    <input id="text_domain_location" class="span10" type="text"/>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" style="width: 125px;"></label>
                <div class="controls" style="margin-left: 125px; margin-top: 15px;">
                    <button id="domain_btn_save" class="btn btn-primary">
                        <i class='fa fa-save'></i>
                        保存
                    </button>
                    <span style="color: red;">编辑后请保存</span>
                </div>
            </div>
        </div>
    </div-->

</div>

<textarea id="text-http-method-table-tbody" style="display:none">
<% if(this.data.length == 0){ %>
    <tr>
        <td colspan="3" style="text-align: center;">无接口数据，请添加</td>
    </tr>
<% } else{ %>
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-info="<%= Y.JSON.stringify(item) %>">
        <td style="width: 25%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.methodUrl %></td>
        <td style="width: 25%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item.serverName %></td>
        <td>
            <a id="delHttpMethodUrl" href="javascript:void(0);" class="config-panel-delete delHttpMethodUrl"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span >删除</span> </a>
        </td>
    </tr>
<% }); %>
<% } %>
</textarea>

<textarea id="text_add_httpMethodUrl_form" style="display:none">
    <div id="add_httpMethodUrl_form" class="form-horizontal">
        <div class="control-group"><label class="control-label" for="s_server_name">服务域名：</label>
            <div class="controls">
                <input type="text"  id="s_server_name" >
                <span class="tips" >当前接口所属的域名</span>
            </div>
        </div>
        <div class="control-group"><label class="control-label" for="s_method_url">接口URL：</label>
            <div class="controls">
                <input type="text" id="s_method_url" />
                <span class="tips" >接口具体的url</span>
            </div>
        </div>

    </div>
</textarea>