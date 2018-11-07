M.add('msgp-component/cmpt_detail', function (Y) {

    var detailListWrapper;

    Y.namespace('msgp.servicedetail').cmpt_detail = cmpt_detail;

    var appkey;
    function cmpt_detail(_appkey) {
        appkey = _appkey;
        detailListWrapper = Y.one("#detail_list_wrapper");
        getTableData();
    }

    function getTableData() {
        showWaitMsg(detailListWrapper);
        var searchData = {
            "owt": "",
            "pdl": "",
            "appkey": appkey
        };
        Y.msgp.utils.urlAddParameters(searchData);
        var url = '/component/details/appkey';
        Y.io(url, {
            method: 'get',
            data: searchData,
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if(data.cmpts.length == 0){
                            setBlankInfo(data);
                        }else {
                            fillTable(data);
                        }
                    } else {
                        showEmptyErrorMsg(detailListWrapper, true);
                        Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                    }
                },
                failure: function () {
                    showEmptyErrorMsg(detailListWrapper, true);
                    Y.msgp.utils.msgpHeaderTip('error', '获取数据失败', 3);
                }
            }
        });
    }

    function setBlankInfo(data) {
        var blank =
            '<table id="detail_list" class="table table-striped table-hover "> ' +
            '<thead> ' +
            '<tr> ' +
            '<th style="width: 10%; text-indent: 5px;">Id</th> ' +
            '<th style="width: 15%; text-indent: 5px;">App</th> ' +
            '<th style="width: 25%; text-indent: 5px;">GroupId</th> ' +
            '<th style="width: 25%; text-indent: 5px;">ArtifactId</th> ' +
            '<th style="width: 25%; text-indent: 5px;">Version</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody> ' +
            '<tr>' +
            '<td style="text-indent: 5px;" colspan="5">没有组件依赖统计数据, 请点击<a href="https://123.sankuai.com/km/page/28354561" target="_Blank">组件依赖问题自查</a>快速解决。</td>' +
            '</tr>' +
            '</tbody> ' +
            '</table>';

        detailListWrapper.setHTML(blank);
        Y.mt.widget.init();
        Y.one("#cmpt_num").set("text", data.countTotal);
        Y.one("#cmpt_num_distinct").set("text", data.count);
    }

    function fillTable(data) {
        var template =
            '<table id="detail_list" class="table table-striped table-hover " data-widget="sortTable"> ' +
            '<thead> ' +
            '<tr> ' +
            '<th style="width: 10%; text-indent: 5px;">Id</th> ' +
            '<th style="width: 15%; text-indent: 5px;">App</th> ' +
            '<th style="width: 25%; text-indent: 5px;">GroupId</th> ' +
            '<th style="width: 25%; text-indent: 5px;">ArtifactId</th> ' +
            '<th style="width: 25%; text-indent: 5px;">Version</th> ' +
            '</tr> ' +
            '</thead> ' +
            '<tbody> ' +
            '<% Y.Array.each(this.data, function( item, index ){ %>' +
            '<tr>' +
            '<td style="width: 10%; text-indent: 5px;"><%=index + 1 %></td>' +
            '<td style="width: 15%; text-indent: 5px;"><%=item.appArtifactId %></td>' +
            '<td style="width: 25%; text-indent: 5px;"><%=item.groupId %></td>' +
            '<td style="width: 25%; text-indent: 5px;"><%=item.artifactId %></td>' +
            '<td style="width: 25%; text-indent: 5px;"><%=item.version %></td>' +
            '</tr>' +
            '<% }); %>'+
            '</tbody> ' +
            '</table>';

        var micro = new Y.Template();
        var str = micro.render(template, {data : data.cmpts});
        detailListWrapper.setHTML(str);
        Y.mt.widget.init();
        Y.one("#cmpt_num").set("text", data.countTotal);
        Y.one("#cmpt_num_distinct").set("text", data.count);
    }

    function showWaitMsg(node) {
        var html = '<div style="text-align: center;"><i class="fa fa-spinner fa-spin fa-3x text-blue"></i><span class="ml20">获取数据中...</span></div>';
        node.setHTML(html);
    }

    function clearWaitMsg(node) {
        node.setHTML('');
    }

    function showEmptyErrorMsg(node, isError) {
        var html = '<div style="text-align: center; font-size:30px;">' + (isError ? '查询出错' : '没有内容') + '</div>';
        node.setHTML(html);
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'mt-date',
        'w-base',
        'w-paginator',
        'template',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/common'
    ]
});
