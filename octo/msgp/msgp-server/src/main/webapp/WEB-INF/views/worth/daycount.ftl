
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_daycount">
<#--
    <script type="text/javascript" src="/static/worth/js/echarts.js"></script>
-->
</div>
<textarea id="text_daycount" style="display:none">
    <div id="searchForm" class="form-inline mb20">
    <div class="control-group" nowrap>
             <div id="count_type_select_all" class="btn-group">
            <a value="0" type="button" class="btn btn-primary" href="javascript:void(0)">PV</a>
            <a value="1" type="button" class="btn btn-default" href="javascript:void(0)">UV</a>
            </div>
            <label style="padding-left:1em"><strong>日期：</strong></label>
            <input id="start" type="text" class="span2" placeholder="开始日期">
            <span> - </span>
            <input id="end" type="text" class="span2" placeholder="结束日期">
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button>

        </div>
    </div>
    <table>
        <tr>
            <td style="vertical-align: top; width: 1000px;">
                 <div  class="clearfix" style="margin-top: 15px;">
                    <div class="charts-wrapper-out">
                        <div id="daycount_charts" class="charts-wrapper" style="width: 800px;height: 400px;border: 0"></div>
                    </div>
                </div>
            </td>
            <td style="vertical-align: middle;">
                <table>
                    <tr>
                        <td style="text-align: right;"><span> UV 历史最高值</span></td>
                        <td rowspan="2" style="padding-left: 20px; font-size: 50px; color: #3fab99;"><span id="uv_value">0</span></td>
                    </tr>
                    <tr>
                        <td style="text-align: right; color: #bbbbbb;"><span id="uv_date">1970-01-01</span></td>
                    </tr>
                    <tr>
                        <td colspan = "2" style="padding-top: 20px; padding-bottom: 20px;">
                            <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10)" width="100%" color=#987cb9 SIZE=1>
                        </td>
                    </tr>
                    <tr>
                        <td style="text-align: right;"><span> PV 历史最高值</span></td>
                        <td rowspan="2" style="padding-left: 20px; font-size: 50px; color: #3fab99;" ><span id="pv_value">0</span></td>
                    </tr>
                    <tr>
                        <td style="text-align: right; color: #bbbbbb;"><span id="pv_date">1970-01-01</span></td>
                    </tr>

                </table>
            </td>
        </tr>
    </table>
</textarea>