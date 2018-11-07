hello tree!
<div id="orgtree">
</div>
<script>
    M.use("w-tree", function(Y) {
        var levelUrl = '/manage/orgTreeLevel';
        var searchUrl = '/manage/orgTreeSearch';
        new Y.mt.widget.Tree({
            elTarget: '#orgtree',
            position: 'inner',
            maxHeight: '600',
            skin: 'widget-tree-big',
            placeholder: '请输入拼音或中文搜索',
            levelAsync: {
                url: levelUrl
            },
            searchAsync: {
                url: searchUrl
            },
            afterChoose: function(nodeData) { // nodeData与mtorg-remote-service中的OrgTreeNodeVo对应
                _this.onOrgSelect(
                        nodeData.dataId,
                        nodeData.name
                );
            },
            noDataMsg: '用户不存在或者没有该应用的权限'
        });
    });
</script>