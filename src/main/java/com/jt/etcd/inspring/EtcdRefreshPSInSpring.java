package com.jt.etcd.inspring;

import com.jt.etcd.component.EtcdConfigService;
import com.jt.etcd.listener.UpdatePSListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 帅气的景天老师
 * @create 2022/6/21 16:13
 */
public class EtcdRefreshPSInSpring implements SmartLifecycle, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(EtcdRefreshPSInSpring.class);

    EtcdConfigService etcdConfigService;

    public EtcdRefreshPSInSpring(EtcdConfigService etcdConfigService){
        this.etcdConfigService = etcdConfigService;
    }

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void start() {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        Properties properties = new Properties();
        //默认使用spring.application.name作为dataId去etcd上拿数据
        String dataId = (String) environment.getProperty("spring.application.name");

        if (StringUtils.isEmpty(dataId)) {
            String dir = environment.getProperty("user.dir");
            String[] dirArr = dir.split("\\\\");
            dataId = dirArr[dirArr.length-1];
        }

        String finalDataId = dataId;
        etcdConfigService.addListener(dataId, new UpdatePSListener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                MutablePropertySources propertySources = environment.getPropertySources();
                InputStream configStream = new ByteArrayInputStream(configInfo.getBytes());
                try {
                    properties.load(configStream);
                    ConcurrentHashMap<Object,Object> ch = new ConcurrentHashMap<>();
                    for (Map.Entry entry : properties.entrySet()) {
                        ch.put(entry.getKey(),entry.getValue());
                    }
                    OriginTrackedMapPropertySource source = new OriginTrackedMapPropertySource(finalDataId,ch);
                    //替换配置内容
                    propertySources.replace(finalDataId, source);
                    LOG.info("Refresh PS,dataId={}", finalDataId);
                }catch (Exception e){
                    LOG.error(e.getMessage());
                }
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
