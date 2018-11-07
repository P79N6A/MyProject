//
// Created by huixiangbo on 17/7/18.
//

#ifndef CTHRIFT_KMS_H
#define CTHRIFT_KMS_H

#include "cthrift_common.h"


namespace cthrift {

            class CthriftKmsTools {
            private:

                StrStrMap    appekyTokenMap_;
                StrStrMap    whiteMap_;
                StrSMMap     methodAppkeyTokenMap_;
                std::string  local_token_;

                std::string appkey_;

                void OnGetAppekyTokenMap();

                void OnGetAppkeyWhitelist();

                void OnGetMethodAppkeyTokenMap();

                void OnGetLocalTokenString();

            public:


                StrStrMap& GetAppekyTokenMap(){
                     return appekyTokenMap_;
                }

                 StrStrMap& GetAppkeyWhitelist(){
                   return whiteMap_;
                }

                 StrSMMap& GetMethodAppkeyTokenMap(){
                     return methodAppkeyTokenMap_;
                }

                 std::string&  GetLocalTokenString(){
                    return local_token_;
                }

                std::string&  GetLocalTokenString_X(){
                    OnGetLocalTokenString();
                    return local_token_;
                }

                void Update();

                CthriftKmsTools(const std::string& appkey);
                ~CthriftKmsTools();




                static std::string hmacSHA1(const std::string& token, const std::string& data);


            };

}


#endif //CTHRIFT_KMS_H
