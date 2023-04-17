package com.jt.etcd.config;

import com.jt.etcd.constants.EtcdConfigConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 帅气的景天老师
 * @create 2023/3/16 17:41
 */
//该注解在读配置文件的时候只读以etcd.config开头的配置，然后将配置注入到属性中
//这个注解要跟@Bean或者@Component一起使用，要不然没意义的
@ConfigurationProperties(prefix = EtcdConfigConstants.CONFIG_PROPERTIES_PREFIX)
public class EtcdConfigProperties {
    //是否开启etcd的功能
    private boolean enabled;
    //etcd服务端地址
    private List<String> serverAddr;
    //etcd服务端登录账户
    private String username;
    //etcd服务端登录密码
    private String password;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(List<String> serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "ImEtcdConfigProperties{" +
                "enabled=" + enabled +
                ", serverAddr=" + serverAddr +
                ", username='" + username + '\'' +
                ", password='" + "******" + '\'' +
                '}';
    }
}
