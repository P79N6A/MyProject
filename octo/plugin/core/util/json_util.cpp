#include "json_util.h"

#include <glog/logging.h>
#include <iostream>
#include <rapidjson/document.h>
#include <rapidjson/writer.h>
#include <rapidjson/stringbuffer.h>
#include <rapidjson/prettywriter.h>


using namespace std;
using namespace rapidjson;

namespace cplugin {

    std::string Json_Util::getMoniterStr(const int& status, const string& msg){
        Document doc;
        doc.SetObject();
        Document::AllocatorType &allocator=doc.GetAllocator(); //获取分配器

        doc.AddMember("ret", status,allocator);

        rapidjson::Value strObject(rapidjson::kStringType);
        strObject.SetString(msg.c_str(), allocator);
        doc.AddMember("retMsg", strObject, allocator);

        Value array(kArrayType);

        doc.AddMember("data",array,allocator);

        StringBuffer buffer;
        PrettyWriter<StringBuffer> pretty_writer(buffer);  //PrettyWriter是格式化的json，如果是Writer则是换行空格压缩后的json
        doc.Accept(pretty_writer);

        return buffer.GetString();
    }

} // namespace cplugin
