M.add('msgp-utils/localEdit', function(Y){
    Y.namespace('msgp.utils').localEdit = localEdit;
    var EDITABLE = 'contenteditable';
    var EVENT_FOCUS = 'localEdit.focus',
        EVENT_BLUR  = 'localEdit.blur';
    function localEdit( sel, cb, regex, msg ){
        if( !sel || ( !Y.Lang.isString( sel ) && !(sel instanceof Y.Node) && !(sel instanceof Y.NodeList) ) ){
            throw new Error('Need selector or YUI node or YUI nodelist passed as the first argument!');
        }
        if( cb && !Y.Lang.isFunction(cb) ){
            throw new Error('Callback should be a function');
        }
        var obj = new Init( sel, cb, regex, msg );
        bindDocumentClick( obj );
        bindNodesEvent( obj );
    }
    function Init( sel, cb, regex, msg ){
        //construct a YUI NodeList from sel
        if( Y.Lang.isString( sel ) ){
            this.nodes = Y.all( sel );
        }else if( sel instanceof Y.Node ){
            this.nodes = Y.all( sel );
        }else{
            this.nodes = sel;
        }
        this.callback = cb || function(){};
        this.reg = regex;
        this.msg = msg || '数据格式错误';
    }
    function bindDocumentClick( obj ){
        Y.one(document).on('click', function(){
            obj.nodes.each(function( node ){
                if( node.getAttribute(EDITABLE) ){
                    node.fire( EVENT_BLUR );
                }
            });
        });
    }
    function bindNodesEvent( obj ){
        //do nothing if no matched nodes
        if( obj.nodes.size() === 0 ) return;
        obj.nodes.each(function(node){
            node.on('click', function(e){
                e.stopPropagation();
                if( this.getAttribute( EDITABLE ) ){
                    return;
                }else{
                    this.fire( EVENT_FOCUS );
                }
            });
        });
        obj.nodes.each(function(node){
            node.on('keydown', function(e){
                if( e.keyCode === 13 || e.keyCode === 9 ){
                    e.preventDefault();
                    this.fire( EVENT_BLUR );
                }
            });
        });
        obj.nodes.each(function(node){
            node.on( EVENT_FOCUS, function(){
                this.oldValue = this.getHTML();
                this.setAttribute( EDITABLE, true );
                this.focus();
                var self = this;
                obj.nodes.each(function( nd ){
                    if( nd.getAttribute(EDITABLE) && nd !== self ){
                        nd.fire( EVENT_BLUR );
                    }
                });
            });
        });
        obj.nodes.each(function(node){
            node.on(EVENT_BLUR, function(){
                this.removeAttribute( EDITABLE );
                this.newValue = this.getHTML();
                if( this.oldValue !== this.newValue ){
                    if( obj.reg ){
                        obj.reg.lastIndex = 0;
                        if( !obj.reg.test(this.newValue) ){
                            Y.msgp.utils.msgpHeaderTip('error', obj.msg, 3);
                            this.setHTML( this.oldValue );
                            return;
                        }
                    }
                    obj.callback( this, this.oldValue, this.newValue );
                }
            });
        });
    }
},'0.0.1', {
    requires : [
        'mt-base',
        'msgp-utils/msgpHeaderTip'
    ]
});
