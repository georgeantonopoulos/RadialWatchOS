package com.example.myfirstwearapp.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import kotlin.math.PI
import kotlin.math.atan2
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as colorLerp
import kotlin.math.abs

@Composable
fun RadialSlider(
    modifier: Modifier = Modifier,
    percentage: Float,
    onPercentageChange: (Float) -> Unit
) {
    // Move MaterialTheme color calculation outside Canvas
    val backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = 0.3f)
    val centerColor = MaterialTheme.colors.background
    
    // Compute colors and gradient only when percentage changes
    val baseColor = remember(percentage) {
        colorLerp(Color.Red, Color.Green, percentage / 100f)
    }
    val darkColor = remember(baseColor) {
        baseColor.copy(
            red = baseColor.red * 0.6f,
            green = baseColor.green * 0.6f,
            blue = baseColor.blue * 0.6f,
            alpha = baseColor.alpha
        )
    }
    val delta = 0.1f
    val adjustedStops = remember(baseColor, darkColor, percentage) {
        // Normalize the percentage to [0,1] and adjust for starting at -90 degrees
        val normalizedPercentage = (percentage / 100f)
        val offset = (0.75f + normalizedPercentage).mod(1f)
        
        // Create more granular stops for smoother transition
        val stops = mutableListOf<Pair<Float, Color>>()
        
        // Add more intermediate stops with finer granularity
        for (i in -5..5) {  // Increased range from -3..3 to -5..5
            val pos = (offset + (i * delta/3)).mod(1f)  // Decreased step size from delta/2 to delta/3
            val intensity = 1f - (abs(i) * 0.15f).coerceIn(0f, 1f)  // Adjusted intensity curve
            stops.add(pos to colorLerp(darkColor, baseColor, intensity))
        }

        // Add wrapping stops for smooth transition at 0/360 degrees
        stops.addAll(stops.map { (pos, color) ->
            if (pos < 0f) 1f + pos to color
            else if (pos > 1f) pos - 1f to color
            else pos to color
        })

        stops.distinctBy { it.first }.sortedBy { it.first }
    }

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
        
        // Create the gradient here where we have access to density information
        val sweepGradient = Brush.sweepGradient(
            colorStops = adjustedStops.toTypedArray(),
            center = Offset(size.width / 2f, size.height / 2f)
        )

        // Use the pre-calculated background color
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            brush = sweepGradient,
            startAngle = -90f,
            sweepAngle = 360f * (percentage / 100),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Use the pre-calculated center color
        drawCircle(
            color = centerColor,
            radius = strokeWidth
        )
    }
}