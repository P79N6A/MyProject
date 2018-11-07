M.add('msgp-manage/daily', function (Y) {
    Y.namespace('msgp.manage').daily = daily;

    var dayInput = Y.one('#day');

    var template =
        '<table id="daily_table" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th>服务</th> ' +
            '<th>调用量(次数)</th> ' +
            '<th>QPS(次/秒)</th> ' +
            '<th>50%耗时(毫秒)</th> ' +
            '<th>90%耗时(毫秒)</th> ' +
            '<th>95%耗时(毫秒)</th> ' +
            '<th>99%耗时(毫秒)</th> ' +
            '<th>最大耗时(毫秒)</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody id="app_list"> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<td style="word-break: break-all; overflow: hidden;width: 30%"><a href="/data/performance?appkey=<%= item.appkey %>"><%= item.appkey %></a></td>' +
            '<td style="width: 10%"><%= item.count %></td>' +
            '<td style="width: 10%"><%= item.qps %></td>' +
            '<td style="width: 10%"><%= item.upper50 %></td>' +
            '<td style="width: 10%"><%= item.upper90 %></td>' +
            '<td style="width: 10%"><%= item.upper95 %></td>' +
            '<td style="width: 10%"><%= item.upper99 %></td>' +
            '<td style="width: 10%"><%= item.upper %></td>' +
            '</tr>' +
            '<% }); %>' +
            '</tbody> ' +
            '</table>';

    function daily() {
        initDatePicker()
        refreshData()
    }

    function refreshData() {
        getDaily()
    }

    function initDatePicker() {
        var now = new Date();
        var widget = new Y.mt.widget.Datepicker({
            node: dayInput,
            showSetTime: false
        });
        dayInput.set('value', Y.mt.date.formatDateByString(now, 'yyyy-MM-dd'));

        Y.one('#query_btn').on('click', function(){
            refreshData();
        });
    }

    function initList(data) {
        var micro = new Y.Template();
        var str = micro.render(template, {data: data});
        Y.one("#daily_list").setHTML(str);
        Y.mt.widget.init();
    }

    function getDaily() {
        var url = "/data/api/daily?day=" + dayInput.get("value")
        Y.io(url, {
            method: 'get',
            data: '',
            on: {
                success: function (id, o) {
                    searching = false;
                    var ret = Y.JSON.parse(o.responseText);
                    console.log(ret)
                    if (ret.isSuccess) {
                        var arr = ret.data;
                        if (arr.length !== 0) {
                            initList(arr)
                        } else {
                            showEmptyErrorMsg();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', ret.msg || '获取数据失败', 3);
                        showEmptyErrorMsg(!0);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(!0);
                }
            }
        });
    }

    function showEmptyErrorMsg(isError) {
        var html = '<tr><td colspan="12">' + (isError ? '获取失败' : '没有内容') + '</td></tr>'

        Y.one("#daily_list").empty();
        var micro = new Y.Template();
        var str = micro.render(template, {data: []});
        Y.one("#daily_list").setHTML(str);
        Y.one("#app_list").setHTML(html)
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