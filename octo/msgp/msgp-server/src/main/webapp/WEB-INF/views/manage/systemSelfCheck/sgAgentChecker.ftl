<div class="content-body" id = "wrapper_sg_version_outline">
    <div class="form-inline mb20">
        <a href="javascript:void(0)">分布类型：</a>
        <div id="sgAgent_version_region" class="btn-group">
            <button value="all" type="button" class="btn btn-primary">全部</button>
            <button value="beijing" type="button" class="btn btn-default">北京</button>
            <button value="shanghai" type="button" class="btn btn-default">上海</button>
            <button value="hulk" type="button" class="btn btn-default">hulk</button>
        </div>
        <div style="float: right;">
            <a id = "mnszkfaclonurl" href="http://falcon.sankuai.com/screen/4378" target="_blank">
                facon监控
            </a>
        </div>
    </div>
    <div class="dashboard-wrap">
        <fieldset>
            <ul class="clearfix">
                <li>
                    <div id="sg_agent_prod_check_version" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="sg_agent_stage_check_version" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="sg_agent_test_check_version" class="dashboard-inner"></div>
                </li>


            </ul>
        </fieldset>
    </div>
</div>
<div id="wrapper_sg_version_detail" style="display: none;">
<#include "sgAgentVersionDetail.ftl"/>
</div>


<div class="overlay-mask-process"></div>