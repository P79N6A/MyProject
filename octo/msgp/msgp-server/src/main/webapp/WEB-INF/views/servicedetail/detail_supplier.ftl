<#--<div class="content-overlay">-->
<#--<i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>-->
<#--</div>-->
<style>
    .sorttable-header-bg:hover, .sorttable-header-asc, .sorttable-header-desc {
        background: #ddd;
        cursor: pointer;
    }

    .form-inline {
        padding: 0;
    }

    .overview {
        display: inline-block;
        width: 200px;
        height: 200px;
        overflow: hidden;
        margin: 10px 20px 0 60px;
        cursor: pointer;
        /*border:1px solid #868686;*/
        position: relative;
        z-index: 998
    }

    .menu-special {
        float: none;
        box-shadow: none;
        border: none;
        position: relative;
        margin-bottom: 20px;
        margin-top: 30px;
        z-index: 8;
    }

    .overview-btn {
        position: relative;
        margin-left: 1em;
    }


    div.tooltip-inner {
        text-align: left;
        max-width: 100%;
        width:auto;
        background-color: #505050;
        font-size: 14px;
    }

    .provider-outline-mask {
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
<div class="content-body">
    <div class="form-inline mb20">
        <button id="add_supplier" type="button" class="btn btn-primary" title="新增提供者">
            <i class="fa fa-plus">新增</i>
        </button>

        <a>类型：</a>

        <div id="supplier_thrift_http" class="btn-group">
            <a value="1" type="button" id="supplier_type" class="btn <#if (!type?? || type='1')>btn-primary</#if>"
               href="javascript:void(0)">thrift</a>
            <a value="2" type="button" id="supplier_type"
               class="btn btn-default <#if (type?exists && type='2')>btn-primary</#if>"
               href="javascript:void(0)">http</a>
        </div>

    <#include "/common/env.ftl" >

        <div id="supplier_env_select" class="btn-group">
            <a value="prod" type="button" class="btn <#if (!env?? || env='prod')>btn-primary</#if>"
               href="javascript:void(0)">prod</a>
            <a value="stage" type="button" class="btn btn-default <#if (env?exists && env='stage')>btn-primary</#if>"
               href="javascript:void(0)">stage</a>
            <#if isOffline>
                <a value="test" type="button" class="btn btn-default <#if (env?exists && env='test')>btn-primary</#if>"
                   href="javascript:void(0)">test</a>
            </#if>
        </div>
        <a>状态：</a>

        <div id="supplier_status_select" class="btn-group">
            <a value="-1" type="button" class="btn btn-default <#if (!status?? || status='-1')>btn-primary</#if>"
               href="javascript:void(0)">所有</a>
            <a value="2" type="button" class="btn btn-default <#if (status?exists && status='2')>btn-primary</#if>"
               href="javascript:void(0)">正常</a>
            <a value="4" type="button" class="btn btn-default <#if (status?exists && status='4')>btn-primary</#if>"
               href="javascript:void(0)">禁用</a>
            <a value="0" type="button" class="btn btn-default <#if (status?exists && status='0')>btn-primary</#if>"
               href="javascript:void(0)">未启动</a>
        </div>

        <a style="padding-left:0.5em"></a>

        <div class="input-append">
            <input id="searchBox" class="span2" placeholder="主机,端口,状态等组合搜索,以空格隔开" type="text" style="width:300px"
                   value="${keyword!""}"/>
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
        </div>
    <#--<button id="refresh_supplier" type="button" class="btn btn-primary" style="margin-left: 20px;" title="刷新列表">-->
    <#--<i class="fa fa-refresh"></i>-->
    <#--</button>-->
        <a class="btn overview-btn btn-primary" href="javascript:;" style="display: none;">
            概要
            <span class="fa fa-angle-down"></span>

        </a>
    <#-- <span class="corner corner-danger" id="yui_3_17_2_3_1469762066045_62">New!</span>-->

        <div class="menu-special" style="text-align: center; display: none">
            <div class="overview" id="providerIDC"></div>
            <div class="overview" id="providerStatus"></div>
            <div class="provider-outline-mask" style="display:none"></div>
        </div>
    </div>
<#--<div class="form-inline mb20">
    <table style="width: 100%;">
        <tr>
            <td style="padding-right: 10px;">
                <div id="provider_idc" style="box-shadow: 0 1px 1px 0 #FFF; padding: 12px 10px 12px 20px; background: #EDEDED;">哈哈哈哈</div>
            </td>
            <td style="padding-left: 10px">
                <div id="provider_status" style="box-shadow: 0 1px 1px 0 #FFF; padding: 12px 10px 12px 20px; background: #EDEDED;">嘿嘿嘿嘿</div>
            </td>
        </tr>
    </table>
</div>-->
    <div class="form-inline mb20">
        <table style="width: 100%;">
            <tr>
                <td style="padding-right: 10px; width: 50%;">
                    <div style="border: 1px solid #ccc; box-shadow: 0 1px 2px 0 #aaa;">
                        <div id="provider_idc_title" style="color: #888;
                        background-color: #fafafa;
                        border-bottom: 1px solid #ebebeb;
                        border-radius: 5px 5px 0 0 ;
                        font-weight: 700;
                        min-height: 20px;
                        line-height: 30px;
                        text-align: center;
                        text-shadow: 1px 1px 0 #fff;
                        font-size: 18px;">机房分布统计(节点数/机器数)
                        </div>
                        <div id="provider_idc_content"
                             style="padding: 10px 10px 17px; position: relative; overflow: hidden">
                            <div style="text-align: center">无数据</div>
                        </div>
                    </div>
                </td>
                <td style="padding-left: 10px; width: 50%;">
                    <div style="border: 1px solid #ccc; box-shadow: 0 1px 2px 0 #aaa;">
                        <div id="provider_status_title" style="color: #888;
                        background-color: #fafafa;
                        border-bottom: 1px solid #ebebeb;
                        border-radius: 5px 5px 0 0 ;
                        font-weight: 700;
                        min-height: 20px;
                        line-height: 30px;
                        text-align: center;
                        text-shadow: 1px 1px 0 #fff;
                        font-size: 18px;">节点状态统计
                        </div>
                        <div id="provider_status_content"
                             style="padding: 10px 10px 17px; position: relative; overflow: hidden">
                            <div style="text-align: center">无数据</div>
                        </div>
                    </div>
                </td>
            </tr>
        </table>
    </div>
    <div class="form-inline mb20">
    </div>
    <div>
        <div style="font-size: 0;margin-top: 10px;margin-bottom: 10px; float: left;">
            <div id="all-role" class="btn-group btn-role">
                <button data-role="0" class="btn btn-small btn-alive ">主用</button>
                <button data-role="1" class="btn btn-small btn-dead ">备机</button>
            </div>
        <#--<span id="all-weight" class="change-weight" title="点击修改">10</span>-->
            <div id="all-enabled" class="btn-group btn-enabled">
                <button data-enabled="0" class="btn btn-small btn-alive" title="启用">启用</button>
                <button data-enabled="1" class="btn btn-small btn-dead" title="禁用">禁用</button>
                <button data-enabled="2" class="btn btn-small btn-normal" title="正常">正常</button>
            </div>

            <div class="btn-group btn-enabled">
                <button id="del-all-supplier" class="btn btn-small btn-del" title="删除节点">删除</button>
            </div>


            <div id="all-weight" class="btn-group btn-weight">
                <button id="data-weight" class="btn btn-small
             btn-alive" title="修改权重">权重
                </button>
            </div>
        </div>
        <div style="float: left; padding: 12px 0 0 20px;">
            <div style="float: left;">
                <label style="padding-left:1em"> 每页节点数量：</label>
            </div>
            <div id="pageSize" style="float: left;">
                [<a style="cursor:pointer;" data-value="10"> 10 </a>]
                [<a style="cursor:pointer;" data-value="20"> 20 </a>]
                [<a style="cursor:pointer;" data-value="50"> 50 </a>]
                [<a style="cursor:pointer;" data-value="100"> 100 </a>]
            </div>
        </div>

        <div style="float: right; padding: 12px 0 0 20px; display: none;" id="resource_utilization_wrapper">
            <span style="font-size: 14px; padding-left: 10px; font-weight: bold;">资源利用率: </span><span
                id="resource_utilization_rate" style="font-size: 14px;">100.00%</span>
            <span style="font-size: 14px; padding-left: 10px; font-weight: bold;">资源空闲率: </span><span
                id="resource_vacancy_rate" style="font-size: 14px;">100.00%</span>
        <#--
                    <span style="font-size: 14px; padding-left: 10px; font-weight: bold;">容灾等级: </span><span id="disaster_tolerance_grade" style="font-size: 14px;">100.00%</span>
        -->
            <a href="" target="_blank" id="resource_utilization_link"
               style="padding-left: 20px; padding-right: 20px;"><i class="fa fa-bar-chart-o"> 指标详情</i></a>
            <a href="https://123.sankuai.com/km/page/14741528" target="_blank"
               id="resource_utilization_link"><i class="fa fa-file-text-o"> 指标说明</i></a>
        </div>
        <div style="float: right; padding: 12px 0 0 20px; display: none;" id="resource_capacity_wrapper">
            <span style="font-size: 14px; padding-left: 10px; font-weight: bold;">服务容量
                <a target="_blank" style="padding-left:5px"
                   href="https://123.sankuai.com/km/page/32957924">
                    <i class="fa fa-question-circle"></i></a>: </span><span
                id="resource_capacity" style="font-size: 14px;">0</span>
        </div>
        <div id="currentIdc" class="btn-group btn-weight" style="float: right; padding-right: 20px;">
            <label style="color:red;margin-left:100px"></label>
        </div>
    </div>

    <div id="table_supplier" class="table-responsive">
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="2%">
                <col width="10%">
                <col width="8%">
                <col width="5%">
                <col width="5%">
                <col width="7%">
                <col width="10%">
                <col width="5%">
                <#--<col width="5%">-->
                <col width="5%">
            <#if isOffline>
                <col width="5%">
            </#if>
                <#--<col width="5%">-->
                <col width="13%">
                <col width="8%">
                <col width="4%">
            </colgroup>
            <thead>
            <tr>
                <th><input id="all-check" type="checkbox">
                </th>
                <th class="sorttable-header-bg" value="-1">主机名 <i class="fa fa-sort"/></th>
                <th class="sorttable-header-bg" value="-2">IP <i class="fa fa-sort"/></th>
                <th class="sorttable-header-bg" value="-3">端口 <i class="fa fa-sort"/></th>
                <th class="sorttable-header-bg" value="-4">角色 <i class="fa fa-sort"/></th>
                <th class="sorttable-header-bg" value="-5">octo版本 <i class="fa fa-sort"/></th>
                <th class="sorttable-header-bg" value="-6">权重(<span class="text-red">点击修改</span>) <i
                        class="fa fa-sort"/></th>
                <th class="sorttable-header-bg" value="-7">状态 <i class="fa fa-sort"/></th>
                <#--<th>工作模式</th>-->
                <#if isOffline>
                    <th id="set">单元</th>
                    <th id="swimlane">泳道</th>
                <#else>
                    <th id="set">单元</th>
                </#if>
                <th class="sorttable-header-bg" value="-8">更新时间 <i class="fa fa-sort-desc"/></th>
                <th>节点状态</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody id="supplier_content_body">
            <tr id="content_overlay">
                <td colspan="12">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="paginator_supplier">
    </div>
</div>