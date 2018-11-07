/**
 * Created by lhmily on 03/31/2016.
 */
M.add('msgp-manage/check_mcc', function (Y) {
    Y.namespace('msgp.manage').check_mcc = check_mcc;
    var showOverlay,
        showContent;
    var wrapper = Y.one('#wrap_mcc');

    var template =
        '<table id="daily_table" class="table table-striped table-hover " data-widget="sortTable"> ' +
        '<thead> ' +
        '<tr> ' +
        '<th>序号</th> ' +
        '<th>时间</th> ' +
        '<th>appkey</th> ' +
        '<th>业务线</th> ' +
        '</tr> ' +
        '</thead> ' +
        '<tbody id="app_list"> ' +
        '<% Y.Array.each(this.data, function( item, index ){ %>' +
        '<tr>' +
        '<td style="width: 3%"><%= index+1 %></td>' +
        '<td style="width: 5%"><%= Y.mt.date.formatDateByString(new Date(Number(item.time)), "yyyy-MM-dd") %></td>' +
        '<td style="width: 10%"><%= item.appkey %></td>' +
        '<td style="width: 10%"><%= item.businessLine %></td>' +
        '</tr>' +
        '<% }); %>' +
        '</tbody> ' +
        '</table>';

    function check_mcc(key, func1, func2) {
        showOverlay = func1;
        showContent = func2;
        bind();
        wrapper.one('#dynamic_or_file a')._node.click();
    }

    function bind() {
        wrapper.delegate('click', function () {
            var type = Number(this.getAttribute("value"));
            this.ancestor('div').all('a').removeClass('btn-primary');
            this.addClass("btn-primary");
            var URLType = (1 == type) ? "dynamic" : "file";
            var url = "/manage/mcc/statistic/" + URLType;
            getData(url);
        }, '#dynamic_or_file a');
    }


    function initList(data) {
        var micro = new Y.Template();
        var str = micro.render(template, {data: data});
        wrapper.one("#show_data").setHTML(str);
        Y.mt.widget.init();
    }

    function getData(url) {
        showOverlay(wrapper.one('#statistic'));
        Y.io(url, {
            method: 'get',
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var retData = ret.data;
                    initList(retData);
                    showContent(wrapper.one('#statistic'));
                },
                failure: function () {
                    showEmptyErrorMsg(!0);
                }
            }
        });
    }

    function showEmptyErrorMsg(isError) {
        var html = '<tr><td colspan="12">' + (isError ? '获取失败' : '没有内容') + '</td></tr>'

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
