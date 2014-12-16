package edu.luc.etl.volley

import com.android.volley.{Request, Response, VolleyError}

/** The beginning of a Scala wrapper for Volley. */
class StringRequest(method: Int, url: String, onSuccess: (String) => Unit, onFailure: (VolleyError) => Unit)
  extends com.android.volley.toolbox.StringRequest(
    method,
    url,
    new Response.Listener[String] {
      override def onResponse(response: String) = onSuccess(response)
    }, new Response.ErrorListener {
      override def onErrorResponse(error: VolleyError) = onFailure(error)
    }
  ) {
  def this(url: String, onResponse: (String) => Unit, onErrorResponse: (VolleyError) => Unit) =
    this(Request.Method.GET, url, onResponse, onErrorResponse)
}
