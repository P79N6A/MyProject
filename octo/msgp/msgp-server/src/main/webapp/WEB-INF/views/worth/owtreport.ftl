<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_owtreport">
</div>
<textarea id="text_owtreport" style="display:none"><div id="searchForm" class="form-inline mb20">
    <div class="control-group">
        <label style="padding-left:1em">业务线：</label>
        <input id="owt" class="span2" placeholder="业务线" type="text" value="${owt!""}"/>

        <label id="label_text"></label>

        <label style="padding-left:1em">日期：</label>
        <input id="day" type="text" class="span2" placeholder="天">
        <label style="padding-left:1em">时间类型：</label>
        <select id="dtype" class="span1">
            <option value="0">天</option>
            <option value="1">周</option>
            <option value="2">月</option>
        </select>
        <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    </div>
</div>

    <div id="table_wrapper" class="table-responsive mt20">
        <table class="table table-striped table-hover">
            <thead>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div id="paginator_wrapper">
    </div></textarea>

<textarea id="text_owt" style="display:none">
                <label style="padding-left:1em">appkey：</label>
                <input id="appkey" class="span2" placeholder="appkey" type="text" value="${appkey!""}"/>
            </textarea>



<textarea id="text_owt_model" style="display:none">
                <label style="padding-left:1em">OCTO模块：</label>
                <input id="model" class="span2" placeholder="模块" type="text" value="${model!""}"/>
            </textarea>