/**
 * Created by zmz on 2017/7/31.
 */
M.add('msgp-data/data_tag', function (Y) {
    Y.namespace('msgp.data').data_tag = data_tag;
    var appkey;
    var tagWrapper;
    var listWrapper;
    var methodToFind;
    var pbody;

    function data_tag(_appkey, _isFirstIn) {
        appkey = _appkey;
        document.title = "标签治理";
        if (!_isFirstIn) {
            return;
        }

        initNodes();
        initAllSpan(appkey);
    }

    function initNodes(){
        tagWrapper = Y.one("#div_data_tag");
        listWrapper = tagWrapper.one(".tag_list");
        pbody = tagWrapper.one('#paginator_tagDegree');
    }

    function initAllSpan(appkey){
        var url = '/data/idc_host';
        var requestPara = {
            appkey : appkey,
        };
        Y.io(url, {
            method: 'get',
            data : requestPara,
            on: {
                success: function(id, o){
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var tagsData = ret.data;
                        setData(tagsData, appkey);
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '该appkey暂无该接口数据', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '该appkey暂无该接口数据', 3);
                }
            }
        });
    }

    function setData(data, appkey){
        saveMethodNames(data);
        getDegreeData(methodToFind, appkey, 1);
    }

    function saveMethodNames(data){
        var len = data.spannames.length;
        methodToFind = [];

        for(var i=0; i<len; i++){
            var saveData = data.spannames[i];
            if(saveData=='*' || saveData=='all'){
                continue;
            }
            methodToFind.push(saveData);
        }
    }

    function getDegreeData(data, appkey, index){
        var url = '/data/tags_get_data';
        var requestPara = data;
        // max显示最多页码，index表示当前页面，size设定一个页面展示数据，count标识数据总条数
        var size=20;
        var count=requestPara.length;
        var max=Math.ceil(count/size);
        var index=index;
        refreshPaginator(pbody, index, max, size, count);
        // 对请求数据做一个处理
        var sendData=data.slice(20*(index-1), count<20*(index)?count:20*(index));
        Y.io(url, {
            method: 'GET',
            data : {
                appkey: appkey,
                methods: sendData
            },
            on: {
                success: function(id, o){
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var tagsData = ret.data;
                        fillList(tagsData, appkey);
                        listWrapper.delegate('click', function () {
                            var el = this;
                            if (el.hasClass('btn-primary')) return;
                            var line = el.ancestor('tr');
                            line.all('.one-job .btn').removeClass('btn-primary');
                            this.addClass("btn-primary");
                            changeDegree(line, appkey);
                        }, '.one-job .btn');
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '该appkey暂无该接口数据', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '该appkey暂无该接口数据', 3);
                }
            }
        });
    }

    function changeDegree(line, appkey) {
        var data = line.getData('info');
        if (Y.Lang.isString(data)) {
            data = Y.JSON.parse(data);
        }
        setDegreeData(appkey, data.method, line.one('.btn-primary').getData('degree'));
    }

    function setDegreeData(appkey, method, degree){
        var url= '/data/tags_set_data';
        Y.io(url, {
            method: 'post',
            data: {
                appkey: appkey,
                method: method,
                degree: degree
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if(ret.isSuccess){
                        Y.msgp.utils.msgpHeaderTip('success', '增加成功', 3);
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '该appkey暂无该接口数据', 3);
                }
            }
        });
    }

    function fillList(data, appkey){
        var micro = new Y.Template();
        var str = micro.render(template(appkey), {data: data});
        Y.one(".tag_list").setHTML(str);
        Y.mt.widget.init();
    }

    function template(appkey) {
        return '<table id="kpi_table" class="table table-striped table-hover "> ' +
            '<thead> ' +
            '<tr> ' +
            '<th>接口</th> ' +
            '<th>重要度</th> ' +
            '<th>详细信息</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody id="span_list"> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<%if((item!="all") && (item!="*")){%>' +
            '<tr id="tags_<%=index%>" class="find_click" data-info="<%= Y.JSON.stringify(item) %>">' +
            '<td class="method" style="width: 40%"><%= item.method %></td>' +
            '<td style="width: 40%">'+
            '<div class="one-job btn-group">'+
            '<a data-degree="3" class="btn btn-mini <%= item.tag==3?"btn-primary":"" %>">核心</a>'+
            '<a data-degree="2" class="btn btn-mini <%= item.tag==2?"btn-primary":"" %>">重要</a>'+
            '<a data-degree="1" class="btn btn-mini <%= item.tag==1?"btn-primary":"" %>">不重要</a>'+
            '<a data-degree="0" class="btn btn-mini <%= item.tag==0?"btn-primary":"" %>">未标注</a>'+
            '</div>'+
            '</td>'+

            '<td style="width: 20%"><a href="https://mtrace.sankuai.com/treeview?appkey='+appkey+ '&methodName=' + '<%= item.method %>' + '">信息展示</a></td>'+

            '</tr>' +
            '<%} }); %>' +
            '</tbody> ' +
            '</table>';
    }

    function refreshPaginator(pbody, index, max, size, count) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index:  index || 1,
            max: max || 1,
            pageSize: size,
            totalCount: count,
            callback: changePage
        });
    }

    function changePage(params) {
        getDegreeData(methodToFind, appkey, params.page);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-date',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'w-autocomplete',
        'template'
    ]
});
