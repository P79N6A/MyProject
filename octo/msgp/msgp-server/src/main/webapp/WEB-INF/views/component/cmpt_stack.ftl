<title>部门组件技术栈</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_stack"></div>
<textarea id="text_cmpt_stack" style="display:none">
    <div id="searchForm" class="form-inline mb20">
    <table style="width: 100%;">
            <tr style="vertical-align: bottom; float: left">
                <td style="text-align: right;"><label class="ml20">归属地：</label></td>
                <td>
                      <div id="base_select" class="btn-group" style="width: 214px;">
                        <a value="all" type="button" id="base_type" class="btn btn-default btn-primary" href="javascript:void(0)" style="width: 46px;">全部</a>
                        <a value="meituan" type="button" id="base_type" class="btn btn-default" href="javascript:void(0)" style="width: 46px">北京</a>
                        <a value="dianping" type="button" id="base_type" class="btn btn-default" href="javascript:void(0)" style="width: 46px">上海</a>
                      </div>
                </td>
                <td style="text-align: right;"><label class="ml20">事业群：</label></td>
                <td>
                  <select id="business" name="business" title="business" style="width: 214px;">
                      <option value="" selected = "selected">all</option>
                      <#list businessGroup as key>
                          <#if key != "">
                              <option value="${key}">${key}</option>
                          </#if>
                      </#list>
                      <#--<option value="" selected = "selected">all</option>-->
                      <#--<option value="客服平台部">客服平台部</option>-->
                      <#--<option value="到店综合事业群">到店综合事业群</option>-->
                      <#--<option value="到店餐饮">到店餐饮</option>-->
                      <#--<option value="平台事业群">平台事业群</option>-->
                      <#--<option value="技术工程及基础数据平台">技术工程及基础数据平台</option>-->
                      <#--<option value="外卖配送">外卖配送</option>-->
                      <#--<option value="金融服务平台">金融服务平台</option>-->
                      <#--<option value="餐饮生态">餐饮生态</option>-->
                      <#--<option value="酒店旅游事业群">酒店旅游事业群</option>-->
                      <#--<option value="猫眼电影">猫眼电影</option>-->
                      <#--<option value="互联网+大学">互联网+大学</option>-->
                      <#--<option value="广告平台">广告平台</option>-->
                      <#--<option value="企业平台">企业平台</option>-->
                      <#--<option value="其他">其他</option>-->
                    </select>
                </td>
                <td style="text-align: right;"><label class="ml20">组件类别：</label></td>
                <td>
                  <select id="category" name="category" title="category" style="width: 214px;">
                      <option value="all" selected = "selected">all</option>
                      <option value="database_relevant">数据库连接相关</option>
                      <option value="http">HTTP服务</option>
                      <option value="web_framework">WEB框架</option>
                      <option value="web_container">WEB容器</option>
                      <option value="monitor">监控</option>
                      <option value="json">JSON</option>
                      <option value="log">LOG</option>
                      <option value="kv">KV</option>
                      <option value="mq">MQ</option>
                      <option value="orm">ORM</option>
                      <option value="octo">OCTO</option>
                      <option value="bom">BOM</option>
                  </select>
                </td>
                <td><button class="btn btn-primary" type="button" id="searchBtn"  style="margin-left: 20px; width: 63px;">查询</button></td>
                <td>
                    <label style="padding-left: 10px;float: right">
                        <a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28354576" target="_blank" id="count_guide">事业群技术栈使用说明
                            <i class="fa fa-question-circle"></i></a>
                    </label>
                </td>
            </tr>
             <tr>
                <td colspan="6" style="padding-top: 30px">
                    <div>
                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="6" style="padding-top: 30px; padding-bottom: 50px">
                    <div  id="cmpt_stack_charts" style=" height: atuo;"></div>
                    <div id="cmpt_stack_tips" class="charts-wrapper" style="width: 100%; height: auto;border: 0;"></div>
                </td>
            </tr>
        </table>
    </div>
</textarea>
