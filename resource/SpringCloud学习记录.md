# SpringCloud学习记录

## 学习路线图

![](pictures\springcloud组件.png)

## 开发步骤

模块开发

1. 新建模块；
2. 修改pom文件；
3. 配置yml文件；
4. 新建主启动类；
5. 业务类编写。
   1. 建表；
   2. entity；
   3. dao；
   4. service；
   5. controller。

## cloud-api-common

将各模块重复使用的实体、工具类单独提出为一个模块，一处改动多处受益。

将此模块`mvn clean install`编译完成后，在其他模块pom文件引入，即可使用公共类。

```xml
<!--将通用组件抽离为公共模块，在其他模块调用这个公共模块-->
<dependency>
    <groupId>com.feng.springcloud</groupId>
    <artifactId>cloud-api-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

## cloud-provider-payment

支付模块

### 支付集群

相同模块，只需要修改yml文件的`port`，并启动所有支付节点。具体参考测试代码部分。

## cloud-consumer-order

客户端模块。

### 调用

#### RestTemplate

##### 简介

使用RestTemplate进行其他服务模块的调用，使用`RestTemplte`需要先注册到spring容器中，可使用自动配置类进行注册。

常用API：

- postForObject：将json数据写入到调用服务中
- getForObject：从其他服务返回json数据
- postForEntity：通过将给定对象 POST 到 URI 模板来创建新资源，并将响应返回为[`ResponseEntity`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseEntity.html)。简单说就是包含响应头，响应编码，响应内容
- getForEntity：从其他服务返回ResponseEntity。

##### 使用

@LoadBalanced注解表示支持负载均衡。

```java
/**
 * Description: RestTemplateConfig自动配置类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 15:13
 * @since JDK 1.8
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

使用如下：

```java
/**
 * 使用RestTemplate 的方式从订单客户端访问支付服务
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 15:15
 * @since JDK 1.8
 */
@RestController
public class OrderController {
    //private static final String PAYMENT_URL = "http://localhost:8001";
    //支付服务构建为多节点集群时，客户端调用不能在配置指定路径，而直接指定对应的微服务名称
    //使用微服务名称后，需要开启RestTemplate负载均衡功能，在注册bean时添加注解 @LoadBalanced
    private static final String PAYMENT_URL = "http://CLOUD-PROVIDER-PAYMENT/";
    @Resource
    private RestTemplate restTemplate;

    @GetMapping("order/payment/create")
    public CommonResult<Payment> create(Payment payment) {
        return restTemplate.postForObject(PAYMENT_URL + "payment/create", payment, CommonResult.class);
    }

    @GetMapping("order/payment/queryById/{id}")
    public CommonResult<Payment> queryById(@PathVariable("id") Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "payment/queryById/" + id, CommonResult.class);
    }

    @GetMapping("order/payment/create2")
    public CommonResult<Payment> create2(Payment payment) {
        ResponseEntity<CommonResult> entity = restTemplate.postForEntity(PAYMENT_URL + "payment/create", payment, CommonResult.class);
        if (entity.getStatusCode().is2xxSuccessful()){
            return entity.getBody();
        }else {
            return new CommonResult<>(500, "服务调用失败");
        }
    }

    @GetMapping("order/payment/queryById2/{id}")
    public CommonResult<Payment> queryById2(@PathVariable("id") Long id) {
        ResponseEntity<CommonResult> entity = restTemplate.getForEntity(PAYMENT_URL + "payment/queryById/" + id, CommonResult.class);
        if (entity.getStatusCode().is2xxSuccessful()){
            return entity.getBody();
        }else {
            return new CommonResult<>(500, "服务调用失败");
        }
    }
}
```

## 服务注册

### 差异点

常见的服务注册组件差异点如下图。

![](pictures\注册中心差异点.png)

### Eureka

![](pictures\Euraka服务.png)

#### 使用

![](pictures\Eureka运作说明.png)

##### 服务端

- pom文件

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

- yml文件

```yaml
server:
  port: 7001

eureka:
  instance:
    hostname: localhost
  client:
    #服务端不需要注册自动
    register-with-eureka: false
    #服务端不需要检索服务
    fetch-registry: false
    service-url:
      #设置与Euraka服务端地址查询与注册服务的地址
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

- 主启动类

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerMain {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerMain.class, args);
    }
}
```

##### 客户端

- pom文件

```xml
<dependency>
    <!--Eureka 客户端依赖-->
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

- yml文件

```yaml
eureka:
  client:
    #将当前服务注册到Eureka服务端中
    register-with-eureka: true
    #客户端单机默认值为true，使用集群时配合Ribbon负载均衡时一定需要配置
    fetch-registry: true
    service-url:
      #设置与Eureka服务端查询与注册服务的地址
      defaultZone: http://localhost:7001/eureka/
```

- 主启动类

```java
@SpringBootApplication
@EnableEurekaClient
public class PaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain.class, args);
    }
}
```

#### 集群

未来避免单机故障，`Eureka`可以使用集群方案。

以两个节点为例，重点需要修改两个节点的yml文件，指定集群的其他节点注册路径，将当前节点加入到集群中。

**具体查看代码。**

##### 服务端

节点1

```yaml
server:
  port: 7001

# Eureka集群构建情况下，注释掉单机服务器配置，而将当前服务注册到集群其他节点之中
eureka:
  instance:
    #hostname: localhost
    hostname: eureka.server7001.com
  client:
    #服务端不需要注册自动
    register-with-eureka: false
    #服务端不需要检索服务
    fetch-registry: false
    service-url:
      #设置与Euraka服务端地址查询与注册服务的地址
      #defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
      defaultZone: http://eureka.server7002.com:7002/eureka/
```

