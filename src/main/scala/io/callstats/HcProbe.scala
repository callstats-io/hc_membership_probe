package io.callstats

import java.util.Date

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.jetty.JettyHttpContainerFactory
import org.glassfish.jersey.server.JSONP
import org.glassfish.jersey.server.ResourceConfig

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance
import com.typesafe.scalalogging.Logger

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import javax.ws.rs.core.UriInfo

case class HCMember(@BeanProperty uuID: String, @BeanProperty address: String)

object HcStatusProbe {

  val logger = Logger(classOf[HcStatusProbe])

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  // needs to be initialized when application launches
  var hcClient: Option[HazelcastInstance] = None
  var clusterAddres: Array[String] = _

  def createClient(clusterAddrs: Array[String]): Unit = {
    this.clusterAddres = clusterAddrs
    var clientConfig = new ClientConfig();

    logger.info(s"connecting to hazelcast $clusterAddres")
    clientConfig.getNetworkConfig().addAddress(clusterAddrs: _*)

    hcClient = Some(HazelcastClient.newHazelcastClient(clientConfig))
  }

}

@Path("/hc")
class HcStatusProbe {

  @GET
  @Path("ping")
  def getServerTime(): String = {
    "received ping at " + new Date().toString()
  }

  @GET
  @JSONP
  @Path("clusterinfo")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getMemberStatus = {
    val clusterInfo = HcStatusProbe.hcClient.get.getCluster
    var memberList = clusterInfo.getMembers().asScala.map(member => HCMember(member.getUuid, member.getSocketAddress().toString())).toList

    // return the result as java list
    memberList.asJava

  }

  // check members of the hazelcast cluster.
  // an optional query parameter 'gte' can specify the expected minimum size of the cluster.
  // if the size of the member list is less than the 'gte' parameter, return "412" status code
  // else return 200
  @GET
  @Path("members")
  def getMembers(@Context uriInfo: UriInfo): Response = {
    if (!HcStatusProbe.hcClient.get.getLifecycleService().isRunning()) {
      HcStatusProbe.logger.error("hazelcast client is not connected. Let's create a new connection!");
      HcStatusProbe.createClient(HcStatusProbe.clusterAddres)
    }

    val clusterInfo = HcStatusProbe.hcClient.get.getCluster
    var memberList = clusterInfo.getMembers().asScala.map(member => HCMember(member.getUuid, member.getSocketAddress().toString())).toList

    HcStatusProbe.logger.info(s"cluster has ${memberList.size} members")
    
    val jsonStr = HcStatusProbe.mapper.writeValueAsString(memberList);

    val gteParams = Option(uriInfo.getQueryParameters.getFirst("gte"))

    // use the 'gte' query parameter as minimum expected cluster size, default to 1
    val minClusterSize = if (gteParams.isDefined) gteParams.get.toInt else 1

    if (memberList.size >= minClusterSize) Response.ok(jsonStr, MediaType.APPLICATION_JSON).build()
    else Response.status(Response.Status.PRECONDITION_FAILED).`type`(MediaType.APPLICATION_JSON).entity(jsonStr).build()

  }
}

object HcProbe {

  def main(args: Array[String]): Unit = {

    // the "HC_ADDRS" can be comma separated 'ip:port' tuples indicate the hazelcast node addresses
    // if it is not defined, we'll just crash
    var hcClusterAddrEnv = sys.env.get("HC_ADDRS")
    if (!hcClusterAddrEnv.isDefined) {
      sys.error("environment variable HC_ADDRS not set!")
      sys.exit(-1)
    }

    var clusterAddrs = hcClusterAddrEnv.get.split(",")

    // initialize a HC client which connects to the provide HC cluster address
    HcStatusProbe.createClient(clusterAddrs)

    val defaultListenAddr = "127.0.0.1:8080"
    val listenAddr = sys.env.get("LISTEN_ADDR").getOrElse(defaultListenAddr)

    JettyHttpContainerFactory.createServer(
      UriBuilder.fromUri(s"http://${listenAddr}").build(),
      new ResourceConfig(classOf[HcStatusProbe], classOf[JacksonFeature]))
  }

}