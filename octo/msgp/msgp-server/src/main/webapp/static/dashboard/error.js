M.add('msgp-dashboard/error', function (Y) {
        Y.namespace('msgp.dashboard').error = error;
        var startInput = Y.one('#start_time'),
            endInput = Y.one('#end_time');
        var sort = "falcon_count desc";
        var default_sort={id:"falcon_count",sort:"fa-sort-desc"}
        function error() {
            document.title = '服务治理平台-报错大盘';
            initTimeLine();
            initDatePicker();
            //获取数据,并填充
            getData();
            bindQuery()
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
        function bindQuery() {
            Y.one("#triggers_wrap").delegate('click', function () {
                getData();
            }, '#query_btn');

            var sortDatas = [Y.one("#falcon_count"),Y.one("#octo_count")];
            Y.Array.each(sortDatas, function(item,index){
                item.on('click',function(){
                    Y.one("#"+default_sort.id+"_i").removeClass(default_sort.sort);
                    Y.one("#"+default_sort.id+"_i").addClass("fa-sort");
                    var id= this.get("id")
                    if(id==default_sort.id ){
                        if(default_sort.sort=="fa-sort-asc"){
                            default_sort.sort="fa-sort-desc"
                        }else{
                            default_sort.sort = "fa-sort-asc";
                        }
                        Y.one("#"+default_sort.id+"_i").addClass(default_sort.sort);
                    }else{
                        default_sort.sort = "desc";
                        default_sort.id = id;
                        Y.one("#"+default_sort.id+"_i").removeClass("fa-sort");
                        Y.one("#"+default_sort.id+"_i").addClass("fa-sort-"+default_sort.sort);
                    }
                    getData()
                })
            })
        }

        function getData() {
            var start = startInput.get("value");
            var end = endInput.get("value");
            sort = default_sort.id  + " " + default_sort.sort.replace("fa-sort-","");
            var url = '/error/alarm?start='+start+'&end='+ end+'&sort='+sort+'&size='+25;
            Y.io(url, {
                method: 'get',
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
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
            '<%  node = encodeURIComponent(item.node) %>',
            '<tr <% if(item.status==0){%> style="color: red" <%}%> >',
            '<td><%= item.owt %></td>',
            '<td title="<%=item.appkey%>"><a style="cursor:pointer" target="_blank" href="/data/tabNav?appkey=<%= item.appkey %>#stream" ><%= item.appkey %></a></td>',
            '<td>',
            '<% if(item.falconCount > 0){ %>',
            '<a style="cursor:pointer; color: red;" target="_blank" href="http://p.falcon.sankuai.com/api/alarm/dashboard/?node=<%= node %>"><%= item.falconCount %></a></td>',
            '<% }else{ %>',
            '<a style="cursor:pointer;" target="_blank" href="http://p.falcon.sankuai.com/api/alarm/dashboard/?node=<%= node %>"><%= item.falconCount %></a></td>',
            '<% }%>',
            '<td>',
            '<% if(item.octoCount > 0){ %>',
            '<a style="cursor:pointer;color: red;" target="_blank" href="monitor/log?appkey=<%= item.appkey %>"><%= item.octoCount %></a></td>',
            '<% }else{ %>',
            '<a style="cursor:pointer;" target="_blank" href="monitor/log?appkey=<%= item.appkey %>"><%= item.octoCount %></a></td>',
            '<% }%>',
            '</tr>',
            '<% }); %>'
        ].join('');


        function fillFalconAlarm(data) {
            var micro = new Y.Template();
            var dom = Y.one('#falcon_alarm').one("tbody");
            if(data.length>0){
                var html = micro.render(alarmTemplate, {data: data});
                dom.setHTML(html);
            }else{
                setEmpty(5,dom)
            }

        }

        function emptyOrError(isError, colspan, dom) {
            var html = '<tr><td colspan="' + colspan + '">' + (isError ? '获取失败' : '没有内容');
            dom.setHTML(html);
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
                window.location.href="/error?start="+start_time+"&end="+end_time
            })
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
M.use('msgp-dashboard/error', function (Y) {
    Y.msgp.dashboard.error();
});
