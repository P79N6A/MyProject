package com.sankuai.octo.statistic.metrics;

import com.sankuai.octo.statistic.model.MetricKey;
import com.sankuai.octo.statistic.model.PerfProtocolType;
import com.sankuai.octo.statistic.model.StatSource;
import org.junit.Test;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BitSetTest {

    public static void calcSize(Object o) {
        long memShallow = com.candybon.memory.MemoryObserver.shallowSizeOf(o);
        long memDeep = com.candybon.memory.MemoryObserver.deepSizeOf(o);
        System.out.printf("%s, shallow=%d, deep=%d%n", o.getClass().getSimpleName(), memShallow, memDeep);
    }
    public static void main(String[] args) {
        sushu(19999900,20000000);
//        set();
    }
    public static void set() {

        MetricKey key = null;
        Random random = new Random();
        int maxSize = 2000000;
        int setCount = 50;
        int maxCount = maxSize;
        String appkey ="com.sankuai.inf.testFalcon6";
        String spannamePrefix = "testMethod";
        String localHostPrefix = "testHost";
        String remoteAppKeyPrefix = "testRemoteKey";
        String remoteHostPrefix = "testRemoteHost";
        BitSet bitSet = new BitSet(maxSize);
        Set set = new HashSet<Integer>(2000000);
        for (int i = 0; i < maxSize; i++) {
            String spanname =spannamePrefix+ random.nextInt(2000000);
            String localHost =localHostPrefix + random.nextInt(2000000);
            String remoteAppKey =remoteAppKeyPrefix +  random.nextInt(2000000);
            String remoteHost =remoteHostPrefix+ random.nextInt(2000000);
            StatSource  source = StatSource.Client;
            PerfProtocolType perfProtocolType = PerfProtocolType.HTTP;
            String infraName ="";
            key= new MetricKey(appkey,spanname,localHost,remoteAppKey,remoteHost,source,perfProtocolType,infraName);
            int code = (key.hashCode() & 0x7fffffff) % 1975663 ;
            bitSet.set(code);
            set.add(code);
//            System.out.println((key.hashCode() & 0x7fffffff)+"\t"+code);
        }
        System.out.println("cardinality:"+bitSet.cardinality());
        System.out.println("code:"+set.size());
//        calcSize(bitSet);
    }

    static void sushu(int start ,int end){
        int i,n,k=0;
        for (n = start; n<=end; n++) {     //3~100的所有数
            i=2;
            while (i<n) {
                if (n%i==0)  break;  //若能整除说明n不是素数，跳出当前循环
                i++;
            }


            if (i==n) {     //如果i==n则说明n不能被2~n-1整除，是素数
                k++;             //统计输出数的个数
                System.out.print(i+ "\t ");
                if (k %6==0)    //每输出5个则换行
                    System.out.println();
            }
        }
    }
}
