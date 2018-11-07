<link rel="stylesheet" type="text/css" href="/static/css/jquery-ui.css"/>

<div class="form-inline mt20 mb20" id="bannerWrapper">
    <div class="control-group" nowrap id="messageSearchForm">
        <label style="padding-left:1em; ">提醒类型：</label>
        <select id="message_type" name="message_type" title="message_type" style="width: 214px;">
            <option value="1" selected = "selected">通知</option>
            <option value="2">警告</option>
        </select>
        <label style="padding-left:1em;">消息内容：</label>
        <input id="message_body" name="message_body" type="text"  title="" style="width: 500px;" placeholder="输入公告内容, 不包含'提醒/通知/警告"/>
        <button class="btn btn-primary" type="button" id="bannerAddBtn" style="margin-left: 20px; float: right">添加提醒</button>
    </div>


    <div style=" height: atuo; padding-top:25px; display: block" id="message_list_wrapper">
        <table class="table table-striped">
            <thead>
            <tr>
                <th width="15%">消息类型</th>
                <th width="20%">消息标题</th>
                <th width="50%">消息内容</th>
                <th width="15%">移除消息</th>
            </tr>
            </thead>
            <tbody id="message_list_body">
            </tbody>
        </table>
    </div>
</div>

<script type="text/javascript" src="/static/js/jquery.min.js"></script>
<script type="text/javascript" src="/static/js/jquery-ui.js"></script>
