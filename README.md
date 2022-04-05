# vertx-cluster

This repository contains example code to run Vert.x in cluster mode with Hazelcast as default cluster manager. The code is split into 2 main modules - primary and peer. 

- Primary verticle: Run a HTTP server that listens on port 9560, accepts a POST on ``"/send/msg:"`` path. All incoming messages are broadcast to all peers via the clustered eventbus
- Peer Verticles: Connects to the cluster and echo an incoming message back to primary verticle

## How to use

The gradle project has a ``shadowJar`` tasks that will create a fat-jar for both primary and peer modules. 

Run the primary verticle as:
```
java -jar vertx-cluster-primary-all.jar
```

Run the peer verticle as:
```
java -jar vertx-cluster-peer-all.jar
```

Connect to the primary verticle and post message with ``curl`` as:
```
curl -XPOST localhost:9560/send/msg:Test
```

## References
- https://vertx.io/docs/vertx-hazelcast/java/
- https://medium.com/halofina-techology/high-performance-web-app-with-vert-x-cluster-28caf7004aab
- https://github.com/hakdogan/IntroduceToEclicpseVert.x
- https://hakdogan.medium.com/how-to-run-a-vert-x-cluster-with-broadcasting-messaging-fc79ff113c9c
