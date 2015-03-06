package edu.luc.etl.cs313.scala.httpclickcounter.model

import org.json.JSONObject
import rx.lang.scala._
import scala.util.Try
import edu.luc.etl.rx.android.http._

/** A semantic input event. */
trait InputEvent
case object Increment extends InputEvent
case object Decrement extends InputEvent
case object Reset extends InputEvent

/** An abstract model state. */
trait ModelState
case object Empty extends ModelState
case object Counting extends ModelState
case object Full extends ModelState

/** An HTTP-based proxy for a RESTful bounded counter service. */
class BoundedCounterHttpProxy(serviceUrl: String, counterId: String) extends Observer[InputEvent] {

  /** The observable through which this counter emits response events. */
  def observable: Observable[(Int, ModelState)] = eventSource

  /** The observable for the server-sent events. */
  lazy val eventSource =
    HttpEventSourceObservable.getObservable[(Int, ModelState)](
      serviceUrl + "/counters/" + counterId + "/stream")

  lazy val postObserver = new HttpPostObserver(serviceUrl + "/counters/" + counterId + "/")

  private def TAG = "clickcounter-android-rxscala-http" // FIXME centralize

  implicit def parseJsonCounter(response: String): Option[(Int, ModelState)] = Try {
    val jsonResponse = new JSONObject(response)
    val min = jsonResponse.getInt("min")
    val value = jsonResponse.getInt("value")
    val max = jsonResponse.getInt("max")
    val state = if (value <= min)
      Empty
    else if (value < max)
      Counting
    else
      Full
    (value, state)
  } toOption

  /**
   * Provides the reactive behavior of this bounded counter
   * by passing the event on to the remote service.
   * @param arg the current value along with an input event
   */
  override def onNext(arg: InputEvent) = {
    val resourceSuffix = arg match {
      case Increment => "increment"
      case Decrement => "decrement"
      case Reset     => "reset"
    }
    postObserver.onNext(resourceSuffix)
  }
}
