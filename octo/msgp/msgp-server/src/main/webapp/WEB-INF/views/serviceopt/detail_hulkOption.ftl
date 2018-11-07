<div class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="content-body">
    <div style="float: left;">
        <div id="hulk_or_manu" class="btn-group">
            <a value="0" id="hulk_scale" type="button" class="btn btn-primary"
               href="javascript:void(0)">弹性伸缩</a>
            <a value="1" id="manu_scale" type="button" class="btn btn-default"
               href="javascript:void(0)">一键扩(缩)容</a>
        </div>
    </div>
    <div class="hulkContent">
        <div id="wrap_hulkscale">
        <#include "detail_hulkPolicy.ftl" >
        </div>
        <div id="wrap_manuscale">
        <#include "detail_manuScaleOut.ftl" >
        </div>
    </div>
</div>
