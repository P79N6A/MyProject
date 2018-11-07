/* jshint indent: false */
M.add('msgp-utils/check', function(Y){
    Y.namespace('msgp.utils.check').init = function( nd, opts ){
        if( !nd || !(nd instanceof Y.Node) ) {
            throw new Error( 'Need a YUI node!' );
        }
        if( !opts ) opts = {};
        return new Init( nd, opts );
    };
    var defaultOption = {
        /* jshint laxcomma : true */
        type : 'string'
        ,emptyOk : false
        ,tipMsg : ""
        ,emptyMsg : "不能为空"
        ,warnMsg : ""
        ,warnMsg1 : ""
        ,maxValue : null
        ,minValue : null
        ,maxLength : null
        ,minLength : null
        ,vEvent : 'valuechange'
        ,customRegExp : false
        ,warnElement : null
        ,tipMsgInit : false
        ,relationElement : null
        ,callback : null
        ,errorback : null
        ,blurback : null
        ,noInit : false
        ,spaceOk : false
        ,spaceTips : '不能包含空格'
        ,chineseOk : false
        ,chineseTips : '不能包含中文'
    };
    function Init( nd, opts ){
        this.node = nd;
        this.opt = Y.merge( defaultOption, opts );
        this.hasFocus = false;
        this._val = '';
        if( !this.node.get('placeholder') || (this.node.get('placeholder') === this.opt.tipMsg) ){
            if( this.opt.tipMsg ){
                this.node.set( 'placeholder', this.opt.tipMsg );
                this.opt.tipMsgInit = false;
            }
        }
        this._initEvent();
        this._init();
    }
    Init.prototype._initEvent = function(){
        var self = this;
        this.node.on( 'focus', function(){
            self._focusHandler();
        } );
        this.node.on( this.opt.vEvent, function( e ){
            self._checkValue( e );
        } );
        this.node.on( 'blur', function( e ){
            self._blurHandler( e );
        } );
    };
    Init.prototype._init = function(){
        this._val = Y.Lang.trim( this.node.get('value') );
        var _checkVal = this._getResult();
        if( this._val !== '' && this.opt.noInit && _checkVal === 100 ){
            this._setStatus( true );
            return;
        }
        if( _checkVal === 100 ){
            this.opt.warnElement && this.opt.warnElement.setHTML( '√&nbsp;' ).setStyle( 'color', '#0c0' );
            this._setStatus( true );
            return;
        }else if( _checkVal === 0 && this.opt.emptyOk ){
            this._setStatus( true );
            return;
        }else if( _checkVal === 0 && !this.opt.emptyOk ){
            this._setStatus( false );
        }else if( _checkVal !== 0 ){
            this.opt.warnElement && this.opt.warnElement.setHTML( '' + this.opt.warnMsg ).setStyle( 'color', '#f00' );
            this._setStatus( false );
        }
        //是否在初始化的时候显示提示信息
        if( this.opt.tipMsgInit && this.opt.tipMsg && this.opt.warnElement ){
            this.opt.warnElement.setHTML( this.opt.tipMsg ).setStyle( 'color', '#999' );
        }
    };
    Init.prototype._focusHandler = function(){
        this.hasFocus = true;
        this.node.setStyle( 'background-color', '#fff' );
        if( /\s/.test( this.node.get('value') ) && !this.opt.spaceOk ){
            this.opt.warnElement && this.opt.warnElement.setHTML( '' + this.opt.spaceTips ).setStyle( 'color', '#f00' );
            this._setStatus( false );
            return;
        }
        var val = Y.Lang.trim( this.node.get('value') );
        this._val = val;
        var _checkVal = this._getResult();
        if( this._val === "" ){
            // resove the stupid need
        }else if( _checkVal === 100 ){
            if( this.opt.type !== 'psw' && this.opt.type !== 'confirmpsw' ){
                this.opt.warnElement && this.opt.warnElement.setHTML( '√&nbsp;' ).setStyle( 'color', '#0c0' );
            }
            if( typeof this.opt.callback === 'function' ){
                this.opt.callback();
            }
        }else if( _checkVal === 4 ){
            
        }else{
            this.opt.warnElement && this.opt.warnElement.setHTML( '' + this.opt.warnMsg ).setStyle( 'color', '#f00' );
        }
    };
    Init.prototype._blurHandler = function( e ){
        this._checkValue( e );
        this.hasFocus = false;
    };
    Init.prototype._checkValue = function( e ){
        //解决因按tab键获取焦点后会触发这个输入框的keyup事件问题
        if( e.keyCode === 9 ) return;
        this._val = this.node.get( 'value' );
        if( /\s/.test( this._val ) && !this.opt.spaceOk ){
            this.opt.warnElement && this.opt.warnElement.setHTML( '' + this.opt.spaceTips ).setStyle( 'color', '#f00' );
            this._setStatus( false );
            return;
        }
        var result = this._getResult();
        switch ( result ){
            case 0:
                if( this.opt.emptyOk ){
                    this.opt.warnElement && this.opt.warnElement.setHTML( '' );
                    this._setStatus( true );
                }else if( !this.opt.emptyOk ){
                    if( !this.hasFocus ){ //for the ie 10,11 placeholder set bug
                        this._setStatus( false );
                    }else{
                        this.opt.warnElement && this.opt.warnElement.setHTML( '' + this.opt.emptyMsg ).setStyle( 'color', '#f00' );
                        this._setStatus( false );
                    }
                }else{
                    //alert( this.opt.emptyMsg );
                }
                break;
            case 1:
            case 2:
                if( this.opt.warnElement ){
                    this.opt.warnElement.setHTML( '' + this.opt.warnMsg ).setStyle( 'color', '#f00' );
                }else{
                    //alert( this.opt.warnMsg );
                }
                this._setStatus( false );
                break;
            case 3:
                if( this.opt.warnElement ){
                    this.opt.warnElement.setHTML( '' + this.opt.warnMsg ).setStyle( 'color', '#f00' );
                }else{
                    //alert( this.opt.warnMsg );
                }
                this._setStatus( false );
                break;
            case 4:
                if( this.opt.warnElement ){
                    this.opt.warnElement.setHTML( this.opt.warnMsg1 ).setStyle( 'color', '#f00' );
                }else{
                    //alert( this.opt.warnMsg1 );
                }
                this._setStatus( false );
                break;
            case 5:
                if( this.opt.chineseTips ){
                    this.opt.warnElement.setHTML( this.opt.chineseTips ).setStyle( 'color', '#f00' );
                }else{
                    //alert( this.opt.warnMsg1 );
                }
                this._setStatus( false );
                break;
            case 100:
                if( this.opt.warnElement && this.opt.type !== 'psw' ){
                    this.opt.warnElement.setHTML( '√&nbsp;' ).setStyle( 'color', '#0c0' );
                }else{
                    //alert('√');
                }
                this._setStatus( true );
                break;
        }
        if( this._getStatus() && typeof this.opt.callback === 'function' ) {
            this.opt.callback();
        }
        if( !this._getStatus() && typeof this.opt.errorback === 'function' ) {
            this.opt.errorback();
        }
        if( this._getStatus() && e.type === 'blur' && typeof this.opt.blurback === 'function' ){
            this.opt.blurback();
        }
    }   ;
    Init.prototype._check = function( type, str ){
        if( str === undefined || str.length === 0 ) return 0;
        var reg;
        switch ( type ){
            case 'string':
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                if( !this.opt.chineseOk && encodeURIComponent(str).indexOf("%E") != -1 ) return 5;
                return 100;
            case 'id':
                if( !checkID(str) ) return 3;
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                return 100;
            case 'int':
                if( !/^[0-9]{0,}$/.test(str) ) return 3;
                if( this.opt.minValue && parseInt(str, 10) < this.opt.minValue ) return 1;
                if( this.opt.maxValue && parseInt(str, 10) > this.opt.maxValue ) return 2;
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                return 100;
            case 'mobile':
                if( !/^1[0-9]{10}$/.test(str) ) return 3;
                return 100;
            case 'email':
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                reg = /^[a-zA-Z0-9][a-zA-Z0-9._-]{0,}@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
                //fuck focus.cn email address
                if( str.split('@')[1] === 'focus.cn' ){
                    reg = /^[a-zA-Z0-9\u4e00-\u9fff][a-zA-Z0-9\u4e00-\u9fff._-]{0,}@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
                }
                return  reg.test( str ) ? 100 : 3;
             case 'emailprefix':
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                reg = /^[a-zA-Z0-9][a-zA-Z0-9._-]{0,}$/;
                return  reg.test( str ) ? 100 : 3;
            case 'psw':
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                return 100;
            case 'confirmpsw':
                if( this.opt.minLength && str.length < this.opt.minLength ) return 1;
                if( this.opt.maxLength && str.length > this.opt.maxLength ) return 2;
                if( this.opt.relationElement && (this.opt.relationElement.val() === '' || this.opt.relationElement.val() === str) ) return 100;
                if( this.opt.relationElement && this.opt.relationElement.val() !== '' && this.opt.relationElement.val() !== str ) return 4;
                return 100;
            case 'custom':
                if( this.opt.customRegExp ){
                    this.opt.customRegExp.lastIndex = 0;
                    if( !this.opt.customRegExp.test(str) ) return 3;
                }
                return 100;
        }
    };
    Init.prototype._getResult = function(){
        return this._check( this.opt.type, this._val );
    };
    Init.prototype._getStatus = function(){
        return this.node.getData( 'status' );
    };
    Init.prototype._setStatus = function( status ){
        this.node.setData( 'status', status );
    };
    Init.prototype.isValid = function(){
        return this.node.getData('status');
    };
    Init.prototype.showMsg = function(){
        var val = Y.Lang.trim( this.node.get('value') );
        if( val === '' && !this.opt.emptyOk ){
            this.opt.warnElement && this.opt.warnElement.setHTML( '' + this.opt.emptyMsg ).setStyle( 'color', '#f00' );
            textareaBlink( this.node );
        }
        if( !this._getStatus() ){
            textareaBlink( this.node );
        }
    };

    function checkID( str ){
        if( !(/^[1-9][0-9]{16}[0-9xX]$/.test(str) || /^[1-9][0-9]{14}$/.test(str)) ) return false;
        var year, month, date;
        if( str.length === 15 ){
            year = parseInt( '19' + str.substr(6,2), 10 );
            month = parseInt( str.substr(8,2), 10 );
            date = parseInt( str.substr(10,2), 10 );
        }else{
            year = parseInt( str.substr(6,4), 10 );
            month = parseInt( str.substr(10,2), 10 );
            date = parseInt( str.substr(12,2), 10 );
        }
        if( month < 1 || date < 1 || month > 12 || date > 31 ) return false;
        if( month === 4 || month === 6 || month === 9 || month === 11 ){
            if( date > 30 ) return false;
        }
        if ( month === 2 ){
            if( (year%4 === 0 && year%100 !== 0) || (year%100 === 0 && year%400 === 0) ){
                if( date > 29 ) return false;
            }else{
                if( date > 28 ) return false;
            }
        }
        return true;
    }
    function textareaBlink( obj ){
        obj.setStyle( 'background-color', '#fc9' );
        var sti = setInterval( function(){
            obj.setStyle( 'background-color', '#fc9' );
        }, 200 );
        var _sti = setTimeout( function(){
            obj.setStyle( 'background-color', '#fff' );
        }, 210 );
        var st = setTimeout( function(){
            clearTimeout( st );
            clearInterval( sti );
            clearTimeout( _sti );
            obj.setStyle( 'background-color', '#fc9' );
        }, 620 );
    }

}, '0.0.1', {
    requires : [
        'mt-base'
    ]
});
