M.add('msgp-manage/banner', function (Y) {
    Y.namespace('msgp.manage').banner = banner;

    var bannerWrapper, 
        messageListWrapper,
        searchForm,
        tbody;
    var existedItem = [];

    function banner() {
        bannerWrapper = Y.one('#bannerWrapper');
        messageListWrapper = bannerWrapper.one("#message_list_wrapper");
        searchForm = bannerWrapper.one('#messageSearchForm');
        tbody = $('#message_list_body');
        bindWidget();
        getAllMessage();
    }

    function bindWidget() {
        searchForm.delegate('click', afterSaveClick, '#bannerAddBtn');
        messageListWrapper.delegate('click', afterDeteleClick, '.message-delete');
    }

    function afterSaveClick() {
        if(message_body === '') {
            alert('消息不能为空');
        }else{
            saveMessage();
        }
    }

    function uiSetAdd (item) {
        var tableRow =  [
            '<tr class="J-config-panel-item">' +
            '<td style=" width=15%;">' + getMessageTypeDesc(item.message_type) +
            '</td>' +
            '<td style=" width=20%;">'+ item.message_title +
            '</td>' +
            '<td style=" width=50%;">'+ item.message_body +
            '</td>' +
            '<td style=" width=15%;">' +
            '<a href="javascript:void(0);" class="message-delete">' +
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
        var trNode = e.target.ancestor('tr');
        var trId = trNode.get('id');
        var tdNodes = $('#' + trId).children('td');
        var message_type = getMessageTypeCode(tdNodes.eq(0).text());
        var message_title = tdNodes.eq(1).text();
        var message_body = tdNodes.eq(2).text();
        var item = {
            message_type: message_type,
            message_title: message_title,
            message_body: message_body
        };
        existedItem.remove(item);
        deleteMessage(item);
    }

    Array.prototype.indexOf = function(val) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == val) return i;
        }
        return -1;
    };
    Array.prototype.remove = function(val) {
        var index = this.indexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };

    
    function  getAllMessage() {
        $.ajax({
            type: "GET",
            url: "/common/banner/valid_message",
            success: function(result) {
                var messages = result.data;
                if(messages.length == 0) {
                    $('#message_list_wrapper')[0].style.display = 'none';
                }else {
                    tbody.html("");
                    existedItem = [];
                    $('#message_list_wrapper')[0].style.display = 'block';
                    messages.forEach(function (item) {
                        var newItem = {
                            message_type: item.messageType,
                            message_title: item.messageTitle,
                            message_body: item.messageBody
                        };
                        existedItem.push(item);
                        uiSetAdd(newItem);
                    })
                }
            }
        });
    }
    
    function saveMessage() {
        var message_type = searchForm.one("#message_type option:checked").get("value");
        var message_body = searchForm.one('#message_body').get('value');
        var message_title = '';
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: "/common/banner/insert_message",
            data: {type:message_type, title: message_title, content: message_body},
            dataType: 'json',
            success: function(result) {
                if(result.isSuccess) {
                    getAllMessage();
                }else{
                    alert('保存失败');
                }
            }
        });
    }

    function deleteMessage(item) {
        $.ajax({
            type: "GET",
            contentType: "application/json",
            url: "/common/banner/delete_message",
            data: {type: item.message_type, title: item.message_title, content: item.message_body},
            dataType: 'json',
            success: function(result) {
                if(result.isSuccess) {
                    getAllMessage();
                    alert('删除成功');
                }else{
                    alert('删除失败');
                }
            }
        });
    }

    function getMessageTypeCode(message_type_desc) {
        var code = 1;
        switch (message_type_desc) {
            case "通知":
                code = 1;
                break;
            case "警告":
                code = 2;
                break;
        }
        return code;
    }

    function getMessageTypeDesc(message_type_code) {
        var desc = "通知";
        switch (message_type_code) {
            case 1:
                desc = "通知";
                break;
            case 2:
                desc = "警告";
                break;
        }
        return desc;
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'template',
        'msgp-utils/msgpHeaderTip'
    ]
});