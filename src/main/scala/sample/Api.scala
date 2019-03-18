package sample

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import scala.concurrent.duration._

import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContext, Future}

/**
  * jsonへのformatをまとめたtrait
  */
trait MyJsonProtocol extends DefaultJsonProtocol {

  import sample.AccountManager._

  implicit val userFormat = jsonFormat2(User)
  implicit val usersFormat = jsonFormat1(Users)
  implicit val userAgeFormat = jsonFormat1(UserPoints)
  implicit val errorFormat = jsonFormat1(Error)
}

/**
  * APIの実装を行うクラス
  * ActorSystemを受け取って、Actorの生成やExecuteContextの定義などを行う
  * timeoutについては固定
  *
  * @param system
  */
class Api(system: ActorSystem) extends ApiRoutes {
  implicit def executionContext = system.dispatcher

  override def createAccountManager(): ActorRef = system.actorOf(AccountManager.props, AccountManager.name)

  override implicit def requestTimeout: Timeout = Timeout(100 milliseconds)
}

/**
  * APIのルート
  * アクセス時に実際の処理を行うAPI traitをmixinする: AccountManagerApi
  * 返却はJsonで行うため、自作クラスについてformatをまとめたtraitをmixin: MyJsonProtocol
  */
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
          onSuccess(getUsers()) { users =>
            complete(OK, users)
          }
        }
      }
    }

  def userRoute =
    pathPrefix("users" / Segment) { name =>
      pathEndOrSingleSlash {
        get {
          onSuccess(getUser(name)) {
            _.fold(complete(NotFound))(u => complete(OK, u))
          }
        } ~
          post {
            //entity(as[T]) { t: T => ...  となる
            entity(as[UserPoints]) { ua =>
              onSuccess(createUser(name, ua.points)) {
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

/**
  * AccountManager Actor へ ask を行う
  * このtraitにActorRefを使用するものを集約する
  */
trait AccountManagerApi {

  import AccountManager._

  def createAccountManager(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  lazy val accountManager = createAccountManager()

  def createUser(name: String, points: Int) =
    accountManager.ask(CreateUser(name, points)).mapTo[Response]

  def getUser(name: String) =
    accountManager.ask(GetUser(name)).mapTo[Option[User]]

  def getUsers() =
    accountManager.ask(GetUsers).mapTo[Users]
}

case class UserPoints(points: Int)

case class Error(message: String)
