// =====================================================================================
// 
//       Filename:  switch_file.h
// 
//       Description: 自动开关 
// 
//       Version:  1.0
// 
// 
//  =====================================================================================

#ifndef __switch_file_H__
#define __switch_file_H__

#include "comm/tinyxml2.h"
#include "sgagent_common_types.h"

namespace sg_agent {

class SGAgentSwitch {
 public:
  static int initSwitch(const int, bool &);

  static int setSwitch(const int, bool &);

  static bool _checkSwitchFile();

  static int _readDefaultFile(const int, bool &);

  static int _readSwitchFile(const int, bool &);

  static int _createSwitchFile();

  static int _writeSwitchFile(const int, const bool);
};

} //namespace
#endif
