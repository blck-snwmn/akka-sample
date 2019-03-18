package sample

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{MustMatchers, WordSpecLike}
import sample.UserDetail.{GetPoints, GetUser}

class UserDetailSpec extends TestKit(ActorSystem("UserDetail"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "UserDetail" must {
    "return User of points = 0 when don't receiveGetPoints" in {
      val name = "name1"
      val uerDetail = system.actorOf(UserDetail.props(name), name)
      uerDetail.tell(GetUser, testActor)
      expectMsg(Some(AccountManager.User(name, 0)))
    }

    "return User that have received points when receive GetPoints" in {
      val name = "name2"
      val uerDetail = system.actorOf(UserDetail.props(name), name)
      uerDetail ! GetPoints(10)
      uerDetail.tell(GetUser, testActor)
      expectMsg(Some(AccountManager.User(name, 10)))
    }

    "overflow points when receive two GetPoints over Int.MaxValue" in {
      val name = "name3"
      val uerDetail = system.actorOf(UserDetail.props(name), name)
      uerDetail ! GetPoints(Int.MaxValue)
      uerDetail ! GetPoints(1)
      uerDetail.tell(GetUser, testActor)
      expectMsg(Some(AccountManager.User(name, Int.MinValue)))
    }
  }
}
