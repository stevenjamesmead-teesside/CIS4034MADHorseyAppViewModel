package com.stevenjamesmead.cis4034madhorseyappviewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest

/**
 * The purpose of the example is to demonstrate the
 * use of Volley to fetch data from a server,
 * making clearly visible the use of StringRequest
 * and ImageRequest.
 *
 * The architecture/structure is lacking however.
 * There is scope to refactor the code.
 *
 * 1. fetchHorseList and fetchImage differ only by the
 *    type of request the are using.
 */

/**
 * fetchHorseList
 *
 * Demonstrates using Volley to fetch an image
 * using a RequestQueue and StringRequest
 */
fun fetchHorseList(
    context: Context,
    queue: RequestQueue,
    queueTag: String,
    url: String,
    handler: (strings: List<String>) -> Unit = {}
) {
    val authz: String = getAuthz(context)

    // Build a request object
    val request = object : StringRequest(
        Request.Method.GET, url, Response.Listener {
                response -> handler(response.split("\n"))
            Log.d(TAG, "FETCH SUCCESS!")
            Toast.makeText(context, "Successfully fetched horse image list", Toast.LENGTH_SHORT).show()
        },
        Response.ErrorListener {
            Log.d(TAG, "FETCH FAILURE!")
            Log.e(TAG, it.toString())
            Toast.makeText(context, "There was an error getting the resource $url", Toast.LENGTH_SHORT).show()
        })
    {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers.put("Authorization", "Basic $authz")
            return headers
        }
    }

    // Set the tag so that we can cancel
    // the request if required.
    request.tag = queueTag

    queue.add(request)
}

/**
 * fetchImage
 *
 * Demonstrates using Volley to fetch an image
 * using a RequestQueue and ImageRequest
 */
fun fetchImage(
    context: Context,
    queue: RequestQueue,
    queueTag: String,
    url: String,
    handler: (strings: Bitmap) -> Unit = {}
) {
    val authz: String = getAuthz(context)

    val request = object : ImageRequest(
        url,
        Response.Listener {
            Log.d(TAG, "FETCH SUCCESS - $url")
//            Toast.makeText(context, "Successfully fetched the horse image files", Toast.LENGTH_SHORT).show()
            handler(it)
        },
        400, 0, null,
        Bitmap.Config.ARGB_8888,
        Response.ErrorListener {
            Log.d(TAG, "FETCH FAILURE!")
            Log.e(TAG, it.toString())
            Toast.makeText(context, "There was an error getting the resource $url", Toast.LENGTH_SHORT).show()
        }
    ) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers.put("Authorization", "Basic $authz")
            return headers
        }
    }

    // Set the tag so that we can cancel
    // the request if required.
    request.tag = queueTag

    queue.add(request)
}

/**
 * getAuthz
 *
 * Reads the authentication string from the Android Manifest file.
 */
private fun getAuthz(context: Context) : String {
    Log.d(TAG, "API Version = ${android.os.Build.VERSION.SDK_INT}")

    val pm = context.applicationContext.packageManager
    val applicationInfo: ApplicationInfo =
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            //
            // For more recent versions of the Android SDK
            //
            pm.getApplicationInfo(context.packageName, PackageManager.ApplicationInfoFlags.of(0))
        }
        else {
            //
            // For older versions of the Android SDK
            //
            @Suppress("DEPRECATION")
            pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        }

    val authzData = applicationInfo.metaData.getString("authz")!!

    Log.d(TAG, "Authz = $authzData")

    return authzData
}