M.add('msgp-hulk/detailOutline', function(Y){
    Y.namespace('msgp.hulk').detailOutline = detailOutline;
    var inited = false;
    var appkey,
        showOverlay,
        showContent,
        wrapper = Y.one('#wrap_outline'),
        cBody = wrapper.one('.content-body');
    var outlineTemplate = [
        '<div class="control-group" style="display:none;"><label class="control-label">服务名：</label>',
            '<div class="controls">',
                '<span id="outline_name" class="outline-content"><%= this.data.name %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">唯一标识：</label>',
            '<div class="controls">',
                '<span id="outline_appkey" class="outline-content"><%= this.data.appkey %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">负责人：</label>',
            '<div class="controls">',
                '<span id="outline_owners" class="outline-content"><%= this.data.ownersStr %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">类型：</label>',
            '<div class="controls">',
                '<span id="outline_category" class="outline-content"><%= this.data.category %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">所属业务：</label>',
            '<div class="controls">',
                '<span id="outline_business" class="outline-content"><%= this.data.businessName + (this.data.group ? " - "+this.data.group : "") %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">层级：</label>',
            '<div class="controls">',
                '<span id="outline_level" class="outline-content"><%= this.data.levelName %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">服务描述：</label>',
            '<div class="controls">',
                '<span id="outline_intro" class="outline-content"><%= this.data.intro %></span>',
            '</div>',
        '</div>',
        '<div class="control-group"><label class="control-label">标签：</label>',
            '<div class="controls">',
                '<span id="outline_tags" class="outline-content"><%= this.data.tags %></span>',
            '</div>',
        '</div>',
        '<div class="form-actions"><a class="btn btn-primary go-modify" href="/service/desc?appkey=<%= this.data.appkey %>">修改</a></div>'
    ].join('');

    function detailOutline( key, f1, f2 ){
        if( !inited ){
            appkey = key;
            showOverlay = f1;
            showContent = f2;
            inited = true;
        }
        getOutline();
    }
    function getOutline(){
        showOverlay( wrapper );
        var url = '/service/' + appkey + '/desc';
        Y.io( url, {
            method : 'get',
            on : {
                success : function(id, o){
                    var ret = Y.JSON.parse( o.responseText );
                    if( ret.isSuccess ){
                        fillOutline( ret.data );
                    }else{

                    }
                },
                failure : function(){

                }
            }
        });
    }
    function fillOutline( obj ){
        obj.ownersStr = getOwners(obj.owners);
        var micro = new Y.Template();
        var html = micro.render( outlineTemplate, {data:obj} );
        cBody.setHTML( html );
        showContent( wrapper );
    }
    function getOwners(arr){
        var tmp = [];
        for(var i=0,l=arr.length; i<l; i++){
            tmp.push( arr[i].name + "(" + arr[i].login+ ")");
        }
        return tmp.join(',');
    }
}, '0.0.1', {
    requires : [
        'mt-base',
        'mt-io',
        'template'
    ]
});
