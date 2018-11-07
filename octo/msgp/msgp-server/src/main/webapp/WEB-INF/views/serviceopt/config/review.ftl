<div class="form-inline mb20">
    <div id="pr_menu" class="control-group">
        <a>类型:</a>
        <div id="review_type" class="btn-group">
            <a value="0" type="button" class="btn btn-default btn-primary"
               href="javascript:void(0)">Open</a>
            <a value="1" id = "pr_merge_btn" type="button" class="btn btn-default"
               href="javascript:void(0)">Merged</a>
            <a value="-1" id = "pr_decline_btn"  type="button" class="btn btn-default"
               href="javascript:void(0)">Declined</a>
        </div>
        &nbsp;&nbsp;
        <a>环境:</a>
        <label id="pr_env">a</label>
        &nbsp;&nbsp;
        <button id="review_return" class="btn btn-primary" type="button">
            <i class='fa fa-step-backward'></i>
            返回
        </button>
    </div>
    <div id="detail_menu">
        <a>类型:</a>
        <div id="pr_operator" class="btn-group">
            <a value="1" id="merge" type="button" class="btn btn-default" href="javascript:void(0)"  id = "review_merge">Merge</a>
            <a value="-1" type="button" class="btn btn-default" href="javascript:void(0)"  id = "review_decline">Decline</a>
            <a value="2" type="button" class="btn btn-default" href="javascript:void(0)" id = "review_approve">Approve</a>
        <#--<a value="3" type="button" class="btn btn-default"-->
        <#--href="javascript:void(0)">Save</a>-->
            <a value="4" type="button" id="review_reopen" class = "btn btn-default" href="javascript:void(0)">Reopen</a>
            <a value="5" type="button" id="review_note" class="btn btn-default"
               href="javascript:void(0)">Note</a>
        </div>
        &nbsp;&nbsp;
        <a>环境:</a>
        <label id="pr_env_detail">a</label>
        &nbsp;&nbsp;
    <#--<button id="review_modify_all" class="btn btn-default" type="button">
        批量修改
    </button>-->
        <button id="pr_detail_return" class="btn btn-primary" type="button">
            <i class='fa fa-step-backward'></i>
            返回
        </button>
    </div>
</div>


<div id="review_data_div">

    <table class="table  table-striped table-hover">
        <colgroup>
            <col width="8%"></col>
            <col width="12%"></col>
            <col width="10%"></col>
            <col width="10%"></col>
            <col width="60%"></col>
        </colgroup>
        <thead>
        <tr>
            <th>PR_ID</th>
            <th>Time</th>
            <th>Author</th>
            <th>Operation</th>
            <th>Note</th>
        </tr>
        </thead>
        <tbody class="J-config-panel-tbody">
        <tr id="pr_data_loading">
            <td colspan="4">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </td>
        </tr>
        <tr id="pr_error">
            <td colspan="4"></td>
        </tr>
        </tbody>
    </table>
    <div id="page_navigation_pr"></div>
</div>

<div id="pr_detail_data_div">
    <table id = "pr_detail_data_table" class="table  table-striped table-hover" style="word-wrap:break-word;word-break:break-all;">
        <colgroup>
            <col width="8%"></col>
            <col width="15%"></col>
            <col width="10%"></col>
            <col width="12%"></col>
            <col width="12%"></col>
            <col width="12%"></col>
            <col width="12%"></col>
            <col width="10%"></col>
            <col width="10%"></col>
        </colgroup>
        <thead>
        <tr>
            <th>类型</th><#-- type path key current_value new_value current_comment new_comment modify delete-->
            <th>路径</th>
            <th>主键</th>
            <th>当前值</th>
            <th>新值</th>
            <th>当前注释</th>
            <th>新注释</th>
            <th>操作</th>
            <th>修改前后对比</th>
        </tr>
        </thead>
        <tbody class="J-config-panel-tbody">
        <tr id="pr_detail_data_loading">
            <td colspan="6">
                <i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span>
            </td>
        </tr>
        <tr id="pr_detail_error">
            <td colspan="6"></td>
        </tr>
        </tbody>
    </table>

    <div id="log" class="config-file">
        <h3>review记录</h3>

        <div id="log-content" class="config-file log-content" style="overflow: auto;">


        </div>

    </div>

    <div id="note-div" class="config-file">
        <h3>PullRequest Description</h3>

        <div id="note-content" class="config-file log-content" style="overflow: auto;">


        </div>

    </div>
</div>








