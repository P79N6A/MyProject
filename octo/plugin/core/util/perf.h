#ifndef PERF_H_
#define PERF_H_

#include <stdio.h>
#include <malloc.h>
#include <errno.h>
#include <unistd.h>
#include "stdint.h"
#include "time.h"

namespace cplugin {

typedef unsigned long long ul_t;

typedef struct cpu_t {
	ul_t u, n, s, i, w, x, y, z; // as represented in /proc/stat
	ul_t u_sav, s_sav, n_sav, i_sav, w_sav, x_sav, y_sav, z_sav; // in the order of our display
} cpu_t;

typedef struct sys_info_t {
	ul_t mem_ocy;
	ul_t free_mem_ocy;
	ul_t cpu_rate;
	ul_t online_processors;
  ul_t cur_process_cpu_rate;
} SysInfo;

class Perf {
public:
	static Perf* Instance();
	static void Destory();
	void KeepUpdate();
	sys_info_t GetSysInfo() const;
private:
	Perf();
	~Perf();
	cpu_t* cpus_refresh();
  void update_cur_process_cpu_rate();
	void update_cpu_rate();
	void update_mem_state();

	static Perf* instance_;
	cpu_t* cpu_;
	sys_info_t sys_info_;

  uint32_t last_total_;
  uint32_t last_process_;
  time_t last_update_;
};

} // namespace core

#endif // PERF_H_
