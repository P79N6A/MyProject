<style>
    .overview {
        display:inline-block;
        width: 360px;
        height: 280px;
        overflow: hidden;
        margin: 30px 20px 0 0;
        cursor:pointer;
        /*border:1px solid #868686;*/
        position:relative;
        z-index:998
    }
</style>
<div>
    <div class="form-inline mt20">
        <a>类型：</a>

        <div id="supplier_thrift_http" class="btn-group">
            <a value="thrift" type="button" class="btn btn-default" href="javascript:void(0)">thrift</a>
            <a value="http" type="button" class="btn btn-default" href="javascript:void(0)">http</a>
            <a value="-1" type="button" class="btn btn-default" href="javascript:void(0)">所有</a>
        </div>

    <#include "/common/env.ftl" >

        <div id="supplier_env_idc_select" class="btn-group">
            <a value="3" type="button" class="btn btn-default" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
            <a value="-1" type="button" class="btn btn-default" href="javascript:void(0)">所有</a>
        </div>
        <div class="btn-group">
            <input id="idcAppkey" class="span2" placeholder="输入服务唯一标识查询" type="text" style="width:300px" value="">
        </div>
        <button id="idcSearchBtn" class="btn" type="button" style="margin-left:5px;">
            查询
        </button>
        <div>
            <div class="overview" id="outlineIdcTypeChart"></div>
            <div class="overview" id="outlineIdcEnvChart"></div>
            <div class="overview" id="outlineIdcChart"></div>
        </div>
    </div>
    <div id="dashboard_status_table" class="table-responsive mt20" >
        <table class="table table-striped table-hover">
            <colgroup>
                <col width="35%"></col>
                <col width="25%"></col>
                <col width="20%"></col>
                <col width="20%"></col>
            </colgroup>
            <thead>
            <tr>
                <!-- 唯一标识，可选tag：业务线、发布系统key、主机名前缀、其他key -->
                <th>唯一标识</th>
                <th>机房</th>
                <th>节点数</th>
                <th>机器数</th>
            </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
    </div>
</div>
<div id="paginator_dashboard_idc" style = "padding-bottom: 30px;">
</div>