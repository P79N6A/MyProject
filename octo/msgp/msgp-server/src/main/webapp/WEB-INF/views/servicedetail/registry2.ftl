<#if isEdit?exists && isEdit = true>
<title>服务修改</title>
<#else>
<title>服务注册</title>
</#if>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#if isEdit?exists && isEdit = true>
    <h3 class="page-header">修改服务:
        <input class="mb5 span6" type="text" id='apps_select' value='${appkey!""}' autocomplete="off"/>
    </h3>
<#else>

    <h3 class="page-header">服务注册</h3>
</#if>
    <div style="float: right;" id="yui_3_17_2_3_1454393720202_36">
        <a href="https://123.sankuai.com/km/page/28326379" target="_blank" id="yui_3_17_2_3_1454393720202_35">
            权限说明<i class="fa fa-question-circle"></i>
        </a>
    </div>
    <hr/>


    OCTO服务注册已和SRE服务树打通，后续请到<a title="点击注册" href="http://ops.sankuai.com/srvset/service/new/apply/">服务树注册</a>

    </div>

    <script type="text/javascript" src="/static/js/jquery.min.js"></script>
    <script type="text/javascript" src="/static/js/lodash.js"></script>
    <script>
        var isEdit = ${(isEdit?exists && isEdit==true)?string('true', 'false')};
        var appkey = '${appkey!""}';
        var owtsSize = ${owts?size};
        hasOwt = owtsSize>0?true:false;
    </script>
    <script type="text/javascript" src="/static/servicedetail/registry-1.1.js"></script>
    <script>
        $(function () {

        })
    </script>
</div>