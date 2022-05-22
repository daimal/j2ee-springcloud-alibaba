package sentinelNew;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-17 12:01
 **/
@SpringBootApplication
public class StartApplication {

    public static void main(String []args){
        SpringApplication.run(StartApplication.class,args);
    }

    @Bean
    public SentinelResourceAspect sentinelResourceAspect(){
        return new SentinelResourceAspect();
    }

}
