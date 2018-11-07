/**
 * Created by yves on 16/12/27.
 */

M.add('msgp-component/cmpt_config', function (Y) {
    var tab_style = "";
    var groupId = '';
    var artifactId = '';
    var configWrapper;
    //增加控制
    var blackListFormWrapper;
    //白名单
    var whiteListFormWrapper;

    //控制项列表
    var blackListTableWrapper;
    var blackListTableTbody;

    var whiteListTableWrapper;
    var whiteListListTableTbody;

    
    var groupIdCache = {};
    var artifactIdCache= {};
    var limitNumber = 10;
    var addBlackListDialog;
    var addWhiteListDialog;

    Y.namespace('msgp.component').cmpt_config = cmpt_config;
    function cmpt_config(_tab_style, _groupId, _artifactId) {
        document.title = '组件版本控制';
        tab_style = _tab_style;
        configWrapper = Y.one('#div_cmpt_config');
        blackListTableWrapper = configWrapper.one('#config_list_wrapper');
        blackListTableTbody = blackListTableWrapper.one('tbody');
        
        configWrapper.one("#search_groupId" ).set("value", groupId);
        configWrapper.one("#search_artifactId" ).set("value", artifactId);
        initEvent();
        initWidget('search');
        getBlackListData();
    }

    function initEvent() {
        configWrapper.one('#search_config').on('click', function () {
            getBlackListData();
        });

        configWrapper.delegate('click', function () {
            Y.all('#base_select a').removeClass('btn-primary');
            this.addClass("btn-primary");
            getBlackListData();
        }, '.btn-base');

        configWrapper.one('#del_all_black_list').on('click', function () {
            var allCheckedTr = blackListTableWrapper.all("#one-checkbox:checked");
            var configs = [];
            allCheckedTr.each(function (item, index) {
                var line = item.ancestor('tr');
                var info = line.getData('info');
                if (Y.Lang.isString(info)) {
                    info = Y.JSON.parse(info);
                }
                var infoObj = {
                    groupId: info.groupId,
                    artifactId: info.artifactId,
                    base: info.base,
                    business: info.business,
                    owt: info.owt,
                    pdl: info.pdl,
                    version: info.version,
                    action : info.action
                };
                configs.push(infoObj);
            });
            batchDeleteBlackListItem(configs);
        });

        configWrapper.one('#add_black_list').on('click', function () {
            addBlackListDialog = addBlackListDialog ? addBlackListDialog : initAdBlackListDialog();
            var micro = new Y.Template();
            var addTemplate = Y.one('#text_add_black_list_form').get('value');
            var arr = {groupId: groupId, artifactId: artifactId};
            var str = micro.render(addTemplate, {data: arr});
            addBlackListDialog.setContent(str);
            addBlackListDialog.show();

            blackListFormWrapper = addBlackListDialog.getBody();
            initWidget('black_list');
            initCmpt();
            bindOwtPdl(blackListFormWrapper, 'black_list');

            blackListFormWrapper.one('#show-black_list_version-input').on('click', function () {
                blackListFormWrapper.one('#black_list_version-input-line').setStyle('display', 'inherit');
            });

            blackListFormWrapper.one('#black_list_cmpt').on('change', function () {
                var cmpt = this.get('value').split(",");
                groupId = cmpt[0];
                artifactId = cmpt[1];
                blackListFormWrapper.one("#black_list_groupId" ).set("value", groupId);
                blackListFormWrapper.one("#black_list_artifactId" ).set("value", artifactId);
                getVersion(groupId, artifactId);
            });
        });

        blackListTableWrapper.delegate('click', deleteBlackListItem, '.delete-black-list');

        blackListTableWrapper.delegate('click', addWhiteList, '.add-white-list');
        
        bindOwtPdl(configWrapper, 'search');
        bindClickCheckBox();
    }

    function bindOwtPdl(node, view) {
        node.one('#' + view + '_business').on('change', function () {
            var business = this.get('value');
            if(business == "") {
                node.one('#' + view + '_owt').empty();
                node.one('#' + view + '_pdl').empty();
                node.one('#' + view + '_owt').append('<option value=all>all</option>');
                node.one('#' + view + '_pdl').append('<option value=all>all</option>');
            }else{
                getOwt(business, node, view);
            }
        });
        node.one('#' + view + '_owt').on('change', function () {
            var owt = this.get('value');
            if(owt == "all") {
                node.one('#' + view + '_pdl').empty();
                node.one('#' + view + '_pdl').append('<option value=all>all</option>');
            }else{
                getPdl(owt, node, view);
            }
        });
    }

    function bindClickCheckBox() {
        //全选
        blackListTableWrapper.delegate('click', function () {
            var ifChecked = this.get("checked");
            //单选与全选保持一致
            blackListTableWrapper.all('#one-checkbox').set("checked", ifChecked);
        }, '#all-check');
        //单选
        blackListTableWrapper.delegate('click', function () {
            //全选与单选保持一致
            var allOneCheck = blackListTableWrapper.all('#one-checkbox');
            var allOneChecked = blackListTableWrapper.all('#one-checkbox:checked');
            if (allOneChecked.size() === 0) {
                blackListTableWrapper.one('#all-check').set("checked", false);
            } else {
                if (allOneCheck.size() === allOneChecked.size()) {
                    blackListTableWrapper.one('#all-check').set("checked", true);
                }
            }
        }, '#one-checkbox');
    }

    function initWidget(id_prefix) {
        $('#'+ id_prefix + '_groupId').autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in groupIdCache ){
                    response(groupIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/group_id", {
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    groupIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function( event, ui ){
                groupId = ui.item.value;
                $("#artifactId" ).attr("value", "");
            }
        });
        $('#'+ id_prefix + '_artifactId').autocomplete({
            source: function( request, response ) {
                var term = request.term;
                if(term.length < 1){
                    return;
                }
                if ( term in artifactIdCache ){
                    response(artifactIdCache[ term ]);
                    return;
                }
                jQuery.get("/component/artifact_id", {
                    groupId: groupId,
                    keyword: term,
                    limitNumber: limitNumber
                }, function (data) {
                    artifactIdCache[ term ] = data;
                    response(data);
                });
            },
            select: function (event, ui) {
                artifactId = ui.item.value;
                if(id_prefix == 'black_list'){
                    getVersion(groupId, artifactId);
                }
            }
        });
    }

    function initCmpt() {
        blackListFormWrapper.one('#black_list_cmpt').empty();
        var url = '/component/cmpt';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        blackListFormWrapper.one('#black_list_cmpt').append('<option value="">快速填充</option>');
                        Y.each(ret.data, function(item) {
                            blackListFormWrapper.one('#black_list_cmpt').append('<option value=' + item.groupId +',' + item.artifactId + '>' + item.artifactId + '</option>');
                        });
                        blackListFormWrapper.one('#black_list_cmpt').set('value', '');
                        getVersion(groupId, artifactId);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取默认组件列表失败', 3);
                }
            }
        });
    }

    function initAdBlackListDialog() {
        var dialog = new Y.mt.widget.CommonDialog({
            title: '增加配置项',
            width: 640,
            drag: function () {
            },
            refresh: 1,
            btn: {
                pass: saveBlackListDialog
            }
        });
        return dialog;
    }

    function getVersion(groupId, artifactId) {
        blackListFormWrapper.one('#black_list_version').empty();
        var searchData = {
            "groupId": groupId,
            "artifactId": artifactId
        };
        var url = '/component/version';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        blackListFormWrapper.one('#black_list_version').append('<option value="">未选择</option>');
                        Y.each(ret.data, function (item) {
                            blackListFormWrapper.one('#black_list_version').append('<option value=' + item + '>' + item + '</option>');
                        });
                        blackListFormWrapper.one('#black_list_version').set('value', '');
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取版本失败', 3);
                }
            }
        });
    }


    function getOwt(business, node, view) {
        node.one('#' + view + '_owt').empty();
        node.one('#' + view + '_pdl').empty();
        node.one('#' + view + '_owt').append('<option value=all>all</option>');
        node.one('#' + view + '_pdl').append('<option value=all>all</option>');
        var url = '/component/owt';
        Y.io(url, {
            method: 'get',
            data: {
                "business" : business
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            node.one('#' + view + '_owt').append('<option value=' + item + '>' + item + '</option>');
                        });
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                }
            }
        });
    }

    function getPdl(owt, node, view) {
        node.one('#' + view + '_pdl').empty();
        node.one('#' + view + '_pdl').append('<option value=all>all</option>');
        var url = '/component/pdl';
        Y.io(url, {
            method: 'get',
            data: {
                "owt" : owt
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            node.one('#' + view + '_pdl').append('<option value=' + item + '>' + item + '</option>');
                        });
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取产品线失败', 3);
                }
            }
        });
    }

    function getBlackListData(config) {
        var searchData;
       /* if(config != null){
            searchData = config;
            configWrapper.one("#search_business" ).set("value", config.business);
            configWrapper.one("#search_owt" ).set("value", config.owt);
            configWrapper.one("#search_pdl" ).set("value", config.pdl);
            configWrapper.one("#search_action" ).set("value", config.action);
            configWrapper.one("#search_groupId" ).set("value", config.groupId);
            configWrapper.one("#search_artifactId" ).set("value", config.artifactId);
        }else{*/
        var base = configWrapper.one('#base_select a.btn-primary').getAttribute('value');
        var business = configWrapper.one("#search_business").get('value');
        var owt = configWrapper.one("#search_owt").get('value');
        var pdl = configWrapper.one("#search_pdl").get('value');
        var action =  configWrapper.one("#search_action").get('value');
        artifactId = Y.Lang.trim(configWrapper.one("#search_artifactId").get('value'));
        groupId =  Y.Lang.trim(configWrapper.one("#search_groupId").get('value'));

        searchData = {
            groupId: groupId,
            artifactId: artifactId,
            base: base,
            business:business,
            owt: owt,
            pdl: pdl,
            action : action
        };
        /*}*/
        var url = '/component/config/get/blacklist/rich';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        fillBlackListTable(ret.data);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }
    
    function fillBlackListTable(data){
        var html = '';
        if(data.length == 0) {
            html = '<tr><td colspan="8" style="text-align: center; color: seagreen;">暂无版本配置信息</td></tr>';
        }else{
            var micro = new Y.Template();
            html = micro.render(blackListTableTemplate, {data: data});
        }
        blackListTableTbody.setHTML(html);
    }

    //删除单个
    function deleteBlackListItem(e) {
        var line = e.target.ancestor('tr');
        var info = line.getData('info');
        if (Y.Lang.isString(info)) {
            info = Y.JSON.parse(info);
        }
        var configs = [{
            groupId: info.groupId,
            artifactId: info.artifactId,
            base: info.base,
            business: info.business,
            owt: info.owt,
            pdl: info.pdl,
            version: info.version,
            action : info.action
        }];
        var url = '/component/config/delete/blacklist';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(configs),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        line.remove();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                }
            }
        });
    }

    //批量删除
    function batchDeleteBlackListItem(configs) {
        var url = '/component/config/delete/blacklist';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(configs),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                        var searchData = {
                            groupId: '',
                            artifactId: '',
                            base: 'meituan',
                            business:'all',
                            owt: 'all',
                            pdl: 'all',
                            action : 'warning'
                        };
                        getBlackListData(searchData);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                }
            }
        });
    }

    function saveBlackListDialog() {
        var base = blackListFormWrapper.one('#black_list_base').get('value');
        var business = blackListFormWrapper.one('#black_list_business').get('value');
        var owt = blackListFormWrapper.one('#black_list_owt').get('value');
        var pdl = blackListFormWrapper.one('#black_list_pdl').get('value');

        var groupIdSelected = blackListFormWrapper.one('#black_list_groupId').get('value');
        var artifactIdSelected = blackListFormWrapper.one('#black_list_artifactId').get('value');
        var versionSelected = blackListFormWrapper.one('#black_list_version').get('value');
        var versionInputSelected = blackListFormWrapper.one('#black_list_version-input').get('value');
        if(versionInputSelected != null && versionInputSelected != ''){
            versionSelected = versionInputSelected;
        }
        var action = blackListFormWrapper.one('#black_list_action').get('value');

        if(groupIdSelected == '' || artifactIdSelected == '' || versionSelected == ''){
            return
        }

        var addedBlackList = {
            groupId: groupIdSelected,
            artifactId: artifactIdSelected,
            version: versionSelected,
            action: action,
            base: base,
            business: business,
            owt: owt,
            pdl: pdl
        };

        showWaitMsg(blackListTableTbody);
        var url = '/component/config/add/blacklist';
        Y.io(url, {
            method: 'post',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(addedBlackList),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '添加成功', 3);
                        getBlackListData(addedBlackList);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
                        clearWaitMsg(blackListTableTbody);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
                    clearWaitMsg(blackListTableTbody);
                }
            }
        });
    }
    
    function getWhiteListData(appConfigId) {
        var url = '/component/config/get/whitelist';
        Y.io(url, {
            method: 'get',
            data: {
                app_config_id: appConfigId
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        var html = '';
                        if(data.length == 0) {
                            html = '<tr><td style="text-align: center; color: seagreen;" colspan="5">暂无白名单信息</td></tr>';
                        }else{
                            var micro = new Y.Template();
                            html = micro.render(whiteListTableTemplate, {data: data});
                        }
                        whiteListListTableTbody.setHTML(html);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '获取白名单失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '获取白名单失败', 3);
                }
            }
        });
    }
    
    function addWhiteList(e) {
        addWhiteListDialog = addWhiteListDialog ? addWhiteListDialog : new Y.mt.widget.CommonDialog({
            title: '白名单配置',
            width: 1000,
            height: 500,
            overflow: scroll,
            drag: function () {
            },
            refresh: 1
        });
        var micro = new Y.Template();
        var addTemplate = Y.one('#text_add_white_list_form').get('value');
        var str = micro.render(addTemplate);
        addWhiteListDialog.setContent(str);

        var line = e.target.ancestor('tr');
        var info = line.getData('info');
        if (Y.Lang.isString(info)) {
            info = Y.JSON.parse(info);
        }

        whiteListFormWrapper = addWhiteListDialog.getBody();

        var businessOriginal = info.business;
        var owtOriginal = info.owt;
        var pdlOriginal = info.pdl;
        var appConfigId = info.id;
        
        if(businessOriginal != 'all'){
            whiteListFormWrapper.one("#white_list_business" ).set("value", businessOriginal);
            whiteListFormWrapper.one("#white_list_business" ).set('disabled', true);
        }else{
            whiteListFormWrapper.one("#white_list_business" ).set("value", "");
        }
        if(owtOriginal != 'all'){
            whiteListFormWrapper.one("#white_list_owt" ).set("value", owtOriginal);
            whiteListFormWrapper.one("#white_list_owt" ).set('disabled', true);
        }
        if(pdlOriginal != 'all'){
            whiteListFormWrapper.one("#white_list_pdl" ).set("value", pdlOriginal);
            whiteListFormWrapper.one("#white_list_pdl" ).set('disabled', true);
        }
        
        if(businessOriginal != 'all' && owtOriginal != 'all' && pdlOriginal != 'all'){
            whiteListFormWrapper.one("#white_list_app" ).set("placeholder", '项目名(必填)');
        }

        whiteListFormWrapper.one('#add_white_list_item').on('click', function () {
            saveWhiteListDialog(appConfigId);
        });

        whiteListTableWrapper =  whiteListFormWrapper.one('#config_white_list_wrapper');
        whiteListListTableTbody = whiteListTableWrapper.one('tbody');

        whiteListTableWrapper.delegate('click', function (e) {
            var line = e.target.ancestor('tr');
            var info = line.getData('info');
            if (Y.Lang.isString(info)) {
                info = Y.JSON.parse(info);
            }
            var whiteListTobeDelete = {
                appConfigId: info.appConfigId,
                business: info.business,
                owt: info.owt,
                pdl: info.pdl,
                app: info.app
            };
            var url = '/component/config/delete/whitelist';
            Y.io(url, {
                method: 'POST',
                headers : {'Content-Type':"application/json;charset=UTF-8"},
                data: Y.JSON.stringify(whiteListTobeDelete),
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            getWhiteListData(appConfigId);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '白名单删除失败', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '白名单删除失败', 3);
                    }
                }
            });
        }, '.delete-white-list');

        getWhiteListData(appConfigId);
        addWhiteListDialog.show();
    }
    
    function saveWhiteListDialog(appConfigId) {
        //各个输入框只允许 合理值和all这两种
        var businessSelected = whiteListFormWrapper.one('#white_list_business').get('value');
        var owtSelected = whiteListFormWrapper.one('#white_list_owt').get('value');
        var pdlSelected = whiteListFormWrapper.one('#white_list_pdl').get('value');
        var appSelected = whiteListFormWrapper.one('#white_list_app').get('value');

        if(owtSelected == '' || pdlSelected == '' || appSelected == ''){
            Y.msgp.utils.msgpHeaderTip('error', 'owt/pdl/app不能为空', 3);
            return;
        }
        
        var validData = false;
        if(owtSelected != 'all' && pdlSelected != 'all' && appSelected == 'all'){
            //整个产品线加入白名单
            validData = true;
        }else if(owtSelected != 'all' && pdlSelected == 'all' && appSelected == 'all'){
            //整个业务线加入白名单
            validData = true;
        }else if(owtSelected == 'all' && pdlSelected == 'all' && appSelected == 'all'){
            //整个事业群加入白名单
            validData = true;
        }else if(owtSelected != 'all' && pdlSelected != 'all' && appSelected != 'all'){
            //设置单个项目,此时忽略business/owt/pdl
            validData = true;
        }
        
        if(!validData){
            Y.msgp.utils.msgpHeaderTip('error', '输入的owt/pdl/app参数错误,只能为all或具体值', 5);
            return;
        }

        var addedWhiteList = {
            appConfigId: appConfigId,
            business: businessSelected,
            owt: owtSelected,
            pdl: pdlSelected,
            app: appSelected
        };

        var url = '/component/config/add/whitelist';
        Y.io(url, {
            method: 'POST',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(addedWhiteList),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        getWhiteListData(appConfigId);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '白名单保存失败', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '白名单保存失败', 3);
                }
            }
        });
    }

    function showWaitMsg(node) {
        var html = '<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.5); z-index: 999;">'+
            '<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #00aaee;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">后台操作中...</span></div>'+
            '</div>';
        node.append(html);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    var blackListTableTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-info="<%= Y.JSON.stringify(item) %>">',
        '<td><input id="one-checkbox" type="checkbox"></td>',
        '<% if(item.base == "meituan") { %>',
        '<td>北京</td>',
        ' <% } else { %>',
        '<td>上海</td>',
        ' <% } %>',
        '<td><%= item.business %></td>',
        '<td><%= item.owt %></td>',
        '<td><%= item.pdl %></td>',
        '<td><%= item.groupId %> : <%= item.artifactId %></td>',
        '<td><%= item.version %></td>',
        '<% if(item.action == "warning") { %>',
        '<td>warning(低版本消息提醒)</td>',
        ' <% } else { %>',
        '<td style="color: red;">broken(低版本中止发布)</td>',
        ' <% } %>',
        '<td><a class="add-white-list" href="javascript:;"><i class="fa fa-edit"> 白名单</i></a></td>',
        '<td><a class="delete-black-list" href="javascript:;"><i class="fa fa-trash-o"> 删除</i></a></td>',
        '</tr>',
        '<% }); %>'
    ].join('');

    var whiteListTableTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr data-info="<%= Y.JSON.stringify(item) %>">',
        '<td><%= item.business %></td>',
        '<td><%= item.owt %></td>',
        '<td><%= item.pdl %></td>',
        '<td><%= item.app %></td>',
        '<td><a class="delete-white-list" href="javascript:;"><i class="fa fa-trash-o"> 删除配置</i></a></td>',
        '</tr>',
        '<% }); %>'
    ].join('');


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-base',
        'template',
        'msgp-utils/msgpHeaderTip'
    ]
});