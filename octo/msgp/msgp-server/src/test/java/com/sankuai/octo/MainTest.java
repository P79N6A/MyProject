package com.sankuai.octo;

import java.util.BitSet;

public class MainTest {
    public static void main(String[] args) {
        BitSet bitSet=new BitSet(10000000);
        for(int i=0;i<100;i++){
           int v = (int)(i*Math.random());
            bitSet.set(v,true);
        }
        System.out.println(bitSet.cardinality());
    }
}
