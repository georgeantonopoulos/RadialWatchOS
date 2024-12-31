/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.myfirstwearapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.myfirstwearapp.presentation.theme.MyFirstWearAppTheme
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import android.view.MotionEvent
import android.view.InputDevice
import android.view.WindowManager

class MainActivity : ComponentActivity() {
    private var currentPercentage by mutableStateOf(50f)
    private var lastEventTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        setContent {
            WearApp(
                percentage = currentPercentage,
                onPercentageChange = { 
                    currentPercentage = it
                }
            )
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        return when {
            event?.action == MotionEvent.ACTION_SCROLL &&
            event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER) -> {
                val currentTime = System.currentTimeMillis()
                val timeDelta = currentTime - lastEventTime
                lastEventTime = currentTime

                val delta = -event.getAxisValue(MotionEvent.AXIS_SCROLL)
                
                // Calculate sensitivity based on rotation speed
                val sensitivity = when {
                    timeDelta < 30 -> 10.0f    // Very fast: 3x
                    timeDelta < 60 -> 5.0f    // Fast: 2x
                    timeDelta < 100 -> 2.0f   // Normal: 1x
                    timeDelta < 150 -> 1.0f   // Slow: 0.5x
                    else -> 0.25f             // Very slow: 0.25x
                }

                val adjustedDelta = delta * sensitivity
                currentPercentage = (currentPercentage + adjustedDelta).coerceIn(0f, 100f)
                true
            }
            else -> super.onGenericMotionEvent(event)
        }
    }
}

@Composable
fun WearApp(
    percentage: Float,
    onPercentageChange: (Float) -> Unit
) {
    MyFirstWearAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            RadialSlider(
                percentage = percentage,
                onPercentageChange = onPercentageChange,
                modifier = Modifier.size(200.dp)
            )
            Text(
                text = "${percentage.roundToInt()}%",
                style = MaterialTheme.typography.title1,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(
        percentage = 50f,
        onPercentageChange = {}
    )
}
