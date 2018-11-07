package com.sankuai.inf.octo.mns.listener;

import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;

import java.util.List;

/**
 * Created by lhmily on 05/26/2016.
 */
public interface IServiceListChangeListener {
    void changed(ProtocolRequest req,
                       List<SGService> oldList,
                       List<SGService> newList,
                       List<SGService> addList,
                       List<SGService> deletedList,
                       List<SGService> modifiedList);
}
