package com.example.myfirstwearapp.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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

        // Base color blend based on current percentage
        val baseColor = lerp(Color.Red, Color.Green, percentage / 100f)
        val darkColor = baseColor.copy(
            red = baseColor.red * 0.6f,
            green = baseColor.green * 0.6f,
            blue = baseColor.blue * 0.6f,
            alpha = baseColor.alpha
        )

        // Small delta for narrow bright band
        val delta = 0.02f

        // The fraction of the circle corresponding to the tip of the arc
        // 0.75f corresponds to 12 o’clock for a sweep gradient in Compose.
        val offset = (0.75f + (percentage / 100f)).mod(1f)

        // Color stops placing the brightest color at 'offset'
        val adjustedStops = listOf(
            (offset - delta).mod(1f) to darkColor,
            offset to baseColor,
            (offset + delta).mod(1f) to darkColor
        ).sortedBy { it.first }

        // Create the sweep gradient centered on the Canvas
        val sweepGradient = Brush.sweepGradient(
            colorStops = adjustedStops.toTypedArray(),
            center = Offset(size.width / 2f, size.height / 2f)
        )

        // Draw a background arc
        drawArc(
            color = onBackgroundColor.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw the progress arc with the sweep gradient
        drawArc(
            brush = sweepGradient,
            startAngle = -90f,
            sweepAngle = 360f * (percentage / 100),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw a center circle
        drawCircle(
            color = backgroundColor,
            radius = strokeWidth
        )
    }
}