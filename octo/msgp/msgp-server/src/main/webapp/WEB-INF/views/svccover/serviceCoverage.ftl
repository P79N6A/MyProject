<style>
    .form-inline {
        padding: 0;
    }

    .outline_his {
        display: inline-block;
        width: 850px;
        height: 450px;
        overflow: hidden;
        margin: 10px 20px 0 60px;
        cursor: pointer;
        position: relative;
        z-index: 998
    }

    .outline_cur {
        display: inline-block;
        width: 520px;
        height: 340px;
        overflow: hidden;
        margin: 10px 20px 0 60px;
        cursor: pointer;
        position: relative;
        z-index: 998
    }

    .menu-special {
        float: none;
        box-shadow: none;
        border: none;
        position: relative;
        margin-bottom: 20px;
        margin-top: 30px;
        z-index: 8;
    }

    }
</style>
<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>

<script src="/static/js/echarts3/echarts.common.min.js"></script>
<script src="/static/js/echarts3/theme/macarons.js"></script>
<title>服务覆盖率统计</title>
<script>
    M.use('msgp-svccover/serviceCoverage', function (Y) {

        Y.msgp.svccover.serviceCoverage();
    });
</script>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div class="clearfix">
        <label style="padding-left: 10px;float: right">
            <a style=" display: inline-block; height: 20px; line-height: 20px;"
               href="https://123.sankuai.com/km/page/28354558" target="_blank" id="count_guide">服务覆盖率统计使用手册
                <i class="fa fa-question-circle"></i></a>
        </label>
        <h3 class="page-header" id="page_header"
            style="float: left; height: 20px; line-height: 20px; border: none; margin-bottom: 0;">服务覆盖率统计</h3>
    </div>

    <div class="tab-box">

        <div id="content_wrapper">

            <div id="service_coverage_count" class="sheet">

                <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_service_coverage">

                    <div id="searchForm" class="form-inline mb20">
                        <table style="width: 100%;">

                            <tr style="vertical-align: bottom;">
                                <td>
                                    <div id="state_select" class="btn-group" style="width: 170px;">
                                        <a value="current" type="button"
                                           class="state_type btn btn-default btn-primary" href="javascript:void(0)"
                                           style="width: 46px">当前</a>
                                        <a value="history" type="button" class="state_type btn btn-default"
                                           href="javascript:void(0)" style="width: 46px">历史</a>
                                    </div>
                                </td>
                            </tr>
                            <tr style="vertical-align: bottom;">
                                <td><label style="padding-left: 120px;width: 110px;">归属地：</label></td>
                                <td>
                                    <div id="base_select" class="btn-group">
                                        <a value="meituan" type="button" id="base_type"
                                           class="base_type btn btn-default btn-primary"
                                           href="javascript:void(0)"
                                           style="width: 46px">北京</a>
                                        <a value="dianping" type="button" id="base_type"
                                           class="base_type btn btn-default"
                                           href="javascript:void(0)" style="width: 46px">上海</a>
                                    </div>
                                </td>
                                <td style="padding-right: 100px;">
                                    <label>事业群：</label>
                                    <select id="business" name="business" title="business" style="width: 214px;">
                                        <option value=" " selected="selected">all</option>
                                    </select>
                                    <label style="margin-left: 80px;">业务线：</label>
                                    <select id="owt" name="owt" title="owt" style="width: 214px;">
                                        <option
                                                value="all">all
                                        </option>
                                    </select>
                                </td>
                            </tr>
                            <tr style="vertical-align: bottom;">

                                <td style="color: #4fbba9; padding-top: 15px;padding-left: 120px;width: 110px;"><label>常用服务组件：</label>
                                </td>
                                <td><select id="cmpt" name="cmpt" title="cmpt" style="width: 214px; padding-top: 15px;">
                                    <option value="mtthrift" selected="selected">mtthrift</option>
                                    <option value="hlb">hlb</option>
                                    <option value="mns">mns</option>
                                    <option value="mcc">mcc</option>
                                    <option value="hulk">hulk</option>
                                    <option value="mtrace">mtrace</option>
                                    <option value="xmd_log">xmd_log</option>
                                    <option value="inf_bom">inf-bom</option>
                                    <option value="service_degrade">service_degrade</option>
                                    <option value="ptest">ptest</option>
                                </select></td>

                                <td class="date" style="display:none">
                                    <div id="date_select" class="btn-group">
                                        <a value="week" type="button" class="date_type btn btn-default btn-primary"
                                           href="javascript:void(0)" style="width: 20px">周</a>
                                        <a value="month" type="button" class="date_type btn btn-default"
                                           href="javascript:void(0)" style="width: 20px">月</a>
                                        <a value="quarter" type="button" class="date_type btn btn-default"
                                           href="javascript:void(0)" style="width: 30px">季度</a>
                                        <input id="start" type="text" class="span2" placeholder="开始日期"
                                               style="margin-left: 5px">
                                        <label style="margin-left: 3px;margin-right: 3px"> - </label>
                                        <input id="end" type="text" class="span2" placeholder="结束日期">
                                        <button class="btn btn-primary" type="button" id="searchBtn"
                                                style="width: 60px;margin-left: 5px">查询
                                        </button>
                                    </div>
                                </td>

                            </tr>

                            <tr>
                                <td colspan="9" style="padding-top: 30px">
                                    <div>
                                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;"
                                            width="100%; " color=#987cb9 SIZE=1>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="9">
                                    <div class="menu-special" id="menu-special" style="text-align: center;">
                                        <div class="outline_his" id="service_history_count" style="display:none"></div>
                                        <div class="outline_cur" id="service_current_count" style="display:"></div>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="9" style="padding-top: 30px; padding-bottom: 50px">

                                    <div style=" height: atuo;display:;" id="check_list_wrapper">
                                        <table id="check_list" class="table table-striped table-hover "
                                               data-widget="sortTable">
                                            <thead>
                                            <tr>
                                                <th style='width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>
                                                    日期
                                                </th>
                                                <th style='width: 10%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;'>
                                                    业务线
                                                </th>
                                                <th style="width: 20%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                                                    Appkey
                                                </th>
                                                <th style="width: 15%; white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                                                    使用情况
                                                </th>
                                            </tr>
                                            </thead>
                                            <tbody id="check_list_tb">

                                            </tbody>
                                        </table>
                                    </div>
                                    <div id="paginator_checklist"></div>
                                    <div id="paginator_checklist_wrapper" style="padding-right: 50px;display:;"></div>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

            </div>


        </div>

    </div>
</div>
