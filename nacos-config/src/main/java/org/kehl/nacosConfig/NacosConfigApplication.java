package org.kehl.nacosConfig;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * springcloud-alibaba
 * nacos配置中心
 *
 * @author : kehl
 * @date : 2022-05-16 20:44
 **/
@SpringBootApplication
public class NacosConfigApplication {
    public static void main(String []args) throws InterruptedException {
//        run方法，会自动加载配置文件
        ConfigurableApplicationContext applicationContext = SpringApplication.run(NacosConfigApplication.class, args);
        while( true){//测试nacos配置的刷新
            String userName = applicationContext.getEnvironment().getProperty("user.name");
            String userAge = applicationContext.getEnvironment().getProperty("user.age");
            String userConfig = applicationContext.getEnvironment().getProperty("user.config");
            System.err.println("user name :"+userName+"; age: "+userAge+"; config: "+userConfig);
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
