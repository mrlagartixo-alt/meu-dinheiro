package com.example.meudinheiro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
    var paying by remember { mutableStateOf<Bill?>(null) }
    var editingBill by remember { mutableStateOf<Bill?>(null) }

    val pending = bills.filter { !it.paid }
    val paid = bills.filter { it.paid }.sortedByDescending { it.dueDate }
    val today = startOfToday()

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Contas futuras", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Toque em ✔ para pagar (total ou parcial). O valor pago vira gasto essencial.", color = Color.Gray)
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
                    IconButton({ paying = b }) { Icon(Icons.Default.Check, "Pagar", tint = Green) }
                    IconButton({ editingBill = b }) { Icon(Icons.Default.Edit, "Editar") }
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

    paying?.let { b ->
        PayBillDialog(b, onDismiss = { paying = null }, onPay = { v -> vm.payBill(b, v); paying = null })
    }
    editingBill?.let { b ->
        EditBillDialog(b, onDismiss = { editingBill = null }, onSave = { vm.updateBill(it); editingBill = null })
    }
}

@Composable
fun PayBillDialog(b: Bill, onDismiss: () -> Unit, onPay: (Double) -> Unit) {
    var text by remember(b.id) { mutableStateOf(b.amount.toString().replace(".", ",")) }
    val value = parseMoney(text)
    val valid = value != null && value > 0
    val remaining = if (valid) maxOf(0.0, b.amount - value!!) else b.amount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar ${b.description}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Valor da conta: ${money(b.amount)}")
                OutlinedTextField(
                    text, { text = it },
                    label = { Text("Quanto você pagou? (R$)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                if (valid && value!! > b.amount) {
                    Text("O valor é maior que a conta; será registrado ${money(b.amount)}.", color = Amber)
                }
                Text(
                    if (remaining >= 0.01) "Vai restar ${money(remaining)} pendente." else "A conta será quitada.",
                    fontWeight = FontWeight.SemiBold,
                    color = if (remaining >= 0.01) Amber else Green
                )
            }
        },
        confirmButton = { TextButton({ onPay(value!!) }, enabled = valid) { Text("Confirmar pagamento") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EditBillDialog(b: Bill, onDismiss: () -> Unit, onSave: (Bill) -> Unit) {
    var desc by remember(b.id) { mutableStateOf(b.description) }
    var amount by remember(b.id) { mutableStateOf(b.amount.toString().replace(".", ",")) }
    var due by remember(b.id) { mutableStateOf(formatDate(b.dueDate)) }
    val value = parseMoney(amount)
    val date = parseDate(due)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar conta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(desc, { desc = it }, label = { Text("Descrição") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it }, label = { Text("Valor pendente (R$)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(due, { due = it }, label = { Text("Vencimento (dd/mm/aaaa)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(b.copy(description = desc.trim(), amount = value!!, dueDate = date!!)) },
                enabled = value != null && value > 0 && date != null && desc.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}
