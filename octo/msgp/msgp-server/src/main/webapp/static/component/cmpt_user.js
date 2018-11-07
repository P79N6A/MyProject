/**
 * Created by yves on 17/4/1.
 */
M.add('msgp-component/cmpt_user', function (Y) {
    Y.namespace('msgp.component').cmpt_user = cmpt_user;
    var userWrapper;

    function cmpt_user() {
        document.title = '组件自助处理';
        userWrapper = Y.one("#div_cmpt_user");
        bindWidget();
    }

    function bindWidget() {
        userWrapper.one('#applyBtn').on('click', function () {
            var artifactId = Y.Lang.trim(userWrapper.one("#artifactId").get('value'));
            var groupId =  Y.Lang.trim(userWrapper.one("#groupId").get('value'));
            var reason =  Y.Lang.trim(userWrapper.one("#reason").get('value'));
            if(artifactId == '' || groupId == '' || reason == ''){
                Y.msgp.utils.msgpHeaderTip('error', '组件groupId && artifactId && 申请理由 不能为空', 5);
                return;
            }
            var data = {
                "groupId": groupId,
                "artifactId": artifactId,
                "reason": reason
            };
            var url = '/component/delete/application';
            Y.io(url, {
                method: 'get',
                data: data,
                on: {
                    success: function (id, o) {
                        var ret = Y.JSON.parse(o.responseText);
                        if (ret.isSuccess) {
                            Y.msgp.utils.msgpHeaderTip('success', '申请成功,请等待审核通过', 3);
                        } else {
                            Y.msgp.utils.msgpHeaderTip('error', '申请失败,请重新申请', 3);
                        }
                    },
                    failure: function () {
                        Y.msgp.utils.msgpHeaderTip('error', '申请失败,请重新申请', 3);
                    }
                }
            });
        });
    }

}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'mt-date',
        'w-date'
    ]
});