package sentinelNew.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sentinelNew.entity.User;

import javax.annotation.PostConstruct;
import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-17 11:47
 **/
@RestController
@Slf4j
public class HelloController {
    private static final String RESOURCE_NAME="hello";
    private static final String USER_RESOURCE_NAME="user";
    private static final String DEGRADE_RESOURCE_NAME="degrade";


    //进行sentinel流控
    //这种代码方式侵入性太强了，需要在接口内部写很多的流控代码
    @RequestMapping("/hello")
    public String hello(){
        Entry entry =null;
        try{
            //sentinel 针对资源进行限制
            entry= SphU.entry(RESOURCE_NAME);
            //被保护的业务逻辑
            String str="hello world";
            log.info(str);
            return str;
        } catch (BlockException e) {
            //资源访问组织，被限流，或者被降级
            //进行相应的操作处理
            log.info("block!");
            return "被流量控制了！";
        }
        catch(Exception ex){
            Tracer.traceEntry(ex,entry);
        }finally{
            if(entry!=null){
                entry.exit();
            }
        }
        return null;
    }


    /*
        @SentinelResource改善接口中资源定义和被流控降级后的处理方法
        怎么使用: 1.添加依赖<artifactId>sentinel-annotation-aspectj</artifactId>
                2．配置bean—-SentinelResourceAspect(在启动类中)
                value--定义资源
                blockHandler--设置流控降级后的处理方法（默认该方法必须声明在同一个类中）
                如果不想在同一个类中，可以定义在其他类中，但必须要添加static 修饰符，然后在@SentinelResource注释中加上，blockHandlerClass = xxx.class
                fallback 指接口出现了异常，就可以交给fallback指定的方法进行处理

                当blockHandler和fallback同时指定了，则blockHandler优先级更高

                exceptionsToIgnore--排除哪些异常不处理
     */
    @RequestMapping("user")
    @SentinelResource(value = USER_RESOURCE_NAME, blockHandler = "blockHandlerForGetUser",
            fallback = "fallbackHandlerForGetUser",
            exceptionsToIgnore = { ArithmeticException.class}
    )
    public User getUser(String id){
        int a=1/0;
        return new User("1","kehl");
    }
        /*
        注意点：  1.方法一定要是public
                2.返回值必须和源方法一致，这里必须要都是User，还有包含源方法的参数
                3.可以在参数最后添加BlockException 可以区分是什么规则的异常
         */
    public User blockHandlerForGetUser(String id,BlockException e){
        e.printStackTrace();
        return new User("2","流控！！");
    }

    public User fallbackHandlerForGetUser(String id,Throwable e){
        e.printStackTrace();
        return new User("3","异常处理！！");
    }

    @RequestMapping("degradeTest")
    @SentinelResource(value = DEGRADE_RESOURCE_NAME,entryType = EntryType.IN,blockHandler = "blockHandlerForFb")
    public User degradeTest(String id) throws InterruptedException {
        throw new RuntimeException("异常");
//        TimeUnit.SECONDS.sleep(1);
//        return new User("4","正常");
    }

    public User blockHandlerForFb(String id,BlockException e){
        e.printStackTrace();
        return new User("4","熔断降级");
    }


    //spring 的初始化方法，生命周期回调方法
    @PostConstruct
    private static void initFlowRules(){
        //流控规则，流控规则可以有很多个
        List<FlowRule> rules=new ArrayList<>();
        //新建一个流控规则
        FlowRule rule=new FlowRule();
        //设置受保护的资源名称
        rule.setResource(RESOURCE_NAME);
        //设置流控规则为QPS
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //设置受保护的资源阈值
        //1的意思就是，1秒钟之内访问量不能超过1
        rule.setCount(1);
        rules.add(rule);

        FlowRule rule2=new FlowRule();
        rule2.setResource(USER_RESOURCE_NAME);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(1);
        rules.add(rule2);

        //配置完成之后，不要忘记加载
        FlowRuleManager.loadRules(rules);
    }


    //spring 的初始化方法，生命周期回调方法
    @PostConstruct
    private static void initDegradeRule(){
        //降级规则  异常
        List<DegradeRule> degradeRules=new ArrayList<>();
        //新建一个降级规则
        DegradeRule rule=new DegradeRule();
        //设置受保护的资源名称
        rule.setResource(DEGRADE_RESOURCE_NAME);
        //设置降级规则异常数策略
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        //设置异常数的阈值
        rule.setCount(2);
        //触发熔断的最小请求数量：2
        rule.setMinRequestAmount(2);
        //统计时长，默认为1，也就是在1s内执行了2次以上，且触发了2次异常，就会熔断
        rule.setStatIntervalMs(60*1000);
        //熔断持续窗口--触发熔断之后，10s内都会触发降级方法，10s后第一的请求，如果有触发了异常，又会回到熔断状态，否则就回归正常。
        rule.setTimeWindow(5);
        degradeRules.add(rule);
        //配置完成之后，不要忘记加载
        DegradeRuleManager.loadRules(degradeRules);
    }



}
