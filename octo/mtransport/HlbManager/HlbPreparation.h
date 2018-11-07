//
//  HlbPreparation.h
//  HlbManager
//
//  Created by zhangjinlu on 16/3/9.
//  Copyright (c) 2016年 zhangjinlu. All rights reserved.
//

#ifndef __HLB_PREPARATION_H__
#define __HLB_PREPARATION_H__

#include "./utils/Singleton.h"
#include "./utils/auto_lock.h"

namespace inf {
namespace hlb {
using namespace std;

class HlbPreparation : public Singleton<HlbPreparation> {
public:
    HlbPreparation() {}
    bool initialization();

    //初始化nginx配置所需文件
    bool initFiles();
};

}
}



#endif /* defined(__HLB_PREPARATION_H__) */
