<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<link rel="stylesheet" type="text/css" href="/static/css/bootstrap-multiselect.css"/>
<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: #3fab99;
    }
    .common-popdialog {
        overflow: scroll;
    }

    .multiselect-all label {
        font-weight: bold;
    }
    .multiselect-search {
        margin-left: 10px;
    }

    .input-group-btn {
        display: none;
    }

    .multiselect-container li{
        text-align: left;
    }

    .dropdown-menu>li>a:hover, .dropdown-menu>li>a:focus, .dropdown-submenu:hover>a, .dropdown-submenu:focus>a{
        background-color: #3fab99;
    }

    .dropdown-menu>.active>a, .dropdown-menu>.active>a:hover, .dropdown-menu>.active>a:focus {
        background-color: #3fab99;
    }

</style>
<div id="appkey_auth_loading" class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div id="appkey_auth_content" class="content-body" style="display:none;">
    <div>
        <div class="form-inline mb20" style="padding-left: 0;">
            <#include "/common/env.ftl" >
            <div id="auth_env_select" class="btn-group">
                <a value="3" id="auth_prod"  type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
                <a value="2" id="auth__stage" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="1" id="auth__test"  type="button" class="btn btn-default" href="javascript:void(0)">test</a>
                </#if>
            </div>
        </div>
    </div>
    <div style = "margin-bottom: 15px">
        <ol>
            <li>
                <a href="https://123.sankuai.com/km/page/58025008" target="_blank" ">
                    <span style="color: red; font-weight: bold">鉴权功能影响面极大，使用前必读: </span>服务鉴权使用说明
                </a>
            </li>
            <li>
                鉴权所有开关、配置动态生效。请务必按照一下流程接入： 1.详细阅读文档; 2.线下进行完整测试; 3.低峰期线上操作。
            </li>
            <li>
                灰度鉴权（鉴权失败后仍完成请求）能够有效在接入鉴权时保护服务，推荐在接入时开启，开启方式参考使用说明。
            </li>
            <li>
                若对鉴权结果有疑问，先前往<a href = "#auth_diagnosis_param">服务鉴权自查</a>进行鉴权诊断，如还有疑问，请到对应的OCTO技术支持群咨询。
            </li>

        </ol>
    </div>


    <legend>服务白名单<span style="font-size: 10px; margin-left: 10px; color: grey;">进入白名单的服务无需鉴权即可访问</span></legend>
    <div style="padding-bottom: 30px;">
       <div class="form-inline" style="padding-left: 0;">
           <div class="control-group" style="display: inline-block; white-space: nowrap;">
              <#-- <label style="margin-left: 5px"> 服务：</label>
               <select id="appkey-add-remoteAppkey" multiple="multiple"></select>-->
               <button class="btn btn-small" id="appkey-white-list-add"><i class="fa fa-plus"></i>添加服务</button>
               <button id="appkey-white-list-refresh" class="btn btn-small" title="刷新列表"><i class="fa fa-refresh">刷新列表</i></button>
           </div>
       </div>
        <table class="table table-striped table-hover" id="appkey-white-list-table">
            <colgroup>
                <col width="80%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>服务(Appkey)</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <legend>服务鉴权配置<span style="font-size: 10px; margin-left: 10px; color: grey;">支持服务粒度的权限控制</span></legend>
    <div style="padding-bottom: 30px;">
        <div class="form-inline" style="padding-left: 0;">
            <div class="control-group" style="display: inline-block; white-space: nowrap;">
                <button class="btn btn-small" id="appkey-auth-list-add"><i class="fa fa-plus"></i>添加配置</button>
                <button id="appkey-auth-list-refresh" class="btn btn-small" title="刷新列表"><i class="fa fa-refresh">刷新列表</i></button>
                <button id="appkey-auth-list-sync" class="btn btn-small" title="配置同步"><i class="fa fa-upload">配置同步</i></button>
            </div>
        </div>
        <table class="table table-striped table-hover" id="appkey-auth-list-table">
            <colgroup>
                <col width="80%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>服务(Appkey)</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <legend>接口鉴权配置<span style="font-size: 10px; margin-left: 10px; color: grey;">支持接口粒度的权限控制</span></legend>
    <div style="padding-bottom: 30px;">
        <div class="form-inline" style="padding-left: 0; margin-bottom: 10px;">
            <button class="btn btn-small" id="span-auth-list-add"><i class="fa fa-plus"></i>添加配置</button>
            <button id="span-auth-list-refresh" class="btn btn-small" title="刷新列表"><i class="fa fa-refresh">刷新列表</i></button>
            <button id="span-auth-list-sync" class="btn btn-small" title="配置同步"><i class="fa fa-upload">配置同步</i></button>
        </div>
        <table class="table table-striped table-hover" id="span-auth-list-table">
            <colgroup>
                <col width="30%"></col>
                <col width="10%"></col>
                <col width="40%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>接口(Spanname)</th>
                <th></th>
                <th>服务白名单</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>

    <legend>服务鉴权自查<span style="font-size: 10px; margin-left: 10px; color: grey;">根据查询条件进行鉴权诊断</span></legend>
    <div  id = "auth_diagnosis_param">
        <div class="form-inline mb20" style="padding: 0;">
            <table>
                <tr style="vertical-align: bottom;">
                    <td style="padding-left: 20px;">
                        <label> 时间：</label>
                    </td>
                    <td>
                        <input id="start_time" type="text" class="span3" placeholder="查询开始时间" style="width: 160px;">
                    </td>

                    <#--<td style="padding-left: 20px;">-->
                        <#--<label> 结束：</label>-->
                    <#--</td>-->
                    <#--<td>-->
                        <#--<input id="end_time" type="text" class="span3" placeholder="查询结束时间" style="width: 160px;">-->
                    <#--</td>-->
                </tr>

                <tr style="vertical-align: bottom;">
                    <td style="padding-left: 20px;"><label> 客户端：</label></td>
                    <td colspan="3" style="padding-top: 15px;" >
                        <input type="text" placeholder="必填：输入客户端appkey" id="clientAppkey" style="width: 310px;">
                    </td>
                    <td style="padding-left: 20px;"><label> 客户端ip：</label></td>
                    <td colspan="3" style="padding-top: 15px;" >
                        <input type="text" placeholder="手动填写，非必填" id="clientIP" style="width: 310px;">
                    </td>
                </tr>

                <tr style="vertical-align: bottom;">
                    <td style="padding-left: 20px;"><label> 服务端：</label></td>
                    <td colspan="3" style="padding-top: 15px;" >
                        <input type="text" placeholder="必填：输入服务端appkey" id="serverAppkey" style="width: 310px;">
                    </td>
                    <td style="padding-left: 20px;"><label> 服务端ip：</label></td>
                    <td colspan="3" style="padding-top: 15px;" >
                        <input type="text" placeholder="手动填写，非必填" id="serverIP" style="width: 310px;">
                    </td>
                </tr>
            </table>
        </div>

        <div class="btn-group" id="auth_query_btn" style = "margin-left: 20px">
            <a type="button" class="btn graph btn-primary">查看结果</a>
        </div>
    </div>
    <#--<div class="auth_diagnosis_list clearfix" style="text-align: center;"></div>-->
    <div class="auth_diagnosis_list">
        <table class="table table-hover table-striped" id="server_info_table">
            <caption>诊断结论</caption>
            <thead>
            <tr>
                <th style="width: 100%">诊断结论<i class="fa fa-sort"/></th>
            </tr>
            </thead>
            <tbody id="tbody_conclusion">

            </tbody>
        </table>
        <#-- todo：保留，后续可能需要加上-->
        <#--<table class="table table-hover table-striped" id="server_info_table">-->
            <#--<caption>服务端信息</caption>-->
            <#--<thead>-->
            <#--<tr>-->
                <#--<th style="width: 20%">服务端appkey<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">octo版本<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">kms版本<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">统一协议<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">开启灰度<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">鉴权粒度<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 30%">被授权的客户端appkey<i class="fa fa-sort"/></th>-->
            <#--</tr>-->
            <#--</thead>-->
            <#--<tbody id="tbody_server">-->

            <#--</tbody>-->
        <#--</table>-->
        <#--<table class="table table-hover table-striped" id="client_info_table">-->
            <#--<caption>客户端信息</caption>-->
            <#--<thead>-->
            <#--<tr>-->
                <#--<th style="width: 20%">客户端appkey<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">octo版本<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">kms版本<i class="fa fa-sort"/></th>-->
                <#--<th style="width: 10%">统一协议<i class="fa fa-sort"/></th>-->
            <#--</tr>-->
            <#--</thead>-->
            <#--<tbody id="tbody_client">-->

            <#--</tbody>-->
        <#--</table>-->
    </div>

    <legend style="margin-top: 60px">服务鉴权申请</legend>
    <div style="padding-bottom: 30px;">
        点此进行申请:<a href="https://ops.sankuai.com/workflow/#/start/union-auth?data=%7B%22client_appkey%22%3A%22%22,%22server_appkey%22%3A%22%22,%22category%22%3A%22thrift%22,%22service_method%22%3A%7B%22others%22%3A%7B%7D,%22ready%22%3A%7B%7D%7D%7D" target="_blank" ">
            <span font-weight: bold">统一鉴权接入流程</span>
        </a>
    </div>

