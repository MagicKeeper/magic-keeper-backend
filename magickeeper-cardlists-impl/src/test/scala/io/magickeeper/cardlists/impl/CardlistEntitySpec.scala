package io.magickeeper.cardlists.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.{Issue, NotSerializable}
import io.magickeeper.cardlists.api.CardList
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class CardlistEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("CardlistEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(CardlistsSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[CardlistCommand[_], CardlistEvent, CardlistState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CardlistEntity, "cardlist-1")
    block(driver)
    driver.getAllIssues.foreach {
      case i: NotSerializable => i.cause.printStackTrace()
      case i: Issue => println(i)
    }
    driver.getAllIssues should have size 0
  }

  "Cardlist entity" should {
    "be empty by default" in withTestDriver { driver =>
      val outcome = driver.run(GetListCommand)
      outcome.replies should contain only CardList(Map.empty[String, (Int, Int)])
    }

    "allow adding cards" in withTestDriver { driver =>
      val outcome1 = driver.run(ChangeListCommand(Map("card-1" -> (5, 5))))
      outcome1.events should contain only CardlistChanged(Map("card-1" -> (5, 5)))
      val outcome2 = driver.run(GetListCommand)
      outcome2.replies should contain only CardList(Map("card-1" -> (5, 5)))
      val outcome3 = driver.run(ChangeListCommand(Map("card-1" -> (5, 5))))
      outcome3.events should contain only CardlistChanged(Map("card-1" -> (5, 5)))
      val outcome4 = driver.run(GetListCommand)
      outcome4.replies should contain only CardList(Map("card-1" -> (10, 10)))
    }

    "allow removing cards" in withTestDriver( {
      driver =>
        val outcome1 = driver.run(ChangeListCommand(Map("card-1" -> (5, 5))))
        outcome1.events should contain only CardlistChanged(Map("card-1" -> (5, 5)))
        val outcome2 = driver.run(GetListCommand)
        outcome2.replies should contain only CardList(Map("card-1" -> (5, 5)))
        val outcome3 = driver.run(ChangeListCommand(Map("card-1" -> (-5, -5))))
        outcome3.events should contain only CardlistChanged(Map("card-1" -> (-5, -5)))
        val outcome4 = driver.run(GetListCommand)
        outcome4.replies should contain only CardList(Map())
    })

  }
}
