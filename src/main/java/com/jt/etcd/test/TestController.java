//package com.jt.etcd.test;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @author 帅气的景天老师
// * @create 2023/3/18 15:29
// */
//@RestController
//@RefreshScope
//public class TestController {
//    @Value("${jt:girl}")
//    private String jt;
//
//    @RequestMapping("/get")
//    public String get(){
//        return jt;
//    }
//}
