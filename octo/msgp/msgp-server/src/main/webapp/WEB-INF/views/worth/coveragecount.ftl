<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_coveragecount"></div>
<script type="text/javascript" src="/static/js/jquery-2.2.3.min.js"></script>
<script type="text/javascript" src="/static/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="/static/js/jquery.validate.min.js"></script>
<script type="text/javascript" src="/static/js/messages_zh.min.js"></script>
<script type="text/javascript" src="/static/js/socket.io-1.4.5.js"></script>
<script type="text/javascript" src="/static/js/bootstrap-multiselect.js"></script>
<link rel="stylesheet" type="text/css" href="/static/css/bootstrap-multiselect.css" />
<textarea id="text_coveragecount" style="display:none">
  <table>
    <tr>
      <td style="vertical-align: top; width: 1100px">
          <div id="searchForm" class="form-inline mb20" style="margin-bottom: 20px">
            <div class="control-group" nowrap>
              <label style="padding-left:1em">
                <strong>日期：</strong></label>
              <input id="start" type="text" class="span2" placeholder="开始日期">
              <span>-</span>
              <input id="end" type="text" class="span2" placeholder="结束日期">
              <label class="ml20">
                <strong>职位：</strong></label>
              <select id="dev" name="dev" title="职位类型" multiple="multiple">
                <option value="TRACE">算法开发</option>
                <option value="INFO">系统开发</option>
                <option value="WARN">后台开发</option>
                <option value="ERROR">前端开发</option>
                <option value="FATAL">运维</option>
                <option value="OTHER">QA</option></select>
              <label style="padding-left:1em">
                <strong>部门：</strong></label>
              <span id="business" style="display: inline-block; vertical-align:middle; border-radius: 4px; width: 180px; height: 28px; border: 1px solid #ccc;line-height: 28px; text-indent: 5px; white-space: nowrap;"></span>
              <button class="btn btn-primary" type="button" id="searchBtn" style="margin-left: 20px;">查询</button></div>
          </div>

      </td>
      <td>
        <strong>
          <span id="selecttips" style="display: inline-block; vertical-align:middle; width: 180px; height: 28px; line-height: 28px; text-indent: 5px; white-space: nowrap;"></span>
        </strong>
      </td>
    </tr>
    <tr style="vertical-align: top;">
        <td style="width: 1000px;">
            <div id="coveragecount_charts" class="charts-wrapper" style="width: 800px;height: 400px;border: 0;"></div>
        </td>
        <td>
            <div id="orgtree" style="width: 290px; float: left; height: auto"></div>
        </td>
    </tr>
  </table>
</textarea>