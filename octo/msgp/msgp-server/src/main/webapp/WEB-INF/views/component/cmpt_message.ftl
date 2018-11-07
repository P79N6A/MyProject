<div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main" id="div_cmpt_message"></div>

<textarea id="text_cmpt_message" style="display:none">
    <div id="messageSearchForm">
        <table style="width: 100%;">
            <tr style="vertical-align: middle;">
                <td style="text-align: right;"><label style="padding-left:1em; ">GroupId：</label></td>
                <td><input id="groupId" name="groupId" type="text" value="${groupId}" title="" style="width: 200px;" placeholder="输入 groupId 关键字" /></td>

                <td style="text-align: right; padding-top: 10px;"><label style="padding-left:1em;">ArtifactId：</label></td>
                <td><input id="artifactId" name="artifactId" type="text" value="${artifactId}" title="" style="width: 200px;" placeholder="输入 artifactId 关键字"/></td>

                <td style="text-align: right;"><label class="ml20" id="version_label">目标版本：</label></td>
                <td><input id="version" name="version" title="version" style="width: 214px;"/></td>

                <td style="text-align: right;"><label class="ml20" id="recommend_version_label">推荐版本：</label></td>
                <td><input id="recommend_version" name="recommend_version" title="recommend_version" style="width: 214px;" /></td>

                <td><button class="btn btn-primary" type="button" id="cmptAddBtn" style="margin-left: 20px; width: 63px;">添加</button></td>
            </tr>
            <tr style="vertical-align: middle;">
                <td style="text-align: right; color: #4fbba9; padding-top: 10px;"><label class="ml20">常用组件：</label></td>
                <td style="padding-top: 10px;">
                    <select id="cmpt" name="cmpt" title="cmpt" style="width: 214px;"></select>
                </td>

                <td style="text-align: right; padding-top: 10px;"><label style="padding-left:1em; ">说明WIKI：</label></td>
                <td style="padding-top: 10px;"><input id="wiki" name="wiki" type="text"  title="wiki" style="width: 200px;" placeholder="(必填)输入 组件说明文档的WIKI地址" /></td>

                <td style="text-align: right; padding-top: 10px;"><label style="padding-left:1em; ">提醒主题：</label></td>
                <td style="padding-top: 10px;" ><input id="subject" name="subject" type="text"  title="subject" value="组件版本使用过低提醒" style="width: 200px;" placeholder="如 组件版本使用过低提醒" /></td>

                <#--<td style="text-align: right; padding-top: 10px;"><label class="ml20">消息：</label></td>
                <td style="padding-top: 10px;">
                    <select id="message_type" name="message_type" title="message_type" style="width: 214px;" disabled="disabled">
                        <option value="xm" selected = "selected">大象</option>
                        <option value="mail">邮件</option>
                        <option value="mobile">手机</option>
                    </select>
                </td>-->

                <td style="text-align: right; padding-top: 10px;"><label class="ml20">提醒类型：</label></td>
                <td style=" padding-top: 10px;">
                    <select id="option_type" name="option_type" title="option_type" style="width: 220px;">
                        <option value="version" selected = "selected">低版本提醒</option>
                        <option value="blacklist">黑名单提醒</option>
                        <option value="conflict">组件冲突提醒</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td colspan = '9' style="padding-top: 20px;">
                    <div>
                        <HR style="FILTER: progid:DXImageTransform.Microsoft.Glow(color=#987cb9,strength=10); margin:0;" width="100%; " color=#987cb9 SIZE=1>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan = '9'>
                    <div style=" height: atuo; padding-top:15px;" id="message_list_wrapper">
                       <table class="table table-striped">
                           <thead>
                           <tr>
                               <th width="20%">GroupId</th>
                               <th width="15%">ArtifactId</th>
                               <th width="20%">最低版本</th>
                               <th width="20%">推荐版本</th>
                               <th width="15%">组件说明WIKI</th>
                               <th width="10%">移除组件</th>
                           </tr>
                           </thead>
                           <tbody>
                           </tbody>
                       </table>
                   </div>
                </td>
            </tr>
            <tr>
                <td style="padding-top: 10px;" colspan="8"><button class="btn btn-primary" type="button" id="cmptTestBtn" style="float:right;margin-left: 20px; width: 63px;">测试</button></td>
                <td style="padding-top: 10px;" colspan="1"><button class="btn btn-primary" type="button" id="cmptSendBtn" style="float:right;margin-left: 20px; width: 63px;">提醒</button></td>
            </tr>
             <tr>
                <td colspan="9" style="padding: 20px 0 10px 0;">
                    <div style="padding:5px 20px 0 20px;font-size: 12px; font-weight: 100;">
                        <b>使用说明：</b><br/>
                            此项功能可以向服务负责人推送服务所使用的组件的相关信息, 如低版本组件提醒。<br/>
                        <b>操作步骤:</b><br/>
                        1, 选择组件的group_id和artifact_id（控制面板给出了若干常用组件，可以一键设置）；<br/>
                        2, 选择希望配置的最低版本和版本说明文档对应的wiki地址，这两个参数必不可少；<br/>
                        3, 点击添加按钮，将此组件加入备选清单；如果希望添加多个组件，则重复步骤1~2；<br/>
                        4, 最后点击提醒按钮，OCTO公众号会将低版本提醒消息推送至每个相关的服务负责人。<br/>
                        <b>注意事项:</b><br/>
                        1, 此项功能会使用OCTO大象公众号推送消息至相关服务负责人，因此各项参数（如最低版本，wiki说明等）的配置务必准确。<br/>
                        2, 在正式发送前，<span style="color:#E53333;font-family:Tahoma, Helvetica, arial, sans-serif;background-color:#FFFFFF;">请点击测试按钮进行测试</span>，确认消息的准确性。<br/>
                        更多信息，请访问<a style=" display: inline-block; height: 20px; line-height: 20px;" href="https://123.sankuai.com/km/page/28326939" target="_blank" id="count_guide">组件依赖管理使用说明</a> 意见反馈：唐烨（tangye03)
                    </div>
                </td>
            </tr>
        </table>
    </div>
</textarea>
