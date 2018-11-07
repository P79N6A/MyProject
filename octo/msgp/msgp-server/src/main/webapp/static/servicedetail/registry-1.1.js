/**
 * 服务基本信息js
 */
/* jshint indent : false */
M.add('msgp-servicedetail/registry-1.1', function (Y) {
    var ndForm = Y.one('#registryForm');
    var sname,
        appkeyCheck,
        noteCheck,
        owner,
        _hasOwt;
    var ownerTextIdMap = new Map();
    var observerTextIdMap = new Map();
    var ownersIdList = [];
    var observersIdList = [];
    var len = 0;
    var illKeyword = new Array();

    var isEdit = false;
    var WAIT_PLACEHOLDER = '正在加载数据，请稍候...',
        OK_PLACEHOLDER = '可多选，请用控件选择';
    var owner, observer;

    var registry = {
        buildPage: function (edit, key, hasOwt, ownerStr, observerStr) {
            owner = ownerStr;
            observer = observerStr;
            illKeyword.length = 0;
            isEdit = !!edit;
            if (!edit && Y.one('#submit-btn')) {
                Y.one('#submit-btn').set('value', '注册');
            }
            _hasOwt = hasOwt;
            //上海小伙伴可以自己填写owt,pdl
            if (!hasOwt) {
                Y.one('#pdlVal').show()
                Y.one('#owtVal').show()
                Y.one('#owt_name').hide()
                Y.one('#pdl_name').hide()
            }
            this.initCheck();
            if (Y.one("#submit-btn")) {
                this.initAction();
            }

            document.title = "服务注册";
            showContent('data');
            initIllKeyword();
            Y.one('#registry_again').on('click', registryAgain);
            if (Y.one("#owtpdlowner")) {
                Y.one("#owtpdlowner").on('click', function () {
                    var owt = Y.one('#owt_name option:checked').get('value')
                    this.set('href', '/pdl?owt=' + owt);
                });
            }
        },
        initCheck: function () {
            initFormCheck();
            initOwnerAndObserver();
        },
        initAction: function () {
            Y.one("#submit-btn").on('click', doReg);
        },
    };

    function initOwnerAndObserver() {
        $("#owner").select2({
            tags: true
        });
        $("#observer").select2({
            tags: true
        });
        var owners = owner.split(",");
        var observers = observer.split(",");
        var ownerIdsTemp = isEdit ? $("#ownerId").val() : "";
        var ownerIds = ownerIdsTemp.split(",");
        var observersIdTemp = isEdit ? $("#observerId").val() : "";
        var observerIds = observersIdTemp.split(",");
        var ownerHtml = "";
        var observerHtml = "";
        Y.Array.each(owners, function (item, index) {
            ownerTextIdMap.set(item, ownerIds[index]);
            ownerHtml += '<option value="' + item + '" selected="selected">' + item + '</option>';
        });
        if (observers != "") {
            Y.Array.each(observers, function (item, index) {
                observerTextIdMap.set(item, observerIds[index]);
                observerHtml += '<option value="' + item + '" selected="selected">' + item + '</option>'
            });
        }
        $("#owner").html(ownerHtml);
        $("#observer").html(observerHtml);
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

        $("#observer").select2({
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
            templateSelection: formatRepoSelectionObserver, // 函数用于呈现当前的选择
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

    function formatRepoSelectionObserver(repo) {
        var observersLen = $("#observer").val().length;
        len++;
        if (observerTextIdMap.has(repo.text)) {
            observersIdList.push(observerTextIdMap.get(repo.text));
        } else {
            observersIdList.push(repo.idNum);
        }
        if (len == observersLen) {
            $("#observerId").val(observersIdList.join(","));
            observersIdList = [];
            len = 0;
        }
        return '<option style="float: right" selected="selected" value=' + repo.text + '>' + repo.text + '</option>';
    }

    function registryAgain() {
        showContent('data');
    }

    function showContent(flag, data) {
        Y.one('#registryForm').hide();
        Y.one('#registring').hide();
        Y.one('#registry_result').hide();
        Y.one('#registry_error').hide();
        switch (flag) {
            case "loading":
                var registring = Y.one('#registring');
                registring.one('span').set('text', data);
                registring.show();
                break;
            case "error":
                var error = Y.one('#registry_error');
                error.show();
                error.one('span').set('text', data);
                break;
            case "data":
                Y.one('#registryForm').show();
                break;
            case "success":
                var result = Y.one('#registry_result');
                result.one("h3").set('text', data);
                result.show();
                break;

        }

    }

    function initIllKeyword() {
        reflashIllWord();
        //setInterval(reflashIllWord, 30 * 1000);
        function reflashIllWord() {
            var url = "/service/registry/illkeyword";
            Y.io(url, {
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            var data = ret.data;
                            illKeyword.length = 0;
                            data.forEach(function (item) {
                                illKeyword.push(item);
                            });
                        }
                    }
                }
            });
        }
    }

    function setOldOwner(ac, user) {
        var owner = {};
        owner.owner = Y.one('#' + user).get('value');
        owner.ownerId = Y.one('#' + user + 'Id').get('value');
        // set old owners before edit
        var ownerArr = owner.owner.split(',');
        var idArr = owner.ownerId.split(',');
        var len = ownerArr.length;
        for (var i = 0; i < len; i++) {
            ac.checkedItemIds['checked-' + idArr[i]] = {
                id: idArr[i],
                name: ownerArr[i],
                checked: true
            };
        }
    }

    function checkAppkeyOnChanged(pre, suf) {
        var exp_w_d_dot = /^\b[A-Za-z][A-Za-z0-9.]*$/
        if (null == suf.match(exp_w_d_dot)) {
            _setMsg(appkeyCheck, "以字母开头，并且只能包含字母、数字和点号", false);
            return;
        }
        var exp_com = /(^([cC][oO][mM]\.))|([Mm][Ee][Ii][Tt][Uu][Aa][Nn]|[Ss][Aa][Nn][Kk][Uu][Aa][Ii])/
        if (null != suf.match(exp_com)) {
            _setMsg(appkeyCheck, "已经前缀过了com.sankuai/meituan", false);
            return;
        }
        var lowSuf = suf.toLowerCase();
        illKeyword.forEach(function (item) {
            if (("" != item) && (lowSuf.indexOf(item) >= 0)) {
                _setMsg(appkeyCheck, "不能包含" + item, false);
                return;
            }
        });

    }

    function _setMsg(element, msg, type) {
        element._setStatus(type);
        element.opt.warnElement.setHTML(msg).setStyle('color', '#f00');
    }

    function initFormCheck() {
        if (!isEdit && Y.one('#appkey')) {
            appkeyCheck = Y.msgp.utils.check.init(Y.one('#appkey'), {
                type: 'string',
                warnElement: Y.one('#appkey_tips'),
                callback: function () {
                    checkAppkeyOnChanged(Y.one('#sankuai_meituan').get('value'), Y.Lang.trim(Y.one('#appkey').get('value')));
                },
                blurback: function () {
                    checkAppkey(Y.one('#sankuai_meituan').get('value') + Y.Lang.trim(Y.one('#appkey').get('value')));
                }
            });
        }
        if (Y.one('#intro')) {
            sname = Y.msgp.utils.check.init(Y.one('#name'), {
                type: 'string',
                emptyOk: true,
                chineseOk: true,
                warnElement: Y.one('#name').next()
            });
            noteCheck = Y.msgp.utils.check.init(Y.one('#intro'), {
                type: 'string',
                spaceOk: true,
                chineseOk: true,
                callback: function () {
                    var value = Y.Lang.trim(ndForm.one('#intro').get('value'));
                    if ((null == value) | ("" == value)) {
                        _setMsg(noteCheck, "不能为空", false);
                    }
                },
                warnElement: Y.one('#intro_tips')
            });
        }

        // use event delegation here because the autocompleteList plugin will detach all event bind on the input node, holly shit
        if (Y.one('#owner_wrap')) {
            if (_hasOwt) {
                Y.one("#owt_name").on('change', function () {
                    url = '/pdl/list';
                    Y.io(url, {
                        method: 'get',
                        data: "owt=" + this.get('value'),
                        on: {
                            success: function (id, o) {
                                var ret = Y.JSON.parse(o.responseText);
                                var micro = new Y.Template();
                                var html = micro.render(optionTemplatePdl, {data: ret.data});
                                Y.one('#pdl_name').setHTML(html);
                            },
                            failure: function () {
                                Y.msgp.utils.msgpHeaderTip('error', '获取业务线失败', 3);
                            }
                        }
                    });
                })
            }
        }

    }

    function checkAppkey(value) {
        var keyArray = value.split('.');
        keyArray.forEach(function (item) {
            if (item.length <= 0) {
                _setMsg(appkeyCheck, "两个点号之间不能为空，且不能以点号结尾", false);
                return;
            }
        });
        var url = '/service/' + value + '/exist';
        Y.mt.io.get(url, {}, function (res) {
            if (!res.isSuccess || res.data) {
                Y.one('#appkey').setData('status', false);
                Y.one('#appkey_tips').setHTML('appkey已存在！').setStyle('color', '#f00');
            }
        });
    }

    function doReg() {

        var checkResult = doCheck();
        if (!checkResult) return;
        var data = patchData();
        var currentAppkey, url;
        if (isEdit) {
            currentAppkey = Y.one("#apps_select").get('value');
            url = '/service/' + currentAppkey + '/desc';
        } else {
            url = '/service/registry';
        }

        showContent('loading', isEdit ? "正在保存中..." : "正在注册中...");
        Y.io(url, {
            method: isEdit ? 'put' : 'post',
            headers: {'Content-Type': "application/json;charset=UTF-8"},
            data: Y.JSON.stringify(data),
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        if (isEdit) {
                            Y.msgp.utils.msgpHeaderTip('success', '保存成功', 3);
                            showContent('data');
                            setTimeout(redirectOutLine, 1000);
                        } else {
                            showContent('success', "注册成功");
                            setTimeout(redirectService, 1000);
                        }
                    } else if (ret.msg) {
                        showContent('error', ret.msg);
                        //Y.msgp.utils.msgpHeaderTip('error', ret.msg, 3);
                    } else {
                        showContent('error', '操作失败，请稍后重试！');
                        //Y.msgp.utils.msgpHeaderTip('error', '操作失败，请稍后重试！', 3);
                    }
                },
                failure: function () {
                    showContent('error', '操作失败，请稍后重试！');
                    //if (isEdit) {
                    //    Y.msgp.utils.msgpHeaderTip('error', '修改失败', 3);
                    //}
                }
            }
        });
    }

    function redirectService() {
        location.href = '/service';
    }

    function redirectOutLine() {
        var currentAppkey = isEdit ? Y.one("#apps_select").get('value') : Y.one('#sankuai_meituan').get('value') + Y.Lang.trim(ndForm.one('#appkey').get('value'));
        location.href = '/service/detail?appkey=' + currentAppkey + '#outline';
    }

    function patchData() {
        var currentAppkey = isEdit ? Y.one("#apps_select").get('value') : Y.one('#sankuai_meituan').get('value') + Y.Lang.trim(ndForm.one('#appkey').get('value'));
        var owt = ndForm.one('#owtVal').get('value')
        var pdl = ndForm.one('#pdlVal').get('value')
        if (_hasOwt) {
            owt = Y.Lang.trim(ndForm.one('#owt_name option:checked').get('value'));
            var pdlchecck = ndForm.one('#pdl_name option:checked');
            if (pdlchecck) {
                pdl = Y.Lang.trim(pdlchecck.get('value'));
            }
        }
        var obj = {
            name: Y.Lang.trim(ndForm.one('#name').get('value')),
            appkey: currentAppkey,
            category: 'thrift',
            intro: Y.Lang.trim(ndForm.one('#intro').get('value')),
            tags: Y.Lang.trim(ndForm.one('#tags').get('value')),
            owt: owt,
            pdl: pdl,
            base: Y.Lang.trim(ndForm.one('#base').get('value')),//归属地
            regLimit: Y.Lang.trim(ndForm.one('#regLimit').get('value')),//regLimit
            createTime: +Y.Lang.trim(ndForm.one("#createTime").get('value'))
        };

        obj.owners = getUser($('#owner'), $('#ownerId'));
        obj.observers = getUser($('#observer'), $('#observerId'));
        return obj;
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

    //检查必填项为空时的情况
    function doCheck() {
        var stat = true;
        if (isEdit) {
            if (!sname.isValid()) {
                sname.showMsg();
                stat = false;
            }
        } else {
            if (!appkeyCheck.isValid()) {
                appkeyCheck.showMsg();
                stat = false;
            }
        }

        if (!noteCheck.isValid()) {
            noteCheck.showMsg();
            stat = false;
        }

        if (Y.one('#owner').get('value') === '') {
            Y.one('#owner').next('.tips').setHTML('不能为空').setStyle('color', '#f00');
            textareaBlink(Y.one('#owner'));
            stat = false;
        } else {
            if (Y.one('#ownerId').get('value') === '') {
                Y.one('#owner').next('.tips').setHTML('不存在').setStyle('color', '#f00');
                textareaBlink(Y.one('#owner'));
                stat = false;
            }
        }
        if(($('#owner').val().length < 2 || $('#owner').val().length > 12) && isOffline == "false"){
            Y.one('#owner').next('.tips').setHTML('负责人数量在2～12人之间，请确认').setStyle('color', '#f00');
            textareaBlink(Y.one('#owner'));
            stat = false;
        }

        if($('#owner').val().length < 2 && isOffline == "true" ){
            Y.one('#owner').next('.tips').setHTML('负责人数量必须大于2人，请确认').setStyle('color', '#f00');
            textareaBlink(Y.one('#owner'));
            stat = false;
        }
        return stat;
    }

    var loading;

    function textareaBlink(obj) {
        obj.setStyle('background-color', '#fc9');
        var sti = setInterval(function () {
            obj.setStyle('background-color', '#fc9');
        }, 200);
        var _sti = setTimeout(function () {
            obj.setStyle('background-color', '#fff');
        }, 210);
        var st = setTimeout(function () {
            clearTimeout(st);
            clearInterval(sti);
            clearTimeout(_sti);
            obj.setStyle('background-color', '#fc9');
        }, 620);
    }

    var optionTemplatePdl = [
        '<% Y.Array.each(this.data, function(item,index){ %>',
        '<option value="<%= item.pdl %>"><%= item.pdl %></option>',
        '<% }); %>',
    ].join('');

    Y.namespace("msgp.servicedetail").registry = registry;
}, '1.0.0', {
    requires: [
        "mt-base",
        "mt-form",
        "mt-io",
        "node-event-simulate",
        "w-autocomplete",
        'template',
        "msgp-utils/check",
        "msgp-utils/msgpHeaderTip"
    ]
});
M.use("msgp-servicedetail/registry-1.1", function (Y) {
    Y.msgp.servicedetail.registry.buildPage(isEdit, appkey, hasOwt, ownerStr, observerStr)
});
