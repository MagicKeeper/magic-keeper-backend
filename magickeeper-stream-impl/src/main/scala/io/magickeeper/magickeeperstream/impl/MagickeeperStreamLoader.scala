package io.magickeeper.magickeeperstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import io.magickeeper.magickeeperstream.api.MagickeeperStreamService
import io.magickeeper.magickeeper.api.MagickeeperService
import com.softwaremill.macwire._

class MagickeeperStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MagickeeperStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MagickeeperStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[MagickeeperStreamService])
}

abstract class MagickeeperStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[MagickeeperStreamService](wire[MagickeeperStreamServiceImpl])

  // Bind the MagickeeperService client
  lazy val magickeeperService = serviceClient.implement[MagickeeperService]
}
