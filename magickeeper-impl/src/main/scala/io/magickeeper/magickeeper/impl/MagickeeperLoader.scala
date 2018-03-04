package io.magickeeper.magickeeper.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import io.magickeeper.magickeeper.api.MagickeeperService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class MagickeeperLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MagickeeperApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MagickeeperApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[MagickeeperService])
}

abstract class MagickeeperApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[MagickeeperService](wire[MagickeeperServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = MagickeeperSerializerRegistry

  // Register the MagicKeeper persistent entity
  persistentEntityRegistry.register(wire[MagickeeperEntity])
}
