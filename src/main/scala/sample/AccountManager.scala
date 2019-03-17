package sample

import akka.actor.{Actor, Props}

object AccountManager {
  def props() = Props[AccountManager]

  def name = "accountManager"

  case class User(name: String, age: Int)

  case class Users(users: Vector[User])

  case class CreateUser(name: String, age: Int)

  case class GetUser(name: String)

  case object GetUsers

  sealed trait Response

  case class UserCreated(user: User) extends Response

  case object UserExists extends Response

}

class AccountManager extends Actor {

  import AccountManager._

  var users: Map[String, User] = Map.empty[String, User]

  override def receive: Receive = {
    case GetUser(name) => sender() ! users.get(name)
    case GetUsers => users.values
    case CreateUser(name, age) =>
      if (users.contains(name)) sender() ! UserExists
      else {
        val user = User(name, age)
        users = users + (name -> user)
        sender() ! UserCreated(user)
      }
  }
}
