package vertx.cluster.primary

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
            vertx.deployVerticle(PrimaryVerticle())
                .onSuccess { println("PRIMARY Verticle deployed in Cluster mode with id: $it") }
                .onFailure { println("Failed to start PRIMARY Verticle in cluster mode: ${it.localizedMessage}") }
        }
        .onFailure { println("Failed to start cluster: ${it.localizedMessage}") }
}

class PrimaryVerticle() : CoroutineVerticle() {
    private val MESSAGE_PARAM = "message"
    private val PORT = 9560
    private lateinit var router: Router

    override fun init(vertx: Vertx, context: Context) {
        super.init(vertx, context)
        println("Setting up PRIMARY verticle")
        router = Router.router(vertx)
    }

    override suspend fun start() {
        super.start()

        router.route("/").handler { context ->
            val response = context.response()
            response.putHeader("content-type", "text/html")
                .end("<h1>Hello from PRIMARY</h1>")
        }

        router.post("/send/msg:$MESSAGE_PARAM").handler { context ->
            val eventBus = vertx.eventBus()
            val message = context.request().getParam(MESSAGE_PARAM)
            println(">> Outgoing to peers: $message")
            eventBus.publish(CommonValues.PRIMARY_BROADCAST_ADDRESS.value, message)
            println("Current Thread Id ${Thread.currentThread().id} Is Clustered ${vertx.isClustered} ")
            context.response().end(message)
        }

        vertx.eventBus().consumer<String>(CommonValues.PEER_REPLY_ADDRESS.value).handler { message ->
            println("<< Echo from peers: ${message.body()}")
        }

        vertx.createHttpServer().requestHandler(router)
            .listen(PORT)
            .onSuccess { result -> println("Started PRIMARY Verticle on port ${result.actualPort()}") }
            .onFailure { error ->
                println("Failed to start PRIMARY Verticle: ${error.localizedMessage}")
                router.clear()
            }

    }

    override suspend fun stop() {
        super.stop()
    }
}