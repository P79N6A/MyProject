package com.meituan.control.zookeeper.flwc;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *       RuokData用于描述ruok命令的返回：
 *       ruok命令仅仅询问zk server当前状态、返回很简单：“imok”
 * @content:
 *       固定的: “imok”
 */
public class RuokData {
    private String content = "";

    public RuokData(String content) {
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
        return "RuokData{" +
                "content='" + content + '\'' +
                '}';
    }
}
