M.add('msgp-serviceopt/optFileConfig-version0.0.14', function (Y) {
        Y.namespace('msgp.serviceopt').optFileConfig = detailFileConfig;
        Y.namespace('msgp.serviceopt.optFileConfig').returnGroup = returnGroup;
        Y.namespace('msgp.serviceopt.optFileConfig').addIps = editGroup;
        Y.namespace('msgp.serviceopt.optFileConfig').initOnline = initOnline;
        Y.namespace('msgp.serviceopt.optFileConfig').getOnline = getOnline;
        var appkey;
        var wrapper = Y.one("#wrapper-config-dynamic-file"), wrapper_dynamic = wrapper.one("#dynamic_config"),
            wrapper_file = wrapper.one("#file_config");
        var wrapper_sgconfig_migration = wrapper.one('#wrapper-sgconfig-migration');
        var wrapper_review = wrapper.one("#wrapper-review");
        var wrapper_history = wrapper.one("#wrapper-history");
        var wrapper_file_history = wrapper.one("#wrapper-filelog");
        var history_btn = wrapper.one("#history_btn");
        var file_history_btn = wrapper.one("#file_history_btn");
        var pr_reveiw_manage_btn = wrapper.one("#pr_reveiw_manage");
        var currentEnv = 3, dynamic_current_env = 3;
        var migrationEnv = 3;
        var isWaimaiMigration = false;
        var title = Y.one('#mcc_config_title');
        var addGroupDialog, loadingDialog,
            editGroupDialog, addReviewNoteDialog, detailDialog, fileDetailDialog, detailPrDialog, detailJsonDialog;
        var isOpen = false;
        var checkIPs, checkGroupName, checkMigrationApp;
        var prod = wrapper.one('#env_prod'),
            stage = wrapper.one('#env_stage'),
            test = wrapper.one('#env_test');

        var reviewCurrentPageNo = 1;
        var currentPRID = 0;

        var currentEnvText = 'dev';
        var isOnline;
        var currentversion = "0";

        var cells = [];

        var templateDialogStr = [
            '<textarea style="width:620px;height: 300px;" placeholder="review备注,必须输入...">',
            '</textarea>'
        ].join('');

        function ipTag() {
            this.ip = "";
            this.tag = false;
        }

        Array.prototype.checkItemIsExist = function (x) {
            for (var i = 0; i < this.length; ++i) {
                if (this[i] === x) {
                    return true;
                }
            }
            return false;
        }

        Array.prototype.minus = function (b) {
            this.distinct();
            var ret = new Array();
            this.forEach(function (item) {
                if (!b.checkItemIsExist(item)) {
                    ret.push(item);
                }
            });
            return ret;
        }
        Array.prototype.merge = function (b) {
            var ret = new Array();
            this.forEach(function (item) {
                ret.push(item);
            });
            b.forEach(function (item) {
                ret.push(item);
            });
            ret.distinct();
            return ret;
        }

        Array.prototype.distinct = function () {
            if (this.length <= 0) return;
            this.sort();
            var re = [this[0]];
            for (var i = 1; i < this.length; i++) {
                if (this[i] !== re[re.length - 1]) {
                    re.push(this[i]);
                }
            }
            this.length = re.length;
            for (var i = 0; i < re.length; i++) {
                this[i] = re[i];
            }
        }

        var currentGroupNames = null, currentGroupsIPs = null;
        var logSpan = '<span>[<font color="{color}">{msgType}</font>][{time}]:{log_content}<br/></span>';

        var group_tr_template = [
            '<% Y.Array.each(this.data,function(item){ %>',
            '<tr class="group_tr" data-id="<%= item.id%>" data-groupname="<%= item.groupName%>">',
            '<td>',
            '<a id="in_group" href="javascript:void(0);"><%= item.groupName%></a>',
            '</td>',
            '<td><%= Y.mt.date.formatDateByString( new Date(item.createTime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
            '<td><%= Y.mt.date.formatDateByString( new Date(item.updateTime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
            '<td>',
            '<a id="edit_group" href="javascript:void(0);" title="编辑IP地址" class="config-panel-edit"> &nbsp;&nbsp;<i class="fa fa-edit"></i> <span>编辑</span> </a>',
            '<% if(0!=Number(item.id)){%>',
            '<a id="delete_group" href="javascript:void(0);" class="config-panel-delete"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span >删除</span> </a>',
            '<%}%>',
            '</td>',
            '</tr>',
            '<%});%>'
        ].join('');

        var pr_tr_template = [
            '<% var self = this; Y.Array.each(this.data,function(item){ %>',
            '<tr class="pr_tr" data-id="<%= item.prID%>">',
            '<td><%= item.prID%></td>',
            '<td><%= Y.mt.date.formatDateByString( new Date(item.prTime), "yyyy-MM-dd hh:mm:ss" ) %></td>',
            '<td>',
            '<%= item.prMisID%>',
            '</td>',
            '<td>',
            '<a href="javascript:void(0);" class="config-panel-edit"><i class="fa fa-edit"></i> <span>查看</span> </a>',
            '<% if(self.canDeleted){ %>',
            '<a href="javascript:void(0);" class="config-panel-delete">&nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span>删除</span> </a>',
            '<%}%>',
            '</td>',
            '<td>',
            '<% Y.Array.each(item.note, function(iterator, index){ %>',
            '<%= iterator %><br/>',
            '<% }); %>',
            '</td>',
            '</tr>',
            '<%});%>'
        ].join('');

        var pr_detail_tr_template = [
            '<%var self = this; self.canEdit = false; Y.Array.each(this.data,function(item){ %>',
            '<tr class="pr_detail_tr" data-version="<%= item.version%>">',
            '<td>',
            '<%=item.type%>',
            '</td>',
            '<td><%= item.path%></td>',
            '<td><%= item.key%></td>',
            '<td>',
            '<%=item.oldValue%>',
            '</td>',
            '<td>',
            '<% if(self.canEdit){%>',
            '<textarea><%=item.newValue%></textarea>',
            '<%}else {%>',
            '<%=item.newValue%><%}%>',
            '</td>',
            '<td>',
            '<%=item.oldComment%>',
            '</td>',
            '<td>',
            '<%if(self.canEdit){%>',
            '<textarea><%=item.newComment%></textarea>',
            '<%}else {%>',
            '<%=item.newComment%><%}%>',
            '</td>',
            '<td>',
            '<a id="btn_submit_PR_update" href="javascript:void(0);" name = "<%= item.key%>" class="config-panel-modify"> <i class="fa fa-edit"></i> <span>修改</span> </a>',
            '<a id="btn_submit_PR_delete" href="javascript:void(0);" name = "<%= item.key%>" class="config-panel-modify"> &nbsp;&nbsp;<i class="fa fa-trash-o"></i> <span>删除</span> </a>',
            '</td>',
            '<td>',
            '<a id="btn_PR_json" href="javascript:void(0);" name = "<%= item.key%>" class="config-panel-modify"> &nbsp;&nbsp;<span>查看对比</span> </a>',
            '</td>',
            '</tr>',
            '<%});%>'
        ].join('');

        var dialogTemplate = [
            '<div id="add_group_form" class="form-horizontal">',
            '<div class="control-group"><label class="control-label"><b>分组名称：</b></label>',
            '<div class="controls">',
            '<input id="name_input" type="text" class="span4" value="<%= this.name %>" placeholder="分组名称，必填" />',
            '<span class="tips"></span>',
            '</div>',
            '<div class="control-group" style="margin-top: 10px;"><label class="control-label"><b>主机：</b></label>',
            '<div class="controls">',
            '<div class="input-append">',
            '<textarea id="group_add_btn_ip" class="span4" placeholder="IP地址" type="text"/></textarea>',
            '<button id="group_add_ip_btn" class="btn btn-primary" style="height: 50px;margin-left: -1px;" href="javascript:void(0)">添加',
            '</button>',
            '</div>',
            '<span class="tips" id="manual_add_ip_tips"></span>',
            '<p>可批量添加，每行一个IP地址</p>',
            '</div>',
            '</div>',
            '</div>',
            '<div id="ip_form" class="config-file">',
            '<h3>IP地址</h3>',
            '<p>',
            '<span id="ip-all-check" style="cursor:pointer;color: #3fab99;margin-left:100px">全选/</span>',
            '<span id="ip-all-uncheck" style="cursor:pointer;color: #3fab99;;margin-left:5px">反选</span>',
            '</p>',
            '<div id="add_group_ips" style="height: 300px;overflow: auto;margin-top: 10px;margin-left: 10px;margin-right: 10px;">',

            '</div>',
            '</div>',
            '</div>'
        ].join('');

        var ipTableTemplate = [
            '<table style="width:100%;">',
            '<colgroup>',
            '<col width="15%"></col>',
            '<col width="15%"></col>',
            '<col width="15%"></col>',
            '<col width="15%"></col>',
            '<col width="15%"></col>',
            '<col width="15%"></col>',
            '</colgroup>',
            '<% var nullItemNum=0;Y.Array.each(this.data, function(subList,index){ %>',
            '<tr>',
            '<% nullItemNum=0; Y.Array.each(subList, function(item, z){ %>',
            '<%if(null==item||""==item){++nullItemNum;}else{%>',
            '<td><input id="one_ipcheck" <%if(item.tag){%>checked<%}%> type="checkbox" style="margin-bottom: 3px;" value="<%=item.ip%>"/>&nbsp;<%=item.ip%></td>',
            '<% }}); %>',
            '<% for(var i=0;i < 6 - subList.length-nullItemNum; ++i){%>',
            '<td id="fill_up_ip"></td>',
            '<% } %>',
            '</tr>',

            '<% }); %>',
            '</table>'
        ].join('');

        var logDetailTemplate = [
            '<div id="log-detail-table" class="form-horizontal">',
            '<table class="table table-bordered">',
            '<thead>',
            '<tr>',
            '<th>修改前</th>',
            '<th>修改后</th>',
            '</tr>',
            '</thead>',
            '<tbody>',
            '<tr>',
            '<td width="50%" style="word-break:break-all;"><pre style="height:500px;overflow-y:auto" class="logOldValue"></pre></td>',
            '<td width="50%" style="word-break:break-all;overflow-y:hidden"><pre style="height:500px;overflow-y:auto" class="logNewValue"></pre></td>',
            '</tr>',
            '</tbody>',
            '</table>',
            '</div>'
        ].join('');

        var jsonDetailTemplate = [
            '<div id="log-detail-table" class="form-horizontal">',
            '<table class="table table-bordered">',
            '<thead>',
            '<tr>',
            '<th>修改前</th>',
            '<th>修改后</th>',
            '</tr>',
            '</thead>',
            '<tbody>',
            '<tr>',
            '<td width="50%" style="word-break:break-all;"><pre style="height:500px;overflow-y:auto" class="jsonOldValue"></pre></td>',
            '<td width="50%" style="word-break:break-all;overflow-y:hidden"><pre style="height:500px;overflow-y:auto" class="jsonNewValue"></pre></td>',
            '</tr>',
            '</tbody>',
            '</table>',
            '</div>'
        ].join('');

        var prDetailTemplate = [
            '<div id="pr-detail-table" class="form-horizontal">',
            '<table class="table table-bordered">',
            '<tbody>',
            '<tr>',
            '<td style="word-break:break-all;"><pre style="height:500px;overflow-y:auto" class="value"></pre></td>',
            '</tr>',
            '</tbody>',
            '</table>',
            '</div>'
        ].join('');

        var envstrUrl;
        var envstr;
        var isMerge;
        var prInfoUrl = document.location.href;
        var prId;
        if (prInfoUrl.indexOf("prID") != -1) {
            envstrUrl = prInfoUrl.split("env=")[1].split("&")[0];
            isMerge = prInfoUrl.split("ismerge=")[1].split("&")[0];
            prId = prInfoUrl.split("prID=")[1].split("#")[0];
            currentPRID = parseInt(prId);
        }

        function detailFileConfig(key) {
            appkey = key;
            bind();
            wrapper.one('.typeBtn')._node.click();
            if (typeof(prId) !== "undefined") {
                wrapper.one("#wrapper-mcc-body").hide();
                wrapper.one("#wrapper-review").show();
                //wrapper_review.one('#pr_detail_return').hide();
                getPRDetail(prId, 0);
            }else{
                wrapper.one('.typeBtn')._node.click();
            }
        }

        function bind() {
            wrapper.delegate('click', function () {
                this.ancestor('div').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                wrapper.one('#J-config-container-menu').show();
                history_btn.hide();
                file_history_btn.hide();
                pr_reveiw_manage_btn.hide();
                wrapper_sgconfig_migration.one('#wrapper-sgconfig-migration-body').hide();
                showSgconfigMigration('none');
                switch (this.getAttribute('value')) {
                    case 'file':
                        wrapper.one('#dynamic_env').hide();
                        wrapper.one('#file_env').show();
                        wrapper_dynamic.hide();
                        wrapper_file.show();
                        file_history_btn.show();
                        wrapper.one('.fileEnvBtn')._node.click();
                        break;
                    case 'dynamic':
                        wrapper.one('#dynamic_env').show();
                        wrapper.one('#file_env').hide();
                        wrapper_dynamic.show();
                        wrapper_file.hide();
                        pr_reveiw_manage_btn.show();
                        history_btn.show();
                        wrapper.one('#add_new_item').hide();
                        showSgconfigMigration('link');
                        break;
                }
            }, '.typeBtn');

            wrapper.delegate('click', function () {
                this.ancestor('div').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                isWaimaiMigration = (0 == Number(this.get('value')))
            }, '#migration_business_line a');
            wrapper.delegate('click', function () {
                wrapper.one('#wrapper-mcc-body').hide();
                showSgconfigMigration('body');
                checkMigrationApp = Y.msgp.utils.check.init(wrapper_sgconfig_migration.one('#sgconfig_app_input'), {
                    chineseOk: false,
                    spaceOk: false,
                    emptyOk: false,
                    warnElement: wrapper_sgconfig_migration.one('#sgconfig_app_tips')
                });
                showSgconfigContent('data');
            }, '#sgconfig_migrate');

            wrapper_sgconfig_migration.delegate('click', function () {
                if (!checkMigrationApp.isValid()) {
                    checkMigrationApp.showMsg();
                    return;
                }
                sgconfigMigration();
            }, '#migration_button')

            wrapper_sgconfig_migration.delegate('click', function () {
                this.ancestor('div').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                migrationEnv = Number(this.getAttribute('value'));
            }, '#migration_env a')

            wrapper_sgconfig_migration.delegate('click', function () {
                if (!checkMigrationApp.isValid()) {
                    checkMigrationApp.showMsg();
                    return;
                }
                showSgconfigContent('loading');
                sgconfigPreview();
            }, '#sgconfig_preview_button');

            wrapper.delegate('click', function () {
                showSgconfigMigration('none');
                wrapper.one('#wrapper-mcc-body').show();
                clickDynamicEnv();
            }, '#sgconfig_migration_return');

            wrapper.delegate('click', function () {
                this.ancestor('div').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                // history_btn.hide();
                wrapper.one('#add_new_item').show();
                wrapper.one('#add_new_item').one('span').set('text', '添加分组');
                // filelog_btn.show();
                currentEnv = Number(this.getAttribute('value'));
                currentEnvText = this.get('text');
                wrapper_file.one('#file_config_group').show();
                wrapper_file.one('#file_config_detail_wrap').hide();
                fillGroupTable('loading');
                loadGroups(1);
            }, '.fileEnvBtn');

            wrapper.delegate('click', function () {
                // filelog_btn.hide();///
                dynamic_current_env = Number(envStr3Int(this.getAttribute('value')));
                var rollbackBtn = wrapper.one('.J-config-panel-configrollback')._node;
                rollbackBtn.removeAttribute('disabled')
            }, '#dynamic_env a');

            wrapper.delegate('click', function () {
                responeAddGroup(this);
            }, '.add_group_btn');

            file_history_btn.delegate('click', function () {
                showPage('file_history');
                initDatePicker();
                wrapper_file_history.one("#filelog_env a").simulate('click');
            }, '.file_history_btn');
            wrapper_file.delegate('click', function () {
                var tr = this.ancestor('tr');
                var groupID = tr.getData('id');
                var groupName = tr.getData("groupName");
                var url = '/serverOpt/' + appkey + '/config/file/group';
                var data = {
                    env: currentEnv,
                    groupID: groupID
                };
                myIO(url, "DELETE", data, suc, fail);

                function suc(ret) {
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', "分组[" + groupName + "]删除成功", 3);
                        loadGroups(1);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    }
                }

                function fail() {
                    Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
                }
            }, '#delete_group');

            wrapper_file.delegate('click', function () {
                editGroup(this);
            }, '#edit_group');
            wrapper_file.delegate('click', function () {
                var tr = this.ancestor('tr');
                var groupID = tr.getData('id');
                goToGroup(groupID);
            }, '#in_group');

            wrapper_review.delegate('click', function () {
                showPage('home');
                wrapper.one('#dynamic_env .btn-primary').simulate('click');
            }, '#review_return');
            wrapper_review.delegate('click', function () {
                this.ancestor('div').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                getPR(1);
            }, '#review_type a');
            wrapper_review.delegate('click', function (nd) {
                var prID = this.ancestor('tr').getData('id');
                deletePR(prID);
            }, 'tbody .config-panel-delete');
            wrapper_review.delegate('click', function (nd) {
                var prID = this.ancestor('tr').getData('id');
                currentPRID = Number(prID);
                var type = Number(wrapper_review.one('#review_type .btn-primary').getAttribute("value"));
                getPRDetail(prID, type);
            }, 'tbody .config-panel-edit');

            wrapper_review.delegate('click', function () {
                var name = this.getAttribute('name');
                var i = 0;
                for(; i < prDetailData.length; i++){
                    if(name == prDetailData[i].key) break;
                }
                clickModifyPRDetail(i);
            }, '#btn_submit_PR_update');

            wrapper_review.delegate('click', function () {
                var name = this.getAttribute('name');
                var i = 0;
                for(; i < prDetailData.length; i++){
                    if(name == prDetailData[i].key) break;
                }
                clickDeletePRDetail(i);
            }, '#btn_submit_PR_delete');

            wrapper_review.delegate('click', function () {
                var name = this.getAttribute('name');
                var i = 0;
                for (; i < prDetailData.length; i++) {
                    if (name == prDetailData[i].key) break;
                }

                var oldValue = prDetailData[i].oldValue;
                var newValue = prDetailData[i].newValue;
                detailJsonDialog = detailJsonDialog ? detailJsonDialog : new Y.mt.widget.CommonDialog({
                    id: 'detail_json_dialog',
                    title: '修改前后对比详情',
                    width: 1000
                });
                var micro = new Y.Template();
                var html = micro.render(jsonDetailTemplate, {
                    oldValue: oldValue,
                    newValue: newValue
                });
                detailJsonDialog.setContent(html);
                detailJsonDialog.show();
                var body = detailJsonDialog.getBody(),
                    tbody = body.one('#log-detail-table tbody');

                tbody.one('.jsonOldValue').setHTML(IsJsonString(oldValue) ? formatJson(oldValue) : oldValue);
                tbody.one('.jsonNewValue').setHTML(IsJsonString(newValue) ? formatJson(newValue) : newValue);
                Y.one("#detail_json_dialog")._node.style.marginTop = '50px';
            }, '#btn_PR_json');

            wrapper_review.delegate('click', function () {
                modifyAllPRDetail();
            }, '#review_modify_all');

            wrapper_review.delegate('click', function () {
                var detail = wrapper_review.one('#pr_detail_data_div');
                var prDIV = wrapper_review.one('#review_data_div');
                var detail_menu = wrapper_review.one('#detail_menu');
                var pr_menu = wrapper_review.one('#pr_menu');
                pr_menu.show();
                detail_menu.hide();
                if(typeof(prId) !== "undefined"){
                    getPR(1);
                }
                prDIV.show();
                detail.hide();
            }, '#pr_detail_return');
            wrapper_review.delegate('click', function () {
                switch (Number(this.getAttribute('value'))) {
                    case 1:
                        //merge
                        mergePR();
                        break;
                    case -1:
                        //decline
                        decline();
                        break;
                    case 2:
                        //approve
                        approve();
                        break;
                    case 3:
                        //save

                        break;
                    case 4:
                        reopen();
                        break;
                    case 5:
                        addReviewNote();
                        break;
                }
            }, '#detail_menu #pr_operator a');

            wrapper_history.delegate('click', function () {
                if (this.hasClass('btn-primary')) {
                    return;
                }
                this.ancestor('#history_env').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                var obj = getStartEnd();
                if (!obj) {
                    return;
                }
                showContent('loading');
                wrapper_history.one('#log_key').set('value',"");
                wrapper_history.one('#log_value').set('value',"");
                getHistory(1, this.getAttribute('value'));
            }, '#history_env a');

            wrapper_history.delegate('click', function () {
                showPage('home');
                wrapper.one('#dynamic_env .btn-primary').simulate('click');
            }, '#history_return');

            wrapper_history.delegate('keyup', function (e) {
                if (13 === e.keyCode) {
                    wrapper.one('#historySearchButton').simulate('click');
                }
            }, '#log_key');

            wrapper_history.delegate('keyup', function (e) {
                if (13 === e.keyCode) {
                    wrapper_history.one("#historySearchButton").simulate('click');
                }
            }, "#log_value");

            wrapper_history.delegate('click', function () {
                showContent('loading');
                getHistory(1, this.getAttribute('value'));
            }, '#historySearchButton');

            ///
            wrapper_file_history.delegate('click', function () {
                Y.msgp.service.setEnvText('filelog_env');
                if (this.hasClass('btn-primary')) {
                    return;
                }
                this.ancestor('#filelog_env').all('a').removeClass('btn-primary');
                this.addClass('btn-primary');
                var obj = getFilelogStartEnd();
                if (!obj) {
                    return;
                }
                showFilelogContent('loading');
                getFilelog(1, this.getAttribute('value'));
            }, '#filelog_env a');
            ///
            wrapper_file_history.delegate('click', function () {
                showPage('home');
                wrapper.one('#file_env .btn-primary').simulate('click');
                // filelog_env.one("#prod");
            }, '#filelog_return');
        }

        function IsJsonString(str) {
            try {
                JSON.parse(str);
            } catch (e) {
                return false;
            }
            return true;
        }

        var formatJson = function (json, options) {
            var reg = null,
                formatted = '',
                pad = 0,
                PADDING = '    ';
            options = options || {};
            options.newlineAfterColonIfBeforeBraceOrBracket = (options.newlineAfterColonIfBeforeBraceOrBracket === true) ? true : false;
            options.spaceAfterColon = (options.spaceAfterColon === false) ? false : true;
            if (typeof json !== 'string') {
                json = JSON.stringify(json);
            } else {
                json = JSON.parse(json);
                json = JSON.stringify(json);
            }
            reg = /([\{\}])/g;
            json = json.replace(reg, '\r\n$1\r\n');
            reg = /([\[\]])/g;
            json = json.replace(reg, '\r\n$1\r\n');
            reg = /(\,)/g;
            json = json.replace(reg, '$1\r\n');
            reg = /(\r\n\r\n)/g;
            json = json.replace(reg, '\r\n');
            reg = /\r\n\,/g;
            json = json.replace(reg, ',');
            if (!options.newlineAfterColonIfBeforeBraceOrBracket) {
                reg = /\:\r\n\{/g;
                json = json.replace(reg, ':{');
                reg = /\:\r\n\[/g;
                json = json.replace(reg, ':[');
            }
            if (options.spaceAfterColon) {
                reg = /\:/g;
                json = json.replace(reg, ':');
            }
            (json.split('\r\n')).forEach(function (node, index) {
                //console.log(node);
                var i = 0,
                    indent = 0,
                    padding = '';

                if (node.match(/\{$/) || node.match(/\[$/)) {
                    indent = 1;
                } else if (node.match(/\}/) || node.match(/\]/)) {
                    if (pad !== 0) {
                        pad -= 1;
                    }
                } else {
                    indent = 0;
                }

                for (i = 0; i < pad; i++) {
                    padding += PADDING;
                }

                formatted += padding + node + '\r\n';
                pad += indent;
            });
            return formatted;
        };

        function addReviewNote() {

            addReviewNoteDialog = addReviewNoteDialog ? addReviewNoteDialog : new Y.mt.widget.CommonDialog({
                id: 'add_review_note',
                title: '添加评论',
                width: 640,
                btn: {
                    pass: doAddReviewDialog,
                    passName: "提交"
                }
            });
            var micro = new Y.Template();
            var str = micro.render(templateDialogStr);
            addReviewNoteDialog.setContent(str);
            addReviewNoteDialog.show();

            function doAddReviewDialog(saveButton, container) {
                var reviewNote = Y.Lang.trim(container.one('textarea').get('value'));
                if ("" === reviewNote) {
                    Y.msgp.utils.msgpHeaderTip('error', "review备注不能为空", 3);
                    return true;
                }
                var url = '/serverOpt/' + appkey + '/config/review';
                var data = {
                    appkey: appkey,
                    prID: currentPRID,
                    approve: 0,
                    note: reviewNote
                }
                myIO(url, 'POST', data, function (ret) {
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '备注添加成功', 3);
                        getPRDetail(currentPRID, 0);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '服务器出错', 3);
                    }
                }, function () {
                    Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
                });
            }

        }

        function printLog(msgType, time, logMsg) {
            var log_content = [];
            var params = {
                color: ("decline" == msgType.toLowerCase().toString()) ? "red" : "blue",
                msgType: msgType.toUpperCase(),
                time: Y.mt.date.formatDateByString(new Date(time), "yyyy-MM-dd hh:mm:ss"),
                log_content: logMsg
            }
            log_content.push(Y.Lang.sub(logSpan, params));
            var template = log_content.join('');
            wrapper_review.one('#log-content').append(template);
        }

        function reopen() {
            var url = "/serverOpt/config/reopenpr";
            var data = {
                prID: currentPRID,
                appkey: appkey,
                env: dynamic_current_env
            }
            myIO(url, 'GET', data, function (ret) {
                if (ret.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('success', 'reopen成功', 3);
                    wrapper_review.one('#pr_detail_return').simulate('click');
                    wrapper_review.one('#review_type a').simulate('click');
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                }
            }, function () {
                Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
            });
        }

        function decline() {
            var url = '/serverOpt/' + appkey + '/config/review';
            var data = {
                appkey: appkey,
                prID: currentPRID,
                approve: -1,
                note: ""
            }
            myIO(url, 'POST', data, function (ret) {
                if (ret.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('success', 'decline成功', 3);
                    wrapper_review.one('#pr_detail_return').simulate('click');
                    wrapper_review.one('#pr_decline_btn').simulate('click');
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                }
            }, function () {
                Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
            });
        }

        function approve() {
            var url = '/serverOpt/' + appkey + '/config/review';
            var data = {
                appkey: appkey,
                prID: currentPRID,
                approve: 1,
                note: ""
            }
            myIO(url, 'POST', data, function (ret) {
                if (ret.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('success', 'approve成功', 3);
                    getPRDetail(currentPRID, 0);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                }
            }, function () {
                Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
            });
        }

        function mergePR() {
            var btnMergeAttr = wrapper_review.one('#detail_menu').one("#merge").hasAttribute("disabled")
            if(btnMergeAttr == true) {
                Y.msgp.utils.msgpHeaderTip('error', '这个PR还没有被Approve，不能merge！', 3);
                return;
            }
            var configURL = '/serverOpt/config/space/node/get';
            var isMergable = true
            prDetailData.forEach(function (item) {
                var nodeName = getNodenameFromPath(appkey, item.path);
                $.ajax({
                    type:"GET",
                    url: configURL,
                    data: {appkey: appkey, nodeName: nodeName},
                    async: false,
                    success:function(configRet){
                        if (configRet.success) {
                            if (item.version != configRet.data.version) {
                                Y.msgp.utils.msgpHeaderTip('error', '当前的配置已经更新，请刷新重试', 3);
                                isMergable = false;
                            }
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
                    }
                });
                /*myIO(configURL, 'GET', {appkey: appkey, nodeName: nodeName}, function (configRet) {
                    if (configRet.success) {
                        if (item.version != configRet.data.version) {
                            Y.msgp.utils.msgpHeaderTip('error', '当前的配置已经更新，请刷新重试', 3);
                            isMergable = false;
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
                    }
                }, function () {
                    Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
                })*/
                if (!isMergable) {
                    return;
                }
            });
            if (isMergable) {
                doMergePR();
            }
        }

        function doMergePR() {
            var url = "/serverOpt/" + appkey + "/config/mergepr";
            var data = {
                prID: currentPRID,
                appkey: appkey
            }
            myIO(url, 'GET', data, function (ret) {
                if (ret.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('success', 'merge成功', 3);
                    wrapper_review.one('#pr_detail_return').simulate('click');
                    wrapper_review.one('#pr_merge_btn').simulate('click');
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '服务器出错', 3);
                }

            }, function () {
                Y.msgp.utils.msgpHeaderTip('error', '服务器出错', 3);
            });
        }

        function getNodenameFromPath(appkey, path) {
            currentEnv = dynamic_current_env;
            var pathItems = path.slice(appkey.length).split("/");
            if (typeof(prId) !== "undefined") {
                var correctEnvStr = correctShowEnv(envstrUrl);
                pr_env_detail.innerHTML = correctEnvStr;
                envstr = envStrConvert(correctEnvStr);
            }else{
                envstr = envStr();
            }
            pathItems.splice(0, 2);
            var nodeName = appkey + "." + envstr;
            if (0 == pathItems.length) {
                return nodeName
            } else {
                pathItems.forEach(function (item) {
                    nodeName += "." + item;
                })
            }
            return nodeName;
        }

        var prDetailData;
        function getPRDetail(prID, type) {
            showPR(true, 'loading', null, prID);
            var url = '/serverOpt/config/prdetail';
            myIO(url, 'GET', {prID: prID}, function (ret) {
                var configURL = '/serverOpt/config/space/node/get';
                if (null == ret.data) return;
                if(0 == ret.data.detail.length) showPR(true, 'none', '无内容');
                ret.data.detail.forEach(function (item) {
                    prDetailData = ret.data.detail;

                    var nodeName = getNodenameFromPath(appkey, item.path);
                    myIO(configURL, 'GET', {appkey: appkey, nodeName: nodeName}, function (configRet) {
                        if (configRet.success) {
                            if (item.isDeleted) {
                                item.type = "删除";
                            } else {
                                var currentConfig = null;
                                configRet.data.data.forEach(function (curItem) {
                                    if (curItem.key === item.key) {
                                        currentConfig = curItem;
                                    }
                                });
                                if (null != currentConfig) {
                                    item.oldValue = currentConfig.value;
                                    item.oldComment = currentConfig.comment;
                                    item.type = "修改";
                                } else {
                                    item.type = "新增";
                                }
                            }
                            item.version = configRet.data.version;
                        }
                        ret.data.canEdit = (0 == type);
                        ret.data.type = type;
                        showPR(true, 'data', ret, prID);
                    }, function () {})
                });

            }, function () {
                Y.msgp.utils.msgpHeaderTip('error', '服务器错误', 5);
            })
        }

        function deletePR(prID) {
            showPR(false, 'loading');
            var url = '/serverOpt/' + appkey + '/config/pullrequest';
            myIO(url, 'DELETE', {prID: prID, appkey: appkey}, function (ret) {
                getPR(reviewCurrentPageNo);
                if (ret.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('info', '删除成功', 5);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '删除失败', 5);
                }
            }, function (ret) {
                getPR(reviewCurrentPageNo);
                Y.msgp.utils.msgpHeaderTip('error', '服务器错误', 5);
            });

        }

        function initOnline() {
            var url = '/common/online';
            $.ajax({
                type: "get",
                url: url,
                async: false,//取消异步
                success: function (res) {
                    if (res.isSuccess) {
                        isOnline = res.data;
                    }
                }
            });
        }

        function getOnline() {
            return isOnline;
        }

        function getEnvDesc(currentEnvNum) {
            var envDesc;
            if (3 == currentEnvNum) {
                envDesc = isOnline ? 'prod' : 'dev';
            } else if (2 == currentEnvNum) {
                envDesc = isOnline ? 'stage' : 'ppe';
            } else {
                envDesc = 'test';
            };
            return envDesc;
        }

        function getPR(pageNo) {
            initOnline();
            if(typeof(prId) !== "undefined"){
                var correctEnvStr = correctShowEnv(envstrUrl);
                pr_env.innerHTML = correctEnvStr;
                pr_env_detail.innerHTML = correctEnvStr;
                dynamic_current_env = envStr3Int(correctEnvStr);
            }else{
                pr_env.innerHTML = getEnvDesc(dynamic_current_env);
                pr_env_detail.innerHTML = getEnvDesc(dynamic_current_env);
            }
            reviewCurrentPageNo = pageNo;
            var status = Number(wrapper_review.one('#review_type .btn-primary').getAttribute('value'));
            showPR(false, 'loading');
            var url = '/serverOpt/' + appkey + '/config/pullrequest';
            var data = {
                env: dynamic_current_env,
                status: status,
                pageNo: pageNo
            }
            myIO(url, 'GET', data, function (ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    if (data.length > 0) {
                        data.forEach(function (item) {
                            if(-1 !== item.note.indexOf("\n")){
                                item.note = item.note.split("\n");
                            }else{
                                item.note =  new Array(item.note);
                            }
                        });
                        showPR(false, 'data', data);

                    } else {
                        showPR(false, 'error', '无内容');
                    }
                    refreshGroupPageNavigation4PR(ret.page);
                } else {
                    showPR(false, 'error', ret.msg || "服务器出错");
                }
            }, function () {
                showPR(false, 'error', "服务器出错");
            });

        }

        var can_modify;
        function showDetailMenu(detail_menu, prID) {
            $.ajax({
                type:"GET",
                url: '/serverOpt/config/prauthor',
                data: {prID: prID},
                async: false,
                success:function(ret){
                    detail_menu.show();
                    detail_menu.all('a').show();
                    detail_menu.one('#review_reopen').hide();
                    if (ret.isSuccess) {
                        can_modify = ret.data;
                        if (ret.data) {
                            detail_menu.one('#review_approve').hide();
                        } else {
                            detail_menu.one('#review_decline').hide();
                        }
                    }
                },
                failure: function () {}
            });
        }

        function showPR(isDetail, type, data, prID) {
            var detail = wrapper_review.one('#pr_detail_data_div');
            var prDIV = wrapper_review.one('#review_data_div');
            var detail_menu = wrapper_review.one('#detail_menu');
            var pr_menu = wrapper_review.one('#pr_menu');
            pr_menu.hide();
            detail_menu.hide();
            prDIV.hide();
            detail.hide();
            if (isDetail) {
                detail.show();
                showDetailMenu(detail_menu, prID);
                var loading = wrapper_review.one('#pr_detail_data_loading');
                var error = wrapper_review.one('#pr_detail_error');
                error.hide();
                loading.hide();
                detail.one('tbody').all('.pr_detail_tr').remove();
                detail_menu.one("#merge").removeAttribute("disabled");
                switch (type) {
                    case 'loading':
                        loading.show();
                        break;
                    case 'data':
                        if (typeof(isMerge) !== "undefined" && "2" == isMerge) {
                            data.data.canEdit = 0;
                            isMerge = "0";
                        }
                        if (!data.data.canMerge) {
                            detail_menu.one("#merge").setAttribute("disabled", "disabled");
                        }
                        if (!data.data.canEdit) {
                            detail_menu.all('a').hide();
                            if (data.data.type == -1) {
                                detail_menu.one('#review_reopen').show();
                            }
                        }
                        var micro = new Y.Template();
                        var html = micro.render(pr_detail_tr_template, {
                            data: data.data.detail,
                            canEdit: data.data.canEdit,
                            canModify:can_modify
                        });
                        detail.one('tbody').append(html);
                        if(!can_modify || 0 != data.data.pr.status){
                            var oTable = document.getElementById('pr_detail_data_table');
                            oTable.rows[0].cells[7].style.display = "none";
                            for (var i=3;i < oTable.rows.length ; i++){
                                oTable.rows[i].cells[7].style.display = "none";
                            }
                        }else{
                            var oTable = document.getElementById('pr_detail_data_table');
                            oTable.rows[0].cells[7].style.display = "block";
                            for (var i=3;i < oTable.rows.length ; i++){
                                oTable.rows[i].cells[7].style.display = "block";
                            }
                        }
                        wrapper_review.one('#log-content').all('span').remove();
                        data.data.review.forEach(function (item) {
                            var msgType = "INFO";
                            var logMsg = "";
                            switch (item.approve) {
                                case -1:
                                    msgType = "decline";
                                    logMsg = item.reviewerMisID + "不同意本次修改。";
                                    break;
                                case 1:
                                    logMsg = item.reviewerMisID + "同意了本次修改。";
                                    msgType = "approve";
                                    break;
                                case 2:
                                    logMsg = item.reviewerMisID + "合并了本次修改。";
                                    msgType = "merge";
                                    break;
                                default:
                                    logMsg = item.reviewerMisID + " 备注:" + item.note;
                                    msgType = "INFO";
                                    break;
                            }
                            printLog(msgType, item.reviewTime, logMsg);
                        });
                        if (data.data.pr != undefined) {
                            wrapper_review.one('#note-content')._node.innerText = data.data.pr.note;
                        }
                        AdjustHeight();
                        bindPrDetail();
                        break;
                    case 'error':
                        error.one('td').set('text', data);
                        error.show();
                        break;
                    case 'none':
                        detail_menu.show();
                        error.one('td').set('text', data);
                        error.show();
                        break;
                }

            } else {
                prDIV.show();
                pr_menu.show();
                var loading = wrapper_review.one('#pr_data_loading');
                var error = wrapper_review.one('#pr_error');
                error.hide();
                loading.hide();
                prDIV.one('tbody').all('.pr_tr').remove();
                switch (type) {
                    case 'loading':
                        loading.show();
                        break;
                    case 'data':

                        var reviewType = Number(wrapper_review.one('#review_type .btn-primary').getAttribute("value"));

                        var micro = new Y.Template();
                        var html = micro.render(pr_tr_template, {
                            data: data,
                            canDeleted: 1 != reviewType
                        });
                        prDIV.one('tbody').append(html);
                        break;
                    case 'error':
                        error.one('td').set('text', data);
                        error.show();
                        break;
                }
            }

        }

        function AdjustHeight() {
            var panel = wrapper_review.one('#pr_detail_data_div').one('tbody').all('.pr_detail_tr');
            var allnodes = panel._nodes;
            allnodes.forEach(function (node) {
                cells = node.childNodes;
                cells.forEach(function (item) {
                    if(7 != item.cellIndex && 8 != item.cellIndex){
                        var valuetext = item.innerHTML,
                            vflag = valuetext && valuetext.length > 50,
                            newvaluetext = vflag ? (valuetext.slice(0, 50) + '...') : valuetext;
                        var btnValueHtml = '<span class="label prValueDetail" title="点击查看详情" ' +
                            ' newvaluetext=\'' + (valuetext || '') + '\'>' +
                            '<i class="fa fa-expand" aria-hidden="true"></i></span>';
                        var valueDetail = newvaluetext + '<br/>' + (vflag ? btnValueHtml : '');
                        item.innerHTML = valueDetail;
                    }
                });
            });
        }

        function bindPrDetail() {
            var tbody = wrapper_review.one('#pr_detail_data_table tbody');
            tbody.delegate('click', function () {
                var newvaluetext = this.getAttribute('newvaluetext');
                detailPrDialog = detailPrDialog ? detailPrDialog : new Y.mt.widget.CommonDialog({
                    id: 'detail_pr_dialog',
                    title: '详细信息',
                    width: 800,
                });
                var micro = new Y.Template();
                var html = micro.render(prDetailTemplate, {
                    value: IsJsonString(newvaluetext) ? formatJson(newvaluetext) : newvaluetext
                });
                detailPrDialog.setContent(html);
                detailPrDialog.show();
                var body = detailPrDialog.getBody(),
                    prtbody = body.one('#pr-detail-table tbody');
                    prtbody.one('.value').setHTML(IsJsonString(newvaluetext) ? formatJson(newvaluetext) : newvaluetext);
                Y.one("#detail_pr_dialog")._node.style.marginTop = '50px';
            }, '.prValueDetail');
        }

        function showPage(type) {
            wrapper.one("#wrapper-sgconfig-migration-body").hide();
            wrapper.one("#wrapper-review").hide();
            wrapper.one("#wrapper-mcc-body").hide();
            wrapper.one("#wrapper-history").hide();
            wrapper.one("#wrapper-filelog").hide();///
            switch (type) {
                case 'review':
                    wrapper.one("#wrapper-review").show();
                    break;
                case 'history':
                    wrapper.one("#wrapper-history").show();
                    break;
                ///
                case 'file_history':
                    wrapper.one("#wrapper-filelog").show();
                    break;
                case 'home':
                default:
                    wrapper.one("#wrapper-mcc-body").show();
                    break;
            }
        }

        function showSgconfigMigration(type) {
            wrapper_sgconfig_migration.one('#wrapper-sgconfig-migration-body').hide();
            switch (type) {
                case 'body':
                    wrapper_sgconfig_migration.one('#wrapper-sgconfig-migration-body').show();
                    break;
                case 'none':
                    break;
            }
        }

        function sgconfigMigration() {
            showSgconfigContent('loading');
            var url = '/serverOpt/' + appkey + '/config/sgconfig/migration';
            var data = {
                app: wrapper_sgconfig_migration.one('#sgconfig_app_input').get('value'),
                env: migrationEnv,
                iswaimai: isWaimaiMigration
            }

            myIO(url, 'GET', data, suc, fail);

            function suc(ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    wrapper_sgconfig_migration.one('#sgconfig_data_view').set('value', data);
                    showSgconfigContent('data');
                    Y.msgp.utils.msgpHeaderTip('info', '导入成功', 5);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '服务器异常', 5);
                }
            }

            function fail() {
                Y.msgp.utils.msgpHeaderTip('error', '服务器异常', 5);
            }
        }

        function showSgconfigContent(type) {
            var loading = wrapper_sgconfig_migration.one('#sgconfig_data_loading');
            var textArea = wrapper_sgconfig_migration.one('#sgconfig_data_view');
            loading.hide();
            textArea.hide();
            switch (type) {
                case 'loading':
                    loading.show();
                    break;
                case 'data':
                    textArea.show();
                    break;
            }
        }

        function sgconfigPreview() {
            var url = '/serverOpt/config/sgconfig';
            var data = {
                app: wrapper_sgconfig_migration.one('#sgconfig_app_input').get('value'),
                env: migrationEnv,
                iswaimai: isWaimaiMigration
            }

            myIO(url, 'GET', data, suc, fail);

            function suc(ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    wrapper_sgconfig_migration.one('#sgconfig_data_view').set('value', data);
                    showSgconfigContent('data');
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '服务器异常', 5);
                }
            }

            function fail() {
                Y.msgp.utils.msgpHeaderTip('error', '服务器异常', 5);
            }
        }

        var editGroupID = null;

        function editGroup(self, curGroupID) {
            if (curGroupID) {
                editGroupID = curGroupID;
            } else {
                var tr = self.ancestor('tr');
                var groupID = tr.getData('id');
                editGroupID = groupID;
            }
            var url = '/serverOpt/' + appkey + '/config/file/group';
            var data = {
                env: currentEnv,
                groupID: editGroupID
            };
            myIO(url, "GET", data, suc, fail);

            function suc(ret) {
                if (ret.isSuccess) {
                    var data = ret.data;
                    var ipData = new Array();
                    var ipTag_temp = null;
                    currentversion = (null == data.version) ? "0" : data.version;
                    data.IPs.forEach(function (x) {
                        ipTag_temp = new ipTag();
                        ipTag_temp.ip = x;
                        ipTag_temp.tag = true;
                        ipData.push(ipTag_temp);
                    });
                    editGroupDialog = editGroupDialog ? editGroupDialog : new Y.mt.widget.CommonDialog({
                        id: 'edit_group_dialog',
                        title: '编辑分组',
                        width: 840,
                        btn: {
                            pass: function () {
                                doEditDialog(data.groupName, curGroupID);
                            },
                            passName: "保存"
                        }
                    });

                    var micro = new Y.Template();
                    var html = micro.render(dialogTemplate);
                    editGroupDialog.setContent(html);
                    editGroupDialog.show();
                    var dialogBody = editGroupDialog.getBody();
                    initCheck(dialogBody);
                    currentGroupsIPs = ipData;
                    dialogBody.one('#name_input').set('value', data.groupName);
                    dialogBody.one('#name_input').setAttribute('disabled', 'disabled');
                    var micro_ip = new Y.Template();
                    var ip_table = micro_ip.render(ipTableTemplate, {data: ipTagListToTwoArray(ipData)});
                    dialogBody.one("#add_group_ips").setHTML(ip_table);
                    dialogBody.delegate('click', function () {
                        clickAddIPInDialog(dialogBody);
                    }, '#group_add_ip_btn');
                    dialogBody.delegate('click', function () {
                        dialogBody.all('#one_ipcheck').set("checked", true);
                    }, '#ip-all-check');
                    dialogBody.delegate('click', function () {
                        dialogBody.all('#one_ipcheck').each(function (item) {
                            var isChecked = !this.get('checked');
                            item.set("checked", isChecked);
                        });
                    }, '#ip-all-uncheck');
                    initCurrentGroupsIps(groupID);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
                }
            }

            function fail() {
                Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
            }
        }

        function initCurrentGroupNames() {
            currentGroupNames = null;
            var url_temp = '/serverOpt/' + appkey + "/config/file/existGroupNames";
            var data_temp = {
                env: currentEnv
            }
            myIO(url_temp, 'GET', data_temp, suc_temp, fail_temp);

            function suc_temp(ret) {
                if (ret.isSuccess) {
                    currentGroupNames = ret.data;
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
                }
            }

            function fail_temp() {
                Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
            }
        }

        function ipTagListToTwoArray(ipdata) {
            var colspan_temp = 6;
            var row_num = ipdata.length / colspan_temp;
            var trData = new Array();
            for (var i = 0; i < row_num; ++i) {
                trData.push(ipdata.slice(colspan_temp * i, colspan_temp * i + colspan_temp));
            }
            return trData;
        }

        function doEditDialog(groupName, curGroupID) {
            var dialogBody = editGroupDialog.getBody();
            var ips_temp = dialogBody.all('#one_ipcheck:checked');
            var ips = new Array();
            ips_temp.each(function (item) {
                ips.push(item.get('value'));
            });
            var url = '/serverOpt/' + appkey + '/config/file/updateGroup';
            var data = {
                env: currentEnv,
                appkey: appkey,
                groupName: groupName,
                IPs: ips,
                id: editGroupID,
                version: currentversion,
            };
            myIO(url, "POST", data, suc, fail);

            function suc(ret) {
                if (ret.isSuccess) {
                    if (curGroupID) {
                        Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                        Y.msgp.serviceopt.getConfigMachine();
                        return;
                    }
                    Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                    clickCurrentEnv();
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
                }
            }

            function fail() {
                Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
            }
        }

        function initCheck(body) {
            var textArea = body.one('#group_add_btn_ip');
            checkIPs = Y.msgp.utils.check.init(textArea, {
                chineseOk: false,
                spaceOk: true,
                emptyOk: true,
                callback: checkAddIP,
                warnElement: body.one('#manual_add_ip_tips')
            });

            function checkAddIP() {
                var textArea = checkIPs.node;
                var ips = textArea.get('value').split('\n');

                var currentGroupExistIPs = new Array();
                body.all('#one_ipcheck').each(function (item) {
                    currentGroupExistIPs.push(item.get('value'));
                });

                Y.Array.each(ips, function (item, index) {
                    var ip = Y.Lang.trim(item);
                    var rowNum = index + 1;
                    if (ip.length > 0) {
                        if (!checkIP(ip)) {
                            _setMsg(checkIPs, "第" + rowNum + "行的IP地址[" + ip + "]非法", false);
                        }
                        if (null != currentGroupsIPs) {
                            for (var i = 0; i < currentGroupsIPs.length; ++i) {
                                if (currentGroupsIPs[i] === ip && !IsInArray(currentGroupExistIPs, ip)) {
                                    _setMsg(checkIPs, "第" + rowNum + "行的IP地址[" + ip + "]已经存在其他分组中", false);
                                    break;
                                }
                            }
                        }
                    }
                });

                function checkIP(ip) {
                    var exp = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
                    return ip.match(exp);
                }
            }

            function IsInArray(arr,val){
                var testStr=','+arr.join(",")+",";
                return testStr.indexOf(","+val+",")!=-1;
            }

        }

        function initCheckGroupName(body) {
            var inputName = body.one('#name_input');
            checkGroupName = Y.msgp.utils.check.init(inputName, {
                chineseOk: false,
                spaceOk: false,
                emptyOk: false,
                warnElement: inputName.next(),
                callback: function () {
                    var groupName = Y.Lang.trim(checkGroupName.node.get('value'));
                    if ((null != currentGroupNames) && (currentGroupNames.checkItemIsExist(groupName))) {
                        _setMsg(checkGroupName, "分组名已经存在", false);
                    }
                }
            });
        }

        function _setMsg(element, msg, type) {
            element._setStatus(type);
            element.opt.warnElement.setHTML(msg).setStyle('color', '#f00');
        }

        function responeAddGroup(self) {
            addGroupDialog = addGroupDialog ? addGroupDialog : new Y.mt.widget.CommonDialog({
                id: 'add_group_dialog',
                title: '增加分组',
                width: 840,
                btn: {
                    pass: doAddGroup,
                    passName: "保存"
                }
            });
            var micro = new Y.Template();
            var html = micro.render(dialogTemplate);
            addGroupDialog.setContent(html);
            addGroupDialog.show();
            var dialogBody = addGroupDialog.getBody();
            initCheck(dialogBody);
            initCheckGroupName(dialogBody);
            dialogBody.delegate('click', function () {
                clickAddIPInDialog(dialogBody);
            }, '#group_add_ip_btn');
            initCurrentGroupsIps("");
            initCurrentGroupNames();
        }

        function clickAddIPInDialog(body) {
            if (!checkIPs.isValid()) {
                checkIPs.showMsg();
                return;
            }
            var textArea = body.one('#group_add_btn_ip');
            var ips = textArea.get('value').split('\n');
            var ips_arr = new Array();

            Y.Array.each(ips, function (item) {
                var ip = Y.Lang.trim(item);
                if (ip.length > 0) {
                    ips_arr.push(ip);
                }
            });
            //保存输入框中的IPs
            ips_arr.distinct();
            var checkedIPs = new Array();
            body.all('#one_ipcheck:checked').each(function (item) {
                checkedIPs.push(item.get('value'));
            });
            var allExistIPs = new Array();
            body.all('#one_ipcheck').each(function (item) {
                allExistIPs.push(item.get('value'));
            });
            var unCheckedIPs = allExistIPs.minus(checkedIPs);
            var result_unCheckedIPs = unCheckedIPs.minus(ips_arr);
            var result_checkedIPs = checkedIPs.merge(ips_arr);
            result_checkedIPs.distinct();
            var result_IP_nodes = new Array();
            var ipTag_temp = null;
            result_checkedIPs.forEach(function (item) {
                ipTag_temp = new ipTag();
                ipTag_temp.tag = true;
                ipTag_temp.ip = item;
                result_IP_nodes.push(ipTag_temp);
            });
            result_unCheckedIPs.forEach(function (item) {
                ipTag_temp = new ipTag();
                ipTag_temp.ip = item;
                result_IP_nodes.push(ipTag_temp);
            });
            handleAddIPs(body, result_IP_nodes);
            textArea.set('value', '');
        }

        function initCurrentGroupsIps(groupID) {
            currentGroupsIPs = null;
            var url_temp = '/serverOpt/' + appkey + "/config/file/existGroupIPs";
            var data_temp = {
                env: currentEnv,
                groupID: groupID
            }
            myIO(url_temp, 'GET', data_temp, suc_temp, fail_temp);

            function suc_temp(ret) {
                if (ret.isSuccess) {
                    currentGroupsIPs = ret.data;
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
                }
            }

            function fail_temp() {
                Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
            }
        }

        function handleAddIPs(body, ipTagList) {
            var micro_ip = new Y.Template();
            var ip_table = micro_ip.render(ipTableTemplate, {data: ipTagListToTwoArray(ipTagList)});
            body.one("#add_group_ips").setHTML(ip_table);
        }

        function doAddGroup() {
            var groupName = checkGroupName.node.get('value');
            if ("" == groupName) {
                _setMsg(checkGroupName, "不能为空", false);
                return true;
            }
            if ("default" == groupName) {
                _setMsg(checkGroupName, "分组名称不能为default", false);
            }

            if ("default" == groupName)
                if (!checkGroupName.isValid()) {
                    checkGroupName.showMsg();
                    return true;
                }

            var dialogBody = addGroupDialog.getBody();
            var ips_temp = dialogBody.all('#one_ipcheck:checked');
            var ips = new Array();
            ips_temp.each(function (item) {
                ips.push(item.get('value'));
            });
            var url = '/serverOpt/' + appkey + '/config/file/addGroup';
            var data = {
                env: currentEnv,
                appkey: appkey,
                groupName: groupName,
                IPs: ips
            };
            myIO(url, "POST", data, suc, fail);

            function suc(ret) {
                if (ret.isSuccess) {
                    Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                    clickCurrentEnv();
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || "服务器异常", 3);
                }
            }

            function fail() {
                Y.msgp.utils.msgpHeaderTip('error', "服务器异常", 3);
            }

        }

        function clickDynamicEnv() {
            wrapper.one('#dynamic_env .btn-primary').simulate('click');
        }

        function clickCurrentEnv() {
            switch (currentEnv) {
                case 1:
                    test.simulate('click');
                    break;
                case 2:
                    stage.simulate('click');
                    break;
                case 3:
                    prod.simulate('click');
                    break;
            }
        }

        function envStr3Int(str) {
            switch (str) {
                case 'prod':
                    return 3;
                case 'stage':
                    return 2;
                case 'test':
                    return 1;
                case 'dev':
                    return 3;
                case 'ppe':
                    return 2;
                default:
                    return 3;
            }
        }

        function envStr() {
            switch (currentEnv) {
                case 3:
                    return "prod";
                case 2:
                    return "stage";
                case 1:
                    return "test";
            }
        }

        function envStrConvert(envstr) {
            switch (envstr) {
                case 'dev':
                    return "prod";
                case 'ppe':
                    return "stage";
                case 'test':
                    return "test";
                case 'prod':
                    return "prod";
                case 'stage':
                    return "stage";
            }
        }

        function correctShowEnv(envstr) {
            switch (envstr) {
                case 'staging':
                    return "stage";
                case 'beta':
                    return "ppe";
                case 'dev':
                    return "dev";
                case 'test':
                    return "test";
                case 'prod':
                    return "prod";
            }
        }

        function fillGroupTable(type, data) {
            var loading = wrapper_file.one('#groups_loading');
            var error_tr = wrapper_file.one('#group_error_data');
            var tbody = wrapper_file.one('#file_config_group').one('tbody');
            var navigation = wrapper_file.one('#page_navigation_groups');
            tbody.all('.group_tr').remove();
            error_tr.hide();
            loading.hide();
            navigation.hide();
            switch (type) {
                case 'error':
                    error_tr.show();
                    error_tr.one('td').set('text', data);
                    break;
                case 'loading':
                    loading.show();
                    break;
                case 'data':
                    if (data.length <= 0) {
                        fillGroupTable('error', '无内容');
                        return;
                    }
                    var micro = new Y.Template();
                    var html = micro.render(group_tr_template, {
                        data: data
                    });
                    tbody.append(html);
                    navigation.show();
                    break;
            }
        }

        function loadGroups(pageNo) {
            var url = '/serverOpt/' + appkey + '/config/file/groups';
            var data = {
                env: currentEnv,
                pageNo: pageNo
            };
            myIO(url, 'GET', data, suc, fail);

            function suc(ret) {
                if (ret.isSuccess) {
                    fillGroupTable('data', ret.data);
                    refreshGroupPageNavigation(ret.page);
                } else {
                    fillGroupTable('error', ret.msg || '内部异常');
                }
            }

            function fail() {
                fillGroupTable('error', "内部异常");
            }
        }

        function refreshGroupPageNavigation4PR(ret_page) {
            new Y.mt.widget.Paginator({
                contentBox: wrapper_review.one('#page_navigation_pr'),
                index: ret_page.pageNo,
                max: ret_page.totalPageCount,
                pageSize: ret_page.pageSize,
                totalCount: ret_page.totalCount,
                callback: groupChangePage
            });

            function groupChangePage(arg) {
                getPR(arg.page)
            }
        }

        function refreshGroupPageNavigation(ret_page) {
            new Y.mt.widget.Paginator({
                contentBox: wrapper_file.one('#page_navigation_groups'),
                index: ret_page.pageNo,
                max: ret_page.totalPageCount,
                pageSize: ret_page.pageSize,
                totalCount: ret_page.totalCount,
                callback: groupChangePage
            });

            function groupChangePage(arg) {
                loadGroups(arg.page)
            }
        }

        function myIO(url, method, data, suc, fail) {
            if ('POST' == method) data = Y.JSON.stringify(data);
            Y.io(url, {
                method: method,
                data: data,
                on: {
                    success: function (id, o) {
                        suc(Y.JSON.parse(o.responseText));
                    },
                    failure: function () {
                        fail();
                    }
                }
            });
        }


        function getMyData(url, data, msg, callback) {
            showDialog(msg);
            myIO(url, "GET", data, suc, fail);

            function suc(ret) {
                if (!isOpen) return;
                hideDialog();
                if (ret.isSuccess) {
                    callback && callback(ret.data);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.msg || '加载数据失败', 3);
                }
            }

            function fail() {
                hideDialog();
                Y.msgp.utils.msgpHeaderTip('error', '加载数据失败', 3);
            }

        }

        function showDialog(msg) {
            loadingDialog = loadingDialog ? loadingDialog : new Y.mt.widget.CommonDialog({
                width: 400,
                title: '加载数据',
                content: '<i class="fa fa-spinner fa-spin text-blue mr10"></i>' + msg,
                closeCallback: function () {
                    isOpen = false;
                }
            });
            loadingDialog.show();
            isOpen = true;
        }

        function hideDialog() {
            loadingDialog.close();
        }


        function goToGroup(groupID) {
            wrapper_file.one('#file_config_group').hide();
            var navigation = wrapper_file.one('#page_navigation_groups');
            if (null != navigation) {
                navigation.hide();
            }
            wrapper_file.one('#file_config_detail_wrap').show();
            wrapper.one('#J-config-container-menu').hide();
            Y.msgp.serviceopt.dynamicFileConfig(appkey, currentEnv, wrapper_file.one('#file_config_detail_wrap'), groupID, currentEnvText);

        }

        function returnGroup() {
            wrapper_file.one('#file_config_group').show();
            wrapper.one('#J-config-container-menu').show();
            clickCurrentEnv();
        }

        function getHistory(pageNo, env) { //获取操作日志数据
            var successCallback = function (o) {
                //wrapper_history.one('#history_data_loading').hide();
                if (o.success) {
                    initOnline();
                    var data = o.data;
                    var logs = data.operatorLog;
                    var tbody = wrapper_history.one("#history_data_div table tbody");
                    var html = '';
                    Y.Array.forEach(logs, function (log) {
                        html += generateTr(log, (env || envStr()));
                    });
                    refreshLogPageNavigation(data.page);
                    showContent('data', html);
                    bindDetail();
                } else {
                    showContent('error', '获取失败', pageNo);
                }
            };
            var failureCallback = function (msg) {
                showContent('error', '获取失败', pageNo);
            };
            var node = {
                spaceName: appkey,
                nodeName: appkey + "." + (env || envStr()),
                isLeaf: true,
                enableAdd: true,
            };
            var url = 'config/space/' + node.spaceName + '/node/clientsynclog';
            var start = wrapper_history.one('#log_start_time').get('value');
            var end = wrapper_history.one('#log_end_time').get('value');
            var searchKey = wrapper_history.one('#log_key').get('value');
            var searchValue = wrapper_history.one('#log_value').get('value');

            if(typeof(searchKey)  === 'undefined'){
                searchKey = "";
            }
            searchKey = searchKey.trim();

            if(typeof(searchValue)  === 'undefined'){
                searchValue = "";
            }
            searchValue = searchValue.trim();

            var params = {
                nodeName: node.nodeName,
                start: start,
                end: end,
                searchKey:searchKey,
                searchValue:searchValue,
                pageNo: pageNo,
                pageSize: 20
            };
            myIO(url, 'GET', params, successCallback, failureCallback);
        }

        ///
        function getFilelog(pageNo, env) { //配置文件：获取操作日志数据
            var successCallback = function (o) {
                if (o.success) {
                    var data = o.data;
                    var logs = data.operatorfileLog;

                    var tbody = wrapper_file_history.one("#filelog_data_div table tbody");
                    var html = '';
                    Y.Array.forEach(logs, function (log) {
                        //console.log("html:" + log['type']);
                        html += generateFilelog(log);
                    });
                    refreshFileLogPageNavigation(data.page);
                    showFilelogContent('data', html);
                    wordBreak(200);
                    bindFilelogDetail();
                } else {
                    showFilelogContent('error', '获取失败', pageNo);
                }
            };
            var failureCallback = function (msg) {
                showFilelogContent('error', '获取失败', pageNo);
            };
            var filelogparam = {
                filelogName: appkey,
                paramName: appkey + "." + (env || envStr()),
            };
            //获取操作日志url及参数
            var url = 'config/filelog/' + filelogparam.filelogName + '/clientsyncfilelog',
                start = wrapper_file_history.one('#file_log_start_time').get('value'),
                end = wrapper_file_history.one('#file_log_end_time').get('value');
            var params = {
                paramName: filelogparam.paramName,
                start: start,
                end: end,
                pageNo: pageNo,
                pageSize: 20
            };
            myIO(url, 'GET', params, successCallback, failureCallback);
        }

        function refreshLogPageNavigation(ret_page) {
            new Y.mt.widget.Paginator({
                contentBox: wrapper_history.one('#page_navigation_history'),
                index: ret_page.pageNo,
                max: ret_page.totalPageCount,
                pageSize: ret_page.pageSize,
                totalCount: ret_page.totalCount,
                callback: logChangePage
            });

            function logChangePage(arg) {
                var obj = getStartEnd();
                if (!obj) {
                    return;
                }
                showContent('loading');
                var env = wrapper_history.one('#history_env a.btn-primary').getAttribute('value');

                getHistory(arg.page, env);
            }
        }

        ///
        function refreshFileLogPageNavigation(ret_page) {
            new Y.mt.widget.Paginator({
                contentBox: wrapper_file_history.one('#page_navigation_filelog'),
                index: ret_page.pageNo,
                max: ret_page.totalPageCount,
                pageSize: ret_page.pageSize,
                totalCount: ret_page.totalCount,
                callback: logChangePage
            });

            function logChangePage(arg) {
                var obj = getFilelogStartEnd();
                if (!obj) {
                    return;
                }
                showFilelogContent('loading');
                var env = wrapper_file_history.one('#filelog_env a.btn-primary').getAttribute('value');
                getFilelog(arg.page, env);
            }
        }

        function showContent(type, data, pageNo) {
            var tbody = wrapper_history.one("#history_data_div table tbody");
            switch (type) {
                case 'loading':
                    tbody.setHTML('<tr id="history_data_loading"><td colspan="6">' +
                        '<i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span></td></tr>')
                    break;
                case 'data':
                    tbody.setHTML(data);
                    break;
                case 'error':
                    tbody.setHTML('<td colspan="6">' + data + '&nbsp;<a href="javascript:;" class="get-again">' +
                        '重新获取</a></td>');
                    tbody.delegate('click', function () { //绑定重新获取事件
                        var env = wrapper_history.one('#history_env a.btn-primary').getAttribute('value');
                        getHistory(pageNo, env);
                    }, '.get-again');
                    break;
            }
        }

        ///
        function showFilelogContent(type, data, pageNo) {
            var tbody = wrapper_file_history.one("#filelog_data_div table tbody");
            switch (type) {
                case 'loading':
                    tbody.setHTML('<tr id="filelog_data_loading"><td colspan="6">' +
                        '<i class="fa fa-spinner fa-spin text-blue"></i><span class="ml20">获取数据中...</span></td></tr>')
                    break;
                case 'data':
                    tbody.setHTML(data);
                    break;
                case 'error':
                    tbody.setHTML('<td colspan="6">' + data + '&nbsp;<a href="javascript:;" class="get-again">' +
                        '重新获取</a></td>');
                    tbody.delegate('click', function () { //绑定重新获取事件
                        var env = wrapper_file_history.one('#filelog_env a.btn-primary').getAttribute('value');
                        getFilelog(pageNo, env);
                    }, '.get-again');
                    break;
            }
        }

        function getStartEnd() {
            var startInput = wrapper_history.one('#log_start_time');
            var endInput = wrapper_history.one('#log_end_time');
            var obj = {
                start: '',
                end: ''
            };
            var s = startInput.get('value'),
                e = endInput.get('value');
            var reg = /^\d{4}(-\d{2}){2} \d{2}:\d{2}:\d{2}$/;
            if (s && reg.test(s)) {
                obj.start = s;
            }
            reg.lastIndex = 0;
            if (e && reg.test(e)) {
                obj.end = e;
            }
            if (s !== obj.start || e !== obj.end) {
                Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
                return null;
            }
            if (obj.start > obj.end) {
                Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
                return null;
            }
            return obj;
        }

        ///
        function getFilelogStartEnd() {
            var startInput = wrapper_file_history.one('#file_log_start_time');
            var endInput = wrapper_file_history.one('#file_log_end_time');
            var obj = {
                start: '',
                end: ''
            };
            var s = startInput.get('value'),
                e = endInput.get('value');
            var reg = /^\d{4}(-\d{2}){2} \d{2}:\d{2}:\d{2}$/;
            if (s && reg.test(s)) {
                obj.start = s;
            }
            reg.lastIndex = 0;
            if (e && reg.test(e)) {
                obj.end = e;
            }
            if (s !== obj.start || e !== obj.end) {
                Y.msgp.utils.msgpHeaderTip('error', '时间格式错误', 3);
                return null;
            }
            if (obj.start > obj.end) {
                Y.msgp.utils.msgpHeaderTip('error', '开始时间要小于结束时间', 3);
                return null;
            }
            return obj;
        }

        function bindDetail() {
            var tbody = wrapper_history.one("#history_data_div table tbody");
            tbody.delegate('click', function () { //绑定重新获取事件
                viewDetail(this);
            }, '.valueDetail');
            tbody.delegate('click', function () { //绑定重新获取事件
                viewDetail(this);
            }, '.commentDetail');
        }

        /**
         * 点击查看具体的文件内容
         */
        function bindFilelogDetail() {
            var tbodyFile = wrapper_file_history.one("#filelog_data_div table tbody");
            tbodyFile.delegate('click', function () { //绑定重新获取事件
                viewfileDetail(this);
            }, '.label.fileDetail');
        }

        function viewDetail(item, key) {
            var oldValue = item.getAttribute('oldValue');
            var newValue = item.getAttribute('newValue');
            var key = item.getAttribute('key');
            var typeInt = item.getAttribute('typeInt');
            detailDialog = detailDialog ? detailDialog : new Y.mt.widget.CommonDialog({
                id: 'detail_log_dialog',
                title: '详细信息',
                width: 1000
            });
            var micro = new Y.Template();
            var html = micro.render(logDetailTemplate, {
                oldValue: oldValue,
                newValue: newValue
            });
            detailDialog.setContent(html);
            detailDialog.show();
            var body = detailDialog.getBody(),
                tbody = body.one('#log-detail-table tbody');
            if (3 === parseInt(typeInt)) {
                tbody.one('.logOldValue').setHTML(oldValue);
                tbody.one('.logNewValue').setHTML(newValue);
            } else {
                body.one('#log-detail-table thead').hide();
                ;
                tbody.one('tr').all('td').item(0).hide();
                tbody.one('.logNewValue').setHTML(newValue);
            }
            Y.one("#detail_log_dialog").one('.title').setHTML(key);
            Y.one("#detail_log_dialog")._node.style.marginTop = '50px';
        }

        function generateTr(log, env) {
            var tr = '', td = '';
            log['time'] = Y.Date.format(new Date(log['time']), {format: '%F %T'});
            var oldValue = log['oldValue'] || '',
                newValue = log['newValue'] || '',
                oldComment = log['oldComment'] || '',
                newComment = log['newComment'] || '',
                vflag = (oldValue && oldValue.length > 200) || (newValue && newValue.length > 200),//是否截断
                cflag = (oldComment && oldComment.length > 50) || (newComment && newComment.length > 50);//是否截断
            oldValue = (oldValue && oldValue.length > 200) ? (oldValue.slice(0, 200) + '...') : oldValue;
            newValue = (newValue && newValue.length > 200) ? (newValue.slice(0, 200) + '...') : newValue;
            oldComment = (oldComment && oldComment.length > 50) ? (oldComment.slice(0, 50) + '...') : oldComment;
            newComment = (newComment && newComment.length > 50) ? (newComment.slice(0, 50) + '...') : newComment;
            //展开按钮
            var btnValueHtml = '<span class="label valueDetail" title="点击查看详情" ' +
                'key=' + log['key'] +
                ' typeInt=' + log['typeInt'] +
                ' oldValue=\'' + (log['oldValue'] || '') + '\'' +
                ' newValue=\'' + (log['newValue'] || '') + '\');">' +
                '<i class="fa fa-expand" aria-hidden="true"></i></span>';
            var btnCommentHtml = '<span class="label commentDetail" title="点击查看详情" ' +
                'key=' + log['key'] +
                ' typeInt=' + log['typeInt'] +
                ' oldValue=\'' + (log['oldComment'] || '') + '\'' +
                ' newValue=\'' + (log['newComment'] || '') + '\');">' +
                '<i class="fa fa-expand" aria-hidden="true"></i></span>';
            //value和comment详细信息
            var valueDetail = ((3 === log['typeInt']) ? '<span class="label">修改前</span>&nbsp;' +
                oldValue + '</br><span class="label">修改后</span>&nbsp;' : '') +
                newValue + '<br/>' + (vflag ? btnValueHtml : '');
            var commentDetail = ((3 === log['typeInt']) ? '<span class="label">修改前</span>&nbsp;' +
                oldComment + '</br>' + '<span class="label">修改后</span>&nbsp;' : '') +
                newComment + '<br/>' + (cflag ? btnCommentHtml : '');
            var path = log['path'];
            if (!isOnline) {
                switch (env) {
                    case 'prod' :
                        path = path.replace(/\/prod/, '/dev');
                        break;
                    case 'stage' :
                        path = path.replace(/\/stage/, '/ppe');
                        break;
                }
            }

            //通过rhino平台修改配置，操作人显示
            var operator = "rhino平台";
            if(log['operator'].length > 10 && log['operator'].indexOf("arpa") > 0){
                log['operator'] = operator;
            }

            //构建日志行
            td += '<td>' + log['time'] + '</td>';
            td += '<td>' + getTypeStr(log['typeInt']) + '</td>';
            td += '<td>' + log['operator'] + '</td>';
            td += '<td>' + path + '</td>';
            td += '<td>' + log['key'] + '</td>';
            td += '<td style="word-break: break-all;">' + valueDetail + '</td>';
            td += '<td style="word-break: break-all;">' + commentDetail + '</td>';
            tr = '<tr>' + td + '</tr>';
            return tr;
        }

        function getTypeStr(type) {
            switch (type) {
                case 1:
                    return '添加';
                case 2:
                    return '删除';
                case 3:
                    return '修改';
            }
        }

        function initDatePicker() {
            var self = this;
            var startInput = wrapper.one('#file_log_start_time');
            var endInput = wrapper.one('#file_log_end_time');
            var now = new Date();
            var yesterday = new Date(now - 7 * 24 * 60 * 60 * 1000);
            var sdate = new Y.mt.widget.Datepicker({
                node: startInput,
                showSetTime: true
            });
            sdate.on('Datepicker.select', function () {
                dateSelect();
            });
            startInput.set('value', Y.mt.date.formatDateByString(yesterday, 'yyyy-MM-dd hh:mm:ss'));
            var edate = new Y.mt.widget.Datepicker({
                node: endInput,
                showSetTime: true
            });
            edate.on('Datepicker.select', function () {
                dateSelect();
            });
            endInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd hh:mm:ss'));
        }

        function dateSelect() {
            var aArr = wrapper.one('#filelog_env').all('a'),
                curEnv = wrapper.one('#filelog_env a.btn-primary').getAttribute('value');
            aArr.each(function (item) {
                if (curEnv === item.getAttribute('value')) {
                    item.removeClass('btn-primary');
                    item.simulate('click');
                    return;
                }
            });
        }

        /**
         * 生成文件操作记录的具体内容，包含删除，下发，修改(包含上传)
         * @param log
         * @returns {string|*}
         */
        function generateFilelog(log) {
            var tr, td = '';
            log['time'] = Y.Date.format(new Date(log['time']), {format: '%F %T'});
            //构建日志行
            td += '<td>' + log['time'] + '</td>';
            td += '<td>' + log['type'] + '</td>';
            td += '<td>' + log['operator'] + '</td>';
            td += '<td>' + log['groupname'] + '</td>';
            td += '<td>' + log['filename'] + '</td>';
            if ('FILE_DISTRIBUTE' == log['type']) {
                var dSuccessList = (null != log['dSuccessList'] && '' != log['dSuccessList']) ? "下发且生效成功：" + log['dSuccessList'] + '<br/>' : '';
                var dErrorList = (null != log['dErrorList'] && '' != log['dErrorList']) ? "下发失败：" + log['dErrorList'] + '<br/>' : '';
                var eErrorList = (null != log['eErrorList'] && '' != log['eErrorList']) ? "生效失败：" + log['eErrorList'] : '';
                td += '<td>' + dSuccessList + dErrorList + eErrorList + '</td>';
            } else if ('GROUP_ADD' == log['type'] || 'GROUP_UPDATE' == log['type']) {
                var oldDetails = (null != log['oldFileContent']) ? log['oldFileContent'] : '';
                var newDetails = (null != log['newFileContent']) ? log['newFileContent'] : '';
                var dflag = (oldDetails && oldDetails.length > 200) || (newDetails && newDetails.length > 200);//是否截断
                // 展开按钮
                var btnDetailHtml = '<span class="label fileDetail" title="点击查看详情" ' +
                    'filename=' + log['filename'] +
                    ' type=' + log['type'] +
                    ' oldcontent=\'' + HTMLEncode(oldDetails) + '\'' +
                    ' newcontent=\'' + HTMLEncode(newDetails) + '\');">' +
                    '<i class="fa fa-expand" aria-hidden="true"></i></span>';
                var fileDetail = '';
                if ('' != newDetails) {
                    fileDetail += '<span class="label">新增主机</span>&nbsp;' +
                        '<span class="fileDetail" >' + HTMLEncode(newDetails) + '</span><br/>';
                }
                if ('' != oldDetails) {
                    fileDetail += '<span class="label">删除主机</span>&nbsp;' +
                        '<span class="fileDetail" >' + HTMLEncode(oldDetails) + '</span></br>';
                }
                fileDetail += (dflag && ('' != oldDetails || '' != newDetails) ? btnDetailHtml : '')

                td += '<td style="word-break: break-all;">' + fileDetail + '</td>';
            } else if ('GROUP_DELETE' == log['type']) {
                var newDetails = (null != log['newFileContent']) ? log['newFileContent'] : '';
                td += '<td style="word-break: break-all;"><span class="fileDetail" >' + HTMLEncode(newDetails) + '</span></td>';
            } else if ('FILE_DELETE' == log['type']) {
                var oldDetails = (null != log['oldFileContent']) ? log['oldFileContent'] : '';
                var dflag = (oldDetails && oldDetails.length > 200) || (newDetails && newDetails.length > 200);//是否截断
                // 展开按钮
                var btnDetailHtml = '<span class="label fileDetail" title="点击查看详情" ' +
                    'filename=' + log['filename'] +
                    ' type=' + log['type'] +
                    ' oldcontent=\'' + HTMLEncode(oldDetails) + '\'' +
                    ' newcontent=\'\');">' +
                    '<i class="fa fa-expand" aria-hidden="true"></i></span>';
                var fileDetail = '<span class="label">修改前</span>&nbsp;' +
                    '<span class="fileDetail" >' + HTMLEncode(oldDetails) + '</span></br>' + (dflag ? btnDetailHtml : '');
                td += '<td style="word-break: break-all;">' + fileDetail + '</td>';
            } else if ('FILE_UPDATE' == log['type']) {
                var oldDetails = (null != log['oldFileContent']) ? log['oldFileContent'] : '';
                var newDetails = (null != log['newFileContent']) ? log['newFileContent'] : '';
                var dflag = (oldDetails && oldDetails.length > 200) || (newDetails && newDetails.length > 200);//是否截断
                // 展开按钮
                var btnDetailHtml = '<span class="label fileDetail" title="点击查看详情" ' +
                    'filename=' + log['filename'] +
                    ' type=' + log['type'] +
                    ' oldcontent=\'' + HTMLEncode(oldDetails) + '\'' +
                    ' newcontent=\'' + HTMLEncode(newDetails) + '\');">' +
                    '<i class="fa fa-expand" aria-hidden="true"></i></span>';

                var fileDetail = '<span class="label">修改前</span>&nbsp;' +
                    '<span class="fileDetail" >' + HTMLEncode(oldDetails) + '</span></br><span class="label">修改后</span>&nbsp;' +
                    '<span class="fileDetail" >' + HTMLEncode(newDetails) + '</span><br/>' + (dflag ? btnDetailHtml : '');
                td += '<td style="word-break: break-all;">' + fileDetail + '</td>';
            }
            tr = '<tr>' + td + '</tr>';
            return tr;
        }

        /**
         * 文件内容转义
         * @param html
         * @returns {string}
         * @constructor
         */
        function HTMLEncode(html) {
            var temp = document.createElement("filelogdiv");
            (temp.textContent != null) ? (temp.textContent = html) : (temp.innerText = html);
            var output = temp.innerHTML;
            temp = null;
            return output;
        }

        /**
         * 截断过长的文件内容
         * @param maxwidth：展示长度
         */
        function wordBreak(maxwidth) {
            $(document).ready(function () {
                //限制字符个数
                $(".fileDetail").each(function () {
                    if ($(this).text().length > maxwidth) {
                        $(this).text($(this).text().substring(0, maxwidth));
                        $(this).html($(this).html() + '...');
                    }
                });
            });
        }

        /**
         * 查看具体的修改或删除的文件内容
         * @param item
         */
        function viewfileDetail(item) {
            //console.log(item);
            var oldContent = item.getAttribute('oldcontent');
            var newContent = item.getAttribute('newcontent');
            var filename = item.getAttribute('filename');
            var type = item.getAttribute('type');
            fileDetailDialog = fileDetailDialog ? fileDetailDialog : new Y.mt.widget.CommonDialog({
                id: 'detail_filelog_dialog',
                title: '详细信息',
                width: 1000
            });
            var micro = new Y.Template();
            var html = micro.render(fileLogDetailTemplate, {
                oldContent: oldContent,
                newContent: newContent
            });
            fileDetailDialog.setContent(html);
            fileDetailDialog.show();
            var body = fileDetailDialog.getBody(),
                tbody = body.one('#log-detail-table tbody');
            if ('FILE_UPDATE' === type) {
                tbody.one('.logOldValue').set("text", oldContent);
                tbody.one('.logNewValue').set("text", newContent);
            } else {
                body.one('#log-detail-table thead').hide();
                tbody.one('tr').all('td').item(0).hide();
                tbody.one('.logNewValue').set("text", oldContent);
            }

            Y.one("#detail_filelog_dialog").one('.title').setHTML(filename);
            Y.one("#detail_filelog_dialog")._node.style.marginTop = '50px';
        }

        var modifyPRStr = [
            '<%var key = this.key;%>',
            '<tr>',
            '<td>key:</td>',
            '<td><%= key%></td>',
            '</tr>',
            '<tr>',
            '<td>new_value:</td>',
            '<td>',
            '<textarea id="modifyPRValue" style="width:120px;height: 30px;" placeholder="修改的value">',
            '</textarea>',
            '</td>',
            '</tr>',
            '<tr>',
            '<td>new_comment:</td>',
            '<td>',
            '<textarea id="modifyPRComment" style="width:120px;height: 30px;" placeholder="修改的comment">',
            '</textarea>',
            '</td>',
            '</tr>',
        ].join('');

        var modify_key;
        function clickModifyPRDetail(itemNum) {
            updatePRDialog = new Y.mt.widget.CommonDialog({
                id: 'modify_pr_dialog',
                title: '修改PR',
                width: 320,
                btn: {
                    pass: doUpdatePR,
                    passName: '提交'
                }
            });
            modify_key = prDetailData[itemNum].key;
            var micro = new Y.Template();
            var str = micro.render(modifyPRStr, {
                key: prDetailData[itemNum].key
            });
            updatePRDialog.setContent(str);
            updatePRDialog.show();
            function doUpdatePR() {
                var value = $("#modifyPRValue").val();
                var comment = $("#modifyPRComment").val();
                var b = new Array();
                for(var i = 0; i < prDetailData.length; i++){
                    if(i == itemNum){
                        var data = {
                            PrDetailID:prDetailData[i].prDetailID,
                            key: prDetailData[i].key,
                            NewValue:value,
                            NewComment:comment,
                            isDeleted:prDetailData[i].isDeleted
                        };
                    }else {
                        var data = {
                            PrDetailID:prDetailData[i].prDetailID,
                            key: prDetailData[i].key,
                            NewValue: prDetailData[i].newValue,
                            NewComment: prDetailData[i].newComment,
                            isDeleted:prDetailData[i].isDeleted
                        };
                    }
                    b.push(data);
                }
                var datas = {
                    appkey: appkey,
                    PrID: prDetailData[0].prID,
                    data: b
                };

                updatePRDetail(datas);
            }
        }

        function clickDeletePRDetail(itemNum) {
            deletePRDialog = new Y.mt.widget.CommonDialog({
                id: 'delete_pr_dialog',
                title: '删除',
                width: 320,
                btn: {
                    pass: doDeletePRDetail,
                    passName: '确定'
                }
            });
            //var micro = new Y.Template();
            deletePRDialog.setContent("确定要删除吗?");
            deletePRDialog.show();
            function doDeletePRDetail() {
                var b = new Array();
                for(var i = 0; i < prDetailData.length; i++){
                    if(i == itemNum) continue;
                    var data = {
                        PrDetailID:prDetailData[i].prDetailID,
                        key: prDetailData[i].key,
                        NewValue: prDetailData[i].newValue,
                        NewComment: prDetailData[i].newComment,
                        isDeleted:prDetailData[i].isDeleted
                    }
                    b.push(data);
                }
                if(b.length == 0){
                    Y.msgp.utils.msgpHeaderTip('error', "只剩一条记录，请勿删除。如需删除，请点击Decline撤销PR。", 3);
                }else {
                    var datas = {
                        appkey: appkey,
                        PrID: prDetailData[0].prID,
                        data: b
                    };
                    updatePRDetail(datas);
                }
            }
        }

        var pr_detail_modify_all_template = [
            '<%Y.Array.each(this.data,function(item){ %>',
            '<tr class="pr_detail_tr" data-version="<%= item.version%>">',
            '<td><%= item.key%>:</td>',
            '<td>&nbsp&nbsp&nbsp',
            '<textarea id ="<%= item.key%>_modify_value"><%=item.newValue%></textarea>',
            '</td>&nbsp&nbsp&nbsp',
            '<td>',
            '<textarea id = "<%= item.key%>_modify_comment"><%=item.newComment%></textarea>',
            '</td>',
            '</tr>',
            '<%});%>'
        ].join('');

        function modifyAllPRDetail(){
            updateAllPRDialog = new Y.mt.widget.CommonDialog({
                id: 'update_all_pr_dialog',
                title: '批量修改',
                width: 640,
                btn: {
                    pass: doUpdateAllPRDetail,
                    passName: '提交'
                }
            });
            var micro = new Y.Template();
            var html = micro.render(pr_detail_modify_all_template, {
                data: prDetailData
            });
            updateAllPRDialog.setContent(html);
            updateAllPRDialog.show();
            function doUpdateAllPRDetail() {
                var array = new Array();
                var newValue;
                var newComment;
                for(var i = 0; i < prDetailData.length; i++){
                    newValue = $("#" + prDetailData[i].key + "_modify_value").val();
                    newComment = $("#" + prDetailData[i].key + "_modify_comment").val();
                    var data = {
                        PrDetailID:prDetailData[i].prDetailID,
                        key: prDetailData[i].key,
                        NewValue: newValue,
                        NewComment: newComment,
                        isDeleted:prDetailData[i].isDeleted
                    }
                    array.push(data);
                }
                var datas = {
                    appkey: appkey,
                    PrID: prDetailData[0].prID,
                    data: array
                };
                updatePRDetail(datas);
            }
        }

        function updatePRDetail(datas) {
            var successCallback = function (ret) {
                if (ret.isSuccess) {
                    $("#modify_pr_dialog").remove();
                    $("#delete_pr_dialog").remove();
                    $("#update_all_pr_dialog").remove();
                    var type = Number(wrapper_review.one('#review_type .btn-primary').getAttribute("value"));
                    getPRDetail(prDetailData[0].prID, type);
                    Y.msgp.utils.msgpHeaderTip('success', ret.data, 3);
                } else {
                    Y.msgp.utils.msgpHeaderTip('error', ret.data || '系统出错', 3);
                }
            };
            var failureCallback = function (msg) {
                Y.msgp.config.popup.alert("创建失败");
            };

            Y.io('/serverOpt/config/updateprdetail', {
                method: 'POST',
                headers: {'Content-Type': "application/json;charset=UTF-8"},
                data:  Y.JSON.stringify(datas),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            successCallback(ret);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', ret.msg || '服务器出错', 3);
                        }
                    },
                    failure: function (id, o) {
                        failureCallback("系统出错");
                    }
                }
            });
        }

        var fileLogDetailTemplate = [
            '<div id="log-detail-table" class="form-horizontal">',
            '<table class="table table-bordered">',
            '<thead>',
            '<tr>',
            '<th>修改前</th>',
            '<th>修改后</th>',
            '</tr>',
            '</thead>',
            '<tbody>',
            '<tr>',
            '<td width="50%" style="word-break:break-all;"><pre style="height:500px;overflow-y:auto" class="logOldValue"></pre></td>',
            '<td width="50%" style="word-break:break-all;overflow-y:hidden"><pre style="height:500px;overflow-y:auto" class="logNewValue"></pre></td>',
            '</tr>',
            '</tbody>',
            '</table>',
            '</div>'
        ].join('');

    },

    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'w-base',
            'template',
            'datatype-date-format',
            'msgp-serviceopt/fileConfig-version0.0.11',
            'msgp-utils/msgpHeaderTip',
            'msgp-serviceopt/dynamicFileConfig-version0.0.10',
        ]
    }
)
;

