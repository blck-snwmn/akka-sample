package sample

import akka.actor.{Actor, Props}

object UserDetail {
  def props(name: String) = Props(new UserDetail(name))

  case object GetUser

  case class GetPoints(points: Int)

  case class UsePoint(points: Int)

}

class UserDetail(name: String) extends Actor {

  import UserDetail._

  var points: Int = 0

  override def receive: Receive = {
    case GetPoints(gotPoints) => points = points + gotPoints
    case GetUser => sender() ! Some(AccountManager.User(name, points))
  }
}
