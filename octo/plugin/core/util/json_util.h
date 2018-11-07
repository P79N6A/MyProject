#ifndef _JSON_H_
#define _JSON_H_

#include <string>

namespace cplugin {

    class Json_Util {
    public:
        Json_Util() {}
        ~Json_Util() {}
        static std::string getMoniterStr(const int& status, const std::string& msg);

    private:

    };

} // namespace cplugin

#endif // _JSON_H_
