/**
 * Created by yangjie on 7/23/15.
 */
M.add('msgp-manage/sgagent/agentSearchService', function (Y) {
    Y.namespace('msgp.manage').agentSearchService = agentSearchService;

    // define variable
    var wrapper = Y.one('#wrap_agentSearchService');
    var search_ip = wrapper.one('#search_ip');
    var search_appIP = wrapper.one('#search_appIP');
    var search_port = wrapper.one('#search_port');
    var search_button = wrapper.one('#search_button');
    var content_overlay = wrapper.one('.content-overlay');
    var content_table = wrapper.one('#table_wrapper');
    var tbody = content_table.one('tbody');
    var pbody = wrapper.one('#paginator_wrapper');
    var everPaged = false, totalPage, totalCount;
    var currentProtcol = "thrift";
    var appkey;

    //the template of table data.
    var trTemplate = [
        '<% Y.Array.each(this.data, function(item, index){ %>',
        '<tr id="ret-tr">',
        '<td><%= item.ip%></td>',
        '<td><%= item.version %></td>',
        '<td><%= item.port %></td>',
        '<td><%= item.weight %></td>',
        '<td><%= item.fweight %></td>',
        '<td class="status status-<%= item.status %>"><%= item.statusDesc %></td>',
        '<td><%= item.roleDesc %></td>',
        '<td><%= item.envDesc %></td>',
        '<td><%= Y.mt.date.formatDateByString( new Date(item.lastUpdateTime*1000), "yyyy-MM-dd hh:mm:ss" ) %></td>',
        '</tr>',
        '<% }); %>'
    ].join('');


    //
    function agentSearchService(key) {
        appkey=key;
        var agent_value = Y.Lang.trim(search_ip.get('value'));
        if ('' == agent_value) emptyOrError();

        bind();

        search_ip.on('keyup', searchOnkeyup);
        search_appIP.on('keyup', searchOnkeyup);
        search_port.on('keyup', searchOnkeyup);
        search_button.on('click', function () {
            getServiceList(1);
        });
    }

    function bind() {
        wrapper.delegate('click', function () {
            var type = Number(this.getAttribute("value"));
            this.ancestor('div').all('a').removeClass('btn-primary');
            switch (type) {
                case 1:
                    currentProtcol = "thrift";
                    this.addClass('btn-primary');
                    break;
                case 2:
                    currentProtcol = "http";
                    this.addClass('btn-primary');
                    break;
            }
        }, '#thrift_http a');
    }


    function searchOnkeyup(e) {
        if (e.keyCode === 13) {
            getServiceList(1);
        }
    }

    function getServiceList(pageNo) {
        var ip_value = Y.Lang.trim(search_ip.get('value'));
        if (!check_input_valid(appkey, ip_value)) return;
        var appIP_value = Y.Lang.trim(search_appIP.get('value'));
        var port_value = Y.Lang.trim(search_port.get('value'));
        var url = '/manage/agent/' + appkey + '/service/search';
        showContent(false);
        Y.io(url, {
            method: 'get',
            data: {
                ip: ip_value,
                pageNo: pageNo,
                pageSize: 20,
                appIP: appIP_value,
                port: port_value,
                protocol: currentProtcol
            },
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    var data = ret.data;
                    var pobj = ret.page;
                    if (ret.isSuccess) {
                        if (data && data.length > 0) {
                            fillTracking(data);
                            if (!everPaged || totalPage !== pobj.totalPageCount || totalCount !== pobj.totalCount) {
                                refreshPaginator(pbody, pobj);
                            }
                        } else {
                            emptyOrError();
                        }
                        everPaged = true;
                        totalPage = pobj.totalPageCount;
                        totalCount = pobj.totalCount;

                    } else {
                        emptyOrError();
                    }

                },
                failure: function () {
                    emptyOrError();
                }
            }
        });
    }

    function showContent(flag) {
        if (flag) {
            content_overlay.hide();
            pbody.show();
        } else {
            pbody.hide();
            wrapper.all('#ret-tr').remove();
            wrapper.all('#none-content').remove();
            content_overlay.show();
        }
    }

    function refreshPaginator(pbody, pobj) {
        new Y.mt.widget.Paginator({
            contentBox: pbody,
            index: pobj.pageNo || 1,
            max: pobj.totalPageCount || 1,
            pageSize: pobj.pageSize,
            totalCount: pobj.totalCount,
            callback: changePage
        });
    }

    function changePage(params) {
        getServiceList(params.page);
    }

    function fillTracking(arr) {
        Y.msgp.service.commonMap(fillTrackingAfter);
        function fillTrackingAfter(obj) {
            for (var i = 0, l = arr.length; i < l; i++) {
                var tmp = arr[i];
                tmp.statusDesc = obj.status[tmp.status] || tmp.status;
                tmp.envDesc = obj.env[tmp.envir] || tmp.envir;
                tmp.roleDesc = obj.role[tmp.role] || tmp.role;
            }
            var micro = new Y.Template();
            var html = micro.render(trTemplate, {data: arr});
            wrapper.all('#ret-tr').remove();
            tbody.append(html);
            showContent(true);
        }


    }

    function emptyOrError() {
        var html = '<tr id="none-content"><td colspan="9" class = "mb5 span4">获取失败：没有内容</td></tr>';
        wrapper.all('#ret-tr').remove();
        wrapper.all('#none-content').remove();
        tbody.append(html);
        pbody.hide();
        content_overlay.hide();
    }

    function check_input_valid(appkey_value, ip_value) {
        if (appkey_value == '') {
            local_error_alert('\"Appkey\"不能为空');
            return false;
        }

        if (ip_value == '') {
            local_error_alert('\"sg_agent IP地址\"不能为空');
            return false;
        } else if (!Y.msgp.utils.checkIP(ip_value)) {
            local_error_alert('请输入合法的\"IP地址\"');
            return false;
        }

        if (ip_value == '' && appkey_value == '') {
            local_error_alert('\"Appkey\"和\"IP地址\"不能为空');
            return false;
        }
        return true;
    }

    function local_error_alert(msg) {
        Y.msgp.utils.msgpHeaderTip('error', msg, 3);
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'template',
        'w-date',
        'mt-date',
        'transition',
        'w-paginator',
        'msgp-utils/msgpHeaderTip',
        'msgp-utils/checkIP',
        'msgp-service/commonMap'
    ]
});