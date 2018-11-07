<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_modulecount"></div>
<textarea id="text_modulecount" style="display:none">
  <table style="width: 100%">
    <tr>
      <td>
        <div id="searchForm" class="form-inline mb20">
          <div class="control-group" nowrap>
            <div id="count_type_select_module" class="btn-group">
              <a value="0" type="button" class="btn btn-primary" href="javascript:void(0)">PV</a>
              <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">UV</a></div>
            <label style="padding-left:1em">
              <strong>日期：</strong></label>
            <input id="start" type="text" class="span2" placeholder="开始日期">
            <span>-</span>
            <input id="end" type="text" class="span2" placeholder="结束日期">
            <label style="padding-left:1em">
              <strong>模块：</strong></label>
            <select id="module" placeholder="模块">
              <option value="-1">所有</option>
              <option value="0">命名服务</option>
              <option value="1">配置管理</option>
              <option value="2">数据分析</option>
              <option value="3">监控报警</option>
              <option value="4">异常监控</option>
              <option value="5">服务视图</option>
              <option value="6">服务文档</option>
              <option value="7">服务分组</option>
              <option value="8">一键截流</option>
              <option value="9">访问控制</option>
              <option value="10">接口访问</option>
              <option value="11">治理报告</option>
              <option value="13">组件依赖</option>
              <option value="12">其他</option></select>
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button></div>
        </div>
      </td>
      <td>
        <span id="tableTitle" style="color: #333;font-size: 14px;"></span>
      </td>
    </tr>
    <tr style="text-align: center;">
      <td style="vertical-align: top; width: 1000px;" >
        <div id="modulecount_charts" class="charts-wrapper" style="width: 800px;height: 400px;border: 0"></div>
      </td>
      <td style="vertical-align: top;">
        <div id="count_list" style="width: 300px;"></div>
      </td>
    </tr>
</textarea>