节点2

```yaml
server:
  port: 7002

spring:
  application:
    name: eureka-server-7002

eureka:
  instance:
    #通过修改设置本级hosts文件中，新增Eureka集群地址映射
    hostname: eureka.server7002.com
  client:
    #服务端不需要注册自动
    register-with-eureka: false
    #服务端不需要检索服务
    fetch-registry: false
    service-url:
      #设置与Euraka服务端地址查询与注册服务的地址
      # Eureka集群下，多个服务端互相注册
      defaultZone: http://eureka.server7001.com:7001/eureka/
```

##### 客户端

客户端需要指定所有的Eureka集群中每个节点的注册地址

```yaml
eureka:
  client:
    #将当前服务注册到Eureka服务端中
    register-with-eureka: true
    #客户端单机默认值为true，使用集群时配合Ribbon负载均衡时一定需要配置
    fetch-registry: true
    service-url:
      #设置与Eureka服务端查询与注册服务的地址
      # Eureka单机服务注册地址
      #defaultZone: http://localhost:7001/eureka/
      # Eureka集群版注册地址，指向所有Eureka服务端节点
      defaultZone: http://eureka.server7001.com:7001/eureka/, http://eureka.server7002.com:7002/eureka/
```

#### 负载均衡

`Eureka`整合`Ribbon`实现业务集群访问的负载均衡。

启动支付模块多个节点，注册到Eureka同一个服务下。

`RestTemplate`组件开启负载均衡功能，并且不再指定url，而是指定注册在Eureka上的服务名。

```java
//private static final String PAYMENT_URL = "http://localhost:8001";
//支付服务构建为多节点集群时，客户端调用不能在配置指定路径，而直接指定对应的微服务名称
//使用微服务名称后，需要开启RestTemplate负载均衡功能，在注册bean时添加注解 @LoadBalanced
private static final String PAYMENT_URL = "http://CLOUD-PROVIDER-PAYMENT/";
```

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    //负载均衡注解
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

Ribbon默认使用**轮循**的负载均衡算法。

客户端通过 http://localhost/order/payment/queryById/1调用支付集群服务，将会循环出现8001，8002两个端口，负载均衡得到验证。

#### 服务发现

服务发现指注册在`Eureka`服务器上的服务可以获取到注册在Eureka服务器上的信息。

- @EnableDiscoveryClient

  开启Eureka服务发现，可以通过DiscoveryClient获得Eureka服务器上注册的信息

- DiscoveryClient

  通过`DiscoveryClient`对象可以获得服务列表或者获得对应服务的实例集合

  ```java
  List<String> services = discoveryClient.getServices();
  for (String s : services) {
      System.out.println(s);
  }
  List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PROVIDER-PAYMENT");
  for (ServiceInstance instance : instances) {
      System.out.println(instance.getInstanceId() + "\t" + instance.getHost() + "\t" + instance.getPort() + "\t" + instance.getUri());
  }
  ```


#### 自我保护机制

Eureka的自我保护机制是分布式微服务CAP理论下，为了实现AP的策略，某一时刻大量客户端由于网络波动等原因导致没有及时发送心跳，服务端不会立刻将这些没有续期的服务剔除，而是保留他们，

**关闭自我保护**

服务端

application.yml

```yaml
eureka:
  server:
    #关闭Eureka自我保护
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 2000
```

客户端

application.yml

```yaml
eureka:
  instance:
    #客户端向Eureka服务端发送心跳的时间间隔
    lease-renewal-interval-in-seconds: 1
    #Eureka服务端等待客户端发送心跳时间
    lease-expiration-duration-in-seconds: 2
```

#### 踩坑

```
2021-06-09 12:35:30.749 ERROR 6800 --- [nfoReplicator-0] c.n.d.s.t.d.RedirectingEurekaHttpClient  : Request execution error. endpoint=DefaultEndpoint{ serviceUrl='http://localhost:8761/eureka/}

com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: Connection refused: connect
```

启动`Eureka`服务端后，启动客户端服务时报此异常

说明Eureka客户端并没有使用自定义的服务端注册地址，而是使用了默认的`8761`端口。

查看`application.yml`文件发现配置问题**defaultzone这里竟然写成小写了，正确写法应该是defaultZone**

```yaml
  client:
    #将当前服务注册到Eureka服务端中
    register-with-eureka: true
    #客户端单机默认值为true，使用集群时配合Ribbon负载均衡时一定需要配置
    fetch-registry: true
    service-url:
      #设置与Eureka服务端查询与注册服务的地址
      defaultzone: http://localhost:7001/eureka/
```

### Consul

文档：https://www.springcloud.cc/spring-cloud-consul.html

#### 安装启动

下载地址：https://www.consul.io/downloads

以windows版本为例，下载解压缩后，双击consul.exe文件完成安装。

使用开发者模式，即consul.exe文件路径下进入控制台，输入`consul agent -dev`启动consul服务。

![](pictures\consul_cmd.png)

访问 http://localhost:8500/ 

展示下图页面，启动成功。

![](pictures\consul ui.png)

#### 使用

pom文件添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
```

yml文件配置consul为注册中心

```yaml
server:
  port: 8006

spring:
  application:
    name: cloud-provider-payment
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}

