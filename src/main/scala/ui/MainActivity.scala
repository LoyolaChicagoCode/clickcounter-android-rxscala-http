package edu.luc.etl.cs313.scala.httpclickcounter
package ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.android.volley.toolbox.Volley

/**
 * The main Android activity, which provides the required lifecycle methods.
 * By mixing in the reactive view behaviors, this class serves as the Adapter
 * in the Model-View-Adapter pattern. It connects the Android GUI view with the
 * reactive model.
 */
class MainActivity extends Activity with TypedActivity with ObservableView with ViewUpdater {

  private def TAG = "clickcounter-android-rxscala-http"

  lazy val counter = new model.BoundedCounterHttpProxy(
    Volley.newRequestQueue(this),
    getString(R.string.counter_service_url),
    getString(R.string.counter_id)
  )

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    Log.i(TAG, "onCreate")
    // inject the (implicit) dependency on the view
    setContentView(R.layout.main)
    // connect everything in several steps using RxScala
    // connect
    this.subject.subscribe(counter)
    // connect the view updater directly to the model
    counter.observable.subscribe(this)
  }

  override def onDestroy() = {
    counter.onCompleted()
    Log.i(TAG, "onDestroy")
    super.onDestroy()
  }
}
