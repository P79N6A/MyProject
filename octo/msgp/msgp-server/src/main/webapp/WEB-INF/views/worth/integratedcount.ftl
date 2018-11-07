<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_integratedcount"></div>
<textarea id="text_integratedcount" style="display:none">
  <div id="searchForm" class="form-inline mb20">
    <div class="control-group" nowrap>
      <label style="padding-left:1em">
        <strong>日期：</strong></label>
      <input id="start" type="text" class="span2" placeholder="开始日期">
      <label style="padding-left:1em">
        <strong>模块：</strong></label>
      <select id="module" placeholder="模块" style="width: 120px">
        <option value="-1">选择全部</option>
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
      <label style="padding-left:1em">
        <strong>功能：</strong></label>
      <select id="function_desc" placeholder="功能" style="width: 120px">
        <option value="-1">选择全部</option></select>
      <label style="padding-left:1em">
        <strong>部门：</strong></label>
      <select id="business" class="selector" placeholder="部门" style="width: 180" >
        <option value="-1">选择全部</option>
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
       <label class="control-label" style="padding-left: 1em;">
              <strong>用户：</strong></label>

        <div style="display: inline-block">
        <input id="user-list" type="text" class="user" value="${userName!''}" placeholder="输入 中文|拼音|首拼 搜索"/>
        <input id="user-id" type="hidden" name="user-id" value="${userId!''}"/></div>

        <button class="btn btn-primary" type="button" id="searchBtn">查询</button></div>
  </div>
  <div id="count_list" class="clearfix"></div>
    <div id="paginator_monitor"></div>
    <div id="paginator_wrapper" style="padding-right: 50px;"></div>
</textarea>