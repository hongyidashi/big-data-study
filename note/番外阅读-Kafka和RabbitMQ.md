# 番外阅读-Kafka和RabbitMQ
1. [异步消息模式](#异步消息模式)
2. [RabbitMQ](#RabbitMQ)
3. [Kafka](#Kafka)
4. [RabbitMQ和Kafka的的区别](#RabbitMQ和Kafka的的区别)
   - [消息顺序](#消息顺序)
   - [消息路由](#消息路由)
   - [消息时序](#消息时序)
   - [消息留存（retention）](#消息留存（retention）)
   - [容错处理](#容错处理)
   - [伸缩](#伸缩)
   - [消费者复杂度](#消费者复杂度)
5. [如何选择](#如何选择)



## <span id="异步消息模式">异步消息模式</span>
异步消息可以作为解耦消息的生产和处理的一种解决方案。提到消息系统，我们通常会想到两种主要的消息模式——消息队列和发布/订阅模式。

**消息队列**  
多个生产者可以向同一个消息队列发送消息；但是，一个消息在被一个消息者处理的时候，这个消息在队列上会被锁住或者被移除并且其他消费者无法处理该消息。
也就是说**一个具体的消息只能由一个消费者消费**。

![消息队列](images/Kafka和RabbitMQ/消息队列.jpg)

如果消费者处理一个消息失败了，消息系统一般会把这个消息放回队列，这样其他消费者可以继续处理。消息队列除了提供解耦功能之外，
它还能够对生产者和消费者进行独立的伸缩（scale），以及提供对错误处理的容错能力。

**发布/订阅**  
发布/订阅（pub/sub）模式中，单个消息可以被**多个订阅者并发的获取和处理**。

![发布/订阅](images/Kafka和RabbitMQ/发布:订阅.jpg)

一般来说，订阅有两种类型：  
1. 临时（ephemeral）订阅，这种订阅只有在消费者启动并且运行的时候才存在。一旦消费者退出，相应的订阅以及尚未处理的消息就会丢失；
2. 持久（durable）订阅，这种订阅会一直存在，除非主动去删除。消费者退出后，消息系统会继续维护该订阅，并且后续消息可以被继续处理。

## <span id="RabbitMQ">RabbitMQ</span>
RabbitMQ 作为消息中间件的一种实现，常常被当作一种**服务总线**来使用。RabbitMQ 原生就支持上面提到的两种消息模式。
>服务总线：简单来说就是服务间数据通信的一种通道。（应该）

**队列**  
RabbitMQ 支持典型的开箱即用的消息队列。开发者可以定义一个命名队列，然后发布者可以向这个命名队列中发送消息。
最后消费者可以通过这个命名队列获取待处理的消息。

**消息交换器(Exchange)**  
RabbitMQ 使用**消息交换器**（交换机 Exchange）来实现发布/订阅模式。发布者可以把消息发布到消息交换器上而不用知道这些消息都有哪些订阅者。

每一个订阅了交换器的消费者都会创建一个队列；然后消息交换器会把生产的消息放入队列以供消费者消费。
消息交换器也可以基于各种路由规则为一些订阅者过滤消息。

![RabbitMQ消息交换器](images/Kafka和RabbitMQ/RabbitMQ消息交换器.jpg)

RabbitMQ 支持临时和持久两种订阅类型，消费者可以调用 RabbitMQ 的 API 来选择想要的订阅类型。

根据 RabbitMQ 的架构设计，也可以创建一种混合方法——多个订阅者形成一个组，然后在组内以竞争关系作为消费者去处理某个具体队列上的消息，
这种由订阅者构成的组我们称为消费者组。按照这种方式，我们实现了发布/订阅模式，同时也能够很好的伸缩（scale-up）订阅者去处理收到的消息。

![发布/订阅与队列的联合使用](images/Kafka和RabbitMQ/发布:订阅与队列的联合使用.jpg)

## <span id="Kafka">Kafka</span>
Kafka 不是消息中间件的一种实现，相反，它只是一种分布式流式系统。不同于基于队列和交换器的 RabbitMQ，
Kafka 的存储层是使用分区事务日志来实现的。

**主题(topic)**  
Kafka 没有实现队列这种东西，相应的，Kafka 按照类别存储记录集，并且把这种类别称为主题(topic)。

Kafka 为每个主题维护一个消息分区日志。每个分区都是由有序的不可变的记录序列组成，并且消息都是连续的被追加在尾部。当消息到达时，
Kafka 就会把他们追加到分区尾部。默认情况下，Kafka 使用轮询分区器（partitioner）把消息一致的分配到多个分区上。

Kafka 可以改变创建消息逻辑流的行为。  
例如，在一个多租户的应用中，我们可以根据每个消息中的租户 ID 创建消息流。确保来自相同逻辑流上的消息映射到相同分区上，
这就保证了消息能够按照顺序提供给消费者。

消费者通过维护分区的偏移（或者说索引）来顺序的读出消息，然后消费消息。

单个消费者可以消费多个不同的主题，并且消费者的数量可以伸缩到可获取的最大分区数量。所以在创建主题的时候，要考虑在创建的主题上预期的消息吞吐量。

消费同一个主题的多个消费者构成的组称为消费者组。通过 Kafka 提供的 API 可以处理同一消费者组中多个消费者之间的分区平衡以及消费者当前分区偏移的存储。

**Kafka实现的消息模式**  
Kafka 的实现很好地契合发布/订阅模式。

生产者可以向一个具体的主题发送消息，然后多个消费者组可以消费相同的消息；每一个消费者组都可以独立的伸缩去处理相应的负载。
由于消费者维护自己的分区偏移，所以他们可以选择持久订阅或者临时订阅，
持久订阅在重启之后不会丢失偏移而临时订阅在重启之后会丢失偏移并且每次重启之后都会从分区中最新的记录开始读取。

Kafka 是按照**预先配置好的时间**保留分区中的消息，而不是根据消费者是否消费了这些消息；这种保留机制可以让消费者自由的重读之前的消息。
另外，开发者也可以利用 Kafka 的存储层来实现诸如事件溯源和日志审计功能。

## <span id="RabbitMQ和Kafka的的区别">RabbitMQ和Kafka的的区别</span>

### <span id="消息顺序">消息顺序</span>
**RabbitMQ**  
对于发送到队列或者交换器上的消息，RabbitMQ 在单个消费者读取一个队列的情况下保证顺序，多个消费者从同一个队列中读取消息，
那么消息的处理顺序就没法保证了。  
由于消费者读取消息之后可能会把消息放回（或者重传）到队列中（例如，处理失败的情况），这样就会导致消息的顺序无法保证。  
一旦一个消息被重新放回队列，另一个消费者可以继续处理它，即使这个消费者已经处理到了放回消息之后的消息。因此，消费者组处理消息是无序的。

**Kafka**  
Kafka 在消息处理方面提供了可靠的顺序保证。Kafka 能够保证发送到相同主题分区的所有消息都能够按照顺序处理。

### <span id="消息路由">消息路由</span>
**RabbitMQ**  
RabbitMQ 可以基于定义的订阅者路由规则路由消息给一个消息交换器上的订阅者。  
一个主题交换器可以通过一个叫做 routing_key 的特定头来路由消息，或者通过一个头部（headers）交换器可以基于任意的消息头来路由消息。
这两种交换器都能够有效地让消费者设置他们感兴趣的消息类型。

在消息路由和过滤方面，RabbitMQ提供了很好的支持。

>头交换机（headers exchange）：使用多个消息属性来代替路由键建立路由规则。通过判断消息头的值能否与指定的绑定相匹配来确立路由规则。
>头交换机可以视为直连交换机的另一种表现形式。头交换机能够像直连交换机一样工作，不同之处在于头交换机的路由规则是建立在头属性值之上，
>而不是路由键；路由键必须是一个字符串，而头属性值则没有这个约束，它们甚至可以是整数或者哈希值（字典）等。  
>注：headers 类型的交换器性能会很差，且不实用，不推荐使用。

**Kafka**  
Kafka 在处理消息之前是不允许消费者过滤一个主题中的消息。一个订阅的消费者在没有异常情况下会接受一个分区中的所有消息。  
Kafka 可以自定义一些拦截器对消息进行过滤。

### <span id="消息时序">消息时序</span>
**RabbitMQ**  
在测定发送到一个队列的消息时间方面，RabbitMQ 提供了多种能力：
1. **消息存活时间（TTL）**  
发送到RabbitMQ的每条消息都可以关联一个TTL属性，系统可以根据设置的 TTL 来限制消息的有效期。如果消费者在预期时间内没有处理该消息，
那么这条消息会自动的从队列上被移除（并且会被移到死信交换器上，同时在这之后的消息都会这样处理）；
>死信交换机  
>死信交换机(DLXs)就是普通的交换机，可以是任何一种类型。  
>队列中的消息可能会变成死信消息(dead-lettered)，进而当以下几个事件任意一个发生时，消息将会被重新发送到一个交换机：
>- 消息被拒绝（basic.reject或basic.nack）并且requeue=false
>- 消息TTL过期
>- 队列达到最大长度（队列满了，无法再添加数据到mq中）

2. **延迟/预定的消息**  
RabbitMQ 可以通过插件的方式来支持延迟或者预定的消息。当这个插件在消息交换器上启用的时候，生产者可以发送消息到RabbitMQ上，
然后这个生产者可以延迟 RabbitMQ 路由这个消息到消费者队列的时间。

**Kafka**  
Kafka 没有提供这些功能。它在消息到达的时候就把它们写入分区中，这样消费者就可以立即获取到消息去处理。

### <span id="消息留存（retention）">消息留存（retention）</span>
**RabbitMQ**  
当消费者成功消费消息之后，RabbitMQ 就会把对应的消息从存储中删除。这种行为没法修改，它几乎是所有消息代理设计的必备部分。

**Kafka**  
Kafka 会给每个主题配置超时时间，只要没有达到超时时间的消息都会保留下来。在消息留存方面，Kafka仅仅把它当做消息日志来看待，
并不关心消费者的消费状态。 
 
消费者可以不限次数的消费每条消息，并且他们可以操作分区偏移来“及时”往返的处理这些消息。Kafka 会周期的检查分区中消息的留存时间，
一旦消息超过设定保留的时长，就会被删除。

Kafka 的性能不依赖于存储大小。所以，理论上，它存储消息几乎不会影响性能（只要节点有足够多的空间保存这些分区）。

### <span id="容错处理">容错处理</span>
消息处理存在两种可能的故障：  
1. 瞬时故障——故障产生是由于临时问题导致，比如网络连接，CPU负载，或者服务崩溃。可以通过一遍又一遍的尝试来减轻这种故障；
2. 持久故障——故障产生是由于永久的问题导致的，并且这种问题不能通过额外的重试来解决。比如常见的原因有bug或者无效的消息格式。

**RabbitMQ**  
RabbitMQ 提供了诸如交付重试和死信交换器（DLX）来处理消息处理故障。  
DLX 的主要思路是根据合适的配置信息自动地把路由失败的消息发送到 DLX，并且在交换器上根据规则来进一步的处理，比如异常重试，
重试计数以及发送到“人为干预”的队列。

**Kafka**  
Kafka 没有提供开箱即用的机制。在 Kafka 中，需要开发者在应用层提供和实现消息重试机制。  
另外，当一个消费者正在同步地处理一个特定的消息时，那么同在这个分区上的其他消息是没法被处理的。

### <span id="伸缩">伸缩</span>
Kafka 通常被认为比 RabbitMQ 有更优越的性能。

从 Kafka 使用分区的架构上看，它在横向扩展上会优于 RabbitMQ，当然 RabbitMQ 在纵向扩展上会有更多的优势。

### <span id="消费者复杂度">消费者复杂度</span>
**RabbitMQ**  
RabbitMQ 使用的是智能代理和傻瓜式消费者模式。

消费者注册到消费者队列，然后 RabbitMQ 把传进来的消息推送给消费者。  
RabbitMQ 管理消息的分发以及队列上消息的移除（也可能转移到DLX），消费者不需要考虑这块。  
根据 RabbitMQ 结构的设计，当负载增加的时候，一个队列上的消费者组可以有效的从仅仅一个消费者扩展到多个消费者，
并且不需要对系统做任何的改变。

**Kafka**  
Kafka 使用的是傻瓜式代理和智能消费者模式。  

消费者组中的消费者需要协调他们之间的主题分区租约（以便一个具体的分区只由消费者组中一个消费者监听）。
消费者也需要去管理和存储他们分区偏移索引，Kafka SDK 已经为封装了，所以不需要自己管理。

## <span id="如何选择">如何选择</span>
优先选择 RabbitMQ 的条件：
- 高级灵活的路由规则；
- 消息时序控制（控制消息过期或者消息延迟）；
- 高级的容错处理能力，在消费者更有可能处理消息不成功的情景中（瞬时或者持久）；
- 更简单的消费者实现。

优先选择 Kafka 的条件：
- 严格的消息顺序；
- 延长消息留存时间，包括过去消息重放的可能；
- 传统解决方案无法满足的高伸缩能力。
