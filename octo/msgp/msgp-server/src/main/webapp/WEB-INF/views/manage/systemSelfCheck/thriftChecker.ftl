<div class="content-body">
    <div id="searchForm" class="form-inline mb20">
        <div class="control-group" nowrap>
            <label class="ml20">事业群：</label>
            <select id="business" name="business" title="business" style="width: 214px;">
                <option value="-1" selected = "selected">all</option>
                <option value="0">到店餐饮事业群</option>
                <option value="1">技术工程及基础数据平台</option>
                <option value="2">猫眼电影</option>
                <option value="4">酒店旅游事业群</option>
                <option value="5">外卖配送事业群</option>
                <option value="7">金融服务平台</option>
                <option value="10">企业平台研发部</option>
                <option value="11">广告平台</option>
                <option value="12">平台事业群</option>
                <option value="13">到店综合事业群</option>
                <option value="14">餐饮生态</option>
                <option value="100">其他</option></select>
                <label class="ml20">业务线：</label>
            <select id="owt" name="owt" title="owt" style="width: 214px;"><option value="all">all</option></select>

            <label class="ml20">产品线：</label>
            <select id="pdl" name="pdl" title="pdl" style="width: 214px;"><option value="all">all</option></select>

        </div>
    </div>
    <div>
        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
    </div>

    <div class="dashboard-wrap" id="charts_wrapper">
        <fieldset>
            <ul class="clearfix">
                <li>
                    <div id="mtthrift_self_check_version" class="dashboard-inner"></div>
                </li>
                <li>
                    <div id="cthrift_self_check_version" class="dashboard-inner"></div>
                </li>
            </ul>
        </fieldset>
    </div>

    <div class="dashboard-wrap" id="waiting_message_wrapper"></div>

    <script type="text/javascript" src="/static/js/jquery.min.js"></script>

</div>