</div>

<textarea id="text-appkey-white-list-table-tbody" style="display:none">
<% if(this.data.length == 0){ %>
    <tr>
        <td colspan="2" style="text-align: center;">无服务白名单数据, 请添加</td>
    </tr>
<% } else{ %>
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-appkey="<%= item %>">
        <td style="width: 80%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item %></td>
        <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><a class="appkey-white-list-delete" href="javascript:;"><i class="fa fa-trash-o"></i> 删除单个配置</a></td>
    </tr>
<% }); %>
<% } %>
</textarea>


<textarea id="text-appkey-auth-list-table-tbody" style="display:none">
<% if(this.data.length == 0){ %>
    <tr>
        <td colspan="2" style="text-align: center;">无服务鉴权数据, 请添加</td>
    </tr>
<% } else{ %>
<% Y.Array.each(this.data, function(item, index){ %>
    <tr data-appkey="<%= item %>">
        <td style="width: 80%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= item %></td>
        <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><a class="appkey-auth-list-delete" href="javascript:;"><i class="fa fa-trash-o"></i> 删除单个配置</a></td>
    </tr>
<% }); %>
<% } %>
</textarea>

<textarea id="text-span-auth-list-table-tbody" style="display:none">
<% if(this.data.length == 0){ %>
    <tr>
        <td colspan="4" style="text-align: center;">无接口鉴权数据, 请添加</td>
    </tr>
<% } else{ %>
    <% Y.Array.each(this.data, function(item, index){ %>
        <% var span = item.spanname; %>
        <% var appkeyList = item.appkeyList; %>
        <% var rowspanSize = appkeyList.length  + 1; %>
        <% Y.Array.each(appkeyList, function(appkey, index){ %>
            <% if(index == 0 ){ %>
                <tr data-span="<%= span %>"  data-appkey="<%= appkey %>">
                    <td rowspan= <%= rowspanSize %> ><%= span %></td>
                    <td rowspan= <%= rowspanSize %> ><a class="span-auth-list-delete" href="javascript:;" style="margin-left: 0;"><i class="fa fa-trash-o"></i> 删除所有配置</a></td>
                    <td style="width: 40%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= appkey %></td>
                    <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><a class="span-appkey-auth-list-delete" href="javascript:;" style="margin-left: 0;"><i class="fa fa-trash-o"></i> 删除单个配置</a></td>
                </tr>
            <% }else{ %>
                <tr data-span="<%= span %>"  data-appkey="<%= appkey %>">
                    <td style="width: 40%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><%= appkey %></td>
                    <td style="width: 20%;text-overflow:ellipsis; overflow: hidden;white-space: nowrap"><a class="span-appkey-auth-list-delete" href="javascript:;" style="margin-left: 0;"><i class="fa fa-trash-o"></i> 删除单个配置</a></td>
                </tr>
            <% } %>
        <% }); %>
        <tr data-span="<%= span %>">
            <td colspan="2" style="text-align: center;"><a class="span-appkey-auth-list-add" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 增加更多服务</a></td>
        </tr>
    <% }); %>
<% } %>
</textarea>

