package com.example.meudinheiro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen(vm: FinanceViewModel) {
    var text by remember { mutableStateOf("") }
    var essential by remember { mutableStateOf(true) }
    var last by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Fale com seu dinheiro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Escreva como falaria normalmente.", color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Exemplos:", fontWeight = FontWeight.Bold)
                Text("• Gastei 100 reais no mercado")
                Text("• Ganhei 20 reais vendendo bolo")
                Text("• Gastei 35,50 no Uber")
                Text("• Recebi 1.500 de um freela")
                Text("• Gastei 60 no cinema, não essencial")
            }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), label = { Text("Digite seu lançamento") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(essential, { essential = it })
            Text("Marcar como essencial")
        }
        Button(
            onClick = {
                val t = vm.parseAndAdd(text, essential)
                last = if (t != null) "✅ Registrado: ${money(t.amount)} — ${t.category}"
                else "Não consegui identificar o valor. Tente: \"Gastei 100 reais no mercado\"."
                if (t != null) text = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Registrar") }
        last?.let {
            Spacer(Modifier.height(18.dp))
            Text(it, fontWeight = FontWeight.SemiBold)
        }
    }
}
