M.add('msgp-component/componentTabNavigationExt', function (Y) {
    Y.namespace('msgp.component').componentTabNavigationExt = componentTabNavigationExt;
    var tab_style;
    var tab;
    var validHash = ['cmpt_version', 'cmpt_coverage', 'cmpt_stack', 'cmpt_query', 'cmpt_app', 'cmpt_user'];
    var preHash;
    var currentGroupId;
    var currentArtifactId;

    function componentTabNavigationExt(_tab_style,_groupId, _artifactId) {
        tab_style = _tab_style;
        currentGroupId = _groupId;
        currentArtifactId = _artifactId;
        initPageHashAndTab();
        Y.msgp.utils.hashchange(hashChangeCallback);
    }

    function initPageHashAndTab() {
        //init hash
        var hash = location.hash.slice(1);
        if (hash === '' || Y.Array.indexOf(validHash, hash) === -1) {
            location.hash = '#' + tab_style;
        }
        //init tab
        tab = Y.mt.widget.Tab('#tab_trigger li', '#content_wrapper .sheet', {
            defaultSkin: true,
            defaultIndex: 0
        });
    }

    function hashChangeCallback(hash) {
        if (preHash == hash) {
            return;
        }
        switchToTab(hash);
        tab.select({
            trigger: Y.one('a[href="#' + hash + '"]').ancestor('li'),
            sheet: Y.one('#wrap_' + hash)
        });
        //隐藏当前hash,显示第一个hash
        if(preHash) {
            $('#div_' + preHash).empty();
            Y.one('#div_' + preHash).hide();
            Y.one('#wrap_' + preHash).hide();
        }
        Y.one('#div_' + hash).show();
        Y.one('#wrap_' + hash).show();
        preHash = hash;
    }

    function switchToTab(hash) {
        tab_style = hash;
        var hashIndex = Y.Array.indexOf(validHash, hash);
        if (hashIndex === -1) {
            throw new Error('Invalid hash value');
        }
        if(Y.one("#groupId")){
            currentGroupId = Y.one("#groupId").get("value");
        }
        if(Y.one("#artifactId")){
            currentArtifactId = Y.one("#artifactId").get("value");
        }

        if(currentArtifactId == "" || currentGroupId == ""){
            currentGroupId = "com.meituan.inf";
            currentArtifactId = "xmd-common-log4j2";
        }

        if (hash) {
            var div_content = Y.one('#div_' + hash);
            var content = Y.one('#text_' + hash).get("value");
            div_content.setHTML(content);
            Y.msgp.component[hash](tab_style, currentGroupId, currentArtifactId);
        } else {
            throw new Error('Invalid hash value');
        }
    }
}, '0.0.1', {
    requires: [
        'mt-base',
        'mt-io',
        'w-tab',
        'w-base',
        'msgp-utils/hashchange',
        'msgp-component/cmpt_version',
        'msgp-component/cmpt_coverage',
        'msgp-component/cmpt_query',
        'msgp-component/cmpt_stack',
        'msgp-component/cmpt_app',
        'msgp-component/cmpt_user'
    ]
});
