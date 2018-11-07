<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_modelreport">
</div>
<textarea id="text_modelreport" style="display:none"><div id="searchForm" class="form-inline mb20">
        <div class="control-group">
            <label style="padding-left:1em">功能模块：</label>
            <select id="model" placeholder="功能模块">
                <option value="">所有</option>
                <option value="命名服务">命名服务</option>
                <option value="治理报告">治理报告</option>
                <option value="配置管理">配置管理</option>
                <option value="数据分析">数据分析</option>
                <option value="监控报警">监控报警</option>
                <option value="异常监控">异常监控</option>
                <option value="服务视图">服务视图</option>
                <option value="服务分组">服务分组</option>
                <option value="一键截流">一键截流</option>
                <option value="访问控制">访问控制</option>
                <option value="接口访问">接口访问</option>
                <option value="服务文档">服务文档</option>
                <option value="其它">其它</option>
            </select>

            <label style="padding-left:1em">子功能：</label>
            <input id="method" class="span2" placeholder="子功能" type="text" value="${method!""}"/>

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