local appkey = {{APPKEY}}
local providerAppkey = {{PROVIDERAPPKEY}}
local degradeRatio = {{DEGRADERATIO}}
local degradeStrategy = {{DEGRADESTRATEGY}}
local degradeRedirect = {{DEGRADEREDIRECT}}

ngx.log( ngx.DEBUG, string.format("[=HLB=] appkey=%s  providerAppkey=%s  degradeRatio=%s  degradeStrategy=%s  degradeRedirect=%s", appkey,providerAppkey, tostring(degradeRatio), degradeStrategy, degradeRedirect) )

if not ( string.upper(degradeStrategy) == "DROP") then
    ngx.log( ngx.INFO, "[=HLB=] WTF degradeStrategy not DROP")
    ngx.var.upstream_name = appkey
    return
end

math.randomseed(os.time())
local randomNum = math.random()
ngx.log( ngx.DEBUG, "[=HLB=] randomNum= " .. tostring(randomNum) .. " degradeRatio= " .. tostring(degradeRatio))
if (randomNum < degradeRatio) then
    ngx.log( ngx.WARN, "[=HLB=] DEGRADED")
    ngx.status = ngx.HTTP_SERVICE_UNAVAILABLE
    ngx.var.mt_hlb_status = "Service has been degraded"
    ngx.say("Service has been degraded")
    return ngx.exit(ngx.status)
end

ngx.log( ngx.DEBUG, "[=HLB=] NOT DEGRADED")
ngx.var.upstream_name = appkey
