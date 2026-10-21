package com.example.meudinheiro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.meudinheiro.data.Transaction
import java.util.Calendar

@Composable
fun PieChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val total = data.sumOf { it.second }
    if (total <= 0.0) return
    Canvas(modifier) {
        val d = minOf(size.width, size.height)
        val topLeft = Offset((size.width - d) / 2f, (size.height - d) / 2f)
        var start = -90f
        data.forEachIndexed { i, item ->
            val sweep = (item.second / total * 360.0).toFloat()
            drawArc(
                color = ChartColors[i % ChartColors.size],
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = Size(d, d)
            )
            start += sweep
        }
    }
}

@Composable
fun PieLegend(data: List<Pair<String, Double>>) {
    val total = data.sumOf { it.second }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        data.forEachIndexed { i, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(ChartColors[i % ChartColors.size]))
                Spacer(Modifier.width(8.dp))
                Text(item.first, Modifier.weight(1f))
                val pct = if (total > 0) (item.second / total * 100).toInt() else 0
                Text("$pct%  •  ${money(item.second)}")
            }
        }
    }
}

private fun monthKey(ms: Long): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = ms
    return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
}

/** Barras de entradas x gastos dos últimos 6 meses. */
@Composable
fun MonthlyBars(list: List<Transaction>) {
    val names = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val keys = (5 downTo 0).map { back ->
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, -back)
        c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
    }
    val income = keys.map { k -> list.filter { it.type == "INCOME" && monthKey(it.date) == k }.sumOf { it.amount } }
    val expense = keys.map { k -> list.filter { it.type == "EXPENSE" && monthKey(it.date) == k }.sumOf { it.amount } }
    val maxV = maxOf(1.0, (income + expense).maxOrNull() ?: 1.0)

    Column {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val slot = size.width / keys.size
            val barW = slot * 0.32f
            keys.indices.forEach { i ->
                val hi = (income[i] / maxV).toFloat() * size.height
                val he = (expense[i] / maxV).toFloat() * size.height
                drawRect(Green, Offset(i * slot + slot * 0.12f, size.height - hi), Size(barW, hi))
                drawRect(Red, Offset(i * slot + slot * 0.12f + barW + 4f, size.height - he), Size(barW, he))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            keys.forEach { k ->
                Text(names[k % 12], Modifier.weight(1f), color = Color.Gray, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Green)); Spacer(Modifier.width(6.dp)); Text("Entradas")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Red)); Spacer(Modifier.width(6.dp)); Text("Gastos")
            }
        }
    }
}
