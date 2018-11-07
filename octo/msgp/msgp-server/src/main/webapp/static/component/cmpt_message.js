/**
 * Created by liwen on 16/9/6.
 */

M.add('msgp-component/cmpt_message', function (Y) {
    Y.namespace('msgp.component').cmpt_message = cmpt_message;
    var tab_style = "",
        groupId,
        artifactId,
        groupIdCache = {},
        artifactIdCache= {},
        versionCache=[],
        pbody,
        limitNumber = 10,
        messageWrapper,
        messageListWrapper,
        searchForm,
        componentSelected = [];
    var inited = false;

    function cmpt_message(_tab_style, _groupId, _artifactId) {
        tab_style = _tab_style;
        document.title = '组件依赖提醒';
        groupId = _groupId;
        artifactId = _artifactId;
        messageWrapper = Y.one('#div_cmpt_message');
        messageListWrapper = messageWrapper.one("#message_list_wrapper");
        searchForm = messageWrapper.one('#messageSearchForm');
        initWidget();
        bindWidget();
        setDefaultValue();
        window.history.pushState({}, 0, window.location.pathname+window.location.hash);
    }

    function initCmpt() {
        messageWrapper.one('#cmpt').empty();
        var url = '/component/cmpt';
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.each(ret.data, function(item) {
                            messageWrapper.one('#cmpt').append('<option value=' + item.groupId +',' + item.artifactId + '>' + item.artifactId + '</option>');
                        });
                        messageWrapper.one('#cmpt').set('value',groupId +',' + artifactId);
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

    function initWidget() {
        $("#groupId" ).autocomplete({
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
                $('#version').empty();
                $('#version').append('<option value=all>all</option>');
            }
        });
        $("#artifactId" ).autocomplete({
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
            select: function( event, ui ){
                artifactId = ui.item.value;
                getVersion(groupId, artifactId);
            }
        });
        initCmpt();
        pbody = messageWrapper.one('#paginator_message_wrapper');
    }


    function setDefaultValue() {
        messageWrapper.one("#groupId" ).set("value", groupId);
        messageWrapper.one("#artifactId" ).set("value", artifactId);
        messageWrapper.one("#version_label" ).set("value", "最低版本");
        inited = false;
        getVersion(groupId, artifactId);
    }

    function bindWidget() {
        searchForm.delegate('click', afterSaveClick, '#cmptAddBtn');
        messageListWrapper.delegate('click', afterDeteleClick, '.message-version-delete');
        messageWrapper.one('#cmpt').on('change', function () {
            var cmpt = this.get('value').split(",");
            groupId = cmpt[0];
            artifactId = cmpt[1];
            messageWrapper.one("#groupId" ).set("value", groupId);
            messageWrapper.one("#artifactId" ).set("value", artifactId);
            searchForm.one('#wiki').set("value", '');
            getVersion(groupId, artifactId);
        });

        messageWrapper.one('#option_type').on('change', function () {
            var option_type = this.get('value');
            if(option_type != 'version') {
                $.confirmDialog("功能开发中 (ง •̀_•́)ง ", "提示", 'tips');
                //同时改变subject类型
            }
        });


        $.extend({confirmDialog: function (message, title, option_type) {
            $("<div id='confirm_dialog'></div>").dialog( {
                buttons: { "确认": function () {
                    if(option_type === 'version' && componentSelected.length > 0) {
                        afterSendClick(0);
                    }
                    $(this).dialog("close");
                    }
                },
                dialogClass: "no-close",
                resizable: false,
                title: title,
                modal: true
            }).html(message);
        }
        });

        $('#cmptSendBtn').click(function () {
            var option_type = $("#option_type").val();
            var content, title;
            switch(option_type) {
                case 'version':
                    content = "是否向使用低版本组件的服务负责人发送提醒? (无法撤回)";
                    title = "发送提醒确认";
                    break;
                case 'blacklist':
                    content = "是否向使用黑名单组件的服务的负责人发送提醒? (无法撤回)";
                    title = "发送提醒确认";
                    break;
                case 'conflict':
                    content = "是否向会发生组件冲突的服务的负责人发送提醒? (无法撤回)";
                    title = "发送提醒确认";
                    break;
                default:
                    return;
            }
            $.confirmDialog(content, title, option_type);
        });

        $('#cmptTestBtn').click(function () {
            var option_type = $("#option_type").val();
            switch(option_type) {
                case 'version':
                    afterSendClick(1);
                    break;
                case 'blacklist':
                    break;
                case 'conflict':
                    break;
                default:
                    return;
            }
        });
    }

    function getVersion(groupId, artifactId) {
        messageWrapper.one('#version').empty();
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
                        versionCache = ret.data;
                        if(!inited){
                            $("#version").autocomplete({
                                source: versionCache,
                                minLength: 0
                            });
                            $("#recommend_version").autocomplete({
                                source: versionCache,
                                minLength: 0
                            });
                            inited = true;
                        }
                        $("#version").autocomplete("option", "source", versionCache);
                        $("#recommend_version").autocomplete("option", "source", versionCache);
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

    function afterSaveClick() {
        var cmpt = searchForm.one("#cmpt option:checked").get("text");
        var groupId = searchForm.one('#groupId').get('value');
        var artifactId = searchForm.one('#artifactId').get('value');
        var version = searchForm.one('#version').get('value');
        var recommend_version = searchForm.one('#recommend_version').get('value');

        var wiki = searchForm.one('#wiki').get('value');
        var option_type = searchForm.one('#option_type').get('value');
        var newItem = {
            component: {
                groupId: groupId,
                artifactId: artifactId,
                version: version
            },
            recommend_version: recommend_version,
            wiki: wiki
        };
        switch(option_type) {
            case 'version':
                if(wiki === ''|| !isURL(wiki)) {
                    $.confirmDialog('请输入正确的组件说明文档WIKI链接', '提示', 'tips');
                }else if(judgeExist(newItem)) {
                    $.confirmDialog('组件已存在', '提示', 'tips');
                }else{
                    componentSelected.push(newItem);
                    searchForm.one('#wiki').set("value", '');
                    uiSetAdd(newItem);
                }
                break;
            case 'blacklist':
                break;
            case 'conflict':
                break;
            default:
        }
    }

    function isURL(str){
        return !!str.match(/(((^https?:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)$/g);
    }
    
    function judgeExist(curItem) {
        var component = curItem.component;
        var exist = false;
        if(componentSelected) {
            componentSelected.forEach(function (item) {
                if (item.component.groupId === component.groupId && item.component.artifactId === component.artifactId) {
                    exist = true;
                    return exist;
                }
            });
        }
        return exist;
    }
    function uiSetAdd (item) {
        var tableRow =  [
            '<tr class="J-config-panel-item">' +
            '<td style=" width=20%;">' + item.component.groupId +
            '</td>' +
            '<td style=" width=15%;">'+item.component.artifactId +
            '</td>' +
            '<td style=" width=20%;">'+item.component.version +
            '</td>' +
            '<td style=" width=20%;">'+item.recommend_version +
            '</td>' +
            '<td style=" width=15%;">'+ '<a href="'+ item.wiki + '" target="_blank">WIKI说明文档</a>' +
            '</td>' +
            '<td style=" width=10%;">' +
            '<a href="javascript:void(0);" class="message-version-delete">' +
            '<i class="fa fa-trash-o"></i>' +
            '<span>删除</span>' +
            '</a>' +
            '</td>' +
            '</tr>' 
        ].join('\n');
        var itemNode = Y.Node.create(tableRow);
        var tbody = messageListWrapper.one('table tbody');
        tbody.append(itemNode);
    }
    function afterDeteleClick(e) {
        var itemNode = e.target.ancestor('tr');
        var index = getItemIndex(itemNode);
        componentSelected.splice(index,1);
        itemNode.remove();
    }
    function getItemIndex(itemNode) {
        var itemNodeList = messageListWrapper.all('.message-version-delete');
        return itemNodeList.indexOf(itemNode);
    }
    function afterSendClick(isTesting) {
        var subjectTemp = searchForm.one('#subject').get('value');
        var subject = subjectTemp === '' ? '组件依赖提醒' : subjectTemp;
        var option_type = searchForm.one('#option_type').get('value');
        //var message_type = searchForm.one('#message_type').get('value');
        var message_type = ['xm'];
        var dependencies = [];
        var recommend_dependencies = [];
        var wikis = [];
        Y.Array.each(componentSelected,function(element) {
            dependencies.push({
                groupId: element.component.groupId,
                artifactId: element.component.artifactId,
                version: element.component.version
            });
            recommend_dependencies.push({
                groupId: element.component.groupId,
                artifactId: element.component.artifactId,
                version: element.recommend_version
            });
            wikis.push(element.wiki);
        });
        var params = {
            isTesting: isTesting,
            subject: subject,
            option_type: option_type,
            message_type: message_type,
            dependencies: dependencies,
            recommend_dependencies : recommend_dependencies,
            wikis: wikis
        };
        var url = '/component/message';
        Y.io(url, {
            method: 'POST',
            dataType: 'json',
            headers : {'Content-Type':"application/json;charset=UTF-8"},
            data: Y.JSON.stringify(params),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        if(ret.data == 'offline is not supported') {
                            Y.msgp.utils.msgpHeaderTip('error', '不支持线下环境', 3);
                        }else{
                            Y.msgp.utils.msgpHeaderTip('success', '发送成功', 3);
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '发送失败' || ret.msg, 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '发送失败', 3);
                }
            }
        });
    }

}, '0.0.1', {
    requires: [
        "w-tree",
        'w-base',
        'mt-base',
        'mt-io',
        'mt-date',
        'w-date',
        'w-paginator',
        'msgp-utils/common',
        'msgp-utils/msgpHeaderTip'
    ]
});
