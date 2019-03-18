package sample

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.util.Timeout
import org.scalatest.{MustMatchers, WordSpecLike}
import scala.concurrent.duration._

class AccountManagerSpec extends TestKit(ActorSystem("AccountManager"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  import AccountManager._

  implicit val requestTimeout: Timeout = Timeout(100 milliseconds)

  "AccountManager" must {
    "return saved user when receive CreateUser" in {
      val accountManager = system.actorOf(AccountManager.props, "test-AccountManager-1")
      accountManager.tell(CreateUser("bob", 100), testActor)
      expectMsg(UserCreated(User("bob", 100)))
      accountManager.tell(GetUser("bob"), testActor)
      expectMsg(Some(User("bob", 100)))
    }

    "return two saved user when receive two different CreateUser" in {
      val accountManager = system.actorOf(AccountManager.props, "test-AccountManager-2")
      accountManager ! CreateUser("bob", 100)
      accountManager ! CreateUser("tom", 50)
      accountManager.tell(GetUsers, testActor)
      expectMsg(Users(Vector(User("bob", 100), User("tom", 50))))
    }

    "return exits message when receive two same CreateUser" in {
      val accountManager = system.actorOf(AccountManager.props, "test-AccountManager-3")
      accountManager ! CreateUser("bob", 100)
      accountManager.tell(CreateUser("bob", 50), testActor)
      expectMsg(UserExists)
    }

    "return None when don't send CreateUser" in {
      val accountManager = system.actorOf(AccountManager.props, "test-AccountManager-4")
      accountManager.tell(GetUser("bob"), testActor)
      expectMsg(None)
    }
  }
}
