<style>
    .sorttable-header-bg:hover, .sorttable-header-asc, .sorttable-header-desc {
        background: #ddd;
        cursor: pointer;
    }
    .form-inline {
        padding: 0;
    }
    .overview {
        display:inline-block;
        width: 520px;
        height: 320px;
        overflow: hidden;
        margin: 10px 20px 0 60px;
        cursor:pointer;
        /*border:1px solid #868686;*/
        position:relative;
        z-index:998
    }
    .menu-special {
        float: none;
        box-shadow: none;
        border: none;
        position: relative;
        margin-bottom: 20px;
        margin-top: 30px;
        z-index:8;
    }
    .overview-btn {
        position: relative;
        margin-left: 1em;
    }
    .consumer-outline-mask {
        position: relative;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 998;
        background: black;
        opacity: 0.25;
        filter: alpha(opacity=25);
    }
</style>

<div class="content-overlay" style="text-align: center; padding-bottom: 20px; padding-top: 20px;" >
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="content-body" style="display:none;">
    <div class="form-inline mb20">
        <div class="control-group" style="display: inline-block;">
        <#include "/common/env.ftl" >
            <div id="consumer_env_select" class="btn-group">
                <a value="prod" type="button" class="btn <#if (!env?? || env='prod')>btn-primary</#if>" href="javascript:void(0)">prod</a>
                <a value="stage" type="button" class="btn btn-default <#if (env?exists && env='stage')>btn-primary</#if>" href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="test" type="button" class="btn btn-default <#if (env?exists && env='test')>btn-primary</#if>" href="javascript:void(0)">test</a>
                </#if>
            </div>

            <label style="margin-left: 5px">周期：</label>
            <div id="consumer_range_select" class="btn-group">
                <a value="1m" type="button" class="btn btn-default <#if (range?exists && range='1m')>btn-primary</#if>" href="javascript:void(0)">前1分钟</a>
                <a value="1h" type="button" class="btn <#if (!range?? || range='1h')>btn-primary</#if>" href="javascript:void(0)">前1小时</a>
                <a value="yesterday" type="button" class="btn btn-default <#if (range?exists && range='yesterday')>btn-primary</#if>" href="javascript:void(0)">昨天</a>
                <a value="today" type="button" class="btn btn-default <#if (range?exists && range='today')>btn-primary</#if>" href="javascript:void(0)">今天</a>
            </div>

            <label style="margin-left: 5px;"> 客户端服务：</label><input type="text" style="width: 180px;" placeholder="选择服务" value="${remoteApp!'*'}"
                                                               id="remoteApp">
            <label style="margin-left: 5px;"> 客户端主机：</label><input type="text" style="width: 180px;" placeholder="选择接口" value="${remoteHost!'*'}"
                                                               id="remoteHost">
        </div>
        <div class="control-group" style="display: inline-block;">
            <button id="query_btn" class="btn btn-primary ml20">查询</button>
        </div>
        <a class="btn overview-btn btn-primary" href="javascript:;">
            概要

            <span class="fa fa-angle-down"></span>

        </a>
        <span class="corner corner-danger" id="yui_3_17_2_3_1469762066045_62">New!</span>

        <div class="menu-special" style="text-align: center;">
            <div class="overview" id="consumerApp""></div>

           <div class="overview" id="consumerHost"></div>

            <div class="consumer-outline-mask" style="display:none"></div>
        </div>
    </div>

    <div id="table_consumer" class="table-responsive">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="30%"></col>
                <col width="30%"></col>
                <col width="20%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>服务标识</th>
                <th>主机名</th>
                <th>IP</th>
                <th>调用次数</th>
            </tr>
            </thead>
            <tbody id="consumer_content_body">
            <tr>
                <td colspan=4>Loading contents...</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="paginator_consumer">
    </div>
</div>
