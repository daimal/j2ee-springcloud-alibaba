# j2ee-springcloud-alibaba
# J2EE课程记录
SpringCloud-Alibaba整体架构图

## 环境安装

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

#### **2.1.nacos 单机模式**

```
docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --restart=always -e MODE=standalone -e JVM_XMS=200m -e JVM_XMX=200m -e JVM_XMN=200m --name nacos nacos/nacos-server:2.0.2
```

这条语句会自动下载nacos镜像

```
docker stop nacos &&docker rm nacos
```

如果要创建其他模式的nacos 的话，可以先把之前创建的容器先停掉然后删除

#### **2.2.nacos mysql 模式**

下面的代码要做一些修改，password要改为自身数据库的密码，host要改为数据库的host

```
docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --restart=always -e MODE=standalone -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_PASSWORD=password -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=host --name nacos nacos/nacos-server:2.0.2

docker run -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --restart=always -e MODE=standalone -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_PASSWORD=123456 -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=localhost --name nacos nacos/nacos-server:2.0.2

docker run -d -p 8848:8848 --restart=always -e MODE=standalone -e MYSQL_SERVICE_PORT=3306 -e MYSQL_SERVICE_USER=root -e MYSQL_SERVICE_DB_NAME=nacos -e MYSQL_SERVICE_PASSWORD=123456 -e SPRING_DATASOURCE_PLATFORM=mysql -e MYSQL_SERVICE_HOST=localhost --name nacos nacos/nacos-server:1.4.1

```

查看nacos 日志

docker logs -f nacos

### 3.安装Sentinel
sentinel是阿里巴巴开源的，面向分布式服务架构的高可用防护软件。

随着微服务的流行，服务和服务之间的稳定性变得越来越重要。Seniel,是面向分布式服务架构的流量控制组件，主要以流量为切入点，从**限流、流量整形、熔断降级、系统负载保护、热点防护**等多个维度来帮助开发者保障微服务的稳定性。

源码地址: https://github.com/alibaba/Sentinel

官方文档: https://github.com/alibaba/Sentinel/wiki

#### **3.1 控制台方式**
引入依赖
<!--sentinel 组件-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
 然后下载官方文档中的启动包就可以了
</dependency>
