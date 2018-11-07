<title>组件自助处理</title>
<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_user"></div>
<textarea id="text_cmpt_user" style="display:none">
    <legend>移除发布项<span style="font-size: 10px; margin-left: 10px; color: grey;">适用于非线上prod项目和废弃项目(GroupId和ArtifactId在POM中定义)</span></legend>
    <div class="form-horizontal">
        <div class="control-group">
            <label class="control-label">GroupId</label>
            <div class="controls">
                <input id="groupId" name="groupId" type="text" style="width: 300px;" placeholder="请输入 groupId" />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">ArtifactId</label>
            <div class="controls">
                <input id="artifactId" name="artifactId" type="text" style="width: 300px;" placeholder="请输入 artifactId"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">删除理由</label>
            <div class="controls">
                <input id="reason" name="reason" type="text" style="width: 300px;" placeholder="填写移除项目的理由"/>
            </div>
        </div>
         <div class="control-group">
            <label class="control-label"></label>
            <div class="controls">
                <button id="applyBtn" class="btn btn-primary" type="button">提交删除申请</button><span style="font-size: 10px; margin-left: 10px; color: grey;">申请后若长时间未接收到结果,请联系zhangyun16</span>
            </div>
        </div>
    </div>
</textarea>
