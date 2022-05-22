# J2EE课程记录

## 环境安装

**docker desktop 使用记录**

### 1.安装mysql 

#### **1.拉取mysql镜像**

```
docker pull mysql：5.7
```

#### **2.启动镜像**

```
docker run --name mysql -v D:/docker/mysql/datadir:/var/lib/mysql -v D:/docker/mysql:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=123456 -e TZ=Asia/Shanghai --restart=always -d -i -p 3306:3306 mysql:5.7
```

### 2.安装Nacos

#### **1.nacos 单机模式**

```
docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --restart=always -e MODE=standalone -e JVM_XMS=200m -e JVM_XMX=200m -e JVM_XMN=200m --name nacos nacos/nacos-server:2.0.2
```

这条语句会自动下载nacos镜像

```
docker stop nacos &&docker rm nacos
```

如果要创建其他模式的nacos 的话，可以先把之前创建的容器先停掉然后删除

#### **2.nacos mysql 模式**

下面的代码要做一些修改，password要改为自身数据库的密码，host要改为数据库的host

```
docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --restart=always -e MODE=standalone -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_PASSWORD=password -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=host --name nacos nacos/nacos-server:2.0.2

docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --restart=always -e MODE=standalone -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_PASSWORD=123456 -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=localhost --name nacos nacos/nacos-server:2.0.2

docker run -d -p 8848:8848 --restart=always -e MODE=standalone -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_PASSWORD=123456 -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=localhost --name nacos nacos/nacos-server:1.4.1

```

查看nacos 日志

docker logs -f nacos

springcloud alibaba 整体架构图

![img](file:///C:/Users/KeHaole/AppData/Local/Temp/msohtmlclip1/01/clip_image002.jpg)

![image-20220520112652439](images\image-20220520112652439.png)

## nacos 

#### nacos参数介绍

服务列表

![image-20220515205748683](images\image-20220515205748683.png)

##### 1.保护阈值（0-1）

健康实例数/总实例数<保护阈值，如果出现这种情况，就算实例是不健康的也会调用，只是会报错。

如果是0，就相当于不开启雪崩保护

##### 2.spring.cloud.nacos.discovery.ephemeral=false

fasle代表永久实例，哪怕宕机了也不会删除实例

##### 3.命名空间

命名空间就是为了分隔不同的环境，例如dev开发环境，prod生产环境

##### 4.分组

就是更细的分割粒度，在同一个环境中，可以分为不同的分组

##### 5.权重

值越大，权重越高，就会分配更多的流量

##### 6.元数据

#metadata: 假设元数据中有 version=1，那么可以结合元数据做扩展功能

##### 7.订阅者列表

![image-20220515205627307](images\image-20220515205627307.png)

netstat  -aon|findstr "62753"

通过上面的命令可查询本地进程服务的PID

## Ribbon

![image-20220516102813153](images\image-20220516102813153.png)

### 1.负载均衡策略

#### RandomRule

看名字就知道，这种负载均衡策路就是**随机选择一个服务实例**，看源码我们知道，在RandomRule的无参构造方法中初始化了一个Random对象，然后在它重写的choose方法又调用了choose(LoadBalancer lb, Object key)这个重载的choose方法，在这个重载的choose方法中，每次利用random对象生成一个不大于服务实例总数的随机数，并将该数作为下标所以获取一个服务实例。

#### RoundRobinRule

RoundRobinRule这种负载均衡箫略叫做线性**轮询负载均衡策略**。这个类的choose(LoadBalancer lb, Object key)函数整体逻辑是这样的︰开启一个计数器count,在while循环中遍历服务清单，获取清单之前先通过incrementAndGetModulo方法获取一个下标，这个下标是一个不断自增长的数先加1然后和服务清单总数取模之后获取到的(所以这个下标从来不会越界），拿着下标再去服务清单列表中取服务，每次循环计数器都会加1，如果连续10次都没有取到服务，则会报一个警告No available alive servers after 10 tries from load blancer:XXXX。

#### RetryRule(在轮询的基础上进行重试)

看名字就知道这种负载均衡策略带有**重试**功能。首先RetryRule中又定义了一个subRule，它的实现类是RoundRobinRule，然后在RetryRule的choose(1LoadBalancer lb,Object key)方法中，每次还是采用RoundRobinRule中的choose规则来选择一个服务实例，如果选到的实例正常就返回，如果选择的服务实例为nul或者已经失效，则在失效时间deadline之前不断的进行重试(重试时获取服务的策略还是RoundRobinRule中定义的策略)，如果超过了tcadline还是没取到则会返回一个null。

#### WeightedResponseTimeRule(权重一nacos:INacosRule Nacos还扩展了一个自己的基于配置的权重扩展)

WeightedResponseTimeRule是RoundRobinRule的一个子类，在WeightedResponseTimeRule中对RoundRobinRule的功能进行了扩展，WeightedResponseTimeRule中会根据每一个实例的运行情况来给计算出该实例的一个**权重**，然后在挑选实例的时候则根据权重进行挑选，这样能够实现更优的实例调用。WeightedResponseTimeRule中有一个各叫
DynamicServerWeightTask的定时任务，默认情况下每隔30秒会计算一次各个服务实例的权重，**权重的计算规则也很简单，如果一个服务的平均响应时间越短则权重越大，那么该服务实例被选中执行任务的概率也就越大。**

#### ClientConfigEnabledRoundRobinRule

ClientConfgEnabledRoundRobinRule选择策路的实现很简单，内部定义了RoundRobinRule,choose方法还是采用了RoundRobinRule的choose方法，**所以它的选择策略和RoundRobinRule的选择策略一致**，不赘述。

#### BestAvailableRule

BestAvailableRule继承自ClientConfigEnabledRoundRobinRule，它在ClientConfigEnabledRoundRobinRule的基础上主要增加了根据loadBalancerStats中保存的服务实例的状态信息**来过滤掉失效的服务实例的功能，然后顺便找出并发请求最小的服务实例来使用**。然而loadBalancerStats有可能为null，如果loadBalancerStats为mul，则BestAvailaableRule将采用它的父
类即ClientConfigEnabledRoundRobinRule的服务选取策略（线性轮询)

#### ZoneAvoidanceRule(默认规则，复合判断server所在区域的性能和server的可用性选择服务器。)

ZoneAvoidanceRule是PredicateBasedRule的一个实现类，只不过这里多一个过滤条件，ZoneAvoidanceRule中的过滤条件是以ZoneAvoidancePredicate为主过滤条件和以AvailablityPredicate为次过滤条件组成的一个叫做CompositePredicate的组合过滤条件，过滤成功之后，继续采用**线性轮询(RoundRobinRule)**的方式从过旋结果中选择一个出来。

#### AvailabilityFilteringRule(先过滤掉故障实例，再选择并发较小的实例)

过滤掉一直连接失败的被标记为cicuit trpped的后端Server，并过滤掉那些高并发的后端Sever或者使用一个AvailabilityPredicate来包含过滤server的逻辑，其:就是检查status里记录的各个Server的运行状态。

### 2.自定义负载均衡策略

##### 1.配置类

注意这里有一个坑，就是ribbon配置类不能写在@SpringbootApplication的注解@ComentScan能扫描到的地方，否则自定义的配置累就会被所有的RibbonClients共享。

不建议用这种方式，建议用yml方式

![image-20220516104523933](images\image-20220516104523933.png)

编写配置累，要保证名字就是iRule，不要写错了

![image-20220516103812697](images\image-20220516103812697.png)

然后在application启动类上面添加RibbonClients注解，表明对那个服务提供方使用怎么样的负载均衡策略。

![image-20220516104659870](images\image-20220516104659870.png)

##### 2.配置文件方式

**注意在服务消费者这里要填写的是服务提供者的名字，如下面就是要写product-sevice**

![image-20220516140935761](images\image-20220516140935761.png)

将productApplication权重改为5之后，重新运行

![image-20220516141232288](images\image-20220516141232288.png)

用postman Runner 连续跑十次的结果如下：

![image-20220516141141849](images\image-20220516141141849.png)

##### 3.自定义策略

![image-20220516143209920](images\image-20220516143209920.png)

创建文件，然后实现AbstractLoadBalancerRule类，

重写choose方法，下面是自己实现的随机选择方法：

![image-20220516143244093](images\image-20220516143244093.png)

在配置文件中修改之后，重新run，测试后的结果如下：

![image-20220516143342860](images\image-20220516143342860.png)

![image-20220516143347932](images\image-20220516143347932.png)

##### 4.开启饥饿加载

为了解决第一次调用慢的问题

![image-20220516143638960](images\image-20220516143638960.png)

![image-20220516144207289](images\image-20220516144207289.png)

修改之后的运行结果如下，就没有了加载的情况：

![image-20220516144229385](images\image-20220516144229385.png)

## Feign

#### JAVA 项目中如何实现接口调用?

1. ##### Httpclient

   Hitaoelent是 Apathe Jlacara comon下的子项目，用来提供高效的、最新的、功能丰富的支持Hitp协议的客户端锦程工具包，并且它支持HTTP协议最新版本和建议。FitpaClient相比传统JDK自带的URLConnection，提升了易用性和灵活性，使客户端发送HTTP请求变得容易，提高了开发的效率。

2. ##### Okhttp

  一个处理阿络清求的开源项目，是安卓诺最火的轻量级框架，由Square公司贡献，用于替代HituiCcomection和Apache itpClient, ORitp拥有简洁的API、高效的性能，并支持多种协议(HTTP/2和SPDY)。

3. ##### HttpURLConnection

  HipURLComection是Java的标准类，它继承自URLComecton，可用于向指定网站发送GET请求、POST请求。 HipLURLConnecion使用比较复杂，不像 HitpClient那样容易使用。

3) ##### RestTemplate   WebClient

