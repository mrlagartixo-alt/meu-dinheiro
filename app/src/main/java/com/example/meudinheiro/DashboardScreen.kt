package com.example.meudinheiro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meudinheiro.data.Bill
import com.example.meudinheiro.data.Transaction

@Composable
fun Dashboard(
    all: List<Transaction>,
    bills: List<Bill>,
    vm: FinanceViewModel,
    only30: Boolean,
    onOnly30: (Boolean) -> Unit
) {
    var monthlyBudget by remember { mutableStateOf(vm.loadBudget()) }
    var nonEssentialLimit by remember { mutableStateOf(vm.loadNonEssentialLimit()) }

    val now = System.currentTimeMillis()
    val list = if (only30) all.filter { now - it.date <= 30 * DAY_MS } else all
    val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expenses = list.filter { it.type == "EXPENSE" }
    val expense = expenses.sumOf { it.amount }
    val essential = expenses.filter { it.essential }.sumOf { it.amount }
    val nonEssential = expense - essential
    val balance = income - expense

    val budget = parseMoney(monthlyBudget) ?: 0.0
    val nonEssentialBudget = parseMoney(nonEssentialLimit) ?: 0.0
    val remainingBudget = budget - expense
    val remainingNonEssential = nonEssentialBudget - nonEssential

    val pendingSoon = bills.filter { !it.paid && it.dueDate <= now + 30 * DAY_MS }
    val pendingTotal = pendingSoon.sumOf { it.amount }
    val freeBalance = balance - pendingTotal
    val safe = minOf(freeBalance, remainingBudget, remainingNonEssential)

    val cats = expenses.groupBy { it.category }
        .mapValues { e -> e.value.sumOf { it.amount } }
        .toList().sortedByDescending { it.second }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Meu Dinheiro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Seu controle financeiro em uma conversa.", color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(only30, onOnly30)
                Spacer(Modifier.width(10.dp))
                Text("Somente últimos 30 dias")
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text(if (only30) "Saldo (30 dias)" else "Saldo total", color = Color.Gray)
                    Text(money(balance), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Stat("Entradas", money(income), Green)
                        Stat("Gastos", money(expense), Red)
                    }
                }
            }
        }
        item {
            Text("Posso gastar?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        freeBalance < 0 -> Text(
                            "Não. Seu saldo não cobre as contas dos próximos 30 dias.",
                            fontWeight = FontWeight.Bold, color = Red
                        )
                        remainingNonEssential <= 0 || remainingBudget <= 0 -> Text(
                            "Melhor não. Você atingiu o limite do orçamento. Só gaste com o essencial.",
                            fontWeight = FontWeight.Bold, color = Amber
                        )
                        else -> Text(
                            "Sim, até ${money(safe)} em gastos não essenciais.",
                            fontWeight = FontWeight.Bold, color = Green
                        )
                    }
                    Text("Saldo: ${money(balance)}", color = Color.Gray)
                    Text("Contas a pagar (30 dias): ${money(pendingTotal)}", color = Color.Gray)
                    Text("Saldo livre após contas: ${money(freeBalance)}", color = Color.Gray)
                }
            }
        }
        if (pendingSoon.isNotEmpty()) {
            item {
                Text("Próximas contas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pendingSoon.take(4).forEach { b ->
                            val overdue = b.dueDate < startOfToday()
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${b.description} (${formatDate(b.dueDate)})", Modifier.weight(1f), color = if (overdue) Red else Color.Unspecified)
                                Text(money(b.amount), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item {
            Text("Orçamento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Defina quanto pretende gastar no período.")
                    OutlinedTextField(
                        value = monthlyBudget,
                        onValueChange = { monthlyBudget = it; vm.saveBudget(it) },
                        label = { Text("Orçamento total (R$)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nonEssentialLimit,
                        onValueChange = { nonEssentialLimit = it; vm.saveNonEssentialLimit(it) },
                        label = { Text("Limite não essencial (R$)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Restante do orçamento: ${money(remainingBudget)}", fontWeight = FontWeight.Bold,
                        color = if (remainingBudget >= 0) Green else Red)
                    Text("Restante para não essenciais: ${money(remainingNonEssential)}", fontWeight = FontWeight.Bold,
                        color = if (remainingNonEssential >= 0) Green else Red)
                }
            }
        }
        item {
            Text("Essenciais x não essenciais", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp)) {
                    BarRow("Essenciais", essential, expense)
                    Spacer(Modifier.height(10.dp))
                    BarRow("Não essenciais", nonEssential, expense)
                }
            }
        }
        item {
            Text("Entradas x gastos por mês", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp)) { MonthlyBars(all) }
            }
        }
        item {
            Text("Gastos por categoria", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (cats.isEmpty()) {
                        Text("Sem gastos registrados ainda.", color = Color.Gray)
                    } else {
                        PieChart(cats, Modifier.fillMaxWidth().height(180.dp))
                        PieLegend(cats)
                    }
                }
            }
        }
        item {
            Text("💡 Dica: marque gastos como essenciais ou não essenciais para descobrir onde está seu dinheiro de verdade.", color = Color.Gray)
        }
    }
}

@Composable
fun Stat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun BarRow(label: String, value: Double, total: Double) {
    val fraction = if (total > 0) (value / total).toFloat().coerceIn(0f, 1f) else 0f
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(money(value), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(10.dp))
    }
}
