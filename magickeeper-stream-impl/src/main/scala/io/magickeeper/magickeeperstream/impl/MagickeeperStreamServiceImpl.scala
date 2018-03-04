package io.magickeeper.magickeeperstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import io.magickeeper.magickeeperstream.api.MagickeeperStreamService
import io.magickeeper.magickeeper.api.MagickeeperService

import scala.concurrent.Future

/**
  * Implementation of the MagickeeperStreamService.
  */
class MagickeeperStreamServiceImpl(magickeeperService: MagickeeperService) extends MagickeeperStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(magickeeperService.hello(_).invoke()))
  }
}
