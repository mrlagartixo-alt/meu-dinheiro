package com.example.meudinheiro

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meudinheiro.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val txDao = db.transactionDao()
    private val billDao = db.billDao()
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val transactions: StateFlow<List<Transaction>> = txDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<Bill>> = billDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Orçamento (salvo no aparelho) ----
    fun loadBudget(): String = prefs.getString("budget", "3000") ?: "3000"
    fun loadNonEssentialLimit(): String = prefs.getString("limit", "600") ?: "600"
    fun saveBudget(v: String) { prefs.edit().putString("budget", v).apply() }
    fun saveNonEssentialLimit(v: String) { prefs.edit().putString("limit", v).apply() }

    // ---- Lançamentos ----
    fun add(t: Transaction) = viewModelScope.launch { txDao.insert(t) }
    fun update(t: Transaction) = viewModelScope.launch { txDao.update(t) }
    fun delete(t: Transaction) = viewModelScope.launch { txDao.delete(t) }

    // ---- Contas futuras ----
    fun addBill(b: Bill) = viewModelScope.launch { billDao.insert(b) }
    fun deleteBill(b: Bill) = viewModelScope.launch { billDao.delete(b) }

    fun updateBill(b: Bill) = viewModelScope.launch { billDao.update(b) }

    /**
     * Paga um valor (total ou parcial) de uma conta. Gera a despesa do valor pago
     * e deixa só o restante como pendente.
     */
    fun payBill(b: Bill, value: Double) = viewModelScope.launch {
        val paidValue = Math.round(minOf(value, b.amount) * 100) / 100.0
        val remaining = Math.round((b.amount - paidValue) * 100) / 100.0
        val partial = remaining >= 0.01
        if (partial) billDao.update(b.copy(amount = remaining)) else billDao.update(b.copy(paid = true))
        txDao.insert(
            Transaction(
                description = if (partial) "${b.description} (pagamento parcial)" else b.description,
                amount = paidValue,
                type = "EXPENSE",
                category = "Contas",
                essential = true
            )
        )
    }

    // ---- Linguagem natural ----
    fun parseAndAdd(text: String, essentialDefault: Boolean = true): Transaction? {
        val lower = text.lowercase(PtBr)
        val match = Regex("""\d{1,3}(?:\.\d{3})+(?:,\d{1,2})?|\d+(?:[.,]\d{1,2})?""").find(lower) ?: return null
        val value = parseMoney(match.value) ?: return null

        val incomeWords = listOf("ganhei", "recebi", "recebido", "salário", "salario", "vendi", "venda", "renda", "freela", "entrou")
        val isIncome = incomeWords.any { lower.contains(it) }
        val category = when {
            lower.contains("mercado") || lower.contains("comida") || lower.contains("almoço") || lower.contains("almoco") || lower.contains("lanche") || lower.contains("restaurante") -> "Alimentação"
            lower.contains("uber") || lower.contains("gasolina") || lower.contains("combustível") || lower.contains("combustivel") || lower.contains("ônibus") || lower.contains("onibus") -> "Transporte"
            lower.contains("aluguel") || lower.contains("casa") || lower.contains("luz") || lower.contains("água") || lower.contains("agua") || lower.contains("internet") -> "Moradia"
            lower.contains("netflix") || lower.contains("spotify") || lower.contains("assinatura") -> "Assinaturas"
            lower.contains("bolo") || lower.contains("freela") || lower.contains("salário") || lower.contains("salario") -> "Renda"
            lower.contains("jogo") || lower.contains("cinema") || lower.contains("lazer") -> "Lazer"
            lower.contains("farmácia") || lower.contains("farmacia") || lower.contains("remédio") || lower.contains("remedio") || lower.contains("médico") || lower.contains("medico") -> "Saúde"
            else -> if (isIncome) "Outras receitas" else "Outros"
        }
        val notEssential = listOf("não essencial", "nao essencial", "supérfluo", "superfluo").any { lower.contains(it) }
        val essential = when {
            isIncome -> true
            notEssential -> false
            else -> essentialDefault
        }
        val t = Transaction(
            description = text.trim().replaceFirstChar { it.uppercase() },
            amount = value,
            type = if (isIncome) "INCOME" else "EXPENSE",
            category = category,
            essential = essential
        )
        add(t)
        return t
    }
}
