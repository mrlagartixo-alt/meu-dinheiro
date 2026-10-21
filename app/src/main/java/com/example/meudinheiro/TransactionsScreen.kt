package com.example.meudinheiro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meudinheiro.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun TransactionsScreen(list: List<Transaction>, vm: FinanceViewModel, only30: Boolean, onOnly30: (Boolean) -> Unit) {
    val now = System.currentTimeMillis()
    val filtered = if (only30) list.filter { now - it.date <= 30 * DAY_MS } else list
    var editing by remember { mutableStateOf<Transaction?>(null) }
    var deleting by remember { mutableStateOf<Transaction?>(null) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Lançamentos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(only30, onOnly30)
            Text("Somente últimos 30 dias")
        }
        if (filtered.isEmpty()) Text("Nenhum lançamento.", color = Color.Gray)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { t ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.description, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${t.category} • " + when {
                                    t.type == "INCOME" -> "Entrada"
                                    t.essential -> "Essencial"
                                    else -> "Não essencial"
                                },
                                color = Color.Gray
                            )
                            Text(SimpleDateFormat("dd/MM/yyyy HH:mm", PtBr).format(Date(t.date)), color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                (if (t.type == "INCOME") "+ " else "- ") + money(t.amount),
                                fontWeight = FontWeight.Bold,
                                color = if (t.type == "INCOME") Green else Red
                            )
                            Row {
                                IconButton({ editing = t }) { Icon(Icons.Default.Edit, "Editar") }
                                IconButton({ deleting = t }) { Icon(Icons.Default.Delete, "Excluir") }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { t ->
        EditTransactionDialog(t, onDismiss = { editing = null }, onSave = { vm.update(it); editing = null })
    }
    deleting?.let { t ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Excluir lançamento?") },
            text = { Text("${t.description} — ${money(t.amount)}") },
            confirmButton = { TextButton({ vm.delete(t); deleting = null }) { Text("Excluir") } },
            dismissButton = { TextButton({ deleting = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun EditTransactionDialog(t: Transaction, onDismiss: () -> Unit, onSave: (Transaction) -> Unit) {
    var desc by remember(t.id) { mutableStateOf(t.description) }
    var amount by remember(t.id) { mutableStateOf(t.amount.toString().replace(".", ",")) }
    var category by remember(t.id) { mutableStateOf(t.category) }
    var income by remember(t.id) { mutableStateOf(t.type == "INCOME") }
    var essential by remember(t.id) { mutableStateOf(t.essential) }
    val value = parseMoney(amount)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar lançamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(desc, { desc = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it }, label = { Text("Valor (R$)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text("Categoria") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(!income, { income = false }); Text("Gasto")
                    Spacer(Modifier.width(12.dp))
                    RadioButton(income, { income = true }); Text("Entrada")
                }
                if (!income) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(essential, { essential = it }); Text("Essencial")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        t.copy(
                            description = desc.trim(),
                            amount = value ?: t.amount,
                            category = category.trim().ifEmpty { "Outros" },
                            type = if (income) "INCOME" else "EXPENSE",
                            essential = if (income) true else essential
                        )
                    )
                },
                enabled = value != null && value > 0 && desc.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancelar") } }
    )
}