RestTemplate 是Spring提供的用于访问Rest服务的客户端，RestTemplate提供了多种便捷访问远程HTTP服务的方法，能够大大提高客户端的编写效率。上面介绍的是最常见的几种调用接口的方法，我们下面要介绍的方法比上面的更简单、方便，它就是Feign.

### Feign

Feign是NetFlix开发的声明式、模板化的HTTP客户端。

声明式：可以像调用方法一样调用远程服务

HTTP客户端：意味着Feign是定义在服务消费端的

**springCloud openfeign对Feign进行了增强，使其支持Spring MVC注解，另外还整合了Ribbon和Nacos**

#### SpringCloud Alibaba 快速整合Feign

1.引入依赖

```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>2.2.3.RELEASE</version>
</dependency>
```

2.编写调用接口+@FeignClient注解

![image-20220516152622984](images\image-20220516152622984.png)

3.调用端在启动类上添加@EnableFeignClients注解

![image-20220516152636754](images\image-20220516152636754.png)

4.发起调用，像调用本地方式一样调用远程服务

![image-20220516152704444](images\image-20220516152704444.png)

#### 日志配置

有时候我们遇到Bug，比如接口调用失败、参数没收到等问题，或者想看看调用性能，就需要配置Feign的日志了，以此让Feign把请求信息输出来。

##### 1.定义一个配置类，指定日志级别

通过源码可以看到日志等级有4种，分别是:
	1.NONE【性能最佳，适用于生产】:不记录任何日志(默认值)。
	2.BASIC【适用于生产环境追踪问题】∶仅记录请求方法、URL、响应状态代码以及执行时间。

​	3.HEADERS:记录BASIC级别的基础上，记录请求和响应的header。
​	4.FULL【比较适用于开发及测试环境定位问题】︰记录请求和响应的header、body和元数据。

![image-20220516161025371](images\image-20220516161025371.png)

##### 2.全局配置

如果在FeignConfig上使用了@Configuration注解，就会就昂配置作用于所有的服务提供方

这里有可能会出现 The bean 'endservice.FeignClientSpecification' could not be registered. A bean with that name has already been defined and overriding is disabled.的报错信息， 以上问题报错的意思是一个项目中存在多个接口使用@FeignClient调用同一个服务，意思是说一个服务只能用@FeignClient使用一次。

因为在另一个文件中也定义了FeignClient ，而且name也一样

![image-20220516165057212](images\image-20220516165057212.png)

![image-20220516161221270](images\image-20220516161221270.png)

在application.yml文件中加上allow-bean-definition-overriding： true就可以了

以上代码可以使多个接口使用@FeignClient调用同一个服务

![image-20220516161546348](images\image-20220516161546348.png)

**注意feign的调用比Rest调用更加严格，不仅定义的变量要跟路径中的变量名保持一致，在@PathVariable中也需要指定路径中的变量。**

但是这时候日志还是不会输出的，因为springboot默认的日志级别是info，feign的debug日志级别就不会输出。

所以还要在application.yml文件中，对日志的级别进行设置。

![image-20220516162322513](images\image-20220516162322513.png)日志的输出结果就是：

![image-20220516162300280](images\image-20220516162300280.png)

##### 3.局部配置

​	1.代码形式

先注释掉FeignConfig中@Configuration

​		![image-20220516165405288](images\image-20220516165405288.png)

然后需要日志输出的Service上面添加configuration

![image-20220516165451034](images\image-20220516165451034.png)

​	2.配置文件形式

![image-20220516170039673](images\image-20220516170039673.png)

#### 超时时间设置

##### 1.代码配置方式，在FeignConfig中配置

```java
//超时时间配置
    @Bean
    public Request.Options options(){
        return new Request.Options(5000,3000);
    }
```

##### 2.配置文件配置方式

```java
#连接超时时间，默认2s
connectTimeout: 5000
#请求处理超时时间，默认5s
readTimeout: 3000
```

![image-20220516171607982](images\image-20220516171607982.png)

#### 自定义拦截器

##### 1.Feign的拦截器与SpringMVC拦截器的区别；

SpringMVC拦截器是在客户端调用服务端接口是起作用的

Feign拦截器实在服务消费者调用服务提供者的时候起作用的，两端都是服务端。

可以用这个拦截器实现，日志方式的修改，或者认证服务（一般在网关实现）

##### 2.配置类实现方式

创建一个CustomFeignInterceptor配置类方法

![image-20220516173040046](images\image-20220516173040046.png)

自定义一个拦截器

```java
public class CustomFeignInterceptor implements RequestInterceptor {
    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("xxx","xxx");
        requestTemplate.query("id","111");
        requestTemplate.uri("/9");
        logger.info("feign拦截器!");
    }
```

然后再FeignConfig文件中创建的相关的实例

```
/*
自定义拦截器
 */
@Bean
public CustomFeignInterceptor feignAcceptGzipEncodingInterceptor(){
    return new CustomFeignInterceptor();
}
```

##### 3.配置文件形式

![image-20220516173240312](images\image-20220516173240312.png)

interceptor是一个数组，所以要指定下标

## Nacos 配置中心的使用

Nacos提供用于存储配置和其他元数据的key/value存储，为分布式系统中的外部化配置提供服务器端和客户端支持。使用Spring Cloud Aliaba Nacos Config，您可以在Nacos Server集中管理 Spring Cloud应用的外部属性配置。

![image-20220516195929159](images\image-20220516195929159.png)

 

### 为什么要用配置中心：

以前的配置文件一旦修改，就需要重新启动服务，如果一个两个还好，但是企业级环境开发下，服务都是成百上千个的，每修改一次配置文件，就重启一下，成本就很高，而且，像电商平台，一旦碰到什么大促，需要新增服务器，就要修改数据库等配置文件，然后再重启，就很麻烦，所以Nacos就用配置中心的方法。

### nacos 配置中心与springcloud配置中心的对比的三大优势：

1.springcloud config大部分场景结合git 使用,动态变更还需要依赖Spring Cloud Bus消息总线来通过所有的客户端变化

2.pringcloud config不提供可视化界面

3.nacos config使用长轮询更新配置,一旦配置有变动后，通知Provider的过程非常的迅速,从速度上秒杀springcloud原来的config几条街

三种主流的配置中心的对比

![image-20220516201051974](images\image-20220516201051974.png)



### nacos配置中心的使用

官方文档: https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config

#### 1.权限管理

如果要使用权限管理话，需要先修改nacos启动配置文件application.properties中的nacos.core.auth.enabled。从false 改为true

![image-20220516202801721](images\image-20220516202801721.png)

**注意1:如果开启了权限管理，就一定要在bootstrap.yml文件中加上用户名和密码**

![image-20220516205910153](images\image-20220516205910153.png)

否则就会报如下的错误：

![image-20220516205826538](images\image-20220516205826538.png)

#### 2.客户端使用（具体可以看官网）

**注意2：在配置文件中我们指明了application的name是nacos-config，如果此时在nacos配置中心中没有名为nacos-config的配置，此时就会输出电脑本地环境的用户名**

![image-20220516210241376](images\image-20220516210241376.png)

![image-20220516210523248](images\image-20220516210523248.png)

![image-20220516210835262](images\image-20220516210835262.png)

**说明nacos会自动根据服务名拉取dataid对应的配置文件。如果dataid和服务名不一致就需要手动指定dataid**

nacos配置中心每隔一段时间就会去查询配置文件是否修改，查询方式是用md5对比，每一次修改都会向服务（数据库）所在的ip推送md5信息，

![image-20220516211735509](images\image-20220516211735509.png)

然后就会把最新的md5更新到数据库中

![image-20220516211842954](images\image-20220516211842954.png)

#### 3.可支持profile粒度的配置

application.yml:

```
#在配置中心： 可以通过profile进行设置
#只有默认的配置文件(与服务名相同的配置文件)才能结合profile进行使用
#对应的DataId:${spring.application.name}-${profile}.${file-extension:properties}
#后缀必须跟随默认配置文件的格式来
#配置文件的优先级，（优先级大的会覆盖优先级小的，并且会形成互补）
#profile>默认配置文件
spring:
  profiles:
    active: dev
```

bootstrap.yml:

```
spring:
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        #如果配置了权限管理，就一定要带上用户名和密码
        username: nacos
        password: nacos
        #namespace: public
        #nacos默认读取的配置文件格式是：properties
        #一旦修改为其他格式，就需要在这里指定
        file-extension: yaml
```

![image-20220517101649887](images\image-20220517101649887.png)

#### 4.自定义拓展DataId配置

##### 1.shared-config：

有两种下标形式：一种是（横杠-)，另一种就是普通下标

![image-20220517104255931](images\image-20220517104255931.png)

![image-20220517104416705](images\image-20220517104416705.png)

**Note：**

**1.用命名空间名好像不能正确识别到，可改为hashId**

**2.后定义的共享配置文件权限更高，如果有相同的字段，后定义的配置文件会覆盖前面的配置文件**

##### 2.extension-configs

跟sharedConfig配置方式差不多，但是extension-configs优先级高于sharedConfig

![image-20220517104926611](images\image-20220517104926611.png)



**各个配置文件优先级：**

  **profile>默认配置文件>extension-configs（下标大的优先级高）>shared-configs共享配置文件（下标大的优先级高）**

#### 5.在Controller层中动态获取配置文件中的值

如果不加@RefreshScope注解的话，用@Value获取到的配置文件中的值就不能实时感知到配置文件的变化

![image-20220517105818892](images\image-20220517105818892.png)

## Sentinel

**服务的可用性问题**

![image-20220517110344096](images\image-20220517110344096.png)

#### 1.常见的容错机制：

##### **1.超时机制**

