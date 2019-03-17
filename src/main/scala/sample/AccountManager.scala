package sample

import akka.actor.{Actor, Props}

object AccountManager {
  def props() = Props[AccountManager]

  def name = "accountManager"

  case class CreateUser(name: String, age: Int)

  sealed trait Response

  case class UserCreated(user: User) extends Response

  case object UserExists extends Response

}

class AccountManager extends Actor {

  import AccountManager._

  var users: Map[String, User] = Map.empty[String, User]

  override def receive: Receive = {
    case CreateUser(name, age) =>
      if (users.contains(name)) sender() ! UserExists
      else {
        val user = User(name, age)
        users = users + (name -> user)
        sender() ! UserCreated(user)
      }
  }
}
