/**
 * Created by yves on 16/9/25.
 */

M.add('msgp-component/cmpt_blank', function (Y) {
    var tab_style = "";
    var groupId;
    var artifactId;

    Y.namespace('msgp.component').cmpt_blank = cmpt_blank;
    function cmpt_blank(_tab_style, _groupId, _artifactId) {
        document.title = '开放转移功能';
        tab_style = _tab_style;
        groupId = _groupId;
        artifactId = _artifactId;
    }

}, '0.0.1', {
    requires: [
        'w-base',
        'mt-base',
        'mt-io'
    ]
});
