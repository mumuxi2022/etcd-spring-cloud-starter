package com.jt.etcd.inspring;

import com.jt.etcd.component.EtcdConfigService;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 帅气的景天老师
 * @create 2022/6/21 14:03
 */
public class EtcdPropertySourceInSpring implements SmartLifecycle , ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(EtcdPropertySourceInSpring.class);

    EtcdConfigService etcdConfigService;

    private ConfigurableApplicationContext applicationContext;

    public EtcdPropertySourceInSpring(EtcdConfigService etcdConfigService){
        this.etcdConfigService = etcdConfigService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void start() {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        MutablePropertySources destination = environment.getPropertySources();
        Properties properties = new Properties();
        try {
            //默认使用spring.application.name作为dataId去etcd上拿数据
            String dataId = (String) environment.getProperty("spring.application.name");

            if (StringUtils.isEmpty(dataId)) {
                String dir = environment.getProperty("user.dir");
                String[] dirArr = dir.split("\\\\");
                dataId = dirArr[dirArr.length-1];
            }

            //从配置中心加载配置内容
            String config = etcdConfigService.getConfig(dataId);
            if (StringUtils.isEmpty(config)) {
                throw new RuntimeException("config is null,dataId=" + dataId);
            }
            InputStream configStream = new ByteArrayInputStream(config.getBytes());
            properties.load(configStream);
            ConcurrentHashMap<Object,Object> ch = new ConcurrentHashMap<>();
            for (Map.Entry entry : properties.entrySet()) {
                ch.put(entry.getKey(),entry.getValue());
            }
            OriginTrackedMapPropertySource source = new OriginTrackedMapPropertySource(dataId,ch);
            destination.addLast(source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
