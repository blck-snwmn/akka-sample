package sample

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite => //self-type. can use only extends
  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }
}
