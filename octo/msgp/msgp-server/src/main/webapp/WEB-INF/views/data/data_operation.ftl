<title>服务业务指标</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_operation"></div>
<textarea id="text_data_operation" style="display:none">
    <div class="form-inline mb20">
        <div id="timeline" style="float: left;">
            时间段：
            [<a style="cursor:pointer;" data-value="1"> 1小时 </a>]
            [<a style="cursor:pointer;" data-value="2"> 2小时 </a>]
            [<a style="cursor:pointer;" data-value="4"> 4小时 </a>]
            [<a style="cursor:pointer;" data-value="6"> 6小时 </a>]
            [<a style="cursor:pointer;" data-value="8"> 8小时 </a>]
            [<a style="cursor:pointer;" data-value="12"> 12小时 </a>]
            [<a style="cursor:pointer;" data-value="24"> 24小时 </a>]
            [<a style="cursor:pointer;" data-value="48"> 48小时 </a>]
        </div>
        <div style="float: right">
            <span style="padding-left: 50px; color: grey;">业务指标功能开始逐渐迁移至CAT, 具体请参考 <a href="http://docs.sankuai.com/doc/arch_dp/cat/server/report/#business" target="_blank">CAT业务报表</a></span>
        </div>
    </div>
    <div class="form-inline mb20" style="padding-top: 50px;">
        <div class="control-group">
            <button id="add_metric" type="button" class="btn btn-primary" title="新增指标">
                <i class="fa fa-plus">新增</i>
            </button>
            <label style="padding-left:1em"> 开始时间：</label>
            <input id="start_time" type="text" class="span3" placeholder="查询开始时间">
            <label class="ml20"> 结束时间：</label>
            <input id="end_time" type="text" class="span3" placeholder="查询结束时间">
            <button id="query_btn" class="btn btn-primary ml20">查询</button>
            <a href="https://123.sankuai.com/km/page/28328055" target="_blank"
               style="padding-left:1em; float: right;">业务指标<i class="fa fa-question-circle"></i></a>
        </div>
    </div>

    <hr>

    <div class="app_screen" id="app_screen">
        <div id="screen_charts" class="clearfix" style="margin-top: 15px;">
        </div>
    </div>
</textarea>