<textarea id="text-appkey-white-list-add-dialog" style="display:none">
    <div style="padding-bottom: 80px;">
        <div id="appkey-white-list-add-dialog" class="form-inline mt20 mb20" style="text-align: center;">
            <div class="control-group">
               <label style="margin-left: 5px"> 消费者：</label>
               <select id="appkey-white-list-add-dialog-remoteAppkey" multiple="multiple"></select>
           </div>
            <div>
                <span style="margin-left: 50px; font-size: 10px; color: grey;">已加入白名单且在消费者列表的服务会默认选中</span>
            </div>
            <div style="margin: 20px 0 0 48px;">
                <span style="color: gray;">未在消费者列表找到待添加的服务? <a id="show-appkey-white-list-add-dialog-remoteAppkey-input" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 点此手动输入</a></span>
                <input id="appkey-white-list-add-dialog-remoteAppkey-input" style="width: 260px; margin: 20px auto 0 auto; display: none;" placeholder="请准确输入待添加服务的Appkey">
            </div>
        </div>
    </div>
</textarea>

<textarea id="text-appkey-auth-list-add-dialog" style="display:none">
    <div style="padding-bottom: 80px;">
        <div id="appkey-auth-list-add-dialog" class="form-inline mt20 mb20" style="text-align: center;">
            <div class="control-group">
               <label style="margin-left: 5px"> 消费者：</label>
               <select id="appkey-auth-list-add-dialog-remoteAppkey" multiple="multiple"></select>
           </div>
            <div class="control-group">
               <label style="margin-left: 50px; font-size: 10px; color: grey;">已加入鉴权名单且在消费者列表的服务会默认选中</label>
            </div>
            <div style="margin: 20px 0 0 48px;">
                <span style="color: gray;">未在消费者列表找到待添加的服务? <a id="show-appkey-auth-list-add-dialog-remoteAppkey-input" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 点此手动输入</a></span>
                <input id="appkey-auth-list-add-dialog-remoteAppkey-input" style="width: 260px; margin: 20px auto 0 auto; display: none;" placeholder="请准确输入待添加服务的Appkey">
            </div>
        </div>
    </div>
