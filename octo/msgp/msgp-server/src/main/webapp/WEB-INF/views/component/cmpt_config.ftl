<style>
    .common-popdialog .btn.btn-primary {
        background-color: #3fab99;
    }

    .common-popdialog .head h3 {
        background-color: #3fab99;
    }
    .common-popdialog {
        overflow: scroll;
    }

    .common-popdialog .btn.btn-primary:hover, .common-popdialog .btn.btn-primary:focus {
        background-color: #0D826E;
        color: #fff;
    }
</style>

<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_config"></div>

<textarea id="text_cmpt_config" style="display:none">
    <div id="configForm">
        <div id="searchForm" class="form-inline mb20">
             <div class="control-group" nowrap>
                 <label class="ml20" id="cmpt_label">组件标识：</label>
                 <input id="search_groupId" name="groupId" type="text"  title="" style="width: 180px;" placeholder="输入 groupId 关键字" />
                 <input id="search_artifactId" name="artifactId" type="text"  title="" style="width: 180px;" placeholder="输入 artifactId 关键字"/>
                 <label class="ml20" id="config_label">控制类型：</label>
                 <select id="search_action" name="search_action" title="search_action" style="width: 80px;">
                        <option value="all" selected = "selected">all</option>
                        <option value="warning">warning</option>
                        <option value="broken">broken</option>
                 </select>
                 <label class="ml20">事业群：</label>
                 <select id="search_business" name="search_business" title="search_business" style="width: 160px;">
                      <option value="" selected = "selected">all</option>
                      <option value="客服平台部">客服平台部</option>
                      <option value="到店综合事业群">到店综合事业群</option>
                      <option value="到店餐饮">到店餐饮</option>
                      <option value="平台事业群">平台事业群</option>
                      <option value="技术工程及基础数据平台">技术工程及基础数据平台</option>
                      <option value="外卖配送">外卖配送</option>
                      <option value="金融服务平台">金融服务平台</option>
                      <option value="餐饮生态">餐饮生态</option>
                      <option value="酒店旅游事业群">酒店旅游事业群</option>
                      <option value="猫眼电影">猫眼电影</option>
                      <option value="互联网+大学">互联网+大学</option>
                      <option value="广告平台">广告平台c
                      <option value="企业平台">企业平台</option>
                      <option value="其他">其他</option>
                  </select>
                 <label style="padding-left:1em; ">业务线：</label>
                 <select id="search_owt" name="search_owt" title="search_owt" style="width: 60px;"><option value="all">all</option></select>
                 <label style="padding-left:1em; ">产品线：</label>
                 <select id="search_pdl" name="search_pdl" title="search_pdl" style="width: 60px;"><option value="all">all</option></select>
                 <button class="btn btn-primary" type="button" id="search_config" style="margin-left: 20px;">查询</button>
             </div>
        </div>
         <div class="control-group">
            <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
        </div>
        <div>
            <div style="font-size: 0;margin-top: 10px;margin-bottom: 10px; float: left;">
                <div id="base_select" class="btn-group">
                    <a value="all" type="button" id="base_all" class="btn btn-small btn-base btn-primary"
                       href="javascript:void(0)">全部</a>
                    <a value="meituan" type="button" id="base_meituan" class="btn btn-small btn-base btn-default"
                       href="javascript:void(0)">北京</a>
                    <a value="dianping" type="button" id="base_dianping" class="btn btn-small btn-base btn-default"
                       href="javascript:void(0)">上海</a>
                </div>
                <button id="add_black_list" type="button" class="btn ml20 btn-small btn-add" title="新增一项"><i class="fa fa-plus">新增一项</i></button>
                <button id="del_all_black_list" class="btn btn-small btn-del" title="删除所选" style="margin-left: 10px;"><i class="fa fa-trash-o">删除所选</i></button>
            </div>
        </div>
        <div style=" height: atuo; padding-top:15px;" id="config_list_wrapper">
           <table class="table table-striped">
               <thead>
               <tr>
                   <th><input id="all-check" type="checkbox"></th>
                   <th width="6%">归属地</th>
                   <th width="12%">事业群</th>
                   <th width="7%">业务线</th>
                   <th width="7%">产品线</th>
                   <th width="20%">组件名称</th>
                   <th width="16%">目标版本</th>
                   <th width="14%">控制类型</th>
                   <th width="14%" colspan="2">操作</th>
               </tr>
               </thead>
               <tbody>
               </tbody>
           </table>
        </div>
    </div>
</textarea>

