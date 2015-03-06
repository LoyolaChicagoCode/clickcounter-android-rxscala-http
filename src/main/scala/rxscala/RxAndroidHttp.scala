package edu.luc.etl.rx.android.http

/**
 * The beginning of a lightweight subset of RxApacheHttp for Android
 * based on HttpURLConnection. The only external dependency is RxScala.
 */

import java.net.{URL, HttpURLConnection}
import android.os.AsyncTask
import android.util.Log
import rx.lang.scala._
import scala.concurrent.future
import scala.concurrent.ExecutionContext


/** Opens a connection to a URL given as a string. */
object HttpURLConnection {

  def apply(url: String): HttpURLConnection =
    new URL(url).openConnection().asInstanceOf[HttpURLConnection]
}

/** An observer that posts an intent to a path on each event. */
class HttpPostObserver(resourceUrl: String) extends Observer[String] {

  private val TAG = "edu.luc.etl.rx.android.http.HttpPostObserver"

  /** Android-supplied EC for the futures. */
  implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

  override def onNext(path: String): Unit = {
    future {
      val urlConnection = HttpURLConnection(resourceUrl + path)
      urlConnection.setRequestMethod("POST")
      Log.d(TAG, "getting response status")
      val status = urlConnection.getResponseCode
      Log.d(TAG, "got response status " + status)
    }
  }
}

/** First attempt at an observable for an event source of server-sent events (SSE). */
object HttpEventSourceObservable {

  private val TAG = "edu.luc.etl.rx.android.http.HttpPostObserver"

  val DATA_PREFIX = "data:"

  val DATA_PREFIX_LENGTH = DATA_PREFIX.length

  /** Android-supplied EC for the futures. */
  implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

  def getObservable[T](resourceUrl: String, bufferSize: Int = 1024)(implicit um: String => Option[T]): Observable[T] = {
    val buffer = new Array[Byte](bufferSize)
    val subject = Subject[T]

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
              val event = input.substring(DATA_PREFIX_LENGTH + pos)
              um(event) match {
                case Some(value) => subject.onNext(value)
                case None => Log.d(TAG, "ignoring unparsable data event: " + event)
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

    subject
  }
}
