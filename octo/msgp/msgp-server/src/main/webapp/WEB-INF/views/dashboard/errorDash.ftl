<style>
    .sorttable-header-bg:hover, .sorttable-header-asc, .sorttable-header-desc {
         background: #ddd;
         cursor: pointer;
    }
</style>
<title>异常记录</title>
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
            <li class="current"><a href="/error">报错大盘</a></li>
            <li><a href="/database">数据库大盘</a></li>
            <li><a href="/tair">缓存大盘</a></li>

        </ul>
    </div>
</div>

<div id="triggers_wrap">
    <div class="content-body">
        <div class="form-inline mb20" style="padding:10px 0px 0px">
            <div id="timeline"  class="timeline-css" >时间段:[<a style="cursor:pointer" data-value="1"> 1分钟 </a>]
                [<a style="cursor:pointer" data-value="5"> 5分钟 </a>] [<a style="cursor:pointer" data-value="10"> 10分钟 </a>]
                [<a style="cursor:pointer" data-value="15"> 15分钟 </a>] [<a style="cursor:pointer" data-value="20"> 20分钟 </a>] [<a style="cursor:pointer" data-value="30"> 30分钟 </a>]  [<a style="cursor:pointer" data-value="40"> 40分钟 </a>] [<a style="cursor:pointer" data-value="50"> 50分钟 </a>]
                [<a style="cursor:pointer" data-value="60"> 一小时 </a>]</div>
            <div class="control-group">
                <label style="padding-left:1em"> 开始时间：</label>
                <input id="start_time" type="text" class="span3" style="color: #858585" placeholder="查询开始时间" value="${start}">
                <label class="ml20"> 结束时间：</label>
                <input id="end_time" type="text" class="span3"  style="color: #858585" placeholder="查询结束时间" value="${end}">
                <button id="query_btn" class="btn btn-primary ml20">查询</button>
            </div>

            <div style="float: right;margin-bottom: 20px">
                <span>汇聚了服务的异常监控，点击报警数查看详情。</span>
                <a href="https://123.sankuai.com/km/page/28355034" target="_blank">报错大盘使用说明<i
                        class="fa fa-question-circle"></i></a>
            </div>
        </div>

        <table class="table table-striped table-hover" id="falcon_alarm">
            <thead>
            <tr>
                <th>部门</th>
                <th>服务</th>
                <th id="falcon_count" class="sorttable-header-bg">Falcon异常报警数<i id="falcon_count_i" class="fa fa-sort-desc"></i></th>
                <th id="octo_count"   class="sorttable-header-bg">Octo性能报警数<i  id="octo_count_i"class="fa fa-sort"></i></th>
            </tr>
            </thead>
            <tbody>
            <tr id="content_overlay">
                <td colspan="5">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <script type="text/javascript" src="/static/dashboard/error.js"></script>
</div>