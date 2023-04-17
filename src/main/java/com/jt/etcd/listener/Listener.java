package com.jt.etcd.listener;

/**
 * @author 帅气的景天老师
 * @create 2023/3/16 18:14
 */
public interface Listener {

    /**
     * 配置内容变更通知
     *
     * @param configInfo 配置内容
     */
    void receiveConfigInfo(String configInfo);
}