<textarea id="text_add_black_list_form" style="display:none">
    <div id="add_black_list_form" class="form-horizontal">
         <div class="control-group">
            <label class="control-label" style="color: #4fbba9;">常用组件</label>
            <div class="controls">
                <select id="black_list_cmpt" name="black_list_cmpt" type="text" style="width: 214px;" placeholder="快速填写groupId和artifactId" ></select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">GroupId</label>
            <div class="controls">
                <input id="black_list_groupId" name="groupId" type="text" style="width: 214px;" placeholder="1. 输入 groupId" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">ArtifactId</label>
            <div class="controls">
                <input id="black_list_artifactId" name="artifactId" type="text" style="width: 214px;" placeholder="2. 输入 artifactId"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Version</label>
            <div class="controls">
                <select id="black_list_version" name="black_list_version" style="width: 228px;" placeholder="3. 选择最低要求的版本"></select>
                <span style="color: gray;"><a id="show-black_list_version-input" href="javascript:;" style="width: 100%;"><i class="fa fa-plus"></i> 点此手动输入</a></span>
            </div>
        </div>
        <div class="control-group" id="black_list_version-input-line" style="display: none;">
            <label class="control-label" style="display: none;">Version</label>
            <div class="controls">
                <input id="black_list_version-input" style="width: 224px;" placeholder="请输入版本号(支持区间)">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">控制类型</label>
            <div class="controls">
                 <select id="black_list_action" name="black_list_action" style="width: 228px;"> <option value="warning" selected="selected">warning(低版本提醒)</option> <option value="broken">broken(低版本中止发布)</option> </select>
            </div>
        </div>
        <div class="control-group">
            <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
        </div>
        <div class="control-group">
            <label class="control-label">归属地</label>
            <div class="controls">
                 <select id="black_list_base" name="black_list_base" title="black_list_base" style="width: 228px;">
                     <option value="meituan">北京</option>
                     <option value="dianping">上海</option>
                 </select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">事业群</label>
            <div class="controls" >
                <select id="black_list_business" name="black_list_business" title="black_list_business" style="width: 228px;">
                <option value="all" selected = "selected">all</option>
                <option value="客服平台部">客服平台部</option>
                <option value="到店综合事业群">到店综合事业群</option>
                <option value="到店餐饮">到店餐饮</option>
                <option value="平台事业群">平台事业群</option>
                <option value="技术工程及基础数据平台">技术工程及基础数据平台</option>
                <option value="外卖配送">外卖配送</option>
                <option value="金融服务平台">金融服务平台</option>
                <option value="餐饮生态">餐饮生态</option>
                <option value="酒店旅游事业群">酒店旅游事业群</option>
                <option value="猫眼电影">猫眼电影</option>
                <option value="互联网+大学">互联网+大学</option>
                <option value="广告平台">广告平台</option>
                <option value="企业平台">企业平台</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">业务线</label>
            <div class="controls">
                 <select id="black_list_owt" name="black_list_owt" title="black_list_owt" style="width: 228px;"><option value="all">all</option></select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">产品线</label>
            <div class="controls">
                 <select id="black_list_pdl" name="black_list_pdl" title="black_list_pdl" style="width: 228px;"><option value="all">all</option></select>
            </div>
        </div>
    </div>
</textarea>

<textarea id="text_add_white_list_form" style="display:none">
    <div id="add_white_list_form" class="form-horizontal" style="text-align: center; padding: 20px 0 0 0">
        <table width="100%">
            <tr>
                <td style="text-align: left; width: 100%;">
                    <div class="form-inline mb20">
                        <label class="mr10">事业群: </label>
                        <select id="white_list_business" name="white_list_business" title="white_list_business" style="width: 160px;">
                            <option value="客服平台部">客服平台部</option>
                            <option value="到店综合事业群">到店综合事业群</option>
                            <option value="到店餐饮">到店餐饮</option>
                            <option value="平台事业群">平台事业群</option>
                            <option value="技术工程及基础数据平台">技术工程及基础数据平台</option>
                            <option value="外卖配送">外卖配送</option>
                            <option value="金融服务平台">金融服务平台</option>
                            <option value="餐饮生态">餐饮生态</option>
                            <option value="酒店旅游事业群">酒店旅游事业群</option>
                            <option value="猫眼电影">猫眼电影</option>
                            <option value="互联网+大学">互联网+大学</option>
                            <option value="广告平台">广告平台</option>
                            <option value="企业平台">企业平台</option>
                        </select>
                        <label class="mr10 ml10">业务线: </label>
                        <input id="white_list_owt" name="white_list_owt" title="white_list_owt" style="width: 110px;" placeholder="添加所有请填all">
                        <label class="mr10 ml10">产品线: </label>
                        <input id="white_list_pdl" name="white_list_pdl" title="white_list_pdl" style="width: 110px;" placeholder="添加所有请填all">
                        <label class="mr10 ml10">项目名: </label>
                        <input id="white_list_app" name="white_list_app" style="width: 110px;" placeholder="添加所有请填all"/>
                        <button class="btn btn-primary" type="button" id="add_white_list_item" style="margin-left: 20px;">添加白名单</button>
                    </div>
                </td>
            </tr>
            <tr>
                <td style="text-align: left; font-size: 12px; color: #CD5F15;">
                    <div>
                        <ul>
                            <li style="list-style-type:none; line-height: 24px;">
                                1, 事业群、业务线、产品线、项目名均不能为空;
                            </li>
                            <li style="list-style-type:none; line-height: 24px;">
                                2, 若选择所有,则填写all, 否则填有效值;
                            </li>
                            <li style="list-style-type:none; line-height: 24px;">举例: 若要添加inf下所有产品线到白名单, 则业务线填inf, 产品线填all, 项目名填all。</li>
                        <ul>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div class="control-group">
                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
                    </div>

                </td>
            </tr>
            <tr>
                <td>
                    <div style=" height: atuo; padding:15px 0 40px 0;" id="config_white_list_wrapper">
                       <table class="table table-striped">
                           <thead>
                           <tr>
                               <th width="25%">事业群</th>
                               <th width="15%">业务线</th>
                               <th width="15%">产品线</th>
                               <th width="30%">项目名称</th>
                               <th width="15%">删除配置</th>
                           </tr>
                           </thead>
                           <tbody>
                           </tbody>
                       </table>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</textarea>