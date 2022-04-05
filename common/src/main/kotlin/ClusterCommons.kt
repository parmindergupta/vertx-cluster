package vertx.cluster.common

import com.hazelcast.config.Config

enum class CommonValues(val value: String) {
    PRIMARY_BROADCAST_ADDRESS("vert.cluster.primary.broadcast"),
    PEER_REPLY_ADDRESS("vertx.cluster.peer.reply"),
    CLUSTER_NAME("VERTX-TEST-CLUSTER"),
}

fun getHazelCastConfig(): Config {
    val hazelcastConfig = Config()
    // the name of the cluster. Only member of the same cluster can join the cluster
    hazelcastConfig.clusterName = CommonValues.CLUSTER_NAME.value
    //set the IP Ranges where HazelCast will search for peers
    hazelcastConfig.networkConfig.interfaces.setEnabled(true)
        .addInterface("192.168.1.*")
    var joinConfig = hazelcastConfig.networkConfig.join
    // Use Multicast as discovery mechanism
    joinConfig.multicastConfig.isEnabled = true
    // only 1 method is allowed, use TCP/IP on networks where multicast is disabled
    joinConfig.tcpIpConfig.isEnabled = false
    hazelcastConfig.networkConfig.join = joinConfig
    return hazelcastConfig
}
