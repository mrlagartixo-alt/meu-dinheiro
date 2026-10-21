package com.example.meudinheiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: FinanceViewModel = viewModel()
            MeuDinheiroApp(vm)
        }
    }
}

@Composable
fun MeuDinheiroApp(vm: FinanceViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var only30 by remember { mutableStateOf(false) }
    val list by vm.transactions.collectAsStateWithLifecycle()
    val bills by vm.bills.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = lightColorScheme(primary = Blue, background = Bg, surface = Color.White)
    ) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Resumo") })
                    NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.Chat, null) }, label = { Text("Chat") })
                    NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.List, null) }, label = { Text("Lançam.") })
                    NavigationBarItem(tab == 3, { tab = 3 }, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Contas") })
                }
            }
        ) { p ->
            Box(Modifier.padding(p)) {
                when (tab) {
                    0 -> Dashboard(list, bills, vm, only30) { only30 = it }
                    1 -> ChatScreen(vm)
                    2 -> TransactionsScreen(list, vm, only30) { only30 = it }
                    3 -> BillsScreen(bills, vm)
                }
            }
        }
    }
}
