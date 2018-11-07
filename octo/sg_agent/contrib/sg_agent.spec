Name:       sg_agent
Version:    0.3
Release:    1
Summary:    sg_agent

License:    MT
URL:        http://git.sankuai.com/projects/INF/repos/octo/browse/sg_agent
Source0:    %{name}.tar.gz
BuildRoot:  %{_tmppath}/%{name}-root

BuildRequires:  libzookeeper-devel
Requires:       libzookeeper

%description
sg_agent

%prep
%setup -n %{name}

# sg_agent.conf
%{__cat} <<'EOF' > sg_agent.conf
#configs of sg_agent

#log_path of the sg_agent
log_path=/var/sankuai/logs

#listen client port
client_port=5266

#the number of process that will start
process_num=1

#zookeeper host configs
zk_host=10.64.30.199:2181,10.64.23.173:2181,10.64.29.222:2181
zk_path=/mns/sankuai/prod/
timeout=30000
retry=5

#log configs
#log mod is use flume the agent will send logs to local flume agent
#remote will send logs to remote logserver
#eg: log_mod=[flume/remote]

log_mod=remote

#iflog use flume
flume_host=192.168.11.125
flume_port=4252
flume_category=mtrace
#else use remote
remote_log_appkey=com.sankuai.inf.logCollector
#endlog.
EOF

# run script
%{__cat} <<'EOF' > run
#!/bin/bash

ulimit -c unlimited
cd /opt/meituan/apps/sg_agent
exec setuidgid sankuai /opt/meituan/apps/sg_agent/sg_agent >> /var/sankuai/logs/sg_agent.log 2>&1
EOF

%build
%{__make}

%install
rm -rf %{buildroot}

install -D -m 644 sg_agent.conf %{buildroot}/opt/meituan/apps/sg_agent/sg_agent.conf
install -D -m 755 sg_agent %{buildroot}/opt/meituan/apps/sg_agent/sg_agent
install -D -m 755 run %{buildroot}/opt/meituan/apps/sg_agent/run

%clean
rm -rf %{buildroot}

%pre
#!/bin/bash
set -e

[ -e /service/sg_agent ] && svc -d /service/sg_agent

exit 0

%preun
#!/bin/bash
set -e

# remove or erase
if [ "$1" = "0" ]; then
    [ -e /service/sg_agent ] && svc -d /service/sg_agent
fi

exit 0

%post
#!/bin/bash
set -e

# first install
if [ "$1" = "1" ]; then
    [ -e /service/sg_agent ] || /bin/ln -s /opt/meituan/apps/sg_agent /service/sg_agent
    svc -u /service/sg_agent
fi

# upgrade
if [ "$1" = "2" ]; then
    [ -e /service/sg_agent ] && svc -u /service/sg_agent
fi

exit 0

%postun
#!/bin/bash
set -e

# remove or erase
if [ "$1" = "0" ]; then
    [ -e /service/sg_agent ] && rm -f /service/sg_agent
    [ -d /opt/meituan/apps/sg_agent/supervise ] && rm -rf /opt/meituan/apps/sg_agent/supervise
    [ -f /opt/meituan/apps/sg_agent/down ] && rm -f /opt/meituan/apps/sg_agent/down
    kill $(ps auxf | grep 'supervise sg_agent' | grep -v grep | awk '{print $2}')
fi

exit 0

%files
%defattr(0644, sankuai, sankuai, 0755)
%dir /opt/meituan/apps
%dir /opt/meituan/apps/sg_agent
%config(noreplace) /opt/meituan/apps/sg_agent/sg_agent.conf
%attr(0755, sankuai, sankuai) /opt/meituan/apps/sg_agent/sg_agent
%attr(0755, sankuai, sankuai) /opt/meituan/apps/sg_agent/run

%changelog
* Fri Jan 22 2015 Zhixian Chen <chenzhixian@meituan.com> 0.3-1
- fix dir mode

* Wed Jan 14 2015 Zhixian Chen <chenzhixian@meituan.com> 0.3-1
- Init package sg_agent