在不做任何处理的情况下，服务提供者不可用会导致消费者请求线程强制等待，而造成系统资源耗尽。加入超时机制，一旦超时，就释放资源。由于释放资源速度较快，一定程度上可以抑制资源耗尽的问题。

##### **2.服务限流**

QPS：每秒的访问量

![image-20220517111123851](images\image-20220517111123851.png)

##### **3.隔离**

原理:用户的请求将不再直接访问服务，而是通过线程池中的空闲线程来访问服务，如果线程池已满，则会进行降级处理，用户的请求不会被阻塞，至少可以看到一个执行结果（例如返回友好的提示信息)，而不是无休止的等待或者看到系统崩溃。

**a)线程隔离：**

隔离前:

![image-20220517111230829](images\image-20220517111230829.png)

隔离后：

![image-20220517111344150](images\image-20220517111344150.png)

![image-20220517111352270](images\image-20220517111352270.png)

**b)信号隔离:**
信号隔离也可以用于限制并发访问，防止阻塞扩散、与线程隔离最大不同在于执行依赖代码的线程依然是清求线程〈饿线程需要通过信号申请,如果客户锦是可信的且可以快速返回，可以使用信号隔离替换线程隔离,降低开销。信号量的大小可以动态调整,线程池大小不可以。

##### **4.服务熔断**

**远程服务不稳定或网络抖动时暂时关闭，就叫服务熔断。**
现实世界的断路器大家肯定都很了解，断路器实时监控电路的情况，如果发现电路电流异常，就会跳闸，从而防止电路被烧毁。
软件世界的断路器可以这样理解:实时监测应用，如果发现在一定时间内失败次数失败率达到一定阈值，就*跳闸”，断路器打开——此时，请求直接返回，而不去调用原本调用的逻辑。跳闸一段时间后(例如10秒)，断路器会进入半开状态，这是一个瞬间态，此时允许一次请求调用该调用的逻辑，如果成功，则断路器关闭，应用正常调用;如果调用依然不成功，断路器继续回到打开状态，过段时间再进入半开状态尝试——通过“跳闸"，应用可以保护自己，而且避免浪费资源;而通过半开的设计，可实现应用的"自我修复“。
所以，同样的道理，**当依赖的服务有大量超时时，在让新的请求去访问根本没有意义，只会无畏的消耗现有资源**。比如我们设置了超时时间为1s.如果短时间内有大量请求在1s内都得不到响应，就意味着这个服务出现了异常，此时就没有必要再让其他的请求去访问这个依赖了，这个时候就应该使用断路器避免资源浪费。

![image-20220517111857898](images\image-20220517111857898.png)

**服务降级**

有服务熔断，必然要有服务降级。
所谓降级，就是当某个服务熔断之后，服务将不再被调用，此时客户端可以自己准备一个本地的alack(回退)回调，返回一个缺省值。例如:(备用接口缓存/mock数据) 。
这样做，虽然服务水平下降，但好歹可用，比直接挂掉要强，当然这也要看适合的业务场景。

服务降级一般会在**弱依赖**的服务上实现，**弱依赖服务**就是，计算服务挂掉了，也不影响整个服务的实现，比如商品秒杀中的积分服务，计算积分服务挂掉了，对他进行降级处理，比如写一条日志记录一下用户信息，之后根据用户信息对用户进行补偿即可。

#### 2.sentinel是什么

sentinel是阿里巴巴开源的，面向分布式服务架构的高可用防护软件。

随着微服务的流行，服务和服务之间的稳定性变得越来越重要。Seniel,是面向分布式服务架构的流量控制组件，主要以流量为切入点，从**限流、流量整形、熔断降级、系统负载保护、热点防护**等多个维度来帮助开发者保障微服务的稳定性。

源码地址: https://github.com/alibaba/Sentinel

官方文档: https://github.com/alibaba/Sentinel/wiki

##### **1.sentinel和Hystrix的对比：**

![image-20220517113118313](images\image-20220517113118313.png)

##### 2.sentinel使用

官方文档：https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8

###### 1.代码侵入的方式

对接口进行改造，添加sentinel 逻辑

​	![image-20220517145108211](images\image-20220517145108211.png)

然后对相应的资源设置流控保护规则，当然保护规则无论是那种方法都是要写的，保护规则和被保护接口通过ResourceName连接在一起：

![image-20220517145136393](images\image-20220517145136393.png)

熔断降级规则：

1.先写个测试接口

```
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
```

2.设置触发熔断降级规则

```
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
```

###### 2.@SentinelResource注解的使用

​	1.先添加相关的依赖

```
<!--使用@SentinelResource注解-->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-annotation-aspectj</artifactId>
</dependency>
```

​	2.配置bean—-SentinelResourceAspect(在启动类中)

```
@Bean
public SentinelResourceAspect sentinelResourceAspect(){
    return new SentinelResourceAspect();
}
```

![image-20220517145926327](images\image-20220517145926327.png)

3.在接口上添加@SentinelResource注解，然后添加相对应的blockHandler和exceptionsToIgnore方法

```
    /*
        @SentinelResource改善接口中资源定义和被流控降级后的处理方法
        怎么使用: 1.添加依赖<artifactId>sentinel-annotation-aspectj</artifactId>
                2．配置bean—-SentinelResourceAspect(在启动类中)
                参数：
                value--定义资源
                blockHandler--设置流控降级后的处理方法（默认该方法必须声明在同一个类中）
                	如果不想在同一个类中，可以定义在其他类中，但必须要添加static 修饰符，然后在@SentinelResource注释中加上
                blockHandlerClass = xxx.class
                fallback--指接口出现了异常，就可以交给fallback指定的方法进行处理
                	当blockHandler和fallback同时指定了，则blockHandler优先级更高
                exceptionsToIgnore--排除哪些异常不处理
     */
@RequestMapping("user")
@SentinelResource(value = USER_RESOURCE_NAME, blockHandler = "blockHandlerForGetUser",
        fallback = "fallbackHandlerForGetUser",
        exceptionsToIgnore = { ArithmeticException.class}
)
```

```
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
```

###### 3.控制台的方式

1.下载jar包

下载控制台jar包并在本地启动,jar包下载界面：https://github.com/alibaba/Sentinel/releases

```
#启动控制台命令
java -Dserver.port=8858 -jar sentinel-dashboard-1.8.0.jar
```

用户可以通过如下参数进行配置:
-Dsentinel.dashboard.auth.username=sentinel用于指定控制台的登录用户名为sentinel

-Dsentinel.dastboard.auth.password=123456用于指定控制台的登录密码为123456;如果省略这两个参数，默认用户和密码均为sentinel;

-server.servlet. session.timeout=7200用于指定Spring Boot服务端session的过期时间，如7200表示7200秒; 6om表示60分钟，默认为30分钟;

```
java -Dserver.port=8858 -Dsentinel.dashboard.auth.username=kehl -Dsentinel.dashboard.auth.password=123456 -jar sentinel-dashboard-1.8.0.jar
```

为了方便快捷启动可以在桌面创建.bat文件

```
java -Dserver.port=8858 -Dsentinel.dashboard.auth.username=kehl -Dsentinel.dashboard.auth.password=123456 -jar D: \sentinel-dashboard-1.8.0.jar
pause
```

访问http://localhost:8080/#/login ,默认用户名密码: sentinel/sentinel

2.客户端参数配置

引入依赖

```
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-transport-simple-http</artifactId>
    <version>x.y.z</version>
</dependency>
```

配置启动参数

```
-Dcsp.sentinel.dashboard.server=127.0.0.1:8858
```

###### 4.spring cloud alibaba 整合Sentinel

1.引入依赖

