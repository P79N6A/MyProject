/**
 * Created by yves on 17/3/21.
 */
M.add('msgp-dashboard/personal', function (Y) {
    Y.namespace('msgp.dashboard').personal = personal;
    var personalWrapper;
    var favoriteAppkeysCache = [];
    var favoriteAppkeyTable;
    var hostUrl;
    
    function personal(_favoriteAppkeys, _hostUrl) {
        document.title = '服务治理平台-个人主页';
        favoriteAppkeysCache = _favoriteAppkeys;
        hostUrl = _hostUrl;
        
        initAppkeySelector();
        initEvent();
    }

    function initEvent() {
        personalWrapper = Y.one('#personal-wrapper');
        favoriteAppkeyTable = personalWrapper.one('#favorite-appkey-table');

        favoriteAppkeyTable.one("tbody").delegate('click', function () {
            personalWrapper.one('#favorite-appkey-add-selector-wrapper').setStyle('display', 'table-cell');
        }, '.favorite-appkey-add');

        personalWrapper.one('#favorite-appkey-save').on('click', function () {
            var selectedAppkeys = [];
            $('#favorite-appkey-add-selector option:selected').map(function (a, item) {
                selectedAppkeys.push(item.value);
            });
            addFavoriteAppkey(selectedAppkeys);
        });

        favoriteAppkeyTable.one("tbody").delegate('click', function () {
            var tr = this.ancestor('tr');
            var appkeyDelete = tr.getData('appkey');
            console.log(appkeyDelete);
            deleteFavoriteAppkey(appkeyDelete);
        }, '.delete-favorite-appkey');
    }

    function getFavoriteAppkey() {
        var url = '/personal/get';
        $.ajax({
            type:"GET",
            url: url,
            async: false,
            success:function(ret){
                if (ret.isSuccess) {
                    favoriteAppkeysCache = ret.data;
                    //设置选中
                    $('#favorite-appkey-add-selector').multiselect('select', favoriteAppkeysCache);

                    //禁用选中
                    var selectedOptions = $('#favorite-appkey-add-selector option').filter(function() {
                        return $(this).is(':selected');
                    });

                    selectedOptions.each(function() {
                        var input = $('input[value="' + $(this).val() + '"]');
                        input.prop('disabled', true);
                        input.parent('li').addClass('disabled');
                    });
                    fillFavoriteAppkeyTable();
                }
            }
        });
    }
    
    function fillFavoriteAppkeyTable() {
        var micro = new Y.Template();
        var favoriteAppkeyTbodyTemplate = Y.one('#text-favorite-appkey-table-tbody').get('value');
        var templateDate = [];
        favoriteAppkeysCache.forEach(function (item) {
            templateDate.push({
                appkey: item,
                hostUrl: hostUrl
            })
        });
        var html = micro.render(favoriteAppkeyTbodyTemplate, {data: templateDate});
        favoriteAppkeyTable.one("tbody").setHTML(html);
        personalWrapper.one('#favorite-appkey-add-selector-wrapper').setStyle('display', 'none');
    }

    function addFavoriteAppkey(selectedAppkeys) {
        var url = '/personal/add';
        Y.io(url, {
            method: 'GET',
            data:  {
                appkeys: selectedAppkeys
            },
            on: {
                success: function (id, o) {
                    favoriteAppkeysCache = selectedAppkeys;
                    getFavoriteAppkey();
                    Y.msgp.utils.msgpHeaderTip('success', '添加成功', 3);
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '添加失败', 3);
                }
            }
        });
    }

    function deleteFavoriteAppkey(appkey) {
        var url = '/personal/delete';
        Y.io(url, {
            method: 'GET',
            data:  {
                appkey: appkey
            },
            on: {
                success: function (id, o) {
                    //取消已经选中的
                    $('option', $('#favorite-appkey-add-selector')).each(function(element) {
                        $(this).removeAttr('selected').prop('selected', false);
                    });
                    $("#favorite-appkey-add-selector").multiselect('refresh');
                    //重新获取最新的
                    getFavoriteAppkey();
                    Y.msgp.utils.msgpHeaderTip('success', '删除成功', 3);
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '删除失败', 3);
                }
            }
        });
    }

    function initAppkeySelector() {
        var ownAppkeys = [];
        $.ajax({
            type:"GET",
            url: '/service/owner',
            async: false,
            success:function(ret){
                ret.data.forEach(function (item) {
                    ownAppkeys.push(item.appkey);
                });
            }
        });

        $('#favorite-appkey-add-selector').multiselect({
            selectAllText: "选择全部",
            allSelectedText: "已选择全部服务",
            nonSelectedText: "未选择服务",
            placeholder: "请选择服务",
            buttonWidth: '300px',
            maxHeight: 200,
            filterPlaceholder: '搜索服务',
            enableFiltering: true,
            includeSelectAllOption: true,
            selectAllNumber: true,
            buttonText: function (options, select) {
                var total = $('#favorite-appkey-add-selector option').length;
                if (options.length === 0) {
                    return '未选择服务';
                }
                else if (options.length < total && options.length > 1) {
                    return '已选择' + options.length + '个服务';
                } else if (options.length == total) {
                    return '已选择全部服务(' + total + '个)';
                } else {
                    var labels = [];
                    options.each(function () {
                        if ($(this).attr('label') !== undefined) {
                            labels.push($(this).attr('label'));
                        }
                        else {
                            labels.push($(this).html());
                        }
                    });
                    return labels.join(', ') + '';
                }
            }
        });
        var options = [];
        $.each(ownAppkeys, function(i, span) {
            options.push({
                label: span,
                value: span
            })
        });
        $("#favorite-appkey-add-selector").multiselect('dataprovider', options);

        //设置选中
        $('#favorite-appkey-add-selector').multiselect('select', favoriteAppkeysCache);

        //禁用选中
        var selectedOptions = $('#favorite-appkey-add-selector option').filter(function() {
            return $(this).is(':selected');
        });

        selectedOptions.each(function() {
            var input = $('input[value="' + $(this).val() + '"]');
            input.prop('disabled', true);
            input.parent('li').addClass('disabled');
        });
    }
    
},
'0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip'
    ]
});

