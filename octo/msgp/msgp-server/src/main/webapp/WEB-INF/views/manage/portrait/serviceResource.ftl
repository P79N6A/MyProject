<#--服务资源展示页面-->

<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div id="wrap_serviceResource">
        <div class="content-body">
            <table class="table table-striped table-hover" id="tagTable">
                <colgroup>
                    <col width="5%"></col>
                    <col width="12%"></col>
                    <col width="15%"></col>
                    <col width="10%"></col>
                    <col width="10%"></col>
                    <col width="15%"></col>
                    <col width="10%"></col>
                    <col width="13%"></col>
                    <col width="10%"></col>
                </colgroup>
                <thead>
                <tr>
                    <th></th>
                    <th>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;appkey</th>
                    <th>1分钟单CPU负载 <i title="最大值/最小值/平均值(Bps)" class="load1min_perCPU fa fa-question-circle"></i></th>
                    <th>网卡入流量 <i title="最大值/最小值/平均值(Bps)" class="net_in fa fa-question-circle"></i></th>
                    <th>网卡出流量 <i title="最大值/最小值/平均值(Bps)" class="net_out fa fa-question-circle"></i></th>
                    <th>网卡总流量 <i title="最大值/最小值/平均值(Bps)" class="net_total fa fa-question-circle"></i></th>
                    <th>JVM线程数量 <i title="最大值/最小值/平均值" class="jvm_count fa fa-question-circle"></i></th>
                    <th>JVM线程运行数量 <i title="最大值/最小值/平均值" class="jvm_running fa fa-question-circle"></i></th>
                    <th>Load图像</th>
                </tr>
                </thead>

                <tbody class="forclick"  id="tagTableNewNew">
                <td colspan="9">Loading contents...</td>
                <textarea id="text_graph" style="display:none">
                        <#--展示一张load图-->
                            <div class="charts-wrapper-out" style="margin: 0 auto; left: 50%; margin-left:-450px;">
                            <div id="screen_qps" class="charts-wrapper" style="width: 900px;"></div>
                        </div>
                    </textarea>
                </tbody>
            </table>

        </div>
    </div>
</div>