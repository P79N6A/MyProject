<#--服务性能展示页面-->

<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
    <div id="wrap_serviceProperty">
        <div class="content-body">
            <table class="table table-striped table-hover" id="tagTable">
                <colgroup>
                    <col width="5%"></col>
                    <col width="20%"></col>
                    <col width="15%"></col>
                    <col width="15%"></col>
                    <col width="15%"></col>
                    <col width="15%"></col>
                </colgroup>
                <thead>
                <tr>
                    <th></th>
                    <th>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;appkey</th>
                    <th>单机最大值／分钟</th>
                    <th>单机最小值／分钟</th>
                    <th>TP50/TP90/TP99</th>
                    <th>QPS基线-5分钟粒度</th>
                    <th>QPS基线-30分钟粒度</th>
                </tr>
                </thead>

                <tbody class="forclick"  id="tagTableNew">
                <td colspan="7">Loading contents...</td>
                    <textarea id="text_graph" style="display:none">
                        <#--展示一张qps图-->
                        <div class="charts-wrapper-out" style="margin: 0 auto; left: 50%; margin-left:-450px;">
                            <div id="screen_qps" class="charts-wrapper" style="width:900px; "></div>
                        </div>
                    </textarea>
                </tbody>
            </table>

        </div>
    </div>
</div>