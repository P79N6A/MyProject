package com.sankuai.meituan.config.service
import com.alibaba.fastjson.JSON
import com.sankuai.meituan.borp.BorpService
import com.sankuai.meituan.borp.vo.Action
import com.sankuai.meituan.borp.vo.BorpRequest
import com.sankuai.meituan.config.test.NewBaseTest
import org.junit.Test

import javax.annotation.Resource

class OperatorRecordServiceTest extends NewBaseTest {
    @Resource
    BorpService borpService

    @Test
    void testGet() {
        def operation = borpService.getOperationById("f0fbc7b6-3fd0-4efa-a6b3-c31e158dd584")
        println(JSON.toJSONString(operation))
//        println(JSON.toJSONString(borpService.getActionsByOperationIds(Lists.newArrayList(operation.operationId), Lists.newArrayList(PropertyValue.class.name), Lists.newArrayList())))
        println(JSON.toJSONString(borpService.getByBorpRequest(BorpRequest.builder()
                .mustEq("operationId", operation.getOperationId()).size(30)
                .build(), Action.class)))
    }
}
