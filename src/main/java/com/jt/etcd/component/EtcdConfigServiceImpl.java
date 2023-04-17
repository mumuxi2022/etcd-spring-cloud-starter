package com.jt.etcd.component;

//import com.jt.etcd.listener.AbstractNotifyUserListener;
import com.jt.etcd.listener.Listener;
import com.jt.etcd.listener.UpdatePSListener;
import com.jt.etcd.listener.UpdateObjectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author 帅气的景天老师
 * @create 2023/3/16 18:18
 */
public class EtcdConfigServiceImpl implements EtcdConfigService, ApplicationContextAware {

    //打印日志的
    private static final Logger LOG = LoggerFactory.getLogger(EtcdConfigServiceImpl.class);

    public static final String BEAN_NAME = "etcdConfigServiceImpl";

    private EtcdComponent etcdComponent;

    public EtcdConfigServiceImpl(EtcdComponent etcdComponent){
        this.etcdComponent = etcdComponent;
    }

    //保存所有的listener（就是所有跟key绑定的回调方法）
    private final AtomicReference<Map<String, CopyOnWriteArrayList<Listener>>> cacheMap = new AtomicReference<>(new HashMap<>());

    //代表所有dataId
    private static final String ALL_DATA_ID = "*";

    @Override
    public String getConfig(String dataId) {
        try {
            return etcdComponent.getValue(dataId);
        } catch (Exception e) {
            throw new RuntimeException("获取配置失败", e);
        }

    }

    @Override
    public void addListener(String dataId, Listener listener) {
        //指定dataId增加watch，listener就是回调方法而已，它就在本地跟这个key进行绑定，
        //一旦etcdserver端的key发生变化，etcdserver端就会回调watch，然后在watch里面
        //拿本地跟这个key绑定的listener，然后执行listener的业务流程
        registerWatchAndCacheListener(dataId, listener);

    }

    @Override
    public void addAllListener(Listener listener) {
        //为所有dataId增加watchlistener就是回调方法而已，它就在本地跟这个key进行绑定，
        //一旦etcdserver端的key发生变化，etcdserver端就会回调watch，然后在watch里面
        // 拿本地跟这个key绑定的listener，然后执行listener的业务流程
        registerWatchAndCacheListener(ALL_DATA_ID, listener);
    }

    //向etcdserver端注册watch（就是把key告诉etcdserver，一旦这个key里面的数据发生了变化
    // etcdserver回调watch中的accept方法，然后在accept方法中找到跟key绑定的listener，
    // 最后执行listener的业务流程），同时保存所有的listener到本地缓存
    private void registerWatchAndCacheListener(String dataId, Listener listener) {
        //拿本地跟key绑定的所有listener集合，如果集合是null，证明key还没有注册成watch
        // 就把key注册到etcdserver端，同时把参数中的listener和加到listener集合里面
        CopyOnWriteArrayList<Listener> listeners = getListAndRegisterEtcdWatchIfAbsent(dataId);
        //将listener存在本地
        listeners.add(listener);
    }

    private CopyOnWriteArrayList<Listener> getListAndRegisterEtcdWatchIfAbsent(String dataId) {
        //先在本地拿看有没有已经跟key绑定好的listener集合
        CopyOnWriteArrayList<Listener> listeners = cacheMap.get().get(dataId);
        //双重检查锁去注册key
        if (listeners == null) {
            synchronized (cacheMap) {
                listeners = cacheMap.get().get(dataId);
                if (listeners == null) {
                    listeners = new CopyOnWriteArrayList<>();

                    Map<String, CopyOnWriteArrayList<Listener>> copy = cacheMap.get();
                    copy.put(dataId, listeners);
                    cacheMap.set(copy);

                    registerEtcdWatch(dataId);
                }
            }
        }
        return listeners;
    }

    //每个dataId只会向etcd注册一次
    private void registerEtcdWatch(String dataId) {
        if (!ALL_DATA_ID.equals(dataId)) {
            //注册etcd watch
            etcdComponent.watch(dataId, new Consumer<String>() {
                //etcdserver数据发生变化，回调，在invokeEtcdListener里面调真正的回调方法
                @Override
                public void accept(String s) {
                    invokeEtcdListener(dataId, s);
                }
            });
            LOG.info("注册watch dataId={}", dataId);
        }
    }

    //真正执行回调
    private void invokeEtcdListener(String dataId, String configInfo) {

        try {
            //拿到跟这个key绑定的listeners数组
            CopyOnWriteArrayList<Listener> listeners1 = cacheMap.get().get(dataId);
            CopyOnWriteArrayList<Listener> listeners2 = cacheMap.get().get(ALL_DATA_ID);
            List<Listener> listenerList = combine(listeners1, listeners2);//合并listener
            if (listenerList.size() == 0) {
                return;
            }
            //从上面的数组中拿到具体的listener回调
            //listener定义有两种：当然首先定义标准接口listener，定义标准方法，然后提供模板方法模式的抽象类
            //AbstractNotifyUserListener，在这哥们里面我们要创建线程池去异步完成回调
            //1、完成本地PS容器刷新
            //2、完成对象刷新
            List<Listener> updatePSListener = getUpdatePSListener(listenerList);
            List<Listener> updateObjectListener = getUpdateObjectListener(listenerList);
//            List<Listener> notifyUserListener = getNotifyUserListener(listenerList);
            invoke(updatePSListener, configInfo);
            invoke(updateObjectListener, configInfo);
//            invoke(notifyUserListener, configInfo);
        } catch (Exception e) {
            LOG.error("", e);
        }

    }

    private void invoke(List<Listener> listeners, String configInfo) {
        for (Listener listener : listeners) {
            listener.receiveConfigInfo(configInfo);
        }
    }

    private List<Listener> combine(CopyOnWriteArrayList<Listener> listeners1, CopyOnWriteArrayList<Listener> listeners2) {
        List<Listener> list = new ArrayList<>();
        if (listeners1 != null) {
            list.addAll(listeners1);
        }
        if (listeners2 != null) {
            list.addAll(listeners2);
        }
        return list;
    }

//    private List<Listener> getNotifyUserListener(List<Listener> listenerList) {
//        List<Listener> list = new ArrayList<>();
//        for (Listener listener : listenerList) {
//            if (listener instanceof AbstractNotifyUserListener) {
//                list.add(listener);
//            }
//        }
//        return list;
//    }

    private List<Listener> getUpdateObjectListener(List<Listener> listenerList) {
        List<Listener> list = new ArrayList<>();
        for (Listener listener : listenerList) {
            if (listener instanceof UpdateObjectListener) {
                list.add(listener);
            }
        }
        return list;
    }

    private List<Listener> getUpdatePSListener(List<Listener> listenerList) {
        List<Listener> list = new ArrayList<>();
        for (Listener listener : listenerList) {
            if (listener instanceof UpdatePSListener) {
                list.add(listener);
            }
        }
        return list;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.etcdComponent = applicationContext.getBean(EtcdComponent.BEAN_NAME, EtcdComponent.class);
    }
}
