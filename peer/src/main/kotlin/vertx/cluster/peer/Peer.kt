package vertx.cluster.peer

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import vertx.cluster.common.CommonValues
import vertx.cluster.common.getHazelCastConfig
import java.net.InetAddress

fun main(args: Array<String>) {
    val host = InetAddress.getLocalHost().hostAddress
    val mgr: ClusterManager = HazelcastClusterManager(getHazelCastConfig())

    val options = VertxOptions().setClusterManager(mgr)
    Vertx.clusteredVertx(options)
        .onSuccess { vertx ->
            vertx.deployVerticle(PeerVerticle())
                .onSuccess { println("PEER Verticle deployed in Cluster mode with id: $it") }
                .onFailure { println("Failed to start PEER Verticle in cluster mode: ${it.localizedMessage}") }
        }
        .onFailure { println("Failed to start cluster: ${it.localizedMessage}") }
}

class PeerVerticle(): CoroutineVerticle() {

    private lateinit var router : Router

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        println("Setting up PEER verticle")
        router = Router.router(vertx)
    }

    override suspend fun start() {
        super.start()

        val eventBus = vertx.eventBus()
        eventBus.consumer<String>(CommonValues.PRIMARY_BROADCAST_ADDRESS.value).handler { message ->
            println("<< MSG from PRIMARY: ${message.body()}")
            eventBus.publish(CommonValues.PEER_REPLY_ADDRESS.value, "Received message: ${message.body()}")
            println(">> Echo to PRIMARY: ${message.body()}")
        }
    }

    override suspend fun stop() {
        super.stop()
    }
}