```

主启动类添加注解

```java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentMain8006 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8006.class, args);
    }
}
```

启动主启动类后 http://localhost:8500/查看服务注册是否成功。

![](pictures\consul_注册成功.png)

## 服务调用

### Ribbon

#### 简介

微服务架构下，通过集成在消费方的`Ribbon`实现软性的负载均衡，具体：通过消费方本地`Ribbon+RestTemplate`实现远程**RPC**调用。

> 消费方负载均衡策略：Ribbon等；服务提供方负载均衡：nginx，F5等。

#### 使用

pom文件

> Eureka客户端已经包含Ribbon依赖，不需要额外引用。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    <version>2.2.1.RELEASE</version>
    <scope>compile</scope>
</dependency>
```

#### 过程

![](pictures\Ribbon实现负载均衡策略.png)

以服务注册中心为`Eureka`为例：

1. `Ribbon`首先选择`Eureka Server`，它将优先选择负载较小的Server；
2. 根据服务名在`Eureka Server`中找到服务集群中对应的服务实例，可通过用户配置**轮循、随机、加权**等负载均衡策略。

#### 负载均衡策略

前文提到，用户可以设置不同的负载均衡策略，如轮循、随机、加权等，此处进行简单介绍及使用。

##### IRule

IRule为Ribbon负载均衡策略的上层接口，多种具体策略都是对这个接口的实现。

![](D:/work/springcloud/resource/pictures/IRule%E7%BB%A7%E6%89%BF%E5%85%B3%E7%B3%BB%E5%9B%BE.png)

- RoundRobinRule：轮循策略；
- RandomRule：随机策略；
- RetryRule：重试策略。对选定的负载均衡策略机上重试机制，在一个配置时间段内当选择Server不成功，则一直尝试使用subRule的方式选择一个可用的server；
- BestAvailableRule：最低并发策略。逐个考察server，如果server断路器打开，则忽略，再选择其中并发链接最低的server
- AvailabilityFilteringRule：可用过滤策略。过滤掉一直失败并被标记为circuit tripped的server，过滤掉那些高并发链接的server（active connections超过配置的阈值）或者使用一个AvailabilityPredicate来包含过滤server的逻辑，其实就就是检查status里记录的各个Server的运行状态
- ZoneAvoidanceRule：区域权重策略。综合判断server所在区域的性能，和server的可用性，轮询选择server并且判断一个AWS Zone的运行性能是否可用，剔除不可用的Zone中的所有server。

**特别的，对轮循算法核心取模做分析**：

```java
//此处使用多个多线程核心概念
private int incrementAndGetModulo(int modulo) {
    //自旋锁
    for (;;) {
        //nextServerCyclicCounter此对象为AtomicInteger 原子类
        int current = nextServerCyclicCounter.get();
        int next = (current + 1) % modulo;
        //cas 比较并交换
        if (nextServerCyclicCounter.compareAndSet(current, next))
            return next;
    }
}
```

##### 操作

1. 添加自动配置类进行策略替换。需要注意的是：**此自动配置类不可位于@ComponentScan路径下，否则将会被全局替换，造成替换策略失效。**

```java
/**
 * Ribbon替换负载均衡策略自动配置类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/13 15:12
 * @since JDK 1.8
 */
@Configuration
public class MySelfRule {

    /**
     * 自定义Ribbon负载均衡算法，此处使用随机算法
     * @return IRule the IRule
     */
    @Bean
    public IRule randomRule(){
        return new RandomRule();
    }
}
```

2. 客户端主启动类添加`@RibbonClient`注解。

```java
/**
 * Description: 订单服务主启动类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/6/8 12:24
 * @since JDK 1.8
 */
@SpringBootApplication
@RibbonClient(value = "CLOUD-PROVIDER-PAYMENT", configuration = MySelfRule.class)
public class OrderMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain.class, args);
    }
}
```

### OpenFeign

#### 简介

`Feign`是web Service服务调用组件。对需要调用接口方法的声明，通过动态代理的方式实现对其他微服务接口的调用，`OpenFeign`集成了`Ribbon`，可自动实现客户端的负载均衡。

#### 使用

pom

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

yml

```yaml
server:
  port: 80

eureka:
  client:
    register-with-eureka: false
    service-url:
      #设置与Eureka服务端查询与注册服务的地址
      # Eureka单机服务注册地址
      #defaultZone: http://localhost:7001/eureka/
      # Eureka集群版注册地址，指向所有Eureka服务端节点
      defaultZone: http://eureka.server7001.com:7001/eureka/, http://eureka.server7002.com:7002/eureka/
```

主启动类添加`@EnableFeignClients`注解

```java
@SpringBootApplication
@EnableFeignClients
public class FeignOrderMain80 {
    public static void main(String[] args) {
        SpringApplication.run(FeignOrderMain80.class, args);
    }
}
```

新增接口声明，添加`@FeignClient`注解，指定调用的服务名称。

```java
/**
 * 创建调用接口方法声明，使用OpenFeign动态代理实现其他服务接口调用
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/17 15:50
 * @since JDK 1.8
 */
@Component
@FeignClient(value = "CLOUD-PROVIDER-PAYMENT")
public interface PaymentService {
    /**
     * 根据主键查询支付单据
     *
     * @param id id
     * @return CommonResult CommonResult
     */
    @GetMapping(value = "payment/queryById/{id}")
    CommonResult<Payment> queryById(@PathVariable("id") Long id);
}
```

正常调用

```java
@RestController
@Slf4j
public class OrderController {
    @Resource
    PaymentService paymentService;
    
    /**
     * 根据主键查询支付单据
     *
     * @param id id
     * @return CommonResult CommonResult
     */
    @GetMapping(value = "consumer/payment/queryById/{id}")
    public CommonResult<Payment> queryById(@PathVariable("id") Long id){
        return paymentService.queryById(id);
    }
}
```

