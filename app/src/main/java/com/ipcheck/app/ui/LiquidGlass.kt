package com.ipcheck.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ipcheck.app.ui.theme.BgBottom
import com.ipcheck.app.ui.theme.BgMid
import com.ipcheck.app.ui.theme.BgTop
import com.ipcheck.app.ui.theme.GlassFill
import com.ipcheck.app.ui.theme.GlassFillStrong
import com.ipcheck.app.ui.theme.GlassStroke
import com.ipcheck.app.ui.theme.IosBlue
import com.ipcheck.app.ui.theme.IosPink
import com.ipcheck.app.ui.theme.IosPurple

/**
 * 液态玻璃背景：深色渐变 + 缓慢漂浮的高饱和光晕（blur 模糊），营造 iOS 26 的流动玻璃底衬。
 */
@Composable
fun LiquidGlassBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgTop, BgMid, BgBottom))
            )
    ) {
        val transition = rememberInfiniteTransition(label = "blobs")
        val shift by transition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
            label = "shift"
        )

        // 三个漂浮光晕，位置随动画缓慢移动
        Blob(color = IosPurple, xFrac = 0.15f + shift * 0.1f, yFrac = 0.12f, size = 260.dp)
        Blob(color = IosBlue, xFrac = 0.7f - shift * 0.08f, yFrac = 0.30f + shift * 0.05f, size = 300.dp)
        Blob(color = IosPink, xFrac = 0.35f + shift * 0.06f, yFrac = 0.8f - shift * 0.05f, size = 240.dp)

        content()
    }
}

@Composable
private fun Blob(color: Color, xFrac: Float, yFrac: Float, size: Dp) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val x = maxWidth * xFrac
        val y = maxHeight * yFrac
        Box(
            Modifier
                .offset(x = x, y = y)
                .size(size)
                .blur(90.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(color.copy(alpha = 0.55f))
        )
    }
}

/**
 * 玻璃卡片：半透明填充 + 顶部高光渐变 + 细亮边 + 圆角。可用于所有内容容器。
 */
@Composable
fun glassSurface(
    cornerRadius: Dp = 28.dp,
    strong: Boolean = false
): Modifier {
    val fill = if (strong) GlassFillStrong else GlassFill
    val shape = RoundedCornerShape(cornerRadius)
    return Modifier
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    fill,
                    Color.White.copy(alpha = 0.06f)
                ),
                start = Offset(0f, 0f),
                end = Offset(0f, 600f)
            )
        )
        .border(1.dp, GlassStroke, shape)
}
