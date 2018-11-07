package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.model.SGAgentClient.ClientType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lhmily on 06/21/2016.
 */
public class AgentClientFactoryTest {
    @Test
    public void testConnection() {
        checkConnection(ClientType.mcc,true);
        checkConnection(ClientType.mns,true);
        checkConnection(ClientType.multiProto,true);
        checkConnection(ClientType.temp,false);
    }

    private void checkConnection(ClientType type, boolean isLong) {
        SGAgentClient client = AgentClientFactory.borrowClient(type);
        Assert.assertNotNull(client);
        Assert.assertNotNull(client.getTSocket());

        AgentClientFactory.returnClient(client);
        Assert.assertNotNull(client);
        if (isLong) {
            Assert.assertNotNull(client.getTSocket());
        } else {
            Assert.assertNull(client.getTSocket());
        }
        Assert.assertEquals(type, client.getType());
    }
}
