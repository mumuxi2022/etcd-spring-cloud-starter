package com.jt.etcd.inspring;

import com.jt.etcd.component.EtcdConfigService;
import com.jt.etcd.listener.UpdateObjectListener;
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
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Properties;

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
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        //默认使用spring.application.name作为dataId去etcd上拿数据
        String dataId = (String) environment.getProperty("spring.application.name");

        if (StringUtils.isEmpty(dataId)) {
            String dir = environment.getProperty("user.dir");
            String[] dirArr = dir.split("\\\\");
            dataId = dirArr[dirArr.length-1];
        }

        String finalDataId = dataId;
        etcdConfigService.addListener(finalDataId,new UpdateObjectListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                scope.refreshAll();
                LOG.info("Refresh Bean,dataId={}", finalDataId);
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
