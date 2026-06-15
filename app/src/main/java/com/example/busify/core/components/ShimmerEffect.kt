package com.example.busify.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    width: Dp = 0.dp,
    height: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(
        modifier = modifier
            .then(if (width > 0.dp) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun ShimmerBusCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ShimmerEffect(height = 24.dp, width = 180.dp)
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerEffect(height = 16.dp, width = 120.dp)
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerEffect(height = 1.dp, width = 0.dp)
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerEffect(height = 16.dp, width = 140.dp)
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerEffect(height = 16.dp, width = 100.dp)
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerEffect(height = 16.dp, width = 160.dp)
    }
}

@Composable
fun ShimmerCircle(size: Dp = 48.dp) {
    ShimmerEffect(
        modifier = Modifier.width(size).height(size),
        height = size,
        width = size,
        shape = CircleShape
    )
}

@Composable
fun ShimmerProfileHeader() {
    Column {
        ShimmerCircle(size = 80.dp)
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerEffect(height = 24.dp, width = 160.dp)
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerEffect(height = 16.dp, width = 200.dp)
    }
}