```
<!--sentinel 组件-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

2.Controller层

![image-20220517165808130](images\image-20220517165808130.png)

3.可以自定义一个统一的异常处理类

Result--返回结果的封装类

![image-20220517165840794](images\image-20220517165840794.png)

![image-20220517165937373](images\image-20220517165937373.png)



###### 5.流控模式

1.直接模式：统计当前资源的请求，触发阈值时对当前资源直接限流，也是默认的模式

**2.关联流控：**

关联模式：统计与当前资源相关的另一个资源，触发阈值时，对当前资源限流

使用场景：比如用户支付时需要**修改订单状态**，同时用户要**查询订单**。查询和修改操作会争抢数据库锁，产生竞争。业务需求是有限支付和更新订单的业务，因此当修改订单业务触发阈值时，需要对查询订单业务限流。

比如下图，**订单查询**关联了**新增订单**的接口，如果发现订单新增接口的调用频率超过了阈值，就触发流控。

![image-20220517172441414](images\image-20220517172441414.png)

因为有关联效果，用浏览器测试比较难，所以借助一下jmeter的工具：

创建一个线程组，线程数设置为300，ramp-up为100，就相当于一秒钟启动3个线程。

ramp up的值应该是启动全部线程所需的时间

![image-20220517185148641](images\image-20220517185148641.png)

新建一个HTTP请求：

![image-20220517185411734](images\image-20220517185411734.png)

然后就可以开始测试了

3.链路：

统计从指定链路访问到本资源的请求，触发阈值时，对指定链路限流。

主要是针对业务方法进行流控，因为访问业务方法的接口可能不止一个，可以针对该业务方法下的某一个接口请求进行流控。

比如orderService中，有一个资源叫getUser

![image-20220517173530880](images\image-20220517173530880.png)

在controller层中，有两个接口test1和test2调用了它

![image-20220517173554735](images\image-20220517173554735.png)

调用树如下：

![image-20220517173932658](images\image-20220517173932658.png)

这时候我们只想限制**test1的访问**，就在控制台上添加**链路**规则

![image-20220517173834782](images\image-20220517173834782.png)

Note：这里有一个坑，链路流控呢，sentinel需要在底层为我们维护一棵上图显示的调用树，但是它默认的情况下是不维护的，需要在application.properties修改配置信息

```
web-context-unify: false #默认是true，即将调用链路收起来，不维护
```

![image-20220517174310008](images\image-20220517174310008.png)

这个时候因为在getUser上使用了@SentinelResource注解，他就不会使用我们统一的异常处理类，那就只能自己写一个BlockHandler，

**注意不要忘记加 （BlockException e）   !!!!**

![image-20220517175129485](images\image-20220517175129485.png)

###### 6.流控效果

1.直接失败：如果超过了阈值，就直接返回失败结果

2.Warm Up（主要针对激增流量，防止缓存击穿等问题）

Wam Up (RuleConstant.CONTROL_BEHAVIOR_WARM_UP)方式，即预热/冷启动方式。当系统长期处于低水位的情况下，当流量突然增加时，直接把系统拉升到高水位可能瞬间把系统压垮。通过"冷启动"，让通过的流量缓慢增加，在一定时间内逐渐增加到阈值上限，给冷系统一个预热的时间,避免冷系统被压垮。
冷加载因子: codeFactor默认是3，即请求QPS 从 threshold / 3开始，经预热时长逐渐升至设定的QPS 阈值。通常冷启动的过程系统允许通过的QPS曲线如下图所示

控制台设置如下：

![image-20220517190751179](images\image-20220517190751179.png)

用jmeter测试结果后：

实时监控结果如下，可以看到通过QPS在逐渐地增加

![image-20220517190916110](images\image-20220517190916110.png)

3.排队等待（主要针对脉冲流量）

脉冲流量可以理解为：有规律的间断式的激增流量，就跟心脏跳动一样，有节奏。

因为第一个脉冲流量中，如果缓存没有被击穿，那么就已经有了缓存数据，在后面的脉冲流量中，就不需要预热了。

考虑到每一个脉冲之间有一定的时间间隔，这个时间间隔可以被利用起来，把流量高峰期没有处理的请求先排上队，在空闲时间里在进行处理

在jmeter中设置一个模拟脉冲流量：

1.循环5次脉冲

![image-20220517192259180](images\image-20220517192259180.png)

2.添加一个固定定时器，每次循环之后等待5s

![image-20220517192344320](images\image-20220517192344320.png)

然后设置流控规则：

![image-20220517192438297](images\image-20220517192438297.png)

之后运行jmeter脚本就会有这样的效果：

![image-20220517192218107](images\image-20220517192218107.png)

###### 7.熔断降级与隔离

熔断一般都定义在消费者端， 主要是对弱依赖服务进行服务降级.

![image-20220518100741322](images\image-20220518100741322.png)

###### 8.熔断策略

1.慢调用比例

​	慢调用比例(SLOW_REQUEST_RATIO):**选择以慢调用比例作为阈值，需要设置允许的慢调用RT(即最大的响应时间)，请求的响应时间大于该值则统计为慢调用。当单位统计时长(statIntervalMs)内请求数目大于设置的最小请求数目，并且慢调用的比例大于阈值**，则接下来的**熔断时长**内请求会自动被熔断。经过熔断时长后熔断器会进入探测恢复状态(HALF-OPEN状态,半开状态)，若接下来的一个请求响应时间小于设置的慢调用RT则结束熔断，若大于设置的慢调用RT 则会再次被熔断。

2.异常比例

异常比例和异常数和慢调用比例类似，唯一的区别就是将判断标准改为了异常所占的比例，和异常的数量。

![image-20220518144555366](images\image-20220518144555366.png)

3.异常数

![image-20220518144619797](images\image-20220518144619797.png)

###### 9.热点限流规则

​	1.写一个测试接口

```
//热点流控规则
/*
热点流控规则只能只用@sentinelResource注解
 */
@GetMapping("/get/{id}")
@SentinelResource(value = "getOrderById",blockHandler = "blockHandlerForGetOrderById")
public String getOrderById(@PathVariable("id") String id){
    return "查询订单，id:"+id;
}
public String blockHandlerForGetOrderById(String id,BlockException e){
    e.printStackTrace();
    return "热点限流了";
}
```

​	2.在控制台修改规则

​	上面的单机阈值就是对于这个接口所有的值进行的限流，

​	限免参数例外项，就是对热点参数进行的限制，添加一个的时候，可能会添加失败，多添加几个然后删掉多余的就好。

![image-20220518143930235](images\image-20220518143930235.png)

###### 10.系统保护规则

因为有的时候规则没有设置到位，还是会造成系统的崩溃，所以需要一个整体的系统保护规则，比如CPU使用率到了多少，就开始限流等等。

Serniel系统自适应限流从整体维度对应用入口流量进行控制，结合**应用的Load、CPU使用率、总体平均RT、入口QPS和并发线程数**等几个维度的监控指标，通过自适应的流控策略，让系统的入口流量和系统的负载达到一个平衡，让系统尽可能跑在最大吞吐量的同时保证系统整体的稳定性。
    **1.Load自适应(仅对Linux/Unix-like机器生效)**︰系统的load1作为启发指标，进行自适应系统保护当系统load1超过设定的启发值，且系统当前的并发线程数超过估算的系统容量时才会触发系统保护(BBR阶段)。系统容量由系统的maxQps * minRt估算得出。设定参考值一般是CPU cores * 2.5。
https://www.cnblogs.com/gentlemanhai/p/8484839.html
     **2.CPU usage (1.5.0+版本)**︰当系统CPU使用率超过阈值即触发系统保护（取值范围0.0-1.0)，比较灵敏。

​     **3.平均RT**: 当单台机器上所有入口流量的平均RT达到阈值即触发系统保护，单位是毫秒。
​     **4.并发线程数**: 当单台机器上所有入口流量的并发线程数达到阈值即触发系统保护。
​     **5.入口QPS**: 当单台机器上所有入口流量的QPS达到阈值即触发系统保护。

###### 11.sentinel持久化

​    1.原始模式

如果不做任何修改，Dashboard的推送规则方式是通过API将规则推送至客户端并直接更新到内存中:

![image-20220518150029907](images\image-20220518150029907.png)

这种做法的好处是简单，无依赖;坏处是应用重启规则就会消失，仅用于简单测试，不能用于生产环境。

​	2.拉模式

  	pull模式的数据源(如本地文件、RDBMS等)一般是可写入的。使用时需要在客户端注册数据源:将对应的读数据源主册至对应的RuleManager，将写数据源注册至tansport的WritableDataSourceRegistry 中。

​	**3.推模式**（主要用推模式）

​	生产环境下一般更常用的是push模式的数据源。对于push模式的数据源如远程配置中心(Zookeeper,Nacos,Apollo等等)，推送的操作下应由 Sentinel客户端进行，而应该经控制台统一进行管理，直接进行推送，数据源仅负责获取配置中心推送的配置并更新到本地。因此推送规则正确做法应该是配置中心控制台Sentinel控制台→配置中心→ Sentinel数据源→ sentinel,，而不是经Sentinel数据源推送至配置中心。这样的流程就非常清晰了∶

基于nacos配置中心控制台实现推送

​		**1.引入依赖**

```
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
```

​		**2.在nacos配置中心添加配置**

默认是json格式

![image-20220518153057644](images\image-20220518153057644.png)

```json
[{

​    "resource": "/order/flow", #资源名的命名为要测试的接口的路径

​    "controlBehavior": 0,

​    "count": 2.0,

​    "grade": 1,

​    "limitApp": "default",

​    "strategy": 0

}]
```

resource :资源名，即限流规则的作用对象

count:限流阈值

grade:限流阈值类型(QPS（0）或并发线程数（1）)

limitApp:流控针对的调用来源，若为default则不区分调用来源

strategy:调用关系限流策略

controlBehavior :流量控制效果(直接拒绝（0）、Warm Up（1）、匀速排队（2）)

​	**3.在application.properties中添加相关配置**

![image-20220518153307495](images\image-20220518153307495.png)

**Note：配置的层级不要少打了**

到目前为止，实现了sentinel规则文件的持久化存储，这个时候，在nacos配置中心中更新后，会实时的显示在sentinel控制台，但是还是做不到在sentinel控制台更新后同步到nacos配置文件。

###### 12.sentine控制台配置更新反推到nacos控制台

​	

![image-20220518151150342](images\image-20220518151150342.png)

##### 3.openfeign整合sentinel

1.在openfeign中添加sentinel的依赖

```
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

2.在application.properties中设置

```
#openfeign整合sentinel
feign:
  sentinel:
    enabled: true
```

3.实现service的fallback类

![image-20220518142450909](images\image-20220518142450909.png)

```
@Component
public class ProductFeignServiceFallbackImpl implements ProductFeignService {
    @Override
    public String getProduct() {
        return "降级了";
    }
}
```

4.在fheignClient中指定fallback函数

![image-20220518142535749](images\image-20220518142535749.png)

## Seata--springcloud分布式事务组件

### 1.Seata概况

#### 1.seata是什么

Seata是一款开源的分布式事务解决方案，致力于提供高性能和简单易用的分布式事务服务。Seata将为用户提供了AT、TCC、SAGA和XA事务模式，为用户打造一站式的分布式解决方案。

AT模式是阿里首推的模式,阿里云上有商用版本的GTS (Global Transaction Service 全局事务服务)
官网: https://seata.io/zh-cn/index.html
源码: https://github.com/seata/seata
官方Demo: https://github.com/seata/seata-samples

seata版本: v1.4.0

#### **Seata的三大角色**

在Seata的架构中，一共有三个角色:

**TC (Transaction Coordinator)-**事务协调者
维护全局和分支事务的状态，驱动全局事务提交或回滚。

**TM（Transaction manager）事务管理器**
定义全局事务的范围:开始全局事务、提交或回滚全局事务。

