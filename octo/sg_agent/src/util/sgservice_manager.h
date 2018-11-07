#include <string>
#include <vector>

#include "SGAgent.h"

class SGServiceManager {
 public:
  SGServiceManager();
  ~SGServiceManager();
  int UpdateSvrList(std::vector <SGService> &,
                    std::vector <SGService> &vec_sgservice_add,
                    std::vector <SGService> &vec_sgservice_del,
                    std::vector <SGService> &vec_sgservice_chg);

  std::vector <SGService> getSvrList();

  int getOneSvr(SGService &svr);

  void delOneSvr(const std::string &host, const int port);

 private:
  std::vector <SGService> svrList;

};
