<div class="content-overlay">
    <i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span>
</div>
<div class="content-body" style="display:none;">
    <div class="form-inline mb20">
        <button id="refresh_supplier" type="button" class="btn btn-primary" title="刷新列表">
            <i class="fa fa-refresh"></i>
        </button>
        <button id="add_supplier" type="button" class="btn btn-primary" title="新增提供者">
            <i class="fa fa-plus">新增配置</i>
        </button>

        <#include "/common/env.ftl" >
        <div id="supplier_env_select" class="btn-group">
            <a value="3" type="button" class="btn btn-primary" href="javascript:void(0)">prod</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)">stage</a>
            <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">test</a>
        </div>

        <div style="float: right;">
            <a href="https://123.sankuai.com/km/page/28210757" target="_blank">
                过载保护使用说明书<i class="fa fa-question-circle"></i>
            </a>
        </div>

    </div>
    <div class="form-inline mb20">
    </div>
    <div>
    </div>
    <div id="table_supplier" class="table-responsive">
        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <th hidden>id</th>
                <th hidden>环境</th>
                <th>方法</th>
                <th>节点计算方式</th>
                <th>总配额(单节点配额*节点数)</th>
                <th>监控周期</th>
                <th>报警</th>
                <th>自动降级</th>
                <#--<th>过载保护方式</th>-->
                <th>配置详情(消费者;配额比例;降级策略)</th>
                <th >提供者</th>
                <th >消费者</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="10">Loading contents...</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="paginator_quota">
    </div>
</div>