#### 超时控制

由于`OpenFeign`默认超时时间是**1秒**，超时将会报错，因此在长业务流程调用时，需要自定义其超时时间。由于`OpenFeign`底层是集成`Ribbon`的，因此设置超时时间即设置`Ribbon`的超时时间。

```yaml
ribbon:
  #建立连接后读时间
  ReadTimeout: 5000
  #建立连接时间
  ConnectTimeout: 5000
```

#### 日志打印

`OpenFeign`提供其调用服务时日志监控的功能。

其提供多种级别的日志

- NONE：默认不展示日志；
- BASIC：仅记录请求方法、url、响应编码及执行时间；
- HEADERS：除了BIASIC包含的信息外，还包含请求和响应头信息；
- FULL：除了HEADERS包含的信息外，还有请求及响应的正文及数据

##### 使用

添加自动配置类，注册日志级别组件

```java
/**
 * Feign日志级别配置类
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/8/17 17:07
 * @since JDK 1.8
 */
@Configuration
public class FeignLogConfiguration {
    @Bean
    public Logger.Level level(){
        return Logger.Level.FULL;
    }
}
```

yml文件指定监控的接口

```yaml
logging:
  level:
    com.feng.springcloud.service.PaymentService: debug
```

调用接口后打印如下信息

```verilog
2021-08-17 17:18:51.223 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] <--- HTTP/1.1 200 (342ms)
2021-08-17 17:18:51.224 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] connection: keep-alive
2021-08-17 17:18:51.224 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] content-type: application/json
2021-08-17 17:18:51.224 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] date: Tue, 17 Aug 2021 09:18:51 GMT
2021-08-17 17:18:51.224 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] keep-alive: timeout=60
2021-08-17 17:18:51.225 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] transfer-encoding: chunked
2021-08-17 17:18:51.225 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] 
2021-08-17 17:18:51.226 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] {"code":200,"message":"查询数据成功,port  8001","data":{"id":1,"serial":"serial000001"}}
2021-08-17 17:18:51.227 DEBUG 30172 --- [p-nio-80-exec-2] c.f.springcloud.service.PaymentService   : [PaymentService#queryById] <--- END HTTP (94-byte body)
```

## 服务降级

### Hystrix

`Hystrix`（豪猪）是一个处理分布式系统延时及故障的开源库。分布式系统中不可避免出现服务调用失败、超时、异常等情况，`Hystrix`可以保证在一个依赖服务中断的情况下，不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性。

#### 重要概念

##### 服务降级

服务出现故障不可用时，不让客户端进行等待，而是立刻返回一个友好提示（fallback）。

例如：服务器忙，请稍后再试。

出现情况：

- 程序异常；
- 服务熔断导致服务降级；
- 超时；
- 线程池或信号量满

##### 服务熔断

服务发生故障，进行服务降级，给客户端返回一个友好提示，失败请求达到一定阈值后打开断路器，并会在一定时间后尝试连接。

>  对于熔断机制的实现，Hystrix设计了三种状态：
>
> 1. 熔断关闭状态（Closed）
>    服务没有故障时，熔断器所处的状态，对调用方的调用不做任何限制。
> 2. 熔断开启状态（Open）
>    在固定时间内（Hystrix默认是10秒），接口调用出错比率达到一个阈值（Hystrix默认为50%），会进入熔断开启状态。
>    进入熔断状态后，  后续对该服务接口的调用不再经过网络，直接执行本地的fallback方法。
> 3. 半熔断状态（Half-Open）
>    在进入熔断开启状态一段时间之后（Hystrix默认是5秒），熔断器会进入半熔断状态。
>    所谓半熔断就是尝试恢复服务调用，允许有限的流量调用该服务，并监控调用成功率。
>    如果成功率达到预期，则说明服务已恢复，进入熔断关闭状态；如果成功率仍旧很低，则重新进入熔断开启状态。

##### 服务限流

高并发场景下对请求数量进行控制

#### 工作原理

以下为Hystrix工作原理图：

![](pictures\hystrix-command-flow-chart.png)

对上图进行简单分析：

参考：

[1]: https://github.com/Netflix/Hystrix/wiki/How-it-Works	"官方文档"
[2]: https://www.jianshu.com/p/b9af028efebb

1. 构建一个`HystrixCommand` 或`HystrixObservableCommand`对象，来表示对依赖项所做的请求，前者的命令位于`run()`，而后者的命令逻辑写在`construct()`。此处可以直接使用`@HystrixCommand`注解进行实现；

