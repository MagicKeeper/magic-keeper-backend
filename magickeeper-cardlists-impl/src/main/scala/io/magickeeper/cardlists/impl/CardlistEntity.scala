package io.magickeeper.cardlists.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import io.magickeeper.cardlists.api.CardList
import play.api.libs.json._

import scala.collection.immutable.Seq

class CardlistEntity extends PersistentEntity {
  override type Command = CardlistCommand[_]
  override type Event = CardlistEvent
  override type State = CardlistState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: CardlistState = CardlistState(LocalDateTime.now.toString, Map())

  /**
    * An entity can define different behaviours for different states, so the behaviour
    * is a function of the current state to a set of actions.
    */
  override def behavior: Behavior = {
    case CardlistState(_, map) => Actions().onCommand[ChangeListCommand, Done] {
      case (ChangeListCommand(change), ctx, state) =>
        val ch = change.withDefaultValue((0, 0))

        if (state.cards.map {
          case (key, (amt, amtFoil)) =>
            val diff = ch(key)
            key -> (amt + diff._1, amtFoil + diff._2)
        } ++ change.filterNot { case (key, _) => state.cards.contains(key) } exists { case (_, (a1, a2)) => a1 < 0 || a2 < 0 }) {
          ctx.invalidCommand("A card amount cannot end up being negative!")
          ctx.done
        } else ctx.thenPersist(
          CardlistChanged(change)
        ) { _ =>
          // Then once the event is successfully persisted, we respond with done.
          ctx.reply(Done)
        }
    }.onReadOnlyCommand[GetListCommand.type, CardList] {
      case (GetListCommand, ctx, state) =>
        ctx.reply(CardList(state.cards))
    }.onEvent {
      case (CardlistChanged(change), state) =>
        // We simply update the current state with the change from the event
        val ch = change.withDefaultValue((0, 0))

        CardlistState(LocalDateTime.now().toString, state.cards.map {
          case (key, (amt, amtFoil)) =>
            val diff = ch(key)
            key -> (amt + diff._1, amtFoil + diff._2)
        } ++ change.filterNot { case (key, _) => state.cards.contains(key) } // add new cards
          filterNot { case (_, (v1, v2)) => v1 == 0 && v2 == 0 }) // remove cards with 0 cards
    }
  }
}


case class CardlistState(timestamp: String, cards: Map[String, (Int, Int)])

object CardlistState {
  implicit val format: Format[CardlistState] = Json.format[CardlistState]
}

/**
  * This interface defines all the events that the MagickeeperEntity supports.
  */
sealed trait CardlistEvent extends AggregateEvent[CardlistEvent] {
  def aggregateTag = CardlistEvent.Tag
}

object CardlistEvent {
  val Tag = AggregateEventTag[CardlistEvent]
}

case class CardlistChanged(change: Map[String, (Int, Int)]) extends CardlistEvent

object CardlistChanged {
  implicit val format: Format[CardlistChanged] = Json.format[CardlistChanged]
}

/**
  * This interface defines all the commands that the entity supports.
  */
sealed trait CardlistCommand[R] extends ReplyType[R]

case class ChangeListCommand(change: Map[String, (Int, Int)]) extends CardlistCommand[Done]

object ChangeListCommand {
  implicit val format: Format[ChangeListCommand] = Json.format[ChangeListCommand]
}

case object GetListCommand extends CardlistCommand[CardList]

object CardlistsSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[ChangeListCommand],
    JsonSerializer[CardlistChanged],
    JsonSerializer[CardlistState],
    JsonSerializer(JsonSerializer.emptySingletonFormat(GetListCommand)),
    JsonSerializer[CardList]
  )
}
