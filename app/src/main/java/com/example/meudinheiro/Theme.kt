package com.example.meudinheiro

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

internal val Bg = Color(0xFFF7F8FA)
internal val Green = Color(0xFF16855B)
internal val Red = Color(0xFFC83C4A)
internal val Blue = Color(0xFF356AE6)
internal val Amber = Color(0xFFB7791F)
internal val TextDark = Color(0xFF1E2430)

internal val ChartColors = listOf(
    Color(0xFF356AE6), Color(0xFF16855B), Color(0xFFC83C4A), Color(0xFFB7791F),
    Color(0xFF8E44AD), Color(0xFF17A2B8), Color(0xFFE67E22), Color(0xFF7F8C8D)
)

internal val PtBr = Locale("pt", "BR")
internal const val DAY_MS = 24L * 60 * 60 * 1000

fun money(v: Double): String = NumberFormat.getCurrencyInstance(PtBr).format(v)

fun formatDate(ms: Long): String = SimpleDateFormat("dd/MM/yyyy", PtBr).format(Date(ms))

/** Converte "1.234,56", "10.50", "R$ 20" etc. em Double. */
fun parseMoney(raw: String): Double? {
    var s = raw.lowercase(PtBr).replace("r$", "").replace(" ", "").trim()
    if (s.isEmpty()) return null
    s = when {
        s.contains(".") && s.contains(",") -> s.replace(".", "").replace(",", ".")
        s.contains(",") -> s.replace(",", ".")
        s.contains(".") && Regex("""^\d{1,3}(\.\d{3})+$""").matches(s) -> s.replace(".", "")
        else -> s
    }
    return s.toDoubleOrNull()
}

/** Aceita dd/MM/yyyy. */
fun parseDate(raw: String): Long? = try {
    val f = SimpleDateFormat("dd/MM/yyyy", PtBr)
    f.isLenient = false
    f.parse(raw.trim())?.time
} catch (e: Exception) {
    null
}

fun startOfToday(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}
