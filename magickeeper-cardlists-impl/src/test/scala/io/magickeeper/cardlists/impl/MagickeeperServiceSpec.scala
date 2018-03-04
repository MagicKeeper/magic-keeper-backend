package io.magickeeper.magickeeper.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import io.magickeeper.cardlists.api.CardlistsService
import io.magickeeper.cardlists.impl.CardlistApplication
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class MagickeeperServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new CardlistApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[CardlistsService]

  override protected def afterAll() = server.stop()

  "CardList service" should {
    /*
        "say hello" in {
          client.hello("Alice").invoke().map { answer =>
            answer should ===("Hello, Alice!")
          }
        }

        "allow responding with a custom message" in {
          for {
            _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
            answer <- client.hello("Bob").invoke()
          } yield {
            answer should ===("Hi, Bob!")
          }
        }*/
  }
}
