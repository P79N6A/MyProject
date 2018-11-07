<script src="/static/js/echarts3/echarts.common.min.js"></script>
<title>订阅dashboard</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_data_dashboard"></div>
<textarea id="text_data_dashboard" style="display:none">
    <h2>实时性能数据</h2>
    <div id="charts_outer" class="clearfix" style="margin-top: 15px;">
    </div>
    <br/>
    <h2>可用率数据
    <a style="float:right; display: inline;padding-left:1em;font-size: 15px" target="_blank" href="https://123.sankuai.com/km/page/28327894" >可用率指标<i class="fa fa-question-circle"></i></a>
    </h2>
    <div id="availabilityChartOuter" class="clearfix">
        <div class="charts-wrapper-out" style="border:1px solid #8a8a8a;width:1225px;margin:10px 50px 30px;">
            <div id="showAvailability" style="float:left;height: 400px;width:1000px;position:relative;">
            </div>
            <div id="averageAvailability" style="position:relative;margin-top: 120px">
            </div>
        </div>
    </div>
    <br/>
    <h2>关键指标</h2>
    <div id="key_metric" class="table-responsive">
        <table id="falcon_alarm" class="table table-striped table-hover">
            <colgroup>
                <col width="30%"></col>
                <col width="30%"></col>
                <col width="40%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>时间</th>
                <th>服务树节点</th>
                <th>falcon未恢复报警总数</th>
            </tr>
            </thead>
            <tbody>
            <tr id="content_overlay">
                <td colspan="11">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>
        <br/>

        <table id="perf" class="table table-striped table-hover">
            <colgroup>
                <col width="10%"></col>
                <col width="15%"></col>
                <col width="15%"></col>
                <col width="15%"></col>
                <col width="15%"></col>
                <col width="15%"></col>
                <col width="15%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>时间</th>
                <th>集群QPS</th>
                <th>单机平均QPS</th>
                <th>单机最大QPS</th>
                <th>集群TP90</th>
            <#--<th>Falcon告警数</th>-->
                <th>性能报警数</th>
                <th>ERROR日志数</th>
            </tr>
            </thead>
            <tbody>
            <tr id="content_overlay">
                <td colspan="11">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>

        <br/>

        <table id="key" class="table table-striped table-hover">
            <colgroup>
                <col width="30%"></col>
                <col width="30%"></col>
                <col width="40%"></col>
            </colgroup>
            <thead>
            <tr>
                <th>前五高load节点</th>
                <th>前五fullGC数节点</th>
                <th>前五GC数节点</th>
            </tr>
            </thead>
            <tbody>
            <tr id="content_overlay">
                <td colspan="11">
                    <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</textarea>
