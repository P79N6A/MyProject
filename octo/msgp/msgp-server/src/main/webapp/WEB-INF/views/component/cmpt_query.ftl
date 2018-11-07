<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_query"></div>

<textarea id="text_cmpt_query" style="display:none"><div id="searchForm" class="form-inline mb20">
    <table style="width: 100%;">
        <tr style="vertical-align: bottom;">
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
            <td style="text-align: right;"><label class="ml20">业务线：</label></td>
            <td><select id="owt" name="owt" title="owt" style="width: 214px;"><option value="all">all</option></select></td>

            <td style="text-align: right;"><label class="ml20">产品线：</label></td>
            <td><select id="pdl" name="pdl" title="pdl" style="width: 214px;"><option value="all">all</option></select></td>

            <td><button class="btn btn-primary" type="button" id="searchBtn"   style="margin-left: 20px; width: 63px;">查询</button></td>

        </tr>
        <tr style="vertical-align: bottom;">
            <td style="text-align: right; padding-top: 15px;"><label style="padding-left:1em; ">GroupId：</label></td>
            <td style="padding-top: 15px;"><input id="groupId" name="groupId" type="text" value="${groupId}" title="" style="width: 200px;" placeholder="输入 groupId 关键字" /></td>

            <td style="text-align: right; padding-top: 15px;"><label " style="padding-left:1em;">ArtifactId：</label></td>
            <td style="padding-top: 15px;"><input id="artifactId" name="artifactId" type="text" value="${artifactId}" title="" style="width: 200px;" placeholder="输入 artifactId 关键字"/></td>

            <td style="text-align: right; padding-top: 15px;"><label class="ml20" ">Version：</label></td>
            <td style="padding-top: 15px;"><select id="version" name="version" title="version" style="width: 214px;"><option value="all">all</option></select></td>

            <td style="text-align: right; color: #4fbba9; padding-top: 15px;"><label class="ml20">常用组件：</label></td>
            <td ><select id="cmpt" name="cmpt" title="cmpt" style="width: 214px; padding-top: 15px;"></select></td>

        </tr>
        <tr>
            <td colspan="9" style="padding-top: 30px">
                <div>
                    <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
                </div>
            </td>
        </tr>
        <tr>
            <td colspan="9" style="padding-top: 30px; padding-bottom: 50px">
                 <div style="height: atuo;" id="query_list_wrapper"></div>
                <div id="paginator_query"></div>
                <div id="paginator_query_wrapper" style="padding-right: 50px;"></div>
            </td>
        </tr>
    </table>
    </div></textarea>
