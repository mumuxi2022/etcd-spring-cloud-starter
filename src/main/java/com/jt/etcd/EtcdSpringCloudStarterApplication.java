package com.jt.etcd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EtcdSpringCloudStarterApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(EtcdSpringCloudStarterApplication.class, args);
    }

}
