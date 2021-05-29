verx. in action
asynchronous and reactive java
discovered this flexible framework for building asynchronous application

it's more like a toolkit of high-quality tools that are designed to work together,but
you have to decide how to integrate them

reactive applications are all about:latency is under control both as the workload grows and when failures happen.

asynchronous and reactive applications are an important topic in modern distributed system.

embracing failure is key to designing dependable system

https://github.com/jponge/vertx-in-action
www.manning.com/books/vertx-in-action

the first step toward building reactive systems is to adopt asynchronous programming

asynchronous and reactive are important topics in modern applications

the idea behind non-blocking I/O is to request a (blocking) operation, and move on to doing ohter tasks until the operation
result is ready.

many concurrent connections can be multiplexed on a single thread,
as network latency typically exceeds the CPU time it takes to read incoming bytes.

java.nio focues solely on what it does,it does not provide higher-level protocol-specific helpers
java.nio does not prescribe a threading moedl, which is still important to properly utilize CPU cores, nor does it handle
asnychronous I/O event or articulate the application processing logic.

a popular threading model for processign asynchronous events is that of the event loop.
instead of polling for events that may have arrived, like nio, events are pushed to an event loop.

events are queued as they arrvie. a single thread is assigned to an event loop, and processing events shouldn't perform
any blocking or long-running operation.

the four properties fo reactive system are exposed :
responsive,resilient,elastic,message-driven
Elastic--Elasticity is the ability for the application to work with a vairable number of instances.
Resilient--Resiliency is partially the flip side of elasticity. 快速转移和恢复
Responsive-- is the result of combining elasticity and resiliency. consistent response
Message-driven--Using asynchrounous message passing rather than blocking paradigms like remote procedure calls

reactive implies asynchronous, but the converse is not nessarily true
shopping, session are being stored in memory or in local files, the system is not reactive. indeed an instance of the application
cannot take over another one because session are application state
a reactive varant of this example would use a memory grid serviec(eg. redis) to store the web session, so that incoming requests
could be routed to any instance
也就是可以动态扩容，即使有状态吧。

vert.x is a tool-kit for building reactive application on the jvm
the focus of vert.x is processing asynchronous event, mostly coming from non-blocking I/O, and the threading model processes events in an event loop

vert.x project is organized in composable modules

asynchronous programming allows you to multiplex multiple networked connections on a single thread
the event loop and reactor pattern simplify asynchronous event processing
a reactive systrem is both scalable and resilient, producing responses with consistent latencies despite demanding workloads and failures.
vertx is an approachable, efficient toolkit for writing asynchronous and reactive applicaiton on the jvm


a verticle is the fundamental processing unit in vert.x
the role of a verticle is to encapsulate a technical funcational unit for processing event.
verticles can be deployed, and they have a life cycle.

verticles have private state that may be updated when receiving events, they can deploy other verticles, and they can communicate via
message-passing

no long-running or blocking I/O operations should happen on the vent loop

warnings start to appear while the event-loop thread is running the infinite loop and hence is not available for processing other event.

全局改变配置
-Dvertx.options.blockedThreadCheckInterval=5000  #改变vertx的检查阻塞时间
-Dvertx.threadChecks=false  #禁止线程检查

it is a good robustness practice to use the asynchrounous method variants that accept a callback to notify of errors.

the basic rule when running code on an event loop is that it should not block, it should run "fast engough".
vertx detects and warns when an event loop is being blocked for too long.
for long run, vertx provides two options for dealing with such cases: worker verticles and executeBlocking operation

worker verticles execute on worker threads, threads taken from special worker pool.
a worker verticle processes events just like an event-loop verticle, except that it can take an arbitrarily long time to do
a worker verticle is not tied to a single worker thread, successive events may not execute on the same thread
worker verticles may only be accessed by a single worker thread at a given time.


a verticle object is essentially the combination of two objects:
the vert.x instance the verticle belongs to
a dedicated context instance that allows events to be dispatched to handlers

the vertx instance is being shared by multiple verticles, and there is generally only one instacne of vertx per jvm process
事件都在context上，被关联到eventloop上执行

verticles are the core compoenent for asynchrounous event processing in vertx application
event-loop verticles process asynchrounous I/O events and shoud be free of blocking and long-running operations
worker verticles can be used to proess blocking I/O and long-running operations

a vertx application is made up of one or more verticles, and each verticle forms a unit for processing asynchronous event.



connecting verticles and making sure they can cooperate is the role of the event bus
the event bus offers a way to transparently distribute event-processing work both inside a process and across several nodes over the network
the event bus is a means for sending and receiving messages in an asynchronous fashion.
messages have a body, optional headers, an expiration timestamp after which they will be discarded if they haven't been processed yet.

the event bus allows for decoupling between verticles. allows verticles written in different languages
the event bus can extended outside of the application process.

an event bus for verticle-to-verticle communications inside an aplication, not a message bus for application-to-application communications.

the event bus does not do the following:
support message acknowledgments
support message priorities
supoort message durability to recover from crashes
provide routine rules
provide transformation rules(schema adaptation, scatter/gather)

the event bus simply carries volatile events that are being processed asynchrounously by verticles.
not all events are created equal, and while some may be lost, some may not
the event bus is a simple and fast event conveyor, we can take advantage of it for most verticle-to-verticle interactions,
while turning to more costly middleware for events that cannot be lost.

point-to-point messaging
one of the possibly multiple consumers picks a message and processes it, round-robin
there is no fainess mechanism to distribute fewer mesages to an overloaded consumer

request-reply messaging
when a message is sent in point-to-point messaging, it is possiple to reigster a reply handler.
when you do, the event bus generates a temporary desitnation name dedicated solely to communications between the request message
producer that is expecting a reply, and the consumer that will eventually receiver and process the mesasge.
eventbus特殊处理了，不像rpc那样一直等待