**RM(Resource Manager)**-资源管理器
管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。

**其中，TC为单独部署的Server服务端，TM和RM为嵌入到应用中的Client客户端。**

#### 常见分布式事务解决方案

1.seata阿里分布式事务框架

2.消息队列

3.saga

4.XA

他们有一个共同点，都是“两阶段(2PC)”。"两阶段"是指完成整个分布式事务，划分成两个步骤完成。实际上，这四种常见的分布式事务解决方案，分别对应着分布式事务的四种模式:AT（auto transaction）、TCC、Saga、XA;
四种分布式事务模式，都有各自的理论基础，分别在不同的时间被提出;每种模式都有它的适用场景，同样每个模式也都诞生有各自的代表产品;
而这些代表产品，可能就是我们常见的(全局事务、基于可靠消息、最大努力通知、TCC)。
今天，我们会分别来看4种模式(AT、TCC、Saga、XA)的分布式事务实现。

**seata，AT模式**

AT模式是无入侵的模式，他会拦截sql请求，然后自动解析sql语句，然后自动生成二阶段的回滚/提交请求

![image-20220518202424914](images\image-20220518202424914.png)

​		在一阶段，Seata 会拦截"业务SQL"，首先解析SQL语义，找到“业务SQL"要更新的业务数据，在业务数据被更新前，将其保存成“before
image”，然后执行"业务SQL"更新业务数据，在业务数据更新之后，再将其保存成"after image"，最后生成行锁。以上操作全部在一个数据库事务内完成,
这样保证了一阶段操作的原子性。

![image-20220518202924164](images\image-20220518202924164.png)

而二阶段如果是提交的话，因为“业务SQL”在一阶段已经提交至数据库，所以Seata框架只需将一阶段保存的快照数据和行锁删掉，完成数据清理即可。

![image-20220518202949489](images\image-20220518202949489.png)

如果第二阶段是回滚：

二阶段如果是回滚的话，Seata就需要回滚一阶段已经执行的"业务SQL"，还原业务数据。回滚方式便是用"before image"还原业务数据;但
在还原前要首先校验脏写，对比"数据库当前业务数据"和“after image”，如果两份数据完全一致就说明没有脏写，可以还原业务数据，如果不一致就说明有
脏写，出现脏就需要转人工处理。

![image-20220518203637786](images\image-20220518203637786.png)

### 2.Seata快速开始

#### 2.1 Seata Server (TC)环境搭建

https://seata.io/zh-cn/docs/ops/deploy-guide-beginner.html

Server端存储模式(store.mode)支持三种:

​	file:单机模式，全局事务会话信息内存中读写并持久化本地文件root.data，性能较高(默认)

​	db:高可用模式，全局事务会话信息通过db共享，相应性能差些

​	redis: Seata-Server 1.3及以上版本支持,性能较高,存在事务信息丢失风险,请提前配置适合当前场景的redis持久化配置

资源目录: https://github.com/seata/seata/tree/1.3.0/script
	**client:**存放client端sql脚本，参数配置

​	**config-center:**各个配置中心参数导入脚本，config.txt(包含server和client，原名nacos-config.txt)为通用参数文件

​	**server**:server端数据库脚本及各个容器配置

1.下载启动包

从https://seata.io/zh-cn/docs/ops/deploy-guide-beginner.html里下载启动包，修改conf目录下的配置文件

![image-20220519140802890](images\image-20220519140802890.png)

![image-20220519140825802](images\image-20220519140825802.png)

2.创建seata数据库

在资源目录: https://github.com/seata/seata/tree/1.3.0/script/server/db中找到mysql.db然后运行

![image-20220519141005859](images\image-20220519141005859.png)

3.修改registry.conf中的配置

![image-20220519141616865](images\image-20220519141616865.png)

![image-20220519141634038](images\image-20220519141634038.png)

4.修改sctipt文件夹中的config.txt文件

![image-20220519142830898](images\image-20220519142830898.png)

将mode改为db

![image-20220519142903304](images\image-20220519142903304.png)

然后这个是配置事务分组，要与客户端配置的事务分组保持一致，其中my_test_tx_group可以自定义,

**Note 版本别下错了**

![image-20220519200941840](images\image-20220519200941840.png)

然后再gitbash中运行如下所示的文件，注意，在nacos-config.sh中要先添加nacos的用户名和密码：

![image-20220519143018249](images\image-20220519143018249.png)

![image-20220519142929834](images\image-20220519142929834.png)

5.然后就可以运行seata了

![image-20220519143058313](images\image-20220519143058313.png)

```
seata-server.bat -p 8091
```

如果想要seata集群部署，很简单，只要修改端口号，多运行几次bat文件就好

```
seata-server.sh -p 8091 -n 1
seata-server.sh -p 8092 -n 2
seata-server.sh -p 8093 -n 3
```

#### 2.2 Seata Client 搭建

1.添加依赖

