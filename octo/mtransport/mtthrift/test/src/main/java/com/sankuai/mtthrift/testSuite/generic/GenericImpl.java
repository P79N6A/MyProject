package com.sankuai.mtthrift.testSuite.generic;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GenericImpl implements Generic.Iface {
    private static final Logger logger = LoggerFactory.getLogger(GenericImpl.class);

    @Override
    public void echo1() throws TException {
        logger.info("echo1");
    }

    @Override
    public String echo2(String message) throws TException {
        logger.info("echo2");
        return message;
    }

    @Override
    public SubMessage echo3(SubMessage message) throws TException {
        logger.info("echo3");
        return message;
    }

    @Override
    public List<SubMessage> echo4(List<SubMessage> messages) throws TException {
        return messages;
    }

    @Override
    public Map<SubMessage, SubMessage> echo5(Map<SubMessage, SubMessage> messages) throws TException {
        logger.info("echo5");
        return messages;
    }

    @Override
    public Message echo6(Message message) throws TException {
        logger.info("echo6");
        return message;
    }

    @Override
    public SubMessage echo7(String strMessage, SubMessage message) throws TException {
        logger.info("echo7");
        logger.info("string message: {}", strMessage);
        return message;
    }

    @Override
    public void echo8() throws GenericException, TException {
        throw new GenericException("generic error");
    }

    @Override
    public byte echo9(byte param1, int param2, long param3, double param4) throws TException {
        return param1;
    }
}