2. 运行命令

   `execute()`、`queue()`、`observe()`、`toObservable()`这4个方法用来触发执行`run()/construct()`，一个实例只能执行一次这4个方法，`HystrixObservableCommand`没有`execute()`和`queue()`。

   - `execute()`：以同步堵塞方式执行`run()`。调用`execute()`后，hystrix先创建一个新线程运行`run()`，接着调用程序要在`execute()`调用处一直堵塞着，直到`run()`运行完成；
   - `queue()`：以异步非堵塞方式执行`run()`。调用`queue()`直接返回一个Future对象，同时hystrix创建一个新线程运行`run()`，调用程序通过`Future.get()`拿到`run()`的返回结果，而`Future.get()`是堵塞执行的；
   - `observe()`：事件注册前执行`run()/construct()`，第一步是事件注册前，先调用`observe()`自动触发执行`run()/construct()`（如果继承的是`HystrixCommand`，hystrix将创建新线程非堵塞执行`run()`；如果继承的是`HystrixObservableCommand`，将以调用程序线程堵塞执行`construct()`），第二步是从`observe()`返回后调用程序调用`subscribe()`完成事件注册，如果`run()/construct()`执行成功则触发`onNext()`和`onCompleted()`，如果执行异常则触发`onError()`；
   - `toObservable()`：事件注册后执行`run()/construct()`。第一步是事件注册前，一调用`toObservable()`就直接返回一个`Observable<String>`对象，第二步调用`subscribe()`完成事件注册后自动触发执行`run()/construct()`（如果继承的是`HystrixCommand`，hystrix将创建新线程非堵塞执行`run()`，调用程序不必等待`run()`；如果继承的是`HystrixObservableCommand`，将以调用程序线程堵塞执行`construct()`，调用程序等待`construct()`执行完才能继续往下走），如果`run()/construct()`执行成功则触发`onNext()`和`onCompleted()`，如果执行异常则触发`onError()`。

3. 响应以key-value的形式存在缓存中，如果缓存启用且存在缓存中，则该请求可由缓存直接响应；

4. 检查断路器是否断开，如果断开，将不会执行命令，而是转到节点8，没有断开则下一步；

5. 检查信号量或线程池是否已满，这里是`Hystrix`服务隔离的策略，已经满了则转到节点8，没有则下一步；

6. 运行带有`Hystrix`依赖的请求，如果请求发生异常，将会跳转到节点8，如果超时未得到响应也会跳转的节点8，而如果成功响应，则正常返回；

7. Hystrix 将成功、失败、拒绝和超时报告给断路器，断路器维护一组滚动计算统计数据的计数器。

   它使用这些统计信息来确定电路何时应该“跳闸”，此时它会短路任何后续请求，直到恢复期结束，然后在首先检查某些健康检查后再次关闭电路。

8. 每当命令执行失败时，Hystrix 都会尝试恢复到您的回退：当由`construct()`或`run()`(6.)抛出异常时，当由于电路打开而导致命令短路时 (4.)，当命令的线程池和队列或信号量已达到容量 (5.)，或者当命令超过其超时长度时。

   如果 fallback 方法返回一个响应，那么 Hystrix 将把这个响应返回给调用者；如果你还没有为你的 Hystrix 命令实现回退方法，或者回退本身抛出一个异常，Hystrix 仍然返回一个 Observable，但一个不发出任何东西并立即终止的`onError`通知，通过这个`onError`通知，将命令失败的异常被传回给调用者。

9. 成功响应。

   如果 Hystrix 命令成功，它将以`Observable`. 根据您在上述步骤 2 中调用命令的方式，这`Observable`可能会在返回给您之前进行转换：

   ![](pictures\hystrix-return-flow.png)

   - `execute()`—与` .queue()`相同的方式获取 一个`Future`，然后调用`get()`这个`Future`对象以获取由发出的单个值`Observable`
   - `queue()`— 将 转换`Observable`为 一个`BlockingObservable`以便可以将其转换为一个 `Future`，然后返回 this`Future`
   - `observe()`—`Observable`立即订阅并开始执行命令的流程；返回一个`Observable`，当您`subscribe`使用它时，会重播排放和通知
   - `toObservable()`— 返回`Observable`不变的；您必须`subscribe`这样做才能真正开始导致执行命令的流程

#### 服务降级

> 以下测试用例采用OpenFeign进行服务间接口调用，不再赘述此部分代码。

**服务端降级与客户端降级差异点在于主启动类使用注解不同。**

##### 服务端降级

服务端因模块响应超时或代码缺陷导致异常（超时、除数为0、空指针异常等）。客户端访问时进行服务降级，给出友好提示。

**踩的坑**

问题：使用`OpenFeign`进行rpc服务请求超时测试，出现timeout 500异常，并未出现预期友好提示。

原因：OpenFeign默认超时时间为2秒，为得到响应即抛出timeout异常。

解决：消费端配置文件配置OpenFeign超时时间，如下：

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 60000
        readTimeout: 60000
```

**服务端降级实现：**

1. 添加依赖

```xml
<!--hystrix断路器引入-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

2. 主启动类添加断路器使用注解`@EnableCircuitBreaker`

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class PaymentMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain8001.class, args);
    }
}
```

3. 使用方法级注解`@HystrixCommand`标记潜在异常api，指定降级处理方法并设置超时时间降级阈值

```java
@Service
public class PaymentServiceImpl implements IPaymentService {

    @Value("${server.port}")
    int port;

