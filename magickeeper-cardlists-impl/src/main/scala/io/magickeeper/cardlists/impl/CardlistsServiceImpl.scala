package io.magickeeper.cardlists.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import io.magickeeper.cardlists.api.{CardList, CardListChanged, CardlistsService}

/**
  * Implementation of the MagickeeperService.
  */
class CardlistsServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends CardlistsService {
  /**
    * Updates a list of cards using a list of cards changes.
    *
    * @param id the id of the cardlist to update
    */
  override def updateList(id: String): ServiceCall[Map[String, (Int, Int)], Done] = ServiceCall { request =>
    // Look up the CardlistEntity entity for the given ID.
    val ref = persistentEntityRegistry.refFor[CardlistEntity](id)

    // Submit the list change to the entity
    ref.ask(ChangeListCommand(request))
  }

  /**
    * Get a list of cards knowing its ID
    *
    * @param id the id of the cardlist to retrieve
    */
  override def getList(id: String): ServiceCall[NotUsed, CardList] = ServiceCall { _ =>
    // Look up the CardlistEntity entity for the given ID.
    val ref = persistentEntityRegistry.refFor[CardlistEntity](id)

    // Submit the request to the entity
    ref.ask(GetListCommand)
  }

  /**
    * This gets published to Kafka.
    */
  override def updateTopic(): Topic[CardListChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(CardlistEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(cardListEvent: EventStreamElement[CardlistEvent]): CardListChanged = {
    cardListEvent.event match {
      case CardlistChanged(msg) => CardListChanged(cardListEvent.entityId, msg)
    }
  }
}
