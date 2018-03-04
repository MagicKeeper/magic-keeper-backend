package io.magickeeper.cardlists.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

object CardlistsService  {
  val TOPIC_NAME = "cardlistChange"
}

/**
  * The CardLists service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the CardlistsService.
  */
trait CardlistsService extends Service {

  /**
    * Updates a list of cards using a list of cards changes.
    * @param id the id of the cardlist to update
    */
  def updateList(id: String): ServiceCall[Map[String, (Int, Int)], Done]

  /**
    * Get a list of cards knowing its ID
    * @param id the id of the cardlist to retrieve
    */
  def getList(id: String): ServiceCall[NotUsed, CardList]

  /**
    * This gets published to Kafka.
    */
  def updateTopic(): Topic[CardListChanged]

  override final def descriptor = {
    import Service._
    named("cardlists")
      .withCalls(
        pathCall("/list/:id", getList _),
        pathCall("/list/:id", updateList _)
      )
      .withTopics(
        topic(CardlistsService.TOPIC_NAME, updateTopic())
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[CardListChanged](_.id) // partition messages using the unique collection ID
          )
      )
      .withAutoAcl(true)
  }
}

/**
  *
  * @param id the id of the cardlist to update
  * @param change the cards changed (id of the card -> (change, change foil))
  */
case class CardListChanged(id: String, change: Map[String, (Int, Int)])

object CardListChanged {
  implicit val format: Format[CardListChanged] = Json.format[CardListChanged]
}

/**
  * This is more or less a workaround because Lagom doesn't want to use my map format if the map is a result type
  * @param map the map in the cardlist
  */
case class CardList(map: Map[String, (Int, Int)])

object CardList {
  implicit val mapReads: Reads[Map[String, (Int, Int)]] = {
    case obj: JsObject =>
      JsSuccess[Map[String, (Int, Int)]](obj.value.mapValues {
        case arr: JsArray => (arr(0).as[Int], arr(1).as[Int])
      }.toMap)
    case _ => JsError()
  }

  implicit val mapWrites: Writes[Map[String, (Int, Int)]] = (map: Map[String, (Int, Int)]) => Json.obj(map.map{
    case (k, (v1, v2)) =>
      val ret: (String, JsValueWrapper) = k -> Json.toJson(List(v1, v2))
      ret
  }.toSeq:_*)

  implicit val mapFormat: Format[Map[String, (Int, Int)]] = Format(mapReads, mapWrites)

  implicit val format: Format[CardList] = Json.format[CardList]
}