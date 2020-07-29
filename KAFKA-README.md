# Kafka
1. [Kafka概述](#Kafka概述)
2. [Kafka基础架构](#Kafka基础架构)

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
![Kafka基础架构](https://upload-images.jianshu.io/upload_images/13134428-8c888e3fcda0d81d.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp)

**Messages And Batches**  
Kafka 的基本数据单元被称为 message(消息)，为减少网络开销，提高效率，多个消息会被放入同一批次 (Batch) 中后再写入。

1. Producer：消息生产者，就是向 kafka broker 发消息的客户端；
2. Consumer：消息消费者，向 kafka broker 取消息的客户端；
3. Consumer Group (CG)：消费者组，由多个 consumer 组成。消费者组内每个消费者负责消费**不同分区**的数据，
**一个分区只能由一个组内消费者消费**；消费者组之间互不影响。所有的消费者都属于某个消费者组，即消费者组是**逻辑上的一个订阅者**；
4. Broker：一台 kafka 服务器就是一个 broker。一个集群由多个 broker 组成；一个 broker 可以容纳多个 topic；
5. Topic：可以理解为一个队列，生产者和消费者面向的都是一个 topic; 
6. Partition：为了实现扩展性，一个非常大的 topic 可以分布到多个 broker(即服务器)上， 一个 topic 可以分为多个 partition，
每个 partition 是一个有序的队列；
>一个分区就是一个提交日志 (commit log)。消息以追加的方式写入分区，然后以先入先出的顺序读取；  
>由于一个 Topic 包含多个分区，因此无法在整个 Topic 范围内保证消息的顺序性，但可以保证消息在单个分区内的顺序性。

>**Offset**  
>Partition 中的每条消息都会被分配一个有序的 id（Offset）。Kafka 只保证每个 Partition中的顺序，不保证多个Partition的顺序。
7. Replica：副本，为保证集群中的某个节点发生故障时，该节点上的 partition 数据不丢失，且 kafka 仍然能够继续工作，
kafka 提供了副本机制，一个 topic 的每个分区都有若干个副本，一个 leader 和若干个 follower；
8. leader：每个分区多个副本的“主”，生产者发送数据的对象，以及消费者消费数据的对象都是 leader。
9. follower：每个分区多个副本中的“从”，实时从 leader 中同步数据，保持和 leader 数据的同步。leader 发生故障时，某个 follower 会成为新的 leader。

