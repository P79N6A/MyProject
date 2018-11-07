package com.sankuai.octo.test.utils;

import com.taobao.tair3.client.Result;
import com.taobao.tair3.client.TairClient;
import com.taobao.tair3.client.error.TairException;
import com.taobao.tair3.client.error.TairFlowLimit;
import com.taobao.tair3.client.error.TairRpcError;
import com.taobao.tair3.client.error.TairTimeout;
import com.taobao.tair3.client.impl.DefaultTairClient;

import java.io.UnsupportedEncodingException;

public class TairHelper {
    private static short area = 5;
    private static TairClient.TairOption opt = new TairClient.TairOption(5000);

    private static class Holder {
        private static String master = "10.64.23.204:5198";
        private static String group = "group_1";
        private static final DefaultTairClient instance = new DefaultTairClient();
        private static volatile Boolean inited = false;

        private static DefaultTairClient get() {
            if (!inited) {
                synchronized (inited) {
                    if (!inited) {
                        Holder.instance.setMaster(master);
                        Holder.instance.setGroup(group);
                        try {
                            Holder.instance.init();
                            inited = true;
                            System.out.println("tair inited...");
                            Runtime.getRuntime().addShutdownHook(new Thread() {
                                @Override
                                public void run() {
                                    System.out.println("shutdown tair close...");
                                    Holder.instance.close();
                                }
                            });
                        } catch (TairException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return Holder.instance;
        }
    }

    public static DefaultTairClient client() {
        return Holder.get();
    }

    public static <T> void put(String key, T value) {
        put(key, JsonHelper.write(value));
    }

    public static <T> T getObject(String key, Class<T> clazz) {
        return JsonHelper.read(getValue(key), clazz);
    }

//    private static <T> T getObject(String key, TypeReference<T> clazz) {
//        return JsonHelper.read(get(key), clazz);
//    }

    public static void put(String key, String value) {
        try {
            put(key, value.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void put(String key, byte[] value) {
        try {
            Result<Void> result = client().put(area, key.getBytes("utf-8"), value, opt);
            if (!result.isSuccess()) {
                System.out.println(result.getCode());
            }
        } catch (TairRpcError tairRpcError) {
            tairRpcError.printStackTrace();
        } catch (TairFlowLimit tairFlowLimit) {
            tairFlowLimit.printStackTrace();
        } catch (TairTimeout tairTimeout) {
            tairTimeout.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getValue(String key) {
        try {
            Result<byte[]> result = client().get(area, key.getBytes("utf-8"), opt);
            if (result.isSuccess()) {
                return result.getResult();
            } else {
                System.out.println(result.getCode());
            }
        } catch (TairRpcError tairRpcError) {
            tairRpcError.printStackTrace();
        } catch (TairFlowLimit tairFlowLimit) {
            tairFlowLimit.printStackTrace();
        } catch (TairTimeout tairTimeout) {
            tairTimeout.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String get(String key) {
        try {
            return new String(getValue(key), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
