//
//  TemplateRender.cpp
//  HbManager
//
//  Created by zhangjinlu on 15/11/2.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#include <iostream>
#include <stdlib.h>
#include <string>
#include <iostream>
#include <ctemplate/template.h>
#include "TemplateRender.h"
#include "utils/log4cplus.h"

using namespace inf::hlb;

bool TemplateRender::illegalCheck4Appkey( const string& appkey) {
    return true;
}

bool TemplateRender::illegalCheck4DegradeAction( const DegradeAction& degradeAction) {
    return true;
}

//** 传入appkey， 返回正常情况下的lua脚本内容
string TemplateRender::getNormalLua( const string& appkey) {
    string luaString = "";

    //入口参数合法性检测
    if (!illegalCheck4Appkey(appkey)) {
        return luaString;
    }

    //填充Template Dict
    ctemplate::TemplateDictionary dict( "hlb_normal_lua_v1");
    string dict_appkey = "\""+ appkey +"\"";
    dict.SetValue( "APPKEY", dict_appkey);
    
    //渲染
    ctemplate::Template* tpl = ctemplate::Template::GetTemplate( _normal_lua_tpl_filename, ctemplate::DO_NOT_STRIP);
    tpl->Expand(&luaString, &dict);
    LOG_DEBUG("[TemplateRender::getNormalLua] luaString = "<<luaString);
    ctemplate::Template::ClearCache();

    return luaString;
}

//** 传入DegradeAction， 返回执行该降级操作的lua脚本内容
string TemplateRender::getDegradeLua( const string& appkey, const DegradeAction& degradeAction) {
    string luaString = "";

    //入口参数合法性检测
    if (!illegalCheck4DegradeAction( degradeAction)) {
        return getNormalLua( appkey);
    }

    //填充Template Dict
    ctemplate::TemplateDictionary dict( "hlb_degrade_lua_v1");
    string dict_appkey = "\""+ appkey +"\"";
    dict.SetValue( "APPKEY", dict_appkey);

    string providerAppkey = "\""+ degradeAction.providerAppkey +"\"";
    dict.SetValue( "PROVIDERAPPKEY", providerAppkey);

    double degradeRatio = degradeAction.degradeRatio;
    dict.SetFormattedValue( "DEGRADERATIO", "%f", degradeRatio);

    if (degradeAction.degradeStrategy == DegradeStrategy::CUSTOMIZE) {
        dict.SetValue( "DEGRADESTRATEGY", "\"CUSTOMIZE\"");
    } else {
        dict.SetValue( "DEGRADESTRATEGY", "\"DROP\"");
    }

    dict.SetValue( "DEGRADEREDIRECT", "\"\"");

    //渲染
    ctemplate::Template* tpl = ctemplate::Template::GetTemplate( _degrade_lua_tpl_filename, ctemplate::DO_NOT_STRIP);
    tpl->Expand(&luaString, &dict);
    LOG_DEBUG("[TemplateRender::getDegradeLua] luaString = "<<luaString);
    ctemplate::Template::ClearCache();

    return luaString;
}

