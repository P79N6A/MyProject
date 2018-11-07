YUI.add('config-tpl', function(Y) {
    Y.namespace('mt.config');
    var each = Y.Object.each;
    var tpl = {
        headerUl: [
            '{{#each spacesList}}',
            '<li><a href="javascript:void(0);">{{this.name}}</a></li>',
            '{{/each}}'
        ].join('\n'),
        rootNode: [
            '<div class="J-config-tree-wrapper">',
            '    <div class="config-tree-node J-config-tree-node">',
            '        <a href="javascript:void(0);" class="J-config-tree-toggle">',
            '            {{#if isLeaf}}',
            '                <span class="J-config-tree-toggle-fa">&#8226;</span>',
            '            {{else}}',
            '                <i class="fa fa-caret-right J-config-tree-toggle-fa"></i>',
            '            {{/if}}',
            '            {{nodeLastName}}',
            '        </a>',
            '        <span class="pull-right">',
            '            <a href="javascript:void(0);" class="J-config-tree-controller-update config-tree-controller config-tree-controller-update"><i class="fa fa-pencil-square"></i></a>',
            '            <a href="javascript:void(0);" class="J-config-tree-controller-add config-tree-controller config-tree-controller-add"><i class="fa fa-plus"></i></a>',
            '            <a href="javascript:void(0);" class="J-config-tree-controller-synclog config-tree-controller config-tree-controller-synclog"><i class="fa fa-exchange"></i></a>',
            '            <a href="javascript:void(0);" class="J-config-tree-controller-delete config-tree-controller config-tree-controller-delete"><i class="fa fa-trash-o"></i></a>',
            '        </span>',
            '    </div>',
            '    <ul></ul>',
            '</div>'
        ].join('\n'),
        panelTrList: [
            '{{#each data}}',
            '<tr class="J-config-panel-item">',
            '    <td class="control-group J-control-group J-config-panel-key" role="key">',
            '        <input type="text" value="{{key}}">',
            '    </td>',
            '    <td class="J-control-group J-config-panel-value" role="value">',
            '        <textarea rows="1">{{value}}</textarea>',
            '    </td>',
            '    <td class="J-control-group J-config-panel-comment" role="comment">',
            '        <input type="text" value="{{comment}}">',
            '    </td>',
            '    <td class="config-panel-delete-td">',
            '        <a href="javascript:void(0);" class="config-panel-delete J-config-panel-delete">',
            '            <i class="fa fa-trash-o"></i>',
            '            <span class="J-config-panel-delete-text">删除</span>',
            '        </a>',
            '    </td>',
            '</tr>',
            '{{/each}}'
        ].join('\n'),
        panelTr: [
            '<tr class="J-config-panel-item">',
            '    <td class="control-group J-control-group J-config-panel-key" role="key">',
            '        <input type="text">',
            '    </td>',
            '    <td class="J-control-group J-config-panel-value" role="value">',
            '        <textarea rows="1"></textarea>',
            '    </td>',
            '    <td class="J-control-group J-config-panel-comment" role="comment">',
            '        <input type="text">',
            '    </td>',
            '    <td class="config-panel-delete-td">',
            '        <a href="javascript:void(0);" class="config-panel-delete J-config-panel-delete">',
            '            <i class="fa fa-trash-o"></i>',
            '            <span class="J-config-panel-delete-text">删除</span>',
            '        </a>',
            '    </td>',
            '</tr>'
        ].join('\n')
    };
    tpl.node = [
        '<li>',
        tpl.rootNode,
        '</li>'
    ].join('\n');
    function compileTpl(tpl) {
        var compiledTpl = {};
        var compile = Y.Handlebars.compile;
        each(tpl, function(value, key) {
            compiledTpl[key] = compile(value);
        });
        return compiledTpl;
    }
    Y.mt.config.tpl = compileTpl(tpl);
}, '', { requires: [
    'handlebars'
]});
