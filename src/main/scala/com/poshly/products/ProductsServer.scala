package com.poshly.products

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.io.IO
import akka.util.Timeout
import com.poshly.core.configuration.Configuration
import com.poshly.products.actors.ProductsWebActor
import com.poshly.server.{Server, admin}
import spray.can.Http
import spray.routing.SimpleRoutingApp

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ProductsServer
  extends Configuration
  with Server
  with SimpleRoutingApp {

  override def code = "products"

  override def name = "Products UI"

  def port = config.getInt("products.network.admin-port")

  implicit val executor = ExecutionContext.Implicits.global
  implicit val system = ActorSystem("products-akka-system", config)

  onExit(IO(Http) ! PoisonPill)

  def main() {
    implicit val bindingTimeout: Timeout = 10.seconds

    // Start ZooKeeper
    val components = new ProductsComponents
    components.zkClient.start()

    val api = system.actorOf(Props(new ProductsWebActor(system, components)), "products-web-actor")
    IO(Http) ! Http.Bind(api, interface = config.getString("products.network.interface"), port = config.getInt("products.network.web-port"))
  }
}
