package sample

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Created, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import scala.concurrent.duration._

import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContext, Future}

trait MyJsonProtocol extends DefaultJsonProtocol {

  import sample.AccountManager._

  implicit val userFormat = jsonFormat2(User)
  implicit val userAgeFormat = jsonFormat1(UserAge)
  implicit val errorFormat = jsonFormat1(Error)
}

class Api(system: ActorSystem) extends ApiRoutes {
  implicit def executionContext = system.dispatcher

  override def createAccountManager(): ActorRef = system.actorOf(AccountManager.props, AccountManager.name)

  override implicit def requestTimeout: Timeout = Timeout(100 milliseconds)
}

trait ApiRoutes extends MyJsonProtocol with AccountManagerApi {

  def routes: Route =
    greetingRoute ~ usersRoute ~ userRoute

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

  def usersRoute =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          onSuccess(Future(List(AccountManager.User("bob", 10), AccountManager.User("tom", 12)))) { users =>
            complete(OK, users)
          }
        }
      }
    }

  def userRoute =
    pathPrefix("users" / Segment) { name =>
      pathEndOrSingleSlash {
        get {
          complete(OK, AccountManager.User(name, 10))
        } ~
          post {
            entity(as[UserAge]) { ua =>
              onSuccess(createUser(name, ua.age)) {
                case AccountManager.UserCreated(user) =>
                  complete(Created, user)
                case AccountManager.UserExists =>
                  val err = Error(s"$name user exists already.")
                  complete(BadRequest, err)
              }
            }
          }
      }
    }
}

trait AccountManagerApi {

  import AccountManager._

  def createAccountManager(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  lazy val accountManager = createAccountManager()

  def createUser(name: String, age: Int) =
    accountManager.ask(CreateUser(name, age)).mapTo[Response]
}

case class UserAge(age: Int)

case class Error(message: String)
