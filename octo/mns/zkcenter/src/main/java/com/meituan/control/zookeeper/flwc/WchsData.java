package com.meituan.control.zookeeper.flwc;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      WchsData用于描述wchs命令的返回：
 *      wchs命令返回关于监听的统计结果，两行数据、类似:
 *      "902 connections watching 774 paths
 *       Total watches:25801"
 * @content:
 *      "902 connections watching 774 paths
 *      Total watches:25801"
 */
public class WchsData {
    private String content = "";

    public WchsData(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "WchsData{" +
                "content='" + content + '\'' +
                '}';
    }
}
