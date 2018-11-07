/*jshint ignore:start*/
YUI.add('config-user/template/list-tpl', function(Y, NAME) {
    Y.namespace('mt.config.user.template').list = Y.Template.Micro.compile([
          '<div class="<%= data.css_prefix %>-filter-container">'
        , '    <input type="text" class="<%= data.css_prefix %>-filter" />'
        , '    <% if (data.canUpdate) { %>'
        , '    <button class="btn <%= data.css_prefix %>-add J-<%= data.css_prefix %>-add">添加</button>'
        , '    <% } %>'
        , '</div>'
        , '<ul class="<%= data.css_prefix %>-list clearfix">'
        , '    <% Y.Array.each(data.items, function(item) { %>'
        , '    <li <% Y.Array.each(Y.Object.keys(item), function(key) { %>'
        , '        data-<%= key %>="<%= item[key] %>"<% }) %>>'
        , '        <%= item.name %> - <%= item.login %>'
        , '        <i class="fa fa-trash-o <%= data.css_prefix %>-delete"></i>'
        , '    </li>'
        , '    <% }) %>'
        , '</ul>'
    ].join('\n'));
}, '0.1.0', { requires: [ 'template', 'template-micro' ] });
