<div class="content-body">
    <div class="form-inline mb20" style="padding-left: 0px;">
        <#include "/common/env.ftl" >

        <div id="env_select" class="btn-group">
            <a value="3" id="access_prod" type="button" class="btn btn-primary btn-access-env"
               href="javascript:void(0)">prod</a>
            <a value="2" id="access_stage" type="button" class="btn btn-default btn-access-env" href="javascript:void(0)">stage</a>
            <#if isOffline>
                <a value="1" id="access_test" type="button" class="btn btn-default btn-access-env" href="javascript:void(0)">test</a>
            </#if>
        </div>

        <a style="padding-left:1em">服务控制：</a>

        <div id="page_select" class="btn-group">
            <a value="0" id="access_page" type="button" class="btn btn-default btn-access-page"
               href="javascript:void(0)">访问控制</a>
            <a value="1" id="registry_page" type="button" class="btn btn-primary btn-access-page"
               href="javascript:void(0)">注册控制</a>

        </div>
    </div>
    <div id="access_consumer_wrap">
    <#include "consumer.ftl">
    </div>
    <div id="access_registry_wrap">
    <#include "registry.ftl">
    </div>

    <hr/>
    <p>状态：禁用或者启用本功能</p>

    <p>注册控制白名单：在本功能的启用的情况下，只有在白名单中的IP才能对本appkey完成注册。</p>

    <p>访问控制白名单：在本功能的启用的情况下，只有在白名单中的IP才能对本appkey进行访问。</p>
</div>