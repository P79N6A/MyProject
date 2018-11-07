<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_trend"></div>
<textarea id="text_cmpt_trend" style="display:none">
  <table style="width: 100%;">
    <tr>
      <td style="vertical-align: top; padding-top: 20px; width: 1030px">
        <div id="cmpt_trend_tips" class="charts-wrapper" style="width: 800px;height: auto;border: 0;"></div>
        <div id="cmpt_trend_charts" class="charts-wrapper" style="width: 800px;height: 400px;border: 0;"></div>

      </td>
      <td rowspan="2" style="vertical-align: top; width: 310px; padding-top: 20px;">
        <table>
          <tbody>
            <tr>
              <td colspan="2">
                  <input id="start" type="text" class="span2" placeholder="开始日期">
                      <span> - </span>
                  <input id="end" type="text" class="span2" placeholder="结束日期">
              </td>
            </tr>

            <tr>
                <td colspan="2">
                    <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10)" width="100%" color=#987cb9 SIZE=1>
                </td>
            </tr>
            <tr>
                <td style="text-align: right;">
                   <label class="ml20">归属地：</label>
                </td>
                <td>
                      <div id="base_select" class="btn-group" style="width: 214px; padding-bottom: 10px">
                        <a value="all" type="button" id="base_type" class="btn btn-default btn-primary" href="javascript:void(0)" style="width: 46px;">全部</a>
                        <a value="meituan" type="button" id="base_type" class="btn btn-default" href="javascript:void(0)" style="width: 46px">北京</a>
                        <a value="dianping" type="button" id="base_type" class="btn btn-default" href="javascript:void(0)" style="width: 46px">上海</a>
                      </div>
                </td>
            </tr>
            <tr>
              <td style="text-align: right;">
                  <label class="ml20">事业群：</label>
                </td>
              <td>
                  <select id="business" name="business" title="business" style="width: 214px;">
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
                      <option value="广告平台">广告平台</option>
                      <option value="企业平台">企业平台</option>
                      <option value="其他">其他</option>
                  </select>
              </td>
            </tr>
            <tr>
              <td style="text-align: right;">
                  <label class="ml20">业务线：</label>
                </td>
              <td>
                  <select id="owt" name="owt" title="owt" style="width: 214px;">
                      <option value="all">all</option>
                  </select>
              </td>
            </tr>
            <tr>
              <td style="text-align: right;">
                  <label class="ml20">产品线：</label>
              </td>
              <td>
                  <select id="pdl" name="pdl" title="pdl" style="width: 214px;">
                      <option value="all">all</option>
                  </select>
              </td>
            </tr>
           <#-- <tr>
                <td colspan="2">
                    <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10)" width="100%" color=#987cb9 SIZE=1>
                </td>
            </tr>-->
            <tr>
               <td style="text-align: right; color: #4fbba9">
                  <label>常用组件：</label>
              </td>
              <td>
                  <select id="cmpt" name="cmpt" title="cmpt" style="width: 214px;">
                  </select>
              </td>
            </tr>
            <tr>
              <td style="text-align: right;">
                  <label style="padding-left:1em; ">GroupId<span style="color:red;">*</span>：</label>
              </td>
              <td>
                  <input id="groupId" name="groupId" type="text" value="${groupId}" title="" style="width: 200px;" placeholder="输入关键字选择 或 输入完整值" />
              </td>
            </tr>
            <tr>
              <td style="text-align: right;">
                  <label style="padding-left:1em;">ArtifactId<span style="color:red;">*</span>：</label>
              </td>
              <td>
                  <input id="artifactId" name="artifactId" type="text" value="${artifactId}" title="" style="width: 200px;" placeholder="输入关键字选择 或 输入完整值"/>
              </td>
            </tr>
            <tr>
              <td style="text-align: right;">
                  <label class="ml20" >Version：</label>
              </td>
              <td>
                   <select id="version" name="version" title="version" style="width: 214px;">
                      <option value="all">all</option>
                  </select>
              </td>
            </tr>
           <#-- <tr>
                <td colspan="2">
                    <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10)" width="100%" color=#987cb9 SIZE=1>
                </td>
            </tr>-->
            <tr>
                <td colspan="2" style="text-align: right;">
                    <button class="btn btn-primary" type="button" id="searchBtn" style="margin-left: 20px; width: 63px;">查询</button>
                </td>
            </tr>
          </tbody>
        </table>
      </td>
    </tr>
  </table>
</textarea>
