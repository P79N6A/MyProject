<title>服务管理平台授权</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2">
    <h3 class="page-header"><p class="text-center mt5 mb5">服务管理平台授权绑定</p></h3>
    <hr/>
    <div class="control-group mb0">
        <div class="box row-fluid dialog-ul-wrapper" style="border: 0 none;height: 100%">
            <div class="span3" style="margin-left: 15%; margin-top: 3%">
                <img src="/static/img/octo.png">
            </div>
        <#if apps?has_content>
            <div class="span4" style="margin-left: 4%;">
                <h4><p class="text-center mt5 mb5">${username}, 请点击输入框选择服务：</p></h4>
                <h3 class="page-header">
                <input style="width: 500px" class="mb5 span6" type="text" id='apps_select' value='${appkey!""}' autocomplete="off"/>
                </h3>
                <button id="bind_button" class="btn btn-primary" style="margin-left: 5%;margin-top: 5%" type="button">
                    授权
                </button>
                <h4><p class="mt5 mb5" style="margin-left: 5%;margin-top: 5%" >未找到服务？<a href="/service/registry" target="_blank"> 注册服务</a></p></h4>
            </div>
            <script>
                var response_type = "${response_type}";
                var client_id = "${client_id}";
                var redirect_uri = "${redirect_uri}";
                var list = [<#list apps![] as app>'${app}',</#list>];
            </script>
            <script type="text/javascript" src="/static/oauth2/authorize.js"></script>
        <#else>
            <div class="span4" style="margin-top: 5%;">
                <h4><p class="text-center mt5 mb5">${username}, 您在管理平台还没有服务</p></h4>
                北京侧 普通应用请到<a title="点击注册" href="http://ops.sankuai.com/workflow/#/start/service-apply">服务树注册</a>,
                不需要服务节点<a href="/service/registry?force=true">注册</a><br/>
                上海侧
                新服务注册<a title="点击注册" href="http://ttt.dp/#!/start/5">应用树注册</a>,
                已有服务绑定<a title="点击绑定" href="http://ttt.dp/#!/start/23">应用树绑定</a>
            </div>
        </#if>
            <div class="span4" style="margin-top: 5%;">
                <p class="text-center mt5 mb5"><span style="color :red">提示:</span>只能授权自己所负责的服务</p>
            </div>
        </div>

    </div>
</div>