    @Override
    @HystrixCommand(fallbackMethod = "getTimeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")})
    public String getTimeout(int id) {
        int timeout = 5;
        try {
            Thread.sleep(timeout*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "调用getTimeout成功，id=" + id + "port=" + port + "调用时长（秒）=" + timeout;
    }

    /**
     * hystrix服务降级处理器
     * @param id id
     * @return String String
     */
    public String getTimeoutHandler(int id){
        return Thread.currentThread().getName()+ "服务调用超时或系统内部异常，请稍后再试";
    }
}

```

##### 客户端降级

针对客户端防止自身代码异常或更低运行时长要求，可在客户端测进行主动服务降级。

1. 添加依赖

   ```xml
   <!--hystrix断路器引入-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   ```

2. 主启动类添加`@EnableHystrix`注解

   ```java
   @SpringBootApplication
   @EnableFeignClients
   @EnableHystrix
   public class HystrixOrderMain {
       public static void main(String[] args) {
           SpringApplication.run(HystrixOrderMain.class, args);
       }
   }
   ```

3. 使用方法级注解`@HystrixCommand`标记潜在异常api，指定降级处理方法并设置超时时间降级阈值

   ```java
   @RestController
   public class OrderController {
       @Resource
       FeignOrderService service;
   
       /**
        * 测试请求_超时
        *
        * @param id id
        * @return String String
        */
       @GetMapping("/consumer/payment/getTimeout/{id}")
       @HystrixCommand(fallbackMethod = "getTimeoutHandler",
               commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")})
       public String getTimeout(@PathVariable("id") int id) {
           return service.getTimeout(id);
       }
   
       public String getTimeoutHandler(int id){
           return "客户端服务异常，请稍后再试";
       }
   }
   ```

##### 全局服务降级

场景在于由上代码可知，每一个降级方法需要指定一个降级后处理handler，这样会造成代码的膨胀。因此在实际开发中，可配置全局的handler，特殊业务再指定额外的handler。

> 本例以客户端为例，已经引入Hystrix依赖。

过程：

1. 编写全局依赖handler；
2. 使用`@DefaultProperties(defaultFallback = "getGlobalHandler")`注解指定当前Controller默认全局服务降级处理器；
3. 在使用默认降级处理方法前添加`@HystrixCommand`注解；特殊业务指定相应的handler。

详细代码：

```java
package com.feng.springcloud.controller;

import com.feng.springcloud.service.FeignOrderService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试Hystrix客户端服务降级
 * 
 * {@link DefaultProperties}注解用于指定默认全局服务降级handler
 * {@link HystrixCommand}注解不带属性将使用@DefaultProperties指定的handler
 *
 * @author makeDoBetter
 * @version 1.0
 * @date 2021/9/27 15:15
 * @see DefaultProperties
 * @see HystrixCommand
 * @since JDK 1.8
 */
@RestController
@DefaultProperties(defaultFallback = "getGlobalHandler")
public class OrderController {
    @Resource
    FeignOrderService service;

    /**
     * 测试请求_正常
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/consumer/payment/getOk/{id}")
    public String getOk(@PathVariable("id") int id) {
        return service.getOk(id);
    }

    /**
     * 测试除数异常
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/consumer/payment/getNum/{id}")
    @HystrixCommand
    public String getNum(@PathVariable("id") int id) {
        int i = id / 0;
        return "/consumer/payment/getNum/" + id;
    }

    /**
     * 测试除数异常
     *
     * @return String String
     */
    @GetMapping("/consumer/payment/getNullPoint")
    @HystrixCommand
    public String getNullPoint() {
        Long a = null;
        long l = a / 1;
        return "/consumer/payment/getNullPoint";
    }

    /**
     * 测试请求_超时
     *
     * @param id id
     * @return String String
     */
    @GetMapping("/consumer/payment/getTimeout/{id}")
    @HystrixCommand(fallbackMethod = "getTimeoutHandler",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")})
    public String getTimeout(@PathVariable("id") int id) {
        return service.getTimeout(id);
    }

    public String getTimeoutHandler(int id) {
        return "客户端服务异常，请稍后再试";
    }

    public String getGlobalHandler() {
        return "全部处理器GlobalHandler：客户端服务异常，请稍后再试";
    }
}


```

##### 通配服务降级

场景：客户端对一个服务的对外api可能存在多处调用，将服务降级handler与调用方法一一对应会造成代码膨胀，全局服务降级也会使hander与业务代码混乱。

解决：以`OpenFeign`服务调用为例，编写fallBack类实现api接口，每个实现方法为对应的服务降级策略代码。

1. 客户端添加Feign对Hystrix的支持

   ```yaml
   feign:
     hystrix:
       enabled: true
   ```

2. 实现调用api，编写降级逻辑

   ```java
   package com.feng.springcloud.service;
   
   import org.springframework.stereotype.Component;
   
   /**
    * FeignOrderService api中单个方法服务降级实现
    *
    * @author makeDoBetter
    * @version 1.0
    * @date 2021/9/28 15:24
    * @since JDK 1.8
    */
   @Component
   public class FeignHystrixFallBackService implements FeignOrderService{
       @Override
       public String getOk(int id) {
           return "服务端出现故障，请稍后再试";
       }
   
       @Override
       public String getTimeout(int id) {
           return "服务端出现故障，请稍后再试";
       }
   }
   
   ```

3. `@FeignClient`注解指定fallback的组件。`@FeignClient(name = "cloud-provider-hytrix-payment", fallback = FeignHystrixFallBackService.class)`，其中fallback指定实现的服务降级类

   ```java
   package com.feng.springcloud.service;
   
   import org.springframework.cloud.openfeign.FeignClient;
   import org.springframework.stereotype.Component;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PathVariable;
   
   /**
    * 支付模块远程调用
    *
    * @author makeDoBetter
    * @version 1.0
    * @date 2021/9/27 9:51
    * @since JDK 1.8
    */
   @Component
   @FeignClient(name = "cloud-provider-hytrix-payment", fallback = FeignHystrixFallBackService.class)
   public interface FeignOrderService {
   
       /**
        * 测试请求_正常
        *
        * @param id id
        * @return String String
        */
       @GetMapping("/payment/getOk/{id}")
       String getOk(@PathVariable("id") int id);
   
       /**
        * 测试请求_超时
        *
        * @param id id
        * @return String String
        */
       @GetMapping("/payment/getTimeout/{id}")
       String getTimeout(@PathVariable("id") int id);
   }
   
   ```

#### 服务熔断

服务请求失败后，触发服务降级，当在一个时间窗口内，失败服务达到阈值，服务将会断开，此后在一定时间内，该服务无法访问。服务断开后一定时间，断路器将会尝试服务是否恢复，如果服务恢复，断路器将会关闭，服务正常，否则继续断开状态。

> 原理：采用滑动窗口的方式。

1. 引入`Hystrix`依赖

   ```xml
   <!--hystrix断路器引入-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   ```

2. 主启动类添加`@EnableCircuitBreaker`注解

   ```java 
   @SpringBootApplication
   @EnableDiscoveryClient
   @EnableCircuitBreaker
   public class PaymentMain8001 {
       public static void main(String[] args) {
           SpringApplication.run(PaymentMain8001.class, args);
       }
   }
   ```

3. 需要做服务熔断方法添加`@HystrixCommand`注解，并配置熔断属性。

   - **circuitBreaker.enabled**：开启断路器；
   - **circuitBreaker.requestVolumeThreshold**：设置使熔断判断逻辑开始工作的最小请求数，设置10个，只有9个请求，即使都是错误的也不会触发熔断机制，默认为20；
   - **circuitBreaker.sleepWindowInMilliseconds**：窗口期，断路器底层采用滑动窗口的机制；
   - **circuitBreaker.errorThresholdPercentage**：触发断路器的失败请求占的比例，默认50%。

   ```java
   /**
    * PaymentServiceImpl.class
    *
    * @author makeDoBetter
    * @version 1.0
    * @date 2021/8/25 15:38
    * @since JDK 1.8
    */
   @Service
   public class PaymentServiceImpl implements IPaymentService {
   
       @Override
       @HystrixCommand(fallbackMethod = "getCircuitHandler", commandProperties = {
               @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),//开启断路器
               //设置使熔断判断逻辑开始工作的最小请求数，设置10个，只有9个请求，即使都是错误的也不会触发熔断机制
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),//窗口期，断路器底层采用滑动窗口的机制
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),//触发断路器的失败请求占的比例
       })
       public String getCircuit(int id) {
           if (id < 0){
               throw new RuntimeException("id不可为负数" + id);
           }
           return "调用getCircuit成功" + UUID.randomUUID().toString();
       }
   
       public String getCircuitHandler(int id){
           return "服务端异常，请稍后再试";
       }
   }
   
   ```

#### 服务隔离

hystrix提供了两种隔离策略：**线程池隔离**和**信号量隔离**。`hystrix`默认采用线程池隔离。

- **线程池隔离**：不同服务通过使用不同线程池，彼此间将不受影响，达到隔离效果。
- **信号量隔离**：线程隔离会带来线程开销，有些场景（比如无网络请求场景）可能会因为用开销换隔离得不偿失，为此hystrix提供了信号量隔离，当服务的并发数大于信号量阈值时将进入fallback。

#### 服务监控

`Hystrix`提供图形监控工具对指定的服务请求进行监控并在界面上进行展示。

![](pictures\hystrix_dashboard.png)

如上图，监控界面展示成功、失败、请求异常、失败率、断路器开合状态、请求次数曲线等。

上图7个数字指标颜色与下图类型一一对应。

![](pictures\hystrix_dashboard_1.png)

##### 监控服务搭建

1. 引入hystrix_dashboard依赖；

   ```xml
   <!--hystrix图形化界面工具引入-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
   </dependency>
   ```

2. 配置服务端口号

   ```yaml
   server:
     port: 9001
   ```

3. 主启动类添加`@EnableHystrixDashboard`注解

   ```java
   /**
    * Hystrix dashboard 图形化监控服务主启动类
    *
    * @author makeDoBetter
    * @version 1.0
    * @date 2021/9/29 10:46
    * @since JDK 1.8
    */
   @SpringBootApplication
   @EnableHystrixDashboard
   public class HystrixDashboardMain9001 {
       public static void main(String[] args) {
           SpringApplication.run(HystrixDashboardMain9001.class, args);
       }
   }
   ```

服务启动后，浏览器输入 http://localhost:9001/hystrix，进入到图形化监控主页。

##### 监控对象服务配置

监控对象服务需要进行一定配置可实现监控。

1. pom文件引入监控及hystrix依赖

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   <!--hystrix断路器引入-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
   </dependency>
   ```

