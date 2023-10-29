package com.stevenjamesmead.cis4034madhorseyappviewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.volley.toolbox.ImageRequest

class HorseyViewModel(
    private val authz: String,
    private val requestQueue: RequestQueue) : ViewModel() {

    // TODO: Need to replace this with Flow to emit
    //       horse images as they're created.

    private val _state = mutableStateOf(mutableListOf<HorseImage>())

    val uiState: MutableState<MutableList<HorseImage>>
        get() = _state

    init {
        fetchHorseList("horse_images.txt")
    }

    fun toggleLike(horseImage: HorseImage) {
        horseImage.liked = !horseImage.liked
    }

    private fun fetchHorseList(filename: String) {

        val horseImages = mutableListOf<HorseImage>()

        viewModelScope.launch {

            // Build a request object
            val request = object : StringRequest(
                Request.Method.GET, "$BASE_URL/$filename", Response.Listener {
                        response ->
                            response
                                .split("\n")
                                .forEach {
                                    horseInfo ->
                                        if(horseInfo.isNotEmpty()) {
                                            val (fileName, description) = horseInfo.split(",")

                                            fetchImage(fileName, {
                                                val horseImage = HorseImage(fileName, description, it)

                                                Log.d(TAG, "HorseImage - $horseImage")

                                                horseImages.add(horseImage)

                                            })
                                        }
                                }

                    uiState.value = horseImages


                    Log.d(TAG, "FETCH SUCCESS!")
                },
                Response.ErrorListener {
                    Log.d(TAG, "FETCH FAILURE!")
                    Log.e(TAG, it.toString())
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
            request.tag = REQUEST_TAG

            requestQueue.add(request)
        }
    }

    private fun fetchImage(
        imageFileName: String,
        handler: (ImageBitmap) -> Unit = {}) {

        viewModelScope.launch {
            val url = "$BASE_URL/$imageFileName"

            val request = object : ImageRequest(
                url,
                Response.Listener {
                    Log.d(TAG, "FETCH SUCCESS - $url")
                    handler(it.asImageBitmap())
                    it.asImageBitmap()
                },
                400, 0, null,
                Bitmap.Config.ARGB_8888,
                Response.ErrorListener {
                    Log.d(TAG, "FETCH FAILURE!")
                    Log.e(TAG, it.toString())
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers.put("Authorization", "Basic $authz")
                    return headers
                }
            }

            requestQueue.add(request)
        }
    }

    companion object {
        val REQUEST_TAG = "VIEWMODEL_AND_VOLLEY_DEMO"
        val BASE_URL = "https://scedt-intranet.tees.ac.uk/users/u0012604/CIS4034-N/horse_images"

        val Factory: ViewModelProvider.Factory = viewModelFactory  {
            initializer {
                val app: MyApplication = this[APPLICATION_KEY] as MyApplication
                val savedStateHandle = createSavedStateHandle()
                val authz = app.authz
                val rq = app.requestQueue
                HorseyViewModel(authz, rq)
            }
        }
    }
}