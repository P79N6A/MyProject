package com.sankuai.msgp.common.model;


/**
 * Created by yves on 17/1/4.
 */
public enum Business {
    platform(0, "到店餐饮"),
    engineering(1, "技术工程及基础数据平台"),
    movie(2, "猫眼电影"),
    @Deprecated
    creative(3, "创新业务部"),
    hotel(4, "酒店旅游事业群"),
    waimai(5, "外卖配送"),
    //云计算部合并到技术工程及基础数据平台
    @Deprecated
    cloud(6, "云计算部"),
    finance(7, "金融服务平台"),
    //支付平台合并到金融服务平台
    @Deprecated
    pay(8, "支付平台部"),
    @Deprecated
    canting(9, "智能餐厅部"),
    it(10, "企业平台"),
    adplatform(11, "广告平台"),
    platformbg(12, "平台事业群"),
    storebg(13, "到店综合事业群"),
    caterenvbg(14, "餐饮生态"),
    shanghai(50, "上海"),
    other(100, "其他");


    Business(int id, String name) {
        this.name = name;
        this.id = id;
    }

    private String name;
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static int getBusinessIdByName(String name) {
        for (Business business : Business.values()) {
            if (business.getName().equalsIgnoreCase(name)) {
                return business.getId();
            }
        }
        return Business.other.getId();
    }

    public static String getBusinessNameById(int id) {
        for (Business business : Business.values()) {
            if (business.getId() == id) {
                return business.getName();
            }
        }
        return Business.other.getName();
    }
}
