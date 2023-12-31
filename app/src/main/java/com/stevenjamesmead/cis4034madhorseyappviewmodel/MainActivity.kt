package com.stevenjamesmead.cis4034madhorseyappviewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.stevenjamesmead.cis4034madhorseyappviewmodel.ui.theme.CIS4034MADHorseyAppViewModelTheme

val TAG = "MainActivity"

fun String.trailOff(maxChars: Int): String {
    val chars = maxChars.coerceAtMost(this.length)

    val trailOff = this.substring(0, chars) +
            if(chars < this.length)
                "..."
            else
                ""

    return trailOff
}

class MainActivity : ComponentActivity() {

    // Volley RequeueQueue object that will be used to send out requests.
    // Note the use of 'lateinit'.  This can't be given a value until
    // Android is up and running and the onCreate method is called.
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Now we are able to set the requestQueue
        requestQueue = Volley.newRequestQueue(applicationContext)

        setContent {
            CIS4034MADHorseyAppViewModelTheme {
                val context = LocalContext.current
                var horseImages = remember { mutableStateListOf<HorseImage>() }

                fetchHorseList(
                    context,
                    requestQueue,
                    TAG,
                    "${MainActivity.BASE_URL}/horse_images.txt"
                ) {
                    it.forEach {
                        horseInfo ->

                        if(horseInfo.isNotEmpty()) {
                            val (fileName, description) = horseInfo.split(",")

                            Log.d(TAG, fileName + " --- " + description)
                            fetchImage(
                                context,
                                requestQueue,
                                TAG,
                                "${MainActivity.BASE_URL}/$fileName"
                            ) {
                                bitmap ->
                                Log.d(TAG, "Loaded image")
                                horseImages.add(HorseImage(fileName, description, bitmap.asImageBitmap()))
                            }
                        }
                    }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen(
                        horseImages,
                        {
                            //
                            // Force an update on the list that will instigate recomposition
                            //
                            val index = horseImages.indexOf(it)
                            horseImages[index] = horseImages[index].copy(liked = !it.liked)
                        }
                    )
                }
            }
        }
    }

    // onPause will cancel any pending Volley requests
    override fun onPause() {
        super.onPause()

        requestQueue.cancelAll(TAG)
    }

    companion object {
        val BASE_URL = "https://scedt-intranet.tees.ac.uk/users/u0012604/CIS4034-N/horse_images"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    horseImages: List<HorseImage>,
    onLike: (it: HorseImage) -> Unit) {

    Column(modifier = Modifier.fillMaxSize()) {
        if(horseImages.isNotEmpty()) {
            horseImages.forEach {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                    ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            it.image,
                            contentDescription = it.description,
                            modifier = Modifier.fillMaxWidth(0.45f)
                        )

                        Text(text = it.description.trailOff(30), modifier = Modifier.fillMaxWidth(0.45f))

                        ConstraintLayout(modifier = Modifier.size(100.dp)) {
                            val like1 = createRef()

                            LikeButton(it, onLike = onLike, modifier = Modifier.constrainAs(like1) {
                                top.linkTo(parent.top, margin = 0.dp)
                                bottom.linkTo(parent.bottom, margin = 0.dp)
                                absoluteRight.linkTo(parent.absoluteRight, margin = 0.dp)
                            })
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun LikeButton(
    horseImage: HorseImage,
    onLike: (it: HorseImage) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val imageRes =  if(horseImage.liked) painterResource(id = R.drawable.baseline_favorite_48)
                    else painterResource(id = R.drawable.baseline_favorite_border_48)

    IconButton(
        onClick = { onLike(horseImage) },
        modifier = modifier) {
        Icon(
            painter = imageRes,
            contentDescription = null,
            modifier = Modifier.width(50.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    val context = LocalContext.current

    var horseImages = remember { mutableStateListOf<HorseImage>() }

    horseImages.addAll(listOfPreviewHorses(context))

    CIS4034MADHorseyAppViewModelTheme {
        AppScreen(horseImages, onLike = {})
    }
}

private fun listOfPreviewHorses(context: Context): List<HorseImage> {
    // Horse image from heres:
    // - https://pngfre.com/horse-png/horse-png-from-pngfre-6/
    // - https://pngfre.com/horse-png/horse-png-from-pngfre-20/
    // - https://pngfre.com/horse-png/horse-png-from-pngfre-12/

   return listOf(   R.drawable.horse_png_from_pngfre_6_scaled,
                    R.drawable.horse_png_from_pngfre_20_scaled,
                    R.drawable.horse_png_from_pngfre_12_scaled)
            .map {
                val bmp = BitmapFactory.decodeResource(context.resources, it)

                HorseImage("From Resources", "Preview Cartoon Horse", bmp.asImageBitmap())
            }
}