publish/subscribe messgin
all subscribers receiver it ，所有消费者都要收到
is useful when you are not sure how many verticles nad handlers will be interested in a paritcular event.

所有node都连接到clustermanager，每个node可能交互

a cluster manager ensures nodes can exchange messages over the event bus, enabling the following set of functionalities:
a. group membership and discovery allow discovering new nodes, maintaining the list of current nodes, and detecting when node disappear.
b. shared data allows maps and counters to be maintained cluster-wide,so that all nodes share the same valus.
c. subscriber topology allows knowing what event-bus destinations each node has interest in.

there are several cluster manager implementations for verx based on Hazelcast(default), Infinispan, Ignite, ZK

the event-bus communications between nodes happen through direct TCP connections, using a custom protol.
节点发送消息到目标时，检查订阅拓扑使用cluster manager并派发消息到订阅的consuemr的node上

by default, some cluster managers use multicast communications for node discovery, which may be disabled on some network.
you will need to conifgure the cluster manager to work in these environments

the event bus is the preferred way for verticles to communicate, and it uses asynchronous message passing.
the event bus does not provide durability guarantees, so it must only be used for transient data.



reactive applications deal with non-blocking I/O, efficient and correct stream processing is key

vertx offers a unified abstraction of streams across several types of resourcs, such as files, network sockets, and more
a read stream is a source of events that can be read

ReadStream essential metdhos
handler  --  handles a new read vlaue of type T
exceptionHandler  --  Handlers a read exception
endHandler  --  Called when the stream has ended, either all data has been read or because an exception has been raised


pull and push data from steam, the difference is major, nad we need to understand it to master asynchrounous streams

back-pressure is a mechanism for a consumer of events to signal an event's producers that it is emmitting events at a
faster rate than the consumer can handle them.

in reactive systems, back-pressure is used to pause or slow down a producer so that consumers avoid accumulating unprocessed
events in unbounded memory buffers, possibly exhausing resources.

http服务器提供下载
Reading and then writing data between streams with any back-pressure signaling
File read stream--data-->Handler<Buffer>--data-->HTTP response--data-->Write queue--data-->TCP buffer

since the tcp buffer may be full(either because of the network or because the client is busy), it is necessary to maintain a
buffer of pending buffers to be written. a write operation is non-blocking, so buffering is needed.
Reading from a filesystem is generally fast an low-latency. By contrast, writing to the network is much slower, and bandwidth
depends on the weakest network link, delays also occur.
As the reads are much faster than writes, a write buffer may quickly grow very large.

one solution is back-pressure signaling, which enalbes the read stream to adapt to the throughput of the write stream.
in practice, pausing the source stream is a goold way to manage back-pressure, because it gives time to write the items in
the write buffer while not accmulaing new ones.

By default, a read stream reads data as fast as it can, unless it is being paused.
ReadStream back-pressure management methods
pause  --  Pauses the stream, preventing further data from begin sent to the handler
resume  --  Starts reading data again and sending it to the handler.
fetch  --  Demands a number, n of elements to be read(at most). the stream must be paused before calling fetch

WriteStream back-pressure management methods
setWriteQueueMaxSize  -- defines maximun write buffer queue size shoud be before being considered full.
writeQueueFull  --  indicate when the write buffer queue size is full.
drainHandler  --  defins a callback indicating when the write buffer queue has been drained(typically when it is back to half of its maximum size)
when the writer knows that the write queue is full, it can be notified through a drain handlerwhen data can be written aggin.

the recipe for contorlling the flow in upon http example :
1. for each read buffer, write it to the HTTP response stream.
2. check if the write buffer queue is full.
3. if it is full:
   Pause the file read stream.
   install a drain handler that resumes the file read stream when it is called


vertx streams model asynchronous event and data flows, and they can be used in both push and pull/fetch modes.
back-pressure management is essential for ensuring teh coordinated exchange of events between asynchronous system.


callback hell can easily be mitigated using one methdo for each asynchronous operation callback, using a method invoke


a promise holds the value of some computation for which there is no value right now.
a promise is eventually completed with a result value or an error.
a promise is used to write an eventual value, and a future is used to read it when it is available.


reactive extensions are an elaborted form of the observable design pattern
reactive extesions are defined by three things:
a.Observing evnet or data streams
b.Composing operators to trnasform streams
c.Subscribing to streams and reacting to events and errors

we covered three different asynchronous programming models that are generally better than callback.
Opting for one model or the other depends essentially on what you are trying to achieve.

Callbacks have expressiveness limitations when it comes to composing asynchronous operation
Parallel and sequential asynchronous operations can be composed with other asynchronous programming moedls:futures and promises,
reactive extension, and coroutines.
Reactive extensions have a rich set of composable operators, and they are especially well suited for event streams.
Futures and promises are great for simple chaining of asynchronous operations
Kotlin coroutines provide language-level support for asynchronous operation, which is another interesting option
There is no universally good asynchronous programming model as they all have their preferred use cases.


automated testing is critical in designing software

event-bus services and proxies abstract from event-bus communications by providing an asynchronous service interface


## second part2

previous we covered some elements of reactive:
a.back-pressure
b.reactive programming
c.reactive application

reactive manifesto: reactive application are responsive, resilient, elastic, message-driven

a good responsiveness example would be a service that responds within 500 ms in the 95% percentile, provided that
500ms is a good number given the service's funcional requirements and operational constraints
如何处理失败？假设对于db连接，有个死锁，而直到超时，我们才能感知到错误，太长了，若是db马上挂，tcp立即感应到错误，那么就没有任何latency

it is also important to see failure in the light of a service's functional requirements and application domain: the response to
failure may not always be an error.


CORS。。。。
