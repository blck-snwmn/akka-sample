package sample

import akka.actor.{Actor, Props}

/**
  * AccountManagerのPropsの定義、メッセージの定義
  */
object AccountManager {
  def props() = Props[AccountManager]

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
class AccountManager extends Actor {

  import AccountManager._

  var users: Map[String, User] = Map.empty[String, User]

  override def receive: Receive = {
    case GetUser(name) => sender() ! users.get(name)
    case GetUsers =>
      sender() ! Users(users.values.toVector)
    case CreateUser(name, points) =>
      if (users.contains(name)) sender() ! UserExists
      else {
        val user = User(name, points)
        users = users + (name -> user)
        sender() ! UserCreated(user)
      }
  }
}
