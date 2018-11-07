package com.sankuai.octo.demo;

import com.sankuai.octo.config.model.ConfigData;
import com.sankuai.octo.sandbox.thrift.model.Sandbox;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangxi@meituan.com
 * @version 1.0.0
 * @octo.appkey com.sankuai.inf.test
 * @permission 公开
 * @staus 可用
 * @link http://wiki.sankuai.com/octo/demo
 */
public interface UserService {

    /**
     * @param name 用户名
     * @return
     * @throws TApplicationException
     * @name 创建用户
     * @desc 根据特定名字创建用户
     */
    public User createUser(String name) throws TApplicationException;

    /**
     * @param user
     * @return
     * @throws TException
     * @name 更新用户
     * @desc 更新用户信息
     */
    public User updateUser(User user) throws TException;

    /**
     * @param userId  用户ID
     * @param message 消息内容
     * @return
     * @throws MessageException
     * @name 发送消息
     * @desc 给某个用户发送一条消息
     */
    public int sendMessage(Integer userId, String message) throws com.sankuai.octo.demo.MessageException;

    /**
     * @param userId 用户ID
     * @throws IllegalStateException  用户不存在
     * @throws IllegalAccessException
     * @group 好友功能
     * @name 加好友
     * @desc 申请与某个用户成为好友
     */
    public List<User> makeFriend(Integer userId) throws IllegalStateException, IllegalAccessException;

    /**
     * @param users 用户列表
     * @return
     * @name 示例容器
     */
    public Set<User> diff(Map<String, User> users);

    /**
     * @param sandbox
     * @return
     * @group 测试
     * @name 创建沙箱
     */
    public int createSandbox(Sandbox sandbox);

    /**
     * @param appkey 服务标识
     * @return
     * @group 测试
     * @name 获取配置
     */
    public ConfigData getConfig(String appkey);
}
