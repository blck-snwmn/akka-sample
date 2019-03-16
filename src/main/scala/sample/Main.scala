package sample

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App {
  val interface = "localhost"
  val port = 8080

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val binding: Future[ServerBinding] = Http().bindAndHandle(new Api(system).routes, interface, port)

  val log = Logging(system.eventStream, "Main")

  binding.onComplete {
    case Success(_) =>
      log.info("success")
    case Failure(_) =>
      log.error("failure")
  }
}

