/* jshint indent : false */
M.add('msgp-servicedetail/oncall', function (Y) {
    Y.namespace('msgp.servicedetail').oncall = oncall;

    var addOncallDialog;
    var ownerTextIdMap = new Map();
    var ownersIdList = [];
    var len = 0;
    var currentAppkey = Y.one("#apps_select").get('value');
    var oncallsStr = Y.one("#outline_oncalls");

    function oncall() {
        var addButton = Y.one('#addOncall');
        addButton.on('click', addNewOncaller);
        initAddOncall();
    }

    function addNewOncaller() {
        addOncallDialog = addOncallDialog ? addOncallDialog : initAddOncall();
        var micro = new Y.Template();
        var str = micro.render(addTemplate);
        addOncallDialog.setContent(str);
        addOncallDialog.show();
        initOwnerAndObserver();
    }

    function initAddOncall() {
        var dialog = new Y.mt.widget.CommonDialog({
            id: 'add_oncall_dialog',
            title: '增加值班人',
            width: 640,
            drag: function () {
            },
            refresh: 1,
            btn: {
                pass: doPutOncall
            }
        });
        return dialog;
    }

    function doPutOncall() {
        var oncaller = getUser($('#owner'), $('#ownerId'));
        var url = "/oncall/save";
        var data = {
            appkey: currentAppkey,
            data: oncaller
        }
        Y.io(url, {
            method: 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        Y.msgp.utils.msgpHeaderTip('success', '保存成功', 3);
                        redirectOncall();
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.data, 3);
                    }
                },
                failure: function () {
                    showContent('error', '操作失败，请稍后重试！');
                    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                }
            }
        });
    }

    function redirectOncall() {
        console.log(oncallsStr);
        location.reload();
        location.href = '/service/detail?appkey=' + currentAppkey + '#oncall';
    }

    function getUser(nameWrapper, nameIDWrapper) {
        var i,
            ownersArr = nameWrapper.val(),
            ownerIds = nameIDWrapper.val(),
            ownerIdsArr = ownerIds.split(','),
            tmpArr = [],
            reg = /^([^\(]+)\(([^\)]+)\)$/g,
            mArr;
        if (ownersArr == null) {
            return tmpArr;
        }
        var len = ownersArr.length;
        var j = 0;
        for (i = 0; i < len; i++) {
            reg.lastIndex = 0;
            mArr = reg.exec(ownersArr[i]);
            if (null == mArr || mArr.length < 2) {
                continue;
            }
            tmpArr[j] = {
                id: +ownerIdsArr[i],
                login: mArr[2],
                name: mArr[1]
            };
            j++;
        }
        return tmpArr;
    }

    function initOwnerAndObserver() {
        $("#owner").select2({
            tags: true
        });
        var owners = "";
        var ownerIdsTemp = "";
        var ownerIds = ownerIdsTemp.split(",");
        var ownerHtml = "";
        if (owners != "") {
            Y.Array.each(owners, function (item, index) {
                ownerTextIdMap.set(item, ownerIds[index]);
                ownerHtml += '<option value="' + item + '" selected="selected">' + item + '</option>';
            });
        }
        $("#owner").html(ownerHtml);
        $("#owner").select2({
            ajax: {
                url: '/userorg/user/searchEmployeeInfo',
                dataType: 'json',
                delay: 250,
                data: function (params) {
                    return {
                        q: params.term
                    };
                },
                processResults: function (data) {
                    return {
                        results: data
                    };
                },
                cache: true
            },
            escapeMarkup: function (markup) {
                return markup;
            },
            minimumInputLength: 1,
            templateResult: formatRepo, // 函数用来渲染结果
            templateSelection: formatRepoSelectionOwner, // 函数用于呈现当前的选择
        });
    }

    function formatRepo(repo) {
        if (repo.loading) return repo.text;
        return '<option>' + repo.text + '</option>';
    }

    function formatRepoSelectionOwner(repo) {
        var ownersLen = $("#owner").val().length;
        len++;
        if (ownerTextIdMap.has(repo.text)) {
            ownersIdList.push(ownerTextIdMap.get(repo.text));
        } else {
            ownersIdList.push(repo.idNum);
        }
        if (len == ownersLen) {
            $("#ownerId").val(ownersIdList.join(","));
            ownersIdList = [];
            len = 0;
        }
        return '<option style="float: right" selected="selected" value=' + repo.text + '>' + repo.text + '</option>';
    }

    var addTemplate = [
        '<div id="oncall_add_form" class="form-horizontal">',
        '<div class="control-group mb0"><label class="control-label">值班人：</label>',
        '<div class="controls">',
        '<select id="owner" multiple="multiple" data-name="clainUserNames" data-node="userInput" style="width: 80%; height: 30px;">',
        '</select>',
        '<input type="hidden" id="ownerId" data-name="claimUsers" value=""/>',
        '<br/><span class="tips" style="color: red;"><small>注意，这里是覆盖添加，请输入全量mis账号，7天后自动失效!</small></span>',
        '</div>',
        '</div>'
    ].join('');


}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/check',
        'msgp-utils/localEdit',
        'msgp-service/commonMap',
        'msgp-utils/common',
        'msgp-config/popup',
        "mt-form",
        "node-event-simulate",
        "w-autocomplete",
    ]
});