```
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

2.各服务的数据库添加undo_log数据库,保存sql执行前后的元数据

```
CREATE TABLE `undo_log`(
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`branch_id` bigint(20) NOT NULL,
`xid` varchar(100) NOT NULL,
`context` varchar(128) NOT NULL,
`rollback_info` longblob NOT NULL,
`log_status` int(11) NOT NULL,
`log_created` datetime NOT NULL,
`log_modified` datetime NOT NULL,
PRIMARY KEY ( `id` ),
UNIQUE KEY `ux_undo_log` (`xid` , `branch_id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```

3.配置对应的事务组，注意跟seata服务端的事务组名字保持一致，见2.1

![image-20220519200941840](images\image-20220519200941840.png)



**下面这个版本就是有问题的版本**，会导致出错

![image-20220519192913746](images\image-20220519192913746.png)

```
default_tx_group
```

然后在客户端的配置文件中指定事务组

![image-20220519201524081](images\image-20220519201524081.png)

**下面这个是有问题的**

![image-20220519193007017](images\image-20220519193007017.png)

4.配置seata的注册中心

配置完事务组之后，客户端还是不知道seata的服务在哪里，得配置 客户端seata的注册中心和配置中心，告诉seata client怎么去访问seataserver（协调者）

所以我们要在配置文件里面配置seata的注册中心和配置中心地址

![image-20220519193643399](images\image-20220519193643399.png)

5.seata的运行原理图

![image-20220519202500340](images\image-20220519202500340.png)

其中的before Image 和After Image都会存在Undo_log的表中rollback_info字段中,但是字段是格式是BLOB格式的，不能直接看，所以要用下面的语句转化一下

```
SELECT CONVERT(t.rollback_info USING utf8) from undo_log t 
```

如果分布式事务执行成功，，则TC通知RM异步删除undo_log

![image-20220519203114937](images\image-20220519203114937.png)

如果执行失败，就需要回滚，

1.根据xid找到undo_log中对应的数据

然后生成一个逆向sql，并执行

然后删除Undo_log

![image-20220519203223156](images\image-20220519203223156.png)

## springcloud 微服务网关Gateway组件

### 1.什么是Spring Cloud Gateway

网关作为流量的入口，常用的功能包括路由转发，权限校验，限流等。
Sping Clovd Gateway 是Sping Cloud官方推出的第二代网关框架，定位于取代Nerlic Zuul1.0。相比Zul来说，Spring Cloud Gateway 提供更优秀的性能，更强大的有功能。

Spring Cloud Gateway是由 WebFlux + Netty + Reactor实现的响应式的API网关。它不能在传统的 servlet容器中工作，也不能构建成war包。

Sping Cloud Gateway旨在为微服务架构提供一种简单且有效的API路由的管理方式，并基于Filter的方式提供网关的基本功能，例如说安全认证、监控、限流等等。

#### 1.1其他网关组件：

在SpringLCloud微服务体系中，**有个很重要的组件就是网关**，在1.x版本中都是采用的Zuul网关;但在2.x版预本中，zuul的升级一直跳票，SpringCloud最后自己研发了一个网关替代Zuul,那就是SpringCloud Gateway
阿上很多地方都说Zuul是阻塞的、Gateway是非阻塞的，这种说法是不严谨的，准确的zuul1.x是阻塞的，而在2的版本中，zuul他也是基于Netty，也是非阻塞的，如果一定要说性能，其实这个真没多大差距。
而官方出过一个测试项目，创建了一个benchmark的测试项目: spring-cloud-gateway-bench，其中对比了:

![image-20220520113520632](images\image-20220520113520632.png)

官方文档：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-how-it-works

#### 1.2功能特性：

基于Spring Framework 5, Project Reactor和 Spring Boot 2.0进行构建;

1.动态路由:能够匹配任何请求属性;

2.支持路径重写;

3.集成 Spring Cloud服务发现功能(Nacos、Eruka) ;

4.可集成流控降级功能(Sentinel、Hystrix) ;

5.可以对路由指定易于编写的Predicate (断言）和Filter (过滤器);

核心概念：

路由（route)

路由是网关中最基础的部分，路由信息包括一个ID，一个目的URL，一组断言工厂，一组Filter组成，如果断言为真，则说明请求的URL和配置的路由匹配

断言（predicate）

JAVA8中的断言函数，SpringCloud中的断言函数类型是Spring5.0框架中ServerWebExchange。断言函数允许开发者去定义匹配Http request中的任何信息，比如请求头和参数等。

过滤器（filter）

SpringCloud Gateway中的filter分为Gateway Filter和Global Filter 。Filter可以对请求和响应进行处理。

### 2.gateway使用

#### 2.1 添加依赖

```
<!--        gateway的依赖，springcloud开发-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
```

#### 2.2 添加配置

有三种配置方式：

1.普通版，指定所需要调用的服务ip地址，然后自定义断言和拦截器

```
server:
  port: 8077
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      #路由规则
      routes:
        - id: order_route  #路由的唯一标识，路由到订单，名字可以自定义
          uri: http://localhost:8075 #需要转发的地址
          #断言规则 用于路由规则的匹配
          #一旦路径中包含了/order-service/就说明断言成功，会进行路由转发
          # http://localhost:8077/order-service/order/add -> http://localhost:8075/order-service/order/add
          predicates:
            - Path= /order-service/**
          #但是由于在order服务的controller层接口中，是不存在/order-service/地址的，所有要用过滤器，过滤掉第一层地址
          filters:
            - StripPrefix=1 #转发之前去掉第一层路径
            #http://localhost:8075/order/add
        # - id: stock_route
```

2.集成nacos服务发现

就可以根据服务名来进行路由，还是需要自定义断言和过滤器，**（推荐用法）**

```
server:
  port: 8077
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      #路由规则
      routes:
        - id: order_route  #路由的唯一标识，路由到订单，名字可以自定义
#          uri: http://localhost:8075 #需要转发的地址
          uri: lb://order-seata-alibaba-service #集成nacos后地址就可以改为服务名,lb:load balance 使用nacos的本地负载均衡
          #断言规则 用于路由规则的匹配
          #一旦路径中包含了/order-service/就说明断言成功，会进行路由转发
          # http://localhost:8077/order-service/order/add -> http://localhost:8075/order-service/order/add
          predicates:
            - Path= /order-service/**
          #但是由于在order服务的controller层接口中，是不存在/order-service/地址的，所有要用过滤器，过滤掉第一层地址
          filters:
            - StripPrefix=1 #转发之前去掉第一层路径
            #http://localhost:8075/order/add
        # - id: stock_route
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

3.nacos服务自动发现

这种方式就不用自己配断言器和过滤器了，但是代码的阅读性并不好，而且丢失了自定义断言和过滤器的自由性

```
server:
  port: 8077
#集成nacos版本
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true #是否启动自动识别nacos服务，相当于他会自动用服务名当断言规则，而且会自动去掉第一层地址，一般不用因为不能自定义断言，代码阅读性也不好
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

#### 2.3断言工厂

官网地址：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories

SpringCloud Gateway包括很多内置的断言工厂，所有这些断言都与HTTP请求的不同属性匹配，具体如下：

##### **1.基于Datetime类型的断言工厂此类型的断言根据时间做判断，主要有三个:**

AfterRoutePredicateFactory:接收一个日期参数，判断请求日期是否晚于指定日期

BeforeRoutePredicateFactory:接收一个日期参数，判断请求日期是否早于指定日期

BetweenRoutePredicateFactory:接收两个日期参数，判断请求日期是否在指定时间段内

```
spring:
  cloud:
    gateway:
      routes:
      - id: method_route
        uri: https://example.org
        predicates:
		- After=2019-12-31T23:59:59.789+08:00[Asia/Shanghai]
```

##### **2.基于远程地址的断言工厂**

RemoteAddrRoutePredicateFactory:接收一个IP地址段，判断请求主机地址是否在地址段中

```
- RemoteAddr=192.168.1.1/24
```

##### **3.基于Cookie的断言工厂**

CookieRoutePredicateFactory:接收两个参数，cookie名字和一个正则表达式。判断请求cookie是否具有给定名称且值与正则表达式匹配。 

```
- Cookie=chocolate,ch.p
```

##### **4.基于Header的断言工厂**

HeaderRoutePredicateFactory:接收两个参数，标题名称和正则表达式。判断请求Header是否具有给定名称且值与正则表达式匹配。

逗号后面不要加空格，否则正则表达式会把空格也算上，\d+表示任意数字

```
- Header=x-Request-Id,\d+
```

##### **5.基于Host的断言工厂**

HostRoutePredicateFactory:接收一个参数，主机名模式。判断请求的Host是否满足匹配规则。

```
- Host=**.testhost.org
```

##### **6.基于Method请求方法的断言工厂**

可以接受多个参数，判断请求的方法是否符合要求

```yaml
- Method=GET,POST
```

**7.基于请求路径的断言工厂**

所请求的路径要符合Path中的定义{segment}占位符

```yaml
- Path=/red/{segment},/blue/{segment}
```

##### **8.基于请求参数的断言工厂**

也就是请求的路径中必须要包含所指定的参数，如果需要指定参数值的类型，也可以在后面用正则表达式的形式指定。

例如下面就需要请求路径中包含：

http://localhost:8077/order-seata-alibaba-service/order/add?red=greet就表示断言成功，

http://localhost:8077/order-seata-alibaba-service/order/add?red=gret就表示断言失败

```yaml
- Query=red, gree.
```

##### **9.根据请求权重的断言工厂**

比如下面的配置，有10次请求，会有8次路由到group1，2次路由到group2

```yaml
spring:
  cloud:
    gateway:
      routes:
      - id: weight_high
        uri: https://weighthigh.org
        predicates:
        - Weight=group1, 8
      - id: weight_low
        uri: https://weightlow.org
        predicates:
        - Weight=group1, 2
```

##### **10.自定义断言工厂**

自定义路由断言工厂需要继承AbstractRoutePedicateFactory类，重写apply方法的逻辑。在apply方法中可以通过exchange.getRequest)拿到ServerHttpRequest对象，从而可以获取到请求的参数、请求方式、请求头等信息。

**注意：**

**1.必须是spring组件bean**

**2.类名必须以RoutePedicateFactory结尾**

**3.必须继承AbstractRoutePedicateFactory类**

**4.在类中必须声明一个静态的内部类，生命属性来接受配置文件中对应的断言的信息**

**5.需要结合shortcutFieldOrder进行绑定**

**6.通过apply进行逻辑判断，true就是匹配成功，false就是匹配失败**



**代码实现：**

1.首先自定义一个断言字段

![image-20220520161735877](images\image-20220520161735877.png)

2.实现自定义的路由断言工厂

保证类名是 自定义断言字段名+RoutePredicateFactory

```java
@Component
public class CheckAuthRoutePredicateFactory extends AbstractRoutePredicateFactory<CheckAuthRoutePredicateFactory.Config> {


    public CheckAuthRoutePredicateFactory() {
        super(CheckAuthRoutePredicateFactory.Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder(){
        return Arrays.asList("name");
    }

    @Override
    public Predicate<ServerWebExchange> apply(CheckAuthRoutePredicateFactory.Config config){
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                if (config.getName().equals("kehl")){
                    return true;
                }
                return false;
            }
        };
    }

    //用于接受配置文件中断言的信息
    @Validated
    public static class Config{


        private String name;
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
```

#### **2.4自定义过滤器**

官方文档：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories

##### 1.添加请求头参数

![image-20220520162758590](images\image-20220520162758590.png)

在请求的接口中，就可以接受这个参数：

```
@RequestMapping("/header")
public String header(@RequestHeader("X-Request-red") String color){
    return color;
}
```

##### 2.添加请求参数

```yaml
- AddRequestParameter=red, blue
```

然后请求的接口可以接受这个参数

```
@RequestMapping("/getParam")
public String getParam(@RequestParam("red") String color){
	return color;
}
```

##### 3.为匹配的路由统一添加前缀

如果微服务中配置了context-path，（即上下文路径，可以表示是属于哪个项目的），那么就需要在网关中添加相应的配置。

例如在order-seata-alibaba中配置了context-path

```
server:
  port: 8075
  servlet:
    context-path: /springCloud-order-service
```

那么就需要在网关中配置

![image-20220520164116787](images\image-20220520164116787.png)

这样通过网关请求对应服务时，就不需要加上context-path了，如果直接请求对应服务的话，还是需要加上context-path。

其他过滤器可以去官网上看。

##### 4.自定义内置过滤器

1.自定义filter配置

![image-20220520172848614](images\image-20220520172848614.png)

2.添加自定以的filter类

```
package org.kehl.filters;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RedirectToGatewayFilterFactory;
import org.springframework.cloud.gateway.support.GatewayToStringStyler;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-20 16:48
 **/
@Component
public class CheckAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<CheckAuthGatewayFilterFactory.Config> {
    public static final String STATUS_KEY = "status";
    public static final String URL_KEY = "url";

    public CheckAuthGatewayFilterFactory() {
        super(CheckAuthGatewayFilterFactory.Config.class);
    }

    public List<String> shortcutFieldOrder() {
        return Arrays.asList("name","value");//注意别忘了添加Config中定义的字段名
    }

    public GatewayFilter apply(CheckAuthGatewayFilterFactory.Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                //1.获取name参数
                String name= config.getName();
                String value= config.getValue();
                if (StringUtils.isNotBlank(value)) {
                    //3.如果相等就成功
                    if (config.getValue().equals("kehl")) {
                        return chain.filter(exchange);//注意return
                    } else {
                        //2.如果！=value就失败
                        exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
                        return exchange.getResponse().setComplete();//注意return
                    }
                }
                return  chain.filter(exchange);//注意return
            }
        };
    }

    public static class Config {
        String name;//键
        String value;//键值

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
```

##### 5.全局过滤器

![image-20220520172955028](images\image-20220520172955028.png)

自定义全局过滤器：

```
@Component
public class LogFilter implements GlobalFilter {

    Logger log= LoggerFactory.getLogger(this.getClass());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info(exchange.getRequest().getPath().value());
        return chain.filter(exchange);
    }
}
```

以上用全局过滤器实现了一个日志记录的功能

其实idea当中有一个设置就可以启动Reactor netty访问日志，

```
-Dreactor.netty.http.server.accessLogEnabled=true
```

![image-20220520180127664](images\image-20220520180127664.png)

上面就是通过全局过滤器输出的日志，下面就是Reactor netty输出的日志

![image-20220520180048370](images\image-20220520180048370.png)

#### 2.5gateway跨域

官方文档：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration

1.配置文件

```
#跨域设置
globalcors:
  cors-configurations:
    '[/**]':#允许跨域访问的资源
      allowedOrigins: "*" #跨域允许的来源，在开发阶段就设置为*所有
      allowedMethods:
        - GET
        - POST
```

2.Bean设置

```
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsFilter() {
        corsconfiguration config = new Corsconfiguration();
        config.addAllowedMethod("*"); //允许的method
        config.addAllowedOrigin("*"); //允许的来源
        config.addAllowedHeader("*"); //允许的请求头参数
        //允许访问的资源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
```

#### 2.6 gateway整合sentinel

1.添加依赖

```
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>
```

2.添加配置

```
server:
  port: 8078
spring:
  application:
    name: api-gateway-sentinel
  cloud:
    gateway:
      #路由规则
      routes:
        - id: order_route  #路由的唯一标识，路由到订单，名字可以自定义
          uri: lb://order-service #集成nacos后地址就可以改为服务名,lb:load balance 使用nacos的本地负载均衡
          predicates:
            - Path=/order/**
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
    sentinel:
      transport:
        dashboard: 127.0.0.1:8858

```

![image-20220520203232837](images\image-20220520203232837.png)

网关api名称

![image-20220520203258720](images\image-20220520203320588.png)

流控规则截图：

![image-20220520203400693](images\image-20220520203400693.png)

也可以针对请求的特定属性进行限流，如果请求的参数符合设置的规则，就会对该请求进行限流：

![image-20220520203629900](images\image-20220520203629900.png)

自定义异常：

1.通过GatewayCallbackManager

```
@Configuration
public class GatewayConfig {
    @PostConstruct
    public void init(){
        BlockRequestHandler blockRequestHandler =new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                //自定义异常处理
                HashMap<String,String> map =new HashMap<>();
                map.put("code",HttpStatus.TOO_MANY_REQUESTS.toString());
                map.put("message","限流了");
                return ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(map));

            }
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
```

2.通过yml

```
sentinel:
  transport:
    dashboard: 127.0.0.1:8858
  scg:
    fallback:
      mode: response
      response-body: '{"code":403,"message":"限流了"}'
```

## SkyWalking--微服务链路追踪

链路追踪介绍：

对于一个大型的几十个、几百个微服务构成的微服务架构系统，通常会遇到下面一些问题，比如:
1.如何串联整个调用链路，快速定位问题?

2.如何缕清各个微服务之间的依赖关系?

3.如何进行各个微服务接口的性能分折?

4.如何跟踪整个业务流程的调用处理顺序?

### 1、skywalking是什么

#### 1.1 概况

 Skywalking是一个国产开源框架,，2015年由吴晟开源，217年加入Apache孵化器。**Skywalking是分布式系统的应用程序性能监视工具，专为微服务、云原生架构和基于容器(Docker、K8s、Mesos)架构而设计**。SkyWalking是观察性分析平台和应用性能管理系统，提供分布式追踪、服务网格遥测分析、度量聚合和可视化一体化解决方案。

官网: http://skywalking.apache.org/

下载: http://skywalking.apache.org/downloads/

Github: https://github.com/apache/skywalking

文档: https://skywalking.apache.org/docs/main/v8.4.0/readme/

中文文档: https://skyapm.github.io/document-cn-translation-of-skywalking/

版本: v8.3.0升级到v8.4.0

#### 1.2链路追踪框架对比

1.Zipkin是Twitter开源的调用链分析工具，目前基于springcloud sleuth得到了广泛的使用，特点是轻量，使用部署简单。

2.Pinpoint是韩国人开源的基于字节码注入的调用链分析，以及应用监控分析工具。特点是支持多种插件，UI功能强大，接入端无代码侵入。

3.SkyWaking是本土开源的基于字节码注入的调用链分析，以及应用监控分析工具。特点是支持多种插件，U功能较强，接入端无代码侵入。目前已加入Apac
e孵化器。
4.CAT是大众点评开源的基于编码和配置的调用链分析，应用监控分析，日志采集，监控报警等一系列的监控平台工具。

![image-20220520212159947](images\image-20220520212159947.png)

#### 1.3性能对比

模拟了三种并发用户:500，750，1000。使用jmeter测试，每个线程发送30个请求，设置思考时间为10ms。使用的采样率为1，即100%，这边与生产可能有
差别。pinpoint默认的采样率为20，即50%，通过设置agent的配置文件改为100%。zipkin默认也是1。组合起来，一共有12种。下面看下汇总表:

![image-20220520212539618](images\image-20220520212539618.png)

从上表可以看出，在三种链路监控组件中，**skywalking的探针对吞吐量的影响最小，zipkin的吞吐量居中。pinpoint的探针对吞吐量的影响较为明显**，在500并发用户时，测试服务的吞吐量从1385降低到774，影响很大。然后再看下CPU和memory的影响，在内部服务器进行的压测，对CPU和memory的影响都差不多在10%之内。

#### 1.4 Skywalking的主要功能特性

1、多种监控手段，可以通过语言探针和service mesh获得监控的数据;

2、支持多种语言自动探针，包括Java，.NET Core和Node.JS;

3、轻量高效，无需大数据平台和大量的服务器资源;

4、模块化，UI、存储、集群管理都有多种机制可选;

5、支持告警;

 6、优秀的可视化解决方案;

### 2.环境搭建

![image-20220520213406705](images\image-20220520213406705.png)

1.skywalking agent和业务系统绑定在一起，负责收集各种监控数据

2.Skywalking oapservice是负责处理监控数据的，比如接受skywalking agent的监控数据，并存储在数据库中;接受skywalking webapp
3.的前端请求，从数据库查询数据，并返回数据给前端。Skywalking oapservice通常以集群的形式存在。

skywalking webapp，前端界面，用于展示数据。

4.用于存储监控数据的数据库，比如mysql、elasticsearch等。

#### 2.1服务端搭建

1.下载SkyWalking

下载: http://skywalking.apache.org/downloads/

![image-20220520213644954](images\image-20220520213644954.png)

**目录结构：**

webapp: UI前端(web 监控页面)的jar包和配置文件;

oap-libs:后台应用的jar包，以及它的依赖jar包，里边有一个serve-starter-*.jar就是启动程序;
config:启动后台应用程序的配置文件，是使用的各种配置
bin:各种启动脚本，一般使用脚本startup.*来启动**web页面**和对应的后台应用;
	oapService:默认使用的后台程序的启动脚本;(使用的是默认模式启动，还支持其他模式，各模式区别见启动模式)
    oapServicelnit.*:使用init模式启动;在此模式下，OAP服务器启动以执行初始化工作，然后退出
    oapServiceNolnit :使用no init模式启动;在此模式下，OAP服务器不进行初始化。
    webappService.*: UI前端的启动脚本;
    startup.*:组合脚本，同时启动oapService.*:、webappService.*脚本;
agent:
    skywalking-agent.jar:代理服务jar包

​	config:代理服务启动时使用的配置文件
​    plugins:包含多个插件，代理服务启动时会加载改目录下的所有插件（实际是各种jar包)
​    optional-plugins:可选插件，当需要支持某种功能时，比如SpringCloud Gateway，则需要把对应的jar包拷贝到plugins目录下;

