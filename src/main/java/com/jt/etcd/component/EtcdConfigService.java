package com.jt.etcd.component;

import com.jt.etcd.listener.Listener;

/**
 * @author 帅气的景天老师
 * @create 2023/3/16 18:12
 */
public interface EtcdConfigService {
    /**
     * 从配置中心获取配置
     *
     * @param dataId 配置id
     * @return 配置中心的配置
     * @throws Exception 配置异常
     */
    String getConfig(String dataId) throws Exception;

    /**
     * 监听dataId的配置变化
     *
     * @param dataId   配置id
     * @param listener 配置变化监听者
     */
    void addListener(String dataId, Listener listener);

    /**
     * 监听所有dataId的配置变化
     *
     * @param listener 配置变化监听者
     */
    void addAllListener(Listener listener);
}
