<script src="/static/js/echarts3/echarts.common.min.js"></script>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="form-inline mb20">
        <div id="all_or_one" class="btn-group">
            <a value="1" type="button" id="supplier_type" class="btn btn-default" href="javascript:void(0)">单组件</a>
            <a value="2" type="button" id="supplier_type" class="btn btn-primary" href="javascript:void(0)">整体</a>
        </div>
    </div>
</div>
<div id="allAvailability"></div>
<div id = "oneAvailability">
    <h2>MCC SERVER
    </h2>
    <div id="availabilityChartOuterOfMccServer" class="clearfix">
        <div class="charts-wrapper-out" style="border:1px solid #8a8a8a;width:1225px;margin:10px 50px 30px;">
            <div id="showAvailabilityOfMccServer" style="float:left;height: 400px;width:1000px;position:relative;">
            </div>
            <div id="averageAvailabilityOfMccServer" style="position:relative;margin-top: 120px">
            </div>
        </div>
    </div>

    <h2>MNSC
    </h2>
    <div id="availabilityChartOuterOfMnsc" class="clearfix">
        <div class="charts-wrapper-out" style="border:1px solid #8a8a8a;width:1225px;margin:10px 50px 30px;">
            <div id="showAvailabilityOfMnsc" style="float:left;height: 400px;width:1000px;position:relative;">
            </div>
            <div id="averageAvailabilityOfMnsc" style="position:relative;margin-top: 120px">
            </div>
        </div>
    </div>
</div>

