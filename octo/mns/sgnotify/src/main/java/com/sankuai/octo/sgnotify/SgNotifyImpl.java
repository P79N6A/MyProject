package com.sankuai.octo.sgnotify;

import com.sankuai.octo.config.model.ConfigFileRequest;
import com.sankuai.octo.config.model.ConfigFileResponse;
import com.sankuai.octo.sgnotify.comm.FileConfigCmdType;
import com.sankuai.octo.sgnotify.model.ConfigUpdateEvent;
import com.sankuai.octo.sgnotify.model.ConfigUpdateResult;
import com.sankuai.octo.sgnotify.notify.DynamicNotify;
import com.sankuai.octo.sgnotify.service.SgNotify;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class SgNotifyImpl implements SgNotify.Iface {
    private final static Logger LOG = LoggerFactory.getLogger(SgNotifyImpl.class);

    @Autowired
    private DynamicNotify dynamicNotify;

    @Override
    public String Notify(int cmdType, String sData) throws TException {
        //没有被使用的接口
        return "";
    }

    @Override
    public ConfigUpdateResult notifyConfig(ConfigUpdateEvent event) throws TException {
        ConfigUpdateResult ret = new ConfigUpdateResult();

        if (null != event && event.isSetChanged()) {
            Map<String, Integer> codes = dynamicNotify.batchNotify(event.getChanged());
            ret.setCodes(codes);
        }
        return ret;
    }

    @Override
    public ConfigFileResponse distributeConfigFile(ConfigFileRequest request) throws TException {
        return NotifyImpl.distributeOrEnableFileConfig(request, FileConfigCmdType.DISTRIBUTE);
    }


    @Override
    public ConfigFileResponse enableConfigFile(ConfigFileRequest request) throws TException {
        return NotifyImpl.distributeOrEnableFileConfig(request, FileConfigCmdType.ENABLE);
    }
}
