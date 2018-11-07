M.add('msgp-manage/portrait/serviceStatus', function(Y){
    Y.namespace('msgp.manage').serviceStatus = serviceStatus;
    var tbody = Y.one('#wrap_serviceStatus tbody'),
        colspan = 7;
    var wrapper;

    function serviceStatus(appkeytmp) {
        initTable(appkeytmp)
    }

    function initTable(appkeysToFind) {
        setLoading();
        wrapper = Y.one('#wrap_serviceStatus');

        // 全选方框
        bindClickCheckBox();

        // 自己设定的后端接口
        var url = '/manage/data/portrait/serviceStatus';

        Y.io(url, {
            method: 'get',
            data: {appkeys: appkeysToFind},
            on: {
                success: function (id, o) {
                    var ret = Y.JSON.parse(o.responseText);
                    if (ret.isSuccess) {
                        var data = ret.data;
                        if (Y.Lang.isArray(data) && data.length !== 0) {
                            fillTableContent(data);
                        } else if (data.length === 0) {
                            setEmpty();
                        }
                    } else {
                        Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                        setError();
                    }
                },
                failure: function () {
                    Y.msgp.utils.msgpHeaderTip('error', '数据获取失败', 3);
                    setError();
                }
            }
        });
    }



    function fillTableContent(data) {
        // 数据格式在这里定义，自己仔细设计。一行行添加数据
        var html = '';
        for (var i = 0, l = data.length; i < l; i++) {
            var showData = data[i];
            var tdDatas = Array();
            // tdDatas.push("<input id=\"one-checkbox\" type=\"checkbox\">");
            tdDatas.push(" ");
            tdDatas.push("<a  target='_blank'>" +showData.appkey +"</a>");

            // 是否自动重启
            var text= "";
            var state = showData.tags.restartable;
            if(typeof state == 'string'){
                var num = Number(state);
                if(num == 0){
                    text = "<span style='color: green;'>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;否</span>";
                }else if(num==1){
                    text = "<span style='color: red;'>&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;是</span>";
                }else if(num==2 ) {
                    text = "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;---";
                }
            }else if(typeof state == 'null'){
                text = "<span style='color: grey;'>未确定</span>";
            }else {
                text = "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;---";
            }
            tdDatas.push(text);

            // 是否有状态
            var text= "";
            var state = showData.tags.stateful;
            if(typeof state == 'string'){
                var num = Number(state);
                if(num == 0){
                    text = "<span style='color: green;'>&ensp;无状态</span>";
                }else if(num==1){
                    text = "<span style='color: red;'>&ensp;有状态</span>";
                }else if(num==2 ) {
                    text = "未确定";
                }
            }else if(typeof state == 'null'){
                text = "<span style='color: grey;'>&ensp;未确定</span>";
            }else {
                text = "&ensp;未确定";
            }
            tdDatas.push(text);

            var nulltext = " ";
            if(i==0){
                nulltext= "开发中";
            }
            tdDatas.push("&ensp;&ensp;&ensp;&ensp;"+nulltext);
            tdDatas.push("&ensp;&ensp;&ensp;&ensp;"+nulltext);
            tdDatas.push("&ensp;"+nulltext);


            html+=  "<tr><td>" + tdDatas.join("</td><td>") + "</td></tr>";
        }
        tbody.setHTML(html);
    }

    function bindClickCheckBox() {
        // 全选方框
        wrapper.delegate('click', function () {
            var ifChecked = this.get("checked");
            //单选与全选保持一致
            wrapper.all('#one-checkbox').set("checked", ifChecked);
        }, '#all-check');
        //单选
        wrapper.delegate('click', function () {
            //全选与单选保持一致
            var allOneCheck = wrapper.all('#one-checkbox');
            var allOneChecked = wrapper.all('#one-checkbox:checked');
            if (allOneChecked.size() === 0) {
                wrapper.one('#all-check').set("checked", false);
            } else {
                if (allOneCheck.size() === allOneChecked.size()) {
                    wrapper.one('#all-check').set("checked", true);
                }
            }
        }, '#one-checkbox');
    }

    function setLoading() {
        var loadingHtml = '<tr><td colspan="' + colspan + '"><i class="fa fa-spinner fa-spin ml5 mr10"></i>获取数据中...</td></tr>';
        tbody.setHTML(loadingHtml);
    }

    function setError() {
        var errorHtml = '<tr><td colspan="' + colspan + '">获取失败</td></tr>';
        tbody.setHTML(errorHtml);
    }

    function setEmpty() {
        var emptyHtml = '<tr><td colspan="' + colspan + '">没有数据</td></tr>';
        tbody.setHTML(emptyHtml);
    }

    function setHint() {
        var emptyHtml = '<tr><td colspan="' + colspan + '">请点击查询</td></tr>';
        tbody.setHTML(emptyHtml);
    }

}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'w-base',
        'msgp-utils/msgpHeaderTip'
    ]
});