<title xmlns="http://www.w3.org/1999/html">性能指标</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_performance"></div>
<textarea id="text_data_performance" style="display:none">
    <div class="form-inline mb20" style="padding: 0;">
        <div class="control-group">
            <span style="padding-left:1em">角色：</span>

            <div id="server_select" class="btn-group">
                <a value="server" type="button" class="btn <#if (!role?? || role='server')>btn-primary</#if>"
                   href="javascript:void(0)">来源</a>
                <a value="client" type="button" class="btn <#if (role?exists && role='client')>btn-primary</#if>"
                   href="javascript:void(0)">去向</a>
            </div>
            <a style="margin-left:1em" id="env_tooltip" href="https://123.sankuai.com/km/page/28253677">环境<i
                    class="fa fa-question-circle"></i>：</a>
            <div id="performance_env_select" class="btn-group">
                <a value="prod" type="button" class="btn <#if (!env?? || env='prod')>btn-primary</#if>"
                   href="javascript:void(0)">prod</a>
                <a value="stage" type="button" class="btn <#if (env?exists && env='stage')>btn-primary</#if>"
                   href="javascript:void(0)">stage</a>
                <#if isOffline>
                    <a value="test" type="button" class="btn <#if (env?exists && env='test')>btn-primary</#if>"
                       href="javascript:void(0)">test</a>
                </#if>
            </div>

            <label style="padding-left:1em">时间粒度：</label>

            <div id="time_unit_select" class="btn-group">
                <a value="day" type="button" class="btn <#if (!timeUnit?? || timeUnit='day')>btn-primary</#if>"
                   href="javascript:void(0)">天</a>
                <a value="hour" type="button" class="btn <#if (timeUnit?exists && timeUnit='hour')>btn-primary</#if>"
                   href="javascript:void(0)">小时</a>
            </div>

            <label style="padding-left:1em">时间：</label>
            <input id="day" type="text" class="span2" placeholder="时间" <#if (timeUnit?exists && timeUnit='hour')>
                   style="display:none; width: 160px;" </#if>
                   value="${day?default(.now?string('yyyy-MM-dd'))}">

            <input id="hour" type="text" class="span3" placeholder="时间" <#if (!timeUnit?? || timeUnit='day')>
                   style="display:none; width: 160px;" </#if>
                   value="${hour?default(.now?string('yyyy-MM-dd HH:00:00'))}">
            <button id="query_btn" class="btn btn-primary ml20">查询</button>
            <a target="_blank"
               style="padding-left:1em" id="indicator" href="https://123.sankuai.com/km/page/28327894" >可用率指标<i class="fa fa-question-circle"></i></a>
            <a target="_blank"
               style="padding-left:1em" id="time-consuming" href="https://123.sankuai.com/km/page/28354578">TP耗时数据<i class="fa fa-question-circle"></i></a>
            <a target="_blank"
               style="padding-left:1em" id="comparePre" href="https://123.sankuai.com/km/page/28354844">
                同比环比<i class="fa fa-question-circle"></i>
            </a>
        </div>
    </div>
    <hr>
    <div id="charts_all" class="clearfix">
        <div style="padding-bottom: 20px; padding-left: 1em;">
<span style="color:#666666;"><span style="color:#333333;">说明</span><span style="color:#333333;">：</span><strong><span style="color:#000000;">1</span></strong>，QPS，Tp99被表示为：[<em>数值，环比， 同比</em>] ，<span style="color:#666666;">客户端统计量被表示为：</span><span style="color:#666666;">[</span><em>调用量，失败数， 失败数占比</em><span style="color:#666666;">]&nbsp;</span>。<span style="color:#000000;"><strong>2</strong></span>, 耗时指标(eg. 平均耗时,TP数据)的单位为毫秒；<span style="color:#000000;"><strong>3</strong></span>，可用率相关指标说明见上方WIKI。 <span style="color:#000000;"><strong>4</strong></span>，avg:平均耗时  max:最大耗时       </div>
        <div id="kpi_table_wrapper" class="clearfix""></div>
    </div>
    <textarea id="text_graph" style="display:none">
        <div class="charts-wrapper-out">
            <div id="screen_availability" class="charts-wrapper" style="width: 400px;height: 320px;border: 0"></div>
        </div>
        <div class="charts-wrapper-out">
            <div id="screen_count" class="charts-wrapper" style="width: 400px;height: 320px;border: 0"></div>
        </div>
        <div class="charts-wrapper-out">
            <div id="screen_qps" class="charts-wrapper" style="width: 400px;height: 320px;border: 0"></div>
        </div>
    </textarea>
</textarea>
