package sample

import scala.concurrent.Future
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

/**
  * AccountManagerのPropsの定義、メッセージの定義
  */
object AccountManager {
  def props(implicit timeout: Timeout) = Props(new AccountManager())

  def name = "accountManager"

  case class User(name: String, points: Int)

  case class Users(users: Vector[User])

  case class CreateUser(name: String, points: Int)

  case class GetUser(name: String)

  case object GetUsers

  sealed trait Response

  case class UserCreated(user: User) extends Response

  case object UserExists extends Response

}

/**
  * Accountの管理を行うActor
  */
class AccountManager(implicit timeout: Timeout) extends Actor {

  import AccountManager._
  import context._

  def createUserDetail(name: String) =
    context.actorOf(UserDetail.props(name), name)

  override def receive: Receive = {
    case GetUser(name) =>
      def notFound(): Unit = sender() ! None

      /**
        * about forward
        * parent: Actor
        * child: Actor
        * grandchild: Actor
        * 1. parent send child 'messageA'
        * 2. child receive 'messageA'
        * 3. child forward grandchild 'messageB'
        * 4. in grandchild, sender() is parent
        */
      def forwardGetMessage(child: ActorRef): Unit = child forward UserDetail.GetUser

      context.child(name).fold(notFound())(forwardGetMessage)

    case GetUsers =>
      import akka.pattern.ask
      import akka.pattern.pipe
      def getUsers = context.children.map { child =>
        self.ask(GetUser(child.path.name)).mapTo[Option[User]]
      }

      val f = Future.sequence(getUsers).map(_.flatten).map(l => Users(l.toVector))
      pipe(f) to sender()

    case CreateUser(name, points) =>
      def create(): Unit = {
        val actor = createUserDetail(name)
        actor ! UserDetail.GetPoints(points)
        sender() ! UserCreated(User(name, points))
      }

      context.child(name).fold(create())(_ => sender() ! UserExists)
  }
}
