package com.sankuai.mtthrift.testSuite.httpinvoke;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMapperClass {

    private int num;
    private String str;
    private List<String> strList;
    private HashMap<String, Integer> map;
    private TestMapperSubClass subClass;
    private List<TestMapperSubClass> objList;
    private Map<TestMapperSubClass, TestMapperSubClass> objMap;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public List<String> getStrList() {
        return strList;
    }

    public void setStrList(List<String> strList) {
        this.strList = strList;
    }

    public HashMap<String, Integer> getMap() {
        return map;
    }

    public void setMap(HashMap<String, Integer> map) {
        this.map = map;
    }

    public TestMapperSubClass getSubClass() {
        return subClass;
    }

    public void setSubClass(TestMapperSubClass subClass) {
        this.subClass = subClass;
    }

    public List<TestMapperSubClass> getObjList() {
        return objList;
    }

    public void setObjList(List<TestMapperSubClass> objList) {
        this.objList = objList;
    }

    public Map<TestMapperSubClass, TestMapperSubClass> getObjMap() {
        return objMap;
    }

    public void setObjMap(Map<TestMapperSubClass, TestMapperSubClass> objMap) {
        this.objMap = objMap;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public static void main(String[] args) {
        Field[] fields = TestMapperClass.class.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field.getGenericType().toString());
        }
    }
}
