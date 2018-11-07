<title>业务大盘</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="page-header">
        <div class="input-append">
            <input id="search_input" class="span6" type="text" placeholder="appkey、负责人、标签，为空表示查询全部"/>
            <button id="search_button" class="btn btn-primary" type="button">查询服务</button>
        </div>
        <script type="text/javascript" src="/static/dashboard/bindSearch.js"></script>
        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28178492" target="_blank">
                服务如何接入OCTO<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
    <div class="tab-box">
        <ul class="nav nav-tabs widget-edit-tab">
            <li><a href="/personal">个人中心</a></li>
            <li><a href="/subscribe">订阅中心</a></li>
            <li><a href="/error">报错大盘</a></li>
            <li><a href="/database">数据库大盘</a></li>
            <li><a href="/tair">缓存大盘</a></li>
        </ul>
    </div>
</div>

<div id="triggers_wrap">

    <div class="content-body">
        <div class="form-inline" style="padding:10px 0px 0px">
            <div class="control-group">
                <label class="business-css" id="timeline" style="padding:0px 0px 10px;padding-left:2em">
                    时间段：[<a style="cursor:pointer;" data-value="1"> 1小时 </a>]
                    [<a style="cursor:pointer;" data-value="2"> 2小时 </a>]
                    [<a style="cursor:pointer;" data-value="4"> 4小时 </a>]
                    [<a style="cursor:pointer;" data-value="6"> 6小时 </a>]
                    [<a style="cursor:pointer;" data-value="8"> 8小时 </a>]
                    [<a style="cursor:pointer;" data-value="12"> 12小时 </a>]
                    [<a style="cursor:pointer;" data-value="24"> 24小时 </a>]
                    [<a style="cursor:pointer;" data-value="48"> 48小时 </a>]
                </label>
                <div style="float: right;margin-bottom: 20px">
                    <a href="https://123.sankuai.com/km/page/28328055" target="_blank"
                       style="padding-left:1em">业务指标&业务大盘<i class="fa fa-question-circle"></i></a>
                </div>
            </div>
        </div>

        <div class="form-inline mb20" style="padding:0px 0px 0px">
            <div class="control-group">
                <label style="padding-left:1em">选择业务线：
                    <input class="mb5 span3" type="text" id='owtSelect' value='' autocomplete="off"/>
                    <input type="hidden" class="f-text" id='owtSelect_hidden' value="">
                </label>

                <button style="margin-bottom: 4px" id="add_metric" type="button" class="btn btn-primary" title="配置">
                    <i class="fa fa-edit">配置</i>
                </button>

                <label style="padding-left:1em"> 开始：</label>
                <input id="start_time" type="text" style="color: #858585; width: 135px" placeholder="查询开始时间"
                       value="${start}">
                <label class="ml20"> 结束：
                </label>
                <input id="end_time" type="text" style="color: #858585; width: 135px" placeholder="查询结束时间"
                       value="${end}">
                <button id="query_btn" class="btn btn-primary ml20">查询</button>
            </div>
        </div>
    </div>

    <div class="app_screen" id="app_screen">
        <div id="screen_charts" class="clearfix" style="margin-top: 15px;">
        </div>
    </div>

    <script>
        var businessDash = {
            owtList : [<#list owtList![] as owt>'${owt}',</#list>],
            owt : '${owt!""}'
        }

        <#--var timeRange = '${timeRange!"4"}';-->
    </script>
    <script type="text/javascript" src="/static/dashboard/businessDash.js"></script>
</div>