2. 主启动类添加`@EnableCircuitBreaker`注解

   ```java
   /**
    * 模块主启动类
    *
    * @author makeDoBetter
    * @version 1.0
    * @date 2021/8/25 15:35
    * @since JDK 1.8
    */
   @SpringBootApplication
   @EnableDiscoveryClient
   @EnableCircuitBreaker
   public class PaymentMain8001 {
       public static void main(String[] args) {
           SpringApplication.run(PaymentMain8001.class, args);
       }
   }
   
   ```

3. 添加自动配置类，手动注册bean，避免出现SpringBoot不兼容异常

   ```java
   /**
    * ServletRegistrationBean自动配置类
    *
    * @author makeDoBetter
    * @version 1.0
    * @date 2021/9/29 11:29
    * @since JDK 1.8
    */
   @Configuration
   public class HystrixDashboardConfig {
   
       /**
        * 手动添加 ServletRegistrationBean，避免出现springBoot对hystrix dashboard的不兼容
        * 监控页面出现 Unable to connect to Command Metric Stream. 异常
        * @return ServletRegistrationBean bean
        */
       @Bean
       public ServletRegistrationBean getServlet(){
           HystrixMetricsStreamServlet servlet = new HystrixMetricsStreamServlet();
           ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet);
           registrationBean.setLoadOnStartup(1);
           registrationBean.addUrlMappings("/hystrix.stream");
           registrationBean.setName("HystrixMetricsStreamServlet");
           return registrationBean;
       }
   }
   
   ```

