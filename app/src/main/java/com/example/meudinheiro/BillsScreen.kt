package com.example.meudinheiro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meudinheiro.data.Bill

@Composable
fun BillsScreen(bills: List<Bill>, vm: FinanceViewModel) {
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val pending = bills.filter { !it.paid }
    val paid = bills.filter { it.paid }.sortedByDescending { it.dueDate }
    val today = startOfToday()

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Contas futuras", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Ao pagar, a conta vira um gasto essencial.", color = Color.Gray)
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(desc, { desc = it }, label = { Text("Descrição (ex.: Aluguel)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(amount, { amount = it }, label = { Text("Valor (R$)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(due, { due = it }, label = { Text("Vencimento (dd/mm/aaaa)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    error?.let { Text(it, color = Red) }
                    Button(
                        onClick = {
                            val v = parseMoney(amount)
                            val d = parseDate(due)
                            when {
                                desc.isBlank() -> error = "Informe a descrição."
                                v == null || v <= 0 -> error = "Valor inválido."
                                d == null -> error = "Data inválida. Use dd/mm/aaaa."
                                else -> {
                                    vm.addBill(Bill(description = desc.trim(), amount = v, dueDate = d))
                                    desc = ""; amount = ""; due = ""; error = null
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Adicionar conta") }
                }
            }
        }
        item { Text("A pagar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (pending.isEmpty()) item { Text("Nenhuma conta pendente.", color = Color.Gray) }
        items(pending, key = { "p${it.id}" }) { b ->
            val overdue = b.dueDate < today
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(b.description, fontWeight = FontWeight.SemiBold)
                        Text(
                            (if (overdue) "Vencida em " else "Vence em ") + formatDate(b.dueDate),
                            color = if (overdue) Red else Color.Gray
                        )
                        Text(money(b.amount), fontWeight = FontWeight.Bold)
                    }
                    IconButton({ vm.payBill(b) }) { Icon(Icons.Default.Check, "Marcar como paga", tint = Green) }
                    IconButton({ vm.deleteBill(b) }) { Icon(Icons.Default.Delete, "Excluir") }
                }
            }
        }
        if (paid.isNotEmpty()) {
            item { Text("Pagas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(paid, key = { "k${it.id}" }) { b ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(b.description)
                            Text("${formatDate(b.dueDate)} • ${money(b.amount)}", color = Color.Gray)
                        }
                        IconButton({ vm.deleteBill(b) }) { Icon(Icons.Default.Delete, "Excluir") }
                    }
                }
            }
        }
    }
}
