package sample

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol

trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat2(User)
  implicit val userAgeFormat = jsonFormat1(UserAge)
}

class Api extends ApiRoutes

trait ApiRoutes extends MyJsonProtocol {
  def routes: Route =
    greetingRoute ~ userRoute

  def greetingRoute =
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
      }

  def userRoute =
    pathPrefix("users" / Segment) { name =>
      pathEndOrSingleSlash {
        get {
          complete(OK, User(name, 10))
        } ~
          post {
            entity(as[UserAge]) { ua =>
              complete(OK, User(name, ua.age))
            }
          }
      }
    }
}

case class User(name: String, age: Int)

case class UserAge(age: Int)
