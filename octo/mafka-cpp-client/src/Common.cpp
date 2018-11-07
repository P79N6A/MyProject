#include "Common.h"
#include "StringUtil.h"

#include <unistd.h>
#include <sys/syscall.h>


#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <arpa/inet.h>
#include <map>
#include <string>

#include "Log.h"

namespace mafka
{

	int GetTid()
	{
		return syscall(SYS_gettid);
	}

	void GetPublicIPAddress(std::string& ip_address)
	{
		int MAXINTERFACES=16;  
		char *ip = NULL;  
		int fd, intrface;    
		struct ifreq buf[MAXINTERFACES];    
		struct ifconf ifc;    

		std::string private_address;
		if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) >= 0)    
		{    
			ifc.ifc_len = sizeof(buf);    
			ifc.ifc_buf = (caddr_t)buf;    
			if (!ioctl(fd, SIOCGIFCONF, (char *)&ifc))    
			{    
				intrface = ifc.ifc_len / sizeof(struct ifreq);    

				while (intrface-- > 0)    
				{    
					if (!(ioctl (fd, SIOCGIFADDR, (char *) &buf[intrface])))    
					{    
						ip=(inet_ntoa(((struct sockaddr_in*)(&buf[intrface].ifr_addr))->sin_addr));    
						if( NULL != ip)
						{
							if(StringUtil::StartsWith(ip, "10."))
							{
								ip_address = ip;
								break;
							}
							if(StringUtil::StartsWith(ip, "172."))
							{
								private_address = ip;
							}
						} else {
							ERROR("ip get null\n");
						}
					}                        
				}  
			}    
			close (fd);    


			if(ip_address.empty())
			{
				ip_address = private_address;
			}
		}


	}

	void setStringByMap(std::map<std::string, std::string> srcMap, std::string& tarStr) {
		std::map<string, string>::const_iterator it;
		for(it = srcMap.begin(); it != srcMap.end(); ++it) {
			tarStr += it->first + ":" + it->second + ",";
		}
	}
}

