package com.example.marblegame

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setGravitySensor
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val gravityFlow = getGravityData(gravitySensor, sensorManager)
        setContent {
            MarbleGame(gravityFlow)
        }
    }

    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun MarbleGame(gravityFlow: Flow<GravityReading>) {
        // small ball location state

        var ballX by remember { mutableStateOf(0f) }
        var ballY by remember { mutableStateOf(0f) }

        // TO get the screen size
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val maxWidth = constraints.maxWidth
            val maxHeight = constraints.maxHeight
            // ball size
            val ballSizePx = with(LocalDensity.current) { 50.dp.toPx() }
          //To get gravity data from sensor to move the ball
            LaunchedEffect(key1 = gravityFlow) {
                // initial location of ball
                ballX = (maxWidth / 2) - (ballSizePx / 2)
                ballY = (maxHeight / 2) - (ballSizePx / 2)
                gravityFlow.collect { gravity ->
                    // speed of ball
                    val speedFactor = 3
                    // lacation of ball
                    ballX += gravity.x * speedFactor
                    ballY += gravity.y * speedFactor

                    // bounder check for ball
                    ballX = ballX.coerceIn(0f, maxWidth.toFloat() - ballSizePx)
                    ballY = ballY.coerceIn(0f, maxHeight.toFloat() - ballSizePx)
                }
            }

            // draw the ball
            Box(
                modifier = Modifier
                    .offset(
                        x = with(LocalDensity.current) { ballX.toDp() },
                        y = with(LocalDensity.current) { ballY.toDp() }
                    )
                    .background(Color.Magenta, shape = CircleShape)
                    .size(50.dp)
            )
        }
    }


    // data class to save gravity sensor data
    data class GravityReading(val x: Float, val y: Float, val z: Float)

    // To collect gravity sensor data
    fun getGravityData(gravitySensor: Sensor?, sensorManager: SensorManager): Flow<GravityReading> {
        return channelFlow {

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) { channel.trySend(GravityReading(
                                event.values[0],
                                event.values[1],
                                event.values[2]
                            )
                        )
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                }
            }


            sensorManager.registerListener(
                listener,
                gravitySensor,
                SensorManager.SENSOR_DELAY_GAME
            )

            awaitClose {

                sensorManager.unregisterListener(listener)
            }
        }
    }
}
