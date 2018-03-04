package io.magickeeper.cardlists.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import io.magickeeper.cardlists.api.{CardList, CardlistsService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class CardlistServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new CardlistApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[CardlistsService]

  override protected def afterAll() = server.stop()

  "CardList service" should {
    "return an empty cardlist by default" in {
      client.getList("list-1").invoke().map(ans => {
        ans shouldBe CardList(Map())
      })
    }

    "allow adding cards" in {
      for {
        _ <- client.updateList("list-1").invoke(Map("card-1" -> (1, 1)))
        answer <- client.getList("list-1").invoke()
        answer2 <- client.getList("list-2").invoke()
      } yield {
        answer shouldBe CardList(Map("card-1" -> (1, 1)))
        answer2 shouldBe CardList(Map())
      }
    }

    "allow updating cards" in {
      for {
        _ <- client.updateList("list-2").invoke(Map("card-1" -> (1, 1)))
        answer <- client.getList("list-2").invoke()
        _ <- client.updateList("list-2").invoke(Map("card-1" -> (-1, 1)))
        answer2 <- client.getList("list-2").invoke()
        _ <- client.updateList("list-2").invoke(Map("card-1" -> (0, -2), "card-2" -> (2, 0)))
        answer3 <- client.getList("list-2").invoke()
      } yield {
        answer shouldBe CardList(Map("card-1" -> (1, 1)))
        answer2 shouldBe CardList(Map("card-1" -> (0, 2)))
        answer3 shouldBe CardList(Map("card-2" -> (2, 0)))
      }
    }
  }
}
