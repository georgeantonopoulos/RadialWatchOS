package com.example.myfirstwearapp.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.input.rotary.RotaryScrollEvent
import androidx.compose.ui.graphics.Brush

@Composable
fun RadialSlider(
    modifier: Modifier = Modifier,
    percentage: Float,
    onPercentageChange: (Float) -> Unit
) {
    val backgroundColor = MaterialTheme.colors.background
    val onBackgroundColor = MaterialTheme.colors.onBackground

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        val touchPoint = change.position
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = touchPoint.x - center.x
                        val dy = touchPoint.y - center.y

                        val angle = (atan2(dy, dx) * (180f / PI)).toFloat()
                        var newPercentage = ((angle + 90f) / 360f) * 100f
                        if (newPercentage < 0) {
                            newPercentage += 100f
                        }

                        newPercentage = newPercentage.coerceIn(0f, 100f)
                        onPercentageChange(newPercentage)
                    }
                )
            }
            .then(modifier)
    ) {
        val strokeWidth = 20f

        // Calculate the dynamicArcColor within the Canvas draw scope
        val dynamicArcColor = lerp(Color.Red, Color.Green, percentage / 100f)
        val darkColor = dynamicArcColor.copy(
            red = dynamicArcColor.red * 0.6f,
            green = dynamicArcColor.green * 0.6f,
            blue = dynamicArcColor.blue * 0.6f,
            alpha = dynamicArcColor.alpha
        )
        
        // Define a small delta for the bright spot
        val delta = 0.02f // Adjust as needed for sharpness

        // Calculate the exact position for the bright spot
        val offset = percentage / 100f

        // Define color stops with the bright spot centered at the offset
        val adjustedStops = listOf(
            (offset - delta).mod(1f) to darkColor,
            offset to dynamicArcColor,
            (offset + delta).mod(1f) to darkColor
        ).sortedBy { it.first }

        // Create the sweep gradient with the correct center
        val sweepGradient = Brush.sweepGradient(
            colorStops = adjustedStops.toTypedArray(),
            center = Offset(size.width / 2f, size.height / 2f)
        )

        // Draw background circle
        drawArc(
            color = onBackgroundColor.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw progress arc with the adjusted sweepGradient
        drawArc(
            brush = sweepGradient,
            startAngle = -90f,
            sweepAngle = 360f * (percentage / 100),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Center circle
        drawCircle(
            color = backgroundColor,
            radius = strokeWidth
        )
    }
}