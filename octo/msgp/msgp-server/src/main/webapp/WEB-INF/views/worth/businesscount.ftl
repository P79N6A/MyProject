<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_businesscount"></div>
<textarea id="text_businesscount" style="display:none">
  <table style="width: 100%">
    <tr>
      <td>
        <div id="searchForm" class="form-inline mb20">
          <div class="control-group" nowrap>
            <div id="count_type_select_business" class="btn-group">
              <a value="0" type="button" class="btn btn-primary" href="javascript:void(0)">PV</a>
              <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">UV</a></div>
            <label style="padding-left:1em">
              <strong>日期：</strong></label>
            <input id="start" type="text" class="span2" placeholder="开始日期">
            <span>-</span>
            <input id="end" type="text" class="span2" placeholder="结束日期">
            <label style="padding-left:1em">
              <strong>部门：</strong></label>
            <select id="business" placeholder="部门">
              <option value="-1">所有</option>
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
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button></div>
      </td>
      <td>
        <span id="tableTitle" style="color: #333;font-size: 14px;"></span>
      </td>
    </tr>
    <tr>
      <td style="vertical-align: top; width: 1000px;">
        <div id="businesscount_charts" class="charts-wrapper" style="width: 800px;height: 400px;border: 0"></div>
      </td>
      <td style="vertical-align: top;">
        <div id="count_list" style="width: 300px;"></div>
      </td
    </tr>
  </table>
</textarea>