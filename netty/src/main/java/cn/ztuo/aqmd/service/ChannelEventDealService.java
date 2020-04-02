/*
 * Copyright (c) 2017-2018 阿期米德 All Rights Reserved.
 * @Author: sanfeng
 * @Date: 2018/3/21 17:17
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng,  2018/3/21 17:17, Create
 */
package cn.ztuo.aqmd.service;

/**
 * <p>Description: </p>
 * 发生连接建立或者断开时的特殊处理服务
 * @Author: sanfeng
 * @Date: 2018/3/21 17:17
 */
public interface ChannelEventDealService {

    /**
     * 处理连接激活请求
     * @param serverIp 服务器端ip
     * @param clientIp 客户端ip
     * @param clientPort 客户端端口
     */
    void dealChannelActive(String serverIp, String clientIp, int clientPort);
    /**
     * 处理连接断开请求
     * @param serverIp 服务器端ip
     * @param clientIp 客户端ip
     * @param clientPort 客户端端口
     */
    void dealChannelDestory(String serverIp, String clientIp, int clientPort);
}