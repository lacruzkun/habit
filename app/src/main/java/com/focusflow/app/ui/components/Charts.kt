package com.focusflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.focusflow.app.ui.theme.LocalFocusColors
import java.time.LocalDate
import kotlin.math.max

@Composable
fun FocusRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 14f,
    trackColor: Color = LocalFocusColors.current.grid,
    color: Color = MaterialTheme.colorScheme.primary,
    center: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(650),
        label = "focus-ring",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            color.copy(alpha = 0.65f),
                            color,
                            color.copy(alpha = 0.85f),
                        ),
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = stroke,
                )
            }
        }
        center()
    }
}

@Composable
fun MiniBarChart(
    values: List<Long>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = LocalFocusColors.current.grid,
) {
    val maxValue = max(1L, values.maxOrNull() ?: 1L)
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val gap = if (values.size <= 8) 12.dp.toPx() else size.width * 0.012f
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        val radius = minOf(barWidth * 0.32f, 10.dp.toPx())
        values.forEachIndexed { index, value ->
            val ratio = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            val height = max(6f, size.height * ratio)
            val left = index * (barWidth + gap)
            val top = size.height - height
            drawRoundRect(
                brush = if (value > 0) {
                    Brush.verticalGradient(
                        colors = listOf(barColor.copy(alpha = 0.65f), barColor),
                        startY = top,
                        endY = size.height,
                    )
                } else {
                    Brush.verticalGradient(listOf(inactiveColor, inactiveColor))
                },
                topLeft = Offset(left, top),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}

@Composable
fun PomodoroDots(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = LocalFocusColors.current.grid,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total.coerceIn(1, 12)) { index ->
            val active = index < completed
            Box(
                modifier = Modifier
                    .size(if (active) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (active) activeColor else inactiveColor),
            )
        }
    }
}

@Composable
fun HabitHeatmap(
    completedDays: Set<Long>,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
    weeks: Int = 14,
    activeColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val colors = LocalFocusColors.current
    val end = endDate
    val start = end.minusWeeks((weeks - 1).toLong())
        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    Canvas(modifier = modifier) {
        val columns = weeks
        val rows = 7
        val cellGap = 3.dp.toPx()
        val cellWidth = (size.width - cellGap * (columns - 1)) / columns
        val cellHeight = (size.height - cellGap * (rows - 1)) / rows
        val cell = minOf(cellWidth, cellHeight)
        for (column in 0 until columns) {
            for (row in 0 until rows) {
                val date = start.plusDays((column * 7 + row).toLong())
                if (date.isAfter(end)) continue
                val active = date.toEpochDay() in completedDays
                drawRoundRect(
                    color = if (active) activeColor else colors.grid.copy(alpha = 0.55f),
                    topLeft = Offset(
                        x = column * (cell + cellGap),
                        y = row * (cell + cellGap),
                    ),
                    size = Size(cell, cell),
                    cornerRadius = CornerRadius(cell * 0.28f, cell * 0.28f),
                )
            }
        }
    }
}

@Composable
fun ChartLabels(
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LocalFocusColors.current.muted,
            )
        }
    }
}

@Composable
fun WeekBars(
    values: List<Long>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    highlightIndex: Int = -1,
) {
    val colors = LocalFocusColors.current
    val maxValue = max(1L, values.maxOrNull() ?: 1L)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(
                            (110.dp * (value.toFloat() / maxValue.toFloat()))
                                .coerceAtLeast(6.dp),
                        )
                        .clip(CircleShape)
                        .background(
                            when {
                                index == highlightIndex -> MaterialTheme.colorScheme.primary
                                value == 0L -> colors.grid
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            },
                        ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted,
                )
            }
        }
    }
}

/** Smooth area line chart with dots and an optional tooltip bubble on one point. */
@Composable
fun LineChart(
    values: List<Long>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    highlightIndex: Int = -1,
    tooltip: String? = null,
) {
    val colors = LocalFocusColors.current
    val maxValue = max(1L, values.maxOrNull() ?: 1L).toFloat()
    BoxWithConstraints(modifier = modifier) {
        val chartWidth = maxWidth
        val chartHeight = maxHeight
        val pad = 14.dp
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (values.isEmpty()) return@Canvas
            val padPx = pad.toPx()
            val usable = size.height - padPx * 2
            val points = values.indices.map { i ->
                val x = if (values.size <= 1) size.width / 2f else size.width * i / (values.size - 1)
                val y = padPx + usable * (1f - values[i] / maxValue)
                Offset(x, y)
            }
            repeat(3) { k ->
                val y = padPx + usable * k / 2f
                drawLine(
                    color = colors.grid,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val a = points[i - 1]
                    val b = points[i]
                    val cx = (a.x + b.x) / 2f
                    cubicTo(cx, a.y, cx, b.y, b.x, b.y)
                }
            }
            val fill = Path().apply {
                moveTo(points.first().x, size.height)
                lineTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val a = points[i - 1]
                    val b = points[i]
                    val cx = (a.x + b.x) / 2f
                    cubicTo(cx, a.y, cx, b.y, b.x, b.y)
                }
                lineTo(points.last().x, size.height)
                close()
            }
            drawPath(
                path = fill,
                brush = Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                ),
            )
            drawPath(
                path = line,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            points.forEachIndexed { i, p ->
                val r = if (i == highlightIndex) 6.dp.toPx() else 4.5.dp.toPx()
                drawCircle(Color.White, radius = r, center = p)
                drawCircle(lineColor, radius = r, center = p, style = Stroke(width = 2.5.dp.toPx()))
            }
        }
        if (tooltip != null && highlightIndex in values.indices) {
            val fx = if (values.size <= 1) 0.5f else highlightIndex.toFloat() / (values.size - 1)
            val xDp = chartWidth * fx
            val yDp = pad + (chartHeight - pad * 2) * (1f - values[highlightIndex] / maxValue)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.navy,
                modifier = Modifier.offset(
                    x = (xDp - 28.dp).coerceIn(0.dp, maxOf(0.dp, chartWidth - 60.dp)),
                    y = (yDp - 36.dp).coerceAtLeast(0.dp),
                ),
            ) {
                Text(
                    text = tooltip,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
