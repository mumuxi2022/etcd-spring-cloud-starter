package com.jt.etcd.config;

import com.jt.etcd.component.EtcdComponent;
import com.jt.etcd.component.EtcdConfigService;
import com.jt.etcd.component.EtcdConfigServiceImpl;
import com.jt.etcd.inspring.EtcdPropertySourceInSpring;
import com.jt.etcd.inspring.EtcdRefreshObjectInSpring;
import com.jt.etcd.inspring.EtcdRefreshPSInSpring;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 帅气的景天老师
 * @create 2023/3/16 17:31
 */
@Configuration
@EnableConfigurationProperties(value = EtcdConfigProperties.class)
public class EtcdAutoConfiguration{

    @Bean
    public EtcdConfigProperties etcdConfigProperties(){
        return new EtcdConfigProperties();
    }

    @Bean
    public EtcdComponent etcdComponent(EtcdConfigProperties etcdConfigProperties){
        return new EtcdComponent(etcdConfigProperties);
    }

    @Bean
    public EtcdConfigService etcdConfigService(EtcdComponent etcdComponent){
        return new EtcdConfigServiceImpl(etcdComponent);
    }

    @Bean
    @ConditionalOnMissingBean(RefreshScope.class)
    public static RefreshScope refreshScope() {
        return new RefreshScope();
    }

    @Bean
    public EtcdPropertySourceInSpring etcdPropertySourceInSpring(EtcdConfigService etcdConfigService){
        return new EtcdPropertySourceInSpring(etcdConfigService);
    }

    @Bean
    public EtcdRefreshObjectInSpring etcdRefreshObjectInSpring(EtcdConfigService etcdConfigService,
                                                               RefreshScope refreshScope){
        return new EtcdRefreshObjectInSpring(etcdConfigService,refreshScope);
    }

    @Bean
    public EtcdRefreshPSInSpring etcdRefreshPSInSpring(EtcdConfigService etcdConfigService){
        return new EtcdRefreshPSInSpring(etcdConfigService);
    }

}