**2点击startup.bat进行启动**

![image-20220520214737969](images\image-20220520214737969.png)

启动成功后会启动两个服务，一个是skywalking-oap-server，一个是skywalking-web-ui : 8868
skywalking-oap-servar服务启动后会暴露11800和12800两个端口，分别为收集监控数据的端口1180Q和接受前端请求的端口12800，修改
端口可以修改
config、applicaiton.yml

#### 2.2.客户端搭建

Skywalking跨多个微服务跟踪，只需要每个微服务启动时添加javaagent参数即可,注意修改-DSW_AGENT_NAME为各个的微服务名

```
-javaagent:E:\develop\apache-skywalking-apm-bin-es7\agent\skywalking-agent.jar
-DSW_AGENT_NAME=api-gateway-sentinel
-DSW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
```

注意：跟踪链路默认是不显示gateway

要拷贝agent/optional-plugins目录下面的gateway插件到agent/plugins目录

![image-20220522121112959](images\image-20220522121112959.png)

**然后重启各个服务**，调用一下接口，就会显示getaway路径了

![image-20220522122118670](images\image-20220522122118670.png)

#### 2.3 Skywalking持久化跟踪数据

默认使用H2数据库存储

config/application.yml

![image-20220522122257733](images\image-20220522122257733.png)

##### 2.3.1基于mysql的持久化

1.修改config 目录下的application.yml，使用mysql作为持久化存储的仓库

修改存储方式为mysql

![image-20220522122531245](images\image-20220522122531245.png)

2.修改mysql配置，**需要手动去创建一下数据库**

![image-20220522122634991](images\image-20220522122634991.png)

保存之后重启一下，这时候会发现启动失败了，去查看日志文件，会发现出现了如下的错误：

![image-20220522142710423](images\image-20220522142710423.png)

![image-20220522142649326](images\image-20220522142649326.png)

原因是因为，skywalking的oap-libs的jar包中，没有mysql的驱动，去把项目中的mysql驱动复制一份到skywalking的oap-libs文件夹中即可

![image-20220522143052572](images\image-20220522143052572.png)

![image-20220522143122340](images\image-20220522143122340.png)

#### 2.4自定义链路追踪

1.添加依赖

```
<!--skywalking工具类-->
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-trace</artifactId>
    <version>8.5.0</version>
</dependency>
```

2.@Trace注解

在业务方法上添加一个@Trace注解，就可以对它进行链路追踪

![image-20220522145936764](images\image-20220522145936764.png)

3.记录返回值和对应参数

**注意：**