</textarea>

<textarea id="text-span-auth-list-add-dialog" style="display:none">
    <div style="padding-bottom: 80px;">
        <div id="span-auth-list-add-dialog" class="form-inline mt20 mb20" style="text-align: center;">
            <div class="control-group" style="padding-bottom: 20px;">
                <label style="margin-left: 5px"> 接口名：</label>
                <input id="span-auth-list-add-dialog-spanname" style="width: 300px;">
            </div>
            <div class="control-group">
                <label style="margin-left: 5px"> 消费者：</label>
                <select id="span-auth-list-add-dialog-remoteAppkey" multiple="multiple" style="width: 300px;"></select>
            </div>
            <div class="control-group">
               <label style="margin-left: 50px; font-size: 10px; color: grey;">已加入鉴权名单且在消费者列表的服务会默认选中</label>
            </div>
            <div style="margin: 20px 0 0 48px;">
                <span style="color: gray;">未在消费者列表找到待添加的服务? <a id="show-span-auth-list-add-dialog-remoteAppkey-input" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 点此手动输入</a></span>
                <input id="span-auth-list-add-dialog-remoteAppkey-input" style="width: 260px; margin: 20px auto 0 auto; display: none;" placeholder="请准确输入待添加服务的Appkey">
            </div>
        </div>
    </div>
</textarea>
 <script>
     var appkeyCache = {
         key : '${appkey!""}',
         list : [<#list apps![] as app>'${app}',</#list>]
     }
 </script>
<script src="/static/js/jquery.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="/static/js/bootstrap-multiselect.js"></script>