#include "perf.h"

#include <string.h>
#include <glog/logging.h>

namespace cplugin {

#define ONE_MB (1024 * 1024)
#define SMLBUFSIZ 256

Perf * Perf::instance_ = NULL;

Perf * Perf::Instance()
{
	if (NULL == instance_)
		instance_ = new Perf();
	return instance_;
}

void Perf::Destory()
{
  if (!instance_) 
	  delete instance_;
  instance_ = NULL;
}

Perf::Perf()
{
	cpu_ = (cpu_t *)malloc(sizeof(cpu_t));
  memset(cpu_, 0, sizeof(cpu_t));
	sys_info_.online_processors = sysconf(_SC_NPROCESSORS_ONLN);
  last_update_ = 0;
  last_total_ = 0;
  last_process_ = 0;
}

Perf::~Perf()
{
	free(cpu_);
}

cpu_t *Perf::cpus_refresh()
{
	static FILE *fp = NULL;
	int num;
	char buf[SMLBUFSIZ];

	if (!fp) {
		if (!(fp = fopen("/proc/stat", "r")))
      LOG(ERROR) << "failed /proc/stat open: " << strerror(errno);
	}
	rewind(fp);
	fflush(fp);

	// first value the last slot with the cpu summary line
	if (!fgets(buf, sizeof(buf), fp)) LOG(ERROR) << "failed /proc/stat read";

	cpu_->x = 0;
	cpu_->y = 0;
	cpu_->z = 0;
	num = sscanf(buf, "cpu %Lu %Lu %Lu %Lu %Lu %Lu %Lu %Lu",
              &cpu_->u,
              &cpu_->n,
              &cpu_->s,
              &cpu_->i,
              &cpu_->w,
              &cpu_->x,
              &cpu_->y,
              &cpu_->z
              );
	if (num < 4)
    LOG(ERROR) << "failed /proc/stat read. parameters less than 4";

	return cpu_;
}

void Perf::update_cpu_rate()
{
#define TRIMz(x)  ((tz = (long long)(x)) < 0 ? 0 : tz)
	long long u_frme, s_frme, n_frme, i_frme, w_frme, x_frme, y_frme, z_frme, tot_frme, tz;
	float scale;

	u_frme = cpu_->u - cpu_->u_sav;
	s_frme = cpu_->s - cpu_->s_sav;
	n_frme = cpu_->n - cpu_->n_sav;
	i_frme = TRIMz(cpu_->i - cpu_->i_sav);
	w_frme = cpu_->w - cpu_->w_sav;
	x_frme = cpu_->x - cpu_->x_sav;
	y_frme = cpu_->y - cpu_->y_sav;
	z_frme = cpu_->z - cpu_->z_sav;
	tot_frme = u_frme + s_frme + n_frme + i_frme + w_frme + x_frme + y_frme + z_frme;
	if (tot_frme < 1) tot_frme = 1;
	scale = 100.0 / (float)tot_frme;

	cpu_->u_sav = cpu_->u;
	cpu_->s_sav = cpu_->s;
	cpu_->n_sav = cpu_->n;
	cpu_->i_sav = cpu_->i;
	cpu_->w_sav = cpu_->w;
	cpu_->x_sav = cpu_->x;
	cpu_->y_sav = cpu_->y;
	cpu_->z_sav = cpu_->z;

	sys_info_.cpu_rate = (tot_frme - i_frme) * scale;

#undef TRIMz
}

void Perf::update_mem_state()
{
	long page_size = sysconf(_SC_PAGESIZE);
	long num_pages = sysconf(_SC_PHYS_PAGES);
	long free_pages = sysconf(_SC_AVPHYS_PAGES);
	ul_t mem = (ul_t)((ul_t)num_pages * (ul_t)page_size);
	sys_info_.mem_ocy = (mem /= ONE_MB);
	ul_t free_mem = (ul_t)((ul_t)free_pages * (ul_t)page_size);
	sys_info_.free_mem_ocy = (free_mem /= ONE_MB);
}

void Perf::KeepUpdate()
{
	update_mem_state();
    update_cur_process_cpu_rate();
	cpus_refresh();
	update_cpu_rate();
}

sys_info_t Perf::GetSysInfo() const
{
	return sys_info_;
}

char* get_items(char* buffer, int ie)
{
    char* p = buffer;
    int len = strlen(buffer);
    int count = 0;
    if (1 == ie || ie < 1)
    {
        return p;
    }
    int i;

    for (i = 0; i < len; i++)
    {
        if (' ' == *p)
        {
            count++;
            if (count == ie - 1)
            {
                p++;
                break;
            }
        }
        p++;
    }

    return p;
}

unsigned int get_cpu_process_occupy(const pid_t p)
{
    char file[64] = { 0 };
    pid_t pid;
    unsigned int utime;
    unsigned int stime;
    unsigned int cutime;
    unsigned int cstime;

    FILE *fd;
    char line_buff[1024] = { 0 };
    sprintf(file, "/proc/%d/stat", p);
    fd = fopen(file, "r");
    fgets(line_buff, sizeof(line_buff), fd);

    sscanf(line_buff, "%u", &pid);
    char* q = get_items(line_buff, 14);
    sscanf(q, "%u %u %u %u", &utime, &stime, &cutime, &cstime);
    fclose(fd);
    return (utime + stime + cutime + cstime);
}


unsigned int get_cpu_total_occupy()
{
    FILE *fd;
    char buff[1024] = { 0 };
    unsigned int user;
    unsigned int nice;
    unsigned int system;
    unsigned int idle;

    fd = fopen("/proc/stat", "r");
    fgets(buff, sizeof(buff), fd);
    char name[16];
    sscanf(buff, "%s %u %u %u %u", name, &user, &nice, &system, &idle);

    fclose(fd);
    return (user + nice + system + idle);
}

void Perf::update_cur_process_cpu_rate() {
    unsigned int totalcputime = get_cpu_total_occupy();
    unsigned int procputime = get_cpu_process_occupy(getpid());
     
    if (last_update_  > 0)
    {
        if (time(NULL) - last_update_ > 2)
        {
            sys_info_.cur_process_cpu_rate 
                = (ul_t)(100.0*(procputime - last_process_) / (totalcputime - last_total_)*sys_info_.online_processors);
            last_total_ = totalcputime;
            last_process_ = procputime;
            last_update_ = time(NULL);
        }
    }
    else {
        last_total_ = totalcputime;
        last_process_ = procputime;
        last_update_ = time(NULL);
    }
}

} // namespace core
