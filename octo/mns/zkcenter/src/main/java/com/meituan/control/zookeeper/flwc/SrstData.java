package com.meituan.control.zookeeper.flwc;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      SrstData用于描述srst命令的返回：
 *      srst命令将zk server的统计项重置、返回很简单：“Server stats reset.”
 * @content:
 *      固定的：“Server stats reset.”
 */
public class SrstData {
    private String content = "";

    public SrstData(String content) {
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
        return "SrstData{" +
                "content='" + content + '\'' +
                '}';
    }
}
