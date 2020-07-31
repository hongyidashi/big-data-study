# Kafka
1. [Kafka概述](#Kafka概述)
2. [Kafka基础架构](#Kafka基础架构)
3. [工作流程及文件存储机制概述](#工作流程及文件存储机制概述)
4. [工作流程分析](#工作流程分析)
    + [发送数据](#发送数据)
    + [分区策略](#分区策略)
    + [数据可靠性保证](#数据可靠性保证)
    + [保存数据](#保存数据)

## <span id="Kafka概述">Kafka概述</span>
Kafka 是一个分布式的基于发布/订阅模式的消息队列(Message Queue)，主要应用于大数据实时处理领域。

**消息队列的两种模式**  
1. 点对点模式(一对一，消费者主动拉取数据，消息收到后消息清除)  
消息生产者生产消息发送到 Queue 中，然后消息消费者从 Queue 中取出并且消费消息。  
消息被消费以后，Queue 中不再有存储，所以消息消费者不可能消费到已经被消费的消息。 Queue 支持存在多个消费者，但是对一个消息而言，只会有一个消费者可以消费。

2. 发布/订阅模式(一对多，消费者消费数据之后不会清除消息)  
消息生产者(发布)将消息发布到 topic 中，同时有多个消息消费者(订阅)消费该消息。和点对点方式不同，发布到 topic 的消息会被所有订阅者消费。


## <span id="Kafka基础架构">Kafka基础架构</span>
Kafka基础架构：  
![Kafka基础架构](http://cdn.17coding.info/WeChat%20Screenshot_20190325215237.png)

**Messages And Batches**  
Kafka 的基本数据单元被称为 message(消息)，为减少网络开销，提高效率，多个消息会被放入同一批次 (Batch) 中后再写入。

1. Producer：消息生产者，就是向 kafka broker 发消息的客户端；
2. Consumer：消息消费者，向 kafka broker 取消息的客户端；
3. Consumer Group (CG)：消费者组，由多个 consumer 组成。消费者组内每个消费者负责消费**不同分区**的数据，
**一个分区只能由一个组内消费者消费**；消费者组之间互不影响。所有的消费者都属于某个消费者组，即消费者组是**逻辑上的一个订阅者**；
4. Broker：一台 kafka 服务器就是一个 broker（Broker是kafka实例）。一个集群由多个 broker 组成；一个 broker 可以容纳多个 topic；
5. Topic：可以理解为一个队列，生产者和消费者面向的都是一个 topic； 
6. Partition：为了实现扩展性，一个非常大的 topic 可以分布到多个 broker(即服务器)上，一个 topic 可以分为多个 partition，
每个 partition 是一个有序的队列；
>一个分区就是一个提交日志 (commit log)。消息以追加的方式写入分区，然后以先入先出的顺序读取；  
>由于一个 Topic 包含多个分区，因此无法在整个 Topic 范围内保证消息的顺序性，但可以保证消息在单个分区内的顺序性。
>同一个 topic 在不同的分区的数据是不重复的，partition 的表现形式就是一个一个的文件夹！

>**Offset**  
>Partition 中的每条消息都会被分配一个有序的 id（Offset）；用于保证每个 Partition中的顺序。

7. Replica：副本，为保证集群中的某个节点发生故障时，该节点上的 partition 数据不丢失，且 kafka 仍然能够继续工作，
kafka 提供了副本机制，一个 topic 的每个分区都有若干个副本，一个 leader 和若干个 follower；
>follower 和 leader 绝对是在不同的机器，同一机器对同一个分区也只可能存放一个副本（包括自己）。
8. leader：每个分区多个副本的“主”，生产者发送数据的对象，以及消费者消费数据的对象都是 leader。
9. follower：每个分区多个副本中的“从”，实时从 leader 中同步数据，保持和 leader 数据的同步。leader 发生故障时，某个 follower 会成为新的 leader；
10. Zookeeper：kafka集群依赖zookeeper来保存集群的的元信息，来保证系统的可用性。

## <span id="工作流程及文件存储机制概述">工作流程及文件存储机制概述</span>

![工作流程图1](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1596079588949&di=cb3f76980e99de29a54d7474700f7a9b&imgtype=0&src=http%3A%2F%2Fimg.8ym8.com%2Fwp-content%2Fuploads%2F2019%2F02%2F1503038490224_3768_1503038490469.png)

Kafka 中消息是以 topic 进行分类的，生产者生产消息，消费者消费消息，都是面向 topic 的，其本质就是一个目录，
而 topic 是由一些Partition Logs(分区日志)组成。  

topic 是逻辑上的概念，而 partition 是物理上的概念，每个 partition 对应于一个 log 文 件，该 log 文件中存储的就是 producer 生产的数据。
Producer 生产的数据会被不断追加到该 log 文件末端，且每条数据都有自己的 offset。消费者组中的每个消费者，都会实时记录自己消费到了哪个 offset，
以便出错恢复时，从上次的位置继续消费。

Kafka文件存储机制：  
![Kafka文件存储机制](https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=312610625,1404522876&fm=15&gp=0.jpg)

由于生产者生产的消息会不断追加到 log 文件末尾，为防止 log 文件过大导致数据定位 效率低下，Kafka 采取了分片和索引机制，
将每个 partition 分为多个 segment。每个 segment 对应两个文件 ——“.index”文件和“.log”文件。这些文件位于一个文件夹下，
该文件夹的命名 规则为：topic 名称+分区序号。
>一个topic是对一组消息的归纳。对每个topic，Kafka 对它的日志进行了分区；  
>例如，first 这个 topic 有三个分区，则其对应的文件夹为 first- 0,first-1,first-2。

“.index”文件存储大量的索引信息，“.log”文件存储大量的数据，索引文件中的元数据指向对应数据文件中 message 的物理偏移地址。


## <span id="工作流程分析">工作流程分析</span>
看完上面的可能还是8够了解 kafka 的工作流程，这里详细再讲一下。   

### <span id="发送数据">发送数据</span>
![发送数据](http://cdn.17coding.info/WeChat%20Screenshot_20190325215252.png)
发送的流程就在图中已经说明了，就不单独在文字列出来了！需要注意的一点是，消息写入leader后，follower 是主动的去 leader 进行同步的！
producer 采用 push 模式将数据发布到 broker，每条消息追加到分区中，顺序写入磁盘，所以保证同一分区内的数据是有序的！写入示意图如下：
![数据写入topic示意图](http://cdn.17coding.info/WeChat%20Screenshot_20190325215312.png)

### <span id="分区策略">分区策略</span>
上面提到数据会写入到不同的分区，那么问题来了，kafka 为什么要做分区呐？

**分区原因**  
1. 负载均衡，方便在集群中扩展，每个 Partition 可以通过调整以适应它所在的机器，而一个 topic又可以有多个 Partition 组成，
因此整个集群就可以适应任意大小的数据了；
2. 可以提高并发，因为可以以 Partition 为单位读写了；
3. 总结来说就是：方便扩展，提高并发。

好了，问题又来了，如果某个 topic 有多个 partition，producer 又怎么知道该将数据发往哪个 partition 呐？

**分区原则**  
producer 需要将发送的数据封装成一个 ProducerRecord 对象：
1. 指明 partition 的情况下，直接将指明的值直接作为 partition 值；（已指定Partition，则直接使用该Partition）
2. 没有指明 partition 值但有 key 的情况下，将 key 的 hash 值与 topic 的 partition 数进行取余得到 partition 值；（未指定Partition但指定了Key，则通过对Key进行哈希计算得出一个Partition）
3. 既没有 partition 值又没有 key 值的情况下，第一次调用时随机生成一个整数(后面每次调用在这个整数上自增)，
将这个值与 topic 可用的 partition 总数取余得到 partition 值，也就是常说的 round-robin 算法。（Partition和Key都未指定，则轮询选出一个Partition）

### <span id="数据可靠性保证">数据可靠性保证</span>
保证消息不丢失是一个消息队列中间件的基本保证，那 producer 在向 kafka 写入消息的时候，怎么保证消息不丢失呢？—— **ACK应答机制**

为保证 producer 发送的数据，能可靠的发送到指定的 topic，topic 的每个 partition 收到 producer 发送的数据后，
都需要向 producer 发送 ack(acknowledgement 确认收到)，如果 producer 收到 ack，就会进行下一轮的发送，否则重新发送数据。

>ack应答机制的3个级别
>0：代表 producer 往集群发送数据不需要等到集群的返回，不确保消息发送成功。安全性最低但是效率最高；
>1：代表 producer 往集群发送数据只要 leader 应答就可以发送下一条，只确保 leader 发送成功；
>all：代表 producer 往集群发送数据需要所有的 follower 都完成从 leader 的同步才会发送下一条，
>确保 leader 发送成功和所有的副本都完成备份；安全性最高，但是效率最低。

要注意的是，如果往不存在的 topic 写数据，能不能写入成功呢？  
**kafka会自动创建topic，分区和副本的数量根据默认配置都是1。**
>RabbitMQ 往不存在的 topic 发送消息是会消失在黑洞的。~~（我不知道，网上说的）~~

**ISR**  
Leader 维护了一个动态的 in-sync replica set (ISR)，意为和 leader 保持同步的 follower 集合。
当 ISR 中的 follower 完成数据的同步之后，follower 就会给 leader 发送 ack。如果 follower 长时间未向 leader 同步数据，
则该 follower 将被踢出 ISR，该时间阈值由replica.lag.time.max.ms 参数设定。  
Leader 发生故障之后，就会从 ISR 中选举新的 leader。

**故障处理细节**  
Log文件中的HW和LEO：  
![Log文件中的HW和LEO](https://img-blog.csdnimg.cn/20200506171910881.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzE2MTQ2MTAz,size_16,color_FFFFFF,t_70)

LEO：指的是每个副本最大的 offset；  
HW：指的是消费者能见到的最大的 offset，ISR 队列中最小的 LEO。

1. follower故障  
follower 发生故障后会被临时踢出 ISR，待该 follower 恢复后，follower会读取本地磁盘记录的上次的 HW，并将 log 文件高于 HW 的部分截取掉，
从 HW 开始向 leader 进行同步。等**该 follower 的 LEO 大于等于该 Partition 的 HW**，即 follower 追上 leader 之后，就可以重新加入 ISR 了。

2. leader故障  
leader 发生故障之后，会从 ISR 中选出一个新的 leader，之后，为保证多个副本之间的数据一致性，
其余的 follower 会先**将各自的 log 文件高于 HW 的部分截掉**，然后从新的 leader 同步数据。

>注意：这只能保证副本之间的**数据一致性**，并不能保证数据不丢失或者不重复。

### <span id="保存数据">保存数据</span>
Producer 将数据写入 kafka 后，集群就需要对数据进行保存了。  

**Partition 结构**  
前面说过了每个 topic 都可以分为一个或多个 partition。Partition 在服务器上的表现形式就是一个一个的文件夹，
每个 partition 的文件夹下面会有多组 segment 文件，每组 segment 文件又包含 .index 文件、.log 文件、.timeindex 文件（早期版本中没有）三个文件，
log 文件就实际是存储 message 的地方，而 index 和 timeindex 文件为索引文件，用于检索消息。

![partition结构图](http://cdn.17coding.info/WeChat%20Screenshot_20190325215337.png)

如上图，这个 partition 有三组 segment 文件，每个 log 文件的大小是一样的，但是存储的 message 数量是不一定相等的（每条的 message 大小不一致）。
文件的命名是以该 segment 最小 offset 来命名的，如 000.index 存储 offset 为 0~368795 的消息，kafka 就是利用分段+索引的方式来解决查找效率的问题。

**Message 结构**  
上面说到 log 文件就实际是存储 message 的地方，我们在 producer 往 kafka 写入的也是一条一条的 message，
那存储在 log 中的 message 长啥样呢？消息主要包含消息体、消息大小、offset、压缩类型……等等！重点需要知道的是下面三个：
1. offset：offset 是一个占 8byte 的有序id号，它可以唯一确定每条消息在 partition 内的位置！
2. 消息大小：消息大小占用 4byte，用于描述消息的大小；
3. 消息体：消息体存放的是实际的消息数据（被压缩过），占用的空间根据具体的消息而不一样。

**存储策略**  
无论消息是否被消费，kafka 都会保存所有的消息。那对于旧数据有什么删除策略呢？  
1. 基于时间，默认配置是168小时（7天）；
2. 基于大小，默认配置是1073741824。
>需要注意的是，kafka 读取特定消息的时间复杂度是 O(1)，所以这里删除过期的文件并不会提高 kafka 的性能！

### <span id="消费数据">消费数据</span>
消息存储在log文件后，消费者就可以进行消费了。与生产消息相同的是，消费者在拉取消息的时候也是找leader去拉取。

多个消费者可以组成一个消费者组（consumer group），每个消费者组都有一个组id！同一个消费组者的消费者可以消费同一topic下不同分区的数据，
但是不会组内多个消费者消费同一分区的数据！！！可能有点绕。看下图：  

![消费者组消费不同分区消息](http://cdn.17coding.info/WeChat%20Screenshot_20190325215326.png)

图示是消费者组内的消费者小于 partition 数量的情况，所以会出现某个消费者消费多个 partition 数据的情况，
消费的速度也就不及只处理一个 partition 的消费者的处理速度！如果是消费者组的消费者多于 partition 的数量，
是不会出现多个消费者消费同一个 partition 的数据的！多出来的消费者不消费任何partition的数据。  
所以在实际的应用中，建议**消费者组的 consumer 的数量与 partition 的数量一致！**  

问题又来了，一个 consumer group 中有多个 consumer，一个 topic 有多个 partition，所以必然会涉及到 partition 的分配问题，
即确定那个 partition 由哪个 consumer 来消费。

**分区分配策略**  
Kafka 有两种分配策略，一是 RoundRobin，一是 Range。

**Range**  
是对每个 topic 而言的。首先按照分区序号排序，然后将消费者排序。分区数/消费者数=m，如果m！=0，前m个消费者多消费一个分区（每个 topic）；  
如果有多个 topic 的 m 都 不等于 0，那么，前 m 个消费者将多消费多个分区，造成倾斜，这就是 Range 的一个很明显的弊端。

**RoundRobin**  
RoundRobin 策略的工作原理：将所有主题的分区组成 TopicAndPartition 列表，然后对 TopicAndPartition 列表按照 hashCode 进行排序，
分发给每一个消费者。（其实就是按分区名 hash 排序后平均分配给每一个消费者的线程）

使用RoundRobin策略有两个前提条件必须满足：
1. 同一个Consumer Group里面的所有消费者的 num.streams(这玩意他丫的究竟是啥) 必须相等；
2. 每个消费者订阅的主题必须相同。

目前还不能自定义分区分配策略，只能通过 partition.assignment.strategy 参数选择 range 或 roundrobin。
>partition.assignment.strategy参数默认的值是range。

**分区分配的触发条件**  
- 同一个 Consumer Group 内新增消费者；
- 消费者离开当前所属的 Consumer Group，包括 shuts down 或 crashes(崩~溃~)；
- 订阅的主题新增分区。

**offset**  
前面曾多次提到 segment 和 offset，查找消息的时候是如何利用 segment+offset 配合查找的呢？
现以需要查找一个 offset 为 368801 的 message 的过程为例，先看看下面的图：  
![图片裂开就别看了](http://cdn.17coding.info/WeChat%20Screenshot_20190325215338.png)  

1. 先找到 offset 的 368801message 所在的 segment 文件（利用二分法查找），这里找到的就是在第二个 segment 文件；
2. 打开找到的 segment 中的 .index 文件（也就是 368796.index 文件，该文件起始偏移量为 368796+1，
我们要查找的 offset 为 368801 的 message 在该 index 内的偏移量为 368796+5=368801，所以这里要查找的相对 offset为5）。
由于该文件采用的是稀疏索引的方式存储着相对 offset 及对应 message 物理偏移量的关系，所以直接找相对 offset 为 5 的索引找不到，
这里同样利用二分法查找相对 offset 小于或者等于指定的相对 offset 的索引条目中最大的那个相对 offset，所以找到的是相对 offset 为 4 的这个索引；
3. 根据找到的相对 offset 为 4 的索引确定 message 存储的物理偏移位置为 256。打开数据文件，
从位置为 256 的那个地方开始顺序扫描直到找到 offset 为 368801 的那条 Message。

这套机制是建立在 offset 为有序的基础上，利用 segment+有序offset+稀疏索引+二分查找+顺序查找 等多种手段来高效的查找数据！



