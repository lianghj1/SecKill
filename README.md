
## 1.系统介绍
- 本系统是使用SpringBoot开发的高并发限时抢购秒杀系统，除了实现基本的登录、查看商品列表、秒杀、下单等功能，项目中还针对高并发情况实现了系统缓存、降级和限流。
## 2.开发环境和工具
- Aliyun CentOS7.3 
-  IntelliJ IDEA + Navicat + Git + Chrome
- 压测工具Apache Jmeter
## 3.开发技术
| 前端技术 | BootStrap  | JQuery | Thymeleaf
|--|--|--|--|
| 后端技术 | SpringBoot | MyBatis | MySQL
| 中间件技术 |  Druid  | Redis  |  RabbitMQ
## 4.秒杀优化方向
- 将请求尽量拦截在系统上游：传统秒杀系统之所以挂，请求都压倒了后端数据层，数据读写锁冲突严重，几乎所有请求都超时，流量虽大，下单成功的有效流量甚小，我们可以通过限流、降级等措施来最大化减少对数据库的访问，从而保护系统。
- 充分利用缓存：秒杀商品是一个典型的读多写少的应用场景，充分利用缓存将大大提高并发量
## 5.项目亮点

### 5.1 使用分布式Seesion，可以实现让多台服务器同时可以响应。
### 5.2 使用redis做缓存提高访问速度和并发量，减少数据库压力，利用内存标记减少redis的访问
- 在秒杀活动中，当队列写入消息达到某一数值时，不再写入消息队列，而直接跳转到活动结束的页面
### 5.3 使用页面静态化，加快用户访问速度，提高QPS，缓存页面至浏览器，前后端分离降低服务器压力
### 5.4 使用消息队列完成异步下单，提升用户体验，削峰和降流
- **冗余（存储）**：在某些情况下处理数据的过程中会失败，消息中间件允许把数据持久化知道他们完全被处理
- **削峰**：在访问量剧增的情况下，但是应用仍然需要发挥作用，但是这样的突发流量并不常见。而使用消息中间件采用队列的形式可以减少突发访问压力，不会因为突发的超时负荷要求而崩溃；消息队列是基于队列的，在秒杀活动中，当队列写入消息达到某一数值时，不再写入消息队列，而直接跳转到活动结束的页面
- **顺序保证**：在大多数场景下，处理数据的顺序也很重要，大部分消息中间件支持一定的顺序性
- **缓冲**：消息中间件通过一个缓冲层来帮助任务最高效率的执行
- **异步通信**：通过把把消息发送给消息中间件，消息中间件并不立即处理它，后续在慢慢处理
### 5.5 安全性优化：双重md5密码校验
### 5.6 秒杀接口地址的隐藏
### 5.7 数学公式验证码
### 5.8 接口限流防刷
### 5.9 优雅的代码编写
- 接口的输出结果做了一个Result封装
- 对错误的代码做了一个CodeMsg的封装
- 访问缓存做了一个key的封装

