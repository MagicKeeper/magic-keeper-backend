package io.magickeeper.magickeeper.impl

import io.magickeeper.magickeeper.api
import io.magickeeper.magickeeper.api.{MagickeeperService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the MagickeeperService.
  */
class MagickeeperServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends MagickeeperService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the MagicKeeper entity for the given ID.
    val ref = persistentEntityRegistry.refFor[MagickeeperEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the MagicKeeper entity for the given ID.
    val ref = persistentEntityRegistry.refFor[MagickeeperEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(MagickeeperEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[MagickeeperEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
