package edu.luc.etl.cs313.scala.httpclickcounter
package model

import android.util.Log
import com.android.volley.{VolleyError, Response, Request, RequestQueue}
import edu.luc.etl.volley.StringRequest
import org.json.JSONObject
import rx.lang.scala._
import rx.lang.scala.ImplicitFunctionConversions._

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
class BoundedCounterHttpProxy(requestQueue: RequestQueue, serviceUrl: String, counterId: String) extends Observer[InputEvent] {

  /** The internal subject for emitting response events. */
  private lazy val subject = Subject[(Int, ModelState)]

  /**
   * The observable through which this counter emits response events.
   * @return the observable
   */
  def observable: Observable[(Int, ModelState)] = subject

  private def TAG = "clickcounter-android-rxscala-http" // FIXME centralize

  onCreate()

  def parseJsonCounter(response: String): (Int, ModelState) = {
    val jsonResponse = new JSONObject(response)
    val value = jsonResponse.getInt("value")
    val state = jsonResponse.getString("state") match {
      case "empty" => Empty
      case "counting" => Counting
      case "full" => Full
    }
    (value, state)
  }

  def onCreate(): Unit = {
    val resourceUrl = serviceUrl + "/counters/" + counterId
    Log.d(TAG, resourceUrl)

    val request = new StringRequest(resourceUrl,
      (response: String) => subject.onNext(parseJsonCounter(response)),
      (error: VolleyError) => throw new RuntimeException(error.getCause)
    )
    requestQueue.add(request)
  }

  /**
   * Provides the reactive behavior of this bounded counter.
   * @param arg the current value along with an input event
   */
  override def onNext(arg: InputEvent) = {
    val resourceSuffix = arg match {
      case Increment => "increment"
      case Decrement => "decrement"
      case Reset     => "reset"
    }

    val resourceUrl = serviceUrl + "/counters/" + counterId + "/" + resourceSuffix
    Log.d(TAG, resourceUrl)

    val request = new StringRequest(Request.Method.POST, resourceUrl,
      (response: String) => subject.onNext(parseJsonCounter(response)),
      (error: VolleyError) => subject.onNext(parseJsonCounter(new String(error.networkResponse.data)))
    )
    requestQueue.add(request)
  }
}