## 6 实现技术点
### 6.1 两次md5密码校验
- 用户端：inputPassToFormPass = MD5（明文+固定salt）
- 服务端：formPassToDBPass = MD5(inputPassToFormPass+ 随机salt）
- 好处：
	* 第一次作用：防止用户明文密码在网络进行传输
	* 第二次作用：防止数据库被盗，避免通过MD5反推出密码，双重保险
### 6.2 分布式session
- 验证用户账号密码都正确情况下，通过UUID生成唯一id作为token，再将token作为key、用户信息作为value模拟session存储到redis，同时将token存储到cookie，保存登录状态，每次需要session，从缓存中取即可
- 好处： 在分布式集群情况下，服务器间需要同步，定时同步各个服务器的session信息，会因为延迟到导致session不一致，使用redis把session数据集中存储起来，解决session不一致问题
### 6.3 JSR303参数检验
- 使用JSR303自定义校验器，实现对用户账号、密码的验证，使得验证逻辑从业务代码中脱离出来
### 6.4 全局异常处理
-  **优点1：** 可以实现对项目中所有产生的异常进行拦截，在同一个类中实现统一处理。避免异常漏处理的情况。
- **优点2：** 当Service 出现业务逻辑错误的时候，这个时候我们可以直接抛出异常，让拦截器来捕捉，捕捉之后，就不需要冗余的代码来return 一个不符合业务逻辑的返回值来作为输出。
-  **优点3：** 当参数校验不通过的时候，输出也是Result（CodeMsg）,传给前端用于前端显示获取处理
### 6.5 页面缓存 + 对象缓存，Redis缓解数据库压力
- 本项目大量的利用了缓存技术，包括用户信息缓存（分布式session），商品信息的缓存，商品库存缓存，订单的缓存，页面缓存，对象缓存减少了对数据库服务器的访问
- 页面缓存：通过在手动渲染得到的html页面缓存到redis
- 对象缓存：包括对用户信息、商品信息、订单信息和token等数据进行缓存，利用缓存来减少对数据库的访问，大大加快查询速度。
### 6.6 页面静态化，前后端分离
 - 页面静态化的主要目的是为了加快页面的加载速度，将商品详情和订单详情页面做成静态HTML（纯的HTML），数据的加载只需要通过ajax来请求服务器，实现前后端分离，静态页面无需连接数据库打开速度较动态页面会有明显提高，并且做了静态化HTML页面可以缓存在客户端的浏览器。

### 6.7 通用缓存Key封装
 - 大量的缓存引用也出现了一个问题，如何识别不同模块中的缓存（key值重复，如何辨别是不同模块的key）
- 解决：利用一个抽象类，定义BaseKey（前缀），在里面定义缓存key的前缀以及缓存的过期时间从而实现将缓存的key进行封装。让不同模块继承它，这样每次存入一个模块的缓存的时候，加上这个缓存特定的前缀，以及可以统一制定不同的过期时间
### 6.8 本地内存标记 + redis预处理 + RabbitMQ异步下单 + 客户端轮询
- **描述：通过三级缓冲保护，1、本地标记 2、redis预处理 3、RabbitMQ异步下单，最后才会访问数据库，这样做是为了最大力度减少对数据库的访问**
1. 系统初始化，把商品库存数量stock加载到Redis
2.  服务器接收秒杀请求，在秒杀阶段使用本地标记localOverMap（goodsId，boolean）对秒杀商品做标记，若被标记为true，表明商品秒杀完毕，直接返回秒杀结束，未被标记为true才查询redis，通过本地标记来减少对redis的访问
3. Redis预减库存，如果库存已经到达临界值的时候，直接返回失败，即后面的大量请求无需给系统带来压力，通过Redis预减少库存减少数据库访问
4. 通过redis缓存判断这个秒杀订单形成没有，避免同一用户重复秒杀。如果是重复秒杀，则需要对Redis的预减库存进行回增，并重重置本地标记localOverMap为false。
5. 为了保护系统不受高流量的冲击而导致系统崩溃的问题，使用RabbitMQ用异步队列处理下单，实际做了一层缓冲保护，做了一个窗口模型，窗口模型会实时的刷新用户秒杀的状态。
6. 后端RabbitMQ监听秒杀MIAOSHA_QUEUE的这名字的通道，如果有消息过来，获取到传入的信息，执行真正的秒杀之前，要判断数据库的库存，判断是否重复秒杀，然后执行秒杀事务（减库存，下订单，写入秒杀订单），秒杀订单还需要写到Redis中，方便判断是否重复秒杀。
7. 客户端根据商品id用js轮询接口，用来获取处理状态

### 6.9 秒杀接口地址的隐藏
- 每次点击秒杀按钮，才会生成秒杀地址，秒杀地址不是写死的，是从服务端获取，动态拼接而成的地址

### 6.10 数学公式验证码
- 点击秒杀前，先让用户输入数学公式验证码，验证正确才能获取秒杀地址进行秒杀
- 优点：
	- **防止恶意的机器人和爬虫**,刷票软件恶意频繁点击按钮来刷请求秒杀地址接口的操作
	- **分散用户的请求：** 高并发下场景，在刚刚开始秒杀的那一瞬间，迎来的并发量是最大的，减少同一时间点的并发量，将并发量分流也是一种减少数据库以及系统压力的措施（使得1s中来10万次请求过渡为10s中来10万次请求）
### 6.11 接口限流防刷
- **限制同一用户一定时间内（如1 min）只能访问固定次数，可以使用拦截器减少对业务的侵入，在服务端对系统做一层保护**
## 7.具体技术实现细节
1. [明文密码两次MD5处理](https://blog.csdn.net/qq_40776491/article/details/98312614)
2. [Redis通用缓存Key封装](https://blog.csdn.net/qq_40776491/article/details/98339039)
3. [JSR303 参数校验 + 全局异常处理](https://blog.csdn.net/qq_40776491/article/details/98390551)
4. [自定义参数解析器](https://blog.csdn.net/qq_40776491/article/details/98446983)
5. [页面优化技术（页面缓存 + 对象缓存）](https://blog.csdn.net/qq_40776491/article/details/98469684)
6. [页面优化技术（页面静态化 ，前后端分离）](https://blog.csdn.net/qq_40776491/article/details/98470586)
7. [使用RabbitMQ实现高并发接口优化](https://blog.csdn.net/qq_40776491/article/details/98473554)
8. [秒杀地址隐藏](https://blog.csdn.net/qq_40776491/article/details/98476347)
9. [数学公式验证码](https://blog.csdn.net/qq_40776491/article/details/98481193)
10. [接口限流防刷](https://blog.csdn.net/qq_40776491/article/details/98506663) 

## 8.优化前后压测对比
### 8.1 商品列表页 页面缓存优化前后压测对比
- 页面缓存优化前 1000 * 10 QPS：222
![页面缓存优化前](https://img-blog.csdnimg.cn/2019080717013142.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwNzc2NDkx,size_16,color_FFFFFF,t_70)
- 页面缓存优化后 1000 * 10 QPS：353
![页面缓存优化后](https://img-blog.csdnimg.cn/20190807170001398.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwNzc2NDkx,size_16,color_FFFFFF,t_70)
### 8.2 秒杀接口优化前后压测对比
- 优化前 1000 * 10 QPS：231
![优化前](https://img-blog.csdnimg.cn/20190807170419697.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwNzc2NDkx,size_16,color_FFFFFF,t_70)
- 优化后 1000 * 10 QPS：1034
![优化后](https://img-blog.csdnimg.cn/20190807170511328.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwNzc2NDkx,size_16,color_FFFFFF,t_70)
## 9. [更多技术细节，请查看个人博客](https://blog.csdn.net/qq_40776491)
