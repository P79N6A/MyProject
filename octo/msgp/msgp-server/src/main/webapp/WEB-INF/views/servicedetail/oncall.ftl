<title xmlns="http://www.w3.org/1999/html">值班管理</title>



<style>
    .select2-selection__choice {
        float: left;
    }
</style>

<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
<#--这里的根目录对应webmvc-config.xml中配置的templateLoaderPath-->
</div>

<div class="form-inline mt20 mb20">
    <div class="control-group">
        <label for="env_select" style="padding-right: 10px;">
            负责人:
        </label>
        <span id="outline_owners" class="outline-content">${ownerStr}</span>
    </div>
    <div class="control-group">
        <label for="env_select" style="padding-right: 10px;">
            值班人:
        </label>
        <span id="outline_oncalls" class="outline-content">${oncallStr}</span>
    </div>
    <div class="control-group">
        <button id="addOncall" class="btn btn-primary">新增值班人</button>
    </div>
</div>


