<title xmlns="http://www.w3.org/1999/html">服务画像</title>
<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: dimgray;
    }

    .common-popdialog {
        overflow: scroll;
    }

    .tooltip-inner {
        background: rgba(0, 0, 0, 0.7);
    }

    .ui-autocomplete {
        max-width: 220px;
        max-height: 220px;
        overflow: auto;
    }

</style>
<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#include "/topLinkEvent.ftl" >
    <div class="tab-box">
        <div id="content_wrapper">
            <div id="wrap_data_portrait" class="sheet">
                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_portrait">
                    <div class="form-inline mb20" style="padding: 0;">
                        <div class="control-group" style="display: inline-block; white-space: nowrap;">
                        <#include "/common/env.ftl" >
                            <div id="portrait_env_select" class="btn-group">
                                <a value="prod" type="button" class="btn <#if (!env?? || env='prod')>btn-primary</#if>"
                                   href="javascript:void(0)">prod</a>
                                <a value="stage" type="button"
                                   class="btn <#if (env?exists && env='stage')>btn-primary</#if>"
                                   href="javascript:void(0)">stage</a>
                                <#if isOffline>
                                    <a value="test" type="button"
                                       class="btn <#if (env?exists && env='test')>btn-primary</#if>"
                                       href="javascript:void(0)">test</a>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group" style="display: inline-block; margin-left: 5px;">
                            <div class="btn-group" id="query_btn">
                                <a type="button" class="btn chart">查询</a>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div id="kpi_list" class="clearfix" style="text-align: center;"></div>
                </div>
            </div>
        </div>
        <script type="text/javascript" src="/static/js/jquery.min.js"></script>
        <script type="text/javascript" src="/static/js/jquery-ui.js"></script>
        <script src="/static/js/tooltip.js"></script>
        <script>
            M.use('msgp-manage/data_portrait', function (Y) {
                var appkey = '${appkey}';
                Y.msgp.manage.data_portrait(appkey);
            });
        </script>
    </div>
</div>


