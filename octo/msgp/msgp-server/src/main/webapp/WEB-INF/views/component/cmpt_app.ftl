<style>
    .form-inline {
        padding: 0;
    }
    .outline {
        display:inline-block;
        width: 520px;
        height: 320px;
        overflow: hidden;
        margin: 10px 20px 0 60px;
        cursor:pointer;
        /*border:1px solid #868686;*/
        position:relative;
        z-index:998
    }
    .menu-special {
        float: none;
        box-shadow: none;
        border: none;
        position: relative;
        margin-bottom: 20px;
        margin-top: 30px;
        z-index:8;
    }
    .cmpt-outline-mask {
        position: relative;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 998;
        background: black;
        opacity: 0.25;
        filter: alpha(opacity=25);
    }
</style>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_app"></div>
<textarea id="text_cmpt_app" style="display:none">
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
                <td style="text-align: right;">
                  <label class="ml20">业务线：</label>
                </td>
                <td>
                  <select id="owt" name="owt" title="owt" style="width: 214px;">
                      <option value="all">all</option>
                  </select>
                </td>
                <td style="text-align: right;">
                  <label class="ml20">产品线：</label>
                </td>
                <td>
                  <select id="pdl" name="pdl" title="pdl" style="width: 214px;">
                      <option value="all">all</option>
                  </select>
                </td>
                <td><button class="btn btn-primary" type="button" id="searchBtn"  style="margin-left: 20px; width: 63px;">查询</button></td>
            </tr>
             <tr>
                <td colspan="6" style="padding-top: 30px">
                    <div>
                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="7" style="padding-bottom: 50px">
                    <div class="menu-special" id="menu-special" style="text-align: center;">
                        <div class="outline" id="cmpt_app_count"></div>
                        <div class="outline" id="cmpt_activeness_count"></div>
                        <div class="cmpt-outline-mask" style="display:none"></div>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</textarea>
