//package com.jt.etcd.util;
//
//import com.jt.etcd.config.EtcdConfigProperties;
//import com.jt.etcd.constants.EtcdConfigConstants;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.context.properties.bind.Bindable;
//import org.springframework.boot.context.properties.bind.Binder;
//import org.springframework.core.ResolvableType;
//import org.springframework.core.env.ConfigurableEnvironment;
//
///**
// * @author 帅气的景天老师
// * @create 2023/3/16 17:55
// */
//public class EtcdConfigPropertiesUtil {
//
//    private static final Logger LOG = LoggerFactory.getLogger(EtcdConfigPropertiesUtil.class);
//
//    public static EtcdConfigProperties buildEtcdConfigProperties(ConfigurableEnvironment environment) {
//        //这哥们就是把我们前面设置的etcd服务端的地址等参数进行类型转换,然后将属性绑定到对象,Map,List等类型上
//        EtcdConfigProperties etcdConfigProperties = new EtcdConfigProperties();
//        //从环境变量里面拿到装配置的容器PropertySources，然后封装到Binder里面
//        Binder binder = Binder.get(environment);
//        //拿到目标对象
//        ResolvableType type = ResolvableType.forClass(EtcdConfigProperties.class);
//        Bindable<?> target = Bindable.of(type).withExistingValue(etcdConfigProperties);
//        //下面的操作就是把PS里面的以etcd.config开头的配置绑定成EtcdConfigProperties对象
//        binder.bind(EtcdConfigConstants.CONFIG_PROPERTIES_PREFIX, target);
//        LOG.info("etcdConfigProperties={}", etcdConfigProperties);
//        return etcdConfigProperties;
//    }
//
//}
