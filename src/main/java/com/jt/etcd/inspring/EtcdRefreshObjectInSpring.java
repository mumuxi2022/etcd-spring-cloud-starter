package com.jt.etcd.inspring;

import com.jt.etcd.component.EtcdConfigService;
import com.jt.etcd.listener.UpdatePSListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.GenericScope;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * @author 帅气的景天老师
 * @create 2022/6/21 16:13
 */
public class EtcdRefreshObjectInSpring implements SmartLifecycle, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(EtcdRefreshObjectInSpring.class);

    EtcdConfigService etcdConfigService;

    private RefreshScope scope;

    private ConfigurableApplicationContext applicationContext;

    public EtcdRefreshObjectInSpring(EtcdConfigService etcdConfigService,RefreshScope scope){
        this.etcdConfigService = etcdConfigService;
        this.scope = scope;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void start() {
        etcdConfigService.addAllListener(new UpdatePSListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                scope.refreshAll();
            }
        });
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

}
