M.add('msgp-dashboard/tair', function (Y) {
        Y.namespace('msgp.dashboard').tair = tair;

        var detailDialog;
        var nodeSearch = "corp%3Dmeituan%26owt%3Dtair",
            _alarmDate = {};

        var startInput = Y.one('#start_time'),
            endInput = Y.one('#end_time');

        function tair() {
            document.title = '服务治理平台-缓存大盘';
            initTimeLine();
            initDatePicker();
            getData();
            Y.one("#triggers_wrap").delegate('click', function () {
                getData();
            }, '#query_btn');
        }

        function getData() {
            var start = startInput.get("value")
            var end = endInput.get("value")
            var start_date = new Date(Date.parse(start.replace(/-/g, "/")));
            var end_date = new Date(Date.parse(end.replace(/-/g, "/")));

            var duration = (end_date.getTime() -start_date.getTime()) / 1000;
            var url = '/database/alarm?nodeSearch=' + nodeSearch + "&duration=" + duration;
            console.log(url);
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.Array.each(ret.data, function (item, ind) {
                                _alarmDate[item.owt+'-'+item.srv] = item;
                            })
                            fillFalconAlarm(ret.data);
                        } else {
                            emptyOrError(true, 2, Y.one('#falcon_alarm').one("tbody"))
                        }
                    }
                }
            });
        }
        var alarmTemplate = [
            '<% Y.Array.each(this.data, function(item,index){ %>',
            '<tr <% if(item.status==0){%> style="color: red" <%}%> >',
            '<td><%= item.owt %></td>',
            '<td><%= item.srv %></td>',
            '<td><a style="cursor:pointer" data-owt-srv="<%= item.owt %>-<%= item.srv %>"><%= item.count %></a></td>',
            '</tr>',
            '<% }); %>',
        ].join('');

        var alarmDetailTemplate = [
            '<table class="table table-striped table-hover">' +
            '<colgroup><col width="10%"></col><col width="6%"></col><col width="10%"></col> <col width="5%"></col> <col width="15%"></col> </colgroup>' +
            '<thead><tr><th>机器节点</th><th>报警级别与次数</th><th>报警指标与报警值</th><th>报警时间</th><th>详情</th></tr></thead>',
            '<tbody><% Y.Array.each(this.data, function(item,index){ %>',
            '<tr>',
            '<td><%= item.Counter %></td>',
            '<td><%= item.Priority_and_step %></td>',
            '<td><%= item.Func_and_value %></td>',
            '<td><%= Y.mt.date.formatDateByString( new Date(item.Timestamp*1000), "yyyy-MM-dd hh:mm:ss" ) %></td>',
            '<td><a  style="cursor:pointer" target="_blank" href="<%= item.Dashboard_link %>">详情</a></td>',
            '</tr>',
            '<% }); %>',
            '</tbody></table>',
        ].join('');

        function fillFalconAlarm(data) {
            var micro = new Y.Template();
            var dom = Y.one('#falcon_alarm').one("tbody");
            if(data.length>0){
                var html = micro.render(alarmTemplate, {data: data});
                dom.setHTML(html);
                bindDetailAlarm()
            }else{
                setEmpty(3,dom)
            }

        }


        function emptyOrError(isError, colspan, dom) {
            var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容');
            dom.setHTML(html);
        }


        function bindDetailAlarm(index) {
            Y.one('#falcon_alarm_' + index).delegate('click', function () {
                var owt = this.getData("owt-srv");
                var dataDetail = _alarmDate[owt];
                console.log(dataDetail)

                detailDialog = detailDialog ? detailDialog : new Y.mt.widget.CommonDialog({
                    id: 'alarm_detail_dialog',
                    title: '数据库报警详情',
                    width: 1024
                });

                var micro = new Y.Template();
                var str = micro.render(alarmDetailTemplate, {data: dataDetail.alarm_data});
                detailDialog.setContent(str);
                detailDialog.show();
            }, 'a');
        }

        function setEmpty(colspan,dom){
            var html = '<tr><td colspan="'+colspan +'" style="color: green;text-align:center">服务正常</td></tr>';
            dom.setHTML(html);
        }

        function initTimeLine(){
            Y.all("#timeline a").on('click',function(e){
                var time = e.target.getData('value');
                var now = new Date();
                var end = now.getTime() - 60*1000;
                var start = (end-time*60*1000);
                var end_time = Y.mt.date.formatDateByString(new Date(end), 'yyyy-MM-dd hh:mm:00');
                var start_time = Y.mt.date.formatDateByString(new Date(start), 'yyyy-MM-dd hh:mm:00');
                window.location.href="/tair?start="+start_time+"&end="+end_time
            })
        }
        function initDatePicker(){
            sdate = new Y.mt.widget.Datepicker({
                node : startInput,
                showSetTime : true
            });

            edate = new Y.mt.widget.Datepicker({
                node : endInput,
                showSetTime : true
            });
        }

    },
    '0.0.1', {
        requires: [
            'mt-base',
            'mt-io',
            'w-base',
            'w-date',
            'mt-date',
            'template',
            'transition',
            'mt-base'
        ]
    }
);
M.use('msgp-dashboard/tair', function (Y) {
    Y.msgp.dashboard.tair();
});
