#!/bin/bash

set j=0
for((j=0;j<10;))

 do
	curl -i -X POST -H 'Content-type':'application/json' -d '{"remoteAppkey":"com.sankuai.octo.tmy","protocol":"thrift","serviceList":[{"ip":"10.4.227.244","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.245","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.246","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"port":5266,"protocol":"thrift","weight":40,"status":2,"lastUpdateTime":1511167765,"extend":"OCTO|slowStartSeconds:180","fweight":10,"appkey":"com.sankuai.octo.tmy","heartbeatSupport":2,"env":3,"serviceInfo":{"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService":{"unifiedProto":1},"xxx":{"unifiedProto":2}},"version":"mtthrift-v1.8.2","ip":"10.4.245.3","serverType":0,"role":0}]}' "http://10.4.227.244:5267/api/mns/provider/update"

    curl -i -X POST -H 'Content-type':'application/json' -d '{"remoteAppkey":"com.sankuai.octo.tmy","protocol":"thrift","serviceList":[{"ip":"10.4.227.244","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.245","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.246","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"port":5266,"protocol":"thrift","weight":40,"status":2,"lastUpdateTime":1511167765,"extend":"OCTO|slowStartSeconds:180","fweight":10,"appkey":"com.sankuai.octo.tmy","heartbeatSupport":2,"env":3,"serviceInfo":{"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService":{"unifiedProto":1},"xxx":{"unifiedProto":2}},"version":"mtthrift-v1.8.2","ip":"10.4.245.3","serverType":0,"role":0}]}' "http://10.4.227.244:5267/api/mns/provider/test"

    curl -i -X POST -H 'Content-type':'application/json' -d '{"remoteAppkey":"com.sankuai.octo.tmy","protocol":"thrift","serviceList":[{"ip":"10.4.227.244","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.245","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.246","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"port":5266,"protocol":"thrift","weight":40,"status":2,"lastUpdateTime":1511167765,"extend":"OCTO|slowStartSeconds:180","fweight":10,"appkey":"com.sankuai.octo.tmy","heartbeatSupport":2,"env":3,"serviceInfo":{"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService":{"unifiedProto":1},"xxx":{"unifiedProto":2}},"version":"mtthrift-v1.8.2","ip":"10.4.245.3","serverType":0,"role":0}]}' "http://10.4.245.3:5267/api/mns/provider/unknow"

    curl -i -X POST -H 'Content-type':'application/json' -d '{"remoteAppkey":"com.sankuai.octo.tmy","protocol":"thrift","serviceList":[{"ip":"10.4.227.247.344","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.245","port":5266,"protocol":"thrift","serverType":1,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.246.345","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"port":5266,"protocol":"thrift","status":2,"lastUpdateTime":1511167765,"extend":"OCTO|slowStartSeconds:180","appkey":"com.sankuai.octo.test","heartbeatSupport":2,"env":3,"serviceInfo":{"com.sankuai.octo.mnsc.idl.thrift.service.MNSCacheService":{"unifiedProto":1},"xxx":{"unifiedProto":2}},"version":"mtthrift-v1.8.2","ip":"10.4.245.3","serverType":0,"role":0}]}' "http://10.4.245.3:5267/api/mns/provider/replace"

    curl -i -X POST -H 'Content-type':'application/json' -d '{"remoteAppkey":"com.sankuai.octo.tmy","protocol":"thrift","serviceList":[{"ip":"10.4.227.244","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"},{"ip":"10.4.227.234","port":5266,"protocol":"thrift","serverType":0,"appkey":"com.sankuai.octo.tmy"}]}' "http://10.4.227.244:5267/api/mns/provider/get"
  let "j=j+1"
	sleep 1

done
echo "finish the http flush node test"


