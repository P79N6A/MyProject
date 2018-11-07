M.add('msgp-serviceopt/optConfig-version0.1.1', function (Y) {
        Y.namespace('msgp.serviceopt').optConfig = Config;
        //Y.namespace('mt.config');

        var inited = false;
        var each = Y.Object.each;
        var filter = Y.Array.filter;
        var sub = Y.Lang.sub;
        var rest = Y.msgp.config.rest;
        var tpl = Y.msgp.config.tpl;
        var popup = Y.msgp.config.popup;
        var Tree = Y.msgp.config.tree;
        var appkey,
            showOverlay,
            showContent,
            wrapper = Y.one('#wrap_config'),
            tbody = wrapper.one('tbody'),
            pbody = wrapper.one('#paginator_config');
        var one = wrapper.one;



        var cookie = Y.mt.cookie;
        var SELECTOR = {
            container: {
                tree: '#J-config-container-tree',
                panel: '#J-config-container-panel',
                // headerH1: '#J-config-header-h1',
                // headerUl: '#J-config-header-ul',
                // headerDelete: '#J-config-header-delete',
                // headerAdd: '#J-config-header-add',
            }
        };
        var TEXT = {
            spacesComfirmDelete: '确定删除空间 {spaceName} 吗，此操作不可逆转',
            spacesTipDeleteSuccess: '删除空间 {spaceName} 成功',
            spacesTipAddSuccess: '添加空间 {spaceName} 成功',
            spacesPromptAdd: '请输入新空间名：'
        };

        var container;

        function Config(key, func1, func2) {
            container = getNodesObj(SELECTOR.container);
            if (!inited) {
                appkey = key;
                showOverlay = func1;
                showContent = func2;
                inited = true;
            }
            initDynamicCfg();
            Y.msgp.serviceopt.optFileConfig(appkey);
        }


        function initDynamicCfg() {

            showOverlay(wrapper);

            //rootNode对应appkey
            var spaceName = appkey;
            var nodeName = spaceName;

            var tree = new Tree({
                spaceName: spaceName,
                nodeName: nodeName,
                isLeaf: true,
                enableAdd: true,
                isCell: false
            }, container.tree, appkey, wrapper.one("#J-config-container-menu"));
            showContent(wrapper);
            this.tree = tree;
        }


        function destroyTree() {
            if (this.tree) {
                this.tree.destroy();
            }
        }



// 通过 selector 获取 node
        function getNodesObj(selectorObj) {
            var containerObj = {};
            each(selectorObj, function (value, key) {
                containerObj[key] = one(value);
            });
            return containerObj;
        }

    },
    '0.0.1', {
        requires: [
            'node',
            'collection',
            'mt-cookie',
            'w-autocomplete',
            'msgp-config/tpl-version0.0.8',
            'msgp-config/rest-version0.0.2',
            'msgp-config/popup',
            'msgp-config/tree-version0.1.2',
            'msgp-serviceopt/optFileConfig-version0.0.14'
        ]
    }
)
;