**1.@Trace和@Tag要一起出现，只有@Tag是没用的**

2.返回响应结果：key是名字，可以自定义，**value必须是returnedObj，不能改**

3.如果想要得到参数，就用arg+下标的方式

```
@Override
@Trace
@Tag(key="all",value="returnedObj")
public List<Order> all(){
    return orderMapper.selectAll();
}

@Override
@Trace
@Tags({@Tag( key="Order",value="returnedObj"),
        @Tag(key="param",value="arg[0]"),
})
public Order getById(Integer id) {
    return orderMapper.selectByPrimaryKey(id);
}
```

![image-20220522152025773](images\image-20220522152025773.png)

这里会发现，红色框中出现的并不是我们想要看到的返回值，而是一串神秘数字，这时候。我们需要在Order实体类中添加ToString方法，然后重新运行服务

![image-20220522152830937](images\image-20220522152830937.png)

![image-20220522152907193](images\image-20220522152907193.png)

就可以看到具体的值了

#### 2.5性能剖析

1.新建任务

![image-20220522153251644](images\image-20220522153251644.png)

然后请求就可以了

#### 2.6 SkyWalking集成日志框架

logback 配置：https://skywalking.apache.org/docs/main/v8.5.0/en/setup/service-agent/java-agent/application-toolkit-logback-1.x/

log4j官方配置

log4j2j官方配置

以logback为例

想要记录skywalking相关的记录

1.在对应的微服务中（本例就是在order-seata-alibaba中）添加依赖

```
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-logback-1.x</artifactId>
    <version>8.5.0</version>
</dependency>
```

2.在resource下创建logback-spring.xml(名字不要写错了，这是约定的名字）,并配置%tid占位符

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration >
    <!--引入SpringBoot 默认的logback xml配置文件-->
    <include resource="org/springframework/boot/logging/logback/default.xml"/>

    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <!--日志格式化-->
            <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
                <Pattern>${CONSOLE_LOG_PATTERN}</Pattern>
            </layout>
        </encoder>
    </appender>
    <!--设置Appender-->
    <root level="INFO">
        <appender-ref ref="console"/>
    </root>
</configuration>
```

3.然后请求一个接口，就会出现对应的trace Id，用这个tid，就可以去skywalking中找对应的链路

![image-20220522161110865](images\image-20220522161110865.png)

4.将idea控制台日志反推到sky walking控制台

1.修改logback-spring.xml文件

```java
<!--    将idea控制台的日志荣国grpc的方式反推到skywalking的页面中-->
    <appender name="grpc-log" class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.log.GRPCLogClientAppender">
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.mdc.TraceIdMDCPatternLogbackLayout">
                <Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{tid}] [%thread] %-5level %logger{36} -%msg%n</Pattern>
            </layout>
        </encoder>
    </appender>
```

**别忘了在下面添加append**

![image-20220522161615361](images\image-20220522161615361.png)

然后就可以在控制台上看到时间，如果点击搜索发现为空，就改一下时间范围。

![image-20220522161557901](images\image-20220522161557901.png)



如果sky walking没有部署在本地，要修改一下如下的配置文件

![image-20220522162000399](images\image-20220522162000399.png)

这是默认的配置，修改为对应服务器的地址就可以了

```properties
plugin.toolkit.log.grpc.reporter.server_host=${SW_GRPC_LOG_SERVER_HOST:127.0.0.1}
plugin.toolkit.log.grpc.reporter.server_port=${SW_GRPC_LOG_SERVER_PORT:11800}
plugin.toolkit.log.grpc.reporter.max_message_size=${SW_GRPC_LOG_MAX_MESSAGE_SIZE:10485760}
plugin.toolkit.log.grpc.reporter.upstream_timeout=${SW_GRPC_LOG_GRPC_UPSTREAM_TIMEOUT:30}
```

![image-20220522162015297](images\image-20220522162015297.png)

#### 2.7告警功能

SkyWalking告警功能是在6.x版本新增的，其核心由一组规则驱动，这些规则定义在**config/alarw-settings.yml**文件中。

**告警规则的定义分为两部分**:
1.告警规则:它们定义了应该如何触发度量警报，应该考虑什么条件。
2.Webhook (网络钩子)︰定义当警告触发时，哪些服务终端需要被告知，就是可以向我自定义的接口发送一个请求，这样我们可以实现比如告警的时候给我们发送邮件的功能，

官方文档地址：https://github.com/apache/skywalking/blob/v8.5.0/docs/en/setup/backend/backend-alarm.md

skywalking的告警消息会通过 HTTP请状进行发送，请求方法为POST,Content-Type为aplication/json，其JSON数据实基于List<org.apache.skywalking.oap.server.core.alarm.AlarmMessage>进行序列化的。Json数据实例：

```
[{
	"scopeId": 1, 
	"scope": "SERVICE",
	"name": "serviceA", 
	"id0": "12",  
	"id1": "",  
    "ruleName": "service_resp_time_rule",
	"alarmMessage": "alarmMessage xxxx",
	"startTime": 1560524171000
}, {
	"scopeId": 1,
	"scope": "SERVICE",
	"name": "serviceB",
	"id0": "23",
	"id1": "",
    "ruleName": "service_resp_time_rule",
	"alarmMessage": "alarmMessage yyy",
	"startTime": 1560524171000
}]
```

**告警规则：**
SkyWIalking 的发行版都会默认提供config/alarm-settings.yml文件，里面预先定义了一些常用的告警规则。如下:
1.过去3分钟内服务平均响应时间超过1秒。

2.过去2分钟服务成功率低于80%。

3.过去3分钟内服务响应时间超过1s 的百分比

4.服务实例在过去2分钟内平均响应时间超过1s，并且实例名称与正则表达式匹酉

5.过去2分钟内端点平均响应时间超过1秒。

6.过去2分钟内数据库访问平均响应时间超过1秒。

7.过去2分钟内端点（endpoint，就是服务端的响应地址）关系平均响应时间超过1秒。
这些预定义的告警规则，打开config/alarm-settings.ym1文件即可看到

字段说明:
scopeld、scope:所有可用的Scope 详见 org.apache. skywalking.oap. server.core.source.DefaultScopeDefine

name:目标Scope的实体名称

id0: Scope 实体的ID

id1:保留字段，目前暂未使用

ruleName:告警规则名称

alarmMessage:告警消息内容

startTime:告警时间，格式为时间戳



测试告警规则：

官方文档：https://github.com/apache/skywalking/blob/master/docs/en/setup/backend/backend-alarm.md

1.在config/alarw-settings.yml中添加钩子接口

![image-20220522165609833](images\image-20220522165609833.png)

2.在gateway项目中添加实体类和接口

![image-20220522165440953](images\image-20220522165440953.png)

实体类代码：

```
@Getter
@Setter
public class SwAlarmDTO {
    private int scopeId;
    private String scope;
    private String name;
    private String id0;
    private String id1;
    private String ruleName;
    private String alarmMessage;
    private List<Tag> tags;
    private long startTime;
    private transient int period;
    private transient boolean onlyAsCondition;

    @Data
    public static class Tag{
        private String key;
        private String value;
    }
}
```

接口代码：

```
@RequestMapping("/alarmnotify")
public void notify(@RequestBody List<SwAlarmDTO> alarmDTOList){
    String content=getContent(alarmDTOList);
    log.info("告警邮件已发送..."+content);
}

private String getContent(List<SwAlarmDTO>  alarmDTOList) {
    StringBuilder result = new StringBuilder();
    for (SwAlarmDTO dto : alarmDTOList) {
        result.append("scopeId: ").append(dto.getScopeId())
                .append("\nscope: ").append(dto.getScope())
                .append("\n目标Scope 的实体名称:").append(dto.getName()).append("\nScope 实体的ID: ").append(dto.getId0())
                .append("\nid1: ").append(dto.getId1())
                .append("\n告警规则名称: ").append(dto.getRuleName())
                .append("\n告警消息内容: ").append(dto.getAlarmMessage()).append("In告警时间: ").append(dto.getStartTime())
                .append("\n标签: ").append(dto.getTags())
                .append("\n\n---------------\n\n");
    }
    return result.toString();
}
```

3.测试结果

![image-20220522171249920](images\image-20220522171249920.png)

![image-20220522165625172](images\image-20220522165625172.png)

#### 2.8 Skywalking 高可用

在大多数生产环境中，后端应用需要支持高吞吐量并且支持高可用来保证服务的稳定，所以你始终需要在生产环境进行集群管理。
Skywalking集群是将skywalking oap作为一个服务注册到naco上，只要skywalking oap服务没有全部宕机，保证有一个skywalking oap在运行，就能进行跟踪。搭建一个skywalking oap集群需要:
(1)至少一个Nacos (也可以是nacos集群)

(2)至少一个ElasticSearch/mysql (也可以是es/msql集群)

(3）至少2个skywalking oap服务;

(4)至少1个UI (UI也可以集群多个，用Nginx代理统一入口)

1.修改config/application.yml文件

使用nacos作为注册中心

![image-20220522170053639](images\image-20220522170053639.png)

2.修改nacos配置

![image-20220522170116819](images\image-20220522170116819.png)

可以选择性的修改监听端口和存储策略

![image-20220522170339186](images\image-20220522170339186.png)

![image-20220522170353629](images\image-20220522170353629.png)

![image-20220522170447290](images\image-20220522170447290.png)

2.配置UI服务webapp.yml文件的listOfservers,写两个不同的oap地址地址

![image-20220522170428172](images\image-20220522170428172.png)

3.启动服务测试

启动Skywalking服务，指定springboot应用的jvm参数

```
-DSW_AGENT_COLLECTOR_BACKEND_SERVICES=192.168.3.10:11800,192.168.3.12:11800
```

