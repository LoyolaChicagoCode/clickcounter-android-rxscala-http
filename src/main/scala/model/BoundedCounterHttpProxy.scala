package edu.luc.etl.cs313.scala.httpclickcounter
package model

import java.net.{URL, HttpURLConnection}
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import rx.lang.scala._
import scala.concurrent.future
import scala.concurrent.ExecutionContext

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

  /** Android-supplied EC for the futures. */
  implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

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
  }

  def HttpURLConnection(url: String): HttpURLConnection =
    new URL(url).openConnection().asInstanceOf[HttpURLConnection]

  def onCreate(): Unit = {
    val resourceUrl = serviceUrl + "/counters/" + counterId + "/stream"
    val buffer = new Array[Byte](1024)
    val DATA_PREFIX = "data:"
    val DATA_PREFIX_LENGTH = DATA_PREFIX.length

    future {
      while (true) {
        var urlConnection: HttpURLConnection = null
        try {
          Log.d(TAG, "opening connection to " + resourceUrl)
          urlConnection = HttpURLConnection(resourceUrl)
          Log.d(TAG, "getting input stream")
          val is = urlConnection.getInputStream
          Log.d(TAG, "type of input stream is " + is.getClass.toString)
          while (true) {
            Log.d(TAG, "attempting to read")
            val bytesRead = is.read(buffer)
            val input = new String(buffer, 0, bytesRead)
            Log.d(TAG, "read " + bytesRead + " bytes: " + input)
            val pos = input.indexOf(DATA_PREFIX)
            if (pos >= 0) {
              try {
                val sub = input.substring(DATA_PREFIX_LENGTH + pos)
                Log.d(TAG, "extracting JSON from " + sub)
                val (value, state) = parseJsonCounter(sub)
                Log.d(TAG, "firing " +(value, state))
                subject.onNext((value, state))
                Log.d(TAG, "fired " +(value, state))
              } catch {
                case ex: Throwable =>
                  Log.d(TAG, "error during JSON extraction: " + ex)
              }
            } else {
              Log.d(TAG, "ignoring unknown message")
            }
          }
        } catch {
          case ex: Throwable =>
            Log.d(TAG, "disconnecting on error: " + ex)
            urlConnection.disconnect()
        }
      }
    }
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
    Log.d(TAG, "opening connection to " + resourceUrl)

    future {
      val urlConnection = HttpURLConnection(resourceUrl)
      urlConnection.setRequestMethod("POST")
      Log.d(TAG, "getting response status")
      val status = urlConnection.getResponseCode
      Log.d(TAG, "got response status " + status)
    }
  }
}