监控对象启动后，进入dashboard主页http://localhost:9001/hystrix，配置监控对象，指定监控对象路径、名称等，点击监控后，将会跳转至监控界面，对象服务的请求都可以在该页面展示出来。

![](pictures\hystrix_dashboard_2.png)

#### commandProperties

`commandProperties`为`@HystrixCommand`注解中的一个属性，用于指定断路器各种参数。

Command属性主要用来控制HystrixCommand命令的行为，它主要分下面的类别：

来源：https://blog.csdn.net/acmman/article/details/100595666

1. **Execution**：用来控制HystrixCommand.run()的执行

   - execution.isolation.strategy：该属性用来设置HystrixCommand.run()执行的隔离策略。默认为THREAD。
   - execution.isolation.thread.timeoutInMilliseconds：该属性用来配置HystrixCommand执行的超时时间，单位为毫秒。
   - execution.timeout.enabled：该属性用来配置HystrixCommand.run()的执行是否启用超时时间。默认为true。
   - execution.isolation.thread.interruptOnTimeout：该属性用来配置当HystrixCommand.run()执行超时的时候是否要它中断。
   - execution.isolation.thread.interruptOnCancel：该属性用来配置当HystrixCommand.run()执行取消时是否要它中断。
   - execution.isolation.semaphore.maxConcurrentRequests：当HystrixCommand命令的隔离策略使用信号量时，该属性用来配置信号量的大小。当最大并发请求达到该设置值时，后续的请求将被拒绝。

2. **Fallback**：用来控制HystrixCommand.getFallback()的执行。

   - fallback.isolation.semaphore.maxConcurrentRequests：该属性用来设置从调用线程中允许

   - HystrixCommand.getFallback()方法执行的最大并发请求数。当达到最大并发请求时，后续的请求将会被拒绝并抛出异常。

   - fallback.enabled：该属性用来设置服务降级策略是否启用，默认是true。如果设置为false，当请求失败或者拒绝发生时，将不会调用HystrixCommand.getFallback()来执行服务降级逻辑。

3. **Circuit Breaker**：用来控制HystrixCircuitBreaker的行为。

   - circuitBreaker.enabled：确定当服务请求命令失败时，是否使用断路器来跟踪其健康指标和熔断请求。默认为true。
   - circuitBreaker.requestVolumeThreshold：用来设置在滚动时间窗中，断路器熔断的最小请求数。例如，默认该值为20的时候，如果滚动时间窗（默认10秒）内仅收到19个请求，即使这19个请求都失败了，断路器也不会打开。
   - circuitBreaker.sleepWindowInMilliseconds：用来设置当断路器打开之后的休眠时间窗。休眠时间窗结束之后，会将断路器设置为“半开”状态，尝试熔断的请求命令，如果依然时候就将断路器继续设置为“打开”状态，如果成功，就设置为“关闭”状态。
   - circuitBreaker.errorThresholdPercentage：该属性用来设置断路器打开的错误百分比条件。默认值为50，表示在滚动时间窗中，在请求值超过requestVolumeThreshold阈值的前提下，如果错误请求数百分比超过50，就把断路器设置为“打开”状态，否则就设置为“关闭”状态。
   - circuitBreaker.forceOpen：该属性默认为false。如果该属性设置为true，断路器将强制进入“打开”状态，它会拒绝所有请求。该属性优于forceClosed属性。
   - circuitBreaker.forceClosed：该属性默认为false。如果该属性设置为true，断路器强制进入“关闭”状态，它会接收所有请求。如果forceOpen属性为true，该属性不生效。

4. **Metrics**：该属性与HystrixCommand和HystrixObservableCommand执行中捕获的指标相关。

   - metrics.rollingStats.timeInMilliseconds：该属性用来设置滚动时间窗的长度，单位为毫秒。该时间用于断路器判断健康度时需要收集信息的持续时间。断路器在收集指标信息时会根据设置的时间窗长度拆分成多个桶来累计各度量值，每个桶记录了一段时间的采集指标。例如，当为默认值10000毫秒时，断路器默认将其分成10个桶，每个桶记录1000毫秒内的指标信息。
   - metrics.rollingStats.numBuckets：用来设置滚动时间窗统计指标信息时划分“桶”的数量。默认值为10
   - metrics.rollingPercentile.enabled：用来设置对命令执行延迟是否使用百分位数来跟踪和计算。默认为true，如果设置为false，那么所有的概要统计都将返回-1
   - metrics.rollingPercentile.timeInMilliseconds：用来设置百分位统计的滚动窗口的持续时间，单位为毫秒。
   - metrics.rollingPercentile.numBuckets：用来设置百分位统计滚动窗口中使用桶的数量。
   - metrics.rollingPercentile.bucketSize：用来设置每个“桶”中保留的最大执行数。
   - metrics.healthSnapshot.intervalInMilliseconds：用来设置采集影响断路器状态的健康快照的间隔等待时间。

5. **Request Context**：涉及HystrixCommand使用HystrixRequestContext的设置。

   - requestCache.enabled：用来配置是否开启请求缓存。
   - requestLog.enabled：用来设置HystrixCommand的执行和事件是否打印到日志的HystrixRequestLog中。

