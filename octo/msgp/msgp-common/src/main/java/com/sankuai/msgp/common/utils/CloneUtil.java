package com.sankuai.msgp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by emma on 2017/9/21.
 */
public class CloneUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloneUtil.class);

    public static <T extends Serializable> T clone(T obj) {
        T clonedObj = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            clonedObj = (T) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            LOGGER.error("Deep copy fail", e);
        }
        return clonedObj;
    }
}
