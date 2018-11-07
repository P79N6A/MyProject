package com.sankuai.mtthrift.testSuite.httpinvoke;

import com.meituan.service.mobile.mtthrift.util.json.JacksonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class JsonMapperTest {

    @Test
    public void testClassMapper() throws Exception {
        TestMapperClass obj = genObject();
        String str = JacksonUtils.serialize(obj);
        System.out.println(str);

        TestMapperClass obj1 = JacksonUtils.deserialize(str, TestMapperClass.class);

        Assert.assertEquals(obj, obj1);
    }

    @Test
    public void testBaseClass() {
        AbstractClass obj1 = new ImplClass();
        String str = JacksonUtils.serialize(obj1);
        AbstractClass obj2 = JacksonUtils.deserialize(str, AbstractClass.class);
        Assert.assertEquals(obj1, obj2);

        InterfaceClass obj3 = new ImplClass();
        str = JacksonUtils.serialize(obj3);
        InterfaceClass obj4 = JacksonUtils.deserialize(str, InterfaceClass.class);
        Assert.assertEquals(obj3, obj4);
    }

    @Test
    public void testMapType() {
        Map<TestMapperSubClass, Double> testMap = new HashMap<TestMapperSubClass, Double>();
        TestMapperSubClass subObj1 = new TestMapperSubClass();
        subObj1.setDate(new Date());
        subObj1.setDay(TestMapperSubClass.Day.FRIDAY);
        testMap.put(subObj1, 10.1);
        TestMapperSubClass subObj2 = new TestMapperSubClass();
        subObj2.setDate(new Date());
        subObj2.setDay(TestMapperSubClass.Day.MONDAY);
        testMap.put(subObj2, 12.2);

        String str = JacksonUtils.serialize(testMap);
        Map mapObj = JacksonUtils.deserialize(str, Map.class);

        Assert.assertEquals(testMap, mapObj);
    }

    private TestMapperClass genObject() {
        TestMapperClass obj = new TestMapperClass();
        obj.setNum(10);
        obj.setStr("Hello");

        List<String> list = new ArrayList<String>();
        list.add("Jack");
        list.add("Tom");
        obj.setStrList(list);

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("Jack", 20);
        map.put("Tom", 30);
        obj.setMap(map);

        TestMapperSubClass subObj = new TestMapperSubClass();
        subObj.setDate(new Date());
        subObj.setDay(TestMapperSubClass.Day.FRIDAY);
        obj.setSubClass(subObj);

        List<TestMapperSubClass> objList = new ArrayList<TestMapperSubClass>();
        objList.add(subObj);
        obj.setObjList(objList);

        Map<TestMapperSubClass, TestMapperSubClass> objMap = new HashMap<TestMapperSubClass, TestMapperSubClass>();
        TestMapperSubClass subObj2 = new TestMapperSubClass();
        subObj2.setDate(new Date());
        subObj2.setDay(TestMapperSubClass.Day.MONDAY);
        objMap.put(subObj2, subObj2);
        obj.setObjMap(objMap);
        return obj;
    }
}
