package com.meituan.control.zookeeper.flwc;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      CrstData用于描述crst命令的返回:
 *      crst命令为写操作、将客户端连接的统计信息重置、所以返回很简单: “Connection stats reset.”
 * @content:
 *      执行重置后的返回、很简单、为“Connection stats reset.”
 */
public class CrstData {
    private String content = "";

    public CrstData(String content) {
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
        return "CrstData{" +
                "content='" + content + '\'' +
                '}';
    }
}
