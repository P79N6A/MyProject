<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_orgreport">
</div>
<textarea id="text_orgreport" style="display:none"><div id="searchForm" class="form-inline mb20">
    <div class="control-group">
        <label style="padding-left:1em">组织部门</label>
        <select id="business" placeholder="组织部门">
            <option value="">所有</option>
            <option value="0">到店事业群</option>
            <option value="1">技术工程部</option>
            <option value="2">猫眼电影</option>
            <option value="3">创新业务部</option>
            <option value="4">酒店旅游事业群</option>
            <option value="5">外卖配送事业群</option>
            <option value="6">云计算部</option>
            <option value="7">金融发展部</option>
            <option value="8">支付平台部</option>
            <option value="9">智能餐厅部</option>
            <option value="10">IT工程部</option>
            <option value="100">其他</option>
        </select>
        <label id="label_text"></label>

        <label style="padding-left:1em">日期：</label>
        <input id="day" type="text" class="span2" placeholder="天">
        <label style="padding-left:1em">时间类型：</label>
        <select id="dtype" class="span1">
            <option value="0">天</option>
            <option value="1">周</option>
            <option value="2">月</option>
        </select>
        <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    </div>
</div>

    <div id="table_wrapper" class="table-responsive mt20">
        <table class="table table-striped table-hover">
            <thead>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div id="paginator_wrapper">
    </div></textarea>

<textarea id="text_org_owt" style="display:none">
                 <label style="padding-left:1em">业务线：</label>
                <input id="owt" class="span2" placeholder="业务线" type="text" value="${owt!""}"/>
            </textarea>

<textarea id="text_org_user" style="display:none">
                <label style="padding-left:1em">用户名：</label>
                <input id="username" class="span2" placeholder="用户" type="text" value="${username!""}"/>
            </textarea>

<textarea id="text_org_model" style="display:none">
                <label style="padding-left:1em">功能模块：</label>
                <select id="model" placeholder="功能模块">
                    <option value="">所有</option>
                    <option value="命名服务">命名服务</option>
                    <option value="配置管理">配置管理</option>
                    <option value="数据分析">数据分析</option>
                    <option value="监控报警">监控报警</option>
                    <option value="异常监控">异常监控</option>
                    <option value="服务视图">服务视图</option>
                    <option value="服务分组">服务分组</option>
                    <option value="过载保护">过载保护</option>
                    <option value="访问控制">访问控制</option>
                    <option value="接口访问">接口访问</option>
                    <option value="服务文档">服务文档</option>
                    <option value="治理报告">治理报告</option>
                    <option value="其它">其它</option>
                </select>
            </textarea>