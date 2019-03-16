package sample

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App {
  val interface = "localhost"
  val port = 8080

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val binding: Future[ServerBinding] = Http().bindAndHandle(new Api().routes, interface, port)

  val log = Logging(system.eventStream, "Main")

  binding.onComplete {
    case Success(_) =>
      log.info("success")
    case Failure(_) =>
      log.error("failure")
  }
}

trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(User)
}

class Api extends ApiRoutes

trait ApiRoutes extends MyJsonProtocol {
  def routes =
    pathPrefix("greeting") {
      pathEndOrSingleSlash {
        get {
          complete("hello akka")
        }
      }
    } ~
      pathPrefix("greeting" / Segment) { name =>
        pathEndOrSingleSlash {
          get {
            //implicit conversion
            // val codeAndValue: ToResponseMarshallable = ToResponseMarshallable(OK, s"hello akka $name")
            // complete(codeAndValue)
            complete(OK, s"hello akka $name")
          }
        }
      } ~
      pathPrefix("user" / Segment) { name =>
        pathEndOrSingleSlash {
          get {
            complete(OK, User(name, 10))
          }
        }
      }
}

case class User(name: String, age: Int)
