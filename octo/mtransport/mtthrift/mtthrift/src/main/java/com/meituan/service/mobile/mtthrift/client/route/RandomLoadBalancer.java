package com.meituan.service.mobile.mtthrift.client.route;


import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import org.aopalliance.intercept.MethodInvocation;

import java.util.List;
import java.util.Random;

/**
 * User: YangXuehua
 * Date: 13-8-9
 * Time: 下午6:56
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    private final Random random = new Random();

    public RandomLoadBalancer(int slowStartSeconds) {
    }

    @Override
    public ServerConn doSelect(List<ServerConn> serverList, MethodInvocation methodInvocation) {
        if (serverList.size() == 0)
            return null;

        int length = serverList.size(); // 总个数
        double[] weightAccumulate = new double[length];
        double totalWeight = 0; // 总权重
        boolean sameWeight = true; // 权重是否都一样
        double lastWeight = -1;
        for (int i = 0; i < length; i++) {
            // 获取权重
            double weight = getWeight(serverList.get(i));
            // 累计总权重
            totalWeight += weight;
            weightAccumulate[i] = totalWeight;
            // 判断所有权重是否一样
            if (sameWeight && i > 0 && Double.compare(weight, lastWeight) != 0) {
                sameWeight = false;
            }
            lastWeight = weight;
        }
        if (!sameWeight && Double.compare(totalWeight, 0) > 0) {
            // 如果权重不相同且权重大于0则按总权重数随机
            double offset = random.nextDouble() * totalWeight;
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < length; i++) {
                double weightA = weightAccumulate[i];
                if (Double.compare(offset, weightA) < 0) {
                    return serverList.get(i);
                }
            }
        }
        // 如果权重相同或权重为0则均等随机
        return serverList.get(random.nextInt(length));
    }

}
