package com.jt.etcd.component;

import com.jt.etcd.config.EtcdConfigProperties;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.watch.WatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author 帅气的景天老师
 * @create 2023/3/16 17:38
 */
public class EtcdComponent implements EnvironmentAware, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EtcdComponent.class);

    public static final String BEAN_NAME = "etcdComponent";

    private ConfigurableEnvironment environment;
    private Client client;
    private EtcdConfigProperties etcdConfigProperties;
    public EtcdComponent(EtcdConfigProperties etcdConfigProperties){
        this.etcdConfigProperties = etcdConfigProperties;
    }



    //通过jetcd客户端去拿数据，然后返回String类型
    public String getValue(String key) throws ExecutionException, InterruptedException {
        String value = "";
        //拿到kv客户端
        KV kvClient = client.getKVClient();
        ByteSequence byteSequence = ByteSequence.from(key, StandardCharsets.UTF_8);

        //根据key去拿value
        GetResponse getResponse = kvClient.get(byteSequence).get();
        if (getResponse.getKvs().size() > 0) {
            KeyValue keyValue = getResponse.getKvs().get(0);
            value = Optional.ofNullable(keyValue.getValue()).map(v -> v.toString(StandardCharsets.UTF_8)).orElse("");

        }
        return value;
    }


    //这个方法麻烦一点，传的参数一个是key值，还有一个是回调方法
    //它完成的就是根据key完成watch注册，同时如果etcd-server端数据发生了变化
    //要提供一个回调函数
    public void watch(String key, Consumer<String> consumer) {
        Watch watchClient = client.getWatchClient();

        ByteSequence byteSequence = ByteSequence.from(key, StandardCharsets.UTF_8);
        //watch一次即可
        watchClient.watch(byteSequence, watchResponse -> {
            for (WatchEvent event : watchResponse.getEvents()) {
                WatchEvent.EventType eventType = event.getEventType();
                if (eventType == WatchEvent.EventType.PUT) {
                    LOG.info("etcd watch put key={}", key);
                    //新增或更新
                    String value = Optional.ofNullable(event.getKeyValue().getValue()).map(v -> v.toString(StandardCharsets.UTF_8)).orElse("");
                    consumer.accept(value);
                } else if (eventType == WatchEvent.EventType.DELETE) {
                    //删除
                    LOG.warn("etcd watch ignore delete key={}", key);
                }
            }
        });

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //这哥们就是把我们前面设置的etcd服务端的地址等参数进行类型转换,然后将属性绑定到对象,Map,List等类型上
//        this.etcdConfigProperties = EtcdConfigPropertiesUtil.buildEtcdConfigProperties(environment);
        ClientBuilder clientBuilder = Client.builder().endpoints(etcdConfigProperties.getServerAddr().toArray(new String[0]));
        if (!StringUtils.isEmpty(etcdConfigProperties.getUsername())) {
            clientBuilder.user(ByteSequence.from(etcdConfigProperties.getUsername(), StandardCharsets.UTF_8));
        }
        if (!StringUtils.isEmpty(etcdConfigProperties.getPassword())) {
            clientBuilder.password(ByteSequence.from(etcdConfigProperties.getPassword(), StandardCharsets.UTF_8));
        }
        //设置客户端调服务端的负载均衡算法
        clientBuilder.loadBalancerPolicy("round_robin");
        this.client = clientBuilder.build();
        LOG.info("etcd客户端初始化成功");
    }
}