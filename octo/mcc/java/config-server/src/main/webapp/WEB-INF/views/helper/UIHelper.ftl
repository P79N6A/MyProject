<#--
 - 此文件由 mt-fe 模块维护，修改请绕行 mt-fe
 -
 - 上传组件的UI
 -->
<#macro uploader compress=true submit=true addFileBtn=true display=true fileType="">
    <div class="widget-uploader-content" <#if display == false> style="display:none;" </#if>>
        <div class="head-area">
            <span class="largerBox"> 文件名</span> <span>大小</span> <span>上传进度</span> <span>操作</span>
            <#if fileType != "">
                <span>类型</span>
            </#if>
        </div>
        <div class="file-container"><ul class="widget-file-list"></ul></div>
        <div class="bottom-operator">
            <p class="widget-uploader-tip"></p>
        </div>
        <div class="bottom-area">
            <span class="widget-selected-overlay"></span>
            <#if addFileBtn == true>
                <input type="button" class="widget-add-file widget-uploader-btn widget-uploader-primary-btn" value="添加文件" />
            </#if>
            <#if submit == true>
                <input type="button" class="widget-start-uploader-btn widget-uploader-btn widget-uploader-btn-info" value="开始上传" />
            </#if>
            <a href="javascript:void(0)" class="widget-clear-file widget-uploader-btn">清空列表</a>
            <a href="javascript:void(0)" gaevent="InnerLink|Click|uploader/retry" class="widget-uploader-tryagain widget-uploader-btn" style="display:none;">出错重试</a>
            <#if compress == true >
            <span class="widget-uploader-compress" style="display:none;">
                <input id="widget-uploader-Compress" type="radio" name="compress_quality" value="1" checked="checked"  /><label for="widget-uploader-Compress">压缩</label>
                <input id="widget-uploader-noCompress" type="radio" name="compress_quality" value="0" /><label for="widget-uploader-noCompress">不压缩</label>
            </span> 
            </#if>
        </div>
        <#if compress == true >
            <div class="widget-uploader-compresstip">请注意：小于500K以及采写图片均不被自动压缩。</div>
        </#if>
    </div>
</#macro>
