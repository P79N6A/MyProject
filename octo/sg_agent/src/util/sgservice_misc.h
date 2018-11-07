#ifndef __sgservice_misc_H__
#define __sgservice_misc_H__

#include <string>

#include "SGAgent.h"

class SGServiceMisc {
 public:
  static std::string SGService2String(const SGService &sgservice);

  static bool SGServiceCompare(const SGService &sgservice1,
                               const SGService &sgservice2);

  static int SGServiceIpPortCompare(const SGService &sgservice1,
                                    const SGService &sgservice2);

  static bool IsSGServiceEqual(const SGService &sgservice1,
                               const SGService &sgservice2);

  // slist&dlist have to be sorted
  static int UpdateSvrList(const std::vector <SGService> &slist,
                           std::vector <SGService> &dlist,
                           std::vector <SGService> &addlist,
                           std::vector <SGService> &dellist,
                           std::vector <SGService> &chglist);

  static bool CheckVerifyCode(const std::string &verifyCode);

  static void ChangeUnifiedProto2False(std::vector <SGService> *);

  static void ChangeUnifiedProto2FalseWithVersionCheck(std::vector <SGService> *);
  static bool IsMTthriftVersion(const std::string &version);
};

#endif
