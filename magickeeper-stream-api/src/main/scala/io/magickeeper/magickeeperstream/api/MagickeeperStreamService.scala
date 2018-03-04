package io.magickeeper.magickeeperstream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The MagicKeeper stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the MagickeeperStream service.
  */
trait MagickeeperStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor = {
    import Service._

    named("magickeeper-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

